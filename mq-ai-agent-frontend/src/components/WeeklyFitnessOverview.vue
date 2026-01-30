<template>
  <div class="weekly-fitness-overview">
    <!-- 卡片头部 -->
    <div class="overview-header">
      <div class="header-left">
        <h2>您的一周</h2>
        <p class="date-range">{{ weekDateRange }}</p>
      </div>
      <div class="header-right">
        <a-button type="primary" class="add-record-btn" @click="showExerciseModal = true">
          <template #icon>
            <icon-plus />
          </template>
          添加运动记录
        </a-button>
      </div>
    </div>

    <!-- 心情打卡日历 -->
    <div class="mood-calendar">
      <div 
        v-for="day in weekMoods" 
        :key="day.date" 
        class="mood-day"
        :class="{ 
          'is-today': day.isToday, 
          'is-future': day.isFuture,
          'has-mood': day.mood 
        }"
        @click="openMoodPicker(day)"
      >
        <div class="mood-emoji">
          <span v-if="day.mood">{{ day.emoji }}</span>
          <span v-else-if="day.isFuture" class="empty-emoji">—</span>
          <span v-else class="empty-emoji add-hint">+</span>
        </div>
        <div class="day-label">{{ day.dayOfWeek }}</div>
      </div>
    </div>

    <!-- 统计数据卡片 -->
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-value primary">{{ weekStats.thisWeek }}</div>
        <div class="stat-label">本周训练次数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value success">{{ weekStats.avgDuration }}</div>
        <div class="stat-label">平均时长(分钟)</div>
      </div>
      <div class="stat-item">
        <div class="stat-value warning">{{ weekStats.consecutiveDays }}</div>
        <div class="stat-label">连续天数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value danger">{{ weekStats.weekCalories.toLocaleString() }}</div>
        <div class="stat-label">本周消耗(kcal)</div>
      </div>
    </div>

    <!-- 每日励志语句 -->
    <div class="daily-quote">
      <div class="quote-icon">{{ dailyQuote.emoji }}</div>
      <div class="quote-content">
        <div class="quote-title">今日寄语</div>
        <div class="quote-text">"{{ dailyQuote.text }}"</div>
      </div>
    </div>

    <!-- 心情选择弹窗 -->
    <a-modal
      v-model:visible="showMoodPicker"
      :title="moodPickerTitle"
      :footer="false"
      width="480px"
      class="mood-picker-modal"
    >
      <div class="mood-picker-content">
        <p class="picker-question">你{{ selectedDay?.isToday ? '今天' : '那天' }}感觉怎么样？</p>
        <div class="mood-options">
          <div 
            v-for="mood in moodLevels" 
            :key="mood.level"
            class="mood-option"
            :class="{ selected: selectedMoodLevel === mood.level }"
            @click="selectMood(mood)"
          >
            <span class="mood-option-emoji">{{ mood.emoji }}</span>
            <span class="mood-option-label">{{ mood.label }}</span>
          </div>
        </div>
        <div class="picker-actions">
          <a-button @click="showMoodPicker = false">取消</a-button>
          <a-button type="primary" :disabled="!selectedMoodLevel" @click="saveMoodRecord">
            确认
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 添加运动记录弹窗 -->
    <a-modal 
      v-model:visible="showExerciseModal" 
      title="添加运动记录" 
      @ok="saveExercise"
      @cancel="resetExerciseForm"
      ok-text="保存"
      cancel-text="取消"
    >
      <a-form :model="exerciseForm" layout="vertical">
        <a-form-item label="体重(kg)" field="weight" :rules="[{required: true, message: '请输入当前体重'}]">
          <a-input-number 
            v-model="exerciseForm.weight" 
            placeholder="输入当前体重" 
            :precision="1"
            :min="1" 
            style="width: 100%"
            @change="calculateCalories"
          />
          <div class="form-helper-text">用于计算消耗的卡路里</div>
        </a-form-item>

        <a-form-item label="运动类型" field="exerciseType" :rules="[{required: true, message: '请选择运动类型'}]">
          <a-select v-model="exerciseForm.exerciseType" placeholder="选择运动类型" @change="calculateCalories">
            <a-option value="跑步">跑步</a-option>
            <a-option value="骑行">骑行</a-option>
            <a-option value="游泳">游泳</a-option>
            <a-option value="力量训练">力量训练</a-option>
            <a-option value="瑹伽">瑹伽</a-option>
            <a-option value="篮球">篮球</a-option>
            <a-option value="足球">足球</a-option>
            <a-option value="羽毛球">羽毛球</a-option>
            <a-option value="散步">散步</a-option>
            <a-option value="爬楼梯">爬楼梯</a-option>
            <a-option value="慢跑">慢跑</a-option>
            <a-option value="快跑">快跑</a-option>
            <a-option value="其他">其他</a-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="运动时长(分钟)" field="duration" :rules="[{required: true, message: '请输入运动时长'}]">
          <a-input-number 
            v-model="exerciseForm.duration" 
            placeholder="输入运动时长" 
            :min="1" 
            style="width: 100%" 
            @change="calculateCalories"
          />
        </a-form-item>
        
        <a-form-item label="消耗卡路里" field="caloriesBurned">
          <a-input-number 
            v-model="exerciseForm.caloriesBurned" 
            placeholder="自动计算" 
            :min="1" 
            style="width: 100%"
            disabled
            readonly
          />
          <div class="form-helper-text">根据运动类型、体重、时长自动计算</div>
        </a-form-item>
        
        <a-form-item label="运动日期" field="dateRecorded" :rules="[{required: true, message: '请选择运动日期'}]">
          <a-date-picker v-model="exerciseForm.dateRecorded" style="width: 100%" value-format="YYYY-MM-DD" />
        </a-form-item>
        
        <a-form-item label="备注">
          <a-textarea v-model="exerciseForm.notes" placeholder="添加运动备注（可选）" :max-length="200" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { getDailyQuote } from '@/utils/motivationalQuotes';
