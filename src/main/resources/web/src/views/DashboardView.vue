<template>
  <div class="dashboard page-container">
    <div class="page-header">
      <h1 class="page-title">系统仪表板</h1>
      <p class="page-subtitle">查看系统状态和统计数据</p>
    </div>
    
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-primary">
              <Document />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardData.userStats?.fileCount || 0 }}</div>
              <div class="stat-label">文档总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-success">
              <DataLine />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardData.userStats?.vectorCount || 0 }}</div>
              <div class="stat-label">向量总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-warning">
              <Monitor />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatStorage(dashboardData.userStats?.usedStorage || 0) }}</div>
              <div class="stat-label">已用存储</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-danger">
              <PieChart />
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatStorage(dashboardData.userStats?.storageQuota || 0) }}</div>
              <div class="stat-label">存储配额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="16" class="charts-row">
      <el-col :xs="24" :md="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">系统资源使用情况</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="progress-item">
              <div class="progress-label">
                <span>CPU使用率</span>
                <span>{{ dashboardData.systemInfo?.cpuUsage || 0 }}%</span>
              </div>
              <el-progress 
                :percentage="dashboardData.systemInfo?.cpuUsage || 0" 
                :stroke-width="8" 
                status="success"
              />
            </div>
            
            <div class="progress-item">
              <div class="progress-label">
                <span>内存使用率</span>
                <span>{{ dashboardData.systemInfo?.memoryUsage || 0 }}%</span>
              </div>
              <el-progress 
                :percentage="dashboardData.systemInfo?.memoryUsage || 0" 
                :stroke-width="8" 
                status="warning"
              />
            </div>
            
            <div class="progress-item">
              <div class="progress-label">
                <span>磁盘使用率</span>
                <span>{{ dashboardData.systemInfo?.diskUsage || 0 }}%</span>
              </div>
              <el-progress 
                :percentage="dashboardData.systemInfo?.diskUsage || 0" 
                :stroke-width="8" 
                status="exception"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">系统信息</span>
            </div>
          </template>
          <div class="info-content">
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="系统负载">{{ dashboardData.systemInfo?.loadAverage || '-' }}</el-descriptions-item>
              <el-descriptions-item label="运行时间">{{ dashboardData.systemInfo?.uptime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="进程数">{{ dashboardData.systemInfo?.processCount || '-' }}</el-descriptions-item>
              <el-descriptions-item label="存储使用率">{{ dashboardData.userStats?.storageUsage || 0 }}%</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DataLine, Monitor, PieChart } from '@element-plus/icons-vue'
import { systemAPI } from '@/services/api'

// 定义系统信息接口
interface SystemInfo {
  cpuUsage: number
  memoryUsage: number
  diskUsage: number
  totalMemory: number
  usedMemory: number
  totalDiskSpace: number
  usedDiskSpace: number
  uptime: string
  processCount: number
  loadAverage: number
}

// 定义用户统计信息接口（根据后端实际返回字段）
interface UserStats {
  fileCount: number
  vectorCount: number
  usedStorage: number
  storageQuota: number
  storageUsage: number
}

// 定义仪表板数据接口
interface DashboardData {
  systemInfo: SystemInfo
  userStats: UserStats
}

const dashboardData = ref<DashboardData>({
  systemInfo: {
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0,
    totalMemory: 0,
    usedMemory: 0,
    totalDiskSpace: 0,
    usedDiskSpace: 0,
    uptime: '',
    processCount: 0,
    loadAverage: 0
  },
  userStats: {
    fileCount: 0,
    vectorCount: 0,
    usedStorage: 0,
    storageQuota: 0,
    storageUsage: 0
  }
})

// 定时器引用
const refreshTimer = ref<number | null>(null)

// 格式化存储大小
const formatStorage = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 获取仪表板数据
const fetchDashboardData = async () => {
  try {
    console.log('开始获取仪表板数据...')
    const response = await systemAPI.getDashboard()
    console.log('仪表板数据响应:', response)
    if (response.success) {
      // 适配新的响应数据结构
      dashboardData.value = {
        systemInfo: response.data?.systemInfo,
        userStats: response.data?.userStats
      }
      console.log('仪表板数据获取成功:', response)
    } else {
      ElMessage.error(response.data || '获取仪表板数据失败')
    }
  } catch (error: any) {
    console.error('获取仪表板数据失败:', error)
    ElMessage.error('获取仪表板数据失败: ' + (error.message || '网络错误'))
  }
}

// 启动定时刷新
const startAutoRefresh = () => {
  // 每5秒刷新一次数据
  refreshTimer.value = window.setInterval(() => {
    fetchDashboardData()
  }, 5000)
}

// 停止定时刷新
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    window.clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

onMounted(() => {
  fetchDashboardData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.page-header {
  margin-bottom: var(--el-spacing-5);
}

.page-title {
  font-size: var(--el-font-size-extra-large);
  font-weight: var(--el-font-weight-primary);
  color: var(--el-text-color-primary);
  margin-bottom: var(--el-spacing-2);
}

.page-subtitle {
  font-size: var(--el-font-size-base);
  color: var(--el-text-color-secondary);
  margin: 0;
}

.stats-row {
  margin-bottom: var(--el-spacing-5);
}

.stat-card {
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  transition: var(--el-transition-duration) var(--el-transition-function-ease-in-out-bezier);
  border: 1px solid var(--el-border-color-lighter);
}

.stat-card:hover {
  box-shadow: var(--el-box-shadow-lighter);
  transform: translateY(-1px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: var(--el-spacing-4);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--el-border-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--el-spacing-3);
  flex-shrink: 0;
}

.stat-icon svg {
  font-size: var(--el-font-size-large);
  color: var(--el-color-white);
}

.bg-primary {
  background-color: var(--el-color-primary);
}

.bg-success {
  background-color: var(--el-color-success);
}

.bg-warning {
  background-color: var(--el-color-warning);
}

.bg-danger {
  background-color: var(--el-color-danger);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: var(--el-font-size-large);
  font-weight: var(--el-font-weight-primary);
  color: var(--el-text-color-primary);
  margin-bottom: var(--el-spacing-1);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--el-font-size-small);
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.chart-card, .info-card {
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  margin-bottom: var(--el-spacing-5);
  border: 1px solid var(--el-border-color-lighter);
}

.chart-card:hover, .info-card:hover {
  box-shadow: var(--el-box-shadow-lighter);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: var(--el-font-weight-primary);
  font-size: var(--el-font-size-base);
  color: var(--el-text-color-primary);
}

.chart-container {
  padding: var(--el-spacing-3);
}

.progress-item {
  margin-bottom: var(--el-spacing-4);
}

.progress-item:last-child {
  margin-bottom: 0;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--el-spacing-2);
  font-size: var(--el-font-size-small);
  color: var(--el-text-color-secondary);
}

.info-content {
  padding: var(--el-spacing-2);
}

/* 响应式调整 */
@media (max-width: 575.98px) {
  .page-header {
    margin-bottom: var(--el-spacing-4);
  }
  
  .page-title {
    font-size: var(--el-font-size-large);
  }
  
  .stat-content {
    padding: var(--el-spacing-3);
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .stat-icon svg {
    font-size: var(--el-font-size-base);
  }
  
  .stat-number {
    font-size: var(--el-font-size-base);
  }
  
  .chart-card, .info-card {
    margin-bottom: var(--el-spacing-4);
  }
  
  .chart-container {
    padding: var(--el-spacing-2);
  }
}

@media (min-width: 576px) and (max-width: 767.98px) {
  .stat-number {
    font-size: var(--el-font-size-base);
  }
  
  .chart-container {
    padding: var(--el-spacing-3);
  }
}
</style>