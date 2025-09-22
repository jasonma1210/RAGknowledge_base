# RAG知识库系统API接口文档 v1.2

## 📋 文档信息

**版本**：v1.2.2  
**更新时间**：2025-09-21  
**API基础路径**：`http://localhost:8080`  
**认证方式**：JWT Bearer Token  

---

## 📖 版本更新说明

### v1.2.2 主要更新
- ✅ 统一错误码体系，深度绑定ErrorCode枚举与API响应
- ✅ 补充完整的业务错误码定义
- ✅ 更新API文档中的错误码说明与后端保持一致

### v1.2.1 主要更新
- ✅ 加强用户数据脱敏机制，防止敏感信息泄露
- ✅ 统一所有后端API响应格式，使用R对象封装
- ✅ 所有接口响应均包含success、code、data字段
- ✅ 成功响应code为200，失败响应code为500
- ✅ 增强错误处理机制，提供更详细的错误信息
- ✅ 更新所有API示例以反映新的统一响应格式

### v1.2.0 主要更新
- ✅ 统一所有后端API响应格式，使用R对象封装
- ✅ 所有接口响应均包含success、code、data字段
- ✅ 成功响应code为200，失败响应code为500
- ✅ 增强错误处理机制，提供更详细的错误信息
- ✅ 更新所有API示例以反映新的统一响应格式

### v1.1.0 (2025-09-19)

**新增功能**:
- ✅ JWT认证机制替代简单Token
- ✅ 向量数据管理接口 (`/vector-data`)
- ✅ 系统监控接口 (`/system/dashboard`)
- ✅ 文档下载功能 (`/documents/{id}/download`)
- ✅ 增强的Actuator监控端点
- ✅ 分布式链路追踪支持
- ✅ XSS防护和敏感信息脱敏

---

## 1. 统一响应格式

所有API接口均使用统一的响应格式：

### 成功响应格式
``json
{
  "success": true,
  "code": 200,
  "data": {},  // 实际数据对象
  "timestamp": "2025-09-21T10:00:00"
}
```

### 失败响应格式
``json
{
  "success": false,
  "code": 500,
  "data": "错误信息",  // 错误信息
  "timestamp": "2025-09-21T10:00:00"
}
```

---

## 2. 认证接口

### 2.1 用户注册

**接口地址**: `POST /auth/register`

**请求头**:
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填，3-50字符，仅支持字母数字下划线）
  "password": "string",  // 密码（必填，6-20字符）
  "email": "string"      // 邮箱（可选，需符合邮箱格式）
}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",  // JWT访问令牌
    "refreshToken": "string",           // 刷新令牌  
    "user": {                          // 用户信息（已脱敏）
      "id": 123,                       // 用户ID
      "username": "string",            // 用户名
      "email": "s****r@example.com",   // 脱敏邮箱
      "level": 0,                      // 用户等级（0:普通用户 1:进阶用户）
      "levelDescription": "普通用户",    // 用户等级描述
      "storageQuota": 5368709120,      // 存储配额（字节）
      "usedStorage": 0,                // 已使用存储空间（字节）
      "storageUsagePercentage": 0.0,   // 存储使用率（百分比）
      "formattedStorageQuota": "5.00 GB",  // 格式化存储配额
      "formattedUsedStorage": "0 B",   // 格式化已使用存储
      "formattedRemainingStorage": "5.00 GB",  // 格式化剩余存储
      "lastLoginTime": "2025-09-21T10:00:00", // 最后登录时间
      "gmtCreate": "2025-09-21T10:00:00"     // 创建时间
    }
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**用户信息脱敏说明**:
- 密码等敏感信息不会通过API接口返回
- 邮箱地址会进行脱敏处理（保留首尾字符，中间用****替换）
- 返回信息中不包含isDeleted等内部字段
**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "用户名已存在",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 注册成功
- 400: 请求参数错误或注册失败
- 409: 用户名已存在

### 2.2 用户登录

**接口地址**: `POST /auth/login`

