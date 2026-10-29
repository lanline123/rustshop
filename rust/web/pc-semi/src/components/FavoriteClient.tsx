'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface Favorite {
  collectId: number;
  productId: number;
  productName?: string;
  picThumb?: string;
  price?: string;
  productPrice?: string;
  productStock?: number;
}

export default function FavoriteClient() {
  const [rows, setRows] = useState<Favorite[]>([]);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  const load = () => {
    apiGet<{ records?: Favorite[] }>('user/collectProduct/list', { page: 1, size: 20 })
      .then((result) => setRows(result.records || []))
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const remove = async (productId: number) => {
    try {
      await apiPost('user/collectProduct/cancel', { id: productId });
      Toast.success({ content: '已取消收藏' });
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  if (needLogin) {
    return <Card className="cart-state"><h2>登录后查看收藏</h2><Link href="/login?redirect=/member/favorite"><Button theme="solid" type="primary" size="large">去登录</Button></Link></Card>;
  }

  return (
    <div className="member-favorite">
      {loading ? (
        <Card className="member-card">收藏加载中...</Card>
      ) : rows.length ? (
        <div className="member-favorite__grid">
          {rows.map((item) => (
            <Card key={item.collectId} className="member-favorite__card">
              <Link href={`/product/${item.productId}`}>
                <img src={item.picThumb || ''} alt={item.productName || ''} />
                <div className="member-favorite__name">{item.productName}</div>
              </Link>
              <div className="member-favorite__foot">
                <b>¥ {Number(item.price || item.productPrice || 0).toFixed(2)}</b>
                <Button size="small" theme="borderless" type="danger" onClick={() => void remove(item.productId)}>取消收藏</Button>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="member-card">暂无收藏商品</Card>
      )}
    </div>
  );
}
