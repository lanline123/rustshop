<template>
  <div class="tig-member-page">
    <div v-loading="user.loading" class="container">
      <!-- 用户信息 -->
      <div class="infout">
        <div class="user-info">
          <el-avatar :size="80" :src="avatar" class="avatar">
            {{ (user.displayName || 'U').slice(0, 1) }}
          </el-avatar>
          <div class="user-col">
            <div class="username">
              <span class="nickname">{{ user.displayName || '未设置昵称' }}</span>
              <el-tag v-if="center?.rankName" size="small" type="warning">{{ center.rankName }}</el-tag>
            </div>
            <div class="user-title">
              <router-link to="/member/orders" class="title-link">我的订单 ›</router-link>
              <router-link to="/member/address" class="title-link">我的地址 ›</router-link>
              <router-link to="/member/messages" class="title-link">站内信 ›</router-link>
              <router-link to="/member/sign" class="title-link">每日签到 ›</router-link>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ formatPrice(center?.balance) }}</div>
              <div class="stat-label">账户余额</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ center?.points ?? 0 }}</div>
              <div class="stat-label">可用积分</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单状态角标 -->
      <div class="user-totu">
        <router-link to="/member/orders" class="totu-entry">
          <span class="entry-icon"><el-icon><List /></el-icon></span>
          <span class="entry-text">我的订单</span>
          <span class="entry-arrow">›</span>
        </router-link>
        <router-link :to="{ path: '/member/orders', query: { status: '0' } }" class="totu-item">
          <span class="totu-number">{{ countValue(orderNum?.awaitPay) }}</span>
          <span class="totu-text">待付款</span>
        </router-link>
        <router-link :to="{ path: '/member/orders', query: { status: '2' } }" class="totu-item">
          <span class="totu-number">{{ countValue(orderNum?.awaitReceived) }}</span>
          <span class="totu-text">待收货</span>
        </router-link>
        <router-link to="/member/comments" class="totu-item">
          <span class="totu-number">{{ countValue(orderNum?.awaitComment) }}</span>
          <span class="totu-text">评论</span>
        </router-link>
        <router-link to="/member/aftersales" class="totu-item">
          <span class="totu-number">{{ countValue(orderNum?.awaitAftersalesCollect) }}</span>
          <span class="totu-text">售后</span>
        </router-link>
      </div>

      <!-- 快捷卡片 -->
      <div class="user-bottom">
        <router-link to="/member/coupons" class="bottom-card">
          <div class="bottom-card-title">
            <el-icon><Ticket /></el-icon>
            <span>优惠券</span>
            <span class="bottom-count">{{ countValue(center?.awaitCoupon) }}</span>
          </div>
          <div class="bottom-card-desc">查看我的优惠券</div>
        </router-link>
        <router-link to="/member/favorites" class="bottom-card">
          <div class="bottom-card-title">
            <el-icon><Star /></el-icon>
            <span>心愿单</span>
          </div>
          <div class="bottom-card-desc">查看我的收藏</div>
        </router-link>
        <router-link to="/member/history" class="bottom-card">
          <div class="bottom-card-title">
            <el-icon><Clock /></el-icon>
            <span>浏览记录</span>
          </div>
          <div class="bottom-card-desc">查看浏览记录</div>
        </router-link>
      </div>

      <!-- 全部功能 -->
      <div class="member-tools">
        <div class="tools-title">全部功能</div>
        <div class="tool-links">
          <router-link to="/member/orders" class="tool-card"><el-icon><List /></el-icon><span>我的订单</span></router-link>
          <router-link to="/member/comments" class="tool-card"><el-icon><ChatDotSquare /></el-icon><span>评价晒单</span></router-link>
          <router-link to="/member/aftersales" class="tool-card"><el-icon><RefreshLeft /></el-icon><span>售后服务</span></router-link>
          <router-link to="/member/coupons" class="tool-card"><el-icon><Ticket /></el-icon><span>我的优惠券</span></router-link>
          <router-link to="/member/favorites" class="tool-card"><el-icon><Star /></el-icon><span>我的收藏</span></router-link>
          <router-link to="/member/history" class="tool-card"><el-icon><Clock /></el-icon><span>浏览历史</span></router-link>
          <router-link to="/member/address" class="tool-card"><el-icon><Location /></el-icon><span>收货地址</span></router-link>
          <router-link to="/member/messages" class="tool-card"><el-icon><Bell /></el-icon><span>站内信</span></router-link>
          <router-link to="/member/sign" class="tool-card"><el-icon><Calendar /></el-icon><span>每日签到</span></router-link>
          <router-link to="/cart" class="tool-card"><el-icon><ShoppingCart /></el-icon><span>我的购物车</span></router-link>
          <a class="tool-card" href="javascript:;" @click="handleLogout"><el-icon><SwitchButton /></el-icon><span>退出登录</span></a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 会员中心首页（对齐原版 `pages/member/index.vue`）。
 *
 * 结构：用户信息（头像/昵称/等级/余额/积分）→ 订单状态角标（我的订单/待付款/
 * 待收货/评论/售后）→ 快捷卡片（优惠券/心愿单/浏览记录）→ 全部功能入口。
 * 数据源：`GET /user/user/memberCenter`、`GET /user/order/orderNum`。
 */
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  List,
  Ticket,
  Star,
  Clock,
  ChatDotSquare,
  RefreshLeft,
  Location,
  Bell,
  Calendar,
  ShoppingCart,
  SwitchButton,
} from '@element-plus/icons-vue';
import { orderApi } from '@/api';
import type { OrderNum } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const orderNum = ref<OrderNum | null>(null);
const center = computed(() => user.memberCenter);
const avatar = computed(() => imageUrl(user.avatar));

