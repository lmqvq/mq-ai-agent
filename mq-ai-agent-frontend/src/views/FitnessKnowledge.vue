<template>
  <div class="fitness-knowledge">
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <icon-left />
      <span>返回</span>
    </div>

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
                <LazyImage 
                  :src="item.image" 
                  :alt="item.title"
                  placeholder="#667eea"
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
                <button class="ace-btn ace-btn--secondary ace-btn--block ace-btn--round" @click="viewKnowledge(item)">
                  了解更多
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 动作指导 -->
        <div v-if="activeCategory === 'exercises'" class="content-section">
          <div class="exercise-categories">
            <div v-for="exerciseType in exerciseTypes" :key="exerciseType.id" class="exercise-type">
              <h3>{{ exerciseType.name }}</h3>
              <div class="knowledge-grid">
                <div v-for="exercise in exerciseType.exercises" :key="exercise.id" class="knowledge-card">
                  <div class="card-image">
                    <LazyImage 
                      :src="exercise.image" 
                      :alt="exercise.name"
                      placeholder="#667eea"
                    />
                    <div class="card-overlay">
                      <div class="difficulty-badge" :class="exercise.difficulty">
                        {{ exercise.difficulty }}
                      </div>
                      <div v-if="isAdmin" class="admin-edit-btn" @click.stop="editItem('exercises', exercise)">
                        <icon-edit />
                      </div>
                    </div>
                  </div>
                  <div class="card-content">
                    <h3>{{ exercise.name }}</h3>
                    <p>{{ exercise.description }}</p>
                    <div class="card-meta">
                      <span><icon-trophy />{{ exercise.muscleGroup }}</span>
                      <span><icon-fire />{{ exercise.difficulty }}</span>
                    </div>
                    <button class="ace-btn ace-btn--secondary ace-btn--block ace-btn--round" @click="viewExerciseDetail(exercise)">
                      了解更多
                    </button>
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
                      <LazyImage 
                        :src="meal.image" 
                        :alt="meal.name"
                        placeholder="#ff7875"
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
            decoding="async"
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

    <!-- 动作指导详情弹窗 -->
    <a-modal 
      v-model:visible="showVideoModal" 
      :footer="false" 
      width="900px"
      :body-style="{ padding: 0 }"
      modal-class="knowledge-modal"
    >
      <template #title>
        <div class="modal-title-simple">
          {{ selectedExercise?.name }}
        </div>
      </template>
      <div v-if="selectedExercise" class="knowledge-detail">
        <div class="detail-image-wrapper">
          <img 
            :src="selectedExercise.image" 
            :alt="selectedExercise.name" 
            class="detail-image"
            decoding="async"
          />
        </div>
        <div class="detail-content">
          <!-- 动作元数据 -->
          <div class="article-meta">
            <span class="difficulty-tag" :class="selectedExercise.difficulty">
              {{ selectedExercise.difficulty }}
            </span>
            <span class="meta-item">
              <icon-trophy /> {{ selectedExercise.muscleGroup }}
            </span>
            <span class="meta-item">
              <icon-fire /> {{ selectedExercise.category }}
            </span>
          </div>

          <!-- 动作说明 -->
          <div class="content-section">
            <h4 class="section-title">
              <icon-book /> 动作说明
            </h4>
            <div class="content-text">
              <p>{{ selectedExercise.description }}</p>
            </div>
          </div>
          
          <!-- 动作要领 -->
          <div class="content-section tips-section">
            <h4 class="section-title">
              <icon-trophy /> 动作要领
            </h4>
            <ul class="tips-list">
              <li v-for="(instruction, index) in selectedExercise.instructions" :key="instruction" class="tip-item">
                <span class="tip-number">{{ index + 1 }}</span>
                <span class="tip-text">{{ instruction }}</span>
              </li>
            </ul>
          </div>

          <!-- 注意事项 -->
          <div class="content-section tips-section" style="margin-top: 20px;">
            <h4 class="section-title">
              <icon-fire /> 注意事项
            </h4>
            <ul class="tips-list">
              <li v-for="(tip, index) in selectedExercise.tips" :key="tip" class="tip-item">
                <span class="tip-number">{{ index + 1 }}</span>
                <span class="tip-text">{{ tip }}</span>
              </li>
            </ul>
          </div>

          <div class="detail-footer">
            <div class="footer-stats">
              <div class="stat-item">
                <icon-trophy class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedExercise.muscleGroup }}</div>
                  <div class="stat-label">目标肌群</div>
                </div>
              </div>
              <div class="stat-item">
                <icon-fire class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedExercise.difficulty }}</div>
                  <div class="stat-label">难度等级</div>
                </div>
              </div>
              <div class="stat-item">
                <icon-heart class="stat-icon" />
                <div class="stat-info">
                  <div class="stat-value">{{ selectedExercise.category }}</div>
                  <div class="stat-label">训练分类</div>
                </div>
              </div>
            </div>
            <div class="action-buttons">
              <a-button type="outline" size="large" @click="shareKnowledge">
                <icon-share-alt /> 分享动作
              </a-button>
              <a-button type="primary" size="large" @click="printKnowledge">
                <icon-printer /> 打印保存
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 基础知识编辑弹窗 -->
    <a-modal
      v-model:visible="showEditModal"
      :title="editModalTitle"
      width="700px"
      modal-class="knowledge-edit-modal"
      @cancel="cancelEdit"
      @ok="saveEdit"
      :ok-loading="editLoading"
      ok-text="确定"
      cancel-text="取消"
    >
      <div v-if="editForm" class="edit-form-content">
        <!-- 基础知识编辑 -->
        <template v-if="editType === 'basics'">
          <a-form :model="editForm" layout="vertical">
            <a-form-item field="title" label="标题" :rules="[{ required: true, message: '请输入标题' }]">
              <a-input v-model="editForm.title" placeholder="请输入标题" />
            </a-form-item>
            <a-form-item field="description" label="简短描述">
              <a-textarea v-model="editForm.description" placeholder="请输入简短描述" :max-length="200" show-word-limit />
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="difficulty" label="难度">
                  <a-select v-model="editForm.difficulty" placeholder="选择难度">
                    <a-option value="初级">初级</a-option>
                    <a-option value="中级">中级</a-option>
                    <a-option value="高级">高级</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="readTime" label="阅读时长(分钟)">
                  <a-input-number v-model="editForm.readTime" :min="1" :max="60" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="sortOrder" label="排序">
                  <a-input-number v-model="editForm.sortOrder" :min="0" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="image" label="封面图片">
              <div class="image-upload-wrapper">
                <a-upload
                  :custom-request="(option) => customUpload(option, 'basics')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <template #upload-button>
                    <div class="upload-trigger">
                      <img v-if="editForm.image" :src="editForm.image" class="preview-image" />
                      <div v-else class="upload-placeholder">
                        <icon-plus />
                        <div>上传图片</div>
                      </div>
                    </div>
                  </template>
                </a-upload>
                <a-input v-model="editForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
              </div>
            </a-form-item>
            <a-form-item field="content" label="详细内容">
              <a-textarea v-model="editForm.content" placeholder="请输入详细内容" :auto-size="{ minRows: 4, maxRows: 8 }" />
            </a-form-item>
            <a-form-item field="tipsText" label="提示列表(每行一条)">
              <a-textarea v-model="editForm.tipsText" placeholder="每行输入一条提示" :auto-size="{ minRows: 3, maxRows: 6 }" />
            </a-form-item>
          </a-form>
        </template>

        <!-- 动作指导编辑 -->
        <template v-else-if="editType === 'exercises'">
          <a-form :model="editForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="name" label="动作名称" :rules="[{ required: true, message: '请输入名称' }]">
                  <a-input v-model="editForm.name" placeholder="请输入名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="category" label="分类" :rules="[{ required: true, message: '请选择分类' }]">
                  <a-select v-model="editForm.category" placeholder="选择分类" allow-create>
                    <a-option value="胸部训练">胸部训练</a-option>
                    <a-option value="背部训练">背部训练</a-option>
                    <a-option value="腿部训练">腿部训练</a-option>
                    <a-option value="肩部训练">肩部训练</a-option>
                    <a-option value="手臂训练">手臂训练</a-option>
                    <a-option value="核心训练">核心训练</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="description" label="简短描述">
              <a-textarea v-model="editForm.description" placeholder="请输入简短描述" :max-length="200" show-word-limit />
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="muscleGroup" label="目标肌群">
                  <a-input v-model="editForm.muscleGroup" placeholder="如: 胸肌" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="difficulty" label="难度">
                  <a-select v-model="editForm.difficulty" placeholder="选择难度">
                    <a-option value="初级">初级</a-option>
                    <a-option value="中级">中级</a-option>
                    <a-option value="高级">高级</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="sortOrder" label="排序">
                  <a-input-number v-model="editForm.sortOrder" :min="0" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="image" label="动作图片">
              <div class="image-upload-wrapper">
                <a-upload
                  :custom-request="(option) => customUpload(option, 'exercise')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <template #upload-button>
                    <div class="upload-trigger">
                      <img v-if="editForm.image" :src="editForm.image" class="preview-image" />
                      <div v-else class="upload-placeholder">
                        <icon-plus />
                        <div>上传图片</div>
                      </div>
                    </div>
                  </template>
                </a-upload>
                <a-input v-model="editForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
              </div>
            </a-form-item>
            <a-form-item field="instructionsText" label="动作步骤(每行一步)">
              <a-textarea v-model="editForm.instructionsText" placeholder="每行输入一个步骤" :auto-size="{ minRows: 3, maxRows: 6 }" />
            </a-form-item>
            <a-form-item field="tipsText" label="注意事项(每行一条)">
              <a-textarea v-model="editForm.tipsText" placeholder="每行输入一条注意事项" :auto-size="{ minRows: 3, maxRows: 6 }" />
            </a-form-item>
          </a-form>
        </template>

        <!-- 营养元素编辑 -->
        <template v-else-if="editType === 'nutrients'">
          <a-form :model="editForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
                  <a-input v-model="editForm.name" placeholder="请输入名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="color" label="显示颜色">
                  <a-input v-model="editForm.color" placeholder="如: #ff7875">
                    <template #prepend>
                      <div class="color-input-preview" :style="{ backgroundColor: editForm.color }"></div>
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="description" label="描述">
              <a-textarea v-model="editForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="icon" label="图标标识">
                  <a-select v-model="editForm.icon" placeholder="选择图标">
                    <a-option value="icon-fire">icon-fire</a-option>
                    <a-option value="icon-heart">icon-heart</a-option>
                    <a-option value="icon-star">icon-star</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="sortOrder" label="排序">
                  <a-input-number v-model="editForm.sortOrder" :min="0" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="benefitsText" label="益处列表(每行一条)">
              <a-textarea v-model="editForm.benefitsText" placeholder="每行输入一个益处" :auto-size="{ minRows: 3, maxRows: 6 }" />
            </a-form-item>
          </a-form>
        </template>

        <!-- 饮食计划编辑 -->
        <template v-else-if="editType === 'meals'">
          <a-form :model="editForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
                  <a-input v-model="editForm.name" placeholder="请输入名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="mealType" label="餐食类型" :rules="[{ required: true, message: '请选择类型' }]">
                  <a-select v-model="editForm.mealType" placeholder="选择类型">
                    <a-option value="早餐">早餐</a-option>
                    <a-option value="午餐">午餐</a-option>
                    <a-option value="晚餐">晚餐</a-option>
                    <a-option value="加餐">加餐</a-option>
                    <a-option value="训练前">训练前</a-option>
                    <a-option value="训练后">训练后</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="description" label="描述">
              <a-textarea v-model="editForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
            </a-form-item>
            <a-form-item field="image" label="餐食图片">
              <div class="image-upload-wrapper">
                <a-upload
                  :custom-request="(option) => customUpload(option, 'meal')"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <template #upload-button>
                    <div class="upload-trigger">
                      <img v-if="editForm.image" :src="editForm.image" class="preview-image" />
                      <div v-else class="upload-placeholder">
                        <icon-plus />
                        <div>上传图片</div>
                      </div>
                    </div>
                  </template>
                </a-upload>
                <a-input v-model="editForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
              </div>
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item field="calories" label="卡路里(kcal)">
                  <a-input-number v-model="editForm.calories" :min="0" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="protein" label="蛋白质(g)">
                  <a-input-number v-model="editForm.protein" :min="0" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="carbs" label="碳水(g)">
                  <a-input-number v-model="editForm.carbs" :min="0" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item field="fat" label="脂肪(g)">
                  <a-input-number v-model="editForm.fat" :min="0" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="editForm.sortOrder" :min="0" style="width: 120px" />
            </a-form-item>
          </a-form>
        </template>

        <!-- 训练计划编辑 -->
        <template v-else-if="editType === 'programs'">
          <a-form :model="editForm" layout="vertical">
            <a-form-item field="name" label="计划名称" :rules="[{ required: true, message: '请输入名称' }]">
              <a-input v-model="editForm.name" placeholder="请输入名称" />
            </a-form-item>
            <a-form-item field="description" label="描述">
              <a-textarea v-model="editForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="type" label="类型">
                  <a-select v-model="editForm.type" placeholder="选择类型" allow-create>
                    <a-option value="beginner">beginner</a-option>
                    <a-option value="intermediate">intermediate</a-option>
                    <a-option value="advanced">advanced</a-option>
                    <a-option value="fat-loss">fat-loss</a-option>
                    <a-option value="strength">strength</a-option>
                    <a-option value="bodyweight">bodyweight</a-option>
                    <a-option value="female">female</a-option>
                    <a-option value="busy">busy</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="icon" label="图标">
                  <a-select v-model="editForm.icon" placeholder="选择图标">
                    <a-option value="icon-user">icon-user</a-option>
                    <a-option value="icon-fire">icon-fire</a-option>
                    <a-option value="icon-trophy">icon-trophy</a-option>
                    <a-option value="icon-heart">icon-heart</a-option>
                    <a-option value="icon-clock-circle">icon-clock-circle</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="sortOrder" label="排序">
                  <a-input-number v-model="editForm.sortOrder" :min="0" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item field="duration" label="持续时间">
                  <a-input v-model="editForm.duration" placeholder="如: 8周" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="intensity" label="强度">
                  <a-input v-model="editForm.intensity" placeholder="如: 中-高等" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item field="level" label="适合级别">
                  <a-select v-model="editForm.level" placeholder="选择级别">
                    <a-option value="初级">初级</a-option>
                    <a-option value="中级">中级</a-option>
                    <a-option value="中高级">中高级</a-option>
                    <a-option value="初中级">初中级</a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="scheduleText" label="训练安排 (JSON格式)">
              <a-textarea v-model="editForm.scheduleText" placeholder='[{"day":"周一","content":"训练内容"}]' :auto-size="{ minRows: 4, maxRows: 8 }" />
            </a-form-item>
          </a-form>
        </template>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { 
  IconBook, IconTrophy, IconFire, IconHeart, IconUser, IconClockCircle, 
  IconEye, IconPlayArrow, IconShareAlt, IconPrinter, IconStar, IconSettings, IconEdit,
  IconLeft, IconImage, IconDelete, IconPlus
} from '@arco-design/web-vue/es/icon';
import ApiService from '@/services/api';
import LazyImage from '@/components/LazyImage.vue';

