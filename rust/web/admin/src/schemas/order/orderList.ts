import type { TableSchema } from '@/components/schema/types';
import { ElMessage } from 'element-plus';

export const orderListTableSchema: TableSchema = {
  title: '商城订单管理',
  apiList: '/order/order/list',
  primaryKey: 'orderId',
  searchFields: [
    {
      prop: 'orderSn',
      label: '订单编号',
      type: 'input',
      placeholder: '搜索订单号...'
    },
    {
      prop: 'orderStatus',
      label: '订单状态',
      type: 'select',
      options: [
        { label: '全部状态', value: '' },
        { label: '待付款', value: 0 },
        { label: '待发货', value: 1 },
        { label: '待收货', value: 2 },
        { label: '已完成', value: 3 },
        { label: '已取消', value: 4 }
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
        3: { text: '已完成', type: 'success' },
        4: { text: '已取消', type: 'info' }
      }
    },
    { prop: 'addTime', label: '下单时间', minWidth: 160 }
  ],
  actions: [
    {
      label: '查看详情',
      type: 'primary',
      onClick: (row) => {
        ElMessage.info(`查看订单详情: ${row.orderSn || row.orderId}`);
      }
    }
  ]
};
