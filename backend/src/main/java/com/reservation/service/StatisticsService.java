package com.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reservation.entity.Reservation;
import com.reservation.entity.User;
import com.reservation.entity.ViolationRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ReservationService reservationService;
    private final SeatService seatService;
    private final UserService userService;
    private final ViolationRecordService violationRecordService;

    public Map<String, Object> usage(LocalDate start, LocalDate end) {
        LocalDate[] range = normalizeRange(start, end);
        long totalSeats = seatService.count();
        long totalReservations = reservationService.count(new LambdaQueryWrapper<Reservation>()
                .between(Reservation::getReserveDate, range[0], range[1])
                .notIn(Reservation::getStatus, "已取消", "已违约"));
        long finishedReservations = reservationService.count(new LambdaQueryWrapper<Reservation>()
                .between(Reservation::getReserveDate, range[0], range[1])
                .in(Reservation::getStatus, "使用中", "已完成"));

        Map<String, Object> data = new HashMap<>();
        data.put("startDate", range[0]);
        data.put("endDate", range[1]);
        data.put("totalSeats", totalSeats);
        data.put("totalReservations", totalReservations);
        data.put("finishedReservations", finishedReservations);
        data.put("usageRate", totalReservations == 0 ? 0 : round(finishedReservations * 100.0 / totalReservations));
        data.put("seatBookingRate", totalSeats == 0 ? 0 : round(totalReservations * 100.0 / totalSeats));
        return data;
    }

    public Map<String, Object> activeUsers(LocalDate start, LocalDate end) {
        LocalDate[] range = normalizeRange(start, end);
        List<Reservation> reservations = reservationService.list(new LambdaQueryWrapper<Reservation>()
                .between(Reservation::getReserveDate, range[0], range[1]));
        Map<Long, Long> userCounts = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getUserId, Collectors.counting()));

        List<Map<String, Object>> topUsers = userCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    User user = userService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", entry.getKey());
                    item.put("username", user == null ? null : user.getUsername());
                    item.put("reservationCount", entry.getValue());
                    return item;
                })
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("activeUserCount", userCounts.size());
        data.put("reservationCount", reservations.size());
        data.put("topUsers", topUsers);
        return data;
    }

    public Map<String, Object> peakHours(LocalDate start, LocalDate end) {
        LocalDate[] range = normalizeRange(start, end);
        List<Reservation> reservations = reservationService.list(new LambdaQueryWrapper<Reservation>()
                .between(Reservation::getReserveDate, range[0], range[1]));
        Map<String, Long> slotCounts = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getTimeSlot, LinkedHashMap::new, Collectors.counting()));

        Map<String, Object> data = new HashMap<>();
        data.put("startDate", range[0]);
        data.put("endDate", range[1]);
        data.put("timeSlots", slotCounts);
        return data;
    }

    public Map<String, Object> violations(LocalDate start, LocalDate end) {
        LocalDate[] range = normalizeRange(start, end);
        long totalReservations = reservationService.count(new LambdaQueryWrapper<Reservation>()
                .between(Reservation::getReserveDate, range[0], range[1]));
        List<ViolationRecord> records = violationRecordService.list(new LambdaQueryWrapper<ViolationRecord>()
                .between(ViolationRecord::getViolationDate, range[0], range[1]));
        Map<String, Long> typeCounts = records.stream()
                .collect(Collectors.groupingBy(ViolationRecord::getViolationType, Collectors.counting()));

        Map<String, Object> data = new HashMap<>();
        data.put("violationCount", records.size());
        data.put("totalReservations", totalReservations);
        data.put("violationRate", totalReservations == 0 ? 0 : round(records.size() * 100.0 / totalReservations));
        data.put("types", typeCounts);
        data.put("records", records);
        return data;
    }

    private LocalDate[] normalizeRange(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        LocalDate from = start == null ? today.minusDays(6) : start;
        LocalDate to = end == null ? today : end;
        if (from.isAfter(to)) {
            return new LocalDate[]{to, from};
        }
        return new LocalDate[]{from, to};
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
