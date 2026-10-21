import type { TableSchema } from '@/components/schema/types';

export const couponTableSchema: TableSchema = {
  title: '优惠券管理',
  apiList: '/promotion/coupon/list',
  apiDelete: '/promotion/coupon/del',
  primaryKey: 'couponId',
  searchFields: [
    { prop: 'couponName', label: '优惠券名称', type: 'input', placeholder: '搜索优惠券...' }
  ],
  columns: [
    { prop: 'couponId', label: 'ID', width: 80, align: 'center' },
    { prop: 'couponName', label: '优惠券名称', minWidth: 160 },
    { prop: 'couponMoney', label: '减免面额', type: 'money', width: 110 },
    { prop: 'minOrderAmount', label: '使用门槛', type: 'money', width: 110 },
    { prop: 'sendTotal', label: '发放总量', width: 100, align: 'center' },
    { prop: 'usedCount', label: '已核销数', width: 100, align: 'center' },
    {
      prop: 'status',
      label: '状态',
      type: 'tag',
      width: 100,
      tagMap: {
        1: { text: '进行中', type: 'success' },
        0: { text: '已结束', type: 'info' }
      }
    },
    { prop: 'sendEndDate', label: '发放截止时间', minWidth: 160 }
  ],
  formSchema: {
    title: '优惠券',
    apiSave: '/promotion/coupon/update',
    labelWidth: '130px',
    sections: [
      {
        fields: [
          {
            prop: 'couponName',
            label: '优惠券名称',
            type: 'input',
            rules: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }]
          },
          {
            prop: 'couponMoney',
            label: '优惠面额(元)',
            type: 'number',
            min: 1,
            defaultValue: 10
          },
          {
            prop: 'minOrderAmount',
            label: '使用门槛(元)',
            type: 'number',
            min: 0,
            defaultValue: 99
          },
          {
            prop: 'sendTotal',
            label: '发放总量',
            type: 'number',
            min: 1,
            defaultValue: 1000
          }
        ]
      }
    ]
  }
};
