# BMI计算器API接口文档

## 接口概述

BMI计算器接口提供了一个便捷的方式来计算用户的BMI指数，并提供相应的健康建议和理想体重范围。

## 接口详情

### 计算BMI指数

**接口地址**: `GET /api/fitness/data/calculate-bmi`

**请求参数**:
- `weight` (Float, 必填): 体重，单位：kg，范围：0-500
- `height` (Float, 必填): 身高，单位：cm，范围：0-300

**请求示例**:
```http
GET /api/fitness/data/calculate-bmi?weight=70.5&height=175.0
```

**响应格式**:
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

**响应字段说明**:
- `weight`: 输入的体重值
- `height`: 输入的身高值
- `bmi`: 计算得出的BMI指数（保留两位小数）
- `category`: BMI分类（偏瘦/正常/超重/肥胖）
- `idealWeightMin`: 理想体重范围最小值
- `idealWeightMax`: 理想体重范围最大值
- `healthAdvice`: 基于BMI分类的健康建议

## BMI分类标准

| BMI范围 | 分类 | 健康建议 |
|---------|------|----------|
| < 18.5 | 偏瘦 | 建议适当增加营养摄入，进行力量训练来增加肌肉量 |
| 18.5 - 24.9 | 正常 | 请继续保持健康的生活方式和适量运动 |
| 25.0 - 29.9 | 超重 | 建议控制饮食，增加有氧运动，逐步减重 |
| ≥ 30.0 | 肥胖 | 建议咨询专业医生，制定科学的减重计划 |

## 计算公式

BMI = 体重(kg) / 身高(m)²

理想体重范围 = 18.5 ~ 24.9 × 身高(m)²

## 测试用例

### 测试用例1：正常体重
```http
GET /api/fitness/data/calculate-bmi?weight=70&height=175
```
预期结果：BMI ≈ 22.86，分类为"正常"

### 测试用例2：偏瘦
```http
GET /api/fitness/data/calculate-bmi?weight=50&height=175
```
预期结果：BMI ≈ 16.33，分类为"偏瘦"

### 测试用例3：超重
```http
GET /api/fitness/data/calculate-bmi?weight=85&height=175
```
预期结果：BMI ≈ 27.76，分类为"超重"

### 测试用例4：肥胖
```http
GET /api/fitness/data/calculate-bmi?weight=100&height=175
```
预期结果：BMI ≈ 32.65，分类为"肥胖"

## 错误处理

### 参数缺失
```http
GET /api/fitness/data/calculate-bmi?weight=70
```
响应：
```json
{
    "code": 40000,
    "data": null,
    "message": "体重和身高不能为空"
}
```

### 体重数据不合理
```http
GET /api/fitness/data/calculate-bmi?weight=600&height=175
```
响应：
```json
{
    "code": 40000,
    "data": null,
    "message": "体重数据不合理，请输入0-500kg之间的值"
}
```

### 身高数据不合理
```http
GET /api/fitness/data/calculate-bmi?weight=70&height=400
```
响应：
```json
{
    "code": 40000,
    "data": null,
    "message": "身高数据不合理，请输入0-300cm之间的值"
}
```

## 使用场景

1. **健身应用**: 用户可以快速计算自己的BMI，了解身体状况
2. **健康评估**: 医疗或健身专业人士可以使用此接口进行初步健康评估
3. **数据录入辅助**: 在录入健身数据时，可以预先计算BMI进行数据验证
4. **健康教育**: 提供BMI相关的健康知识和建议

## 注意事项

1. 此接口不需要用户登录，可以作为公共工具使用
2. BMI指数仅作为参考，不能完全代表健康状况
3. 对于运动员、孕妇、老年人等特殊人群，BMI标准可能不完全适用
4. 建议结合其他健康指标（如体脂率、肌肉量等）进行综合评估

## 集成示例

### JavaScript调用示例
```javascript
async function calculateBMI(weight, height) {
    try {
        const response = await fetch(`/api/fitness/data/calculate-bmi?weight=${weight}&height=${height}`);
        const result = await response.json();
        
        if (result.code === 0) {
            console.log('BMI计算结果:', result.data);
            return result.data;
        } else {
            console.error('计算失败:', result.message);
            return null;
        }
    } catch (error) {
        console.error('请求失败:', error);
        return null;
    }
}

// 使用示例
calculateBMI(70.5, 175.0).then(result => {
    if (result) {
        console.log(`您的BMI是${result.bmi}，属于${result.category}范围`);
        console.log(`健康建议：${result.healthAdvice}`);
    }
});
```

### Python调用示例
```python
import requests

def calculate_bmi(weight, height):
    url = "http://localhost:8123/api/fitness/data/calculate-bmi"
    params = {"weight": weight, "height": height}
    
    try:
        response = requests.get(url, params=params)
        result = response.json()
        
        if result["code"] == 0:
            return result["data"]
        else:
            print(f"计算失败: {result['message']}")
            return None
    except Exception as e:
        print(f"请求失败: {e}")
        return None

# 使用示例
result = calculate_bmi(70.5, 175.0)
if result:
    print(f"您的BMI是{result['bmi']}，属于{result['category']}范围")
    print(f"健康建议：{result['healthAdvice']}")
```
