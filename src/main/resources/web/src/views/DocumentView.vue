<template>
  <div class="document-management page-container">
    <div class="page-header">
      <h1 class="page-title">文档管理</h1>
      <p class="page-subtitle">管理系统中的所有文档资源</p>
    </div>

    <el-card class="search-card modern-card">
      <div class="card-header">
        <h3 class="card-title">搜索条件</h3>
      </div>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="搜索关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入文档标题或描述" 
            clearable
            class="search-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchDocuments" class="action-button search-button">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch" class="reset-button">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="toolbar-card modern-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="showUploadDialog" class="action-button">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button @click="refreshDocuments" class="refresh-button">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>
    
    <el-card class="table-card modern-card">
      <div class="table-header">
        <h3 class="table-title">文档列表</h3>
        <div class="table-stats">
          <span class="stat-item">总计: {{ pagination.total }} 条</span>
        </div>
      </div>
      <el-table 
        :data="documents" 
        v-loading="loading"
        stripe 
        style="width: 100%"
        class="modern-table"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column prop="title" label="文档标题" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="document-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="fileType" label="文件类型" width="100">
          <template #default="scope">
            <el-tag 
              :type="getFileTagType(scope.row.fileType)" 
              class="file-type-tag"
            >
              {{ scope.row.fileType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.uploadTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="downloadDocument(scope.row)" class="table-action-button download-button">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button size="small" type="danger" @click="deleteDocument(scope.row)" class="table-action-button delete-button">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
    
    <!-- 上传文档对话框 -->
    <el-dialog 
      v-model="uploadDialogVisible" 
      title="上传文档" 
      width="500px"
      class="modern-dialog"
    >
      <el-form :model="uploadForm" label-width="80px" class="upload-form">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            class="upload-area"
          >
            <el-button type="primary" class="upload-button">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
            <template #tip>
              <div class="upload-tip">请选择要上传的文档文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="文档标题">
          <el-input 
            v-model="uploadForm.title" 
            placeholder="请输入文档标题" 
            class="form-input"
          />
        </el-form-item>
        <el-form-item label="文档描述">
          <el-input 
            v-model="uploadForm.description" 
            type="textarea" 
            placeholder="请输入文档描述" 
            :rows="3"
            class="form-textarea"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input 
            v-model="uploadForm.tags" 
            placeholder="请输入标签，多个标签用逗号分隔" 
            class="form-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false" class="cancel-button">取消</el-button>
          <el-button 
            type="primary" 
            @click="uploadDocument" 
            :loading="uploadLoading" 
            class="confirm-button"
          >
            <el-icon v-if="!uploadLoading"><Upload /></el-icon>
            {{ uploadLoading ? '上传中...' : '上传' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Upload, 
  Download, 
  Delete, 
  View 
} from '@element-plus/icons-vue'
import { documentAPI } from '@/services/api'

const loading = ref(false)
const uploadLoading = ref(false)
const uploadDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 上传表单
const uploadForm = reactive({
  file: null as File | null,
  title: '',
  description: '',
  tags: ''
})

// 分页信息
const pagination = reactive({
  page: 0,
  size: 10,
  total: 0
})

// 文档列表
const documents = ref([])

// 格式化文件大小
const formatFileSize = (bytes: number) => {
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

// 格式化状态
const formatStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '等待处理',
    'PROCESSING': '正在处理',
    'COMPLETED': '处理完成',
    'FAILED': '处理失败'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | ''> = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'FAILED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取文件类型标签类型
const getFileTagType = (fileType: string) => {
  const typeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    'PDF': 'danger',
    'DOC': 'primary',
    'DOCX': 'primary',
    'TXT': 'info',
    'MD': 'success',
    'PPT': 'warning',
    'PPTX': 'warning'
  }
  return typeMap[fileType.toUpperCase()] || 'info'
}

// 搜索文档
const searchDocuments = () => {
  pagination.page = 0
  fetchDocuments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  pagination.page = 0
  fetchDocuments()
}

// 刷新文档列表
const refreshDocuments = () => {
  fetchDocuments()
}

// 获取文档列表
const fetchDocuments = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword
    }
    
    const response = await documentAPI.getDocuments(params)
    if (response.success) {
      // 适配新的响应数据结构
      documents.value = response.data.data
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.data || '获取文档列表失败')
    }
  } catch (error: any) {
    console.error('获取文档列表失败:', error)
    ElMessage.error('获取文档列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchDocuments()
}

// 处理当前页变化
const handleCurrentChange = (val: number) => {
  pagination.page = val - 1
  fetchDocuments()
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialogVisible.value = true
  // 重置表单
  uploadForm.file = null
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.tags = ''
}

// 处理文件选择
const handleFileChange = (file: any) => {
  uploadForm.file = file.raw
}

// 上传文档
const uploadDocument = async () => {
  if (!uploadForm.file) {
    ElMessage.error('请选择要上传的文件')
    return
  }
  
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    if (uploadForm.title) formData.append('title', uploadForm.title)
    if (uploadForm.description) formData.append('description', uploadForm.description)
    if (uploadForm.tags) formData.append('tags', uploadForm.tags)
    
    const response = await documentAPI.uploadDocument(formData)
    if (response.success) {
      ElMessage.success('文档上传成功')
      uploadDialogVisible.value = false
      fetchDocuments() // 刷新文档列表
    } else {
      ElMessage.error(response.data || '文档上传失败')
    }
  } catch (error: any) {
    console.error('文档上传失败:', error)
    ElMessage.error('文档上传失败: ' + (error.message || '网络错误'))
  } finally {
    uploadLoading.value = false
  }
}

