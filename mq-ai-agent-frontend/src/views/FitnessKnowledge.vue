<template>
  <div class="fitness-knowledge">
    <div class="knowledge-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <h1><icon-book />健身知识库</h1>
          <p>专业的健身知识，科学的训练方法，助您成为健身达人</p>
        </div>
        <a-button v-if="isAdmin" type="primary" @click="goToManage">
          <icon-settings /> 管理知识库
        </a-button>
      </div>

      <!-- 知识分类导航 -->
      <div class="category-nav">
        <div class="nav-tabs">
          <div 
            v-for="category in categories" 
            :key="category.id"
            :class="['nav-tab', { active: activeCategory === category.id }]"
            @click="activeCategory = category.id"
          >
            <component :is="category.icon" />
            <span>{{ category.name }}</span>
          </div>
        </div>
      </div>

      <!-- 知识内容区域 -->
      <div class="knowledge-content">
        <!-- 基础知识 -->
        <div v-if="activeCategory === 'basics'" class="content-section">
          <div class="knowledge-grid">
            <div v-for="item in basicsKnowledge" :key="item.id" class="knowledge-card">
              <div class="card-image">
                <img 
                  :src="item.image" 
                  :alt="item.title" 
                  loading="lazy"
                  @error="handleImageError"
                />
                <div class="card-overlay">
                  <div class="difficulty-badge" :class="item.difficulty.toLowerCase()">
                    {{ item.difficulty }}
                  </div>
                  <div v-if="isAdmin" class="admin-edit-btn" @click.stop="editItem('basics', item)">
                    <icon-edit />
                  </div>
                </div>
              </div>
              <div class="card-content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
                <div class="card-meta">
                  <span><icon-clock-circle />{{ item.readTime }}分钟阅读</span>
                  <span><icon-eye />{{ item.views }}次浏览</span>
                </div>
                <a-button type="primary" @click="viewKnowledge(item)">
                  阅读详情
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 动作指导 -->
        <div v-if="activeCategory === 'exercises'" class="content-section">
          <div class="exercise-categories">
            <div v-for="exerciseType in exerciseTypes" :key="exerciseType.id" class="exercise-type">
              <h3>{{ exerciseType.name }}</h3>
              <div class="exercise-grid">
                <div v-for="exercise in exerciseType.exercises" :key="exercise.id" class="exercise-card">
                  <div class="exercise-image">
                    <img 
                      :src="exercise.image" 
                      :alt="exercise.name" 
                      loading="lazy"
                      @error="handleImageError"
                    />
                    <div class="play-button" @click="playExerciseVideo(exercise)">
                      <icon-play-arrow />
                    </div>
                    <div v-if="isAdmin" class="admin-edit-btn" @click.stop="editItem('exercises', exercise)">
                      <icon-edit />
                    </div>
                  </div>
                  <div class="exercise-info">
                    <h4>{{ exercise.name }}</h4>
                    <p>{{ exercise.description }}</p>
                    <div class="exercise-details">
                      <span class="muscle-group">{{ exercise.muscleGroup }}</span>
                      <span class="difficulty" :class="exercise.difficulty.toLowerCase()">
                        {{ exercise.difficulty }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 营养知识 -->
        <div v-if="activeCategory === 'nutrition'" class="content-section">
          <div class="nutrition-sections">
            <div class="nutrition-overview">
              <h3>营养基础知识</h3>
              <div class="nutrition-cards">
                <div v-for="nutrient in nutrients" :key="nutrient.id" class="nutrient-card">
                  <div v-if="isAdmin" class="admin-edit-btn inline" @click.stop="editItem('nutrients', nutrient)">
                    <icon-edit />
                  </div>
                  <div class="nutrient-icon" :style="{ backgroundColor: nutrient.color }">
                    <component :is="nutrient.icon" />
                  </div>
                  <div class="nutrient-info">
                    <h4>{{ nutrient.name }}</h4>
                    <p>{{ nutrient.description }}</p>
                    <div class="nutrient-benefits">
                      <span v-for="benefit in nutrient.benefits" :key="benefit" class="benefit-tag">
                        {{ benefit }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="meal-plans">
              <h3>推荐饮食计划</h3>
              <div class="meal-grid">
                <div v-for="meal in mealPlans" :key="meal.id" class="meal-card">
                  <div class="meal-header">
                    <h4>{{ meal.name }}</h4>
                    <span class="meal-type">{{ meal.type }}</span>
                    <div v-if="isAdmin" class="admin-edit-btn inline" @click.stop="editItem('meals', meal)">
                      <icon-edit />
                    </div>
                  </div>
                  <div class="meal-content">
                    <div class="meal-image">
                      <img 
                        :src="meal.image" 
                        :alt="meal.name" 
                        loading="lazy"
                        @error="handleImageError"
                      />
                    </div>
                    <div class="meal-details">
                      <p>{{ meal.description }}</p>
                      <div class="nutrition-facts">
                        <div class="fact">
                          <span class="label">卡路里</span>
                          <span class="value">{{ meal.calories }}kcal</span>
                        </div>
                        <div class="fact">
                          <span class="label">蛋白质</span>
                          <span class="value">{{ meal.protein }}g</span>
                        </div>
                        <div class="fact">
                          <span class="label">碳水</span>
                          <span class="value">{{ meal.carbs }}g</span>
                        </div>
                        <div class="fact">
                          <span class="label">脂肪</span>
                          <span class="value">{{ meal.fat }}g</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 训练计划 -->
        <div v-if="activeCategory === 'training'" class="content-section">
          <div class="training-programs">
            <div v-for="program in trainingPrograms" :key="program.id" class="program-card">
              <div v-if="isAdmin" class="admin-edit-btn corner" @click.stop="editItem('programs', program)">
                <icon-edit />
              </div>
              <div class="program-header">
                <div class="program-icon" :class="program.type">
                  <component :is="program.icon" />
                </div>
                <div class="program-info">
                  <h3>{{ program.name }}</h3>
                  <p>{{ program.description }}</p>
                  <div class="program-meta">
                    <span><icon-clock-circle />{{ program.duration }}</span>
                    <span><icon-fire />{{ program.intensity }}</span>
                    <span><icon-user />{{ program.level }}</span>
                  </div>
                </div>
              </div>
              <div class="program-content">
                <div class="program-schedule">
                  <h4>训练安排</h4>
                  <div class="schedule-list">
                    <div v-for="day in program.schedule" :key="day.day" class="schedule-item">
                      <div class="day-label">{{ day.day }}</div>
                      <div class="day-content">{{ day.content }}</div>
                    </div>
                  </div>
                </div>
                <div class="program-actions">
                  <a-button type="primary" @click="startProgram(program)">
                    开始训练
                  </a-button>
                  <a-button type="outline" @click="viewProgramDetails(program)">
                    查看详情
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 知识详情弹窗 -->
    <a-modal 
      v-model:visible="showKnowledgeModal" 
      :footer="false" 
      width="900px"
      :body-style="{ padding: 0 }"
      modal-class="knowledge-modal"
    >
      <template #title>
        <div class="modal-title-simple">
          {{ selectedKnowledge?.title }}
        </div>
      </template>
      <div v-if="selectedKnowledge" class="knowledge-detail">
        <div class="detail-image-wrapper">
          <img 
            :src="selectedKnowledge.image" 
            :alt="selectedKnowledge.title" 
            class="detail-image" 
            loading="lazy"
          />
        </div>
        <div class="detail-content">
          <!-- 文章元数据 -->
          <div class="article-meta">
            <span class="difficulty-tag" :class="selectedKnowledge.difficulty">
              {{ selectedKnowledge.difficulty }}
            </span>
            <span class="meta-item">
              <icon-clock-circle /> {{ selectedKnowledge.readTime }} 分钟阅读
            </span>
            <span class="meta-item">
              <icon-eye /> {{ selectedKnowledge.views }} 次浏览
            </span>
          </div>
          <div class="content-section">
            <h4 class="section-title">
              <icon-book /> 核心内容
            </h4>
            <div class="content-text">
              <p>{{ selectedKnowledge.content }}</p>
            </div>
          </div>
          
          <div class="content-section tips-section">
            <h4 class="section-title">
              <icon-fire /> 重要提示
            </h4>
            <ul class="tips-list">
              <li v-for="(tip, index) in selectedKnowledge.tips" :key="tip" class="tip-item">
                <span class="tip-number">{{ index + 1 }}</span>
                <span class="tip-text">{{ tip }}</span>
              </li>
            </ul>
          </div>

          <div class="detail-footer">
            <div class="footer-stats">
              <div class="stat-item">
                <icon-eye class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedKnowledge.views }}</div>
                  <div class="stat-label">浏览量</div>
                </div>
              </div>
              <div class="stat-item">
                <icon-clock-circle class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedKnowledge.readTime }}分钟</div>
                  <div class="stat-label">阅读时间</div>
                </div>
              </div>
              <div class="stat-item">
                <icon-fire class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedKnowledge.difficulty }}</div>
                  <div class="stat-label">难度等级</div>
                </div>
              </div>
            </div>
            <div class="action-buttons">
              <a-button type="outline" size="large" @click="shareKnowledge">
                <icon-share-alt /> 分享文章
              </a-button>
              <a-button type="primary" size="large" @click="printKnowledge">
                <icon-printer /> 打印保存
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 运动视频弹窗 -->
    <a-modal v-model:visible="showVideoModal" :title="selectedExercise?.name" width="800px">
      <div v-if="selectedExercise" class="video-content">
        <div class="video-placeholder">
          <icon-play-arrow />
          <p>{{ selectedExercise.name }}动作演示视频</p>
        </div>
        <div class="exercise-instructions">
          <h4>动作要领</h4>
          <ol>
            <li v-for="instruction in selectedExercise.instructions" :key="instruction">
              {{ instruction }}
            </li>
          </ol>
          <div class="exercise-tips">
            <h4>注意事项</h4>
            <ul>
              <li v-for="tip in selectedExercise.tips" :key="tip">{{ tip }}</li>
            </ul>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { 
  IconBook, IconTrophy, IconFire, IconHeart, IconUser, IconClockCircle, 
  IconEye, IconPlayArrow, IconShareAlt, IconPrinter, IconStar, IconSettings, IconEdit
} from '@arco-design/web-vue/es/icon';
import ApiService from '@/services/api';

