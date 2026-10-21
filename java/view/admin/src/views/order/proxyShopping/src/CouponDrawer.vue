<template>
    <div class="coupon-drawer" v-loading="loading">
        <template v-if="!loading && coupons.length === 0">
            <div class="empty-bg"></div>
        </template>
        <el-radio-group class="couponBox" v-model="checkKey">
            <div
                class="couponItem"
                :class="{ 'couponItem--disabled': !!item.userId && !isCouponUsable(item) }"
                v-for="(item, index) in coupons"
                :key="index"
            >
                <div class="couponItemLeft">
                    <div v-if="Number(item.couponType) === 1" class="price">
                        <span>{{ currency }}</span> {{ item.couponMoney ?? "-" }}
                    </div>
                    <div v-else-if="Number(item.couponType) === 2" class="price">{{ $t('{couponDiscount}折', { couponDiscount: item.couponDiscount ?? "-" }) }}</div>
                    <div v-else class="price">-</div>
                </div>
                <div class="couponItemRight">
                    <div class="title">{{ item.couponName }}</div>
                    <div class="scene">
                        <span>{{ item.couponType === 2 ? $t('折扣券') : $t('满减券') }}</span>
                        <span v-if="item.isGlobal" class="coupon-globle">{{ $t('全场券') }}</span>
                    </div>
                    <div class="date">
                        <span class="label">{{ $t('有效期至：') }}</span>
                        <span class="value">{{ item.useEndDateFormat }}</span>
                    </div>
                    <!-- <div class="unusable-tip" v-if="!isCouponUsable(item)">当前订单不可用</div> -->
                </div>
                <div class="info-sex" v-if="item.userId">
                    <el-radio :disabled="!isCouponUsable(item)" @click.prevent="toggleSelect(item)" :value="getKey(item)"></el-radio>
                </div>
                <div class="info-sex" v-else>
                    <el-button class="btn" type="primary" @click="handlePopupConfirm(item)">{{ $t('领取并使用') }}</el-button>
                </div>
            </div>
        </el-radio-group>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, nextTick } from "vue";
import { message } from "ant-design-vue";
import { claimProxyShoppingCoupon, getProxyShoppingCouponList } from "@/api/proxyShopping/cart";
import { useConfigStore } from "@/store/config";
import { useCouponUsability } from "./composables/useCouponUsability";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    couponList?: any[] | { enableCoupons?: any[]; disableCoupons?: any[]; [key: string]: any };
    selectedCouponId?: number;
    userId?: number;
    productIds?: number[];
    enableUsePromotion?: any[];
    afterClaimRefresh?: () => Promise<any>;
}>();

const emit = defineEmits(["submitCallback", "okType"]);

const configStore = useConfigStore();
const currency = computed(() => configStore.get("dollarSign") ?? "¥");

const loading = ref(false);
const couponLoading = ref(false);
const couponListData = ref<any[]>([]);
const coupons = computed(() => couponListData.value || []);

/**
 * 约定：
 * - DialogForm 的确定按钮会调用 onFormSubmit()
 * - 这里以 “用户优惠券ID(优先) + 券ID” 组成 key，方便兼容不同字段
 */
// 统一约定：抽屉组件只使用 couponId 作为选中 key
const getCouponId = (c: any) => Number(c?.couponId || 0);
const getKey = (c: any) => getCouponId(c);

const checkKey = ref<number>(0);

/**
 * 结算页返回的可用优惠活动（enableUsePromotion）：
 * - enableUsePromotion[i].type === 2 表示优惠券
 * - 可用券ID路径：enableUsePromotion[i].data.couponId
 * 约定（对齐本需求）：
 * - 未传（undefined）：不做额外限制（保持历史行为）
 * - 传了但为空数组（[]）：表示“当前订单无可用券”，已领取券应全部禁用
 */
const { isCouponUsable } = useCouponUsability(() => props.enableUsePromotion, { restrictWhenProvided: true, getCouponId });

const normalizeFallbackList = () => {
    const list = props.couponList as any;
    if (Array.isArray(list)) return list;
    const enable = Array.isArray(list?.enableCoupons) ? list.enableCoupons : [];
    const disable = Array.isArray(list?.disableCoupons) ? list.disableCoupons : [];
    return [...enable, ...disable];
};

const initSelected = () => {
    const selectedC = Number(props.selectedCouponId || 0);

    // 初始化选中：只根据 couponId 回填（需在当前列表命中后再设置 checkKey）
    if (selectedC) {
        const found = coupons.value.find((item: any) => getCouponId(item) === selectedC);
        checkKey.value = found ? getKey(found) : 0;
        emit("okType", true);
        return;
    }

    checkKey.value = 0;
    emit("okType", true);
};

const loadCoupons = async () => {
    const uid = Number(props.userId || 0);
    const productIds = Array.isArray(props.productIds) ? props.productIds.map((x) => Number(x)).filter((x) => Number.isFinite(x) && x > 0) : [];
    loading.value = true;
    try {
        const res: any = await getProxyShoppingCouponList({ userId: uid, productIds });
        couponListData.value = Array.isArray(res) ? res : [];
    } catch (e: any) {
        // 接口失败则兜底使用旧入参，避免抽屉空白
        couponListData.value = normalizeFallbackList();
        message.error(e?.message || t('获取优惠券列表失败'));
    } finally {
        loading.value = false;
    }
};

onMounted(async () => {
    await loadCoupons();
    initSelected();
});

// 抽屉反复打开时组件可能被复用：当外部选中值或列表变化时，重新回填选中态
watch(
    () => [props.selectedCouponId],
    async () => {
        await nextTick();
        initSelected();
    },
    { immediate: true }
);
watch(
    coupons,
    async () => {
        await nextTick();
        initSelected();
    },
    { flush: "post" }
);

