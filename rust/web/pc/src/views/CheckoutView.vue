<template>
  <div class="tig-checkout-page container">
    <h1 class="page-title">确认订单</h1>

    <div v-loading="loading" class="checkout-body">
      <!-- 收货地址 -->
      <section class="panel">
        <div class="panel-head">
          <h2>收货地址</h2>
          <el-button link type="primary" @click="$router.push('/member/address')">管理收货地址</el-button>
        </div>
        <div v-if="addresses.length === 0" class="empty-tip">
          暂无收货地址，请先
          <router-link to="/member/address" class="link">添加收货地址</router-link>
        </div>
        <div v-else class="address-grid">
          <div
            v-for="addr in addresses"
            :key="addr.addressId"
            class="address-card"
            :class="{ active: selectedAddressId === addr.addressId }"
            @click="selectAddress(addr.addressId)"
          >
            <div class="addr-top">
              <span class="consignee">{{ addr.consignee }}</span>
              <span class="mobile">{{ addr.mobile }}</span>
              <el-tag v-if="addr.isDefault === 1" size="small" type="danger">默认</el-tag>
            </div>
            <div class="addr-text">{{ addr.regionName }} {{ addr.address }}</div>
          </div>
        </div>
      </section>

      <!-- 订单商品 -->
      <section class="panel">
        <div class="panel-head">
          <h2>商品清单</h2>
          <router-link to="/cart" class="link">返回购物车修改</router-link>
        </div>
        <el-table :data="cart.items.filter((it) => it.checked)" border>
          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <div class="table-product">
                <img :src="imageUrl(row.picThumb)" :alt="row.productName" class="thumb" />
                <div>
                  <div class="name">{{ row.productName }}</div>
                  <div v-if="row.skuData" class="sku">{{ row.skuData }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center" prop="quantity" />
          <el-table-column label="小计" width="140" align="center">
            <template #default="{ row }">¥{{ formatPrice(row.subtotal) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!cart.items.some((it) => it.checked)" description="没有勾选商品，请先回购物车勾选" :image-size="80" />
      </section>

      <!-- 优惠 / 支付方式 / 备注 -->
      <section class="panel">
        <div class="panel-head">
          <h2>支付与配送</h2>
        </div>

        <div class="form-row">
          <span class="form-label">支付方式</span>
          <div class="pay-types">
            <el-radio-group v-model="payTypeId">
              <el-radio v-for="pt in availablePayTypes" :key="pt.typeId" :value="pt.typeId">
                {{ pt.typeName }}
                <span v-if="pt.disabled" class="disabled-desc">（{{ pt.disabledDesc || '不可用' }}）</span>
              </el-radio>
            </el-radio-group>
            <span v-if="availablePayTypes.length === 0" class="muted">暂无可用的线上支付方式</span>
          </div>
        </div>

        <div class="form-row" v-if="coupons.length">
          <span class="form-label">优惠券</span>
          <el-checkbox-group v-model="couponIds">
            <el-checkbox v-for="c in coupons" :key="c.couponId ?? c.id" :value="c.couponId ?? c.id">
              {{ c.couponName ?? c.name ?? `优惠券 ${c.couponId ?? c.id}` }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="form-row">
          <span class="form-label">订单备注</span>
          <el-input v-model="buyerNote" type="textarea" :rows="2" maxlength="120" show-word-limit placeholder="选填，例如：请尽快发货" />
        </div>
      </section>

      <!-- 金额汇总 -->
      <section class="panel summary-panel">
        <div class="summary-row"><span>商品金额</span><span>¥{{ formatPrice(checkTotal.productAmount) }}</span></div>
        <div class="summary-row"><span>运费</span><span>¥{{ formatPrice(checkTotal.shippingFee ?? checkTotal.storeShippingFeeTotal) }}</span></div>
        <div class="summary-row"><span>优惠金额</span><span>-¥{{ formatPrice(checkTotal.discountAmount ?? 0) }}</span></div>
        <div class="summary-row total-row">
          <span>应付金额</span>
          <span class="total-amount">¥{{ formatPrice(payableAmount) }}</span>
        </div>
      </section>

      <div class="submit-bar flex-between">
        <div class="submit-tip">
          <span v-if="checkError" class="error-text">{{ checkError }}</span>
          <span v-else>提交订单前请确认收货地址与商品信息</span>
        </div>
        <div class="submit-actions">
          <el-button size="large" @click="$router.push('/cart')">返回购物车</el-button>
          <el-button type="primary" size="large" :loading="submitting" :disabled="!canSubmit" @click="submitOrder">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { addressApi, orderApi } from '@/api';
import { pageItems } from '@/api/types';
import type { AddressVO, OrderCheckVO, PaymentType } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

const loading = ref(true);
const submitting = ref(false);
const addresses = ref<AddressVO[]>([]);
const paymentTypes = ref<PaymentType[]>([]);
const selectedAddressId = ref<number | undefined>(undefined);
const payTypeId = ref<number | undefined>(undefined);
const buyerNote = ref('');
const couponIds = ref<number[]>([]);
const checkVO = ref<OrderCheckVO | null>(null);
const checkError = ref('');

const availablePayTypes = computed(() => paymentTypes.value.filter((pt) => pt.isShow !== false));
const checkTotal = computed<Record<string, any>>(() => (checkVO.value?.total ?? {}) as Record<string, any>);
const payableAmount = computed(() => {
  const total = checkTotal.value;
  return total.totalAmount ?? total.paidAmount ?? total.discountAfter ?? cart.checkedAmount;
});
const coupons = computed<any[]>(() => checkVO.value?.couponList?.enableCoupons ?? []);
const canSubmit = computed(() => {
  return (
    cart.checkedCount > 0 &&
    Boolean(selectedAddressId.value) &&
    Boolean(payTypeId.value) &&
    !submitting.value
  );
});

function selectAddress(id: number) {
  selectedAddressId.value = id;
  refreshCheck();
}

function buildCheckParam() {
  return {
    addressId: selectedAddressId.value,
    payTypeId: payTypeId.value,
    useCouponIds: couponIds.value.length ? couponIds.value : undefined,
    buyerNote: buyerNote.value || undefined,
  };
}

async function refreshCheck() {
  if (!selectedAddressId.value) return;
  try {
    checkVO.value = await orderApi.checkUpdate(buildCheckParam());
    checkError.value = '';
  } catch (err: any) {
    checkVO.value = null;
    checkError.value = err?.message || '结算数据加载失败';
  }
}

async function loadAddresses() {
  const page = await addressApi.list();
  addresses.value = pageItems(page);
  const preferred =
    addresses.value.find((a) => a.isSelected === 1) ??
    addresses.value.find((a) => a.isDefault === 1) ??
    addresses.value[0];
  selectedAddressId.value = preferred?.addressId;
}

async function loadPaymentTypes() {
  paymentTypes.value = await orderApi.paymentTypes();
  const firstEnabled = paymentTypes.value.find((pt) => !pt.disabled && pt.isShow !== false);
  payTypeId.value = firstEnabled?.typeId;
}

async function loadCheck() {
  if (!selectedAddressId.value) {
    checkError.value = '请先添加收货地址';
    return;
  }
  try {
    checkVO.value = await orderApi.checkIndex(buildCheckParam());
    checkError.value = '';
  } catch (err: any) {
    checkVO.value = null;
    checkError.value = err?.message || '结算数据加载失败';
  }
}

async function submitOrder() {
  const cartIds = cart.items.filter((it) => it.checked).map((it) => it.cartId);
  if (!cartIds.length) {
    ElMessage.warning('没有勾选商品');
    return;
  }
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址');
    return;
  }
  if (!payTypeId.value) {
    ElMessage.warning('请选择支付方式');
    return;
  }
  submitting.value = true;
  try {
    const result = await orderApi.submit({
      cartIds,
      addressId: selectedAddressId.value,
      payTypeId: payTypeId.value,
      useCouponIds: couponIds.value.length ? couponIds.value : undefined,
      buyerNote: buyerNote.value || undefined,
    });
    await cart.fetchSummary(user.token);
    await cart.fetchCount(user.token);
    router.replace({ path: '/pay/result', query: { orderId: String(result.orderId) } });
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  applySeo({ title: '确认订单', noindex: true });
  loading.value = true;
  try {
    await cart.fetchSummary(user.token);
    await Promise.all([loadAddresses(), loadPaymentTypes()]);
    await loadCheck();
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.tig-checkout-page {
  padding: 20px 0 50px;
}

.page-title { font-size: 22px; font-weight: 700; margin-bottom: 16px; }

.panel {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 16px;

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    h2 { font-size: 16px; font-weight: 700; }
  }

  .link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
  .muted { color: #999; font-size: 13px; }
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.address-card {
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    border-color: var(--tig-red);
    background: #fff7f6;
  }

  .addr-top {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;

    .consignee { font-weight: 700; }
    .mobile { color: #888; font-size: 13px; }
  }

  .addr-text { color: #555; font-size: 13px; line-height: 1.5; }
}

.empty-tip { color: #888; font-size: 13px; }

.table-product {
  display: flex;
  align-items: center;
  gap: 10px;

  .thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
  .name { font-size: 13px; }
  .sku { color: #999; font-size: 12px; margin-top: 4px; }
}

.form-row {
  display: flex;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px dashed #f5f5f5;
  &:last-child { border-bottom: none; }

  .form-label { flex: 0 0 74px; color: #888; font-size: 13px; padding-top: 4px; }
  .pay-types { flex: 1; }
  .disabled-desc { color: #bbb; font-size: 12px; }
}

.summary-panel {
  text-align: right;
}

.summary-row {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 13px;
  color: #666;
  padding: 6px 0;

  span:last-child { width: 120px; text-align: right; }
}

.total-row {
  border-top: 1px solid #f0f0f0;
  margin-top: 6px;
  padding-top: 12px;
  font-size: 14px;
  color: #333;

  .total-amount { color: var(--tig-red); font-size: 24px; font-weight: 800; }
}

.submit-bar {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 14px 18px;

  .submit-tip { color: #888; font-size: 13px; }
  .error-text { color: var(--tig-red); }
  .submit-actions { display: flex; gap: 12px; }
}
</style>
