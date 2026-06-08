package com.reservation.dto;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageDto {
    private Integer page = 1;
    private Integer size = 10;
}
