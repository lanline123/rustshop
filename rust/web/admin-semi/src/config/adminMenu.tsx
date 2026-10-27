import type { ReactNode } from 'react';
import {
  IconApartment,
  IconApps,
  IconBox,
  IconComment,
  IconCreditCard,
  IconFile,
  IconGift,
  IconHistogram,
  IconHome,
  IconIdCard,
  IconLive,
  IconOrderedList,
  IconServer,
  IconSetting,
  IconShare,
  IconShoppingBag,
  IconUser,
  IconVideo,
} from '@douyinfe/semi-icons';

export interface NavItem {
  key: string;
  label: string;
}

export interface NavGroup {
  key: string;
  label: string;
  icon: ReactNode;
  items: NavItem[];
}

/** 与原版 authority 表 parent_id=0 / parent_id=一级 对齐。 */
export const navGroups: NavGroup[] = [
  {
    key: 'customerService',
    label: '客服',
    icon: <IconComment />,
    items: [
      { key: 'cs-workbench', label: '客服工作台' },
      { key: 'cs-history', label: '历史会话' },
      { key: 'cs-profile', label: '个人设置' },
      { key: 'cs-manage', label: '客服管理' },
      { key: 'cs-setting', label: '系统设置' },
    ],
  },
  {
    key: 'pos',
    label: '收银',
    icon: <IconShoppingBag />,
    items: [
      { key: 'pos-workbench', label: '收银工作台' },
      { key: 'pos-setting', label: '收银设置' },
      { key: 'pos-cashier', label: '收银员管理' },
    ],
  },
  {
    key: 'panel',
    label: '面板',
    icon: <IconHome />,
    items: [
      { key: 'dashboard', label: '概览' },
      { key: 'panel-sales', label: '销售统计' },
      { key: 'panel-user', label: '用户统计' },
    ],
  },
  {
    key: 'organize',
    label: '组织',
    icon: <IconApartment />,
    items: [
      { key: 'org-shop', label: '店铺管理' },
      { key: 'org-supplier', label: '供应商管理' },
      { key: 'org-store', label: '门店管理' },
      { key: 'org-pickup', label: '自提点管理' },
      { key: 'org-staff', label: '员工管理' },
      { key: 'org-merchant', label: '商户管理' },
    ],
  },
  {
    key: 'live',
    label: '直播',
    icon: <IconLive />,
    items: [
      { key: 'live-mini', label: '小程序直播' },
      { key: 'live-video', label: '视频号' },
    ],
  },
  {
    key: 'product',
    label: '商品',
    icon: <IconShoppingBag />,
    items: [
      { key: 'products', label: '商品管理' },
      { key: 'product-batch', label: '批量操作' },
      { key: 'product-stock', label: '库存管理' },
      { key: 'product-inquiry', label: '询价管理' },
    ],
  },
  {
    key: 'promotion',
    label: '营销',
    icon: <IconGift />,
    items: [
      { key: 'promotion-overview', label: '营销概览' },
      { key: 'promotion-manage', label: '营销管理' },
      { key: 'promotion-style', label: '营销样式' },
    ],
  },
  {
    key: 'order',
    label: '订单',
    icon: <IconOrderedList />,
    items: [
      { key: 'orders', label: '订单管理' },
      { key: 'order-aftersales', label: '售后管理' },
    ],
  },
  {
    key: 'supplyChain',
    label: '供应链',
    icon: <IconServer />,
    items: [
      { key: 'supply-purchase', label: '采购管理' },
      { key: 'supply-supplier', label: '供应商管理' },
      { key: 'supply-setting', label: '设置' },
    ],
  },
  {
    key: 'decorate',
    label: '装修',
    icon: <IconApps />,
    items: [
      { key: 'decorate-theme', label: '主题风格' },
      { key: 'decorate-mobile', label: '移动端装修' },
      { key: 'decorate-pc', label: 'PC端装修' },
    ],
  },
  {
    key: 'salesman',
    label: '分销',
    icon: <IconShare />,
    items: [
      { key: 'salesman-overview', label: '分销概览' },
      { key: 'salesman-setting', label: '分销设置' },
      { key: 'salesman-product', label: '分销商品' },
      { key: 'salesman-member', label: '分销员' },
      { key: 'salesman-settle', label: '分销结算' },
      { key: 'salesman-content', label: '内容管理' },
    ],
  },
  {
    key: 'content',
    label: '内容',
    icon: <IconFile />,
    items: [
      { key: 'content-material', label: '素材管理' },
      { key: 'content', label: '文章管理' },
      { key: 'content-protocol', label: '协议管理' },
    ],
  },
  {
    key: 'finance',
    label: '财务',
    icon: <IconCreditCard />,
    items: [
      { key: 'finance-member', label: '会员资金管理' },
      { key: 'finance-split', label: '分账管理' },
      { key: 'finance-shop', label: '店铺资金管理' },
      { key: 'finance-vendor', label: '供应商资金管理' },
      { key: 'finance-term', label: '账期支付' },
      { key: 'finance-invoice', label: '发票管理' },
      { key: 'finance-deposit', label: '保证金管理' },
      { key: 'finance-refund', label: '退款管理' },
      { key: 'finance-trade', label: '交易管理' },
      { key: 'finance-staff', label: '员工收益管理' },
    ],
  },
  {
    key: 'user',
    label: '会员',
    icon: <IconUser />,
    items: [
      { key: 'members', label: '会员管理' },
      { key: 'user-realname', label: '实名认证' },
      { key: 'user-message', label: '消息管理' },
      { key: 'user-premium', label: '付费会员' },
    ],
  },
  {
    key: 'authority',
    label: '权限',
    icon: <IconIdCard />,
    items: [
      { key: 'authority-admin', label: '管理员' },
      { key: 'authority-role', label: '系统权限' },
    ],
  },
  {
    key: 'wholesale',
    label: '批发',
    icon: <IconBox />,
    items: [
      { key: 'wholesale-merchant', label: '批发商' },
      { key: 'wholesale-product', label: '批发商品' },
    ],
  },
  {
    key: 'setting',
    label: '设置',
    icon: <IconSetting />,
    items: [
      { key: 'setting-shop', label: '商城设置' },
      { key: 'setting-shipping', label: '配送设置组' },
      { key: 'setting-system', label: '系统设置' },
      { key: 'setting-language', label: '多语言管理' },
      { key: 'setting-message', label: '消息设置' },
      { key: 'setting-wechat', label: '微信公众号' },
    ],
  },
];

export const pageTitles: Record<string, string> = Object.fromEntries(
  navGroups.flatMap((group) => group.items.map((item) => [item.key, item.label])),
);