**请求头**:
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填）
  "password": "string"   // 密码（必填）
}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",  // JWT访问令牌（24小时有效）
    "refreshToken": "string",           // 刷新令牌（7天有效）
    "user": {                          // 用户信息（已脱敏）
      "id": 123,
      "username": "string",
      "email": "s****r@example.com",   // 脱敏邮箱
      "level": 0,
      "levelDescription": "普通用户",    // 用户等级描述
      "storageQuota": 5368709120,
      "usedStorage": 1048576,
      "storageUsagePercentage": 1.95,   // 存储使用率（百分比）
      "formattedStorageQuota": "5.00 GB",  // 格式化存储配额
      "formattedUsedStorage": "1.00 MB",   // 格式化已使用存储
      "formattedRemainingStorage": "5.00 GB",  // 格式化剩余存储
      "lastLoginTime": "2025-09-21T10:00:00",
      "gmtCreate": "2025-09-21T09:00:00"
    }
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**用户信息脱敏说明**:
- 密码等敏感信息不会通过API接口返回
- 邮箱地址会进行脱敏处理（保留首尾字符，中间用****替换）
- 返回信息中不包含gmtModified和isDeleted等内部字段
**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "用户名或密码错误",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 登录成功
- 400: 用户名或密码错误
- 401: 认证失败

### 2.3 获取用户信息

**接口地址**: `GET /auth/profile`

**请求头**:
```
Authorization: Bearer {access_token}
X-Trace-Id: {trace_id}  // 可选，链路追踪ID
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 123,
    "username": "string",
    "email": "s****r@example.com",   // 脱敏邮箱
    "level": 0,
    "levelDescription": "普通用户",    // 用户等级描述
    "storageQuota": 5368709120,
    "usedStorage": 1048576,
    "storageUsagePercentage": 1.95,   // 存储使用率（百分比）
    "formattedStorageQuota": "5.00 GB",  // 格式化存储配额
    "formattedUsedStorage": "1.00 MB",   // 格式化已使用存储
    "formattedRemainingStorage": "5.00 GB",  // 格式化剩余存储
    "lastLoginTime": "2025-09-21T10:00:00",
    "gmtCreate": "2025-09-21T09:00:00"
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**用户信息脱敏说明**:
- 密码等敏感信息不会通过API接口返回
- 邮箱地址会进行脱敏处理（保留首尾字符，中间用****替换）
- 返回信息中不包含gmtModified和isDeleted等内部字段
**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "Token已过期",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 获取成功
- 401: 未授权访问或Token过期

### 2.4 刷新Token

**接口地址**: `POST /auth/refresh`

**请求头**:
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "refreshToken": "string"  // 刷新令牌
}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",  // 新的访问令牌
    "refreshToken": "string"            // 新的刷新令牌
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "刷新令牌无效",
  "timestamp": "2025-09-21T10:00:00"
}
```

### 2.5 用户登出

**接口地址**: `POST /auth/logout`

**请求头**:
```
Authorization: Bearer {access_token}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "登出成功",
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "登出失败",
  "timestamp": "2025-09-21T10:00:00"
}
```

---

## 3. 文档管理接口

### 3.1 上传文档

**接口地址**: `POST /documents/upload`

**请求头**:
```
Content-Type: multipart/form-data
Authorization: Bearer {access_token}
X-Trace-Id: {trace_id}  // 可选，链路追踪ID
```

**请求参数**:
```
file: 文件（必填，支持PDF、DOCX、TXT、MD、EPUB格式）
title: 文档标题（可选，最大100字符）
description: 文档描述（可选，最大500字符）
tags: 标签（可选，逗号分隔，最大10个标签）
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": "doc_123456789",      // 文档ID
    "title": "string",          // 文档标题
    "description": "string",    // 文档描述
    "fileName": "example.pdf",  // 文件名
    "fileType": "pdf",          // 文件类型
    "fileSize": 1048576,        // 文件大小（字节）
    "tags": "标签1,标签2",       // 标签
    "uploadTime": "2025-09-21T10:00:00", // 上传时间
    "chunkCount": 15,           // 分块数量
    "vectorCount": 15,          // 向量数量
    "processingStatus": "COMPLETED"  // 处理状态
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "文件上传失败，请检查文件格式和大小",
  "timestamp": "2025-09-21T10:00:00"
}
```

