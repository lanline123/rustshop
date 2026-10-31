'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Button, Card, Input, Toast, Typography } from '@douyinfe/semi-ui';

async function getCsrf(): Promise<string> {
  const response = await fetch('/api/common/csrf/create', { credentials: 'include' });
  const json = await response.json();
  if (!response.ok || (json.code !== undefined && json.code !== 0)) {
    throw new Error(json.msg || '获取 CSRF Token 失败');
  }
  return typeof json.data === 'string' ? json.data : json.data?.token || '';
}

export default function RegisterPage() {
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [nickname, setNickname] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!/^1\d{10}$/.test(mobile.trim())) {
      setError('请输入正确的手机号');
      return;
    }
    if (password.length < 6) {
      setError('密码至少 6 位');
      return;
    }
    if (password !== confirm) {
      setError('两次输入的密码不一致');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const csrf = await getCsrf();
      const response = await fetch('/api/user/regist/registAct', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', 'X-CSRF-Token': csrf },
        body: JSON.stringify({ mobile: mobile.trim(), password, nickname: nickname.trim() }),
      });
      const json = await response.json().catch(() => ({}));
      if (!response.ok || (json.code !== undefined && json.code !== 0)) {
        throw new Error(json.msg || json.message || '注册失败');
      }
      Toast.success({ content: '注册成功，请登录' });
      setTimeout(() => window.location.replace('/login'), 600);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '注册失败');
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
      <Card className="pc-login-card">
        <Typography.Title heading={3} style={{ marginBottom: 6 }}>注册账号</Typography.Title>
        <Typography.Text type="tertiary">手机号注册，注册后可直接登录</Typography.Text>
        <form onSubmit={submit} style={{ marginTop: 22 }}>
          <div className="pc-form-item">
            <Input size="large" placeholder="手机号" value={mobile} onChange={setMobile} />
          </div>
          <div className="pc-form-item">
            <Input size="large" placeholder="昵称（选填）" value={nickname} onChange={setNickname} />
          </div>
          <div className="pc-form-item">
            <Input size="large" mode="password" placeholder="密码（至少 6 位）" value={password} onChange={setPassword} />
          </div>
          <div className="pc-form-item">
            <Input size="large" mode="password" placeholder="确认密码" value={confirm} onChange={setConfirm} />
          </div>
          {error ? <div className="pc-login-error">{error}</div> : null}
          <Button htmlType="submit" theme="solid" type="primary" size="large" block loading={loading}>
            注册
          </Button>
        </form>
        <div className="pc-login-links">
          <Link href="/login">已有账号？去登录</Link>
          <Link href="/">返回首页</Link>
        </div>
      </Card>
    </main>
  );
}
