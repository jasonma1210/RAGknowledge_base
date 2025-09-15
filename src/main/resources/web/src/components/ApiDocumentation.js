import React from 'react';
import { Layout, Menu, Typography, Card, List, Tag, Divider } from 'antd';
import {
  UserOutlined,
  FileOutlined,
  SearchOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons';

const { Header, Content, Sider } = Layout;
const { Title, Paragraph } = Typography;

const ApiDocumentation = () => {
  // 认证接口数据
  const authEndpoints = [
    {
      key: '1-1',
      name: '用户注册',
      method: 'POST',
      path: '/api/auth/register',
      description: '注册新用户',
      request: {
        headers: 'Content-Type: application/json',
        params: `{\n  \"username\": \"string\",  // 用户名（必填）\n  \"password\": \"string\",  // 密码（必填）\n  \"email\": \"string\"      // 邮箱（可选）\n}`
      },
      response: `{\n  \"token\": \"string\",     // 访问令牌\n  \"user\": {              // 用户信息\n    \"id\": 123,           // 用户ID\n    \"username\": \"string\",// 用户名\n    \"email\": \"string\",   // 邮箱\n    \"level\": 0,          // 用户等级（0:普通用户 1:进阶用户）\n    \"storageQuota\": 123, // 存储配额（字节）\n    \"usedStorage\": 123,  // 已使用存储空间（字节）\n    \"lastLoginTime\": \"2025-09-11T10:00:00\", // 最后登录时间\n    \"gmtCreate\": \"2025-09-11T10:00:00\",     // 创建时间\n    \"gmtModified\": \"2025-09-11T10:00:00\",   // 修改时间\n    \"isDeleted\": 0       // 是否删除（0:未删除 1:已删除）\n  },\n  \"success\": true,       // 是否成功\n  \"message\": \"string\"    // 错误信息\n}`
    },
    {
      key: '1-2',
      name: '用户登录',
      method: 'POST',
      path: '/api/auth/login',
      description: '用户登录获取访问令牌',
      request: {
        headers: 'Content-Type: application/json',
        params: `{\n  \"username\": \"string\",  // 用户名（必填）\n  \"password\": \"string\"   // 密码（必填）\n}`
      },
      response: `{\n  \"token\": \"string\",     // 访问令牌\n  \"user\": {              // 用户信息\n    \"id\": 123,           // 用户ID\n    \"username\": \"string\",// 用户名\n    \"email\": \"string\",   // 邮箱\n    \"level\": 0,          // 用户等级（0:普通用户 1:进阶用户）\n    \"storageQuota\": 123, // 存储配额（字节）\n    \"usedStorage\": 123,  // 已使用存储空间（字节）\n    \"lastLoginTime\": \"2025-09-11T10:00:00\", // 最后登录时间\n    \"gmtCreate\": \"2025-09-11T10:00:00\",     // 创建时间\n    \"gmtModified\": \"2025-09-11T10:00:00\",   // 修改时间\n    \"isDeleted\": 0       // 是否删除（0:未删除 1:已删除）\n  },\n  \"success\": true,       // 是否成功\n  \"message\": \"string\"    // 错误信息\n}`
    }
  ];

  // 文档管理接口数据
  const documentEndpoints = [
    {
      key: '2-1',
      name: '上传文档',
      method: 'POST',
      path: '/api/documents/upload',
      description: '上传新文档到知识库',
      request: {
        headers: 'Content-Type: multipart/form-data\nAuthorization: Bearer {access_token}',
        params: `file: 文件（必填）\ntitle: 文档标题（可选）\ndescription: 文档描述（可选）\ntags: 标签（可选，逗号分隔）`
      },
      response: `{\n  \"id\": \"string\",        // 文档ID\n  \"title\": \"string\",     // 文档标题\n  \"description\": \"string\",// 文档描述\n  \"fileName\": \"string\",  // 文件名\n  \"fileType\": \"string\",  // 文件类型\n  \"fileSize\": 123,       // 文件大小（字节）\n  \"tags\": \"string\",      // 标签\n  \"uploadTime\": \"2025-09-11T10:00:00\", // 上传时间\n  \"chunkCount\": 123      // 分块数量\n}`
    },
    {
      key: '2-2',
      name: '获取所有文档',
      method: 'GET',
      path: '/api/documents',
      description: '获取用户所有上传的文档列表',
      request: {
        headers: 'Authorization: Bearer {access_token}',
        params: ''
      },
      response: `[\n  {\n    \"id\": \"string\",        // 文档ID\n    \"title\": \"string\",     // 文档标题\n    \"description\": \"string\",// 文档描述\n    \"fileName\": \"string\",  // 文件名\n    \"fileType\": \"string\",  // 文件类型\n    \"fileSize\": 123,       // 文件大小（字节）\n    \"tags\": \"string\",      // 标签\n    \"uploadTime\": \"2025-09-11T10:00:00\", // 上传时间\n    \"chunkCount\": 123      // 分块数量\n  }\n]`
    },
    {
      key: '2-3',
      name: '删除文档',
      method: 'DELETE',
      path: '/api/documents/{documentId}',
      description: '删除指定文档',
      request: {
        headers: 'Authorization: Bearer {access_token}',
        params: 'documentId: 文档ID'
      },
      response: '无'
    }
  ];

  // 搜索接口数据
  const searchEndpoints = [
    {
      key: '3-1',
      name: '知识库搜索',
      method: 'POST',
      path: '/api/search',
      description: '在知识库中进行语义或关键词搜索',
      request: {
        headers: 'Content-Type: application/json\nAuthorization: Bearer {access_token}',
        params: `{\n  \"query\": \"string\",      // 搜索内容（必填）\n  \"searchType\": \"SEMANTIC\",// 搜索类型（可选，默认SEMANTIC）\n  \"maxResults\": 10,       // 最大结果数（可选，默认10）\n  \"minScore\": 0.7         // 最小相似度分数（可选，默认0.7）\n}`
      },
      response: `[\n  {\n    \"fileRecordId\": \"string\",  // 文件记录ID\n    \"title\": \"string\",       // 文档标题\n    \"content\": \"string\",     // 匹配内容\n    \"score\": 0.8,            // 相似度分数\n    \"source\": \"string\",      // 来源\n    \"position\": 123          // 位置\n  }\n]`
    },
    {
      key: '3-2',
      name: '简单搜索',
      method: 'GET',
      path: '/api/search/simple',
      description: '简单的搜索接口',
      request: {
        headers: 'Authorization: Bearer {access_token}',
        params: `query: 搜索内容（必填）\ntype: 搜索类型（可选，默认SEMANTIC）\nlimit: 最大结果数（可选，默认10）`
      },
      response: `[\n  {\n    \"fileRecordId\": \"string\",  // 文件记录ID\n    \"title\": \"string\",       // 文档标题\n    \"content\": \"string\",     // 匹配内容\n    \"score\": 0.8,            // 相似度分数\n    \"source\": \"string\",      // 来源\n    \"position\": 123          // 位置\n  }\n]`
    }
  ];

  // 问答接口数据
  const qaEndpoints = [
    {
      key: '4-1',
      name: '智能问答',
      method: 'POST',
      path: '/api/search/ask',
      description: '基于知识库内容进行智能问答',
      request: {
        headers: 'Authorization: Bearer {access_token}',
        params: `question: 问题内容（必填）\nsearchType: 搜索类型（可选，默认SEMANTIC）\nmaxResults: 最大结果数（可选，默认10）\nminScore: 最小相似度分数（可选，默认0.7）`
      },
      response: `{\n  \"question\": \"string\",      // 问题内容\n  \"answer\": \"string\",        // 回答内容\n  \"sources\": [               // 来源信息\n    {\n      \"fileRecordId\": \"string\",  // 文件记录ID\n      \"title\": \"string\",       // 文档标题\n      \"content\": \"string\",     // 匹配内容\n      \"score\": 0.8,            // 相似度分数\n      \"source\": \"string\",      // 来源\n      \"position\": 123          // 位置\n    }\n  ],\n  \"sourceCount\": 123         // 来源数量\n}`
    }
  ];

  // 渲染接口详情
  const renderEndpointDetails = (endpoint) => (
    <Card 
      key={endpoint.key} 
      title={
        <span>
          <Tag color={endpoint.method === 'GET' ? 'green' : endpoint.method === 'POST' ? 'blue' : endpoint.method === 'DELETE' ? 'red' : 'orange'}>
            {endpoint.method}
          </Tag>
          {endpoint.name}
        </span>
      } 
      size="small"
      style={{ marginBottom: 16 }}
    >
      <Paragraph strong>接口地址: <code>{endpoint.path}</code></Paragraph>
      <Paragraph>{endpoint.description}</Paragraph>
      
      {endpoint.request.headers && (
        <>
          <Paragraph strong>请求头:</Paragraph>
          <pre style={{ background: '#f5f5f5', padding: '8px', borderRadius: '4px' }}>{endpoint.request.headers}</pre>
        </>
      )}
      
      {endpoint.request.params && (
        <>
          <Paragraph strong>请求参数:</Paragraph>
          <pre style={{ background: '#f5f5f5', padding: '8px', borderRadius: '4px' }}>{endpoint.request.params}</pre>
        </>
      )}
      
      <Paragraph strong>响应参数:</Paragraph>
      <pre style={{ background: '#f5f5f5', padding: '8px', borderRadius: '4px' }}>{endpoint.response}</pre>
    </Card>
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={250} theme="light">
        <Menu
          mode="inline"
          defaultSelectedKeys={['1']}
          style={{ height: '100%', borderRight: 0 }}
          items={[
            {
              key: '1',
              icon: <UserOutlined />,
              label: '认证接口',
            },
            {
              key: '2',
              icon: <FileOutlined />,
              label: '文档管理接口',
            },
            {
              key: '3',
              icon: <SearchOutlined />,
              label: '搜索接口',
            },
            {
              key: '4',
              icon: <QuestionCircleOutlined />,
              label: '问答接口',
            },
          ]}
        />
      </Sider>
      <Layout style={{ padding: '0 24px 24px' }}>
        <Content
          style={{
            padding: 24,
            margin: 0,
            minHeight: 280,
          }}
        >
          <Title level={2}>RAG知识库系统API接口文档</Title>
          
          <Divider orientation="left">1. 认证接口</Divider>
          <List
            dataSource={authEndpoints}
            renderItem={renderEndpointDetails}
          />
          
          <Divider orientation="left">2. 文档管理接口</Divider>
          <List
            dataSource={documentEndpoints}
            renderItem={renderEndpointDetails}
          />
          
          <Divider orientation="left">3. 搜索接口</Divider>
          <List
            dataSource={searchEndpoints}
            renderItem={renderEndpointDetails}
          />
          
          <Divider orientation="left">4. 问答接口</Divider>
          <List
            dataSource={qaEndpoints}
            renderItem={renderEndpointDetails}
          />
          
          <Divider orientation="left">5. 错误码说明</Divider>
          <Card size="small">
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>状态码</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>说明</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>200</td>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>请求成功</td>
                </tr>
                <tr>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>400</td>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>请求参数错误或业务处理失败</td>
                </tr>
                <tr>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>401</td>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>未授权访问</td>
                </tr>
                <tr>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>500</td>
                  <td style={{ border: '1px solid #ddd', padding: '8px' }}>服务器内部错误</td>
                </tr>
              </tbody>
            </table>
          </Card>
          
          <Divider orientation="left">6. 认证方式</Divider>
          <Card size="small">
            <Paragraph>
              除注册和登录接口外，其他接口都需要在请求头中添加认证信息：
            </Paragraph>
            <pre style={{ background: '#f5f5f5', padding: '8px', borderRadius: '4px' }}>
              Authorization: Bearer {'{access_token}'}
            </pre>
            <Paragraph>
              其中 {'{access_token}'} 为用户登录成功后返回的token值。
            </Paragraph>
          </Card>
        </Content>
      </Layout>
    </Layout>
  );
};

export default ApiDocumentation;