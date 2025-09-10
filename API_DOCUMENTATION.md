# RAG知识库系统API接口文档

## 1. 概述

本文档描述了RAG知识库系统提供的RESTful API接口，包括文档管理、知识库搜索和系统健康检查等功能。

## 2. 公共信息

### 2.1 基础URL
```
http://localhost:8080
```

### 2.2 请求格式
- 所有请求和响应数据都使用JSON格式
- 字符编码为UTF-8
- 请求头需要包含: `Content-Type: application/json`

### 2.3 响应格式
- 成功响应: HTTP状态码200
- 客户端错误: HTTP状态码400-499
- 服务器错误: HTTP状态码500-599

## 3. 文档管理接口

### 3.1 上传文档

**接口地址**: `POST /api/documents/upload`

**请求方式**: `POST`

**请求类型**: `multipart/form-data`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | MultipartFile | 是 | 文档文件 |
| title | String | 否 | 文档标题 |
| description | String | 否 | 文档描述 |
| tags | String | 否 | 标签，多个标签用逗号分隔 |

**成功响应**:
```json
{
  "id": "文档唯一标识符",
  "title": "文档标题",
  "description": "文档描述",
  "fileName": "文件名",
  "fileType": "文件类型",
  "fileSize": 12345,
  "tags": "标签1,标签2",
  "uploadTime": "2025-09-09T12:00:00",
  "chunkCount": 10
}
```

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "文档上传失败: 错误信息",
  "path": "/api/documents/upload"
}
```

### 3.2 获取所有文档

**接口地址**: `GET /api/documents`

**请求方式**: `GET`

**请求参数**: 无

**成功响应**:
```json
[
  {
    "id": "文档唯一标识符",
    "title": "文档标题",
    "description": "文档描述",
    "fileName": "文件名",
    "fileType": "文件类型",
    "fileSize": 12345,
    "tags": "标签1,标签2",
    "uploadTime": "2025-09-09T12:00:00",
    "chunkCount": 10
  }
]
```

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "获取文档列表失败: 错误信息",
  "path": "/api/documents"
}
```

### 3.3 删除文档

**接口地址**: `DELETE /api/documents/{documentId}`

**请求方式**: `DELETE`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| documentId | String | 是 | 文档唯一标识符 |

**成功响应**: HTTP状态码200，无响应体

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "删除文档失败: 错误信息",
  "path": "/api/documents/{documentId}"
}
```

## 4. 搜索接口

### 4.1 搜索知识库

**接口地址**: `POST /api/search`

**请求方式**: `POST`

**请求类型**: `application/json`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| query | String | 是 | 搜索内容 |
| searchType | String | 否 | 搜索类型: SEMANTIC(语义搜索)、KEYWORD(关键词搜索)、HYBRID(混合搜索)，默认为SEMANTIC |
| maxResults | Integer | 否 | 最大返回结果数，默认为10 |
| minScore | Double | 否 | 最小相似度分数，默认为0.7 |

**请求示例**:
```json
{
  "query": "人工智能发展趋势",
  "searchType": "SEMANTIC",
  "maxResults": 5,
  "minScore": 0.7
}
```

**成功响应**:
```json
[
  {
    "documentId": "文档唯一标识符",
    "title": "文档标题",
    "content": "匹配的文档内容片段",
    "score": 0.85,
    "source": "文档来源",
    "position": 1
  }
]
```

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "搜索失败: 错误信息",
  "path": "/api/search"
}
```

### 4.2 简单搜索

**接口地址**: `GET /api/search/simple`

**请求方式**: `GET`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| query | String | 是 | 搜索内容 |
| type | String | 否 | 搜索类型: SEMANTIC(语义搜索)、KEYWORD(关键词搜索)、HYBRID(混合搜索)，默认为SEMANTIC |
| limit | Integer | 否 | 最大返回结果数，默认为10 |

**请求示例**:
```
GET /api/search/simple?query=人工智能发展趋势&type=SEMANTIC&limit=5
```

**成功响应**:
```json
[
  {
    "documentId": "文档唯一标识符",
    "title": "文档标题",
    "content": "匹配的文档内容片段",
    "score": 0.85,
    "source": "文档来源",
    "position": 1
  }
]
```

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "搜索失败: 错误信息",
  "path": "/api/search/simple"
}
```

### 4.3 AI问答

**接口地址**: `POST /api/search/ask`

**请求方式**: `POST`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| question | String | 是 | 问题内容 |
| searchType | String | 否 | 搜索类型: SEMANTIC(语义搜索)、KEYWORD(关键词搜索)、HYBRID(混合搜索)，默认为SEMANTIC |
| maxResults | Integer | 否 | 最大返回结果数，默认为10 |
| minScore | Double | 否 | 最小相似度分数，默认为0.7 |

**请求示例**:
```
POST /api/search/ask?question=什么是深度学习&searchType=HYBRID&maxResults=5&minScore=0.7
```

**成功响应**:
```json
{
  "question": "问题内容",
  "answer": "基于检索到的文档内容生成的回答",
  "sources": [
    {
      "documentId": "文档唯一标识符",
      "title": "文档标题",
      "content": "匹配的文档内容片段",
      "score": 0.85,
      "source": "文档来源",
      "position": 1
    }
  ],
  "sourceCount": 1
}
```

**错误响应**:
```json
{
  "timestamp": "2025-09-09T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "问答失败: 错误信息",
  "path": "/api/search/ask"
}
```

## 5. 健康检查接口

### 5.1 系统健康状态

**接口地址**: `GET /health`

**请求方式**: `GET`

**请求参数**: 无

**成功响应**:
```json
{
  "status": "UP",
  "service": "RAG Knowledge Base System",
  "version": "1.0.0"
}
```

## 6. 错误码说明

| HTTP状态码 | 说明 |
| --- | --- |
| 200 | 请求成功 |
| 400 | 客户端请求参数错误 |
| 404 | 请求的资源不存在 |
| 500 | 服务器内部错误 |

## 7. 使用示例

### 7.1 上传文档
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@example.pdf" \
  -F "title=示例文档" \
  -F "description=这是一个测试文档" \
  -F "tags=测试,示例"
```

### 7.2 搜索文档
```bash
# 语义搜索
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "人工智能发展趋势", "searchType": "SEMANTIC", "maxResults": 5}'

# 关键词搜索
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "机器学习", "searchType": "KEYWORD", "maxResults": 5}'
```

### 7.3 AI问答
```bash
curl -X POST "http://localhost:8080/api/search/ask?question=什么是深度学习&searchType=HYBRID&maxResults=3"
```