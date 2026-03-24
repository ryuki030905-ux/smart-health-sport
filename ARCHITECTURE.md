# 个人健康与运动管理系统 — 架构背景文档
> 本文件用于向 Roo Code 提供项目架构上下文，每次新任务时 @本文件 即可。

---

## 项目概述

- **项目名称**：个人健康与运动管理系统（毕业设计）
- **架构模式**：单体架构，前后端分离（B/S）
- **后端**：Spring Boot 3.2、Spring Security 6（JWT）、MyBatis-Plus、MySQL 8.0
- **前端**：Vue 3、Element-Plus、Axios、ECharts、Vue-Router、Pinia
- **包名根路径**：`com.healthsport`
- **后端端口**：8080
- **前端端口**：5173

---

## 一、数据库设计

数据库名：`health_sport`，字符集：`utf8mb4`，引擎：InnoDB。

### 表关系说明

- `user` 是核心主表
- `health_record`、`exercise_record`、`diet_record`、`exercise_plan` 均通过 `user_id` 关联 `user`
- `exercise_record` 关联 `exercise_dict`
- `diet_record` 关联 `food_dict`

### 完整建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS health_sport
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE health_sport;

-- 1. 用户表
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '登录用户名（唯一）',
  `password`    VARCHAR(100) NOT NULL                COMMENT 'BCrypt加密密码',
  `nickname`    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `gender`      TINYINT      DEFAULT 0               COMMENT '性别: 0=保密 1=男 2=女',
  `age`         TINYINT UNSIGNED DEFAULT NULL        COMMENT '年龄',
  `occupation`  VARCHAR(50)  DEFAULT NULL            COMMENT '职业',
  `avatar_url`  VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1=正常 0=封禁',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 健康记录表
CREATE TABLE `health_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL,
  `record_date`  DATE         NOT NULL,
  `height`       DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight`       DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `systolic_bp`  SMALLINT     DEFAULT NULL COMMENT '收缩压(mmHg)',
  `diastolic_bp` SMALLINT     DEFAULT NULL COMMENT '舒张压(mmHg)',
  `blood_sugar`  DECIMAL(4,2) DEFAULT NULL COMMENT '血糖(mmol/L)',
  `heart_rate`   SMALLINT     DEFAULT NULL COMMENT '心率(次/分)',
  `bmi`          DECIMAL(5,2) DEFAULT NULL COMMENT '自动计算',
  `body_fat`     DECIMAL(5,2) DEFAULT NULL COMMENT '体脂率%，自动计算',
  `health_status` VARCHAR(20) DEFAULT NULL COMMENT 'UNDERWEIGHT/NORMAL/OVERWEIGHT/OBESE',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `record_date`),
  CONSTRAINT `fk_hr_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表';

-- 3. 运动字典表
CREATE TABLE `exercise_dict` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL                COMMENT '运动名称',
  `category`    VARCHAR(30)  DEFAULT NULL            COMMENT '有氧/无氧/球类',
  `met_value`   DECIMAL(4,1) NOT NULL                COMMENT 'MET代谢当量系数',
  `icon`        VARCHAR(50)  DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动字典表';

INSERT INTO `exercise_dict`(`name`,`category`,`met_value`,`description`) VALUES
  ('跑步','有氧',8.0,'户外/跑步机慢跑'),('快走','有氧',4.3,'健步走'),
  ('游泳','有氧',6.0,'自由泳/蛙泳'),('骑自行车','有氧',6.8,'户外骑行'),
  ('跳绳','有氧',10.0,'中速跳绳'),('篮球','球类',6.5,'业余篮球'),
  ('羽毛球','球类',5.5,'业余羽毛球'),('瑜伽','柔韧',2.5,'哈他瑜伽'),
  ('力量训练','无氧',5.0,'器械/自重训练'),('健身操','有氧',5.5,'跟练健身操');

-- 4. 运动记录表
CREATE TABLE `exercise_record` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT       NOT NULL,
  `exercise_dict_id` BIGINT       NOT NULL,
  `exercise_date`    DATE         NOT NULL,
  `duration_min`     INT          NOT NULL                COMMENT '时长（分钟）',
  `intensity`        VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH',
  `calories_burned`  DECIMAL(8,2) DEFAULT NULL            COMMENT '自动计算',
  `remark`           VARCHAR(255) DEFAULT NULL,
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `exercise_date`),
  CONSTRAINT `fk_er_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_er_dict` FOREIGN KEY (`exercise_dict_id`) REFERENCES `exercise_dict` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动记录表';

-- 5. 运动计划表
CREATE TABLE `exercise_plan` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       NOT NULL,
  `plan_name`       VARCHAR(100) NOT NULL,
  `plan_type`       VARCHAR(10)  NOT NULL DEFAULT 'WEEKLY' COMMENT 'WEEKLY/MONTHLY',
  `start_date`      DATE         NOT NULL,
  `end_date`        DATE         NOT NULL,
  `target_duration` INT          NOT NULL COMMENT '目标总时长（分钟）',
  `target_times`    INT          NOT NULL DEFAULT 1,
  `actual_duration` INT          NOT NULL DEFAULT 0,
  `actual_times`    INT          NOT NULL DEFAULT 0,
  `status`          VARCHAR(10)  NOT NULL DEFAULT 'ONGOING' COMMENT 'ONGOING/COMPLETED/ABANDONED',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  CONSTRAINT `fk_ep_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动计划表';

-- 6. 食物字典表
CREATE TABLE `food_dict` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `name`             VARCHAR(100) NOT NULL,
  `category`         VARCHAR(50)  DEFAULT NULL COMMENT '主食/蔬菜/肉类/饮料',
  `calories_per100g` DECIMAL(7,2) NOT NULL     COMMENT '每100g热量(kcal)',
  `protein`          DECIMAL(6,2) DEFAULT NULL,
  `fat`              DECIMAL(6,2) DEFAULT NULL,
  `carbohydrate`     DECIMAL(6,2) DEFAULT NULL,
  `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物热量字典表';

-- 7. 饮食记录表
CREATE TABLE `diet_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL,
  `food_dict_id` BIGINT       NOT NULL,
  `meal_type`    VARCHAR(15)  NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
  `diet_date`    DATE         NOT NULL,
  `quantity_g`   DECIMAL(7,2) NOT NULL COMMENT '摄入量（克）',
  `calories`     DECIMAL(8,2) DEFAULT NULL COMMENT '自动计算',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `diet_date`),
  CONSTRAINT `fk_dr_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_dr_food` FOREIGN KEY (`food_dict_id`) REFERENCES `food_dict` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';
```

---

## 二、后端包结构

```
com.healthsport
├── HealthSportApplication.java
├── config/
│   ├── SecurityConfig.java        # JWT过滤器链，放行/api/v1/auth/**
│   ├── MybatisPlusConfig.java     # 分页插件 + 自动填充
│   └── CorsConfig.java            # 开发阶段放开所有来源
├── controller/
│   ├── AuthController.java        # POST /api/v1/auth/register|login
│   ├── UserController.java
│   ├── HealthRecordController.java
│   ├── ExerciseController.java
│   ├── DietController.java
│   ├── StatisticsController.java
│   └── admin/
│       ├── AdminUserController.java
│       ├── AdminExerciseDictController.java
│       └── AdminFoodDictController.java
├── service/
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── HealthRecordServiceImpl.java  # 调用HealthCalculator
│       ├── ExerciseServiceImpl.java
│       ├── DietServiceImpl.java
│       └── StatisticsServiceImpl.java
├── mapper/
│   ├── UserMapper.java
│   ├── HealthRecordMapper.java
│   ├── ExerciseRecordMapper.java
│   ├── ExerciseDictMapper.java
│   ├── ExercisePlanMapper.java
│   ├── DietRecordMapper.java
│   └── FoodDictMapper.java
├── entity/          # 与数据库表一一对应，使用MP注解
├── dto/             # 接收前端参数（含@Valid校验注解）
├── vo/              # 返回给前端的视图对象（不含密码等敏感字段）
├── security/
│   ├── JwtUtils.java              # 生成/解析/验证Token
│   ├── JwtAuthFilter.java         # OncePerRequestFilter
│   └── UserDetailsServiceImpl.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ErrorCode.java
└── utils/
    ├── HealthCalculator.java      # BMI、体脂率、卡路里计算（静态方法）
    ├── Result.java                # 统一响应封装
    └── SecurityUtils.java         # getCurrentUserId()
