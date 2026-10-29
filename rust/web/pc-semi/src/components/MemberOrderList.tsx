'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Input, Modal, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface OrderItem {
  productName?: string;
  picThumb?: string;
  productPicThumb?: string;
  quantity?: number;
  price?: string;
  skuData?: string;
}

interface OrderRow {
  orderId: number;
  orderSn?: string;
  orderStatusName?: string;
  totalAmount?: string;
  unpaidAmount?: string;
  addTime?: string;
  items?: OrderItem[];
  availableActions?: Record<string, boolean>;
}

const TABS = [
  { key: 'all', label: '全部', orderStatus: -1, commentStatus: -1 },
  { key: 'awaitPay', label: '待付款', orderStatus: 0, commentStatus: -1 },
  { key: 'awaitShipping', label: '待发货', orderStatus: 1, commentStatus: -1 },
  { key: 'awaitReceived', label: '待收货', orderStatus: 2, commentStatus: -1 },
  { key: 'awaitComment', label: '待评价', orderStatus: -1, commentStatus: 0 },
];

function money(value: unknown): string {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export default function MemberOrderList() {
  const [tab, setTab] = useState('all');
  const [keyword, setKeyword] = useState('');
  const [rows, setRows] = useState<OrderRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  const load = () => {
    const current = TABS.find((item) => item.key === tab) || TABS[0];
    setLoading(true);
    apiGet<{ records?: OrderRow[]; total?: number }>('user/order/list', {
      page,
      size: 10,
      sortField: 'addTime',
      sortOrder: 'desc',
      keyword,
      payStatus: -1,
      orderStatus: current.orderStatus,
      commentStatus: current.commentStatus,
      dateType: 1,
    })
      .then((result) => {
        setRows(result.records || []);
        setTotal(Number(result.total || 0));
      })
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
        setRows([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [tab, page]);

  const confirmReceipt = async (orderId: number) => {
    try {
      await apiPost('user/order/confirmReceipt', { orderId });
      Toast.success({ content: '已确认收货' });
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  const cancelOrder = (orderId: number) => {
    Modal.confirm({
      title: '取消订单',
      content: '取消后订单将关闭，已支付金额按原路退回（如有）。',
      okText: '确认取消',
      cancelText: '再想想',
      onOk: async () => {
        try {
          await apiPost('user/order/cancelOrder', { id: orderId, description: '用户主动取消' });
          Toast.success({ content: '订单已取消' });
          load();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '取消失败' });
        }
      },
    });
  };

  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后查看订单</h2>
        <p>登录后可查看订单状态、物流和售后。</p>
        <Link href="/login?redirect=/member/order/list">
          <Button theme="solid" type="primary" size="large">去登录</Button>
        </Link>
      </Card>
    );
  }

  return (
    <div className="member-orders">
      <div className="member-orders__toolbar">
        <div className="member-orders__tabs">
          {TABS.map((item) => (
            <button
              key={item.key}
              type="button"
              className={item.key === tab ? 'is-active' : ''}
              onClick={() => {
                setPage(1);
                setTab(item.key);
              }}
            >
              {item.label}
            </button>
          ))}
        </div>
        <div className="member-orders__search">
          <Input value={keyword} onChange={setKeyword} placeholder="收货人名称或订单号" onEnterPress={() => { setPage(1); load(); }} />
          <Button onClick={() => { setPage(1); load(); }}>搜索</Button>
        </div>
      </div>

      {loading ? (
        <Card className="member-card">订单加载中...</Card>
      ) : rows.length ? (
        <div className="member-orders__list">
          {rows.map((order) => (
            <Card key={order.orderId} className="member-order">
              <div className="member-order__head">
                <span className="member-order__status">{order.orderStatusName || '订单'}</span>
                <span>{order.addTime}</span>
                <span>订单号：{order.orderSn}</span>
              </div>
              <div className="member-order__items">
                {(order.items || []).map((item, index) => (
                  <div key={index} className="member-order__item">
                    <img src={item.productPicThumb || item.picThumb || ''} alt={item.productName || ''} />
                    <div>
                      <div>{item.productName}</div>
                      {item.skuData ? <span>{item.skuData}</span> : null}
                    </div>
                    <b>¥ {money(item.price)}</b>
                    <span>× {item.quantity}</span>
                  </div>
                ))}
              </div>
              <div className="member-order__foot">
                <span>
                  合计：<b>¥ {money(order.totalAmount)}</b>
                </span>
                <div>
                  {order.availableActions?.toPay ? (
                    <Link href={`/order/pay?id=${order.orderId}`}>
                      <Button theme="solid" type="primary" size="small">去支付</Button>
                    </Link>
                  ) : null}
                  {order.availableActions?.confirmReceipt ? (
                    <Button size="small" onClick={() => void confirmReceipt(order.orderId)}>确认收货</Button>
                  ) : null}
                  {order.availableActions?.cancelOrder ? (
                    <Button size="small" onClick={() => cancelOrder(order.orderId)}>取消订单</Button>
                  ) : null}
                  <Link href={`/member/order/detail?id=${order.orderId}`}>
                    <Button size="small" theme="borderless" type="primary">订单详情</Button>
                  </Link>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="member-card">暂无订单</Card>
      )}

      {total > 10 ? (
        <div className="pc-pagination">
          {Array.from({ length: Math.ceil(total / 10) }).map((_, index) => (
            <button key={index} type="button" className={index + 1 === page ? 'is-active' : ''} onClick={() => setPage(index + 1)}>
              {index + 1}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
