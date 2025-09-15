import React, { useState } from 'react';
import '../styles/theme.css';

const UserInfoPage = () => {
  // 模拟用户信息数据
  const [userInfo, setUserInfo] = useState({
    id: 123,
    username: "example_user",
    email: "user@example.com",
    level: 1,
    storageQuota: 1073741824, // 1GB in bytes
    usedStorage: 322122547,   // 307MB in bytes
    lastLoginTime: "2025-09-11T10:00:00",
    gmtCreate: "2025-09-01T10:00:00",
    gmtModified: "2025-09-11T10:00:00",
    isDeleted: 0
  });

  // 计算存储使用百分比
  const storageUsagePercentage = Math.round((userInfo.usedStorage / userInfo.storageQuota) * 100);

  return (
    <div className="container">
      <h1>用户信息查询</h1>
      
      <div className="card">
        <h2>用户基本信息</h2>
        <p><strong>接口地址:</strong> <code>GET /api/user/info</code></p>
        <p><strong>接口描述:</strong> 查询当前登录用户的基本信息</p>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>用户ID</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.id}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>用户名</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.username}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>邮箱</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.email}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>用户等级</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.level === 0 ? '普通用户' : '进阶用户'}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>最后登录时间</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.lastLoginTime}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>账户创建时间</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{userInfo.gmtCreate}</p>
          </div>
        </div>
        
        <h3 style={{ marginTop: '30px' }}>存储使用情况</h3>
        <div className="card" style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span>存储使用情况</span>
            <span>{(userInfo.usedStorage / (1024 * 1024)).toFixed(2)} MB / {(userInfo.storageQuota / (1024 * 1024)).toFixed(2)} MB</span>
          </div>
          <div style={{ height: '10px', backgroundColor: '#f1f3f4', borderRadius: '5px', overflow: 'hidden' }}>
            <div 
              style={{ 
                height: '100%', 
                backgroundColor: storageUsagePercentage > 80 ? 'var(--secondary-color)' : 'var(--primary-color)', 
                width: `${storageUsagePercentage}%` 
              }}
            ></div>
          </div>
          <div style={{ marginTop: '8px' }}>
            <span>已使用 {storageUsagePercentage}%</span>
          </div>
        </div>
        
        <h3 style={{ marginTop: '30px' }}>响应参数</h3>
        <pre style={{ background: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
{`{
  "id": 123,           // 用户ID
  "username": "string",// 用户名
  "email": "string",   // 邮箱
  "level": 0,          // 用户等级（0:普通用户 1:进阶用户）
  "storageQuota": 123, // 存储配额（字节）
  "usedStorage": 123,  // 已使用存储空间（字节）
  "lastLoginTime": "2025-09-11T10:00:00", // 最后登录时间
  "gmtCreate": "2025-09-11T10:00:00",     // 创建时间
  "gmtModified": "2025-09-11T10:00:00",   // 修改时间
  "isDeleted": 0       // 是否删除（0:未删除 1:已删除）
}`}
        </pre>
      </div>
    </div>
  );
};

export default UserInfoPage;