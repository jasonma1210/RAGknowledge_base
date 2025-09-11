# RAG个人知识库系统

基于Spring Boot和LangChain4j构建的进阶版RAG（Retrieval-Augmented Generation）个人知识库系统，支持语义搜索、关键词搜索和混合搜索。

## 项目概述

这是一个完整的个人知识库系统，结合了现代AI技术和向量数据库技术，为用户提供智能化的文档管理和信息检索功能。系统采用RAG（检索增强生成）架构，通过将用户上传的文档转换为向量存储在Milvus向量数据库中，实现高效的语义搜索和智能问答功能。

## 核心功能

### 👥 多用户支持
- **用户注册与登录**：支持用户注册和登录功能
- **用户等级管理**：普通用户(5GB存储)和进阶用户(100GB存储)
- **独立数据空间**：每个用户拥有独立的知识库和文件存储空间
- **AI问答长记忆**：支持为每个用户保存问答历史记录(默认20条)

### 📚 文档管理
- **多格式支持**：支持PDF、DOCX、TXT、MD、EPUB等多种文档格式上传和解析
- **元数据管理**：支持为文档添加标题、描述和标签等元数据信息
- **文档存储**：将文档内容分块处理并存储到向量数据库中
- **文档检索**：提供文档列表查询和删除功能

### 🔍 智能搜索
- **语义搜索**：基于向量相似度的语义搜索，理解用户查询的真实意图
- **关键词搜索**：传统的基于文本匹配的关键词搜索
- **混合搜索**：结合语义搜索和关键词搜索的优势，提供更精准的搜索结果
- **结果过滤**：支持设置相似度阈值和返回结果数量限制

### 💬 AI问答
- **智能问答**：基于检索到的相关文档内容，使用大语言模型生成准确的回答
- **上下文理解**：能够理解复杂问题并提供详细的解答
- **来源追溯**：提供回答所依据的文档来源信息
- **长记忆支持**：保存问答历史记录，支持上下文对话

## 数据库设计

### 表结构说明

#### 用户信息表 (user_info)
存储用户基本信息、认证信息和存储配额管理。

#### 用户文件上传记录表 (user_file_record)
记录用户上传的文件信息，包括文件名、路径、大小等元数据。

#### 文档与Milvus向量ID映射表 (document_milvus_mapping)
存储用户上传文件与Milvus向量ID的映射关系，一个文件对应多个向量ID。

### 设计思路更新
为了更好地管理用户上传文件与向量数据的关联关系，我们对`document_milvus_mapping`表进行了优化：
1. 将原来的`document_id`字段改为`file_record_id`，直接指向`user_file_record`表的主键
2. 这样设计可以更清晰地表达文件记录与向量ID的1对多关系
3. 便于后续的数据维护和查询优化

## 用户数据隔离实现

为了确保每个用户只能访问自己的知识库数据，我们采用了更安全的方案：每个用户拥有独立的Milvus collection。

### 实现方案
1. **用户独立Collection**：每个用户拥有一个独立的Milvus collection，命名规则为`用户名_用户ID`
2. **完全数据隔离**：不同用户的数据存储在不同的collection中，实现物理隔离
3. **自动Collection创建**：系统会根据用户信息自动创建和管理用户的collection

### 实现流程
1. 用户上传文档时，系统根据用户信息创建独立的Milvus collection（如果不存在）
2. 文档向量存储在用户专属的collection中
3. 用户进行搜索或问答时，系统直接访问用户专属的collection
4. 由于数据存储在独立的collection中，天然实现了数据隔离

### 配置说明
系统会根据用户信息自动创建Milvus collection，无需手动配置每个用户的collection：

```java
/**
 * 为指定用户创建MilvusEmbeddingStore实例
 * 每个用户使用独立的collection实现数据隔离
 * 
 * @param userId 用户ID
 * @param username 用户名
 * @return MilvusEmbeddingStore实例
 */
public MilvusEmbeddingStore createUserEmbeddingStore(Long userId, String username) {
    // 使用用户名和用户ID组合命名collection，确保唯一性
    String collectionName = username + "_" + userId;
    
    return MilvusEmbeddingStore.builder()
            .host(host)
            .port(port)
            .collectionName(collectionName)
            .dimension(dimension)
            .idFieldName("id") // 明确指定ID字段名
            .textFieldName("text") // 明确指定文本字段名
            .vectorFieldName("vector") // 明确指定向量字段名
            .build();
}
```

这种实现方式提供了更强的数据隔离性，每个用户的数据完全独立存储，从根本上避免了数据泄露的风险，提高了系统的安全性和隐私保护。