export default {
  name: 'FitnessKnowledge',
  components: {
    IconBook, IconTrophy, IconFire, IconHeart, IconUser, IconClockCircle, 
    IconEye, IconPlayArrow, IconShareAlt, IconPrinter, IconStar, IconSettings, IconEdit
  },
  setup() {
    const router = useRouter();
    const activeCategory = ref('basics');
    const showKnowledgeModal = ref(false);
    const showVideoModal = ref(false);
    const selectedKnowledge = ref(null);
    const selectedExercise = ref(null);
    const loading = ref(false);

    const categories = [
      { id: 'basics', name: '基础知识', icon: 'icon-book' },
      { id: 'exercises', name: '动作指导', icon: 'icon-trophy' },
      { id: 'nutrition', name: '营养知识', icon: 'icon-heart' },
      { id: 'training', name: '训练计划', icon: 'icon-fire' }
    ];

    // 响应式数据 - 从API获取
    const basicsKnowledge = ref([]);
    const exerciseTypes = ref([]);
    const nutrients = ref([]);
    const mealPlans = ref([]);
    const trainingPrograms = ref([]);

    // 加载基础知识
    const loadBasicsKnowledge = async () => {
      try {
        const res = await ApiService.getKnowledgeBasicsList();
        if (res.code === 0 && res.data?.records) {
          basicsKnowledge.value = res.data.records.map(item => ({
            ...item,
            tips: typeof item.tips === 'string' ? JSON.parse(item.tips) : item.tips
          }));
        }
      } catch (error) {
        console.error('加载基础知识失败:', error);
      }
    };

    // 加载动作指导
    const loadExercises = async () => {
      try {
        const res = await ApiService.getKnowledgeExercisesList();
        if (res.code === 0 && res.data?.records) {
          const exercises = res.data.records.map(item => ({
            ...item,
            instructions: typeof item.instructions === 'string' ? JSON.parse(item.instructions) : item.instructions,
            tips: typeof item.tips === 'string' ? JSON.parse(item.tips) : item.tips
          }));
          // 按分类分组
          const grouped = {};
          exercises.forEach(ex => {
            if (!grouped[ex.category]) {
              grouped[ex.category] = { id: ex.category, name: ex.category, exercises: [] };
            }
            grouped[ex.category].exercises.push(ex);
          });
          exerciseTypes.value = Object.values(grouped);
        }
      } catch (error) {
        console.error('加载动作指导失败:', error);
      }
    };

    // 加载营养知识
    const loadNutrients = async () => {
      try {
        const res = await ApiService.getKnowledgeNutrientsList();
        if (res.code === 0 && res.data) {
          nutrients.value = res.data.map(item => ({
            ...item,
            benefits: typeof item.benefits === 'string' ? JSON.parse(item.benefits) : item.benefits
          }));
        }
      } catch (error) {
        console.error('加载营养知识失败:', error);
      }
    };

    // 加载饮食计划
    const loadMealPlans = async () => {
      try {
        const res = await ApiService.getKnowledgeMealsList();
        if (res.code === 0 && res.data) {
          mealPlans.value = res.data.map(item => ({
            ...item,
            type: item.mealType // 后端字段是mealType
          }));
        }
      } catch (error) {
        console.error('加载饮食计划失败:', error);
      }
    };

    // 加载训练计划
    const loadTrainingPrograms = async () => {
      try {
        const res = await ApiService.getKnowledgeProgramsList();
        if (res.code === 0 && res.data) {
          trainingPrograms.value = res.data.map(item => ({
            ...item,
            schedule: typeof item.schedule === 'string' ? JSON.parse(item.schedule) : item.schedule
          }));
        }
      } catch (error) {
        console.error('加载训练计划失败:', error);
      }
    };

    // 初始化加载所有数据
    const loadAllData = async () => {
      loading.value = true;
      try {
        await Promise.all([
          loadBasicsKnowledge(),
          loadExercises(),
          loadNutrients(),
          loadMealPlans(),
          loadTrainingPrograms()
        ]);
      } catch (error) {
        Message.error('加载数据失败，请刷新重试');
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      loadAllData();
    });

    const viewKnowledge = (knowledge) => {
      selectedKnowledge.value = knowledge;
      showKnowledgeModal.value = true;
    };

    const playExerciseVideo = (exercise) => {
      selectedExercise.value = exercise;
      showVideoModal.value = true;
    };

    const startProgram = (program) => {
      console.log('开始训练计划:', program.name);
    };

    const viewProgramDetails = (program) => {
      console.log('查看计划详情:', program.name);
    };

    // 图片加载错误处理
    const handleImageError = (event) => {
      // 使用渐变色占位图
      event.target.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
      event.target.style.display = 'flex';
      event.target.style.alignItems = 'center';
      event.target.style.justifyContent = 'center';
      event.target.alt = '图片加载失败';
    };

    // 分享文章
    const shareKnowledge = () => {
      if (selectedKnowledge.value) {
        const text = `${selectedKnowledge.value.title} - ${selectedKnowledge.value.description}`;
        if (navigator.share) {
          navigator.share({
            title: selectedKnowledge.value.title,
            text: text,
            url: window.location.href
          });
        } else {
          // 复制到剪贴板
          navigator.clipboard.writeText(text).then(() => {
            console.log('已复制到剪贴板');
          });
        }
      }
    };

    // 打印文章
    const printKnowledge = () => {
      window.print();
    };

    // 管理员权限检查
    const isAdmin = computed(() => {
      try {
        const userStr = localStorage.getItem('user_info');
        if (userStr) {
          const user = JSON.parse(userStr);
          return user.userRole === 'admin';
        }
      } catch {
        // ignore
      }
      return false;
    });

    // 跳转到管理页面
    const goToManage = () => {
      router.push('/admin/knowledge');
    };

    // 编辑单个项目 - 跳转到管理页面并传递参数
    const editItem = (type, item) => {
      // 跳转到管理页面并携带编辑信息
      router.push({
        path: '/admin/knowledge',
        query: { tab: type, editId: item.id }
      });
    };

    return {
      activeCategory,
      showKnowledgeModal,
      showVideoModal,
      selectedKnowledge,
      selectedExercise,
      categories,
      basicsKnowledge,
      exerciseTypes,
      nutrients,
      mealPlans,
      trainingPrograms,
      viewKnowledge,
      playExerciseVideo,
      startProgram,
      viewProgramDetails,
      handleImageError,
      shareKnowledge,
      printKnowledge,
      isAdmin,
      goToManage,
      editItem
    };
  }
};
</script>

