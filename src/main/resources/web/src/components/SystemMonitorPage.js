import React, { useState, useEffect } from 'react';
import '../styles/theme.css';

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

const SystemMonitorPage = () => {
  const [systemInfo, setSystemInfo] = useState({
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0,
    uptime: ''
  });

  // 获取系统信息
  const fetchSystemInfo = async () => {
    try {
      const response = await fetch('/api/monitor/system');
      if (response.ok) {
        const data = await response.json();
        setSystemInfo(data);
      }
    } catch (error) {
      console.error('获取系统信息失败:', error);
    }
  };

  // 刷新所有数据
  const refreshAll = () => {
    fetchSystemInfo();
  };

  // 组件挂载时获取数据
  useEffect(() => {
    refreshAll();
    // 设置定时器定期刷新数据
    const interval = setInterval(refreshAll, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3>系统状态概览</h3>
          <button className="btn btn-primary" onClick={refreshAll}>刷新</button>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>CPU使用率</h4>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ flex: 1, height: '10px', backgroundColor: '#f1f3f4', borderRadius: '5px', overflow: 'hidden', marginRight: '10px' }}>
                <div 
                  style={{ 
                    height: '100%', 
                    backgroundColor: systemInfo.cpuUsage > 80 ? 'var(--danger-color)' : systemInfo.cpuUsage > 60 ? 'var(--warning-color)' : 'var(--primary-color)', 
                    width: `${systemInfo.cpuUsage}%` 
                  }}
                ></div>
              </div>
              <span>{systemInfo.cpuUsage?.toFixed(2) || 0}%</span>
            </div>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>内存使用率</h4>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ flex: 1, height: '10px', backgroundColor: '#f1f3f4', borderRadius: '5px', overflow: 'hidden', marginRight: '10px' }}>
                <div 
                  style={{ 
                    height: '100%', 
                    backgroundColor: systemInfo.memoryUsage > 80 ? 'var(--danger-color)' : systemInfo.memoryUsage > 60 ? 'var(--warning-color)' : 'var(--primary-color)', 
                    width: `${systemInfo.memoryUsage}%` 
                  }}
                ></div>
              </div>
              <span>{systemInfo.memoryUsage?.toFixed(2) || 0}%</span>
            </div>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>磁盘使用率</h4>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <div style={{ flex: 1, height: '10px', backgroundColor: '#f1f3f4', borderRadius: '5px', overflow: 'hidden', marginRight: '10px' }}>
                <div 
                  style={{ 
                    height: '100%', 
                    backgroundColor: systemInfo.diskUsage > 80 ? 'var(--danger-color)' : systemInfo.diskUsage > 60 ? 'var(--warning-color)' : 'var(--primary-color)', 
                    width: `${systemInfo.diskUsage}%` 
                  }}
                ></div>
              </div>
              <span>{systemInfo.diskUsage?.toFixed(2) || 0}%</span>
            </div>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>系统运行时间</h4>
            <p style={{ fontSize: '18px', fontWeight: '500' }}>{systemInfo.uptime || '未知'}</p>
          </div>
        </div>
      </div>

      {/* 存储使用情况 */}
      <StorageUsageCard />
    </div>
  );
};

export default SystemMonitorPage;