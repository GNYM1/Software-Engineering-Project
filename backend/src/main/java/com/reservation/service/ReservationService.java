package com.reservation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.entity.Reservation;
import com.reservation.mapper.ReservationMapper;
import org.springframework.stereotype.Service;

@Service
public class ReservationService extends ServiceImpl<ReservationMapper, Reservation> {
    // TODO: 预约、取消、签到、签退、违约判定
}
