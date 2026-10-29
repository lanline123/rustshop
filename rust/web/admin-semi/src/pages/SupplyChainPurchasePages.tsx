import React, { useState } from 'react';
import {
  Button,
  Card,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconPlus } from '@douyinfe/semi-icons';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

/* ==================================================================
   1. 采购订单管理 (PurchaseOrders / purchaseOrderManage)
================================================================== */
interface PurchaseOrderItem {
  orderId: number;
  orderSn: string;
  supplierName?: string;
  totalAmount: number | string;
  totalQuantity: number;
  status: number; // 0: 待审核, 1: 采购中, 2: 部分入库, 3: 已完成, 4: 已作废
  createTime?: string;
}

export function PurchaseOrders() {
  const [reloadKey, setReloadKey] = useState(0);

  const columns: ColumnConfig<PurchaseOrderItem>[] = [
    {
      title: '采购单号',
      dataIndex: 'orderSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      render: (val: any) => val || '--',
    },
    {
      title: '采购总额',
      dataIndex: 'totalAmount',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#165dff' }}>¥{val}</span>,
    },
    {
      title: '采购件数',
      dataIndex: 'totalQuantity',
      width: 110,
      render: (val: any) => val ?? 0,
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      width: 120,
      render: (val: any) => {
        const map: Record<number, { text: string; color: any }> = {
          0: { text: '待审核', color: 'orange' },
          1: { text: '采购中', color: 'blue' },
          2: { text: '部分入库', color: 'cyan' },
          3: { text: '已完成', color: 'green' },
          4: { text: '已作废', color: 'grey' },
        };
        const cur = map[val] || { text: '处理中', color: 'blue' };
        return <Tag color={cur.color}>{cur.text}</Tag>;
      },
    },
    {
      title: '下单时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
    {
      title: '操作',
      width: 120,
      render: (_: any, record: PurchaseOrderItem) => (
        record.status === 1 ? (
          <Popconfirm
            title="确认标记采购完成？"
            onConfirm={async () => {
              await apiPost('/supplyChain/purchaseOrder/finish', { id: record.orderId });
              Toast.success('采购已完成');
              setReloadKey((k) => k + 1);
            }}
          >
            <Button theme="borderless" type="primary" size="small">
              完结
            </Button>
          </Popconfirm>
        ) : (
          <Text type="tertiary" size="small">详情</Text>
        )
      ),
    },
  ];

  return (
    <ListPage<PurchaseOrderItem>
      key={reloadKey}
      api="/supplyChain/purchaseOrder/list"
      rowKey="orderId"
      columns={columns}
      filters={[
        {
          field: 'orderSn',
          label: '采购单号',
          placeholder: '输入采购单号',
          type: 'input',
        },
        {
          field: 'status',
          label: '状态',
          type: 'select',
          options: [
            { label: '全部', value: '' },
            { label: '待审核', value: 0 },
            { label: '采购中', value: 1 },
            { label: '已完成', value: 3 },
          ],
        },
      ]}
    />
  );
}

/* ==================================================================
   2. 采购入库 (PurchaseInbound / purchaseInboundManage)
================================================================== */
interface PurchaseReceiptItem {
  receiptId: number;
  receiptSn: string;
  orderSn?: string;
  supplierName?: string;
  inboundQuantity: number;
  operator?: string;
  inboundTime?: string;
}

export function PurchaseInbound() {
  const columns: ColumnConfig<PurchaseReceiptItem>[] = [
    {
      title: '入库单号',
      dataIndex: 'receiptSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '关联采购单',
      dataIndex: 'orderSn',
      width: 180,
      render: (val: any) => val || '--',
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      render: (val: any) => val || '--',
    },
    {
      title: '入库件数',
      dataIndex: 'inboundQuantity',
      width: 120,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#00b42a' }}>+{val}</span>,
    },
    {
      title: '操作人',
      dataIndex: 'operator',
      width: 120,
      render: (val: any) => val || '管理员',
    },
    {
      title: '入库时间',
      dataIndex: 'inboundTime',
      width: 170,
      render: (val: any) => val || '--',
    },
  ];

  return (
    <ListPage<PurchaseReceiptItem>
      api="/supplyChain/purchaseReceipt/list"
      rowKey="receiptId"
      columns={columns}
      filters={[
        {
          field: 'keyword',
          label: '入库单号',
          placeholder: '输入入库单号/采购单号',
          type: 'input',
        },
      ]}
    />
  );
}

/* ==================================================================
   3. 采购退货 (PurchaseReturns / purchaseReturnManage)
================================================================== */
interface PurchaseReturnItem {
  returnId: number;
  returnSn: string;
  orderSn?: string;
  supplierName?: string;
  returnAmount: number | string;
  returnQuantity: number;
  status: number; // 0: 待审核, 1: 退货中, 2: 已退款
  createTime?: string;
}

export function PurchaseReturns() {
  const columns: ColumnConfig<PurchaseReturnItem>[] = [
    {
      title: '退货单号',
      dataIndex: 'returnSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '原采购单',
      dataIndex: 'orderSn',
      width: 180,
      render: (val: any) => val || '--',
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      render: (val: any) => val || '--',
    },
    {
      title: '退货金额',
      dataIndex: 'returnAmount',
      width: 130,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#eb2f96' }}>¥{val}</span>,
    },
    {
      title: '退货件数',
      dataIndex: 'returnQuantity',
      width: 110,
      render: (val: any) => val ?? 0,
    },
    {
      title: '退货状态',
      dataIndex: 'status',
      width: 110,
      render: (val: any) => {
        const map: Record<number, { text: string; color: any }> = {
          0: { text: '待审核', color: 'orange' },
          1: { text: '退货中', color: 'blue' },
          2: { text: '已退款', color: 'green' },
        };
        const cur = map[val] || { text: '处理中', color: 'blue' };
        return <Tag color={cur.color}>{cur.text}</Tag>;
      },
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      width: 170,
      render: (val: any) => val || '--',
    },
  ];

  return (
    <ListPage<PurchaseReturnItem>
      api="/supplyChain/purchaseReturn/list"
      rowKey="returnId"
      columns={columns}
      filters={[
        {
          field: 'keyword',
          label: '退货单号',
          placeholder: '输入退货单号/采购单号',
          type: 'input',
        },
      ]}
    />
  );
}

/* ==================================================================
   4. 采购对账/结算 (PurchaseReconciliation / purchaseReconciliationManage)
================================================================== */
interface PurchaseReconciliationItem {
  id: number;
  reconciliationSn: string;
  supplierName?: string;
  billingAmount: number | string;
  paidAmount: number | string;
  status: number; // 0: 待对账, 1: 已确认, 2: 已结算
  periodStart?: string;
  periodEnd?: string;
}

export function PurchaseReconciliation() {
  const [reloadKey, setReloadKey] = useState(0);

  const columns: ColumnConfig<PurchaseReconciliationItem>[] = [
    {
      title: '对账批次号',
      dataIndex: 'reconciliationSn',
      width: 180,
      render: (val: any) => <span style={{ fontWeight: 600 }}>{val}</span>,
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      render: (val: any) => val || '--',
    },
    {
      title: '应付账款',
      dataIndex: 'billingAmount',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600 }}>¥{val}</span>,
    },
    {
      title: '实付金额',
      dataIndex: 'paidAmount',
      width: 140,
      render: (val: any) => <span style={{ fontWeight: 600, color: '#00b42a' }}>¥{val}</span>,
    },
    {
      title: '对账状态',
      dataIndex: 'status',
      width: 110,
      render: (val: any) => {
        if (val === 2) return <Tag color="green">已结算</Tag>;
        if (val === 1) return <Tag color="blue">已确认</Tag>;
        return <Tag color="orange">待对账</Tag>;
      },
    },
    {
      title: '对账周期',
      dataIndex: 'period',
      width: 220,
      render: (_: any, row: PurchaseReconciliationItem) => (
        <span>{row.periodStart || '--'} ~ {row.periodEnd || '--'}</span>
      ),
    },
    {
      title: '操作',
      width: 130,
      render: (_: any, record: PurchaseReconciliationItem) => (
        record.status === 0 ? (
          <Popconfirm
            title="确认审核通过此对账批次？"
            onConfirm={async () => {
              await apiPost('/supplyChain/purchaseReconciliation/approve', { id: record.id });
              Toast.success('对账单已确认');
              setReloadKey((k) => k + 1);
            }}
          >
            <Button theme="borderless" type="primary" size="small">
              确认对账
            </Button>
          </Popconfirm>
        ) : (
          <Text type="tertiary" size="small">已完成</Text>
        )
      ),
    },
  ];

  return (
    <ListPage<PurchaseReconciliationItem>
      key={reloadKey}
      api="/supplyChain/purchaseReconciliation/list"
      rowKey="id"
      columns={columns}
      filters={[
        {
          field: 'keyword',
          label: '对账批次',
          placeholder: '输入对账批次号/供应商',
          type: 'input',
        },
      ]}
    />
  );
}
