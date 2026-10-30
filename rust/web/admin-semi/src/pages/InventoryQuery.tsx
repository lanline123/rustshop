import { useState } from 'react';
import { Button, Modal, Spin, Table, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface InventoryRow {
  productId?: number;
  skuId?: number;
  productName?: string;
  picThumb?: string;
  productSkuName?: string;
  currentProductStock?: number;
  productCost?: number | string;
}

interface StockLogRow {
  productName?: string;
  desc?: string;
  productSkuName?: string;
  quantity?: number;
  createTime?: string;
}

/** 库存查询：对应旧版 product/inventoryQuery/List.vue + Logs.vue（出入库明细弹窗）。 */
export default function InventoryQuery() {
  const [logOpen, setLogOpen] = useState(false);
  const [logLoading, setLogLoading] = useState(false);
  const [logRows, setLogRows] = useState<StockLogRow[]>([]);
  const [logTitle, setLogTitle] = useState('出入库明细');
  const [reloadKey, setReloadKey] = useState(0);

  const reload = () => setReloadKey((key) => key + 1);

  const openLogs = async (row: InventoryRow) => {
    setLogTitle(`出入库明细：${row.productName || ''}`);
    setLogOpen(true);
    setLogLoading(true);
    try {
      const result = await apiGet<any>('/product/stock/stockDetail', {
        productId: row.productId,
        skuId: row.skuId,
        page: 1,
        size: 50,
      });
      setLogRows(result?.records || result?.list || []);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '加载失败' });
    } finally {
      setLogLoading(false);
    }
  };

  const columns: ColumnConfig<InventoryRow>[] = [
    {
      title: '商品名称',
      dataIndex: 'productName',
      width: 300,
      render: (value: string, row: InventoryRow) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {row.picThumb ? <img src={row.picThumb} alt="" width={36} height={36} style={{ borderRadius: 4, objectFit: 'cover' }} /> : null}
          <span>{value || '-'}</span>
        </div>
      ),
    },
    {
      title: '商品规格',
      dataIndex: 'productSkuName',
      width: 160,
      render: (value: string) => <span>{value || '--'}</span>,
    },
    {
      title: '单位',
      dataIndex: 'productUnit',
      width: 80,
      render: (value: string) => <span>{value || '--'}</span>,
    },
    { title: '可用库存', dataIndex: 'currentProductStock', width: 120 },
    {
      title: '成本',
      dataIndex: 'productCost',
      width: 110,
      render: (value: unknown) =>
        <span>{value === 0 || value ? String(value) : '--'}</span>,
    },
  ];

  return (
    <div className="inventory-query-page">
      <ListPage<InventoryRow>
        key={reloadKey}
        api="/product/stock/page"
        rowKey={(row?: InventoryRow) => `${row?.productId ?? 0}-${row?.skuId ?? 0}`}
        pageSize={10}
        actionWidth={140}
        filters={[
          { field: 'keyword', label: '商品名称', type: 'input', placeholder: '商品名称', width: 240 },
        ]}
        columns={columns}
        rowActions={(row) => (
          <Button
            theme="borderless"
            size="small"
            onClick={() => openLogs(row)}
          >
            出入库明细
          </Button>
        )}
      />

      <Modal
        title={logTitle}
        visible={logOpen}
        footer={null}
        onCancel={() => setLogOpen(false)}
        width={900}
      >
        <Spin spinning={logLoading}>
          <Table
            size="small"
            pagination={false}
            rowKey="__logKey"
            dataSource={logRows.map((row, index) => ({ ...row, __logKey: `${row.createTime ?? ''}-${index}` }))}
            columns={[
              {
                title: '商品名称',
                dataIndex: 'productName',
                width: 240,
                render: (value: string) => <span>{value || '-'}</span>,
              },
              {
                title: '出入库类型',
                dataIndex: 'desc',
                width: 140,
                render: (value: string) => <span>{value || '--'}</span>,
              },
              {
                title: '商品规格',
                dataIndex: 'productSkuName',
                width: 140,
                render: (value: string) => <span>{value || '--'}</span>,
              },
              {
                title: '出入库数量',
                dataIndex: 'quantity',
                width: 120,
                render: (value: number) => (
                  <Text type={Number(value) >= 0 ? 'success' : 'danger'}>{Number(value) >= 0 ? `+${value}` : value}</Text>
                ),
              },
              { title: '时间', dataIndex: 'createTime' },
            ]}
          />
          {!logRows.length && !logLoading ? <Text type="tertiary">暂无数据</Text> : null}
        </Spin>
      </Modal>
    </div>
  );
}
