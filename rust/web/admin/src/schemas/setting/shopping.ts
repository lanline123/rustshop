import type { FormSchema } from '@/components/schema/types';

export const shoppingSettingSchema: FormSchema = {
  title: '交易与履约设置',
  apiGet: '/setting/config/shoppingSettings',
  apiSave: '/setting/config/saveShopping',
  labelWidth: '180px',
  sections: [
    {
      title: '订单生命周期规则',
      description: '设置未支付自动关闭、发货后自动收货时间',
      fields: [
        {
          prop: 'orderCancelMinutes',
          label: '未支付超时取消(分)',
          type: 'number',
          min: 1,
          max: 1440,
          defaultValue: 30,
          extra: '买家下单后超过指定分钟数未支付，订单将自动取消并返还库存'
        },
        {
          prop: 'autoReceiveDays',
          label: '发货后自动确认收货(天)',
          type: 'number',
          min: 1,
          max: 30,
          defaultValue: 7,
          extra: '商家发货后超过指定天数买家未主动点击收货，系统将自动变为已收货'
        },
        {
          prop: 'afterSaleDays',
          label: '收货后申请售后时限(天)',
          type: 'number',
          min: 0,
          max: 60,
          defaultValue: 7,
          extra: '确认收货后超过该天数，买家将无法再对该订单发起售后申请'
        }
      ]
    },
    {
      title: '积分抵扣规则',
      fields: [
        {
          prop: 'pointsRate',
          label: '消费送积分比例',
          type: 'number',
          min: 0,
          defaultValue: 1,
          extra: '每消费 1 元赠送多少点积分'
        },
        {
          prop: 'pointsDeductionRate',
          label: '积分抵现比例',
          type: 'number',
          min: 1,
          defaultValue: 100,
          extra: '多少点积分可抵扣 1 元人民币现金'
        }
      ]
    }
  ]
};
