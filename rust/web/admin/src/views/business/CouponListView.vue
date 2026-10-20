<template>
  <div class="business-page">
    <SchemaTable :schema="schema" />
  </div>
</template>

<script setup lang="ts">
/**
 * 营销优惠券：列表 + 新增/编辑弹窗。
 *
 * 接口（Rust `routes/marketing/coupon_manage.rs`）：
 * - 列表：`GET /adminapi/promotion/coupon/list`
 * - 详情：`GET /adminapi/promotion/coupon/detail?id=`
 * - 新增：`POST /adminapi/promotion/coupon/create`（CouponDTO）
 * - 编辑：`POST /adminapi/promotion/coupon/update`（CouponDTO）
 * - 删除：`POST /adminapi/promotion/coupon/del`（OperateDTO { id }）
 * - 批量删除：`POST /adminapi/promotion/coupon/batch`（BatchDTO { ids, type: 'del' }）
 */
import { computed } from 'vue';
import SchemaTable from '@/components/schema/SchemaTable.vue';
import type { TableSchema } from '@/components/schema/types';

const COUPON_TYPE: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  1: { text: '满减券', type: 'success' },
  2: { text: '折扣券', type: 'warning' }
};

const COUPON_STATUS: Record<number, { text: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
  1: { text: '进行中', type: 'success' },
  2: { text: '待生效', type: 'warning' },
  3: { text: '已失效', type: 'info' }
};

const schema = computed<TableSchema>(() => ({
  title: '优惠券管理',
  apiList: '/promotion/coupon/list',
  apiDelete: '/promotion/coupon/del',
  apiBatchDelete: '/promotion/coupon/batch',
  primaryKey: 'couponId',
  searchFields: [
    { prop: 'couponName', label: '优惠券名称', type: 'input', placeholder: '搜索优惠券名称' },
    {
      prop: 'couponType',
      label: '优惠券类型',
      type: 'select',
      options: [
        { label: '满减券', value: 1 },
        { label: '折扣券', value: 2 }
      ]
    }
  ],
  columns: [
    { prop: 'couponId', label: 'ID', width: 80, align: 'center' },
    { prop: 'couponName', label: '优惠券名称', minWidth: 180 },
    {
      prop: 'couponType',
      label: '类型',
      type: 'tag',
      width: 100,
      tagMap: COUPON_TYPE
    },
    { prop: 'couponMoney', label: '减免面额', type: 'money', width: 110 },
    { prop: 'couponDiscount', label: '折扣', width: 90, align: 'center' },
    { prop: 'minOrderAmount', label: '使用门槛', type: 'money', width: 110 },
    { prop: 'sendNum', label: '发放数量', width: 100, align: 'center' },
    { prop: 'receiveNum', label: '已领取', width: 100, align: 'center' },
    {
      prop: 'status',
      label: '状态',
      type: 'tag',
      width: 100,
      tagMap: COUPON_STATUS
    },
    { prop: 'statusName', label: '状态说明', width: 110 }
  ],
  formSchema: {
    title: '优惠券',
    primaryKey: 'couponId',
    apiCreate: '/promotion/coupon/create',
    apiUpdate: '/promotion/coupon/update',
    apiDetail: '/promotion/coupon/detail',
    labelWidth: '140px',
    sections: [
      {
        title: '基本信息',
        fields: [
          {
            prop: 'couponName',
            label: '优惠券名称',
            type: 'input',
            rules: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }]
          },
          {
            prop: 'couponType',
            label: '优惠券类型',
            type: 'select',
            defaultValue: 1,
            options: [
              { label: '满减券', value: 1 },
              { label: '折扣券', value: 2 }
            ],
            rules: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }]
          },
          { prop: 'couponDesc', label: '使用说明', type: 'textarea' },
          { prop: 'isShow', label: '前台展示', type: 'switch', defaultValue: 1 }
        ]
      },
      {
        title: '优惠规则',
        fields: [
          { prop: 'couponMoney', label: '减免金额(元)', type: 'number', min: 0, step: 0.01, defaultValue: 10 },
          {
            prop: 'couponDiscount',
            label: '折扣(如 0.8)',
            type: 'number',
            min: 0,
            max: 1,
            step: 0.01,
            extra: '仅折扣券生效：0.8 表示 8 折'
          },
          { prop: 'minOrderAmount', label: '使用门槛(元)', type: 'number', min: 0, step: 0.01, defaultValue: 0 },
          { prop: 'maxOrderAmount', label: '最高抵扣(元)', type: 'number', min: 0, step: 0.01 },
          { prop: 'limitNum', label: '每人限领', type: 'number', min: 0, defaultValue: 1 }
        ]
      },
      {
        title: '发放设置',
        fields: [
          {
            prop: 'sendType',
            label: '发放方式',
            type: 'select',
            defaultValue: 1,
            options: [
              { label: '固定时间领取', value: 1 },
              { label: '领取后 N 天有效', value: 2 }
            ]
          },
          { prop: 'sendStartDate', label: '发放开始', type: 'datetime' },
          { prop: 'sendEndDate', label: '发放结束', type: 'datetime' },
          { prop: 'useDay', label: '领取后有效天数', type: 'number', min: 0, defaultValue: 7 },
          { prop: 'sendNum', label: '发放总量', type: 'number', min: 0, defaultValue: 100 }
        ]
      }
    ]
  }
}));
</script>

<style scoped>
.business-page {
  width: 100%;
}
</style>
