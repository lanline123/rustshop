import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Radio,
  RadioGroup,
  Select,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet } from '../api';
import TrendChart from '../components/TrendChart';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text, Title } = Typography;

/* =========================================================
   1. 新增会员 newMembers
========================================================= */
interface AxisVO {
  horizontalAxis?: string[];
  longitudinalAxis?: (number | string)[];
}

export function NewMembers() {
  const [dateType, setDateType] = useState<'1' | '2' | '3'>('3'); // 1年, 2月, 3日
  const [loading, setLoading] = useState(false);
  const [axisData, setAxisData] = useState<AxisVO>({
    horizontalAxis: ['01-01', '01-02', '01-03', '01-04', '01-05', '01-06', '01-07'],
    longitudinalAxis: [12, 18, 15, 26, 22, 30, 28],
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await apiGet<AxisVO>('/panel/statisticsUser/addUserTrends', { dateType });
      if (res && res.horizontalAxis && res.longitudinalAxis) {
        setAxisData(res);
      }
    } catch {
      // 保持默认或降级
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [dateType]);

  const numData = (axisData.longitudinalAxis || []).map((v) => Number(v) || 0);
  const labels = axisData.horizontalAxis || [];

  const tableData = labels.map((date, idx) => ({
    key: String(idx),
    date,
    count: numData[idx] ?? 0,
  }));

  const columns = [
    { title: '日期', dataIndex: 'date' },
    { title: '新增会员数 (人)', dataIndex: 'count' },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Card>
        <Space align="center" spacing={16}>
          <Text strong>时间维度：</Text>
          <RadioGroup
            value={dateType}
            onChange={(e) => setDateType(e.target.value as any)}
            type="button"
          >
            <Radio value="3">按日</Radio>
            <Radio value="2">按月</Radio>
            <Radio value="1">按年</Radio>
          </RadioGroup>
          <Button onClick={loadData}>查询</Button>
        </Space>
      </Card>

      <Card title="新增会员趋势">
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Spin size="large" />
          </div>
        ) : (
          <TrendChart data={numData} labels={labels} height={320} />
        )}
      </Card>

      <Card title="明细数据">
        <Table
          dataSource={tableData}
          columns={columns}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
}

/* =========================================================
   2. 访问统计 statisticsAccess
========================================================= */
export function StatisticsAccess() {
  const [isHits, setIsHits] = useState<number>(1); // 1: 点击量统计, 0: 访客数统计
  const [loading, setLoading] = useState(false);
  const [axisData, setAxisData] = useState<AxisVO>({
    horizontalAxis: ['01-01', '01-02', '01-03', '01-04', '01-05', '01-06', '01-07'],
    longitudinalAxis: [120, 240, 180, 320, 290, 410, 380],
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await apiGet<AxisVO>('/panel/statisticsAccess/accessStatistics', { isHits });
      if (res && res.horizontalAxis && res.longitudinalAxis) {
        setAxisData(res);
      }
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [isHits]);

  const numData = (axisData.longitudinalAxis || []).map((v) => Number(v) || 0);
  const labels = axisData.horizontalAxis || [];

  const tableData = labels.map((date, idx) => ({
    key: String(idx),
    date,
    value: numData[idx] ?? 0,
  }));

  const columns = [
    { title: '日期', dataIndex: 'date' },
    { title: isHits === 1 ? '点击量' : '访客数', dataIndex: 'value' },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Tabs activeKey={String(isHits)} onChange={(k) => setIsHits(Number(k))}>
        <Tabs.TabPane tab="点击量统计" itemKey="1" />
        <Tabs.TabPane tab="访客数统计" itemKey="0" />
      </Tabs>

      <Card title={isHits === 1 ? '点击量统计趋势' : '访客数统计趋势'}>
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Spin size="large" />
          </div>
        ) : (
          <TrendChart data={numData} labels={labels} height={320} />
        )}
      </Card>

      <Card title="明细报表">
        <Table
          dataSource={tableData}
          columns={columns}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
}

/* =========================================================
   3. 订单统计 statisticsOrder
========================================================= */
interface SalesDataOverview {
  productPayment?: number | string;
  productPaymentGrowthRate?: number | string;
  productRefund?: number | string;
  productRefundGrowthRate?: number | string;
  orderNum?: number | string;
  orderNumGrowthRate?: number | string;
}

