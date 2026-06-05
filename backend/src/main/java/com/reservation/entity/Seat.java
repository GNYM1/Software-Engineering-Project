package com.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seats")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long seatId;
    private Long roomId;
    private String seatNumber;
    private String seatType;    // 普通 / 靠窗 / 插座
    private String qrCode;
    private String status;      // 空闲 / 已预约 / 使用中 / 维护中
    private Integer rowNum;
    private Integer colNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
