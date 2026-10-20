<template>
  <div class="tig-pay-channel-page">
    <div class="container">
      <div class="channel-card">
        <div class="channel-title">{{ channelName }}</div>

        <div class="message">订单提交成功，请尽快付款！订单号：{{ order?.orderSn || id }}</div>
        <div class="message">
          应付金额：<span class="amount">¥{{ formatPrice(order?.unpaidAmount ?? order?.totalAmount) }}</span>
        </div>

        <!-- 支付渠道内容 -->
        <div class="payment" v-loading="creating">
          <div class="title">{{ channelName }}</div>

          <div class="image-div">
            <div class="left-image">
              <!-- 渠道返回二维码链接时展示扫码入口；无二维码能力时如实说明 -->
              <template v-if="payInfo?.codeUrl || payInfo?.qrCode || payInfo?.qrCodeUrl">
                <div class="ewm">
                  <img v-if="isImageUrl" :src="qrImageUrl" alt="支付二维码" class="qr-img" />
                  <el-button v-else type="primary" @click="openPayUrl(qrImageUrl)">打开扫码支付页</el-button>
                </div>
                <div class="qr-tip">
                  <div>请使用{{ channelName }}扫一扫</div>
                  <div>扫描二维码支付</div>
                </div>
                <div class="qr-link">
                  二维码内容：<el-link type="primary" :href="qrImageUrl" target="_blank">{{ shortUrl }}</el-link>
                </div>
              </template>

              <!-- 跳转类渠道 -->
              <template v-else-if="payRedirectUrl">
                <div class="redirect-box">
                  <p>请点击下方按钮前往支付页面完成付款</p>
                  <el-button type="primary" @click="openPayUrl(payRedirectUrl)">前往支付</el-button>
                  <div class="qr-link">支付地址：<el-link type="primary" :href="payRedirectUrl" target="_blank">{{ shortUrl }}</el-link></div>
                </div>
              </template>

              <!-- 渠道未返回支付参数 / 未迁移：展示真实错误 -->
              <template v-else>
                <el-alert
                  v-if="createError"
                  type="error"
                  :closable="false"
                  :title="createError"
                  show-icon
                />
                <el-alert
                  v-else
                  type="info"
                  :closable="false"
                  title="该支付渠道暂未返回可用的支付参数，请选择其他支付方式或联系客服。"
                  show-icon
                />
              </template>
            </div>

            <div class="right-image">
              <div class="pay-amount">应付金额：<span class="amount">¥{{ formatPrice(order?.unpaidAmount ?? order?.totalAmount) }}</span></div>
              <div class="countdown-box">
                <div v-if="count <= 50">
                  二维码有效期剩余：<strong>{{ remainText }}</strong>
                </div>
                <div v-else>
                  已超时，<el-link type="primary" @click="refreshPage">点击刷新</el-link>
                </div>
              </div>
              <el-button @click="goBack">选择其他支付方式</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 支付渠道收银台（对齐原版 `pages/order/{wechat,alipay,huifu,...}.vue`）。
 *
 * 流程：加载订单 → `GET /order/pay/create?id=&type=` 创建支付单 →
 * 展示二维码/跳转地址（渠道返回什么就展示什么）→
 * 每 2 秒 `GET /order/pay/checkStatus?id=` 轮询（最多 50 次）；
 * 支付成功弹窗提示并跳转订单列表。
 * 渠道未迁移/未配置时直接展示后端真实错误，不伪造成功。
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { payApi } from '@/api';
import { applySeo } from '@/composables/useSeo';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const PAY_CHANNEL_NAMES: Record<string, string> = {
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

const id = Number(route.query.id) || 0;
const type = String(route.query.type ?? '');
const channelName = PAY_CHANNEL_NAMES[type] ?? type;
const creating = ref(false);
const order = ref<Record<string, any> | null>(null);
const payInfo = ref<Record<string, any> | null>(null);
const createError = ref('');
const count = ref(0);
const remainSeconds = ref(100);

let pollTimer: number | null = null;
let countTimer: number | null = null;

const qrImageUrl = computed(() => {
  const info = payInfo.value ?? {};
  return String(info.codeUrl ?? info.qrCode ?? info.qrCodeUrl ?? '');
});
const isImageUrl = computed(() => /^data:image\//i.test(qrImageUrl.value));
const payRedirectUrl = computed(() => {
  const info = payInfo.value ?? {};
  return String(info.payUrl ?? info.url ?? info.redirectUrl ?? info.redirect ?? '');
});
const shortUrl = computed(() => {
  const value = qrImageUrl.value || payRedirectUrl.value;
  return value.length > 60 ? `${value.slice(0, 60)}...` : value;
});
const remainText = computed(() => {
  const minutes = Math.floor(remainSeconds.value / 60);
  const seconds = remainSeconds.value % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
});

function openPayUrl(url: string) {
  if (!url) return;
  window.open(url, '_blank', 'noopener');
}

function goBack() {
  router.push({ path: '/pay/result', query: { orderId: String(id) } });
}

function refreshPage() {
  count.value = 0;
  remainSeconds.value = 100;
  startTimers();
  createPayment();
}

function stopTimers() {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
  if (countTimer !== null) {
    window.clearInterval(countTimer);
    countTimer = null;
  }
}

async function loadOrder() {
  try {
    const index = await payApi.index(id);
    order.value = (index?.order as Record<string, any>) ?? null;
    if (order.value && (Number(order.value.payStatus) === 2)) {
      paidSuccess();
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '获取订单支付信息失败');
  }
}

async function createPayment() {
  creating.value = true;
  createError.value = '';
  try {
    const result = await payApi.create(id, type);
    payInfo.value = (result?.payInfo as Record<string, any>) ?? null;
    if (!payInfo.value && result?.code) {
      payInfo.value = { codeUrl: result.code };
    }
    if (!payInfo.value && !result?.orderId) {
      createError.value = '支付渠道未返回支付参数';
    }
  } catch (err: any) {
    // 真实错误：渠道未迁移/未配置时后端返回明确业务错误
    createError.value = err?.message || '创建支付单失败';
    payInfo.value = null;
  } finally {
    creating.value = false;
  }
}

async function checkStatus() {
  if (!id) return;
  try {
    const status = await payApi.checkStatus(id);
    if (Number(status) === 1) {
      stopTimers();
      paidSuccess();
    }
  } catch {
    // 轮询失败静默，继续下一次
  }
}

function paidSuccess() {
  ElMessageBox.confirm('订单支付成功', '提示', {
    confirmButtonText: '查看订单',
    showClose: false,
    showCancelButton: false,
    closeOnClickModal: false,
    type: 'success',
  })
    .then(() => {
      router.replace('/member/orders');
    })
    .catch(() => {
      router.replace('/member/orders');
    });
}

function startTimers() {
  stopTimers();
  pollTimer = window.setInterval(() => {
    count.value += 1;
    if (count.value >= 50) {
      stopTimers();
      ElMessage.error('支付已超时，您可以刷新页面重新支付');
      return;
    }
    checkStatus();
  }, 2000);
  countTimer = window.setInterval(() => {
    remainSeconds.value = Math.max(0, remainSeconds.value - 1);
  }, 1000);
}

function goAnother() {
  goBack();
}
void goAnother;

onMounted(async () => {
  applySeo({ title: channelName, noindex: true });
  await loadOrder();
  if (order.value && Number(order.value.payStatus) === 2) return;
  await createPayment();
  startTimers();
});

onBeforeUnmount(stopTimers);
</script>

<style scoped lang="less">
.tig-pay-channel-page {
  background: #fff;
  min-height: 70vh;
  padding: 30px 0 60px;
}

.channel-card {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .channel-title {
    font-size: 20px;
    font-weight: 700;
    color: #333;
  }

  .message {
    font-size: 14px;
    color: #666;
    .amount { color: var(--tig-red); font-size: 18px; font-weight: 700; }
  }
}

.payment {
  background-color: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.12);
  padding: 16px 30px 26px;
  display: flex;
  flex-direction: column;
  gap: 14px;

  .title { font-size: 18px; font-weight: 700; }

  .image-div {
    display: flex;
    gap: 40px;
    align-items: flex-start;
  }

  .left-image {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .ewm {
      width: 300px;
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fafafa;
      border: 1px dashed #ddd;
      border-radius: 8px;
    }

    .qr-img { width: 100%; height: 100%; object-fit: contain; }
    .qr-tip { color: #666; font-size: 14px; line-height: 1.8; }
    .qr-link { color: #999; font-size: 12px; word-break: break-all; }
    .redirect-box { display: flex; flex-direction: column; gap: 12px; color: #666; font-size: 14px; }
  }

  .right-image {
    width: 260px;
    flex: 0 0 260px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    border-left: 1px solid #f0f0f0;
    padding-left: 30px;

    .pay-amount { font-size: 14px; color: #666; .amount { color: var(--tig-red); font-size: 20px; font-weight: 700; } }
    .countdown-box { font-size: 13px; color: #888; }
  }
}
</style>
