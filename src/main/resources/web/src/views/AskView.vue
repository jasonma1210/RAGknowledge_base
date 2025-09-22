<template>
  <div class="ask-page page-container">
    <div class="page-header">
      <h1 class="page-title">AI问答</h1>
      <p class="page-subtitle">基于知识库的智能问答系统，为您提供精准的答案</p>
    </div>

    <el-card class="ask-card modern-card">
      <div class="ask-form-container">
        <el-form :model="askForm" class="ask-form">
          <el-form-item>
            <el-input
              v-model="askForm.question"
              type="textarea"
              placeholder="请输入您的问题..."
              :rows="4"
              size="large"
              class="question-input"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="ask-actions">
              <el-button 
                type="primary" 
                size="large" 
                class="ask-button"
                :loading="asking"
                @click="askQuestion"
              >
                <el-icon><ChatDotRound /></el-icon>
                {{ asking ? '回答中...' : '提问' }}
              </el-button>
              <el-button 
                @click="clearQuestion"
                class="clear-button"
                :disabled="asking"
              >
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <el-collapse v-model="activeCollapse" class="advanced-options-collapse">
        <el-collapse-item title="高级选项" name="advanced">
          <el-form :model="advancedOptions" label-width="100px" label-position="left" class="advanced-options">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="搜索类型">
                  <el-select 
                    v-model="advancedOptions.searchType" 
                    style="width: 100%"
                    class="option-select"
                  >
                    <el-option label="语义搜索" value="SEMANTIC" />
                    <el-option label="关键词搜索" value="KEYWORD" />
                    <el-option label="混合搜索" value="HYBRID" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="上下文窗口">
                  <el-slider 
                    v-model="advancedOptions.contextWindow" 
                    :min="1" 
                    :max="10" 
                    show-input 
                    :show-input-controls="false"
                    class="slider-input"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最大结果数">
                  <el-slider 
                    v-model="advancedOptions.maxResults" 
                    :min="1" 
                    :max="20" 
                    show-input 
                    :show-input-controls="false"
                    class="slider-input"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>
    
    <el-card class="answer-card modern-card" v-if="answer || streamingAnswer">
      <div class="card-header">
        <h3 class="card-title">AI回答</h3>
        <div class="card-header-right">
          <span class="answer-time" v-if="answerTime > 0">耗时: {{ answerTime }}ms</span>
          <el-button 
            @click="copyAnswer" 
            size="small"
            class="copy-button"
            :disabled="asking"
          >
            <el-icon><CopyDocument /></el-icon>
            复制答案
          </el-button>
        </div>
      </div>
      
      <div class="answer-content">
        <div class="answer-text" v-if="displayedAnswer" v-html="formatAnswer(displayedAnswer)"></div>
        <div class="answer-text" v-else-if="answer && answer.answer" v-html="formatAnswer(answer.answer)"></div>
        <div class="loading-text" v-if="loadingText">{{ loadingText }}</div>
        <span v-if="showCursor" class="typewriter-cursor"></span> <!-- 打字机光标 -->
        
        <div class="answer-confidence" v-if="answer && answer.confidence !== undefined">
          <div class="confidence-header">
            <span class="confidence-label">回答置信度</span>
            <span class="confidence-value">{{ (answer.confidence * 100).toFixed(1) }}%</span>
          </div>
          <el-progress 
            :percentage="Math.round(answer.confidence * 100)" 
            :status="getConfidenceStatus(answer.confidence)"
            class="confidence-progress"
          />
        </div>
        
        <div class="answer-sources" v-if="(answer && answer.sources && answer.sources.length > 0) || (streamingSources && streamingSources.length > 0)">
          <h4 class="sources-title">参考来源:</h4>
          <div 
            v-for="(source, index) in (answer ? answer.sources : streamingSources)" 
            :key="index" 
            class="source-item"
          >
            <div class="source-header">
              <el-tag 
                size="small" 
                :type="getSourceTagType(source.relevance)"
                class="relevance-tag"
              >
                {{ formatRelevance(source.relevance) }}
              </el-tag>
              <span class="source-title-text">{{ source.title }}</span>
            </div>
            <div class="source-content">{{ source.content }}</div>
            <div class="source-meta">
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                {{ source.source }}
              </span>
              <span class="meta-item">
                <el-icon><DataAnalysis /></el-icon>
                相似度: {{ (source.score * 100).toFixed(2) }}%
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="history-card modern-card" v-if="history.length > 0">
      <div class="card-header">
        <h3 class="card-title">问答历史</h3>
        <div class="card-header-right">
          <el-button 
            @click="clearHistory" 
            size="small"
            class="clear-history-button"
          >
            <el-icon><Delete /></el-icon>
            清空历史
          </el-button>
        </div>
      </div>
      
      <div class="history-list">
        <div 
          v-for="(item, index) in history" 
          :key="index" 
          class="history-item"
          @click="loadHistory(item)"
        >
          <div class="history-question">{{ item.question }}</div>
          <div class="history-meta">
            <span class="history-time">{{ formatTime(item.time) }}</span>
            <el-tag 
              v-if="item.answer && item.answer.confidence !== undefined"
              :type="getConfidenceStatus(item.answer.confidence)"
              size="small"
              class="history-confidence"
            >
              {{ (item.answer.confidence * 100).toFixed(0) }}%
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, 
  Delete, 
  CopyDocument, 
  Document, 
  DataAnalysis 
} from '@element-plus/icons-vue'
import { searchAPI } from '@/services/api'