const toggleSelect = (item: any) => {
    // 只对“已领取券”做不可用拦截；未领取券不会出现 radio
    if (item?.userId && !isCouponUsable(item)) {
        message.warning(t('该优惠券当前订单不可用'));
        return;
    }
    const key = getKey(item);
    checkKey.value = checkKey.value === key ? 0 : key;
    emit("okType", true);
};

// 领取并使用：领取后调用结算侧刷新函数，基于最新 enableUsePromotion 判断是否可用
const handlePopupConfirm = async (item: any) => {
    const uid = Number(props.userId || 0);
    const cid = getCouponId(item);
    couponLoading.value = true;
    try {
        const res: any = await claimProxyShoppingCoupon({ couponId: cid, userId: uid });
        item.userId = uid;
        // 领取后刷新结算态：以刷新后的 enableUsePromotion 判定是否可用
        if (props.afterClaimRefresh) {
            try {
                await props.afterClaimRefresh();
            } catch {
                // ignore：刷新失败时保持当前行为（不自动选中）
            }
        }
        await nextTick();
        if (cid && isCouponUsable(item)) {
            checkKey.value = cid;
        } else {
            checkKey.value = 0;
            message.warning(t('已领取，但当前订单不可用'));
        }
        emit("okType", true);
    } catch (e: any) {
        message.error(e?.message || t('领取优惠券失败'));
    } finally {
        couponLoading.value = false;
    }
};

const findSelected = () => {
    if (!checkKey.value) return null;
    return coupons.value.find((c: any) => getKey(c) === checkKey.value) || null;
};

const onFormSubmit = () => {
    const selected = findSelected();
    if (!selected) {
        emit("submitCallback", { couponId: 0 });
        return;
    }
    if (selected?.userId && !isCouponUsable(selected)) {
        message.warning(t('该优惠券当前订单不可用'));
        emit("submitCallback", { couponId: 0 });
        return;
    }
    emit("submitCallback", { couponId: getCouponId(selected) });
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.empty-bg {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    background: #fff;
    color: #909399;
}

.couponBox {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    position: relative;

    .couponItem {
        width: calc((100% - 10px) / 2);
        display: flex;
        position: relative;
        height: 100px;
        overflow: hidden;
        background: #f5f5f5;
        border-radius: 6px;
        box-sizing: border-box;
        cursor: pointer;
        transition: opacity 0.15s ease;

        &.couponItem--disabled {
            opacity: 0.55;
            cursor: not-allowed;
        }

        .couponItemRight {
            flex: 1;
            position: relative;
            display: flex;
            flex-direction: column;
            gap: 10px;
            padding: 18px;
            box-sizing: border-box;

            .title {
                font-weight: bold;
                font-size: 14px;
                word-break: break-all;
                display: -webkit-box;
                -webkit-line-clamp: 1;
                line-clamp: 1;
                -webkit-box-orient: vertical;
                overflow: hidden;
                flex-shrink: 0;
            }
            .scene {
                font-size: 12px;
            }
            .date {
                font-size: 12px;
                display: flex;
                .label {
                    flex-shrink: 0;
                }
            }

            .unusable-tip {
                font-size: 12px;
                color: #f56c6c;
                line-height: 1;
                margin-top: -4px;
            }

            &::before,
            &::after {
                content: "";
                position: absolute;
                top: 0;
                left: 0;
                transform: translate(-50%, -50%);
                width: 15px;
                height: 15px;
                background-color: #fff;
                border-radius: 50%;
            }
            &::after {
                top: 100%;
            }
        }

        .couponItemLeft {
            width: 25%;
            display: flex;
            align-items: center;
            justify-content: center;
            row-gap: 10px;
            padding: 18px;
            box-sizing: border-box;

            .price {
                color: var(--tig-primary);
                font-size: 26px;
                display: flex;
                span {
                    font-size: 24px;
                    align-self: flex-end;
                }
            }

            &::after {
                display: block;
                content: "";
                width: 1px;
                height: calc(100% + 40px);
                position: absolute;
                top: -20px;
                border-right: 2px dashed #f5f5f5;
                left: calc(25% - 1px);
            }
        }
    }
}

.coupon-globle {
    margin-left: 6px;
    padding: 1px 6px;
    border-radius: 10px;
    background: rgba(20, 86, 240, 0.1);
    color: var(--tig-primary);
}

.info-sex {
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    pointer-events: auto;

    :deep(.el-radio) {
        height: 100%;
        width: 100%;
        position: relative;
    }
    :deep(.el-radio__input) {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
    }
    :deep(.el-radio__inner) {
        display: none;
        width: 0;
        height: 0;
        border: none;
    }
    :deep(.el-radio__input) {
        width: 22px;
        height: 22px;
        &::after {
            content: "";
            display: block;
            width: 20px;
            height: 20px;
            border: 2px solid #dcdfe6;
            border-radius: 50%;
            background-color: #fff;
            box-sizing: border-box;
            transition: all 0.2s ease;
        }
        &.is-checked::after {
            background-color: var(--tig-primary, #409eff);
            border-color: var(--tig-primary, #409eff);
        }
        &.is-checked::before {
            content: "";
            position: absolute;
            left: 7.2px;
            top: 4px;
            width: 4px;
            height: 8px;
            border: 2px solid #fff;
            border-top: none;
            border-left: none;
            transform: rotate(45deg);
            z-index: 1;
        }
    }
}

.btn {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-40%);
    border-radius: var(--tig-pos-radius);
}

.couponItem-none {
    justify-content: center;
    .couponItemRight {
        align-items: center;
        text-align: center;
        &::before,
        &::after {
            display: none;
        }
    }
}
</style>
