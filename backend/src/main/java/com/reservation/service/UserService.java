package com.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.common.BusinessException;
import com.reservation.dto.LoginDto;
import com.reservation.entity.User;
import com.reservation.entity.ViolationRecord;
import com.reservation.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final ViolationRecordService violationRecordService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public User register(User user) {
        if (user == null || !StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        boolean exists = lambdaQuery().eq(User::getUsername, user.getUsername()).count() > 0;
        if (exists) {
            throw new BusinessException("用户名已存在");
        }

        User entity = new User();
        entity.setUsername(user.getUsername());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setPhone(user.getPhone());
        entity.setRole("student");
        entity.setStatus(1);
        entity.setViolationCount(0);
        save(entity);
        return safeUser(entity);
    }

    public Map<String, Object> login(LoginDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()), false);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(key)
                .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", safeUser(user));
        return data;
    }

    public User profile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return safeUser(user);
    }

    public User updateProfile(Long userId, User input) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (input != null && StringUtils.hasText(input.getPhone())) {
            user.setPhone(input.getPhone());
        }
        updateById(user);
        return safeUser(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new BusinessException("原密码和新密码不能为空");
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    public List<ViolationRecord> violations(Long userId) {
        return violationRecordService.lambdaQuery()
                .eq(ViolationRecord::getUserId, userId)
                .orderByDesc(ViolationRecord::getCreatedAt)
                .list();
    }

    public void clearExpiredBan(User user) {
        if (user.getBanUntil() != null && user.getBanUntil().isBefore(LocalDateTime.now())) {
            user.setBanUntil(null);
            user.setViolationCount(0);
            updateById(user);
        }
    }

    private User safeUser(User source) {
        User user = new User();
        user.setUserId(source.getUserId());
        user.setUsername(source.getUsername());
        user.setPhone(source.getPhone());
        user.setRole(source.getRole());
        user.setStatus(source.getStatus());
        user.setViolationCount(source.getViolationCount());
        user.setBanUntil(source.getBanUntil());
        user.setCreatedAt(source.getCreatedAt());
        user.setUpdatedAt(source.getUpdatedAt());
        return user;
    }
}
