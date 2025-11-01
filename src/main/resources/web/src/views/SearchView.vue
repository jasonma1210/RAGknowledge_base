<template>
  <div class="search-page page-container">
    <div class="page-header">
      <h1 class="page-title">知识搜索</h1>
      <p class="page-subtitle">基于语义的智能搜索，快速找到您需要的知识</p>
    </div>

    <el-card class="search-card modern-card">
      <div class="search-form-container">
        <el-form :model="searchForm" class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.query"
              placeholder="请输入搜索关键词"
              size="large"
              @keyup.enter="performSearch"
              class="search-input"
              clearable
            >
              <template #prepend>
                <el-select 
                  v-model="advancedOptions.searchType" 
                  style="width: 120px"
                  class="search-type-select"
                >
                  <el-option label="语义搜索" value="SEMANTIC" />
                  <el-option label="关键词搜索" value="KEYWORD" />
                  <el-option label="混合搜索" value="HYBRID" />
                </el-select>
              </template>
              <template #append>
                <el-button 
                  type="primary" 
                  @click="performSearch" 
                  :loading="searchLoading"
                  class="search-button"
                >
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <el-collapse v-model="activeCollapse" class="advanced-options-collapse">
        <el-collapse-item title="高级搜索选项" name="advanced">
          <el-form :model="advancedOptions" label-width="100px" label-position="left" class="advanced-options">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="最大结果数">
                  <el-slider 
                    v-model="advancedOptions.maxResults" 
                    :min="1" 
                    :max="50" 
                    show-input 
                    :show-input-controls="false"
                    class="slider-input"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最小相似度">
                  <el-slider 
                    v-model="advancedOptions.minScore" 
                    :min="0" 
                    :max="1" 
                    :step="0.01" 
                    show-input 
                    :show-input-controls="false"
                    class="slider-input"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <div class="advanced-actions">
                  <el-button @click="resetOptions" class="reset-button">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>
    
    <el-card class="results-card modern-card" v-if="searchResults.length > 0">
      <div class="card-header">
        <h3 class="card-title">搜索结果</h3>
        <div class="card-header-right">
          <span class="result-stats">共 {{ pagination.total }} 条结果</span>
          <span class="search-time">耗时: {{ searchTime }}ms</span>
        </div>
      </div>
      
      <div class="results-list">
        <div 
          v-for="(result, index) in searchResults" 
          :key="index" 
          class="result-item"
        >
          <div class="result-header">
            <h4 class="result-title">{{ (result as any).title }}</h4>
            <div class="result-tags">
              <el-tag v-if="(result as any).source && (result as any).source !== 'Unknown'" size="small" type="primary" class="source-tag">
                {{ (result as any).source }}
              </el-tag>
              <el-tag size="small" type="success" class="score-tag">
                相似度: {{ ((result as any).score * 100).toFixed(2) }}%
              </el-tag>
            </div>
          </div>
          <div class="result-content">
            {{ (result as any).content }}
          </div>
          <div class="result-meta">
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ (result as any).position }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime((result as any).timestamp) }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
          background
          class="modern-pagination"
        />
      </div>
    </el-card>
    
    <el-card class="no-results-card modern-card" v-else-if="!searchLoading && searchPerformed">
      <el-empty description="暂无搜索结果" />
    </el-card>
    
    <el-card class="welcome-card modern-card" v-else>
      <div class="welcome-content">
        <div class="welcome-icon">
          <el-icon size="48" color="var(--color-primary)"><Search /></el-icon>
        </div>
        <h3 class="welcome-title">RAG知识库搜索</h3>
        <p class="welcome-text">请输入关键词开始搜索，系统将基于语义理解为您找到最相关的结果</p>
        <div class="welcome-tips">
          <h4>搜索技巧：</h4>
          <ul>
            <li>使用具体关键词可以获得更精确的结果</li>
            <li>尝试使用自然语言描述您的问题</li>
            <li>结合多个关键词可以提高搜索准确性</li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Document, 
  Clock, 
  Refresh 
} from '@element-plus/icons-vue'
import { searchAPI } from '@/services/api'

