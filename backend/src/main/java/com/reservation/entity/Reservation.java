package com.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reservations")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long reservationId;
    private Long userId;
    private Long seatId;
    private LocalDate reserveDate;
    private String timeSlot;    // 上午 / 下午 / 晚上
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String status;      // 已预约 / 使用中 / 已完成 / 已取消 / 已违约
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
