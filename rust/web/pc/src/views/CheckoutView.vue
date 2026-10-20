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

      <!-- 店铺分组商品 + 运费 -->
      <section class="panel">
        <div class="panel-head">
          <h2>商品清单</h2>
          <router-link to="/cart" class="link">返回购物车修改</router-link>
        </div>

        <template v-if="shopGroups.length">
          <div v-for="group in shopGroups" :key="group.shopId" class="shop-group">
            <div class="shop-head flex-between">
              <span class="shop-title">{{ group.shopTitle || `店铺 ${group.shopId}` }}</span>
              <span class="shop-shipping">
                运费：<strong>¥{{ formatPrice(groupShippingFee(group)) }}</strong>
                <span v-if="shippingTypeName(group)" class="shipping-type">（{{ shippingTypeName(group) }}）</span>
              </span>
            </div>
            <div v-for="(row, idx) in group.carts" :key="row.cartId ?? idx" class="shop-item flex-between">
              <div class="item-left">
                <img :src="imageUrl(row.picThumb)" :alt="row.productName" class="thumb" />
                <div class="item-meta">
                  <div class="name">{{ row.productName }}</div>
                  <div v-if="row.skuValue || row.skuData" class="sku">{{ row.skuValue || row.skuData }}</div>
                </div>
              </div>
              <div class="item-right">
                <span class="price">¥{{ formatPrice(row.price) }}</span>
                <span class="qty">× {{ row.quantity }}</span>
                <span class="subtotal">¥{{ formatPrice(row.subtotal) }}</span>
              </div>
            </div>
            <div class="shop-total">
              店铺小计：<span class="amount">¥{{ formatPrice(group.total?.totalAmount ?? group.total?.paidAmount) }}</span>
            </div>
          </div>
        </template>

        <el-empty v-else description="没有勾选商品，请先回购物车勾选" :image-size="80" />
      </section>

      <!-- 优惠券 -->
      <section class="panel">
        <div class="panel-head">
          <h2>优惠券</h2>
          <span class="muted" v-if="disableCouponCount">有 {{ disableCouponCount }} 张券不满足使用条件</span>
        </div>
        <div v-if="coupons.length === 0" class="muted">暂无可用优惠券</div>
        <el-checkbox-group v-else v-model="selectedCouponIds" class="coupon-grid" @change="onCouponChange">
          <el-checkbox
            v-for="c in coupons"
            :key="c.userCouponId"
            :value="c.userCouponId"
            class="coupon-item"
          >
            <div class="coupon-card">
              <div class="coupon-money">
                <span class="symbol">¥</span>{{ formatPrice(c.couponMoney) }}
                <span class="coupon-type">{{ couponTypeText(c.couponType) }}</span>
              </div>
              <div class="coupon-info">
                <div class="coupon-name">{{ c.couponName }}</div>
                <div class="coupon-desc">{{ c.couponDesc }}</div>
                <div class="coupon-limit">满 ¥{{ formatPrice(c.minOrderAmount) }} 可用</div>
              </div>
            </div>
          </el-checkbox>
        </el-checkbox-group>
      </section>

      <!-- 积分 / 余额 -->
      <section class="panel">
        <div class="panel-head"><h2>积分与余额</h2></div>
        <div class="form-row">
          <span class="form-label">积分抵扣</span>
          <div class="inline-control">
            <el-checkbox v-model="usePointCheck" :disabled="availablePoints <= 0" @change="onDeductionChange">
              使用积分（可用 {{ availablePoints }}）
            </el-checkbox>
            <el-input-number
              v-if="usePointCheck"
              v-model="usePointInput"
              :min="0"
              :max="availablePoints"
              size="small"
              @change="onDeductionChange"
            />
            <span v-if="usePointCheck" class="muted">可抵 ¥{{ formatPrice(checkTotal.pointsAmount) }}</span>
          </div>
        </div>
        <div class="form-row">
          <span class="form-label">余额支付</span>
          <div class="inline-control">
            <el-checkbox v-model="useBalanceCheck" :disabled="balanceAvailable <= 0" @change="onDeductionChange">
              使用账户余额（可用 ¥{{ formatPrice(balanceAvailable) }}）
            </el-checkbox>
            <span v-if="useBalanceCheck" class="muted">将抵扣 ¥{{ formatPrice(checkTotal.balance) }}</span>
          </div>
        </div>
      </section>

      <!-- 发票 -->
      <section class="panel">
        <div class="panel-head">
          <h2>发票信息</h2>
          <el-switch v-model="needInvoice" active-text="需要发票" @change="loadInvoice" />
        </div>
        <div v-if="needInvoice" class="invoice-form">
          <div class="form-row">
            <span class="form-label">发票类型</span>
            <el-radio-group v-model="invoiceForm.invoiceType" @change="loadInvoice">
              <el-radio :value="1">电子普通发票</el-radio>
              <el-radio :value="2">增值税专用发票</el-radio>
            </el-radio-group>
          </div>
          <div class="form-row">
            <span class="form-label">抬头类型</span>
            <el-radio-group v-model="invoiceForm.titleType">
              <el-radio :value="1">个人</el-radio>
              <el-radio :value="2">单位</el-radio>
            </el-radio-group>
          </div>
          <div class="form-row">
            <span class="form-label">发票抬头</span>
            <el-input v-model="invoiceForm.title" maxlength="60" placeholder="个人姓名或单位名称" />
          </div>
          <div class="form-row" v-if="invoiceForm.titleType === 2">
            <span class="form-label">税号</span>
            <el-input v-model="invoiceForm.taxNo" maxlength="30" placeholder="纳税人识别号" />
          </div>
          <div class="form-row">
            <span class="form-label">发票内容</span>
            <el-input v-model="invoiceForm.content" maxlength="40" placeholder="商品明细 / 商品类别" />
          </div>
          <div class="form-row">
            <span class="form-label">接收邮箱</span>
            <el-input v-model="invoiceForm.email" maxlength="60" placeholder="电子发票接收邮箱" />
          </div>
          <div class="form-row">
            <span class="form-label">手机号</span>
            <el-input v-model="invoiceForm.mobile" maxlength="20" placeholder="选填" />
          </div>
          <div v-if="lastInvoice" class="invoice-hint">
            已带入上次发票信息：{{ lastInvoice.invoiceTypeName }} / {{ lastInvoice.companyName || '-' }}
          </div>
        </div>
      </section>

      <!-- 支付方式 / 备注 -->
      <section class="panel">
        <div class="panel-head"><h2>支付方式与备注</h2></div>
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
        <div class="form-row">
          <span class="form-label">订单备注</span>
          <el-input v-model="buyerNote" type="textarea" :rows="2" maxlength="120" show-word-limit placeholder="选填，例如：请尽快发货" />
        </div>
      </section>

      <!-- 金额汇总（按后端 CheckTotalVO 字段展示） -->
      <section class="panel summary-panel">
        <div class="summary-row"><span>商品金额</span><span>¥{{ formatPrice(checkTotal.productAmount) }}</span></div>
        <div class="summary-row"><span>运费</span><span>¥{{ formatPrice(checkTotal.shippingFee) }}</span></div>
        <div class="summary-row">
          <span>优惠金额</span><span>-¥{{ formatPrice(checkTotal.discountAmount ?? checkTotal.discounts ?? 0) }}</span>
        </div>
        <div v-if="Number(checkTotal.couponAmount) > 0" class="summary-row">
          <span>优惠券抵扣</span><span>-¥{{ formatPrice(checkTotal.couponAmount) }}</span>
        </div>
        <div v-if="Number(checkTotal.pointsAmount) > 0" class="summary-row">
          <span>积分抵扣</span><span>-¥{{ formatPrice(checkTotal.pointsAmount) }}</span>
        </div>
        <div v-if="Number(checkTotal.balance) > 0" class="summary-row">
          <span>余额抵扣</span><span>-¥{{ formatPrice(checkTotal.balance) }}</span>
        </div>
        <div class="summary-row total-row">
          <span>应付金额</span>
          <span class="total-amount">¥{{ formatPrice(payableAmount) }}</span>
        </div>
      </section>

      <div class="submit-bar flex-between">
        <div class="submit-tip">
          <span v-if="checkError" class="error-text">{{ checkError }}</span>
          <span v-else>提交订单前请确认收货地址、优惠与发票信息</span>
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
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { addressApi, orderApi } from '@/api';
import { pageItems } from '@/api/types';
import type { AddressVO, CheckCoupon, InvoiceData, OrderCheckVO, OrderInvoice, PaymentType } from '@/api/types';
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
const checkVO = ref<OrderCheckVO | null>(null);
const shippingTypes = ref<Record<string, any[]>>({});
const checkError = ref('');