<style lang="scss" scoped>
// 图片加载优化
img {
  // 渐进式加载效果
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
  
  &.loaded,
  &[loading="lazy"] {
    opacity: 1;
  }
  
  // 图片加载时的背景
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  // 图片加载失败时的样式
  &[alt="图片加载失败"] {
    min-height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 14px;
  }
}

.fitness-knowledge {
  min-height: 100vh;
  background: #ffffff;
  padding: 20px;
  padding-bottom: 80px;
}

.knowledge-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  color: #1a1a1a;

  .header-content {
    text-align: center;
    flex: 1;
    
    h1 {
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 12px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
    }

    p {
      font-size: 16px;
      opacity: 0.9;
      margin: 0;
    }
  }

  :deep(.arco-btn) {
    flex-shrink: 0;
  }
}

.category-nav {
  margin-bottom: 32px;

  .nav-tabs {
    display: flex;
    justify-content: center;
    gap: 8px;
    background: #f5f5f5;
    padding: 8px;
    border-radius: 16px;

    .nav-tab {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      border-radius: 12px;
      color: #666666;
      cursor: pointer;
      transition: all 0.3s ease;
      font-weight: 500;

      :deep(svg) {
        width: 16px;
        height: 16px;
      }

      &:hover {
        color: #1a1a1a;
        background: #e8e8e8;
      }

      &.active {
        color: #ffffff;
        background: #667eea;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }
    }
  }
}

.knowledge-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.knowledge-card {
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  overflow: hidden;
  background: white;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.2);
    border-color: #667eea;

    &::before {
      transform: scaleX(1);
    }

    .card-image img {
      transform: scale(1.05);
    }
  }

  .card-image {
    position: relative;
    height: 220px;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .card-overlay {
      position: absolute;
      top: 16px;
      right: 16px;

      .difficulty-badge {
        padding: 6px 14px;
        border-radius: 20px;
        font-size: 13px;
        font-weight: 600;
        color: white;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

        &.初级 {
          background: linear-gradient(135deg, rgba(0, 180, 42, 0.95) 0%, rgba(0, 214, 143, 0.95) 100%);
        }

        &.中级 {
          background: linear-gradient(135deg, rgba(255, 125, 0, 0.95) 0%, rgba(255, 169, 64, 0.95) 100%);
        }

        &.高级 {
          background: linear-gradient(135deg, rgba(245, 63, 63, 0.95) 0%, rgba(255, 120, 117, 0.95) 100%);
        }
      }
    }
  }
}