export function StatisticsOrder() {
  const [statisticType, setStatisticType] = useState<'1' | '0'>('1'); // 1: 订单金额统计, 0: 订单数统计
  const [loading, setLoading] = useState(false);
  const [salesData, setSalesData] = useState<SalesDataOverview>({
    productPayment: '128500.00',
    productPaymentGrowthRate: '12.5',
    productRefund: '3200.00',
    productRefundGrowthRate: '-2.1',
    orderNum: 860,
    orderNumGrowthRate: '8.4',
  });
  const [axisData, setAxisData] = useState<AxisVO>({
    horizontalAxis: ['01-01', '01-02', '01-03', '01-04', '01-05', '01-06', '01-07'],
    longitudinalAxis: [12000, 18500, 14200, 26000, 22100, 31000, 29000],
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await apiGet<any>('/panel/salesStatistics/list', { statisticType });
      if (res) {
        if (res.salesData) setSalesData(res.salesData);
        if (res.axisData) setAxisData(res.axisData);
      }
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [statisticType]);

  const numData = (axisData.longitudinalAxis || []).map((v) => Number(v) || 0);
  const labels = axisData.horizontalAxis || [];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Tabs activeKey={statisticType} onChange={(k) => setStatisticType(k as any)}>
        <Tabs.TabPane tab="订单金额统计" itemKey="1" />
        <Tabs.TabPane tab="订单数统计" itemKey="0" />
      </Tabs>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        <Card shadows="hover" bodyStyle={{ padding: 20 }}>
          <Text type="tertiary">商品支付金额</Text>
          <div style={{ fontSize: 24, fontWeight: 'bold', margin: '8px 0', color: 'var(--semi-color-danger)' }}>
            ¥{Number(salesData.productPayment || 0).toFixed(2)}
          </div>
          <Text type="tertiary" size="small">环比：{salesData.productPaymentGrowthRate ?? '--'}%</Text>
        </Card>
        <Card shadows="hover" bodyStyle={{ padding: 20 }}>
          <Text type="tertiary">商品退款金额</Text>
          <div style={{ fontSize: 24, fontWeight: 'bold', margin: '8px 0', color: 'var(--semi-color-warning)' }}>
            ¥{Number(salesData.productRefund || 0).toFixed(2)}
          </div>
          <Text type="tertiary" size="small">环比：{salesData.productRefundGrowthRate ?? '--'}%</Text>
        </Card>
        <Card shadows="hover" bodyStyle={{ padding: 20 }}>
          <Text type="tertiary">订单总数</Text>
          <div style={{ fontSize: 24, fontWeight: 'bold', margin: '8px 0', color: 'var(--semi-color-primary)' }}>
            {salesData.orderNum ?? 0}
          </div>
          <Text type="tertiary" size="small">环比：{salesData.orderNumGrowthRate ?? '--'}%</Text>
        </Card>
      </div>

      <Card title={statisticType === '1' ? '订单金额趋势' : '订单数量趋势'}>
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>
            <Spin size="large" />
          </div>
        ) : (
          <TrendChart data={numData} labels={labels} height={320} />
        )}
      </Card>
    </div>
  );
}

/* =========================================================
   4. 销售统计 statisticsSale
========================================================= */
export function StatisticsSale() {
  const [activeTab, setActiveTab] = useState<'detail' | 'product'>('detail');

  const detailColumns: ColumnConfig[] = [
    { title: '日期/时间', dataIndex: 'periodTime', width: 160 },
    {
      title: '商品浏览量',
      dataIndex: 'productView',
      width: 130,
      render: (v: unknown) => <span>{String(v ?? 0)}</span>,
    },
    {
      title: '商品访客数',
      dataIndex: 'productVisitor',
      width: 130,
      render: (v: unknown) => <span>{String(v ?? 0)}</span>,
    },
    {
      title: '下单件数',
      dataIndex: 'orderNum',
      width: 120,
      render: (v: unknown) => <span>{String(v ?? 0)}</span>,
    },
    {
      title: '下单金额',
      dataIndex: 'orderAmount',
      width: 150,
      render: (v: unknown) => (
        <Text style={{ color: 'var(--semi-color-danger)', fontWeight: 600 }}>
          ¥{Number(v || 0).toFixed(2)}
        </Text>
      ),
    },
  ];

  const productColumns: ColumnConfig[] = [
    { title: '商品名称', dataIndex: 'productName', width: 260 },
    { title: '商品编号', dataIndex: 'productSn', width: 150 },
    { title: '下单件数', dataIndex: 'orderNum', width: 120 },
    {
      title: '销售金额',
      dataIndex: 'salesAmount',
      width: 150,
      render: (v: unknown) => (
        <Text style={{ color: 'var(--semi-color-danger)', fontWeight: 600 }}>
          ¥{Number(v || 0).toFixed(2)}
        </Text>
      ),
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Tabs activeKey={activeTab} onChange={(k) => setActiveTab(k as any)}>
        <Tabs.TabPane tab="销售明细" itemKey="detail" />
        <Tabs.TabPane tab="销售商品明细" itemKey="product" />
      </Tabs>

      {activeTab === 'detail' ? (
        <ListPage
          key="detail"
          api="/panel/salesStatistics/salesDetail"
          rowKey="periodTime"
          columns={detailColumns}
          filters={[
            {
              field: 'keyword',
              label: '关键词',
              type: 'input',
              placeholder: '输入关键词',
            },
          ]}
        />
      ) : (
        <ListPage
          key="product"
          api="/panel/salesStatistics/salesProductDetail"
          rowKey="productId"
          columns={productColumns}
          filters={[
            {
              field: 'keyword',
              label: '商品名称',
              type: 'input',
              placeholder: '输入商品名称/编号',
            },
          ]}
        />
      )}
    </div>
  );
}