import { 
  MOOD_LEVELS, 
  getWeekMoods, 
  saveMood 
} from '@/services/moodStorage';
import ApiService from '@/services/api';

export default {
  name: 'WeeklyFitnessOverview',
  components: {
    IconPlus
  },
  emits: ['exercise-added'],
  setup(props, { emit }) {
    // 心情相关
    const weekMoods = ref([]);
    const showMoodPicker = ref(false);
    const selectedDay = ref(null);
    const selectedMoodLevel = ref(null);
    const moodLevels = MOOD_LEVELS;

    // 运动记录相关
    const showExerciseModal = ref(false);
    const exerciseForm = reactive({
      weight: null,
      exerciseType: '',
      duration: null,
      caloriesBurned: null,
      dateRecorded: new Date().toISOString().split('T')[0],
      notes: ''
    });

    // 运动类型对应的 MET 值（代谢当量）
    const MET_VALUES = {
      '跑步': 9.8,
      '慢跑': 7.0,
      '快跑': 12.0,
      '骑行': 7.5,
      '游泳': 8.0,
      '力量训练': 6.0,
      '瑹伽': 3.0,
      '篮球': 8.0,
      '足球': 10.0,
      '羽毛球': 5.5,
      '散步': 3.5,
      '爬楼梯': 8.0,
      '其他': 5.0
    };

    // 计算卡路里消耗
    const calculateCalories = () => {
      const { weight, exerciseType, duration } = exerciseForm;
      if (weight && exerciseType && duration) {
        const met = MET_VALUES[exerciseType] || 5.0;
        // 卡路里 = MET × 体重(kg) × 时间(小时)
        const calories = Math.round(met * weight * (duration / 60));
        exerciseForm.caloriesBurned = calories;
      }
    };

    // 统计数据
    const weekStats = ref({
      thisWeek: 0,
      avgDuration: 0,
      consecutiveDays: 0,
      weekCalories: 0
    });

    // 每日励志语句
    const dailyQuote = computed(() => getDailyQuote());

    // 计算本周日期范围
    const weekDateRange = computed(() => {
      if (weekMoods.value.length === 0) return '';
      const start = weekMoods.value[0];
      const end = weekMoods.value[6];
      return `${start.year}年${start.month}月${start.day}日至${end.year}年${end.month}月${end.day}日`;
    });

    // 心情选择弹窗标题
    const moodPickerTitle = computed(() => {
      if (!selectedDay.value) return '记录心情';
      const day = selectedDay.value;
      return `${day.year}年${day.month}月${day.day}日 ${day.dayOfWeek}`;
    });

    // 加载本周心情
    const loadWeekMoods = () => {
      weekMoods.value = getWeekMoods();
    };

    // 打开心情选择器
    const openMoodPicker = (day) => {
      if (day.isFuture) {
        Message.info('不能记录未来的心情哦~');
        return;
      }
      selectedDay.value = day;
      selectedMoodLevel.value = day.mood;
      showMoodPicker.value = true;
    };

    // 选择心情
    const selectMood = (mood) => {
      selectedMoodLevel.value = mood.level;
    };

    // 保存心情记录
    const saveMoodRecord = () => {
      if (!selectedDay.value || !selectedMoodLevel.value) return;
      
      const moodInfo = moodLevels.find(m => m.level === selectedMoodLevel.value);
      const success = saveMood(selectedDay.value.date, selectedMoodLevel.value, moodInfo.emoji);
      
      if (success) {
        Message.success('心情记录成功！');
        loadWeekMoods();
        showMoodPicker.value = false;
        selectedMoodLevel.value = null;
      } else {
        Message.error('保存失败，请重试');
      }
    };

    // 加载运动统计数据
    const loadExerciseStats = async () => {
      try {
        const response = await ApiService.getMyExerciseLogByPage({
          current: 1,
          pageSize: 100
        });

        if (response.code === 0 && response.data?.records) {
          calculateWeekStats(response.data.records);
        }
      } catch (error) {
        console.error('加载运动数据失败:', error);
      }
    };

    // 计算本周统计数据
    const calculateWeekStats = (records) => {
      if (!records || records.length === 0) {
        weekStats.value = {
          thisWeek: 0,
          avgDuration: 0,
          consecutiveDays: 0,
          weekCalories: 0
        };
        return;
      }

      const today = new Date();
      const dayOfWeek = today.getDay();
      const daysFromMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
      
      // 本周一的日期
      const monday = new Date(today);
      monday.setDate(today.getDate() - daysFromMonday);
      monday.setHours(0, 0, 0, 0);

      // 本周日的日期
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);
      sunday.setHours(23, 59, 59, 999);

      // 筛选本周记录
      const weekRecords = records.filter(record => {
        const recordDate = new Date(record.dateRecorded);
        return recordDate >= monday && recordDate <= sunday;
      });

      // 本周训练次数
      const thisWeekCount = weekRecords.length;

      // 本周消耗卡路里
      const weekCalories = weekRecords.reduce((sum, r) => sum + (r.caloriesBurned || 0), 0);

      // 平均时长（基于本周数据）
      const totalDuration = weekRecords.reduce((sum, r) => sum + (r.duration || 0), 0);
      const avgDuration = thisWeekCount > 0 ? Math.round(totalDuration / thisWeekCount) : 0;

      // 连续训练天数
      const consecutiveDays = calculateConsecutiveDays(records);

      weekStats.value = {
        thisWeek: thisWeekCount,
        avgDuration,
        consecutiveDays,
        weekCalories: Math.round(weekCalories)
      };
    };

    // 计算连续训练天数
    const calculateConsecutiveDays = (records) => {
      if (!records || records.length === 0) return 0;

      const sortedRecords = [...records].sort((a, b) => 
        new Date(b.dateRecorded) - new Date(a.dateRecorded)
      );

      const uniqueDates = [...new Set(sortedRecords.map(record => {
        const date = new Date(record.dateRecorded);
        return date.toISOString().split('T')[0];
      }))];

      if (uniqueDates.length === 0) return 0;

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const latestWorkoutDate = new Date(uniqueDates[0]);
      latestWorkoutDate.setHours(0, 0, 0, 0);

      const daysDiff = Math.floor((today - latestWorkoutDate) / (1000 * 60 * 60 * 24));
      if (daysDiff > 1) return 0;

      let consecutive = 1;
      let currentDate = new Date(latestWorkoutDate);

      for (let i = 1; i < uniqueDates.length; i++) {
        const prevDate = new Date(uniqueDates[i]);
        prevDate.setHours(0, 0, 0, 0);
        
        const expectedDate = new Date(currentDate);
        expectedDate.setDate(expectedDate.getDate() - 1);
        
        if (prevDate.getTime() === expectedDate.getTime()) {
          consecutive++;
          currentDate = prevDate;
        } else {
          break;
        }
      }

      return consecutive;
    };

    // 保存运动记录
    const saveExercise = async () => {
      try {
        if (!exerciseForm.weight || !exerciseForm.exerciseType || 
            !exerciseForm.duration || !exerciseForm.dateRecorded) {
          Message.warning('请填写完整的运动信息');
          return;
        }

        // 确保卡路里已计算
        if (!exerciseForm.caloriesBurned) {
          calculateCalories();
        }

        const response = await ApiService.addExerciseLog({
          exerciseType: exerciseForm.exerciseType,
          duration: exerciseForm.duration,
          caloriesBurned: exerciseForm.caloriesBurned,
          dateRecorded: exerciseForm.dateRecorded,
          notes: exerciseForm.notes
        });

        if (response.code === 0) {
          Message.success('运动记录添加成功！');
          showExerciseModal.value = false;
          resetExerciseForm();
          await loadExerciseStats();
          emit('exercise-added');
        } else {
          Message.error(response.message || '添加失败');
        }
      } catch (error) {
        console.error('添加运动记录失败:', error);
        Message.error('添加运动记录失败');
      }
    };

    // 重置运动记录表单
    const resetExerciseForm = () => {
      Object.assign(exerciseForm, {
        weight: null,
        exerciseType: '',
        duration: null,
        caloriesBurned: null,
        dateRecorded: new Date().toISOString().split('T')[0],
        notes: ''
      });
    };

    // 初始化
    onMounted(() => {
      loadWeekMoods();
      loadExerciseStats();
    });

    return {
      weekMoods,
      showMoodPicker,
      selectedDay,
      selectedMoodLevel,
      moodLevels,
      showExerciseModal,
      exerciseForm,
      weekStats,
      dailyQuote,
      weekDateRange,
      moodPickerTitle,
      openMoodPicker,
      selectMood,
      saveMoodRecord,
      saveExercise,
      resetExerciseForm,
      loadExerciseStats,
      calculateCalories
    };
  }
};
</script>

