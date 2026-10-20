<template>
  <div class="tig-orders-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">我的订单</h1>
      <router-link to="/member" class="back-link">返回个人中心</router-link>
    </div>

    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="onTabChange">
      <el-tab-pane v-for="tab in tabs" :key="tab.name" :label="tab.label" :name="tab.name" />
    </el-tabs>

    <div v-loading="loading" class="orders-body">
      <el-empty v-if="!orders.length && !loading" description="暂无订单">
        <el-button type="primary" @click="$router.push('/search')">去购物</el-button>
      </el-empty>

      <div v-for="order in orders" :key="order.orderId" class="order-card">
        <div class="order-head flex-between">
          <div class="order-head-left flex-align-center">
            <span class="order-sn">订单号：{{ order.orderSn }}</span>
            <span class="order-time">{{ formatTime(order.addTime) }}</span>
          </div>
          <el-tag :type="orderStatusTagType(order.orderStatus)">{{ orderStatusText(order.orderStatus, order.statusLabel) }}</el-tag>
        </div>

        <div class="order-body flex-between">
          <div class="order-info">
            <div class="order-amount">
              实付金额：<span class="amount">¥{{ formatPrice(order.paidAmount || order.totalAmount) }}</span>
            </div>
            <div class="order-consignee">
              收货人：{{ order.consignee || '-' }} {{ order.mobile || '' }}
            </div>
            <div class="order-count">共 {{ order.itemCount }} 件商品</div>
          </div>

          <div class="order-actions">
            <el-button v-if="order.orderStatus === 0" type="primary" size="small" @click="goPay(order)">
              去支付
            </el-button>
            <el-button v-if="order.orderStatus === 0" size="small" @click="cancelOrder(order)">取消订单</el-button>
            <el-button v-if="order.orderStatus === 2" type="primary" size="small" @click="confirmReceipt(order)">
              确认收货
            </el-button>
            <el-button size="small" @click="goDetail(order)">查看详情</el-button>
            <el-button v-if="[3, 4, 5, 6].includes(order.orderStatus)" size="small" @click="buyAgain(order)">
              再次购买
            </el-button>
            <el-button
              v-if="[1, 2, 3, 4].includes(order.orderStatus)"
              size="small"
              @click="goApplyAftersales(order)"
            >
              申请售后
            </el-button>
            <el-button size="small" @click="$router.push('/member/aftersales')">售后记录</el-button>
            <el-button v-if="[4, 5, 6].includes(order.orderStatus)" size="small" type="danger" plain @click="removeOrder(order)">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, jumper, total"
          background
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi } from '@/api';
import { pageItems } from '@/api/types';
import type { OrderVO } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, formatTime, orderStatusTagType, orderStatusText } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const tabs = [
  { name: 'all', label: '全部订单', status: undefined as number | undefined },
  { name: '0', label: '待付款', status: 0 },
  { name: '1', label: '待发货', status: 1 },
  { name: '2', label: '待收货', status: 2 },
  { name: '3', label: '待评价', status: 3 },
];

const activeStatus = ref('all');
const orders = ref<OrderVO[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);

function currentStatus(): number | undefined {
  return tabs.find((t) => t.name === activeStatus.value)?.status;
}

async function loadOrders() {
  loading.value = true;
  try {
    const result = await orderApi.list({
      orderStatus: currentStatus(),
      page: page.value,
      pageSize: pageSize.value,
    });
    orders.value = pageItems(result);
    total.value = Number(result?.total ?? 0) || 0;
  } catch {
    orders.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onTabChange() {
  page.value = 1;
  const status = activeStatus.value === 'all' ? undefined : activeStatus.value;
  router.replace({ path: '/member/orders', query: status ? { status } : {} });
  loadOrders();
}

function goApplyAftersales(order: OrderVO) {
  router.push({ path: '/member/aftersales/apply', query: { orderId: String(order.orderId) } });
}

function onPageChange(next: number) {
  page.value = next;
  loadOrders();
}

function goPay(order: OrderVO) {
  router.push({ path: '/pay/result', query: { orderId: String(order.orderId) } });
}

function goDetail(order: OrderVO) {
  router.push(`/member/orders/${order.orderId}`);
}

async function cancelOrder(order: OrderVO) {
  await ElMessageBox.confirm('确定取消该订单？取消后库存将回滚。', '取消订单', {
    type: 'warning',
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想',
  });
  await orderApi.cancel(order.orderId);
  ElMessage.success('订单已取消');
  await loadOrders();
}

async function confirmReceipt(order: OrderVO) {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
    type: 'warning',
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
  });
  await orderApi.confirmReceipt(order.orderId);
  ElMessage.success('已确认收货');
  await loadOrders();
}

async function buyAgain(order: OrderVO) {
  await orderApi.buyAgain(order.orderId);
  ElMessage.success('商品已加入购物车');
  router.push('/cart');
}

async function removeOrder(order: OrderVO) {
  await ElMessageBox.confirm('删除后订单记录将不可恢复，确定删除？', '删除订单', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  });
  await orderApi.remove(order.orderId);
  ElMessage.success('订单已删除');
  await loadOrders();
}

onMounted(() => {
  const status = route.query.status;
  if (typeof status === 'string' && tabs.some((t) => t.name === status)) {
    activeStatus.value = status;
  }
  applySeo({ title: '我的订单', noindex: true });
  loadOrders();
});
</script>

<style scoped lang="less">
.tig-orders-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 10px;

  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.status-tabs { margin-bottom: 14px; }

.orders-body { min-height: 240px; }

.order-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;

  .order-head {
    padding: 12px 18px;
    background: #fafafa;
    border-bottom: 1px solid #f0f0f0;
    font-size: 13px;
    color: #666;

    .order-head-left { gap: 18px; }
    .order-time { color: #999; }
  }

  .order-body {
    padding: 16px 18px;
    gap: 16px;
    align-items: flex-start;
  }

  .order-info {
    .order-amount {
      font-size: 14px;
      color: #555;
      margin-bottom: 6px;
      .amount { color: var(--tig-red); font-size: 18px; font-weight: 800; }
    }
    .order-consignee,
    .order-count { color: #888; font-size: 12px; margin-top: 4px; }
  }

  .order-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
