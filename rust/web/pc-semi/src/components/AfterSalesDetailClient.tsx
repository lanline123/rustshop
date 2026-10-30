'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Modal, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface AfterSaleDetail {
  aftersaleId?: number;
  aftersalesSn?: string;
  aftersalesTypeName?: string;
  statusName?: string;
  addTime?: string;
  refundAmount?: string;
  description?: string;
  reply?: string;
  returnAddress?: string;
  trackingNo?: string;
  statusDesc?: string;
  aftersalesItems?: Array<Record<string, any>>;
  aftersalesLog?: Array<{ addTime?: string; logInfo?: string }>;
  stepStatus?: { steps?: Array<{ title?: string; active?: boolean; description?: string }> };
  canCancel?: boolean;
}

export default function AfterSalesDetailClient({ id }: { id: number }) {
  const [detail, setDetail] = useState<AfterSaleDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  useEffect(() => {
    apiGet<AfterSaleDetail>('user/aftersales/detail', { id })
      .then((result) => setDetail(result || null))
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  }, [id]);

  const cancel = () => {
    Modal.confirm({
      title: '取消售后申请',
      content: '确认取消该售后申请？',
      onOk: async () => {
        try {
          await apiPost('user/aftersales/cancel', { aftersaleId: id });
          Toast.success({ content: '售后申请已取消' });
          window.location.reload();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '取消失败' });
        }
      },
    });
  };

  if (loading) return <Card className="cart-state">售后详情加载中...</Card>;
  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后查看售后详情</h2>
        <Link href={`/login?redirect=/member/aftersales/detail?id=${id}`}><Button theme="solid" type="primary">去登录</Button></Link>
      </Card>
    );
  }
  if (!detail) return <Card className="cart-state">售后单不存在</Card>;

  return (
    <div className="member-aftersales-detail">
      <Card className="member-card">
        <div className="member-aftersales__head">
          <b>{detail.aftersalesTypeName || '售后申请'}</b>
          <span>{detail.statusName || '处理中'}</span>
          <span>{detail.addTime}</span>
        </div>
        <div className="member-aftersales__meta">
          <span>售后单号：{detail.aftersalesSn || '-'}</span>
          <span>退款金额：¥ {Number(detail.refundAmount || 0).toFixed(2)}</span>
        </div>
        {detail.statusDesc ? <p className="member-aftersales__desc">{detail.statusDesc}</p> : null}
        {detail.canCancel ? (
          <div className="member-aftersales__actions">
            <Button type="danger" size="small" onClick={cancel}>取消售后</Button>
          </div>
        ) : null}
      </Card>

      <Card className="member-card" title="售后商品">
        {(detail.aftersalesItems || []).map((item, index) => (
          <div key={index} className="member-aftersales__item">
            <img src={item.picThumb || ''} alt={item.productName || ''} />
            <span>{item.productName}</span>
            <span>× {item.quantity}</span>
          </div>
        ))}
      </Card>

      <Card className="member-card" title="售后信息">
        <div className="member-aftersales-detail__info">
          <span>申请描述：{detail.description || '-'}</span>
          <span>商家回复：{detail.reply || '-'}</span>
          {detail.returnAddress ? <span>退货地址：{detail.returnAddress}</span> : null}
          {detail.trackingNo ? <span>退货物流：{detail.trackingNo}</span> : null}
        </div>
      </Card>

      {detail.aftersalesLog?.length ? (
        <Card className="member-card" title="处理进度">
          {detail.aftersalesLog.map((log, index) => (
            <div key={index} className="member-aftersales-detail__log">
              <span>{log.addTime}</span>
              <b>{log.logInfo}</b>
            </div>
          ))}
        </Card>
      ) : null}

      <Link href="/member/aftersales" className="member-order-detail__back">返回售后列表</Link>
    </div>
  );
}
