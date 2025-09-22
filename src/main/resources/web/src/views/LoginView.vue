<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <div class="logo">
            <h1>RAG知识库系统</h1>
          </div>
          <p class="subtitle">基于检索增强生成的智能知识库</p>
        </div>
        
        <!-- 切换按钮 -->
        <div class="toggle-buttons">
          <el-button 
            :class="['toggle-btn', { active: activeTab === 'login' }]"
            @click="switchToLogin"
            size="large"
            round
          >
            登录
          </el-button>
          <el-button 
            :class="['toggle-btn', { active: activeTab === 'register' }]"
            @click="switchToRegister"
            size="large"
            round
          >
            注册
          </el-button>
        </div>
        
        <!-- 登录表单 -->
        <div v-show="activeTab === 'login'" class="form-wrapper">
          <el-form 
            :model="loginForm" 
            :rules="loginRules" 
            ref="loginFormRef" 
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名" 
                size="large"
                prefix-icon="User"
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                size="large"
                prefix-icon="Lock"
                @keyup.enter="handleLogin"
                show-password
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                class="login-button"
                :loading="loginLoading"
                @click="handleLogin"
                round
                block
              >
                登录
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <p>还没有账号？ <el-button type="text" @click="switchToRegister">立即注册</el-button></p>
            </div>
          </el-form>
        </div>
        
        <!-- 注册表单 -->
        <div v-show="activeTab === 'register'" class="form-wrapper">
          <el-form 
            :model="registerForm" 
            :rules="registerRules" 
            ref="registerFormRef" 
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名(3-50字符，仅支持字母数字下划线)" 
                size="large"
                prefix-icon="User"
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input 
                v-model="registerForm.email" 
                placeholder="请输入邮箱(可选)" 
                size="large"
                prefix-icon="Message"
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="请输入密码(6-20字符)" 
                size="large"
                prefix-icon="Lock"
                show-password
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password" 
                placeholder="请确认密码" 
                size="large"
                prefix-icon="Lock"
                show-password
                class="underline-input"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                class="register-button"
                :loading="registerLoading"
                @click="handleRegister"
                round
                block
              >
                注册
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <p>已有账号？ <el-button type="text" @click="switchToLogin">立即登录</el-button></p>
            </div>
          </el-form>
        </div>
      </div>
      
      <div class="login-footer">
        <p>© 2025 RAG知识库系统. 保留所有权利.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { authAPI } from '@/services/api'

const router = useRouter()
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

// 切换到登录
const switchToLogin = () => {
  activeTab.value = 'login'
}

// 切换到注册
const switchToRegister = () => {
  activeTab.value = 'register'
}

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20字符', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名仅支持字母数字下划线', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度应为3-50字符', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 登录表单引用
const loginFormRef = ref<any>(null)

// 注册表单引用
const registerFormRef = ref<any>(null)

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loginLoading.value = true
    try {
      const response: any = await authAPI.login(loginForm.username, loginForm.password)
      console.log('登录响应:', response)
      if (response.success) {
        // 适配新的响应数据结构
        localStorage.setItem('token', response.data.token)
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken)
        }
        
        // 保存用户信息
        if (response.data.user) {
          localStorage.setItem('userInfo', JSON.stringify(response.data.user))
        }
        ElMessage.success('登录成功')
        // 登录成功后直接跳转，不使用setTimeout
        router.push('/dashboard')
      } else {
        ElMessage.error(response.data || '登录失败')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败: ' + (error.message || '网络错误'))
    } finally {
      loginLoading.value = false
    }
  })
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    registerLoading.value = true
    try {
      const response: any = await authAPI.register(
        registerForm.username, 
        registerForm.password, 
        registerForm.email
      )
      console.log('注册响应:', response)
      if (response.success) {
        ElMessage.success('注册成功')
        // 切换到登录标签页
        activeTab.value = 'login'
        // 清空注册表单
        registerForm.username = ''
        registerForm.email = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
      } else {
        ElMessage.error(response.data || '注册失败')
      }
    } catch (error: any) {
      console.error('注册失败:', error)
      ElMessage.error('注册失败: ' + (error.message || '网络错误'))
    } finally {
      registerLoading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f2f5 0%, #e4e7f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--el-spacing-4);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.05) 0%, transparent 70%);
  z-index: 0;
}