const asking = ref(false)
const answerTime = ref(0)
const activeCollapse = ref([]) // 控制折叠面板展开状态

// 问答表单
const askForm = reactive({
  question: ''
})

// 高级选项
const advancedOptions = reactive({
  searchType: 'SEMANTIC',
  contextWindow: 3,
  maxResults: 10
})

// 答案
const answer = ref<any>(null)

// 流式回答相关状态
const streamingAnswer = ref('')
const streamingSources = ref<any[]>([])
const eventSource = ref<EventSource | null>(null)
// 打字机效果相关状态
const displayedAnswer = ref('')
const typingTimer = ref<number | null>(null)
const showCursor = ref(false) // 控制光标显示状态
const loadingText = ref('') // 加载中文字效果
const loadingTimer = ref<number | null>(null) // 加载文字定时器
// 历史记录
const history = ref<any[]>([])

// 提问
const askQuestion = async () => {
  if (!askForm.question.trim()) {
    ElMessage.warning('请输入您的问题')
    return
  }
  
  asking.value = true
  answer.value = null
  streamingAnswer.value = ''
  streamingSources.value = []
  // 重置相关状态
  displayedAnswer.value = ''
  showCursor.value = true // 显示光标
  loadingText.value = 'AI思考中' // 初始化加载文字
  startLoadingTextEffect() // 启动加载文字效果
  
  if (typingTimer.value) {
    clearInterval(typingTimer.value)
    typingTimer.value = null
  }
  const startTime = Date.now()
  
  // 保存读取器引用，用于可能的取消操作
  let reader: ReadableStreamDefaultReader<Uint8Array> | null = null
  
  try {
    // 构造请求数据
    const requestData = {
      question: askForm.question,
      searchType: advancedOptions.searchType,
      maxResults: advancedOptions.maxResults,
      minScore: 0.7,
      contextWindow: advancedOptions.contextWindow
    }
    
    // 使用流式请求
    const response = await fetch('/api/search/ask/streaming', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(requestData)
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    if (!response.body) {
      throw new Error('ReadableStream not supported')
    }
    
    reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8') // 关键：避免中文乱码
    
    // 清除加载文字效果
    if (loadingTimer.value) {
      clearInterval(loadingTimer.value)
      loadingTimer.value = null
    }
    loadingText.value = ''
    
    // 读取流式响应
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true }) // stream:true 表示后续还有数据
      
      // 解析SSE格式的数据
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5) // 去掉 'data:' 前缀
          // 直接将数据追加到最终答案中
          displayedAnswer.value += data
        } else if (line.startsWith('event:') && line.substring(6) === 'complete') {
          // 处理完成事件
          console.log('Stream completed')
        }
      }
    }
    
    // 流结束后，隐藏光标
    showCursor.value = false
    
    // 完成后设置答案
    answerTime.value = Date.now() - startTime
    answer.value = {
      answer: displayedAnswer.value,
      sources: streamingSources.value,
      answerTime: answerTime.value
    }
    
    // 添加到历史记录
    history.value.unshift({
      question: askForm.question,
      answer: answer.value,
      time: new Date()
    })
    
    // 保留最近10条记录
    if (history.value.length > 10) {
      history.value.pop()
    }
  } catch (error: any) {
    console.error('提问失败:', error)
    ElMessage.error('提问失败: ' + (error.message || '网络错误'))
    // 出错时隐藏光标和加载文字
    showCursor.value = false
    loadingText.value = ''
    if (loadingTimer.value) {
      clearInterval(loadingTimer.value)
      loadingTimer.value = null
    }
  } finally {
    asking.value = false
    // 清理定时器
    if (typingTimer.value) {
      clearInterval(typingTimer.value)
      typingTimer.value = null
    }
    if (loadingTimer.value) {
      clearInterval(loadingTimer.value)
      loadingTimer.value = null
    }
  }
}