const searchLoading = ref(false)
const searchPerformed = ref(false)
const searchTime = ref(0)
const activeCollapse = ref([]) // 控制折叠面板展开状态

// 搜索表单
const searchForm = reactive({
  query: ''
})

// 高级选项
const advancedOptions = reactive({
  searchType: 'SEMANTIC',
  maxResults: 10,
  minScore: 0.7
})

// 搜索结果
const searchResults = ref<any[]>([])

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 执行搜索
const performSearch = async () => {
  if (!searchForm.query.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searchLoading.value = true
  searchPerformed.value = true
  const startTime = Date.now()
  
  try {
    const request = {
      query: searchForm.query,
      searchType: advancedOptions.searchType,
      maxResults: advancedOptions.maxResults,
      minScore: advancedOptions.minScore
    }
    
    const response: any = await searchAPI.search(request)
    if (response.success) {
      // 适配新的响应数据结构
      searchResults.value = response.data.data
      pagination.total = response.data.total
      searchTime.value = response.data.searchTime || Date.now() - startTime
    } else {
      ElMessage.error(response.data || '搜索失败')
    }
  } catch (error: any) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败: ' + (error.message || '网络错误'))
  } finally {
    searchLoading.value = false
  }
}

// 处理分页变化
const handlePageChange = (val: number) => {
  pagination.page = val
  // 这里可以实现分页搜索逻辑
  performSearch()
}

// 重置选项
const resetOptions = () => {
  advancedOptions.searchType = 'SEMANTIC'
  advancedOptions.maxResults = 10
  advancedOptions.minScore = 0.7
}
</script>

<style scoped>
.search-page {
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
  gap: 16px;
  align-items: center;
}

.result-stats,
.search-time {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.search-form-container {
  padding: 20px 0;
}

.search-input {
  border-radius: var(--border-radius-md);
}

.search-type-select {
  border-radius: var(--border-radius-md) 0 0 var(--border-radius-md);
}

.search-button {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
  border-radius: 0 var(--border-radius-md) var(--border-radius-md) 0;
}

.search-button:hover {
  background-color: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.advanced-options-collapse {
  margin-top: 20px;
}

:deep(.el-collapse-item__header) {
  font-weight: 500;
  color: var(--color-text-primary);
  border-bottom-color: var(--color-border);
}

.advanced-options {
  padding: 20px 0 10px;
}

.slider-input {
  width: 100%;
}

.advanced-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}

.reset-button {
  border-color: var(--color-border);
}

.results-list {
  padding: 10px 0;
}

.result-item {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.2s;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background-color: var(--color-bg-hover);
  border-radius: var(--border-radius-md);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.result-title {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 500;
  flex: 1;
}

.result-tags {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}

.source-tag,
.score-tag {
  margin: 0;
}

.result-content {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  font-size: 14px;
}

.result-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.modern-pagination {
  padding: 16px 0;
}

.no-results-card {
  text-align: center;
  padding: 40px 0;
}

.welcome-card {
  text-align: center;
  padding: 40px 20px;
}

.welcome-content {
  max-width: 600px;
  margin: 0 auto;
}

.welcome-icon {
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}

.welcome-text {
  color: var(--color-text-secondary);
  margin-bottom: 24px;
  line-height: 1.6;
}

.welcome-tips {
  text-align: left;
  background-color: var(--color-bg-secondary);
  padding: 20px;
  border-radius: var(--border-radius-md);
}

.welcome-tips h4 {
  margin-top: 0;
  color: var(--color-text-primary);
}

.welcome-tips ul {
  padding-left: 20px;
  margin: 10px 0;
}

.welcome-tips li {
  margin-bottom: 8px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .result-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .result-tags {
    margin-left: 0;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-header-right {
    align-self: flex-end;
  }
  
  .advanced-options .el-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .advanced-options .el-col {
    width: 100%;
  }
  
  .advanced-actions {
    justify-content: flex-start;
  }
}
</style>