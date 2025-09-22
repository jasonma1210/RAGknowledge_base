<template>
  <div class="system-monitor-page page-container">
    <div class="page-header">
      <h1 class="page-title">系统监控</h1>
      <p class="page-subtitle">实时监控系统健康状态和性能指标</p>
    </div>

    <el-card class="health-card modern-card">
      <div class="card-header">
        <h3 class="card-title">系统健康状态</h3>
        <el-button 
          @click="fetchHealth" 
          size="small" 
          class="refresh-button"
          :loading="healthLoading"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="health-content">
        <div class="health-status">
          <el-tag 
            :type="getHealthStatusType(healthStatus.status)" 
            size="large"
            class="health-status-tag"
          >
            <el-icon v-if="healthStatus.status === 'UP'"><SuccessFilled /></el-icon>
            <el-icon v-else-if="healthStatus.status === 'DOWN'"><CircleCloseFilled /></el-icon>
            <el-icon v-else><InfoFilled /></el-icon>
            {{ formatHealthStatus(healthStatus.status) }}
          </el-tag>
        </div>
        
        <el-row :gutter="20" class="health-components">
          <el-col 
            v-for="(component, name) in healthStatus.components" 
            :key="name" 
            :span="12"
            class="component-col"
          >
            <el-card 
              class="component-card modern-card" 
              :class="`status-${component.status?.toLowerCase()}`"
            >
              <div class="component-header">
                <h4 class="component-title">{{ formatComponentName(name) }}</h4>
                <el-tag 
                  :type="getHealthStatusType(component.status)" 
                  size="small"
                  class="component-status-tag"
                >
                  {{ formatHealthStatus(component.status) }}
                </el-tag>
              </div>
              <div class="component-details">
                <div 
                  v-for="(value, key) in component.details || {}" 
                  :key="key" 
                  class="detail-item"
                >
                  <span class="detail-key">{{ formatDetailKey(key) }}:</span>
                  <span class="detail-value">{{ value }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-card class="metrics-card modern-card">
      <div class="card-header">
        <h3 class="card-title">系统指标</h3>
        <div class="card-header-right">
          <el-select 
            v-model="selectedMetric" 
            @change="fetchMetricDetails" 
            style="width: 200px;"
            class="metric-select"
          >
            <el-option 
              v-for="metric in availableMetrics" 
              :key="metric" 
              :label="metric" 
              :value="metric"
            />
          </el-select>
          <el-button 
            @click="fetchMetrics" 
            class="refresh-button"
            :loading="metricsLoading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <div class="metrics-content">
        <div v-if="selectedMetricDetails" class="metric-details">
          <el-descriptions :column="1" border class="modern-descriptions">
            <el-descriptions-item label="指标名称">{{ selectedMetricDetails.name }}</el-descriptions-item>
            <el-descriptions-item label="描述">{{ selectedMetricDetails.description }}</el-descriptions-item>
            <el-descriptions-item label="基础单位">{{ selectedMetricDetails.baseUnit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="测量值">
              <div v-for="(measurement, index) in selectedMetricDetails.measurements" :key="index" class="measurement-item">
                <span class="measurement-statistic">{{ measurement.statistic }}:</span>
                <span class="measurement-value">{{ measurement.value }}</span>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <el-empty v-else description="请选择要查看的指标" />
      </div>
    </el-card>
    
    <el-card class="info-card modern-card">
      <div class="card-header">
        <h3 class="card-title">系统信息</h3>
        <div class="card-header-right">
          <el-button 
            @click="fetchSystemInfo" 
            class="refresh-button"
            :loading="infoLoading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <div class="info-content">
        <el-tabs v-model="activeInfoTab" class="modern-tabs">
          <el-tab-pane label="应用信息" name="application">
            <el-descriptions :column="1" border class="modern-descriptions">
              <el-descriptions-item label="应用名称">{{ systemInfo.application?.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ systemInfo.application?.version || '-' }}</el-descriptions-item>
              <el-descriptions-item label="描述">{{ systemInfo.application?.description || '-' }}</el-descriptions-item>
              <el-descriptions-item label="启动时间">{{ formatTime(systemInfo.application?.startTime) || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="构建信息" name="build">
            <el-descriptions :column="1" border class="modern-descriptions">
              <el-descriptions-item label="Java版本">{{ systemInfo.build?.['java.version'] || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Java厂商">{{ systemInfo.build?.['java.vendor'] || '-' }}</el-descriptions-item>
              <el-descriptions-item label="操作系统">{{ systemInfo.build?.['os.name'] || '-' }}</el-descriptions-item>
              <el-descriptions-item label="系统版本">{{ systemInfo.build?.['os.version'] || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="系统资源" name="system">
            <el-descriptions :column="1" border class="modern-descriptions">
              <el-descriptions-item label="最大内存">{{ formatMemory(systemInfo.system?.memory?.maxMemoryMB) }}</el-descriptions-item>
              <el-descriptions-item label="总内存">{{ formatMemory(systemInfo.system?.memory?.totalMemoryMB) }}</el-descriptions-item>
              <el-descriptions-item label="已用内存">{{ formatMemory(systemInfo.system?.memory?.usedMemoryMB) }}</el-descriptions-item>
              <el-descriptions-item label="可用内存">{{ formatMemory(systemInfo.system?.memory?.freeMemoryMB) }}</el-descriptions-item>
              <el-descriptions-item label="可用处理器">{{ systemInfo.system?.availableProcessors || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  SuccessFilled, 
  CircleCloseFilled, 
  InfoFilled 
} from '@element-plus/icons-vue'
import { healthAPI } from '@/services/api'

const healthLoading = ref(false)
const metricsLoading = ref(false)
const infoLoading = ref(false)

const activeInfoTab = ref('application')

// 健康状态
const healthStatus = ref({
  status: 'UNKNOWN',
  components: {} as Record<string, any>
})

// 可用指标
const availableMetrics = ref<string[]>([])

// 选中的指标
const selectedMetric = ref('')

// 选中的指标详情
const selectedMetricDetails = ref<any>(null)

// 系统信息
const systemInfo = ref({
  application: {} as Record<string, any>,
  build: {} as Record<string, any>,
  system: {} as Record<string, any>
})

// 获取健康状态
const fetchHealth = async () => {
  healthLoading.value = true
  try {
    const response: any = await healthAPI.getHealth()
    if (response.success !== false) {
      // 健康检查接口返回的是直接的数据结构，不是包装后的ApiResponse
      healthStatus.value = {
        status: response.status || 'UNKNOWN',
        components: response.components || {}
      }
    } else {
      ElMessage.error(response.data || '获取健康状态失败')
    }
  } catch (error: any) {
    console.error('获取健康状态失败:', error)
    ElMessage.error('获取健康状态失败: ' + (error.message || '网络错误'))
  } finally {
    healthLoading.value = false
  }
}

// 获取指标列表
const fetchMetrics = async () => {
  metricsLoading.value = true
  try {
    const response: any = await healthAPI.getMetrics()
    if (response.success !== false) {
      // 指标接口返回的是直接的数据结构
      if (response.names) {
        availableMetrics.value = response.names
      }
    } else {
      ElMessage.error(response.data || '获取指标列表失败')
    }
  } catch (error: any) {
    console.error('获取指标列表失败:', error)
    ElMessage.error('获取指标列表失败: ' + (error.message || '网络错误'))
  } finally {
    metricsLoading.value = false
  }
}

// 获取指标详情
const fetchMetricDetails = async (metricName: string) => {
  if (!metricName) {
    selectedMetricDetails.value = null
    return
  }
  
  metricsLoading.value = true
  try {
    const response: any = await healthAPI.getMetrics(metricName)
    if (response.success !== false) {
      selectedMetricDetails.value = response
    } else {
      ElMessage.error(response.data || '获取指标详情失败')
      selectedMetricDetails.value = null
    }
  } catch (error: any) {
    console.error('获取指标详情失败:', error)
    ElMessage.error('获取指标详情失败: ' + (error.message || '网络错误'))
    selectedMetricDetails.value = null
  } finally {
    metricsLoading.value = false
  }
}

// 获取系统信息
const fetchSystemInfo = async () => {
  infoLoading.value = true
  try {
    const response: any = await healthAPI.getInfo()
    if (response.success !== false) {
      // 系统信息接口返回的是直接的数据结构
      systemInfo.value = {
        application: response.application || {},
        build: response.build || {},
        system: response.system || {}
      }
    } else {
      ElMessage.error(response.data || '获取系统信息失败')
    }
  } catch (error: any) {
    console.error('获取系统信息失败:', error)
    ElMessage.error('获取系统信息失败: ' + (error.message || '网络错误'))
  } finally {
    infoLoading.value = false
  }
}

// 获取健康状态类型
const getHealthStatusType = (status: string) => {
  const typeMap: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    'UP': 'success',
    'DOWN': 'danger',
    'OUT_OF_SERVICE': 'warning',
    'UNKNOWN': 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化健康状态
const formatHealthStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'UP': '运行中',
    'DOWN': '故障',
    'OUT_OF_SERVICE': '停止服务',
    'UNKNOWN': '未知'
  }
  return statusMap[status] || status
}

// 格式化组件名称
const formatComponentName = (name: string) => {
  const nameMap: Record<string, string> = {
    'database': '数据库',
    'redis': 'Redis缓存',
    'storage': '存储系统',
    'diskSpace': '磁盘空间'
  }
  return nameMap[name] || name
}

// 格式化详情键名
const formatDetailKey = (key: string | number) => {
  const keyStr = String(key)
  const keyMap: Record<string, string> = {
    'database': '数据库',
    'status': '状态',
    'validationQuery': '验证查询',
    'redis': 'Redis',
    'response': '响应',
    'storage': '存储',
    'freeSpaceGB': '可用空间(GB)',
    'usagePercent': '使用率',
    'total': '总量',
    'free': '可用',
    'threshold': '阈值'
  }
  return keyMap[keyStr] || keyStr
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化内存大小
const formatMemory = (memoryMB: number) => {
  if (memoryMB === undefined || memoryMB === null) return '-'
  return memoryMB + ' MB'
}

onMounted(() => {
  fetchHealth()
  fetchMetrics()
  fetchSystemInfo()
})
</script>

<style scoped>
.system-monitor-page {
  padding: 20px;
  background-color: var(--color-bg-primary);
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.modern-card {
  margin-bottom: 20px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-2);
  border: 1px solid var(--color-border);
  transition: box-shadow 0.3s ease;
}

.modern-card:hover {
  box-shadow: var(--shadow-3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.card-header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.refresh-button {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.refresh-button:hover {
  background-color: var(--color-bg-hover);
  border-color: var(--color-border-hover);
}

.health-status {
  text-align: center;
  margin-bottom: 30px;
}

.health-status-tag {
  padding: 12px 24px;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.health-components {
  margin-top: 20px;
}

.component-col {
  margin-bottom: 20px;
}

.component-card {
  height: 100%;
  border-radius: var(--border-radius-md);
}

.component-card.status-up {
  border-left: 4px solid var(--color-success);
}

.component-card.status-down {
  border-left: 4px solid var(--color-danger);
}

.component-card.status-out_of_service {
  border-left: 4px solid var(--color-warning);
}

.component-card.status-unknown {
  border-left: 4px solid var(--color-info);
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.component-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.component-status-tag {
  margin: 0;
}

.component-details {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 6px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-key {
  font-weight: 500;
  color: var(--color-text-secondary);
}

.detail-value {
  color: var(--color-text-primary);
  font-weight: 500;
}

.metric-select {
  border-radius: var(--border-radius-md);
}

.modern-descriptions {
  border-radius: var(--border-radius-md);
}

.modern-descriptions :deep(.el-descriptions__header) {
  background-color: var(--color-bg-secondary);
}

.modern-descriptions :deep(.el-descriptions__label) {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-weight: 500;
}

.modern-descriptions :deep(.el-descriptions__content) {
  color: var(--color-text-primary);
}

.measurement-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.measurement-statistic {
  font-weight: 500;
  color: var(--color-text-secondary);
}

.measurement-value {
  font-weight: 600;
  color: var(--color-text-primary);
}

.modern-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border);
}

.modern-tabs :deep(.el-tabs__item) {
  color: var(--color-text-secondary);
}

.modern-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

.modern-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

.info-content {
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-monitor-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header-right {
    align-self: stretch;
    justify-content: space-between;
  }
  
  .component-col {
    width: 100%;
  }
  
  .health-status-tag {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>