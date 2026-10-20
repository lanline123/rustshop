import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import MallLayout from '@/layouts/MallLayout.vue';
import HomeView from '@/views/HomeView.vue';
import SearchView from '@/views/SearchView.vue';
import ProductDetailView from '@/views/ProductDetailView.vue';
import CartView from '@/views/CartView.vue';
import LoginView from '@/views/LoginView.vue';
import MemberView from '@/views/MemberView.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '会员登录' }
  },
  {
    path: '/',
    component: MallLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: HomeView,
        meta: { title: '官方旗舰商城 - 首页' }
      },
      {
        path: 'search',
        name: 'Search',
        component: SearchView,
        meta: { title: '商品搜索列表' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: ProductDetailView,
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: CartView,
        meta: { title: '我的购物车' }
      },
      {
        path: 'member',
        name: 'Member',
        component: MemberView,
        meta: { title: '个人中心' }
      },
      {
        path: 'member/orders',
        name: 'MemberOrders',
        component: MemberView,
        meta: { title: '我的订单' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - TigShop 官方商城`;
  }
  next();
});

export default router;
