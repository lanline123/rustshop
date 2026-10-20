<template>
  <div class="tig-admin-layout">
    <!-- Left Sidebar -->
    <aside :class="['admin-sidebar', { collapsed: isCollapsed }]">
      <div class="sidebar-logo flex-align-center">
        <div class="logo-box">T</div>
        <span v-if="!isCollapsed" class="logo-title">TigShop Pro</span>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-el-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>控制台概况</template>
          </el-menu-item>

          <!-- 商品管理 -->
          <el-sub-menu index="/product">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/product/list">商品列表</el-menu-item>
            <el-menu-item index="/product/category">商品分类</el-menu-item>
          </el-sub-menu>

          <!-- 订单管理 -->
          <el-sub-menu index="/order">
            <template #title>
              <el-icon><Tickets /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/order/list">订单列表</el-menu-item>
            <el-menu-item index="/order/aftersales">售后管理</el-menu-item>
            <el-menu-item index="/order/proxy-shopping">代客下单</el-menu-item>
          </el-sub-menu>

          <!-- 财务中心（退款申请 / 账期管理，对应原版 finance + termPay） -->
          <el-sub-menu index="/finance">
            <template #title>
              <el-icon><Wallet /></el-icon>
              <span>财务中心</span>
            </template>
            <el-menu-item index="/finance/refund-apply">退款申请</el-menu-item>
            <el-menu-item index="/finance/credit-user">账期会员</el-menu-item>
            <el-menu-item index="/finance/credit-order">账期订单</el-menu-item>
            <el-menu-item index="/finance/credit-settlement">账期结算</el-menu-item>
          </el-sub-menu>

          <!-- 会员管理 -->
          <el-sub-menu index="/member">
            <template #title>
              <el-icon><User /></el-icon>
              <span>会员管理</span>
            </template>
            <el-menu-item index="/member/users">会员用户列表</el-menu-item>
          </el-sub-menu>

          <!-- 营销中心 -->
          <el-sub-menu index="/promotion">
            <template #title>
              <el-icon><Present /></el-icon>
              <span>营销中心</span>
            </template>
            <el-menu-item index="/promotion/coupon">优惠券管理</el-menu-item>
          </el-sub-menu>

          <!-- 店铺装修 -->
          <el-sub-menu index="/decorate">
            <template #title>
              <el-icon><Brush /></el-icon>
              <span>店铺装修</span>
            </template>
            <el-menu-item index="/decorate/mobile">移动端首页装修</el-menu-item>
            <el-menu-item index="/decorate/pc">PC 端商城装修</el-menu-item>
          </el-sub-menu>

          <!-- 内容管理 -->
          <el-sub-menu index="/content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/content/article">文章资讯与帮助</el-menu-item>
          </el-sub-menu>

          <!-- 多商户管理 -->
          <el-sub-menu index="/merchant">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>商户管理</span>
            </template>
            <el-menu-item index="/merchant/shops">入驻店铺列表</el-menu-item>
          </el-sub-menu>

          <!-- 系统设置 -->
          <el-sub-menu index="/setting">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/setting/basic">商城基础设置</el-menu-item>
            <el-menu-item index="/setting/product">商品全局设置</el-menu-item>
            <el-menu-item index="/setting/shopping">交易履约设置</el-menu-item>
            <el-menu-item index="/setting/payment">支付通道配置</el-menu-item>
          </el-sub-menu>

          <!-- 权限管理 -->
          <el-sub-menu index="/authority">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>权限管理</span>
            </template>
            <el-menu-item index="/authority/admin-user">管理员账号</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- Right Container -->
    <div class="admin-main-wrapper">
      <!-- Top Header -->
      <header class="admin-topbar flex-between">
        <div class="topbar-left flex-align-center">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>

          <el-breadcrumb separator="/" class="breadcrumb-nav">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right flex-align-center">
          <!-- Multi-Tenant / Shop Switcher -->
          <div class="shop-switcher-box flex-align-center">
            <el-icon class="mr4"><Shop /></el-icon>
            <el-select
              v-model="currentShopId"
              size="small"
              class="shop-select"
              @change="handleShopChange"
            >
              <el-option :value="0" label="平台运营总控台 (全域)" />
              <el-option :value="1" label="自营官方旗舰店 (Shop#1)" />
              <el-option :value="2" label="生鲜果蔬专营店 (Shop#2)" />
            </el-select>
          </div>

          <el-tooltip content="进入移动端可视化装修" placement="bottom">
            <el-button link :icon="Brush" @click="$router.push('/decorate/mobile')">
              可视化装修
            </el-button>
          </el-tooltip>

          <el-badge :value="5" class="notification-badge">
            <el-icon class="header-icon-btn"><Bell /></el-icon>
          </el-badge>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-profile-trigger flex-align-center">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="user-nickname">{{ currentUsername }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="basicSetting">基础设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Multi-Tabs -->
      <div class="admin-tab-bar">
        <div
          v-for="tab in openedTabs"
          :key="tab.path"
          :class="['tab-item', { active: tab.path === $route.path }]"
          @click="$router.push(tab.path)"
        >
          <span>{{ tab.title }}</span>
          <el-icon
            v-if="tab.path !== '/dashboard'"
            class="tab-close"
            @click.stop="closeTab(tab.path)"
          >
            <Close />
          </el-icon>
        </div>
      </div>

      <!-- Page Content Area -->
      <main class="admin-page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Odometer,
  Goods,
  Tickets,
  User,
  Present,
  Brush,
  Document,
  Shop,
  Setting,
  UserFilled,
  Fold,
  Expand,
  Bell,
  ArrowDown,
  Close
} from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();

