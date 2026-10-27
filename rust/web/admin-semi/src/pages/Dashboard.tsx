import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Col,
  Row,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
} from '@douyinfe/semi-ui';
import {
  IconArrowUp,
  IconComment,
  IconCreditCard,
  IconFile,
  IconGift,
  IconOrderedList,
  IconShoppingBag,
  IconUser,
} from '@douyinfe/semi-icons';
import { apiGet, isDemo } from '../api';
import { mockDashboard, mockOrders } from '../mock';
import type { DashboardData, OrderRow } from '../types';
import type { PageKey } from '../layout/AdminLayout';
import TrendChart from '../components/TrendChart';

const { Text } = Typography;

const orderTagColor: Record<string, string> = {
  待付款: 'orange',
  待发货: 'blue',
  待收货: 'cyan',
  已完成: 'green',
  已取消: 'grey',
};

function money(value: number) {
  if (!Number.isFinite(value)) return '¥ 0';
  if (value >= 10000) return `¥ ${(value / 10000).toFixed(1)} 万`;
  return `¥ ${value.toFixed(2)}`;
}

export default function Dashboard({ onNavigate }: { onNavigate: (page: PageKey) => void }) {
  const [data, setData] = useState<DashboardData | null>(isDemo() ? mockDashboard : null);
  const [orders, setOrders] = useState<OrderRow[]>(isDemo() ? mockOrders : []);
  const [loading, setLoading] = useState(!isDemo());

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    Promise.all([
      apiGet<DashboardData>('/panel/panel/index'),
      apiGet<{ records: OrderRow[] }>('/order/order/list', { page: 1, size: 5 }),
    ])
      .then(([dashboard, orderPage]) => {
        if (!alive) return;
        setData(dashboard);
        setOrders(orderPage.records || []);
      })
      .catch(() => {
        if (!alive) return;
        setData(mockDashboard);
        setOrders(mockOrders);
      })
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  const stats = [
    {
      label: '今日订单',
      value: String(data?.consoleData.todayOrderNum ?? 0),
      growth: data?.orderGrowthRate ?? 0,
      icon: <IconOrderedList />,
      tone: 'blue',
    },
    {
      label: '今日成交额',
      value: money(data?.consoleData.todayOrderAmount ?? 0),
      growth: data?.orderAmountGrowthRate ?? 0,
      icon: <IconCreditCard />,
      tone: 'green',
    },
    {
      label: '在售商品',
      value: String(data?.consoleData.productOnSaleNum ?? 0),
      growth: 0,
      icon: <IconShoppingBag />,
      tone: 'purple',
    },
    {
      label: '今日访客',
      value: String(data?.consoleData.todayVisitNum ?? 0),
      growth: 0,
      icon: <IconUser />,
      tone: 'orange',
    },
  ];

  return (
    <Spin spinning={loading} wrapperClassName="full-width">
      <Space vertical align="start" spacing={16} style={{ width: '100%' }}>
        <Row gutter={16} style={{ width: '100%' }}>
          {stats.map((stat) => (
            <Col span={6} key={stat.label}>
              <Card shadows="hover" className={`stat-card tone-${stat.tone}`}>
                <div className="stat-row">
                  <span className="stat-icon">{stat.icon}</span>
                  <div className="stat-main">
                    <div className="stat-label">{stat.label}</div>
                    <div className="stat-value">{stat.value}</div>
                  </div>
                </div>
                <div className="stat-foot">
                  {stat.growth !== 0 ? (
                    <span className={stat.growth > 0 ? 'trend-up' : 'trend-down'}>
                      <IconArrowUp
                        size="small"
                        style={{ transform: stat.growth > 0 ? 'none' : 'rotate(180deg)' }}
                      />
                      {Math.abs(stat.growth).toFixed(1)}%
                    </span>
                  ) : (
                    <span className="trend-flat">—</span>
                  )}
                  <span className="stat-compare">较昨日</span>
                </div>
              </Card>
            </Col>
          ))}
        </Row>

        <Row gutter={16} style={{ width: '100%' }}>
          <Col span={16}>
            <Card
              title="成交趋势"
              headerExtraContent={<Text type="tertiary" size="small">近 7 日 · 示意数据</Text>}
              shadows="hover"
              className="chart-card"
            >
              <TrendChart height={276} />
            </Card>
          </Col>

          <Col span={8}>
            <Card
              title="待处理事项"
              headerExtraContent={
                <Button theme="borderless" size="small" onClick={() => onNavigate('orders')}>
                  全部
                </Button>
              }
              shadows="hover"
              className="todo-card"
            >
              {[
                { label: '待发货订单', value: data?.awaitShip ?? 0, page: 'orders' as PageKey, icon: <IconOrderedList /> },
                { label: '待付款订单', value: data?.awaitPay ?? 0, page: 'orders' as PageKey, icon: <IconCreditCard /> },
                { label: '待审核售后', value: data?.awaitAfterSale ?? 0, page: 'aftersales' as PageKey, icon: <IconComment /> },
                { label: '待回复评价', value: data?.awaitComment ?? 0, page: 'marketing' as PageKey, icon: <IconGift /> },
              ].map((item) => (
                <button
                  className="todo-row"
                  key={item.label}
                  onClick={() => onNavigate(item.page)}
                  type="button"
                >
                  <Space>
                    <span className="todo-icon">{item.icon}</span>
                    <span>{item.label}</span>
                  </Space>
                  <Tag color={(item.value > 0 ? 'orange' : 'grey') as any}>{item.value}</Tag>
                </button>
              ))}
            </Card>
          </Col>
        </Row>

        <Row gutter={16} style={{ width: '100%' }}>
          <Col span={16}>
            <Card
              title="最新订单"
              headerExtraContent={
                <Button theme="borderless" size="small" onClick={() => onNavigate('orders')}>
                  查看全部
                </Button>
              }
              shadows="hover"
              className="table-card"
            >
              <Table
                dataSource={orders}
                pagination={false}
                rowKey="orderId"
                columns={[
                  { title: '订单号', dataIndex: 'orderSn', render: (value: string) => <span className="mono">{value}</span> },
                  { title: '收货人', dataIndex: 'consignee' },
                  { title: '金额', dataIndex: 'totalAmount', render: (value: string) => `¥ ${value}` },
                  {
                    title: '状态',
                    dataIndex: 'orderStatusName',
                    render: (value: string) => <Tag color={(orderTagColor[value] || 'blue') as any}>{value}</Tag>,
                  },
                  { title: '下单时间', dataIndex: 'addTime' },
                ]}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card title="快捷入口" shadows="hover" className="quick-card">
              <div className="quick-grid">
                {[
                  { label: '商品管理', page: 'products' as PageKey, icon: <IconShoppingBag /> },
                  { label: '订单处理', page: 'orders' as PageKey, icon: <IconOrderedList /> },
                  { label: '内容管理', page: 'content' as PageKey, icon: <IconFile /> },
                  { label: '会员管理', page: 'members' as PageKey, icon: <IconUser /> },
                ].map((item) => (
                  <button className="quick-item" key={item.label} onClick={() => onNavigate(item.page)} type="button">
                    <span className="quick-icon">{item.icon}</span>
                    <span>{item.label}</span>
                  </button>
                ))}
              </div>
            </Card>
          </Col>
        </Row>
      </Space>
    </Spin>
  );
}
