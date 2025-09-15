import React, { useState, useEffect } from 'react';
import '../styles/theme.css';

const ProfilePage = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  const [message, setMessage] = useState('');

  // 获取用户信息
  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/auth/profile', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (response.ok) {
          const userData = await response.json();
          setUserInfo(userData);
        } else {
          setError('获取用户信息失败');
        }
      } catch (err) {
        setError('网络错误');
      } finally {
        setLoading(false);
      }
    };
    
    fetchUserInfo();
  }, []);

  // 计算存储使用百分比
  const storageUsagePercentage = userInfo ? Math.round((userInfo.usedStorage / userInfo.storageQuota) * 100) : 0;

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm({
      ...passwordForm,
      [name]: value
    });
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    
    // 验证新密码和确认密码是否一致
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setMessage('新密码和确认密码不一致');
      return;
    }
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/auth/change-password', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
      });
      
      const result = await response.json();
      
      if (response.ok && result.success) {
        setMessage('密码修改成功');
        // 重置表单
        setPasswordForm({
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
      } else {
        setMessage(result.message || '密码修改失败');
      }
    } catch (err) {
      setMessage('网络错误，请稍后重试');
    }
  };

  if (loading) {
    return <div>加载中...</div>;
  }

  if (error) {
    return <div>错误: {error}</div>;
  }

  return (
    <div>
      <div className="card">
        <h3>基本信息</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="form-group">
            <label className="form-label">用户ID</label>
            <input
              type="text"
              className="form-input"
              value={userInfo.id}
              readOnly
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">用户名</label>
            <input
              type="text"
              className="form-input"
              value={userInfo.username}
              readOnly
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">邮箱</label>
            <input
              type="email"
              className="form-input"
              value={userInfo.email}
              readOnly
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">用户等级</label>
            <input
              type="text"
              className="form-input"
              value={userInfo.level === 1 ? '管理员' : '普通用户'}
              readOnly
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">注册时间</label>
            <input
              type="text"
              className="form-input"
              value={userInfo.gmtCreate}
              readOnly
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">最后登录时间</label>
            <input
              type="text"
              className="form-input"
              value={userInfo.lastLoginTime}
              readOnly
            />
          </div>
        </div>
      </div>

      <div className="card">
        <h3>存储使用情况</h3>
        <div className="card" style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span>存储使用情况</span>
            <span>{(userInfo.usedStorage / (1024 * 1024)).toFixed(2)} MB / {(userInfo.storageQuota / (1024 * 1024)).toFixed(2)} MB</span>
          </div>
          <div style={{ height: '10px', backgroundColor: '#f1f3f4', borderRadius: '5px', overflow: 'hidden' }}>
            <div 
              style={{ 
                height: '100%', 
                backgroundColor: storageUsagePercentage > 80 ? 'var(--danger-color)' : storageUsagePercentage > 60 ? 'var(--warning-color)' : 'var(--primary-color)', 
                width: `${storageUsagePercentage}%` 
              }}
            ></div>
          </div>
          <div style={{ marginTop: '8px' }}>
            <span>已使用 {storageUsagePercentage}%</span>
          </div>
        </div>
      </div>

      <div className="card">
        <h3>修改密码</h3>
        {message && (
          <div className={`alert ${message.includes('成功') ? 'alert-success' : 'alert-error'}`}>
            {message}
          </div>
        )}
        <form onSubmit={handlePasswordSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
            <div className="form-group">
              <label className="form-label">原密码</label>
              <input
                type="password"
                name="oldPassword"
                className="form-input"
                value={passwordForm.oldPassword}
                onChange={handlePasswordChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">新密码</label>
              <input
                type="password"
                name="newPassword"
                className="form-input"
                value={passwordForm.newPassword}
                onChange={handlePasswordChange}
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">确认新密码</label>
              <input
                type="password"
                name="confirmPassword"
                className="form-input"
                value={passwordForm.confirmPassword}
                onChange={handlePasswordChange}
                required
              />
            </div>
          </div>
          
          <button type="submit" className="btn btn-primary">保存</button>
        </form>
      </div>
    </div>
  );
};

export default ProfilePage;