<style lang="scss" scoped>
.weekly-fitness-overview {
  background: var(--theme-bg-card);
  border-radius: 20px;
  padding: 28px;
  box-shadow: var(--theme-shadow-md);
  border: 1px solid var(--theme-border-secondary);
}

// 头部
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;

  .header-left {
    h2 {
      margin: 0 0 4px 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--theme-text-primary);
    }

    .date-range {
      margin: 0;
      font-size: 14px;
      color: var(--theme-text-secondary);
    }
  }

  .add-record-btn {
    border-radius: 20px;
    padding: 8px 20px;
    font-weight: 500;
    
    :deep(.arco-btn-icon) {
      margin-right: 6px;
    }
  }
}

// 心情日历
.mood-calendar {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 28px;
  padding: 20px;
  background: var(--theme-bg-container);
  border-radius: 16px;
  border: 1px solid var(--theme-border-secondary);

  .mood-day {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    padding: 12px 8px;
    border-radius: 12px;
    transition: all 0.2s ease;

    &:hover:not(.is-future) {
      background: var(--theme-bg-hover);
      transform: translateY(-2px);
    }

    &.is-today {
      background: var(--theme-color-primary-light);
      
      .day-label {
        color: var(--theme-color-primary);
        font-weight: 600;
      }
    }

    &.is-future {
      opacity: 0.5;
      cursor: not-allowed;
    }

    &.has-mood {
      .mood-emoji {
        transform: scale(1.1);
      }
    }

    .mood-emoji {
      font-size: 32px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
      transition: transform 0.2s ease;

      .empty-emoji {
        font-size: 24px;
        color: var(--theme-text-muted);
        
        &.add-hint {
          width: 36px;
          height: 36px;
          border: 2px dashed var(--theme-border-primary);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: var(--theme-text-tertiary);
        }
      }
    }

    .day-label {
      font-size: 13px;
      color: var(--theme-text-secondary);
      font-weight: 500;
    }
  }
}

