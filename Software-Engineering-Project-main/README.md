# 自习室座位预约系统

基于 Vue 3 + Spring Boot + MySQL 的 Web 端自习室座位预约系统。

## 技术栈

| 层面 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Axios + Vite |
| 后端 | Spring Boot 3.2 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 认证 | JWT |

## 项目结构

```
seat-reservation/
├── backend/                          # 后端 Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/reservation/
│       │   ├── ReservationApplication.java
│       │   ├── common/               # Result、异常、拦截器
│       │   ├── config/               # WebConfig、MyBatisPlusConfig
│       │   ├── controller/           # 5个Controller
│       │   ├── dto/                  # 数据传输对象
│       │   ├── entity/               # 5个实体类
│       │   ├── mapper/               # MyBatis Mapper接口
│       │   └── service/              # 业务逻辑层
│       └── resources/
│           ├── application.yml
│           └── db/schema.sql         # 建表SQL + 初始数据
├── frontend/                         # 前端 Vue 3
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/request.js            # Axios封装
│       ├── components/NavBar.vue
│       └── views/
│           ├── Login.vue
│           ├── student/              # Rooms、SeatMap、MyReservations
│           └── admin/                # RoomManage、SeatManage、Statistics
└── README.md
```

## 快速启动

### 1. 数据库

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2. 后端

```bash
cd backend
mvn spring-boot:run
# 启动在 http://localhost:8080
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
# 启动在 http://localhost:5173
```

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 学生 | zhangsan | 123456 |
| 学生 | lisi | 123456 |

## 核心功能

- **学生端**：自习室浏览 → 座位查看 → 预约 → 签到 → 签退
- **管理端**：自习室管理、座位管理、数据统计
- **业务规则**：每日最多3次预约、开始前30分钟不可取消、连续3次违约暂停7天
