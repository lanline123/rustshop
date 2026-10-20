<template>
  <div class="tig-checkout-page">
    <div class="container">
      <div class="page-title-bar">
        <h1 class="page-title">订单结算页</h1>
      </div>

      <div v-loading="loading" class="check-layout">
        <!-- 左侧：订单确认 -->
        <div class="order-confirm-con">
          <!-- 收货地址 -->
          <section class="check-card">
            <div class="card-title">收货地址</div>
            <div v-if="!addressList.length" class="address-empty">
              <span>您还没有收货地址，请先添加</span>
              <el-button type="primary" size="small" @click="$router.push('/member/address')">新增收货地址</el-button>
            </div>
            <template v-else>
              <div v-if="selectedAddress" class="selected-address">
                <div class="addr-main">
                  <span class="consignee">{{ selectedAddress.consignee }}</span>
                  <span class="mobile">{{ selectedAddress.mobile }}</span>
                  <el-tag v-if="selectedAddress.isDefault === 1" size="small" type="danger">默认</el-tag>
                </div>
                <div class="addr-detail">{{ selectedAddress.regionName }} {{ selectedAddress.address }}</div>
                <div class="addr-actions">
                  <el-button link type="primary" @click="showMoreAddress = !showMoreAddress">
                    {{ showMoreAddress ? '收起地址' : '更多地址' }}
                  </el-button>
                  <el-button link @click="$router.push('/member/address')">管理地址</el-button>
                </div>
              </div>
              <div v-if="showMoreAddress" class="address-grid">
                <div
                  v-for="addr in addressList"
                  :key="addr.addressId"
                  class="address-card"
                  :class="{ active: formState.addressId === addr.addressId }"
                  @click="onChangeAddress(addr.addressId)"
                >
                  <div class="addr-top">
                    <span class="consignee">{{ addr.consignee }}</span>
                    <span class="mobile">{{ addr.mobile }}</span>
                  </div>
                  <div class="addr-text">{{ addr.regionName }} {{ addr.address }}</div>
                </div>
              </div>
            </template>
          </section>

          <!-- 支付方式 -->
          <section v-if="paymentTypeList.length" class="check-card">
            <div class="card-title">支付方式</div>
            <div class="pay-type-list">
              <div
                v-for="item in paymentTypeList"
                :key="item.typeId"
                class="pay-type-item"
                :class="{ active: formState.payTypeId === item.typeId, disabled: item.disabled }"
                @click="!item.disabled && onChangePayType(item.typeId)"
              >
                <span class="pay-name">{{ item.typeName }}</span>
                <span v-if="item.disabled" class="pay-disabled">{{ item.disabledDesc || '不可用' }}</span>
              </div>
            </div>
          </section>

          <!-- 店铺商品 + 配送方式 -->
          <template v-if="cartList.length">
            <section v-for="store in cartList" :key="store.shopId" class="check-card">
              <div class="shop-name">{{ store.shopId === 0 ? '自营' : `商家：${store.shopTitle}` }}</div>

              <div class="delivery-row">
                <span class="delivery-label">配送</span>
                <template v-if="store.noShipping === 1">
                  <div class="delivery-tags">
                    <div class="delivery-tab active">无需配送</div>
                  </div>
                </template>
                <template v-else-if="shippingTypeData[store.shopId]?.length">
                  <div class="delivery-tags">
                    <div
                      v-for="item in shippingTypeData[store.shopId]"
                      :key="item.shippingTypeId"
                      class="delivery-tab"
                      :class="{ active: formState.shippingType[store.shopId]?.typeId === item.shippingTypeId }"
                      @click="onChangeShippingType(store.shopId, item)"
                    >
                      {{ item.shippingTypeName }}
                    </div>
                  </div>
                </template>
                <template v-else>
                  <span class="err-info">该收货地址不支持配送</span>
                </template>
              </div>

              <div class="goods-list">
                <div v-for="product in store.carts" :key="product.cartId" class="goods-item">
                  <router-link :to="`/product/${product.productId}`" class="goods-pic">
                    <img :src="imageUrl(product.picThumb)" :alt="product.productName || ''" />
                  </router-link>
                  <div class="goods-describe">
                    <div class="goods-name">
                      <router-link :to="`/product/${product.productId}`">{{ product.productName }}</router-link>
                    </div>
                    <div class="goods-sku">
                      <span v-for="(sku, index) in skuPairs(product)" :key="index">{{ sku.name }}：{{ sku.value }}</span>
                    </div>
                  </div>
                  <div class="goods-price">¥{{ formatPrice(product.price) }}</div>
                  <div class="goods-qty">x{{ product.quantity }}</div>
                </div>

                <div v-if="store.gift?.length" class="gift-box">
                  <div class="gift-title">赠品</div>
                  <div class="gift-list">
                    <div v-for="sub in store.gift" :key="sub.giftId" class="gift-item">
                      <img :src="imageUrl(sub.picThumb || sub.productPicThumb)" :alt="sub.productName || ''" />
                      <div class="gift-name">{{ sub.productName }}</div>
                      <div class="gift-num">x{{ sub.num }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </template>

          <!-- 订单备注 -->
          <section class="check-card">
            <div class="card-title">订单备注</div>
            <el-input
              v-model="formState.buyerNote"
              maxlength="50"
              show-word-limit
              placeholder="提示：请勿填写有关支付、收货、发票方面的信息"
            />
          </section>

          <!-- 优惠券 -->
          <section class="check-card">
            <div class="card-title">
              优惠券
              <el-switch v-model="useCoupon" active-text="使用优惠券" @change="onCouponSwitch" />
            </div>
            <template v-if="useCoupon">
              <div v-if="couponList.enableCoupons.length === 0" class="muted">暂无可用优惠券</div>
              <template v-else>
                <div v-if="platformCoupons.length" class="coupon-group">
                  <div class="coupon-group-title">平台券</div>
                  <div class="coupon-grid">
                    <div
                      v-for="coupon in platformCoupons"
                      :key="coupon.userCouponId"
                      class="coupon-card"
                      :class="{ selected: selectedUserCouponIds.includes(coupon.userCouponId) }"
                      @click="toggleCoupon(coupon.userCouponId)"
                    >
                      <div class="coupon-money">
                        <span class="symbol">¥</span>{{ formatPrice(coupon.couponMoney) }}
                      </div>
                      <div class="coupon-info">
                        <div class="coupon-name">{{ coupon.couponName }}</div>
                        <div class="coupon-desc">{{ coupon.couponDesc }}</div>
                        <div class="coupon-limit">满 ¥{{ formatPrice(coupon.minOrderAmount) }} 可用</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="storeCoupons.length" class="coupon-group">
                  <div class="coupon-group-title">店铺券</div>
                  <div class="coupon-grid">
                    <div
                      v-for="coupon in storeCoupons"
                      :key="coupon.userCouponId"
                      class="coupon-card"
                      :class="{ selected: selectedUserCouponIds.includes(coupon.userCouponId) }"
                      @click="toggleCoupon(coupon.userCouponId)"
                    >
                      <div class="coupon-money">
                        <span class="symbol">¥</span>{{ formatPrice(coupon.couponMoney) }}
                      </div>
                      <div class="coupon-info">
                        <div class="coupon-name">{{ coupon.couponName }}</div>
                        <div class="coupon-desc">{{ coupon.couponDesc }}</div>
                        <div class="coupon-limit">
                          满 ¥{{ formatPrice(coupon.minOrderAmount) }} 可用 · 店铺 {{ coupon.shopId }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="couponList.disableCoupons.length" class="muted">
                  有 {{ couponList.disableCoupons.length }} 张券不满足当前订单使用条件
                </div>
              </template>
            </template>
            <div v-else class="muted">已选择不使用优惠券</div>
          </section>

          <!-- 积分 -->
          <section v-if="availablePoints > 0" class="check-card">
            <div class="card-title">
              使用积分
              <span class="card-tip">当前账户共 <span class="font-color">{{ availablePoints }}</span> 积分</span>
            </div>
            <div class="points-row">
              <span class="row-label">使用积分</span>
              <el-input
                v-model.number="inputPoint"
                type="number"
                :min="0"
                :max="availablePoints"
                style="width: 200px"
                @change="onUsePointChange"
              />
              <span v-if="Number(total.pointsAmount) > 0" class="points-tip">
                共抵扣 <span class="font-color">¥{{ formatPrice(total.pointsAmount) }}</span>
              </span>
              <span class="points-tip">该订单最多可用 {{ availablePoints }} 积分</span>
            </div>
          </section>

          <!-- 余额 -->
          <section class="check-card">
            <div class="card-title">余额</div>
            <div class="balance-row">
              <el-checkbox :model-value="formState.useBalance > 0" @change="onUseBalance">
                使用余额
              </el-checkbox>
              <span class="card-tip">当前账户余额 <span class="font-color">¥{{ formatPrice(balanceAvailable) }}</span></span>
              <span v-if="formState.useBalance > 0" class="card-tip">
                支付 ¥{{ formatPrice(total.balance) }}
              </span>
            </div>
          </section>

          <!-- 发票 -->
          <section class="check-card">
            <div class="card-title">
              发票信息
              <span class="card-tip">开企业抬头发票须填写纳税人识别号，以免影响报销</span>
            </div>
            <div class="invoice-row">
              <div class="invoice-title">
                {{ invoiceTitleText }}
                <el-tooltip
                  v-if="invoiceForm.invoiceType === 1"
                  content="电子普通发票与纸质发票具有同等法律效力，可支持报销入账、商品售后凭证。"
                  placement="bottom-start"
                  effect="light"
                >
                  <el-icon class="tip-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
              <div class="invoice-type" v-if="invoiceForm.invoiceType === 1">
                {{ invoiceForm.titleType === 1 ? '个人' : '企业' }}
              </div>
              <div class="invoice-company">{{ invoiceForm.companyName || '' }}</div>
              <el-button link type="primary" @click="invoiceDialogVisible = true">修改</el-button>
            </div>
          </section>

          <!-- 账期支付 -->
          <section v-if="creditPaymentInfo?.canUseCredit" class="check-card">
            <div class="card-title">账期</div>
            <div class="credit-row" :class="{ 'credit-disabled': creditDisabled }">
              <el-checkbox :disabled="creditDisabled" :model-value="formState.useCredit === 1" @change="onUseCredit">
                账期支付
              </el-checkbox>
              <span v-if="creditDisabledText" class="font-color">{{ creditDisabledText }}</span>
              <span v-else class="card-tip">
                当前可用额度 <span class="font-color">¥{{ formatPrice(creditPaymentInfo?.availableAmount) }}</span>
              </span>
            </div>
          </section>
        </div>

        <!-- 右侧：订单详情 -->
        <aside class="money-info">
          <div class="money-title">订单详情</div>

          <div v-if="Number(couponExcludeAmount) > 0 || Number(pointsExcludeAmount) > 0" class="wholesale-exclude-tip">
            <div v-if="Number(couponExcludeAmount) > 0">
              <span class="tip-icon-mark">!</span>批发商品不参与店铺优惠券，已排除 ¥{{ formatPrice(couponExcludeAmount) }}
            </div>
            <div v-if="Number(pointsExcludeAmount) > 0">
              <span class="tip-icon-mark">!</span>批发商品不赠送积分，已排除 ¥{{ formatPrice(pointsExcludeAmount) }} 对应积分
            </div>
          </div>

          <div class="total-list">
            <div class="total-row"><span>商品总价：</span><span>¥{{ formatPrice(total.productAmount) }}</span></div>
            <div v-if="Number(total.serviceFee) > 0" class="total-row">
              <span>附加费用：</span><span>¥{{ formatPrice(total.serviceFee) }}</span>
            </div>
            <div v-if="Number(total.balance) > 0" class="total-row discount-row">
              <span>使用余额：</span><span>- ¥{{ formatPrice(total.balance) }}</span>
            </div>
            <div v-if="Number(total.pointsAmount) > 0" class="total-row discount-row">
              <span>积分减免：</span><span>- ¥{{ formatPrice(total.pointsAmount) }}</span>
            </div>
            <div v-if="Number(total.discounts) > 0" class="total-row discount-row">
              <span>共优惠：</span><span>- ¥{{ formatPrice(total.discounts) }}</span>
            </div>
            <div v-if="Number(total.discountCouponAmount) > 0" class="total-row discount-row">
              <span>优惠券：</span><span>- ¥{{ formatPrice(total.discountCouponAmount) }}</span>
            </div>
            <div v-if="Number(total.discountDiscountAmount) > 0" class="total-row discount-row">
              <span>其他优惠：</span><span>- ¥{{ formatPrice(total.discountDiscountAmount) }}</span>
            </div>
            <div v-if="needShipping" class="total-row">
              <span>
                快递：
                <el-popover placement="left" width="330" trigger="hover">
                  <template #reference>
                    <el-icon class="freight-icon"><InfoFilled /></el-icon>
                  </template>
                  <div class="freight-detail">
                    <div class="freight-title">运费明细</div>
                    <div v-for="store in cartList" :key="store.shopId" class="freight-shop">
                      <div class="freight-shop-name">{{ store.shopId === 0 ? '自营' : store.shopTitle }}</div>
                      <div class="freight-shop-fee">
                        快递：
                        <span v-if="Number(total.storeShippingFee?.[store.shopId]) > 0">
                          ¥{{ formatPrice(total.storeShippingFee[store.shopId]) }}
                        </span>
                        <span v-else>包邮</span>
                      </div>
                    </div>
                  </div>
                </el-popover>
              </span>
              <span v-if="Number(total.shippingFee) > 0">¥{{ formatPrice(total.shippingFee) }}</span>
              <span v-else class="free-text">包邮</span>
            </div>
            <div v-if="Number(total.taxRateAmount) > 0" class="total-row">
              <span>
                税费：
                <el-tooltip placement="left" effect="light">
                  <template #content>
                    <div class="tax-tip">
                      <div>税费说明</div>
                      <div>税费按所选地址所在国家/地区政策计算。</div>
                      <div>选择地址后，系统自动计算税费，结算页显示税费金额。</div>
                      <div>税费会随订单一起支付，由商城统一代缴。</div>
                    </div>
                  </template>
                  <el-icon class="freight-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </span>
              <span>¥{{ formatPrice(total.taxRateAmount) }}</span>
            </div>
            <div v-if="Number(total.discountAfter) > 0" class="total-row">
              <span>合计：</span><span>¥{{ formatPrice(total.discountAfter) }}</span>
            </div>
          </div>

          <div v-if="Number(total.orderSendPoint) > 0" class="order-point-tip">
            *该订单完成后，您将预计获得 {{ total.orderSendPoint }} 积分
          </div>

          <div class="total-fee">
            <span>仍需支付：</span>
            <span class="price">¥{{ formatPrice(payAmount) }}</span>
          </div>

          <div class="submit">
            <el-button
              type="primary"
              size="large"
              class="buy-btn"
              :disabled="submitDisabled"
              :loading="submitLoading"
              @click="onSubmit"
            >
              {{ isDepositPresale ? '支付定金' : '立即下单' }}
            </el-button>
          </div>
        </aside>
      </div>
    </div>

    <!-- 发票弹窗 -->
    <el-dialog v-model="invoiceDialogVisible" title="发票信息" width="560px">
      <el-form :model="invoiceForm" label-width="90px">
        <el-form-item label="发票类型">
          <el-radio-group v-model="invoiceForm.invoiceType">
            <el-radio :value="0">不开发票</el-radio>
            <el-radio :value="1">电子普通发票</el-radio>
            <el-radio :value="2">增值税专用发票</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          v-if="invoiceForm.invoiceType === 2 && !invoiceQualificationApproved"
          type="warning"
          :closable="false"
          show-icon
          class="invoice-qualification-alert"
        >
          <template #title>
            增值税专用发票需先完成
            <router-link to="/member/invoice" class="font-color">增票资质认证</router-link>
            （当前状态：{{ invoiceQualificationStatus === null ? '未查询' : invoiceQualificationStatus === 1 ? '审核通过' : invoiceQualificationStatus === 2 ? '待审核' : invoiceQualificationStatus === 3 ? '审核未通过' : '未申请' }}）
          </template>
        </el-alert>
        <template v-if="invoiceForm.invoiceType">
          <el-form-item label="抬头类型">
            <el-radio-group v-model="invoiceForm.titleType">
              <el-radio :value="1">个人</el-radio>
              <el-radio :value="2">企业</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="发票抬头">
            <el-input v-model="invoiceForm.companyName" maxlength="60" placeholder="请输入个人姓名或企业名称" />
          </el-form-item>
          <el-form-item v-if="invoiceForm.invoiceType === 2 || invoiceForm.titleType === 2" label="纳税人识别号">
            <el-input v-model="invoiceForm.taxNo" maxlength="30" placeholder="请输入纳税人识别号" />
          </el-form-item>
          <el-form-item label="发票内容">
            <el-input v-model="invoiceForm.content" maxlength="40" placeholder="商品明细 / 商品类别" />
          </el-form-item>
          <el-form-item label="接收邮箱">
            <el-input v-model="invoiceForm.email" maxlength="60" placeholder="电子发票接收邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="invoiceForm.mobile" maxlength="20" placeholder="选填" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="invoiceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveInvoice">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 订单结算页（对齐原版 `pages/order/check.vue`）。
 *
 * 结构：收货地址 → 支付方式 → 店铺商品（含配送方式切换）→ 订单备注 →
 * 优惠券（平台券/店铺券）→ 积分 → 余额 → 发票 → 账期；右侧为订单详情汇总。
 * 接口全部来自 Rust 契约：`/order/check/{index,update,updateCoupon,submit,getStoreShippingType,getInvoice,getAvailablePaymentType}`。
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { InfoFilled } from '@element-plus/icons-vue';
import { addressApi, invoiceQualificationApi, orderApi } from '@/api';
import { pageItems } from '@/api/types';
import type {
  AddressVO,
  CheckCoupon,
  CreditPaymentInfo,
  InvoiceData,
  PaymentType,
  ShippingType,
  ShippingTypeMap,
  SelectedShippingType,
} from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { formatPrice, imageUrl } from '@/utils/format';

const router = useRouter();
const user = useUserStore();
const cart = useCartStore();

interface CheckFormState {
  addressId: number;
  shippingType: Record<string, SelectedShippingType>;
  payTypeId: number;
  usePoint: number;
  useBalance: number;
  useCredit: number;
  useCouponIds: number[];
  selectUserCouponIds: number[];
  useDefaultCouponIds: number;
  buyerNote: string;
}

const loading = ref(true);
const submitLoading = ref(false);
const checkLoading = ref(false);
const addressList = ref<AddressVO[]>([]);
const showMoreAddress = ref(false);
const paymentTypeList = ref<PaymentType[]>([]);
const shippingTypeData = ref<ShippingTypeMap>({});
const cartList = ref<any[]>([]);
const total = ref<Record<string, any>>({});
const couponList = ref<{ enableCoupons: CheckCoupon[]; disableCoupons: any[] }>({
  enableCoupons: [],
  disableCoupons: [],
});
const availablePoints = ref(0);
const creditPaymentInfo = ref<CreditPaymentInfo | null>(null);
const couponExcludeAmount = ref('');
const pointsExcludeAmount = ref('');
const isDepositPresale = ref(false);

const formState = reactive<CheckFormState>({
  addressId: 0,
  shippingType: {},
  payTypeId: 0,
  usePoint: 0,
  useBalance: 0,
  useCredit: 0,
  useCouponIds: [],
  selectUserCouponIds: [],
  useDefaultCouponIds: 0,
  buyerNote: '',
});

const useCoupon = ref(true);
const inputPoint = ref(0);

// 发票
const invoiceDialogVisible = ref(false);
const invoiceForm = reactive({
  invoiceType: 0,
  titleType: 1,
  companyName: '',
  taxNo: '',
  content: '商品明细',
  email: '',
  mobile: '',
});
/** 增票资质（与 `/member/invoice` 页联动）：0 未申请 / 1 通过 / 2 待审 / 3 未通过。 */
const invoiceQualificationStatus = ref<number | null>(null);
const invoiceQualificationApproved = computed(() => Number(invoiceQualificationStatus.value) === 1);

const selectedAddress = computed(() =>
  addressList.value.find((addr) => addr.addressId === formState.addressId),
);
const selectedUserCouponIds = computed(() => formState.selectUserCouponIds);
const platformCoupons = computed(() => couponList.value.enableCoupons.filter((c) => c.isGlobal));
const storeCoupons = computed(() => couponList.value.enableCoupons.filter((c) => !c.isGlobal));
const balanceAvailable = computed(() => Number(total.value.balance ?? 0));
const needShipping = computed(() => cartList.value.some((store) => store.noShipping === 0));
const payAmount = computed(() =>
  isDepositPresale.value
    ? Number(total.value.actualPayDepositAmount ?? total.value.unpaidAmount ?? 0)
    : Number(total.value.unpaidAmount ?? 0),
);
const invoiceTitleText = computed(() => {
  if (!invoiceForm.invoiceType) return '不开发票';
  return invoiceForm.invoiceType === 1 ? '电子普通发票' : '增值税专用发票';
});
const creditDisabled = computed(() => [2, 3].includes(Number(creditPaymentInfo.value?.creditPaymentStatus ?? 0)));
const creditDisabledText = computed(() => {
  if (Number(creditPaymentInfo.value?.creditPaymentStatus) === 2) {
    return '您有账期逾期待结算订单，敬请及时处理';
  }
  if (Number(creditPaymentInfo.value?.creditPaymentStatus) === 3) {
    return '账期支付功能已关闭，详情请咨询平台方';
  }
  return '';
});
const submitDisabled = computed(() => {
  if (!addressList.value.length || formState.addressId === 0) return true;
  if (formState.payTypeId === 0) return true;
  for (const store of cartList.value) {
    if (store.noShipping === 0 && !formState.shippingType[store.shopId]) return true;
  }
  return false;
});

/** 规格数组转 name/value 展示（后端 `skuData` 为数组）。 */
function skuPairs(product: Record<string, any>): Array<{ name: string; value: string }> {
  const raw = product.skuData;
  if (!Array.isArray(raw)) return [];
  return raw
    .map((item: any) => ({ name: String(item?.name ?? ''), value: String(item?.value ?? '') }))
    .filter((item) => item.name || item.value);
}

function requestParam() {
  return {
    flowType: 1,
    addressId: formState.addressId || undefined,
    shippingType: formState.shippingType,
    payTypeId: formState.payTypeId || undefined,
    usePoint: formState.usePoint,
    useBalance: formState.useBalance,
    useCredit: formState.useCredit,
    useCouponIds: formState.useCouponIds,
    selectUserCouponIds: formState.selectUserCouponIds,
    useDefaultCouponIds: formState.useDefaultCouponIds,
    buyerNote: formState.buyerNote || undefined,
    invoiceData: buildInvoiceData() ?? undefined,
  };
}

function buildInvoiceData(): InvoiceData | null {
  if (!invoiceForm.invoiceType) return null;
  return {
    invoiceType: invoiceForm.invoiceType,
    titleType: invoiceForm.titleType,
    title: invoiceForm.companyName || undefined,
    companyName: invoiceForm.companyName || undefined,
    taxNo: invoiceForm.taxNo || undefined,
    content: invoiceForm.content || undefined,
    email: invoiceForm.email || undefined,
    mobile: invoiceForm.mobile || undefined,
  };
}

function syncCheckResult(result: any, assignForm = false) {
  if (!result) return;
  if (assignForm && result.item) {
    const item = result.item;
    if (item.addressId) formState.addressId = Number(item.addressId);
    if (item.shippingType && typeof item.shippingType === 'object') {
      formState.shippingType = item.shippingType;
    }
    if (item.payTypeId) formState.payTypeId = Number(item.payTypeId);
    if (typeof item.usePoint === 'number') {
      formState.usePoint = item.usePoint;
      inputPoint.value = item.usePoint;
    }
    if (item.useBalance !== undefined && item.useBalance !== null) {
      formState.useBalance = Number(item.useBalance);
    }
    if (typeof item.useCredit === 'number') formState.useCredit = item.useCredit;
    if (Array.isArray(item.useCouponIds)) formState.useCouponIds = item.useCouponIds;
    if (Array.isArray(item.selectUserCouponIds)) formState.selectUserCouponIds = item.selectUserCouponIds;
  }
  cartList.value = Array.isArray(result.cartList) ? result.cartList : [];
  total.value = result.total ?? {};
  couponList.value = result.couponList ?? { enableCoupons: [], disableCoupons: [] };
  availablePoints.value = Number(result.availablePoints ?? 0);
  creditPaymentInfo.value = result.creditPaymentInfo ?? null;
  couponExcludeAmount.value = result.couponExcludeAmount ?? '';
  pointsExcludeAmount.value = result.pointsExcludeAmount ?? '';
  isDepositPresale.value = result.productPresale?.saleMode === 2;
  if (result.useCouponIds?.length) {
    formState.useCouponIds = result.useCouponIds;
    formState.selectUserCouponIds = result.selectUserCouponIds ?? [];
  }
}

async function loadAddresses() {
  const page = await addressApi.list();
  addressList.value = pageItems(page);
  const preferred =
    addressList.value.find((addr) => addr.isSelected === 1) ??
    addressList.value.find((addr) => addr.isDefault === 1) ??
    addressList.value[0];
  if (preferred && !formState.addressId) {
    formState.addressId = preferred.addressId;
  }
}

async function loadPaymentTypes() {
  try {
    paymentTypeList.value = await orderApi.paymentTypes(1);
    if (paymentTypeList.value.length) {
      formState.payTypeId = paymentTypeList.value[0].typeId;
    }
  } catch {
    paymentTypeList.value = [];
  }
}

async function loadShippingTypes() {
  try {
    shippingTypeData.value = await orderApi.storeShippingType({
      flowType: 1,
      addressId: formState.addressId || undefined,
      payTypeId: formState.payTypeId || undefined,
    });
    // 默认选中每个店铺的第一个配送方式（对齐原版）
    for (const [key, list] of Object.entries(shippingTypeData.value)) {
      if (list?.length && !formState.shippingType[key]) {
        formState.shippingType[key] = {
          typeId: list[0].shippingTypeId,
          shopId: list[0].shopId ?? Number(key),
          typeName: list[0].shippingTypeName ?? '',
        };
      }
    }
  } catch {
    shippingTypeData.value = {};
  }
}

async function getOrderCheck() {
  checkLoading.value = true;
  try {
    const result = await orderApi.checkIndex(requestParam());
    syncCheckResult(result, true);
  } catch (err: any) {
    ElMessage.error(err?.message || '结算数据加载失败');
  } finally {
    checkLoading.value = false;
  }
}

async function updateOrderCheck() {
  checkLoading.value = true;
  try {
    const result = await orderApi.checkUpdate(requestParam());
    syncCheckResult(result, true);
    return result;
  } catch (err: any) {
    ElMessage.error(err?.message || '结算数据更新失败');
    return null;
  } finally {
    checkLoading.value = false;
  }
}

async function onChangeAddress(addressId: number) {
  formState.addressId = addressId;
  showMoreAddress.value = false;
  await loadShippingTypes();
  await updateOrderCheck();
  await fetchInvoice();
}

async function onChangePayType(typeId: number) {
  if (formState.payTypeId === typeId) return;
  formState.payTypeId = typeId;
  await updateOrderCheck();
}

async function onChangeShippingType(shopId: number, data: ShippingType) {
  const current = formState.shippingType[shopId];
  if (current?.typeId === data.shippingTypeId) return;
  formState.shippingType[shopId] = {
    typeId: data.shippingTypeId,
    shopId,
    typeName: data.shippingTypeName ?? '',
  };
  await updateOrderCheck();
}

function onUsePointChange() {
  inputPoint.value = Math.floor(Number(inputPoint.value) || 0);
  if (inputPoint.value > availablePoints.value) inputPoint.value = availablePoints.value;
  if (inputPoint.value < 0) inputPoint.value = 0;
  formState.usePoint = inputPoint.value;
  updateOrderCheck();
}

function onUseBalance() {
  formState.useBalance = formState.useBalance > 0 ? 0 : balanceAvailable.value;
  if (formState.useBalance > 0) formState.useCredit = 0;
  updateOrderCheck();
}

function onUseCredit() {
  if (!creditPaymentInfo.value?.canUseCredit) return;
  const next = formState.useCredit === 1 ? 0 : 1;
  if (next === 1 && Number(creditPaymentInfo.value.availableAmount ?? 0) <= 0) return;
  formState.useCredit = next;
  if (next === 1) formState.useBalance = 0;
  updateOrderCheck();
}

function onCouponSwitch(checked: boolean | string | number) {
  const enabled = Boolean(checked);
  formState.useDefaultCouponIds = enabled ? 1 : 0;
  if (!enabled) {
    formState.useCouponIds = [];
    formState.selectUserCouponIds = [];
  }
  updateCoupon();
}

function toggleCoupon(userCouponId: number) {
  const next = new Set(formState.selectUserCouponIds);
  if (next.has(userCouponId)) next.delete(userCouponId);
  else next.add(userCouponId);
  formState.selectUserCouponIds = Array.from(next);
  formState.useDefaultCouponIds = 0;
  updateCoupon();
}

async function updateCoupon() {
  checkLoading.value = true;
  try {
    const result = await orderApi.updateCoupon(requestParam());
    syncCheckResult(result, false);
  } catch (err: any) {
    ElMessage.error(err?.message || '更新优惠券失败');
  } finally {
    checkLoading.value = false;
  }
}

async function fetchInvoice() {
  try {
    const invoice = await orderApi.getInvoice({ invoiceType: invoiceForm.invoiceType || undefined });
    if (invoice) {
      invoiceForm.invoiceType = invoice.invoiceType || invoiceForm.invoiceType;
      invoiceForm.titleType = invoice.titleType || invoiceForm.titleType;
      invoiceForm.companyName = invoice.companyName || invoiceForm.companyName;
      invoiceForm.taxNo = invoice.companyCode || invoiceForm.taxNo;
      invoiceForm.content = invoice.invoiceContent || invoiceForm.content;
      invoiceForm.email = invoice.email || invoiceForm.email;
      invoiceForm.mobile = invoice.mobile || invoiceForm.mobile;
    }
  } catch {
    // 无历史发票信息时保持空表单
  }
}

function saveInvoice() {
  if (invoiceForm.invoiceType === 2 && !invoiceQualificationApproved.value) {
    ElMessage.warning('增值税专用发票需先完成增票资质认证，正在前往认证页面');
    invoiceDialogVisible.value = false;
    router.push('/member/invoice');
    return;
  }
  invoiceDialogVisible.value = false;
  updateOrderCheck();
}

/** 加载增票资质状态与资质信息（专票校验 + 自动带入企业抬头）。 */
async function loadInvoiceQualification() {
  try {
    const [status, detail] = await Promise.all([
      invoiceQualificationApi.status().catch(() => null),
      invoiceQualificationApi.detail().catch(() => null),
    ]);
    invoiceQualificationStatus.value = status === null ? (detail?.status ?? null) : Number(status);
    if (detail && invoiceForm.invoiceType === 2) {
      invoiceForm.companyName = detail.companyName || invoiceForm.companyName;
      invoiceForm.taxNo = detail.companyCode || invoiceForm.taxNo;
      invoiceForm.titleType = 2;
      invoiceForm.email = detail.email || invoiceForm.email;
      invoiceForm.mobile = detail.mobile || invoiceForm.mobile;
    }
  } catch {
    invoiceQualificationStatus.value = null;
  }
}

async function onSubmit() {
  if (submitDisabled.value) return;
  if (invoiceForm.invoiceType === 2 && !invoiceQualificationApproved.value) {
    ElMessage.warning('增值税专用发票需先完成增票资质认证，正在前往认证页面');
    router.push('/member/invoice');
    return;
  }
  submitLoading.value = true;
  try {
    const result = await orderApi.submit({
      cartIds: cart.items.filter((item) => item.checked).map((item) => item.cartId),
      addressId: formState.addressId,
      payTypeId: formState.payTypeId,
      usePoint: formState.usePoint,
      useBalance: formState.useBalance,
      useCredit: formState.useCredit,
      useCouponIds: formState.useCouponIds,
      buyerNote: formState.buyerNote || undefined,
      invoiceData: buildInvoiceData() ?? undefined,
      shippingType: formState.shippingType,
    } as any);
    await cart.fetchSummary(user.token);
    await cart.fetchCount(user.token);
    if (Number(result.returnType) === 2) {
      router.replace('/member/orders');
    } else {
      router.replace({ path: '/pay/result', query: { orderId: String(result.orderId) } });
    }
  } finally {
    submitLoading.value = false;
  }
}

onMounted(async () => {
  applySeo({ title: '订单结算页', noindex: true });
  loading.value = true;
  try {
    await cart.fetchSummary(user.token);
    await loadAddresses();
    await Promise.all([loadPaymentTypes(), loadShippingTypes()]);
    await getOrderCheck();
    await fetchInvoice();
    await loadInvoiceQualification();
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.tig-checkout-page {
  background: #f5f6fa;
  padding: 0 0 50px;
  min-height: 60vh;
}

.page-title-bar {
  padding: 24px 0 16px;
  .page-title { font-size: 22px; font-weight: 700; }
}

.check-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.order-confirm-con {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.check-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;

  .card-title {
    display: flex;
    align-items: center;
    gap: 14px;
    font-size: 15px;
    font-weight: 700;
    color: #333;
    margin-bottom: 12px;

    .card-tip { font-size: 12px; font-weight: 400; color: #999; }
    .el-switch { margin-left: auto; }
  }
}

.address-empty {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #888;
  font-size: 13px;
}

.selected-address {
  border: 1px solid #ffdcdc;
  background: #fff7f6;
  border-radius: 6px;
  padding: 12px 14px;

  .addr-main {
    display: flex;
    align-items: center;
    gap: 12px;
    font-weight: 700;
    .mobile { color: #888; font-size: 13px; font-weight: 400; }
  }
  .addr-detail { color: #555; font-size: 13px; margin-top: 6px; }
  .addr-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 4px; }
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 12px;
}

.address-card {
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  padding: 10px 12px;
  cursor: pointer;
  font-size: 13px;

  &.active { border-color: var(--tig-red); background: #fff7f6; }
  .addr-top { display: flex; gap: 10px; font-weight: 600; .mobile { color: #888; font-weight: 400; } }
  .addr-text { color: #666; margin-top: 6px; line-height: 1.5; }
}

.pay-type-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pay-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;

  &.active { border-color: var(--tig-red); color: var(--tig-red); background: #fff7f6; font-weight: 700; }
  &.disabled { color: #bbb; cursor: not-allowed; }
  .pay-disabled { font-size: 11px; color: #bbb; margin-top: 2px; }
}

.shop-name {
  font-size: 14px;
  font-weight: 700;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f2f2;
}

.delivery-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;

  .delivery-label { color: #888; font-size: 13px; }
  .delivery-tags { display: flex; gap: 8px; flex-wrap: wrap; }
  .delivery-tab {
    padding: 4px 14px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    &.active { border-color: var(--tig-red); color: var(--tig-red); background: #fff7f6; }
  }
  .err-info { color: var(--tig-red); font-size: 13px; }
}

.goods-list { border-top: 1px solid #f7f7f7; }

.goods-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed #f5f5f5;

  .goods-pic img {
    width: 72px;
    height: 72px;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #f0f0f0;
  }

  .goods-describe {
    flex: 1;
    min-width: 0;

    .goods-name {
      font-size: 13px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      a:hover { color: var(--tig-red); }
    }
    .goods-sku {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      color: #999;
      font-size: 12px;
      margin-top: 6px;
    }
  }

  .goods-price { width: 100px; text-align: right; font-size: 14px; color: #333; }
  .goods-qty { width: 60px; text-align: right; color: #888; font-size: 13px; }
}

.gift-box {
  padding: 10px 0 4px;

  .gift-title { font-size: 13px; color: #888; margin-bottom: 8px; }
  .gift-list { display: flex; gap: 12px; flex-wrap: wrap; }
  .gift-item {
    display: flex;
    align-items: center;
    gap: 8px;
    background: #fafafa;
    border-radius: 6px;
    padding: 6px 10px;
    font-size: 12px;

    img { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
    .gift-name { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .gift-num { color: #999; }
  }
}

.muted { color: #999; font-size: 13px; }

.coupon-group { margin-bottom: 12px; }
.coupon-group-title { font-size: 13px; color: #666; margin-bottom: 8px; font-weight: 600; }

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.coupon-card {
  display: flex;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid #ffe1df;
  border-radius: 6px;
  background: #fffaf9;
  cursor: pointer;

  &.selected { border-color: var(--tig-red); box-shadow: 0 0 0 1px var(--tig-red) inset; }

  .coupon-money { color: var(--tig-red); font-size: 20px; font-weight: 800; min-width: 74px; .symbol { font-size: 12px; } }
  .coupon-info {
    min-width: 0;
    .coupon-name { font-size: 13px; font-weight: 600; }
    .coupon-desc, .coupon-limit { font-size: 12px; color: #999; margin-top: 2px; }
  }
}

.points-row, .balance-row, .credit-row {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: #666;

  .row-label { color: #888; }
  .points-tip { color: #999; font-size: 12px; }
}

.invoice-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #555;

  .invoice-title { display: flex; align-items: center; gap: 6px; font-weight: 600; }
  .tip-icon { color: #b0b0b0; cursor: help; }
  .invoice-type { color: #888; }
  .invoice-company { color: #333; }
}

.font-color { color: var(--tig-red); }

.credit-disabled { opacity: 0.6; }

.money-info {
  width: 320px;
  flex: 0 0 320px;
  position: sticky;
  top: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;

  .money-title { font-size: 15px; font-weight: 700; margin-bottom: 12px; }
}

.wholesale-exclude-tip {
  color: #dd6b20;
  background: #fff8f1;
  border: 1px solid #ffd9b3;
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 12px;
  font-size: 12px;
  line-height: 1.6;

  .tip-icon-mark {
    display: inline-block;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: currentColor;
    color: #fff;
    text-align: center;
    line-height: 14px;
    font-size: 11px;
    font-weight: bold;
    margin-right: 4px;
  }
}

.total-list {
  .total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    color: #666;
    padding: 5px 0;

    &.discount-row { color: #999; }
    .freight-icon { color: #aaa; cursor: help; margin-left: 4px; }
    .free-text { color: #999; }
  }
}

.freight-detail {
  .freight-title { font-weight: 700; margin-bottom: 8px; font-size: 13px; }
  .freight-shop { font-size: 12px; color: #666; margin-bottom: 6px; }
  .freight-shop-name { font-weight: 600; color: #333; }
}

.tax-tip { font-size: 12px; line-height: 1.8; }

.order-point-tip { color: #999; font-size: 12px; margin-top: 8px; }

.total-fee {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  border-top: 1px solid #f0f0f0;
  margin-top: 10px;
  padding-top: 12px;
  font-size: 14px;
  color: #333;

  .price { color: var(--tig-red); font-size: 24px; font-weight: 800; }
}

.submit {
  margin-top: 16px;
  .buy-btn { width: 100%; font-weight: 700; }
}
</style>
