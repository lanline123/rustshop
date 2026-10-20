<template>
  <header class="tig-pc-header">
    <!-- 1. Top Shortcut Bar -->
    <div class="top-shortcut-bar">
      <div class="container flex-between">
        <div class="shortcut-left flex-align-center">
          <span v-if="!username">您好，欢迎来到 TigShop 官方商城！</span>
          <span v-else>欢迎您，<strong>{{ username }}</strong></span>
          <template v-if="!username">
            <router-link to="/login" class="login-link">请登录</router-link>
            <router-link to="/register" class="register-link">免费注册</router-link>
          </template>
          <template v-else>
            <a href="javascript:;" class="logout-link" @click="handleLogout">退出</a>
          </template>
        </div>

        <div class="shortcut-right flex-align-center">
          <router-link to="/member">我的订单</router-link>
          <span class="sep">|</span>
          <router-link to="/member">个人中心</router-link>
          <span class="sep">|</span>
          <a href="javascript:;" @click="showMobileTip">手机商城</a>
          <span class="sep">|</span>
          <a href="javascript:;">客户服务</a>
        </div>
      </div>
    </div>

    <!-- 2. Main Search & Logo Bar -->
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
            <input
              v-model="keyword"
              placeholder="搜索心仪的商品..."
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </button>
          </div>
        </div>

        <router-link to="/cart" class="header-cart-box flex-align-center">
          <el-icon class="cart-icon"><ShoppingCart /></el-icon>
          <span>我的购物车</span>
          <span class="cart-badge">{{ cartCount }}</span>
        </router-link>
      </div>
    </div>

    <!-- 3. Navigation Bar -->
    <nav class="mall-nav-bar">
      <div class="container flex-align-center">
        <!-- All Categories Dropdown Button -->
        <div class="all-categories-btn flex-align-center" @click="$router.push('/search')">
          <el-icon class="mr6"><Menu /></el-icon>
          <span>全部商品分类</span>
        </div>

        <!-- Main Nav Links -->
        <div class="nav-links flex-align-center">
          <router-link to="/" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/search" :class="{ active: $route.path === '/search' }">商品大厅</router-link>
          <router-link to="/member">会员中心</router-link>
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, ShoppingCart, Menu } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const router = useRouter();
const keyword = ref('');
const username = ref(localStorage.getItem('tig_user_name') || '');
const cartCount = ref(0);

async function fetchCartCount() {
  const token = localStorage.getItem('tig_user_token');
  if (!token) {
    cartCount.value = 0;
    return;
  }
  try {
    const res: any = await request({
      url: '/cart/cart/getCount',
      method: 'get'
    });
    if (typeof res === 'number') {
      cartCount.value = res;
    } else if (res && typeof res.count === 'number') {
      cartCount.value = res.count;
    }
  } catch (err) {
    cartCount.value = 0;
  }
}

function handleSearch() {
  if (!keyword.value.trim()) {
    router.push('/search');
    return;
  }
  router.push({
    path: '/search',
    query: { keyword: keyword.value.trim() }
  });
}

function handleLogout() {
  localStorage.removeItem('tig_user_token');
  localStorage.removeItem('tig_user_name');
  username.value = '';
  cartCount.value = 0;
  ElMessage.success('已安全退出登录');
  router.push('/login');
}

function showMobileTip() {
  ElMessage.info('手机 H5 端与小程序可通过移动端浏览器直接访问');
}

onMounted(() => {
  fetchCartCount();
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
    .register-link { color: #666; &:hover { color: var(--tig-red); } }
    .logout-link { color: #999; &:hover { color: var(--tig-red); } }
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
        transition: background 0.2s;

        &:hover {
          background: #c81c13;
        }
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
      .cart-badge {
        background: #ffffff;
        color: var(--tig-red);
      }
    }

    .cart-icon {
      font-size: 18px;
    }

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
      position: relative;
      transition: color 0.2s;

      &:hover, &.active {
        color: var(--tig-red);
      }
    }
  }
}
</style>