**处理状态说明**:
- `PENDING`: 等待处理
- `PROCESSING`: 正在处理
- `COMPLETED`: 处理完成
- `FAILED`: 处理失败

**状态码**:
- 200: 上传成功
- 400: 请求参数错误或上传失败
- 401: 未授权访问
- 413: 文件过大
- 415: 不支持的文件格式

### 3.2 获取所有文档

**接口地址**: `GET /documents`

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
```
page: 页码（可选，默认0）
size: 每页大小（可选，默认10，最大100）
sortBy: 排序字段（可选，默认uploadTime）
sortDir: 排序方向（可选，默认desc，可选值：asc、desc）
keyword: 搜索关键词（可选，搜索标题和描述）
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "data": [                // 文档列表
      {
        "id": "doc_123456789",
        "title": "string",
        "description": "string",
        "fileName": "example.pdf",
        "fileType": "pdf",
        "fileSize": 1048576,
        "tags": "标签1,标签2",
        "uploadTime": "2025-09-21T10:00:00",
        "chunkCount": 15,
        "vectorCount": 15,
        "processingStatus": "COMPLETED"
      }
    ],
    "page": 0,               // 当前页码
    "size": 10,              // 每页大小
    "total": 50,             // 总元素数
    "totalPages": 5          // 总页数
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "获取文档列表失败",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 获取成功
- 401: 未授权访问

### 3.3 获取文档详情

**接口地址**: `GET /documents/{documentId}`

**请求头**:
```
Authorization: Bearer {access_token}
```

**路径参数**:
```
documentId: 文档ID
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": "doc_123456789",
    "title": "string",
    "description": "string",
    "fileName": "example.pdf",
    "fileType": "pdf",
    "fileSize": 1048576,
    "tags": "标签1,标签2",
    "uploadTime": "2025-09-21T10:00:00",
    "chunkCount": 15,
    "vectorCount": 15,
    "processingStatus": "COMPLETED",
    "downloadUrl": "/documents/doc_123456789/download",
    "previewAvailable": true
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "文档不存在",
  "timestamp": "2025-09-21T10:00:00"
}
```

### 3.4 下载文档 🆕

**接口地址**: `GET /documents/{documentId}/download`

**请求头**:
```
Authorization: Bearer {access_token}
```

**路径参数**:
```
documentId: 文档ID
```

**响应**:
- **成功**: 返回文件二进制流
- **响应头**:
  ```
  Content-Disposition: attachment; filename="example.pdf"
  Content-Type: application/octet-stream
  Content-Length: 1048576
  ```

**状态码**:
- 200: 下载成功
- 401: 未授权访问
- 404: 文档不存在
- 410: 文件已被删除

### 3.5 删除文档

**接口地址**: `DELETE /documents/{documentId}`

**请求头**:
```
Authorization: Bearer {access_token}
```

**路径参数**:
```
documentId: 文档ID
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": "文档删除成功",
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "文档正在处理中，无法删除",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 删除成功
- 401: 未授权访问
- 404: 文档不存在
- 409: 文档正在处理中，无法删除

---

## 4. 搜索接口

### 4.1 知识库搜索