## 技术架构

### 后端技术栈
- **框架**：Spring Boot 3.5.5
- **语言**：Java 17
- **AI框架**：LangChain4j 1.4.0（社区版）
- **向量数据库**：Milvus 2.4.5
- **文件存储**：七牛云对象存储
- **AI模型**：
  - 聊天模型：阿里DashScope qwen-max-latest
  - 嵌入模型：阿里DashScope text-embedding-v4

### 核心组件
1. **AuthService**：用户认证服务，提供注册、登录和认证功能
2. **DocumentProcessor**：文档处理服务，负责解析各种格式的文档并进行智能分块
3. **EmbeddingService**：嵌入服务，使用阿里DashScope模型将文本转换为向量表示
4. **VectorStoreService**：向量存储服务，基于Milvus实现向量的存储和检索
5. **RAGService**：核心业务服务，整合各组件提供完整的RAG功能
6. **QiniuUploadService**：七牛云文件上传服务，支持多用户独立存储

## 系统特点

### 多用户支持
- 完整的用户注册、登录和认证流程
- 基于用户等级的存储配额管理（普通用户5GB，进阶用户100GB）
- 每个用户拥有独立的知识库和文件存储空间，数据完全隔离
- 支持AI问答长记忆功能，为每个用户保存问答历史记录

### 高效性
- 使用Milvus向量数据库实现毫秒级的向量检索
- 智能分块算法确保检索精度和效率的平衡
- 支持大规模文档存储和检索

### 智能化
- 基于大语言模型的语义理解和生成能力
- 支持多种搜索模式满足不同场景需求
- 提供准确的问答服务和来源追溯
- 支持上下文对话的长记忆功能

### 易用性
- RESTful API设计，接口简洁明了
- 完善的文档和示例，便于快速上手
- 支持Docker部署，简化环境搭建

## 存在的问题

1. **文档管理功能不完整**：当前getAllDocuments()方法返回空列表，需要实现从数据库获取文档列表的功能
2. **DashScope配置被注释**：DashScopeConfig.java中的配置被注释，需要根据实际需求决定是否启用
3. **缺少单元测试**：项目中缺少完整的单元测试覆盖
4. **日志级别设置**：应用日志级别设置为DEBUG，生产环境可能需要调整

## 快速开始

### 环境准备

#### 必需组件
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

#### 启动Milvus
```bash
# 使用Docker Compose启动Milvus
docker-compose up -d milvus-standalone etcd minio
```

### 配置阿里云API密钥

在`application.yml`中添加阿里云DashScope API密钥：
```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your-dashscope-api-key
      embedding-model:
        api-key: your-dashscope-api-key
```

### 配置七牛云存储

在`application.yml`中添加七牛云配置：
```yaml
qiniu:
  access-key: your-qiniu-access-key
  secret-key: your-qiniu-secret-key
  bucket: your-bucket-name
  domain: your-domain
```

### 启动应用

#### 本地启动
```bash
# 克隆项目
git clone <repository-url>
cd rag-knowledge-base

# 安装依赖
./mvnw clean install

# 启动应用
./mvnw spring-boot:run
```

#### Docker启动
```bash
# 构建并启动所有服务
docker-compose up -d
```

## API接口文档

### 认证接口

#### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "用户名",
  "password": "密码",
  "email": "邮箱"
}
```

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "用户名",
  "password": "密码"
}
```

### 文档管理接口

#### 上传文档
```http
POST /api/documents/upload
Content-Type: multipart/form-data
Authorization: Bearer <access_token>

表单字段：
- file: 文档文件（必需）
- title: 文档标题（可选）
- description: 文档描述（可选）
- tags: 标签（可选，逗号分隔）
```

#### 获取文档列表
```http
GET /api/documents
Authorization: Bearer <access_token>
```

#### 删除文档
```http
DELETE /api/documents/{documentId}
Authorization: Bearer <access_token>
```

### 搜索接口

#### 搜索知识库
```http
POST /api/search
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "query": "搜索内容",
  "searchType": "SEMANTIC", // SEMANTIC, KEYWORD, HYBRID
  "maxResults": 10,
  "minScore": 0.7
}
```

#### 简单搜索
```http
GET /api/search/simple?query=搜索内容&type=SEMANTIC&limit=10
Authorization: Bearer <access_token>
```

#### AI问答
```http
POST /api/search/ask?question=你的问题&searchType=HYBRID&maxResults=5&minScore=0.7
Authorization: Bearer <access_token>
```

### 健康检查
```http
GET /health
```

## 配置说明