// 启动打字机效果
const startTypingEffect = () => {
  if (typingTimer.value) {
    clearInterval(typingTimer.value)
  }
  
  // 定时器：每 50ms 渲染 1 个字符（可调整速度）
  typingTimer.value = window.setInterval(() => {
    // 不再需要处理缓冲区，因为我们直接将数据追加到最终答案中
  }, 50) // 50ms 间隔，可以根据需要调整速度
}

// 启动加载文字效果
const startLoadingTextEffect = () => {
  if (loadingTimer.value) {
    clearInterval(loadingTimer.value)
  }
  
  // 定时器：每 500ms 添加一个点
  let dotCount = 0
  loadingTimer.value = window.setInterval(() => {
    dotCount = (dotCount + 1) % 4 // 0, 1, 2, 3 循环
    loadingText.value = 'AI思考中' + '.'.repeat(dotCount)
  }, 500)
}

// 等待打字机效果完成
const waitForTypingComplete = () => {
  return new Promise<void>((resolve) => {
    const checkCompletion = () => {
      // 不再需要等待缓冲区清空，因为我们直接将数据追加到最终答案中
      if (!typingTimer.value) {
        resolve()
      } else {
        setTimeout(checkCompletion, 50)
      }
    }
    checkCompletion()
  })
}

// 清空问题
const clearQuestion = () => {
  askForm.question = ''
  answer.value = null
  streamingAnswer.value = ''
  streamingSources.value = []
  displayedAnswer.value = ''
  showCursor.value = false // 隐藏光标
  loadingText.value = '' // 清除加载文字
  if (typingTimer.value) {
    clearInterval(typingTimer.value)
    typingTimer.value = null
  }
  if (loadingTimer.value) {
    clearInterval(loadingTimer.value)
    loadingTimer.value = null
  }
}

// 格式化答案
const formatAnswer = (text: string) => {
  if (!text) return ''
  // 简单的换行处理
  return text.replace(/\n/g, '<br>')
}

// 获取置信度状态
const getConfidenceStatus = (confidence: number) => {
  if (confidence >= 0.8) return 'success'
  if (confidence >= 0.6) return 'warning'
  return 'exception'
}

// 获取来源标签类型
const getSourceTagType = (relevance: string) => {
  const typeMap: Record<string, 'success' | 'warning' | 'danger' | ''> = {
    'HIGH': 'success',
    'MEDIUM': 'warning',
    'LOW': 'danger'
  }
  return typeMap[relevance] || ''
}

