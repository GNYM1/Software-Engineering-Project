package com.reservation.controller;

import com.reservation.common.BusinessException;
import com.reservation.common.Result;
import com.reservation.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/usage")
    public Result<Map<String, Object>> usage(HttpServletRequest request,
                                             @RequestParam(required = false) LocalDate start,
                                             @RequestParam(required = false) LocalDate end) {
        requireAdmin(request);
        return Result.ok(statisticsService.usage(start, end));
    }

    @GetMapping("/active-users")
    public Result<Map<String, Object>> activeUsers(HttpServletRequest request,
                                                   @RequestParam(required = false) LocalDate start,
                                                   @RequestParam(required = false) LocalDate end) {
        requireAdmin(request);
        return Result.ok(statisticsService.activeUsers(start, end));
    }

    @GetMapping("/peak-hours")
    public Result<Map<String, Object>> peakHours(HttpServletRequest request,
                                                 @RequestParam(required = false) LocalDate start,
                                                 @RequestParam(required = false) LocalDate end) {
        requireAdmin(request);
        return Result.ok(statisticsService.peakHours(start, end));
    }

    @GetMapping("/violations")
    public Result<Map<String, Object>> violations(HttpServletRequest request,
                                                  @RequestParam(required = false) LocalDate start,
                                                  @RequestParam(required = false) LocalDate end) {
        requireAdmin(request);
        return Result.ok(statisticsService.violations(start, end));
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!"admin".equals(request.getAttribute("role"))) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
