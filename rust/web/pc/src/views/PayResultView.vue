<template>
  <div class="tig-pay-result-page">
    <div v-loading="loading" class="container">
      <template v-if="order">
        <!-- 订单已提交提示 -->
        <div class="success-txt">您的订单已成功提交，请付款！</div>

        <!-- 金额 / 订单号 -->
        <div class="price-box">
          <div class="num-box">
            <div class="tit">需支付：</div>
            <div class="price">¥{{ formatPrice(order.unpaidAmount ?? order.totalAmount) }}</div>
          </div>
          <div class="order-num">
            <div class="tit">订单号：</div>
            <router-link :to="`/member/orders/${order.orderId}`" class="num">{{ order.orderSn }}</router-link>
          </div>
        </div>

        <!-- 已支付 -->
        <div v-if="paid" class="paid-box">
          <el-icon class="paid-icon"><CircleCheckFilled /></el-icon>
          <div class="paid-text">
            <h2>订单支付成功</h2>
            <p>支付已完成，订单将尽快为您安排发货。</p>
          </div>
          <div class="paid-actions">
            <el-button type="primary" @click="$router.push(`/member/orders/${order.orderId}`)">查看订单</el-button>
            <el-button @click="$router.push('/search')">继续购物</el-button>
          </div>
        </div>

        <template v-else>
          <!-- 线上支付方式 -->
          <div v-if="Number(order.payTypeId) < 3" class="pay-type-box">
            <div v-if="paymentMethods.length" class="payment-section">
              <div class="tit-row">
                <h3>平台支付</h3>
                <p>支持所有银行卡或信用卡，更迅速、安全</p>
              </div>
              <el-radio-group v-model="payType" class="type-tab">
                <el-radio v-for="item in paymentMethods" :key="item" class="tab" :value="item">
                  <span class="pay-label">{{ payLabel(item) }}</span>
                </el-radio>
              </el-radio-group>
            </div>
          </div>

          <!-- 线下支付说明 -->
          <div v-if="Number(order.payTypeId) >= 3 && offlineKeys.length" class="pay-type-box">
            <el-tabs type="border-card" v-model="activeOfflineTab">
              <el-tab-pane v-for="key in offlineKeys" :key="key" :label="offlineLabel(key)" :name="key">
                <div class="offline-content" v-html="offlinePaymentList[key]"></div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 去付款 -->
          <div v-if="Number(order.payTypeId) < 3" class="btn-box">
            <el-button type="primary" size="large" class="pay-btn" :disabled="!payType" @click="goPay">
              去付款 <el-icon><Right /></el-icon>
            </el-button>
          </div>

          <div v-if="payHint" class="pay-hint">{{ payHint }}</div>
        </template>
      </template>

      <el-empty v-else-if="!loading" description="未找到待支付订单">
        <el-button type="primary" @click="$router.push('/member/orders')">查看我的订单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 支付方式选择页（对齐原版 `pages/order/pay.vue`）。
 *
 * 结构：订单提交成功提示 → 需支付金额/订单号 → 平台支付渠道选择 →
 * 线下汇款说明 tabs → 去付款（跳转渠道收银台）。
 * 渠道收银台见 `PayChannelView`（`/pay/channel?type=&id=`）。
 */
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheckFilled, Right } from '@element-plus/icons-vue';
import { payApi } from '@/api';
import type { PayIndex } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const paid = ref(false);
const order = ref<Record<string, any> | null>(null);
const payIndex = ref<PayIndex | null>(null);
const payType = ref('');
const activeOfflineTab = ref('');
const payHint = ref('');

const orderId = computed(() => Number(route.query.orderId) || 0);
const paymentList = computed<string[]>(() => payIndex.value?.paymentList ?? []);
const offlinePaymentList = computed<Record<string, string>>(() => payIndex.value?.offlinePaymentList ?? {});
const offlineKeys = computed(() => Object.keys(offlinePaymentList.value));

/** 可用渠道（过滤线下/余额类，与原版一致）。 */
const paymentMethods = computed(() =>
  paymentList.value.filter((code) => !['offline', 'balance', 'cash', 'transfer'].includes(code.toLowerCase())),
);