// 统计数据行
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-item {
    background: var(--theme-bg-container);
    border-radius: 14px;
    padding: 20px 16px;
    text-align: center;
    border: 1px solid var(--theme-border-secondary);
    transition: all 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--theme-shadow-sm);
    }

    .stat-value {
      font-size: 28px;
      font-weight: 800;
      margin-bottom: 6px;
      letter-spacing: -1px;

      &.primary {
        color: var(--theme-color-primary);
      }
      &.success {
        color: var(--theme-color-success);
      }
      &.warning {
        color: var(--theme-color-warning);
      }
      &.danger {
        color: #ff6b6b;
      }
    }

    .stat-label {
      font-size: 12px;
      color: var(--theme-text-secondary);
      font-weight: 500;
    }
  }
}

// 每日励志语句
.daily-quote {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: var(--theme-bg-container);
  border-radius: 14px;
  border: 1px solid var(--theme-border-secondary);

  .quote-icon {
    font-size: 36px;
    flex-shrink: 0;
  }

  .quote-content {
    flex: 1;

    .quote-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--theme-text-secondary);
      margin-bottom: 6px;
    }

    .quote-text {
      font-size: 15px;
      color: var(--theme-text-primary);
      line-height: 1.6;
      font-style: italic;
    }
  }
}