```

---

## 三、核心计算公式

### BMI
```
BMI = weight(kg) / (height(cm) / 100)²

判定（中国成人标准）：
< 18.5  → UNDERWEIGHT（偏瘦）
18.5~24 → NORMAL（正常）
24~28   → OVERWEIGHT（超重）
≥ 28    → OBESE（肥胖）
```

### 体脂率（Deurenberg公式）
```
体脂率(%) = 1.20×BMI + 0.23×age - 10.8×gender - 5.4
gender: 男=1，女=0
结果取 max(0, 计算值)，保留2位小数
```

### 运动卡路里（MET公式）
```
calories = MET × weight(kg) × (duration_min / 60) × intensity_factor

intensity_factor:
  LOW    = 0.8
  MEDIUM = 1.0
  HIGH   = 1.2

示例：跑步(MET=8.0)，体重70kg，30分钟，MEDIUM
calories = 8.0 × 70 × 0.5 × 1.0 = 280 kcal
```

### 饮食热量
```
calories = (quantity_g / 100) × food_dict.calories_per100g
```

---

## 四、核心 API 约定

- 基础路径：`/api/v1`
- 认证方式：`Authorization: Bearer <token>`（除注册/登录外均需要）
- 统一响应格式：
```json
{ "code": 200, "message": "操作成功", "data": {} }
```

| 接口 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 注册 | POST | /api/v1/auth/register | 无需Token |
| 登录 | POST | /api/v1/auth/login | 返回JWT token |
| 新增健康记录 | POST | /api/v1/health/records | 自动计算BMI/体脂率 |
| 查询健康记录 | GET | /api/v1/health/records | 分页 |
| 最新健康记录 | GET | /api/v1/health/records/latest | |
| 运动打卡 | POST | /api/v1/exercise/records | 自动计算卡路里 |
| 运动字典列表 | GET | /api/v1/exercise/dict | 公开接口 |
| 运动计划 | POST/GET | /api/v1/exercise/plans | VO含progressRate字段 |
| 饮食记录 | POST | /api/v1/diet/records | 自动计算热量 |
| 食物搜索 | GET | /api/v1/diet/food?keyword= | 模糊查询 |
| 体重趋势 | GET | /api/v1/statistics/weight-trend?days=30 | ECharts折线图 |
| 卡路里收支 | GET | /api/v1/statistics/calorie-balance?year=&month= | ECharts柱状图 |
| 周运动统计 | GET | /api/v1/statistics/weekly-exercise?startDate= | ECharts柱状图 |
| 管理-用户列表 | GET | /api/v1/admin/users | 需ADMIN角色 |
| 管理-封禁用户 | PUT | /api/v1/admin/users/{id}/status | 需ADMIN角色 |
| 管理-运动字典 | CRUD | /api/v1/admin/exercise-dict | 需ADMIN角色 |
| 管理-食物字典 | CRUD | /api/v1/admin/food-dict | 需ADMIN角色 |

---

## 五、各Step任务清单

| Step | 任务 | 核心交付物 |
|---|---|---|
| 1 | 工程骨架与基础配置 | pom.xml、application.yml、Result.java、CorsConfig、GlobalExceptionHandler |
| 2 | 实体类与Mapper | 7个Entity + 7个Mapper + MybatisPlusConfig |
| 3 | JWT安全模块 | JwtUtils、JwtAuthFilter、SecurityConfig、AuthController |
| 4 | 健康档案模块 | HealthCalculator、HealthRecordService/Controller |
| 5 | 运动+饮食模块 | ExerciseService/Controller、DietService/Controller |
| 6 | 统计图表接口 | StatisticsController、StatisticsMapper.xml、前端Charts.vue |
| 7 | 管理后台+前端收尾 | AdminController、Pinia store、路由守卫、Axios拦截器 |