.login-container {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
}

.login-box {
  background: var(--el-bg-color-overlay);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow);
  padding: var(--el-spacing-6);
  transition: var(--el-transition-duration) var(--el-transition-function-ease-in-out-bezier);
  border: 1px solid var(--el-border-color-lighter);
  position: relative;
  overflow: hidden;
}

.login-box:hover {
  box-shadow: var(--el-box-shadow-dark);
}

.login-box::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--el-color-primary), var(--el-color-primary-light-3));
}

.login-header {
  text-align: center;
  margin-bottom: var(--el-spacing-5);
}

.logo h1 {
  margin: 0 0 var(--el-spacing-2) 0;
  color: var(--el-color-primary);
  font-size: var(--el-font-size-extra-large);
  font-weight: var(--el-font-weight-primary);
  letter-spacing: 1px;
}

.subtitle {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: var(--el-font-size-small);
}

/* 切换按钮样式 */
.toggle-buttons {
  display: flex;
  gap: var(--el-spacing-3);
  margin-bottom: var(--el-spacing-5);
  justify-content: center;
}

.toggle-btn {
  flex: 1;
  font-weight: var(--el-font-weight-primary);
  border: 1px solid var(--el-border-color-light);
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light);
  transition: all 0.3s;
}

.toggle-btn:hover {
  border-color: var(--el-color-primary-light-3);
  color: var(--el-color-primary);
  background-color: var(--el-bg-color-overlay);
}

.toggle-btn.active {
  border-color: var(--el-color-primary);
  color: var(--el-color-white);
  background-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.form-wrapper {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-form,
.register-form {
  margin-top: var(--el-spacing-2);
}

.login-form :deep(.el-form-item),
.register-form :deep(.el-form-item) {
  margin-bottom: var(--el-spacing-4);
}

.login-form :deep(.el-form-item__label),
.register-form :deep(.el-form-item__label) {
  display: none;
}

/* 下划线输入框样式 */
.underline-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: none !important;
  border-radius: 0 !important;
  border-bottom: 1px solid var(--el-border-color) !important;
  padding: var(--el-spacing-2) 0 !important;
  background: transparent !important;
  transition: border-color 0.3s ease;
}

.underline-input :deep(.el-input__wrapper:hover) {
  box-shadow: none !important;
  border-bottom: 1px solid var(--el-color-primary) !important;
}

.underline-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none !important;
  border-bottom: 2px solid var(--el-color-primary) !important;
}

.underline-input :deep(.el-input__prefix) {
  margin-right: var(--el-spacing-2);
}

.login-button,
.register-button {
  width: 100%;
  height: 40px;
  font-size: var(--el-font-size-base);
  font-weight: var(--el-font-weight-primary);
  margin-top: var(--el-spacing-2);
}

.form-footer {
  text-align: center;
  margin-top: var(--el-spacing-3);
}

.form-footer p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: var(--el-font-size-small);
}

.form-footer .el-button {
  font-size: var(--el-font-size-small);
  font-weight: var(--el-font-weight-primary);
}

.login-footer {
  text-align: center;
  margin-top: var(--el-spacing-4);
}

.login-footer p {
  margin: 0;
  color: var(--el-text-color-placeholder);
  font-size: var(--el-font-size-extra-small);
}

/* 响应式调整 */
@media (max-width: 575.98px) {
  .login-page {
    padding: var(--el-spacing-3);
  }
  
  .login-box {
    padding: var(--el-spacing-5);
  }
  
  .logo h1 {
    font-size: var(--el-font-size-large);
  }
  
  .login-header {
    margin-bottom: var(--el-spacing-4);
  }
  
  .toggle-buttons {
    margin-bottom: var(--el-spacing-4);
  }
}

@media (min-width: 576px) and (max-width: 767.98px) {
  .login-container {
    max-width: 400px;
  }
  
  .login-box {
    padding: var(--el-spacing-5);
  }
}
</style>