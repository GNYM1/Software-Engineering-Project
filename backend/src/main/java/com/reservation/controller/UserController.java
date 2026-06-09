package com.reservation.controller;

import com.reservation.common.Result;
import com.reservation.dto.LoginDto;
import com.reservation.entity.User;
import com.reservation.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return Result.ok(userService.register(user));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto dto) {
        return Result.ok(userService.login(dto));
    }

    @GetMapping("/profile")
    public Result<User> profile(HttpServletRequest request) {
        return Result.ok(userService.profile(currentUserId(request)));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(HttpServletRequest request, @RequestBody User user) {
        return Result.ok(userService.updateProfile(currentUserId(request), user));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        userService.changePassword(currentUserId(request), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    @GetMapping("/violations")
    public Result<?> violations(HttpServletRequest request) {
        return Result.ok(userService.violations(currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
