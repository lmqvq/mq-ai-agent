#!/bin/bash

# MQ AI Agent 缓存性能测试脚本
# 用于验证缓存优化效果

BASE_URL="http://localhost:8123/api"
CACHE_URL="${BASE_URL}/cache"
AI_URL="${BASE_URL}/ai"

echo "=== MQ AI Agent 缓存性能测试 ==="
echo "测试开始时间: $(date)"
echo

# 1. 检查缓存系统健康状态
echo "1. 检查缓存系统健康状态..."
curl -s -X GET "${CACHE_URL}/health" | jq '.' || echo "健康检查失败"
echo

# 2. 重置性能计数器
echo "2. 重置性能计数器..."
curl -s -X POST "${CACHE_URL}/reset-metrics" | jq '.message' || echo "重置失败"
echo

# 3. 测试AI对话缓存效果
echo "3. 测试AI对话缓存效果..."

# 第一次调用（应该缓存未命中）
echo "第一次调用相同问题..."
start_time=$(date +%s%N)
curl -s -X GET "${AI_URL}/keep_app/chat/sync" \
  -d "message=我想增肌，请给我建议" \
  -d "chatId=cache_test_$(date +%s)" > /dev/null
end_time=$(date +%s%N)
first_call_time=$((($end_time - $start_time) / 1000000))
echo "第一次调用耗时: ${first_call_time}ms"

# 等待1秒确保缓存生效
sleep 1

# 第二次调用相同问题（应该缓存命中）
echo "第二次调用相同问题..."
start_time=$(date +%s%N)
curl -s -X GET "${AI_URL}/keep_app/chat/sync" \
  -d "message=我想增肌，请给我建议" \
  -d "chatId=cache_test_$(date +%s)" > /dev/null
end_time=$(date +%s%N)
second_call_time=$((($end_time - $start_time) / 1000000))
echo "第二次调用耗时: ${second_call_time}ms"

# 第三次调用相似问题（应该缓存命中）
echo "第三次调用相似问题..."
start_time=$(date +%s%N)
curl -s -X GET "${AI_URL}/keep_app/chat/sync" \
  -d "message=我想增肌，请给我一些建议" \
  -d "chatId=cache_test_$(date +%s)" > /dev/null
end_time=$(date +%s%N)
third_call_time=$((($end_time - $start_time) / 1000000))
echo "第三次调用耗时: ${third_call_time}ms"

echo

# 4. 批量测试相似问题
echo "4. 批量测试相似问题缓存效果..."
questions=(
  "我想增肌，请给我建议"
  "我想增肌，请给我一些建议"
  "增肌应该怎么做"
  "如何进行力量训练"
  "力量训练的方法有哪些"
  "怎样制定健身计划"
  "健身计划应该如何安排"
)

total_time=0
for i in "${!questions[@]}"; do
  echo "测试问题 $((i+1)): ${questions[i]}"
  start_time=$(date +%s%N)
  curl -s -X GET "${AI_URL}/keep_app/chat/sync" \
    -d "message=${questions[i]}" \
    -d "chatId=batch_test_${i}" > /dev/null
  end_time=$(date +%s%N)
  call_time=$((($end_time - $start_time) / 1000000))
  total_time=$((total_time + call_time))
  echo "  耗时: ${call_time}ms"
done

average_time=$((total_time / ${#questions[@]}))
echo "批量测试平均耗时: ${average_time}ms"
echo

# 5. 获取缓存性能报告
echo "5. 获取缓存性能报告..."
curl -s -X GET "${CACHE_URL}/performance" | jq '.' || echo "获取性能报告失败"
echo

# 6. 获取缓存状态
echo "6. 获取缓存状态..."
curl -s -X GET "${CACHE_URL}/status" | jq '.' || echo "获取缓存状态失败"
echo

# 7. 计算性能提升
echo "7. 性能分析..."
if [ $second_call_time -lt $first_call_time ]; then
  improvement=$(( (first_call_time - second_call_time) * 100 / first_call_time ))
  echo "第二次调用相比第一次提升: ${improvement}%"
else
  echo "第二次调用未显示明显提升（可能缓存未生效或网络波动）"
fi

if [ $third_call_time -lt $first_call_time ]; then
  improvement=$(( (first_call_time - third_call_time) * 100 / first_call_time ))
  echo "相似问题调用相比第一次提升: ${improvement}%"
else
  echo "相似问题调用未显示明显提升"
fi

echo
echo "=== 测试完成 ==="
echo "测试结束时间: $(date)"
echo
echo "说明："
echo "- 第一次调用通常较慢，因为需要调用AI模型并缓存结果"
echo "- 第二次调用相同问题应该更快，因为直接从缓存获取"
echo "- 相似问题调用也应该较快，因为相似度匹配命中缓存"
echo "- 如果Redis未启动或配置错误，缓存功能会自动降级到数据库直连"
echo
echo "如需查看详细日志，请检查应用日志文件"
