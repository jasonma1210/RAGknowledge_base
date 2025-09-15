RAG个人知识库系统
基于Spring Boot和LangChain4j构建的进阶版RAG（Retrieval-Augmented Generation）个人知识库系统，支持语义搜索、关键词搜索和混合搜索。

项目概述
这是一个完整的个人知识库系统，结合了现代AI技术和向量数据库技术，为用户提供智能化的文档管理和信息检索功能。系统采用RAG（检索增强生成）架构，通过将用户上传的文档转换为向量存储在Milvus向量数据库中，实现高效的语义搜索和智能问答功能。

核心功能
👥 多用户支持
用户注册与登录：支持用户注册和登录功能
用户等级管理：普通用户(5GB存储)和进阶用户(100GB存储)
独立数据空间：每个用户拥有独立的知识库和文件存储空间
AI问答长记忆：支持为每个用户保存问答历史记录(默认20条)
📚 文档管理
多格式支持：支持PDF、DOCX、TXT、MD、EPUB等多种文档格式上传和解析
元数据管理：支持为文档添加标题、描述和标签等元数据信息
文档存储：将文档内容分块处理并存储到向量数据库中
文档检索：提供文档列表查询和删除功能
🔍 智能搜索
语义搜索：基于向量相似度的语义搜索，理解用户查询的真实意图
关键词搜索：传统的基于文本匹配的关键词搜索
混合搜索：结合语义搜索和关键词搜索的优势，提供更精准的搜索结果
结果过滤：支持设置相似度阈值和返回结果数量限制
💬 AI问答
智能问答：基于检索到的相关文档内容，使用大语言模型生成准确的回答
上下文理解：能够理解复杂问题并提供详细的解答
来源追溯：提供回答所依据的文档来源信息
长记忆支持：保存问答历史记录，支持上下文对话
数据库设计
表结构说明
用户信息表 (user_info)
存储用户基本信息、认证信息和存储配额管理。

用户文件上传记录表 (user_file_record)
记录用户上传的文件信息，包括文件名、路径、大小等元数据。

文档与Milvus向量ID映射表 (document_milvus_mapping)
存储用户上传文件与Milvus向量ID的映射关系，一个文件对应多个向量ID。

设计思路更新
为了更好地管理用户上传文件与向量数据的关联关系，我们对document_milvus_mapping表进行了优化：

将原来的document_id字段改为file_record_id，直接指向user_file_record表的主键
这样设计可以更清晰地表达文件记录与向量ID的1对多关系
便于后续的数据维护和查询优化
用户数据隔离实现
为了确保每个用户只能访问自己的知识库数据，我们采用了更安全的方案：每个用户拥有独立的Milvus collection。

实现方案
用户独立Collection：每个用户拥有一个独立的Milvus collection，命名规则为用户名_用户ID
完全数据隔离：不同用户的数据存储在不同的collection中，实现物理隔离
自动Collection创建：系统会根据用户信息自动创建和管理用户的collection
实现流程
用户上传文档时，系统根据用户信息创建独立的Milvus collection（如果不存在）
文档向量存储在用户专属的collection中
用户进行搜索或问答时，系统直接访问用户专属的collection
由于数据存储在独立的collection中，天然实现了数据隔离
配置说明
系统会根据用户信息自动创建Milvus collection，无需手动配置每个用户的collection：

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
  这种实现方式提供了更强的数据隔离性，每个用户的数据完全独立存储，从根本上避免了数据泄露的风险，提高了系统的安全性和隐私保护。

技术架构
后端技术栈
框架：Spring Boot 3.5.5
语言：Java 17
AI框架：LangChain4j 1.4.0（社区版）
向量数据库：Milvus 2.4.5
文件存储：七牛云对象存储
AI模型：
聊天模型：阿里DashScope qwen-max-latest
嵌入模型：阿里DashScope text-embedding-v4
核心组件
AuthService：用户认证服务，提供注册、登录和认证功能
DocumentProcessor：文档处理服务，负责解析各种格式的文档并进行智能分块
EmbeddingService：嵌入服务，使用阿里DashScope模型将文本转换为向量表示
VectorStoreService：向量存储服务，基于Milvus实现向量的存储和检索
RAGService：核心业务服务，整合各组件提供完整的RAG功能
QiniuUploadService：七牛云文件上传服务，支持多用户独立存储
系统特点
多用户支持
完整的用户注册、登录和认证流程
基于用户等级的存储配额管理（普通用户5GB，进阶用户100GB）
每个用户拥有独立的知识库和文件存储空间，数据完全隔离
支持AI问答长记忆功能，为每个用户保存问答历史记录
高效性
使用Milvus向量数据库实现毫秒级的向量检索
智能分块算法确保检索精度和效率的平衡
支持大规模文档存储和检索
智能化
基于大语言模型的语义理解和生成能力
支持多种搜索模式满足不同场景需求
提供准确的问答服务和来源追溯
支持上下文对话的长记忆功能
易用性
RESTful API设计，接口简洁明了
完善的文档和示例，便于快速上手
支持Docker部署，简化环境搭建
存在的问题
文档管理功能不完整：当前getAllDocuments()方法返回空列表，需要实现从数据库获取文档列表的功能
DashScope配置被注释：DashScopeConfig.java中的配置被注释，需要根据实际需求决定是否启用
缺少单元测试：项目中缺少完整的单元测试覆盖
日志级别设置：应用日志级别设置为DEBUG，生产环境可能需要调整
快速开始
环境准备
必需组件
Java 17+
Maven 3.8+
Docker & Docker Compose
启动Milvus
# 使用Docker Compose启动Milvus
docker-compose up -d milvus-standalone etcd minio
配置阿里云API密钥
在application.yml中添加阿里云DashScope API密钥：

langchain4j:
community:
dashscope:
chat-model:
api-key: your-dashscope-api-key
embedding-model:
api-key: your-dashscope-api-key
配置七牛云存储
在application.yml中添加七牛云配置：

qiniu:
access-key: your-qiniu-access-key
secret-key: your-qiniu-secret-key
bucket: your-bucket-name
domain: your-domain
启动应用
本地启动
# 克隆项目
git clone <repository-url>
cd rag-knowledge-base

# 安装依赖
./mvnw clean install

# 启动应用
./mvnw spring-boot:run
Docker启动
# 构建并启动所有服务
docker-compose up -d
# 运维实施流程

## 1. 前端页面部署流程

### 1.1 构建前端项目
```bash
# 进入前端项目目录
cd src/main/resources/web

# 安装依赖
npm install

# 构建生产版本
npm run build
```

### 1.2 部署到服务器
1. 将构建生成的 `build` 目录中的所有文件复制到服务器的静态资源目录中
2. 配置Web服务器（如Nginx）指向该目录
3. 重启Web服务器使配置生效

## 2. 后端服务部署流程

### 2.1 构建后端项目
```bash
# 在项目根目录执行Maven构建
./mvnw clean package
```

### 2.2 部署到服务器
1. 将生成的 `target/RAGknowledge_base-1.0.0.jar` 文件上传到服务器
2. 在服务器上运行以下命令启动服务：
```bash
java -jar RAGknowledge_base-1.0.0.jar
```

## 3. 环境配置要求

### 3.1 前端环境
- Node.js >= 14.0.0
- npm >= 6.0.0

### 3.2 后端环境
- Java >= 8
- Maven >= 3.6.0

## 4. 依赖服务配置

确保以下服务已正确配置并可访问：
1. Milvus向量数据库
2. MySQL数据库
3. 七牛云存储服务（可选）

在 `application-local.yml` 文件中配置相应的连接信息。