'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface Coupon {
  couponId: number;
  couponName?: string;
  couponMoney?: number | string;
  couponDiscount?: number | string;
  couponType?: number;
  couponDesc?: string;
  minOrderAmount?: number | string;
  isReceive?: number;
}

export default function CouponCenterClient() {
  const [rows, setRows] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  const load = () => {
    apiGet<Coupon[]>('home/home/getCoupon')
      .then((result) => setRows(Array.isArray(result) ? result : []))
      .catch(() => setRows([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const claim = async (coupon: Coupon) => {
    try {
      await apiPost('user/coupon/claim', { couponId: coupon.couponId });
      Toast.success({ content: '领取成功' });
      load();
    } catch (error) {
      const message = error instanceof Error ? error.message : '领取失败';
      if (message.includes('登录') || message.includes('401')) setNeedLogin(true);
      else Toast.error({ content: message });
    }
  };

  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后领取优惠券</h2>
        <Link href="/login?redirect=/coupon/list"><Button theme="solid" type="primary" size="large">去登录</Button></Link>
      </Card>
    );
  }

  return (
    <div className="coupon-center">
      {loading ? (
        <Card className="member-card">优惠券加载中...</Card>
      ) : rows.length ? (
        <div className="coupon-center__grid">
          {rows.map((coupon) => (
            <Card key={coupon.couponId} className="coupon-center__card">
              <div className="coupon-center__value">
                {coupon.couponType === 2 ? (
                  <b>{coupon.couponDiscount}<small>折</small></b>
                ) : (
                  <b><small>¥</small>{Number(coupon.couponMoney || 0).toFixed(0)}</b>
                )}
                <span>满 {Number(coupon.minOrderAmount || 0).toFixed(0)} 可用</span>
              </div>
              <b>{coupon.couponName || '优惠券'}</b>
              <span>{coupon.couponDesc || '全场通用'}</span>
              <Button theme="solid" type="primary" onClick={() => void claim(coupon)}>
                {coupon.isReceive ? '已领取' : '立即领取'}
              </Button>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="member-card">暂无可领取优惠券</Card>
      )}
    </div>
  );
}
