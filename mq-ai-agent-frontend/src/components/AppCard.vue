<template>
  <router-link :to="to" class="app-card" :class="cardClass">
    <div class="app-card-inner">
      <div class="card-content">
        <h3 class="app-title">{{ title }}</h3>
        <p class="app-desc">{{ description }}</p>
      </div>
      <div class="app-footer">
        <div class="app-icon" :class="iconColor">
          <i :class="icon"></i>
        </div>
        <div class="arrow-icon">
          <i class="icon-right"></i>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script>
export default {
  name: 'AppCard',
  props: {
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: 'icon-apps'
    },
    to: {
      type: [String, Object],
      required: true
    },
    bgColor: {
      type: String,
      default: '#fff'
    }
  },
  computed: {
    iconColor() {
      const iconMap = {
        'icon-trophy': 'icon-orange',
        'icon-robot': 'icon-blue',
        'icon-book': 'icon-purple',
        'icon-heart': 'icon-green',
        'icon-bar-chart': 'icon-green'
      };
      return iconMap[this.icon] || '';
    },
    cardClass() {
      // 根据图标类型返回对应的卡片样式类
      const classMap = {
        'icon-trophy': 'card-theme-orange',
        'icon-robot': 'card-theme-blue',
        'icon-bar-chart': 'card-theme-green',
        'icon-book': 'card-theme-purple'
      };
      return classMap[this.icon] || 'card-theme-default';
    }
  }
}
</script>

<style lang="scss" scoped>
/* ================================================================
   AppCard.vue 样式 - 使用CSS变量实现主题切换
================================================================ */

.app-card {
  display: block;
  width: 100%;
  background: var(--theme-bg-card);
  border-radius: 16px;
  box-shadow: var(--theme-shadow-md);
  text-decoration: none;
  color: var(--theme-text-primary);
  overflow: hidden;
  height: 100%;
  position: relative;
  border: 1px solid var(--theme-border-secondary);

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--theme-shadow-lg);
    background: var(--theme-bg-card-hover);
    
    .arrow-icon {
      transform: translateX(4px);
      opacity: 1;
    }
  }

  .app-card-inner {
    padding: 32px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
    min-height: 220px;
    position: relative;
    z-index: 1;
  }
  
  .card-content {
    flex-grow: 1;
    margin-bottom: 20px;
  }

  .app-title {
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 14px;
    color: var(--theme-text-primary);
    letter-spacing: -0.3px;
    line-height: 1.3;
  }

  .app-desc {
    color: var(--theme-text-secondary);
    margin: 0;
    line-height: 1.6;
    font-size: 15px;
    font-weight: 500;
  }
  
  .app-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .app-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    
    &.icon-orange {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    &.icon-blue {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    
    &.icon-purple {
      background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
    }
    
    &.icon-green {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }
    
    i {
      font-size: 26px;
    }
  }
  
  .arrow-icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: var(--theme-color-primary-light);
    opacity: 0.7;
    
    &::before {
      content: '→';
      font-size: 16px;
      color: var(--theme-color-primary);
      font-weight: 600;
    }
  }
}
</style>
