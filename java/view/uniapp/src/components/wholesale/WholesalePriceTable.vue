<template>
    <view v-if="renderTiers.length > 0" class="wholesale-price-table">
        <!-- 仅一档：单价 / 多规格区间（价格相同则单价） -->
        <view v-if="renderTiers.length === 1 && renderSingle" class="tier-range" :class="{ vertical: !renderSingle.isRange }">
            <view class="tier-range-price">
                <format-price
                    :show-text="false"
                    :font-style="priceFontStyle"
                    :currency-style="priceCurrencyStyle"
                    :decimals-style="priceDecimalsStyle"
                    :price-data="renderSingle.minPrice"
                />
                <template v-if="renderSingle.isRange">
                    <text class="range-sep">~</text>
                    <format-price
                        :show-text="false"
                        :font-style="priceFontStyle"
                        :currency-style="priceCurrencyStyle"
                        :decimals-style="priceDecimalsStyle"
                        :price-data="renderSingle.maxPrice"
                    />
                </template>
            </view>
            <view class="tier-range-qty">{{ renderTiers[0].qtyText }}</view>
        </view>
        <!-- 多档：横向阶梯条 -->
        <scroll-view v-else scroll-x="true" class="tier-scroll" :show-scrollbar="false">
            <view class="tier-list">
                <view v-for="tier in renderTiers" :key="tier.tierMinQty" class="tier-item">
                    <format-price
                        :show-text="false"
                        :font-style="priceFontStyle"
                        :currency-style="priceCurrencyStyle"
                        :decimals-style="priceDecimalsStyle"
                        :price-data="tier.displayPrice"
                    />
                    <view class="tier-qty">{{ tier.qtyText }}</view>
                </view>
            </view>
        </scroll-view>
    </view>
</template>

<script lang="ts" setup>
/**
 * @description 批发价阶梯表（仅展示一组「生效阶梯」）
 *
 * 数据流：
 * - 入参 priceList 取自详情接口 `product/product/detail` 的 `wholesalePriceList[]`（仅批发商身份返回）
 * - 阶梯优先级：当前用户等级（gradeId===currentGradeId 且 >0）有配置则用之；否则回退通用（gradeId=0）
 * - 不再按等级分组、不展示等级标签，只渲染生效阶梯的价格 + 数量门槛（对齐 SKU 弹层头部样式）
 * - 同一 tierMinQty 去重取最低 displayPrice（SPU 维度聚合多 SKU 时避免重复列）
 * - 仅 1 档时退化为「批发最低价 ~ 零售最高价」区间：
 *   最低 = 该档阶梯价（displayPrice 最低）；最高 = 不满足阶梯时的零售价 retailMaxPrice（仅当零售价 ≥ 阶梯价时作为上界，避免倒挂）
 * - 多档时横向阶梯条 + scroll-x 滑动（极端 20 档不写死宽度）
 */
import { computed, type PropType } from "vue";
import { useI18n } from "vue-i18n";
import type { WholesalePriceTier } from "@/types/wholesale/price";

const { t } = useI18n();

/** 阶梯展示项（与 WholesaleSkuPopup 顶部同源） */
interface RenderTier {
    tierMinQty: number;
    qtyText: string;
    displayPrice: string;
}
/** 展示模型（由 WholesaleSkuPopup 广播） */
interface DisplayModel {
    tiers: RenderTier[];
    singleRange: { minPrice: string; maxPrice: string; isRange: boolean } | null;
}

const props = defineProps({
    /**
     * 展示模型（优先级最高）：由 WholesaleSkuPopup 经 index 下传，保证「内外同源、随选中规格联动」
     * - 有值时直接渲染其 tiers / singleRange，忽略下方 priceList 等自算入参
     */
    model: {
        type: Object as PropType<DisplayModel | null>,
        default: null
    },
    /** 详情接口 wholesalePriceList，由父组件传入（model 缺省时的兜底自算） */
    priceList: {
        type: Array as PropType<WholesalePriceTier[]>,
        default: () => []
    },
    /** 当前用户的批发等级 ID（来自 wholesaleStore.identity.gradeId） */
    currentGradeId: {
        type: Number,
        default: 0
    },
    /**
     * 已选 SKU；
     * - 未传或 null：聚合所有 SKU 的阶梯（SPU 维度展示）
     * - 传入 skuId：仅展示该 SKU 的阶梯（含通用组 skuId=null）
     */
    selectedSkuId: {
        type: Number,
        default: null as unknown as number
    },
    /**
     * 零售最高价（一级阶梯区间上界）；
     * - 取各 SKU 原始零售价的最高值，无 SKU 时退到 SPU 零售价
     * - 仅在「单档」时用作区间上界（零售价 ≥ 阶梯价才覆盖，避免倒挂）
     */
    retailMaxPrice: {
        type: String,
        default: ""
    },
    /**
     * 代表性起批量（来自 wholesale/product/buyRule，由父组件算好传入）
     * - 首档「N 件起批」优先展示此值；为 0（未配置）时回退到阶梯首档的 tierMinQty
     */
    moq: {
        type: Number,
        default: 0
    }
});

/** 价格字号（单档区间 + 多档阶梯条共用） */
const priceFontStyle = { fontSize: "48rpx", fontWeight: "bold", color: "var(--product-price-color, #2a3145)" };
const priceCurrencyStyle = { fontSize: "28rpx", fontWeight: "bold", color: "var(--product-price-color, #2a3145)" };
const priceDecimalsStyle = { fontSize: "32rpx", fontWeight: "bold", color: "var(--product-price-color, #2a3145)" };

/**
 * 生效阶梯：优先当前等级，回退通用（gradeId=0），按 tierMinQty 升序去重
 */