// 下载文档
const downloadDocument = async (row: any) => {
  try {
    const blob = await documentAPI.downloadDocument(row.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('文档下载成功')
  } catch (error: any) {
    console.error('文档下载失败:', error)
    ElMessage.error('文档下载失败: ' + (error.message || '网络错误'))
  }
}

// 删除文档
const deleteDocument = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除文档 "${row.title}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'modern-confirm-dialog'
    }
  ).then(async () => {
    try {
      const response = await documentAPI.deleteDocument(row.id)
      if (response.success) {
        ElMessage.success('文档删除成功')
        fetchDocuments() // 刷新文档列表
      } else {
        ElMessage.error(response.data || '文档删除失败')
      }
    } catch (error: any) {
      console.error('文档删除失败:', error)
      ElMessage.error('文档删除失败: ' + (error.message || '网络错误'))
    }
  }).catch(() => {
    // 用户取消删除
  })
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.document-management {
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
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 16px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: end;
}

.search-input {
  width: 240px;
}

.action-button {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.action-button:hover {
  background-color: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.search-button {
  background-color: black;
  border-color: black;
  color: white;
}

.search-button:hover {
  background-color: #333;
  border-color: #333;
}

.reset-button {
  border-color: var(--color-border);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 12px;
}

.refresh-button {
  border-color: var(--color-border);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0;
}

.table-stats {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.stat-item {
  margin-left: 16px;
}

.modern-table {
  border-radius: var(--border-radius-md);
}

.document-title {
  color: var(--color-primary);
  font-weight: 500;
}

.file-type-tag,
.status-tag {
  margin: 0;
}

.table-action-button {
  padding: 6px 10px;
  font-size: 12px;
  margin-right: 6px;
}

.download-button {
  color: var(--color-success);
  border-color: var(--color-success-light);
}

.download-button:hover {
  background-color: var(--color-success-light);
  color: white;
}

.delete-button {
  color: var(--color-danger);
  border-color: var(--color-danger-light);
}

.delete-button:hover {
  background-color: var(--color-danger-light);
  color: white;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.modern-pagination {
  padding: 16px 0;
}

.modern-dialog {
  border-radius: var(--border-radius-lg);
}

.upload-form {
  padding: 20px 0;
}

.upload-area {
  width: 100%;
}

.upload-button {
  width: 100%;
  background-color: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.upload-button:hover {
  background-color: var(--color-bg-hover);
  border-color: var(--color-border-hover);
}

.upload-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 6px;
}

.form-input,
.form-textarea {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 20px 20px;
}

.cancel-button {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.confirm-button {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.confirm-button:hover {
  background-color: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .document-management {
    padding: 16px 12px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .table-stats {
    align-self: flex-end;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .el-table__cell) {
    padding: 8px 0;
  }
}
</style>