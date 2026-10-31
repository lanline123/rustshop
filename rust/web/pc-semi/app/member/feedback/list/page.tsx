'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Input, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface FeedbackItem {
  id: number;
  title: string;
  content: string;
  statusText?: string;
  addTime?: string;
  reply?: { content?: string; addTime?: string; username?: string } | null;
}

export default function FeedbackPage() {
  const [items, setItems] = useState<FeedbackItem[]>([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);

  const load = async () => {
    try {
      const data = await apiGet<any>('user/feedback/list', { page: 1, size: 20 });
      const records = data?.records || data?.list || [];
      setItems(records);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '留言加载失败' });
    }
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async () => {
    if (!content.trim()) {
      Toast.warning({ content: '请输入留言内容' });
      return;
    }
    setLoading(true);
    try {
      await apiPost('user/feedback/submit', {
        type: 1,
        title: title.trim() || content.trim().slice(0, 20),
        content: content.trim(),
      });
      Toast.success({ content: '提交成功' });
      setTitle('');
      setContent('');
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '提交失败' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="pc-static-page">
      <div className="pc-container">
        <nav className="pc-breadcrumb">
          <Link href="/">首页</Link><span>/</span><Link href="/member">会员中心</Link><span>/</span><span>留言反馈</span>
        </nav>
        <h1 className="pc-cart-title">留言反馈</h1>
        <Card style={{ marginBottom: 16 }}>
          <Input value={title} onChange={setTitle} placeholder="标题（选填）" style={{ marginBottom: 10 }} />
          <Input value={content} onChange={setContent} placeholder="请输入您的留言内容" style={{ marginBottom: 10 }} />
          <Button theme="solid" type="primary" loading={loading} onClick={submit}>提交留言</Button>
        </Card>
        {items.length ? items.map((item) => (
          <Card key={item.id} style={{ marginBottom: 12 }}>
            <Typography.Title heading={5} style={{ margin: 0 }}>{item.title || '留言'}</Typography.Title>
            <Typography.Paragraph style={{ margin: '8px 0' }}>{item.content}</Typography.Paragraph>
            <Typography.Text type="tertiary">{item.addTime || ''} · {item.statusText || ''}</Typography.Text>
            {item.reply?.content ? (
              <div style={{ marginTop: 10, padding: 10, background: '#f8fafc', borderRadius: 8 }}>
                <b>平台回复：</b>{item.reply.content}
              </div>
            ) : null}
          </Card>
        )) : <Card className="cart-state">暂无留言</Card>}
      </div>
    </main>
  );
}
