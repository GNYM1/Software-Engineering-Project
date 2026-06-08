package com.reservation.controller;

import com.reservation.common.BusinessException;
import com.reservation.common.Result;
import com.reservation.dto.ReserveDto;
import com.reservation.entity.Reservation;
import com.reservation.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public Result<Reservation> reserve(HttpServletRequest request, @RequestBody ReserveDto dto) {
        return Result.ok(reservationService.reserve(currentUserId(request), dto));
    }

    @GetMapping("/my")
    public Result<List<Reservation>> myReservations(HttpServletRequest request) {
        return Result.ok(reservationService.myReservations(currentUserId(request)));
    }

    @DeleteMapping("/{id}")
    public Result<Reservation> cancel(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(reservationService.cancel(currentUserId(request), currentRole(request), id));
    }

    @PostMapping("/{id}/checkin")
    public Result<Reservation> checkin(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(reservationService.checkin(currentUserId(request), currentRole(request), id));
    }

    @PostMapping("/{id}/checkout")
    public Result<Reservation> checkout(HttpServletRequest request, @PathVariable Long id) {
        return Result.ok(reservationService.checkout(currentUserId(request), currentRole(request), id));
    }

    @GetMapping("/all")
    public Result<List<Reservation>> all(HttpServletRequest request,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) LocalDate date) {
        requireAdmin(request);
        return Result.ok(reservationService.allReservations(status, date));
    }

    @PostMapping("/violations/judge")
    public Result<Integer> judgeViolations(HttpServletRequest request) {
        requireAdmin(request);
        return Result.ok(reservationService.judgeViolations());
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String currentRole(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!"admin".equals(currentRole(request))) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
