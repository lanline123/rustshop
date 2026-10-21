<template>
  <header class="tig-pc-header">
    <!-- 顶部快捷条 -->
    <div class="top-shortcut-bar">
      <div class="container flex-between">
        <div class="shortcut-left flex-align-center">
          <span v-if="!user.isLoggedIn">您好，欢迎来到 TigShop 官方商城！</span>
          <el-dropdown v-else trigger="hover" @command="handleUserCommand">
            <span class="user-menu-trigger">
              欢迎您，<strong>{{ user.displayName || user.username }}</strong>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="address">收货地址</el-dropdown-item>
                <el-dropdown-item command="coupons">我的优惠券</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="aftersales">售后服务</el-dropdown-item>
                <el-dropdown-item command="comments">评价晒单</el-dropdown-item>
                <el-dropdown-item command="messages">站内信</el-dropdown-item>
                <el-dropdown-item command="sign">每日签到</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link v-if="!user.isLoggedIn" to="/login" class="login-link">请登录</router-link>
        </div>
        <div class="shortcut-right flex-align-center">
          <router-link to="/member/orders">我的订单</router-link>
          <span class="sep">|</span>
          <router-link to="/member">个人中心</router-link>
          <span class="sep">|</span>
          <router-link to="/member/address">收货地址</router-link>
          <span class="sep">|</span>
          <a href="javascript:;" @click="showMobileTip">手机商城</a>
        </div>
      </div>
    </div>

    <!-- 搜索 / Logo 主栏 -->
    <div class="main-search-bar">
      <div class="container flex-between">
        <router-link to="/" class="mall-logo flex-align-center">
          <div class="logo-icon">T</div>
          <div class="logo-text">
            <span class="brand">TigShop</span>
            <span class="sub">官方旗舰商城</span>
          </div>
        </router-link>

        <div class="search-input-wrapper">
          <div class="search-input-box flex-align-center">
            <input v-model="keyword" placeholder="搜索心仪的商品..." @keyup.enter="handleSearch" />
            <button class="search-btn" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </button>
          </div>
        </div>

        <router-link to="/cart" class="header-cart-box flex-align-center">
          <el-icon class="cart-icon"><ShoppingCart /></el-icon>
          <span>我的购物车</span>
          <span class="cart-badge">{{ cart.count }}</span>
        </router-link>
      </div>
    </div>

    <!-- 导航栏 -->
    <nav class="mall-nav-bar">
      <div class="container flex-align-center">
        <div class="all-categories-btn flex-align-center" @click="toggleCategoryPanel">
          <el-icon class="mr6"><Menu /></el-icon>
          <span>全部商品分类</span>
        </div>
        <div class="nav-links flex-align-center">
          <router-link to="/" :class="{ active: route.path === '/' }">首页</router-link>
          <router-link to="/search" :class="{ active: route.path === '/search' }">商品大厅</router-link>
          <router-link to="/activity/seckill" :class="{ active: route.path === '/activity/seckill' }">限时秒杀</router-link>
          <router-link to="/activity/groupon" :class="{ active: route.path === '/activity/groupon' }">拼团活动</router-link>
          <router-link to="/member" :class="{ active: route.path.startsWith('/member') }">会员中心</router-link>
        </div>
      </div>

      <!-- 分类面板（真实分类树） -->
      <div v-if="panelVisible" class="category-panel">
        <div class="container category-panel-inner">
          <div v-if="categorizing" class="panel-tip">分类加载中...</div>
          <div v-else-if="categories.length === 0" class="panel-tip">暂无分类数据</div>
          <div v-else class="category-panel-grid">
            <div
              v-for="node in categories"
              :key="node.categoryId"
              class="category-panel-item"
              @click="goCategory(node.categoryId)"
            >
              <div class="cat-title">{{ node.categoryName }}</div>
              <div class="cat-children">
                <span v-for="child in (node.children || []).slice(0, 6)" :key="child.categoryId">
                  {{ child.categoryName }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Search, ShoppingCart, Menu, ArrowDown } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { homeApi } from '@/api';
import type { CategoryNode } from '@/api/types';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const keyword = ref('');
const categories = ref<CategoryNode[]>([]);
const categorizing = ref(false);
const panelVisible = ref(false);

function handleSearch() {
  router.push({
    path: '/search',
    query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {},
  });
}

function goCategory(categoryId: number) {
  panelVisible.value = false;
  router.push({ path: '/search', query: { categoryId: String(categoryId) } });
}

function toggleCategoryPanel() {
  panelVisible.value = !panelVisible.value;
  if (panelVisible.value && categories.value.length === 0) {
    loadCategories();
  }
}

async function loadCategories() {
  categorizing.value = true;
  try {
    categories.value = await homeApi.categoryTree();
  } catch {
    categories.value = [];
  } finally {
    categorizing.value = false;
  }
}

function handleLogout() {
  user.logout();
  cart.reset();
  ElMessage.success('已退出登录');
  router.push('/');
}

