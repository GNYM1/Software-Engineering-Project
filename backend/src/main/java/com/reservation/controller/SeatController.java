package com.reservation.controller;

import com.reservation.common.BusinessException;
import com.reservation.common.Result;
import com.reservation.entity.Seat;
import com.reservation.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 获取指定自习室的座位布局
     * GET /api/rooms/{roomId}/seats
     */
    @GetMapping("/rooms/{roomId}/seats")
    public Result<List<Seat>> listByRoom(@PathVariable Long roomId) {
        return Result.ok(seatService.getSeatsByRoom(roomId));
    }

    /**
     * 获取单个座位详情
     * GET /api/seats/{id}
     */
    @GetMapping("/seats/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        Seat seat = seatService.getById(id);
        if (seat == null) {
            throw new BusinessException(404, "座位不存在");
        }
        return Result.ok(seat);
    }

    /**
     * 新增座位（管理员）
     * POST /api/rooms/{roomId}/seats
     */
    @PostMapping("/rooms/{roomId}/seats")
    public Result<Seat> create(@PathVariable Long roomId, @RequestBody Seat seat, HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(seatService.createSeat(roomId, seat));
    }

    /**
     * 修改座位（管理员）
     * PUT /api/seats/{id}
     */
    @PutMapping("/seats/{id}")
    public Result<Seat> update(@PathVariable Long id, @RequestBody Seat seat, HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(seatService.updateSeat(id, seat));
    }

    /**
     * 删除座位（管理员）
     * DELETE /api/seats/{id}
     */
    @DeleteMapping("/seats/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        seatService.deleteSeat(id);
        return Result.ok();
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            throw new BusinessException(403, "无权限，仅管理员可操作");
        }
    }
}