// 优惠券
const selectedCouponIds = ref<number[]>([]);

// 积分 / 余额
const usePointCheck = ref(false);
const usePointInput = ref(0);
const useBalanceCheck = ref(false);

// 发票
const needInvoice = ref(false);
const lastInvoice = ref<OrderInvoice | null>(null);
const invoiceForm = reactive({
  invoiceType: 1,
  titleType: 1,
  title: '',
  taxNo: '',
  content: '商品明细',
  email: '',
  mobile: '',
});

const availablePayTypes = computed(() => paymentTypes.value.filter((pt) => pt.isShow !== false));
const checkTotal = computed<Record<string, any>>(() => (checkVO.value?.total ?? {}) as Record<string, any>);
const payableAmount = computed(() => {
  const total = checkTotal.value;
  return total.totalAmount ?? total.paidAmount ?? cart.checkedAmount;
});
const coupons = computed<CheckCoupon[]>(() => checkVO.value?.couponList?.enableCoupons ?? []);
const disableCouponCount = computed(() => checkVO.value?.couponList?.disableCoupons?.length ?? 0);
const availablePoints = computed(() => Number(checkVO.value?.availablePoints ?? checkTotal.value.exchangePoints ?? 0));
const balanceAvailable = computed(() => Number(checkTotal.value.balance ?? 0));
const shopGroups = computed(() => checkVO.value?.cartList ?? []);
const canSubmit = computed(() => {
  return (
    cart.checkedCount > 0 &&
    Boolean(selectedAddressId.value) &&
    Boolean(payTypeId.value) &&
    !submitting.value
  );
});

