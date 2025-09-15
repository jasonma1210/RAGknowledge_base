import React, { useState } from 'react';
import '../styles/theme.css';

const UserStoragePage = () => {
  // 模拟用户存储数据
  const [storageInfo, setStorageInfo] = useState({
    storageQuota: 1073741824, // 1GB in bytes
    usedStorage: 322122547,   // 307MB in bytes
    documentsCount: 12,
    vectorsCount: 12500
  });

  // 计算存储使用百分比
  const storageUsagePercentage = Math.round((storageInfo.usedStorage / storageInfo.storageQuota) * 100);

  // 计算剩余存储空间
  const remainingStorage = storageInfo.storageQuota - storageInfo.usedStorage;

  return (
    <div className="container">
      <h1>用户存储情况</h1>
      
      <div className="card">
        <h2>存储使用情况</h2>
        <p><strong>接口地址:</strong> <code>GET /api/user/storage</code></p>
        <p><strong>接口描述:</strong> 查询用户存储使用情况</p>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>存储配额</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{(storageInfo.storageQuota / (1024 * 1024 * 1024)).toFixed(2)} GB</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>已使用存储</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{(storageInfo.usedStorage / (1024 * 1024)).toFixed(2)} MB</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>剩余存储</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{(remainingStorage / (1024 * 1024)).toFixed(2)} MB</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>文档数量</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{storageInfo.documentsCount}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ marginTop: 0 }}>向量数量</h3>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{storageInfo.vectorsCount.toLocaleString()}</p>
          </div>
        </div>
        
        <h3 style={{ marginTop: '30px' }}>存储使用情况图表</h3>
        <div className="card" style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span>存储使用情况</span>
            <span>{storageUsagePercentage}%</span>
          </div>
          <div style={{ height: '20px', backgroundColor: '#f1f3f4', borderRadius: '10px', overflow: 'hidden' }}>
            <div 
              style={{ 
                height: '100%', 
                backgroundColor: storageUsagePercentage > 80 ? 'var(--secondary-color)' : 'var(--primary-color)', 
                width: `${storageUsagePercentage}%` 
              }}
            ></div>
          </div>
          <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <div style={{ width: '20px', height: '20px', backgroundColor: 'var(--primary-color)', borderRadius: '4px', display: 'inline-block', marginRight: '8px' }}></div>
              <span>已使用: {(storageInfo.usedStorage / (1024 * 1024)).toFixed(2)} MB</span>
            </div>
            <div>
              <div style={{ width: '20px', height: '20px', backgroundColor: '#f1f3f4', borderRadius: '4px', display: 'inline-block', marginRight: '8px' }}></div>
              <span>剩余: {(remainingStorage / (1024 * 1024)).toFixed(2)} MB</span>
            </div>
          </div>
        </div>
        
        <h3 style={{ marginTop: '30px' }}>响应参数</h3>
        <pre style={{ background: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
{`{
  "storageQuota": 1073741824, // 存储配额（字节）
  "usedStorage": 322122547,   // 已使用存储空间（字节）
  "documentsCount": 12,       // 文档数量
  "vectorsCount": 12500       // 向量数量
}`}
        </pre>
      </div>
    </div>
  );
};

export default UserStoragePage;