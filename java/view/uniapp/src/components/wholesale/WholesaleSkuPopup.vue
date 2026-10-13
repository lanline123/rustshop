<template>
    <tig-popup v-model:show="show" :show-close="false" position="bottom" :z-index="9999" @close="handleClose">
        <view class="wholesale-sku-popup">
            <!-- 顶部：商品图 + 多档价 + 关闭 -->
            <view class="popup-head">
                <view class="product-thumb">
                    <tig-image :src="productImage" mode="aspectFill" />
                </view>
                <view class="head-price-wrap">
                    <scroll-view scroll-x="true" class="head-price-scroll" :show-scrollbar="false" :scroll-left="headScrollLeft" scroll-with-animation>
                        <view class="head-price-list">
                            <!-- 单档：退化为「批发最低价 ~ 零售最高价」区间 -->
                            <view
                                v-if="headTiers.length === 1 && headSingleRange"
                                class="head-price-item head-price-primary"
                                :class="{ 'head-range-inline': headSingleRange.isRange }"
                            >
                                <view class="head-price-val">
                                    <format-price
                                        :price-data="headSingleRange.minPrice"
                                        :show-text="false"
                                        :font-style="headPriceStyle(0).font"
                                        :currency-style="headPriceStyle(0).currency"
                                        :decimals-style="headPriceStyle(0).decimals"
                                    />
                                    <template v-if="headSingleRange.isRange">
                                        <text class="head-range-sep">~</text>
                                        <format-price
                                            :price-data="headSingleRange.maxPrice"
                                            :show-text="false"
                                            :font-style="headPriceStyle(0).font"
                                            :currency-style="headPriceStyle(0).currency"
                                            :decimals-style="headPriceStyle(0).decimals"
                                        />
                                    </template>
                                </view>
                                <view class="head-price-qty active">{{ headTiers[0].qtyText }}</view>
                            </view>
                            <!-- 多档：横向阶梯条 -->
                            <template v-else>
                                <view
                                    v-for="(tier, idx) in headTiers"
                                    :key="tier.tierMinQty"
                                    class="head-price-item"
                                    :class="{ 'head-price-primary': idx === hitTierIndex }"
                                >
                                    <view class="head-price-val">
                                        <format-price
                                            :price-data="tier.displayPrice"
                                            :show-text="false"
                                            :font-style="headPriceStyle(idx).font"
                                            :currency-style="headPriceStyle(idx).currency"
                                            :decimals-style="headPriceStyle(idx).decimals"
                                        />
                                    </view>
                                    <view class="head-price-qty" :class="{ active: idx === hitTierIndex }">{{ tier.qtyText }}</view>
                                </view>
                            </template>
                        </view>
                    </scroll-view>
                    <view class="head-price-fade" />
                </view>
                <view class="popup-close" @click="handleClose">
                    <uni-icons type="closeempty" size="20" color="#c8c9cc" />
                </view>
            </view>

            <!-- 规格区 -->
            <scroll-view scroll-y="true" class="popup-body">
                <!-- 前 N-1 维：图块 / 标签选择器（多维度时启用） -->
                <template v-if="frontDims.length > 0">
                    <view v-for="(dim, dimIdx) in frontDims" :key="dim.specId" class="dim-section">
                        <view class="dim-title">{{ dim.specName }}</view>
                        <view class="dim-options">
                            <view
                                v-for="opt in dim.options"
                                :key="opt.optionId"
                                class="dim-option"
                                :class="{ active: frontSelection[dim.specId] === opt.optionId }"
                                @click="selectFrontOption(dim.specId, opt.optionId)"
                            >
                                <!-- 已选数量角标：仅前置维度第一类规格展示 -->
                                <text v-if="dimIdx === 0 && getOptionBadgeCount(dim.specName, opt.label) > 0" class="dim-badge">
                                    x{{ getOptionBadgeCount(dim.specName, opt.label) }}
                                </text>
                                <view v-if="opt.image" class="dim-option-img">
                                    <tig-image :src="opt.image" mode="aspectFill" />
                                </view>
                                <text class="dim-option-label">{{ opt.label }}</text>
                            </view>
                        </view>
                    </view>
                </template>

                <!-- 末维列表（SKU 行）：始终展示末维标题 -->
                <view class="sku-section">
                    <view v-if="lastDimPicks.length > 0" class="dim-title">{{ lastDimName }}</view>
                    <view v-for="pick in lastDimPicks" :key="pick.skuId" class="sku-row">
                        <view v-if="pick.thumb" class="sku-thumb">
                            <tig-image :src="pick.thumb" mode="aspectFill" />
                            <text v-if="(pickedQty[pick.skuId] || 0) > 0" class="sku-thumb-badge">x{{ pickedQty[pick.skuId] }}</text>
                        </view>
                        <view class="sku-info">
                            <view class="sku-name line2">{{ pick.skuName }}</view>
                            <view class="sku-meta">
                                <format-price
                                    class="sku-price"
                                    :price-data="rowUnitPrice(pick.skuId, pick.unitPrice)"
                                    :show-text="false"
                                    :font-style="{ fontSize: '24rpx', color: '#999' }"
                                    :currency-style="{ fontSize: '20rpx', color: '#999' }"
                                    :decimals-style="{ fontSize: '22rpx', color: '#999' }"
                                />
                                <text class="sku-divider">|</text>
                                <text class="sku-stock">{{ $t("库存") }}{{ pick.stockText }}</text>
                            </view>
                        </view>
                        <wholesale-stepper
                            :value="pickedQty[pick.skuId] || 0"
                            :moq="pick.moqSku"
                            :multiple="pick.multiple"
                            :max="pick.stock"
                            :disabled="pick.stock === 0"
                            @update:value="(v: number) => onQtyInput(pick.skuId, v)"
                            @change="onQtyCommitted"
                        />
                    </view>
                </view>
            </scroll-view>

            <!-- 底部：合计 + 操作按钮（列表态双按钮 / 详情态单确定） -->
            <view class="popup-footer">
                <view class="footer-summary">
                    <view class="summary-text">
                        <text v-if="selectedSkuCount > 0"
                            >{{ $t("已选") }}<text class="summary-num">{{ selectedSkuCount }}</text
                            >{{ $t("款") }}<text class="summary-num">{{ totalQty }}</text
                            >{{ $t("个") }}</text
                        >
                    </view>
                    <view class="summary-amount">
                        <text class="amount-label">{{ $t("商品金额") }}:</text>
                        <format-price
                            :price-data="totalAmountText"
                            :show-text="false"
                            :font-style="{ fontSize: '40rpx', fontWeight: 'bold', color: 'var(--price)' }"
                            :currency-style="{ fontSize: '22rpx', fontWeight: 'bold', color: 'var(--price)' }"
                            :decimals-style="{ fontSize: '24rpx', fontWeight: 'bold', color: 'var(--price)' }"
                        />
                    </view>
                </view>
                <!-- 列表加购：双按钮 -->
                <view v-if="listMode" class="footer-actions">
                    <view class="footer-btn-box">
                        <tig-button2
                            :disabled="isFooterDisabled"
                            :delay="300"
                            style="height: 70rpx; border-color: #505259; border-width: 2px"
                            @click="handleConfirm(false)"
                        >
                            {{ submittingAction === "cart" ? $t("处理中...") : $t("批量加入购物车") }}
                        </tig-button2>
                    </view>
                    <view class="footer-btn-box">
                        <tig-button2 :disabled="isFooterDisabled" :delay="300" style="height: 70rpx" theme @click="handleConfirm(true)">
                            {{ submittingAction === "buy" ? $t("处理中...") : $t("立即购买") }}
                        </tig-button2>
                    </view>
                </view>
                <!-- 详情页：单确定（由唤起方 isQuick 决定加购或立即购买） -->
                <view v-else class="footer-single">
                    <tig-button2 :disabled="submitting" :delay="300" style="height: 70rpx" theme @click="handleConfirm">
                        {{ submitting ? $t("处理中...") : $t("确定") }}
                    </tig-button2>
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
/**
 * @description 批发 SKU 选购半屏弹层
 *
 * UI 形态：
 * - 顶部：商品缩略图 + 多档价并列（首档突出，标"起"+"预估单价"）+ 关闭按钮
 * - 规格区：前 N-1 维做图块/标签选择器；末维做 SKU 列表行（每行步进器）
 * - 底部：已选 K 款 J 个 / 商品金额 ¥X + 确定按钮（红色圆角）
 *
 * 业务规则：
 * - 默认所有 SKU 数量为 0（混批多 SKU 场景，0=不买）
 * - 合法值 {0} ∪ {≥moq 的 multiple 整数倍}（由 WholesaleStepper 封装）
 * - 切换前置维度时 pickedQty 全局保持（跨维混批）
 * - 实时合计 K 款 J 个 + 商品金额（取 wholesalePriceList 对应 SKU 当前命中档的 displayPrice）
 * - 确定 → V1~V4 本地校验 → 批量加车 cart/cart/addToCart（一次请求带 skuItem 数组）→ 角标刷新
 * - 后端按身份判定批发价并回业务码，message 直接 Toast 透传
 */
