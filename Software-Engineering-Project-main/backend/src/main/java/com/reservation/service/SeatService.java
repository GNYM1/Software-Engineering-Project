package com.reservation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.entity.Seat;
import com.reservation.mapper.SeatMapper;
import org.springframework.stereotype.Service;

@Service
public class SeatService extends ServiceImpl<SeatMapper, Seat> {
    // TODO: 座位CRUD、状态查询
}
