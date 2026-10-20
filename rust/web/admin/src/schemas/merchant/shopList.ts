import type { TableSchema } from '@/components/schema/types';

export const shopListTableSchema: TableSchema = {
  title: '入驻商户店铺管理',
  apiList: '/merchant/merchant/list',
  primaryKey: 'shopId',
  searchFields: [
    { prop: 'shopTitle', label: '店铺名称', type: 'input', placeholder: '搜索店铺名称...' },
    {
      prop: 'status',
      label: '审核状态',
      type: 'select',
      options: [
        { label: '全部', value: '' },
        { label: '正常营业', value: 1 },
        { label: '待审核', value: 0 },
        { label: '已关闭', value: 2 }
      ]
    }
  ],
  columns: [
    { prop: 'shopId', label: '店铺ID', width: 90, align: 'center' },
    { prop: 'shopLogo', label: '店铺LOGO', type: 'image', width: 90 },
    { prop: 'shopTitle', label: '店铺全称', minWidth: 160 },
    { prop: 'contactName', label: '联系人', minWidth: 120 },
    { prop: 'contactMobile', label: '联系电话', minWidth: 130 },
    {
      prop: 'status',
      label: '状态',
      type: 'tag',
      width: 100,
      tagMap: {
        1: { text: '正常', type: 'success' },
        0: { text: '待审核', type: 'warning' },
        2: { text: '已冻结', type: 'danger' }
      }
    },
    { prop: 'createTime', label: '入驻时间', minWidth: 160 }
  ]
};