**接口地址**: `POST /search`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {access_token}
X-Trace-Id: {trace_id}  // 可选，链路追踪ID
```

**请求参数**:
```json
{
  "query": "string",           // 搜索内容（必填，1-500字符）
  "searchType": "SEMANTIC",    // 搜索类型（可选，默认SEMANTIC）
  "maxResults": 10,            // 最大结果数（可选，默认10，最大50）
  "minScore": 0.7,             // 最小相似度分数（可选，默认0.7，范围0-1）
  "documentIds": ["doc_123"],  // 指定文档ID列表（可选）
  "tags": ["标签1", "标签2"]    // 指定标签过滤（可选）
}
```

**搜索类型说明**:
- `SEMANTIC`: 语义搜索（基于向量相似度）
- `KEYWORD`: 关键词搜索（基于文本匹配）
- `HYBRID`: 混合搜索（结合语义和关键词）

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "data": [
      {
        "fileRecordId": "doc_123456789",  // 文件记录ID
        "title": "string",               // 文档标题
        "content": "string",             // 匹配内容片段
        "score": 0.85,                   // 相似度分数
        "source": "example.pdf",         // 来源文件名
        "position": 123,                 // 在文档中的位置
        "chunkId": "chunk_456",          // 分块ID
        "tags": "标签1,标签2",            // 文档标签
        "uploadTime": "2025-09-21T10:00:00"
      }
    ],
    "page": 0,                         // 当前页码
    "size": 10,                        // 每页大小
    "total": 25,                       // 总元素数
    "totalPages": 3                    // 总页数
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "搜索服务异常",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 搜索成功
- 400: 请求参数错误或搜索失败
- 401: 未授权访问

### 4.2 简单搜索

**接口地址**: `GET /search/simple`

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
```
query: 搜索内容（必填）
type: 搜索类型（可选，默认SEMANTIC）
limit: 最大结果数（可选，默认10）
minScore: 最小相似度（可选，默认0.7）
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "data": [
      {
        "fileRecordId": "doc_123456789",
        "title": "string",
        "content": "string",
        "score": 0.85,
        "source": "example.pdf",
        "position": 123
      }
    ],
    "page": 0,
    "size": 10,
    "total": 15,
    "totalPages": 2
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "搜索参数错误",
  "timestamp": "2025-09-21T10:00:00"
}
```

---

## 5. 问答接口

### 5.1 智能问答

**接口地址**: `POST /search/ask`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {access_token}
X-Trace-Id: {trace_id}  // 可选，链路追踪ID
```

**请求参数**:
```json
{
  "question": "string",           // 问题内容（必填，1-500字符）
  "searchType": "SEMANTIC",       // 搜索类型（可选，默认SEMANTIC）
  "maxResults": 10,               // 最大结果数（可选，默认10）
  "minScore": 0.7,                // 最小相似度分数（可选，默认0.7）
  "contextWindow": 3              // 上下文窗口大小（可选，默认3）
}
```

