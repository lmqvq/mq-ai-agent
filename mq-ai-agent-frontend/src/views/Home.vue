<template>
  <div class="home-container">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="sidebar-logo">
        <icon-robot class="logo-icon" />
        <span class="logo-text">LMQ</span>
      </div>
      <div class="sidebar-menu">
        <router-link to="/" class="menu-item" active-class="active" exact>
          <icon-home />
          <span>主页</span>
        </router-link>
        <router-link to="/fitness" class="menu-item" active-class="active">
          <icon-trophy />
          <span>AI健身教练</span>
        </router-link>
        <router-link to="/agent" class="menu-item" active-class="active">
          <icon-robot />
          <span>AI超级智能体</span>
        </router-link>
        <router-link to="/profile" class="menu-item" active-class="active" v-if="isLoggedIn">
          <icon-user />
          <span>个人中心</span>
        </router-link>
        <router-link to="/data" class="menu-item" active-class="active" v-if="isLoggedIn">
          <icon-bar-chart />
          <span>健身数据</span>
        </router-link>
        <router-link to="/ranking" class="menu-item" active-class="active" v-if="isLoggedIn">
          <icon-trophy />
          <span>健身排行榜</span>
        </router-link>
        <router-link to="/knowledge" class="menu-item" active-class="active">
          <icon-book />
          <span>健身知识</span>
        </router-link>
      </div>

      <!-- 用户区域 -->
      <div class="user-area">
        <!-- 未登录状态 -->
        <div v-if="!isLoggedIn" class="login-button" @click="goToLogin">
          <icon-user />
          <span>登录</span>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="user-info" @click="toggleUserMenu" ref="userInfoRef">
          <div class="user-avatar">
            <img v-if="userInfo.userAvatar" :src="userInfo.userAvatar" :alt="userInfo.userName" />
            <icon-user v-else />
          </div>
          <div class="user-details">
            <div class="user-name">{{ userInfo.userName || '用户' }}</div>
            <div class="user-role">{{ getRoleText(userInfo.userRole) }}</div>
          </div>

          <!-- 用户菜单 -->
          <div v-show="showUserMenu" class="user-menu">
            <div class="menu-header">
              <div class="menu-avatar">
                <img v-if="userInfo.userAvatar" :src="userInfo.userAvatar" :alt="userInfo.userName" />
                <icon-user v-else />
              </div>
              <div class="menu-user-info">
                <div class="menu-user-name">{{ userInfo.userName || '用户' }}</div>
                <div class="menu-user-role">{{ getRoleText(userInfo.userRole) }}</div>
              </div>
            </div>
            <div class="menu-divider"></div>
            <div class="menu-items">
              <div class="menu-item" @click="goToProfile">
                <icon-edit />
                <span>修改信息</span>
              </div>
              <div class="menu-item logout" @click="handleLogout">
                <icon-poweroff />
                <span>退出登录</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content" :class="{ 'dark-mode': themeStore.theme === 'dark' }">
    <!-- 右上角操作区 -->
      <div class="header-actions">
        <!-- 主题切换按钮 -->
        <div class="theme-toggle" @click="themeStore.toggleTheme()">
          <transition name="icon-fade" mode="out-in">
            <icon-moon v-if="themeStore.theme === 'light'" key="moon" />
            <icon-sun v-else key="sun" />
          </transition>
        </div>
        <!-- GitHub 链接 -->
        <a 
          class="github-link" 
          href="https://github.com/lmqvq/mq-ai-agent" 
          target="_blank" 
          rel="noopener noreferrer"
        >
          <icon-github />
        </a>
      </div>
      
      <div class="welcome-section">
        <div class="welcome-icon">
          <icon-robot />
        </div>
        <h1>欢迎使用LMQ-AI健身教练</h1>
        <p>智能助手，让健身更轻松</p>
      </div>

      <div class="apps-grid">
        <div v-for="app in apps" :key="app.id" class="app-item">
          <app-card
            :title="app.title"
            :description="app.description"
            :icon="app.icon"
            :to="app.route"
            :bg-color="app.bgColor"
          />
        </div>
      </div>

      <!-- 本周健身概览（登录用户可见） -->
      <div v-if="isLoggedIn" class="weekly-overview-section">
        <WeeklyFitnessOverview 
          ref="weeklyOverviewRef"
          @exercise-added="handleExerciseAdded" 
        />
      </div>

      <!-- 健身数据概览（未登录用户可见） -->
      <div v-else class="fitness-overview">
        <h2>开始您的健身之旅</h2>
        <div class="login-prompt">
          <p>登录后可记录运动数据、追踪健身进度、获取个性化建议</p>
          <a-button type="primary" size="large" @click="goToLogin">
            立即登录
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import AppCard from '@/components/AppCard.vue';
import WeeklyFitnessOverview from '@/components/WeeklyFitnessOverview.vue';
import {
  IconHome, IconUser, IconBarChart, IconTrophy,
  IconBook, IconEdit, IconPoweroff, IconRobot,
  IconMoon, IconSun, IconGithub
} from '@arco-design/web-vue/es/icon';
import { Message } from '@arco-design/web-vue';
import { useUserStore } from '@/stores/user';
import { useThemeStore } from '@/stores/theme';

