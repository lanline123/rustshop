<template>
  <aside class="tig-member-nav">
    <div class="nav-group-title">{{ groupTitle }}</div>
    <router-link
      v-for="item in items"
      :key="item.path"
      :to="item.path"
      class="nav-item"
      :class="{ active: isActive(item.path) }"
    >
      <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </router-link>
  </aside>
</template>

<script setup lang="ts">
/**
 * 会员中心左侧导航（对齐原版 `MemberNav`）。
 *
 * 菜单项按原版结构：会员中心 / 我的订单 / 评价晒单 / 售后申请 / 我的优惠券 /
 * 心愿单 / 浏览记录 / 收货地址 / 站内信 / 签到。
 * 仅保留有 Rust 契约支撑的入口（礼品卡无客户端契约，未列入）。
 */
import { useRoute } from 'vue-router';
import {
  HomeFilled,
  List,
  ChatDotSquare,
  RefreshLeft,
  Ticket,
  Star,
  Clock,
  Location,
  Bell,
  Calendar,
} from '@element-plus/icons-vue';

withDefaults(
  defineProps<{
    groupTitle?: string;
  }>(),
  { groupTitle: '会员中心' },
);

const route = useRoute();

const items = [
  { label: '会员中心', path: '/member', icon: HomeFilled },
  { label: '我的订单', path: '/member/orders', icon: List },
  { label: '评价晒单', path: '/member/comments', icon: ChatDotSquare },
  { label: '售后申请', path: '/member/aftersales', icon: RefreshLeft },
  { label: '我的优惠券', path: '/member/coupons', icon: Ticket },
  { label: '心愿单', path: '/member/favorites', icon: Star },
  { label: '浏览记录', path: '/member/history', icon: Clock },
  { label: '收货地址', path: '/member/address', icon: Location },
  { label: '站内信', path: '/member/messages', icon: Bell },
  { label: '签到', path: '/member/sign', icon: Calendar },
];

function isActive(path: string): boolean {
  if (path === '/member') return route.path === '/member';
  return route.path === path || route.path.startsWith(`${path}/`);
}
</script>

<style scoped lang="less">
.tig-member-nav {
  background: #ffffff;
  border-radius: 8px;
  padding: 10px 0;
  width: 216px;
  flex: 0 0 216px;

  .nav-group-title {
    padding: 8px 24px;
    font-size: 14px;
    font-weight: 700;
    color: var(--tig-text-main);
    margin-bottom: 4px;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 24px;
    font-size: 14px;
    color: #555;
    transition: all 0.2s;
    border-left: 3px solid transparent;

    .nav-icon { font-size: 16px; color: #999; }
    .nav-count {
      margin-left: auto;
      background: var(--tig-red);
      color: #fff;
      font-size: 11px;
      border-radius: 9px;
      padding: 0 6px;
      line-height: 16px;
    }

    &:hover { color: var(--tig-red); background: #fff7f6; }

    &.active {
      color: var(--tig-red);
      background: #fff7f6;
      border-left-color: var(--tig-red);
      font-weight: 600;
      .nav-icon { color: var(--tig-red); }
    }
  }
}
</style>