// 管理员编辑按钮样式
.admin-edit-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.95);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

  :deep(svg) {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #764ba2;
    transform: scale(1.1);
  }

  &.inline {
    position: relative;
    top: auto;
    left: auto;
    width: 28px;
    height: 28px;
    margin-left: auto;
    flex-shrink: 0;

    :deep(svg) {
      width: 14px;
      height: 14px;
    }
  }

  &.corner {
    top: 16px;
    right: 16px;
    left: auto;
  }
}

.knowledge-card {
  .card-content {
    padding: 24px;
    background: white;

    h3 {
      margin: 0 0 12px 0;
      font-size: 20px;
      font-weight: 600;
      color: #1a1a1a;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    p {
      margin: 0 0 16px 0;
      color: #666;
      font-size: 14px;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-meta {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      padding: 12px 0;
      border-top: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;

      span {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #999;
        font-weight: 500;

        :deep(svg) {
          width: 14px;
          height: 14px;
          color: #667eea;
        }
      }
    }

    .arco-btn {
      width: 100%;
      height: 42px;
      font-size: 15px;
      font-weight: 500;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      position: relative;

      &::after {
        content: '→';
        position: absolute;
        right: 20px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 18px;
        transition: transform 0.3s ease;
      }

      &:hover {
        opacity: 0.9;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);

        &::after {
          transform: translateY(-50%) translateX(4px);
        }
      }
    }
  }
}

