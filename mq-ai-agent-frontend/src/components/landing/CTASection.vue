<template>
  <section class="cta-section">
    <!-- 装饰圆点 -->
    <div class="cta-decorations">
      <div class="decoration-dot dot-left"></div>
      <div class="decoration-dot dot-right"></div>
    </div>

    <div class="cta-container">
      <!-- 瑜伽插图 -->
      <div class="cta-illustration">
        <img 
          src="@/assets/illustrations/yoga-woman.svg" 
          alt="瑜伽冥想" 
          class="yoga-illustration"
        />
      </div>

      <!-- 文案内容 -->
      <div class="cta-content">
        <h2 class="cta-title">与我们一起变得更好</h2>
        <p class="cta-description">
          查阅数百篇专业文章，从健身技巧到饮食建议，<br>
          从减脂方法到正念冥想，助您全面提升身心健康。
        </p>
        
        <!-- CTA 按钮 -->
        <div class="cta-buttons">
          <router-link to="/login" class="cta-btn cta-primary">
            免费试用
          </router-link>
          <router-link to="/knowledge" class="cta-btn cta-secondary">
            浏览知识库
          </router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'CTASection'
};
</script>

<style lang="scss" scoped>
.cta-section {
  position: relative;
  padding: 80px 24px 96px;
  background: var(--landing-bg);
  overflow: hidden;
}

.cta-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .decoration-dot {
    position: absolute;
    border-radius: 50%;
    background: var(--landing-accent);
    animation: pulse 4s ease-in-out infinite;
    
    &.dot-left {
      width: 16px;
      height: 16px;
      top: 30%;
      left: 10%;
      opacity: 0.7;
    }
    
    &.dot-right {
      width: 20px;
      height: 20px;
      top: 25%;
      right: 12%;
      opacity: 0.6;
      animation-delay: 1.5s;
    }
  }
}

.cta-container {
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 1;
}

.cta-illustration {
  margin-bottom: 40px;
  animation: float 6s ease-in-out infinite;
  display: inline-block;
  
  .yoga-illustration {
    width: 240px;
    height: auto;
    display: block;
    filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.08));
  }
}

.cta-content {
  animation: fadeInUp 0.6s ease-out;
}

.cta-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--landing-text-primary);
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.cta-description {
  font-size: 16px;
  line-height: 1.8;
  color: var(--landing-text-secondary);
  margin-bottom: 32px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.cta-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  
  &.cta-primary {
    background: var(--landing-btn-primary-bg);
    color: var(--landing-btn-primary-text);
    box-shadow: 0 4px 14px rgba(11, 15, 13, 0.15);
    
    &:hover {
      background: var(--landing-btn-primary-hover);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(11, 15, 13, 0.2);
    }
  }
  
  &.cta-secondary {
    background: var(--landing-btn-secondary-bg);
    color: var(--landing-btn-secondary-text);
    border: 1px solid transparent;
    
    &:hover {
      background: var(--landing-btn-secondary-hover);
      border-color: var(--landing-mint);
    }
  }
}

// 动画
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
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
@media (max-width: 768px) {
  .cta-section {
    padding: 60px 20px 80px;
  }
  
  .cta-illustration .yoga-illustration {
    width: 200px;
  }
  
  .cta-title {
    font-size: 28px;
  }
  
  .cta-description {
    br {
      display: none;
    }
  }
}

@media (max-width: 480px) {
  .cta-illustration .yoga-illustration {
    width: 160px;
  }
  
  .cta-title {
    font-size: 24px;
  }
  
  .cta-buttons {
    flex-direction: column;
  }
  
  .cta-btn {
    width: 100%;
  }
}
</style>