const isCollapsed = ref(false);
const activeMenu = computed(() => route.path);
const currentUsername = ref(localStorage.getItem('tig_username') || '超级管理员');

const currentShopId = ref<number>(
  Number(localStorage.getItem('tig_shop_id') || 0)
);

function handleShopChange(val: number) {
  localStorage.setItem('tig_shop_id', String(val));
  ElMessage.success(val === 0 ? '已切换至平台运营总控台' : `已切换至店铺 #${val}`);
  setTimeout(() => {
    window.location.reload();
  }, 200);
}

const currentRouteTitle = computed(() => (route.meta?.title as string) || '');

interface TabItem {
  title: string;
  path: string;
}

const openedTabs = ref<TabItem[]>([
  { title: '控制台概况', path: '/dashboard' }
]);

watch(
  () => route.path,
  (newPath) => {
    const title = (route.meta?.title as string) || '新标签页';
    if (!openedTabs.value.find((t) => t.path === newPath)) {
      openedTabs.value.push({ title, path: newPath });
    }
  },
  { immediate: true }
);

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value;
}

function closeTab(path: string) {
  const index = openedTabs.value.findIndex((t) => t.path === path);
  if (index !== -1) {
    openedTabs.value.splice(index, 1);
    if (route.path === path) {
      const next = openedTabs.value[index - 1] || openedTabs.value[0];
      router.push(next.path);
    }
  }
}

function handleUserCommand(cmd: string) {
  if (cmd === 'basicSetting') {
    router.push('/setting/basic');
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出当前管理员登录吗？', '提示', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('tig_token');
      ElMessage.success('已安全退出登录');
      router.push('/login');
    });
  }
}
</script>

<style scoped lang="less">
.tig-admin-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.admin-sidebar {
  width: var(--tig-sidebar-width);
  height: 100%;
  background: #ffffff;
  border-right: 1px solid var(--tig-border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.25s cubic-bezier(0.2, 0, 0, 1);
  flex-shrink: 0;

  &.collapsed {
    width: var(--tig-sidebar-collapsed-width);
  }

  .sidebar-logo {
    height: var(--tig-header-height);
    padding: 0 16px;
    gap: 10px;
    border-bottom: 1px solid var(--tig-border-color);

    .logo-box {
      width: 32px;
      height: 32px;
      background: var(--tig-primary);
      color: #fff;
      font-size: 18px;
      font-weight: 800;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
    }
    .logo-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--tig-text-main);
      white-space: nowrap;
    }
  }

  .menu-scrollbar {
    flex: 1;
  }

  .sidebar-el-menu {
    border-right: none;
    :deep(.el-menu-item) {
      &.is-active {
        color: var(--tig-primary);
        background: var(--tig-primary-light-9);
        font-weight: 600;
        border-right: 3px solid var(--tig-primary);
      }
    }
  }
}

/* Right Wrapper */
.admin-main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Topbar */
.admin-topbar {
  height: var(--tig-header-height);
  background: #ffffff;
  border-bottom: 1px solid var(--tig-border-color);
  padding: 0 20px;
  flex-shrink: 0;

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 16px;
    color: var(--tig-text-regular);
    &:hover { color: var(--tig-primary); }
  }

  .topbar-right {
    gap: 18px;

    .mr4 {
      margin-right: 4px;
      color: var(--tig-text-secondary);
    }
    .shop-select {
      width: 210px;
    }

    .header-icon-btn {
      font-size: 18px;
      cursor: pointer;
      color: var(--tig-text-regular);
    }

    .user-profile-trigger {
      cursor: pointer;
      gap: 8px;
      .user-nickname {
        font-size: 14px;
        font-weight: 500;
        color: var(--tig-text-main);
      }
    }
  }
}

/* Tab Bar */
.admin-tab-bar {
  height: 38px;
  background: #ffffff;
  border-bottom: 1px solid var(--tig-border-color);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 6px;
  overflow-x: auto;
  flex-shrink: 0;

  .tab-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    font-size: 12px;
    color: var(--tig-text-regular);
    background: #f4f5f8;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #eef1f6;
    }

    &.active {
      color: #ffffff;
      background: var(--tig-primary);
      font-weight: 500;

      .tab-close {
        color: #ffffff;
        &:hover { background: rgba(255, 255, 255, 0.2); }
      }
    }

    .tab-close {
      border-radius: 50%;
      padding: 1px;
      font-size: 10px;
      &:hover { background: #dcdfe6; }
    }
  }
}

/* Main Page Container */
.admin-page-container {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  background-color: var(--tig-bg-color);
}

/* Page Transition */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