**成功响应**:
``json
{
  "success": true,
  "code": 200,
  "data": {
    "question": "string",           // 问题内容
    "answer": "string",             // AI生成的回答
    "sources": [                    // 来源信息
      {
        "fileRecordId": "doc_123456789",
        "title": "string",
        "content": "string",
        "score": 0.85,
        "source": "example.pdf",
        "position": 123,
        "chunkId": "chunk_456",
        "relevance": "HIGH"         // 相关度等级：HIGH/MEDIUM/LOW
      }
    ],
    "sourceCount": 5,               // 来源数量
    "answerTime": 1245,             // 回答生成时间（毫秒）
    "confidence": 0.92,             // 答案置信度
    "model": "qwen-max-latest",     // 使用的模型
    "traceId": "abc123def456"       // 链路追踪ID
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "AI服务异常",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 问答成功
- 400: 请求参数错误或问答失败
- 401: 未授权访问
- 429: 请求过于频繁

---

## 6. 向量数据管理接口 🆕

### 6.1 获取用户向量数据

**接口地址**: `GET /vector-data`

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
```
page: 页码（可选，默认0）
size: 每页大小（可选，默认10，最大100）
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "data": [
      {
        "id": "vector_123",
        "documentId": "doc_123456789",
        "chunkId": "chunk_456",
        "content": "文档内容片段",
        "vectorDimension": 1024,
        "createdTime": "2025-09-21T10:00:00",
        "score": 0.85
      }
    ],
    "page": 0,
    "size": 10,
    "total": 150,
    "totalPages": 15
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "获取向量数据失败",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 获取成功
- 401: 未授权访问

### 6.2 获取向量统计信息

**接口地址**: `GET /vector-data/stats`

**请求头**:
```
Authorization: Bearer {access_token}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "totalVectors": 1500,           // 总向量数
    "totalDocuments": 45,           // 总文档数
    "avgVectorsPerDocument": 33.3,  // 平均每文档向量数
    "vectorDimension": 1024,        // 向量维度
    "collectionName": "user_123",   // 集合名称
    "storageSize": "15.6MB",        // 存储大小
    "lastUpdated": "2025-09-21T10:00:00"
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "获取向量统计信息失败",
  "timestamp": "2025-09-21T10:00:00"
}
```

---

## 7. 系统监控接口 🆕

### 7.1 系统仪表板

**接口地址**: `GET /system/dashboard`

**请求头**:
```
Authorization: Bearer {access_token}
```

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "systemInfo": {
      "cpuUsage": 45.6,             // CPU使用率(%)
      "memoryUsage": 68.2,          // 内存使用率(%)
      "diskUsage": 35.8,            // 磁盘使用率(%)
      "systemLoad": 1.25,           // 系统负载
      "uptime": "5 days 12:30:45",  // 系统运行时间
      "jvmVersion": "21.0.7",       // JVM版本
      "serverTime": "2025-09-21T10:00:00"
    },
    "userStats": {
      "totalDocuments": 45,         // 用户文档总数
      "totalVectors": 1500,         // 用户向量总数
      "storageUsed": 1048576,       // 已使用存储
      "storageQuota": 5368709120,   // 存储配额
      "storageUsage": 1.95,         // 存储使用率（百分比）
      "lastUpload": "2025-09-21T08:30:00"
    }
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

**失败响应**:
```json
{
  "success": false,
  "code": 500,
  "data": "获取仪表板信息失败",
  "timestamp": "2025-09-21T10:00:00"
}
```

**状态码**:
- 200: 获取成功
- 401: 未授权访问

---

## 8. 监控端点（Actuator）🆕

### 8.1 健康检查

**接口地址**: `GET /actuator/health`

**成功响应**:
```
{
  "success": true,
  "code": 200,
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "database": "MySQL",
          "status": "Connected",
          "validationQuery": "SELECT 1"
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "redis": "Redis Server",
          "status": "Connected",
          "response": "PONG"
        }
      },
      "storage": {
        "status": "UP",
        "details": {
          "storage": "Local File System",
          "status": "Available",
          "freeSpaceGB": "1898.24",
          "usagePercent": "49.00%"
        }
      },
      "diskSpace": {
        "status": "UP",
        "details": {
          "total": 3996329328640,
          "free": 2038221303808,
          "threshold": 10485760
        }
      }
    }
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

### 8.2 监控指标

**接口地址**: `GET /actuator/metrics`

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "names": [
      "document.upload.count",
      "document.delete.count",
      "user.login.count",
      "search.request.count",
      "ai.chat.count",
      "document.processing.time",
      "vector.search.time",
      "users.active.count",
      "storage.usage.bytes",
      "jvm.memory.used",
      "jvm.threads.live",
      "http.server.requests"
    ]
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

### 8.3 具体指标详情

**接口地址**: `GET /actuator/metrics/{metric.name}`

**示例**: `GET /actuator/metrics/document.upload.count`

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "name": "document.upload.count",
    "description": "Total number of documents uploaded",
    "baseUnit": null,
    "measurements": [
      {
        "statistic": "COUNT",
        "value": 156.0
      }
    ],
    "availableTags": [
      {
        "tag": "type",
        "values": ["upload"]
      }
    ]
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

### 8.4 系统信息

**接口地址**: `GET /actuator/info`

