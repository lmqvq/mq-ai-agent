<template>
  <div class="login-container">
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
          <h1>您的专属</h1>
          <h1>智能健身教练</h1>
        </div>
        
        <!-- 副标题 -->
        <p class="brand-subtitle">
          AI 驱动的个性化健身方案，让每一次训练都更高效
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
            <span>智能训练计划</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <span>实时动作纠正</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <span>营养饮食建议</span>
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
          <h2>欢迎回来</h2>
          <p>登录您的账号继续健身之旅</p>
        </div>
        
        <div class="login-form">
          <a-form :model="loginForm" @submit="handleLogin" layout="vertical">
            <a-form-item 
              label="账号" 
              field="userAccount"
              :rules="[{ required: true, message: '请输入账号' }]"
            >
              <a-input 
                v-model="loginForm.userAccount" 
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
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <a-input-password 
                v-model="loginForm.userPassword" 
                placeholder="请输入您的密码"
                size="large"
                :disabled="loading"
                @keyup.enter="handleLogin"
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
                @click="handleLogin"
              >
                登录
              </a-button>
            </a-form-item>
          </a-form>
          
          <div class="login-footer">
            <span>还没有账号？</span>
            <a-link @click="goToRegister">立即注册</a-link>
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
  name: 'LoginPage',
  components: {
    IconUser,
    IconLock
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    const loading = ref(false)
    const loginForm = reactive({
      userAccount: '',
      userPassword: ''
    })

    /**
     * 处理登录
     */
    const handleLogin = async () => {
      // 表单验证
      if (!loginForm.userAccount.trim()) {
        Message.error('请输入账号')
        return
      }
      
      if (!loginForm.userPassword.trim()) {
        Message.error('请输入密码')
        return
      }
      
      loading.value = true
      
      try {
        const result = await userStore.login({
          userAccount: loginForm.userAccount.trim(),
          userPassword: loginForm.userPassword.trim()
        })
        
        if (result.success) {
          Message.success('登录成功')
          // 跳转到主页
          router.push('/home')
        } else {
          Message.error(result.message)
        }
      } catch (error) {
        console.error('登录错误:', error)
        Message.error('登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    /**
     * 跳转到注册页面
     */
    const goToRegister = () => {
      router.push('/register')
    }

    return {
      loading,
      loginForm,
      handleLogin,
      goToRegister
    }
  }
}
</script>

<style lang="scss" scoped>
// =========================================================
// 登录页样式 - 使用全局 CSS 变量实现统一薄荷绿主题
// 参考设计规范：双 CTA（黑主按钮 + Mint 次按钮）
// =========================================================

.login-container {
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
  margin-bottom: 40px;
  
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

.login-form {
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
    padding: 14px 16px;
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

.login-footer {
  text-align: center;
  margin-top: 32px;
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
