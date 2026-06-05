package com.reservation.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginDto {
    private String username;
    private String password;
}
