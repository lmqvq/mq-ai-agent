<template>
  <div class="knowledge-manage">
    <div class="page-header">
      <h1><icon-settings /> 知识库管理</h1>
      <p>管理健身知识库内容，包括基础知识、动作指导、营养知识、训练计划和饮食计划</p>
    </div>

    <a-tabs v-model:active-key="activeTab" type="rounded" @change="handleTabChange">
      <a-tab-pane key="basics" title="基础知识">
        <div class="tab-content">
          <div class="toolbar">
            <a-button type="primary" @click="handleAdd('basics')">
              <icon-plus /> 添加知识
            </a-button>
            <a-input-search
              v-model="searchText"
              placeholder="搜索标题或描述"
              style="width: 300px"
              @search="loadData"
            />
          </div>
          <a-table :data="basicsData" :loading="loading" :pagination="pagination" @page-change="onPageChange">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="70" />
              <a-table-column title="图片" :width="100">
                <template #cell="{ record }">
                  <a-image v-if="record.image" :src="record.image" width="60" height="60" fit="cover" />
                  <span v-else class="no-image">暂无</span>
                </template>
              </a-table-column>
              <a-table-column title="标题" data-index="title" :width="180" />
              <a-table-column title="难度" data-index="difficulty" :width="80">
                <template #cell="{ record }">
                  <a-tag :color="getDifficultyColor(record.difficulty)">{{ record.difficulty }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="阅读时长" :width="90">
                <template #cell="{ record }">{{ record.readTime }}分钟</template>
              </a-table-column>
              <a-table-column title="浏览量" data-index="views" :width="80" />
              <a-table-column title="排序" data-index="sortOrder" :width="70" />
              <a-table-column title="操作" :width="150" fixed="right">
                <template #cell="{ record }">
                  <a-button type="text" size="small" @click="handleEdit('basics', record)">编辑</a-button>
                  <a-popconfirm content="确定删除该知识吗？" @ok="handleDelete('basics', record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </a-tab-pane>

      <a-tab-pane key="exercises" title="动作指导">
        <div class="tab-content">
          <div class="toolbar">
            <a-button type="primary" @click="handleAdd('exercises')">
              <icon-plus /> 添加动作
            </a-button>
            <a-input-search v-model="searchText" placeholder="搜索名称或描述" style="width: 300px" @search="loadData" />
          </div>
          <a-table :data="exercisesData" :loading="loading" :pagination="pagination" @page-change="onPageChange">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="70" />
              <a-table-column title="图片" :width="100">
                <template #cell="{ record }">
                  <a-image v-if="record.image" :src="record.image" width="60" height="60" fit="cover" />
                  <span v-else class="no-image">暂无</span>
                </template>
              </a-table-column>
              <a-table-column title="名称" data-index="name" :width="140" />
              <a-table-column title="分类" data-index="category" :width="100" />
              <a-table-column title="目标肌群" data-index="muscleGroup" :width="100" />
              <a-table-column title="难度" :width="80">
                <template #cell="{ record }">
                  <a-tag :color="getDifficultyColor(record.difficulty)">{{ record.difficulty }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="排序" data-index="sortOrder" :width="70" />
              <a-table-column title="操作" :width="150" fixed="right">
                <template #cell="{ record }">
                  <a-button type="text" size="small" @click="handleEdit('exercises', record)">编辑</a-button>
                  <a-popconfirm content="确定删除该动作吗？" @ok="handleDelete('exercises', record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </a-tab-pane>

      <a-tab-pane key="nutrients" title="营养知识">
        <div class="tab-content">
          <div class="toolbar">
            <a-button type="primary" @click="handleAdd('nutrients')">
              <icon-plus /> 添加营养
            </a-button>
            <a-input-search v-model="searchText" placeholder="搜索名称或描述" style="width: 300px" @search="loadData" />
          </div>
          <a-table :data="nutrientsData" :loading="loading" :pagination="false">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="70" />
              <a-table-column title="颜色" :width="80">
                <template #cell="{ record }">
                  <div class="color-preview" :style="{ backgroundColor: record.color }"></div>
                </template>
              </a-table-column>
              <a-table-column title="名称" data-index="name" :width="140" />
              <a-table-column title="描述" data-index="description" ellipsis />
              <a-table-column title="图标" data-index="icon" :width="100" />
              <a-table-column title="排序" data-index="sortOrder" :width="70" />
              <a-table-column title="操作" :width="150" fixed="right">
                <template #cell="{ record }">
                  <a-button type="text" size="small" @click="handleEdit('nutrients', record)">编辑</a-button>
                  <a-popconfirm content="确定删除该营养知识吗？" @ok="handleDelete('nutrients', record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </a-tab-pane>

      <a-tab-pane key="programs" title="训练计划">
        <div class="tab-content">
          <div class="toolbar">
            <a-button type="primary" @click="handleAdd('programs')">
              <icon-plus /> 添加计划
            </a-button>
            <a-input-search v-model="searchText" placeholder="搜索名称或描述" style="width: 300px" @search="loadData" />
          </div>
          <a-table :data="programsData" :loading="loading" :pagination="false">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="70" />
              <a-table-column title="名称" data-index="name" :width="160" />
              <a-table-column title="类型" data-index="type" :width="100" />
              <a-table-column title="时长" data-index="duration" :width="100" />
              <a-table-column title="强度" data-index="intensity" :width="100" />
              <a-table-column title="级别" data-index="level" :width="80" />
              <a-table-column title="排序" data-index="sortOrder" :width="70" />
              <a-table-column title="操作" :width="150" fixed="right">
                <template #cell="{ record }">
                  <a-button type="text" size="small" @click="handleEdit('programs', record)">编辑</a-button>
                  <a-popconfirm content="确定删除该计划吗？" @ok="handleDelete('programs', record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </a-tab-pane>

      <a-tab-pane key="meals" title="饮食计划">
        <div class="tab-content">
          <div class="toolbar">
            <a-button type="primary" @click="handleAdd('meals')">
              <icon-plus /> 添加饮食
            </a-button>
            <a-input-search v-model="searchText" placeholder="搜索名称或描述" style="width: 300px" @search="loadData" />
          </div>
          <a-table :data="mealsData" :loading="loading" :pagination="false">
            <template #columns>
              <a-table-column title="ID" data-index="id" :width="70" />
              <a-table-column title="图片" :width="100">
                <template #cell="{ record }">
                  <a-image v-if="record.image" :src="record.image" width="60" height="60" fit="cover" />
                  <span v-else class="no-image">暂无</span>
                </template>
              </a-table-column>
              <a-table-column title="名称" data-index="name" :width="160" />
              <a-table-column title="类型" data-index="mealType" :width="90" />
              <a-table-column title="卡路里" :width="90">
                <template #cell="{ record }">{{ record.calories }}kcal</template>
              </a-table-column>
              <a-table-column title="蛋白质" :width="80">
                <template #cell="{ record }">{{ record.protein }}g</template>
              </a-table-column>
              <a-table-column title="碳水" :width="80">
                <template #cell="{ record }">{{ record.carbs }}g</template>
              </a-table-column>
              <a-table-column title="脂肪" :width="80">
                <template #cell="{ record }">{{ record.fat }}g</template>
              </a-table-column>
              <a-table-column title="排序" data-index="sortOrder" :width="70" />
              <a-table-column title="操作" :width="150" fixed="right">
                <template #cell="{ record }">
                  <a-button type="text" size="small" @click="handleEdit('meals', record)">编辑</a-button>
                  <a-popconfirm content="确定删除该饮食吗？" @ok="handleDelete('meals', record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>

    <!-- 基础知识编辑弹窗 -->
    <a-modal v-model:visible="basicsModalVisible" :title="isEdit ? '编辑基础知识' : '添加基础知识'" width="700px" @ok="handleBasicsSubmit" @cancel="resetForm">
      <a-form ref="basicsFormRef" :model="basicsForm" layout="vertical">
        <a-form-item field="title" label="标题" :rules="[{ required: true, message: '请输入标题' }]">
          <a-input v-model="basicsForm.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item field="description" label="简短描述">
          <a-textarea v-model="basicsForm.description" placeholder="请输入简短描述" :max-length="200" show-word-limit />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="difficulty" label="难度">
              <a-select v-model="basicsForm.difficulty" placeholder="选择难度">
                <a-option value="初级">初级</a-option>
                <a-option value="中级">中级</a-option>
                <a-option value="高级">高级</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="readTime" label="阅读时长(分钟)">
              <a-input-number v-model="basicsForm.readTime" :min="1" :max="60" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="basicsForm.sortOrder" :min="0" />
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
                  <img v-if="basicsForm.image" :src="basicsForm.image" class="preview-image" />
                  <div v-else class="upload-placeholder">
                    <icon-plus />
                    <div>上传图片</div>
                  </div>
                </div>
              </template>
            </a-upload>
            <a-input v-model="basicsForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
          </div>
        </a-form-item>
        <a-form-item field="content" label="详细内容">
          <a-textarea v-model="basicsForm.content" placeholder="请输入详细内容" :auto-size="{ minRows: 4, maxRows: 8 }" />
        </a-form-item>
        <a-form-item field="tips" label="提示列表(每行一条)">
          <a-textarea v-model="basicsForm.tipsText" placeholder="每行输入一条提示" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 动作指导编辑弹窗 -->
    <a-modal v-model:visible="exercisesModalVisible" :title="isEdit ? '编辑动作指导' : '添加动作指导'" width="700px" @ok="handleExercisesSubmit" @cancel="resetForm">
      <a-form ref="exercisesFormRef" :model="exercisesForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="动作名称" :rules="[{ required: true, message: '请输入名称' }]">
              <a-input v-model="exercisesForm.name" placeholder="请输入名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="category" label="分类" :rules="[{ required: true, message: '请选择分类' }]">
              <a-select v-model="exercisesForm.category" placeholder="选择分类" allow-create>
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
          <a-textarea v-model="exercisesForm.description" placeholder="请输入简短描述" :max-length="200" show-word-limit />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="muscleGroup" label="目标肌群">
              <a-input v-model="exercisesForm.muscleGroup" placeholder="如: 胸肌" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="difficulty" label="难度">
              <a-select v-model="exercisesForm.difficulty" placeholder="选择难度">
                <a-option value="初级">初级</a-option>
                <a-option value="中级">中级</a-option>
                <a-option value="高级">高级</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="exercisesForm.sortOrder" :min="0" />
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
                  <img v-if="exercisesForm.image" :src="exercisesForm.image" class="preview-image" />
                  <div v-else class="upload-placeholder">
                    <icon-plus />
                    <div>上传图片</div>
                  </div>
                </div>
              </template>
            </a-upload>
            <a-input v-model="exercisesForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
          </div>
        </a-form-item>
        <a-form-item field="instructions" label="动作步骤(每行一步)">
          <a-textarea v-model="exercisesForm.instructionsText" placeholder="每行输入一个步骤" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
        <a-form-item field="tips" label="注意事项(每行一条)">
          <a-textarea v-model="exercisesForm.tipsText" placeholder="每行输入一条注意事项" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 营养知识编辑弹窗 -->
    <a-modal v-model:visible="nutrientsModalVisible" :title="isEdit ? '编辑营养知识' : '添加营养知识'" width="600px" @ok="handleNutrientsSubmit" @cancel="resetForm">
      <a-form ref="nutrientsFormRef" :model="nutrientsForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
              <a-input v-model="nutrientsForm.name" placeholder="请输入名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="color" label="显示颜色">
              <a-input v-model="nutrientsForm.color" placeholder="如: #ff7875">
                <template #prepend>
                  <div class="color-input-preview" :style="{ backgroundColor: nutrientsForm.color }"></div>
                </template>
              </a-input>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="description" label="描述">
          <a-textarea v-model="nutrientsForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="icon" label="图标标识">
              <a-select v-model="nutrientsForm.icon" placeholder="选择图标">
                <a-option value="icon-fire">icon-fire</a-option>
                <a-option value="icon-heart">icon-heart</a-option>
                <a-option value="icon-star">icon-star</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="nutrientsForm.sortOrder" :min="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="benefits" label="益处列表(每行一条)">
          <a-textarea v-model="nutrientsForm.benefitsText" placeholder="每行输入一个益处" :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 训练计划编辑弹窗 -->
    <a-modal v-model:visible="programsModalVisible" :title="isEdit ? '编辑训练计划' : '添加训练计划'" width="700px" @ok="handleProgramsSubmit" @cancel="resetForm">
      <a-form ref="programsFormRef" :model="programsForm" layout="vertical">
        <a-form-item field="name" label="计划名称" :rules="[{ required: true, message: '请输入名称' }]">
          <a-input v-model="programsForm.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item field="description" label="描述">
          <a-textarea v-model="programsForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="type" label="类型">
              <a-select v-model="programsForm.type" placeholder="选择类型" allow-create>
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
              <a-select v-model="programsForm.icon" placeholder="选择图标">
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
              <a-input-number v-model="programsForm.sortOrder" :min="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="duration" label="持续时间">
              <a-input v-model="programsForm.duration" placeholder="如: 8周" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="intensity" label="强度">
              <a-input v-model="programsForm.intensity" placeholder="如: 中-高等" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="level" label="适合级别">
              <a-select v-model="programsForm.level" placeholder="选择级别">
                <a-option value="初级">初级</a-option>
                <a-option value="中级">中级</a-option>
                <a-option value="中高级">中高级</a-option>
                <a-option value="初中级">初中级</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="schedule" label="训练安排 (JSON格式)">
          <a-textarea v-model="programsForm.scheduleText" placeholder='[{"day":"周一","content":"训练内容"}]' :auto-size="{ minRows: 4, maxRows: 8 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 饮食计划编辑弹窗 -->
    <a-modal v-model:visible="mealsModalVisible" :title="isEdit ? '编辑饮食计划' : '添加饮食计划'" width="600px" @ok="handleMealsSubmit" @cancel="resetForm">
      <a-form ref="mealsFormRef" :model="mealsForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="名称" :rules="[{ required: true, message: '请输入名称' }]">
              <a-input v-model="mealsForm.name" placeholder="请输入名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="mealType" label="餐食类型" :rules="[{ required: true, message: '请选择类型' }]">
              <a-select v-model="mealsForm.mealType" placeholder="选择类型">
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
          <a-textarea v-model="mealsForm.description" placeholder="请输入描述" :max-length="200" show-word-limit />
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
                  <img v-if="mealsForm.image" :src="mealsForm.image" class="preview-image" />
                  <div v-else class="upload-placeholder">
                    <icon-plus />
                    <div>上传图片</div>
                  </div>
                </div>
              </template>
            </a-upload>
            <a-input v-model="mealsForm.image" placeholder="或直接输入图片URL" style="margin-top: 8px" />
          </div>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="calories" label="卡路里(kcal)">
              <a-input-number v-model="mealsForm.calories" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="protein" label="蛋白质(g)">
              <a-input-number v-model="mealsForm.protein" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="carbs" label="碳水(g)">
              <a-input-number v-model="mealsForm.carbs" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="fat" label="脂肪(g)">
              <a-input-number v-model="mealsForm.fat" :min="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="sortOrder" label="排序">
          <a-input-number v-model="mealsForm.sortOrder" :min="0" style="width: 120px" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconSettings, IconPlus } from '@arco-design/web-vue/es/icon';
import ApiService from '@/services/api';

export default {
  name: 'KnowledgeManage',
  components: { IconSettings, IconPlus },
  setup() {
    const route = useRoute();
    const activeTab = ref('basics');
    const loading = ref(false);
    const searchText = ref('');
    const isEdit = ref(false);
    const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
    const pendingEditId = ref(null); // 用于存储待编辑的ID

    // 数据列表
    const basicsData = ref([]);
    const exercisesData = ref([]);
    const nutrientsData = ref([]);
    const programsData = ref([]);
    const mealsData = ref([]);

    // 弹窗控制
    const basicsModalVisible = ref(false);
    const exercisesModalVisible = ref(false);
    const nutrientsModalVisible = ref(false);
    const programsModalVisible = ref(false);
    const mealsModalVisible = ref(false);

    // 表单数据
    const basicsForm = reactive({ id: null, title: '', description: '', image: '', difficulty: '初级', readTime: 5, content: '', tipsText: '', sortOrder: 0 });
    const exercisesForm = reactive({ id: null, name: '', category: '', description: '', image: '', muscleGroup: '', difficulty: '初级', instructionsText: '', tipsText: '', sortOrder: 0 });
    const nutrientsForm = reactive({ id: null, name: '', description: '', color: '#ff7875', icon: 'icon-fire', benefitsText: '', sortOrder: 0 });
    const programsForm = reactive({ id: null, name: '', description: '', type: 'beginner', icon: 'icon-user', duration: '', intensity: '', level: '初级', scheduleText: '[]', sortOrder: 0 });
    const mealsForm = reactive({ id: null, name: '', mealType: '午餐', description: '', image: '', calories: 0, protein: 0, carbs: 0, fat: 0, sortOrder: 0 });

    // 加载数据
    const loadData = async () => {
      loading.value = true;
      try {
        const params = { searchText: searchText.value };
        switch (activeTab.value) {
          case 'basics': {
            const basicsRes = await ApiService.getKnowledgeBasicsList({ ...params, current: pagination.current, pageSize: pagination.pageSize });
            if (basicsRes.code === 0) {
              basicsData.value = basicsRes.data.records || [];
              pagination.total = basicsRes.data.total || 0;
            }
            break;
          }
          case 'exercises': {
            const exercisesRes = await ApiService.getKnowledgeExercisesList({ ...params, current: pagination.current, pageSize: pagination.pageSize });
            if (exercisesRes.code === 0) {
              exercisesData.value = exercisesRes.data.records || [];
              pagination.total = exercisesRes.data.total || 0;
            }
            break;
          }
          case 'nutrients': {
            const nutrientsRes = await ApiService.getKnowledgeNutrientsList(params);
            if (nutrientsRes.code === 0) nutrientsData.value = nutrientsRes.data || [];
            break;
          }
          case 'programs': {
            const programsRes = await ApiService.getKnowledgeProgramsList(params);
            if (programsRes.code === 0) programsData.value = programsRes.data || [];
            break;
          }
          case 'meals': {
            const mealsRes = await ApiService.getKnowledgeMealsList(params);
            if (mealsRes.code === 0) mealsData.value = mealsRes.data || [];
            break;
          }
        }
      } catch (error) {
        Message.error('加载数据失败');
      } finally {
        loading.value = false;
      }
    };

    const handleTabChange = () => {
      searchText.value = '';
      pagination.current = 1;
      loadData();
    };

    const onPageChange = (page) => {
      pagination.current = page;
      loadData();
    };

    const getDifficultyColor = (difficulty) => {
      const colors = { '初级': 'green', '中级': 'orange', '高级': 'red' };
      return colors[difficulty] || 'blue';
    };

    // 图片上传
    const customUpload = async (option, type) => {
      try {
        const res = await ApiService.uploadKnowledgeImage(option.fileItem.file, type);
        if (res.code === 0) {
          // 根据类型更新对应表单
          if (type === 'basics') basicsForm.image = res.data;
          else if (type === 'exercise') exercisesForm.image = res.data;
          else if (type === 'meal') mealsForm.image = res.data;
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

    // 重置表单
    const resetForm = () => {
      Object.assign(basicsForm, { id: null, title: '', description: '', image: '', difficulty: '初级', readTime: 5, content: '', tipsText: '', sortOrder: 0 });
      Object.assign(exercisesForm, { id: null, name: '', category: '', description: '', image: '', muscleGroup: '', difficulty: '初级', instructionsText: '', tipsText: '', sortOrder: 0 });
      Object.assign(nutrientsForm, { id: null, name: '', description: '', color: '#ff7875', icon: 'icon-fire', benefitsText: '', sortOrder: 0 });
      Object.assign(programsForm, { id: null, name: '', description: '', type: 'beginner', icon: 'icon-user', duration: '', intensity: '', level: '初级', scheduleText: '[]', sortOrder: 0 });
      Object.assign(mealsForm, { id: null, name: '', mealType: '午餐', description: '', image: '', calories: 0, protein: 0, carbs: 0, fat: 0, sortOrder: 0 });
    };

    // 添加
    const handleAdd = (type) => {
      isEdit.value = false;
      resetForm();
      if (type === 'basics') basicsModalVisible.value = true;
      else if (type === 'exercises') exercisesModalVisible.value = true;
      else if (type === 'nutrients') nutrientsModalVisible.value = true;
      else if (type === 'programs') programsModalVisible.value = true;
      else if (type === 'meals') mealsModalVisible.value = true;
    };

    // 编辑
    const handleEdit = (type, record) => {
      isEdit.value = true;
      if (type === 'basics') {
        Object.assign(basicsForm, record);
        basicsForm.tipsText = Array.isArray(record.tips) ? record.tips.join('\n') : '';
        basicsModalVisible.value = true;
      } else if (type === 'exercises') {
        Object.assign(exercisesForm, record);
        exercisesForm.instructionsText = Array.isArray(record.instructions) ? record.instructions.join('\n') : '';
        exercisesForm.tipsText = Array.isArray(record.tips) ? record.tips.join('\n') : '';
        exercisesModalVisible.value = true;
      } else if (type === 'nutrients') {
        Object.assign(nutrientsForm, record);
        nutrientsForm.benefitsText = Array.isArray(record.benefits) ? record.benefits.join('\n') : '';
        nutrientsModalVisible.value = true;
      } else if (type === 'programs') {
        Object.assign(programsForm, record);
        programsForm.scheduleText = typeof record.schedule === 'object' ? JSON.stringify(record.schedule, null, 2) : record.schedule || '[]';
        programsModalVisible.value = true;
      } else if (type === 'meals') {
        Object.assign(mealsForm, record);
        mealsModalVisible.value = true;
      }
    };

    // 删除
    const handleDelete = async (type, id) => {
      try {
        let res;
        if (type === 'basics') res = await ApiService.deleteKnowledgeBasics(id);
        else if (type === 'exercises') res = await ApiService.deleteKnowledgeExercise(id);
        else if (type === 'nutrients') res = await ApiService.deleteKnowledgeNutrient(id);
        else if (type === 'programs') res = await ApiService.deleteKnowledgeProgram(id);
        else if (type === 'meals') res = await ApiService.deleteKnowledgeMeal(id);
        
        if (res.code === 0) {
          Message.success('删除成功');
          loadData();
        } else {
          Message.error(res.message || '删除失败');
        }
      } catch (error) {
        Message.error('删除失败');
      }
    };

    // 提交表单
    const handleBasicsSubmit = async () => {
      try {
        const data = { ...basicsForm, tips: basicsForm.tipsText.split('\n').filter(t => t.trim()) };
        delete data.tipsText;
        const res = isEdit.value ? await ApiService.updateKnowledgeBasics(data) : await ApiService.addKnowledgeBasics(data);
        if (res.code === 0) {
          Message.success(isEdit.value ? '更新成功' : '添加成功');
          basicsModalVisible.value = false;
          loadData();
        } else {
          Message.error(res.message || '操作失败');
        }
      } catch (error) {
        Message.error('操作失败');
      }
    };

    const handleExercisesSubmit = async () => {
      try {
        const data = {
          ...exercisesForm,
          instructions: exercisesForm.instructionsText.split('\n').filter(t => t.trim()),
          tips: exercisesForm.tipsText.split('\n').filter(t => t.trim())
        };
        delete data.instructionsText;
        delete data.tipsText;
        const res = isEdit.value ? await ApiService.updateKnowledgeExercise(data) : await ApiService.addKnowledgeExercise(data);
        if (res.code === 0) {
          Message.success(isEdit.value ? '更新成功' : '添加成功');
          exercisesModalVisible.value = false;
          loadData();
        } else {
          Message.error(res.message || '操作失败');
        }
      } catch (error) {
        Message.error('操作失败');
      }
    };

    const handleNutrientsSubmit = async () => {
      try {
        const data = { ...nutrientsForm, benefits: nutrientsForm.benefitsText.split('\n').filter(t => t.trim()) };
        delete data.benefitsText;
        const res = isEdit.value ? await ApiService.updateKnowledgeNutrient(data) : await ApiService.addKnowledgeNutrient(data);
        if (res.code === 0) {
          Message.success(isEdit.value ? '更新成功' : '添加成功');
          nutrientsModalVisible.value = false;
          loadData();
        } else {
          Message.error(res.message || '操作失败');
        }
      } catch (error) {
        Message.error('操作失败');
      }
    };

    const handleProgramsSubmit = async () => {
      try {
        let schedule;
        try {
          schedule = JSON.parse(programsForm.scheduleText);
        } catch {
          Message.error('训练安排JSON格式错误');
          return;
        }
        const data = { ...programsForm, schedule };
        delete data.scheduleText;
        const res = isEdit.value ? await ApiService.updateKnowledgeProgram(data) : await ApiService.addKnowledgeProgram(data);
        if (res.code === 0) {
          Message.success(isEdit.value ? '更新成功' : '添加成功');
          programsModalVisible.value = false;
          loadData();
        } else {
          Message.error(res.message || '操作失败');
        }
      } catch (error) {
        Message.error('操作失败');
      }
    };

    const handleMealsSubmit = async () => {
      try {
        const res = isEdit.value ? await ApiService.updateKnowledgeMeal(mealsForm) : await ApiService.addKnowledgeMeal(mealsForm);
        if (res.code === 0) {
          Message.success(isEdit.value ? '更新成功' : '添加成功');
          mealsModalVisible.value = false;
          loadData();
        } else {
          Message.error(res.message || '操作失败');
        }
      } catch (error) {
        Message.error('操作失败');
      }
    };

    // 根据ID查找并打开编辑弹窗
    const openEditById = (type, id) => {
      let dataList, record;
      switch (type) {
        case 'basics':
          dataList = basicsData.value;
          record = dataList.find(item => item.id === Number(id));
          if (record) handleEdit('basics', record);
          break;
        case 'exercises':
          dataList = exercisesData.value;
          record = dataList.find(item => item.id === Number(id));
          if (record) handleEdit('exercises', record);
          break;
        case 'nutrients':
          dataList = nutrientsData.value;
          record = dataList.find(item => item.id === Number(id));
          if (record) handleEdit('nutrients', record);
          break;
        case 'programs':
          dataList = programsData.value;
          record = dataList.find(item => item.id === Number(id));
          if (record) handleEdit('programs', record);
          break;
        case 'meals':
          dataList = mealsData.value;
          record = dataList.find(item => item.id === Number(id));
          if (record) handleEdit('meals', record);
          break;
      }
    };

    // 监听数据加载完成后自动打开编辑
    watch([basicsData, exercisesData, nutrientsData, programsData, mealsData], () => {
      if (pendingEditId.value && activeTab.value) {
        openEditById(activeTab.value, pendingEditId.value);
        pendingEditId.value = null; // 清除待编辑ID
      }
    });

    onMounted(async () => {
      // 检查URL参数
      const { tab, editId } = route.query;
      if (tab && ['basics', 'exercises', 'nutrients', 'programs', 'meals'].includes(tab)) {
        activeTab.value = tab;
        if (editId) {
          pendingEditId.value = editId;
        }
      }
      await loadData();
    });

    return {
      activeTab, loading, searchText, isEdit, pagination,
      basicsData, exercisesData, nutrientsData, programsData, mealsData,
      basicsModalVisible, exercisesModalVisible, nutrientsModalVisible, programsModalVisible, mealsModalVisible,
      basicsForm, exercisesForm, nutrientsForm, programsForm, mealsForm,
      loadData, handleTabChange, onPageChange, getDifficultyColor, customUpload, resetForm,
      handleAdd, handleEdit, handleDelete,
      handleBasicsSubmit, handleExercisesSubmit, handleNutrientsSubmit, handleProgramsSubmit, handleMealsSubmit
    };
  }
};
</script>

<style lang="scss" scoped>
.knowledge-manage {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  p {
    color: #666;
    margin: 0;
  }
}

:deep(.arco-tabs) {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.tab-content {
  padding-top: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.no-image {
  color: #999;
  font-size: 12px;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.color-input-preview {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.image-upload-wrapper {
  .upload-trigger {
    width: 120px;
    height: 120px;
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    
    &:hover {
      border-color: #667eea;
    }
    
    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .upload-placeholder {
      text-align: center;
      color: #999;
      
      :deep(svg) {
        font-size: 24px;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
