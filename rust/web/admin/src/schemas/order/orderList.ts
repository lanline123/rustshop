import type { TableSchema } from '@/components/schema/types';

/**
 * 订单列表（Schema 版；实际页面为 `views/business/OrderListView.vue`，支持详情/发货/改价/物流）。
 *
 * 状态码对齐 Rust `OrderStatusEnum`（`services/trade/order.rs`）：
 * 0 待付款 / 1 待发货 / 2 已发货 / 3 已取消 / 4 已失效 / 5 已完成。
 */
export const orderListTableSchema: TableSchema = {
  title: '商城订单管理',
  apiList: '/order/order/list',
  primaryKey: 'orderId',
  searchFields: [
    {
      prop: 'keyword',
      label: '关键词',
      type: 'input',
      placeholder: '订单号 / 收货人 / 手机号'
    },
    {
      prop: 'orderStatus',
      label: '订单状态',
      type: 'select',
      options: [
        { label: '待付款', value: 0 },
        { label: '待发货', value: 1 },
        { label: '已发货', value: 2 },
        { label: '已取消', value: 3 },
        { label: '已失效', value: 4 },
        { label: '已完成', value: 5 }
      ]
    }
  ],
  columns: [
    { prop: 'orderSn', label: '订单号', minWidth: 180 },
    { prop: 'consignee', label: '收件人姓名', minWidth: 120 },
    { prop: 'mobile', label: '收件人手机', minWidth: 130 },
    { prop: 'totalAmount', label: '应付金额', type: 'money', width: 120 },
    {
      prop: 'orderStatus',
      label: '订单状态',
      type: 'tag',
      width: 100,
      tagMap: {
        0: { text: '待付款', type: 'warning' },
        1: { text: '待发货', type: 'primary' },
        2: { text: '已发货', type: 'primary' },
        3: { text: '已取消', type: 'info' },
        4: { text: '已失效', type: 'info' },
        5: { text: '已完成', type: 'success' }
      }
    },
    { prop: 'addTime', label: '下单时间', minWidth: 160 }
  ]
};
