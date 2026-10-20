<template>
  <div class="tig-pay-result-page container">
    <div v-loading="loading" class="pay-card">
      <template v-if="order">
        <!-- 结果状态 -->
        <div class="status-block" :class="paid ? 'is-paid' : 'is-unpaid'">
          <el-icon class="status-icon"><component :is="paid ? CircleCheckFilled : WarningFilled" /></el-icon>
          <div class="status-text">
            <h1>{{ paid ? '支付成功' : '订单待支付' }}</h1>
            <p v-if="paid">支付已完成，感谢您的购买，订单将尽快为您安排发货。</p>
            <p v-else>
              请在 <strong class="countdown">{{ remainText }}</strong> 内完成支付，超时订单将自动关闭。
            </p>
          </div>
        </div>

        <!-- 订单信息 -->
        <el-descriptions :column="2" border class="order-desc">
          <el-descriptions-item label="订单号">{{ order.orderSn }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(order.addTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ formatPrice(order.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="待付金额">
            <span class="amount">¥{{ formatPrice(order.unpaidAmount ?? order.totalAmount) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 支付方式（未支付时展示） -->
        <template v-if="!paid">
          <div class="pay-section">
            <h2 class="section-title">选择支付方式</h2>
            <div v-if="payList.length === 0" class="muted">当前订单没有可用的线上支付渠道，请联系客服线下处理。</div>
            <div v-else class="pay-list">
              <button
                v-for="code in payList"
                :key="code"
                class="pay-item"
                :class="{ active: selectedPayType === code }"
                @click="selectedPayType = code"
              >
                {{ payLabel(code) }}
              </button>
            </div>
          </div>

          <div class="pay-actions">
            <el-button
              type="primary"
              size="large"
              :loading="paying"
              :disabled="!selectedPayType"
              @click="createPayment"
            >
              立即支付
            </el-button>
            <el-button size="large" @click="refreshStatus">刷新支付状态</el-button>
            <el-button size="large" @click="$router.push('/member/orders')">查看我的订单</el-button>
          </div>

          <div v-if="payHint" class="pay-hint">{{ payHint }}</div>
        </template>

        <template v-else>
          <div class="pay-actions">
            <el-button type="primary" size="large" @click="$router.push(`/member/orders/${order.orderId}`)">查看订单详情</el-button>
            <el-button size="large" @click="$router.push('/search')">继续购物</el-button>
          </div>
        </template>
      </template>

      <el-empty v-else-if="!loading" description="未找到支付订单">
        <el-button type="primary" @click="$router.push('/member/orders')">查看我的订单</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue';
import { payApi } from '@/api';
import type { PayIndex } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, formatTime } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const paying = ref(false);
const paid = ref(false);
const order = ref<Record<string, any> | null>(null);
const payIndex = ref<PayIndex | null>(null);
const selectedPayType = ref<string>('');
const payHint = ref('');

let pollTimer: number | null = null;
let pollCount = 0;
const MAX_POLL = 30;

const payList = computed<string[]>(() => payIndex.value?.paymentList ?? []);
const orderId = computed(() => Number(route.query.orderId) || 0);

/** 渠道编码 → 中文名（与后端支付渠道编码对齐；未知编码原样展示）。 */
const PAY_LABELS: Record<string, string> = {
  wechat: '微信支付',
  wechatpay: '微信支付',
  alipay: '支付宝',
  balance: '余额支付',
  huifu: '汇付支付',
  lakala: '拉卡拉支付',
  yeepay: '易宝支付',
  yunpay: '云支付',
  paypal: 'PayPal',
  yabanpay: '亚版支付',
  offline: '线下支付',
};

function payLabel(code: string): string {
  return PAY_LABELS[code.toLowerCase()] ?? code;
}

const remainText = computed(() => {
  if (!order.value) return '--';
  const addTime = Number(order.value.addTime ?? 0);
  const end = addTime > 0 ? addTime + 30 * 60 : 0;
  const now = Math.floor(Date.now() / 1000);
  const remain = Math.max(0, end - now);
  const minutes = Math.floor(remain / 60);
  const seconds = remain % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
});

function stopPolling() {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
}

async function refreshStatus(): Promise<boolean> {
  if (!orderId.value) return false;
  try {
    const status = await payApi.checkStatus(orderId.value);
    if (Number(status) === 1) {
      paid.value = true;
      if (order.value) order.value.payStatus = 2;
      stopPolling();
      return true;
    }
  } catch {
    // 轮询失败静默，用户可手动刷新
  }
  return false;
}

function startPolling() {
  stopPolling();
  pollCount = 0;
  pollTimer = window.setInterval(async () => {
    pollCount += 1;
    if (paid.value || pollCount > MAX_POLL) {
      stopPolling();
      return;
    }
    await refreshStatus();
  }, 2000);
}

async function createPayment() {
  if (!selectedPayType.value) {
    ElMessage.warning('请选择支付方式');
    return;
  }
  paying.value = true;
  payHint.value = '';
  try {
    const result = await payApi.create(orderId.value, selectedPayType.value);
    if (result?.payInfo || result?.code) {
      // 渠道返回的支付参数由后续渠道页处理（当前渠道实现未迁移时不会走到这里）
      payHint.value = '已获取支付参数，请按渠道提示完成支付后点击「刷新支付状态」。';
      ElMessage.success('已创建支付单');
    } else {
      payHint.value = '支付单已创建，请完成支付后点击「刷新支付状态」。';
    }
    await refreshStatus();
    if (!paid.value) startPolling();
  } catch (err: any) {
    // 后端对未迁移渠道返回明确业务错误，这里如实展示，不伪造成功
    payHint.value = err?.message || '支付渠道创建失败，请稍后重试或联系客服';
    ElMessage.error(payHint.value);
  } finally {
    paying.value = false;
  }
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
      paid.value = Number(order.value.payStatus) === 2 || Number(order.value.payStatus) === 1;
    }
    if (payList.value.length) {
      selectedPayType.value = payList.value[0];
    }
    await refreshStatus();
    if (!paid.value) startPolling();
    applySeo({ title: '支付结果', noindex: true });
  } catch (err: any) {
    order.value = null;
    ElMessage.error(err?.message || '获取支付信息失败');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
onBeforeUnmount(stopPolling);
</script>

<style scoped lang="less">
.tig-pay-result-page {
  padding: 30px 0 60px;
}

.pay-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 10px;
  padding: 26px 30px;
  min-height: 320px;
}

.status-block {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 22px;
  border-radius: 8px;
  margin-bottom: 22px;

  &.is-paid {
    background: #f0f9eb;
    .status-icon { color: #67c23a; }
  }

  &.is-unpaid {
    background: #fdf6ec;
    .status-icon { color: #e6a23c; }
  }

  .status-icon { font-size: 46px; }

  .status-text {
    h1 { font-size: 22px; margin-bottom: 6px; }
    p { color: #666; font-size: 13px; }
    .countdown { color: var(--tig-red); }
  }
}

.order-desc {
  margin-bottom: 22px;
  .amount { color: var(--tig-red); font-weight: 700; }
}

.section-title { font-size: 16px; font-weight: 700; margin-bottom: 12px; }

.pay-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.pay-item {
  min-width: 140px;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;

  &.active {
    border-color: var(--tig-red);
    color: var(--tig-red);
    background: #fff7f6;
    font-weight: 700;
  }
}

.pay-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.pay-hint {
  margin-top: 14px;
  color: #888;
  font-size: 13px;
}

.muted {
  color: #999;
  font-size: 13px;
}
</style>