function countValue(value: number | undefined | null): string {
  return value && Number(value) > 0 ? String(value) : '-';
}

function handleLogout() {
  user.logout();
  cart.reset();
  ElMessage.success('已退出登录');
  router.push('/');
}

async function loadOrderNum() {
  try {
    orderNum.value = await orderApi.orderNum();
  } catch {
    orderNum.value = null;
  }
}

onMounted(async () => {
  applySeo({ title: '会员中心', noindex: true });
  await Promise.all([user.loadCenter(), loadOrderNum()]);
});
</script>

<style scoped lang="less">
.tig-member-page {
  background: #f5f6fa;
  padding: 24px 0 50px;
  min-height: 70vh;
}

.infout {
  background: #fff;
  border-radius: 8px;
  padding: 22px 26px;
  margin-bottom: 14px;

  .user-info {
    display: flex;
    align-items: center;
    gap: 18px;
    flex-wrap: wrap;

    .avatar { border: 1px solid #f0f0f0; }

    .user-col {
      flex: 1;
      min-width: 240px;

      .username {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
        .nickname { font-size: 20px; font-weight: 700; }
      }

      .user-title {
        display: flex;
        gap: 18px;
        flex-wrap: wrap;

        .title-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
      }
    }

    .user-stats {
      display: flex;
      gap: 32px;
      .stat-item {
        text-align: center;
        .stat-value { font-size: 22px; font-weight: 800; color: var(--tig-red); }
        .stat-label { color: #888; font-size: 12px; margin-top: 4px; }
      }
    }
  }
}

.user-totu {
  display: flex;
  align-items: stretch;
  gap: 0;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;

  .totu-entry {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 20px 24px;
    background: #fff7f6;
    color: var(--tig-red);
    font-weight: 700;
    min-width: 170px;
    .entry-icon { font-size: 20px; }
    .entry-arrow { margin-left: auto; }
  }

  .totu-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 16px 0;
    color: #666;
    border-left: 1px solid #f5f5f5;
    transition: background 0.2s;

    &:hover { background: #fffafa; color: var(--tig-red); }
    .totu-number { font-size: 22px; font-weight: 800; color: #333; }
    .totu-text { color: #888; font-size: 12px; margin-top: 4px; }
  }
}

.user-bottom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 14px;

  .bottom-card {
    background: #fff;
    border-radius: 8px;
    padding: 18px 20px;
    transition: box-shadow 0.2s;

    &:hover { box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06); }

    .bottom-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 700;
      color: #333;

      .bottom-count { margin-left: auto; color: var(--tig-red); }
    }

    .bottom-card-desc { color: #999; font-size: 12px; margin-top: 8px; }
  }
}

.member-tools {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;

  .tools-title { font-size: 15px; font-weight: 700; margin-bottom: 14px; }
}

.tool-links {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;

  .tool-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 0;
    color: #555;
    font-size: 13px;
    border: 1px solid #f2f2f2;
    border-radius: 8px;
    cursor: pointer;

    .el-icon { font-size: 24px; }
    &:hover { border-color: #ffdcdc; color: var(--tig-red); background: #fff7f6; }
  }
}

@media (max-width: 1280px) {
  .tool-links { grid-template-columns: repeat(4, 1fr); }
}
</style>
