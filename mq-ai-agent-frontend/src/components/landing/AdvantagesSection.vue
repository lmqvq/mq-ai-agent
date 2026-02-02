<template>
  <section class="advantages-section" id="advantages">
    <div class="section-container">
      <!-- 标题 -->
      <div class="section-header">
        <h2 class="section-title">我们的优势</h2>
        <p class="section-subtitle">
          把训练、饮食、恢复放进同一套系统，让健身变得简单高效
        </p>
      </div>

      <!-- 功能卡片 -->
      <div class="features-grid" id="features">
        <div 
          v-for="(feature, index) in features" 
          :key="feature.id"
          class="feature-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="feature-icon" :class="feature.iconClass">
            <component :is="feature.icon" />
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-description">{{ feature.description }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { h } from 'vue';

// 自定义 SVG 图标组件
const IconAI = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('circle', { cx: '12', cy: '12', r: '3' }),
      h('path', { d: 'M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83' })
    ]);
  }
};

const IconChart = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M18 20V10M12 20V4M6 20v-6' })
    ]);
  }
};

const IconTarget = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('circle', { cx: '12', cy: '12', r: '10' }),
      h('circle', { cx: '12', cy: '12', r: '6' }),
      h('circle', { cx: '12', cy: '12', r: '2' })
    ]);
  }
};

const IconHeart = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z' })
    ]);
  }
};

export default {
  name: 'AdvantagesSection',
  setup() {
    const features = [
      {
        id: 'ai-coach',
        title: 'AI 智能教练',
        description: '基于深度学习的智能算法，为您量身定制训练计划，实时调整训练强度。',
        icon: IconAI,
        iconClass: 'icon-ai'
      },
      {
        id: 'data-track',
        title: '全面数据追踪',
        description: '记录每一次训练数据，可视化展示进度，让您清晰看到每一点进步。',
        icon: IconChart,
        iconClass: 'icon-chart'
      },
      {
        id: 'personalized',
        title: '个性化方案',
        description: '根据您的身体状况、健身目标和时间安排，制定最适合您的健身方案。',
        icon: IconTarget,
        iconClass: 'icon-target'
      },
      {
        id: 'health',
        title: '健康生活指导',
        description: '不只是训练，还包括饮食建议、睡眠管理、恢复指导，全方位关注您的健康。',
        icon: IconHeart,
        iconClass: 'icon-heart'
      }
    ];

    return {
      features
    };
  }
};
</script>

<style lang="scss" scoped>
.advantages-section {
  padding: 96px 24px;
  background: var(--landing-bg-secondary);
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-title {
  font-size: 40px;
  font-weight: 700;
  color: var(--landing-text-primary);
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.section-subtitle {
  font-size: 16px;
  line-height: 1.6;
  color: var(--landing-text-secondary);
  max-width: 520px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: var(--landing-bg);
  border-radius: 16px;
  padding: 32px 24px;
  border: 1px solid var(--landing-border);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out backwards;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--landing-shadow-card);
    border-color: transparent;
  }
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  
  svg {
    width: 28px;
    height: 28px;
  }
  
  &.icon-ai {
    background: var(--landing-mint-light);
    color: var(--landing-mint-dark);
  }
  
  &.icon-chart {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
  }
  
  &.icon-target {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
  }
  
  &.icon-heart {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--landing-text-primary);
  margin-bottom: 12px;
}

.feature-description {
  font-size: 14px;
  line-height: 1.7;
  color: var(--landing-text-secondary);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .advantages-section {
    padding: 64px 20px;
  }
  
  .section-title {
    font-size: 32px;
  }
  
  .section-header {
    margin-bottom: 48px;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: 28px;
  }
}
</style>
