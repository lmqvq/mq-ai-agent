/**
 * SSE 消息解析器
 * 
 * 用于解析 AI 超级智能体返回的 SSE 消息，支持：
 * - 思考内容（thinking）
 * - 工具调用状态（tool_start/tool_complete/tool_error）
 * - 步骤进度（step_start）
 * - 最终结果（result）
 * - 错误信息（error）
 * - 完成信号（complete）
 */

// SSE 事件类型常量
export const SSE_EVENT_TYPES = {
  THINKING: 'thinking',
  TOOL_START: 'tool_start',
  TOOL_COMPLETE: 'tool_complete',
  TOOL_ERROR: 'tool_error',
  STEP_START: 'step_start',
  STEP_COMPLETE: 'step_complete',
  RESULT: 'result',
  ERROR: 'error',
  COMPLETE: 'complete'
};

/**
 * 解析 SSE 消息数据
 * 
 * @param {string} data - SSE 消息的原始数据
 * @returns {Object|null} 解析后的消息对象，解析失败返回 null
 */
export function parseSseMessage(data) {
  if (!data) return null;
  
  try {
    // 尝试作为 JSON 解析
    const parsed = JSON.parse(data);
    
    // 验证必须有 type 字段
    if (parsed && parsed.type) {
      return {
        type: parsed.type,
        content: parsed.content || '',
        toolName: parsed.toolName || '',
        status: parsed.status || '',
        summary: parsed.summary || '',
        arguments: parsed.arguments || '',
        result: parsed.result || '',
        stepNumber: parsed.stepNumber || 0,
        maxSteps: parsed.maxSteps || 0,
        collapsible: parsed.collapsible !== false
      };
    }
    
    // 没有 type 字段，可能是旧格式的纯文本
    return null;
  } catch {
    // JSON 解析失败，返回 null 表示是旧格式的纯文本消息
    return null;
  }
}

/**
 * 判断消息是否是新的结构化格式
 * 
 * @param {string} data - SSE 消息的原始数据
 * @returns {boolean} 是否是结构化格式
 */
export function isStructuredMessage(data) {
  if (!data) return false;
  
  try {
    const parsed = JSON.parse(data);
    return parsed && typeof parsed.type === 'string';
  } catch {
    return false;
  }
}

/**
 * 判断是否是工具相关的事件
 * 
 * @param {string} type - 事件类型
 * @returns {boolean}
 */
export function isToolEvent(type) {
  return [
    SSE_EVENT_TYPES.TOOL_START,
    SSE_EVENT_TYPES.TOOL_COMPLETE,
    SSE_EVENT_TYPES.TOOL_ERROR
  ].includes(type);
}

/**
 * 判断是否是终止事件
 * 
 * @param {string} type - 事件类型
 * @returns {boolean}
 */
export function isTerminalEvent(type) {
  return [
    SSE_EVENT_TYPES.RESULT,
    SSE_EVENT_TYPES.ERROR,
    SSE_EVENT_TYPES.COMPLETE
  ].includes(type);
}

/**
 * 将工具状态转换为显示状态
 * 
 * @param {string} eventType - 事件类型
 * @returns {string} - 'executing' | 'completed' | 'failed'
 */
export function getToolStatusFromEvent(eventType) {
  switch (eventType) {
    case SSE_EVENT_TYPES.TOOL_START:
      return 'executing';
    case SSE_EVENT_TYPES.TOOL_COMPLETE:
      return 'completed';
    case SSE_EVENT_TYPES.TOOL_ERROR:
      return 'failed';
    default:
      return 'executing';
  }
}

/**
 * 创建 SSE 消息处理器
 * 
 * 用于 ChatManager 中处理 SSE 消息流
 * 
 * @param {Object} callbacks - 回调函数集合
 * @param {Function} callbacks.onThinking - 收到思考内容时的回调
 * @param {Function} callbacks.onToolStart - 工具开始执行时的回调
 * @param {Function} callbacks.onToolComplete - 工具执行完成时的回调
 * @param {Function} callbacks.onToolError - 工具执行失败时的回调
 * @param {Function} callbacks.onStepStart - 步骤开始时的回调
 * @param {Function} callbacks.onResult - 收到最终结果时的回调
 * @param {Function} callbacks.onError - 收到错误时的回调
 * @param {Function} callbacks.onComplete - 任务完成时的回调
 * @param {Function} callbacks.onLegacyMessage - 收到旧格式消息时的回调
 * @returns {Function} 消息处理函数
 */
export function createSseHandler(callbacks) {
  return (data) => {
    const parsed = parseSseMessage(data);
    
    if (!parsed) {
      // 旧格式的纯文本消息
      if (callbacks.onLegacyMessage) {
        callbacks.onLegacyMessage(data);
      }
      return;
    }
    
    switch (parsed.type) {
      case SSE_EVENT_TYPES.THINKING:
        if (callbacks.onThinking) {
          callbacks.onThinking(parsed.content);
        }
        break;
        
      case SSE_EVENT_TYPES.TOOL_START:
        if (callbacks.onToolStart) {
          callbacks.onToolStart({
            toolName: parsed.toolName,
            arguments: parsed.arguments,
            summary: parsed.summary,
            collapsible: parsed.collapsible
          });
        }
        break;
        
      case SSE_EVENT_TYPES.TOOL_COMPLETE:
        if (callbacks.onToolComplete) {
          callbacks.onToolComplete({
            toolName: parsed.toolName,
            result: parsed.result,
            summary: parsed.summary,
            collapsible: parsed.collapsible
          });
        }
        break;
        
      case SSE_EVENT_TYPES.TOOL_ERROR:
        if (callbacks.onToolError) {
          callbacks.onToolError({
            toolName: parsed.toolName,
            result: parsed.result,
            summary: parsed.summary
          });
        }
        break;
        
      case SSE_EVENT_TYPES.STEP_START:
        if (callbacks.onStepStart) {
          callbacks.onStepStart({
            stepNumber: parsed.stepNumber,
            maxSteps: parsed.maxSteps
          });
        }
        break;
        
      case SSE_EVENT_TYPES.RESULT:
        if (callbacks.onResult) {
          callbacks.onResult(parsed.content);
        }
        break;
        
      case SSE_EVENT_TYPES.ERROR:
        if (callbacks.onError) {
          callbacks.onError(parsed.content);
        }
        break;
        
      case SSE_EVENT_TYPES.COMPLETE:
        if (callbacks.onComplete) {
          callbacks.onComplete();
        }
        break;
        
      default:
        console.warn('Unknown SSE event type:', parsed.type);
    }
  };
}

export default {
  SSE_EVENT_TYPES,
  parseSseMessage,
  isStructuredMessage,
  isToolEvent,
  isTerminalEvent,
  getToolStatusFromEvent,
  createSseHandler
};
