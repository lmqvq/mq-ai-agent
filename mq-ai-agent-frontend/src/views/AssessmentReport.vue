<template>
  <div class="assessment-report">
    <div class="back-button" @click="goBack">
      <icon-left />
      <span>返回</span>
    </div>

    <div class="report-container">
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h1><icon-bar-chart />体测报告中心</h1>
            <p>集中查看最近体测成绩、AI 个性化建议和历史变化趋势。</p>
          </div>
          <div class="header-actions">
            <a-button @click="refreshData">刷新数据</a-button>
            <a-button
              type="primary"
              :loading="generating"
              :disabled="!selectedRecordId"
              @click="regenerateReport"
            >
              <icon-fire />
              重新生成 AI 建议
            </a-button>
          </div>
        </div>
      </div>

      <a-spin class="page-spin" :loading="pageLoading">
        <div v-if="unauthorized" class="status-panel">
          <a-empty description="登录后才能查看个人体测报告">
            <template #extra>
              <a-button type="primary" @click="goToLogin">去登录</a-button>
            </template>
          </a-empty>
        </div>

        <div v-else-if="!hasRecords" class="status-panel">
          <a-empty description="暂无体测记录，录入成绩后这里会自动生成报告">
            <template #extra>
              <a-button type="primary" @click="refreshData">重新检查</a-button>
            </template>
          </a-empty>
        </div>

        <div v-else class="report-content">
          <div class="toolbar-card">
            <div class="toolbar-main">
              <div class="toolbar-label">选择体测记录</div>
              <a-select
                v-model="selectedRecordId"
                class="record-select"
                placeholder="选择一条体测记录"
                @change="handleRecordChange"
              >
                <a-option
                  v-for="option in recordOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </a-option>
              </a-select>
            </div>
            <div class="toolbar-meta">
              <span v-for="tag in snapshotTags" :key="tag" class="meta-pill">{{ tag }}</span>
            </div>
          </div>

          <a-spin class="content-spin" :loading="contentLoading">
            <div class="overview-cards">
              <div class="overview-card">
                <div class="card-icon score">
                  <icon-trophy />
                </div>
                <div class="card-content">
                  <h3>总分</h3>
                  <div class="value">{{ formatDecimal(currentRecord?.totalScore) }}<span>分</span></div>
                  <div class="sub-text">本次体测综合成绩</div>
                </div>
              </div>

              <div class="overview-card">
                <div class="card-icon level">
                  <icon-dashboard />
                </div>
                <div class="card-content">
                  <h3>等级</h3>
                  <div class="value">{{ currentRecord?.level || '未评分' }}</div>
                  <div class="sub-text">系统按标准自动判级</div>
                </div>
              </div>

              <div class="overview-card">
                <div class="card-icon strength">
                  <icon-thunderbolt />
                </div>
                <div class="card-content">
                  <h3>强项数量</h3>
                  <div class="value">{{ currentRecord?.strengthCount ?? '--' }}<span>项</span></div>
                  <div class="sub-text">分数较高，适合继续保持</div>
                </div>
              </div>

              <div class="overview-card">
                <div class="card-icon weakness">
                  <icon-fire />
                </div>
                <div class="card-content">
                  <h3>弱项数量</h3>
                  <div class="value">{{ currentRecord?.weaknessCount ?? '--' }}<span>项</span></div>
                  <div class="sub-text">建议优先补强的项目</div>
                </div>
              </div>
            </div>

            <div class="summary-card">
              <div class="summary-header">
                <h3><icon-bulb />报告摘要</h3>
                <span class="summary-time">{{ formatDateTime(currentRecord?.assessmentDate) }}</span>
              </div>
              <p>{{ summaryText }}</p>
            </div>

            <div v-if="insightCards.length" class="insight-grid">
              <div
                v-for="card in insightCards"
                :key="card.key"
                class="insight-card"
                :class="card.theme"
              >
                <div class="insight-icon">
                  <component :is="card.icon" />
                </div>
                <div class="insight-content">
                  <h4>{{ card.title }}</h4>
                  <p>{{ card.content }}</p>
                </div>
              </div>
            </div>

            <div v-if="currentReport" class="suggestion-section section-card">
              <div class="section-header">
                <h3><icon-heart />AI 个性化体测建议</h3>
                <span class="section-tip">
                  {{ suggestionSections.length ? `已结构化 ${suggestionSections.length} 段建议` : '展示原始建议文本' }}
                </span>
              </div>

              <div v-if="suggestionSections.length" class="suggestion-grid">
                <div
                  v-for="section in suggestionSections"
                  :key="section.key"
                  class="suggestion-card"
                  :class="section.theme"
                >
                  <div class="suggestion-card-header">
                    <div class="suggestion-icon">
                      <component :is="section.icon" />
                    </div>
                    <div class="suggestion-title">
                      <h4>{{ section.title }}</h4>
                      <span>{{ section.subtitle }}</span>
                    </div>
                  </div>
                  <p class="suggestion-content">{{ section.content }}</p>
                </div>
              </div>

              <div v-else class="raw-suggestion-card">
                <pre>{{ currentReport.aiSuggestion || '暂未生成 AI 建议，请点击上方按钮重新生成。' }}</pre>
              </div>
            </div>

            <div v-else class="report-placeholder section-card">
              <div class="section-header">
                <h3><icon-heart />AI 个性化体测建议</h3>
              </div>
              <p>当前还没有成功生成体测报告，你仍然可以先查看本次基础成绩。需要时可点击上方按钮重新生成 AI 建议。</p>
            </div>

            <div class="item-section section-card">
              <div class="section-header">
                <h3><icon-bar-chart />单项成绩分析</h3>
                <span class="section-tip">{{ currentRecord?.itemList?.length || 0 }} 个项目</span>
              </div>

              <div class="item-grid">
                <div
                  v-for="item in currentRecord?.itemList || []"
                  :key="item.id || item.itemCode"
                  class="item-card"
                >
                  <div class="item-card-header">
                    <div>
                      <h4>{{ item.itemName || item.itemCode }}</h4>
                      <p>{{ formatRawValue(item) }}</p>
                    </div>
                    <a-tag :color="getItemTagColor(item)">
                      {{ item.itemLevel || '待评分' }}
                    </a-tag>
                  </div>

                  <div class="item-score-row">
                    <div class="item-score-box">
                      <span class="item-score-label">单项分</span>
                      <strong>{{ formatDecimal(item.itemScore) }}</strong>
                    </div>
                    <div class="item-score-box">
                      <span class="item-score-label">附加分</span>
                      <strong>{{ formatDecimal(item.extraScore) }}</strong>
                    </div>
                    <div class="item-score-box">
                      <span class="item-score-label">权重</span>
                      <strong>{{ formatWeight(item.itemWeight) }}</strong>
                    </div>
                  </div>

                  <div class="item-progress">
                    <div class="item-progress-track">
                      <div class="item-progress-fill" :style="{ width: `${getScorePercent(item.itemScore)}%` }"></div>
                    </div>
                    <span class="item-progress-text">{{ getScorePercent(item.itemScore) }}%</span>
                  </div>

                  <div class="item-markers">
                    <span v-if="item.isStrength" class="marker strength-marker">强项</span>
                    <span v-if="item.isWeakness" class="marker weakness-marker">弱项</span>
                    <span v-if="item.remark" class="marker neutral-marker">{{ item.remark }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="trend-section section-card">
              <div class="section-header">
                <h3><icon-calendar />历史趋势</h3>
                <span class="section-tip">最近 {{ trends.length }} 次体测记录</span>
              </div>

              <div v-if="trends.length" class="trend-list">
                <div v-for="trend in trends" :key="trend.recordId" class="trend-item">
                  <div class="trend-date">
                    <strong>{{ formatDate(trend.assessmentDate) }}</strong>
                    <span>{{ formatTime(trend.assessmentDate) }}</span>
                  </div>
                  <div class="trend-score">
                    <span class="trend-score-value">{{ formatDecimal(trend.totalScore) }}</span>
                    <a-tag :color="getLevelColor(trend.level)">{{ trend.level || '未评分' }}</a-tag>
                  </div>
                  <p class="trend-summary">{{ trend.summary || '暂无摘要' }}</p>
                </div>
              </div>

              <div v-else class="trend-empty">暂无趋势数据。</div>
            </div>
          </a-spin>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconBarChart,
  IconBulb,
  IconCalendar,
  IconCheckCircle,
  IconDashboard,
  IconExclamationCircle,
  IconFire,
  IconHeart,
  IconLeft,
  IconThunderbolt,
  IconTrophy
} from '@arco-design/web-vue/es/icon';
import ApiService from '@/services/api';

