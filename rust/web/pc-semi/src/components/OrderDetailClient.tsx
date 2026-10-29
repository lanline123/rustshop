'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface OrderDetail {
  orderId?: number;
  orderSn?: string;
  orderStatusName?: string;
  addTime?: string;
  payTime?: string;
  shippingTime?: string;
  consignee?: string;
  mobile?: string;
  detailedAddress?: string;
  userAddress?: string;
  shippingTypeName?: string;
  trackingNo?: string;
  logisticsName?: string;
  buyerNote?: string;
  items?: Array<Record<string, any>>;
  productAmount?: number;
  shippingFee?: number;
  couponAmount?: number;
  pointsAmount?: number;
  discountAmount?: number;
  totalAmount?: number;
  unpaidAmount?: number;
  availableActions?: Record<string, boolean>;
}

function money(value: unknown): string {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export default function OrderDetailClient({ orderId }: { orderId: number }) {
  const [detail, setDetail] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  const load = () => {
    apiGet<{ item?: OrderDetail }>('user/order/detail', { id: orderId })
      .then((result) => setDetail(result.item || null))
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [orderId]);

  const confirm = async () => {
    try {
      await apiPost('user/order/confirmReceipt', { orderId });
      Toast.success({ content: '已确认收货' });
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  if (loading) return <Card className="cart-state">订单详情加载中...</Card>;
  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后查看订单</h2>
        <Link href={`/login?redirect=/member/order/detail?id=${orderId}`}>
          <Button theme="solid" type="primary" size="large">去登录</Button>
        </Link>
      </Card>
    );
  }
  if (!detail) return <Card className="cart-state">订单不存在</Card>;

  return (
    <div className="member-order-detail">
      <Card className="member-card">
        <div className="member-order-detail__head">
          <div>
            <span className="member-order__status">{detail.orderStatusName || '订单'}</span>
            <h2>订单号：{detail.orderSn}</h2>
            <p>下单时间：{detail.addTime || '-'}</p>
          </div>
          <div className="member-order-detail__actions">
            {detail.availableActions?.toPay ? (
              <Link href={`/order/pay?id=${detail.orderId}`}>
                <Button theme="solid" type="primary">去支付</Button>
              </Link>
            ) : null}
            {detail.availableActions?.confirmReceipt ? (
              <Button onClick={() => void confirm()}>确认收货</Button>
            ) : null}
          </div>
        </div>
      </Card>

      <Card className="member-card" title="收货信息">
        <div className="member-order-detail__address">
          <b>{detail.consignee} {detail.mobile}</b>
          <span>{detail.detailedAddress || detail.userAddress || '-'}</span>
          <span>配送方式：{detail.shippingTypeName || '-'}</span>
          {detail.trackingNo ? <span>物流：{detail.logisticsName} {detail.trackingNo}</span> : null}
        </div>
      </Card>

      <Card className="member-card" title="商品清单">
        {(detail.items || []).map((item, index) => (
          <div key={index} className="member-order__item">
            <img src={item.picThumb || item.productPicThumb || ''} alt={item.productName || ''} />
            <div>
              <div>{item.productName}</div>
              {item.skuData ? <span>{item.skuData}</span> : null}
            </div>
            <b>¥ {money(item.price)}</b>
            <span>× {item.quantity}</span>
          </div>
        ))}
      </Card>

      <Card className="member-card" title="金额明细">
        <div className="member-order-detail__amounts">
          <span>商品金额 <b>¥ {money(detail.productAmount)}</b></span>
          <span>运费 <b>¥ {money(detail.shippingFee)}</b></span>
          <span>优惠券 <b>- ¥ {money(detail.couponAmount)}</b></span>
          <span>积分抵扣 <b>- ¥ {money(detail.pointsAmount)}</b></span>
          <span>优惠 <b>- ¥ {money(detail.discountAmount)}</b></span>
          <span className="total">应付金额 <b>¥ {money(detail.unpaidAmount || detail.totalAmount)}</b></span>
        </div>
      </Card>

      {detail.buyerNote ? (
        <Card className="member-card" title="买家留言">
          {detail.buyerNote}
        </Card>
      ) : null}

      <Link href="/member/order/list" className="member-order-detail__back">返回订单列表</Link>
    </div>
  );
}