**成功响应**:
```json
{
  "success": true,
  "code": 200,
  "data": {
    "application": {
      "name": "RAG Knowledge Base",
      "version": "1.2.0",
      "description": "RAG-based Knowledge Base System",
      "startTime": "2025-09-21T06:55:47.898039"
    },
    "build": {
      "java.version": "21.0.7",
      "java.vendor": "Azul Systems, Inc.",
      "os.name": "Mac OS X",
      "os.version": "26.0"
    },
    "system": {
      "memory": {
        "maxMemoryMB": 12288,
        "totalMemoryMB": 184,
        "usedMemoryMB": 133,
        "freeMemoryMB": 50
      },
      "availableProcessors": 14
    }
  },
  "timestamp": "2025-09-21T10:00:00"
}
```

### 8.5 Prometheus监控端点

**接口地址**: `GET /actuator/prometheus`

**响应**: Prometheus格式的监控指标

---

## 9. 错误码说明

### HTTP状态码

| 状态码 | 说明 | 应用场景 |
|-------|------|----------|
| 200 | 请求成功 | 正常业务处理 |
| 400 | 请求参数错误 | 参数验证失败、业务规则校验失败 |
| 401 | 未授权访问 | Token无效、Token过期、未提供Token |
| 403 | 权限不足 | 用户权限不够 |
| 404 | 资源不存在 | 文档不存在、接口不存在 |
| 409 | 资源冲突 | 用户名已存在、文档正在处理中 |
| 413 | 请求实体过大 | 文件上传超过大小限制 |
| 415 | 不支持的媒体类型 | 文件格式不支持 |
| 429 | 请求过于频繁 | 触发限流机制 |
| 500 | 服务器内部错误 | 系统异常 |

### 业务错误码

| 错误码 | 错误信息 | 说明 |
|-------|---------|------|
| 1001 | 用户名或密码错误 | 登录认证失败 |
| 1002 | 访问令牌无效 | Token验证失败 |
| 1003 | 访问令牌已过期 | Token已过期 |
| 1004 | 缺少访问令牌 | 未提供Token |
| 1005 | 权限不足 | 用户权限不够 |
| 1006 | 用户不存在 | 指定用户不存在 |
| 1007 | 用户名已存在 | 注册时用户名重复 |
| 1008 | 原密码错误 | 修改密码时原密码错误 |
| 1009 | 新密码不能与原密码相同 | 修改密码时新旧密码相同 |
| 2001 | 参数验证失败 | 请求参数验证失败 |
| 2002 | 缺少必需参数 | 请求缺少必需参数 |
| 2003 | 参数格式错误 | 请求参数格式不正确 |
| 3001 | 文件上传失败 | 文件上传过程中发生错误 |
| 3002 | 文件过大 | 上传文件超过大小限制 |
| 3003 | 不支持的文件类型 | 上传的文件格式不在支持列表中 |
| 3004 | 文件不存在 | 指定文件不存在 |
| 3005 | 文件处理失败 | 文件处理过程中发生错误 |
| 4001 | 存储空间不足 | 用户存储配额不足 |
| 4002 | 文档不存在 | 指定文档ID不存在 |
| 4003 | 文档处理失败 | 文档解析或向量化失败 |
| 4004 | 文档正在处理中，无法删除 | 尝试删除正在处理的文档 |
| 5001 | 搜索失败 | 搜索过程中发生错误 |
| 5002 | 搜索内容不能为空 | 搜索查询内容为空 |
| 5003 | 未找到相关内容 | 搜索未返回结果 |
| 6001 | AI服务不可用 | AI服务当前不可用 |
| 6002 | AI请求失败 | 向AI服务发送请求失败 |
| 6003 | AI响应错误 | AI服务返回错误响应 |
| 9001 | 系统内部错误 | 系统内部发生未预期错误 |
| 9002 | 数据库错误 | 数据库操作失败 |
| 9003 | 网络错误 | 网络连接异常 |
| 9004 | 服务不可用 | 依赖服务不可用 |
| 9005 | 操作超时 | 操作执行超时 |
| 9006 | 系统维护中 | 系统正在维护 |
| 9007 | 请求过于频繁 | 触发限流机制 |

### 错误响应格式

