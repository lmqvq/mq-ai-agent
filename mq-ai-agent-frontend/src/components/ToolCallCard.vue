<template>
  <div 
    class="tool-call-card" 
    :class="[statusClass, { collapsed: isCollapsed }]"
  >
    <div class="tool-header" @click="toggleCollapse">
      <span class="tool-icon">{{ toolIcon }}</span>
      <span class="tool-name">{{ displayName }}</span>
      <span class="tool-summary">{{ summary }}</span>
      <span class="tool-status-indicator">
        <span v-if="status === 'executing'" class="status-spinner"></span>
        <icon-check-circle v-else-if="status === 'completed'" class="status-icon success" />
        <icon-close-circle v-else-if="status === 'failed'" class="status-icon error" />
      </span>
      <span v-if="collapsible && hasDetails" class="expand-icon">
        <icon-down v-if="isCollapsed" />
        <icon-up v-else />
      </span>
    </div>
    <transition name="slide-fade">
      <div v-show="!isCollapsed && hasDetails" class="tool-content">
        <div v-if="arguments" class="tool-section">
          <div class="section-label">参数</div>
          <div class="section-content">{{ formatArguments }}</div>
        </div>
        <div v-if="result" class="tool-section">
          <div class="section-label">结果</div>
          <div class="section-content result-content">{{ truncatedResult }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { IconCheckCircle, IconCloseCircle, IconDown, IconUp } from '@arco-design/web-vue/es/icon';

export default {
  name: 'ToolCallCard',
  components: {
    IconCheckCircle,
    IconCloseCircle,
    IconDown,
    IconUp
  },
  props: {
    toolName: {
      type: String,
      required: true
    },
    status: {
      type: String,
      default: 'executing', // executing, completed, failed
      validator: (value) => ['executing', 'completed', 'failed'].includes(value)
    },
    summary: {
      type: String,
      default: ''
    },
    arguments: {
      type: String,
      default: ''
    },
    result: {
      type: String,
      default: ''
    },
    collapsible: {
      type: Boolean,
      default: true
    },
    defaultCollapsed: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const isCollapsed = ref(props.defaultCollapsed);
    
    // 工具图标映射
    const toolIconMap = {
      searchWeb: '🔍',
      googleSearch: '🔍',
      crawl: '🌐',
      writeFile: '📝',
      readFile: '📄',
      doTerminate: '✅',
      default: '🔧'
    };
    
    // 工具显示名称映射
    const toolNameMap = {
      searchWeb: '网页搜索',
      googleSearch: 'Google搜索',
      crawl: '网页爬取',
      writeFile: '写入文件',
      readFile: '读取文件',
      doTerminate: '结束任务'
    };
    
    const toolIcon = computed(() => {
      return toolIconMap[props.toolName] || toolIconMap.default;
    });
    
    const displayName = computed(() => {
      return toolNameMap[props.toolName] || props.toolName;
    });
    
    const statusClass = computed(() => {
      return `status-${props.status}`;
    });
    
    const hasDetails = computed(() => {
      return props.arguments || props.result;
    });
    
    const formatArguments = computed(() => {
      if (!props.arguments) return '';
      try {
        const parsed = JSON.parse(props.arguments);
        return JSON.stringify(parsed, null, 2);
      } catch {
        return props.arguments;
      }
    });
    
    const truncatedResult = computed(() => {
      if (!props.result) return '';
      const maxLen = 500;
      if (props.result.length > maxLen) {
        return props.result.substring(0, maxLen) + '...';
      }
      return props.result;
    });
    
    const toggleCollapse = () => {
      if (props.collapsible && hasDetails.value) {
        isCollapsed.value = !isCollapsed.value;
      }
    };
    
    return {
      isCollapsed,
      toolIcon,
      displayName,
      statusClass,
      hasDetails,
      formatArguments,
      truncatedResult,
      toggleCollapse
    };
  }
};
</script>

<style lang="scss" scoped>
.tool-call-card {
  margin: 8px 0;
  border-radius: 8px;
  border: 1px solid var(--theme-border-primary);
  background-color: var(--theme-bg-card);
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--theme-border-hover, var(--theme-border-secondary));
  }
  
  &.status-executing {
    border-left: 3px solid var(--theme-color-primary, #165DFF);
  }
  
  &.status-completed {
    border-left: 3px solid var(--theme-color-success, #00B42A);
  }
  
  &.status-failed {
    border-left: 3px solid var(--theme-color-error, #F53F3F);
  }
}

.tool-header {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  user-select: none;
  gap: 8px;
  
  &:hover {
    background-color: var(--theme-bg-card-hover);
  }
}

.tool-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tool-name {
  font-weight: 500;
  color: var(--theme-text-primary);
  font-size: 13px;
  flex-shrink: 0;
}

.tool-summary {
  flex: 1;
  color: var(--theme-text-secondary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-status-indicator {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

.status-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--theme-border-primary);
  border-top-color: var(--theme-color-primary, #165DFF);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.status-icon {
  width: 16px;
  height: 16px;
  
  &.success {
    color: var(--theme-color-success, #00B42A);
  }
  
  &.error {
    color: var(--theme-color-error, #F53F3F);
  }
}

.expand-icon {
  display: flex;
  align-items: center;
  color: var(--theme-text-muted);
  flex-shrink: 0;
  
  svg {
    width: 14px;
    height: 14px;
  }
}

.tool-content {
  padding: 0 12px 12px;
  border-top: 1px solid var(--theme-border-primary);
  margin-top: 0;
}

.tool-section {
  margin-top: 8px;
  
  .section-label {
    font-size: 11px;
    color: var(--theme-text-muted);
    margin-bottom: 4px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .section-content {
    font-size: 12px;
    color: var(--theme-text-secondary);
    background-color: var(--theme-bg-page);
    padding: 8px;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 150px;
    overflow-y: auto;
  }
  
  .result-content {
    max-height: 200px;
  }
}

// 过渡动画
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