const DEFAULT_MODEL = 'qwen-plus';

const SUGGESTION_SECTIONS = [
  {
    key: 'overallJudgment',
    title: '整体判断',
    subtitle: '先看本次体测状态',
    icon: IconBarChart,
    theme: 'theme-blue'
  },
  {
    key: 'trainingAdvice',
    title: '训练建议',
    subtitle: '明确接下来训练方向',
    icon: IconThunderbolt,
    theme: 'theme-green'
  },
  {
    key: 'weeklyPlan',
    title: '每周安排',
    subtitle: '把建议落到一周节奏',
    icon: IconCalendar,
    theme: 'theme-orange'
  },
  {
    key: 'recoveryAndNutrition',
    title: '恢复与营养',
    subtitle: '兼顾恢复、睡眠和饮食',
    icon: IconHeart,
    theme: 'theme-cyan'
  },
  {
    key: 'precautions',
    title: '注意事项',
    subtitle: '避免无效训练和受伤',
    icon: IconExclamationCircle,
    theme: 'theme-coral'
  },
  {
    key: 'nextAssessmentGoal',
    title: '下一次体测前目标',
    subtitle: '把目标值定清楚',
    icon: IconTrophy,
    theme: 'theme-dark'
  }
];

export default {
  name: 'AssessmentReport',
  components: {
    IconBarChart,
    IconBulb,
    IconCalendar,
    IconCheckCircle,
    IconDashboard,
    IconExclamationCircle,
    IconFire,
    IconHeart,
    IconLeft,
    IconThunderbolt,
    IconTrophy
  },
  setup() {
    const router = useRouter();

    const pageLoading = ref(false);
    const contentLoading = ref(false);
    const generating = ref(false);
    const unauthorized = ref(false);
    const records = ref([]);
    const trends = ref([]);
    const selectedRecordId = ref(undefined);
    const currentRecord = ref(null);
    const currentReport = ref(null);

    const hasRecords = computed(() => records.value.length > 0);

    const recordOptions = computed(() => records.value.map((record) => ({
      value: record.id,
      label: `${formatDate(record.assessmentDate)} · ${record.level || '未评分'} · ${formatDecimal(record.totalScore)}分`
    })));

    const snapshotTags = computed(() => {
      if (!currentRecord.value) {
        return [];
      }
      const tags = [
        getGenderText(currentRecord.value.genderSnapshot),
        currentRecord.value.gradeSnapshot,
        getGradeGroupText(currentRecord.value.gradeGroupSnapshot),
        currentRecord.value.bmiSnapshot !== null && currentRecord.value.bmiSnapshot !== undefined
          ? `BMI ${formatDecimal(currentRecord.value.bmiSnapshot)}`
          : '',
        currentRecord.value.heightSnapshot ? `身高 ${formatDecimal(currentRecord.value.heightSnapshot)} cm` : '',
        currentRecord.value.weightSnapshot ? `体重 ${formatDecimal(currentRecord.value.weightSnapshot)} kg` : ''
      ];
      return tags.filter(Boolean);
    });

    const summaryText = computed(() => {
      return currentReport.value?.overview
        || currentRecord.value?.summary
        || '这次体测记录已经生成，建议结合下方单项成绩和 AI 建议一起查看。';
    });

    const insightCards = computed(() => {
      if (!currentReport.value) {
        return [];
      }
      return [
        {
          key: 'strengthSummary',
          title: '优势总结',
          content: currentReport.value.strengthSummary,
          icon: IconCheckCircle,
          theme: 'theme-green'
        },
        {
          key: 'weaknessSummary',
          title: '短板提醒',
          content: currentReport.value.weaknessSummary,
          icon: IconExclamationCircle,
          theme: 'theme-orange'
        },
        {
          key: 'trainingFocus',
          title: '训练重点',
          content: currentReport.value.trainingFocus,
          icon: IconThunderbolt,
          theme: 'theme-blue'
        },
        {
          key: 'riskNotes',
          title: '风险提示',
          content: currentReport.value.riskNotes,
          icon: IconHeart,
          theme: 'theme-coral'
        }
      ].filter((item) => item.content);
    });

    const suggestionSections = computed(() => {
      const structured = currentReport.value?.aiSuggestionStructured;
      if (!structured || !structured.structured) {
        return [];
      }

      return SUGGESTION_SECTIONS.map((section) => {
        const content = getStructuredContent(structured, section.key, section.title);
        if (!content) {
          return null;
        }
        return {
          ...section,
          content
        };
      }).filter(Boolean);
    });

    const goBack = () => {
      router.push('/home');
    };

    const goToLogin = () => {
      router.push('/login');
    };

    const refreshData = async () => {
      await loadPage(true);
    };

    const handleRecordChange = async (recordId) => {
      if (!recordId) {
        return;
      }
      await loadRecordBundle(recordId);
    };

    const regenerateReport = async () => {
      if (!selectedRecordId.value) {
        return;
      }
      generating.value = true;
      try {
        currentReport.value = await generateReport(selectedRecordId.value, true, false);
        Message.success('AI 体测建议已重新生成');
      } catch (error) {
        handlePageError(error, '重新生成 AI 建议失败');
      } finally {
        generating.value = false;
      }
    };

    const loadPage = async (keepSelection = true) => {
      const previousRecordId = selectedRecordId.value;
      pageLoading.value = true;
      unauthorized.value = false;
      try {
        const [recordResponse, trendResponse] = await Promise.all([
          ApiService.getMyAssessmentRecordByPage({
            current: 1,
            pageSize: 20
          }),
          ApiService.getMyAssessmentTrends('', 10)
        ]);

        records.value = recordResponse?.data?.records || [];
        trends.value = trendResponse?.data || [];

        if (!records.value.length) {
          selectedRecordId.value = undefined;
          currentRecord.value = null;
          currentReport.value = null;
          return;
        }

        const targetRecordId = keepSelection && records.value.some((item) => item.id === previousRecordId)
          ? previousRecordId
          : records.value[0].id;

        selectedRecordId.value = targetRecordId;
        await loadRecordBundle(targetRecordId);
      } catch (error) {
        handlePageError(error, '加载体测报告失败');
      } finally {
        pageLoading.value = false;
      }
    };

    const loadRecordBundle = async (recordId) => {
      contentLoading.value = true;
      unauthorized.value = false;
      try {
        const detailResponse = await ApiService.getMyAssessmentRecord(recordId);
        if (detailResponse.code !== 0) {
          throw detailResponse;
        }
        currentRecord.value = detailResponse.data;

        try {
          currentReport.value = await getOrGenerateReport(recordId);
        } catch (reportError) {
          if (isUnauthorizedError(reportError)) {
            throw reportError;
          }
          currentReport.value = null;
          Message.warning(reportError?.message || '体测报告加载失败，先展示基础成绩数据');
        }
      } catch (error) {
        handlePageError(error, '加载体测记录失败');
      } finally {
        contentLoading.value = false;
      }
    };

    const getOrGenerateReport = async (recordId) => {
      try {
        const reportResponse = await ApiService.getMyAssessmentReport(recordId);
        if (reportResponse.code === 0) {
          return reportResponse.data;
        }
        throw reportResponse;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          throw error;
        }
        if (isNotFoundLikeError(error)) {
          return generateReport(recordId, false, false);
        }
        throw error;
      }
    };

    const generateReport = async (recordId, regenerate = false, showSuccess = true) => {
      const response = await ApiService.generateAssessmentReport({
        recordId,
        regenerate,
        model: DEFAULT_MODEL
      });
      if (response.code !== 0) {
        throw response;
      }
      if (showSuccess) {
        Message.success('体测报告生成成功');
      }
      return response.data;
    };

    const handlePageError = (error, fallbackMessage) => {
      if (isUnauthorizedError(error)) {
        unauthorized.value = true;
        currentRecord.value = null;
        currentReport.value = null;
        records.value = [];
        trends.value = [];
        return;
      }
      Message.error(error?.message || fallbackMessage);
    };

    onMounted(async () => {
      await loadPage(false);
    });

    return {
      contentLoading,
      currentRecord,
      currentReport,
      generating,
      goBack,
      goToLogin,
      handleRecordChange,
      hasRecords,
      insightCards,
      pageLoading,
      recordOptions,
      refreshData,
      regenerateReport,
      selectedRecordId,
      snapshotTags,
      suggestionSections,
      summaryText,
      trends,
      unauthorized,
      formatDate,
      formatDateTime,
      formatDecimal,
      formatRawValue,
      formatTime,
      formatWeight,
      getItemTagColor,
      getLevelColor,
      getScorePercent
    };
  }
};

