import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
// 注意：这里移除了router的导入，避免循环依赖

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api', // 代理配置会将/api转发到后端
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加链路追踪ID
    const traceId = generateTraceId()
    config.headers['X-Trace-Id'] = traceId
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
apiClient.interceptors.response.use(
  async (response: AxiosResponse) => {
    // 检查是否有新的Access Token在响应头中
    const newAccessToken = response.headers['x-new-access-token']
    if (newAccessToken) {
      localStorage.setItem('token', newAccessToken)
    }
    
    // 检查是否需要刷新Token
    const tokenRefreshNeeded = response.headers['x-token-refresh-needed']
    if (tokenRefreshNeeded) {
      console.log('检测到Token即将过期，建议刷新')
      try {
        // 尝试刷新Token
        await authAPI.refreshToken()
        console.log('Token刷新成功')
      } catch (error) {
        console.error('Token刷新失败:', error)
      }
    }
    
    // 直接返回后端的统一响应格式
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // 401错误，清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      // 注意：不在这里直接跳转，由组件或路由守卫处理跳转
      console.log('401错误，Token已清除')
    }
    
    return Promise.reject(error)
  },
)

// 生成链路追踪ID
function generateTraceId(): string {
  return Math.random().toString(36).substring(2, 16) + Date.now().toString(36)
}

// 统一响应格式接口 - 适配后端R类
export interface ApiResponse<T = any> {
  success: boolean
  code: number
  data: T
  timestamp: string
  askResponse?: any // 扩展字段用于创建会话时的自动问答响应
}

// 认证相关API
class AuthAPI {
  // 用户登录
  async login(username: string, password: string): Promise<ApiResponse<any>> {
    try {
      console.log('发送登录请求:', { username, password })
      const response: any = await apiClient.post<ApiResponse<any>>('/auth/login', { username, password })
      console.log('登录响应:', response)
      if (response.success && response.data?.token) {
        // 保存tokens
        localStorage.setItem('token', response.data.token)
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken)
        }
        
        // 保存用户信息
        if (response.data.user) {
          localStorage.setItem('userInfo', JSON.stringify(response.data.user))
        }
      }
      return response
    } catch (error: any) {
      console.error('登录错误:', error)
      throw error
    }
  }

  // 用户注册
  async register(username: string, password: string, email: string): Promise<ApiResponse<any>> {
    try {
      console.log('发送注册请求:', { username, password, email })
      const response: any = await apiClient.post<ApiResponse<any>>('/auth/register', { username, password, email })
      console.log('注册响应:', response)
      if (response.success && response.data?.token) {
        // 保存tokens
        localStorage.setItem('token', response.data.token)
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken)
        }
        
        // 保存用户信息
        if (response.data.user) {
          localStorage.setItem('userInfo', JSON.stringify(response.data.user))
        }
      }
      return response
    } catch (error: any) {
      console.error('注册错误:', error)
      throw error
    }
  }

  // 获取用户信息
  async getProfile(): Promise<ApiResponse<any>> {
    try {
      console.log('发送获取用户信息请求')
      const response: any = await apiClient.get<ApiResponse<any>>('/auth/profile')
      console.log('获取用户信息响应:', response)
      return response
    } catch (error: any) {
      console.error('获取用户信息错误:', error)
      throw error
    }
  }

  // 修改密码
  async changePassword(oldPassword: string, newPassword: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.put<ApiResponse<any>>('/auth/change-password', { oldPassword, newPassword })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 刷新Token
  async refreshToken(): Promise<ApiResponse<any>> {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      throw new Error('没有刷新令牌')
    }
    
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/auth/refresh', { refreshToken })
      
      // 更新tokens
      if (response.success && response.data?.token) {
        localStorage.setItem('token', response.data.token)
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken)
        }
      }
      
      return response
    } catch (error: any) {
      // 如果刷新失败，清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      throw error
    }
  }

  // 用户登出
  async logout(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/auth/logout')
      return response
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    }
  }

  // 获取本地存储的用户信息
  getLocalUserInfo(): any {
    const userInfo = localStorage.getItem('userInfo')
    return userInfo ? JSON.parse(userInfo) : null
  }

  // 保存用户信息到本地存储
  setLocalUserInfo(userInfo: any): void {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  }

  // 清除本地用户信息
  clearLocalUserInfo(): void {
    localStorage.removeItem('userInfo')
  }
}

