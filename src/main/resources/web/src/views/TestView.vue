<template>
  <div class="test-page page-container">
    <div class="page-header">
      <h1 class="page-title">测试页面</h1>
      <p class="page-subtitle">系统功能测试</p>
    </div>

    <el-card class="test-card ruoyi-card">
      <div class="card-header">
        <h3 class="card-title">测试信息</h3>
      </div>
      <div class="card-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前路由">{{ route.path }}</el-descriptions-item>
          <el-descriptions-item label="Token状态">{{ token }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card class="actions-card ruoyi-card">
      <div class="card-header">
        <h3 class="card-title">测试操作</h3>
      </div>
      <div class="card-body">
        <div class="action-buttons">
          <el-button type="primary" @click="testLogin" class="ruoyi-button">
            <el-icon><User /></el-icon>
            测试登录
          </el-button>
          <el-button type="warning" @click="clearToken" class="ruoyi-button">
            <el-icon><Delete /></el-icon>
            清除Token
          </el-button>
          <el-button type="danger" @click="goToLogin" class="ruoyi-button">
            <el-icon><SwitchButton /></el-icon>
            跳转到登录页
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Delete, SwitchButton } from '@element-plus/icons-vue'
import { authAPI } from '@/services/api'

const router = useRouter()
const route = useRoute()

const token = ref(localStorage.getItem('token') || '无')

const testLogin = async () => {
  try {
    console.log('测试登录...')
    const response = await authAPI.login('testuser', 'testpassword')
    console.log('登录响应:', response)
    token.value = localStorage.getItem('token') || '无'
  } catch (error) {
    console.error('登录测试失败:', error)
  }
}

const clearToken = () => {
  localStorage.removeItem('token')
  token.value = '无'
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.test-page {
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

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.ruoyi-button {
  border-radius: var(--el-border-radius-base);
  transition: all var(--el-transition-duration) var(--el-transition-function-ease-in-out-bezier);
  font-weight: var(--el-font-weight-primary);
  padding: var(--el-spacing-2) var(--el-spacing-4);
  font-size: var(--el-font-size-base);
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid transparent;
  background-color: transparent;
  outline: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .test-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .ruoyi-button {
    width: 100%;
    justify-content: center;
  }
}
</style>