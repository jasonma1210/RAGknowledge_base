# RAG知识库系统

基于Spring Boot和LangChain4j构建的进阶版RAG（Retrieval-Augmented Generation）个人知识库系统，支持语义搜索、关键词搜索和混合搜索。

## 核心功能

### 👥 多用户支持
- 用户注册与登录：支持用户注册和登录功能
- 用户等级管理：普通用户(5GB存储)和进阶用户(100GB存储)
- 独立数据空间：每个用户拥有独立的知识库和文件存储空间
- AI问答长记忆：支持为每个用户保存问答历史记录(默认20条)

### 📚 文档管理
- 多格式支持：支持PDF、DOCX、TXT、MD、EPUB等多种文档格式上传和解析
- 元数据管理：支持为文档添加标题、描述和标签等元数据信息
- 文档存储：将文档内容分块处理并存储到向量数据库中
- 文档检索：提供文档列表查询和删除功能

### 🔍 智能搜索
- 语义搜索：基于向量相似度的语义搜索，理解用户查询的真实意图
- 关键词搜索：传统的基于文本匹配的关键词搜索
- 混合搜索：结合语义搜索和关键词搜索的优势，提供更精准的搜索结果
- 结果过滤：支持设置相似度阈值和返回结果数量限制

### 💬 AI问答
- 智能问答：基于检索到的相关文档内容，使用大语言模型生成准确的回答
- 上下文理解：能够理解复杂问题并提供详细的解答
- 来源追溯：提供回答所依据的文档来源信息
- 长记忆支持：保存问答历史记录，支持上下文对话

## 快速开始

### 环境准备
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 启动Milvus
```bash
# 使用Docker Compose启动Milvus
docker-compose up -d milvus-standalone etcd minio
```

### 配置阿里云API密钥
在application.yml中添加阿里云DashScope API密钥：
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
在application.yml中添加七牛云配置：
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

## 运维实施流程

### 1. 前端页面部署流程
#### 1.1 构建前端项目
```bash
# 进入前端项目目录
cd src/main/resources/web

# 安装依赖
npm install

# 构建生产版本
npm run build
```

#### 1.2 部署到服务器
1. 将构建生成的 `build` 目录中的所有文件复制到服务器的静态资源目录中
2. 配置Web服务器（如Nginx）指向该目录
3. 重启Web服务器使配置生效

### 2. 后端服务部署流程
#### 2.1 构建后端项目
```bash
# 在项目根目录执行Maven构建
./mvnw clean package
```

#### 2.2 部署到服务器
1. 将生成的 `target/RAGknowledge_base-1.0.0.jar` 文件上传到服务器
2. 在服务器上运行以下命令启动服务：
```bash
java -jar RAGknowledge_base-1.0.0.jar
```

## 环境配置要求

### 3.1 前端环境
- Node.js >= 16.0.0
- npm >= 7.0.0

### 3.2 后端环境
- Java >= 17
- Maven >= 3.6.0

## 依赖服务配置
确保以下服务已正确配置并可访问：
1. Milvus向量数据库
2. MySQL数据库
3. 七牛云存储服务（可选）