``json
{
  "success": false,
  "code": 500,
  "data": "具体错误信息",
  "timestamp": "2025-09-21T10:00:00"
}
```

---

## 10. 认证与安全

### 10.1 JWT认证机制

除注册和登录接口外，其他接口都需要在请求头中添加JWT Token：

```
Authorization: Bearer {access_token}
```

**Token说明**：
- **访问令牌(access_token)**: 有效期24小时，用于API访问认证
- **刷新令牌(refresh_token)**: 有效期7天，用于获取新的访问令牌
- **Token格式**: JWT（JSON Web Token），包含用户ID、用户名、权限等信息

### 10.2 安全机制

#### XSS防护
- 自动检测和过滤请求参数中的XSS攻击向量
- 支持URL解码和HTML实体解码检测
- 详细的攻击日志记录

#### CORS跨域控制
- 限制允许的域名列表
- 严格的预检请求验证
- 支持凭据传递控制

#### 敏感信息保护
- 日志中自动脱敏敏感信息（密码、邮箱、API密钥等）
- 响应中不包含敏感用户信息（如密码等）
- 用户邮箱等信息在返回时会进行脱敏处理
- 错误信息不泄露系统内部细节

### 10.3 链路追踪

支持分布式链路追踪，通过以下方式传递TraceId：

**请求头**:
```
X-Trace-Id: {trace_id}
```

**响应头**:
```
X-Trace-Id: {trace_id}
```

如果请求中不包含TraceId，系统会自动生成一个全局唯一的ID。

---

## 11. 限流与配额

### 11.1 用户配额限制

| 用户等级 | 存储配额 | 文档数量限制 | 上传频率限制 |
|---------|----------|-------------|-------------|
| 普通用户(0) | 5GB | 无限制 | 10次/分钟 |
| 进阶用户(1) | 100GB | 无限制 | 30次/分钟 |

### 11.2 API请求限制

| 接口类型 | 限流规则 | 说明 |
|---------|----------|------|
| 认证接口 | 5次/分钟 | 防止暴力破解 |
| 文档上传 | 根据用户等级 | 见上表 |
| 搜索接口 | 60次/分钟 | 通用搜索限制 |
| AI问答 | 20次/分钟 | AI服务成本控制 |
| 监控端点 | 100次/分钟 | 系统监控查询 |

---

## 12. 性能指标

### 12.1 响应时间基准

| 接口类型 | P50响应时间 | P95响应时间 | P99响应时间 |
|---------|-------------|-------------|-------------|
| 用户认证 | <100ms | <200ms | <500ms |
| 文档上传 | <2s | <5s | <10s |
| 文档搜索 | <200ms | <500ms | <1s |
| AI问答 | <1s | <3s | <5s |
| 监控端点 | <50ms | <100ms | <200ms |

### 12.2 系统容量

- **并发用户数**: 1000+
- **文档处理能力**: 100个/小时
- **搜索QPS**: 500+
- **存储容量**: 10TB+

---

## 13. SDK和示例代码

### 13.1 JavaScript/Node.js示例

```
// 安装axios: npm install axios
const axios = require('axios');

class RAGApiClient {
  constructor(baseURL = 'http://localhost:8080', token = null) {
    this.baseURL = baseURL;
    this.token = token;
    this.client = axios.create({ baseURL });
  }

