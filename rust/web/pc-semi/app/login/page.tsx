'use client';

import { useState } from 'react';
import { Button, Card, Form, Input, Toast, Typography } from '@douyinfe/semi-ui';
import { IconLock, IconUser } from '@douyinfe/semi-icons';

const { Title, Text } = Typography;

async function postJson(path: string, body: unknown, headers: Record<string, string> = {}) {
  const response = await fetch(`/api/${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...headers },
    body: JSON.stringify(body),
  });
  const text = await response.text();
  const json = text ? JSON.parse(text) : {};
  if (!response.ok || (json.code !== undefined && json.code !== 0)) {
    throw new Error(json.msg || json.message || `请求失败（${response.status}）`);
  }
  return json.data ?? json;
}

async function getCsrf(): Promise<string> {
  const response = await fetch('/api/common/csrf/create', { credentials: 'include' });
  const json = await response.json();
  if (!response.ok || (json.code !== undefined && json.code !== 0)) {
    throw new Error(json.msg || '获取 CSRF Token 失败');
  }
  return typeof json.data === 'string' ? json.data : json.data?.token || '';
}

export default function LoginPage() {
  const [loading, setLoading] = useState(false);

  const submit = async (values: Record<string, unknown>) => {
    const username = String(values.username || '').trim();
    const password = String(values.password || '');
    if (!username || !password) {
      Toast.warning({ content: '请输入账号和密码' });
      return;
    }
    setLoading(true);
    try {
      const csrf = await getCsrf();
      const result: any = await postJson(
        'user/login/signin',
        {
          loginType: 'password',
          username,
          password,
          mobile: '',
          mobileCode: '',
          verifyToken: '',
        },
        { 'X-CSRF-Token': csrf },
      );
      if (result?.token) {
        localStorage.setItem('accessToken', result.token);
        document.cookie = `accessToken=${encodeURIComponent(result.token)}; path=/; max-age=604800; SameSite=Lax`;
      }
      Toast.success({ content: '登录成功' });
      const params = new URLSearchParams(window.location.search);
      const redirect = params.get('redirect') || '/';
      window.location.href = redirect.startsWith('/') ? redirect : '/';
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '登录失败' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="pc-login-page">
      <div className="pc-login-brand">
        <div className="pc-login-brand__logo">W</div>
        <h1>万物新生</h1>
        <p>退役动力电池循环利用产业服务平台</p>
        <div className="pc-login-brand__points">
          <span>安全合规</span>
          <span>高效协同</span>
          <span>绿色循环</span>
        </div>
      </div>

      <Card className="pc-login-card" shadows="always">
        <Title heading={3} style={{ marginBottom: 4 }}>
          登录
        </Title>
        <Text type="tertiary">登录后可使用购物车、订单和会员服务</Text>
        <Form onSubmit={submit} labelPosition="top" style={{ marginTop: 26 }}>
          <Form.Input
            field="username"
            label="账号"
            placeholder="请输入用户名/手机号"
            prefix={<IconUser />}
            size="large"
            rules={[{ required: true, message: '请输入账号' }]}
          />
          <Form.Input
            field="password"
            label="密码"
            mode="password"
            placeholder="请输入密码"
            prefix={<IconLock />}
            size="large"
            rules={[{ required: true, message: '请输入密码' }]}
          />
          <Button theme="solid" type="primary" htmlType="submit" block size="large" loading={loading} style={{ height: 44, marginTop: 12 }}>
            登录
          </Button>
        </Form>
        <div className="pc-login-tip">
          还没有账号？<a href="/register">立即注册</a>
        </div>
      </Card>
    </main>
  );
}
