import React, { useState } from 'react';
import '../styles/theme.css';

const UploadPage = () => {
  const [uploadForm, setUploadForm] = useState({
    title: '',
    description: '',
    tags: ''
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState(null); // null, 'uploading', 'success', 'error'

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
  };

  const handleUploadSubmit = (e) => {
    e.preventDefault();
    
    if (!selectedFile) {
      setUploadStatus('error');
      return;
    }
    
    // 设置上传状态为上传中
    setUploadStatus('uploading');
    
    // 这里应该调用上传文档API
    console.log('上传文档请求:', { ...uploadForm, file: selectedFile });
    
    // 模拟上传过程
    setTimeout(() => {
      setUploadStatus('success');
      // 重置表单
      setUploadForm({
        title: '',
        description: '',
        tags: ''
      });
      setSelectedFile(null);
    }, 2000);
  };

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / 1048576).toFixed(2) + ' MB';
  };

  return (
    <div className="container">
      <h1>文件上传</h1>
      
      <div className="card">
        <h2>上传文档</h2>
        <p><strong>接口地址:</strong> <code>POST /api/documents/upload</code></p>
        <p><strong>接口描述:</strong> 上传新文档到知识库</p>
        
        <form onSubmit={handleUploadSubmit}>
          <div className="form-group">
            <label className="form-label">选择文件:</label>
            <input
              type="file"
              className="form-input"
              onChange={handleFileChange}
              required
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
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={uploadStatus === 'uploading'}
          >
            {uploadStatus === 'uploading' ? '上传中...' : '上传文档'}
          </button>
          
          {uploadStatus === 'success' && (
            <div style={{ marginTop: '16px', padding: '12px', backgroundColor: '#e6f4ea', border: '1px solid #34a853', borderRadius: '4px', color: '#34a853' }}>
              文件上传成功！
            </div>
          )}
          
          {uploadStatus === 'error' && (
            <div style={{ marginTop: '16px', padding: '12px', backgroundColor: '#fce8e6', border: '1px solid #ea4335', borderRadius: '4px', color: '#ea4335' }}>
              请选择要上传的文件！
            </div>
          )}
        </form>
        
        <h3 style={{ marginTop: '30px' }}>请求参数</h3>
        <pre style={{ background: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
{`file: 文件（必填）
title: 文档标题（可选）
description: 文档描述（可选）
tags: 标签（可选，逗号分隔）`}
        </pre>
        
        <h3>响应参数</h3>
        <pre style={{ background: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
{`{
  "id": "string",        // 文档ID
  "title": "string",     // 文档标题
  "description": "string",// 文档描述
  "fileName": "string",  // 文件名
  "fileType": "string",  // 文件类型
  "fileSize": 123,       // 文件大小（字节）
  "tags": "string",      // 标签
  "uploadTime": "2025-09-11T10:00:00", // 上传时间
  "chunkCount": 123      // 分块数量
}`}
        </pre>
      </div>
    </div>
  );
};

export default UploadPage;