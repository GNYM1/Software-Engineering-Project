package com.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("violation_records")
public class ViolationRecord {
    @TableId(type = IdType.AUTO)
    private Long violationId;
    private Long userId;
    private Long reservationId;
    private String violationType;   // 迟到未签到 / 提前离席
    private LocalDate violationDate;
    private String penalty;
    private String description;
    private LocalDateTime createdAt;
}