.exercise-categories {
  .exercise-type {
    margin-bottom: 32px;

    h3 {
      margin: 0 0 16px 0;
      font-size: 20px;
      font-weight: 600;
      color: #333;
      padding-bottom: 8px;
      border-bottom: 2px solid #4080ff;
    }
  }
}

.exercise-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.exercise-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .exercise-image {
    position: relative;
    height: 180px;
    background: linear-gradient(45deg, #667eea, #764ba2);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .play-button {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: rgba(64, 128, 255, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;

      :deep(svg) {
        width: 24px;
        height: 24px;
        color: white;
      }

      &:hover {
        background: rgba(64, 128, 255, 1);
        transform: translate(-50%, -50%) scale(1.1);
      }
    }
  }

  .exercise-info {
    padding: 16px;

    h4 {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    p {
      margin: 0 0 12px 0;
      color: #666;
      font-size: 14px;
    }

    .exercise-details {
      display: flex;
      gap: 8px;

      .muscle-group {
        padding: 2px 8px;
        background: #f0f2f5;
        border-radius: 12px;
        font-size: 12px;
        color: #666;
      }

      .difficulty {
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 12px;
        color: white;

        &.初级 {
          background: #00b42a;
        }

        &.中级 {
          background: #ff7d00;
        }

        &.高级 {
          background: #f53f3f;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .knowledge-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }

  .exercise-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

// 知识详情弹窗样式
// 修复 Modal 默认样式
:deep(.knowledge-modal) {
  .arco-modal-header {
    padding: 24px 32px !important;
    border-bottom: 1px solid #f0f0f0;
  }
}

// 简化的标题样式
.modal-title-simple {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.5;
}

.knowledge-detail {
  .detail-image-wrapper {
    width: 100%;
    height: 400px;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 100px;
      background: linear-gradient(to top, rgba(255, 255, 255, 1), transparent);
    }
  }

  .detail-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .detail-content {
    padding: 32px;
    background: white;

    // 文章元数据
    .article-meta {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
      padding: 20px 24px;
      background: linear-gradient(135deg, #f0f5ff 0%, #f9f0ff 100%);
      border-radius: 12px;
      border: 1px solid #e8e8ff;
      margin-bottom: 32px;

      .difficulty-tag {
        padding: 6px 16px;
        border-radius: 20px;
        font-size: 13px;
        font-weight: 600;
        color: white;

        &.初级 {
          background: linear-gradient(135deg, #00b42a 0%, #00d68f 100%);
        }

        &.中级 {
          background: linear-gradient(135deg, #ff7d00 0%, #ffa940 100%);
        }

        &.高级 {
          background: linear-gradient(135deg, #f53f3f 0%, #ff7875 100%);
        }
      }

      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #666;
        font-size: 14px;
        font-weight: 500;

        :deep(svg) {
          width: 16px;
          height: 16px;
          color: #667eea;
        }
      }
    }

    .content-section {
      margin-bottom: 32px;

      &:last-of-type {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0 0 20px 0;
        font-size: 18px;
        font-weight: 600;
        color: #1a1a1a;
        padding-bottom: 12px;
        border-bottom: 2px solid #f0f0f0;

        :deep(svg) {
          width: 20px;
          height: 20px;
          color: #667eea;
        }
      }

      .content-text {
        p {
          margin: 0;
          font-size: 16px;
          line-height: 1.8;
          color: #333;
          text-align: justify;
          text-indent: 2em;
        }
      }
    }

    .tips-section {
      background: linear-gradient(135deg, #f0f5ff 0%, #f9f0ff 100%);
      padding: 24px;
      border-radius: 12px;
      border: 1px solid #e8e8ff;

      .section-title {
        border-bottom: none;
        padding-bottom: 0;
        margin-bottom: 16px;
      }

      .tips-list {
        margin: 0;
        padding: 0;
        list-style: none;

        .tip-item {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          margin-bottom: 16px;
          padding: 16px;
          background: white;
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
          transition: all 0.3s ease;

          &:last-child {
            margin-bottom: 0;
          }

          &:hover {
            transform: translateX(4px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
          }

          .tip-number {
            flex-shrink: 0;
            width: 28px;
            height: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 50%;
            font-size: 14px;
            font-weight: 600;
          }

          .tip-text {
            flex: 1;
            font-size: 15px;
            line-height: 1.6;
            color: #333;
          }
        }
      }
    }

    .detail-footer {
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #f0f0f0;

      .footer-stats {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;
        margin-bottom: 24px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px;
          background: linear-gradient(135deg, #f0f5ff 0%, #f9f0ff 100%);
          border-radius: 12px;
          border: 1px solid #e8e8ff;

          .stat-icon {
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 10px;
            color: white;
            flex-shrink: 0;

            :deep(svg) {
              width: 20px;
              height: 20px;
            }
          }

          .stat-info {
            flex: 1;

            .stat-value {
              font-size: 18px;
              font-weight: 600;
              color: #1a1a1a;
              margin-bottom: 2px;
            }

            .stat-label {
              font-size: 12px;
              color: #666;
            }
          }
        }
      }

      .action-buttons {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .arco-btn {
          height: 48px;
          font-size: 15px;
          font-weight: 500;
          border-radius: 8px;
          transition: all 0.3s ease;

          :deep(svg) {
            margin-right: 6px;
          }

          &[type="outline"] {
            border: 2px solid #667eea;
            color: #667eea;

            &:hover {
              background: #667eea;
              color: white;
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
            }
          }

          &[type="primary"] {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;

            &:hover {
              opacity: 0.9;
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
            }
          }
        }
      }
    }
  }
}

// 营养知识模块样式
.nutrition-sections {
  .nutrition-overview {
    margin-bottom: 40px;

    h3 {
      font-size: 22px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0 0 24px 0;
      padding-bottom: 12px;
      border-bottom: 3px solid #667eea;
      display: inline-block;
    }
  }

  .nutrition-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
  }

  .nutrient-card {
    display: flex;
    gap: 20px;
    padding: 24px;
    background: white;
    border-radius: 16px;
    border: 1px solid #e8e8e8;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
      border-color: #667eea;
    }

    .nutrient-icon {
      flex-shrink: 0;
      width: 60px;
      height: 60px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      :deep(svg) {
        width: 28px;
        height: 28px;
      }
    }

    .nutrient-info {
      flex: 1;

      h4 {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 600;
        color: #1a1a1a;
      }

      p {
        margin: 0 0 16px 0;
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }

      .nutrient-benefits {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .benefit-tag {
          padding: 4px 12px;
          background: linear-gradient(135deg, #f0f5ff 0%, #f9f0ff 100%);
          border-radius: 20px;
          font-size: 12px;
          color: #667eea;
          font-weight: 500;
        }
      }
    }
  }

  .meal-plans {
    h3 {
      font-size: 22px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0 0 24px 0;
      padding-bottom: 12px;
      border-bottom: 3px solid #667eea;
      display: inline-block;
    }
  }

  .meal-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .meal-card {
    background: white;
    border-radius: 16px;
    border: 1px solid #e8e8e8;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
      border-color: #667eea;
    }

    .meal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }

      .meal-type {
        padding: 4px 12px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
      }
    }

    .meal-content {
      padding: 20px;

      .meal-image {
        height: 160px;
        border-radius: 12px;
        overflow: hidden;
        margin-bottom: 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .meal-details {
        p {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: #666;
          line-height: 1.6;
        }

        .nutrition-facts {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 12px;

          .fact {
            text-align: center;
            padding: 12px 8px;
            background: linear-gradient(135deg, #f0f5ff 0%, #f9f0ff 100%);
            border-radius: 12px;

            .label {
              display: block;
              font-size: 12px;
              color: #666;
              margin-bottom: 4px;
            }

            .value {
              display: block;
              font-size: 14px;
              font-weight: 600;
              color: #667eea;
            }
          }
        }
      }
    }
  }
}

// 训练计划模块样式
.training-programs {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.program-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
    border-color: #667eea;
  }

  .program-header {
    display: flex;
    gap: 20px;
    padding: 24px;
    background: linear-gradient(135deg, #f8f9fa 0%, #f0f5ff 100%);
    border-bottom: 1px solid #e8e8e8;

    .program-icon {
      flex-shrink: 0;
      width: 64px;
      height: 64px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      &.beginner { background: linear-gradient(135deg, #00b42a 0%, #00d68f 100%); }
      &.intermediate { background: linear-gradient(135deg, #ff7d00 0%, #ffa940 100%); }
      &.advanced { background: linear-gradient(135deg, #f53f3f 0%, #ff7875 100%); }
      &.fat-loss { background: linear-gradient(135deg, #ff7d00 0%, #ffc53d 100%); }
      &.strength { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }
      &.bodyweight { background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%); }
      &.female { background: linear-gradient(135deg, #eb2f96 0%, #ff85c0 100%); }
      &.busy { background: linear-gradient(135deg, #13c2c2 0%, #5cdbd3 100%); }

      :deep(svg) {
        width: 28px;
        height: 28px;
      }
    }

    .program-info {
      flex: 1;

      h3 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
      }

      p {
        margin: 0 0 12px 0;
        font-size: 14px;
        color: #666;
        line-height: 1.5;
      }

      .program-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #999;
          font-weight: 500;

          :deep(svg) {
            width: 14px;
            height: 14px;
            color: #667eea;
          }
        }
      }
    }
  }

  .program-content {
    padding: 24px;

    .program-schedule {
      margin-bottom: 20px;

      h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
        display: flex;
        align-items: center;
        gap: 8px;

        &::before {
          content: '';
          width: 4px;
          height: 16px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 2px;
        }
      }

      .schedule-list {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .schedule-item {
          display: flex;
          gap: 16px;
          padding: 14px 16px;
          background: linear-gradient(135deg, #f8f9fa 0%, #f0f5ff 100%);
          border-radius: 12px;
          border: 1px solid #e8e8ff;
          transition: all 0.3s ease;

          &:hover {
            transform: translateX(4px);
            border-color: #667eea;
          }

          .day-label {
            flex-shrink: 0;
            min-width: 60px;
            font-weight: 600;
            color: #667eea;
            font-size: 14px;
          }

          .day-content {
            flex: 1;
            font-size: 14px;
            color: #333;
            line-height: 1.5;
          }
        }
      }
    }

    .program-actions {
      display: flex;
      gap: 12px;

      .arco-btn {
        flex: 1;
        height: 44px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 10px;
        transition: all 0.3s ease;

        &[type="primary"] {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;

          &:hover {
            opacity: 0.9;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
          }
        }

        &[type="outline"] {
          border: 2px solid #667eea;
          color: #667eea;
          background: transparent;

          &:hover {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .knowledge-grid, .exercise-grid {
    grid-template-columns: 1fr;
  }

  .category-nav .nav-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .knowledge-content {
    padding: 20px;
  }

  .knowledge-detail {
    .detail-image-wrapper {
      height: 250px;
    }

    .detail-content {
      padding: 20px;

      .content-section {
        margin-bottom: 24px;

        .content-text p {
          font-size: 15px;
        }
      }

      .tips-section {
        padding: 16px;

        .tips-list .tip-item {
          padding: 12px;

          .tip-text {
            font-size: 14px;
          }
        }
      }
    }
  }

  .modal-title-wrapper {
    h3 {
      font-size: 20px;
    }

    .title-meta {
      flex-wrap: wrap;
      gap: 8px;
      font-size: 12px;
    }
  }

  // 营养知识响应式
  .nutrition-sections {
    .nutrition-cards {
      grid-template-columns: 1fr;
    }

    .nutrient-card {
      flex-direction: column;
      text-align: center;

      .nutrient-icon {
        margin: 0 auto;
      }

      .nutrient-benefits {
        justify-content: center;
      }
    }

    .meal-grid {
      grid-template-columns: 1fr;
    }

    .meal-card .meal-content .meal-details .nutrition-facts {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  // 训练计划响应式
  .training-programs {
    grid-template-columns: 1fr;
  }

  .program-card {
    .program-header {
      flex-direction: column;
      text-align: center;

      .program-icon {
        margin: 0 auto;
      }

      .program-meta {
        justify-content: center;
      }
    }

    .program-content .program-actions {
      flex-direction: column;
    }
  }
}
</style>
