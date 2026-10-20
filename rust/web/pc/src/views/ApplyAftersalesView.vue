<template>
  <div class="tig-apply-aftersales-page container">
    <div class="page-head flex-between">
      <h1 class="page-title">申请售后</h1>
      <router-link to="/member/aftersales" class="back-link">返回售后服务</router-link>
    </div>

    <div v-loading="loading" class="apply-body">
      <el-empty v-if="!loading && !applyData" description="未找到可申请售后的订单">
        <el-button type="primary" @click="$router.push('/member/orders')">返回我的订单</el-button>
      </el-empty>

      <template v-else-if="applyData">
        <!-- 订单信息 -->
        <section class="panel">
          <div class="panel-head"><h2>订单信息</h2></div>
          <div class="order-line">
            <span>订单号：{{ applyData.order.orderSn }}</span>
            <span>实付金额：<strong class="amount">¥{{ formatPrice(applyData.order.paidAmount || applyData.order.totalAmount) }}</strong></span>
          </div>
        </section>

        <!-- 选择售后商品 -->
        <section class="panel">
          <div class="panel-head"><h2>选择售后商品</h2></div>
          <el-empty v-if="applyData.list.length === 0" description="该订单暂无可申请售后的商品" :image-size="80" />
          <div v-for="item in applyData.list" :key="item.itemId" class="item-row">
            <el-checkbox v-model="selected[item.itemId]" :label="item.itemId" class="item-check">
              <div class="item-content">
                <img :src="imageUrl(item.picThumb)" :alt="item.productName" class="thumb" />
                <div class="item-meta">
                  <div class="name">{{ item.productName }}</div>
                  <div class="meta">
                    ¥{{ formatPrice(item.price) }} × {{ item.quantity }}
                    <span class="apply-tip">可申请 {{ item.canApplyQuantity }} 件</span>
                  </div>
                </div>
              </div>
            </el-checkbox>
            <div v-if="selected[item.itemId]" class="qty-control">
              <span class="qty-label">数量</span>
              <el-input-number v-model="quantities[item.itemId]" :min="1" :max="item.canApplyQuantity" size="small" />
            </div>
          </div>
        </section>

        <!-- 售后信息 -->
        <section class="panel">
          <div class="panel-head"><h2>售后信息</h2></div>
          <div class="form-row">
            <span class="form-label">售后类型</span>
            <el-radio-group v-model="aftersaleType">
              <el-radio v-for="(name, code) in config?.aftersaleType ?? {}" :key="code" :value="Number(code)">
                {{ name }}
              </el-radio>
            </el-radio-group>
          </div>
          <div class="form-row">
            <span class="form-label">售后原因</span>
            <el-select v-model="aftersaleReason" placeholder="请选择售后原因" style="width: 320px">
              <el-option v-for="reason in config?.aftersaleReason ?? []" :key="reason" :label="reason" :value="reason" />
            </el-select>
          </div>
          <div class="form-row">
            <span class="form-label">问题描述</span>
            <el-input
              v-model="description"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="请描述遇到的问题，便于商家处理"
            />
          </div>
          <div class="form-row">
            <span class="form-label">退款金额</span>
            <span class="refund-amount">¥{{ formatPrice(refundAmount) }}</span>
          </div>
        </section>

        <div class="submit-bar flex-between">
          <span class="tip">提交后商家将在 48 小时内处理</span>
          <div class="actions">
            <el-button size="large" @click="$router.back()">取消</el-button>
            <el-button type="primary" size="large" :loading="submitting" :disabled="!canSubmit" @click="submit">
              提交申请
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { aftersaleApi } from '@/api';
import type { AftersalesApplyData, AftersalesConfig } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { formatPrice, imageUrl } from '@/utils/format';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const submitting = ref(false);
const applyData = ref<AftersalesApplyData | null>(null);
const config = ref<AftersalesConfig | null>(null);

const selected = reactive<Record<number, boolean>>({});
const quantities = reactive<Record<number, number>>({});
const aftersaleType = ref<number>(1);
const aftersaleReason = ref('');
const description = ref('');

const orderId = computed(() => Number(route.query.orderId) || 0);

const selectedItems = computed(() =>
  (applyData.value?.list ?? []).filter((item) => selected[item.itemId]),
);

const refundAmount = computed(() =>
  selectedItems.value.reduce(
    (sum, item) => sum + Number(item.price ?? 0) * (quantities[item.itemId] ?? 1),
    0,
  ),
);

const canSubmit = computed(
  () => selectedItems.value.length > 0 && Boolean(aftersaleReason.value) && !submitting.value,
);

async function load() {
  if (!orderId.value) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const [data, cfg] = await Promise.all([
      aftersaleApi.applyData(orderId.value),
      aftersaleApi.config().catch(() => null),
    ]);
    applyData.value = data;
    config.value = cfg;
    const firstType = Object.keys(cfg?.aftersaleType ?? {})[0];
    if (firstType) aftersaleType.value = Number(firstType);
    for (const item of data.list ?? []) {
      selected[item.itemId] = false;
      quantities[item.itemId] = Math.max(1, Math.min(item.canApplyQuantity, 1));
    }
  } catch {
    applyData.value = null;
  } finally {
    loading.value = false;
  }
}

async function submit() {
  const items = selectedItems.value.map((item) => ({
    orderItemId: item.itemId,
    number: quantities[item.itemId] ?? 1,
  }));
  submitting.value = true;
  try {
    await aftersaleApi.create({
      orderId: orderId.value,
      aftersaleType: aftersaleType.value,
      aftersaleReason: aftersaleReason.value,
      description: description.value || undefined,
      refundAmount: refundAmount.value,
      items,
    });
    ElMessage.success('售后申请已提交');
    router.replace('/member/aftersales');
  } finally {
    submitting.value = false;
  }
}

onMounted(load);
applySeo({ title: '申请售后', noindex: true });
</script>

<style scoped lang="less">
.tig-apply-aftersales-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 14px;
  .page-title { font-size: 22px; font-weight: 700; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.apply-body { min-height: 240px; }

.panel {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 14px;

  .panel-head {
    margin-bottom: 12px;
    h2 { font-size: 16px; font-weight: 700; }
  }
}

.order-line {
  display: flex;
  gap: 30px;
  font-size: 13px;
  color: #666;
  .amount { color: var(--tig-red); font-weight: 700; }
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f7f7f7;
  &:last-child { border-bottom: none; }

  .item-check {
    flex: 1;
    height: auto;
    align-items: flex-start;
    :deep(.el-checkbox__label) { width: 100%; }
  }

  .item-content {
    display: flex;
    align-items: center;
    gap: 10px;

    .thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
    .item-meta {
      .name { font-size: 13px; }
      .meta { color: #888; font-size: 12px; margin-top: 4px; }
      .apply-tip { color: var(--tig-red); margin-left: 10px; }
    }
  }

  .qty-control { display: flex; align-items: center; gap: 8px; .qty-label { color: #888; font-size: 13px; } }
}

.form-row {
  display: flex;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px dashed #f5f5f5;
  &:last-child { border-bottom: none; }

  .form-label { flex: 0 0 74px; color: #888; font-size: 13px; padding-top: 4px; }
  .refund-amount { color: var(--tig-red); font-size: 20px; font-weight: 800; }
}

.submit-bar {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 14px 18px;
  .tip { color: #888; font-size: 13px; }
  .actions { display: flex; gap: 12px; }
}
</style>
