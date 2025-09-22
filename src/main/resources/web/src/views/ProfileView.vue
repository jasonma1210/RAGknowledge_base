<template>
  <div class="profile-page page-container">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理您的个人信息和账户设置</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="info-card modern-card">
          <div class="card-header">
            <h3 class="card-title">基本信息</h3>
            <el-button 
              @click="fetchUserInfo" 
              size="small" 
              class="refresh-button"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <el-form :model="userInfo" label-width="120px" class="info-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户ID">
                  <el-input v-model="userInfo.id" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用户等级">
                  <el-input :value="formatUserLevel(userInfo.level)" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="注册时间">
                  <el-input :value="formatTime(userInfo.gmtCreate)" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最后登录时间">
                  <el-input :value="formatTime(userInfo.lastLoginTime)" readonly class="readonly-input" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        
        <el-card class="password-card modern-card">
          <div class="card-header">
            <h3 class="card-title">修改密码</h3>
          </div>
          
          <el-form 
            :model="passwordForm" 
            :rules="passwordRules" 
            ref="passwordFormRef" 
            label-width="120px" 
            class="password-form"
            @submit.prevent="changePassword"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                show-password 
                placeholder="请输入原密码"
                class="form-input"
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password 
                placeholder="请输入新密码"
                class="form-input"
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password 
                placeholder="请再次输入新密码"
                class="form-input"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                :loading="passwordLoading"
                @click="changePassword"
                class="submit-button"
              >
                <el-icon v-if="!passwordLoading"><Lock /></el-icon>
                {{ passwordLoading ? '保存中...' : '保存' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="storage-card modern-card">
          <div class="card-header">
            <h3 class="card-title">存储使用情况</h3>
          </div>
          
          <div class="storage-info">
            <div class="storage-stats">
              <div class="storage-number">{{ formatStorage(userInfo.usedStorage) }}</div>
              <div class="storage-label">已使用</div>
            </div>
            
            <div class="storage-stats">
              <div class="storage-number">{{ formatStorage(userInfo.storageQuota) }}</div>
              <div class="storage-label">总配额</div>
            </div>
          </div>
          
          <div class="storage-progress">
            <el-progress 
              :percentage="calculateStoragePercentage()" 
              :status="getStorageStatus()"
              :stroke-width="10"
              striped
              striped-flow
              class="storage-progress-bar"
            />
            <div class="progress-text">
              {{ calculateStoragePercentage() }}% 已使用
            </div>
          </div>
          
          <div class="storage-details">
            <el-descriptions :column="1" size="small" class="modern-descriptions">
              <el-descriptions-item label="用户等级">
                {{ formatUserLevel(userInfo.level) }}
              </el-descriptions-item>
              <el-descriptions-item label="文档数量限制">
                {{ userInfo.level === 1 ? '无限制' : '无限制' }}
              </el-descriptions-item>
              <el-descriptions-item label="上传频率限制">
                {{ userInfo.level === 1 ? '30次/分钟' : '10次/分钟' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
        
        <el-card class="account-actions modern-card">
          <div class="card-header">
            <h3 class="card-title">账户操作</h3>
          </div>
          
          <div class="actions-content">
            <el-button 
              type="danger" 
              plain
              @click="logout"
              class="action-button logout-button"
            >
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  Lock, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { authAPI } from '@/services/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const passwordLoading = ref(false)

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  email: '',
  level: 0,
  storageQuota: 0,
  usedStorage: 0,
  lastLoginTime: '',
  gmtCreate: '',
  gmtModified: '',
  isDeleted: 0
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 密码表单引用
const passwordFormRef = ref<any>(null)

// 格式化用户等级
const formatUserLevel = (level: number) => {
  return level === 1 ? '进阶用户' : '普通用户'
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化存储大小
const formatStorage = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 计算存储使用百分比
const calculateStoragePercentage = () => {
  if (userInfo.value.storageQuota === 0) return 0
  return Math.round((userInfo.value.usedStorage / userInfo.value.storageQuota) * 100)
}

// 获取存储状态
const getStorageStatus = () => {
  const percentage = calculateStoragePercentage()
  if (percentage >= 90) return 'exception'
  if (percentage >= 70) return 'warning'
  return 'success'
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 先从本地存储获取
    const localUserInfo = authAPI.getLocalUserInfo()
    if (localUserInfo) {
      userInfo.value = localUserInfo
    }
    
    // 再从接口获取最新信息
    const response = await authAPI.getProfile()
    if (response.success) {
      // 适配新的响应数据结构
      userInfo.value = { ...userInfo.value, ...response.data }
      // 更新本地存储
      authAPI.setLocalUserInfo(userInfo.value)
    } else {
      ElMessage.error(response.data || '获取用户信息失败')
    }
  } catch (error: any) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败: ' + (error.message || '网络错误'))
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    passwordLoading.value = true
    try {
      const response = await authAPI.changePassword(
        passwordForm.oldPassword,
        passwordForm.newPassword
      )
      if (response.success) {
        ElMessage.success('密码修改成功')
        // 清空密码表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } else {
        ElMessage.error(response.data || '密码修改失败')
      }
    } catch (error: any) {
      console.error('密码修改失败:', error)
      ElMessage.error('密码修改失败: ' + (error.message || '网络错误'))
    } finally {
      passwordLoading.value = false
    }
  })
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '退出登录',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 清除本地存储的用户信息
    authAPI.clearLocalUserInfo()
    // 跳转到登录页面
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {
    // 用户取消退出
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
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

.refresh-button {
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.refresh-button:hover {
  background-color: var(--color-bg-hover);
  border-color: var(--color-border-hover);
}

.readonly-input {
  background-color: var(--color-bg-secondary);
  border-color: var(--color-border);
}

.form-input {
  border-radius: var(--border-radius-md);
}

.submit-button {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.submit-button:hover {
  background-color: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.storage-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.storage-stats {
  text-align: center;
  flex: 1;
  padding: 16px;
  background-color: var(--color-bg-secondary);
  border-radius: var(--border-radius-md);
  margin: 0 8px;
}

.storage-stats:first-child {
  margin-left: 0;
}

.storage-stats:last-child {
  margin-right: 0;
}

.storage-number {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.storage-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

.storage-progress {
  margin: 20px 0 30px;
}

.storage-progress-bar {
  border-radius: var(--border-radius-md);
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
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

.account-actions {
  margin-bottom: 0;
}

.actions-content {
  padding: 20px 0 10px;
}

.action-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-button {
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.logout-button:hover {
  background-color: var(--color-danger-light);
  border-color: var(--color-danger);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 16px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .el-row {
    flex-direction: column;
    gap: 20px;
  }
  
  .el-col {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .storage-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .storage-stats {
    margin: 0;
  }
}
</style>
