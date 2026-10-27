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
  IconGift,
  IconOrderedList,
  IconShoppingBag,
  IconUser,
} from '@douyinfe/semi-icons';
import { apiGet, isDemo } from '../api';
import { mockDashboard, mockOrders } from '../mock';
import type { DashboardData, OrderRow } from '../types';
import type { PageKey } from '../layout/AdminLayout';

const { Title, Text } = Typography;

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
    },
    {
      label: '今日成交额',
      value: money(data?.consoleData.todayOrderAmount ?? 0),
      growth: data?.orderAmountGrowthRate ?? 0,
      icon: <IconCreditCard />,
    },
    {
      label: '在售商品',
      value: String(data?.consoleData.productOnSaleNum ?? 0),
      growth: 0,
      icon: <IconShoppingBag />,
    },
    {
      label: '今日访客',
      value: String(data?.consoleData.todayVisitNum ?? 0),
      growth: 0,
      icon: <IconUser />,
    },
  ];

  return (
    <Spin spinning={loading} wrapperClassName="full-width">
      <Space vertical align="start" spacing={20} style={{ width: '100%' }}>
        <div className="welcome-row">
          <div>
            <Title heading={3} style={{ margin: 0 }}>
              欢迎回来，admin
            </Title>
            <Text type="tertiary">这是万物新生平台今天的运营概况。</Text>
          </div>
          <Space>
            <Button icon={<IconShoppingBag />} onClick={() => onNavigate('products')}>
              商品管理
            </Button>
            <Button
              theme="solid"
              type="primary"
              icon={<IconOrderedList />}
              onClick={() => onNavigate('orders')}
            >
              处理订单
            </Button>
          </Space>
        </div>

        <Row gutter={16} style={{ width: '100%' }}>
          {stats.map((stat) => (
            <Col span={6} key={stat.label}>
              <Card shadows="hover" className="stat-card">
                <div className="stat-top">
                  <span className="stat-icon">{stat.icon}</span>
                  {stat.growth !== 0 && (
                    <span className={stat.growth > 0 ? 'trend-up' : 'trend-down'}>
                      <IconArrowUp size="small" style={{ transform: stat.growth > 0 ? 'none' : 'rotate(180deg)' }} />
                      {Math.abs(stat.growth).toFixed(1)}%
                    </span>
                  )}
                </div>
                <Text type="tertiary">{stat.label}</Text>
                <Title heading={2} style={{ margin: '8px 0 0' }}>
                  {stat.value}
                </Title>
              </Card>
            </Col>
          ))}
        </Row>

        <Row gutter={16} style={{ width: '100%' }}>
          <Col span={15}>
            <Card
              title="近 7 日成交趋势"
              headerExtraContent={<Text type="tertiary">示意数据，接入统计接口后替换</Text>}
              shadows="hover"
            >
              <div className="chart-placeholder">
                <div className="chart-bars">
                  {[38, 52, 45, 68, 58, 82, 74].map((height, index) => (
                    <div className="chart-bar" key={index} style={{ height: `${height}%` }} />
                  ))}
                </div>
                <div className="chart-labels">
                  {['周一', '周二', '周三', '周四', '周五', '周六', '周日'].map((day) => (
                    <span key={day}>{day}</span>
                  ))}
                </div>
              </div>
            </Card>
          </Col>

          <Col span={9}>
            <Card title="待处理事项" shadows="hover" className="todo-card">
              {[
                { label: '待发货订单', value: data?.awaitShip ?? 0, page: 'orders' as PageKey, icon: <IconOrderedList /> },
                { label: '待付款订单', value: data?.awaitPay ?? 0, page: 'orders' as PageKey, icon: <IconCreditCard /> },
                { label: '待审核售后', value: data?.awaitAfterSale ?? 0, page: 'aftersales' as PageKey, icon: <IconComment /> },
                { label: '待回复评价', value: data?.awaitComment ?? 0, page: 'marketing' as PageKey, icon: <IconGift /> },
              ].map((item) => (
                <div
                  className="todo-row"
                  key={item.label}
                  onClick={() => onNavigate(item.page)}
                  role="button"
                  tabIndex={0}
                >
                  <Space>
                    <span className="todo-icon">{item.icon}</span>
                    <Text>{item.label}</Text>
                  </Space>
                  <Tag color={(item.value > 0 ? 'orange' : 'grey') as any}>{item.value}</Tag>
                </div>
              ))}
            </Card>
          </Col>
        </Row>

        <Card
          title="最新订单"
          headerExtraContent={
            <Button theme="borderless" onClick={() => onNavigate('orders')}>
              查看全部
            </Button>
          }
          shadows="hover"
          style={{ width: '100%' }}
        >
          <Table
            dataSource={orders}
            pagination={false}
            rowKey="orderId"
            columns={[
              { title: '订单号', dataIndex: 'orderSn' },
              { title: '收货人', dataIndex: 'consignee' },
              { title: '订单金额', dataIndex: 'totalAmount', render: (value: string) => `¥ ${value}` },
              {
                title: '状态',
                dataIndex: 'orderStatusName',
                render: (value: string) => <Tag color={(orderTagColor[value] || 'blue') as any}>{value}</Tag>,
              },
              { title: '下单时间', dataIndex: 'addTime' },
            ]}
          />
        </Card>
      </Space>
    </Spin>
  );
}