  // 设置认证Token
  setToken(token) {
    this.token = token;
    this.client.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  // 用户登录
  async login(username, password) {
    try {
      const response = await this.client.post('/auth/login', {
        username,
        password
      });
      
      if (response.data.success) {
        this.setToken(response.data.data.token);
      }
      
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // 上传文档
  async uploadDocument(file, title = null, description = null) {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    if (description) formData.append('description', description);

    try {
      const response = await this.client.post('/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-Trace-Id': this.generateTraceId()
        }
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // 搜索文档
  async search(query, searchType = 'SEMANTIC', maxResults = 10) {
    try {
      const response = await this.client.post('/search', {
        query,
        searchType,
        maxResults
      }, {
        headers: {
          'X-Trace-Id': this.generateTraceId()
        }
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // AI问答
  async ask(question, searchType = 'SEMANTIC') {
    try {
      const response = await this.client.post('/search/ask', {
        question,
        searchType
      }, {
        headers: {
          'X-Trace-Id': this.generateTraceId()
        }
      });
      
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // 生成链路追踪ID
  generateTraceId() {
    return Math.random().toString(36).substr(2, 16);
  }
}

// 使用示例
const client = new RAGApiClient();

// 登录
client.login('username', 'password')
  .then(result => {
    if (result.success) {
      console.log('登录成功:', result.data.user.username);
      
      // 搜索文档
      return client.search('机器学习');
    }
  })
  .then(searchResult => {
    if (searchResult.success) {
      console.log('搜索结果:', searchResult.data.data.length);
    }
  })
  .catch(error => {
    console.error('API调用失败:', error.response?.data || error.message);
  });
```

### 13.2 Python示例

```
import requests
import uuid
from typing import Optional, Dict, Any

class RAGApiClient:
    def __init__(self, base_url: str = "http://localhost:8080", token: Optional[str] = None):
        self.base_url = base_url
        self.token = token
        self.session = requests.Session()
        if token:
            self.set_token(token)
    
    def set_token(self, token: str):
        """设置认证Token"""
        self.token = token
        self.session.headers.update({"Authorization": f"Bearer {token}"})
    
    def login(self, username: str, password: str) -> Dict[str, Any]:
        """用户登录"""
        try:
            response = self.session.post(f"{self.base_url}/auth/login", json={
                "username": username,
                "password": password
            })
            response.raise_for_status()
            result = response.json()
            
            if result.get("success"):
                self.set_token(result["data"]["token"])
            
            return result
        except requests.exceptions.RequestException as e:
            raise Exception(f"登录失败: {e}")
    
    def upload_document(self, file_path: str, title: Optional[str] = None, 
                       description: Optional[str] = None) -> Dict[str, Any]:
        """上传文档"""
        try:
            with open(file_path, 'rb') as f:
                files = {'file': f}
                data = {}
                if title:
                    data['title'] = title
                if description:
                    data['description'] = description
                
                headers = {'X-Trace-Id': self.generate_trace_id()}
                response = self.session.post(
                    f"{self.base_url}/documents/upload", 
                    files=files, 
                    data=data,
                    headers=headers
                )
            
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"文档上传失败: {e}")
    
    def search(self, query: str, search_type: str = "SEMANTIC", 
               max_results: int = 10) -> Dict[str, Any]:
        """搜索文档"""
        try:
            headers = {'X-Trace-Id': self.generate_trace_id()}
            response = self.session.post(f"{self.base_url}/search", json={
                "query": query,
                "searchType": search_type,
                "maxResults": max_results
            }, headers=headers)
            
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"搜索失败: {e}")
    
    def ask(self, question: str, search_type: str = "SEMANTIC") -> Dict[str, Any]:
        """AI问答"""
        try:
            headers = {
                'X-Trace-Id': self.generate_trace_id(),
                'Content-Type': 'application/json'
            }
            data = {
                'question': question,
                'searchType': search_type
            }
            response = self.session.post(
                f"{self.base_url}/search/ask", 
                json=data,
                headers=headers
            )
            
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"AI问答失败: {e}")
    
    def generate_trace_id(self) -> str:
        """生成链路追踪ID"""
        return str(uuid.uuid4()).replace('-', '')[:16]

# 使用示例
if __name__ == "__main__":
    client = RAGApiClient()
    
    try:
        # 登录
        login_result = client.login("username", "password")
        if login_result.get("success"):
            print(f"登录成功: {login_result['data']['user']['username']}")
            
            # 搜索文档
            search_result = client.search("机器学习")
            if search_result.get("success"):
                print(f"搜索到 {search_result['data']['total']} 个结果")
            
            # AI问答
            qa_result = client.ask("什么是机器学习？")
            if qa_result.get("success"):
                print(f"AI回答: {qa_result['data']['answer'][:100]}...")
            
    except Exception as e:
        print(f"API调用失败: {e}")