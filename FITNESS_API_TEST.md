# 健身数据管理API测试文档

## 概述

本文档描述了健身数据管理系统的API接口测试方法和示例。系统包含以下四个主要模块：

1. **健身数据管理** - 体重、体脂率、身高等身体数据
2. **运动记录管理** - 训练记录和卡路里消耗统计
3. **个性化训练计划** - 训练方案和计划管理
4. **健身目标管理** - 目标设定和进度追踪

## API接口列表

### 1. 健身数据相关接口

#### 1.1 添加健身数据
```http
POST /api/fitness/data/add
Content-Type: application/json

{
    "weight": 70.5,
    "bodyFat": 15.2,
    "height": 175.0,
    "dateRecorded": "2024-01-15T10:30:00"
}
```

#### 1.2 获取健身数据趋势
```http
GET /api/fitness/data/trends?days=30
```

#### 1.3 分页查询我的健身数据
```http
POST /api/fitness/data/my/list/page
Content-Type: application/json

{
    "current": 1,
    "pageSize": 10,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-01-31T23:59:59"
}
```

#### 1.4 删除健身数据
```http
POST /api/fitness/data/delete
Content-Type: application/json

{
    "id": 1
}
```

#### 1.5 计算BMI指数
```http
GET /api/fitness/data/calculate-bmi?weight=70.5&height=175.0
```

响应示例：
```json
{
    "code": 0,
    "data": {
        "weight": 70.5,
        "height": 175.0,
        "bmi": 23.02,
        "category": "正常",
        "idealWeightMin": 56.7,
        "idealWeightMax": 76.6,
        "healthAdvice": "您的体重在正常范围内，请继续保持健康的生活方式和适量运动。"
    },
    "message": "ok"
}
```

### 2. 运动记录相关接口

#### 2.1 添加运动记录
```http
POST /api/fitness/exercise/add
Content-Type: application/json

{
    "exerciseType": "跑步",
    "duration": 30,
    "caloriesBurned": 300.5,
    "dateRecorded": "2024-01-15T18:00:00",
    "notes": "今天跑步感觉很好，配速稳定"
}
```

#### 2.2 获取运动统计
```http
GET /api/fitness/exercise/stats?days=7
```

#### 2.3 获取总卡路里消耗
```http
GET /api/fitness/exercise/calories?days=7
```

#### 2.4 分页查询我的运动记录
```http
POST /api/fitness/exercise/my/list/page
Content-Type: application/json

{
    "current": 1,
    "pageSize": 10,
    "exerciseType": "跑步",
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-01-31T23:59:59"
}
```

### 3. 训练计划相关接口

#### 3.1 创建训练计划
```http
POST /api/fitness/plan/add
Content-Type: application/json

{
    "planName": "增肌训练计划",
    "planType": "增肌",
    "planDetails": "{\"day1\":{\"exercise\":\"深蹲（4组×10次）、卧推（3组×12次）\",\"note\":\"注意动作规范\"},\"day2\":{\"exercise\":\"硬拉（3组×8次）、引体向上（2组×8次）\",\"note\":\"背部发力\"}}",
    "isDefault": 0,
    "startDate": "2024-01-15T00:00:00",
    "endDate": "2024-04-15T00:00:00"
}
```

#### 3.2 获取默认训练计划
```http
GET /api/fitness/plan/default
```

#### 3.3 分页查询我的训练计划
```http
POST /api/fitness/plan/my/list/page
Content-Type: application/json

{
    "current": 1,
    "pageSize": 10,
    "planType": "增肌"
}
```

### 4. 健身目标相关接口

#### 4.1 创建健身目标
```http
POST /api/fitness/goal/add
Content-Type: application/json

{
    "goalType": "减脂",
    "targetValue": "体脂率降到12%",
    "startDate": "2024-01-15T00:00:00",
    "endDate": "2024-06-15T00:00:00"
}
```

#### 4.2 更新健身目标
```http
POST /api/fitness/goal/update
Content-Type: application/json

{
    "id": 1,
    "goalType": "减脂",
    "targetValue": "体脂率降到12%",
    "progress": "{\"2024-01-15\":\"体脂率15.2%\",\"2024-02-15\":\"体脂率14.1%\"}",
    "isAchieved": 0
}
```

#### 4.3 获取进行中的健身目标
```http
GET /api/fitness/goal/active
```

#### 4.4 分页查询我的健身目标
```http
POST /api/fitness/goal/my/list/page
Content-Type: application/json

{
    "current": 1,
    "pageSize": 10,
    "goalType": "减脂",
    "isAchieved": 0
}
```

## 测试场景

### 场景1：新用户完整健身数据录入流程

1. 用户登录系统
2. 录入基础身体数据（体重、身高、体脂率）
3. 设定健身目标（如：3个月内减脂到12%）
4. 选择或创建训练计划
5. 开始记录每日运动数据
6. 定期查看数据趋势和目标进度

### 场景2：数据分析和趋势查看

1. 查看30天内的体重变化趋势
2. 统计7天内的运动消耗卡路里
3. 分析不同运动类型的效果
4. 评估目标完成进度

### 场景3：训练计划管理

1. 浏览默认训练计划模板
2. 根据个人情况创建自定义计划
3. 执行训练计划并记录运动数据
4. 根据效果调整训练计划

## 数据验证规则

### 健身数据验证
- 体重：20-500kg
- 身高：50-300cm  
- 体脂率：0-100%

### 运动数据验证
- 运动时长：1-1440分钟（最多24小时）
- 卡路里消耗：0-10000卡路里

### BMI计算
- BMI = 体重(kg) / 身高(m)²
- 分类：偏瘦(<18.5)、正常(18.5-24.9)、超重(25.0-29.9)、肥胖(≥30.0)

## 响应格式

所有接口都返回统一的响应格式：

```json
{
    "code": 0,
    "data": {},
    "message": "ok"
}
```

- `code`: 0表示成功，非0表示失败
- `data`: 返回的具体数据
- `message`: 响应消息

## 错误处理

常见错误码：
- `40000`: 请求参数错误
- `40100`: 未登录
- `40300`: 无权限
- `40400`: 请求数据不存在
- `50000`: 系统内部异常

## 注意事项

1. 所有需要用户身份的接口都需要先登录
2. 时间格式统一使用ISO 8601格式
3. 数据录入时会自动计算BMI等衍生指标
4. 删除操作为逻辑删除，数据不会真正删除
5. 分页查询支持排序，默认按创建时间倒序
