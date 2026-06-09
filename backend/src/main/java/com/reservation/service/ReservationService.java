package com.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.common.BusinessException;
import com.reservation.dto.ReserveDto;
import com.reservation.entity.Reservation;
import com.reservation.entity.Seat;
import com.reservation.entity.User;
import com.reservation.entity.ViolationRecord;
import com.reservation.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService extends ServiceImpl<ReservationMapper, Reservation> {

    private static final String RESERVED = "已预约";
    private static final String USING = "使用中";
    private static final String FINISHED = "已完成";
    private static final String CANCELED = "已取消";
    private static final String VIOLATED = "已违约";
    private static final String SEAT_FREE = "空闲";
    private static final String SEAT_RESERVED = "已预约";
    private static final String SEAT_USING = "使用中";
    private static final String SEAT_MAINTAINING = "维护中";

    private final UserService userService;
    private final SeatService seatService;
    private final ViolationRecordService violationRecordService;

    @Value("${reservation.rules.max-per-day}")
    private Integer maxPerDay;

    @Value("${reservation.rules.max-duration}")
    private Integer maxDurationHours;

    @Value("${reservation.rules.cancel-deadline}")
    private Integer cancelDeadlineMinutes;

    @Value("${reservation.rules.checkin-window}")
    private Integer checkinWindowMinutes;

    @Value("${reservation.rules.open-time}")
    private String openTime;

    @Value("${reservation.rules.close-time}")
    private String closeTime;

    @Value("${reservation.rules.violation-limit}")
    private Integer violationLimit;

    @Value("${reservation.rules.ban-days}")
    private Integer banDays;

    @Transactional
    public Reservation reserve(Long userId, ReserveDto dto) {
        User user = requireUsableUser(userId);
        validateReserveDto(dto);

        Seat seat = seatService.getById(dto.getSeatId());
        if (seat == null) {
            throw new BusinessException(404, "座位不存在");
        }
        if (SEAT_MAINTAINING.equals(seat.getStatus())) {
            throw new BusinessException("座位维护中，暂不可预约");
        }

        long todayCount = lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getReserveDate, dto.getReserveDate())
                .notIn(Reservation::getStatus, CANCELED, VIOLATED)
                .count();
        if (todayCount >= maxPerDay) {
            throw new BusinessException("今日预约次数已达上限");
        }

        boolean seatConflict = hasConflict(dto.getSeatId(), dto.getStartTime(), dto.getEndTime(), null);
        if (seatConflict) {
            throw new BusinessException("该座位在所选时段已被预约");
        }

        boolean userConflict = lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus, RESERVED, USING)
                .lt(Reservation::getStartTime, dto.getEndTime())
                .gt(Reservation::getEndTime, dto.getStartTime())
                .count() > 0;
        if (userConflict) {
            throw new BusinessException("您在该时段已有其他预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getUserId());
        reservation.setSeatId(dto.getSeatId());
        reservation.setReserveDate(dto.getReserveDate());
        reservation.setTimeSlot(StringUtils.hasText(dto.getTimeSlot()) ? dto.getTimeSlot() : inferTimeSlot(dto.getStartTime().toLocalTime()));
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setStatus(RESERVED);
        save(reservation);

        seat.setStatus(SEAT_RESERVED);
        seatService.updateById(seat);
        return reservation;
    }

    @Transactional
    public Reservation cancel(Long operatorId, String role, Long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        assertOwnerOrAdmin(operatorId, role, reservation);
        if (!RESERVED.equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态不可取消");
        }
        if (LocalDateTime.now().isAfter(reservation.getStartTime().minusMinutes(cancelDeadlineMinutes))) {
            throw new BusinessException("预约开始前30分钟内不可取消");
        }

        reservation.setStatus(CANCELED);
        updateById(reservation);
        releaseSeat(reservation.getSeatId());
        return reservation;
    }

    @Transactional
    public Reservation checkin(Long operatorId, String role, Long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        assertOwnerOrAdmin(operatorId, role, reservation);
        if (!RESERVED.equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态不可签到");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliest = reservation.getStartTime().minusMinutes(checkinWindowMinutes);
        LocalDateTime latest = reservation.getStartTime().plusMinutes(checkinWindowMinutes);
        if (now.isBefore(earliest)) {
            throw new BusinessException("尚未到签到时间");
        }
        if (now.isAfter(latest)) {
            markViolation(reservation, "迟到未签到", "超过签到窗口未完成签到");
            throw new BusinessException("签到超时，已记录违约");
        }

        reservation.setCheckinTime(now);
        reservation.setStatus(USING);
        updateById(reservation);

        Seat seat = seatService.getById(reservation.getSeatId());
        if (seat != null) {
            seat.setStatus(SEAT_USING);
            seatService.updateById(seat);
        }
        resetViolationCount(reservation.getUserId());
        return reservation;
    }

    @Transactional
    public Reservation checkout(Long operatorId, String role, Long reservationId) {
        Reservation reservation = requireReservation(reservationId);
        assertOwnerOrAdmin(operatorId, role, reservation);
        if (!USING.equals(reservation.getStatus())) {
            throw new BusinessException("当前预约状态不可签退");
        }

        reservation.setCheckoutTime(LocalDateTime.now());
        reservation.setStatus(FINISHED);
        updateById(reservation);
        releaseSeat(reservation.getSeatId());
        resetViolationCount(reservation.getUserId());
        return reservation;
    }

    public List<Reservation> myReservations(Long userId) {
        return lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getStartTime)
                .list();
    }

    public List<Reservation> allReservations(String status, LocalDate date) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(StringUtils.hasText(status), Reservation::getStatus, status)
                .eq(date != null, Reservation::getReserveDate, date)
                .orderByDesc(Reservation::getStartTime);
        return list(wrapper);
    }

    @Transactional
    public int judgeViolations() {
        List<Reservation> timeoutReservations = lambdaQuery()
                .eq(Reservation::getStatus, RESERVED)
                .lt(Reservation::getStartTime, LocalDateTime.now().minusMinutes(checkinWindowMinutes))
                .list();
        for (Reservation reservation : timeoutReservations) {
            markViolation(reservation, "迟到未签到", "系统自动判定超时未签到");
        }
        return timeoutReservations.size();
    }

    @Transactional
    @Scheduled(fixedDelay = 300000)
    public void autoJudgeViolations() {
        judgeViolations();
    }

    private User requireUsableUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        userService.clearExpiredBan(user);
        if (user.getBanUntil() != null && user.getBanUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403, "连续违约处罚中，暂不可预约");
        }
        return user;
    }

    private void validateReserveDto(ReserveDto dto) {
        if (dto == null || dto.getSeatId() == null || dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BusinessException("预约座位和时间不能为空");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException("预约开始时间必须早于结束时间");
        }
        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能预约过去的时间");
        }

        LocalDate reserveDate = dto.getReserveDate() == null ? dto.getStartTime().toLocalDate() : dto.getReserveDate();
        if (!reserveDate.equals(dto.getStartTime().toLocalDate()) || !reserveDate.equals(dto.getEndTime().toLocalDate())) {
            throw new BusinessException("预约必须在同一天内完成");
        }
        dto.setReserveDate(reserveDate);

        long minutes = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes();
        if (minutes > maxDurationHours * 60L || minutes <= 0) {
            throw new BusinessException("单次预约时长不符合规则");
        }

        LocalTime start = dto.getStartTime().toLocalTime();
        LocalTime end = dto.getEndTime().toLocalTime();
        if (start.isBefore(LocalTime.parse(openTime)) || end.isAfter(LocalTime.parse(closeTime))) {
            throw new BusinessException("预约时间不在自习室开放时段内");
        }
    }

    private boolean hasConflict(Long seatId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        return lambdaQuery()
                .eq(Reservation::getSeatId, seatId)
                .in(Reservation::getStatus, RESERVED, USING)
                .ne(excludeId != null, Reservation::getReservationId, excludeId)
                .lt(Reservation::getStartTime, endTime)
                .gt(Reservation::getEndTime, startTime)
                .count() > 0;
    }

    private Reservation requireReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        return reservation;
    }

    private void assertOwnerOrAdmin(Long operatorId, String role, Reservation reservation) {
        if (!reservation.getUserId().equals(operatorId) && !"admin".equals(role)) {
            throw new BusinessException(403, "无权操作该预约");
        }
    }

    private void markViolation(Reservation reservation, String type, String description) {
        if (VIOLATED.equals(reservation.getStatus())) {
            return;
        }

        reservation.setStatus(VIOLATED);
        updateById(reservation);
        releaseSeat(reservation.getSeatId());

        User user = userService.getById(reservation.getUserId());
        int count = (user.getViolationCount() == null ? 0 : user.getViolationCount()) + 1;
        user.setViolationCount(count);
        String penalty = null;
        if (count >= violationLimit) {
            user.setBanUntil(LocalDateTime.now().plusDays(banDays));
            penalty = "暂停预约权限" + banDays + "天";
        }
        userService.updateById(user);

        ViolationRecord record = new ViolationRecord();
        record.setUserId(reservation.getUserId());
        record.setReservationId(reservation.getReservationId());
        record.setViolationType(type);
        record.setViolationDate(LocalDate.now());
        record.setPenalty(penalty);
        record.setDescription(description);
        violationRecordService.save(record);
    }

    private void resetViolationCount(Long userId) {
        User user = userService.getById(userId);
        if (user != null && user.getViolationCount() != null && user.getViolationCount() > 0) {
            user.setViolationCount(0);
            userService.updateById(user);
        }
    }

    private void releaseSeat(Long seatId) {
        Seat seat = seatService.getById(seatId);
        if (seat != null) {
            seat.setStatus(SEAT_FREE);
            seatService.updateById(seat);
        }
    }

    private String inferTimeSlot(LocalTime time) {
        if (time.isBefore(LocalTime.NOON)) {
            return "上午";
        }
        if (time.isBefore(LocalTime.of(18, 0))) {
            return "下午";
        }
        return "晚上";
    }
}