export default {
  name: 'FitnessKnowledge',
  components: {
    IconBook, IconTrophy, IconFire, IconHeart, IconUser, IconClockCircle, 
    IconEye, IconPlayArrow, IconShareAlt, IconPrinter, IconStar, IconSettings, IconEdit,
    IconLeft, IconImage, IconDelete, IconPlus,
    LazyImage
  },
  setup() {
    const router = useRouter();
    const activeCategory = ref('basics');
    const showKnowledgeModal = ref(false);
    const showVideoModal = ref(false);
    const showEditModal = ref(false);
    const selectedKnowledge = ref(null);
    const selectedExercise = ref(null);
    const editType = ref('');
    const editLoading = ref(false);
    const loading = ref(false);

    // 编辑表单数据
    const editForm = reactive({
      id: null,
      // basics
      title: '', description: '', image: '', difficulty: '初级', readTime: 5, content: '', tipsText: '', sortOrder: 0, views: 0,
      // exercises
      name: '', category: '', muscleGroup: '', instructionsText: '',
      // nutrients
      color: '#ff7875', icon: 'icon-fire', benefitsText: '',
      // meals
      mealType: '午餐', calories: 0, protein: 0, carbs: 0, fat: 0,
      // programs
      type: 'beginner', duration: '', intensity: '', level: '初级', scheduleText: '[]'
    });

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
    
    // 数据缓存标记 - 避免重复加载
    const dataLoaded = ref(false);
    // 缓存时间戳
    const cacheTimestamp = ref(0);
    // 缓存有效期（5分钟）
    const CACHE_DURATION = 5 * 60 * 1000;

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

    // 初始化加载所有数据（带缓存检查）
    const loadAllData = async (forceRefresh = false) => {
      // 检查缓存是否有效
      const now = Date.now();
      if (!forceRefresh && dataLoaded.value && (now - cacheTimestamp.value) < CACHE_DURATION) {
        console.log('[FitnessKnowledge] 使用缓存数据');
        return;
      }
      
      loading.value = true;
      try {
        await Promise.all([
          loadBasicsKnowledge(),
          loadExercises(),
          loadNutrients(),
          loadMealPlans(),
          loadTrainingPrograms()
        ]);
        // 标记数据已加载并记录时间戳
        dataLoaded.value = true;
        cacheTimestamp.value = Date.now();
      } catch (error) {
        Message.error('加载数据失败，请刷新重试');
      } finally {
        loading.value = false;
      }
    };
    
    // 强制刷新数据（编辑后调用）
    const refreshData = () => {
      loadAllData(true);
    };

    onMounted(() => {
      loadAllData();
    });
    
    // 组件卸载时清理
    onUnmounted(() => {
      // 关闭所有打开的弹窗
      showKnowledgeModal.value = false;
      showVideoModal.value = false;
      showEditModal.value = false;
      selectedKnowledge.value = null;
      selectedExercise.value = null;
    });
    
    // keep-alive 组件激活时检查缓存
    onActivated(() => {
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

    // 查看动作详情（与基础知识保持一致的弹窗样式）
    const viewExerciseDetail = (exercise) => {
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

    // 返回主页
    const goBack = () => {
      router.push('/');
    };

    // 跳转到管理页面
    const goToManage = () => {
      router.push('/admin/knowledge');
    };

    // 重置编辑表单
    const resetEditForm = () => {
      Object.assign(editForm, {
        id: null,
        title: '', description: '', image: '', difficulty: '初级', readTime: 5, content: '', tipsText: '', sortOrder: 0, views: 0,
        name: '', category: '', muscleGroup: '', instructionsText: '',
        color: '#ff7875', icon: 'icon-fire', benefitsText: '',
        mealType: '午餐', calories: 0, protein: 0, carbs: 0, fat: 0,
        type: 'beginner', duration: '', intensity: '', level: '初级', scheduleText: '[]'
      });
    };

    // 编辑弹窗标题
    const editModalTitle = computed(() => {
      const titles = {
        basics: '编辑基础知识',
        exercises: '编辑动作指导',
        nutrients: '编辑营养知识',
        meals: '编辑饮食计划',
        programs: '编辑训练计划'
      };
      return titles[editType.value] || '编辑';
    });

    // 编辑单个项目 - 打开弹窗编辑
    const editItem = (type, item) => {
      editType.value = type;
      resetEditForm();
      
      // 根据类型填充表单数据
      switch (type) {
        case 'basics':
          Object.assign(editForm, {
            id: item.id,
            title: item.title || '',
            description: item.description || '',
            image: item.image || '',
            difficulty: item.difficulty || '初级',
            readTime: item.readTime || 5,
            content: item.content || '',
            tipsText: Array.isArray(item.tips) ? item.tips.join('\n') : (item.tips || ''),
            sortOrder: item.sortOrder || 0,
            views: item.views || 0
          });
          break;
        case 'exercises':
          Object.assign(editForm, {
            id: item.id,
            name: item.name || '',
            category: item.category || '',
            description: item.description || '',
            image: item.image || '',
            muscleGroup: item.muscleGroup || '',
            difficulty: item.difficulty || '初级',
            instructionsText: Array.isArray(item.instructions) ? item.instructions.join('\n') : (item.instructions || ''),
            tipsText: Array.isArray(item.tips) ? item.tips.join('\n') : (item.tips || ''),
            sortOrder: item.sortOrder || 0
          });
          break;
        case 'nutrients':
          Object.assign(editForm, {
            id: item.id,
            name: item.name || '',
            description: item.description || '',
            color: item.color || '#ff7875',
            icon: item.icon || 'icon-fire',
            benefitsText: Array.isArray(item.benefits) ? item.benefits.join('\n') : (item.benefits || ''),
            sortOrder: item.sortOrder || 0
          });
          break;
        case 'meals':
          Object.assign(editForm, {
            id: item.id,
            name: item.name || '',
            mealType: item.mealType || '午餐',
            description: item.description || '',
            image: item.image || '',
            calories: item.calories || 0,
            protein: item.protein || 0,
            carbs: item.carbs || 0,
            fat: item.fat || 0,
            sortOrder: item.sortOrder || 0
          });
          break;
        case 'programs':
          Object.assign(editForm, {
            id: item.id,
            name: item.name || '',
            description: item.description || '',
            type: item.type || 'beginner',
            icon: item.icon || 'icon-user',
            duration: item.duration || '',
            intensity: item.intensity || '',
            level: item.level || '初级',
            scheduleText: Array.isArray(item.schedule) ? JSON.stringify(item.schedule, null, 2) : (item.schedule || '[]'),
            sortOrder: item.sortOrder || 0
          });
          break;
      }
      
      showEditModal.value = true;
    };

    // 取消编辑
    const cancelEdit = () => {
      showEditModal.value = false;
      resetEditForm();
      editType.value = '';
    };

    // 保存编辑
    const saveEdit = async () => {
      if (!editForm.id) return;
      
      editLoading.value = true;
      try {
        let res;
        let data = {};
        
        // 根据类型构建提交数据（与 KnowledgeManage.vue 保持一致，传递数组而非 JSON 字符串）
        switch (editType.value) {
          case 'basics':
            data = {
              id: editForm.id,
              title: editForm.title,
              description: editForm.description,
              image: editForm.image,
              difficulty: editForm.difficulty,
              readTime: editForm.readTime,
              content: editForm.content,
              tips: editForm.tipsText.split('\n').filter(t => t.trim()),
              sortOrder: editForm.sortOrder
            };
            res = await ApiService.updateKnowledgeBasics(data);
            if (res.code === 0) {
              await loadBasicsKnowledge();
              Message.success('保存成功');
            } else {
              Message.error(res.message || '保存失败');
            }
            break;
          case 'exercises':
            data = {
              id: editForm.id,
              name: editForm.name,
              category: editForm.category,
              description: editForm.description,
              image: editForm.image,
              muscleGroup: editForm.muscleGroup,
              difficulty: editForm.difficulty,
              instructions: editForm.instructionsText.split('\n').filter(t => t.trim()),
              tips: editForm.tipsText.split('\n').filter(t => t.trim()),
              sortOrder: editForm.sortOrder
            };
            res = await ApiService.updateKnowledgeExercise(data);
            if (res.code === 0) {
              await loadExercises();
              Message.success('保存成功');
            } else {
              Message.error(res.message || '保存失败');
            }
            break;
          case 'nutrients':
            data = {
              id: editForm.id,
              name: editForm.name,
              description: editForm.description,
              color: editForm.color,
              icon: editForm.icon,
              benefits: editForm.benefitsText.split('\n').filter(t => t.trim()),
              sortOrder: editForm.sortOrder
            };
            res = await ApiService.updateKnowledgeNutrient(data);
            if (res.code === 0) {
              await loadNutrients();
              Message.success('保存成功');
            } else {
              Message.error(res.message || '保存失败');
            }
            break;
          case 'meals':
            data = {
              id: editForm.id,
              name: editForm.name,
              mealType: editForm.mealType,
              description: editForm.description,
              image: editForm.image,
              calories: editForm.calories,
              protein: editForm.protein,
              carbs: editForm.carbs,
              fat: editForm.fat,
              sortOrder: editForm.sortOrder
            };
            res = await ApiService.updateKnowledgeMeal(data);
            if (res.code === 0) {
              await loadMealPlans();
              Message.success('保存成功');
            } else {
              Message.error(res.message || '保存失败');
            }
            break;
          case 'programs': {
            let schedule;
            try {
              schedule = JSON.parse(editForm.scheduleText);
            } catch {
              Message.error('训练安排JSON格式错误');
              editLoading.value = false;
              return;
            }
            data = {
              id: editForm.id,
              name: editForm.name,
              description: editForm.description,
              type: editForm.type,
              icon: editForm.icon,
              duration: editForm.duration,
              intensity: editForm.intensity,
              level: editForm.level,
              schedule: schedule,
              sortOrder: editForm.sortOrder
            };
            res = await ApiService.updateKnowledgeProgram(data);
            if (res.code === 0) {
              await loadTrainingPrograms();
              Message.success('保存成功');
            } else {
              Message.error(res.message || '保存失败');
            }
            break;
          }
        }
        
        showEditModal.value = false;
        resetEditForm();
        editType.value = '';
      } catch (error) {
        console.error('保存失败:', error);
        Message.error('保存失败，请重试');
      } finally {
        editLoading.value = false;
      }
    };

    // 图片上传
    const customUpload = async (option, type) => {
      try {
        const res = await ApiService.uploadKnowledgeImage(option.fileItem.file, type);
        if (res.code === 0) {
          editForm.image = res.data;
          Message.success('上传成功');
          option.onSuccess();
        } else {
          Message.error(res.message || '上传失败');
          option.onError();
        }
      } catch (error) {
        Message.error('上传失败');
        option.onError();
      }
    };

    return {
      activeCategory,
      showKnowledgeModal,
      showVideoModal,
      showEditModal,
      selectedKnowledge,
      selectedExercise,
      editForm,
      editType,
      editLoading,
      editModalTitle,
      categories,
      basicsKnowledge,
      exerciseTypes,
      nutrients,
      mealPlans,
      trainingPrograms,
      viewKnowledge,
      playExerciseVideo,
      viewExerciseDetail,
      startProgram,
      viewProgramDetails,
      handleImageError,
      shareKnowledge,
      printKnowledge,
      isAdmin,
      goBack,
      goToManage,
      editItem,
      cancelEdit,
      saveEdit,
      customUpload,
      refreshData
    };
  }
};
</script>

<style lang="scss" scoped>
/* ================================================================
   FitnessKnowledge.vue 样式 - 使用CSS变量实现主题切换
================================================================ */

// 图片加载优化
img {
  background: #667eea;
}

.fitness-knowledge {
  min-height: 100vh;
  background: var(--theme-bg-page);
  padding: 20px;
  padding-bottom: 80px;
  position: relative;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--theme-bg-card);
  border-radius: 12px;
  cursor: pointer;
  // 只对 transform 和 color 做动画（GPU加速）
  transition: transform 0.25s ease, color 0.25s ease;
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
    color: var(--theme-color-primary);
  }
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
  color: var(--theme-text-primary);

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
      color: var(--theme-text-primary);
    }

    p {
      font-size: 16px;
      color: var(--theme-text-secondary);
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
    background: var(--theme-bg-card-hover);
    padding: 8px;
    border-radius: 16px;

    .nav-tab {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      border-radius: 12px;
      color: var(--theme-text-secondary);
      cursor: pointer;
      // GPU 加速动画
      transition: color 0.2s ease, background-color 0.2s ease;
      font-weight: 500;

      :deep(svg) {
        width: 16px;
        height: 16px;
      }

      &:hover {
        color: var(--theme-text-primary);
        background: var(--theme-bg-hover);
      }

      &.active {
        color: #ffffff;
        background: var(--theme-color-primary);
        box-shadow: var(--theme-shadow-md);
      }
    }
  }
}

