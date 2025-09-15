import React, { useState, useEffect } from 'react';
import '../styles/theme.css';

const VectorDataPage = () => {
  const [vectorData, setVectorData] = useState({
    totalVectors: 0,
    vectorDimensions: 0,
    fileCount: 0,
    collectionName: ''
  });

  const [vectors, setVectors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 获取向量统计数据
  const fetchVectorStats = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/vector-data/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const stats = await response.json();
        setVectorData({
          totalVectors: stats.totalCount || 0,
          vectorDimensions: stats.dimension || 0,
          fileCount: stats.fileCount || 0,
          collectionName: stats.collectionName || ''
        });
      } else {
        setError('获取向量统计数据失败');
      }
    } catch (err) {
      setError('网络请求失败');
      console.error('获取向量统计数据失败:', err);
    }
  };

  // 获取向量列表
  const fetchVectors = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/vector-data?page=${currentPage - 1}&size=${pageSize}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const pageResult = await response.json();
        setVectors(pageResult.data || []);
        setTotal(pageResult.total || 0);
        setTotalPages(pageResult.totalPages || 0);
      } else {
        setError('获取向量列表失败');
      }
    } catch (err) {
      setError('网络请求失败');
      console.error('获取向量列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  // 刷新所有数据
  const handleRefresh = () => {
    fetchVectorStats();
    fetchVectors();
  };

  // 组件挂载时获取数据
  useEffect(() => {
    fetchVectorStats();
    fetchVectors();
  }, [currentPage, pageSize]);

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3>向量数据概览</h3>
          <button className="btn btn-primary" onClick={handleRefresh} disabled={loading}>
            {loading ? '刷新中...' : '刷新'}
          </button>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>向量总数</h4>
            <p style={{ fontSize: '24px', fontWeight: '500', color: 'var(--primary-color)' }}>{vectorData.totalVectors.toLocaleString()}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>向量维度</h4>
            <p style={{ fontSize: '24px', fontWeight: '500', color: 'var(--primary-color)' }}>{vectorData.vectorDimensions}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>文件数量</h4>
            <p style={{ fontSize: '24px', fontWeight: '500', color: 'var(--primary-color)' }}>{vectorData.fileCount}</p>
          </div>
          
          <div className="card" style={{ padding: '16px' }}>
            <h4 style={{ marginTop: 0 }}>Collection名称</h4>
            <p style={{ fontSize: '16px', fontWeight: '500', color: 'var(--primary-color)', wordBreak: 'break-all' }}>{vectorData.collectionName || 'N/A'}</p>
          </div>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3>向量列表</h3>
        </div>
        
        {error && (
          <div style={{ 
            padding: '10px', 
            marginBottom: '15px', 
            borderRadius: '4px',
            backgroundColor: '#fce8e6',
            color: '#ea4335',
            border: '1px solid #ea4335'
          }}>
            {error}
          </div>
        )}
        
        {loading ? (
          <div style={{ textAlign: 'center', padding: '20px' }}>加载中...</div>
        ) : vectors.length === 0 ? (
          <p>暂无向量数据</p>
        ) : (
          <div>
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>文本内容</th>
                </tr>
              </thead>
              <tbody>
                {vectors.map((vector) => (
                  <tr key={vector.id}>
                    <td>{vector.id}</td>
                    <td style={{ maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {vector.displayText || vector.text || ''}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {/* 分页组件 */}
            <div className="pagination" style={{ marginTop: '20px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
              <span style={{ marginRight: '10px' }}>
                总计 {total} 条记录，共 {totalPages} 页
              </span>
              <button
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1 || loading}
                style={{ marginRight: '5px' }}
              >
                上一页
              </button>
              <span style={{ margin: '0 10px' }}>
                第 {currentPage} 页
              </span>
              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages || loading}
                style={{ marginLeft: '5px' }}
              >
                下一页
              </button>
              <select
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setCurrentPage(1); // 重置到第一页
                }}
                style={{ marginLeft: '10px', padding: '4px' }}
                disabled={loading}
              >
                <option value={5}>5条/页</option>
                <option value={10}>10条/页</option>
                <option value={20}>20条/页</option>
                <option value={50}>50条/页</option>
              </select>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VectorDataPage;