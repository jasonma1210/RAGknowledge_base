import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('token');
      
      // 如果没有token，直接跳转到登录页面
      if (!token) {
        navigate('/', { 
          replace: true,
          state: { from: location }
        });
        setIsAuthenticated(false);
        setIsLoading(false);
        return;
      }

      try {
        // 验证token是否有效
        const response = await fetch('/api/auth/profile', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          setIsAuthenticated(true);
        } else {
          // 如果token无效，清除本地存储并跳转到登录页面
          localStorage.removeItem('token');
          navigate('/', { 
            replace: true,
            state: { from: location }
          });
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('验证用户信息失败:', error);
        // 如果验证失败，清除本地存储并跳转到登录页面
        localStorage.removeItem('token');
        navigate('/', { 
          replace: true,
          state: { from: location }
        });
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    // 初始检查
    checkAuth();

    // 监听storage事件，当其他标签页登录或登出时能及时更新
    const handleStorageChange = (e) => {
      if (e.key === 'token') {
        checkAuth();
      }
    };

    window.addEventListener('storage', handleStorageChange);

    // 清理函数
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [navigate, location]);

  // 如果还在检查认证状态，显示loading
  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>正在验证用户信息...</div>
      </div>
    );
  }

  // 如果未认证，不渲染子组件
  if (!isAuthenticated) {
    return null;
  }

  return children;
};

export default ProtectedRoute;