<template>
    <div class="container">
        <div class="">
            <div v-show="step === 'index'" class="proxy-shopping-index content_wrapper"
                style="position: relative; height: 100%">
                <div class="page-body">
                    <Alert class="notice-alert" type="warning">
                        <template #message>{{ $t('注意事项：') }}</template>
                        <template #description>
                            <ol class="notice-list">
                                <li>{{ $t('1、代客下单存在支付金额与用户自主下单金额不一致的风险，请仔细核对订单金额，确保交易金额准确无误。') }}</li>
                                <li>{{ $t('2、订单支付金额可在商品列表中进行修改。') }}</li>
                                <li v-if="isStore()">
                                </li>
                                <li v-else>{{ $t('3、虚拟商品、卡密商品、付费内容、礼品卡只能单独购买。') }}</li>
                            </ol>
                        </template>
                    </Alert>
                    <div class="user-row">
                        <div v-if="selectedUserInfo?.userId" class="sticky-user">
                            <div class="user-info-card">
                                <div class="info-card">
                                    <div class="user-info-card-left">
                                        <el-avatar :size="64" :src="imageFormat(selectedUserInfo?.avatar)"></el-avatar>
                                        <div class="card-info">
                                            <div class="name">
                                                {{ selectedUserInfo?.nickname || selectedUserInfo?.username || "-" }}
                                            </div>
                                            <div>{{ $t('手机号：') }}<MobileCard :mobile="selectedUserInfo?.mobile"></MobileCard>
                                            </div>
                                            <div>
                                                <span class="label">{{ $t('可用余额：') }}</span>
                                                <span class="value">{{ priceFormat(Number(selectedUserInfo?.balance ||
                                                    0)) }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="user-row-left">
                            <DialogForm width="520px" v-model:visible="userDialogVisible"
                                path="order/proxyShopping/src/SelectUserDialog" :title="$t('选择下单会员')"
                                :params="{ userId: selectedUserId, showTips: false }" @okCallback="onUserDialogOk">
                                <el-button type="primary">{{ selectedUserInfo?.userId ? $t('重新选择') : $t('选择下单会员') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>

                    <div class="two-col">
                        <!-- 左：商品列表 -->
                        <div class="panel">
                            <div class="panel-head">
                                <div class="panel-title">{{ $t('全部商品') }}</div>
                                <div class="panel-tools list-table-tool lyecs-search-warp">
                                    <el-form :model="productFilter" @submit.native.prevent="searchProduct">
                                        <div class="list-table-tool-row">
                                            <div class="simple-form-warp">
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <div class="control-container" style="width: 180px">
                                                            <SelectCategory
                                                                v-model:categoryId="productFilter.categoryId" />
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <div class="control-container" style="width: 240px">
                                                            <TigInput width="240px" v-model="productFilter.keyword"
                                                                :placeholder="$t('商品名称/编码')" clearable
                                                                @keyup.enter="searchProduct" @clear="searchProduct">
                                                            </TigInput>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="simple-form-field">
                                                    <div class="form-group">
                                                        <el-space>
                                                            <el-button type="primary"
                                                                @click="searchProduct">{{ $t('搜索') }}</el-button>
                                                            <el-button plain @click="resetProduct">{{ $t('重置') }}</el-button>
                                                        </el-space>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </el-form>
                                </div>
                            </div>

                            <a-spin :spinning="productLoading">
                                <el-table :data="productList" class="result-table" height="600">
                                    <el-table-column type="index" :label="$t('序号')" width="60" />
                                    <el-table-column :label="$t('商品/编码')" min-width="260">
                                        <template #default="{ row }">
                                            <div class="product-cell">
                                                <div class="product-thumb">
                                                    <img v-if="row.picUrl || row.picThumb || row.picLarge"
                                                        class="product-thumb-img"
                                                        :src="imageFormat(row.picUrl || row.picThumb || row.picLarge)"
                                                        alt="" />
                                                    <div v-else class="product-thumb-placeholder" />
                                                </div>
                                                <div class="product-info">
                                                    <div class="product-name">{{ row.productName || "-" }}</div>
                                                    <div class="gray">{{ $t('编码：{productSn}', { productSn: row.productSn || "-" }) }}</div>
                                                </div>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('售价({currency})', { currency })" width="120">
                                        <template #default="{ row }">
                                            {{ row.productPrice ?? "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('库存')" width="120">
                                        <template #default="{ row }">
                                            {{ row.productStock ?? "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('操作')" width="100" fixed="right">
                                        <template #default="{ row }">
                                            <el-button link type="primary"
                                                :disabled="!selectedUserInfo?.userId || row.isTimeDiscount == 1"
                                                @click="addProduct(row)"> {{ $t('选择') }} </el-button>
                                        </template>
                                    </el-table-column>
                                    <template #empty>
                                        <div class="empty-warp">
                                            <div v-if="!productLoading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                        </div>
                                    </template>
                                </el-table>
                            </a-spin>

                            <div v-if="productTotal > 0" class="pagination-con">
                                <Pagination v-model:page="productPage" v-model:size="productSize" :total="productTotal"
                                    @callback="loadProduct" layout="slot ,prev, pager, next" :background="false" />
                            </div>
                        </div>

                        <!-- 右：已选商品（购物车） -->
                        <div class="panel">
                            <div class="panel-head">
                                <div class="panel-title">
                                    <span>{{ $t('已选商品') }}</span>
                                    <span v-if="cartCount > 0">（{{ cartCount }}）</span>
                                </div>
                            </div>

                            <a-spin :spinning="cartLoading">
                                <el-table :data="cartItems" class="result-table" height="600">
                                    <el-table-column type="index" :label="$t('序号')" width="60" />
                                    <el-table-column :label="$t('商品/编码')" min-width="220">
                                        <template #default="{ row }">
                                            <div class="product-cell">
                                                <div class="product-thumb">
                                                    <img v-if="imageFormat(row.picUrl || row.picThumb)"
                                                        class="product-thumb-img"
                                                        :src="imageFormat(row.picUrl || row.picThumb)" alt="" />
                                                    <div v-else class="product-thumb-placeholder" />
                                                </div>
                                                <div class="product-info">
                                                    <div class="product-name">{{ row.productName || "-" }}</div>
                                                    <div v-if="formatSkuDataValues(row.skuData)" class="gray">{{
                                                        formatSkuDataValues(row.skuData) }}</div>
                                                    <div class="gray">{{ $t('编码：{productSn}', { productSn: row.productSn || "-" }) }}</div>
                                                    <div v-if="row.extraSkuData && row.extraSkuData.length"
                                                        class="extra-service-box">
                                                        <div v-for="(extra, idx) in row.extraSkuData"
                                                            :key="String(extra?.attributesId ?? idx)"
                                                            class="extra-service-row">
                                                            <div class="extra-service-left">
                                                                <span class="extra-service-left-text">
                                                                    {{ (extra?.attrName || "-") + "｜" +
                                                                        (extra?.attrValue || "-") }}
                                                                </span>
                                                            </div>
                                                            <div class="extra-service-price">
                                                                {{
                                                                    extra?.attrPrice === null || extra?.attrPrice ===
                                                                        undefined
                                                                        ? "-"
                                                                        : priceFormat(Number(extra.attrPrice))
                                                                }}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('单价({currency})', { currency })" width="120">
                                        <template #default="{ row }">
                                            {{ !row.price ? "-" : priceFormat(Number(row.price), false) }}
                                        </template>
                                    </el-table-column>
                                    <!-- <el-table-column :label="`会员价(${currency})`" width="120">
                                        <template #default="{ row }">
                                            {{ !row.memberPrice ? "-" : priceFormat(Number(row.memberPrice), false) }}
                                        </template>
                                    </el-table-column> -->
                                    <el-table-column :label="$t('数量')" width="150">
                                        <template #default="{ row }">
                                            <el-input-number v-model="row.quantity" style="width: 120px" :min="1"
                                                :step="1" @change="(val: number) => updateQty(row, val)" />
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('小计({currency})', { currency })" width="120">
                                        <template #default="{ row }">
                                            {{ !row.subtotal ? "-" : priceFormat(Number(row.subtotal), false) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column :label="$t('操作')" width="100" fixed="right">
                                        <template #default="{ row }">
                                            <el-button link type="danger" @click="removeCart(row)">{{ $t('移除') }}</el-button>
                                        </template>
                                    </el-table-column>
                                    <template #empty>
                                        <div class="empty-warp">
                                            <div v-if="!cartLoading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                        </div>
                                    </template>
                                </el-table>
                            </a-spin>
                        </div>
                    </div>
                </div>

                <!-- 底部结算栏 -->
                <div class="selected-action-warp selected-warp-left proxy-checkout-warp"
                    :style="{ left: themeInfo.layout !== 'topMenu' ? '23.0625rem' : '16.875rem' }">
                    <div class="selected-action">
                        <el-button type="primary" :disabled="!canCheckout" @click="goCheck">{{ $t('结算') }}</el-button>
                    </div>
                </div>

                <!-- 选择会员弹窗：通过 DialogForm 动态加载 -->
            </div>

            <div v-if="step === 'check'" style="position: relative">
                <CheckPanel v-if="checkMounted" :userId="Number(selectedUserId || 0)" :userInfo="selectedUserInfo"
                    :flowType="checkoutFlowType" :deliveryPreSelected="deliveryPreSelected"
                    :cartCheckData="cartCheckDataForPanel" :active="step === 'check'" @back="onBackFromCheck" />
            </div>
        </div>
    </div>

    <ProductSpecPop v-model:open="specPopOpen" :productId="specPopProduct?.productId"
        :productSn="specPopProduct?.productSn" :userId="selectedUserId" @confirm="onSpecConfirm" />

    <DialogForm ref="deliveryOptionsDialogRef" isDrawer :title="$t('选择配送方式')" width="500px" :showFooter="false"
        path="order/proxyShopping/src/DeliveryOptionsDrawer" :params="{ options: deliveryOptionsData }"
        @okCallback="onDeliveryOptionSelected">
        <span style="display: none"></span>
    </DialogForm>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, onMounted, reactive, ref } from "vue";
import { message, Alert } from "ant-design-vue";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { Pagination } from "@/components/list";
import { SelectCategory } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";
import { imageFormat, priceFormat } from "@/utils/format";
import type { ProxyCartItem, ProxyCartListByStoreVO, ProxyProductListItem } from "@/types/order/proxyShopping.d";
import { getProxyProductList } from "@/api/proxyShopping/product";
import { addToProxyCart, getProxyCartList, removeProxyCartItem, updateProxyCartItem } from "@/api/proxyShopping/cart";
import { getProxyShoppingO2ODeliveryOptions } from "@/api/proxyShopping/delivery";
import { getProxyShoppingUserList } from "@/api/proxyShopping/user";
import { isStore } from "@/utils/version";
import type { UserFormState } from "@/types/user/user.d";
import ProductSpecPop from "@/views/order/proxyShopping/src/ProductSpecPop.vue";
import CheckPanel from "./src/CheckPanel.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const { themeInfo } = useThemeStore();

const currency = computed(() => configStore.get("dollarSign") ?? "¥");

type SkuDataItem = {
    name?: string;
    value?: string;
    attrPicThumb?: string;
    [key: string]: any;
};

const formatSkuDataValues = (skuData: unknown) => {
    if (skuData === undefined || skuData === null) return "";

    let list: unknown = skuData;
    if (typeof skuData === "string") {
        const s = skuData.trim();
        if (!s) return "";
        try {
            list = JSON.parse(s);
        } catch {
            return "";
        }
    }

    if (!Array.isArray(list)) return "";
    const values = (list as SkuDataItem[])
        .map((it) => it?.value)
        .filter((v) => v !== undefined && v !== null && String(v).trim() !== "")
        .map((v) => String(v).trim());
    return values.join("，");
};

// 会员
const userDialogVisible = ref(false);
const selectedUserId = ref<number | undefined>(undefined);
const selectedUserInfo = ref<UserFormState | null>(null);

// 视图步骤：index=下单页，check=结算页（同页切换）
const step = ref<"index" | "check">("index");
// 用于避免“刷新恢复到结算时先闪一下下单页”
// 只有在 userId 恢复完成后才挂载结算面板，避免传入 0
const checkMounted = ref(false);

// 商品列表
const productLoading = ref(false);
const productTotal = ref(0);
const productList = ref<ProxyProductListItem[]>([]);
const productFilter = reactive({
    page: 1,
    size: Number((configStore as any).get("pageSize") || 10),
    keyword: "",
    categoryId: undefined as number | undefined
});

const productPage = computed({
    get: () => productFilter.page,
    set: (val: number) => (productFilter.page = val)
});
const productSize = computed({
    get: () => productFilter.size,
    set: (val: number) => (productFilter.size = val)
});

// 购物车（右侧）
const cartLoading = ref(false);
const cartItems = ref<ProxyCartItem[]>([]);
const cartSummary = ref<Pick<ProxyCartListByStoreVO, "totalQuantity" | "totalAmount">>({});
const cartCount = computed(() => {
    const fromApi = Number(cartSummary.value.totalQuantity);
    if (!Number.isNaN(fromApi) && fromApi > 0) return fromApi;
    return cartItems.value.reduce((sum, it) => sum + (Number(it.quantity) || 0), 0);
});
const canCheckout = computed(() => !!selectedUserId.value && cartItems.value.length > 0);
// addToCart 返回的 flowType：用于跳转到结算页时透传
const checkoutFlowType = ref<number | undefined>(undefined);
const deliveryPreSelected = ref(false);
const cartCheckDataForPanel = ref<{ cartId: number; isChecked: number }[]>([]);
const deliveryOptionsData = ref<any[]>([]);
const deliveryOptionsDialogRef = ref<any>(null);

const DELIVERY_NAME_MAP: Record<number, string> = {
    1: t('商家配送'),
    2: t('到店自提'),
    3: t('同城配送'),
    4: t('到店自提'),
    5: t('同城配送')
};

const toArray = <T = any>(v: any): T[] => (Array.isArray(v) ? v : []);
const calcTotalQuantity = (list: any[]) =>
    list.reduce((sum, item) => {
        const n = Number(item?.quantity ?? item?.number ?? item?.num ?? item?.count ?? 0);
        return sum + (Number.isFinite(n) ? n : 0);
    }, 0);

const resolveFlowTypeByDeliveryType = (deliveryType: any) => {
    const n = Number(deliveryType || 0);
    if (n === 2 || n === 4) return 2;
    if (n === 3 || n === 5) return 3;
    return 1;
};

const buildProductIdSignature = (productsSource: any) => {
    const products = toArray(productsSource);
    if (products.length === 0) return "";
    const ids = new Set<number>();
    products.forEach((p: any) => {
        const productId = Number(p?.productId ?? p?.goodsId ?? p?.id ?? 0);
        if (Number.isFinite(productId) && productId > 0) {
            ids.add(productId);
        }
    });
    if (ids.size === 0) return "";
    return Array.from(ids).sort((a, b) => a - b).join("|");
};

const getFlowProductSignatureFromPayload = (payload: any, flowType: number) => {
    if (flowType === 2) {
        const pickupGroups = toArray(payload?.pickupProducts);
        const firstPickupProducts = toArray(pickupGroups[0]?.products);
        return buildProductIdSignature(firstPickupProducts);
    }
    if (flowType === 3) {
        return buildProductIdSignature(payload?.shopDeliveryProducts);
    }
    return buildProductIdSignature(payload?.deliveryProducts);
};

const shouldAutoCheckoutWhenProductsSame = (options: any[], payload: any) => {
    if (!Array.isArray(options) || options.length <= 1) return false;
    const signatures = options.map((opt) => {
        const flowType = Number(opt?.flowType || 1);
        return getFlowProductSignatureFromPayload(payload, flowType);
    });
    if (signatures.some((sig) => !sig)) return false;
    const base = signatures[0];
    if (!base) return false;
    return signatures.every((sig) => sig === base);
};

const pickPreferredDeliveryOption = (options: any[], preferredFlowType: number) => {
    return options.find((opt) => Number(opt?.flowType || 0) === preferredFlowType) || options[0];
};

const buildDeliveryOptionsFromResponse = (payload: any) => {
    const options: any[] = [];

    const deliveryProducts = toArray(payload?.deliveryProducts);
    if (deliveryProducts.length > 0) {
        options.push({
            flowType: 1,
            deliveryType: 1,
            deliveryTypeName: DELIVERY_NAME_MAP[1],
            productList: deliveryProducts,
            totalQuantity: calcTotalQuantity(deliveryProducts)
        });
    }

    const pickupProducts = toArray(payload?.pickupProducts);
    pickupProducts.forEach((group: any) => {
        const products = toArray(group?.products);
        if (products.length === 0) return;
        const pickups = toArray(group?.pickups);
        const pickup = pickups[0] || {};
        options.push({
            flowType: 2,
            deliveryType: 2,
            deliveryTypeName: DELIVERY_NAME_MAP[2],
            productList: products,
            totalQuantity: calcTotalQuantity(products),
            storeName: pickup?.shopName || group?.shopName || "",
            shopId: group?.shopId ?? pickup?.shopId,
            pickupId: pickup?.shopId
        });
    });

    const shopDeliveryProducts = toArray(payload?.shopDeliveryProducts);
    if (shopDeliveryProducts.length > 0) {
        options.push({
            flowType: 3,
            deliveryType: 3,
            deliveryTypeName: DELIVERY_NAME_MAP[3],
            productList: shopDeliveryProducts,
            totalQuantity: calcTotalQuantity(shopDeliveryProducts)
        });
    }

    return options;
};

const onUserConfirm = async (payload: number | UserFormState | (UserFormState & Record<string, any>)) => {
    const nextId = typeof payload === "number" ? Number(payload) : Number((payload as any)?.userId);
    const prev = selectedUserId.value;
    selectedUserId.value = nextId;
    // 如果是从弹窗选中的完整用户对象，直接回显（避免再次 getUser）
    if (typeof payload !== "number" && payload) {
        selectedUserInfo.value = payload as any;
    }
    // 切换会员时，清理与“上一次加购”强相关的本地状态（避免串号）
    if (prev && prev !== selectedUserId.value) {
        skuValueCache.value = new Map();
        checkoutFlowType.value = undefined;
    }
    await refreshCart();
};

const onUserDialogOk = async (user: UserFormState & Record<string, any>) => {
    await onUserConfirm(user);
};

const normalizeCategoryId = (val: any) => {
    const n = Number(val);
    if (!val || n === 0 || Number.isNaN(n)) return undefined;
    return n;
};

const loadProduct = async () => {
    productLoading.value = true;
    try {
        const categoryId = normalizeCategoryId(productFilter.categoryId);
        const res = await getProxyProductList({
            page: productFilter.page,
            size: productFilter.size,
            keyword: productFilter.keyword || undefined,
            categoryId
        });
        productList.value = res.records || [];
        productTotal.value = Number(res.total || 0);
    } catch (e: any) {
        message.error(e?.message || t('获取商品列表失败'));
    } finally {
        productLoading.value = false;
    }
};

const searchProduct = async () => {
    productFilter.page = 1;
    await loadProduct();
};

const resetProduct = async () => {
    productFilter.keyword = "";
    productFilter.categoryId = undefined;
    productFilter.page = 1;
    await loadProduct();
};

const refreshCart = async () => {
    if (!selectedUserId.value) {
        cartItems.value = [];
        cartSummary.value = {};
        return;
    }
    cartLoading.value = true;
    try {
        const res = await getProxyCartList({ userId: selectedUserId.value });
        cartSummary.value = {
            totalQuantity: res?.totalQuantity,
            totalAmount: res?.totalAmount
        };
        const flat = (res.cartList || []).flatMap((s: any) => s?.carts || []);
        // 部分接口可能不返回 skuValue，这里用本地记录补齐回显
        flat.forEach((it: any) => {
            if (it?.skuValue) return;
            const key = `${it?.productId || 0}_${it?.skuId || 0}`;
            const v = skuValueCache.value.get(key);
            if (v) it.skuValue = v;
        });
        cartItems.value = flat;
    } catch (e: any) {
        message.error(e?.message || t('获取购物车失败'));
    } finally {
        cartLoading.value = false;
    }
};

// 规格弹窗
const specPopOpen = ref(false);
const specPopProduct = ref<(ProxyProductListItem & Record<string, any>) | null>(null);
const skuValueCache = ref<Map<string, string>>(new Map());

const addProduct = async (row: ProxyProductListItem & Record<string, any>) => {
    if (!selectedUserId.value) {
        message.warning(t('请先选择下单会员'));
        return;
    }
    specPopProduct.value = row;
    specPopOpen.value = true;
};

const onSpecConfirm = async (payload: { skuId?: number; skuValue?: string; quantity: number; shopId?: number; extraAttrIds?: string }) => {
    if (!selectedUserId.value || !specPopProduct.value) return;
    try {
        if (payload?.skuValue) {
            const key = `${specPopProduct.value.productId}_${payload.skuId || 0}`;
            skuValueCache.value.set(key, payload.skuValue);
        }
        const rawShopId = isStore()
            ? (Number(localStorage.getItem("shopId")) || undefined)
            : (payload?.shopId ?? specPopProduct.value?.shopId);
        const shopId = rawShopId === undefined || rawShopId === null ? undefined : Number(rawShopId);
        const res = await addToProxyCart({
            userId: selectedUserId.value,
            id: specPopProduct.value.productId,
            number: payload.quantity,
            skuId: payload.skuId,
            extraAttrIds: payload.extraAttrIds || undefined,
            shopId: Number.isNaN(shopId as any) ? undefined : shopId
        });
        checkoutFlowType.value = res.flowType;
        message.success(t('已加入购物车'));
        await refreshCart();
    } catch (e: any) {
        message.error(e?.message || t('加入购物车失败'));
    } finally {
        specPopProduct.value = null;
    }
};

const updateQty = async (row: ProxyCartItem & Record<string, any>, val: number) => {
    if (!row?.cartId) return;
    const quantity = Math.max(1, Number(val || 1));
    try {
        await updateProxyCartItem({
            cartId: row.cartId,
            data: { quantity }
        });
        await refreshCart();
    } catch (e: any) {
        message.error(e?.message || t('更新数量失败'));
        await refreshCart();
    }
};

const removeCart = async (row: ProxyCartItem) => {
    if (!row?.cartId) return;
    try {
        await removeProxyCartItem({ cartIds: [row.cartId] });
        message.success(t('已移除'));
        await refreshCart();
    } catch (e: any) {
        message.error(e?.message || t('移除失败'));
    }
};

const goCheck = async () => {
    if (!selectedUserId.value) return;

    if (isStore()) {
        try {
            const res = await getProxyShoppingO2ODeliveryOptions({
                userId: selectedUserId.value
            });
            const payload =
                res && (res.deliveryType !== undefined || res.deliveryProducts || res.pickupProducts || res.shopDeliveryProducts)
                    ? res
                    : res?.data || res?.result || {};
            deliveryOptionsData.value = buildDeliveryOptionsFromResponse(payload);
            if (deliveryOptionsData.value.length === 0) {
                message.warning(t('暂无可用配送方式'));
                return;
            }
            const preferredFlowType = resolveFlowTypeByDeliveryType(payload?.deliveryType);
            if (deliveryOptionsData.value.length === 1) {
                deliveryPreSelected.value = false;
                goToCheckout(pickPreferredDeliveryOption(deliveryOptionsData.value, preferredFlowType));
                return;
            }
            if (shouldAutoCheckoutWhenProductsSame(deliveryOptionsData.value, payload)) {
                deliveryPreSelected.value = false;
                goToCheckout(pickPreferredDeliveryOption(deliveryOptionsData.value, preferredFlowType));
                return;
            }
            deliveryOptionsDialogRef.value?.show?.();
            return;
        } catch (e: any) {
            message.error(e?.message || t('获取配送方式失败'));
            return;
        }
    }

    deliveryPreSelected.value = false;

    cartCheckDataForPanel.value = cartItems.value
        .filter((ci: any) => Number(ci?.cartId || 0) > 0)
        .map((ci: any) => ({ cartId: Number(ci.cartId), isChecked: 1 }));

    checkMounted.value = true;
    step.value = "check";
};

const buildCartCheckPayload = (selectedItem: any) => {
    const getProductIds = (opt: any) =>
        ((opt?.productList || []) as any[])
            .map((p: any) => Number(p?.productId || 0))
            .filter((v: number) => v > 0);

    const keepIds = getProductIds(selectedItem);
    const keepSet = new Set(keepIds);

    const cancelIds: number[] = [];
    deliveryOptionsData.value.forEach((opt: any) => {
        if (opt === selectedItem) return;
        getProductIds(opt).forEach((pid: number) => cancelIds.push(pid));
    });
    const cancelIdsSet = new Set(cancelIds.filter((id) => !keepSet.has(id)));

    return cartItems.value
        .filter((ci: any) => Number(ci?.cartId || 0) > 0)
        .map((ci: any) => ({
            cartId: Number(ci.cartId),
            isChecked: cancelIdsSet.has(Number(ci.productId || 0)) ? 0 : 1
        }));
};

const goToCheckout = (item: any) => {
    const candidateFlowType = Number(item?.flowType || (Number(item?.deliveryType) === 2 ? 2 : Number(item?.deliveryType) === 3 ? 3 : 1));
    if (Number.isFinite(candidateFlowType) && candidateFlowType > 0) {
        checkoutFlowType.value = candidateFlowType;
    }

    cartCheckDataForPanel.value = buildCartCheckPayload(item);

    checkMounted.value = true;
    step.value = "check";
};

const onDeliveryOptionSelected = (item: any) => {
    deliveryPreSelected.value = true;
    goToCheckout(item);
};

const onBackFromCheck = () => {
    step.value = "index";
};

const initFromQuery = async () => {
    // 不再从 URL 恢复任何状态，刷新时回到初始状态
};

/**
 * 页面加载时：根据关键字自动匹配默认会员（如存在则自动选中并加载购物车）
 */
const initDefaultUserByKeyword = async () => {
    if (selectedUserId.value) return;
    const keyword = "123123";
    try {
        const res = await getProxyShoppingUserList({ page: 1, size: 1, keyword, isExact: true });
        const hit = res?.records?.[0];
        const hitUserId = Number((hit as any)?.userId || 0);
        if (Number.isFinite(hitUserId) && hitUserId > 0) {
            await onUserConfirm(hit as any);
        }
    } catch (e: any) {
        // 不阻塞页面主流程：自动匹配失败时仅提示，不影响手动选择
        message.error(e?.message || t('自动匹配会员失败'));
    }
};

onMounted(async () => {
    await initFromQuery();
    // 若未通过 URL 恢复 userId，则按关键字自动查找并选中
    if (!selectedUserId.value) {
        await initDefaultUserByKeyword();
    }
    await loadProduct();
});
</script>

<style scoped lang="less">
.proxy-shopping-index {
    height: 100%;
}

.sticky-user .user-info-card {
    display: flex;
}

.sticky-user .info-card {
    padding: 12px;
    border-radius: 8px;
    min-width: 250px;
    border: 1px solid var(--el-color-primary);
}

.sticky-user .info-card .user-info-card-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.sticky-user .info-card .card-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
}

.sticky-user .info-card .name {
    font-size: 14px;
    font-weight: 600;
}

.sticky-user .info-card .label {
    color: #333;
}

.sticky-user .info-card .value {
    color: var(--el-color-primary);
}

.page-body {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding-bottom: 30px;
}

.notice-list {
    margin: 0;

    li {
        line-height: 20px;
        margin: 2px 0;
    }
}

.user-row {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
}

.user-row-left {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
}

.proxy-checkout-warp {
    right: 16px;
}

.proxy-checkout-warp .selected-action {
    padding-left: 0;
    padding: 0 16px;
    width: 100%;
}

.two-col {
    display: flex;
    gap: 40px;
    align-items: stretch;
    flex: 1;
}

.panel {
    flex: 1;
    min-width: 0;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    padding: 12px 20px;
    display: flex;
    flex-direction: column;
}

.panel-right {
    flex: 0 0 520px;
}

.panel-head {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;
}

.panel-title {
    font-size: 14px;
    padding-top: 5px;
    font-weight: 600;
}

.panel-tools {
    display: flex;
    align-items: center;
}

.product-cell {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    min-width: 0;
}

.product-thumb {
    width: 70px;
    height: 70px;
    flex: 0 0 70px;
    border-radius: 4px;
    overflow: hidden;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
}

.product-thumb-img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
}

.product-thumb-placeholder {
    width: 100%;
    height: 100%;
}

.product-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    flex: 1;
}

.product-name {
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    white-space: normal;
    word-break: break-word;
}

.gray {
    color: #909399;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.extra-service-box {
    margin-top: 4px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
}

.extra-service-row {
    background: #f7f8fa;
    border-radius: 4px;
    padding: 4px 8px;
    display: flex;
    align-items: center;
    max-width: 250px;
    justify-content: space-between;
    flex-wrap: nowrap;
    gap: 10px;
    min-width: 0;
    box-sizing: border-box;
}

.extra-service-left {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    min-width: 0;
    flex: 1 1 auto;
    overflow: hidden;
}

.extra-service-left-text {
    display: block;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
    line-height: 18px;
}

.extra-service-split {
    flex: 0 0 auto;
    color: #c0c4cc;
}

.extra-service-price {
    flex: 0 0 auto;
    color: #606266;
    font-weight: 500;
    white-space: nowrap;
}

.pagination-con {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
}
</style>
