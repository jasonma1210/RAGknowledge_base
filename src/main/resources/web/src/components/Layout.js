import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import '../styles/theme.css';

const Layout = () => {
  const [activeMenu, setActiveMenu] = useState('dashboard');
  const [userInfo, setUserInfo] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  // 获取用户信息
  useEffect(() => {
    const fetchUserInfo = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          const response = await fetch('/api/auth/profile', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (response.ok) {
            const userData = await response.json();
            setUserInfo({
              username: userData.username,
              storageQuota: userData.storageQuota,
              usedStorage: userData.usedStorage
            });
          }
        } catch (error) {
          console.error('获取用户信息失败:', error);
        }
      }
    };
    
    fetchUserInfo();
  }, []);

  // 根据当前路径设置活动菜单
  useEffect(() => {
    const path = location.pathname;
    if (path.includes('vector-data')) {
      setActiveMenu('vector');
    } else if (path.includes('documents')) {
      setActiveMenu('documents');
    } else if (path.includes('profile')) {
      setActiveMenu('profile');
    } else {
      setActiveMenu('dashboard');
    }
  }, [location]);

  const handleMenuClick = (menu) => {
    setActiveMenu(menu);
    switch (menu) {
      case 'dashboard':
        navigate('/dashboard');
        break;
      case 'documents':
        navigate('/documents');
        break;
      case 'vector':
        navigate('/vector-data');
        break;
      case 'profile':
        navigate('/profile');
        break;
      default:
        break;
    }
  };

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        // 调用退出登录API
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          }
        });
      }
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      // 清除用户信息并跳转到登录页面
      localStorage.removeItem('token');
      navigate('/');
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* 侧边栏 */}
      <div style={{ 
        width: '220px', 
        backgroundColor: 'var(--sidebar-background)', 
        color: 'var(--sidebar-text)',
        padding: '20px 0',
        display: 'flex',
        flexDirection: 'column'
      }}>
        <div style={{ padding: '0 20px 20px', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
          <h2 style={{ color: 'white', fontSize: '18px' }}>RAG知识库系统</h2>
        </div>
        
        <nav style={{ flex: 1 }}>
          <ul style={{ listStyle: 'none', padding: '0', margin: '0' }}>
            <li>
              <button
                onClick={() => handleMenuClick('dashboard')}
                style={{
                  width: '100%',
                  padding: '15px 20px',
                  textAlign: 'left',
                  backgroundColor: activeMenu === 'dashboard' ? 'rgba(255,255,255,0.1)' : 'transparent',
                  color: 'white',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                <span>首页</span>
              </button>
            </li>
            <li>
              <button
                onClick={() => handleMenuClick('documents')}
                style={{
                  width: '100%',
                  padding: '15px 20px',
                  textAlign: 'left',
                  backgroundColor: activeMenu === 'documents' ? 'rgba(255,255,255,0.1)' : 'transparent',
                  color: 'white',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                <span>我的文档</span>
              </button>
            </li>
            <li>
              <button
                onClick={() => navigate('/file-upload')}
                style={{
                    width: '100%',
                    padding: '15px 20px',
                    textAlign: 'left',
                    backgroundColor: activeMenu === 'file-upload' ? 'rgba(255,255,255,0.1)' : 'transparent',
                  // backgroundColor: location.pathname.includes('file-upload') ? 'rgba(255,255,255,0.1)' : 'transparent',
                    color: 'white',
                    border: 'none',
                    cursor: 'pointer',
                    fontSize: '14px'
                }}
              >
                <span>文件上传</span>
              </button>
            </li>
            <li>
              <button
                onClick={() => handleMenuClick('vector')}
                style={{
                  width: '100%',
                  padding: '15px 20px',
                  textAlign: 'left',
                  backgroundColor: activeMenu === 'vector' ? 'rgba(255,255,255,0.1)' : 'transparent',
                  color: 'white',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                <span>向量数据</span>
              </button>
            </li>
            
          </ul>
        </nav>
        
        <div style={{ padding: '20px', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
          <button
            onClick={() => handleMenuClick('profile')}
            style={{
              width: '100%',
              padding: '10px',
              textAlign: 'left',
              backgroundColor: activeMenu === 'profile' ? 'rgba(255,255,255,0.1)' : 'transparent',
              color: 'white',
              border: 'none',
              cursor: 'pointer',
              fontSize: '14px',
              marginBottom: '10px'
            }}
          >
            个人中心
          </button>
          <button
            onClick={handleLogout}
            style={{
              width: '100%',
              padding: '10px',
              textAlign: 'left',
              backgroundColor: 'transparent',
              color: 'white',
              border: 'none',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            退出登录
          </button>
        </div>
      </div>
      
      {/* 主内容区 */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* 顶部栏 */}
        <header style={{ 
          backgroundColor: 'var(--header-background)', 
          padding: '15px 20px', 
          boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h3 style={{ margin: 0, color: 'var(--text-color)' }}>
              {activeMenu === 'documents' && '我的文档'}
              {activeMenu === 'vector' && '向量数据'}
              {activeMenu === 'profile' && '个人中心'}
            </h3>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            {userInfo ? (
              <>
                <div style={{ textAlign: 'right', marginRight: '10px' }}>
                  <div style={{ fontSize: '14px', fontWeight: '500' }}>{userInfo.username}</div>
                  {userInfo.storageQuota && userInfo.usedStorage !== undefined && (
                    <div style={{ fontSize: '12px', color: '#666' }}>
                      {(userInfo.usedStorage / (1024 * 1024)).toFixed(2)} MB / {(userInfo.storageQuota / (1024 * 1024)).toFixed(2)} MB
                    </div>
                  )}
                </div>
                <div style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '50%',
                  backgroundColor: '#ddd',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  color: '#666'
                }}>
                  {userInfo.username.charAt(0).toUpperCase()}
                </div>
              </>
            ) : (
              <div style={{ fontSize: '14px', color: '#666' }}>加载中...</div>
            )}
          </div>
        </header>
        
        {/* 页面内容 */}
        <main style={{ flex: 1, padding: '20px', backgroundColor: 'var(--background-color)' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;