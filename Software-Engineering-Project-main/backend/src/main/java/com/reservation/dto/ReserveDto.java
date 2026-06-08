package com.reservation.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约请求
 */
@Data
public class ReserveDto {
    private Long seatId;
    private LocalDate reserveDate;
    private String timeSlot;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
