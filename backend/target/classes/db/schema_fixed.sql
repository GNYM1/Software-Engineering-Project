SET NAMES utf8mb4;

DROP DATABASE IF EXISTS seat_reservation;

CREATE DATABASE seat_reservation
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE seat_reservation;

CREATE TABLE users (
    user_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(20)   NOT NULL COMMENT '用户名',
    password        VARCHAR(100)  NOT NULL COMMENT '密码',
    phone           VARCHAR(15)   DEFAULT NULL COMMENT '手机号',
    role            VARCHAR(10)   NOT NULL DEFAULT 'student' COMMENT '角色',
    status          TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '状态',
    violation_count INT           NOT NULL DEFAULT 0 COMMENT '连续违约次数',
    ban_until       DATETIME      DEFAULT NULL COMMENT '处罚截止时间',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE study_rooms (
    room_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自习室ID',
    room_name       VARCHAR(50)   NOT NULL COMMENT '自习室名称',
    location        VARCHAR(100)  NOT NULL COMMENT '位置信息',
    total_seats     INT           NOT NULL DEFAULT 0 COMMENT '总座位数',
    open_time       TIME          NOT NULL DEFAULT '07:00:00' COMMENT '开放时间',
    close_time      TIME          NOT NULL DEFAULT '22:00:00' COMMENT '关闭时间',
    status          TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '状态',
    description     VARCHAR(255)  DEFAULT NULL COMMENT '描述信息',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (room_id),
    UNIQUE KEY uk_room_name (room_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自习室表';

CREATE TABLE seats (
    seat_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    room_id         BIGINT        NOT NULL COMMENT '所属自习室ID',
    seat_number     VARCHAR(10)   NOT NULL COMMENT '座位编号',
    seat_type       VARCHAR(20)   NOT NULL DEFAULT '普通' COMMENT '座位类型',
    qr_code         VARCHAR(100)  DEFAULT NULL COMMENT '签到二维码标识',
    status          VARCHAR(20)   NOT NULL DEFAULT '空闲' COMMENT '状态',
    row_num         INT           DEFAULT NULL COMMENT '行号',
    col_num         INT           DEFAULT NULL COMMENT '列号',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (seat_id),
    UNIQUE KEY uk_room_seat (room_id, seat_number),
    INDEX idx_room_id (room_id),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES study_rooms(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

CREATE TABLE reservations (
    reservation_id  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    user_id         BIGINT        NOT NULL COMMENT '用户ID',
    seat_id         BIGINT        NOT NULL COMMENT '座位ID',
    reserve_date    DATE          NOT NULL COMMENT '预约日期',
    time_slot       VARCHAR(10)   NOT NULL COMMENT '时段',
    start_time      DATETIME      NOT NULL COMMENT '开始时间',
    end_time        DATETIME      NOT NULL COMMENT '结束时间',
    checkin_time    DATETIME      DEFAULT NULL COMMENT '签到时间',
    checkout_time   DATETIME      DEFAULT NULL COMMENT '签退时间',
    status          VARCHAR(20)   NOT NULL DEFAULT '已预约' COMMENT '状态',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (reservation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_reserve_date (reserve_date),
    INDEX idx_status (status),
    INDEX idx_seat_date_slot (seat_id, reserve_date, time_slot),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

CREATE TABLE violation_records (
    violation_id    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id         BIGINT        NOT NULL COMMENT '用户ID',
    reservation_id  BIGINT        NOT NULL COMMENT '关联预约ID',
    violation_type  VARCHAR(30)   NOT NULL COMMENT '违约类型',
    violation_date  DATE          NOT NULL COMMENT '违约日期',
    penalty         VARCHAR(50)   DEFAULT NULL COMMENT '处罚措施',
    description     VARCHAR(255)  DEFAULT NULL COMMENT '详细说明',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (violation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_violation_date (violation_date),
    CONSTRAINT fk_violation_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_violation_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违约记录表';

INSERT INTO users (username, password, role) VALUES
('admin', '$2b$10$T2hi7YwNZhrKZyItI8mfIuSfxJW.R5kdQK2ud6sqnUjcphaQOlS5O', 'admin'),
('zhangsan', '$2b$10$4NcUxBugJ8EWa6ZicmIB7OVr9IlPW0pzOAB91PpDrd.rwER47ybFq', 'student'),
('lisi', '$2b$10$4NcUxBugJ8EWa6ZicmIB7OVr9IlPW0pzOAB91PpDrd.rwER47ybFq', 'student');

INSERT INTO study_rooms (room_name, location, total_seats, description) VALUES
('自习室A', '图书馆2楼东侧', 20, '安静学习区，配独立台灯'),
('自习室B', '图书馆3楼西侧', 15, '靠窗区域，自然光线好');

INSERT INTO seats (room_id, seat_number, seat_type, row_num, col_num) VALUES
(1, 'A01', '普通', 1, 1), (1, 'A02', '普通', 1, 2), (1, 'A03', '插座', 1, 3), (1, 'A04', '普通', 1, 4), (1, 'A05', '靠窗', 1, 5),
(1, 'A06', '普通', 2, 1), (1, 'A07', '插座', 2, 2), (1, 'A08', '普通', 2, 3), (1, 'A09', '普通', 2, 4), (1, 'A10', '靠窗', 2, 5),
(1, 'A11', '插座', 3, 1), (1, 'A12', '普通', 3, 2), (1, 'A13', '普通', 3, 3), (1, 'A14', '插座', 3, 4), (1, 'A15', '靠窗', 3, 5),
(1, 'A16', '普通', 4, 1), (1, 'A17', '普通', 4, 2), (1, 'A18', '插座', 4, 3), (1, 'A19', '普通', 4, 4), (1, 'A20', '靠窗', 4, 5),
(2, 'B01', '普通', 1, 1), (2, 'B02', '靠窗', 1, 2), (2, 'B03', '插座', 1, 3), (2, 'B04', '普通', 1, 4), (2, 'B05', '靠窗', 1, 5),
(2, 'B06', '插座', 2, 1), (2, 'B07', '普通', 2, 2), (2, 'B08', '普通', 2, 3), (2, 'B09', '靠窗', 2, 4), (2, 'B10', '插座', 2, 5),
(2, 'B11', '普通', 3, 1), (2, 'B12', '靠窗', 3, 2), (2, 'B13', '插座', 3, 3), (2, 'B14', '普通', 3, 4), (2, 'B15', '靠窗', 3, 5);
