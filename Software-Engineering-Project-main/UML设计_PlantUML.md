# 自习室座位预约系统 UML 设计

本文档给出自习室座位预约系统的 UML 图设计源码，可直接复制到 PlantUML、ProcessOn、draw.io 的 PlantUML 插件或支持 PlantUML 的 Markdown 编辑器中生成图片。

## 1. 用例图

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle
skinparam shadowing false

actor "学生用户" as Student
actor "管理员" as Admin

rectangle "自习室座位预约系统" {
  usecase "注册/登录" as UC_Login
  usecase "浏览自习室" as UC_Rooms
  usecase "查看座位状态" as UC_Seats
  usecase "预约座位" as UC_Reserve
  usecase "取消预约" as UC_Cancel
  usecase "查看我的预约" as UC_My
  usecase "签到" as UC_Checkin
  usecase "签退" as UC_Checkout
  usecase "查看违约记录" as UC_Violation

  usecase "用户管理" as UC_UserManage
  usecase "自习室管理" as UC_RoomManage
  usecase "座位管理" as UC_SeatManage
  usecase "预约监控" as UC_ReservationManage
  usecase "违约处理" as UC_ViolationManage
  usecase "数据统计" as UC_Stats
  usecase "导出报表" as UC_Report
}

Student --> UC_Login
Student --> UC_Rooms
Student --> UC_Seats
Student --> UC_Reserve
Student --> UC_Cancel
Student --> UC_My
Student --> UC_Checkin
Student --> UC_Checkout
Student --> UC_Violation

Admin --> UC_Login
Admin --> UC_UserManage
Admin --> UC_RoomManage
Admin --> UC_SeatManage
Admin --> UC_ReservationManage
Admin --> UC_ViolationManage
Admin --> UC_Stats

UC_Reserve ..> UC_Seats : <<include>>
UC_Cancel ..> UC_My : <<include>>
UC_Checkin ..> UC_My : <<include>>
UC_Checkout ..> UC_My : <<include>>
UC_Report ..> UC_Stats : <<extend>>
@enduml
```

## 2. 类图

```plantuml
@startuml
skinparam classAttributeIconSize 0
skinparam shadowing false

class User {
  - Long userId
  - String username
  - String password
  - String phone
  - String role
  - Integer status
  - Integer violationCount
  - LocalDateTime banUntil
  - LocalDateTime createdAt
  - LocalDateTime updatedAt
}

class StudyRoom {
  - Long roomId
  - String roomName
  - String location
  - Integer totalSeats
  - LocalTime openTime
  - LocalTime closeTime
  - Integer status
  - String description
  - LocalDateTime createdAt
  - LocalDateTime updatedAt
}

class Seat {
  - Long seatId
  - Long roomId
  - String seatNumber
  - String seatType
  - String qrCode
  - String status
  - Integer rowNum
  - Integer colNum
  - LocalDateTime createdAt
  - LocalDateTime updatedAt
}

class Reservation {
  - Long reservationId
  - Long userId
  - Long seatId
  - LocalDate reserveDate
  - String timeSlot
  - LocalDateTime startTime
  - LocalDateTime endTime
  - LocalDateTime checkinTime
  - LocalDateTime checkoutTime
  - String status
  - LocalDateTime createdAt
  - LocalDateTime updatedAt
}

class ViolationRecord {
  - Long violationId
  - Long userId
  - Long reservationId
  - String violationType
  - LocalDate violationDate
  - String penalty
  - String description
  - LocalDateTime createdAt
}

class UserController {
  + register()
  + login()
  + getProfile()
  + updateProfile()
}

class StudyRoomController {
  + listRooms()
  + getRoom()
  + createRoom()
  + updateRoom()
  + deleteRoom()
}

class SeatController {
  + listSeatsByRoom()
  + createSeat()
  + updateSeat()
  + deleteSeat()
}

class ReservationController {
  + createReservation()
  + listMyReservations()
  + cancelReservation()
  + checkin()
  + checkout()
}

class StatisticsController {
  + getOverview()
  + getRoomUsage()
  + getDailyReservations()
}

class UserService
class StudyRoomService
class SeatService
class ReservationService
class StatisticsService

User "1" --> "0..*" Reservation : creates
StudyRoom "1" --> "0..*" Seat : contains
Seat "1" --> "0..*" Reservation : reserved by
User "1" --> "0..*" ViolationRecord : has
Reservation "1" --> "0..1" ViolationRecord : produces

UserController --> UserService
StudyRoomController --> StudyRoomService
SeatController --> SeatService
ReservationController --> ReservationService
StatisticsController --> StatisticsService

ReservationService --> User
ReservationService --> Seat
ReservationService --> Reservation
ReservationService --> ViolationRecord
SeatService --> StudyRoom
SeatService --> Seat
StatisticsService --> Reservation
StatisticsService --> StudyRoom
@enduml
```

## 3. 预约座位顺序图

```plantuml
@startuml
skinparam shadowing false
actor "学生用户" as Student
boundary "Vue前端" as Web
control "ReservationController" as RC
control "ReservationService" as RS
database "MySQL" as DB

