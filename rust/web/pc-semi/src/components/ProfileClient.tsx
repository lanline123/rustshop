'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Form, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface Profile {
  userId?: number;
  username?: string;
  nickname?: string;
  avatar?: string;
  mobile?: string;
  email?: string;
  birthday?: string;
}

export default function ProfileClient() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [needLogin, setNeedLogin] = useState(false);

  useEffect(() => {
    apiGet<Profile>('user/user/detail')
      .then((result) => setProfile(result || {}))
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  }, []);

  const submit = async (values: Record<string, unknown>) => {
    setSaving(true);
    try {
      await apiPost('user/user/updateInformation', {
        nickname: values.nickname,
        email: values.email,
        birthday: values.birthday,
      });
      Toast.success({ content: '资料已更新' });
      setProfile((current) => ({ ...current, ...values }));
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Card className="member-card">资料加载中...</Card>;
  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后查看资料</h2>
        <Link href="/login?redirect=/member/profile">
          <Button theme="solid" type="primary" size="large">去登录</Button>
        </Link>
      </Card>
    );
  }

  return (
    <Card className="member-card member-profile">
      <Form
        labelPosition="top"
        initValues={{
          nickname: profile?.nickname || '',
          email: profile?.email || '',
          birthday: profile?.birthday || '',
        }}
        onSubmit={submit}
      >
        <Form.Input field="nickname" label="昵称" placeholder="请输入昵称" />
        <Form.Input field="email" label="邮箱" placeholder="请输入邮箱" />
        <Form.Input field="birthday" label="生日" placeholder="YYYY-MM-DD" />
        <div className="member-profile__readonly">
          <span>账号：{profile?.username || '-'}</span>
          <span>手机号：{profile?.mobile || '-'}</span>
        </div>
        <Button theme="solid" type="primary" htmlType="submit" loading={saving}>保存资料</Button>
      </Form>
    </Card>
  );
}
