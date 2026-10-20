import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/stores/user';
import MallLayout from '@/layouts/MallLayout.vue';

/**
 * PC 商城路由。
 *
 * - 布局内页面全部懒加载，首屏只加载首页 chunk；
 * - `meta.requiresAuth` 的页面在未登录时跳登录页并携带 `redirect`；
 * - 404 统一重定向首页（PC 端 SPA 无独立 404 页，属已知未完成项）。
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '会员登录' },
  },
  {
    path: '/',
    component: MallLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/SearchView.vue'),
        meta: { title: '商品搜索' },
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetailView.vue'),
        meta: { title: '商品详情' },
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/CartView.vue'),
        meta: { title: '我的购物车', requiresAuth: true },
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/CheckoutView.vue'),
        meta: { title: '确认订单', requiresAuth: true },
      },
      {
        path: 'pay/result',
        name: 'PayResult',
        component: () => import('@/views/PayResultView.vue'),
        meta: { title: '支付结果', requiresAuth: true },
      },
      {
        path: 'member',
        name: 'Member',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      {
        path: 'member/orders',
        name: 'MemberOrders',
        component: () => import('@/views/OrdersView.vue'),
        meta: { title: '我的订单', requiresAuth: true },
      },
      {
        path: 'member/orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetailView.vue'),
        meta: { title: '订单详情', requiresAuth: true },
      },
      {
        path: 'member/address',
        name: 'MemberAddress',
        component: () => import('@/views/AddressView.vue'),
        meta: { title: '收货地址', requiresAuth: true },
      },
      {
        path: 'member/coupons',
        name: 'MemberCoupons',
        component: () => import('@/views/CouponsView.vue'),
        meta: { title: '我的优惠券', requiresAuth: true },
      },
      {
        path: 'member/favorites',
        name: 'MemberFavorites',
        component: () => import('@/views/FavoritesView.vue'),
        meta: { title: '我的收藏', requiresAuth: true },
      },
      {
        path: 'member/history',
        name: 'MemberHistory',
        component: () => import('@/views/HistoryView.vue'),
        meta: { title: '浏览历史', requiresAuth: true },
      },
      {
        path: 'member/aftersales',
        name: 'MemberAftersales',
        component: () => import('@/views/AftersalesView.vue'),
        meta: { title: '售后服务', requiresAuth: true },
      },
      {
        path: 'member/aftersales/apply',
        name: 'ApplyAftersales',
        component: () => import('@/views/ApplyAftersalesView.vue'),
        meta: { title: '申请售后', requiresAuth: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to) => {
  const title = to.meta.title as string | undefined;
  document.title = title ? `${title} - TigShop 官方商城` : 'TigShop 官方商城';

  if (!to.meta.requiresAuth) {
    return true;
  }

  const user = useUserStore();
  if (user.isLoggedIn) {
    return true;
  }

  return {
    path: '/login',
    query: { redirect: to.fullPath },
  };
});

export default router;
