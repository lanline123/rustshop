import React, { useEffect, useMemo, useState } from 'react';
import {
  Button,
  DatePicker,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Radio,
  RadioGroup,
  Select,
  SideSheet,
  Space,
  Table,
  Tag,
  TextArea,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconPlus, IconSearch } from '@douyinfe/semi-icons';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import { apiDownload, apiGet, apiPost } from '../api';

const { Text } = Typography;

/* ==================================================================
   供应链公共工具（对齐原版 views/supplyChain/shared + api/supplyChain 契约）
   说明：本模块金额在 live 接口层均为十进制「元」（createPurchaseOrder /
   savePurchaseReceipt / savePurchaseReturn 均按元提交，列表同样返回元），
   前端汇总计算用「分」(整数) 避免浮点误差，提交时转回元。
================================================================== */
const MONEY_PATTERN = /^\d+(?:\.\d{1,2})?$/;

function errMsg(error: unknown): string {
  if (error instanceof Error) return error.message;
  return typeof error === 'string' ? error : '操作失败';
}

function currentShopId(): string {
  return localStorage.getItem('shopId') || '';
}

function isPlatformAdmin(): boolean {
  return (localStorage.getItem('adminType') || 'admin') === 'admin';
}

function pad2(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}

function toYMD(date: Date): string {
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
}