import { ref, computed, watch, nextTick, getCurrentInstance, type PropType } from "vue";
import { useI18n } from "vue-i18n";
import WholesaleStepper from "./WholesaleStepper.vue";
import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";
import { getWholesaleBuyRule } from "@/api/wholesale/product";
import { addToCart, getProductAmount } from "@/api/product/product";
import { useCartStore } from "@/store/cart";
import { isWholesaleBusinessError, getRepresentativeMoq, WHOLESALE_MOQ_NOT_MET_HINT } from "@/utils/wholesale";
import type { BuyRule } from "@/types/wholesale/buyRule";
import type { WholesalePriceTier } from "@/types/wholesale/price";
import type { ProductItem, SkuList, AttrList, ProductAmountItem } from "@/types/product/product";

const { t } = useI18n();

interface DimOption {
    optionId: number;
    label: string;
    image?: string;
}
interface FrontDim {
    specId: number;
    specName: string;
    options: DimOption[];
}
interface SkuPick {
    skuId: number;
    skuName: string;
    thumb: string;
    /** 单价文案（去币符），取自 wholesalePriceList 中该 SKU 当前命中档的 displayPrice */
    unitPrice: string;
    stock: number;
    stockText: string;
    /** SKU 起订量门槛 */
    moqSku: number;
    /** SKU 倍数 */
    multiple: number;
}