// 心情选择弹窗
.mood-picker-content {
  padding: 12px 0;

  .picker-question {
    text-align: center;
    font-size: 17px;
    color: var(--theme-text-primary);
    margin-bottom: 28px;
    font-weight: 500;
  }

  .mood-options {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 28px;

    .mood-option {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 12px;
      border-radius: 14px;
      cursor: pointer;
      border: 2px solid transparent;
      background: var(--theme-bg-container);
      transition: all 0.2s ease;

      &:hover {
        background: var(--theme-bg-hover);
        transform: translateY(-3px);
        box-shadow: var(--theme-shadow-sm);
      }

      &.selected {
        border-color: var(--theme-color-primary);
        background: var(--theme-color-primary-light);
        transform: translateY(-2px);
        box-shadow: var(--theme-shadow-sm);
      }

      .mood-option-emoji {
        font-size: 40px;
        margin-bottom: 10px;
      }

      .mood-option-label {
        font-size: 13px;
        color: var(--theme-text-secondary);
        font-weight: 500;
      }
    }
  }

  .picker-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 8px;
  }
}

// 表单辅助文字
.form-helper-text {
  font-size: 12px;
  color: var(--theme-text-tertiary);
  margin-top: 4px;
}

// 响应式
@media (max-width: 768px) {
  .overview-header {
    flex-direction: column;
    gap: 16px;

    .add-record-btn {
      width: 100%;
    }
  }

  .mood-calendar {
    padding: 16px 12px;
    
    .mood-day {
      padding: 8px 4px;

      .mood-emoji {
        font-size: 24px;
        height: 32px;
      }

      .day-label {
        font-size: 11px;
      }
    }
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .mood-picker-content {
    .mood-options {
      flex-wrap: wrap;
      
      .mood-option {
        flex: 0 0 calc(33.33% - 6px);
        
        &:nth-child(4),
        &:nth-child(5) {
          flex: 0 0 calc(50% - 4px);
        }
      }
    }
  }
}
</style>
