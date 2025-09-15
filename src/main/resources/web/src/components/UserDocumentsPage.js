import React, { useState, useEffect } from 'react';
import '../styles/theme.css';

const UserDocumentsPage = () => {
  const [documents, setDocuments] = useState([]);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // 确认弹窗状态
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null); // 'download' 或 'delete'
  const [actionDocumentId, setActionDocumentId] = useState(null);
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / 1048576).toFixed(2) + ' MB';
  };

  // 获取文档列表
  const fetchDocuments = async () => {
    setLoading(true);
    setError('');
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/documents?page=${currentPage - 1}&size=${pageSize}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        const pageResult = await response.json();
        setDocuments(pageResult.data || []);
        setTotal(pageResult.total || 0);
        setTotalPages(pageResult.totalPages || 0);
      } else {
        setError('获取文档列表失败');
      }
    } catch (err) {
      setError('网络请求失败');
      console.error('获取文档列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  // 处理删除文档
  const handleDeleteDocument = async (documentId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/documents/${documentId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        // 重新加载文档列表
        fetchDocuments();
        if (selectedDocument && selectedDocument.id === documentId) {
          setSelectedDocument(null);
        }
      } else {
        setError('删除文档失败');
      }
    } catch (err) {
      setError('删除文档请求失败');
      console.error('删除文档失败:', err);
    }
  };

  // 处理下载文档
  const handleDownloadDocument = async (documentId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/documents/${documentId}/download`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        // 获取文件名
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'document.pdf'; // 默认文件名
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
          if (filenameMatch && filenameMatch[1]) {
            filename = filenameMatch[1];
          }
        }
        
        // 创建下载链接
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      } else {
        setError('下载文档失败');
      }
    } catch (err) {
      setError('下载文档请求失败');
      console.error('下载文档失败:', err);
    }
  };

  // 显示确认弹窗
  const showConfirm = (action, documentId) => {
    setConfirmAction(action);
    setActionDocumentId(documentId);
    setShowConfirmDialog(true);
  };

  // 处理确认操作
  const handleConfirmAction = () => {
    if (confirmAction === 'delete') {
      handleDeleteDocument(actionDocumentId);
    } else if (confirmAction === 'download') {
      handleDownloadDocument(actionDocumentId);
    }
    setShowConfirmDialog(false);
    setConfirmAction(null);
    setActionDocumentId(null);
  };

  // 刷新文档列表
  const handleRefresh = () => {
    fetchDocuments();
  };

  // 组件挂载时获取数据
  useEffect(() => {
    fetchDocuments();
  }, [currentPage, pageSize]);

  return (
    <div className="container">
      <h1>用户上传文件信息</h1>
      
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h2>文档列表</h2>
          <button className="btn btn-primary" onClick={handleRefresh} disabled={loading}>
            {loading ? '刷新中...' : '刷新'}
          </button>
        </div>
        
        {/*<p><strong>接口地址:</strong> <code>GET /api/documents</code></p>*/}
        {/*<p><strong>接口描述:</strong> 分页获取用户上传的文档列表</p>*/}
        
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
        ) : documents.length === 0 ? (
          <p>暂无上传的文档</p>
        ) : (
          <div>
            <table className="table">
              <thead>
                <tr>
                  <th>文档标题</th>
                  <th>文件名</th>
                  <th>文件类型</th>
                  <th>文件大小</th>
                  <th>上传时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {documents.map(doc => (
                  <tr key={doc.id}>
                    <td>
                      <button 
                        onClick={() => setSelectedDocument(doc)}
                        style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer', textDecoration: 'underline' }}
                      >
                        {doc.title}
                      </button>
                    </td>
                    <td>{doc.fileName}</td>
                    <td>{doc.fileType}</td>
                    <td>{formatFileSize(doc.fileSize)}</td>
                    <td>{new Date(doc.uploadTime).toLocaleString()}</td>
                    <td style={{ display: 'flex', gap: '8px' }}>
                      <button 
                        className="btn btn-primary"
                        onClick={() => showConfirm('download', doc.id)}
                        style={{ padding: '4px 8px', fontSize: '14px' }}
                      >
                        下载
                      </button>
                      <button 
                        className="btn btn-danger" 
                        onClick={() => showConfirm('delete', doc.id)}
                        style={{ padding: '4px 8px', fontSize: '14px' }}
                      >
                        删除
                      </button>
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
                disabled={currentPage === 1}
                style={{ marginRight: '5px' }}
              >
                上一页
              </button>
              <span style={{ margin: '0 10px' }}>
                第 {currentPage} 页
              </span>
              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
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
              >
                <option value={5}>5条/页</option>
                <option value={10}>10条/页</option>
                <option value={20}>20条/页</option>
                <option value={50}>50条/页</option>
              </select>
            </div>
            
            {selectedDocument && (
              <div className="card" style={{ marginTop: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ marginTop: 0 }}>{selectedDocument.title}</h3>
                  <button
                    onClick={() => setSelectedDocument(null)}
                    style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer' }}
                  >
                    ×
                  </button>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px', marginTop: '16px' }}>
                  <div>
                    <strong>文档描述:</strong>
                    <p>{selectedDocument.description}</p>
                  </div>

                  <div>
                    <strong>文件名:</strong>
                    <p>{selectedDocument.fileName}</p>
                  </div>

                  <div>
                    <strong>文件类型:</strong>
                    <p>{selectedDocument.fileType}</p>
                  </div>

                  <div>
                    <strong>文件大小:</strong>
                    <p>{formatFileSize(selectedDocument.fileSize)}</p>
                  </div>

                  <div>
                    <strong>标签:</strong>
                    <p>{selectedDocument.tags}</p>
                  </div>

                  <div>
                    <strong>上传时间:</strong>
                    <p>{new Date(selectedDocument.uploadTime).toLocaleString()}</p>
                  </div>

                  <div>
                    <strong>分块数量:</strong>
                    <p>{selectedDocument.chunkCount}</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
        
        {/* 确认弹窗 */}
        {showConfirmDialog && (
          <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000
          }}>
            <div className="card" style={{ padding: '20px', minWidth: '300px' }}>
              <h3 style={{ marginTop: 0 }}>
                {confirmAction === 'delete' ? '确认删除' : '确认下载'}
              </h3>
              <p>
                {confirmAction === 'delete' 
                  ? '确定要删除这个文档吗？此操作不可撤销。' 
                  : '确定要下载这个文档吗？'}
              </p>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button 
                  className="btn btn-secondary" 
                  onClick={() => setShowConfirmDialog(false)}
                  style={{ padding: '6px 12px' }}
                >
                  取消
                </button>
                <button 
                  className={confirmAction === 'delete' ? 'btn btn-danger' : 'btn btn-primary'}
                  onClick={handleConfirmAction}
                  style={{ padding: '6px 12px' }}
                >
                  确认
                </button>
              </div>
            </div>
          </div>
        )}
        
        {/*<h3 style={{ marginTop: '30px' }}>响应参数</h3>*/}
{/*     ce*/}
      </div>
    </div>
  );
};

export default UserDocumentsPage;