function getStructuredContent(structured, key, title) {
  if (structured?.[key]) {
    return structured[key];
  }
  const section = structured?.sectionList?.find((item) => item.key === key || item.title === title);
  return section?.content || '';
}

function formatDecimal(value) {
  if (value === null || value === undefined || value === '') {
    return '--';
  }
  const numericValue = Number(value);
  if (Number.isNaN(numericValue)) {
    return '--';
  }
  return numericValue.toFixed(2).replace(/\.?0+$/, '');
}

function formatWeight(value) {
  if (value === null || value === undefined || value === '') {
    return '--';
  }
  return `${formatDecimal(Number(value) * 100)}%`;
}

function formatRawValue(item) {
  if (!item) {
    return '--';
  }
  const value = formatDecimal(item.rawValue);
  return item.unit ? `${value} ${item.unit}` : value;
}

function getScorePercent(score) {
  const numericScore = Number(score);
  if (Number.isNaN(numericScore) || numericScore <= 0) {
    return 0;
  }
  return Math.min(100, Math.round(numericScore));
}

function getItemTagColor(item) {
  if (item?.isWeakness) {
    return 'red';
  }
  if (item?.isStrength) {
    return 'green';
  }
  return getLevelColor(item?.itemLevel);
}

function getLevelColor(level) {
  if (level === '优秀') {
    return 'green';
  }
  if (level === '良好') {
    return 'arcoblue';
  }
  if (level === '及格') {
    return 'orange';
  }
  if (level === '不及格') {
    return 'red';
  }
  return 'gray';
}

function getGenderText(gender) {
  if (gender === 'male' || gender === '男') {
    return '男生';
  }
  if (gender === 'female' || gender === '女') {
    return '女生';
  }
  return gender || '';
}

function getGradeGroupText(gradeGroup) {
  if (gradeGroup === 'freshman_sophomore') {
    return '大一 / 大二';
  }
  if (gradeGroup === 'junior_senior') {
    return '大三 / 大四';
  }
  return gradeGroup || '';
}

function formatDate(value) {
  if (!value) {
    return '--';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '--';
  }
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatTime(value) {
  if (!value) {
    return '--';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '--';
  }
  const hour = `${date.getHours()}`.padStart(2, '0');
  const minute = `${date.getMinutes()}`.padStart(2, '0');
  return `${hour}:${minute}`;
}

function formatDateTime(value) {
  if (!value) {
    return '--';
  }
  return `${formatDate(value)} ${formatTime(value)}`;
}

function isUnauthorizedError(error) {
  const message = error?.message || '';
  return error?.code === 40100
    || error?.status === 401
    || message.includes('未登录')
    || message.includes('登录');
}

function isNotFoundLikeError(error) {
  const message = error?.message || '';
  return error?.code === 40400
    || message.includes('not found')
    || message.includes('不存在')
    || message.includes('未找到');
}
</script>

<style lang="scss" scoped>
.assessment-report {
  min-height: 100vh;
  background: var(--theme-bg-container);
  padding: 20px;
  padding-bottom: 80px;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--theme-bg-card);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  box-shadow: var(--theme-shadow-sm);
  font-weight: 500;
  color: var(--theme-text-primary);
  border: 1px solid var(--theme-border-primary);

  :deep(svg) {
    width: 18px;
    height: 18px;
  }

  &:hover {
    transform: translateX(-4px);
    box-shadow: var(--theme-shadow-md);
    border-color: var(--theme-color-primary);
    color: var(--theme-color-primary);
  }
}

