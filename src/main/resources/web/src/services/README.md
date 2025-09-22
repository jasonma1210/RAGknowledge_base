# API服务层使用说明

## 概述

API服务层位于`/src/services/api.ts`，负责统一管理所有后端API接口调用。该服务层基于Axios实现，提供了以下特性：

- 统一的请求/响应拦截器
- 自动的JWT Token管理
- 链路追踪支持
- 错误处理机制
- 类型安全的API调用

## 模块说明

### AuthAPI - 认证相关接口

```typescript
import { authAPI } from '@/services/api'

// 用户登录
const response = await authAPI.login(username, password)

// 用户注册
const response = await authAPI.register(username, password, email)

// 获取用户信息
const response = await authAPI.getProfile()

// 修改密码
const response = await authAPI.changePassword(oldPassword, newPassword)

// 刷新Token
const response = await authAPI.refreshToken()

// 用户登出
const response = await authAPI.logout()
```

### DocumentAPI - 文档管理接口

```typescript
import { documentAPI } from '@/services/api'

// 获取文档列表（分页）
const response = await documentAPI.getDocuments({
  page: 0,
  size: 10,
  keyword: '搜索关键词'
})

// 获取文档详情
const response = await documentAPI.getDocument(documentId)

// 上传文档
const formData = new FormData()
formData.append('file', file)
formData.append('title', title)
formData.append('description', description)
const response = await documentAPI.uploadDocument(formData)

// 下载文档
const blob = await documentAPI.downloadDocument(documentId)

// 删除文档
const response = await documentAPI.deleteDocument(documentId)
```

### SearchAPI - 搜索接口

```typescript
import { searchAPI } from '@/services/api'

// 知识库搜索
const response = await searchAPI.search({
  query: '搜索内容',
  searchType: 'SEMANTIC',
  maxResults: 10,
  minScore: 0.7
})

// 简单搜索
const response = await searchAPI.simpleSearch({
  query: '搜索内容',
  type: 'SEMANTIC',
  limit: 10,
  minScore: 0.7
})

// AI问答
const response = await searchAPI.ask(
  '问题内容',
  'SEMANTIC',
  10,
  0.7,
  3
)
```

### VectorDataAPI - 向量数据接口

```typescript
import { vectorDataAPI } from '@/services/api'

// 获取向量数据（分页）
const response = await vectorDataAPI.getVectorData(0, 10, 'createdTime', 'desc')

// 获取向量统计信息
const response = await vectorDataAPI.getVectorStats()
```

### SystemAPI - 系统监控接口

```typescript
import { systemAPI } from '@/services/api'

// 获取系统仪表板信息
const response = await systemAPI.getDashboard()

// 获取系统状态
const response = await systemAPI.getSystemStatus()
```

### HealthAPI - 健康检查接口

```typescript
import { healthAPI } from '@/services/api'

// 获取健康状态
const response = await healthAPI.getHealth()

// 获取系统信息
const response = await healthAPI.getInfo()

// 获取监控指标
const response = await healthAPI.getMetrics(metricName)

// 获取Prometheus指标
const response = await healthAPI.getPrometheusMetrics()

// 获取环境信息
const response = await healthAPI.getEnvironment()

// 获取配置属性
const response = await healthAPI.getConfigProperties()
```

## 拦截器机制

### 请求拦截器

自动添加以下请求头：

1. `Authorization: Bearer {token}` - JWT认证Token
2. `X-Trace-Id: {traceId}` - 链路追踪ID

### 响应拦截器

自动处理以下情况：

1. 401错误 - 自动清除本地存储并跳转到登录页
2. 统一响应格式处理

## 错误处理

所有API调用都可能抛出异常，建议使用try-catch包装：

```typescript
try {
  const response = await authAPI.login(username, password)
  if (response.success) {
    // 处理成功响应
    console.log('登录成功')
  } else {
    // 处理业务失败
    console.error('登录失败:', response.message)
  }
} catch (error) {
  // 处理网络错误或其他异常
  console.error('网络错误:', error)
}
```

## 类型定义

### ApiResponse<T>

统一的响应格式接口：

```typescript
interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  traceId?: string
  error?: string
  code?: number
  status?: number
  timestamp?: string
  path?: string
}
```

## 最佳实践

### 1. 导入方式

```typescript
// 方式1: 命名导出（推荐）
import { authAPI, documentAPI, searchAPI } from '@/services/api'

// 方式2: 默认导出
import API from '@/services/api'
// 使用: API.auth.login(...)
```

### 2. 错误处理

```typescript
try {
  const response = await authAPI.login(username, password)
  if (response.success) {
    // 处理成功逻辑
  } else {
    // 处理业务错误
    ElMessage.error(response.message || '操作失败')
  }
} catch (error: any) {
  // 处理网络错误
  ElMessage.error('网络错误: ' + (error.message || '请检查网络连接'))
}
```

### 3. Token管理

Token会自动保存到localStorage，无需手动管理：

- `token` - 访问令牌
- `refreshToken` - 刷新令牌
- `userInfo` - 用户信息

### 4. 链路追踪

每次请求都会自动生成唯一的`X-Trace-Id`，便于问题排查。

## 注意事项

1. 所有API调用都是异步的，需要使用`await`或`.then()`
2. 响应数据通过`response.data`获取
3. 业务成功状态通过`response.success`判断
4. 401错误会自动处理，无需额外判断
5. 文件上传需要使用FormData格式
6. 文件下载返回Blob对象，需要手动处理下载