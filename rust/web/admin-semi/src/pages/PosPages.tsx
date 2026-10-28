import { useEffect, useState } from 'react';
import { Avatar, Button, Card, Pagination, Space, Switch, Table, Tag, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost, getToken } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface PosStats {
  orderCount?: number;
  totalAmount?: number | string;
  today?: boolean;
}

export function PosWorkbench() {
  const [stats, setStats] = useState<PosStats>({});

  const openPos = () => {
    const token = getToken();
    let payload: Record<string, unknown> = {};
    try {
      const raw = token.split('.')[1] || '';
      payload = JSON.parse(atob(raw.replace(/-/g, '+').replace(/_/g, '/')));
    } catch {
      payload = {};
    }
    const adminType = String(payload.typ || 'admin');
    const shopId = payload.shop_id ?? null;
    if (!token) {
      Toast.warning({ content: '请先登录后台' });
      return;
    }
    localStorage.setItem('accessToken', token);
    localStorage.setItem('adminType', adminType);
    if (shopId !== null && shopId !== undefined) {
      localStorage.setItem('shopId', String(shopId));
    }
    if (adminType !== 'admin' && !shopId) {
      Toast.warning({ content: '请先选择门店后再进入收银台' });
      return;
    }
    window.open('/admin/pos/?fromMenu=1', '_blank', 'noopener');
  };

  useEffect(() => {
    apiGet<PosStats>('/pos/posSwitch/posStatistics')
      .then((result) => setStats(result || {}))
      .catch(() => undefined);
  }, []);

  return (
    <div className="pos-workbench">
      <div className="pos-workbench-hero">
        <div>
          <div className="pos-workbench-eyebrow">POS · 门店收银</div>
          <h2>收银工作台</h2>
          <Text type="tertiary">进入 POS 完成开单、收款、挂单与交接班。</Text>
        </div>
        <Button theme="solid" type="primary" size="large" onClick={openPos}>
          进入收银台
        </Button>
      </div>
      <div className="pos-stat-grid">
        <Card className="pos-stat-card">
          <span>今日订单</span>
          <b>{stats.orderCount ?? 0}</b>
        </Card>
        <Card className="pos-stat-card">
          <span>今日金额</span>
          <b>¥ {stats.totalAmount ?? '0.00'}</b>
        </Card>
        <Card className="pos-stat-card">
          <span>收银状态</span>
          <b>{stats.today ? '营业中' : '未开始'}</b>
        </Card>
      </div>
      <Card className="pos-workbench-tip">
        <b>收银工作台由 POS 应用承载</b>
        <p>商品、购物车、桌台、挂单、结算等高频操作请进入 POS 应用完成；后台仅保留收银配置和基础资料。</p>
      </Card>
    </div>
  );
}

interface DeviceRow {
  id: number;
  deviceName?: string;
  deviceCode?: string;
  status?: number;
  lastUsedTime?: string;
  addTime?: string;
}

export function CashierDevices() {
  const columns: ColumnConfig<DeviceRow>[] = [
    { title: '设备名称', dataIndex: 'deviceName' },
    { title: '设备码', dataIndex: 'deviceCode' },
    { title: '最后使用时间', dataIndex: 'lastUsedTime', width: 180, render: (value: string) => value || '-' },
    { title: '添加时间', dataIndex: 'addTime', width: 180, render: (value: string) => value || '-' },
    {
      title: '设备状态',
      dataIndex: 'status',
      width: 110,
      render: (value: number) => (
        <Tag color={value === 1 ? 'green' : 'grey'}>{value === 1 ? '启用' : '停用'}</Tag>
      ),
    },
  ];

  return (
    <ListPage<DeviceRow>
      api="/pos/device/list"
      rowKey="id"
      pageSize={10}
      actionWidth={150}
      filters={[
        { field: 'keyword', label: '设备', type: 'input', placeholder: '设备名称/设备码', width: 240 },
        {
          field: 'status',
          label: '状态',
          type: 'select',
          width: 120,
          options: [
            { label: '启用', value: 1 },
            { label: '停用', value: 2 },
          ],
        },
      ]}
      columns={columns}
      drawer={{
        title: '收银设备',
        addText: '新增设备',
        width: 520,
        labelWidth: 'auto',
        inputWidth: 320,
        createApi: '/pos/device/create',
        updateApi: '/pos/device/update',
        detailApi: '/pos/device/detail',
        deleteApi: '/pos/device/del',
        idField: 'id',
        sections: [
          {
            key: 'basic',
            title: '设备信息',
            fields: [
              { field: 'deviceName', label: '设备名称', required: true, placeholder: '请输入设备名称' },
              { field: 'deviceCode', label: '设备码', required: true, placeholder: '请输入设备码' },
              {
                field: 'status',
                label: '设备状态',
                type: 'switch',
                initValue: 1,
                checkedText: '启用',
                uncheckedText: '停用',
              },
            ],
          },
        ],
        toPayload: (values) => ({
          ...values,
          id: values.id ? Number(values.id) : undefined,
          status: values.status ? 1 : 2,
        }),
      }}
    />
  );
}