// 文档管理API
class DocumentAPI {
  // 获取文档列表（分页）
  async getDocuments(params: any = {}): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/documents', { params })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取文档详情
  async getDocument(documentId: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>(`/documents/${documentId}`)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 上传文档
  async uploadDocument(formData: FormData): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 异步上传文档（推荐使用）
  async uploadDocumentAsync(formData: FormData): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/documents/upload/async', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 下载文档
  async downloadDocument(documentId: string): Promise<Blob> {
    try {
      // 直接使用axios实例，绕过响应拦截器
      const token = localStorage.getItem('token')
      const response = await axios.get(`/api/documents/${documentId}/download`, {
        responseType: 'blob',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
        },
      })
      
      // 检查响应是否为错误（JSON格式）
      const contentType = response.headers?.['content-type']
      if (contentType && contentType.includes('application/json')) {
        // 如果是JSON响应，说明是错误信息
        const errorText = await response.data.text()
        const errorData = JSON.parse(errorText)
        throw new Error(errorData.data || errorData.message || '下载失败')
      }
      
      return response.data
    } catch (error: any) {
      // 如果已经是解析过的错误，直接抛出
      if (error.message) {
        throw error
      }
      // 否则包装成标准错误
      throw new Error(error.message || '网络错误')
    }
  }

  // 删除文档
  async deleteDocument(documentId: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.delete<ApiResponse<any>>(`/documents/${documentId}`)
      return response
    } catch (error: any) {
      throw error
    }
  }
}

// 搜索API
class SearchAPI {
  // 知识库搜索
  async search(request: any): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/search', request)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 简单搜索
  async simpleSearch(params: any): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/search/simple', { params })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // AI问答
  async ask(question: string, searchType: string = 'SEMANTIC', maxResults: number = 10, minScore: number = 0.7, contextWindow: number = 3): Promise<ApiResponse<any>> {
    try {
      const data = {
        'question': question,
        'searchType': searchType,
        'maxResults': maxResults,
        'minScore': minScore,
        'contextWindow': contextWindow
      }
      
      const response: any = await apiClient.post<ApiResponse<any>>('/search/ask', data)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 流式AI问答
  async askStreaming(question: string, searchType: string = 'SEMANTIC', maxResults: number = 10, minScore: number = 0.7, onToken?: (token: string) => void, onComplete?: () => void, onError?: (error: any) => void): Promise<any> {
    try {
      const requestData = {
        question,
        searchType,
        maxResults,
        minScore
      }

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

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let fullAnswer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            fullAnswer += data
            if (onToken) onToken(data)
          } else if (line.startsWith('event:') && line.substring(6) === 'complete') {
            if (onComplete) onComplete()
          }
        }
      }

      return { answer: fullAnswer }
    } catch (error: any) {
      if (onError) onError(error)
      throw error
    }
  }
}