export default {
  name: 'HomePage',
  components: {
    AppCard,
    WeeklyFitnessOverview,
    IconHome,
    IconUser,
    IconBarChart,
    IconTrophy,
    IconBook,
    IconEdit,
    IconPoweroff,
    IconRobot,
    IconMoon,
    IconSun,
    IconGithub
  },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const themeStore = useThemeStore();
    const showUserMenu = ref(false);
    const userInfoRef = ref(null);
    const weeklyOverviewRef = ref(null);

    // 应用列表
    const apps = [
      {
        id: 'fitness',
        title: 'AI健身教练',
        description: '专业的健身教练，为您提供个性化的健身建议和计划',
        icon: 'icon-trophy',
        route: '/fitness',
        bgColor: 'rgba(255, 235, 218, 0.5)'
      },
      {
        id: 'agent',
        title: 'AI超级智能体',
        description: '强大的智能助手，可以回答问题、提供建议、协助创作和分析信息',
        icon: 'icon-robot',
        route: '/agent',
        bgColor: 'rgba(218, 243, 243, 0.5)'
      },
      // {
      //   id: 'plans',
      //   title: '个性化健身计划',
      //   description: 'AI为您量身定制专业健身计划，科学训练，高效达成目标',
      //   icon: 'icon-calendar',
      //   route: '/plans',
      //   bgColor: 'rgba(230, 247, 255, 0.5)'
      // },
      {
        id: 'data',
        title: '健身数据中心',
        description: '记录和追踪您的健身进展，让数据指导您的健身之路',
        icon: 'icon-bar-chart',
        route: '/data',
        bgColor: 'rgba(246, 255, 237, 0.5)'
      },
      {
        id: 'knowledge',
        title: '健身知识库',
        description: '专业的健身知识，科学的训练方法，助您成为健身达人',
        icon: 'icon-book',
        route: '/knowledge',
        bgColor: 'rgba(255, 247, 230, 0.5)'
      }
    ];

    // 跳转到登录页面
    const goToLogin = () => {
      router.push('/login');
    };

    // 切换用户菜单显示状态
    const toggleUserMenu = (event) => {
      event.stopPropagation();
      showUserMenu.value = !showUserMenu.value;
    };

    // 点击外部区域关闭菜单
    const handleClickOutside = (event) => {
      if (userInfoRef.value && !userInfoRef.value.contains(event.target)) {
        showUserMenu.value = false;
      }
    };

    // 跳转到个人中心
    const goToProfile = () => {
      router.push('/profile');
      showUserMenu.value = false;
    };

    // 处理登出
    const handleLogout = async () => {
      try {
        await userStore.logout();
        Message.success('已退出登录');
        showUserMenu.value = false;
      } catch (error) {
        console.error('登出失败:', error);
        Message.error('登出失败');
      }
    };

    // 获取角色文本
    const getRoleText = (role) => {
      return userStore.getRoleText(role);
    };

    // 处理运动记录添加事件
    const handleExerciseAdded = () => {
      // 子组件已经刷新了数据，这里可以做额外处理（如显示提示等）
      console.log('运动记录已添加');
    };

    // 组件挂载时检查登录状态
    onMounted(async () => {
      await userStore.checkLoginStatus();
      // 添加全局点击事件监听
      document.addEventListener('click', handleClickOutside);
    });

    // 组件卸载时移除事件监听
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside);
    });

    return {
      apps,
      showUserMenu,
      userInfoRef,
      weeklyOverviewRef,
      isLoggedIn: userStore.isLoggedIn,
      userInfo: userStore.userInfo,
      themeStore,
      goToLogin,
      toggleUserMenu,
      goToProfile,
      handleLogout,
      getRoleText,
      handleExerciseAdded
    };
  }
};
</script>