Student -> Web : 选择自习室、座位、日期和时段
Web -> RC : POST /api/reservations
RC -> RS : createReservation(reserveDto, userId)
RS -> DB : 查询用户状态和处罚截止时间
DB --> RS : 用户信息
RS -> DB : 查询当日预约次数
DB --> RS : 预约次数
RS -> DB : 查询座位时段是否冲突
DB --> RS : 冲突检查结果

alt 校验通过
  RS -> DB : INSERT reservations
  RS -> DB : UPDATE seats.status = "已预约"
  DB --> RS : 保存成功
  RS --> RC : 预约记录
  RC --> Web : Result.success(data)
  Web --> Student : 显示预约成功
else 座位冲突/超限/被处罚
  RS --> RC : 抛出 BusinessException
  RC --> Web : Result.error(message)
  Web --> Student : 显示失败原因
end
@enduml
```

## 4. 签到顺序图

```plantuml
@startuml
skinparam shadowing false
actor "学生用户" as Student
boundary "Vue前端" as Web
control "ReservationController" as RC
control "ReservationService" as RS
database "MySQL" as DB

Student -> Web : 点击签到/扫描座位二维码
Web -> RC : POST /api/reservations/{id}/checkin
RC -> RS : checkin(reservationId, userId, qrCode)
RS -> DB : SELECT reservation by id
DB --> RS : 预约记录
RS -> DB : SELECT seat by id
DB --> RS : 座位信息

alt 预约属于当前用户且在签到窗口内
  RS -> DB : UPDATE reservations SET status="使用中", checkin_time=now()
  RS -> DB : UPDATE seats SET status="使用中"
  DB --> RS : 更新成功
  RS --> RC : 签到成功
  RC --> Web : Result.success()
  Web --> Student : 显示签到成功
else 超时未签到
  RS -> DB : UPDATE reservations SET status="已违约"
  RS -> DB : INSERT violation_records
  RS -> DB : UPDATE seats SET status="空闲"
  RS --> RC : 违约处理结果
  RC --> Web : Result.error("签到超时，已记录违约")
  Web --> Student : 显示违约提示
else 无权限/二维码不匹配
  RS --> RC : 抛出 BusinessException
  RC --> Web : Result.error(message)
  Web --> Student : 显示失败原因
end
@enduml
```

## 5. 预约记录状态图

```plantuml
@startuml
skinparam shadowing false

[*] --> 已预约 : 创建预约成功

已预约 --> 已取消 : 用户提前取消
已预约 --> 使用中 : 签到成功
已预约 --> 已违约 : 超过签到窗口未签到

使用中 --> 已完成 : 用户签退
使用中 --> 已完成 : 到达预约结束时间
使用中 --> 已违约 : 异常离席/违规处理

已取消 --> [*]
已完成 --> [*]
已违约 --> [*]
@enduml
```

## 6. 座位状态图

```plantuml
@startuml
skinparam shadowing false

[*] --> 空闲
空闲 --> 已预约 : 用户预约成功
已预约 --> 空闲 : 用户取消预约
已预约 --> 使用中 : 用户签到
已预约 --> 空闲 : 超时未签到释放座位
使用中 --> 空闲 : 用户签退/预约结束

空闲 --> 维护中 : 管理员设为维护
已预约 --> 维护中 : 管理员强制维护
使用中 --> 维护中 : 管理员强制维护
维护中 --> 空闲 : 管理员恢复座位
@enduml
```

## 7. 部署图

```plantuml
@startuml
skinparam shadowing false

node "用户终端" as Client {
  artifact "浏览器" as Browser
  artifact "Vue 3 SPA\n学生端/管理端" as SPA
}

node "Web服务器" as WebServer {
  component "Nginx\n静态资源服务/反向代理" as Nginx
}

node "应用服务器" as AppServer {
  component "Spring Boot API\nPort 8080" as API
  component "JWT拦截器" as JWT
  component "业务服务层\nUser/Room/Seat/Reservation/Statistics" as Service
  component "MyBatis-Plus Mapper" as Mapper
}

database "MySQL 8.0\nseat_reservation" as MySQL
cloud "可选缓存\nRedis" as Redis

Browser --> SPA
SPA --> Nginx : HTTP/HTTPS
Nginx --> API : REST API / JSON
API --> JWT : token校验
API --> Service : 调用业务逻辑
Service --> Mapper : 数据访问
Mapper --> MySQL : JDBC
Service ..> Redis : 缓存热点数据
@enduml
```

## 8. 怎么画成图片

1. 打开 PlantUML 在线编辑器或安装 VS Code 的 PlantUML 插件。
2. 复制任意一个 `@startuml` 到 `@enduml` 之间的代码。
3. 渲染后导出 PNG/SVG。
4. 在设计报告中用 `![图名](图片路径)` 插入导出的图片。

