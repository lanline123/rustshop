<template>
  <div class="tig-order-detail-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">订单详情</h1>
      <router-link to="/member/orders" class="back-link">返回我的订单</router-link>
    </div>

    <el-skeleton v-if="loading" :rows="10" animated />

    <el-empty v-else-if="!detail" description="订单不存在或无权查看">
      <el-button type="primary" @click="$router.push('/member/orders')">返回我的订单</el-button>
    </el-empty>

    <template v-else>
      <!-- 状态区 -->
      <section class="status-panel">
        <div class="status-left">
          <div class="status-title">{{ orderStatusText(order.orderStatus, order.statusLabel) }}</div>
          <div class="status-sn">订单号：{{ order.orderSn }}</div>
          <div class="status-time">下单时间：{{ formatTime(order.addTime) }}</div>
        </div>
        <div class="status-actions">
          <el-button v-if="order.orderStatus === 0" type="primary" @click="goPay">去支付</el-button>
          <el-button v-if="order.orderStatus === 0" @click="cancelOrder">取消订单</el-button>
          <el-button v-if="order.orderStatus === 2" type="primary" @click="confirmReceipt">确认收货</el-button>
          <el-button v-if="[3, 4, 5, 6].includes(order.orderStatus)" @click="buyAgain">再次购买</el-button>
          <el-button v-if="[1, 2, 3, 4].includes(order.orderStatus)" @click="goApplyAftersales">申请售后</el-button>
          <el-button @click="$router.push('/member/aftersales')">售后记录</el-button>
        </div>
      </section>

      <!-- 收货信息 -->
      <section class="panel">
        <h2 class="panel-title">收货信息</h2>
        <div class="info-grid">
          <div class="info-item"><span class="label">收货人</span>{{ order.consignee || '-' }}</div>
          <div class="info-item"><span class="label">联系电话</span>{{ order.mobile || '-' }}</div>
          <div class="info-item full"><span class="label">收货地址</span>{{ orderAddress }}</div>
          <div class="info-item"><span class="label">买家备注</span>{{ order.buyerNote || '无' }}</div>
        </div>
      </section>

      <!-- 商品清单 -->
      <section class="panel">
        <h2 class="panel-title">商品清单</h2>
        <el-table :data="items" border>
          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <div class="table-product">
                <img :src="imageUrl(row.picThumb)" :alt="row.productName" class="thumb" />
                <div>
                  <div class="name">{{ row.productName }}</div>
                  <div class="sku">{{ row.skuData || '' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="130" align="center">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center" prop="quantity" />
          <el-table-column label="小计" width="140" align="center">
            <template #default="{ row }">¥{{ formatPrice(Number(row.price) * Number(row.quantity)) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <!-- 金额 -->
      <section class="panel summary-panel">
        <div class="summary-row"><span>商品总额</span><span>¥{{ formatPrice(order.totalAmount) }}</span></div>
        <div class="summary-row"><span>实付金额</span><span class="amount">¥{{ formatPrice(order.paidAmount ?? order.totalAmount) }}</span></div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi } from '@/api';
import type { OrderDetail, OrderItem } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, formatTime, imageUrl, orderStatusText } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const detail = ref<OrderDetail | null>(null);

const order = computed<Record<string, any>>(() => (detail.value?.order ?? {}) as Record<string, any>);
const items = computed<OrderItem[]>(() => detail.value?.items ?? []);
const orderAddress = computed(() => {
  const region = order.value.regionName || order.value.addressRegion || '';
  const address = order.value.address || '';
  return `${region} ${address}`.trim() || '-';
});

async function load() {
  const id = Number(route.params.id);
  if (!Number.isFinite(id) || id <= 0) {
    detail.value = null;
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    detail.value = await orderApi.detail(id);
    applySeo({ title: `订单详情 ${detail.value.order.orderSn}`, noindex: true });
  } catch {
    detail.value = null;
  } finally {
    loading.value = false;
  }
}

function goPay() {
  router.push({ path: '/pay/result', query: { orderId: String(order.value.orderId) } });
}

function goApplyAftersales() {
  router.push({ path: '/member/aftersales/apply', query: { orderId: String(order.value.orderId) } });
}

async function cancelOrder() {
  await ElMessageBox.confirm('确定取消该订单？', '取消订单', {
    type: 'warning',
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想',
  });
  await orderApi.cancel(Number(order.value.orderId));
  ElMessage.success('订单已取消');
  await load();
}

async function confirmReceipt() {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
    type: 'warning',
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
  });
  await orderApi.confirmReceipt(Number(order.value.orderId));
  ElMessage.success('已确认收货');
  await load();
}

async function buyAgain() {
  await orderApi.buyAgain(Number(order.value.orderId));
  ElMessage.success('商品已加入购物车');
  router.push('/cart');
}

watch(() => route.params.id, load);
onMounted(load);
</script>

<style scoped lang="less">
.tig-order-detail-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 14px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.status-panel {
  background: linear-gradient(90deg, #fff5f4, #ffffff);
  border: 1px solid #ffdcdc;
  border-radius: 8px;
  padding: 18px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .status-title { font-size: 20px; font-weight: 700; color: var(--tig-red); margin-bottom: 6px; }
  .status-sn,
  .status-time { color: #888; font-size: 13px; margin-top: 2px; }
  .status-actions { display: flex; gap: 10px; }
}

.panel {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 16px;

  .panel-title { font-size: 16px; font-weight: 700; margin-bottom: 12px; }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 20px;
  font-size: 13px;
  color: #555;

  .info-item {
    .label { color: #999; margin-right: 8px; }
    &.full { grid-column: 1 / -1; }
  }
}

.table-product {
  display: flex;
  align-items: center;
  gap: 10px;
  .thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
  .name { font-size: 13px; }
  .sku { color: #999; font-size: 12px; margin-top: 4px; }
}

.summary-panel { text-align: right; }

.summary-row {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 13px;
  color: #666;
  padding: 5px 0;
  span:last-child { width: 140px; text-align: right; }
  .amount { color: var(--tig-red); font-size: 20px; font-weight: 800; }
}
</style>
