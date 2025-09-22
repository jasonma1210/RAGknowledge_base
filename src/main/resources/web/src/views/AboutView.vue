<template>
  <div class="about-page page-container">
    <div class="page-header">
      <h1 class="page-title">关于系统</h1>
      <p class="page-subtitle">RAG知识库系统</p>
    </div>

    <el-card class="about-card ruoyi-card">
      <div class="card-header">
        <h3 class="card-title">系统介绍</h3>
      </div>
      <div class="card-body">
        <p>RAG知识库系统是一个基于检索增强生成技术的智能知识库平台，旨在帮助用户高效地管理和检索知识文档。</p>
        
        <h4>核心功能</h4>
        <ul>
          <li>文档管理：支持多种格式文档的上传、存储和管理</li>
          <li>智能搜索：基于语义理解的高效搜索功能</li>
          <li>AI问答：与AI进行智能问答交互</li>
          <li>向量数据：查看和管理向量数据</li>
          <li>系统监控：实时监控系统运行状态</li>
        </ul>
        
        <h4>技术架构</h4>
        <ul>
          <li>前端：Vue 3 + Element Plus + Vite</li>
          <li>后端：Spring Boot</li>
          <li>数据库：关系型数据库</li>
          <li>向量存储：专用向量数据库</li>
          <li>AI引擎：大语言模型集成</li>
        </ul>
      </div>
    </el-card>

    <el-card class="tech-card ruoyi-card">
      <div class="card-header">
        <h3 class="card-title">技术栈</h3>
      </div>
      <div class="card-body">
        <el-row :gutter="20">
          <el-col :span="12">
            <h4>前端技术</h4>
            <ul>
              <li>Vue 3 (Composition API)</li>
              <li>Element Plus UI组件库</li>
              <li>Vite构建工具</li>
              <li>TypeScript</li>
            </ul>
          </el-col>
          <el-col :span="12">
            <h4>后端技术</h4>
            <ul>
              <li>Spring Boot</li>
              <li>Spring Security</li>
              <li>MyBatis Plus</li>
              <li>JWT认证</li>
            </ul>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.about-page {
  padding: 20px;
  background-color: var(--el-bg-color-page);
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.ruoyi-card {
  margin-bottom: 20px;
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  border: 1px solid var(--el-border-color-lighter);
  transition: box-shadow 0.3s ease;
}

.ruoyi-card:hover {
  box-shadow: var(--el-box-shadow-lighter);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0;
}

.card-body {
  padding: 20px;
}

.card-body h4 {
  margin: 20px 0 10px 0;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.card-body ul {
  padding-left: 20px;
}

.card-body li {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  :deep(.el-col) {
    width: 100%;
    margin-bottom: 20px;
  }
  
  :deep(.el-col:last-child) {
    margin-bottom: 0;
  }
}
</style>