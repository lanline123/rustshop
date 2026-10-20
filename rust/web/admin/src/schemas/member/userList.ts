import type { TableSchema } from '@/components/schema/types';

/**
 * 会员用户列表。
 *
 * 接口：`GET /adminapi/user/user/list`（原写 `/member/user/list`，契约中不存在 → 列表恒空）。
 * 字段对齐 Rust `tigshop_core::models::member::user_manage::UserVO`
 * 与 Java 原版 `views/user/user/List.vue` 的列定义。
 */
export const userListTableSchema: TableSchema = {
  title: '会员用户管理',
  apiList: '/user/user/list',
  primaryKey: 'userId',
  searchFields: [
    { prop: 'keyword', label: '关键词', type: 'input', placeholder: '账号 / 昵称 / 手机号...' },
    { prop: 'mobile', label: '手机号码', type: 'input', placeholder: '搜索绑定手机...' }
  ],
  columns: [
    { prop: 'userId', label: 'UID', width: 80, align: 'center' },
    { prop: 'avatar', label: '头像', type: 'image', width: 80 },
    { prop: 'username', label: '客户名称', minWidth: 130 },
    { prop: 'nickname', label: '会员昵称', minWidth: 130 },
    { prop: 'mobile', label: '绑定手机', minWidth: 130 },
    { prop: 'rankName', label: '客户等级', minWidth: 110 },
    { prop: 'isCompanyAuth', label: '是否认证', width: 100, align: 'center' },
    { prop: 'balance', label: '余额', type: 'money', width: 110 },
    { prop: 'points', label: '消费积分', width: 100, align: 'center' },
    { prop: 'orderCount', label: '累计消费次数', width: 120, align: 'center' },
    { prop: 'orderAmount', label: '累计消费金额', type: 'money', width: 130 },
    {
      prop: 'status',
      label: '是否启用',
      type: 'tag',
      width: 100,
      tagMap: {
        1: { text: '启用', type: 'success' },
        0: { text: '停用', type: 'danger' }
      }
    },
    { prop: 'regTime', label: '注册日期', minWidth: 160 }
  ]
};
