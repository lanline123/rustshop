import { useState } from 'react';
import {
  Button,
  InputNumber,
  Modal,
  Radio,
  RadioGroup,
  Space,
  Spin,
  Table,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

interface StockRow {
  productId?: number;
  skuId?: number;
  productName?: string;
  picThumb?: string;
  productSkuName?: string;
  currentProductStock?: number;
}

interface AdjustLogRow {
  adjustType?: number;
  changeBeforeQuantity?: number;
  changeAfterQuantity?: number;
  addBy?: string;
  addTime?: string;
  quantity?: number;
}

const ADJUST_TYPES = [
  { value: 1, label: '增量更新', tip: '在现有库存上增加或减少配额数量，调整数量为负数表示减少' },
  { value: 0, label: '覆盖更新', tip: '将原有库存数量进行覆盖，覆盖后更新为调整数量填写的库存数量' },
];

const adjustTypeLabel = (value?: number) =>
  ADJUST_TYPES.find((item) => item.value === Number(value))?.label || '--';

/** 库存调整：对应旧版 product/inventoryAdjustment/List.vue + Adjustment.vue + Logs.vue。 */
export default function InventoryAdjustments() {
  const [reloadKey, setReloadKey] = useState(0);
  const [adjustTarget, setAdjustTarget] = useState<StockRow | null>(null);
  const [adjustForm, setAdjustForm] = useState<{ adjustType: number; quantity: number }>({
    adjustType: 1,
    quantity: 0,
  });
  const [adjustSaving, setAdjustSaving] = useState(false);
  const [logOpen, setLogOpen] = useState(false);
  const [logLoading, setLogLoading] = useState(false);
  const [logRows, setLogRows] = useState<AdjustLogRow[]>([]);
  const [logTitle, setLogTitle] = useState('记录');

  const reload = () => setReloadKey((key) => key + 1);

  const submitAdjust = async () => {
    if (!adjustTarget) return;
    if (adjustForm.adjustType === 1 && adjustForm.quantity === 0) {
      Toast.error({ content: '请输入变更库存数量' });
      return;
    }
    setAdjustSaving(true);
    try {
      await apiPost('/product/stock/stockAdjust', {
        ...adjustForm,
        quantity: Number(adjustForm.quantity),
        productId: adjustTarget.productId,
        skuId: adjustTarget.skuId,
      });
      Toast.success({ content: '操作成功' });
      setAdjustTarget(null);
      reload();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '调整失败' });
    } finally {
      setAdjustSaving(false);
    }
  };

  const openLogs = async (row: StockRow) => {
    setLogTitle(`记录：${row.productName || ''}`);
    setLogOpen(true);
    setLogLoading(true);
    try {
      const result = await apiGet<any>('/product/stock/adjustDetail', {
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

  const columns: ColumnConfig<StockRow>[] = [
    {
      title: '商品名称',
      dataIndex: 'productName',
      width: 300,
      render: (value: string, row: StockRow) => (
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
    {
      title: '库存操作说明',
      dataIndex: 'desc',
      width: 160,
      render: (value: string) => <span>{value || '--'}</span>,
    },
  ];

  return (
    <>
      <ListPage<StockRow>
      key={reloadKey}
      api="/product/stock/page"
      rowKey={(row?: StockRow) => `${row?.productId ?? 0}-${row?.skuId ?? 0}`}
      pageSize={10}
      actionWidth={180}
      filters={[
        { field: 'keyword', label: '商品名称', type: 'input', placeholder: '商品名称', width: 240 },
      ]}
      columns={columns}
      rowActions={(row) => (
        <Space spacing={2}>
          <Button
            theme="borderless"
            type="danger"
            size="small"
            onClick={() => {
              setAdjustForm({ adjustType: 1, quantity: 0 });
              setAdjustTarget(row);
            }}
          >
            库存调整
          </Button>
          <Button theme="borderless" size="small" onClick={() => openLogs(row)}>
            记录
          </Button>
        </Space>
      )}
      />
      <Modal
        title="库存调整"
        visible={Boolean(adjustTarget)}
        confirmLoading={adjustSaving}
        onOk={submitAdjust}
        onCancel={() => setAdjustTarget(null)}
      >
        <div className="stock-adjust-form">
          <label>入库类型</label>
          <RadioGroup
            value={adjustForm.adjustType}
            onChange={(event) => setAdjustForm((c) => ({ ...c, adjustType: Number(event.target.value) }))}
          >
            {ADJUST_TYPES.map((item) => (
              <Radio key={item.value} value={item.value}>
                <div>
                  <div>{item.label}</div>
                  <Text type="tertiary" size="small">
                    {item.tip}
                  </Text>
                </div>
              </Radio>
            ))}
          </RadioGroup>
          <label>调整数量</label>
          {adjustForm.adjustType === 1 ? (
            <InputNumber
              style={{ width: 180 }}
              value={adjustForm.quantity}
              max={Number(adjustTarget?.currentProductStock ?? 0)}
              onChange={(next) => setAdjustForm((c) => ({ ...c, quantity: Number(next ?? 0) }))}
            />
          ) : (
            <InputNumber
              style={{ width: 180 }}
              min={0}
              precision={0}
              value={adjustForm.quantity}
              onChange={(next) => setAdjustForm((c) => ({ ...c, quantity: Number(next ?? 0) }))}
            />
          )}
        </div>
      </Modal>

      <Modal
        title={logTitle}
        visible={logOpen}
        footer={null}
        onCancel={() => setLogOpen(false)}
        width={860}
      >
        <Spin spinning={logLoading}>
          <Table
            size="small"
            pagination={false}
            rowKey="__logKey"
            dataSource={logRows.map((row, index) => ({ ...row, __logKey: `${row.addTime ?? ''}-${index}` }))}
            columns={[
              {
                title: '调整类型',
                dataIndex: 'adjustType',
                width: 110,
                render: (value: number) => <span>{adjustTypeLabel(value)}</span>,
              },
              {
                title: '调整前数量',
                dataIndex: 'changeBeforeQuantity',
                width: 110,
                render: (value: number) => <span>{value === 0 ? 0 : value ?? '--'}</span>,
              },
              {
                title: '调整后数量',
                dataIndex: 'changeAfterQuantity',
                width: 110,
                render: (value: number) => <span>{value === 0 ? 0 : value ?? '--'}</span>,
              },
              {
                title: '操作账号',
                dataIndex: 'addBy',
                width: 110,
                render: (value: string) => <span>{value || '--'}</span>,
              },
              {
                title: '创建时间',
                dataIndex: 'addTime',
                render: (value: string) => <span>{value || '--'}</span>,
              },
            ]}
          />
          {!logRows.length && !logLoading ? <Text type="tertiary">暂无数据</Text> : null}
        </Spin>
      </Modal>
    </>
  );
}
