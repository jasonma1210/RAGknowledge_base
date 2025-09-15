import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { notification } from 'antd';
import './styles/theme.css';
import LoginPage from './components/LoginPage';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import VectorDataPage from './components/VectorDataPage';
import ProfilePage from './components/ProfilePage';
import FileUploadPage from './components/FileUploadPage';
import UserDocumentsPage from './components/UserDocumentsPage';
import KnowledgeQAPage from './components/KnowledgeQAPage';
import SystemMonitorCard from './components/SystemMonitorCard';

// 存储使用情况组件
const StorageUsageCard = () => {
  const [storageInfo, setStorageInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStorageInfo = async () => {
      try {
        const token = localStorage.getItem('token');
        if (token) {
          const response = await fetch('/api/auth/profile', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (response.ok) {
            const userData = await response.json();
            setStorageInfo({
              usedStorage: userData.usedStorage,
              storageQuota: userData.storageQuota
            });
          }
        }
      } catch (error) {
        console.error('获取存储信息失败:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStorageInfo();
  }, []);

  // 格式化字节大小为MB
  const formatMB = (bytes) => {
    return (bytes / (1024 * 1024)).toFixed(2);
  };

  // 计算使用百分比
  const calculatePercentage = () => {
    if (!storageInfo) return 0;
    return Math.round((storageInfo.usedStorage / storageInfo.storageQuota) * 100);
  };

  if (loading) {
    return (
      <div className="card" style={{ marginTop: '20px' }}>
        <h3>存储使用情况</h3>
        <p>加载中...</p>
      </div>
    );
  }

  if (!storageInfo) {
    return (
      <div className="card" style={{ marginTop: '20px' }}>
        <h3>存储使用情况</h3>
        <p>无法获取存储信息</p>
      </div>
    );
  }

  const percentage = calculatePercentage();
  const usedStorageMB = formatMB(storageInfo.usedStorage);
  const storageQuotaMB = formatMB(storageInfo.storageQuota);

  return (
    <div className="card" style={{ marginTop: '20px' }}>
      <h3>存储使用情况</h3>
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
            <span>已使用: {usedStorageMB} MB</span>
            <span>总计: {storageQuotaMB} MB</span>
          </div>
          <div style={{ 
            height: '20px', 
            backgroundColor: '#e0e0e0', 
            borderRadius: '10px', 
            overflow: 'hidden' 
          }}>
            <div style={{ 
              height: '100%', 
              width: `${percentage}%`, 
              backgroundColor: percentage > 90 ? '#f44336' : percentage > 75 ? '#ff9800' : '#4caf50',
              transition: 'width 0.3s ease'
            }}></div>
          </div>
          <div style={{ marginTop: '5px', textAlign: 'right' }}>
            <span>{percentage}% 已使用</span>
          </div>
        </div>
      </div>
    </div>
  );
};

// 全局通知上下文
export const NotificationContext = React.createContext();

function App() {
  const [api, contextHolder] = notification.useNotification();
  
  // 提供全局通知方法
  const showNotification = (type, message, description) => {
    api[type]({
      message,
      description,
      placement: 'topRight',
    });
  };

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      <Router>
        {contextHolder}
        <Routes>
          {/* 登录页面 */}
          <Route path="/" element={<LoginPage />} />
          
          {/* Dashboard页面 */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={
              <div>
                {/* 系统监控信息 */}
                <SystemMonitorCard />
                {/* 知识库问答页面 */}
                <KnowledgeQAPage />
              </div>
            } />
          </Route>
          
          {/* 文档管理页面 */}
          <Route path="/documents" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<UserDocumentsPage />} />
          </Route>
          
          {/* 向量数据页面 */}
          <Route path="/vector-data" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<VectorDataPage />} />
          </Route>
          
          
          
          {/* 个人中心页面 */}
          <Route path="/profile" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<ProfilePage />} />
          </Route>
          
          {/* 文件上传页面 */}
          <Route path="/file-upload" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<FileUploadPage />} />
          </Route>
        </Routes>
      </Router>
    </NotificationContext.Provider>
  );
}

export default App;