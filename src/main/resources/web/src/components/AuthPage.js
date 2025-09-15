import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/theme.css';

const AuthPage = () => {
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
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // success or error
  const navigate = useNavigate();

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
    setLoading(true);
    setMessage('');
    
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginForm),
      });
      
      const data = await response.json();
      
      if (response.ok && data.success) {
        // 保存令牌到localStorage
        localStorage.setItem('token', data.token);
        setMessage('登录成功');
        setMessageType('success');
        // 登录成功后跳转到主页面
        setTimeout(() => {
          navigate('/dashboard');
        }, 1000);
      } else {
        setMessage(data.message || '登录失败');
        setMessageType('error');
      }
    } catch (error) {
      setMessage('登录请求失败: ' + error.message);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(registerForm),
      });
      
      const data = await response.json();
      
      if (response.ok && data.success) {
        setMessage('注册成功');
        setMessageType('success');
        // 注册成功后自动切换到登录标签
        setTimeout(() => {
          setActiveTab('login');
        }, 1000);
      } else {
        setMessage(data.message || '注册失败');
        setMessageType('error');
      }
    } catch (error) {
      setMessage('注册请求失败: ' + error.message);
      setMessageType('error');
    } finally {
      setLoading(false);
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
        
        {message && (
          <div style={{ 
            padding: '10px', 
            marginBottom: '15px', 
            borderRadius: '4px',
            backgroundColor: messageType === 'success' ? '#e6f4ea' : '#fce8e6',
            color: messageType === 'success' ? '#34a853' : '#ea4335',
            border: `1px solid ${messageType === 'success' ? '#34a853' : '#ea4335'}`
          }}>
            {message}
          </div>
        )}
        
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
                disabled={loading}
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
                disabled={loading}
              />
            </div>
            
            <button 
              type="submit" 
              className="btn btn-primary" 
              style={{ width: '100%' }}
              disabled={loading}
            >
              {loading ? '登录中...' : '登录'}
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
                disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
              />
            </div>
            
            <button 
              type="submit" 
              className="btn btn-primary" 
              style={{ width: '100%' }}
              disabled={loading}
            >
              {loading ? '注册中...' : '注册'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default AuthPage;