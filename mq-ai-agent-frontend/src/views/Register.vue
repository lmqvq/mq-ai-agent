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
// 薄荷绿主题色变量
$mint-primary: #7BE3C0;
$mint-light: #DFF8EE;
$mint-bg: #F1FCF7;
$mint-dark: #2CBF8A;
$warm-accent: #FFB38A;
$text-dark: #0B0F0D;
$text-secondary: #5A6B65;

.register-container {
  min-height: 100vh;
  display: flex;
  
  body:not([arco-theme='dark']) & {
    background: #FFFFFF;
  }
  
  body[arco-theme='dark'] & {
    background: #0f0f1a;
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
  
  body:not([arco-theme='dark']) & {
    // 更浅更柔和的薄荷绿背景，参考着陆页风格
    background: linear-gradient(135deg, #FFFFFF 0%, #F7FAF9 50%, $mint-bg 100%);
  }
  
  body[arco-theme='dark'] & {
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
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
    }
    
    body[arco-theme='dark'] & {
      color: #FFFFFF;
    }
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
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
    }
    
    body[arco-theme='dark'] & {
      color: #FFFFFF;
    }
    
    &:last-child {
      body:not([arco-theme='dark']) & {
        color: $mint-dark;
      }
      
      body[arco-theme='dark'] & {
        color: $mint-primary;
      }
    }
  }
}

.brand-subtitle {
  font-size: 18px;
  line-height: 1.6;
  margin: 0 0 48px 0;
  
  body:not([arco-theme='dark']) & {
    color: $text-secondary;
  }
  
  body[arco-theme='dark'] & {
    color: rgba(255, 255, 255, 0.7);
  }
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
      
      body:not([arco-theme='dark']) & {
        stroke: $mint-dark;
      }
      
      body[arco-theme='dark'] & {
        stroke: $mint-primary;
      }
    }
  }
  
  span {
    font-size: 16px;
    font-weight: 500;
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
    }
    
    body[arco-theme='dark'] & {
      color: rgba(255, 255, 255, 0.9);
    }
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
    
    body:not([arco-theme='dark']) & {
      // 更淡的薄荷绿圈
      background: linear-gradient(135deg, $mint-bg 0%, transparent 70%);
    }
    
    body[arco-theme='dark'] & {
      background: rgba($mint-dark, 0.08);
    }
  }
  
  &.deco-2 {
    width: 300px;
    height: 300px;
    bottom: -100px;
    left: -80px;
    
    body:not([arco-theme='dark']) & {
      background: linear-gradient(135deg, rgba($mint-primary, 0.1) 0%, transparent 70%);
    }
    
    body[arco-theme='dark'] & {
      background: rgba($mint-dark, 0.05);
    }
  }
}

.deco-dot {
  position: absolute;
  border-radius: 50%;
  background: $warm-accent;
  
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
  
  body:not([arco-theme='dark']) & {
    background: #FFFFFF;
  }
  
  body[arco-theme='dark'] & {
    background: #0f0f1a;
  }
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
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
    }
    
    body[arco-theme='dark'] & {
      color: #FFFFFF;
    }
  }
  
  p {
    margin: 0;
    font-size: 15px;
    
    body:not([arco-theme='dark']) & {
      color: $text-secondary;
    }
    
    body[arco-theme='dark'] & {
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.register-form {
  :deep(.arco-form-item-label) {
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 8px;
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
    }
    
    body[arco-theme='dark'] & {
      color: rgba(255, 255, 255, 0.85);
    }
  }

  :deep(.arco-input-wrapper) {
    border-radius: 12px;
    transition: all 0.2s ease;
    
    body:not([arco-theme='dark']) & {
      border: 1px solid #E5E7EB;
      background: #F9FAFB;
      
      &:hover {
        border-color: $mint-primary;
        background: #FFFFFF;
      }
      
      &.arco-input-focus {
        border-color: $mint-dark;
        background: #FFFFFF;
        box-shadow: 0 0 0 3px rgba(44, 191, 138, 0.12);
      }
    }
    
    body[arco-theme='dark'] & {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(255, 255, 255, 0.05);
      
      &:hover {
        border-color: rgba(123, 227, 192, 0.5);
      }
      
      &.arco-input-focus {
        border-color: $mint-primary;
        box-shadow: 0 0 0 3px rgba(123, 227, 192, 0.1);
      }
    }
  }

  :deep(.arco-input) {
    font-size: 15px;
    padding: 12px 16px;
    
    body:not([arco-theme='dark']) & {
      color: $text-dark;
      
      &::placeholder {
        color: #9CA3AF;
      }
    }
    
    body[arco-theme='dark'] & {
      color: #FFFFFF;
      
      &::placeholder {
        color: rgba(255, 255, 255, 0.4);
      }
    }
  }

  :deep(.arco-input-prefix) {
    margin-right: 10px;
    
    body:not([arco-theme='dark']) & {
      color: #9CA3AF;
    }
    
    body[arco-theme='dark'] & {
      color: rgba(255, 255, 255, 0.4);
    }
  }
  
  :deep(.arco-input-wrapper:focus-within .arco-input-prefix) {
    body:not([arco-theme='dark']) & {
      color: $mint-dark;
    }
    
    body[arco-theme='dark'] & {
      color: $mint-primary;
    }
  }

  :deep(.arco-btn-primary) {
    border-radius: 12px;
    font-weight: 600;
    font-size: 15px;
    height: 50px;
    border: none;
    transition: all 0.2s ease;
    margin-top: 8px;
    
    body:not([arco-theme='dark']) & {
      background: $text-dark;
      color: #FFFFFF;
      
      &:hover {
        background: #1a1f1d;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    body[arco-theme='dark'] & {
      background: $mint-primary;
      color: $text-dark;
      
      &:hover {
        background: $mint-dark;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(123, 227, 192, 0.3);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  
  body:not([arco-theme='dark']) & {
    color: $text-secondary;
  }
  
  body[arco-theme='dark'] & {
    color: rgba(255, 255, 255, 0.6);
  }

  :deep(.arco-link) {
    font-weight: 600;
    margin-left: 4px;
    transition: color 0.2s ease;
    
    body:not([arco-theme='dark']) & {
      color: $mint-dark;
      
      &:hover {
        color: darken($mint-dark, 10%);
      }
    }
    
    body[arco-theme='dark'] & {
      color: $mint-primary;
      
      &:hover {
        color: lighten($mint-primary, 10%);
      }
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
    
    body:not([arco-theme='dark']) & {
      background: linear-gradient(180deg, #FFFFFF 0%, $mint-bg 100%);
    }
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
