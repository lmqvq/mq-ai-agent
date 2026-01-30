<template>
  <div id="app">
    <main class="app-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <footer class="global-footer">
      <a href="https://beian.miit.gov.cn/#/Integrated/index" target="_blank" rel="noopener noreferrer">桂ICP备2024028689号-4</a>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style lang="scss">
@import '@arco-design/web-vue/dist/arco.css';
@import './assets/styles/theme.css';
@import './styles/main.scss';
@import './styles/compatibility.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
  min-height: 100%;
  // 使用主题变量
  background-color: var(--theme-bg-page);
  color: var(--theme-text-primary);
}

// 暗黑模式滚动条
html.dark-theme {
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  ::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.02);
  }
  ::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 4px;
    &:hover {
      background: rgba(255, 255, 255, 0.25);
    }
  }
}

#app {
  min-height: 100vh;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--theme-bg-page);
}

.app-content {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: auto;
}

// 全局页脚 - 使用主题变量
.global-footer {
  text-align: center;
  padding: 12px;
  background-color: var(--theme-bg-container);
  border-top: 1px solid var(--theme-border-secondary);
  font-size: 14px;
  color: var(--theme-text-tertiary);
  width: 100%;
  flex-shrink: 0;
  
  a {
    color: var(--theme-text-tertiary);
    text-decoration: none;
    
    &:hover {
      color: var(--theme-color-primary);
    }
  }
}

// 页面过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

