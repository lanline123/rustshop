<template>
  <div class="tig-profile-page container">
    <div v-loading="user.loading" class="profile-body">
      <!-- 会员信息卡 -->
      <section class="member-card">
        <div class="avatar-wrap">
          <el-avatar :size="72" :src="imageUrl(user.avatar)">
            {{ (user.displayName || 'U').slice(0, 1) }}
          </el-avatar>
        </div>
        <div class="member-info">
          <div class="member-name">
            {{ user.displayName || '未设置昵称' }}
            <el-tag v-if="center?.rankName" size="small" type="warning">{{ center.rankName }}</el-tag>
          </div>
          <div class="member-account">账号：{{ center?.username || user.username }}</div>
          <div class="member-level">会员等级：{{ center?.rankName || '-' }}</div>
        </div>
        <div class="member-stats">
          <div class="stat-item">
            <div class="stat-value">{{ formatPrice(center?.balance) }}</div>
            <div class="stat-label">账户余额</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ center?.points ?? 0 }}</div>
            <div class="stat-label">可用积分</div>
          </div>
        </div>
      </section>

      <!-- 订单角标 -->
      <section class="order-panel">
        <div class="panel-head flex-between">
          <h2>我的订单</h2>
          <router-link to="/member/orders" class="more-link">查看全部订单 ›</router-link>
        </div>
        <div class="order-shortcuts">
          <router-link
            v-for="entry in orderEntries"
            :key="entry.status"
            :to="{ path: '/member/orders' }"
            class="shortcut-item"
          >
            <el-badge :value="entry.count" :hidden="!entry.count" :max="99">
              <el-icon class="shortcut-icon"><component :is="entry.icon" /></el-icon>
            </el-badge>
            <span>{{ entry.label }}</span>
          </router-link>
        </div>
      </section>

      <!-- 常用入口 -->
      <section class="tools-panel">
        <div class="panel-head"><h2>账户工具</h2></div>
        <div class="tool-links">
          <router-link to="/member/orders" class="tool-card">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </router-link>
          <router-link to="/member/address" class="tool-card">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </router-link>
          <router-link to="/cart" class="tool-card">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
          </router-link>
          <a class="tool-card" href="javascript:;" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </a>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { List, Location, ShoppingCart, SwitchButton, Wallet, Van, Box, ChatDotSquare } from '@element-plus/icons-vue';
import { applySeo } from '@/composables/useSeo';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const center = computed(() => user.memberCenter);

const orderEntries = computed(() => [
  { label: '待付款', status: 0, count: center.value?.awaitPay ?? 0, icon: Wallet },
  { label: '待发货', status: 1, count: center.value?.awaitShipping ?? 0, icon: Box },
  { label: '待收货', status: 2, count: center.value?.awaitReceived ?? 0, icon: Van },
  { label: '待评价', status: 3, count: center.value?.awaitComment ?? 0, icon: ChatDotSquare },
]);

function handleLogout() {
  user.logout();
  cart.reset();
  ElMessage.success('已退出登录');
  router.push('/');
}

onMounted(async () => {
  applySeo({ title: '个人中心', noindex: true });
  await user.loadCenter();
});
</script>

<style scoped lang="less">
.tig-profile-page {
  padding: 26px 0 60px;
}

.profile-body { min-height: 260px; }

.member-card {
  background: linear-gradient(120deg, #fff7f6, #ffffff 60%);
  border: 1px solid #ffdcdc;
  border-radius: 10px;
  padding: 22px 26px;
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;
  flex-wrap: wrap;

  .member-info {
    flex: 1;
    min-width: 240px;

    .member-name {
      font-size: 20px;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 6px;
    }
    .member-account,
    .member-level { color: #888; font-size: 13px; margin-top: 3px; }
  }

  .member-stats {
    display: flex;
    gap: 28px;
    .stat-item {
      text-align: center;
      .stat-value { font-size: 22px; font-weight: 800; color: var(--tig-red); }
      .stat-label { color: #888; font-size: 12px; margin-top: 4px; }
    }
  }
}

.order-panel,
.tools-panel {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 18px;

  .panel-head {
    margin-bottom: 14px;
    h2 { font-size: 16px; font-weight: 700; }
    .more-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
  }
}

.order-shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 14px 0;
    border-radius: 8px;
    color: #555;
    font-size: 13px;
    transition: background 0.2s;
    &:hover { background: #fff7f6; color: var(--tig-red); }

    .shortcut-icon { font-size: 26px; }
  }
}

.tool-links {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
</style>
