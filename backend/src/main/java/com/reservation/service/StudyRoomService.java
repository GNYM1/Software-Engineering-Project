package com.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.common.BusinessException;
import com.reservation.entity.Seat;
import com.reservation.entity.StudyRoom;
import com.reservation.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyRoomService extends ServiceImpl<StudyRoomMapper, StudyRoom> {

    private final SeatService seatService;

    /**
     * 查询自习室列表，支持按状态筛选
     */
    public List<StudyRoom> listRooms(Integer status) {
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(StudyRoom::getStatus, status);
        }
        wrapper.orderByAsc(StudyRoom::getRoomName);
        return list(wrapper);
    }

    /**
     * 创建自习室（管理员）
     */
    @Transactional
    public StudyRoom createRoom(StudyRoom room) {
        validateRoomInput(room);
        boolean exists = lambdaQuery().eq(StudyRoom::getRoomName, room.getRoomName()).count() > 0;
        if (exists) {
            throw new BusinessException("自习室名称已存在");
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        if (room.getTotalSeats() == null) {
            room.setTotalSeats(0);
        }
        save(room);
        return room;
    }

    /**
     * 修改自习室信息（管理员）
     */
    @Transactional
    public StudyRoom updateRoom(Long roomId, StudyRoom input) {
        StudyRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        if (StringUtils.hasText(input.getRoomName())) {
            boolean nameConflict = lambdaQuery()
                    .eq(StudyRoom::getRoomName, input.getRoomName())
                    .ne(StudyRoom::getRoomId, roomId)
                    .count() > 0;
            if (nameConflict) {
                throw new BusinessException("自习室名称已存在");
            }
            room.setRoomName(input.getRoomName());
        }
        if (StringUtils.hasText(input.getLocation())) {
            room.setLocation(input.getLocation());
        }
        if (input.getTotalSeats() != null && input.getTotalSeats() >= 0) {
            room.setTotalSeats(input.getTotalSeats());
        }
        if (input.getOpenTime() != null) {
            room.setOpenTime(input.getOpenTime());
        }
        if (input.getCloseTime() != null) {
            room.setCloseTime(input.getCloseTime());
        }
        if (StringUtils.hasText(input.getDescription())) {
            room.setDescription(input.getDescription());
        }
        updateById(room);
        return room;
    }

    /**
     * 删除自习室（管理员）
     * 同时删除该自习室下的所有座位
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        StudyRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        long seatCount = seatService.lambdaQuery().eq(Seat::getRoomId, roomId).count();
        if (seatCount > 0) {
            seatService.lambdaUpdate().eq(Seat::getRoomId, roomId).remove();
        }
        removeById(roomId);
    }

    /**
     * 切换自习室开放/关闭状态（管理员）
     */
    @Transactional
    public StudyRoom toggleStatus(Long roomId) {
        StudyRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        room.setStatus(room.getStatus() != null && room.getStatus() == 1 ? 0 : 1);
        updateById(room);
        return room;
    }

    private void validateRoomInput(StudyRoom room) {
        if (room == null || !StringUtils.hasText(room.getRoomName())) {
            throw new BusinessException("自习室名称不能为空");
        }
        if (!StringUtils.hasText(room.getLocation())) {
            throw new BusinessException("位置信息不能为空");
        }
    }
}