<style lang="scss" scoped>
/* ================================================================
   Home.vue 样式 - 使用CSS变量实现主题切换
   亮色/暗色模式通过 var(--theme-xxx) 变量自动切换
================================================================ */

.home-container {
  display: flex;
  height: 100vh;
  width: 100%;
  background: var(--theme-bg-page);
  position: relative;
}

.sidebar {
  width: 200px;
  background: var(--theme-bg-sidebar);
  border-right: 1px solid var(--theme-border-secondary);
  height: 100%;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;

  .sidebar-logo {
    display: flex;
    align-items: center;
    padding: 0 20px;
    margin-bottom: 30px;
    
    .logo-icon {
      font-size: 28px;
      color: var(--theme-color-primary);
    }
    
    .logo-text {
      margin-left: 10px;
      font-size: 20px;
      font-weight: 700;
      color: var(--theme-color-primary);
      letter-spacing: -0.5px;
    }
  }

  .sidebar-menu {
    flex: 1;

    .menu-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      color: var(--theme-text-secondary);
      cursor: pointer;
      font-size: 14px;
      text-decoration: none;
      border-radius: 8px;
      margin: 0 12px 4px;
      font-weight: 500;

      :deep(svg) {
        color: var(--theme-icon-color);
        margin-right: 10px;
      }

      &:hover {
        background: var(--theme-bg-hover);
        color: var(--theme-color-primary);
        
        :deep(svg) {
          color: var(--theme-color-primary);
        }
      }

      &.active {
        background: var(--theme-menu-active-bg);
        color: var(--theme-text-inverse);
        
        :deep(svg) {
          color: var(--theme-text-inverse);
        }
      }
    }
  }

  .user-area {
    padding: 16px 20px;
    border-top: 1px solid var(--theme-border-secondary);

    .login-button {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 12px 16px;
      background: var(--theme-btn-primary-bg);
      color: var(--theme-text-inverse);
      border-radius: 10px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;

      :deep(svg) {
        width: 16px;
        height: 16px;
        margin-right: 8px;
        color: var(--theme-text-inverse);
      }

      &:hover {
        opacity: 0.9;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      padding: 8px;
      border-radius: 8px;
      cursor: pointer;
      position: relative;

      &:hover {
        background: var(--theme-bg-hover);
      }

      .user-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: var(--theme-color-primary);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        :deep(svg) {
          width: 16px;
          height: 16px;
          color: white;
        }
      }

      .user-details {
        flex: 1;
        min-width: 0;

        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: var(--theme-text-primary);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .user-role {
          font-size: 12px;
          color: var(--theme-text-secondary);
          margin-top: 2px;
        }
      }

      .user-menu {
        position: absolute;
        bottom: 100%;
        left: 0;
        right: 0;
        background: var(--theme-bg-card);
        border-radius: 8px;
        box-shadow: var(--theme-shadow-lg);
        border: 1px solid var(--theme-border-primary);
        z-index: 1000;
        margin-bottom: 8px;

        .menu-header {
          display: flex;
          align-items: center;
          padding: 16px;

          .menu-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: var(--theme-color-primary);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }

            :deep(svg) {
              width: 20px;
              height: 20px;
              color: white;
            }
          }

          .menu-user-info {
            flex: 1;

            .menu-user-name {
              font-size: 16px;
              font-weight: 600;
              color: var(--theme-text-primary);
              margin-bottom: 4px;
            }

            .menu-user-role {
              font-size: 12px;
              color: var(--theme-text-secondary);
            }
          }
        }

        .menu-divider {
          height: 1px;
          background: var(--theme-border-secondary);
          margin: 0 16px;
        }

        .menu-items {
          padding: 8px 0;

          .menu-item {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            cursor: pointer;
            font-size: 14px;
            color: var(--theme-text-primary);

            :deep(svg) {
              width: 16px;
              height: 16px;
              margin-right: 12px;
              color: var(--theme-icon-color);
            }

            &:hover {
              background: var(--theme-bg-hover);
              color: var(--theme-color-primary);

              :deep(svg) {
                color: var(--theme-color-primary);
              }
            }

            &.logout {
              color: var(--theme-color-error);

              :deep(svg) {
                color: var(--theme-color-error);
              }

              &:hover {
                background: rgba(245, 34, 45, 0.1);
              }
            }
          }
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: 40px;
  padding-bottom: 60px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  background: var(--theme-bg-page);
  color: var(--theme-text-primary);
}

.welcome-section {
  text-align: center;
  margin-bottom: 64px;
  animation: fadeIn 0.8s ease-in;
  
  .welcome-icon {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: var(--theme-btn-primary-bg);
    color: white;
    margin-bottom: 28px;
    animation: float 3s ease-in-out infinite;
    
    :deep(svg) {
      width: 50px;
      height: 50px;
    }
  }
  
  h1 {
    font-size: 38px;
    font-weight: 800;
    margin-bottom: 16px;
    color: var(--theme-color-primary);
    letter-spacing: -0.5px;
  }
  
  p {
    font-size: 18px;
    color: var(--theme-text-secondary);
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
    font-weight: 500;
  }
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
  max-width: 1000px;
  margin: 0 auto 48px;
  animation: slideUp 0.6s ease-out;
  animation-fill-mode: both;
  
  .app-item {
    animation: scaleIn 0.5s ease-out;
    animation-fill-mode: both;
    
    &:nth-child(1) { animation-delay: 0.1s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.3s; }
    &:nth-child(4) { animation-delay: 0.4s; }
  }
}

// 本周健身概览区域
.weekly-overview-section {
  max-width: 1000px;
  margin: 0 auto 48px;
  animation: slideUp 0.6s ease-out;
  animation-fill-mode: both;
  animation-delay: 0.3s;
}

// 未登录提示
.fitness-overview {
  margin-bottom: 48px;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--theme-text-primary);
    margin: 0 0 24px 0;
    text-align: center;
    letter-spacing: -0.5px;
  }

  .login-prompt {
    background: var(--theme-bg-card);
    border-radius: 20px;
    padding: 48px 32px;
    text-align: center;
    box-shadow: var(--theme-shadow-md);
    border: 1px solid var(--theme-border-secondary);

    p {
      font-size: 16px;
      color: var(--theme-text-secondary);
      margin: 0 0 24px 0;
      line-height: 1.6;
    }

    :deep(.arco-btn) {
      min-width: 160px;
      border-radius: 20px;
      font-weight: 600;
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from { 
    opacity: 0;
    transform: scale(0.9);
  }
  to { 
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .apps-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }

  .fitness-overview .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #eaedf1;
    padding: 15px 0;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .apps-grid {
    grid-template-columns: 1fr;
  }

  .fitness-overview .overview-cards {
    grid-template-columns: 1fr;
  }

  .welcome-section {
    margin-bottom: 30px;

    h1 {
      font-size: 24px;
    }
  }
}

// 右上角操作区
.header-actions {
  position: absolute;
  top: 20px;
  right: 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 100;
}

// 主题切换按钮 - 使用CSS变量
.theme-toggle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--theme-bg-card);
  box-shadow: var(--theme-shadow-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid var(--theme-border-primary);
  
  :deep(svg) {
    width: 24px;
    height: 24px;
    color: var(--theme-color-primary);
  }
  
  &:hover {
    transform: scale(1.1);
    box-shadow: var(--theme-shadow-lg);
    background: var(--theme-bg-card-hover);
  }
}

// GitHub 链接按钮
.github-link {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--theme-bg-card);
  box-shadow: var(--theme-shadow-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid var(--theme-border-primary);
  text-decoration: none;
  transition: all 0.2s ease;
  
  :deep(svg) {
    width: 24px;
    height: 24px;
    color: var(--theme-text-primary);
  }
  
  &:hover {
    transform: scale(1.1);
    box-shadow: var(--theme-shadow-lg);
    background: var(--theme-bg-card-hover);
  }
}

// 图标切换动画
.icon-fade-enter-active,
.icon-fade-leave-active {
  transition: all 0.3s ease;
}

.icon-fade-enter-from {
  opacity: 0;
  transform: rotate(180deg) scale(0.5);
}

.icon-fade-leave-to {
  opacity: 0;
  transform: rotate(-180deg) scale(0.5);
}
</style>
