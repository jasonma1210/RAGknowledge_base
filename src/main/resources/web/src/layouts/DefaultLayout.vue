<template>
  <div class="layout-wrapper">
    <el-container class="layout-container">
      <el-header class="layout-header">
        <div class="header-content">
          <div class="header-left">
            <div class="logo-section">
              <div class="logo-toggle" @click="toggleSidebar">
                <el-icon v-if="isSidebarCollapsed"><Expand /></el-icon>
                <el-icon v-else><Fold /></el-icon>
              </div>
              <div class="logo" v-show="!isSidebarCollapsed">
                <h1>RAG知识库系统</h1>
              </div>
            </div>
          </div>
          
          <div class="header-right">
            <div class="user-actions">
              <el-dropdown @command="handleUserCommand">
                <span class="user-dropdown">
                  <el-avatar :size="32" class="user-avatar">
                    <span class="avatar-text">{{ getAvatarText(username) }}</span>
                  </el-avatar>
                  <span class="username hidden-xs-down">{{ username }}</span>
                  <el-icon class="dropdown-icon">
                    <ArrowDown />
                  </el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>
                      个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="logout">
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-header>
      
      <el-container class="main-container">
        <el-aside 
          :width="isSidebarCollapsed ? '80px' : '220px'" 
          class="layout-sidebar"
        >
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isSidebarCollapsed"
            :collapse-transition="false"
            @select="handleMenuSelect"
            background-color="#001529"
            text-color="#ffffff"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <template #title>仪表板</template>
            </el-menu-item>
            <el-menu-item index="/documents">
              <el-icon><Document /></el-icon>
              <template #title>文档管理</template>
            </el-menu-item>
            <el-menu-item index="/search">
              <el-icon><Search /></el-icon>
              <template #title>知识搜索</template>
            </el-menu-item>
            <el-menu-item index="/conversation">
              <el-icon><ChatLineRound /></el-icon>
              <template #title>AI对话</template>
            </el-menu-item>
            <el-menu-item index="/vector-data">
              <el-icon><DataLine /></el-icon>
              <template #title>向量数据</template>
            </el-menu-item>
            <el-menu-item index="/system-monitor">
              <el-icon><Monitor /></el-icon>
              <template #title>系统监控</template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="layout-main">
          <div class="main-content">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  House, 
  Document, 
  Search, 
  DataLine, 
  Monitor, 
  ChatDotRound,
  ChatLineRound,
  User,
  SwitchButton,
  ArrowDown,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import { authAPI } from '@/services/api'

const router = useRouter()
const route = useRoute()

const username = ref('')
const activeMenu = ref('/dashboard')
const isSidebarCollapsed = ref(false)

// 获取用户信息
const getUserInfo = async () => {
  try {
    console.log('开始获取用户信息...')
    // 先从本地存储获取用户名
    const localUserInfo = authAPI.getLocalUserInfo()
    if (localUserInfo) {
      username.value = localUserInfo.username
    }
    
    // 再从接口获取最新信息
    const response = await authAPI.getProfile()
    console.log('获取用户信息响应:', response)
    if (response.success) {
      username.value = response.data.username
      console.log('用户信息获取成功:', response.data)
      // 更新本地存储
      authAPI.setLocalUserInfo(response.data)
    } else {
      console.log('获取用户信息失败，跳转到登录页')
      // 如果获取用户信息失败，跳转到登录页
      router.push('/login')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 只有在当前不在登录页时才跳转
    if (route.path !== '/login') {
      router.push('/login')
    }
  }
}

// 获取头像文字（用户名首字母）
const getAvatarText = (name: string) => {
  if (!name) return 'U'
  return name.charAt(0).toUpperCase()
}

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  console.log('菜单选择:', index)
  router.push(index)
}

// 处理用户命令
const handleUserCommand = (command: string) => {
  console.log('用户命令:', command)
  switch (command) {
    case 'profile':
      // 跳转到个人中心
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 用户登出
const handleLogout = async () => {
  try {
    console.log('开始登出...')
    await authAPI.logout()
    console.log('登出成功，跳转到登录页')
    router.push('/login')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出接口失败，也清除本地信息并跳转到登录页
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 监听路由变化，更新激活菜单
const updateActiveMenu = () => {
  console.log('当前路由路径:', route.path)
  activeMenu.value = route.path
}

onMounted(() => {
  console.log('DefaultLayout组件挂载')
  getUserInfo()
  updateActiveMenu()
})

// 监听路由变化
watch(route, () => {
  console.log('路由变化后更新菜单')
  updateActiveMenu()
})
</script>

<style scoped>
.layout-wrapper {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color-page);
}

.layout-container {
  height: 100%;
}

.layout-header {
  background-color: var(--el-bg-color-overlay);
  color: var(--el-text-color-primary);
  padding: 0;
  box-shadow: var(--el-box-shadow);
  border-bottom: 1px solid var(--el-border-color-lighter);
  height: 50px;
  line-height: 50px;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 var(--el-spacing-4);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo-toggle {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: var(--el-border-radius-base);
  transition: all 0.3s;
  margin-right: var(--el-spacing-3);
}

.logo-toggle:hover {
  background-color: var(--el-fill-color-light);
}

.logo h1 {
  margin: 0;
  font-size: var(--el-font-size-large);
  font-weight: var(--el-font-weight-primary);
  color: var(--el-color-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--el-spacing-1) var(--el-spacing-2);
  border-radius: var(--el-border-radius-base);
  transition: all 0.3s;
}

.user-dropdown:hover {
  background-color: var(--el-fill-color-light);
}

.user-avatar {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-white);
  margin-right: var(--el-spacing-2);
}

.avatar-text {
  font-weight: var(--el-font-weight-primary);
}

.username {
  margin-right: var(--el-spacing-1);
  font-size: var(--el-font-size-base);
  color: var(--el-text-color-primary);
}

.dropdown-icon {
  font-size: var(--el-font-size-small);
  color: var(--el-text-color-secondary);
}

.main-container {
  height: calc(100% - 50px);
}

.layout-sidebar {
  background-color: #001529;
  border-right: 1px solid var(--el-border-color-lighter);
  transition: width 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  box-shadow: var(--el-box-shadow-light);
  z-index: 99;
}

.sidebar-menu {
  border-right: none !important;
  height: 100%;
}

.layout-main {
  background-color: var(--el-bg-color-page);
  padding: 0;
}

.main-content {
  padding: var(--el-spacing-4);
  height: 100%;
  overflow-y: auto;
}

/* Element Plus 菜单样式覆盖 */
:deep(.el-menu) {
  border-right: none !important;
}

:deep(.el-menu-item) {
  height: 50px !important;
  line-height: 50px !important;
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-menu-item:hover) {
  background-color: #1890ff !important;
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff !important;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #1890ff !important;
  color: #ffffff !important;
}

:deep(.el-menu--collapse .el-menu-item) {
  text-align: center;
}

:deep(.el-menu--collapse .el-sub-menu__title) {
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 767.98px) {
  .layout-sidebar {
    width: 80px !important;
  }
  
  .logo {
    display: none;
  }
  
  .logo-toggle {
    margin-right: 0;
  }
  
  .main-content {
    padding: var(--el-spacing-3);
  }
}

@media (max-width: 575.98px) {
  .header-content {
    padding: 0 var(--el-spacing-3);
  }
  
  .main-content {
    padding: var(--el-spacing-2);
  }
  
  .username {
    display: none;
  }
}
</style>