function couponTypeText(type: number): string {
  return type === 1 ? '满减券' : type === 2 ? '折扣券' : '优惠券';
}

function groupShippingFee(group: Record<string, any>): number | string {
  const map = checkTotal.value.storeShippingFee as Record<string, any> | undefined;
  if (map && group.shopId in map) return map[group.shopId];
  return group.total?.shippingFee ?? 0;
}

function shippingTypeName(group: Record<string, any>): string {
  const list = shippingTypes.value[String(group.shopId)] ?? [];
  const first = list[0];
  return first?.shippingTypeName ?? '';
}

function buildCheckParam() {
  return {
    addressId: selectedAddressId.value,
    payTypeId: payTypeId.value,
    buyerNote: buyerNote.value || undefined,
    usePoint: usePointCheck.value ? usePointInput.value : 0,
    useBalance: useBalanceCheck.value ? String(balanceAvailable.value) : undefined,
    selectUserCouponIds: selectedCouponIds.value.length ? selectedCouponIds.value : undefined,
  };
}

function selectAddress(id: number) {
  selectedAddressId.value = id;
  refreshCheck(false);
}

async function onCouponChange() {
  await refreshCheck(true);
}

async function onDeductionChange() {
  if (usePointCheck.value && (usePointInput.value <= 0 || usePointInput.value > availablePoints.value)) {
    usePointInput.value = Math.min(availablePoints.value, Math.max(usePointInput.value, 0));
  }
  await refreshCheck(false);
}