/** 用户下拉菜单路由映射。 */
const USER_MENU_ROUTES: Record<string, string> = {
  profile: '/member',
  orders: '/member/orders',
  address: '/member/address',
  coupons: '/member/coupons',
  favorites: '/member/favorites',
  aftersales: '/member/aftersales',
  comments: '/member/comments',
  messages: '/member/messages',
  sign: '/member/sign',
};

function handleUserCommand(command: string) {
  if (command === 'logout') {
    handleLogout();
    return;
  }
  const path = USER_MENU_ROUTES[command];
  if (path) router.push(path);
}

function showMobileTip() {
  ElMessage.info('移动端请使用浏览器访问同一商城域名');
}

onMounted(() => {
  if (user.isLoggedIn) {
    cart.fetchCount(user.token);
  }
});
</script>

<style scoped lang="less">
.tig-pc-header {
  background: #ffffff;
  border-bottom: 1px solid var(--tig-border-color);
}

.top-shortcut-bar {
  background: #f5f5f5;
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  color: var(--tig-text-secondary);
  border-bottom: 1px solid #e8e8e8;

  .shortcut-left {
    gap: 12px;
    strong { color: var(--tig-red); }
    .login-link { color: var(--tig-red); font-weight: bold; }
    .logout-link { color: #999; &:hover { color: var(--tig-red); } }
    .user-menu-trigger {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      color: #666;
      outline: none;
      &:hover { color: var(--tig-red); }
    }
  }

  .shortcut-right {
    gap: 10px;
    a {
      color: #666;
      &:hover { color: var(--tig-red); }
    }
    .sep { color: #ddd; }
  }
}

.main-search-bar {
  padding: 24px 0 20px;

  .mall-logo {
    gap: 12px;
    cursor: pointer;

    .logo-icon {
      width: 44px;
      height: 44px;
      background: var(--tig-red);
      color: #ffffff;
      font-size: 26px;
      font-weight: 900;
      line-height: 44px;
      text-align: center;
      border-radius: 8px;
    }

    .logo-text {
      display: flex;
      flex-direction: column;
      .brand {
        font-size: 24px;
        font-weight: 800;
        color: var(--tig-red);
        line-height: 1;
        letter-spacing: -0.5px;
      }
      .sub {
        font-size: 12px;
        color: #888;
        margin-top: 4px;
      }
    }
  }

  .search-input-wrapper {
    width: 540px;
    max-width: 44vw;

    .search-input-box {
      border: 2px solid var(--tig-red);
      border-radius: 4px;
      overflow: hidden;
      height: 40px;

      input {
        flex: 1;
        border: none;
        outline: none;
        padding: 0 14px;
        font-size: 14px;
      }

      .search-btn {
        background: var(--tig-red);
        color: #ffffff;
        border: none;
        height: 100%;
        padding: 0 24px;
        font-size: 15px;
        font-weight: 600;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 6px;
        &:hover { background: #c81c13; }
      }
    }
  }

  .header-cart-box {
    background: #fff6f6;
    border: 1px solid #ffdcdc;
    color: var(--tig-red);
    height: 40px;
    padding: 0 18px;
    border-radius: 4px;
    gap: 8px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--tig-red);
      color: #ffffff;
      border-color: var(--tig-red);
      .cart-badge { background: #ffffff; color: var(--tig-red); }
    }

    .cart-icon { font-size: 18px; }

    .cart-badge {
      background: var(--tig-red);
      color: #ffffff;
      padding: 1px 7px;
      border-radius: 10px;
      font-size: 11px;
    }
  }
}

.mall-nav-bar {
  border-top: 1px solid #f0f0f0;
  height: 42px;
  position: relative;

  .all-categories-btn {
    width: 220px;
    height: 42px;
    background: var(--tig-red);
    color: #ffffff;
    padding: 0 20px;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    .mr6 { margin-right: 6px; font-size: 16px; }
  }

  .nav-links {
    margin-left: 20px;
    gap: 8px;

    a {
      padding: 0 18px;
      height: 42px;
      line-height: 42px;
      font-size: 15px;
      font-weight: 600;
      color: #333333;
      transition: color 0.2s;
      &:hover, &.active { color: var(--tig-red); }
    }
  }
}

.category-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: 42px;
  background: #ffffff;
  border: 1px solid var(--tig-border-color);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  z-index: 30;

  .category-panel-inner {
    max-height: 420px;
    overflow-y: auto;
    padding: 16px 0;
  }

  .category-panel-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .category-panel-item {
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    &:hover { background: #fff5f4; }

    .cat-title {
      font-weight: 700;
      font-size: 14px;
      color: var(--tig-text-main);
      margin-bottom: 6px;
    }

    .cat-children {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      font-size: 12px;
      color: #888;
      span:hover { color: var(--tig-red); }
    }
  }

  .panel-tip {
    color: #999;
    text-align: center;
    padding: 20px 0;
  }
}
</style>
