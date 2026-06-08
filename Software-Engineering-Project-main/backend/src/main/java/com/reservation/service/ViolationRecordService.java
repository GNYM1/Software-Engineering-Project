package com.reservation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reservation.entity.ViolationRecord;
import com.reservation.mapper.ViolationRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class ViolationRecordService extends ServiceImpl<ViolationRecordMapper, ViolationRecord> {
}
