<template>
  <div class="conversation-view">
    <!-- 左侧：会话列表 -->
    <div class="conversation-sidebar">
      <div class="sidebar-header">
        <h3>对话历史</h3>
        <el-button type="success" size="small" @click="createNewConversation" round>
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
      </div>

      <!-- 统计信息卡片 -->
      <div v-if="stats" class="stats-card">
        <div class="stat-item">
          <span class="stat-label">会话总数</span>
          <span class="stat-value">{{ stats.totalConversations }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">消息总数</span>
          <span class="stat-value">{{ stats.totalMessages }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">总问题数</span>
          <span class="stat-value">{{ stats.totalQuestions }}</span>
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="conversation-list">
        <el-scrollbar>
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: currentConversationId === conv.id }"
            @click="selectConversation(conv.id)"
          >
            <div class="conversation-info">
              <div class="conversation-name">{{ conv.sessionName }}</div>
              <div class="conversation-meta">
                <span>{{ conv.messageCount }} 条消息</span>
                <span>{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
            </div>
            <el-button
              type="danger"
              size="small"
              circle
              @click.stop="deleteConversation(conv.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <el-button type="info" text @click="loadMoreConversations" round>
            <el-icon><More /></el-icon>
            加载更多
          </el-button>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 右侧：对话区域 -->
    <div class="conversation-main">
      <!-- 对话头部 -->
      <div class="conversation-header">
        <div class="header-title">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ currentConversation?.sessionName || 'AI助手' }}</span>
        </div>
        <div class="header-actions">
          <el-button type="info" text @click="showHistoryDialog = true" round>
            <el-icon><Clock /></el-icon>
            历史记录
          </el-button>
          <el-switch
            v-model="enableContext"
            active-text="上下文记忆"
            style="margin-left: 16px"
          />
        </div>
      </div>

      <!-- 对话内容 -->
      <div ref="messageContainer" class="message-container">
        <el-scrollbar>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.messageType.toLowerCase()"
          >
            <div class="message-avatar">
              <el-avatar v-if="msg.messageType === 'USER'" :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-avatar v-else :size="40" style="background: #409eff">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>

            <div class="message-content">
              <div class="message-header">
                <span class="message-sender">
                  {{ msg.messageType === 'USER' ? '我' : 'AI助手' }}
                </span>
                <span class="message-time">{{ formatTime(msg.createTime) }}</span>
              </div>

              <div class="message-text">
                <div v-html="renderMarkdown(msg.content)"></div>
                <span v-if="msg.isStreaming" class="streaming-cursor">|</span>
              </div>

              <!-- AI回答的引用来源 -->
              <div v-if="msg.messageType === 'ASSISTANT' && msg.sourceCount > 0" class="message-sources">
                <el-icon><Document /></el-icon>
                <span>引用了 {{ msg.sourceCount }} 个文档</span>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="isLoading" class="message-item assistant">
            <div class="message-avatar">
              <el-avatar :size="40" style="background: #409eff">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="loading-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 输入区域 -->
      <div class="input-container">
        <div class="input-toolbar">
          <el-select v-model="searchType" size="small" style="width: 120px">
            <el-option label="混合搜索" value="HYBRID" />
            <el-option label="语义搜索" value="SEMANTIC" />
            <el-option label="关键词搜索" value="KEYWORD" />
          </el-select>
        </div>

        <div class="input-area">
          <el-input
            v-model="question"
            type="textarea"
            placeholder="输入您的问题... (Ctrl+Enter 发送)"
            :rows="3"
            @keydown.ctrl.enter="sendMessage"
          />
          <el-button
            type="primary"
            :loading="isLoading"
            :disabled="!question.trim()"
            @click="sendMessage"
            round
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 历史记录弹窗 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="历史记录"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-scrollbar height="500px">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="history-item"
          @click="viewHistoryDetail(msg)"
        >
          <div class="history-question">
            <el-icon><ChatDotSquare /></el-icon>
            {{ msg.content.substring(0, 50) }}{{ msg.content.length > 50 ? '...' : '' }}
          </div>
          <div class="history-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </el-scrollbar>
    </el-dialog>

    <!-- 历史详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="'问题详情'"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedMessage" class="detail-content">
        <div class="detail-section">
          <h4>问题</h4>
          <p>{{ selectedMessage.content }}</p>
        </div>

        <div v-if="selectedMessage.messageType === 'USER'" class="detail-section">
          <h4>AI回答</h4>
          <div v-html="renderMarkdown(getNextAssistantMessage(selectedMessage)?.content || '暂无回答')"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  ChatDotRound,
  Clock,
  User,
  Document,
  ChatDotSquare
} from '@element-plus/icons-vue'
import { conversationAPI } from '@/services/api'
import { marked } from 'marked'

// ========== 数据状态 ==========
const conversations = ref<any[]>([])
const currentConversationId = ref<number | null>(null)
const currentConversation = computed(() =>
  conversations.value.find(c => c.id === currentConversationId.value)
)