.report-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-spin,
.content-spin {
  display: block;
}

.page-header {
  margin-bottom: 32px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;
  }

  .title-section {
    flex: 1;
    text-align: center;

    h1 {
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: var(--theme-text-primary);
    }

    p {
      margin: 0;
      font-size: 16px;
      color: var(--theme-text-secondary);
    }
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.status-panel,
.toolbar-card,
.summary-card,
.section-card {
  background: var(--theme-bg-card);
  border-radius: 16px;
  border: 1px solid var(--theme-border-secondary);
  box-shadow: var(--theme-shadow-md);
}

.status-panel {
  padding: 48px 24px;
}

.toolbar-card {
  padding: 20px 24px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text-secondary);
}

.record-select {
  min-width: 320px;
}

.toolbar-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-pill {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--theme-bg-card-hover);
  color: var(--theme-text-secondary);
  font-size: 12px;
  border: 1px solid var(--theme-border-primary);
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.overview-card {
  background: var(--theme-bg-card);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--theme-shadow-md);
  border: 1px solid var(--theme-border-secondary);
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    background: var(--theme-bg-card-hover);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;

  :deep(svg) {
    width: 24px;
    height: 24px;
    color: #fff;
  }

  &.score {
    background: linear-gradient(135deg, #2cbf8a 0%, #7be3c0 100%);
  }

  &.level {
    background: linear-gradient(135deg, #4f8cff 0%, #73a7ff 100%);
  }

  &.strength {
    background: linear-gradient(135deg, #1cd8d2 0%, #2db4c2 100%);
  }

  &.weakness {
    background: linear-gradient(135deg, #ff8a65 0%, #ffb38a 100%);
  }
}

.card-content {
  h3 {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: var(--theme-text-secondary);
    font-weight: 500;
  }

  .value {
    font-size: 28px;
    font-weight: 700;
    color: var(--theme-text-primary);
    margin-bottom: 6px;

    span {
      margin-left: 4px;
      font-size: 16px;
      color: var(--theme-color-primary);
      font-weight: 500;
    }
  }

  .sub-text {
    font-size: 12px;
    color: var(--theme-text-muted);
  }
}

.summary-card {
  padding: 24px;
  margin-bottom: 24px;
}

.summary-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.summary-header {
  margin-bottom: 14px;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--theme-text-primary);
  }
}

.summary-time,
.section-tip {
  color: var(--theme-text-muted);
  font-size: 13px;
}

.summary-card p {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.75;
  white-space: pre-line;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.insight-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: var(--theme-bg-card);
  border: 1px solid var(--theme-border-secondary);
  box-shadow: var(--theme-shadow-md);
}

.insight-icon,
.suggestion-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  :deep(svg) {
    width: 22px;
    height: 22px;
    color: #fff;
  }
}

