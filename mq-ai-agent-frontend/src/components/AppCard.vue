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
   AppCard.vue 样式 - GPU加速流畅动画
================================================================ */

.app-card {
  display: block;
  width: 100%;
  background: var(--theme-bg-card);
  border-radius: 20px;
  text-decoration: none;
  color: var(--theme-text-primary);
  overflow: hidden;
  height: 100%;
  position: relative;
  border: 1px solid var(--theme-border-secondary);
  
  // 启用 GPU 加速
  transform: translateZ(0);
  will-change: transform;
  
  // 只对 transform 和 opacity 做动画（GPU加速属性）
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  // 使用伪元素做阴影动画
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    box-shadow: 
      0 2px 8px rgba(0, 0, 0, 0.04),
      0 4px 16px rgba(0, 0, 0, 0.04);
    opacity: 1;
    transition: opacity 0.4s ease;
    pointer-events: none;
    z-index: 0;
  }
  
  // Hover 阴影层
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    box-shadow: 
      0 8px 24px rgba(0, 0, 0, 0.08),
      0 16px 40px rgba(0, 0, 0, 0.06);
    opacity: 0;
    transition: opacity 0.4s ease;
    pointer-events: none;
    z-index: 0;
  }

  &:hover {
    transform: translateY(-6px) translateZ(0);
    
    &::before {
      opacity: 0;
    }
    
    &::after {
      opacity: 1;
    }
    
    .arrow-icon {
      transform: translateX(4px);
      opacity: 1;
    }
    
    .app-icon {
      transform: translateY(-3px) scale(1.05);
    }
    
    .app-title {
      color: var(--theme-color-primary);
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
    background: var(--theme-bg-card);
    border-radius: inherit;
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
    transition: color 0.3s ease;
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

  // 图标样式 - 简化阴影，使用 transform 做动画
  .app-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    position: relative;
    
    // GPU 加速
    transform: translateZ(0);
    will-change: transform;
    transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    
    // 内部光泽层
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 50%;
      border-radius: 16px 16px 50% 50%;
      background: linear-gradient(
        180deg,
        rgba(255, 255, 255, 0.3) 0%,
        rgba(255, 255, 255, 0) 100%
      );
      pointer-events: none;
    }
    
    // AI健身教练 - 温暖渐变
    &.icon-orange {
      background: linear-gradient(135deg, #ff6b9d 0%, #ff8a80 50%, #ff6b6b 100%);
      box-shadow: 
        0 6px 20px rgba(255, 107, 107, 0.4),
        inset 0 -3px 8px rgba(0, 0, 0, 0.1);
    }
    
    // AI超级智能体 - 清新渐变
    &.icon-blue {
      background: linear-gradient(135deg, #40c9ff 0%, #38d9ff 50%, #1cb5e0 100%);
      box-shadow: 
        0 6px 20px rgba(28, 181, 224, 0.4),
        inset 0 -3px 8px rgba(0, 0, 0, 0.1);
    }
    
    // 健身知识库 - 高级渐变
    &.icon-purple {
      background: linear-gradient(135deg, #b490f5 0%, #c9a0f5 50%, #e0b0ff 100%);
      box-shadow: 
        0 6px 20px rgba(180, 144, 245, 0.4),
        inset 0 -3px 8px rgba(0, 0, 0, 0.08);
    }
    
    // 健身数据中心 - 深邓渐变
    &.icon-green {
      background: linear-gradient(135deg, #1cd8d2 0%, #2db4c2 50%, #1e5799 100%);
      box-shadow: 
        0 6px 20px rgba(28, 180, 194, 0.4),
        inset 0 -3px 8px rgba(0, 0, 0, 0.15);
    }
    
    i {
      font-size: 26px;
      position: relative;
      z-index: 1;
    }
  }
  
  .arrow-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: var(--theme-color-primary-light);
    opacity: 0.6;
    transform: translateZ(0);
    transition: transform 0.3s ease, opacity 0.3s ease;
    
    &::before {
      content: '→';
      font-size: 18px;
      color: var(--theme-color-primary);
      font-weight: 600;
    }
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  .app-card,
  .app-card .app-icon,
  .app-card .arrow-icon {
    transition: none;
  }
}
</style>
