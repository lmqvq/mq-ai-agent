<template>
  <section class="hero-section" id="hero">
    <!-- 背景装饰 -->
    <div class="hero-decorations">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-dot dot-1"></div>
      <div class="decoration-dot dot-2"></div>
      <div class="decoration-dot dot-3"></div>
    </div>

    <div class="hero-container">
      <!-- 左侧文案区 -->
      <div class="hero-content">
        <p class="hero-lead">让训练更聪明</p>
        <h1 class="hero-title">
          <span class="title-main">AI</span>
          <span class="title-highlight">&</span>
          <span class="title-main">健身教练</span>
        </h1>
        <p class="hero-description">
          智能制定训练计划、实时姿态纠正、全面数据追踪，<br>
          让科学健身触手可及。
        </p>
        
        <!-- CTA 按钮组 -->
        <div class="hero-cta">
          <router-link to="/login" class="cta-btn cta-primary">
            免费试用
          </router-link>
          <a href="#features" class="cta-btn cta-secondary" @click.prevent="scrollToFeatures">
            了解更多
          </a>
        </div>
      </div>

      <!-- 右侧展示区 -->
      <div class="hero-visual">
        <!-- 健身人物插图 -->
        <div class="illustration-wrapper">
          <img 
            src="@/assets/illustrations/fitness-man.svg" 
            alt="健身人物" 
            class="fitness-illustration"
          />
        </div>
        
        <!-- 手机 Mockup -->
        <div class="phone-mockup-wrapper">
          <img 
            src="@/assets/illustrations/phone-mockup.svg" 
            alt="应用界面" 
            class="phone-mockup"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'HeroSection',
  setup() {
    const scrollToFeatures = () => {
      const element = document.getElementById('features');
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    };

    return {
      scrollToFeatures
    };
  }
};
</script>

<style lang="scss" scoped>
.hero-section {
  position: relative;
  padding: 40px 24px 60px;
  background: var(--landing-bg);
  overflow: hidden;
}

.hero-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    
    &.circle-1 {
      width: 500px;
      height: 500px;
      background: linear-gradient(135deg, var(--landing-mint-bg) 0%, transparent 70%);
      top: -100px;
      right: -100px;
      animation: float 8s ease-in-out infinite;
    }
    
    &.circle-2 {
      width: 300px;
      height: 300px;
      background: linear-gradient(135deg, var(--landing-accent-light) 0%, transparent 70%);
      bottom: 100px;
      left: -50px;
      opacity: 0.6;
      animation: float 10s ease-in-out infinite reverse;
    }
  }

  .decoration-dot {
    position: absolute;
    border-radius: 50%;
    background: var(--landing-accent);
    animation: pulse 4s ease-in-out infinite;
    
    &.dot-1 {
      width: 20px;
      height: 20px;
      top: 30%;
      left: 15%;
      opacity: 0.8;
    }
    
    &.dot-2 {
      width: 14px;
      height: 14px;
      top: 50%;
      right: 40%;
      opacity: 0.6;
      animation-delay: 1s;
    }
    
    &.dot-3 {
      width: 24px;
      height: 24px;
      bottom: 25%;
      left: 45%;
      opacity: 0.5;
      animation-delay: 2s;
    }
  }
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-content {
  animation: slideInLeft 0.8s ease-out;
}

.hero-lead {
  font-size: 24px;
  font-weight: 500;
  color: var(--landing-text-secondary);
  margin-bottom: 16px;
  line-height: 1.4;
}

.hero-title {
  font-size: 72px;
  font-weight: 800;
  color: var(--landing-text-primary);
  line-height: 1.1;
  margin-bottom: 24px;
  letter-spacing: -2px;

  .title-main {
    display: block;
  }

  .title-highlight {
    display: inline-block;
    font-size: 120px;
    font-weight: 200;
    color: var(--landing-border);
    line-height: 0.8;
    margin: -10px 0;
    font-family: 'Times New Roman', serif;
    font-style: italic;
  }
}

.hero-description {
  font-size: 16px;
  line-height: 1.8;
  color: var(--landing-text-secondary);
  margin-bottom: 32px;
  max-width: 480px;
}

.hero-cta {
  display: flex;
  gap: 16px;
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
    
    &:active {
      transform: translateY(0);
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

.hero-visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 450px;
  animation: slideInRight 0.8s ease-out;
}

.illustration-wrapper {
  position: absolute;
  left: -80px;
  bottom: 30px;
  z-index: 3;
  
  .fitness-illustration {
    width: 280px;
    height: auto;
    display: block;
    filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.1));
  }
}

.phone-mockup-wrapper {
  position: relative;
  z-index: 2;
  margin-left: 60px;
  
  .phone-mockup {
    width: 300px;
    height: auto;
    filter: drop-shadow(0 25px 50px rgba(0, 0, 0, 0.15));
    color: var(--landing-text-primary);
  }
}

// 动画
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(3deg);
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

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 响应式
@media (max-width: 1024px) {
  .hero-title {
    font-size: 56px;
    
    .title-highlight {
      font-size: 90px;
    }
  }
  
  .hero-visual {
    min-height: 380px;
  }
  
  .illustration-wrapper {
    left: -10px;
    
    .fitness-illustration {
      width: 200px;
    }
  }
  
  .phone-mockup-wrapper {
    margin-left: 40px;
    
    .phone-mockup {
      width: 260px;
    }
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 30px 20px 40px;
  }
  
  .hero-container {
    grid-template-columns: 1fr;
    gap: 32px;
    text-align: center;
  }
  
  .hero-content {
    order: 1;
  }
  
  .hero-lead {
    font-size: 20px;
  }
  
  .hero-title {
    font-size: 48px;
    
    .title-highlight {
      font-size: 72px;
      display: inline;
    }
    
    .title-main {
      display: inline;
    }
  }
  
  .hero-description {
    margin-left: auto;
    margin-right: auto;
    
    br {
      display: none;
    }
  }
  
  .hero-cta {
    justify-content: center;
  }
  
  .hero-visual {
    order: 2;
    min-height: 320px;
  }
  
  .illustration-wrapper {
    left: 0;
    bottom: 10px;
    
    .fitness-illustration {
      width: 160px;
    }
  }
  
  .phone-mockup-wrapper {
    margin-left: 20px;
    
    .phone-mockup {
      width: 200px;
    }
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 36px;
    
    .title-highlight {
      font-size: 54px;
    }
  }
  
  .hero-description {
    font-size: 15px;
  }
  
  .cta-btn {
    padding: 12px 24px;
    font-size: 15px;
    width: 100%;
  }
  
  .hero-cta {
    flex-direction: column;
  }
  
  .hero-visual {
    min-height: 280px;
  }
  
  .illustration-wrapper .fitness-illustration {
    width: 130px;
  }
  
  .phone-mockup-wrapper {
    margin-left: 10px;
    
    .phone-mockup {
      width: 170px;
    }
  }
}
</style>
