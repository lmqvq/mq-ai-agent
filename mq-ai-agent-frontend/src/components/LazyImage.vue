<template>
  <div class="lazy-image-wrapper" ref="wrapperRef">
    <div v-if="!isLoaded && !hasError" class="lazy-placeholder" :style="placeholderStyle">
      <div class="loading-shimmer"></div>
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
    <div v-if="hasError" class="lazy-error" :style="placeholderStyle">
      <icon-image />
      <span>加载失败</span>
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

    const placeholderStyle = computed(() => ({
      backgroundColor: props.placeholder
    }));

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
      placeholderStyle,
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

.lazy-placeholder,
.lazy-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  gap: 8px;

  :deep(svg) {
    width: 32px;
    height: 32px;
    opacity: 0.5;
  }
}

.lazy-placeholder {
  .loading-shimmer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.3) 50%,
      transparent 100%
    );
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

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

.lazy-error {
  background: #f5f5f5;
}
</style>
