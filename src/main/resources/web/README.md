# RAG知识库系统前端

基于Vue3 + Element Plus的现代化知识库管理系统前端

## 项目结构

```
src/
├── assets/           # 静态资源文件
├── components/       # 公共组件
├── layouts/          # 布局组件
├── router/           # 路由配置
├── services/         # API服务层
├── stores/           # 状态管理
├── views/            # 页面视图
├── App.vue           # 根组件
└── main.ts           # 入口文件
```

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - JavaScript的超集，提供类型安全
- **Element Plus** - Vue 3的桌面端组件库
- **Vue Router** - Vue.js的官方路由管理器
- **Axios** - 基于Promise的HTTP客户端
- **Vite** - 新一代前端构建工具

## 功能模块

### 1. 认证模块
- 用户登录/注册
- JWT Token管理
- 用户信息获取

### 2. 文档管理
- 文档上传/下载
- 文档列表查看
- 文档删除

### 3. 知识搜索
- 语义搜索
- 关键词搜索
- 混合搜索

### 4. AI问答
- 智能问答系统
- 答案置信度显示
- 参考来源展示

### 5. 向量数据管理
- 向量数据统计
- 向量数据列表查看

### 6. 系统监控
- 系统健康状态检查
- 系统指标监控
- 系统信息查看

## 开发指南

### 环境要求
- Node.js >= 16.0.0
- npm >= 7.0.0

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 类型检查
```bash
npm run type-check
```

## API接口

所有API接口通过`/src/services/api.ts`统一管理，包含以下模块：

- `authAPI` - 认证相关接口
- `documentAPI` - 文档管理接口
- `searchAPI` - 搜索相关接口
- `vectorDataAPI` - 向量数据接口
- `systemAPI` - 系统监控接口
- `healthAPI` - 健康检查接口

## 路由配置

路由配置位于`/src/router/index.ts`，包含以下路径：

- `/login` - 登录页面
- `/` - 默认布局，包含以下子路由：
  - `/dashboard` - 仪表板
  - `/documents` - 文档管理
  - `/search` - 知识搜索
  - `/ask` - AI问答
  - `/vector-data` - 向量数据
  - `/system-monitor` - 系统监控

## 代理配置

为了处理跨域问题，Vite配置了代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    },
    '/actuator': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 开发规范

1. 使用TypeScript进行类型检查
2. 遵循Vue 3 Composition API规范
3. 组件命名使用PascalCase
4. 文件命名使用kebab-case
5. API调用统一使用services层
6. 状态管理使用Pinia（如需要）

## 注意事项

1. 启动前端服务前请确保后端服务已启动
2. 默认后端地址为`http://localhost:8080`
3. 前端开发服务器地址为`http://localhost:5173`
4. 登录后Token会自动保存到localStorage
5. 401错误会自动跳转到登录页