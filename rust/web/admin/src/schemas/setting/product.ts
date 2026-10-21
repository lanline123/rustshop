import type { FormSchema } from '@/components/schema/types';

export const productSettingSchema: FormSchema = {
  title: '商品全局设置',
  apiGet: '/setting/config/productSettings',
  apiSave: '/setting/config/saveProduct',
  labelWidth: '180px',
  sections: [
    {
      title: '商品基础规范',
      description: '定义全平台商品货号前缀、库存预警阈值',
      fields: [
        {
          prop: 'productCodePrefix',
          label: '商品货号前缀',
          type: 'input',
          defaultValue: 'SN',
          extra: '自动生成商品货号时的固定前缀'
        },
        {
          prop: 'goodsStorageWarning',
          label: '全局库存预警数',
          type: 'number',
          min: 0,
          max: 99999,
          defaultValue: 10,
          extra: '当商品 SKU 库存低于此数值时，系统会自动发出预警提示'
        },
        {
          prop: 'marketPriceRate',
          label: '划线市场价倍率',
          type: 'number',
          min: 1,
          max: 10,
          step: 0.1,
          defaultValue: 1.2,
          extra: '未填写市场价时，按本店售价乘以该倍率自动计算划线价'
        }
      ]
    },
    {
      title: '商品展示策略',
      fields: [
        {
          prop: 'showSoldNumber',
          label: '显示商品已售件数',
          type: 'switch',
          defaultValue: 1,
          extra: '开启后在前台商品列表与详情页展示实际销量'
        },
        {
          prop: 'showInventory',
          label: '显示实际剩余库存',
          type: 'switch',
          defaultValue: 1,
          extra: '关闭后前台仅显示有货或缺货状态，不展示具体数字'
        }
      ]
    }
  ]
};
