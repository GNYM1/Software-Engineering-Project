package com.reservation.controller;

import com.reservation.common.BusinessException;
import com.reservation.common.Result;
import com.reservation.entity.StudyRoom;
import com.reservation.service.StudyRoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    /**
     * 获取自习室列表（可选按状态筛选）
     * GET /api/rooms?status=1
     */
    @GetMapping
    public Result<List<StudyRoom>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(studyRoomService.listRooms(status));
    }

    /**
     * 获取单个自习室详情
     * GET /api/rooms/{id}
     */
    @GetMapping("/{id}")
    public Result<StudyRoom> getById(@PathVariable Long id) {
        StudyRoom room = studyRoomService.getById(id);
        if (room == null) {
            throw new BusinessException(404, "自习室不存在");
        }
        return Result.ok(room);
    }

    /**
     * 新增自习室（管理员）
     * POST /api/rooms
     */
    @PostMapping
    public Result<StudyRoom> create(@RequestBody StudyRoom room, HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(studyRoomService.createRoom(room));
    }

    /**
     * 修改自习室（管理员）
     * PUT /api/rooms/{id}
     */
    @PutMapping("/{id}")
    public Result<StudyRoom> update(@PathVariable Long id, @RequestBody StudyRoom room, HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(studyRoomService.updateRoom(id, room));
    }

    /**
     * 删除自习室（管理员）
     * DELETE /api/rooms/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        studyRoomService.deleteRoom(id);
        return Result.ok();
    }

    /**
     * 切换自习室开放/关闭状态（管理员）
     * PUT /api/rooms/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<StudyRoom> toggleStatus(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(studyRoomService.toggleStatus(id));
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            throw new BusinessException(403, "无权限，仅管理员可操作");
        }
    }
}
