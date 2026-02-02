<template>
  <nav class="landing-nav" :class="{ 'scrolled': isScrolled }">
    <div class="nav-container">
      <!-- Logo -->
      <div class="nav-logo" @click="scrollToTop">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none">
            <circle cx="16" cy="16" r="14" fill="var(--landing-mint)"/>
            <path d="M12 16 L16 12 L20 16 L16 20 Z" fill="var(--landing-bg)"/>
          </svg>
        </div>
        <span class="logo-text">AI健身教练</span>
      </div>

      <!-- 导航链接 -->
      <div class="nav-links" :class="{ 'mobile-open': mobileMenuOpen }">
        <a href="#features" class="nav-link" @click.prevent="scrollTo('features')">功能</a>
        <a href="#advantages" class="nav-link" @click.prevent="scrollTo('advantages')">优势</a>
        <a href="#pricing" class="nav-link" @click.prevent="scrollTo('pricing')">价格</a>
      </div>

      <!-- 右侧操作区 -->
      <div class="nav-actions">
        <!-- 主题切换 -->
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
          <transition name="icon-rotate" mode="out-in">
            <svg v-if="isDark" key="sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/>
              <line x1="12" y1="1" x2="12" y2="3"/>
              <line x1="12" y1="21" x2="12" y2="23"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
              <line x1="1" y1="12" x2="3" y2="12"/>
              <line x1="21" y1="12" x2="23" y2="12"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
            <svg v-else key="moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
          </transition>
        </button>

        <!-- GitHub 链接 -->
        <a 
          class="github-link" 
          href="https://github.com/lmqvq/mq-ai-agent" 
          target="_blank" 
          rel="noopener noreferrer"
          title="GitHub"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
        </a>

        <!-- 登录按钮 -->
        <router-link to="/login" class="nav-btn nav-btn-secondary">登录</router-link>
        <router-link to="/register" class="nav-btn nav-btn-primary">免费注册</router-link>
      </div>

      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <span :class="{ 'open': mobileMenuOpen }"></span>
      </button>
    </div>
  </nav>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useThemeStore } from '@/stores/theme';

export default {
  name: 'LandingNav',
  setup() {
    const themeStore = useThemeStore();
    const isScrolled = ref(false);
    const mobileMenuOpen = ref(false);

    const isDark = computed(() => themeStore.theme === 'dark');

    const handleScroll = () => {
      isScrolled.value = window.scrollY > 20;
    };

    const toggleTheme = () => {
      themeStore.toggleTheme();
    };

    const scrollToTop = () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const scrollTo = (id) => {
      const element = document.getElementById(id);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
      mobileMenuOpen.value = false;
    };

    onMounted(() => {
      window.addEventListener('scroll', handleScroll);
    });

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
    });

    return {
      isScrolled,
      mobileMenuOpen,
      isDark,
      toggleTheme,
      scrollToTop,
      scrollTo
    };
  }
};
</script>

<style lang="scss" scoped>
.landing-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 16px 0;
  transition: all 0.3s ease;

  &.scrolled {
    background: var(--landing-bg);
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.06);
    padding: 12px 0;
  }
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  
  .logo-icon {
    width: 36px;
    height: 36px;
    
    svg {
      width: 100%;
      height: 100%;
    }
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--landing-text-primary);
    letter-spacing: -0.5px;
  }
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;

  .nav-link {
    font-size: 14px;
    font-weight: 500;
    color: var(--landing-text-secondary);
    text-decoration: none;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--landing-mint-dark);
    }
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle,
.github-link {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--landing-border);
  background: var(--landing-bg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  text-decoration: none;
  
  svg {
    width: 20px;
    height: 20px;
    color: var(--landing-text-secondary);
  }
  
  &:hover {
    border-color: var(--landing-mint);
    background: var(--landing-mint-bg);
    
    svg {
      color: var(--landing-mint-dark);
    }
  }
}

.nav-btn {
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  
  &-secondary {
    color: var(--landing-text-primary);
    background: transparent;
    
    &:hover {
      background: var(--landing-bg-secondary);
    }
  }
  
  &-primary {
    color: var(--landing-btn-primary-text);
    background: var(--landing-btn-primary-bg);
    
    &:hover {
      background: var(--landing-btn-primary-hover);
    }
  }
}

.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  border: none;
  background: none;
  cursor: pointer;
  position: relative;
  
  span {
    display: block;
    width: 24px;
    height: 2px;
    background: var(--landing-text-primary);
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    transition: all 0.3s ease;
    
    &::before,
    &::after {
      content: '';
      display: block;
      width: 24px;
      height: 2px;
      background: var(--landing-text-primary);
      position: absolute;
      transition: all 0.3s ease;
    }
    
    &::before {
      top: -7px;
    }
    
    &::after {
      top: 7px;
    }
    
    &.open {
      background: transparent;
      
      &::before {
        top: 0;
        transform: rotate(45deg);
      }
      
      &::after {
        top: 0;
        transform: rotate(-45deg);
      }
    }
  }
}

// 图标旋转动画
.icon-rotate-enter-active,
.icon-rotate-leave-active {
  transition: all 0.3s ease;
}

.icon-rotate-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.8);
}

.icon-rotate-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.8);
}

// 响应式
@media (max-width: 768px) {
  .nav-links {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    background: var(--landing-bg);
    padding: 20px;
    flex-direction: column;
    gap: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    transform: translateY(-100%);
    opacity: 0;
    pointer-events: none;
    transition: all 0.3s ease;
    
    &.mobile-open {
      transform: translateY(0);
      opacity: 1;
      pointer-events: auto;
    }
  }
  
  .nav-actions {
    .nav-btn-secondary {
      display: none;
    }
  }
  
  .mobile-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
