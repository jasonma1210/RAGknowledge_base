<template>
  <div class="vector-data-page page-container">
    <div class="page-header">
      <h1 class="page-title">向量数据</h1>
      <p class="page-subtitle">查看和管理系统中的向量数据</p>
    </div>

    <el-card class="stats-card modern-card">
      <div class="card-header">
        <h3 class="card-title">向量数据统计</h3>
        <el-button 
          @click="fetchStats" 
          size="small" 
          class="refresh-button"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="stats-content" v-loading="statsLoading">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon size="24" color="var(--color-primary)"><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalCount || 0 }}</div>
                <div class="stat-label">总向量数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon size="24" color="var(--color-success)"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.fileCount || 0 }}</div>
                <div class="stat-label">总文档数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon size="24" color="var(--color-danger)"><Grid /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.dimension || 0 }}</div>
                <div class="stat-label">向量维度</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon size="24" color="var(--color-warning)"><Histogram /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.storageSize || '-' }}</div>
                <div class="stat-label">存储大小</div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <div class="stat-item horizontal">
              <div class="stat-label">集合名称</div>
              <div class="stat-value">{{ stats.collectionName || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-item horizontal">
              <div class="stat-label">最后更新</div>
              <div class="stat-value">{{ formatTime(stats.lastUpdated) || '-' }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-card class="data-card modern-card">
      <div class="card-header">
        <h3 class="card-title">向量数据列表</h3>
        <div class="card-header-right">
          <el-button 
            @click="fetchVectorData" 
            class="refresh-button"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table 
        :data="vectorData" 
        v-loading="dataLoading"
        stripe 
        style="width: 100%"
        class="modern-table"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column prop="id" label="向量ID" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="vector-id">{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chunkIndex" label="分块ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="displayText" label="内容" min-width="300">
          <template #default="scope">
            <div class="content-preview">{{ truncateContent(scope.row.displayText) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="modern-pagination"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  DataLine, 
  Document, 
  Histogram, 
  Grid 
} from '@element-plus/icons-vue'
import { vectorDataAPI } from '@/services/api'

const statsLoading = ref(false)
const dataLoading = ref(false)

// 统计信息
const stats = ref({
  totalCount: 0,
  dimension: 0,
  fileCount: 0,
  collectionName: '',
  lastUpdated: '',
  storageSize: ''
})

// 向量数据列表
const vectorData = ref([])

// 分页信息
const pagination = reactive({
  page: 0,
  size: 10,
  total: 0
})

// 获取统计信息
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const response = await vectorDataAPI.getVectorStats()
    if (response.success) {
      // 适配新的响应数据结构
      stats.value = response.data
    } else {
      ElMessage.error(response.data || '获取统计信息失败')
    }
  } catch (error: any) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败: ' + (error.message || '网络错误'))
  } finally {
    statsLoading.value = false
  }
}

// 获取向量数据
const fetchVectorData = async () => {
  dataLoading.value = true
  try {
    const response = await vectorDataAPI.getVectorData(
      pagination.page,
      pagination.size
    )
    if (response.success) {
      // 适配新的响应数据结构
      vectorData.value = response.data.data
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.data || '获取向量数据失败')
    }
  } catch (error: any) {
    console.error('获取向量数据失败:', error)
    ElMessage.error('获取向量数据失败: ' + (error.message || '网络错误'))
  } finally {
    dataLoading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchVectorData()
}

// 处理当前页变化
const handleCurrentChange = (val: number) => {
  pagination.page = val - 1
  fetchVectorData()
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 截断内容显示
const truncateContent = (content: string) => {
  if (!content) return '-'
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

onMounted(() => {
  fetchStats()
  fetchVectorData()
})
</script>

<style scoped>
.vector-data-page {
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

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: var(--color-bg-secondary);
  border-radius: var(--border-radius-md);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-2);
}

.stat-icon {
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.stat-item.horizontal {
  flex-direction: column;
  text-align: center;
  gap: 8px;
}

.stat-item.horizontal .stat-label {
  font-weight: 500;
}

.stat-item.horizontal .stat-value {
  font-size: 16px;
  margin: 0;
}

.modern-table {
  border-radius: var(--border-radius-md);
}

.vector-id {
  color: var(--color-primary);
  font-weight: 500;
}

.content-preview {
  color: var(--color-text-secondary);
  line-height: 1.5;
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.modern-pagination {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vector-data-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header-right {
    align-self: stretch;
    justify-content: flex-end;
  }
  
  :deep(.el-col) {
    margin-bottom: 12px;
  }
  
  .stat-item.horizontal {
    flex-direction: row;
    justify-content: space-between;
    text-align: left;
  }
}
</style>