.insight-content,
.suggestion-title {
  min-width: 0;

  h4 {
    margin: 0 0 6px 0;
    font-size: 16px;
    color: var(--theme-text-primary);
  }

  p,
  span {
    margin: 0;
    color: var(--theme-text-secondary);
    line-height: 1.65;
    white-space: pre-line;
  }

  span {
    font-size: 12px;
    color: var(--theme-text-muted);
  }
}

.section-card {
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--theme-text-primary);
  }
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.suggestion-card {
  padding: 20px;
  border-radius: 16px;
  background: var(--theme-bg-card-hover);
  border: 1px solid var(--theme-border-secondary);
  min-height: 240px;
  display: flex;
  flex-direction: column;
}

.suggestion-card-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.suggestion-content {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.75;
  white-space: pre-line;
  flex: 1;
}

.raw-suggestion-card {
  background: var(--theme-bg-card-hover);
  border-radius: 16px;
  border: 1px solid var(--theme-border-secondary);
  padding: 20px;

  pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
    font-family: inherit;
    line-height: 1.75;
    color: var(--theme-text-secondary);
  }
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.item-card {
  padding: 20px;
  border-radius: 16px;
  background: var(--theme-bg-card-hover);
  border: 1px solid var(--theme-border-secondary);
}

.item-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;

  h4 {
    margin: 0 0 6px 0;
    font-size: 17px;
    color: var(--theme-text-primary);
  }

  p {
    margin: 0;
    font-size: 13px;
    color: var(--theme-text-muted);
  }
}