const displayTiers = computed<WholesalePriceTier[]>(() => {
    const list = props.priceList || [];
    // SKU 维度过滤：传入 selectedSkuId 时仅保留对应 SKU + 通用（skuId=null）
    const filtered = props.selectedSkuId ? list.filter((t) => t.skuId === null || t.skuId === props.selectedSkuId) : list;

    // 1）当前等级阶梯（仅在 currentGradeId>0 时生效）
    let tiers = props.currentGradeId > 0 ? filtered.filter((t) => t.gradeId === props.currentGradeId) : [];
    // 2）回退通用阶梯
    if (tiers.length === 0) {
        tiers = filtered.filter((t) => t.gradeId === 0);
    }
    // 3）兜底：避免数据异常时完全不显示
    if (tiers.length === 0) {
        tiers = filtered.slice();
    }

    // 同一 tierMinQty 去重，取最低 displayPrice
    const minTier = new Map<number, WholesalePriceTier>();
    tiers.forEach((t) => {
        const exist = minTier.get(t.tierMinQty);
        if (!exist || Number(t.displayPrice) < Number(exist.displayPrice)) {
            minTier.set(t.tierMinQty, t);
        }
    });

    return Array.from(minTier.values()).sort((a, b) => a.tierMinQty - b.tierMinQty);
});

/**
 * 单档区间：最低 = 阶梯价（displayPrice），最高 = 零售价（retailMaxPrice，仅在 ≥ 阶梯价时作为上界）
 */
const singleRange = computed(() => {
    const tier = displayTiers.value[0];
    if (!tier) return null;
    const minStr = tier.displayPrice;
    const minN = Number(minStr);
    let maxStr = minStr;
    let maxN = minN;
    const retailN = props.retailMaxPrice !== "" ? Number(props.retailMaxPrice) : NaN;
    if (!Number.isNaN(retailN) && retailN >= minN) {
        maxStr = props.retailMaxPrice;
        maxN = retailN;
    }
    return {
        minPrice: minStr,
        maxPrice: maxStr,
        isRange: !Number.isNaN(minN) && !Number.isNaN(maxN) && minN !== maxN
    };
});

/**
 * 阶梯数量文案
 * - 第一级：已设起批量（moq）→「{moq}件起批」；未设 → 正常件数（单档「{N}件」/ 多档「a~b件」）
 * - 中间档：`a~b-1 件`
 * - 最后一档：`≥ a 件`
 */
const formatTierQty = (tier: WholesalePriceTier, tiers: WholesalePriceTier[]): string => {
    const idx = tiers.findIndex((item) => item.priceId === tier.priceId);
    const isLast = idx === tiers.length - 1;
    if (idx === 0) {
        if (props.moq > 0) return `${props.moq}${t("件起批")}`;
        if (isLast) return `${tier.tierMinQty}${t("件")}`;
        return `${tier.tierMinQty}~${tiers[idx + 1].tierMinQty - 1}${t("件")}`;
    }
    if (isLast) return `≥${tier.tierMinQty}${t("件")}`;
    return `${tier.tierMinQty}~${tiers[idx + 1].tierMinQty - 1}${t("件")}`;
};

/**
 * 最终渲染阶梯：优先用外部传入的 model（与半窗同源），否则回退到本组件自算（兼容无 model 的旧用法）
 */
const renderTiers = computed<RenderTier[]>(() => {
    if (props.model && Array.isArray(props.model.tiers)) return props.model.tiers;
    return displayTiers.value.map((t) => ({
        tierMinQty: t.tierMinQty,
        qtyText: formatTierQty(t, displayTiers.value),
        displayPrice: t.displayPrice
    }));
});

/** 最终单档区间：model 优先，否则回退自算 singleRange */
const renderSingle = computed(() => {
    if (props.model) return props.model.singleRange;
    return singleRange.value;
});
</script>

<style lang="scss" scoped>
.wholesale-price-table {
    background: var(--product-content-bg, #fff);
    border-radius: 12rpx;
    padding: 0 28rpx 20rpx;

    // 多规格区间：价格区间 + 数量同行（基线对齐）
    .tier-range {
        display: flex;
        align-items: baseline;

        .tier-range-price {
            display: flex;
            align-items: baseline;

            .range-sep {
                margin: 0 28rpx;
                font-size: 30rpx;
                font-weight: bold;
                color: var(--product-price-color, #2a3145);
            }
        }

        .tier-range-qty {
            margin-left: 12rpx;
            font-size: 24rpx;
            color: #999;
        }

        // 单价（单规格 / 多规格价格相同）：与多档一致，上价格、下「≥N件」竖排，价格左对齐、数量居中
        &.vertical {
            display: inline-flex; // 收缩到内容（价格）宽度，避免撑满整行导致数量按全宽居中
            flex-direction: column;
            align-items: flex-start;

            .tier-range-qty {
                align-self: center;
                margin-left: 0;
                margin-top: 6rpx;
            }
        }
    }

    .tier-scroll {
        flex: 1;
        white-space: nowrap;
        overflow-x: auto;
    }

    .tier-list {
        display: inline-flex;
        flex-wrap: nowrap;
        gap: 80rpx;
    }

    .tier-item {
        display: inline-flex;
        flex-direction: column;
        align-items: center;

        // 第一档价格贴左：内容左对齐，与卡片左边缘对齐
        &:first-child {
            align-items: flex-start;
        }

        .tier-qty {
            align-self: center; // 数量文案始终居中（第一档价格贴左，数量仍居中）
            margin-top: 6rpx;
            font-size: 24rpx;
            color: #999;
        }
    }
}
</style>
