import React, { useState, useContext } from 'react';
import { NotificationContext } from '../App';
import '../styles/theme.css';

const FileUploadPage = () => {
  const { showNotification } = useContext(NotificationContext);
  
  const [uploadForm, setUploadForm] = useState({
    title: '',
    description: '',
    tags: ''
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState(null); // null, 'uploading', 'success', 'error'
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // success or error

  const handleUploadChange = (e) => {
    const { name, value } = e.target;
    setUploadForm({
      ...uploadForm,
      [name]: value
    });
  };

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
    setUploadStatus(null);
    setMessage('');
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedFile) {
      setUploadStatus('error');
      setMessage('请选择要上传的文件！');
      setMessageType('error');
      return;
    }
    
    // 设置上传状态为上传中
    setUploadStatus('uploading');
    setMessage('');
    
    try {
      // 创建FormData对象
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('title', uploadForm.title);
      formData.append('description', uploadForm.description);
      formData.append('tags', uploadForm.tags);
      
      // 获取令牌
      const token = localStorage.getItem('token');
      
      // 调用异步上传文档API
      const response = await fetch('/api/documents/upload/async', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });
      
      if (response.ok) {
        // 显示通知
        showNotification('success', '文件上传', '文件上传请求已提交，正在后台处理中...');
        
        setUploadStatus('success');
        setMessage('文件上传请求已提交，正在后台处理中...');
        setMessageType('success');
        
        // 重置表单
        setUploadForm({
          title: '',
          description: '',
          tags: ''
        });
        setSelectedFile(null);
      } else {
        const errorText = await response.text();
        setUploadStatus('error');
        setMessage(`上传失败: ${errorText}`);
        setMessageType('error');
        showNotification('error', '文件上传失败', errorText);
      }
    } catch (error) {
      setUploadStatus('error');
      setMessage(`上传请求失败: ${error.message}`);
      setMessageType('error');
      showNotification('error', '文件上传失败', error.message);
    }
  };

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / 1048576).toFixed(2) + ' MB';
  };

  return (
    <div>
      <div className="card">
        <h3>上传文件</h3>
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
        <form onSubmit={handleUploadSubmit}>
          <div className="form-group">
            <label className="form-label">选择文件:</label>
            <input
              type="file"
              className="form-input"
              onChange={handleFileChange}
              required
              disabled={uploadStatus === 'uploading'}
            />
            {selectedFile && (
              <div style={{ marginTop: '8px', padding: '8px', backgroundColor: '#f1f3f4', borderRadius: '4px' }}>
                <strong>已选择文件:</strong> {selectedFile.name} ({formatFileSize(selectedFile.size)})
              </div>
            )}
          </div>
          
          <div className="form-group">
            <label className="form-label">文档标题:</label>
            <input
              type="text"
              name="title"
              className="form-input"
              value={uploadForm.title}
              onChange={handleUploadChange}
              placeholder="请输入文档标题"
              disabled={uploadStatus === 'uploading'}
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">文档描述:</label>
            <textarea
              name="description"
              className="form-input"
              value={uploadForm.description}
              onChange={handleUploadChange}
              rows="3"
              placeholder="请输入文档描述"
              disabled={uploadStatus === 'uploading'}
            />
          </div>
          
          <div className="form-group">
            <label className="form-label">标签 (逗号分隔):</label>
            <input
              type="text"
              name="tags"
              className="form-input"
              value={uploadForm.tags}
              onChange={handleUploadChange}
              placeholder="请输入标签，多个标签用逗号分隔"
              disabled={uploadStatus === 'uploading'}
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={uploadStatus === 'uploading'}
          >
            {uploadStatus === 'uploading' ? '上传中...' : '上传文档'}
          </button>
        </form>
      </div>
      
      <div className="card">
        <h3>上传须知</h3>
        <ul>
          <li>支持的文件格式：PDF、DOCX、TXT等</li>
          <li>单个文件大小不能超过100MB</li>
          <li>上传后的文件将被自动处理并建立索引</li>
          <li>处理完成后可在文件管理中查看文件状态</li>
          <li>文件上传采用异步处理，提交后可立即进行其他操作</li>
        </ul>
      </div>
    </div>
  );
};

export default FileUploadPage;