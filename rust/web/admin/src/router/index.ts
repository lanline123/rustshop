import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import AdminLayout from '@/layouts/AdminLayout.vue';
import LoginView from '@/views/login/LoginView.vue';
import Dashboard from '@/views/panel/Dashboard.vue';
import SchemaPageView from '@/views/schema/SchemaPageView.vue';
import DecorateEditor from '@/views/decorate/DecorateEditor.vue';
import ProductListView from '@/views/business/ProductListView.vue';
import OrderListView from '@/views/business/OrderListView.vue';
import MemberListView from '@/views/business/MemberListView.vue';
import CouponListView from '@/views/business/CouponListView.vue';
import AfterSalesListView from '@/views/business/AfterSalesListView.vue';
import RefundApplyListView from '@/views/business/RefundApplyListView.vue';
import ProxyShoppingView from '@/views/business/ProxyShoppingView.vue';
import CreditUserListView from '@/views/business/CreditUserListView.vue';
import CreditOrderListView from '@/views/business/CreditOrderListView.vue';
import CreditSettlementView from '@/views/business/CreditSettlementView.vue';

// Schemas
import { basicSettingSchema } from '@/schemas/setting/basic';
import { productSettingSchema } from '@/schemas/setting/product';
import { shoppingSettingSchema } from '@/schemas/setting/shopping';
import { paymentSettingSchema } from '@/schemas/setting/payment';
import { categoryTableSchema } from '@/schemas/product/category';
import { adminUserTableSchema } from '@/schemas/authority/adminUser';
import { articleTableSchema } from '@/schemas/content/article';
import { shopListTableSchema } from '@/schemas/merchant/shopList';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '管理员登录' }
  },
  {
    path: '/decorate/mobile',
    name: 'MobileDecorate',
    component: DecorateEditor,
    meta: { title: '移动端可视化装修' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '控制台概况' }
      },
      // 商品管理
      {
        path: 'product/list',
        name: 'ProductList',
        component: ProductListView,
        meta: { title: '商品列表' }
      },
      {
        path: 'product/category',
        name: 'ProductCategory',
        component: SchemaPageView,
        meta: {
          title: '商品分类',
          schemaType: 'table',
          schema: categoryTableSchema
        }
      },
      // 订单管理
      {
        path: 'order/list',
        name: 'OrderList',
        component: OrderListView,
        meta: { title: '订单列表' }
      },
      {
        path: 'order/aftersales',
        name: 'OrderAftersales',
        component: AfterSalesListView,
        meta: { title: '售后管理' }
      },
      {
        path: 'order/proxy-shopping',
        name: 'OrderProxyShopping',
        component: ProxyShoppingView,
        meta: { title: '代客下单' }
      },
      // 财务中心
      {
        path: 'finance/refund-apply',
        name: 'FinanceRefundApply',
        component: RefundApplyListView,
        meta: { title: '退款申请' }
      },
      {
        path: 'finance/credit-user',
        name: 'FinanceCreditUser',
        component: CreditUserListView,
        meta: { title: '账期会员' }
      },
      {
        path: 'finance/credit-order',
        name: 'FinanceCreditOrder',
        component: CreditOrderListView,
        meta: { title: '账期订单' }
      },
      {
        path: 'finance/credit-settlement',
        name: 'FinanceCreditSettlement',
        component: CreditSettlementView,
        meta: { title: '账期结算' }
      },
      // 会员管理
      {
        path: 'member/users',
        name: 'MemberUsers',
        component: MemberListView,
        meta: { title: '会员用户列表' }
      },
      // 营销中心
      {
        path: 'promotion/coupon',
        name: 'PromotionCoupon',
        component: CouponListView,
        meta: { title: '优惠券管理' }
      },
      // 店铺装修
      {
        path: 'decorate/pc',
        name: 'PcDecorate',
        component: DecorateEditor,
        meta: { title: 'PC 端装修' }
      },
      // 内容管理
      {
        path: 'content/article',
        name: 'ContentArticle',
        component: SchemaPageView,
        meta: {
          title: '文章资讯与帮助',
          schemaType: 'table',
          schema: articleTableSchema
        }
      },
      // 商户管理
      {
        path: 'merchant/shops',
        name: 'MerchantShops',
        component: SchemaPageView,
        meta: {
          title: '入驻店铺列表',
          schemaType: 'table',
          schema: shopListTableSchema
        }
      },
      // 系统设置
      {
        path: 'setting/basic',
        name: 'SettingBasic',
        component: SchemaPageView,
        meta: {
          title: '商城基础设置',
          schemaType: 'form',
          schema: basicSettingSchema
        }
      },
      {
        path: 'setting/product',
        name: 'SettingProduct',
        component: SchemaPageView,
        meta: {
          title: '商品全局设置',
          schemaType: 'form',
          schema: productSettingSchema
        }
      },
      {
        path: 'setting/shopping',
        name: 'SettingShopping',
        component: SchemaPageView,
        meta: {
          title: '交易履约设置',
          schemaType: 'form',
          schema: shoppingSettingSchema
        }
      },
      {
        path: 'setting/payment',
        name: 'SettingPayment',
        component: SchemaPageView,
        meta: {
          title: '支付通道配置',
          schemaType: 'form',
          schema: paymentSettingSchema
        }
      },
      // 权限管理
      {
        path: 'authority/admin-user',
        name: 'AuthorityAdminUser',
        component: SchemaPageView,
        meta: {
          title: '管理员账号',
          schemaType: 'table',
          schema: adminUserTableSchema
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('tig_token');
  if (to.meta.title) {
    document.title = `${to.meta.title} - TigShop 运营后台`;
  }
  if (!token && to.path !== '/login') {
    next('/login');
  } else {
    next();
  }
});

export default router;