// 向量数据API
class VectorDataAPI {
  // 获取向量数据（分页）
  async getVectorData(page: number = 0, size: number = 10, sortBy: string = 'createdTime', sortDir: string = 'desc'): Promise<ApiResponse<any>> {
    try {
      const params = { page, size, sortBy, sortDir }
      const response: any = await apiClient.get<ApiResponse<any>>('/vector-data', { params })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取向量统计信息
  async getVectorStats(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/vector-data/stats')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 删除向量数据
  async deleteVectorData(vectorId: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.delete<ApiResponse<any>>(`/vector-data/${vectorId}`)
      return response
    } catch (error: any) {
      throw error
    }
  }
}

// 对话管理API
class ConversationAPI {
  // 获取会话列表
  async getConversations(page: number = 0, size: number = 20): Promise<ApiResponse<any>> {
    try {
      const params = { page, size }
      const response: any = await apiClient.get<ApiResponse<any>>('/conversation/list', { params })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 创建新会话
  async createConversation(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.post<ApiResponse<any>>('/conversation/create', {})
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取会话详情
  async getConversation(conversationId: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>(`/conversation/${conversationId}`)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // AI问答
  async ask(conversationId: string, question: string, searchType: string = 'SEMANTIC', maxResults: number = 10, minScore: number = 0.7, enableContext: boolean = true, contextMessageCount: number = 10): Promise<ApiResponse<any>> {
    try {
      const data = {
        conversationId,
        question,
        searchType,
        maxResults,
        minScore,
        enableContext,
        contextMessageCount
      }
      const response: any = await apiClient.post<ApiResponse<any>>('/conversation/ask', data)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 流式AI问答
  async askStreaming(question: string, searchType: string = 'SEMANTIC', maxResults: number = 10, minScore: number = 0.7, onToken?: (token: string) => void, onComplete?: () => void, onError?: (error: any) => void): Promise<any> {
    try {
      const requestData = {
        question,
        searchType,
        maxResults,
        minScore
      }

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

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let fullAnswer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            fullAnswer += data
            if (onToken) onToken(data)
          } else if (line.startsWith('event:') && line.substring(6) === 'complete') {
            if (onComplete) onComplete()
          }
        }
      }

      return { answer: fullAnswer }
    } catch (error: any) {
      if (onError) onError(error)
      throw error
    }
  }

  // 获取会话消息历史
  async getMessages(conversationId: string, page: number = 0, size: number = 20): Promise<ApiResponse<any>> {
    try {
      const params = { page, size }
      const response: any = await apiClient.get<ApiResponse<any>>(`/conversation/${conversationId}/messages`, { params })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 删除会话
  async deleteConversation(conversationId: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.delete<ApiResponse<any>>(`/conversation/${conversationId}`)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 更新会话标题
  async updateConversationTitle(conversationId: string, title: string): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.put<ApiResponse<any>>(`/conversation/${conversationId}`, { title })
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取会话统计信息
  async getStats(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/conversation/stats')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 会话流式AI问答
  async askConversationStreaming(conversationId: string, question: string, searchType: string = 'SEMANTIC', maxResults: number = 10, minScore: number = 0.7, enableContext: boolean = true, contextMessageCount: number = 10, onToken?: (token: string) => void, onComplete?: () => void, onError?: (error: any) => void): Promise<any> {
    try {
      const requestData = {
        conversationId: conversationId ? parseInt(conversationId) : null,
        question,
        searchType,
        maxResults,
        minScore,
        enableContext,
        contextMessageCount
      }

      const response = await fetch('/api/conversation/ask/streaming', {
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

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let fullAnswer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            fullAnswer += data
            if (onToken) onToken(data)
          } else if (line.startsWith('event:') && line.substring(6) === 'complete') {
            if (onComplete) onComplete()
          }
        }
      }

      return { answer: fullAnswer }
    } catch (error: any) {
      if (onError) onError(error)
      throw error
    }
  }
}

// 系统监控API
class SystemAPI {
  // 获取系统仪表板信息
  async getDashboard(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/system/dashboard')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取系统状态
  async getSystemStatus(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/system/status')
      return response
    } catch (error: any) {
      throw error
    }
  }
}

// 健康检查和监控API
class HealthAPI {
  // 获取健康状态
  async getHealth(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/actuator/health')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取系统信息
  async getInfo(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/actuator/info')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取监控指标
  async getMetrics(metricName?: string): Promise<ApiResponse<any>> {
    try {
      const url = metricName ? `/actuator/metrics/${metricName}` : '/actuator/metrics'
      const response: any = await apiClient.get<ApiResponse<any>>(url)
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取Prometheus指标
  async getPrometheusMetrics(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/actuator/prometheus')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取环境信息
  async getEnvironment(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/actuator/env')
      return response
    } catch (error: any) {
      throw error
    }
  }

  // 获取配置属性
  async getConfigProperties(): Promise<ApiResponse<any>> {
    try {
      const response: any = await apiClient.get<ApiResponse<any>>('/actuator/configprops')
      return response
    } catch (error: any) {
      throw error
    }
  }
}

// 创建API实例
export const authAPI = new AuthAPI()
export const documentAPI = new DocumentAPI()
export const searchAPI = new SearchAPI()
export const vectorDataAPI = new VectorDataAPI()
export const conversationAPI = new ConversationAPI()
export const systemAPI = new SystemAPI()
export const healthAPI = new HealthAPI()

// 默认导出
export default {
  auth: authAPI,
  document: documentAPI,
  search: searchAPI,
  vectorData: vectorDataAPI,
  conversation: conversationAPI,
  system: systemAPI,
  health: healthAPI,
  client: apiClient,
}