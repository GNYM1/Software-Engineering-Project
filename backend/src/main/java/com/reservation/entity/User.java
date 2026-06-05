package com.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String phone;
    private String role;        // student / admin
    private Integer status;     // 0禁用 / 1正常
    private Integer violationCount;
    private LocalDateTime banUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
