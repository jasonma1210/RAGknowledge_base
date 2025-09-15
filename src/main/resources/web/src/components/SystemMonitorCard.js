import React, { useState, useEffect } from 'react';

const SystemMonitorCard = () => {
  const [systemInfo, setSystemInfo] = useState({
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0
  });
  const [userStats, setUserStats] = useState({
    fileCount: 0,
    vectorCount: 0,
    storageUsage: 0,
    usedStorage: 0,
    storageQuota: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardInfo = async () => {
      try {
        const token = localStorage.getItem('token');
        if (token) {
          const response = await fetch('/api/system/dashboard', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (response.ok) {
            const data = await response.json();
            setSystemInfo(data.systemInfo || {});
            setUserStats(data.userStats || {});
          }
        }
      } catch (error) {
        console.error('获取仪表板信息失败:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardInfo();
    
    // 设置定时器定期刷新数据
    const interval = setInterval(fetchDashboardInfo, 5000);
    return () => clearInterval(interval);
  }, []);

  // 格式化字节大小为MB
  const formatMB = (bytes) => {
    return (bytes / (1024 * 1024)).toFixed(2);
  };

  if (loading) {
    return (
      <div className="card" style={{ marginTop: '20px' }}>
        <h3>系统监控</h3>
        <p>加载中...</p>
      </div>
    );
  }

  return (
    <div className="card" style={{ marginTop: '20px' }}>
      <h3>系统监控</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '20px' }}>
        {/* 系统资源使用情况 */}
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
        
        {/* 用户统计信息 */}
        <div className="card" style={{ padding: '16px' }}>
          <h4 style={{ marginTop: 0 }}>我的文档</h4>
          <p style={{ fontSize: '18px', fontWeight: '500' }}>{userStats.fileCount || 0} 个</p>
        </div>
        
        <div className="card" style={{ padding: '16px' }}>
          <h4 style={{ marginTop: 0 }}>向量数据</h4>
          <p style={{ fontSize: '18px', fontWeight: '500' }}>{userStats.vectorCount || 0} 个</p>
        </div>
        
        <div className="card" style={{ padding: '16px' }}>
          <h4 style={{ marginTop: 0 }}>存储使用</h4>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                <span>{formatMB(userStats.usedStorage || 0)} MB</span>
                <span>{formatMB(userStats.storageQuota || 0)} MB</span>
              </div>
              <div style={{ 
                height: '10px', 
                backgroundColor: '#e0e0e0', 
                borderRadius: '5px', 
                overflow: 'hidden' 
              }}>
                <div style={{ 
                  height: '100%', 
                  width: `${userStats.storageUsage || 0}%`, 
                  backgroundColor: userStats.storageUsage > 90 ? '#f44336' : userStats.storageUsage > 75 ? '#ff9800' : '#4caf50',
                  transition: 'width 0.3s ease'
                }}></div>
              </div>
              <div style={{ marginTop: '5px', textAlign: 'right' }}>
                <span>{userStats.storageUsage?.toFixed(2) || 0}% 已使用</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemMonitorCard;