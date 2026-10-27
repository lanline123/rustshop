import { useState } from 'react';
import { Button, Card, Form, Toast, Typography } from '@douyinfe/semi-ui';
import { IconLock, IconUser } from '@douyinfe/semi-icons';
import { enterDemo, login, setToken } from '../api';

const { Title, Text } = Typography;

export default function Login({ onSuccess }: { onSuccess: () => void }) {
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: Record<string, unknown>) => {
    const username = String(values.username || '').trim();
    const password = String(values.password || '');
    if (!username || !password) {
      Toast.warning({ content: '请输入账号和密码' });
      return;
    }
    setLoading(true);
    try {
      const result = await login(username, password);
      setToken(result.token);
      Toast.success({ content: '登录成功' });
      onSuccess();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '登录失败' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-brand">
        <div className="login-logo">W</div>
        <Title heading={1} style={{ color: '#fff', margin: '28px 0 14px', fontSize: 40 }}>
          万物新生
        </Title>
        <Text style={{ color: 'rgba(255,255,255,.78)', fontSize: 17, lineHeight: '30px' }}>
          退役动力电池循环利用
          <br />
          产业服务平台
        </Text>
        <div className="login-brand-line" />
        <div className="login-points">
          {['安全合规', '高效协同', '绿色循环', '数据可追溯'].map((item) => (
            <span key={item}>{item}</span>
          ))}
        </div>
      </div>

      <Card className="login-card" shadows="always">
        <Title heading={3} style={{ marginBottom: 4 }}>
          运营管理后台
        </Title>
        <Text type="tertiary">请使用管理员账号登录</Text>

        <Form onSubmit={handleSubmit} labelPosition="top" style={{ marginTop: 28 }}>
          <Form.Input
            field="username"
            label="账号"
            placeholder="请输入管理员账号"
            prefix={<IconUser />}
            rules={[{ required: true, message: '请输入账号' }]}
            size="large"
          />
          <Form.Input
            field="password"
            label="密码"
            mode="password"
            placeholder="请输入密码"
            prefix={<IconLock />}
            rules={[{ required: true, message: '请输入密码' }]}
            size="large"
          />
          <Button
            theme="solid"
            type="primary"
            htmlType="submit"
            loading={loading}
            block
            size="large"
            style={{ marginTop: 12, height: 44 }}
          >
            登录
          </Button>
        </Form>

        <div className="login-demo">
          <Button
            theme="borderless"
            type="tertiary"
            onClick={() => {
              enterDemo();
              onSuccess();
            }}
          >
            进入演示模式
          </Button>
        </div>

        <div className="login-footer">
          <Text type="tertiary" size="small">
            万物新生 · 运营管理后台
          </Text>
        </div>
      </Card>
    </div>
  );
}