const PAY_LABELS: Record<string, string> = {
  wechat: '微信支付',
  alipay: '支付宝',
  paypal: 'PayPal',
  yunpayWechat: '云支付-微信',
  yunpayAlipay: '云支付-支付宝',
  yunpayYunshanfu: '云支付-云闪付',
  yabanpayWechat: '亚版支付-微信',
  yabanpayAlipay: '亚版支付-支付宝',
  huifuWechat: '汇付-微信',
  huifuAlipay: '汇付-支付宝',
  lakalaWechat: '拉卡拉-微信',
  lakalaAlipay: '拉卡拉-支付宝',
  yeepayWechat: '易宝-微信',
  yeepayAlipay: '易宝-支付宝',
  applepay: 'Apple Pay',
};

const OFFLINE_LABELS: Record<string, string> = {
  thirdPartyPaymentTransfer: '第三方支付转账',
  offlinePayBank: '银行汇款',
  offlinePayCompany: '企业汇款',
  otherPaymentRemittance: '其他汇款方式',
};

function payLabel(code: string): string {
  return PAY_LABELS[code] ?? code;
}

function offlineLabel(key: string): string {
  return OFFLINE_LABELS[key] ?? key;
}

function goPay() {
  if (!payType.value) {
    ElMessage.warning('请选择支付方式');
    return;
  }
  // 渠道页面按真实契约调用 /order/pay/create 与 /order/pay/checkStatus
  router.push({ path: '/pay/channel', query: { id: String(orderId.value), type: payType.value } });
}

async function load() {
  if (!orderId.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    payIndex.value = await payApi.index(orderId.value);
    order.value = (payIndex.value?.order as Record<string, any>) ?? null;
    if (order.value) {
      paid.value = Number(order.value.payStatus) === 2;
    }
    if (paymentMethods.value.length) payType.value = paymentMethods.value[0];
    if (offlineKeys.value.length) activeOfflineTab.value = offlineKeys.value[0];
    await refreshStatus();
    applySeo({ title: '支付订单', noindex: true });
  } catch (err: any) {
    order.value = null;
    ElMessage.error(err?.message || '获取支付信息失败');
  } finally {
    loading.value = false;
  }
}

async function refreshStatus() {
  if (!orderId.value || paid.value) return;
  try {
    const status = await payApi.checkStatus(orderId.value);
    if (Number(status) === 1) {
      paid.value = true;
    }
  } catch {
    // 支付状态查询失败时保留页面，用户可继续支付或稍后刷新
  }
}

onMounted(load);
</script>

<style scoped lang="less">
.tig-pay-result-page {
  background: #f5f6fa;
  min-height: 70vh;
  padding: 30px 0 60px;
}

.success-txt {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}

.price-box {
  background: #fff;
  border-radius: 8px;
  padding: 16px 22px;
  display: flex;
  align-items: center;
  gap: 60px;
  margin-bottom: 16px;

  .num-box {
    display: flex;
    align-items: baseline;
    gap: 8px;
    .tit { color: #666; font-size: 14px; }
    .price { color: var(--tig-red); font-size: 26px; font-weight: 800; }
  }

  .order-num {
    display: flex;
    align-items: baseline;
    gap: 8px;
    font-size: 14px;
    .tit { color: #666; }
    .num { color: #333; &:hover { color: var(--tig-red); } }
  }
}

.pay-type-box {
  background: #fff;
  border-radius: 8px;
  padding: 16px 22px;
  margin-bottom: 16px;
}

.payment-section {
  .tit-row {
    margin-bottom: 12px;
    h3 { font-size: 16px; font-weight: 700; }
    p { color: #999; font-size: 12px; margin-top: 4px; }
  }
}

.type-tab {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;

  .tab {
    border: 1px solid #ddd;
    border-radius: 6px;
    padding: 10px 24px;
    height: auto;
    margin-right: 0;

    :deep(.el-radio__label) { padding-left: 6px; }
    &.is-checked { border-color: var(--tig-red); }
  }

  .pay-label { font-size: 14px; color: #333; }
}

.btn-box {
  display: flex;
  justify-content: center;
  margin: 20px 0;

  .pay-btn { min-width: 220px; font-weight: 700; }
}

.offline-content {
  line-height: 1.8;
  font-size: 13px;
  color: #555;
}

.pay-hint {
  text-align: center;
  color: #888;
  font-size: 13px;
}

.paid-box {
  background: #fff;
  border-radius: 8px;
  padding: 26px 30px;
  display: flex;
  align-items: center;
  gap: 18px;

  .paid-icon { font-size: 48px; color: #67c23a; }
  .paid-text {
    h2 { font-size: 20px; margin-bottom: 6px; }
    p { color: #888; font-size: 13px; }
  }
  .paid-actions { margin-left: auto; display: flex; gap: 12px; }
}
</style>