// 格式化相关度
const formatRelevance = (relevance: string) => {
  const relevanceMap: Record<string, string> = {
    'HIGH': '高相关',
    'MEDIUM': '中相关',
    'LOW': '低相关'
  }
  return relevanceMap[relevance] || relevance
}

// 格式化时间
const formatTime = (time: Date) => {
  return time.toLocaleString('zh-CN')
}

// 加载历史记录
const loadHistory = (item: any) => {
  askForm.question = item.question
  answer.value = item.answer
  answerTime.value = item.answer.answerTime || 0
  streamingAnswer.value = ''
  streamingSources.value = []
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  ElMessage.success('历史记录已清空')
}

// 复制答案
const copyAnswer = () => {
  const textToCopy = answer.value?.answer || streamingAnswer.value
  if (textToCopy) {
    navigator.clipboard.writeText(textToCopy)
      .then(() => {
        ElMessage.success('答案已复制到剪贴板')
      })
      .catch(() => {
        ElMessage.error('复制失败')
      })
  }
}
</script>

<style scoped>
.ask-page {
  padding: 20px;
  background-color: var(--color-bg-primary);
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.modern-card {
  margin-bottom: 20px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-2);
  border: 1px solid var(--color-border);
  transition: box-shadow 0.3s ease;
}

.modern-card:hover {
  box-shadow: var(--shadow-3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.card-header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ask-form-container {
  padding: 20px 0;
}

.question-input {
  border-radius: var(--border-radius-md);
}

.ask-actions {
  display: flex;
  gap: 12px;
}

.ask-button {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: #1890ff;
  flex: 1;
}

.ask-button:hover {
  background-color: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.clear-button {
  border-color: var(--color-border);
}

.advanced-options-collapse {
  margin-top: 20px;
}

:deep(.el-collapse-item__header) {
  font-weight: 500;
  color: var(--color-text-primary);
  border-bottom-color: var(--color-border);
}

.advanced-options {
  padding: 20px 0 10px;
}

.option-select {
  border-radius: var(--border-radius-md);
}

.slider-input {
  width: 100%;
}

.answer-time {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.copy-button {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.answer-content {
  padding: 10px 0;
}

.answer-text {
  line-height: 1.8;
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.loading-text {
  color: var(--color-text-secondary);
  font-style: italic;
  margin-bottom: 30px;
}

/* 打字机光标样式 */
.typewriter-cursor {
  display: inline-block;
  width: 8px;
  height: 1.2em;
  background: var(--color-text-primary);
  margin-left: 2px;
  animation: blink 0.8s infinite alternate;
}

@keyframes blink {
  from { opacity: 1; }
  to { opacity: 0; }
}

.answer-confidence {
  margin-bottom: 30px;
  padding: 20px;
  background-color: var(--color-bg-secondary);
  border-radius: var(--border-radius-md);
}

.confidence-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.confidence-label {
  font-weight: 500;
  color: var(--color-text-primary);
}

.confidence-value {
  font-weight: 600;
  color: var(--color-text-primary);
}

.confidence-progress {
  margin-top: 8px;
}

.sources-title {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 16px;
}

.source-item {
  padding: 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-md);
  margin-bottom: 16px;
  transition: border-color 0.2s;
}

.source-item:hover {
  border-color: var(--color-primary-light);
}

.source-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.source-title-text {
  font-weight: 500;
  color: var(--color-text-primary);
  flex: 1;
}

.relevance-tag {
  margin: 0;
}

.source-content {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  font-size: 14px;
}

.source-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: var(--color-bg-hover);
  border-radius: var(--border-radius-md);
}

.history-item:last-child {
  border-bottom: none;
}

.history-question {
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.history-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.history-confidence {
  margin: 0;
}

.clear-history-button {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ask-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header-right {
    align-self: stretch;
    justify-content: space-between;
  }
  
  .ask-actions {
    flex-direction: column;
  }
  
  .advanced-options .el-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .advanced-options .el-col {
    width: 100%;
  }
  
  .source-meta {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .history-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>