const messages = ref<any[]>([])
const question = ref('')
const isLoading = ref(false)
const enableContext = ref(true)
const searchType = ref('HYBRID')

const page = ref(0)
const pageSize = ref(20)
const hasMore = ref(true)

const showHistoryDialog = ref(false)
const showDetailDialog = ref(false)
const selectedMessage = ref<any>(null)
const stats = ref<any>(null)

const messageContainer = ref<HTMLElement>()

// ========== 生命周期 ==========
onMounted(() => {
  loadConversations()
  loadStats()
})

// ========== 方法 ==========

// 加载会话列表
async function loadConversations() {
  try {
    const response = await conversationAPI.getConversations(page.value, pageSize.value)
    if (response.success) {
      if (page.value === 0) {
        conversations.value = response.data
      } else {
        conversations.value.push(...response.data)
      }
      hasMore.value = conversations.value.length < ((response as any).total || 0)

      // 自动选择第一个会话
      if (conversations.value.length > 0 && !currentConversationId.value) {
        selectConversation(conversations.value[0].id)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 加载更多会话
function loadMoreConversations() {
  page.value++
  loadConversations()
}

// 防抖计时器
let statsRefreshTimer: ReturnType<typeof setTimeout> | null = null

// 加载统计信息
async function loadStats() {
  // 清除之前的计时器
  if (statsRefreshTimer) {
    clearTimeout(statsRefreshTimer)
  }
  
  // 设置新的计时器，避免频繁调用
  statsRefreshTimer = setTimeout(async () => {
    try {
      const response = await conversationAPI.getStats()
      if (response.success) {
        stats.value = response.data
      }
    } catch (error) {
      console.error('加载统计信息失败:', error)
    }
  }, 300) // 300ms防抖延迟
}

// 选择会话
async function selectConversation(conversationId: number) {
  currentConversationId.value = conversationId

  try {
    const response = await conversationAPI.getMessages(conversationId.toString())
    if (response.success) {
      messages.value = response.data
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

// 创建新会话
async function createNewConversation() {
  try {
    // 创建空会话
    const response = await conversationAPI.createConversation()
    
    if (response.success) {
      conversations.value.unshift(response.data)
      selectConversation(response.data.id)
      loadStats() // 刷新统计信息
      ElMessage.success('创建会话成功')
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  }
}

// 删除会话
async function deleteConversation(conversationId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await conversationAPI.deleteConversation(conversationId.toString())
    if (response.success) {
      conversations.value = conversations.value.filter(c => c.id !== conversationId)

      if (currentConversationId.value === conversationId) {
        currentConversationId.value = null
        messages.value = []

        if (conversations.value.length > 0) {
          selectConversation(conversations.value[0].id)
        }
      }

      loadStats() // 刷新统计信息
      ElMessage.success('删除成功')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 发送消息
async function sendMessage() {
  if (!question.value.trim()) {
    return
  }

  const userQuestion = question.value.trim()
  question.value = ''
  isLoading.value = true

  // 如果没有选中会话，先创建一个
  if (!currentConversationId.value) {
    try {
      const response = await conversationAPI.createConversation()
      if (response.success) {
        conversations.value.unshift(response.data)
        currentConversationId.value = response.data.id
      }
    } catch (error) {
      console.error('创建会话失败:', error)
      isLoading.value = false
      return
    }
  }

  // 添加用户消息到界面
  messages.value.push({
    messageType: 'USER',
    content: userQuestion,
    createTime: new Date().toISOString()
  })

  await nextTick()
  scrollToBottom()

  try {
    // 添加一个临时的AI消息用于流式显示
    const aiMessageIndex = messages.value.length
    messages.value.push({
      messageType: 'ASSISTANT',
      content: '',
      sourceCount: 0,
      createTime: new Date().toISOString(),
      isStreaming: true
    })

    await nextTick()
    scrollToBottom()

    // 使用会话流式接口
    const response = await conversationAPI.askConversationStreaming(
      currentConversationId.value!.toString(),
      userQuestion,
      searchType.value,
      5,
      0.7,
      true,
      10,
      // onToken - 流式接收token
      (token: string) => {
        messages.value[aiMessageIndex].content += token
        scrollToBottom()
      },
      // onComplete - 流式完成
      () => {
        messages.value[aiMessageIndex].isStreaming = false
        // 更新会话信息
        const conv = conversations.value.find(c => c.id === currentConversationId.value)
        if (conv) {
          conv.messageCount = (conv.messageCount || 0) + 2
          conv.lastMessageTime = new Date().toISOString()
        }
        // 重新加载统计信息
        loadStats()
      },
      // onError - 错误处理
      (error: any) => {
        console.error('流式提问失败:', error)
        messages.value[aiMessageIndex].content = '回答生成失败，请重试'
        messages.value[aiMessageIndex].isStreaming = false
        ElMessage.error('回答生成失败，请重试')
        // 即使出错也要刷新统计信息
        loadStats()
      }
    )

  } catch (error) {
    console.error('提问失败:', error)
    ElMessage.error('提问失败，请重试')
    
    // 移除最后两条消息（用户消息和AI消息）
    messages.value.pop()
    messages.value.pop()
    
    // 即使出错也要刷新统计信息
    loadStats()
  } finally {
    isLoading.value = false
  }
}

// 查看历史详情
function viewHistoryDetail(msg: any) {
  if (msg.messageType === 'USER') {
    selectedMessage.value = msg
    showDetailDialog.value = true
  }
}

// 获取下一条助手消息
function getNextAssistantMessage(userMsg: any) {
  const index = messages.value.findIndex(m => m.id === userMsg.id)
  if (index >= 0 && index < messages.value.length - 1) {
    return messages.value[index + 1]
  }
  return null
}

// 渲染Markdown
function renderMarkdown(content: string): string {
  if (!content) return ''
  return marked.parse(content) as string
}

// 格式化时间
function formatTime(time: string | Date): string {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleString('zh-CN')
}

// 滚动到底部
function scrollToBottom() {
  if (messageContainer.value) {
    const scrollbar = messageContainer.value.querySelector('.el-scrollbar__wrap')
    if (scrollbar) {
      scrollbar.scrollTop = scrollbar.scrollHeight
    }
  }
}
</script>

<style scoped lang="scss">
.conversation-view {
  display: flex;
  height: calc(100vh - 120px);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

// 左侧会话列表
.conversation-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }

  .stats-card {
    margin: 16px;
    padding: 16px;
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: space-around;
    gap: 12px;

    .stat-item {
      flex: 1;
      text-align: center;
      color: #303133;

      .stat-label {
        display: block;
        font-size: 12px;
        color: #606266;
        margin-bottom: 8px;
      }

      .stat-value {
        display: block;
        font-size: 24px;
        font-weight: bold;
        color: #000000;
      }
    }
  }

  .conversation-list {
    flex: 1;
    overflow: hidden;
  }

  .conversation-item {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    justify-content: space-between;
    align-items: center;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #e6f7ff;
      border-left: 3px solid #409eff;
    }

    .conversation-info {
      flex: 1;
      overflow: hidden;

      .conversation-name {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .conversation-meta {
        font-size: 12px;
        color: #909399;
        display: flex;
        justify-content: space-between;

        span {
          margin-right: 8px;
        }
      }
    }

    .el-button {
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover .el-button {
      opacity: 1;
    }
  }

  .load-more {
    padding: 12px;
    text-align: center;
  }
}

// 右侧对话区域
.conversation-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.conversation-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
    color: #303133;

    .el-icon {
      margin-right: 8px;
      font-size: 20px;
      color: #409eff;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
  }
}

.message-container {
  flex: 1;
  padding: 24px;
  overflow: hidden;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.3s;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      background: #e6f7ff;
      margin-left: 0;
      margin-right: 12px;
    }
  }

  &.assistant .message-content {
    background: #f5f7fa;
  }

  .message-avatar {
    flex-shrink: 0;
  }

  .message-content {
    max-width: 70%;
    padding: 12px 16px;
    border-radius: 8px;
    margin-left: 12px;

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .message-sender {
        font-size: 13px;
        font-weight: 500;
        color: #303133;
      }

      .message-time {
        font-size: 12px;
        color: #909399;
        margin-left: 12px;
      }
    }

    .message-text {
      font-size: 14px;
      line-height: 1.6;
      color: #606266;
      word-wrap: break-word;

      :deep(pre) {
        background: #f4f4f5;
        padding: 12px;
        border-radius: 4px;
        overflow-x: auto;
      }

      :deep(code) {
        background: #f4f4f5;
        padding: 2px 6px;
        border-radius: 3px;
        font-size: 13px;
      }

      .streaming-cursor {
        color: #409eff;
        font-weight: bold;
        animation: blink 1s infinite;
      }
    }

    .message-sources {
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px solid #e4e7ed;
      font-size: 12px;
      color: #909399;
      display: flex;
      align-items: center;

      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

// 加载动画
.loading-dots {
  display: flex;
  gap: 4px;

  span {
    width: 8px;
    height: 8px;
    background: #909399;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }

    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 输入区域
.input-container {
  border-top: 1px solid #e4e7ed;
  padding: 16px 24px;

  .input-toolbar {
    margin-bottom: 12px;
  }

  .input-area {
    display: flex;
    gap: 12px;

    .el-textarea {
      flex: 1;
    }

    .el-button {
      align-self: flex-end;
    }
  }
}

// 历史记录
.history-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;

  &:hover {
    background: #f5f7fa;
  }

  .history-question {
    font-size: 14px;
    color: #303133;
    margin-bottom: 4px;
    display: flex;
    align-items: center;

    .el-icon {
      margin-right: 6px;
      color: #409eff;
    }
  }

  .history-time {
    font-size: 12px;
    color: #909399;
  }
}

.detail-content {
  .detail-section {
    margin-bottom: 24px;

    h4 {
      font-size: 14px;
      color: #303133;
      margin-bottom: 12px;
    }

    p {
      font-size: 14px;
      line-height: 1.6;
      color: #606266;
    }
  }
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}
</style>
