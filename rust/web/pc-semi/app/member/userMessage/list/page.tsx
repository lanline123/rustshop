'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface MessageItem {
  messageId?: number;
  title?: string;
  content?: string;
  addTime?: string;
  isRead?: number;
  type?: number;
}

export default function UserMessagePage() {
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);

  const load = async (nextPage: number) => {
    setLoading(true);
    try {
      const data = await apiGet<any>('user/message/list', { page: nextPage, size: 10 });
      const records = data?.records || data?.list || [];
      setMessages(records);
      setTotal(Number(data?.total || records.length || 0));
      setPage(nextPage);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '消息加载失败' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(1);
  }, []);

  const markAllRead = async () => {
    try {
      await apiPost('user/message/updateAllRead', {});
      Toast.success({ content: '已全部标为已读' });
      load(page);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  const remove = async (messageId?: number) => {
    if (!messageId) return;
    try {
      await apiPost('user/message/del', { messageId });
      Toast.success({ content: '已删除' });
      load(page);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
    }
  };

  const totalPages = Math.max(1, Math.ceil(total / 10));

  return (
    <main className="pc-static-page">
      <div className="pc-container">
        <nav className="pc-breadcrumb">
          <Link href="/">首页</Link><span>/</span><Link href="/member">会员中心</Link><span>/</span><span>站内消息</span>
        </nav>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1 className="pc-cart-title">站内消息</h1>
          <Button onClick={markAllRead}>全部已读</Button>
        </div>
        {loading ? <Card className="cart-state">加载中...</Card> : null}
        {!loading && !messages.length ? (
          <Card className="cart-state">暂无消息</Card>
        ) : null}
        {messages.map((item) => (
          <Card key={item.messageId} style={{ marginBottom: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16 }}>
              <div>
                <Typography.Title heading={5} style={{ margin: 0 }}>
                  {item.title || '消息'}
                  {item.isRead ? null : <span style={{ color: '#dc2626', marginLeft: 6, fontSize: 12 }}>未读</span>}
                </Typography.Title>
                <Typography.Paragraph style={{ margin: '8px 0 0', whiteSpace: 'pre-wrap' }}>
                  {item.content || ''}
                </Typography.Paragraph>
                <Typography.Text type="tertiary">{item.addTime || ''}</Typography.Text>
              </div>
              <Button size="small" type="danger" onClick={() => remove(item.messageId)}>删除</Button>
            </div>
          </Card>
        ))}
        {messages.length ? (
          <div className="pc-pagination">
            {page > 1 ? <button type="button" onClick={() => load(page - 1)}>上一页</button> : <span />}
            <span>第 {page} / {totalPages} 页</span>
            {page < totalPages ? <button type="button" onClick={() => load(page + 1)}>下一页</button> : <span />}
          </div>
        ) : null}
      </div>
    </main>
  );
}
