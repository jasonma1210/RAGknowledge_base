import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/theme.css';

const LoginPage = () => {
  const [loginForm, setLoginForm] = useState({
    username: '',
    password: ''
  });
  const [registerForm, setRegisterForm] = useState({
    username: '',
    password: '',
    email: ''
  });
  const [activeTab, setActiveTab] = useState('login');
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  // 检查用户是否已经登录
  useEffect(() => {
    const checkIfLoggedIn = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          // 验证token是否有效
          const response = await fetch('/api/auth/profile', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });

          if (response.ok) {
            // 如果用户已经登录，根据来源决定跳转到哪个页面
            const from = location.state?.from?.pathname || '/dashboard';
            navigate(from, { replace: true });
          } else {
            // 如果token无效，清除本地存储
            localStorage.removeItem('token');
            setIsLoading(false);
          }
        } catch (error) {
          console.error('验证用户信息失败:', error);
          // 如果验证失败，清除本地存储
          localStorage.removeItem('token');
          setIsLoading(false);
        }
      } else {
        setIsLoading(false);
      }
    };

    checkIfLoggedIn();
  }, [navigate, location]);

  // 如果还在检查登录状态，显示loading
  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>正在检查登录状态...</div>
      </div>
    );
  }

  const handleLoginChange = (e) => {
    const { name, value } = e.target;
    setLoginForm({
      ...loginForm,
      [name]: value
    });
  };

  const handleRegisterChange = (e) => {
    const { name, value } = e.target;
    setRegisterForm({
      ...registerForm,
      [name]: value
    });
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginForm),
      });

      if (response.ok) {
        const data = await response.json();
        // 保存token到localStorage
        localStorage.setItem('token', data.token);
        // 登录成功后跳转到主页面
        navigate('/dashboard');
      } else {
        const errorData = await response.json();
        console.error('登录失败:', response.status);
        alert(`登录失败: ${errorData.message || '请检查用户名和密码'}`);
      }
    } catch (error) {
      console.error('登录请求失败:', error);
      alert('登录请求失败，请稍后重试');
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(registerForm),
      });

      if (response.ok) {
        const data = await response.json();
        // 注册成功后自动登录
        localStorage.setItem('token', data.token);
        navigate('/dashboard');
      } else {
        const errorData = await response.json();
        console.error('注册失败:', response.status, errorData.message);
        alert(`注册失败: ${errorData.message}`);
      }
    } catch (error) {
      console.error('注册请求失败:', error);
      alert('注册请求失败，请稍后重试');
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh', 
      backgroundColor: 'var(--background-color)' 
    }}>
      <div className="card" style={{ width: '400px', padding: '30px' }}>
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <h2 style={{ margin: '0 0 10px 0', color: 'var(--primary-color)' }}>RAG知识库系统</h2>
          <p style={{ color: '#666', margin: 0 }}>后台管理系统</p>
        </div>
        
        <div style={{ display: 'flex', marginBottom: '20px' }}>
          <button 
            className={`btn ${activeTab === 'login' ? 'btn-primary' : ''}`}
            onClick={() => setActiveTab('login')}
            style={{ 
              flex: 1, 
              marginRight: '10px',
              backgroundColor: activeTab === 'login' ? 'var(--primary-color)' : 'transparent',
              color: activeTab === 'login' ? 'white' : 'var(--text-color)',
              border: '1px solid var(--border-color)'
            }}
          >
            登录
          </button>
          <button 
            className={`btn ${activeTab === 'register' ? 'btn-primary' : ''}`}
            onClick={() => setActiveTab('register')}
            style={{ 
              flex: 1,
              backgroundColor: activeTab === 'register' ? 'var(--primary-color)' : 'transparent',
              color: activeTab === 'register' ? 'white' : 'var(--text-color)',
              border: '1px solid var(--border-color)'
            }}
          >
            注册
          </button>
        </div>
        
        {activeTab === 'login' && (
          <form onSubmit={handleLoginSubmit}>
            <div className="form-group">
              <label className="form-label">用户名</label>
              <input
                type="text"
                name="username"
                className="form-input"
                value={loginForm.username}
                onChange={handleLoginChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">密码</label>
              <input
                type="password"
                name="password"
                className="form-input"
                value={loginForm.password}
                onChange={handleLoginChange}
                required
              />
            </div>
            
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={!loginForm.username || !loginForm.password}>
              登录
            </button>
          </form>
        )}
        
        {activeTab === 'register' && (
          <form onSubmit={handleRegisterSubmit}>
            <div className="form-group">
              <label className="form-label">用户名</label>
              <input
                type="text"
                name="username"
                className="form-input"
                value={registerForm.username}
                onChange={handleRegisterChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">邮箱</label>
              <input
                type="email"
                name="email"
                className="form-input"
                value={registerForm.email}
                onChange={handleRegisterChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">密码</label>
              <input
                type="password"
                name="password"
                className="form-input"
                value={registerForm.password}
                onChange={handleRegisterChange}
                required
              />
            </div>
            
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={!registerForm.username || !registerForm.password}>
              注册
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default LoginPage;