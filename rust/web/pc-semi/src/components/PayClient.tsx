'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Radio, RadioGroup, Toast } from '@douyinfe/semi-ui';
import { apiGet } from '@/lib/client-api';

interface PayInfo {
  order?: {
    orderId?: number;
    orderSn?: string;
    unpaidAmount?: number | string;
    payTypeId?: number;
  };
  paymentList?: Array<{ type?: string; name?: string; typeName?: string }>;
  offlinePaymentList?: Record<string, unknown>;
}

export default function PayClient({ orderId }: { orderId: number }) {
  const [info, setInfo] = useState<PayInfo | null>(null);
  const [payType, setPayType] = useState('');
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);

  useEffect(() => {
    apiGet<PayInfo>('order/pay/index', { id: orderId })
      .then((result) => {
        setInfo(result);
        setPayType(result.paymentList?.[0]?.type || '');
      })
      .catch((error) => Toast.error({ content: error instanceof Error ? error.message : '订单支付信息加载失败' }))
      .finally(() => setLoading(false));
  }, [orderId]);

  const pay = async () => {
    setPaying(true);
    try {
      const result = await apiGet<any>('order/pay/create', { id: orderId, type: payType });
      const url = result?.payUrl || result?.url || result?.data;
      if (typeof url === 'string' && url.startsWith('http')) {
        window.location.href = url;
        return;
      }
      Toast.info({ content: '支付通道返回成功，请在支付页继续操作' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '发起支付失败' });
    } finally {
      setPaying(false);
    }
  };

  if (loading) return <Card className="cart-state">订单支付信息加载中...</Card>;

  const order = info?.order || {};
  return (
    <div className="pc-pay">
      <Card className="pc-pay__card">
        <div className="pc-pay__success">订单已成功提交，请完成付款</div>
        <div className="pc-pay__amount">
          <span>需支付</span>
          <b>¥ {Number(order.unpaidAmount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</b>
        </div>
        <div className="pc-pay__order">
          <span>订单号</span>
          <b>{order.orderSn || '-'}</b>
        </div>

        {info?.paymentList?.length ? (
          <div className="pc-pay__methods">
            <div className="pc-pay__label">支付方式</div>
            <RadioGroup value={payType} onChange={(value) => setPayType(String(value))}>
              {info.paymentList.map((method) => (
                <Radio key={method.type} value={method.type}>
                  {method.name || method.typeName || method.type}
                </Radio>
              ))}
            </RadioGroup>
          </div>
        ) : null}

        <div className="pc-pay__actions">
          <Button theme="solid" type="primary" size="large" loading={paying} disabled={!payType} onClick={pay}>
            立即支付
          </Button>
          <Link href="/member/order/list">
            <Button size="large">查看订单</Button>
          </Link>
        </div>
      </Card>
    </div>
  );
}
