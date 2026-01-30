<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script>
import { computed } from 'vue';

export default {
  name: 'MarkdownRenderer',
  props: {
    content: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const renderedContent = computed(() => {
      return renderMarkdown(props.content);
    });

    const renderMarkdown = (text) => {
      if (!text) return '';
      
      let html = text;
      
      // 处理代码块 ```code```
      html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
        const languageClass = lang ? ` language-${lang}` : '';
        const escapedCode = escapeHtml(code.trim());
        return `<div class="code-block"><div class="code-header">${lang || 'code'}</div><pre><code class="${languageClass}">${escapedCode}</code></pre></div>`;
      });
      
      // 处理行内代码 `code`
      html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
      
      // 处理标题 (需要在换行处理之前，从多#到少#的顺序处理)
      // #### 标题4 (支持emoji和特殊字符)
      html = html.replace(/^####\s*(.+)$/gm, '<h5 class="md-heading md-h5">$1</h5>');
      // ### 标题3
      html = html.replace(/^###\s*(.+)$/gm, '<h4 class="md-heading md-h4">$1</h4>');
      // ## 标题2
      html = html.replace(/^##\s*(.+)$/gm, '<h3 class="md-heading md-h3">$1</h3>');
      // # 标题1
      html = html.replace(/^#\s*(.+)$/gm, '<h2 class="md-heading md-h2">$1</h2>');
      
      // 处理【】包裹的标题（常见于中文AI回复）
      html = html.replace(/^\u3010(.+?)\u3011\s*$/gm, '<h4 class="md-heading md-h4 md-bracket-title">$1</h4>');
      
      // 处理引用块 > quote (支持多行和emoji)
      html = html.replace(/^>\s*(.*)$/gm, '<blockquote class="md-quote">$1</blockquote>');
      // 合并连续的引用块
      html = html.replace(/<\/blockquote>\n<blockquote class="md-quote">/g, '<br>');
      
      // 处理有序列表
      html = processOrderedLists(html);
      
      // 处理无序列表
      html = processUnorderedLists(html);
      
      // 处理分隔线 ---
      html = html.replace(/^-{3,}$/gm, '<hr class="md-divider">');
      
      // 处理加粗 **text**
      html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
      
      // 处理斜体 *text* (需要在加粗处理之后，避免匹配单个*)
      html = html.replace(/(?<!\*)\*([^*\n]+)\*(?!\*)/g, '<em>$1</em>');
      
      // 处理删除线 ~~text~~
      html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>');
      
      // 处理链接 [text](url)
      html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer" class="md-link">$1</a>');
      
      // 处理全角空格缩进 - 转为不间断空格保持格式 (\u3000 = 全角空格)
      html = html.replace(/\u3000/g, '&emsp;');
      
      // 处理箭头指示的注释行（常见于AI回复中的触发说明）
      html = html.replace(/^(\u2192|\u279C|->)\s*(.+)$/gm, '<div class="md-note"><span class="md-note-arrow">$1</span> $2</div>');
      
      // 处理段落 - 连续两个换行变成段落分隔
      html = html.replace(/\n\n+/g, '</p><p class="md-paragraph">');
      
      // 处理单个换行
      html = html.replace(/\n/g, '<br>');
      
      // 清理连续的<br>标签（超过2个变成2个）
      html = html.replace(/(<br>\s*){3,}/g, '<br><br>');
      
      // 包装成段落
      if (!html.startsWith('<')) {
        html = `<p class="md-paragraph">${html}</p>`;
      }
      
      // 清理多余的空段落
      html = html.replace(/<p class="md-paragraph"><\/p>/g, '');
      html = html.replace(/<p class="md-paragraph">(<h[2-5]|<ul|<ol|<blockquote|<div class="code-block"|<hr)/g, '$1');
      html = html.replace(/(<\/h[2-5]>|<\/ul>|<\/ol>|<\/blockquote>|<\/div>|<hr class="md-divider">)<\/p>/g, '$1');
      
      return html;
    };

    const escapeHtml = (text) => {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    };

    const processOrderedLists = (html) => {
      const lines = html.split('\n');
      let result = [];
      let inList = false;
      let listItems = [];
      
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const match = line.match(/^(\d+)\. (.+)$/);
        
        if (match) {
          if (!inList) {
            inList = true;
            listItems = [];
          }
          listItems.push(`<li>${match[2]}</li>`);
        } else {
          if (inList) {
            result.push(`<ol class="md-list md-ordered-list">${listItems.join('')}</ol>`);
            inList = false;
            listItems = [];
          }
          result.push(line);
        }
      }
      
      if (inList) {
        result.push(`<ol class="md-list md-ordered-list">${listItems.join('')}</ol>`);
      }
      
      return result.join('\n');
    };

    const processUnorderedLists = (html) => {
      const lines = html.split('\n');
      let result = [];
      let inList = false;
      let listItems = [];
      
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const match = line.match(/^[-*•] (.+)$/);
        
        if (match) {
          if (!inList) {
            inList = true;
            listItems = [];
          }
          listItems.push(`<li>${match[1]}</li>`);
        } else {
          if (inList) {
            result.push(`<ul class="md-list md-unordered-list">${listItems.join('')}</ul>`);
            inList = false;
            listItems = [];
          }
          result.push(line);
        }
      }
      
      if (inList) {
        result.push(`<ul class="md-list md-unordered-list">${listItems.join('')}</ul>`);
      }
      
      return result.join('\n');
    };

    return {
      renderedContent
    };
  }
};
</script>

