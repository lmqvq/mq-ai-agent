<template>
  <div class="landing-page">
    <!-- 导航栏 -->
    <LandingNav />
    
    <!-- Hero 区域 -->
    <HeroSection />
    
    <!-- 优势展示 -->
    <AdvantagesSection />
    
    <!-- 底部 CTA -->
    <CTASection />
    
    <!-- 页脚 -->
    <footer class="landing-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">
              <svg viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="14" fill="var(--landing-mint)"/>
                <path d="M12 16 L16 12 L20 16 L16 20 Z" fill="var(--landing-bg)"/>
              </svg>
              <span>AI健身教练</span>
            </div>
            <p class="footer-tagline">让科学健身触手可及</p>
          </div>
          
          <div class="footer-links">
            <div class="footer-column">
              <h4>产品</h4>
              <router-link to="/fitness">AI健身教练</router-link>
              <router-link to="/agent">AI智能体</router-link>
              <router-link to="/knowledge">健身知识库</router-link>
            </div>
            <div class="footer-column">
              <h4>支持</h4>
              <a href="https://github.com/lmqvq/mq-ai-agent" target="_blank" rel="noopener">GitHub</a>
              <router-link to="/login">登录</router-link>
              <router-link to="/register">注册</router-link>
            </div>
          </div>
        </div>
        
        <div class="footer-bottom">
          <p class="copyright">© {{ currentYear }} AI健身教练. All rights reserved.</p>
          <a 
            href="https://beian.miit.gov.cn/#/Integrated/index" 
            target="_blank" 
            rel="noopener noreferrer"
            class="icp-link"
          >
            桂ICP备2024028689号-4
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { computed } from 'vue';
import LandingNav from '@/components/landing/LandingNav.vue';
import HeroSection from '@/components/landing/HeroSection.vue';
import AdvantagesSection from '@/components/landing/AdvantagesSection.vue';
import CTASection from '@/components/landing/CTASection.vue';

export default {
  name: 'LandingPage',
  components: {
    LandingNav,
    HeroSection,
    AdvantagesSection,
    CTASection
  },
  setup() {
    const currentYear = computed(() => new Date().getFullYear());

    return {
      currentYear
    };
  }
};
</script>

<style lang="scss" scoped>
.landing-page {
  min-height: 100vh;
  background: var(--landing-bg);
  color: var(--landing-text-primary);
  padding-top: 72px;
}

// 页脚
.landing-footer {
  background: var(--landing-bg-secondary);
  border-top: 1px solid var(--landing-border);
  padding: 64px 24px 32px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  margin-bottom: 48px;
}

.footer-brand {
  max-width: 300px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  
  svg {
    width: 32px;
    height: 32px;
  }
  
  span {
    font-size: 18px;
    font-weight: 700;
    color: var(--landing-text-primary);
  }
}

.footer-tagline {
  font-size: 14px;
  color: var(--landing-text-secondary);
  line-height: 1.6;
}

.footer-links {
  display: flex;
  gap: 64px;
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--landing-text-primary);
    margin-bottom: 4px;
  }
  
  a {
    font-size: 14px;
    color: var(--landing-text-secondary);
    text-decoration: none;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--landing-mint-dark);
    }
  }
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid var(--landing-border);
}

.copyright {
  font-size: 13px;
  color: var(--landing-text-secondary);
}

.icp-link {
  font-size: 13px;
  color: var(--landing-text-secondary);
  text-decoration: none;
  
  &:hover {
    color: var(--landing-mint-dark);
  }
}

// 响应式
@media (max-width: 768px) {
  .landing-footer {
    padding: 48px 20px 24px;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .footer-links {
    gap: 40px;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .footer-links {
    flex-direction: column;
    gap: 32px;
  }
}
</style>
