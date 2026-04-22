<template>
  <div class="assessment-entry">
    <div class="back-button" @click="goBack">
      <icon-left />
      <span>返回</span>
    </div>

    <div class="entry-container">
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h1><icon-plus />{{ pageTitle }}</h1>
            <p>{{ pageSubtitle }}</p>
          </div>
          <div class="header-actions">
            <a-button @click="goToReport">查看体测报告</a-button>
            <a-button type="primary" :loading="submitting" @click="submitRecord">
              <icon-check-circle />
              {{ submitButtonText }}
            </a-button>
          </div>
        </div>
      </div>

      <a-spin class="page-spin" :loading="pageLoading">
        <div v-if="unauthorized" class="status-panel">
          <a-empty description="登录后才能录入和管理个人体测数据">
            <template #extra>
              <a-button type="primary" @click="goToLogin">去登录</a-button>
            </template>
          </a-empty>
        </div>

        <div v-else-if="!schemeReady" class="status-panel">
          <a-empty description="体测方案暂未加载成功，请稍后重试">
            <template #extra>
              <a-button type="primary" @click="loadInitialData">重新加载</a-button>
            </template>
          </a-empty>
        </div>

        <div v-else class="entry-layout">
          <div class="left-column">
            <div class="section-card profile-card">
              <div class="section-header">
                <h3><icon-dashboard />基础信息</h3>
                <span class="section-tip">{{ schemeName }}</span>
              </div>

              <a-form layout="vertical">
                <div class="form-grid">
                  <a-form-item label="测试时间" required>
                    <a-date-picker
                      v-model="baseForm.assessmentDate"
                      show-time
                      format="YYYY-MM-DD HH:mm:ss"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                      style="width: 100%"
                    />
                  </a-form-item>

                  <a-form-item label="性别" required>
                    <a-radio-group v-model="baseForm.gender">
                      <a-radio value="male">男生</a-radio>
                      <a-radio value="female">女生</a-radio>
                    </a-radio-group>
                  </a-form-item>

                  <a-form-item label="年级" required>
                    <a-select v-model="baseForm.grade" placeholder="选择年级" @change="handleGradeChange">
                      <a-option v-for="option in gradeOptions" :key="option.value" :value="option.value">
                        {{ option.label }}
                      </a-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="年级组">
                    <a-input :model-value="gradeGroupLabel" readonly />
                  </a-form-item>

                  <a-form-item label="身高（cm）" required>
                    <a-input-number
                      v-model="baseForm.height"
                      :min="100"
                      :max="250"
                      :precision="1"
                      placeholder="例如 175"
                      style="width: 100%"
                    />
                  </a-form-item>

                  <a-form-item label="体重（kg）" required>
                    <a-input-number
                      v-model="baseForm.weight"
                      :min="20"
                      :max="200"
                      :precision="1"
                      placeholder="例如 68"
                      style="width: 100%"
                    />
                  </a-form-item>
                </div>
              </a-form>

              <div class="bmi-card">
                <div class="bmi-value">
                  <span class="label">自动计算 BMI</span>
                  <strong>{{ bmiDisplay }}</strong>
                </div>
                <div class="bmi-meta">
                  <span :class="['bmi-tag', bmiStatus.className]">{{ bmiStatus.label }}</span>
                  <span class="bmi-note">BMI 会在提交时自动写入评测记录，无需手填。</span>
                </div>
              </div>

              <div class="helper-card">
                <div class="helper-title"><icon-info-circle />当前录入口径</div>
                <p>{{ currentContextText }}</p>
              </div>

              <div v-if="isEditMode" class="helper-card edit-note">
                <div class="helper-title"><icon-info-circle />编辑模式说明</div>
                <p>保存后会覆盖当前体测记录，并清空旧报告缓存。回到报告页时系统会按最新成绩重新生成建议。</p>
              </div>

              <div class="profile-actions">
                <a-checkbox v-model="saveProfileAfterSubmit">提交后同步保存为常用画像</a-checkbox>
                <div class="profile-action-buttons">
                  <a-button :loading="savingProfile" @click="saveProfileOnly">先保存常用画像</a-button>
                  <a-button v-if="isEditMode" @click="resetForm">恢复当前记录</a-button>
                  <a-button v-else @click="resetForm">重置本页</a-button>
                </div>
              </div>
            </div>

            <div class="section-card tips-card">
              <div class="section-header">
                <h3><icon-heart />录入说明</h3>
                <span class="section-tip">{{ renderedItems.length }} 个必填项目</span>
              </div>

              <div class="tips-list">
                <div class="tip-item">
                  <strong>50 米跑：</strong>
                  <span>直接输入秒数，支持一位小数，例如 `7.6`。</span>
                </div>
                <div class="tip-item">
                  <strong>{{ getEnduranceLabel(baseForm.gender) }}：</strong>
                  <span>用“分钟 + 秒”录入，页面会自动换算成总秒数。</span>
                </div>
                <div class="tip-item">
                  <strong>{{ getUpperBodyLabel(baseForm.gender) }}：</strong>
                  <span>直接输入次数，系统会按当前性别套用对应评分规则。</span>
                </div>
                <div class="tip-item">
                  <strong>{{ isEditMode ? '编辑后：' : '提交后：' }}</strong>
                  <span>{{ isEditMode ? '系统会按新成绩重新评分并刷新报告。' : '系统会自动评分、生成报告，并跳转到对应体测报告页。' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="right-column">
            <div class="section-card item-section">
              <div class="section-header">
                <h3><icon-bar-chart />体测项目成绩</h3>
                <span class="section-tip">按当前画像自动切换项目名称与评分口径</span>
              </div>

              <div class="item-grid">
                <div v-for="item in renderedItems" :key="item.itemCode" class="item-card">
                  <div class="item-card-header">
                    <div>
                      <h4>{{ getDisplayItemName(item, baseForm.gender) }}</h4>
                      <p>{{ getItemMetaText(item) }}</p>
                    </div>
                    <span class="item-weight">{{ formatWeight(item.weight) }}</span>
                  </div>

                  <template v-if="item.itemCode === 'ENDURANCE_RUN'">
                    <div class="duration-group">
                      <a-input-number
                        v-model="enduranceMinutes"
                        :min="0"
                        :max="30"
                        placeholder="分"
                        style="width: 100%"
                        @change="syncEnduranceValue"
                      />
                      <span class="duration-separator">:</span>
                      <a-input-number
                        v-model="enduranceSeconds"
                        :min="0"
                        :max="59"
                        placeholder="秒"
                        style="width: 100%"
                        @change="syncEnduranceValue"
                      />
                    </div>
                    <div class="item-helper">
                      已换算 {{ formatDecimal(itemValues[item.itemCode]) }} 秒。{{ getValidationText(item) }}
                    </div>
                  </template>

                  <template v-else>
                    <a-input-number
                      v-model="itemValues[item.itemCode]"
                      :min="toNumber(item.validationMin)"
                      :max="toNumber(item.validationMax)"
                      :precision="item.inputPrecision || 0"
                      :step="getInputStep(item)"
                      :placeholder="getPlaceholder(item)"
                      style="width: 100%"
                    />
                    <div class="item-helper">{{ getValidationText(item) }}</div>
                  </template>
                </div>
              </div>

              <div class="section-footer">
                <a-button @click="goToReport">返回记录列表</a-button>
                <a-button type="primary" :loading="submitting" @click="submitRecord">
                  {{ submitButtonText }}
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import {
  IconBarChart,
  IconCheckCircle,
  IconDashboard,
  IconHeart,
  IconInfoCircle,
  IconLeft,
  IconPlus
} from '@arco-design/web-vue/es/icon';
import ApiService from '@/services/api';

const DEFAULT_SCHEME_CODE = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD';
const DEFAULT_SOURCE_TYPE = 'manual';

const gradeOptions = [
  { label: '大一', value: '大一' },
  { label: '大二', value: '大二' },
  { label: '大三', value: '大三' },
  { label: '大四', value: '大四' }
];

export default {
  name: 'AssessmentEntry',
  components: {
    IconBarChart,
    IconCheckCircle,
    IconDashboard,
    IconHeart,
    IconInfoCircle,
    IconLeft,
    IconPlus
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const pageLoading = ref(false);
    const submitting = ref(false);
    const savingProfile = ref(false);
    const unauthorized = ref(false);
    const scheme = ref(null);
    const profileSnapshot = ref(null);
    const editingRecordId = ref(null);
    const currentRecordDetail = ref(null);
    const saveProfileAfterSubmit = ref(true);
    const enduranceMinutes = ref(null);
    const enduranceSeconds = ref(null);
    const itemValues = reactive({});

    const baseForm = reactive(getDefaultBaseForm());

    const schemeReady = computed(() => !!scheme.value);
    const isEditMode = computed(() => editingRecordId.value !== null);
    const routeRecordId = computed(() => getRouteRecordId(route.query.recordId));
    const schemeName = computed(() => scheme.value?.schemeName || '体测方案');
    const pageTitle = computed(() => (isEditMode.value ? '编辑体测记录' : '体测数据录入'));
    const pageSubtitle = computed(() => (isEditMode.value
      ? '修改这条体测记录后，系统会重新评分并刷新对应的 AI 报告。'
      : '录入大学生体测成绩后，系统会自动评分并跳转到 AI 体测报告页。'));
    const submitButtonText = computed(() => (isEditMode.value ? '保存并更新报告' : '提交并生成报告'));
    const bmiValue = computed(() => calculateBmi(baseForm.height, baseForm.weight));
    const bmiDisplay = computed(() => formatDecimal(bmiValue.value));
    const bmiStatus = computed(() => getBmiStatus(bmiValue.value));
    const gradeGroupLabel = computed(() => getGradeGroupLabel(baseForm.gradeGroup));

    const renderedItems = computed(() => {
      const itemList = scheme.value?.itemList || [];
      return itemList
        .filter((item) => item.itemCode !== 'BMI')
        .filter((item) => isItemApplicable(item, baseForm.gender, baseForm.gradeGroup))
        .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
    });

    const currentContextText = computed(() => {
      const genderText = baseForm.gender === 'female' ? '女生' : '男生';
      const upperBodyLabel = getUpperBodyLabel(baseForm.gender);
      const enduranceLabel = getEnduranceLabel(baseForm.gender);
      return `当前按 ${genderText}、${gradeGroupLabel.value || '未选择年级组'} 口径录入，力量项目为“${upperBodyLabel}”，耐力项目为“${enduranceLabel}”。`;
    });

    watch(() => baseForm.grade, (value) => {
      baseForm.gradeGroup = mapGradeToGroup(value);
    });

    watch(() => itemValues.ENDURANCE_RUN, (value) => {
      syncEnduranceFieldsFromValue(value);
    });

    watch(routeRecordId, async (newRecordId, oldRecordId) => {
      if (!scheme.value || newRecordId === oldRecordId) {
        return;
      }
      if (newRecordId) {
        await loadRecordForEdit(newRecordId);
      } else {
        editingRecordId.value = null;
        currentRecordDetail.value = null;
        resetForm();
      }
    });

    const goBack = () => {
      router.push('/assessment');
    };

    const goToReport = () => {
      router.push('/assessment');
    };

    const goToLogin = () => {
      router.push('/login');
    };

    const handleGradeChange = (value) => {
      baseForm.gradeGroup = mapGradeToGroup(value);
    };

    const loadInitialData = async () => {
      pageLoading.value = true;
      unauthorized.value = false;
      try {
        const schemeResponse = await ApiService.getAssessmentScheme(DEFAULT_SCHEME_CODE);
        if (schemeResponse.code !== 0) {
          throw schemeResponse;
        }
        scheme.value = schemeResponse.data;
        initializeItemValues();
        await loadProfileSnapshot();

        if (routeRecordId.value) {
          await loadRecordForEdit(routeRecordId.value);
        } else {
          resetForm();
        }
      } catch (error) {
        handlePageError(error, '体测录入页加载失败');
      } finally {
        pageLoading.value = false;
      }
    };

    const loadProfileSnapshot = async () => {
      try {
        const profileResponse = await ApiService.getMyAssessmentProfile(DEFAULT_SCHEME_CODE);
        if (profileResponse.code === 0 && profileResponse.data) {
          profileSnapshot.value = profileResponse.data;
        }
      } catch (error) {
        if (isUnauthorizedError(error)) {
          throw error;
        }
        if (!isNotFoundLikeError(error)) {
          Message.warning(error?.message || '常用画像加载失败，本次将使用空白表单');
        }
      }
    };

    const loadRecordForEdit = async (recordId) => {
      const response = await ApiService.getMyAssessmentRecord(recordId);
      if (response.code !== 0) {
        throw response;
      }
      editingRecordId.value = recordId;
      currentRecordDetail.value = response.data;
      applyRecordDetail(response.data);
    };

    const initializeItemValues = () => {
      const itemList = scheme.value?.itemList || [];
      Object.keys(itemValues).forEach((key) => {
        delete itemValues[key];
      });
      itemList
        .filter((item) => item.itemCode !== 'BMI')
        .forEach((item) => {
          itemValues[item.itemCode] = null;
        });
      enduranceMinutes.value = null;
      enduranceSeconds.value = null;
    };

    const applyProfile = (profile) => {
      if (!profile) {
        return;
      }
      baseForm.gender = profile.gender || baseForm.gender;
      baseForm.grade = profile.grade || baseForm.grade;
      baseForm.gradeGroup = profile.gradeGroup || mapGradeToGroup(profile.grade);
      baseForm.height = toNumber(profile.height);
      baseForm.weight = toNumber(profile.weight);
    };

    const applyRecordDetail = (recordDetail) => {
      Object.assign(baseForm, {
        assessmentDate: formatDateTimeForInput(recordDetail?.assessmentDate ? new Date(recordDetail.assessmentDate) : new Date()),
        gender: recordDetail?.genderSnapshot || 'male',
        grade: recordDetail?.gradeSnapshot || '大一',
        gradeGroup: recordDetail?.gradeGroupSnapshot || mapGradeToGroup(recordDetail?.gradeSnapshot),
        height: toNumber(recordDetail?.heightSnapshot),
        weight: toNumber(recordDetail?.weightSnapshot)
      });

      initializeItemValues();
      (recordDetail?.itemList || []).forEach((item) => {
        if (item.itemCode === 'BMI') {
          return;
        }
        itemValues[item.itemCode] = toNumber(item.rawValue);
      });
      syncEnduranceFieldsFromValue(itemValues.ENDURANCE_RUN);
    };

    const saveProfileOnly = async () => {
      const validationMessage = validateBaseForm();
      if (validationMessage) {
        Message.warning(validationMessage);
        return;
      }

      savingProfile.value = true;
      try {
        const response = await ApiService.saveMyAssessmentProfile(buildProfilePayload());
        if (response.code !== 0) {
          throw response;
        }
        profileSnapshot.value = response.data || buildProfilePayload();
        Message.success('常用画像已保存，后续录入会自动预填');
      } catch (error) {
        handlePageError(error, '保存常用画像失败');
      } finally {
        savingProfile.value = false;
      }
    };

    const submitRecord = async () => {
      const validationMessage = validateRecordForm();
      if (validationMessage) {
        Message.warning(validationMessage);
        return;
      }

      submitting.value = true;
      try {
        const response = isEditMode.value
          ? await ApiService.updateAssessmentRecord(buildRecordPayload())
          : await ApiService.addAssessmentRecord(buildRecordPayload());
        if (response.code !== 0) {
          throw response;
        }

        if (saveProfileAfterSubmit.value) {
          try {
            const profileResponse = await ApiService.saveMyAssessmentProfile(buildProfilePayload());
            if (profileResponse.code === 0 && profileResponse.data) {
              profileSnapshot.value = profileResponse.data;
            }
          } catch (profileError) {
            if (!isUnauthorizedError(profileError)) {
              Message.warning(profileError?.message || '体测记录已保存，但常用画像保存失败');
            }
          }
        }

        Message.success(isEditMode.value ? '体测记录已更新，正在打开最新报告' : '体测记录提交成功，正在打开体测报告');
        router.push({
          path: '/assessment',
          query: {
            recordId: response.data
          }
        });
      } catch (error) {
        handlePageError(error, isEditMode.value ? '更新体测记录失败' : '提交体测记录失败');
      } finally {
        submitting.value = false;
      }
    };

    const resetForm = () => {
      const nextForm = getDefaultBaseForm();
      Object.assign(baseForm, nextForm);
      initializeItemValues();
      applyProfile(profileSnapshot.value);

      if (isEditMode.value && currentRecordDetail.value) {
        applyRecordDetail(currentRecordDetail.value);
      } else {
        editingRecordId.value = null;
        currentRecordDetail.value = null;
      }
    };

    const syncEnduranceValue = () => {
      const minuteValue = toNumber(enduranceMinutes.value);
      const secondValue = toNumber(enduranceSeconds.value);

      if (minuteValue === null && secondValue === null) {
        itemValues.ENDURANCE_RUN = null;
        return;
      }

      itemValues.ENDURANCE_RUN = (minuteValue || 0) * 60 + (secondValue || 0);
    };

    const syncEnduranceFieldsFromValue = (value) => {
      const numericValue = toNumber(value);
      if (numericValue === null) {
        enduranceMinutes.value = null;
        enduranceSeconds.value = null;
        return;
      }
      enduranceMinutes.value = Math.floor(numericValue / 60);
      enduranceSeconds.value = numericValue % 60;
    };

    const buildProfilePayload = () => ({
      schemeCode: DEFAULT_SCHEME_CODE,
      gender: baseForm.gender,
      grade: baseForm.grade,
      gradeGroup: baseForm.gradeGroup,
      height: baseForm.height,
      weight: baseForm.weight
    });

    const buildRecordPayload = () => {
      const payload = {
        schemeCode: DEFAULT_SCHEME_CODE,
        assessmentDate: baseForm.assessmentDate,
        sourceType: DEFAULT_SOURCE_TYPE,
        gender: baseForm.gender,
        grade: baseForm.grade,
        gradeGroup: baseForm.gradeGroup,
        height: baseForm.height,
        weight: baseForm.weight,
        itemList: renderedItems.value.map((item) => ({
          itemCode: item.itemCode,
          rawValue: itemValues[item.itemCode],
          remark: ''
        }))
      };
      if (isEditMode.value) {
        payload.id = editingRecordId.value;
      }
      return payload;
    };

    const validateBaseForm = () => {
      if (!baseForm.assessmentDate) {
        return '请选择测试时间';
      }
      if (!baseForm.gender) {
        return '请选择性别';
      }
      if (!baseForm.grade) {
        return '请选择年级';
      }
      if (!baseForm.gradeGroup) {
        return '当前年级未映射到年级组，请重新选择年级';
      }
      if (toNumber(baseForm.height) === null) {
        return '请输入身高';
      }
      if (toNumber(baseForm.weight) === null) {
        return '请输入体重';
      }
      return '';
    };

    const validateRecordForm = () => {
      const baseMessage = validateBaseForm();
      if (baseMessage) {
        return baseMessage;
      }

      const missingItem = renderedItems.value.find((item) => {
        const value = itemValues[item.itemCode];
        return item.isRequired && (value === null || value === undefined || value === '');
      });
      if (missingItem) {
        return `请填写 ${getDisplayItemName(missingItem, baseForm.gender)}`;
      }
      return '';
    };

    const handlePageError = (error, fallbackMessage) => {
      if (isUnauthorizedError(error)) {
        unauthorized.value = true;
        return;
      }
      Message.error(error?.message || fallbackMessage);
    };

    onMounted(async () => {
      await loadInitialData();
    });

    return {
      baseForm,
      bmiDisplay,
      bmiStatus,
      currentContextText,
      enduranceMinutes,
      enduranceSeconds,
      formatDecimal,
      formatWeight,
      getDisplayItemName,
      getEnduranceLabel,
      getInputStep,
      getItemMetaText,
      getPlaceholder,
      getUpperBodyLabel,
      getValidationText,
      goBack,
      goToLogin,
      goToReport,
      gradeGroupLabel,
      gradeOptions,
      handleGradeChange,
      isEditMode,
      itemValues,
      loadInitialData,
      pageLoading,
      pageSubtitle,
      pageTitle,
      renderedItems,
      resetForm,
      saveProfileAfterSubmit,
      saveProfileOnly,
      savingProfile,
      schemeName,
      schemeReady,
      submitButtonText,
      submitRecord,
      submitting,
      syncEnduranceValue,
      toNumber,
      unauthorized
    };
  }
};

function getDefaultBaseForm() {
  return {
    assessmentDate: formatDateTimeForInput(new Date()),
    gender: 'male',
    grade: '大一',
    gradeGroup: 'freshman_sophomore',
    height: null,
    weight: null
  };
}

function formatDateTimeForInput(date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  const hour = `${date.getHours()}`.padStart(2, '0');
  const minute = `${date.getMinutes()}`.padStart(2, '0');
  const second = `${date.getSeconds()}`.padStart(2, '0');
  return `${year}-${month}-${day}T${hour}:${minute}:${second}`;
}

function getRouteRecordId(recordId) {
  const rawValue = Array.isArray(recordId) ? recordId[0] : recordId;
  const numericValue = Number(rawValue);
  return Number.isFinite(numericValue) && numericValue > 0 ? numericValue : null;
}

function mapGradeToGroup(grade) {
  if (grade === '大一' || grade === '大二') {
    return 'freshman_sophomore';
  }
  if (grade === '大三' || grade === '大四') {
    return 'junior_senior';
  }
  return '';
}

function getGradeGroupLabel(gradeGroup) {
  if (gradeGroup === 'freshman_sophomore') {
    return '大一 / 大二';
  }
  if (gradeGroup === 'junior_senior') {
    return '大三 / 大四';
  }
  return '未识别';
}

function isItemApplicable(item, gender, gradeGroup) {
  const genderMatched = !item.applicableGender
    || item.applicableGender === 'all'
    || item.applicableGender === gender;
  const gradeMatched = !item.applicableGradeGroup
    || item.applicableGradeGroup === 'all'
    || item.applicableGradeGroup === gradeGroup;
  return genderMatched && gradeMatched;
}

function getDisplayItemName(item, gender) {
  if (!item) {
    return '';
  }
  if (item.itemCode === 'UPPER_BODY_OR_CORE') {
    return getUpperBodyLabel(gender || null);
  }
  if (item.itemCode === 'ENDURANCE_RUN') {
    return getEnduranceLabel(gender || null);
  }
  return item.itemName;
}

function getUpperBodyLabel(gender) {
  return gender === 'female' ? '仰卧起坐' : '引体向上';
}

function getEnduranceLabel(gender) {
  return gender === 'female' ? '800 米跑' : '1000 米跑';
}

function getPlaceholder(item) {
  if (item.itemCode === 'RUN_50M') {
    return '例如 7.6';
  }
  if (item.itemCode === 'LUNG_CAPACITY') {
    return '例如 4200';
  }
  if (item.itemCode === 'SIT_AND_REACH') {
    return '例如 18.5';
  }
  if (item.itemCode === 'STANDING_LONG_JUMP') {
    return '例如 220';
  }
  if (item.itemCode === 'UPPER_BODY_OR_CORE') {
    return '请输入次数';
  }
  return '请输入数值';
}

function getInputStep(item) {
  const precision = item.inputPrecision || 0;
  return precision === 0 ? 1 : Number(`0.${'0'.repeat(precision - 1)}1`);
}

function getItemMetaText(item) {
  return `${getUnitLabel(item.unit)} · 权重 ${formatWeight(item.weight)}`;
}

function getValidationText(item) {
  const minValue = formatDecimal(item.validationMin);
  const maxValue = formatDecimal(item.validationMax);
  const unitText = getUnitLabel(item.unit);
  return `建议范围 ${minValue} - ${maxValue} ${unitText}`;
}

function getUnitLabel(unit) {
  if (unit === 'second') {
    return '秒';
  }
  if (unit === 'count') {
    return '次';
  }
  if (unit === 'index') {
    return '指数';
  }
  return unit || '';
}

function calculateBmi(height, weight) {
  const numericHeight = toNumber(height);
  const numericWeight = toNumber(weight);
  if (numericHeight === null || numericWeight === null || numericHeight <= 0) {
    return null;
  }
  const bmi = numericWeight / Math.pow(numericHeight / 100, 2);
  return Number(bmi.toFixed(2));
}

function getBmiStatus(bmi) {
  if (bmi === null || bmi === undefined) {
    return {
      label: '待计算',
      className: 'neutral'
    };
  }
  if (bmi < 18.5) {
    return {
      label: '偏瘦',
      className: 'underweight'
    };
  }
  if (bmi < 24) {
    return {
      label: '正常',
      className: 'normal'
    };
  }
  if (bmi < 28) {
    return {
      label: '超重',
      className: 'overweight'
    };
  }
  return {
    label: '肥胖',
    className: 'obese'
  };
}

function formatDecimal(value) {
  const numericValue = toNumber(value);
  if (numericValue === null) {
    return '--';
  }
  return numericValue.toFixed(2).replace(/\.?0+$/, '');
}

function formatWeight(value) {
  const numericValue = toNumber(value);
  if (numericValue === null) {
    return '--';
  }
  return `${numericValue}%`;
}

function toNumber(value) {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const numericValue = Number(value);
  return Number.isNaN(numericValue) ? null : numericValue;
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
.assessment-entry {
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

.entry-container {
  max-width: 1400px;
  margin: 0 auto;
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

.page-spin {
  display: block;
}

.status-panel,
.section-card {
  background: var(--theme-bg-card);
  border-radius: 16px;
  border: 1px solid var(--theme-border-secondary);
  box-shadow: var(--theme-shadow-md);
}

.status-panel {
  padding: 48px 24px;
}

.entry-layout {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 24px;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: var(--theme-text-primary);
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.section-tip {
  font-size: 13px;
  color: var(--theme-text-muted);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 16px;
}

.bmi-card,
.helper-card {
  margin-top: 8px;
  padding: 18px 20px;
  border-radius: 14px;
  background: var(--theme-bg-card-hover);
  border: 1px solid var(--theme-border-secondary);
}

.bmi-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.bmi-value {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .label {
    font-size: 13px;
    color: var(--theme-text-muted);
  }

  strong {
    font-size: 30px;
    color: var(--theme-text-primary);
  }
}

.bmi-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.bmi-tag {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.bmi-tag.neutral {
  background: var(--theme-bg-card);
  color: var(--theme-text-secondary);
}

.bmi-tag.underweight {
  background: rgba(79, 140, 255, 0.12);
  color: #4f8cff;
}

.bmi-tag.normal {
  background: rgba(44, 191, 138, 0.12);
  color: #2cbf8a;
}

.bmi-tag.overweight {
  background: rgba(255, 183, 77, 0.16);
  color: #ff9800;
}

.bmi-tag.obese {
  background: rgba(255, 138, 101, 0.16);
  color: #ff8a65;
}

.bmi-note {
  font-size: 12px;
  color: var(--theme-text-muted);
}

.helper-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.helper-card p {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.7;
}

.edit-note {
  border-color: rgba(79, 140, 255, 0.2);
}

.profile-actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.profile-action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tip-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--theme-bg-card-hover);
  border: 1px solid var(--theme-border-secondary);
  line-height: 1.7;
  color: var(--theme-text-secondary);

  strong {
    color: var(--theme-text-primary);
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
    font-size: 18px;
    color: var(--theme-text-primary);
  }

  p {
    margin: 0;
    font-size: 12px;
    color: var(--theme-text-muted);
  }
}

.item-weight {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(44, 191, 138, 0.12);
  color: #2cbf8a;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.duration-group {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 12px;
  align-items: center;
}

.duration-separator {
  color: var(--theme-text-secondary);
  font-size: 20px;
  font-weight: 600;
}

.item-helper {
  margin-top: 10px;
  font-size: 12px;
  color: var(--theme-text-muted);
  line-height: 1.6;
}

.section-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 1200px) {
  .entry-layout {
    grid-template-columns: 1fr;
  }

  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .assessment-entry {
    padding: 16px;
    padding-bottom: 60px;
  }

  .page-header .header-content,
  .profile-actions,
  .section-footer,
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .profile-action-buttons {
    width: 100%;
    flex-direction: column;
  }

  .title-section h1 {
    font-size: 28px;
  }

  .form-grid,
  .item-grid {
    grid-template-columns: 1fr;
  }

  .bmi-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .bmi-meta {
    align-items: flex-start;
  }
}
</style>
