package com.reservation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.entity.User;
import com.reservation.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    // TODO: 注册、登录、个人信息管理
}
