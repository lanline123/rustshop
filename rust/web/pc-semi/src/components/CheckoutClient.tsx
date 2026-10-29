'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Toast } from '@douyinfe/semi-ui';

interface CartItem {
  cartId: number;
  productName?: string;
  picThumb?: string;
  price?: string;
  quantity?: number;
  isChecked?: boolean;
}

export default function CheckoutClient({ cartIds }: { cartIds: number[] }) {
  const [items, setItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/cart/cart/list', { credentials: 'include' })
      .then((response) => response.json())
      .then((json) => {
        const groups = json?.data?.cartList || [];
        const all: CartItem[] = groups.flatMap((group: any) => group.carts || []);
        setItems(cartIds.length ? all.filter((item) => cartIds.includes(item.cartId)) : all.filter((item) => item.isChecked));
      })
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, [JSON.stringify(cartIds)]);

  const amount = useMemo(() => items.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0), [items]);

  return (
    <div className="pc-checkout">
      <Card className="pc-checkout__card" title="收货信息">
        <div className="pc-checkout__address">
          <div>
            <b>请选择收货地址</b>
            <p>地址管理将在下一阶段接入，当前可先确认商品和金额。</p>
          </div>
          <Button theme="light" type="primary">
            新增地址
          </Button>
        </div>
      </Card>

      <Card className="pc-checkout__card" title="商品清单">
        {loading ? (
          <p>加载中...</p>
        ) : (
          <div className="pc-checkout__items">
            {items.map((item) => (
              <div key={item.cartId} className="pc-checkout__item">
                <img src={item.picThumb || ''} alt={item.productName || ''} />
                <span>{item.productName}</span>
                <span>× {item.quantity}</span>
                <b>¥ {Number(item.price || 0).toFixed(2)}</b>
              </div>
            ))}
            {!items.length ? <p>没有可结算商品，请返回购物车重新选择。</p> : null}
          </div>
        )}
      </Card>

      <div className="pc-checkout__footer">
        <Link href="/cart">返回购物车</Link>
        <div>
          <span>
            应付金额：<b>¥ {amount.toFixed(2)}</b>
          </span>
          <Button
            theme="solid"
            type="primary"
            size="large"
            disabled={!items.length}
            onClick={() => Toast.info({ content: '支付与订单提交将在下一阶段接入' })}
          >
            提交订单
          </Button>
        </div>
      </div>
    </div>
  );
}
