package com.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("study_rooms")
public class StudyRoom {
    @TableId(type = IdType.AUTO)
    private Long roomId;
    private String roomName;
    private String location;
    private Integer totalSeats;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer status;     // 0关闭 / 1开放
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
