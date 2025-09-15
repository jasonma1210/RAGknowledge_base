import React, { useState } from 'react';
import '../styles/theme.css';

const KnowledgeQAPage = () => {
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [sources, setSources] = useState([]);

  // 处理提问
  const handleAsk = async () => {
    if (!question.trim()) {
      setError('请输入问题');
      return;
    }

    setLoading(true);
    setError('');
    setAnswer('');
    setSources([]);

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/search/ask', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          question: question.trim()
        })
      });

      if (response.ok) {
        const result = await response.json();
        setAnswer(result.answer || '未找到相关答案');
        setSources(result.sources || []);
      } else if (response.status === 401) {
        setError('未授权访问，请重新登录');
      } else {
        setError('问答请求失败');
      }
    } catch (err) {
      setError('网络请求失败');
      console.error('问答请求失败:', err);
    } finally {
      setLoading(false);
    }
  };

  // 处理键盘事件
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleAsk();
    }
  };

  return (
    <div className="container">
      <h1>知识库问答</h1>
      
      <div className="card">
        <h2>提问</h2>
        <div style={{ marginBottom: '20px' }}>
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="请输入您的问题..."
            style={{
              width: '100%',
              minHeight: '100px',
              padding: '12px',
              borderRadius: '4px',
              border: '1px solid #ddd',
              fontSize: '16px',
              fontFamily: 'inherit'
            }}
            disabled={loading}
          />
        </div>
        
        <button 
          className="btn btn-primary" 
          onClick={handleAsk} 
          disabled={loading}
          style={{ marginBottom: '20px' }}
        >
          {loading ? '提问中...' : '提问'}
        </button>
        
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
        
        {answer && (
          <div className="card" style={{ marginBottom: '20px' }}>
            <h3>答案</h3>
            <div style={{ 
              padding: '15px', 
              backgroundColor: '#f8f9fa', 
              borderRadius: '4px',
              whiteSpace: 'pre-wrap'
            }}>
              {answer}
            </div>
          </div>
        )}
        
        {sources.length > 0 && (
          <div className="card">
            <h3>参考来源</h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
              {sources.map((source, index) => (
                <div key={index} className="card" style={{ marginBottom: '10px' }}>
                  <div style={{ 
                    padding: '10px', 
                    backgroundColor: '#e9ecef', 
                    borderRadius: '4px',
                    marginBottom: '8px'
                  }}>
                    <strong>{source.title}</strong>
                  </div>
                  <div style={{ 
                    padding: '10px',
                    border: '1px solid #dee2e6',
                    borderRadius: '4px'
                  }}>
                    {source.content}
                  </div>
                  <div style={{ 
                    marginTop: '8px', 
                    fontSize: '14px', 
                    color: '#6c757d' 
                  }}>
                    相似度: {(source.score * 100).toFixed(2)}%
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default KnowledgeQAPage;