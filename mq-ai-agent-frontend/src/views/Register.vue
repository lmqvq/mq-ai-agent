<template>
  <div class="register-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="brand-content">
        <!-- Logo -->
        <div class="brand-logo">
          <svg viewBox="0 0 32 32" fill="none">
            <circle cx="16" cy="16" r="14" fill="#7BE3C0"/>
            <path d="M12 16 L16 12 L20 16 L16 20 Z" fill="white"/>
          </svg>
          <span>AI健身教练</span>
        </div>
        
        <!-- 主标题 -->
        <div class="brand-headline">
          <h1>开启您的</h1>
          <h1>健身新旅程</h1>
        </div>
        
        <!-- 副标题 -->
        <p class="brand-subtitle">
          加入我们，获取专属 AI 健身教练，定制您的完美训练计划
        </p>
        
        <!-- 特性亮点 -->
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <span>免费个性化评估</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <span>7天24小AI指导</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <span>进度跟踪与分析</span>
          </div>
        </div>
      </div>
      
      <!-- 装饰元素 -->
      <div class="brand-decoration">
        <div class="deco-circle deco-1"></div>
        <div class="deco-circle deco-2"></div>
        <div class="deco-dot dot-1"></div>
        <div class="deco-dot dot-2"></div>
      </div>
    </div>
    
    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写信息开始您的健身之旅</p>
        </div>
      
        <div class="register-form">
          <a-form :model="registerForm" @submit="handleRegister" layout="vertical">
            <a-form-item 
              label="账号" 
              field="userAccount"
              :rules="[
                { required: true, message: '请输入账号' },
                { minLength: 4, message: '账号至少4个字符' }
              ]"
            >
              <a-input 
                v-model="registerForm.userAccount" 
                placeholder="请输入您的账号"
                size="large"
                :disabled="loading"
              >
                <template #prefix>
                  <icon-user />
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item 
              label="密码" 
              field="userPassword"
              :rules="[
                { required: true, message: '请输入密码' },
                { minLength: 6, message: '密码至少8个字符' }
              ]"
            >
              <a-input-password 
                v-model="registerForm.userPassword" 
                placeholder="请输入您的密码"
                size="large"
                :disabled="loading"
              >
                <template #prefix>
                  <icon-lock />
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item 
              label="确认密码" 
              field="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validatePassword }
              ]"
            >
              <a-input-password 
                v-model="registerForm.checkPassword" 
                placeholder="请再次输入密码"
                size="large"
                :disabled="loading"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <icon-lock />
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item>
              <a-button 
                type="primary" 
                size="large" 
                long 
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </a-button>
            </a-form-item>
          </a-form>
          
          <div class="register-footer">
            <span>已有账号？</span>
            <a-link @click="goToLogin">立即登录</a-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { IconUser, IconLock } from '@arco-design/web-vue/es/icon'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'RegisterPage',
  components: {
    IconUser,
    IconLock
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    const loading = ref(false)
    const registerForm = reactive({
      userAccount: '',
      userPassword: '',
      checkPassword: ''
    })

    /**
     * 验证确认密码
     */
    const validatePassword = (value, callback) => {
      if (value !== registerForm.userPassword) {
        callback('两次输入的密码不一致')
      } else {
        callback()
      }
    }

    /**
     * 处理注册
     */
    const handleRegister = async () => {
      // 表单验证
      if (!registerForm.userAccount.trim()) {
        Message.error('请输入账号')
        return
      }
      
      if (registerForm.userAccount.trim().length < 4) {
        Message.error('账号至少4个字符')
        return
      }
      
      if (!registerForm.userPassword.trim()) {
        Message.error('请输入密码')
        return
      }
      
      if (registerForm.userPassword.trim().length < 6) {
        Message.error('密码至少8个字符')
        return
      }
      
      if (!registerForm.checkPassword.trim()) {
        Message.error('请确认密码')
        return
      }
      
      if (registerForm.userPassword !== registerForm.checkPassword) {
        Message.error('两次输入的密码不一致')
        return
      }
      
      loading.value = true
      
      try {
        const result = await userStore.register({
          userAccount: registerForm.userAccount.trim(),
          userPassword: registerForm.userPassword.trim(),
          checkPassword: registerForm.checkPassword.trim()
        })
        
        if (result.success) {
          Message.success('注册成功，请登录')
          // 跳转到登录页面
          router.push('/login')
        } else {
          Message.error(result.message)
        }
      } catch (error) {
        console.error('注册错误:', error)
        Message.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    /**
     * 跳转到登录页面
     */
    const goToLogin = () => {
      router.push('/login')
    }

    return {
      loading,
      registerForm,
      validatePassword,
      handleRegister,
      goToLogin
    }
  }
}
</script>

<style lang="scss" scoped>
// =========================================================
// 注册页样式 - 使用全局 CSS 变量实现统一薄荷绿主题
// 参考设计规范：双 CTA（黑主按钮 + Mint 次按钮）
// =========================================================

.register-container {
  min-height: 100vh;
  display: flex;
  background: var(--mint-neutral-00);
  
  html.dark-theme & {
    background: var(--mint-neutral-00);
  }
}

// 左侧品牌展示区
.brand-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 80px;
  position: relative;
  overflow: hidden;
  // 使用全局 CSS 变量实现薄荷绿渐变背景
  background: linear-gradient(135deg, var(--mint-neutral-00) 0%, var(--mint-neutral-05) 50%, var(--mint-15) 100%);
  
  html.dark-theme & {
    background: linear-gradient(135deg, #1a1f2e 0%, #151a28 100%);
  }
}

.brand-content {
  position: relative;
  z-index: 2;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
  
  svg {
    width: 40px;
    height: 40px;
  }
  
  span {
    font-size: 20px;
    font-weight: 700;
    color: var(--mint-neutral-90);
  }
}

.brand-headline {
  margin-bottom: 24px;
  
  h1 {
    margin: 0;
    font-size: 48px;
    font-weight: 700;
    line-height: 1.2;
    letter-spacing: -1px;
    color: var(--mint-neutral-90);
    
    &:last-child {
      color: var(--mint-70);
    }
  }
}

.brand-subtitle {
  font-size: 18px;
  line-height: 1.6;
  margin: 0 0 48px 0;
  color: var(--mint-neutral-60);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .feature-icon {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    svg {
      width: 20px;
      height: 20px;
      stroke: var(--mint-70);
    }
  }
  
  span {
    font-size: 16px;
    font-weight: 500;
    color: var(--mint-neutral-90);
  }
}

// 装饰元素
.brand-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  
  &.deco-1 {
    width: 500px;
    height: 500px;
    top: -150px;
    right: -150px;
    background: linear-gradient(135deg, var(--mint-15) 0%, transparent 70%);
  }
  
  &.deco-2 {
    width: 300px;
    height: 300px;
    bottom: -100px;
    left: -80px;
    background: linear-gradient(135deg, var(--mint-30) 0%, transparent 70%);
    opacity: 0.6;
  }
}

