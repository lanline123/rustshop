'use client';

import { useState } from 'react';

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
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!username.trim() || !password) {
      setError('请输入账号和密码');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const csrf = await getCsrf();
      const result: any = await postJson(
        'user/login/signin',
        {
          loginType: 'password',
          username: username.trim(),
          password,
          mobile: '',
          mobileCode: '',
          verifyToken: '',
        },
        { 'X-CSRF-Token': csrf },
      );
      const token = result?.token || result?.accessToken;
      if (!token) throw new Error('登录接口未返回 token');
      localStorage.setItem('accessToken', token);
      document.cookie = `accessToken=${encodeURIComponent(token)}; path=/; max-age=604800; SameSite=Lax`;
      const params = new URLSearchParams(window.location.search);
      const redirect = params.get('redirect') || '/';
      window.location.replace(redirect.startsWith('/') ? redirect : '/');
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '登录失败');
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

      <div className="pc-login-card pc-login-native">
        <h2>登录</h2>
        <p>登录后可使用购物车、订单和会员服务</p>
        <form onSubmit={submit}>
          <label htmlFor="login-username">账号</label>
          <input
            id="login-username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            placeholder="请输入用户名/手机号"
            autoComplete="username"
          />
          <label htmlFor="login-password">密码</label>
          <input
            id="login-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="请输入密码"
            autoComplete="current-password"
          />
          {error ? <div className="pc-login-native__error">{error}</div> : null}
          <button type="submit" disabled={loading}>
            {loading ? '登录中...' : '登录'}
          </button>
        </form>
        <div className="pc-login-tip">
          还没有账号？<a href="/register">立即注册</a>
        </div>
      </div>
    </main>
  );
}