const props = defineProps({
    /** 商品基本信息（取自 product/product/detail.item） */
    product: {
        type: Object as PropType<ProductItem>,
        required: true
    },
    /** SKU 列表（取自 product/product/detail.skuList） */
    skuList: {
        type: Array as PropType<SkuList[]>,
        default: () => []
    },
    /** 规格属性表（取自 product/product/detail.attrList） */
    attrList: {
        type: Object as PropType<AttrList>,
        default: () => ({ normal: [], spe: [] })
    },
    /** 批发阶梯价（取自 product/product/detail.wholesalePriceList） */
    priceList: {
        type: Array as PropType<WholesalePriceTier[]>,
        default: () => []
    },
    /** 当前用户的批发等级 ID */
    currentGradeId: {
        type: Number,
        default: 0
    },
    /**
     * 批发多规格定价模式（详情接口 wholesalePriceMode）：1=混批（按全规格总数高亮）/ 2=SKU级（顶部不高亮）/ null=未配置
     */
    wholesalePriceMode: {
        type: Number as PropType<number | null>,
        default: null
    },
    /** 起批规则（来自 wholesale/product/buyRule；父组件传入则不再内部拉取） */
    buyRule: {
        type: Object as PropType<BuyRule | null>,
        default: null
    },
    /**
     * 列表加购态：true=底部展示「批量加入购物车 + 立即购买」（对齐零售 specification isBatch）
     * false=详情页唤起，底部仅「确定」（由 handleShowPopup(isQuick) 决定行为）
     */
    listMode: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits<{
    /** 加车成功（successCount > 0），父组件可刷新角标 */
    (e: "add-success", successCount: number): void;
    /** 关闭弹层 */
    (e: "close"): void;
    /** 当前展示阶梯模型变化：供详情页外部阶梯表同步渲染（内外逻辑一致、随选中规格联动） */
    (e: "display-change", model: WholesaleDisplayModel): void;
}>();

// =============== 弹层显隐 ===============
const show = ref(false);
/** 是否「立即购买」模式：true=确定后加车并直达结算页；false=仅加入购物车 */
const quickBuy = ref(false);

/**
 * 暴露给父组件唤起（与现网 specification.vue 一致约定）
 * @param isQuick true=立即购买（确定=快速购买，加车后进结算页）；false=加入购物车
 */
const handleShowPopup = (isQuick = false) => {
    quickBuy.value = isQuick;
    show.value = true;
    // 弹层打开时拉一次起批规则（如未拉过）
    if (!buyRule.value) loadBuyRule();
    // 弹层渲染完成后把命中档滚动到视口中间
    setTimeout(() => centerHitTier(), 200);
};
const handleClose = () => {
    show.value = false;
    submitting.value = false;
    submittingAction.value = null;
    // 关闭后重置已选购买数量与预览金额（下次打开从 0 开始）
    pickedQty.value = {};
    activeSkuId.value = null;
    productAmount.value = { total: "0", count: 0 };
    if (amountTimer) {
        clearTimeout(amountTimer);
        amountTimer = null;
    }
    emit("close");
};
defineExpose({ handleShowPopup });

// =============== 起批规则（仅起批规则不含金额；优先用父组件传入，未传则内部兜底拉取） ===============
const internalBuyRule = ref<BuyRule | null>(null);
const buyRule = computed<BuyRule | null>(() => props.buyRule ?? internalBuyRule.value);
const loadBuyRule = async () => {
    try {
        const res = await getWholesaleBuyRule(props.product.productId);
        internalBuyRule.value = res as unknown as BuyRule;
    } catch (err) {
        console.error("[wholesale-sku] loadBuyRule failed:", err);
    }
};

/** 商品首张图 */
const productImage = computed(() => props.product?.picThumb || props.product?.picUrl || "");

// =============== 顶部多档价 ===============
/** 顶部单档展示模型 */
interface HeadTier {
    tierMinQty: number;
    /** 数量文案：首档「N件起批」/ 末档「≥N件」/ 中间档「a-b件」 */
    qtyText: string;
    /** 该档代表价（原始 displayPrice 字符串，交 format-price 渲染） */
    displayPrice: string;
}

/** 对外广播给详情页外部阶梯表的展示模型（与半窗顶部完全同源） */
interface WholesaleDisplayModel {
    /** 阶梯列表（当前展示 SKU 的阶梯，随选中规格联动） */
    tiers: HeadTier[];
    /** 单档区间（仅 tiers.length===1 时有值；价格相同 isRange=false 即单价） */
    singleRange: { minPrice: string; maxPrice: string; isRange: boolean } | null;
}

// 顶部多档价的 computed（headTiers / headSingleRange / hitTierIndex 等）依赖
// 「前置选中组的可见 SKU」(lastDimPicks)，统一移至 lastDimPicks 定义之后（见下方「顶部多档价（随选中规格联动）」段）。

// =============== 规格维度结构（前 N-1 维 + 末维列表） ===============
const dimensions = computed<FrontDim[]>(() => {
    const spe = props.attrList?.spe || [];
    // specId 用维度下标（attrName 是规格中文名，Number() 会得 NaN→0，多维度会全部冲突成 0）
    return spe.map((d, idx) => {
        const options: DimOption[] = (d.attrList || []).map((opt) => ({
            optionId: opt.attributesId,
            label: opt.attrValue,
            image: opt.attrPicThumb || undefined
        }));
        return {
            specId: idx,
            specName: String(d.attrName || t("规格")),
            options
        };
    });
});

/** 前 N-1 维（多维度时启用，单维度时为空） */
const frontDims = computed(() => dimensions.value.slice(0, -1));
/** 末维名称（用作 SKU 列表区标题）；统一规格无维度时用「数量」 */
const lastDimName = computed(() => {
    const last = dimensions.value[dimensions.value.length - 1];
    if (last) return last.specName || t("规格");
    return t("数量");
});

/** 前置维度单选状态 specId → optionId */
const frontSelection = ref<Record<number, number>>({});

/** 初始默认前置维度选择（每维默认选第一个选项） */
watch(
    () => dimensions.value,
    (dims) => {
        if (dims.length <= 1) return;
        const next: Record<number, number> = { ...frontSelection.value };
        dims.slice(0, -1).forEach((d) => {
            if (next[d.specId] == null && d.options.length > 0) {
                next[d.specId] = d.options[0].optionId;
            }
        });
        frontSelection.value = next;
    },
    { immediate: true }
);

const selectFrontOption = (specId: number, optionId: number) => {
    frontSelection.value = { ...frontSelection.value, [specId]: optionId };
};

/**
 * 某前置维度选项的已选数量合计
 * - 累加所有 skuData 命中「规格名 + 值」的 SKU 当前已选数量
 * - 仅第一类前置规格在模板里调用展示
 */
const getOptionBadgeCount = (specName: string, optionLabel: string): number => {
    let sum = 0;
    const skus = props.skuList || [];
    for (const sku of skus) {
        const qty = pickedQty.value[sku.skuId] || 0;
        if (qty <= 0) continue;
        const hasMatch = (sku.skuData || []).some((sd) => String(sd.name) === String(specName) && String(sd.value) === String(optionLabel));
        if (hasMatch) sum += qty;
    }
    return sum;
};

/**
 * 末维 SKU 列表（按前置维度过滤；单维度时返回全部 SKU）
 * - 每个 SKU 计算单价（取 wholesalePriceList 中该 SKU 命中档的 displayPrice）
 * - 计算 moqSku 和 multiple（来自 buyRule.skuRules）
 */
const lastDimPicks = computed<SkuPick[]>(() => {
    const skus = props.skuList || [];
    // 统一规格：无 SKU 记录，价格挂在 skuId=0（通用行），合成单行使其可填数量
    if (skus.length === 0) {
        const rule0 = buyRule.value?.skuRules?.find((r) => r.skuId === 0);
        const repMoq = buyRule.value?.moq || 0;
        const p = props.product;
        const stock = Number(p?.productStock ?? 0);
        return [
            {
                skuId: 0,
                skuName: p?.productName || t("默认"),
                thumb: productImage.value,
                unitPrice: String(p?.productPrice ?? "0"),
                stock,
                stockText: stock > 0 ? t("充足") : t("无货"),
                moqSku: rule0?.moqSku && rule0.moqSku > 0 ? rule0.moqSku : repMoq,
                multiple: rule0?.multiple ?? 1
            }
        ];
    }
    const dims = dimensions.value;
    const filtered = skus.filter((sku) => {
        if (dims.length <= 1) return true;
        // 多维度时，前置维度全部匹配才显示
        const skuData = sku.skuData || [];
        return dims.slice(0, -1).every((dim) => {
            const selectedOptId = frontSelection.value[dim.specId];
            if (!selectedOptId) return true;
            const opt = dim.options.find((o) => o.optionId === selectedOptId);
            if (!opt) return false;
            // 规格名 + 值双重匹配，避免不同维度同值串台
            return skuData.some((sd) => String(sd.name) === dim.specName && String(sd.value) === String(opt.label));
        });
    });
    // SKU 维度每规格起订量：skuRules 缺省时回退到代表性 moq（后端可能只下发 moq、不下发 skuRules）
    const isSkuDim = buyRule.value?.dimension === 2;
    const repMoq = buyRule.value?.moq || 0;
    return filtered.map((sku) => {
        const skuId = sku.skuId;
        const rule = buyRule.value?.skuRules?.find((r) => r.skuId === skuId);
        const moqSku = rule?.moqSku && rule.moqSku > 0 ? rule.moqSku : isSkuDim ? repMoq : 0;
        return {
            skuId,
            skuName: buildSkuName(sku),
            thumb: buildSkuThumb(sku),
            unitPrice: String(sku.skuPrice ?? sku.price ?? "0"),
            stock: sku.skuStock ?? sku.stock ?? 0,
            stockText: (sku.skuStock ?? sku.stock ?? 0) > 0 ? t("充足") : t("无货"),
            moqSku,
            multiple: rule?.multiple ?? 1
        };
    });
});

/**
 * 末维行名：只取末维这一段的值（如「天空蓝」），不叠加前置已选维度（前置维度已在上方选择区体现）
 * - 兜底：拿不到末维分段时回退完整 skuValue / 全部分段拼接
 */
const buildSkuName = (sku: SkuList): string => {
    const lastSpecName = lastDimName.value;
    const seg = (sku.skuData || []).find((d) => String(d.name) === lastSpecName);
    if (seg) return String(seg.value);
    if (sku.skuValue) return sku.skuValue;
    if (sku.skuData && sku.skuData.length > 0) {
        return sku.skuData.map((d) => d.value).join(" / ");
    }
    // 单 SKU 无规格：回退商品名，与统一规格合成行保持一致
    return sku.title || props.product?.productName || t("默认");
};

/**
 * 末维行缩略图：只取末维规格项自己配置的图片，未配置则为空（不回退商品主图，行内 v-if 隐藏）
 */
const buildSkuThumb = (sku: SkuList): string => {
    const lastDim = dimensions.value[dimensions.value.length - 1];
    if (!lastDim) return "";
    const seg = (sku.skuData || []).find((d) => String(d.name) === lastDim.specName);
    if (!seg) return "";
    const opt = lastDim.options.find((o) => String(o.label) === String(seg.value));
    return opt?.image || "";
};

// =============== 顶部价格阶梯（展示当前 SKU 的阶梯，随加购联动） ===============
/** 当前驱动阶梯展示的 SKU（默认前置选中组首个；点击某 SKU 加购后切到该 SKU） */
const activeSkuId = ref<number | null>(null);

/** 实际用于展示阶梯的 SKU：activeSkuId 在当前可见组内则用之，否则回退到组内首个（切换前置规格自动复位为首个） */
const displaySkuId = computed<number | null>(() => {
    const picks = lastDimPicks.value;
    if (!picks.length) return null;
    if (activeSkuId.value != null && picks.some((p) => p.skuId === activeSkuId.value)) return activeSkuId.value;
    return picks[0].skuId;
});

/** 当前前置选中组的可见 SKU id 集合（多档阶梯展示联动用） */
const visibleSkuIds = computed<Set<number>>(() => new Set(lastDimPicks.value.map((p) => p.skuId)));

/** 商品全部 SKU id（多规格单阶梯区间在此全集内求 min~max，不随前置规格变化） */
const productSkuIds = computed<number[]>(() => (props.skuList || []).map((s) => s.skuId).filter((id): id is number => id != null && id !== 0));

/** 商品是否多规格（用于多档阶梯首档起批文案 / 单档区间判定）：整商品 SKU 数 > 1 */
const isMultiSpec = computed(() => (props.skuList?.length || 0) > 1);

/** 顶部阶梯归组模型（单 SKU，每档单价 + 数量文案） */
interface GroupTier {
    tierMinQty: number;
    /** 该档批发价（原始串） */
    priceStr: string;
    /** 数量文案 */
    qtyText: string;
}

/**
 * 顶部阶梯：仅展示 displaySkuId 这一个 SKU 的阶梯（含通用行 skuId=null/0），默认前置组首个、加购某 SKU 后切到该 SKU；
 * 再按等级（专享→回退通用 gradeId=0）过滤；按 tierMinQty 去重升序，每档取最低批发价（单 SKU 下通常唯一）。
 * - 数量文案：单档「≥N件」；多档多规格首档按 moq 与首档门槛比较；其余 a-b件 / ≥N件（见下）
 */
const groupTiers = computed<GroupTier[]>(() => {
    const all = props.priceList || [];
    if (!all.length) return [];
    const sid = displaySkuId.value;
    const skuFiltered = all.filter((t) => t.skuId == null || t.skuId === 0 || t.skuId === sid);
    if (!skuFiltered.length) return [];
    const gid = props.currentGradeId;
    let graded = gid > 0 ? skuFiltered.filter((t) => t.gradeId === gid) : [];
    if (!graded.length) graded = skuFiltered.filter((t) => t.gradeId === 0);
    if (!graded.length) graded = skuFiltered.slice();
    const byQty = new Map<number, string>();
    for (const t of graded) {
        if (t.tierMinQty == null || t.displayPrice == null) continue;
        const n = Number(t.displayPrice);
        if (Number.isNaN(n)) continue;
        const exist = byQty.get(t.tierMinQty);
        if (exist == null || n < Number(exist)) byQty.set(t.tierMinQty, t.displayPrice);
    }
    const qtys = Array.from(byQty.keys()).sort((a, b) => a - b);
    const repMoq = getRepresentativeMoq(buyRule.value);
    return qtys.map((qty, idx) => ({
        tierMinQty: qty,
        priceStr: byQty.get(qty)!,
        qtyText: buildHeadQtyText(idx, qtys, repMoq)
    }));
});

/**
 * 顶部阶梯数量文案
 * - 第一级：已设起批量 → 「{moq}件起批」；未设 → 正常件数（单档「{qty}件」/ 多档「a-b件」）
 * - 中间档：「a-b件」；末档：「≥N件」
 * - 区间展示（单档多规格）同样取第一级文案，跟在区间价后展示
 */
const buildHeadQtyText = (idx: number, qtys: number[], moq: number): string => {
    const qty = qtys[idx];
    const isFirst = idx === 0;
    const isLast = idx === qtys.length - 1;
    if (isFirst) {
        if (moq > 0) return `${moq}${t("件起批")}`;
        if (isLast) return `${qty}${t("件")}`;
        const upper = qtys[idx + 1] - 1;
        return upper > qty ? `${qty}-${upper}${t("件")}` : `${qty}${t("件")}`;
    }
    if (isLast) return `≥${qty}${t("件")}`;
    const upper = qtys[idx + 1] - 1;
    return upper > qty ? `${qty}-${upper}${t("件")}` : `${qty}${t("件")}`;
};

/** 顶部多档价展示列表（直接由 groupTiers 派生） */
const headTiers = computed<HeadTier[]>(() => groupTiers.value.map((g) => ({ tierMinQty: g.tierMinQty, qtyText: g.qtyText, displayPrice: g.priceStr })));

/**
 * 单档展示：商品级每 SKU 恰好一档 → 跨全商品 SKU 取 min~max（不限 tierMinQty、不随前置规格变化）；价格相同则单价
 */
const headSingleRange = computed(() => {
    const tiers = groupTiers.value;
    const g = tiers[0];
    if (!g) return null;
    if (tiers.length !== 1 || !isMultiSpec.value) {
        return { minPrice: g.priceStr, maxPrice: g.priceStr, isRange: false };
    }
    const all = props.priceList || [];
    const pSkuIds = productSkuIds.value;
    const gid = props.currentGradeId;
    let graded = gid > 0 ? all.filter((t) => t.gradeId === gid) : [];
    if (!graded.length) graded = all.filter((t) => t.gradeId === 0);
    if (!graded.length) graded = all.slice();
    const productSkuTierRows = graded.filter(
        (t) => t.skuId != null && t.skuId !== 0 && pSkuIds.includes(t.skuId as number) && t.tierMinQty != null && t.displayPrice != null
    );
    const tierCountBySku = new Map<number, number>();
    for (const t of productSkuTierRows) {
        const skuId = t.skuId as number;
        tierCountBySku.set(skuId, (tierCountBySku.get(skuId) || 0) + 1);
    }
    const isProductSingleTier = pSkuIds.length > 0 && pSkuIds.every((skuId) => tierCountBySku.get(skuId) === 1);

    let minStr = g.priceStr;
    let minN = Number(minStr);
    let maxStr = minStr;
    let maxN = minN;
    const visible = visibleSkuIds.value;
    const rowsForRange = isProductSingleTier
        ? productSkuTierRows
        : graded.filter((t) => {
              if (t.tierMinQty !== g.tierMinQty || t.displayPrice == null) return false;
              return t.skuId == null || t.skuId === 0 || visible.has(t.skuId as number);
          });
    for (const t of rowsForRange) {
        const n = Number(t.displayPrice);
        if (Number.isNaN(n)) continue;
        if (n < minN) {
            minN = n;
            minStr = t.displayPrice;
        }
        if (n > maxN) {
            maxN = n;
            maxStr = t.displayPrice;
        }
    }
    const isRange = maxN > minN;
    return { minPrice: minStr, maxPrice: isRange ? maxStr : minStr, isRange };
});

/**
 * 对外展示模型：详情页外部阶梯表(WholesalePriceTable)据此渲染，确保「内外同源、随选中规格联动」
 * - 默认即前置默认选中组合首个 SKU 的阶梯；半窗加购切换 displaySkuId 后，外部同步变化
 */
const displayModel = computed<WholesaleDisplayModel>(() => ({
    tiers: headTiers.value,
    singleRange: headTiers.value.length === 1 ? headSingleRange.value : null
}));
watch(displayModel, (m) => emit("display-change", m), { immediate: true, deep: true });

// =============== 选数态（pickedQty 全局保持，跨前置维度不清空） ===============
const pickedQty = ref<Record<number, number>>({});

/** 手输过程中仅同步数量（不拉金额接口）；+/- 与失焦确认后走 onQtyCommitted */
const onQtyInput = (skuId: number, qty: number) => {
    pickedQty.value = { ...pickedQty.value, [skuId]: qty };
    // 加购某 SKU → 顶部阶梯条切换为该 SKU 的价格阶梯
    activeSkuId.value = skuId;
};

/** 数量已确认（步进器 +/- 或手输失焦吸附后）：防抖拉取后端实时金额 */
const onQtyCommitted = () => {
    scheduleFetchAmount();
};

/** 已选 SKU 款数（qty > 0 的数量） */
const selectedSkuCount = computed(() => {
    return Object.values(pickedQty.value).filter((q) => q > 0).length;
});

/** 总件数 */
const totalQty = computed(() => {
    return Object.values(pickedQty.value).reduce((sum, q) => sum + (q || 0), 0);
});

// =============== 商品金额（后端实时预览，getProductAmount，前端不自算） ===============
/** 后端实时预览金额；批发命中后 total 即批发口径（命中行批发价小计 + 未命中行零售价小计） */
const productAmount = ref<ProductAmountItem>({ total: "0", count: 0 });

/**
 * 拉取后端实时金额
 * - 入参复用批量 skuItem（id + skuItem[{ skuId, num }]），后端按身份判定批发价
 * - 未选数时直接归零，不发请求
 */
const fetchProductAmount = async () => {
    const skuItem = Object.entries(pickedQty.value)
        .filter(([, q]) => q > 0)
        .map(([skuIdStr, qty]) => ({ skuId: Number(skuIdStr), num: qty }));
    if (skuItem.length === 0) {
        productAmount.value = { total: "0", count: 0 };
        return;
    }
    try {
        const res = (await getProductAmount({ id: props.product.productId, skuItem })) as unknown as ProductAmountItem;
        productAmount.value = res || { total: "0", count: 0 };
    } catch (err) {
        console.error("[wholesale-sku] getProductAmount failed:", err);
    }
};

/** 100ms 防抖：连续点 +/- 时只发最后一次请求，规避频繁请求 */
let amountTimer: ReturnType<typeof setTimeout> | null = null;
const scheduleFetchAmount = () => {
    if (amountTimer) clearTimeout(amountTimer);
    amountTimer = setTimeout(fetchProductAmount, 200);
};

/** 合计金额文案（去币符，交 format-price 渲染）；未选数时为 0.00 */
const totalAmountText = computed(() => {
    if (totalQty.value <= 0) return "0.00";
    const t = productAmount.value?.total;
    if (t == null || t === "") return "0.00";
    const n = Number(t);
    return Number.isNaN(n) ? String(t) : n.toFixed(2);
});

/**
 * 当前命中的顶部价格档索引（顶部阶梯始终对应当前展示 SKU displaySkuId）
 * - 混批（wholesalePriceMode=1）：按「全规格已选总数」命中「最大 tierMinQty ≤ 总数」的档
 * - SKU 级（=2）/ 未配置（null）：按「当前展示 SKU 自身数量」命中（阶梯本就只展示该 SKU）
 * - 命中数量为 0：不高亮（-1）
 */
const hitTierIndex = computed(() => {
    const tiers = headTiers.value;
    if (tiers.length === 0) return -1;
    const sid = displaySkuId.value;
    const qty = Number(props.wholesalePriceMode) === 1 ? totalQty.value : sid != null ? pickedQty.value[sid] || 0 : 0;
    if (qty <= 0) return -1;
    let hit = -1;
    tiers.forEach((t, i) => {
        if (qty >= t.tierMinQty) hit = i;
    });
    return hit;
});

/**
 * 末维行单价：按当前数量命中的阶梯批发价（随量联动；返回原始价格串交 format-price 渲染）
 * - 取该 SKU 阶梯行（含通用行 skuId=null/0），等级过滤 + 同档去重取最低，按 tierMinQty 升序
 * - 混批（wholesalePriceMode=1）：按全规格已选总数匹配；SKU 级/未配置：按本行数量
 * - 数量 0 → 首档批发价；无阶梯数据 → 回退零售价 fallback
 */
const rowUnitPrice = (skuId: number, fallback: string): string => {
    const all = props.priceList || [];
    if (!all.length) return fallback;
    const skuFiltered = all.filter((t) => t.skuId == null || t.skuId === 0 || t.skuId === skuId);
    const gid = props.currentGradeId;
    let graded = gid > 0 ? skuFiltered.filter((t) => t.gradeId === gid) : [];
    if (!graded.length) graded = skuFiltered.filter((t) => t.gradeId === 0);
    if (!graded.length) graded = skuFiltered.slice();
    const byQty = new Map<number, string>();
    for (const t of graded) {
        if (t.tierMinQty == null || t.displayPrice == null) continue;
        const n = Number(t.displayPrice);
        if (Number.isNaN(n)) continue;
        const exist = byQty.get(t.tierMinQty);
        if (exist == null || n < Number(exist)) byQty.set(t.tierMinQty, t.displayPrice);
    }
    const qtys = Array.from(byQty.keys()).sort((a, b) => a - b);
    if (!qtys.length) return fallback;
    const matchQty = Number(props.wholesalePriceMode) === 1 ? totalQty.value : pickedQty.value[skuId] || 0;
    let hitQty = qtys[0];
    if (matchQty > 0) {
        for (const q of qtys) {
            if (matchQty >= q) hitQty = q;
        }
    }
    return byQty.get(hitQty) ?? fallback;
};

/**
 * 顶部多档价样式（命中档仅变红、不放大；交给 format-price 的 *-style 入参）
 */
const headPriceStyle = (idx: number) => {
    const color = idx === hitTierIndex.value ? "var(--price)" : "#333";
    return {
        font: { fontSize: "40rpx", fontWeight: "bold", color },
        currency: { fontSize: "24rpx", fontWeight: "bold", color },
        decimals: { fontSize: "24rpx", fontWeight: "bold", color }
    };
};

/** 顶部多档价横向滚动位置（带动画，将命中档滚动到视口中间） */
const headScrollLeft = ref(0);
const instance = getCurrentInstance();

/**
 * 命中档不在视口时，把它平滑滚动到视口中间
 * - 用 selectorQuery 量取容器与各档位置，算出居中所需 scrollLeft
 * - 序列号守卫：快速连点会并发多次异步量取，回调可能乱序返回；仅最新一次、且命中档未再变化时才应用，避免旧档覆盖最终位置
 */
let centerSeq = 0;
const centerHitTier = () => {
    const idx = hitTierIndex.value;
    if (idx < 0) return;
    const seq = ++centerSeq;
    nextTick(() => {
        const query = uni.createSelectorQuery().in(instance);
        query.select(".head-price-scroll").boundingClientRect();
        query.select(".head-price-scroll").scrollOffset(() => {});
        query.selectAll(".head-price-item").boundingClientRect();
        query.exec((res: any) => {
            // 已有更新的量取请求 或 命中档已再次变化 → 丢弃本次过期结果
            if (seq !== centerSeq || idx !== hitTierIndex.value) return;
            const container = res[0];
            const scroll = res[1];
            const items = res[2];
            if (!container || !scroll || !Array.isArray(items) || !items[idx]) return;
            const item = items[idx];
            // 命中档左缘在内容坐标系内的偏移 = 视口内相对偏移 + 已滚动距离
            const itemLeftInContent = item.left - container.left + scroll.scrollLeft;
            const target = itemLeftInContent - (container.width - item.width) / 2;
            headScrollLeft.value = Math.max(0, Math.round(target));
        });
    });
};

// 命中档变化时（选数跨档）滚动居中；连点防抖，等数量 settle 后只量取最终一次
let centerTimer: ReturnType<typeof setTimeout> | null = null;
watch(hitTierIndex, () => {
    if (centerTimer) clearTimeout(centerTimer);
    centerTimer = setTimeout(() => centerHitTier(), 120);
});

// =============== 本地起批校验（V1~V4） ===============
const wq = useWholesaleQuantity();
/**
 * 本地校验（起批量/倍数判定复用数量核心 useWholesaleQuantity）
 * - V1 至少 1 个 SKU qty>0
 * - V2 起订量：dimension=1 SPU 合计 ≥ moq / dimension=2 SKU 独立 ≥ moqSku
 * - V3 倍数（永远按 SKU）：qty 为 multiple 整数倍
 * - V4 qty ≤ stock（步进器内已封顶，此处仅兜底）
 *
 * @returns 校验失败的本地兜底文案；通过返回 null
 */
const validateLocal = (): string | null => {
    if (totalQty.value <= 0) return t("请选择购买数量");
    const rule = buyRule.value;
    if (!rule) return null; // 起批规则未拉到，跳过本地校验，由后端兜底
    const dim = rule.dimension;
    if (dim === 1) {
        // SPU 合计校验
        const moq = rule.moq || 0;
        if (!wq.isMoqMet(totalQty.value, moq)) {
            return t(WHOLESALE_MOQ_NOT_MET_HINT);
        }
    } else {
        // SKU 独立校验（skuRules 缺省时回退到代表性 moq）
        const repMoq = rule.moq || 0;
        for (const [skuIdStr, qty] of Object.entries(pickedQty.value)) {
            if (!qty || qty <= 0) continue;
            const skuId = Number(skuIdStr);
            const skuRule = rule.skuRules?.find((r) => r.skuId === skuId);
            const moqSku = skuRule?.moqSku && skuRule.moqSku > 0 ? skuRule.moqSku : repMoq;
            if (!wq.isMoqMet(qty, moqSku)) {
                return t(WHOLESALE_MOQ_NOT_MET_HINT);
            }
        }
    }
    // 倍数永远按 SKU
    for (const [skuIdStr, qty] of Object.entries(pickedQty.value)) {
        if (!qty || qty <= 0) continue;
        const skuId = Number(skuIdStr);
        const skuRule = rule.skuRules?.find((r) => r.skuId === skuId);
        if (!skuRule || skuRule.multiple <= 1) continue;
        if (!wq.isMultipleValid(qty, skuRule.multiple)) {
            const sku = lastDimPicks.value.find((p) => p.skuId === skuId);
            const skuName = sku?.skuName || `${t("规格")} #${skuId}`;
            return `${skuName} ${t("请按")}${skuRule.multiple}${t("件倍数购买")}`;
        }
    }
    return null;
};

// =============== 加车（批量：一次请求带 skuItem） ===============
const submitting = ref(false);
/** 列表态：当前正在提交的按钮（仅该按钮显示「处理中...」） */
const submittingAction = ref<"cart" | "buy" | null>(null);
/** 列表态底部按钮禁用：未选数量或提交中 */
const isFooterDisabled = computed(() => submitting.value || totalQty.value <= 0);
const cartStore = useCartStore();

const handleConfirm = async (isQuickFlag: boolean | Event = false) => {
    if (submitting.value) return;
    const localErr = validateLocal();
    if (localErr) {
        uni.showToast({ title: localErr, icon: "none" });
        return;
    }

    const entries = Object.entries(pickedQty.value).filter(([, q]) => q > 0);
    if (entries.length === 0) {
        uni.showToast({ title: t("请选择购买数量"), icon: "none" });
        return;
    }

    submitting.value = true;
    // 列表态：按钮入参决定；详情态：唤起时 handleShowPopup(isQuick) 决定
    const isQuick = props.listMode ? isQuickFlag === true : quickBuy.value;
    if (props.listMode) {
        submittingAction.value = isQuick ? "buy" : "cart";
    }
    // 批量加车：复用零售 cart/cart/addToCart 的 skuItem 批量能力（字段 id/number/skuItem，后端按身份判定批发价）
    const skuItem = entries.map(([skuIdStr, qty]) => ({ skuId: Number(skuIdStr), num: qty }));
    const totalNumber = entries.reduce((s, [, q]) => s + q, 0);
    const isQuickNum = isQuick ? 1 : 0;
    try {
        const result: any = await addToCart({
            id: props.product.productId,
            number: totalNumber,
            skuItem,
            isQuick: isQuickNum
        });
        // 立即购买：加车后直达结算页
        if (isQuickNum) {
            handleClose();
            uni.navigateTo({ url: `/pages/order/check?flowType=${result?.flowType ?? ""}` });
            return;
        }
        // 加入购物车：刷新角标 + 关闭
        try {
            await cartStore.fetchCartCount();
        } catch (e) {
            console.error(e);
        }
        emit("add-success", skuItem.length);
        uni.showToast({ title: t("已加入购物车"), icon: "none" });
        handleClose();
    } catch (err: any) {
        // 业务码透传后端文案；其它错误加 [错误] 前缀
        const code = err?.code as number | undefined;
        const message = err?.message || t("加车失败");
        const prefix = isWholesaleBusinessError(code) ? "" : t("[错误] ");
        uni.showToast({ title: `${prefix}${message}`, icon: "none", duration: 2500 });
    } finally {
        submitting.value = false;
        submittingAction.value = null;
    }
};
</script>

<style lang="scss" scoped>
.wholesale-sku-popup {
    display: flex;
    flex-direction: column;
    height: 75vh;
    background: #fff;
    box-sizing: border-box;
    width: 100%;
    border-radius: 24rpx 24rpx 0 0;
    overflow: hidden;
}

.popup-head {
    display: flex;
    align-items: flex-start;
    padding: 60rpx 30rpx 24rpx 30rpx;
    position: relative;
    width: 100%;
    box-sizing: border-box;

    .product-thumb {
        width: 160rpx;
        height: 160rpx;
        border-radius: 12rpx;
        overflow: hidden;
        flex-shrink: 0;
        margin-right: 30rpx;
    }

    .head-price-wrap {
        position: relative;
        flex: 1;
        min-width: 0;
        align-self: stretch;
        // 阶梯价内容在缩略图高度内垂直居中
        display: flex;
        flex-direction: column;
        justify-content: center;
    }

    .head-price-scroll {
        width: 100%;
        white-space: nowrap;
        overflow-x: auto;
    }

    // 横向尾部渐变（透明 → 白），提示可横向滑动
    .head-price-fade {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        width: 70rpx;
        background: linear-gradient(to right, rgba(255, 255, 255, 0), #fff);
        pointer-events: none;
    }

    .head-price-list {
        display: inline-flex;
        align-items: flex-end;
        gap: 100rpx;
        padding-right: 60rpx;
    }

    .head-price-item {
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        position: relative;

        // 第一档价格贴左：内容左对齐
        &:first-child {
            align-items: flex-start;
        }

        .head-price-val {
            display: flex;
            align-items: baseline;
        }

        // 价格由 format-price 渲染（命中变色逻辑见 headPriceStyle）
        .head-suffix {
            color: #333;
            font-size: 22rpx;
            margin-left: 4rpx;

            &.primary {
                color: var(--price);
            }
        }
        .head-range-sep {
            margin: 0 16rpx;
            font-size: 28rpx;
            font-weight: bold;
        }
        .head-price-qty {
            align-self: center; // 数量文案始终居中（第一档价格贴左，数量仍居中）
            font-size: 22rpx;
            color: #999;
            margin-top: 6rpx;

            // &.active {
            //     color: var(--price);
            // }
        }

        // 单档区间：区间价 + 件数同行（横排），区别于单价档的竖排
        &.head-range-inline {
            flex-direction: row;
            align-items: baseline;

            .head-price-qty {
                align-self: baseline;
                margin-top: 0;
                margin-left: 12rpx;
            }
        }
    }

    .popup-close {
        position: absolute;
        right: 20rpx;
        top: 20rpx;
        width: 30rpx;
        height: 30rpx;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}

.popup-body {
    flex: 1;
    height: 0;
    overflow-y: auto;
    padding: 0 30rpx;
    width: 100%;
    box-sizing: border-box;
}

.dim-section {
    padding: 24rpx 0 16rpx;

    .dim-title {
        font-size: 28rpx;
        color: #2a3145;
        font-weight: 600;
        margin-bottom: 16rpx;
    }

    .dim-options {
        display: flex;
        flex-wrap: wrap;
        gap: 20rpx;
    }

    .dim-option {
        position: relative;
        display: flex;
        align-items: center;
        padding: 0;
        height: 56rpx;
        box-sizing: border-box;
        background: #f5f5f5;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #2a3145;
        border: 2rpx solid transparent;
        // 允许已选角标外凸（不再用父级 overflow 裁切，圆角下放到图片容器自身）
        overflow: visible;

        &.active {
            color: var(--general);
            border-color: var(--general);
        }

        // 左上角已选数量角标
        .dim-badge {
            position: absolute;
            top: -18rpx; // 进一步往上移动
            left: 0rpx; // 贴紧最左边，防止靠边被截断遮挡
            z-index: 10;
            min-width: 44rpx; // 调大最小宽度
            height: 26rpx; // 降低高度呈椭圆
            padding: 0 8rpx; // 加大左右内边距
            box-sizing: border-box;
            background: var(--general);
            color: #fff;
            font-size: 16rpx; // 配合高度微调字号
            font-weight: bold;
            line-height: 24rpx; // 垂直居中微调
            text-align: center;
            border-radius: 100rpx; // 全圆角无缝实现扁平胶囊椭圆形
        }

        // 图片顶满左边（无外边距），自身圆角裁切左侧（父级已不再 overflow:hidden）
        .dim-option-img {
            width: 56rpx;
            height: 100%;
            flex-shrink: 0;
            overflow: hidden;
            border-radius: 6rpx;
        }

        // 文字用左右 padding 撑开高度
        .dim-option-label {
            display: flex;
            align-items: center;
            height: 100%;
            padding: 0 24rpx;
            box-sizing: border-box;
            white-space: nowrap;
        }
    }
}

.sku-section {
    padding-bottom: 20rpx;

    .dim-title {
        font-size: 28rpx;
        color: #2a3145;
        font-weight: 600;
        margin: 24rpx 0 16rpx;
    }

    .sku-row {
        display: flex;
        align-items: center;
        padding: 20rpx 0;
        width: 100%;
        box-sizing: border-box;

        &:last-child {
            border-bottom: none;
        }

        .sku-thumb {
            position: relative;
            width: 80rpx;
            height: 80rpx;
            border-radius: 8rpx;
            overflow: hidden;
            flex-shrink: 0;
            margin-right: 20rpx;

            // 左上角已选数量角标
            .sku-thumb-badge {
                position: absolute;
                top: 0;
                left: 0;
                min-width: 28rpx;
                height: 28rpx;
                padding: 0 6rpx;
                box-sizing: border-box;
                display: flex;
                align-items: center;
                justify-content: center;
                background: var(--general);
                color: #fff;
                font-size: 20rpx;
                font-weight: bold;
                line-height: 1;
                border-radius: 8rpx 0 8rpx 0;
            }
        }

        .sku-info {
            flex: 1;
            min-width: 0;
            margin-right: 20rpx;
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
        }

        .sku-name {
            font-size: 26rpx;
            color: #333;
            margin-bottom: 8rpx;
            font-weight: 500;
            word-break: break-all;
            white-space: normal;
        }

        wholesale-stepper {
            flex-shrink: 0;
        }

        .sku-meta {
            display: flex;
            align-items: center;
            font-size: 22rpx;
            color: #999;
        }

        .sku-price {
            color: #999;
        }

        .sku-divider {
            margin: 0 12rpx;
            color: #e0e0e0;
        }

        .sku-stock {
            color: #999;
        }
    }
}

.popup-footer {
    flex-shrink: 0;
    border-top: 1rpx solid #f5f5f5;
    padding: 24rpx 30rpx calc(24rpx + env(safe-area-inset-bottom)) 30rpx;
    background: #fff;
    width: 100%;
    box-sizing: border-box;

    .footer-summary {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16rpx;

        .summary-text {
            font-size: 24rpx;
            color: #999;

            .summary-num {
                color: var(--general);
            }
        }

        .summary-amount {
            display: flex;
            align-items: baseline;
            font-size: 24rpx;

            .amount-label {
                color: #2a3145;
                margin-right: 8rpx;
            }
            // 金额由 format-price 渲染（样式见模板内 *-style 入参）
        }
    }

    .footer-actions {
        display: flex;
        gap: 14rpx;
    }

    .footer-btn-box {
        flex: 1;

        :deep(.tig-button2) {
            width: 100%;
            font-size: 28rpx;
            font-weight: 600;
        }
    }

    .footer-single {
        width: 100%;

        :deep(.tig-button2) {
            width: 100%;
            font-size: 30rpx;
            font-weight: 600;
        }
    }
}
</style>