.deco-dot {
  position: absolute;
  border-radius: 50%;
  background: var(--accent-40);
  
  &.dot-1 {
    width: 12px;
    height: 12px;
    top: 20%;
    right: 15%;
    opacity: 0.8;
  }
  
  &.dot-2 {
    width: 8px;
    height: 8px;
    bottom: 30%;
    right: 25%;
    opacity: 0.6;
  }
}

// 右侧表单区
.form-section {
  width: 520px;
  min-width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: var(--mint-neutral-00);
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 32px;
  
  h2 {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 700;
    color: var(--mint-neutral-90);
  }
  
  p {
    margin: 0;
    font-size: 15px;
    color: var(--mint-neutral-60);
  }
}

.register-form {
  :deep(.arco-form-item-label) {
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 8px;
    color: var(--mint-neutral-90);
  }

  :deep(.arco-input-wrapper) {
    border-radius: var(--mint-radius-button);
    transition: all 0.2s ease;
    border: 1px solid var(--mint-neutral-10);
    background: var(--mint-neutral-05);
    
    &:hover {
      border-color: var(--mint-50);
      background: var(--mint-neutral-00);
    }
    
    &.arco-input-focus {
      border-color: var(--mint-70);
      background: var(--mint-neutral-00);
      box-shadow: 0 0 0 3px rgba(44, 191, 138, 0.12);
    }
    
    html.dark-theme & {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(255, 255, 255, 0.05);
      
      &:hover {
        border-color: rgba(123, 227, 192, 0.5);
      }
      
      &.arco-input-focus {
        border-color: var(--mint-50);
        box-shadow: 0 0 0 3px rgba(123, 227, 192, 0.1);
      }
    }
  }

  :deep(.arco-input) {
    font-size: 15px;
    padding: 12px 16px;
    color: var(--mint-neutral-90);
    
    &::placeholder {
      color: var(--mint-neutral-60);
    }
  }

  :deep(.arco-input-prefix) {
    margin-right: 10px;
    color: var(--mint-neutral-60);
  }
  
  :deep(.arco-input-wrapper:focus-within .arco-input-prefix) {
    color: var(--mint-70);
  }

  // 主按钮 - 使用薄荷绿主题统一样式（黑底白字）
  :deep(.arco-btn-primary) {
    border-radius: var(--mint-radius-button);
    font-weight: 600;
    font-size: 15px;
    height: 50px;
    border: none;
    transition: all 0.2s ease;
    margin-top: 8px;
    background: var(--mint-btn-primary-bg) !important;
    color: var(--mint-btn-primary-text) !important;
    box-shadow: var(--mint-btn-primary-shadow);
    
    &:hover {
      background: var(--mint-btn-primary-hover) !important;
      transform: translateY(-1px);
      box-shadow: var(--mint-btn-primary-shadow-hover);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--mint-neutral-60);

  :deep(.arco-link) {
    font-weight: 600;
    margin-left: 4px;
    transition: color 0.2s ease;
    color: var(--mint-70);
    
    &:hover {
      color: var(--mint-50);
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }
  
  .form-section {
    width: 100%;
    min-width: auto;
    min-height: 100vh;
    background: linear-gradient(180deg, var(--mint-neutral-00) 0%, var(--mint-15) 100%);
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 40px 24px;
  }
  
  .form-header {
    h2 {
      font-size: 24px;
    }
  }
  
  :deep(.arco-btn-primary) {
    height: 46px;
  }
}
</style>