/** `useCouponApi=true` 时走 `/order/check/updateCoupon`，否则走 `/order/check/update`。 */
async function refreshCheck(useCouponApi: boolean) {
  if (!selectedAddressId.value) return;
  try {
    const param = buildCheckParam();
    checkVO.value = useCouponApi ? await orderApi.updateCoupon(param) : await orderApi.checkUpdate(param);
    checkError.value = '';
  } catch (err: any) {
    checkError.value = err?.message || '结算数据更新失败';
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

async function loadShippingTypes() {
  if (!shopGroups.value.length) return;
  try {
    shippingTypes.value = await orderApi.storeShippingType({
      addressId: selectedAddressId.value,
      payTypeId: payTypeId.value,
    });
  } catch {
    shippingTypes.value = {};
  }
}

async function loadInvoice() {
  if (!needInvoice.value) return;
  try {
    const invoice = await orderApi.getInvoice({ invoiceType: invoiceForm.invoiceType });
    lastInvoice.value = invoice;
    if (invoice) {
      invoiceForm.titleType = invoice.titleType || invoiceForm.titleType;
      invoiceForm.title = invoice.companyName || invoiceForm.title;
      invoiceForm.taxNo = invoice.companyCode || invoiceForm.taxNo;
      invoiceForm.content = invoice.invoiceContent || invoiceForm.content;
      invoiceForm.email = invoice.email || invoiceForm.email;
      invoiceForm.mobile = invoice.mobile || invoiceForm.mobile;
    }
  } catch {
    lastInvoice.value = null;
  }
}

async function loadCheck() {
  if (!selectedAddressId.value) {
    checkError.value = '请先添加收货地址';
    return;
  }
  try {
    checkVO.value = await orderApi.checkIndex(buildCheckParam());
    checkError.value = '';
    usePointInput.value = Math.min(usePointInput.value, availablePoints.value);
    await loadShippingTypes();
  } catch (err: any) {
    checkVO.value = null;
    checkError.value = err?.message || '结算数据加载失败';
  }
}

function buildInvoiceData(): InvoiceData | undefined {
  if (!needInvoice.value) return undefined;
  if (!invoiceForm.title.trim()) {
    ElMessage.warning('请填写发票抬头');
    return undefined;
  }
  return {
    invoiceType: invoiceForm.invoiceType,
    titleType: invoiceForm.titleType,
    title: invoiceForm.title.trim(),
    taxNo: invoiceForm.invoiceType === 2 || invoiceForm.titleType === 2 ? invoiceForm.taxNo.trim() || undefined : undefined,
    content: invoiceForm.content || undefined,
    companyName: invoiceForm.title.trim(),
    email: invoiceForm.email || undefined,
    mobile: invoiceForm.mobile || undefined,
  };
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
  const invoiceData = buildInvoiceData();
  if (needInvoice.value && !invoiceData) return;
  submitting.value = true;
  try {
    const result = await orderApi.submit({
      cartIds,
      addressId: selectedAddressId.value,
      payTypeId: payTypeId.value,
      buyerNote: buyerNote.value || undefined,
      usePoint: usePointCheck.value ? usePointInput.value : undefined,
      useBalance: useBalanceCheck.value ? String(balanceAvailable.value) : undefined,
      useCouponIds: selectedCouponIds.value.length ? selectedCouponIds.value : undefined,
      invoiceData,
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
    if (needInvoice.value) await loadInvoice();
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

.shop-group {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 12px;

  .shop-head {
    background: #fafafa;
    padding: 8px 12px;
    font-size: 13px;
    .shop-title { font-weight: 700; }
    .shop-shipping {
      color: #888;
      strong { color: var(--tig-red); }
      .shipping-type { color: #aaa; }
    }
  }
}

.shop-item {
  padding: 10px 12px;
  border-top: 1px solid #f7f7f7;

  .item-left {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    .thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 4px; border: 1px solid #f0f0f0; }
    .item-meta {
      min-width: 0;
      .name { font-size: 13px; }
      .sku { color: #999; font-size: 12px; margin-top: 4px; }
    }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 22px;
    font-size: 13px;
    color: #666;
    .price { width: 90px; text-align: right; }
    .qty { width: 60px; text-align: center; }
    .subtotal { width: 100px; text-align: right; color: var(--tig-red); font-weight: 700; }
  }
}

.shop-total {
  text-align: right;
  padding: 8px 12px;
  border-top: 1px dashed #f0f0f0;
  font-size: 13px;
  color: #666;
  .amount { color: var(--tig-red); font-weight: 800; }
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  width: 100%;
}

.coupon-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid #ffe1df;
  border-radius: 6px;
  background: #fffaf9;

  .coupon-money {
    color: var(--tig-red);
    font-size: 22px;
    font-weight: 800;
    min-width: 92px;
    .symbol { font-size: 13px; }
    .coupon-type { display: block; font-size: 11px; color: #999; font-weight: 400; }
  }

  .coupon-info {
    min-width: 0;
    .coupon-name { font-size: 13px; font-weight: 600; }
    .coupon-desc,
    .coupon-limit { font-size: 12px; color: #999; margin-top: 2px; }
  }
}

.form-row {
  display: flex;
  gap: 14px;
  padding: 8px 0;
  border-bottom: 1px dashed #f5f5f5;
  &:last-child { border-bottom: none; }

  .form-label { flex: 0 0 74px; color: #888; font-size: 13px; padding-top: 4px; }
  .inline-control { display: flex; align-items: center; gap: 12px; flex: 1; flex-wrap: wrap; }
  .pay-types { flex: 1; }
  .disabled-desc { color: #bbb; font-size: 12px; }
}

.invoice-hint { color: #aaa; font-size: 12px; padding-top: 6px; }

.summary-panel { text-align: right; }

.summary-row {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 13px;
  color: #666;
  padding: 5px 0;
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