interface ReceiptRow {
  printId: number;
  printName?: string;
  printSn?: string;
  platformText?: string;
  paperSizeDesc?: string;
  deviceCode?: string;
  statusText?: string;
  updateTime?: string;
}

export function ReceiptPrinters() {
  const columns: ColumnConfig<ReceiptRow>[] = [
    { title: '打印机名称', dataIndex: 'printName' },
    { title: '打印机类型', dataIndex: 'platformText', width: 120 },
    { title: '打印机编码', dataIndex: 'printSn', width: 180 },
    { title: '绑定设备', dataIndex: 'deviceCode', width: 150, render: (value: string) => value || '-' },
    { title: '纸张', dataIndex: 'paperSizeDesc', width: 100, render: (value: string) => value || '-' },
    { title: '状态', dataIndex: 'statusText', width: 100 },
    { title: '更新时间', dataIndex: 'updateTime', width: 180, render: (value: string) => value || '-' },
  ];

  return (
    <ListPage<ReceiptRow>
      api="/print/print/list"
      rowKey="printId"
      pageSize={10}
      filters={[
        { field: 'keyword', label: '打印机', type: 'input', placeholder: '打印机名称/编码', width: 260 },
      ]}
      columns={columns}
    />
  );
}

interface CashierRow {
  id: number;
  adminId?: number;
  username?: string;
  email?: string;
  avatar?: string;
  isUsing?: number;
  addTime?: number;
}

export function CashierList() {
  const [rows, setRows] = useState<CashierRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);

  const load = () => {
    setLoading(true);
    apiPost<{ records?: CashierRow[]; total?: number }>('/pos/adminUserPos/list', {
      page,
      size: 10,
      keyword,
    })
      .then((result) => {
        setRows(result?.records || []);
        setTotal(Number(result?.total || 0));
      })
      .catch((error) => Toast.error({ content: error instanceof Error ? error.message : '加载失败' }))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [page]);

  return (
    <Card className="cashier-list-card">
      <div className="cashier-list-toolbar">
        <input
          className="cashier-list-search"
          placeholder="姓名/手机号"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              setPage(1);
              load();
            }
          }}
        />
        <Button
          theme="solid"
          type="primary"
          onClick={() => {
            setPage(1);
            load();
          }}
        >
          搜索
        </Button>
      </div>
      <Table<CashierRow>
        dataSource={rows}
        loading={loading}
        pagination={false}
        rowKey="id"
        columns={[
          {
            title: '头像/姓名',
            dataIndex: 'username',
            render: (value: string, record) => (
              <Space>
                <Avatar size="small" src={record.avatar}>
                  {(value || '收').slice(0, 1)}
                </Avatar>
                <span>{value || '-'}</span>
              </Space>
            ),
          },
          { title: '手机号', dataIndex: 'email', render: (value: string) => value || '-' },
          {
            title: '账号状态',
            dataIndex: 'isUsing',
            width: 120,
            render: (value: number) => (
              <Tag color={value === 1 ? 'green' : 'grey'}>{value === 1 ? '启用' : '禁用'}</Tag>
            ),
          },
          {
            title: '添加时间',
            dataIndex: 'addTime',
            width: 180,
            render: (value: number) =>
              value ? new Date(value * 1000).toLocaleString() : '-',
          },
        ]}
      />
      <div className="cashier-list-pagination">
        <Pagination total={total} pageSize={10} currentPage={page} onPageChange={(next) => setPage(next)} />
      </div>
    </Card>
  );
}
