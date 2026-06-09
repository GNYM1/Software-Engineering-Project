package com.reservation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.common.BusinessException;
import com.reservation.entity.Seat;
import com.reservation.entity.StudyRoom;
import com.reservation.mapper.SeatMapper;
import com.reservation.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService extends ServiceImpl<SeatMapper, Seat> {

    private final StudyRoomMapper studyRoomMapper;

    /**
     * 查询指定自习室的所有座位（按行列排序，方便前端布局展示）
     */
    public List<Seat> getSeatsByRoom(Long roomId) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        return lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNum)
                .orderByAsc(Seat::getColNum)
                .list();
    }

    /**
     * 新增座位（管理员）
     */
    @Transactional
    public Seat createSeat(Long roomId, Seat seat) {
        StudyRoom room = studyRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        validateSeatInput(seat);
        boolean exists = lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .eq(Seat::getSeatNumber, seat.getSeatNumber())
                .count() > 0;
        if (exists) {
            throw new BusinessException("该自习室下座位编号已存在");
        }

        seat.setRoomId(roomId);
        if (!StringUtils.hasText(seat.getStatus())) {
            seat.setStatus("空闲");
        }
        if (!StringUtils.hasText(seat.getSeatType())) {
            seat.setSeatType("普通");
        }
        save(seat);

        // 更新自习室的座位总数
        long total = lambdaQuery().eq(Seat::getRoomId, roomId).count();
        room.setTotalSeats((int) total);
        studyRoomMapper.updateById(room);

        return seat;
    }

    /**
     * 修改座位信息（管理员）
     */
    @Transactional
    public Seat updateSeat(Long seatId, Seat input) {
        Seat seat = getById(seatId);
        if (seat == null) {
            throw new BusinessException(404, "座位不存在");
        }
        if (StringUtils.hasText(input.getSeatNumber())) {
            boolean numConflict = lambdaQuery()
                    .eq(Seat::getRoomId, seat.getRoomId())
                    .eq(Seat::getSeatNumber, input.getSeatNumber())
                    .ne(Seat::getSeatId, seatId)
                    .count() > 0;
            if (numConflict) {
                throw new BusinessException("该自习室下座位编号已存在");
            }
            seat.setSeatNumber(input.getSeatNumber());
        }
        if (StringUtils.hasText(input.getSeatType())) {
            seat.setSeatType(input.getSeatType());
        }
        if (StringUtils.hasText(input.getQrCode())) {
            seat.setQrCode(input.getQrCode());
        }
        if (input.getRowNum() != null) {
            seat.setRowNum(input.getRowNum());
        }
        if (input.getColNum() != null) {
            seat.setColNum(input.getColNum());
        }
        if (StringUtils.hasText(input.getStatus())) {
            seat.setStatus(input.getStatus());
        }
        updateById(seat);
        return seat;
    }

    /**
     * 删除座位（管理员）
     */
    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = getById(seatId);
        if (seat == null) {
            throw new BusinessException(404, "座位不存在");
        }
        // 检查有无正在进行中的预约
        long activeCount = lambdaQuery()
                .eq(Seat::getSeatId, seatId)
                .in(Seat::getStatus, "已预约", "使用中")
                .count();
        if (activeCount > 0) {
            throw new BusinessException("该座位有进行中的预约，暂不可删除");
        }
        removeById(seatId);

        // 更新自习室的座位总数
        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
        if (room != null) {
            long total = lambdaQuery().eq(Seat::getRoomId, seat.getRoomId()).count();
            room.setTotalSeats((int) total);
            studyRoomMapper.updateById(room);
        }
    }

    /**
     * 批量创建座位（管理员）
     */
    @Transactional
    public List<Seat> batchCreateSeats(Long roomId, List<Seat> seats) {
        for (Seat seat : seats) {
            createSeat(roomId, seat);
        }
        return getSeatsByRoom(roomId);
    }

    private void validateSeatInput(Seat seat) {
        if (seat == null || !StringUtils.hasText(seat.getSeatNumber())) {
            throw new BusinessException("座位编号不能为空");
        }
    }
}
