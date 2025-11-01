<template>
  <div class="home-page page-container">
    <div class="page-header">
      <h1 class="page-title">RAG知识库系统</h1>
      <p class="page-subtitle">基于检索增强生成技术的智能知识库平台</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card ruoyi-card">
          <div class="welcome-content">
            <h2>欢迎使用 RAG知识库系统</h2>
            <p>基于检索增强生成技术的智能知识库平台</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToDocuments">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#409eff"><Document /></el-icon>
            <h3>文档管理</h3>
            <p>上传、管理和查看您的知识文档</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToSearch">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#67c23a"><Search /></el-icon>
            <h3>知识搜索</h3>
            <p>基于语义的智能搜索功能</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToAsk">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#e6a23c"><ChatDotRound /></el-icon>
            <h3>AI问答</h3>
            <p>与AI进行智能问答交互</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToVectorData">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#f56c6c"><DataLine /></el-icon>
            <h3>向量数据</h3>
            <p>查看和管理向量数据</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToSystemMonitor">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#909399"><Monitor /></el-icon>
            <h3>系统监控</h3>
            <p>监控系统运行状态和性能</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="feature-card ruoyi-card" @click="goToProfile">
          <div class="feature-content">
            <el-icon class="feature-icon" color="#409eff"><User /></el-icon>
            <h3>个人中心</h3>
            <p>管理个人账户和设置</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Document, Search, ChatDotRound, ChatLineRound, DataLine, Monitor, User } from '@element-plus/icons-vue'

const router = useRouter()

const goToDocuments = () => {
  router.push('/documents')
}

const goToSearch = () => {
  router.push('/search')
}

const goToConversation = () => {
  router.push('/conversation')
}

const goToVectorData = () => {
  router.push('/vector-data')
}

const goToSystemMonitor = () => {
  router.push('/system-monitor')
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.home-page {
  padding: 20px;
  background-color: var(--el-bg-color-page);
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.ruoyi-card {
  margin-bottom: 20px;
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;
}

.ruoyi-card:hover {
  box-shadow: var(--el-box-shadow-lighter);
  transform: translateY(-3px);
}

.welcome-card {
  text-align: center;
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow);
}

.welcome-content h2 {
  margin: 0 0 15px 0;
  color: var(--el-text-color-primary);
  font-size: 24px;
}

.welcome-content p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 16px;
}

.feature-card {
  cursor: pointer;
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--el-box-shadow);
}

.feature-content {
  text-align: center;
  padding: 30px 20px;
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.feature-content h3 {
  margin: 0 0 15px 0;
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 500;
}

.feature-content p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .welcome-content h2 {
    font-size: 20px;
  }
  
  .welcome-content p {
    font-size: 14px;
  }
  
  .feature-content {
    padding: 20px 15px;
  }
  
  .feature-icon {
    font-size: 36px;
    margin-bottom: 15px;
  }
  
  .feature-content h3 {
    font-size: 16px;
  }
  
  .feature-content p {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .el-col:last-child {
    margin-bottom: 0;
  }
}
</style>