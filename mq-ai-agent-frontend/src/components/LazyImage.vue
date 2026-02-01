<template>
  <div class="lazy-image-wrapper" ref="wrapperRef">
    <div v-if="!isLoaded && !hasError" class="lazy-placeholder">
      <div class="placeholder-bg"></div>
      <div class="placeholder-grain"></div>
      <div class="placeholder-gradient"></div>
      <div class="loading-pulse">
        <div class="pulse-ring"></div>
        <div class="pulse-ring delay-1"></div>
        <div class="pulse-ring delay-2"></div>
      </div>
    </div>
    <img
      v-show="isLoaded"
      ref="imgRef"
      :src="currentSrc"
      :alt="alt"
      class="lazy-image"
      :class="{ loaded: isLoaded }"
      decoding="async"
      @load="onLoad"
      @error="onError"
    />
    <div v-if="hasError" class="lazy-error">
      <div class="error-bg"></div>
      <div class="error-grain"></div>
      <div class="error-content">
        <icon-image />
        <span>图片加载失败</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { IconImage } from '@arco-design/web-vue/es/icon';

export default {
  name: 'LazyImage',
  components: { IconImage },
  props: {
    src: {
      type: String,
      required: true
    },
    alt: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '#f0f0f0'
    },
    // 预加载距离（像素）
    rootMargin: {
      type: String,
      default: '200px'
    }
  },
  emits: ['load', 'error'],
  setup(props, { emit }) {
    const wrapperRef = ref(null);
    const imgRef = ref(null);
    const isInView = ref(false);
    const isLoaded = ref(false);
    const hasError = ref(false);
    let observer = null;

    // 当前显示的src（只有进入视口才加载真实图片）
    const currentSrc = computed(() => {
      return isInView.value ? props.src : '';
    });


    const onLoad = () => {
      isLoaded.value = true;
      hasError.value = false;
      emit('load');
    };

    const onError = () => {
      hasError.value = true;
      isLoaded.value = false;
      emit('error');
    };

    // 当src变化时重置状态
    watch(() => props.src, () => {
      isLoaded.value = false;
      hasError.value = false;
    });

    onMounted(() => {
      if (!wrapperRef.value) return;

      // 使用 Intersection Observer 实现懒加载
      observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              isInView.value = true;
              // 一旦进入视口，停止观察
              observer?.unobserve(entry.target);
            }
          });
        },
        {
          rootMargin: props.rootMargin,
          threshold: 0
        }
      );

      observer.observe(wrapperRef.value);
    });

    onUnmounted(() => {
      if (observer && wrapperRef.value) {
        observer.unobserve(wrapperRef.value);
        observer.disconnect();
        observer = null;
      }
    });

    return {
      wrapperRef,
      imgRef,
      isLoaded,
      hasError,
      currentSrc,
      onLoad,
      onError
    };
  }
};
</script>

<style lang="scss" scoped>
.lazy-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

// 占位符样式
.lazy-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  .placeholder-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      135deg,
      #1a1a2e 0%,
      #16213e 25%,
      #0f3460 50%,
      #1a1a2e 75%,
      #16213e 100%
    );
    background-size: 400% 400%;
    animation: gradientFlow 8s ease infinite;
  }

  // 颗粒感噪点层
  .placeholder-grain {
    position: absolute;
    inset: 0;
    opacity: 0.15;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  }

  // 光晕渐变层
  .placeholder-gradient {
    position: absolute;
    inset: 0;
    background: radial-gradient(
      ellipse at 30% 20%,
      rgba(102, 126, 234, 0.3) 0%,
      transparent 50%
    ),
    radial-gradient(
      ellipse at 70% 80%,
      rgba(118, 75, 162, 0.25) 0%,
      transparent 50%
    );
  }

  // 加载脉冲动画
  .loading-pulse {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 60px;
    height: 60px;

    .pulse-ring {
      position: absolute;
      inset: 0;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      animation: pulseRing 2s ease-out infinite;

      &.delay-1 {
        animation-delay: 0.4s;
      }

      &.delay-2 {
        animation-delay: 0.8s;
      }
    }
  }
}

// 错误状态样式
.lazy-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  .error-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      135deg,
      #2d2d3a 0%,
      #1f1f2e 50%,
      #2d2d3a 100%
    );
  }

  .error-grain {
    position: absolute;
    inset: 0;
    opacity: 0.1;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  }

  .error-content {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: rgba(255, 255, 255, 0.5);
    font-size: 12px;

    :deep(svg) {
      width: 32px;
      height: 32px;
      opacity: 0.4;
    }
  }
}

// 图片样式
.lazy-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;

  &.loaded {
    opacity: 1;
  }
}

// 动画定义
@keyframes gradientFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes pulseRing {
  0% {
    transform: scale(0.5);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
</style>