.knowledge-content {
  background: var(--theme-bg-card);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid var(--theme-border-secondary);
  // 使用 content-visibility 优化渲染
  contain: layout style;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  // 滿足条件时跳过屏幕外内容渲染
  content-visibility: auto;
  contain-intrinsic-size: 0 500px;
}

.knowledge-card {
  border: 1px solid var(--theme-border-primary);
  border-radius: 16px;
  overflow: hidden;
  background: var(--theme-bg-card);
  cursor: pointer;
  position: relative;
  // 简化阴影
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  // 移除 hover 动画，提升滚动性能
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .card-image {
    position: relative;
    height: 220px;
    overflow: hidden;
    background: #667eea;

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
        // 移除 backdrop-filter - 严重影响性能

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
  // GPU 加速
  transform: translateZ(0);
  transition: transform 0.2s ease, background-color 0.2s ease;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

  :deep(svg) {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: #764ba2;
    transform: scale(1.1) translateZ(0);
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
    background: var(--theme-bg-card);

    h3 {
      margin: 0 0 12px 0;
      font-size: 20px;
      font-weight: 600;
      color: var(--theme-text-primary);
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    p {
      margin: 0 0 16px 0;
      color: var(--theme-text-secondary);
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
      border-top: 1px solid var(--theme-border-secondary);
      border-bottom: 1px solid var(--theme-border-secondary);

      span {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: var(--theme-text-muted);
        font-weight: 500;

        :deep(svg) {
          width: 14px;
          height: 14px;
          color: var(--theme-color-primary);
        }
      }
    }

  }
}

.exercise-categories {
  .exercise-type {
    margin-bottom: 40px;

    &:last-child {
      margin-bottom: 0;
    }

    h3 {
      margin: 0 0 20px 0;
      font-size: 20px;
      font-weight: 600;
      color: var(--theme-text-primary);
      padding-bottom: 10px;
      border-bottom: 2px solid var(--theme-color-primary);
      display: inline-block;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .knowledge-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

// 知识详情弹窗样式 - 跟随系统主题
:deep(.knowledge-modal) {
  .arco-modal-header {
    padding: 20px 28px !important;
    border-bottom: 1px solid var(--theme-border-secondary);
  }
}

.modal-title-simple {
  font-size: 22px;
  font-weight: 600;
  color: var(--theme-text-primary);
  line-height: 1.4;
}

.knowledge-detail {
  .detail-image-wrapper {
    width: 100%;
    overflow: hidden;
    position: relative;
    background: transparent;
  }

  .detail-image {
    width: 100%;
    height: auto;
    display: block;
  }

  .detail-content {
    padding: 28px;
    background: var(--theme-bg-card);

    // 文章元数据
    .article-meta {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
      padding: 16px 20px;
      background: var(--theme-bg-card-hover);
      border-radius: 12px;
      border: 1px solid var(--theme-border-secondary);
      margin-bottom: 28px;

      .difficulty-tag {
        padding: 6px 14px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 600;

        &.初级 {
          background: rgba(34, 197, 94, 0.12);
          color: #16a34a;
          border: 1px solid rgba(34, 197, 94, 0.25);
        }

        &.中级 {
          background: rgba(245, 158, 11, 0.12);
          color: #d97706;
          border: 1px solid rgba(245, 158, 11, 0.25);
        }

        &.高级 {
          background: rgba(239, 68, 68, 0.12);
          color: #dc2626;
          border: 1px solid rgba(239, 68, 68, 0.25);
        }
      }

      .meta-item {
        display: flex;
        align-items: center;
        gap: 6px;
        color: var(--theme-text-secondary);
        font-size: 13px;
        font-weight: 500;

        :deep(svg) {
          width: 14px;
          height: 14px;
          color: var(--theme-text-muted);
        }
      }
    }

    .content-section {
      margin-bottom: 28px;

      &:last-of-type {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 10px;
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--theme-text-primary);

        :deep(svg) {
          width: 18px;
          height: 18px;
          color: var(--theme-color-primary);
        }
      }

      .content-text {
        p {
          margin: 0;
          font-size: 15px;
          line-height: 1.8;
          color: var(--theme-text-secondary);
          text-align: justify;
        }
      }
    }

    .tips-section {
      background: var(--theme-bg-card-hover);
      padding: 20px;
      border-radius: 12px;
      border: 1px solid var(--theme-border-secondary);

      .section-title {
        margin-bottom: 16px;
      }

      .tips-list {
        margin: 0;
        padding: 0;
        list-style: none;

        .tip-item {
          display: flex;
          align-items: flex-start;
          gap: 14px;
          margin-bottom: 12px;
          padding: 14px 16px;
          background: var(--theme-bg-card);
          border-radius: 10px;
          border: 1px solid var(--theme-border-secondary);

          &:last-child {
            margin-bottom: 0;
          }

          .tip-number {
            flex-shrink: 0;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
          }

          .tip-text {
            flex: 1;
            font-size: 14px;
            line-height: 1.6;
            color: var(--theme-text-primary);
          }
        }
      }
    }

    .detail-footer {
      margin-top: 28px;
      padding-top: 24px;
      border-top: 1px solid var(--theme-border-secondary);

      .footer-stats {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        margin-bottom: 20px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 14px 16px;
          background: var(--theme-bg-card-hover);
          border-radius: 12px;
          border: 1px solid var(--theme-border-secondary);

          // 优化图标配色 - 使用渐变边框而非纯紫色填充
          .stat-icon {
            width: 44px;
            height: 44px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--theme-bg-card);
            border-radius: 12px;
            border: 1.5px solid var(--theme-color-primary);
            flex-shrink: 0;
            position: relative;

            // 微妙的内阴影
            &::before {
              content: '';
              position: absolute;
              inset: 0;
              border-radius: 11px;
              background: linear-gradient(135deg, 
                rgba(var(--primary-rgb), 0.08) 0%, 
                transparent 50%
              );
            }

            :deep(svg) {
              width: 20px;
              height: 20px;
              color: var(--theme-color-primary);
              position: relative;
              z-index: 1;
            }
          }

          .stat-info {
            flex: 1;

            .stat-value {
              font-size: 16px;
              font-weight: 600;
              color: var(--theme-text-primary);
              margin-bottom: 2px;
            }

            .stat-label {
              font-size: 11px;
              color: var(--theme-text-muted);
            }
          }
        }
      }

      .action-buttons {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;

        .arco-btn {
          height: 46px;
          font-size: 14px;
          font-weight: 500;
          border-radius: 10px;

          :deep(svg) {
            margin-right: 6px;
          }

          &[type="outline"] {
            background: var(--theme-bg-card);
            border: 1.5px solid var(--theme-border-primary);
            color: var(--theme-text-primary);

            &:hover {
              border-color: var(--theme-color-primary);
              color: var(--theme-color-primary);
            }
          }

          &[type="primary"] {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            border: none;
            color: #fff;
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);

            &:hover {
              box-shadow: 0 6px 16px rgba(16, 185, 129, 0.35);
            }
          }
        }
      }
    }
  }
}

// 深色模式下的难度标签色彩调整
html.dark-theme {
  .knowledge-detail .detail-content .article-meta {
    .difficulty-tag {
      &.初级 {
        background: rgba(34, 197, 94, 0.15);
        color: #4ade80;
        border-color: rgba(34, 197, 94, 0.3);
      }

      &.中级 {
        background: rgba(251, 191, 36, 0.15);
        color: #fbbf24;
        border-color: rgba(251, 191, 36, 0.3);
      }

      &.高级 {
        background: rgba(239, 68, 68, 0.15);
        color: #f87171;
        border-color: rgba(239, 68, 68, 0.3);
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
      color: var(--theme-text-primary);
      margin: 0 0 24px 0;
      padding-bottom: 12px;
      border-bottom: 3px solid var(--theme-color-primary);
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
    background: var(--theme-bg-card);
    border-radius: 16px;
    border: 1px solid var(--theme-border-primary);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
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
        color: var(--theme-text-primary);
      }

      p {
        margin: 0 0 16px 0;
        font-size: 14px;
        color: var(--theme-text-secondary);
        line-height: 1.6;
      }

      .nutrient-benefits {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .benefit-tag {
          padding: 4px 12px;
          background: var(--theme-color-primary-light);
          border-radius: 20px;
          font-size: 12px;
          color: var(--theme-color-primary);
          font-weight: 500;
        }
      }
    }
  }

  .meal-plans {
    h3 {
      font-size: 22px;
      font-weight: 600;
      color: var(--theme-text-primary);
      margin: 0 0 24px 0;
      padding-bottom: 12px;
      border-bottom: 3px solid var(--theme-color-primary);
      display: inline-block;
    }
  }

  .meal-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .meal-card {
    background: var(--theme-bg-card);
    border-radius: 16px;
    border: 1px solid var(--theme-border-primary);
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .meal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      background: linear-gradient(135deg, var(--theme-color-primary) 0%, #764ba2 100%);
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
        background: #ff7875;
      }

      .meal-details {
        p {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: var(--theme-text-secondary);
          line-height: 1.6;
        }

        .nutrition-facts {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 12px;

          .fact {
            text-align: center;
            padding: 12px 8px;
            background: var(--theme-bg-card-hover);
            border-radius: 12px;

            .label {
              display: block;
              font-size: 12px;
              color: var(--theme-text-secondary);
              margin-bottom: 4px;
            }

            .value {
              display: block;
              font-size: 14px;
              font-weight: 600;
              color: var(--theme-color-primary);
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
  background: var(--theme-bg-card);
  border-radius: 16px;
  border: 1px solid var(--theme-border-primary);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .program-header {
    display: flex;
    gap: 20px;
    padding: 24px;
    background: var(--theme-bg-card-hover);
    border-bottom: 1px solid var(--theme-border-secondary);

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
        color: var(--theme-text-primary);
      }

      p {
        margin: 0 0 12px 0;
        font-size: 14px;
        color: var(--theme-text-secondary);
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
          color: var(--theme-text-muted);
          font-weight: 500;

          :deep(svg) {
            width: 14px;
            height: 14px;
            color: var(--theme-color-primary);
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
        color: var(--theme-text-primary);
        display: flex;
        align-items: center;
        gap: 8px;

        &::before {
          content: '';
          width: 4px;
          height: 16px;
          background: linear-gradient(135deg, var(--theme-color-primary) 0%, #764ba2 100%);
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
          background: var(--theme-bg-card-hover);
          border-radius: 12px;
          border: 1px solid var(--theme-border-secondary);

          .day-label {
            flex-shrink: 0;
            min-width: 60px;
            font-weight: 600;
            color: var(--theme-color-primary);
            font-size: 14px;
          }

          .day-content {
            flex: 1;
            font-size: 14px;
            color: var(--theme-text-primary);
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

        &[type="primary"] {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;
        }

        &[type="outline"] {
          border: 2px solid #667eea;
          color: #667eea;
          background: transparent;
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

// 编辑弹窗样式 - 兼容亮色/深色模式
:deep(.knowledge-edit-modal) {
  .arco-modal {
    background: var(--color-bg-2);
  }

  .arco-modal-header {
    background: var(--color-bg-2);
    border-bottom: 1px solid var(--color-border);
  }

  .arco-modal-title {
    color: var(--color-text-1);
  }

  .arco-modal-body {
    background: var(--color-bg-2);
    padding: 20px 24px;
    max-height: 70vh;
    overflow-y: auto;
  }

  .arco-modal-footer {
    background: var(--color-bg-2);
    border-top: 1px solid var(--color-border);
  }

  .arco-form-item-label {
    color: var(--color-text-1);
  }

  .arco-input,
  .arco-textarea,
  .arco-select-view-single,
  .arco-input-number {
    background: var(--color-fill-2);
    border-color: var(--color-border);
    color: var(--color-text-1);

    &:hover {
      border-color: var(--color-border-3);
    }

    &:focus, &.arco-input-focus {
      border-color: rgb(var(--primary-6));
    }
  }

  .arco-input-prepend {
    background: var(--color-fill-3);
    border-color: var(--color-border);
  }

  .arco-select-view-value {
    color: var(--color-text-1);
  }
}

.edit-form-content {
  :deep(.arco-form-item) {
    margin-bottom: 16px;
  }

  :deep(.arco-form-item-label-col) {
    margin-bottom: 6px;
  }

  :deep(.arco-form-item-label) {
    font-weight: 500;
    color: var(--color-text-1);
  }

  .image-upload-wrapper {
    .upload-trigger {
      width: 120px;
      height: 120px;
      border: 1px dashed var(--color-border-2);
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      background: var(--color-fill-2);

      .preview-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        opacity: 1;
      }

      .upload-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: var(--color-text-3);
        font-size: 12px;
        gap: 8px;

        :deep(svg) {
          width: 24px;
          height: 24px;
        }
      }
    }
  }

  .color-input-preview {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    border: 1px solid var(--color-border);
  }
}

// 响应式 - 编辑弹窗
@media (max-width: 768px) {
  :deep(.knowledge-edit-modal) {
    .arco-modal {
      width: 95% !important;
      max-width: none;
    }

    .arco-modal-body {
      padding: 16px;
    }
  }

  .edit-form-content {
    .image-upload-wrapper .upload-trigger {
      width: 100px;
      height: 100px;
    }
  }
}
</style>