.item-score-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.item-score-box {
  padding: 14px;
  border-radius: 12px;
  background: var(--theme-bg-card);
  border: 1px solid var(--theme-border-primary);

  strong {
    display: block;
    font-size: 22px;
    color: var(--theme-text-primary);
    margin-top: 6px;
  }
}

.item-score-label {
  font-size: 12px;
  color: var(--theme-text-muted);
}

.item-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.item-progress-track {
  flex: 1;
  height: 10px;
  border-radius: 999px;
  background: var(--theme-border-secondary);
  overflow: hidden;
}

.item-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(135deg, #2cbf8a 0%, #7be3c0 100%);
}

.item-progress-text {
  min-width: 44px;
  text-align: right;
  font-size: 12px;
  color: var(--theme-text-muted);
}

.item-markers {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.marker {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.strength-marker {
  background: rgba(44, 191, 138, 0.12);
  color: #2cbf8a;
  border-color: rgba(44, 191, 138, 0.24);
}

.weakness-marker {
  background: rgba(255, 138, 101, 0.12);
  color: #ff8a65;
  border-color: rgba(255, 138, 101, 0.24);
}

.neutral-marker {
  background: var(--theme-bg-card);
  color: var(--theme-text-secondary);
  border-color: var(--theme-border-primary);
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.trend-item {
  display: grid;
  grid-template-columns: 170px 180px 1fr;
  gap: 16px;
  align-items: center;
  padding: 18px 20px;
  border-radius: 14px;
  background: var(--theme-bg-card-hover);
  border: 1px solid var(--theme-border-secondary);
}

.trend-date {
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    color: var(--theme-text-primary);
    font-size: 15px;
  }

  span {
    color: var(--theme-text-muted);
    font-size: 12px;
  }
}

.trend-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trend-score-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--theme-text-primary);
}

.trend-summary {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.7;
}

.trend-empty,
.report-placeholder p {
  color: var(--theme-text-secondary);
  line-height: 1.75;
}

.theme-blue .insight-icon,
.theme-blue .suggestion-icon {
  background: linear-gradient(135deg, #4f8cff 0%, #73a7ff 100%);
}

.theme-green .insight-icon,
.theme-green .suggestion-icon {
  background: linear-gradient(135deg, #2cbf8a 0%, #7be3c0 100%);
}

.theme-orange .insight-icon,
.theme-orange .suggestion-icon {
  background: linear-gradient(135deg, #ffb74d 0%, #ffcc80 100%);
}

.theme-cyan .insight-icon,
.theme-cyan .suggestion-icon {
  background: linear-gradient(135deg, #26c6da 0%, #80deea 100%);
}

.theme-coral .insight-icon,
.theme-coral .suggestion-icon {
  background: linear-gradient(135deg, #ff8a65 0%, #ffb38a 100%);
}

.theme-dark .insight-icon,
.theme-dark .suggestion-icon {
  background: linear-gradient(135deg, #263238 0%, #455a64 100%);
}

@media (max-width: 1200px) {
  .overview-cards,
  .insight-grid,
  .suggestion-grid,
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .trend-item {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .assessment-report {
    padding: 16px;
    padding-bottom: 60px;
  }

  .page-header .header-content,
  .toolbar-card,
  .toolbar-main {
    flex-direction: column;
    align-items: stretch;
  }

  .title-section h1 {
    font-size: 28px;
  }

  .header-actions,
  .record-select {
    width: 100%;
  }

  .overview-cards,
  .insight-grid,
  .suggestion-grid,
  .item-grid,
  .item-score-row {
    grid-template-columns: 1fr;
  }

  .toolbar-meta {
    justify-content: flex-start;
  }

  .suggestion-card {
    min-height: auto;
  }
}
</style>