### 应用配置（application.yml）
```yaml
# 服务器配置
server:
  port: 8080

# Milvus向量数据库配置
milvus:
  host: localhost
  port: 19530
  collection:
    name: knowledge_base
  dimension: 1536  # 向量维度

# 七牛云配置
qiniu:
  access-key: your-qiniu-access-key
  secret-key: your-qiniu-secret-key
  bucket: your-bucket-name
  domain: your-domain

# 文档处理配置
document:
  chunk:
    size: 1000      # 分块大小（字符）
    overlap: 200    # 重叠大小（字符）
  supported-formats: pdf,docx,txt,md,epub

# 检索配置
retrieval:
  top-k: 10         # 返回结果数量
  score-threshold: 0.7  # 相似度阈值
```

## 开发指南

### 项目结构
```
src/main/java/com/aliyun/rag/
├── config/          # 配置类
├── controller/      # 控制器
├── model/           # 数据模型
├── service/         # 业务服务
└── exception/       # 异常处理
```

### 服务说明

#### AuthService（用户认证服务）
- 提供用户注册、登录和认证功能
- 管理用户信息和存储配额
- 实现基于令牌的用户认证机制

#### DocumentProcessor（文档处理服务）
- 支持PDF、DOCX、TXT、MD、EPUB格式解析
- 智能文本分块，支持重叠处理
- 文本清洗和预处理

#### EmbeddingService（嵌入服务）
- 使用阿里DashScope text-embedding-v4模型
- 支持单文本和批量文本嵌入
- 生成1536维向量表示

#### VectorStoreService（向量存储服务）
- 基于Milvus的向量存储和检索
- 支持语义搜索、关键词搜索、混合搜索
- 元数据管理和过滤

#### RAGService（核心业务服务）
- 整合文档处理、嵌入、存储和搜索
- 提供AI问答功能
- 事务管理和错误处理

#### QiniuUploadService（七牛云上传服务）
- 实现多用户独立文件存储
- 按用户名和时间创建文件目录结构
- 支持多种文件格式上传

## 使用示例

### 1. 用户注册与登录
```bash
# 用户注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123", "email": "test@example.com"}'

# 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

### 2. 上传文档
```bash
# 获取访问令牌后上传文档
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer your-access-token" \
  -F "file=@example.pdf" \
  -F "title=示例文档" \
  -F "description=这是一个测试文档" \
  -F "tags=测试,示例"
```

### 3. 搜索文档
```bash
# 语义搜索
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-access-token" \
  -d '{"query": "人工智能发展趋势", "searchType": "SEMANTIC", "maxResults": 5}'

# 关键词搜索
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-access-token" \
  -d '{"query": "机器学习", "searchType": "KEYWORD", "maxResults": 5}'
```

### 4. AI问答
```bash
curl -X POST "http://localhost:8080/api/search/ask?question=什么是深度学习&searchType=HYBRID&maxResults=3" \
  -H "Authorization: Bearer your-access-token"
```

## 部署说明

### 生产环境部署

1. **环境变量配置**
   ```bash
   export DASHSCOPE_API_KEY=your-dashscope-api-key
   export QINIU_ACCESS_KEY=your-qiniu-access-key
   export QINIU_SECRET_KEY=your-qiniu-secret-key
   export MILVUS_HOST=milvus-server
   export MILVUS_PORT=19530
   ```

2. **Docker Compose部署**
   ```bash
   # 生产环境配置
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **Kubernetes部署**
   ```bash
   # 使用Helm部署
   helm install rag-kb ./helm-chart/
   ```

### 性能优化

- **Milvus优化**：调整索引类型和参数
- **分块策略**：根据文档类型优化分块大小
- **缓存策略**：实现Redis缓存层
- **异步处理**：使用消息队列处理大文件

## 故障排除

### 常见问题

1. **Milvus连接失败**
   - 检查Docker容器状态
   - 验证网络连接
   - 检查端口配置

2. **API密钥错误**
   - 验证DashScope API密钥
   - 检查密钥权限
   - 查看API调用日志

3. **文档处理失败**
   - 检查文件格式支持
   - 验证文件完整性
   - 查看处理日志

### 日志查看
```bash
# 查看应用日志
tail -f logs/spring.log

# 查看Docker日志
docker-compose logs -f rag-app
```

## 贡献指南

欢迎提交Issue和Pull Request！

### 开发流程
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

### 代码规范
- 遵循Java编码规范
- 添加必要的注释和文档
- 编写单元测试
- 确保代码质量

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 联系方式

- 邮箱：jason_ma1982@qq.com