# RAG知识库系统API接口文档

## 1. 认证接口

### 1.1 用户注册

**接口地址**: `POST /api/auth/register`

**请求头**:
```
Content-Type: application/json
```

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填）
  "password": "string",  // 密码（必填）
  "email": "string"      // 邮箱（可选）
}
```

**响应参数**:
```json
{
  "token": "string",     // 访问令牌
  "user": {              // 用户信息
    "id": 123,           // 用户ID
    "username": "string",// 用户名
    "email": "string",   // 邮箱
    "level": 0,          // 用户等级（0:普通用户 1:进阶用户）
    "storageQuota": 123, // 存储配额（字节）
    "usedStorage": 123,  // 已使用存储空间（字节）
    "lastLoginTime": "2025-09-11T10:00:00", // 最后登录时间
    "gmtCreate": "2025-09-11T10:00:00",     // 创建时间
    "gmtModified": "2025-09-11T10:00:00",   // 修改时间
    "isDeleted": 0       // 是否删除（0:未删除 1:已删除）
  },
  "success": true,       // 是否成功
  "message": "string"    // 错误信息
}
```

**状态码**:
- 200: 注册成功
- 400: 请求参数错误或注册失败

### 1.2 用户登录

**接口地址**: `POST /api/auth/login`

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

**响应参数**:
```json
{
  "token": "string",     // 访问令牌
  "user": {              // 用户信息
    "id": 123,           // 用户ID
    "username": "string",// 用户名
    "email": "string",   // 邮箱
    "level": 0,          // 用户等级（0:普通用户 1:进阶用户）
    "storageQuota": 123, // 存储配额（字节）
    "usedStorage": 123,  // 已使用存储空间（字节）
    "lastLoginTime": "2025-09-11T10:00:00", // 最后登录时间
    "gmtCreate": "2025-09-11T10:00:00",     // 创建时间
    "gmtModified": "2025-09-11T10:00:00",   // 修改时间
    "isDeleted": 0       // 是否删除（0:未删除 1:已删除）
  },
  "success": true,       // 是否成功
  "message": "string"    // 错误信息
}
```

**状态码**:
- 200: 登录成功
- 400: 用户名或密码错误

## 2. 文档管理接口

### 2.1 上传文档

**接口地址**: `POST /api/documents/upload`

**请求头**:
```
Content-Type: multipart/form-data
Authorization: Bearer {access_token}
```

**请求参数**:
```
file: 文件（必填）
title: 文档标题（可选）
description: 文档描述（可选）
tags: 标签（可选，逗号分隔）
```

**响应参数**:
```json
{
  "id": "string",        // 文档ID
  "title": "string",     // 文档标题
  "description": "string",// 文档描述
  "fileName": "string",  // 文件名
  "fileType": "string",  // 文件类型
  "fileSize": 123,       // 文件大小（字节）
  "tags": "string",      // 标签
  "uploadTime": "2025-09-11T10:00:00", // 上传时间
  "chunkCount": 123      // 分块数量
}
```

**状态码**:
- 200: 上传成功
- 400: 请求参数错误或上传失败
- 401: 未授权访问

### 2.2 获取所有文档

**接口地址**: `GET /api/documents`

**请求头**:
```
Authorization: Bearer {access_token}
```

**响应参数**:
```json
[
  {
    "id": "string",        // 文档ID
    "title": "string",     // 文档标题
    "description": "string",// 文档描述
    "fileName": "string",  // 文件名
    "fileType": "string",  // 文件类型
    "fileSize": 123,       // 文件大小（字节）
    "tags": "string",      // 标签
    "uploadTime": "2025-09-11T10:00:00", // 上传时间
    "chunkCount": 123      // 分块数量
  }
]
```

**状态码**:
- 200: 获取成功
- 401: 未授权访问
- 500: 服务器内部错误

### 2.3 删除文档

**接口地址**: `DELETE /api/documents/{documentId}`

**请求头**:
```
Authorization: Bearer {access_token}
```

**路径参数**:
```
documentId: 文档ID
```

**响应参数**:
```
无
```

**状态码**:
- 200: 删除成功
- 400: 删除失败
- 401: 未授权访问

## 3. 搜索接口

### 3.1 知识库搜索

**接口地址**: `POST /api/search`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {access_token}
```

**请求参数**:
```json
{
  "query": "string",      // 搜索内容（必填）
  "searchType": "SEMANTIC",// 搜索类型（可选，默认SEMANTIC）
  "maxResults": 10,       // 最大结果数（可选，默认10）
  "minScore": 0.7         // 最小相似度分数（可选，默认0.7）
}
```

**搜索类型说明**:
- SEMANTIC: 语义搜索
- KEYWORD: 关键词搜索
- HYBRID: 混合搜索

**响应参数**:
```json
[
  {
    "fileRecordId": "string",  // 文件记录ID
    "title": "string",       // 文档标题
    "content": "string",     // 匹配内容
    "score": 0.8,            // 相似度分数
    "source": "string",      // 来源
    "position": 123          // 位置
  }
]
```

**状态码**:
- 200: 搜索成功
- 400: 请求参数错误或搜索失败
- 401: 未授权访问

### 3.2 简单搜索

**接口地址**: `GET /api/search/simple`

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
```
query: 搜索内容（必填）
type: 搜索类型（可选，默认SEMANTIC）
limit: 最大结果数（可选，默认10）
```

**响应参数**:
```json
[
  {
    "fileRecordId": "string",  // 文件记录ID
    "title": "string",       // 文档标题
    "content": "string",     // 匹配内容
    "score": 0.8,            // 相似度分数
    "source": "string",      // 来源
    "position": 123          // 位置
  }
]
```

**状态码**:
- 200: 搜索成功
- 400: 请求参数错误或搜索失败
- 401: 未授权访问

## 4. 问答接口

### 4.1 智能问答

**接口地址**: `POST /api/search/ask`

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
```
question: 问题内容（必填）
searchType: 搜索类型（可选，默认SEMANTIC）
maxResults: 最大结果数（可选，默认10）
minScore: 最小相似度分数（可选，默认0.7）
```

**响应参数**:
```json
{
  "question": "string",      // 问题内容
  "answer": "string",        // 回答内容
  "sources": [               // 来源信息
    {
      "fileRecordId": "string",  // 文件记录ID
      "title": "string",       // 文档标题
      "content": "string",     // 匹配内容
      "score": 0.8,            // 相似度分数
      "source": "string",      // 来源
      "position": 123          // 位置
    }
  ],
  "sourceCount": 123         // 来源数量
}
```

**状态码**:
- 200: 问答成功
- 400: 请求参数错误或问答失败
- 401: 未授权访问

## 5. 错误码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误或业务处理失败 |
| 401 | 未授权访问 |
| 500 | 服务器内部错误 |

## 6. 认证方式

除注册和登录接口外，其他接口都需要在请求头中添加认证信息：

```
Authorization: Bearer {access_token}
```

其中 `{access_token}` 为用户登录成功后返回的token值。