<style lang="scss">
/* Markdown 渲染样式 - 参考 ChatGPT/Gemini 的简洁风格 */
.markdown-content {
  font-size: 15px;
  line-height: 1.65;
  color: inherit;
  word-wrap: break-word;
  overflow-wrap: anywhere;

  /* 段落样式 */
  .md-paragraph {
    margin: 0 0 8px 0;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  /* 标题样式 - 紧凑的间距 */
  .md-heading {
    font-weight: 600;
    margin: 16px 0 6px 0;
    line-height: 1.4;
    color: var(--theme-text-primary);
    
    &:first-child {
      margin-top: 0;
    }
  }

  .md-h2 {
    font-size: 1.25em;
    padding-bottom: 6px;
    border-bottom: 1px solid var(--theme-border-secondary);
    margin: 18px 0 10px 0;
  }

  .md-h3 {
    font-size: 1.1em;
    margin: 14px 0 6px 0;
  }

  .md-h4 {
    font-size: 1.02em;
    margin: 12px 0 4px 0;
  }

  .md-h5 {
    font-size: 0.98em;
    color: var(--theme-color-primary);
    margin: 10px 0 4px 0;
  }

  /* 【】包裹的标题特殊样式 */
  .md-bracket-title {
    display: inline-block;
    padding: 3px 10px;
    background: linear-gradient(135deg, var(--theme-color-primary-light) 0%, rgba(102, 126, 234, 0.08) 100%);
    border-radius: 4px;
    border-left: 3px solid var(--theme-color-primary);
    margin: 12px 0 6px 0;
  }

  /* 列表样式 */
  .md-list {
    margin: 6px 0 10px 0;
    padding-left: 20px;

    li {
      margin: 2px 0;
      padding: 2px 0;
      line-height: 1.6;
      position: relative;
      
      &::marker {
        color: var(--theme-color-primary);
      }
    }
  }

  .md-ordered-list {
    list-style-type: decimal;
    
    li {
      padding-left: 4px;
      
      &::marker {
        font-weight: 600;
        color: var(--theme-color-primary);
      }
    }
  }

  .md-unordered-list {
    list-style-type: disc;
  }

  /* 引用块样式 */
  .md-quote {
    margin: 10px 0;
    padding: 10px 14px;
    border-left: 3px solid var(--theme-color-primary);
    background-color: var(--theme-bg-card-hover);
    border-radius: 0 6px 6px 0;
    color: var(--theme-text-secondary);
    font-style: normal;
    font-size: 0.92em;
    line-height: 1.55;
  }

  /* 箭头注释行样式 */
  .md-note {
    margin: 4px 0 8px 0;
    padding: 6px 10px;
    background-color: var(--theme-bg-card-hover);
    border-radius: 4px;
    font-size: 0.88em;
    color: var(--theme-text-secondary);
    display: flex;
    align-items: flex-start;
    gap: 4px;
    
    .md-note-arrow {
      color: var(--theme-color-primary);
      font-weight: 600;
      flex-shrink: 0;
    }
  }

  /* 代码块样式 */
  .code-block {
    margin: 12px 0;
    border-radius: 6px;
    overflow: hidden;
    background-color: #1e1e1e;
    box-shadow: var(--theme-shadow-sm);

    .code-header {
      padding: 6px 12px;
      background-color: #2d2d2d;
      color: #858585;
      font-size: 11px;
      font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Mono', monospace;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    pre {
      margin: 0;
      padding: 12px;
      overflow-x: auto;
      
      code {
        font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Mono', monospace;
        font-size: 13px;
        line-height: 1.5;
        color: #d4d4d4;
        background: none;
        padding: 0;
      }
    }
  }

  /* 行内代码样式 */
  .inline-code {
    padding: 1px 5px;
    margin: 0 1px;
    border-radius: 3px;
    background-color: var(--theme-bg-card-hover);
    color: var(--theme-color-primary);
    font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Mono', monospace;
    font-size: 0.88em;
    border: 1px solid var(--theme-border-secondary);
  }

  /* 链接样式 */
  .md-link {
    color: var(--theme-color-primary);
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: border-color 0.2s ease;

    &:hover {
      border-bottom-color: var(--theme-color-primary);
    }
  }

  /* 分隔线样式 - 更紧凑 */
  .md-divider {
    margin: 12px 0;
    border: none;
    border-top: 1px solid var(--theme-border-secondary);
  }

  /* 强调样式 */
  strong {
    font-weight: 600;
    color: var(--theme-text-primary);
  }

  em {
    font-style: italic;
  }

  del {
    text-decoration: line-through;
    color: var(--theme-text-muted);
  }

  /* 表格样式 */
  table {
    width: 100%;
    margin: 12px 0;
    border-collapse: collapse;
    border-radius: 6px;
    overflow: hidden;
    box-shadow: var(--theme-shadow-sm);

    th, td {
      padding: 8px 12px;
      text-align: left;
      border: 1px solid var(--theme-border-secondary);
    }

    th {
      background-color: var(--theme-bg-card-hover);
      font-weight: 600;
      color: var(--theme-text-primary);
    }

    tr:nth-child(even) {
      background-color: var(--theme-bg-card-hover);
    }
  }

  /* 相邻元素的间距优化 */
  .md-heading + .md-paragraph,
  .md-heading + .md-list {
    margin-top: 4px;
  }

  .md-divider + .md-heading {
    margin-top: 10px;
  }

  .md-paragraph + .md-heading {
    margin-top: 14px;
  }

  /* br标签间距 */
  br {
    content: '';
    display: block;
    margin-top: 2px;
  }

  br + br {
    margin-top: 6px;
  }
}

/* 深色模式下的代码块调整 */
[data-theme="dark"] .markdown-content {
  .code-block {
    background-color: #0d1117;
    
    .code-header {
      background-color: #161b22;
      color: #8b949e;
    }
    
    pre code {
      color: #c9d1d9;
    }
  }
  
  .inline-code {
    background-color: rgba(110, 118, 129, 0.2);
    border-color: rgba(110, 118, 129, 0.3);
  }
}
</style>