function toYMDHM(date: Date): string {
  return `${toYMD(date)} ${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
}

function toUnixSeconds(value?: string | number | null): number | undefined {
  if (value === undefined || value === null || value === '') return undefined;
  if (typeof value === 'number') return value > 10000000000 ? Math.floor(value / 1000) : Math.floor(value);
  const ms = new Date(value).getTime();
  if (!Number.isFinite(ms)) return undefined;
  return Math.floor(ms / 1000);
}

function formatUnix(value: unknown, withTime = true): string {
  if (value === undefined || value === null || value === '') return '--';
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    return value.replace('T', ' ').slice(0, withTime ? 16 : 10);
  }
  const ts = Number(value);
  if (!Number.isFinite(ts) || ts <= 0) return String(value);
  const date = new Date(ts < 100000000000 ? ts * 1000 : ts);
  return withTime ? toYMDHM(date) : toYMD(date);
}

/** 十进制元 -> 整数分（对齐原版 money.ts parseMoneyToMinorUnits 的校验规则），非法返回 null */
function parseMoneyToMinorUnits(value: string | number): number | null {
  const text = String(value ?? '').trim();
  if (!MONEY_PATTERN.test(text)) return null;
  const [integer, decimal = ''] = text.split('.');
  return Number(integer) * 100 + Number((decimal || '').padEnd(2, '0').slice(0, 2));
}

function centsToYuanText(cents: number): string {
  return (cents / 100).toFixed(2);
}

function fmtYuan(value: unknown): string {
  const n = Number(value);
  return Number.isFinite(n) ? n.toFixed(2) : '0.00';
}

function todayStart(): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

function orderIdOf(row: { orderId?: number; id?: number }): number {
  return Number(row.orderId ?? row.id ?? 0);
}

function ProductPic({ src, size = 40 }: { src?: string; size?: number }) {
  if (!src) return null;
  return (
    <img
      src={src}
      alt=""
      style={{ width: size, height: size, borderRadius: 4, objectFit: 'cover', flexShrink: 0 }}
    />
  );
}

function ProductCell({ pic, name, sku }: { pic?: string; name?: string; sku?: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <ProductPic src={pic} />
      <div style={{ minWidth: 0 }}>
        <div>{name || '-'}</div>
        {sku ? <Text type="tertiary" size="small">{sku}</Text> : null}
      </div>
    </div>
  );
}

function Field({ label, required, tip, children }: { label: string; required?: boolean; tip?: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <div style={{ marginBottom: 6, fontWeight: 500 }}>
        {required ? <span style={{ color: 'var(--semi-color-danger)', marginRight: 2 }}>*</span> : null}
        {label}
      </div>
      {children}
      {tip ? <div style={{ marginTop: 4, fontSize: 12, color: 'var(--semi-color-text-2)' }}>{tip}</div> : null}
    </div>
  );
}

function SectionTitle({ text }: { text: string }) {
  return (
    <div
      style={{
        fontWeight: 600,
        fontSize: 14,
        margin: '4px 0 14px',
        paddingBottom: 8,
        borderBottom: '1px solid var(--semi-color-border)',
      }}
    >
      {text}
    </div>
  );
}

const summaryStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 4,
  padding: '12px 0',
  color: 'var(--semi-color-text-1)',
};

/* ---------------- 共享数据加载 ---------------- */
interface ShopOptionItem {
  shopId: number;
  shopTitle?: string;
  shopName?: string;
}

interface SupplierOptionItem {
  vendorId: number;
  vendorName: string;
}

interface LogisticsOptionItem {
  logisticsId: number;
  logisticsName: string;
}

async function fetchShopOptions(): Promise<ShopOptionItem[]> {
  const res = await apiGet<any>('/merchant/shop/list', {
    shopType: 1,
    page: 1,
    size: 100,
    includeSelfOperated: 1,
  });
  return (Array.isArray(res) ? res : res.records || []) as ShopOptionItem[];
}

async function fetchSupplierOptions(shopId: string | number): Promise<SupplierOptionItem[]> {
  const res = await apiGet<any>('/supplyChain/supplier/list', { shopId, status: 1, page: 1, size: 100 });
  return (res.records || [])
    .filter((item: any) => item.vendorId !== undefined && item.vendorId !== null)
    .map((item: any) => ({
      vendorId: Number(item.vendorId),
      vendorName: item.vendorName || `供应商${item.vendorId}`,
    }));
}

async function fetchLogisticsOptions(): Promise<LogisticsOptionItem[]> {
  const res = await apiGet<any>('/setting/logisticsCompany/list', { page: 1, size: 100 });
  const list = Array.isArray(res) ? res : res.records || [];
  return list.map((item: any) => ({
    logisticsId: Number(item.logisticsId ?? item.id ?? 0),
    logisticsName: item.logisticsName || item.name || '',
  }));
}

function shopLabel(shop?: ShopOptionItem): string {
  if (!shop) return '';
  return shop.shopTitle || shop.shopName || `店铺${shop.shopId}`;
}

/* ==================================================================
   供应商商品选择弹窗（原版 purchaseOrder/SelectProduct.vue）
   GET supplyChain/supplierProduct/selection
================================================================== */
export interface ProductSelectorLine {
  vendorProductSkuId: string;
  skuId: string;
  productId: string;
  productName: string;
  skuName: string;
  categoryName: string;
  picThumb: string;
  supplierStock: number | string;
  minPurchaseQty: string;
  purchasePrice: string;
  quantity: string;
}

function ProductSelectorModal({
  visible,
  shopId,
  vendorId,
  selectedIds,
  onConfirm,
  onCancel,
}: {
  visible: boolean;
  shopId: string;
  vendorId: string;
  selectedIds: string[];
  onConfirm: (lines: ProductSelectorLine[]) => void;
  onCancel: () => void;
}) {
  const [keyword, setKeyword] = useState('');
  const [appliedKeyword, setAppliedKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [records, setRecords] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pending, setPending] = useState<Map<string, any>>(new Map());
  const pageSize = 10;

  useEffect(() => {
    if (!visible) return;
    setKeyword('');
    setAppliedKeyword('');
    setPage(1);
    setPending(new Map());
  }, [visible]);

  useEffect(() => {
    if (!visible) return;
    const vendor = Number(vendorId);
    if (!Number.isSafeInteger(vendor) || vendor <= 0) {
      setRecords([]);
      setTotal(0);
      return;
    }
    let alive = true;
    setLoading(true);
    apiGet<any>('/supplyChain/supplierProduct/selection', {
      shopId: shopId || undefined,
      vendorId: vendor,
      status: 1,
      keyword: appliedKeyword || undefined,
      page,
      size: pageSize,
    })
      .then((res) => {
        if (!alive) return;
        setRecords(
          (res.records || []).map((row: any, index: number) => ({
            ...row,
            key: String(row.vendorProductSkuId ?? index),
          })),
        );
        setTotal(Number(res.total || 0));
      })
      .catch((error) => alive && Toast.error({ content: errMsg(error) }))
      .finally(() => {
        if (alive) setLoading(false);
      });
    return () => {
      alive = false;
    };
  }, [visible, vendorId, shopId, appliedKeyword, page]);

  const pageKeys = records.map((row) => String(row.vendorProductSkuId ?? row.key));
  const selectedKeysOnPage = pageKeys.filter((key) => pending.has(key));

  const rowSelection: any = {
    selectedRowKeys: selectedKeysOnPage,
    onChange: (keys: Array<string | number>) => {
      setPending((prev) => {
        const next = new Map(prev);
        pageKeys.forEach((key) => next.delete(key));
        records.forEach((row) => {
          const key = String(row.vendorProductSkuId ?? row.key);
          if (keys.map(String).includes(key)) next.set(key, row);
        });
        return next;
      });
    },
    getCheckboxProps: (record: any) => ({
      disabled: selectedIds.includes(String(record.vendorProductSkuId)),
    }),
  };

  const columns = [
    {
      title: '商品信息',
      dataIndex: 'productName',
      render: (_: any, row: any) => (
        <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'categoryName',
      width: 130,
      render: (val: any) => val || '-',
    },
    {
      title: '供应商库存',
      dataIndex: 'supplierStock',
      width: 110,
      render: (val: any) => val ?? '-',
    },
    {
      title: '销售价(元)',
      dataIndex: 'supplyPrice',
      width: 110,
      render: (val: any) => fmtYuan(val),
    },
    {
      title: '',
      width: 70,
      render: (_: any, row: any) =>
        selectedIds.includes(String(row.vendorProductSkuId)) ? (
          <Text type="tertiary" size="small">已添加</Text>
        ) : null,
    },
  ];

  const confirmSelection = () => {
    const lines: ProductSelectorLine[] = [...pending.values()].map((row: any) => ({
      vendorProductSkuId: String(row.vendorProductSkuId),
      skuId: String(row.skuId ?? 0),
      productId: String(row.productId ?? ''),
      productName: row.productName || '',
      skuName: row.skuName || '',
      categoryName: row.categoryName || '',
      picThumb: row.picThumb || '',
      supplierStock: row.supplierStock ?? '-',
      minPurchaseQty: String(row.minPurchaseQty || 1),
      purchasePrice: String(row.supplyPrice ?? 0),
      quantity: String(row.minPurchaseQty || 1),
    }));
    onConfirm(lines);
  };

  return (
    <Modal
      title="添加商品"
      visible={visible}
      onCancel={onCancel}
      width={860}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" disabled={pending.size === 0} onClick={confirmSelection}>
            确认
          </Button>
        </Space>
      }
    >
      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        <Input
          placeholder="请输入商品名称"
          value={keyword}
          showClear
          onChange={(val) => setKeyword(val)}
          style={{ width: 240 }}
          onEnterPress={() => {
            setPage(1);
            setAppliedKeyword(keyword.trim());
          }}
        />
        <Button
          type="primary"
          icon={<IconSearch />}
          onClick={() => {
            setPage(1);
            setAppliedKeyword(keyword.trim());
          }}
        >
          搜索
        </Button>
      </div>
      <Table
        loading={loading}
        dataSource={records}
        rowKey="key"
        columns={columns as any}
        rowSelection={rowSelection}
        pagination={{
          currentPage: page,
          pageSize,
          total,
          onChange: (next: number) => setPage(next),
        }}
      />
    </Modal>
  );
}

/* ==================================================================
   1. 采购订单管理 (PurchaseOrders / purchaseOrderManage)
   列表字段与单据状态对齐原版 purchaseOrder/List.vue：
   1 待入库, 2 部分入库, 3 已完成, 4 已关闭
================================================================== */
interface PurchaseOrderItem {
  id?: number;
  orderId?: number;
  orderSn: string;
  trackingNo?: string;
  status: number;
  totalAmount: number | string;
  totalQuantity: number;
  vendorName?: string;
  supplierName?: string;
  shopName?: string;
  shopId?: number;
  createByName?: string;
  createTime?: string;
  remark?: string;
}

interface InboundLine {
  key: string;
  orderItemId: string;
  vendorProductSkuId: string;
  productName: string;
  skuName: string;
  categoryName: string;
  picThumb: string;
  orderedQuantity: number;
  receivedQuantity: number;
  remainingQuantity: number;
  actualQuantity: string;
  purchasePrice: string;
}

/* ---------------- 新建采购单抽屉（原版 purchaseOrder/Info.vue） ---------------- */
function PurchaseOrderCreateDrawer({
  visible,
  onClose,
  onSaved,
}: {
  visible: boolean;
  onClose: () => void;
  onSaved: () => void;
}) {
  const platform = isPlatformAdmin();
  const [saving, setSaving] = useState(false);
  const [shopOptions, setShopOptions] = useState<ShopOptionItem[]>([]);
  const [supplierOptions, setSupplierOptions] = useState<SupplierOptionItem[]>([]);
  const [logisticsOptions, setLogisticsOptions] = useState<LogisticsOptionItem[]>([]);
  const [shopId, setShopId] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [expectedAt, setExpectedAt] = useState('');
  const [shippingMethod, setShippingMethod] = useState(1); // 1 物流发货 2 无需物流
  const [logisticsId, setLogisticsId] = useState('');
  const [trackingNo, setTrackingNo] = useState('');
  const [remark, setRemark] = useState('');
  const [lines, setLines] = useState<ProductSelectorLine[]>([]);
  const [selectorOpen, setSelectorOpen] = useState(false);

  useEffect(() => {
    if (!visible) return;
    setShopId(platform ? '' : currentShopId());
    setVendorId('');
    setExpectedAt('');
    setShippingMethod(1);
    setLogisticsId('');
    setTrackingNo('');
    setRemark('');
    setLines([]);
    (async () => {
      try {
        const [shops, logistics] = await Promise.all([fetchShopOptions(), fetchLogisticsOptions()]);
        setShopOptions(shops);
        setLogisticsOptions(logistics.filter((item) => item.logisticsId > 0 && item.logisticsName));
      } catch (error) {
        Toast.error({ content: errMsg(error) });
      }
      if (!platform) {
        try {
          setSupplierOptions(await fetchSupplierOptions(currentShopId()));
        } catch {
          setSupplierOptions([]);
        }
      } else {
        setSupplierOptions([]);
      }
    })();
  }, [visible]);

  const handleShopChange = async (value: any) => {
    const next = value === undefined || value === null ? '' : String(value);
    setShopId(next);
    setVendorId('');
    setLines([]);
    if (!next) {
      setSupplierOptions([]);
      return;
    }
    try {
      setSupplierOptions(await fetchSupplierOptions(next));
    } catch (error) {
      Toast.error({ content: errMsg(error) });
      setSupplierOptions([]);
    }
  };

  const updateLine = (key: string, patch: Partial<ProductSelectorLine>) => {
    setLines((prev) => prev.map((line) => (line.vendorProductSkuId === key ? { ...line, ...patch } : line)));
  };

  const lineQuantity = (line: ProductSelectorLine) => Math.max(0, Math.round(Number(line.quantity) || 0));
  const lineCents = (line: ProductSelectorLine) => {
    const cents = parseMoneyToMinorUnits(line.purchasePrice);
    return cents === null ? 0 : cents * lineQuantity(line);
  };
  const totalCents = lines.reduce((sum, line) => sum + lineCents(line), 0);
  const totalQuantity = lines.reduce((sum, line) => sum + lineQuantity(line), 0);

  const lineColumns = [
    {
      title: '商品信息',
      dataIndex: 'productName',
      render: (_: any, row: ProductSelectorLine) => (
        <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'categoryName',
      width: 110,
      render: (val: any) => val || '-',
    },
    {
      title: '供应商库存',
      dataIndex: 'supplierStock',
      width: 100,
      render: (val: any) => val ?? '-',
    },
    {
      title: '采购数量',
      width: 140,
      render: (_: any, row: ProductSelectorLine) => (
        <InputNumber
          size="small"
          style={{ width: 110 }}
          min={Number(row.minPurchaseQty || 1)}
          step={1}
          precision={0}
          value={row.quantity}
          onChange={(val) => updateLine(row.vendorProductSkuId, { quantity: String(val ?? '') })}
        />
      ),
    },
    {
      title: '供货价(元/件)',
      width: 140,
      render: (_: any, row: ProductSelectorLine) => (
        <InputNumber
          size="small"
          style={{ width: 120 }}
          min={0}
          value={row.purchasePrice}
          onChange={(val) => updateLine(row.vendorProductSkuId, { purchasePrice: String(val ?? '') })}
        />
      ),
    },
    {
      title: '小计(元)',
      width: 100,
      render: (_: any, row: ProductSelectorLine) => centsToYuanText(lineCents(row)),
    },
    {
      title: '操作',
      width: 70,
      render: (_: any, row: ProductSelectorLine) => (
        <Button
          theme="borderless"
          type="danger"
          size="small"
          onClick={() => setLines((prev) => prev.filter((line) => line.vendorProductSkuId !== row.vendorProductSkuId))}
        >
          移除
        </Button>
      ),
    },
  ];

  const submit = async () => {
    if (!shopId) return Toast.warning('请选择采购店铺');
    if (!vendorId) return Toast.warning('请选择供应商');
    if (!expectedAt) return Toast.warning('请选择期望送达日期');
    if (!lines.length) return Toast.warning('请添加商品');
    if (shippingMethod === 1 && (!logisticsId || !trackingNo.trim())) {
      return Toast.warning('请完善物流公司与物流单号');
    }
    for (const line of lines) {
      if (parseMoneyToMinorUnits(line.purchasePrice) === null) {
        return Toast.warning(`商品「${line.productName}」的供货价格式不正确（最多两位小数）`);
      }
      if (lineQuantity(line) < Number(line.minPurchaseQty || 1)) {
        return Toast.warning(`商品「${line.productName}」的采购数量不能低于最小起订量 ${line.minPurchaseQty}`);
      }
    }
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseOrder/create', {
        shopId: Number(shopId),
        vendorId: Number(vendorId),
        expectedDeliveryDate: toUnixSeconds(expectedAt),
        shippingMethod,
        ...(shippingMethod === 1
          ? {
              logisticsId: Number(logisticsId),
              logisticsName:
                logisticsOptions.find((item) => String(item.logisticsId) === String(logisticsId))?.logisticsName || '',
              trackingNo: trackingNo.trim(),
            }
          : {}),
        remark: remark.trim(),
        items: lines.map((line) => ({
          vendorProductSkuId: Number(line.vendorProductSkuId),
          quantity: lineQuantity(line),
          purchasePrice: Number(line.purchasePrice),
        })),
      });
      Toast.success('采购单创建成功');
      onSaved();
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      title="新建采购单"
      visible={visible}
      onCancel={onClose}
      width={960}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            提交
          </Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="采购店铺" required>
        <Select
          placeholder="选择采购店铺"
          style={{ width: 320 }}
          showClear
          disabled={!platform}
          value={shopId || undefined}
          optionList={shopOptions.map((shop) => ({ label: shopLabel(shop), value: String(shop.shopId) }))}
          onChange={handleShopChange}
        />
      </Field>
      <Field label="供应商" required>
        <Select
          placeholder={shopId ? '选择供应商' : '请先选择采购店铺'}
          style={{ width: 320 }}
          showClear
          filter
          disabled={!shopId}
          value={vendorId || undefined}
          optionList={supplierOptions.map((item) => ({ label: item.vendorName, value: String(item.vendorId) }))}
          onChange={(val) => {
            setVendorId(val === undefined || val === null ? '' : String(val));
            setLines([]);
          }}
        />
      </Field>
      <Field label="期望送达日期" required>
        <DatePicker
          style={{ width: 320 }}
          type="date"
          placeholder="选择日期"
          value={expectedAt || undefined}
          disabledDate={(date: any) => Boolean(date && date.getTime() < todayStart().getTime())}
          onChange={(date: any) => setExpectedAt(date ? (date instanceof Date ? toYMD(date) : String(date).slice(0, 10)) : '')}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="物流信息" />
      <Field label="发货方式" required>
        <RadioGroup
          value={shippingMethod}
          onChange={(e: any) => {
            const mode = Number(e.target.value);
            setShippingMethod(mode);
            if (mode === 2) {
              setLogisticsId('');
              setTrackingNo('');
            }
          }}
        >
          <Radio value={1}>物流发货</Radio>
          <Radio value={2}>无需物流</Radio>
        </RadioGroup>
      </Field>
      {shippingMethod === 1 ? (
        <>
          <Field label="物流公司" required>
            <Select
              placeholder="请选择"
              style={{ width: 320 }}
              showClear
              filter
              value={logisticsId || undefined}
              optionList={logisticsOptions.map((item) => ({
                label: item.logisticsName,
                value: String(item.logisticsId),
              }))}
              onChange={(val) => setLogisticsId(val === undefined || val === null ? '' : String(val))}
            />
          </Field>
          <Field label="物流单号" required>
            <Input
              placeholder="请输入"
              style={{ width: 320 }}
              value={trackingNo}
              onChange={(val) => setTrackingNo(val)}
            />
          </Field>
        </>
      ) : null}
      <Field label="备注">
        <TextArea
          placeholder="请输入"
          rows={3}
          maxCount={255}
          style={{ width: 520 }}
          value={remark}
          onChange={(val) => setRemark(val)}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="商品信息" />
      <div style={{ marginBottom: 12 }}>
        <Button
          type="primary"
          icon={<IconPlus />}
          disabled={!shopId || !vendorId}
          onClick={() => setSelectorOpen(true)}
        >
          添加商品
        </Button>
        {!shopId || !vendorId ? (
          <Text type="tertiary" size="small" style={{ marginLeft: 8 }}>
            请先选择采购店铺与供应商
          </Text>
        ) : null}
      </div>
      <div style={summaryStyle}>
        <span>商品共</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{lines.length}</strong>
        <span>种，总数</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{totalQuantity}</strong>
        <span>，合计</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{centsToYuanText(totalCents)}</strong>
        <span>元</span>
      </div>
      <Table
        dataSource={lines}
        rowKey="vendorProductSkuId"
        columns={lineColumns as any}
        pagination={false}
        empty="暂无商品，请点击「添加商品」"
      />

      <ProductSelectorModal
        visible={selectorOpen}
        shopId={shopId}
        vendorId={vendorId}
        selectedIds={lines.map((line) => line.vendorProductSkuId)}
        onCancel={() => setSelectorOpen(false)}
        onConfirm={(incoming) => {
          setLines((prev) => {
            const next = [...prev];
            incoming.forEach((line) => {
              if (!next.some((item) => item.vendorProductSkuId === line.vendorProductSkuId)) {
                next.push({ ...line, quantity: line.minPurchaseQty || '1' });
              }
            });
            return next;
          });
          setSelectorOpen(false);
        }}
      />
    </SideSheet>
  );
}

/* ---------------- 收货入库抽屉（原版 purchaseOrder/Inbound.vue） ---------------- */
function PurchaseOrderInboundDrawer({
  order,
  onClose,
  onSaved,
}: {
  order: PurchaseOrderItem | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [orderSn, setOrderSn] = useState('');
  const [vendorName, setVendorName] = useState('');
  const [shopId, setShopId] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [inboundAt, setInboundAt] = useState('');
  const [remark, setRemark] = useState('');
  const [lines, setLines] = useState<InboundLine[]>([]);

  useEffect(() => {
    if (!order) return;
    setLoading(true);
    setInboundAt(toYMDHM(new Date()));
    setRemark('');
    setLines([]);
    let alive = true;
    apiGet<any>('/supplyChain/purchaseOrder/detail', { id: orderIdOf(order) })
      .then((detail) => {
        if (!alive) return;
        setOrderSn(detail.orderSn || order.orderSn || '');
        setVendorName(detail.vendorName || order.vendorName || order.supplierName || '');
        setShopId(String(detail.shopId ?? order.shopId ?? currentShopId() ?? ''));
        setVendorId(String(detail.vendorId ?? ''));
        setLines(
          (detail.items || []).map((item: any, index: number) => {
            const ordered = Math.max(0, Number(item.quantity) || 0);
            const received = Math.max(0, Number(item.receivedQuantity) || 0);
            const remaining =
              item.remainingQuantity === undefined || item.remainingQuantity === null
                ? ordered - received
                : Number(item.remainingQuantity);
            return {
              key: String(item.id ?? `${item.vendorProductSkuId ?? 'sku'}-${index}`),
              orderItemId: String(item.id ?? ''),
              vendorProductSkuId: String(item.vendorProductSkuId ?? ''),
              productName: item.productName || '',
              skuName: item.skuName || '',
              categoryName: item.categoryName || '',
              picThumb: item.picThumb || '',
              orderedQuantity: ordered,
              receivedQuantity: received,
              remainingQuantity: Math.max(0, remaining),
              actualQuantity: '',
              purchasePrice: String(item.purchasePrice || 0),
            };
          }),
        );
      })
      .catch((error) => alive && Toast.error({ content: errMsg(error) }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [order]);

  const submit = async () => {
    if (!order) return;
    if (!lines.length) return Toast.warning('暂无可入库的商品');
    const active = lines.filter((line) => Math.round(Number(line.actualQuantity) || 0) > 0);
    if (!active.length) return Toast.warning('请填写实际入库量');
    for (const line of active) {
      if (Math.round(Number(line.actualQuantity)) > line.remainingQuantity) {
        return Toast.warning(`商品「${line.productName}」的入库量不能超过剩余入库量 ${line.remainingQuantity}`);
      }
    }
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseReceipt/save', {
        shopId: Number(shopId || 0),
        vendorId: Number(vendorId || 0),
        orderId: orderIdOf(order),
        businessDate: toUnixSeconds(inboundAt),
        remark: remark.trim(),
        items: active.map((line) => ({
          orderItemId: Number(line.orderItemId),
          vendorProductSkuId: Number(line.vendorProductSkuId),
          quantity: Math.round(Number(line.actualQuantity)),
          purchasePrice: Number(line.purchasePrice || 0),
        })),
      });
      Toast.success('入库成功');
      onSaved();
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    {
      title: '商品信息',
      dataIndex: 'productName',
      render: (_: any, row: InboundLine) => (
        <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'categoryName',
      width: 110,
      render: (val: any) => val || '-',
    },
    { title: '预计入库量', dataIndex: 'orderedQuantity', width: 110 },
    { title: '已入库量', dataIndex: 'receivedQuantity', width: 100 },
    {
      title: '剩余入库量',
      dataIndex: 'remainingQuantity',
      width: 110,
      render: (val: any) => <span style={{ color: 'var(--semi-color-warning)', fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '实际入库量',
      width: 140,
      render: (_: any, row: InboundLine) => (
        <InputNumber
          size="small"
          style={{ width: 120 }}
          min={0}
          step={1}
          precision={0}
          placeholder="请输入"
          value={row.actualQuantity}
          onChange={(val) =>
            setLines((prev) =>
              prev.map((line) => (line.key === row.key ? { ...line, actualQuantity: String(val ?? '') } : line)),
            )
          }
        />
      ),
    },
  ];

  return (
    <SideSheet
      title="收货入库"
      visible={Boolean(order)}
      onCancel={onClose}
      width={900}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            确认入库
          </Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="采购单号">
        <Text strong>{orderSn || '-'}</Text>
      </Field>
      <Field label="入库类型">
        <Text>采购入库</Text>
      </Field>
      <Field label="供应商">
        <Text>{vendorName || '-'}</Text>
      </Field>
      <Field label="入库时间" required>
        <DatePicker
          style={{ width: 320 }}
          type="dateTime"
          format="yyyy-MM-dd HH:mm"
          placeholder="选择日期时间"
          value={inboundAt || undefined}
          disabledDate={(date: any) => Boolean(date && date.getTime() > Date.now())}
          onChange={(date: any) => setInboundAt(date ? (date instanceof Date ? toYMDHM(date) : String(date).slice(0, 16)) : '')}
        />
      </Field>
      <Field label="备注">
        <TextArea
          placeholder="请输入"
          rows={3}
          maxCount={255}
          style={{ width: 520 }}
          value={remark}
          onChange={(val) => setRemark(val)}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="入库信息" />
      <Table
        loading={loading}
        dataSource={lines}
        rowKey="key"
        columns={columns as any}
        pagination={false}
        empty={loading ? '加载中...' : '暂无数据'}
      />
    </SideSheet>
  );
}

/* ---------------- 采购订单详情抽屉（原版 purchaseOrder/Info.vue readonly） ---------------- */
function PurchaseOrderDetailDrawer({
  order,
  onClose,
}: {
  order: PurchaseOrderItem | null;
  onClose: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<any>(null);

  useEffect(() => {
    if (!order) return;
    setLoading(true);
    setDetail(null);
    let alive = true;
    apiGet<any>('/supplyChain/purchaseOrder/detail', { id: orderIdOf(order) })
      .then((res) => alive && setDetail(res || {}))
      .catch((error) => alive && Toast.error({ content: errMsg(error) }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [order]);

  const detailItems = (detail?.items || []).map((item: any, index: number) => ({
    ...item,
    itemKey: String(item.id ?? `${item.vendorProductSkuId ?? ''}-${index}`),
  }));

  return (
    <SideSheet
      title="采购订单详情"
      visible={Boolean(order)}
      onCancel={onClose}
      width={920}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ textAlign: 'right' }}>
          <Button onClick={onClose}>关闭</Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="采购单号">
        <Text strong>{detail?.orderSn || order?.orderSn || '-'}</Text>
      </Field>
      <Field label="供应商">
        <Text>{detail?.vendorName || order?.vendorName || order?.supplierName || '-'}</Text>
      </Field>
      <Field label="采购组织">
        <Text>{detail?.shopName || order?.shopName || '-'}</Text>
      </Field>
      <Field label="期望送达日期">
        <Text>{formatUnix(detail?.expectedDeliveryDate)}</Text>
      </Field>
      <Field label="发货方式">
        <Text>{detail ? (Number(detail.shippingMethod) === 2 ? '无需物流' : '物流发货') : '-'}</Text>
      </Field>
      {detail && Number(detail.shippingMethod) !== 2 ? (
        <>
          <Field label="物流公司">
            <Text>{detail.logisticsName || '-'}</Text>
          </Field>
          <Field label="物流单号">
            <Text>{detail.trackingNo || '-'}</Text>
          </Field>
        </>
      ) : null}
      <Field label="备注">
        <Text>{detail?.remark || '-'}</Text>
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="商品信息" />
      <Table
        loading={loading}
        dataSource={detailItems}
        rowKey="itemKey"
        pagination={false}
        columns={
          [
            {
              title: '商品信息',
              dataIndex: 'productName',
              render: (_: any, row: any) => (
                <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
              ),
            },
            {
              title: '商品分类',
              dataIndex: 'categoryName',
              width: 110,
              render: (val: any) => val || '-',
            },
            { title: '采购数量', dataIndex: 'quantity', width: 100 },
            { title: '已入库量', dataIndex: 'receivedQuantity', width: 100 },
            {
              title: '供货价(元/件)',
              dataIndex: 'purchasePrice',
              width: 130,
              render: (val: any) => fmtYuan(val),
            },
            {
              title: '小计(元)',
              width: 110,
              render: (_: any, row: any) =>
                fmtYuan((Number(row.purchasePrice) || 0) * Math.max(0, Number(row.quantity) || 0)),
            },
          ] as any
        }
      />
    </SideSheet>
  );
}

export function PurchaseOrders() {
  const [reloadKey, setReloadKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [inboundOrder, setInboundOrder] = useState<PurchaseOrderItem | null>(null);
  const [detailOrder, setDetailOrder] = useState<PurchaseOrderItem | null>(null);

  const statusName = (status: number) =>
    ({ 1: '待入库', 2: '部分入库', 3: '已完成', 4: '已关闭' })[status] || '未知状态';
  const statusColor = (status: number): any =>
    ({ 1: 'orange', 2: 'blue', 3: 'green', 4: 'grey' })[status] || 'grey';

  const columns: ColumnConfig<PurchaseOrderItem>[] = [
    {
      title: '采购单号',
      dataIndex: 'orderSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '物流单号',
      dataIndex: 'trackingNo',
      width: 160,
      render: (val: any) => val || '--',
    },
    {
      title: '单据状态',
      dataIndex: 'status',
      width: 100,
      render: (val: any) => <Tag color={statusColor(Number(val))}>{statusName(Number(val))}</Tag>,
    },
    {
      title: '采购总金额(元)',
      dataIndex: 'totalAmount',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#165dff' }}>{fmtYuan(val)}</span>,
    },
    {
      title: '采购数量',
      dataIndex: 'totalQuantity',
      width: 100,
      render: (val: any) => val ?? 0,
    },
    {
      title: '供应商',
      dataIndex: 'vendorName',
      width: 150,
      render: (val: any, row: PurchaseOrderItem) => val || row.supplierName || '--',
    },
    {
      title: '采购组织',
      dataIndex: 'shopName',
      width: 140,
      render: (val: any) => val || '--',
    },
    {
      title: '制单时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
    {
      title: '备注',
      dataIndex: 'remark',
      width: 160,
      render: (val: any) => val || '--',
    },
    {
      title: '操作',
      width: 180,
      render: (_: any, record: PurchaseOrderItem) => (
        <Space spacing={2}>
          {record.status === 1 || record.status === 2 ? (
            <Button theme="borderless" type="primary" size="small" onClick={() => setInboundOrder(record)}>
              入库
            </Button>
          ) : null}
          {record.status === 2 ? (
            <Popconfirm
              title="确认标记采购完成？"
              onConfirm={async () => {
                try {
                  await apiPost('/supplyChain/purchaseOrder/finish', {
                    id: orderIdOf(record),
                    shopId: Number(record.shopId ?? currentShopId() ?? 0),
                    remark: '',
                  });
                  Toast.success('采购已完成');
                  setReloadKey((k) => k + 1);
                } catch (error) {
                  Toast.error({ content: errMsg(error) });
                }
              }}
            >
              <Button theme="borderless" type="warning" size="small">
                完成
              </Button>
            </Popconfirm>
          ) : null}
          <Button theme="borderless" type="primary" size="small" onClick={() => setDetailOrder(record)}>
            详情
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <ListPage<PurchaseOrderItem>
        key={reloadKey}
        api="/supplyChain/purchaseOrder/list"
        rowKey={(row) => String(row.orderId ?? row.id ?? '')}
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => setCreateOpen(true)}>
            新建采购单
          </Button>
        }
        filters={[
          {
            field: 'orderSn',
            label: '采购单号',
            placeholder: '输入采购单号',
            type: 'input',
          },
          {
            field: 'trackingNo',
            label: '物流单号',
            placeholder: '输入物流单号',
            type: 'input',
          },
          {
            field: 'status',
            label: '状态',
            type: 'select',
            options: [
              { label: '全部', value: '' },
              { label: '待入库', value: 1 },
              { label: '部分入库', value: 2 },
              { label: '已完成', value: 3 },
              { label: '已关闭', value: 4 },
            ],
          },
        ]}
      />

      <PurchaseOrderCreateDrawer
        visible={createOpen}
        onClose={() => setCreateOpen(false)}
        onSaved={() => {
          setCreateOpen(false);
          setReloadKey((k) => k + 1);
        }}
      />
      <PurchaseOrderInboundDrawer
        order={inboundOrder}
        onClose={() => setInboundOrder(null)}
        onSaved={() => {
          setInboundOrder(null);
          setReloadKey((k) => k + 1);
        }}
      />
      <PurchaseOrderDetailDrawer order={detailOrder} onClose={() => setDetailOrder(null)} />
    </div>
  );
}

/* ==================================================================
   2. 采购入库 (PurchaseInbound / purchaseInboundManage)
   原版 purchaseInbound/List.vue + Info.vue(direct) + Detail.vue
================================================================== */
interface PurchaseReceiptItem {
  id?: number;
  receiptId?: number;
  receiptSn: string;
  orderSn?: string;
  orderId?: number;
  shopName?: string;
  vendorName?: string;
  supplierName?: string;
  status?: number; // 2 已入库, 其余 待入库
  totalQuantity?: number;
  totalAmount?: number | string;
  createByName?: string;
  createTime?: string;
  remark?: string;
}

/* ---------------- 直接入库抽屉（原版 purchaseInbound/Info.vue direct 模式） ---------------- */
function ReceiptDirectDrawer({
  visible,
  onClose,
  onSaved,
}: {
  visible: boolean;
  onClose: () => void;
  onSaved: () => void;
}) {
  const platform = isPlatformAdmin();
  const [saving, setSaving] = useState(false);
  const [shopOptions, setShopOptions] = useState<ShopOptionItem[]>([]);
  const [supplierOptions, setSupplierOptions] = useState<SupplierOptionItem[]>([]);
  const [shopId, setShopId] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [inboundAt, setInboundAt] = useState('');
  const [voucherUrl, setVoucherUrl] = useState('');
  const [remark, setRemark] = useState('');
  const [lines, setLines] = useState<ProductSelectorLine[]>([]);
  const [selectorOpen, setSelectorOpen] = useState(false);

  useEffect(() => {
    if (!visible) return;
    setShopId(platform ? '' : currentShopId());
    setVendorId('');
    setInboundAt(toYMDHM(new Date()));
    setVoucherUrl('');
    setRemark('');
    setLines([]);
    if (platform) {
      fetchShopOptions()
        .then(setShopOptions)
        .catch((error) => Toast.error({ content: errMsg(error) }));
    } else {
      fetchSupplierOptions(currentShopId())
        .then(setSupplierOptions)
        .catch(() => setSupplierOptions([]));
    }
  }, [visible]);

  const handleShopChange = async (value: any) => {
    const next = value === undefined || value === null ? '' : String(value);
    setShopId(next);
    setVendorId('');
    setLines([]);
    if (!next) {
      setSupplierOptions([]);
      return;
    }
    try {
      setSupplierOptions(await fetchSupplierOptions(next));
    } catch (error) {
      Toast.error({ content: errMsg(error) });
      setSupplierOptions([]);
    }
  };

  const updateLine = (key: string, patch: Partial<ProductSelectorLine>) => {
    setLines((prev) => prev.map((line) => (line.vendorProductSkuId === key ? { ...line, ...patch } : line)));
  };

  const lineQty = (line: ProductSelectorLine) => Math.max(0, Math.round(Number(line.quantity) || 0));
  const lineCents = (line: ProductSelectorLine) => {
    const cents = parseMoneyToMinorUnits(line.purchasePrice);
    return cents === null ? 0 : cents * lineQty(line);
  };
  const totalCents = lines.reduce((sum, line) => sum + lineCents(line), 0);
  const totalQty = lines.reduce((sum, line) => sum + lineQty(line), 0);

  const lineColumns = [
    {
      title: '商品信息',
      dataIndex: 'productName',
      render: (_: any, row: ProductSelectorLine) => (
        <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'categoryName',
      width: 110,
      render: (val: any) => val || '-',
    },
    {
      title: '入库数量',
      width: 140,
      render: (_: any, row: ProductSelectorLine) => (
        <InputNumber
          size="small"
          style={{ width: 110 }}
          min={0}
          step={1}
          precision={0}
          value={row.quantity}
          onChange={(val) => updateLine(row.vendorProductSkuId, { quantity: String(val ?? '') })}
        />
      ),
    },
    {
      title: '成本价(元/件)',
      width: 140,
      render: (_: any, row: ProductSelectorLine) => (
        <InputNumber
          size="small"
          style={{ width: 120 }}
          min={0}
          value={row.purchasePrice}
          onChange={(val) => updateLine(row.vendorProductSkuId, { purchasePrice: String(val ?? '') })}
        />
      ),
    },
    {
      title: '小计(元)',
      width: 100,
      render: (_: any, row: ProductSelectorLine) => centsToYuanText(lineCents(row)),
    },
    {
      title: '操作',
      width: 70,
      render: (_: any, row: ProductSelectorLine) => (
        <Button
          theme="borderless"
          type="danger"
          size="small"
          onClick={() => setLines((prev) => prev.filter((line) => line.vendorProductSkuId !== row.vendorProductSkuId))}
        >
          移除
        </Button>
      ),
    },
  ];

  const submit = async () => {
    if (!shopId) return Toast.warning('请选择采购店铺');
    if (!vendorId) return Toast.warning('请选择供应商');
    if (!inboundAt) return Toast.warning('请选择入库日期');
    const active = lines.filter((line) => lineQty(line) > 0);
    if (!active.length) return Toast.warning('请选择商品并填写入库数量');
    for (const line of active) {
      if (parseMoneyToMinorUnits(line.purchasePrice) === null) {
        return Toast.warning(`商品「${line.productName}」的成本价格式不正确（最多两位小数）`);
      }
    }
    if (voucherUrl.trim().length > 512) return Toast.warning('入库凭证图 URL 过长（最多 512 字符）');
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseReceipt/save', {
        shopId: Number(shopId),
        vendorId: Number(vendorId),
        expectedDeliveryDate: toUnixSeconds(inboundAt),
        shippingMethod: 2,
        businessDate: toUnixSeconds(inboundAt),
        ...(voucherUrl.trim() ? { voucherUrl: voucherUrl.trim() } : {}),
        remark: remark.trim(),
        items: active.map((line) => ({
          vendorProductSkuId: Number(line.vendorProductSkuId),
          quantity: lineQty(line),
          purchasePrice: Number(line.purchasePrice || 0),
        })),
      });
      Toast.success('入库成功');
      onSaved();
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      title="直接入库"
      visible={visible}
      onCancel={onClose}
      width={920}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            提交
          </Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="采购店铺" required>
        <Select
          placeholder="选择采购店铺"
          style={{ width: 320 }}
          showClear
          disabled={!platform}
          value={shopId || undefined}
          optionList={shopOptions.map((shop) => ({ label: shopLabel(shop), value: String(shop.shopId) }))}
          onChange={handleShopChange}
        />
      </Field>
      <Field label="供应商" required>
        <Select
          placeholder={shopId ? '选择供应商' : '请先选择采购店铺'}
          style={{ width: 320 }}
          showClear
          filter
          disabled={!shopId}
          value={vendorId || undefined}
          optionList={supplierOptions.map((item) => ({ label: item.vendorName, value: String(item.vendorId) }))}
          onChange={(val) => {
            setVendorId(val === undefined || val === null ? '' : String(val));
            setLines([]);
          }}
        />
      </Field>
      <Field label="入库日期" required>
        <DatePicker
          style={{ width: 320 }}
          type="dateTime"
          format="yyyy-MM-dd HH:mm"
          placeholder="选择日期时间"
          value={inboundAt || undefined}
          disabledDate={(date: any) => Boolean(date && date.getTime() > Date.now())}
          onChange={(date: any) => setInboundAt(date ? (date instanceof Date ? toYMDHM(date) : String(date).slice(0, 16)) : '')}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="附件" />
      <Field label="入库凭证图 URL" tip="请填写一张入库凭证图片的 URL，可留空">
        <Input
          placeholder="https://..."
          style={{ width: 520 }}
          value={voucherUrl}
          onChange={(val) => setVoucherUrl(val)}
        />
      </Field>
      {voucherUrl.trim() ? (
        <div style={{ marginBottom: 16 }}>
          <ProductPic src={voucherUrl.trim()} size={96} />
        </div>
      ) : null}
      <Field label="备注">
        <TextArea
          placeholder="请输入"
          rows={3}
          maxCount={255}
          style={{ width: 520 }}
          value={remark}
          onChange={(val) => setRemark(val)}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="商品信息" />
      <div style={{ marginBottom: 12 }}>
        <Button
          type="primary"
          icon={<IconPlus />}
          disabled={!shopId || !vendorId}
          onClick={() => setSelectorOpen(true)}
        >
          选择商品
        </Button>
        {!shopId || !vendorId ? (
          <Text type="tertiary" size="small" style={{ marginLeft: 8 }}>
            请先选择采购店铺与供应商
          </Text>
        ) : null}
      </div>
      <div style={summaryStyle}>
        <span>商品共</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{lines.length}</strong>
        <span>种，总数</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{totalQty}</strong>
        <span>，合计</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{centsToYuanText(totalCents)}</strong>
        <span>元</span>
      </div>
      <Table
        dataSource={lines}
        rowKey="vendorProductSkuId"
        columns={lineColumns as any}
        pagination={false}
        empty="暂无商品，请点击「选择商品」"
      />

      <ProductSelectorModal
        visible={selectorOpen}
        shopId={shopId}
        vendorId={vendorId}
        selectedIds={lines.map((line) => line.vendorProductSkuId)}
        onCancel={() => setSelectorOpen(false)}
        onConfirm={(incoming) => {
          setLines((prev) => {
            const next = [...prev];
            incoming.forEach((line) => {
              if (!next.some((item) => item.vendorProductSkuId === line.vendorProductSkuId)) {
                next.push({ ...line, quantity: '0' });
              }
            });
            return next;
          });
          setSelectorOpen(false);
        }}
      />
    </SideSheet>
  );
}

/* ---------------- 入库单详情抽屉（原版 purchaseInbound/Detail.vue） ---------------- */
function ReceiptDetailDrawer({ record, onClose }: { record: PurchaseReceiptItem | null; onClose: () => void }) {
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<any>(null);

  useEffect(() => {
    if (!record) return;
    // 原版以关联采购订单 id 调 purchaseReceipt/detail（无关联单据无法查看详情）
    const detailId = Number(record.orderId ?? record.id ?? 0);
    if (!detailId) {
      Toast.error('该入库单缺少关联采购订单，无法查看详情');
      return;
    }
    setLoading(true);
    setDetail(null);
    let alive = true;
    apiGet<any>('/supplyChain/purchaseReceipt/detail', { id: detailId })
      .then((res) => alive && setDetail(res || {}))
      .catch((error) => alive && Toast.error({ content: errMsg(error) }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [record]);

  const items = (detail?.items || []).map((item: any, index: number) => ({
    ...item,
    itemKey: String(item.id ?? `${item.skuId ?? ''}-${index}`),
  }));
  const totalQty = items.reduce((sum: number, item: any) => sum + Math.max(0, Number(item.quantity) || 0), 0);
  const totalCents = items.reduce(
    (sum: number, item: any) =>
      sum +
      Math.round(
        (Number(item.amount) || (Number(item.quantity) || 0) * (Number(item.purchasePrice) || 0)) * 100,
      ),
    0,
  );

  return (
    <SideSheet
      title="采购入库详情"
      visible={Boolean(record)}
      onCancel={onClose}
      width={900}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ textAlign: 'right' }}>
          <Button onClick={onClose}>关闭</Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="单据编号">
        <Text strong>{detail?.receiptSn || record?.receiptSn || '-'}</Text>
      </Field>
      <Field label="关联采购单">
        <Text>{detail?.orderSn || record?.orderSn || '-'}</Text>
      </Field>
      <Field label="供应商">
        <Text>{detail?.vendorName || record?.vendorName || record?.supplierName || '-'}</Text>
      </Field>
      <Field label="入库日期">
        <Text>{formatUnix(detail?.businessDate ?? detail?.expectedDeliveryDate)}</Text>
      </Field>
      <Field label="入库凭证">
        {detail?.voucherUrl ? (
          <ProductPic src={detail.voucherUrl} size={96} />
        ) : (
          <Text type="tertiary">-</Text>
        )}
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="商品信息" />
      <div style={summaryStyle}>
        <span>商品共</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{items.length}</strong>
        <span>种，总数</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{totalQty}</strong>
        <span>，合计</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{centsToYuanText(totalCents)}</strong>
        <span>元</span>
      </div>
      <Table
        loading={loading}
        dataSource={items}
        rowKey="itemKey"
        pagination={false}
        columns={
          [
            {
              title: '商品信息',
              dataIndex: 'productName',
              render: (_: any, row: any) => (
                <ProductCell pic={row.picThumb} name={row.productName} sku={row.skuName} />
              ),
            },
            {
              title: '商品分类',
              dataIndex: 'categoryName',
              width: 110,
              render: (val: any) => val || '-',
            },
            { title: '入库数量', dataIndex: 'quantity', width: 100 },
            {
              title: '成本价(元/件)',
              dataIndex: 'purchasePrice',
              width: 130,
              render: (val: any) => fmtYuan(val),
            },
            {
              title: '小计(元)',
              width: 110,
              render: (_: any, row: any) =>
                Number.isFinite(Number(row.amount)) && row.amount !== undefined && row.amount !== null
                  ? fmtYuan(row.amount)
                  : fmtYuan((Number(row.quantity) || 0) * (Number(row.purchasePrice) || 0)),
            },
          ] as any
        }
      />
    </SideSheet>
  );
}

export function PurchaseInbound() {
  const [reloadKey, setReloadKey] = useState(0);
  const [directOpen, setDirectOpen] = useState(false);
  const [detailRecord, setDetailRecord] = useState<PurchaseReceiptItem | null>(null);

  const columns: ColumnConfig<PurchaseReceiptItem>[] = [
    {
      title: '单据编号',
      dataIndex: 'receiptSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '采购订单号',
      dataIndex: 'orderSn',
      width: 180,
      render: (val: any) => val || '--',
    },
    {
      title: '采购组织',
      dataIndex: 'shopName',
      width: 130,
      render: (val: any) => val || '--',
    },
    {
      title: '供应商',
      dataIndex: 'vendorName',
      width: 150,
      render: (val: any, row: PurchaseReceiptItem) => val || row.supplierName || '--',
    },
    {
      title: '单据状态',
      dataIndex: 'status',
      width: 100,
      render: (val: any) => (Number(val) === 2 ? <Tag color="green">已入库</Tag> : <Tag color="orange">待入库</Tag>),
    },
    {
      title: '入库数量',
      dataIndex: 'totalQuantity',
      width: 100,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#00b42a' }}>+{val ?? 0}</span>,
    },
    {
      title: '入库总金额(元)',
      dataIndex: 'totalAmount',
      width: 140,
      render: (val: any) => fmtYuan(val),
    },
    {
      title: '制单人',
      dataIndex: 'createByName',
      width: 110,
      render: (val: any) => val || '--',
    },
    {
      title: '制单时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
    {
      title: '操作',
      width: 90,
      render: (_: any, record: PurchaseReceiptItem) => (
        <Button theme="borderless" type="primary" size="small" onClick={() => setDetailRecord(record)}>
          详情
        </Button>
      ),
    },
  ];

  return (
    <div>
      <ListPage<PurchaseReceiptItem>
        key={reloadKey}
        api="/supplyChain/purchaseReceipt/list"
        rowKey={(row) => String(row.receiptId ?? row.id ?? '')}
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => setDirectOpen(true)}>
            直接入库
          </Button>
        }
        filters={[
          {
            field: 'receiptSn',
            label: '单据编号',
            placeholder: '输入入库单号',
            type: 'input',
          },
          {
            field: 'orderSn',
            label: '采购订单号',
            placeholder: '输入采购订单号',
            type: 'input',
          },
        ]}
      />

      <ReceiptDirectDrawer
        visible={directOpen}
        onClose={() => setDirectOpen(false)}
        onSaved={() => {
          setDirectOpen(false);
          setReloadKey((k) => k + 1);
        }}
      />
      <ReceiptDetailDrawer record={detailRecord} onClose={() => setDetailRecord(null)} />
    </div>
  );
}

/* ==================================================================
   3. 采购退货 (PurchaseReturns / purchaseReturnManage)
   原版 purchaseReturn/List.vue + Info.vue(linked 模式)
================================================================== */
interface PurchaseReturnItem {
  id?: number;
  returnId?: number;
  returnSn: string;
  receiptSn?: string;
  orderSn?: string;
  shopName?: string;
  vendorName?: string;
  supplierName?: string;
  status?: number; // 2 已完成, 其余 待确认
  totalQuantity?: number;
  totalAmount?: number | string;
  createByName?: string;
  createTime?: string;
  remark?: string;
}

interface ReturnLine {
  receiptItemId: string;
  skuId: string;
  productName: string;
  skuName: string;
  categoryName: string;
  availableQuantity: number;
  returnableQuantity: number;
  returnQuantity: string;
  inboundPurchasePrice: string;
  returnPrice: string;
  reason: string;
}

function PurchaseReturnCreateDrawer({
  visible,
  onClose,
  onSaved,
}: {
  visible: boolean;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [receipts, setReceipts] = useState<any[]>([]);
  const [receiptId, setReceiptId] = useState('');
  const [shopId, setShopId] = useState('');
  const [shopName, setShopName] = useState('');
  const [vendorId, setVendorId] = useState('');
  const [vendorName, setVendorName] = useState('');
  const [remark, setRemark] = useState('');
  const [lines, setLines] = useState<ReturnLine[]>([]);

  useEffect(() => {
    if (!visible) return;
    setReceiptId('');
    setShopId('');
    setShopName('');
    setVendorId('');
    setVendorName('');
    setRemark('');
    setLines([]);
    apiGet<any>('/supplyChain/purchaseReceipt/list', { page: 1, size: 100 })
      .then((res) => {
        const rows = (res.records || []).map((item: any) => ({
          ...item,
          receiptSn: item.receiptSn || item.orderSn,
        }));
        setReceipts(rows);
      })
      .catch((error) => Toast.error({ content: errMsg(error) }));
  }, [visible]);

  const selectReceipt = async (row: any) => {
    // 原版以入库单的关联采购订单 id 调 purchaseReceipt/detail 带出可退明细
    const detailId = Number(row.orderId ?? row.id ?? 0);
    if (!detailId) {
      Toast.error('该入库单缺少关联采购订单，无法带出可退明细');
      return;
    }
    setLoading(true);
    try {
      const detail = await apiGet<any>('/supplyChain/purchaseReceipt/detail', { id: detailId });
      setShopId(String(detail.shopId ?? ''));
      setShopName(detail.shopName || '');
      setVendorId(String(detail.vendorId ?? ''));
      setVendorName(detail.vendorName || '');
      setLines(
        (detail.items || []).map((item: any) => ({
          receiptItemId: String(item.id ?? ''),
          skuId: String(item.skuId ?? 0),
          productName: item.productName || '',
          skuName: item.skuName || '',
          categoryName: item.categoryName || '',
          availableQuantity: Number(item.availableQuantity ?? item.returnableQuantity ?? 0),
          returnableQuantity: Number(item.returnableQuantity ?? 0),
          returnQuantity: '0',
          inboundPurchasePrice: String(item.purchasePrice ?? 0),
          returnPrice: String(item.purchasePrice ?? 0),
          reason: '',
        })),
      );
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setLoading(false);
    }
  };

  const handleReceiptChange = (value: any) => {
    const next = value === undefined || value === null ? '' : String(value);
    setReceiptId(next);
    setLines([]);
    if (!next) {
      setShopId('');
      setShopName('');
      setVendorId('');
      setVendorName('');
      return;
    }
    const row = receipts.find((item) => String(item.id ?? item.receiptId) === next);
    if (row) void selectReceipt(row);
  };

  const updateLine = (receiptItemId: string, patch: Partial<ReturnLine>) => {
    setLines((prev) => prev.map((line) => (line.receiptItemId === receiptItemId ? { ...line, ...patch } : line)));
  };

  const lineQty = (line: ReturnLine) => Math.max(0, Math.round(Number(line.returnQuantity) || 0));
  const lineCents = (line: ReturnLine) => {
    const cents = parseMoneyToMinorUnits(line.returnPrice || line.inboundPurchasePrice);
    return cents === null ? 0 : cents * lineQty(line);
  };
  const totalCents = lines.reduce((sum, line) => sum + lineCents(line), 0);
  const totalQty = lines.reduce((sum, line) => sum + lineQty(line), 0);

  const columns = [
    {
      title: '商品信息',
      dataIndex: 'productName',
      render: (_: any, row: ReturnLine) => (
        <div>
          <div>{row.productName}</div>
          {row.skuName ? <Text type="tertiary" size="small">{row.skuName}</Text> : null}
        </div>
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'categoryName',
      width: 100,
      render: (val: any) => val || '-',
    },
    {
      title: '可用库存',
      dataIndex: 'availableQuantity',
      width: 90,
    },
    {
      title: '可退库存',
      dataIndex: 'returnableQuantity',
      width: 90,
      render: (val: any) => <span style={{ color: 'var(--semi-color-warning)', fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '退货数量',
      width: 140,
      render: (_: any, row: ReturnLine) => (
        <InputNumber
          size="small"
          style={{ width: 110 }}
          min={0}
          max={row.returnableQuantity}
          step={1}
          precision={0}
          value={row.returnQuantity}
          onChange={(val) => updateLine(row.receiptItemId, { returnQuantity: String(val ?? '') })}
        />
      ),
    },
    {
      title: '退货价(元)',
      width: 130,
      render: (_: any, row: ReturnLine) => (
        <InputNumber
          size="small"
          style={{ width: 110 }}
          min={0}
          value={row.returnPrice}
          onChange={(val) => updateLine(row.receiptItemId, { returnPrice: String(val ?? '') })}
        />
      ),
    },
    {
      title: '退货原因',
      width: 160,
      render: (_: any, row: ReturnLine) => (
        <Input
          size="small"
          placeholder="请输入"
          value={row.reason}
          onChange={(val) => updateLine(row.receiptItemId, { reason: val })}
        />
      ),
    },
    {
      title: '小计(元)',
      width: 100,
      render: (_: any, row: ReturnLine) => centsToYuanText(lineCents(row)),
    },
  ];

  const submit = async () => {
    if (!receiptId) return Toast.warning('请选择关联采购入库单');
    if (!shopId) return Toast.warning('无法确定退货店铺，请重新选择入库单');
    if (!vendorId) return Toast.warning('无法确定供应商，请重新选择入库单');
    const active = lines.filter((line) => lineQty(line) > 0);
    if (!active.length) return Toast.warning('请填写退货数量');
    for (const line of active) {
      if (lineQty(line) > line.returnableQuantity) {
        return Toast.warning(`商品「${line.productName}」退货数量不能超过可退库存 ${line.returnableQuantity}`);
      }
      if (parseMoneyToMinorUnits(line.returnPrice || line.inboundPurchasePrice) === null) {
        return Toast.warning(`商品「${line.productName}」的退货价格式不正确（最多两位小数）`);
      }
    }
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseReturn/save', {
        shopId: Number(shopId || 0),
        receiptId: Number(receiptId),
        businessDate: Math.floor(Date.now() / 1000),
        reason: '',
        remark: remark.trim(),
        items: active.map((line) => ({
          receiptItemId: Number(line.receiptItemId),
          quantity: lineQty(line),
          returnPrice: Number(line.returnPrice || line.inboundPurchasePrice || 0),
          reason: line.reason.trim(),
        })),
      });
      Toast.success('退货单提交成功');
      onSaved();
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      title="新建退货单"
      visible={visible}
      onCancel={onClose}
      width={1020}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            确认
          </Button>
        </div>
      }
    >
      <SectionTitle text="基本信息" />
      <Field label="关联采购入库单" required>
        <Select
          placeholder="请选择入库单"
          style={{ width: 360 }}
          showClear
          filter
          loading={loading}
          value={receiptId || undefined}
          optionList={receipts.map((row) => ({
            label: String(row.receiptSn || row.orderSn || row.id),
            value: String(row.id ?? row.receiptId),
          }))}
          onChange={handleReceiptChange}
        />
      </Field>
      <Field label="退货店铺">
        <Text>{shopName || (shopId ? `店铺${shopId}` : '选择入库单后自动带出')}</Text>
      </Field>
      <Field label="供应商">
        <Text>{vendorName || (vendorId ? `供应商${vendorId}` : '选择入库单后自动带出')}</Text>
      </Field>
      <Field label="备注">
        <TextArea
          placeholder="请输入"
          rows={3}
          maxCount={255}
          style={{ width: 520 }}
          value={remark}
          onChange={(val) => setRemark(val)}
        />
      </Field>

      <div style={{ height: 16 }} />
      <SectionTitle text="商品信息" />
      <div style={summaryStyle}>
        <span>退货</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{totalQty}</strong>
        <span>件，合计</span>
        <strong style={{ color: 'var(--semi-color-primary)' }}>{centsToYuanText(totalCents)}</strong>
        <span>元</span>
      </div>
      <Table
        loading={loading}
        dataSource={lines}
        rowKey="receiptItemId"
        columns={columns as any}
        pagination={false}
        empty={loading ? '加载中...' : '请先选择采购入库单'}
      />
    </SideSheet>
  );
}

export function PurchaseReturns() {
  const [reloadKey, setReloadKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);

  const columns: ColumnConfig<PurchaseReturnItem>[] = [
    {
      title: '退货单号',
      dataIndex: 'returnSn',
      width: 190,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '关联入库单号',
      dataIndex: 'receiptSn',
      width: 190,
      render: (val: any) => val || '--',
    },
    {
      title: '退货组织',
      dataIndex: 'shopName',
      width: 130,
      render: (val: any) => val || '--',
    },
    {
      title: '供应商',
      dataIndex: 'vendorName',
      width: 150,
      render: (val: any, row: PurchaseReturnItem) => val || row.supplierName || '--',
    },
    {
      title: '退货状态',
      dataIndex: 'status',
      width: 100,
      render: (val: any) => (Number(val) === 2 ? <Tag color="green">已完成</Tag> : <Tag color="orange">待确认</Tag>),
    },
    {
      title: '退货总数量',
      dataIndex: 'totalQuantity',
      width: 110,
      render: (val: any) => val ?? 0,
    },
    {
      title: '退货总金额(元)',
      dataIndex: 'totalAmount',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#eb2f96' }}>{fmtYuan(val)}</span>,
    },
    {
      title: '制单时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
    {
      title: '备注',
      dataIndex: 'remark',
      width: 160,
      render: (val: any) => val || '--',
    },
  ];

  return (
    <div>
      <ListPage<PurchaseReturnItem>
        key={reloadKey}
        api="/supplyChain/purchaseReturn/list"
        rowKey={(row) => String(row.returnId ?? row.id ?? '')}
        columns={columns}
        toolbarExtra={
          <Button icon={<IconPlus />} type="primary" onClick={() => setCreateOpen(true)}>
            新建退货单
          </Button>
        }
        filters={[
          {
            field: 'returnSn',
            label: '退货单号',
            placeholder: '输入退货单号',
            type: 'input',
          },
          {
            field: 'receiptSn',
            label: '入库单号',
            placeholder: '输入关联入库单号',
            type: 'input',
          },
        ]}
      />

      <PurchaseReturnCreateDrawer
        visible={createOpen}
        onClose={() => setCreateOpen(false)}
        onSaved={() => {
          setCreateOpen(false);
          setReloadKey((k) => k + 1);
        }}
      />
    </div>
  );
}

/* ==================================================================
   4. 采购对账/结算 (PurchaseReconciliation / purchaseReconciliationManage)
   原版 purchaseReconciliation/List.vue + Info.vue + SelectDocument.vue
   单据状态：1 待对账, 2 已对账, 3 已驳回, 4 已作废；sourceType 1=入库 2=退货
================================================================== */
interface ReconciliationDocument {
  id: string;
  type: 'inbound' | 'return';
  sn: string;
  vendorId: string;
  vendorName: string;
  totalQuantity: string;
  amount: string;
  shopName: string;
  confirmTime: string;
  items: any[];
}

interface PurchaseReconciliationItem {
  id?: number;
  reconciliationId?: number;
  reconciliationSn: string;
  vendorName?: string;
  supplierName?: string;
  createByName?: string;
  createTime?: string;
  status: number; // 1 待对账, 2 已对账, 3 已驳回, 4 已作废
  remark?: string;
}

function CandidatesModal({
  visible,
  shopId,
  supplierId,
  selectedIds,
  onConfirm,
  onCancel,
}: {
  visible: boolean;
  shopId: string;
  supplierId: string;
  selectedIds: string[];
  onConfirm: (docs: ReconciliationDocument[]) => void;
  onCancel: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<any[]>([]);
  const [selectedKeys, setSelectedKeys] = useState<Array<string | number>>([]);

  useEffect(() => {
    if (!visible) return;
    setSelectedKeys([]);
    if (!supplierId) {
      setRecords([]);
      return;
    }
    setLoading(true);
    let alive = true;
    apiGet<any>('/supplyChain/purchaseReconciliation/candidates', {
      vendorId: Number(supplierId),
      shopId: shopId || undefined,
      page: 1,
      size: 100,
    })
      .then((res) => {
        if (!alive) return;
        setRecords(
          (res.records || []).map((row: any) => {
            const type = Number(row.sourceType) === 2 ? 'return' : 'inbound';
            return {
              ...row,
              key: String(row.sourceId ?? ''),
              id: String(row.sourceId ?? ''),
              sn: row.sn || row.sourceSn || '',
              type,
              typeName: type === 'return' ? '采购退货' : '采购入库',
              amount: String(row.amount ?? row.sourceAmount ?? 0),
              totalQuantity: String(row.totalQuantity ?? 0),
              vendorId: String(row.vendorId ?? row.supplierId ?? supplierId),
              vendorName: row.vendorName || '',
              shopName: row.shopName || '',
              confirmTime: row.confirmTime || '',
              items: row.items || [],
            };
          }),
        );
      })
      .catch((error) => alive && Toast.error({ content: errMsg(error) }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [visible, supplierId, shopId]);

  const rowSelection: any = {
    selectedRowKeys: selectedKeys,
    onChange: (keys: Array<string | number>) => setSelectedKeys(keys || []),
    getCheckboxProps: (record: any) => ({ disabled: selectedIds.includes(String(record.id)) }),
  };

  const confirm = () => {
    const docs: ReconciliationDocument[] = records
      .filter((row) => selectedKeys.map(String).includes(String(row.id)))
      .map((row) => ({
        id: row.id,
        type: row.type,
        sn: row.sn,
        vendorId: row.vendorId,
        vendorName: row.vendorName,
        totalQuantity: row.totalQuantity,
        amount: row.amount,
        shopName: row.shopName,
        confirmTime: row.confirmTime,
        items: row.items,
      }));
    onConfirm(docs);
  };

  return (
    <Modal
      title="选择申请单据"
      visible={visible}
      onCancel={onCancel}
      width={960}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" disabled={selectedKeys.length === 0} onClick={confirm}>
            确认
          </Button>
        </Space>
      }
    >
      <Text type="tertiary" size="small">
        仅展示该供应商未对账的入库/退货单据，跨单据供应商需一致。
      </Text>
      <Table
        style={{ marginTop: 8 }}
        loading={loading}
        dataSource={records}
        rowKey="key"
        rowSelection={rowSelection}
        pagination={false}
        columns={
          [
            { title: '单据编号', dataIndex: 'sn', width: 180 },
            { title: '单据类型', dataIndex: 'typeName', width: 100 },
            { title: '出入库时间', dataIndex: 'confirmTime', width: 170, render: (val: any) => val || '-' },
            { title: '所属店铺', dataIndex: 'shopName', width: 140, render: (val: any) => val || '-' },
            {
              title: '对账总金额(元)',
              dataIndex: 'amount',
              width: 130,
              render: (val: any) => fmtYuan(val),
            },
            { title: '实际入库量', dataIndex: 'totalQuantity', width: 100 },
          ] as any
        }
        empty={loading ? '加载中...' : '暂无可选未对账单据'}
      />
    </Modal>
  );
}

function PurchaseReconciliationCreateDrawer({
  visible,
  onClose,
  onSaved,
}: {
  visible: boolean;
  onClose: () => void;
  onSaved: () => void;
}) {
  const platform = isPlatformAdmin();
  const [saving, setSaving] = useState(false);
  const [shopOptions, setShopOptions] = useState<ShopOptionItem[]>([]);
  const [supplierOptions, setSupplierOptions] = useState<SupplierOptionItem[]>([]);
  const [shopId, setShopId] = useState('');
  const [supplierId, setSupplierId] = useState('');
  const [supplierName, setSupplierName] = useState('');
  const [remark, setRemark] = useState('');
  const [documents, setDocuments] = useState<ReconciliationDocument[]>([]);
  const [selectorOpen, setSelectorOpen] = useState(false);

  useEffect(() => {
    if (!visible) return;
    setShopId(platform ? '' : currentShopId());
    setSupplierId('');
    setSupplierName('');
    setRemark('');
    setDocuments([]);
    if (platform) {
      fetchShopOptions()
        .then(setShopOptions)
        .catch((error) => Toast.error({ content: errMsg(error) }));
    } else {
      fetchSupplierOptions(currentShopId())
        .then(setSupplierOptions)
        .catch(() => setSupplierOptions([]));
    }
  }, [visible]);

  const handleShopChange = async (value: any) => {
    const next = value === undefined || value === null ? '' : String(value);
    setShopId(next);
    setSupplierId('');
    setSupplierName('');
    setDocuments([]);
    if (!next) {
      setSupplierOptions([]);
      return;
    }
    try {
      setSupplierOptions(await fetchSupplierOptions(next));
    } catch (error) {
      Toast.error({ content: errMsg(error) });
      setSupplierOptions([]);
    }
  };

  const acceptDocuments = (docs: ReconciliationDocument[]) => {
    if (!docs.length) return;
    setSupplierId(docs[0].vendorId || supplierId);
    setSupplierName(docs[0].vendorName || supplierName);
    setDocuments(
      docs.map((doc) => ({
        ...doc,
        items: (doc.items || []).map((item: any, index: number) => ({ ...item, itemKey: String(index) })),
      })),
    );
    setSelectorOpen(false);
  };

  const totalCents = documents.reduce(
    (sum, doc) => sum + (doc.type === 'return' ? -1 : 1) * Math.round((Number(doc.amount) || 0) * 100),
    0,
  );
  const totalRows = documents.reduce((sum, doc) => sum + (doc.items?.length || 0), 0);

  const submit = async () => {
    if (platform && !shopId) return Toast.warning('请选择采购店铺');
    if (!supplierId) return Toast.warning('请选择供应商');
    if (!documents.length) return Toast.warning('请选择申请单据');
    setSaving(true);
    try {
      await apiPost('/supplyChain/purchaseReconciliation/create', {
        shopId: Number(shopId || 0),
        vendorId: Number(supplierId),
        remark: remark.trim(),
        sources: documents.map((doc) => ({
          sourceType: doc.type === 'return' ? 2 : 1,
          sourceId: Number(doc.id),
        })),
      });
      Toast.success('对账单创建成功');
      onSaved();
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      title="新建对账单"
      visible={visible}
      onCancel={onClose}
      width={960}
      placement="right"
      bodyStyle={{ overflowY: 'auto' }}
      footer={
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button onClick={onClose}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            提交
          </Button>
        </div>
      }
    >
      <Field label="采购店铺" required>
        <Select
          placeholder="选择采购店铺"
          style={{ width: 320 }}
          showClear
          disabled={!platform}
          value={shopId || undefined}
          optionList={shopOptions.map((shop) => ({ label: shopLabel(shop), value: String(shop.shopId) }))}
          onChange={handleShopChange}
        />
      </Field>
      <Field label="供应商" required>
        <Select
          placeholder={shopId || !platform ? '选择供应商' : '请先选择采购店铺'}
          style={{ width: 320 }}
          showClear
          filter
          disabled={platform && !shopId}
          value={supplierId || undefined}
          optionList={supplierOptions.map((item) => ({ label: item.vendorName, value: String(item.vendorId) }))}
          onChange={(val) => {
            setSupplierId(val === undefined || val === null ? '' : String(val));
            setSupplierName('');
            setDocuments([]);
          }}
        />
      </Field>
      <Button
        type="primary"
        disabled={(platform && !shopId) || !supplierId}
        onClick={() => setSelectorOpen(true)}
        style={{ marginBottom: 12 }}
      >
        选择申请单据
      </Button>
      <div style={{ marginBottom: 12, color: 'var(--semi-color-text-1)' }}>
        共 {totalRows} 行商品明细，合计金额：
        <strong style={{ color: '#f59a23' }}>{centsToYuanText(totalCents)}</strong> 元
      </div>

      {documents.length ? (
        documents.map((doc, index) => (
          <div
            key={`${doc.type}-${doc.id}`}
            style={{ border: '1px solid var(--semi-color-border)', borderRadius: 6, marginBottom: 12 }}
          >
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: 24,
                alignItems: 'center',
                padding: '10px 14px',
                background: 'var(--semi-color-fill-0)',
              }}
            >
              <span>单据类型：{doc.type === 'return' ? '采购退货' : '采购入库'}</span>
              {doc.sn ? <span>单据编号：{doc.sn}</span> : null}
              <span>店铺：{doc.shopName || '-'}</span>
              <span>{doc.type === 'return' ? '出库时间' : '入库时间'}：{doc.confirmTime || '-'}</span>
              <span>
                对账总金额：<strong style={{ color: '#f59a23' }}>{fmtYuan(doc.amount)}</strong>
              </span>
              <Button
                theme="borderless"
                type="danger"
                size="small"
                style={{ marginLeft: 'auto' }}
                onClick={() => setDocuments((prev) => prev.filter((_, i) => i !== index))}
              >
                删除
              </Button>
            </div>
            <div style={{ padding: '6px 14px 10px' }}>
              <Table
                dataSource={doc.items || []}
                rowKey="itemKey"
                pagination={false}
                size="small"
                columns={
                  [
                    {
                      title: '商品名称',
                      dataIndex: 'productName',
                      render: (_: any, item: any) => (
                        <div>
                          <div>{item.productName || '-'}</div>
                          {item.skuName ? <Text type="tertiary" size="small">{item.skuName}</Text> : null}
                        </div>
                      ),
                    },
                    { title: '出入库数量', dataIndex: 'quantity', width: 110 },
                    { title: '单价(元)', dataIndex: 'price', width: 100, render: (val: any) => fmtYuan(val) },
                    { title: '小计(元)', dataIndex: 'subtotal', width: 100, render: (val: any) => fmtYuan(val) },
                  ] as any
                }
              />
            </div>
          </div>
        ))
      ) : (
        <Text type="tertiary">尚未选择单据，点击「选择申请单据」勾选需要聚合对账的入库/退货单。</Text>
      )}

      <CandidatesModal
        visible={selectorOpen}
        shopId={shopId}
        supplierId={supplierId}
        selectedIds={documents.map((doc) => doc.id)}
        onCancel={() => setSelectorOpen(false)}
        onConfirm={acceptDocuments}
      />
    </SideSheet>
  );
}

export function PurchaseReconciliation() {
  const [reloadKey, setReloadKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [snInput, setSnInput] = useState('');
  const [statusInput, setStatusInput] = useState('');
  const [applied, setApplied] = useState<{ reconciliationSn?: string; status?: string }>({});
  const [listKey, setListKey] = useState(0);

  const query = useMemo(() => ({ ...applied }), [applied]);

  const applyFilters = () => {
    setApplied({
      reconciliationSn: snInput.trim() || undefined,
      status: statusInput || undefined,
    });
    setListKey((k) => k + 1);
  };

  const resetFilters = () => {
    setSnInput('');
    setStatusInput('');
    setApplied({});
    setListKey((k) => k + 1);
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      await apiDownload(
        '/supplyChain/purchaseReconciliation/export',
        { ...applied },
        `采购对账单_${toYMDHM(new Date()).replace(/[-: ]/g, '')}`,
      );
      Toast.success('导出成功');
    } catch (error) {
      Toast.error({ content: errMsg(error) });
    } finally {
      setExporting(false);
    }
  };

  const columns: ColumnConfig<PurchaseReconciliationItem>[] = [
    {
      title: '对账单编号',
      dataIndex: 'reconciliationSn',
      width: 190,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '对账单状态',
      dataIndex: 'status',
      width: 110,
      render: (val: any) => {
        const status = Number(val);
        if (status === 2) return <Tag color="green">已对账</Tag>;
        if (status === 3) return <Tag color="red">已驳回</Tag>;
        if (status === 4) return <Tag color="grey">已作废</Tag>;
        return <Tag color="orange">待对账</Tag>;
      },
    },
    {
      title: '供应商',
      dataIndex: 'vendorName',
      width: 150,
      render: (val: any, row: PurchaseReconciliationItem) => val || row.supplierName || '--',
    },
    {
      title: '创建人',
      dataIndex: 'createByName',
      width: 110,
      render: (val: any) => val || '--',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
    {
      title: '操作',
      width: 130,
      render: (_: any, record: PurchaseReconciliationItem) => (
        Number(record.status) === 1 ? (
          <Popconfirm
            title="确认对账吗？"
            onConfirm={async () => {
              try {
                await apiPost('/supplyChain/purchaseReconciliation/approve', {
                  id: Number(record.id ?? record.reconciliationId ?? 0),
                  shopId: Number(currentShopId() || 0),
                  remark: '',
                });
                Toast.success('对账成功');
                setReloadKey((k) => k + 1);
              } catch (error) {
                Toast.error({ content: errMsg(error) });
              }
            }}
          >
            <Button theme="borderless" type="primary" size="small">
              对账
            </Button>
          </Popconfirm>
        ) : (
          <Text type="tertiary" size="small">--</Text>
        )
      ),
    },
  ];

  return (
    <div>
      <ListPage<PurchaseReconciliationItem>
        key={`${reloadKey}-${listKey}`}
        api="/supplyChain/purchaseReconciliation/list"
        rowKey={(row) => String(row.id ?? row.reconciliationId ?? '')}
        columns={columns}
        query={query}
        toolbarExtra={
          <>
            <Input
              prefix={<IconSearch />}
              placeholder="输入对账单号"
              value={snInput}
              showClear
              style={{ width: 200 }}
              onChange={(val) => setSnInput(val)}
              onEnterPress={applyFilters}
            />
            <Select
              placeholder="状态"
              style={{ width: 130 }}
              value={statusInput || undefined}
              optionList={[
                { label: '全部', value: '' },
                { label: '待对账', value: '1' },
                { label: '已对账', value: '2' },
                { label: '已驳回', value: '3' },
                { label: '已作废', value: '4' },
              ]}
              onChange={(val) => setStatusInput(val === undefined || val === null ? '' : String(val))}
            />
            <Button type="primary" theme="solid" icon={<IconSearch />} onClick={applyFilters}>
              搜索
            </Button>
            <Button onClick={resetFilters}>重置</Button>
            <Button loading={exporting} onClick={handleExport}>
              导出EXCEL
            </Button>
            <Button icon={<IconPlus />} type="primary" theme="solid" onClick={() => setCreateOpen(true)}>
              新建对账单
            </Button>
          </>
        }
      />

      <PurchaseReconciliationCreateDrawer
        visible={createOpen}
        onClose={() => setCreateOpen(false)}
        onSaved={() => {
          setCreateOpen(false);
          setReloadKey((k) => k + 1);
        }}
      />
    </div>
  );
}
