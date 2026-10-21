<template>
    <div class="wholesale-sku-selector" :class="{ 'new-ui': uiVersion === 'new' }">
        <!-- 前 N-1 维：图块/标签选择器 -->
        <div v-for="(dim, dimIdx) in frontDimensions" :key="dim.specId" class="dim-group">
            <div class="dim-title">{{ dim.specName }}</div>
            <div class="dim-content">
                <div class="dim-options" :class="{ 'is-image': dim.hasImage }">
                    <div
                        v-for="opt in dim.options"
                        :key="opt.optionId"
                        class="dim-option"
                        :class="{ 'is-active': frontSelection[dim.specId] === opt.optionId }"
                        @click="onPickFront(dim.specId, opt.optionId)"
                    >
                        <!-- 已选数量徽标：仅前置维度的第一类规格展示 -->
                        <span v-if="dimIdx === 0 && getOptionBadgeCount(dim.specName, opt.label) > 0" class="dim-badge">
                            {{ getOptionBadgeCount(dim.specName, opt.label) }}
                        </span>
                        <img v-if="dim.hasImage && opt.image" :src="opt.image" :alt="opt.label" />
                        <span class="option-label" :data-text="opt.label">{{ opt.label }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 末维：列表行选数 -->
        <div class="dim-group last-dim-group" v-if="props.lastDimPicks.length > 0">
            <div class="dim-title last-dim-title" v-if="lastDimension">{{ lastDimension.specName }}</div>
            <div class="dim-content last-dim-content">
                <div class="last-dim-list">
                    <div v-for="pick in props.lastDimPicks" :key="pick.skuId" class="last-dim-row">
                        <div class="row-info">
                            <img v-if="pick.thumb" :src="pick.thumb" class="row-thumb" :alt="pick.skuName" />
                            <div class="row-meta">
                                <div class="row-name">{{ pick.skuName }}</div>
                            </div>
                        </div>
                        <div class="row-price-stock">
                            <span class="row-price">{{ rowUnitPriceText(pick.skuId, pick.unitPriceText) }}</span>
                            <span class="row-stock"> ({{ pick.stock === 0 ? $t("库存不足") : pick.stockText }}) </span>
                        </div>
                        <div class="row-stepper">
                            <el-input-number
                                :model-value="pickedQty[pick.skuId] || 0"
                                :min="0"
                                :max="pick.stock"
                                :step="multipleMap[pick.skuId] || 1"
                                :controls="true"
                                :disabled="pick.stock === 0"
                                @change="(val) => onQtyChange(pick.skuId, Number(val) || 0)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 全局合计 -->
        <div v-if="pickedKinds > 0" class="picked-summary">
            <span class="summary-left"
                >{{ $t("已选") }}<em class="red">{{ pickedKinds }}</em
                >{{ $t("款") }}<em class="red">{{ pickedTotal }}</em
                >{{ $t("个") }}</span
            >
            <span class="summary-amount">
                {{ $t("商品金额") }}：<em
                    ><FormatPrice
                        :model-value="props.totalAmountRaw || '0'"
                        :font-style="{ fontSize: '18px', fontWeight: 'bold', color: 'var(--price)' }"
                        :currency-style="{ fontSize: '14px', fontWeight: 'bold', color: 'var(--price)' }"
                        :decimals-style="{ fontSize: '14px', fontWeight: 'bold', color: 'var(--price)' }"
                /></em>
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ElInputNumber } from "element-plus";
import type { WholesaleSpecDimension, WholesaleSkuPick, WholesaleQtyChangePayload } from "@/types/wholesale/sku";
import type { WholesaleBuyRule } from "@/types/wholesale/buyRule";

interface Props {
    /** 规格维度，按定义顺序；长度 = N。N=0 时为单 SKU 商品（不渲染选择器，仅末维 1 行） */
    dimensions: WholesaleSpecDimension[];
    /** 当前前置维度选择下的末维可选数 SKU 行（由调用方根据 frontSelection 派生） */
    lastDimPicks: WholesaleSkuPick[];
    /** 起批规则（含倍数与起订量） */
    buyRule: WholesaleBuyRule | null;
    /** 商品总金额原始值；交给 FormatPrice 渲染（统一币种符号），未选/未回值时回退 0 */
    totalAmountRaw?: string;
    /** 是否处于「金额预估」态（无后端文案时为 true） */
    isAmountEstimated?: boolean;
    /** 全量 SKU 列表，用于计算前置维度的已选数量角标 */
    skuList?: any[];
    /** 批发定价模式：1=混批（按全规格总数匹配阶梯）/ 2=SKU 级独立 / null=未配置 */
    wholesalePriceMode?: number | null;
    /** 行单价解析器：传入 skuId + 命中数量，返回阶梯批发价文案（空串表示无阶梯，回退零售价） */
    resolveRowPrice?: (skuId: number, qty: number) => string;
    /** UI 版本：old 老版，new 新版（新版与商品信息区现代风格保持一致） */
    uiVersion?: string;
}

const props = withDefaults(defineProps<Props>(), {
    totalAmountRaw: "",
    isAmountEstimated: false,
    skuList: () => [],
    buyRule: null,
    wholesalePriceMode: null,
    resolveRowPrice: undefined,
    uiVersion: "old"
});

const emit = defineEmits<{
    /** 前置维度选择变化（调用方据此重算 lastDimPicks） */
    "front-select": [specId: number, optionId: number, selection: Record<number, number>];
    /** 选数变化 */
    "qty-change": [payload: WholesaleQtyChangePayload];
}>();

/**
 * 获取某个前置选项（如某种颜色）下，所有已选 SKU 的数量总和
 */
const getOptionBadgeCount = (specName: string, optionLabel: string) => {
    let sum = 0;
    if (!props.skuList) return 0;
    for (const sku of props.skuList) {
        const qty = pickedQty.value[sku.skuId] || 0;
        if (qty <= 0) continue;
        const hasMatch = sku.skuData?.some((sd: any) => String(sd.name) === String(specName) && String(sd.value) === String(optionLabel));
        if (hasMatch) {
            sum += qty;
        }
    }
    return sum;
};

/** 前 N-1 维 */
const frontDimensions = computed(() => props.dimensions.slice(0, -1));
/** 末维（最后 1 维），可能为 undefined（单 SKU 商品） */
const lastDimension = computed(() => props.dimensions[props.dimensions.length - 1]);

/** 前置维度单选态：specId -> optionId */
const frontSelection = ref<Record<number, number>>({});

/** 全局选数：skuId -> qty（跨前置维度切换不清空） */
const pickedQty = ref<Record<number, number>>({});

/** 初始化 / 同步默认前置选择（首次有 lastDimPicks 时不动；调用方负责派生） */
watch(
    () => props.dimensions,
    (dims) => {
        for (const dim of dims.slice(0, -1)) {
            if (frontSelection.value[dim.specId] === undefined && dim.options.length > 0) {
                frontSelection.value[dim.specId] = dim.options[0].optionId;
            }
        }
    },
    { immediate: true, deep: true }
);

/** 倍数映射：skuId -> multiple（永远 ≥1） */
const multipleMap = computed(() => {
    const map: Record<number, number> = {};
    if (!props.buyRule) return map;
    for (const r of props.buyRule.skuRules) map[r.skuId] = Math.max(1, r.multiple);
    return map;
});

/**
 * 就近吸附到 multiple 整数倍
 * - 不强制起订量（moq 在父级确认时统一校验，与移动端一致）
 * - 内置 +/- 已按 :step=multiple 增减，手输统一在此就近取倍数（4 倍时手输 5 → 取 4）
 * - 超库存则向下取不超过库存的最大合法倍数
 */
const normalizeQty = (skuId: number, raw: number, stock: number): number => {
    const mul = multipleMap.value[skuId] || 1;
    const cap = Math.max(0, stock);
    if (raw <= 0 || cap === 0) return 0;
    const k = Math.max(1, Math.round(raw / mul));
    let v = k * mul;
    if (v > cap) {
        const downK = Math.floor(cap / mul);
        v = downK >= 1 ? downK * mul : 0;
    }
    return Math.max(0, v);
};

const onPickFront = (specId: number, optionId: number) => {
    if (frontSelection.value[specId] === optionId) return;
    frontSelection.value = { ...frontSelection.value, [specId]: optionId };
    emit("front-select", specId, optionId, { ...frontSelection.value });
};

const onQtyChange = (skuId: number, raw: number) => {
    const pick = props.lastDimPicks.find((p) => p.skuId === skuId);
    if (!pick) return;
    const prev = pickedQty.value[skuId] || 0;
    // 内置 +/- 已按 :step=multiple 增减、手输也走这里，统一就近吸附到 multiple 整数倍，
    // 不再用 delta 区分点按钮/手输（否则手输恰好为「当前值±1」时会被误判，导致 4↔8 来回跳）
    const next = normalizeQty(skuId, raw, pick.stock);

    if (next === prev) return;
    if (next > 0) {
        pickedQty.value = { ...pickedQty.value, [skuId]: next };
    } else {
        const map = { ...pickedQty.value };
        delete map[skuId];
        pickedQty.value = map;
    }
    // 携带本次变更的 SKU，驱动价格阶梯条切换为该 SKU 的阶梯
    emit("qty-change", buildPayload(skuId));
};

const pickedKinds = computed(() => Object.values(pickedQty.value).filter((q) => q > 0).length);
const pickedTotal = computed(() => Object.values(pickedQty.value).reduce((s, q) => s + (q > 0 ? q : 0), 0));

/**
 * 末维行单价：阶梯批发价随量联动
 * - 混批（wholesalePriceMode=1）：按全规格已选总数匹配阶梯；SKU 级/未配置：按本行数量
 * - 阶梯缺失（解析器返回空）时回退零售价 fallback
 */
const rowUnitPriceText = (skuId: number, fallback: string): string => {
    if (!props.resolveRowPrice) return fallback;
    const qty = Number(props.wholesalePriceMode) === 1 ? pickedTotal.value : pickedQty.value[skuId] || 0;
    return props.resolveRowPrice(skuId, qty) || fallback;
};

const buildPayload = (changedSkuId?: number): WholesaleQtyChangePayload => ({
    pickedQty: { ...pickedQty.value },
    pickedKinds: pickedKinds.value,
    pickedTotal: pickedTotal.value,
    changedSkuId
});

/** 暴露给父组件的方法：清空选数（加车成功后调用） */
defineExpose({
    resetPicked: () => {
        pickedQty.value = {};
        emit("qty-change", buildPayload());
    },
    getPicked: () => buildPayload()
});
</script>

<style scoped lang="less">
.wholesale-sku-selector {
    background: #fff;
    width: 100%;
}

.dim-group {
    display: flex;
    align-items: flex-start;
    padding: 15px 0 0;

    &.last-dim-group {
        border-bottom: none;
        padding-top: 18px;
    }
}

.dim-title {
    flex: 0 0 50px;
    font-size: 14px;
    color: #666;
    padding: 5px 10px 0 0;
    font-weight: normal;

    &.last-dim-title {
        padding-top: 0;
        height: 28px; /* 完美匹配右侧步进器的高 28px */
        display: flex;
        align-items: center; /* 完美将文字居中于 28px 高度，与右侧首行文字的中线绝对水平对齐 */
        margin-top: 0;
    }
}

.dim-content {
    flex: 1;
    min-width: 0;
}

.dim-options {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}

.dim-option {
    position: relative;
    display: flex;
    align-items: center;
    padding: 0; /* 👈 关键：外层容器彻底取消 Padding，由内部子元素分别撑开 */
    height: 28px; /* 👈 整体高度固定为 28px */
    border: 1px solid #eaeaea;
    border-radius: 2px;
    cursor: pointer;
    background: #fff;
    transition: all 0.2s;
    user-select: none;

    img {
        width: 28px; /* 👈 图片高度完美顶满 28px 高度 */
        height: 28px;
        object-fit: contain;
        display: block;
        flex-shrink: 0;
        border-radius: 2px;
    }

    .option-label {
        font-size: 13px;
        color: #333;
        padding: 0 12px; /* 👈 垂直高度由外层 28px 决定，这里只需设置左右 Padding */
        display: inline-flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        box-sizing: border-box;
        white-space: nowrap;

        &::after {
            display: block;
            content: attr(data-text);
            font-weight: 500;
            height: 0;
            overflow: hidden;
            visibility: hidden;
            user-select: none;
            pointer-events: none;
        }
    }

    &:hover {
        border-color: var(--general);
    }

    &.is-active {
        border-color: var(--general);
        background: #fff;

        .option-label {
            color: var(--general);
            font-weight: 500;
        }
    }
}

.dim-badge {
    position: absolute;
    top: -6px;
    left: -2px;
    background: var(--general);
    color: #fff;
    font-size: 10px;
    min-width: 16px;
    height: 12px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    box-sizing: border-box;
    z-index: 10;
    font-weight: bold;
    line-height: 1;
}

.last-dim-list {
    display: flex;
    flex-direction: column;
    width: 100%;
}

.last-dim-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 0; /* 加宽行高 */
    border-bottom: 1px solid #f5f5f5; /* 更清淡的底边线 */

    &:first-child {
        padding-top: 0; /* 彻底消除首行 padding-top，让首行的内容直接顶到容器最上方 */
    }

    &:last-child {
        border-bottom: none;
    }
}

.row-info {
    flex: 1; /* 自动填充左侧区域，将价格、库存和步进器整体推向最右侧 */
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.row-thumb {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: 2px;
}

.row-meta {
    flex: 1;
    min-width: 0;
}

.row-name {
    font-size: 14px;
    color: #333;
    line-height: 1.4;
}

.row-price-stock {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-right: 14px; /* 👈 库存和价格紧挨着步进器，间隔 14px */
}

.row-price {
    color: #333;
    font-size: 14px;
    font-weight: bold;
}

.row-stock {
    font-size: 12px;
    color: #999;
}

.row-stepper {
    flex: 0 0 100px; /* 👈 完美匹配内部步进器的 100px 宽度，消除多余的 10px 间隙，使 margin-right: 14px 成为真实的 14px 间隔 */
    display: flex;
    justify-content: flex-end;

    // 自定义 el-input-number 样式，使其变为极简扁平灰色背景步进器
    :deep(.el-input-number) {
        width: 100px !important;
        height: 28px !important;
        line-height: 28px !important;
        border: none !important; /* 无边框 */
        border-radius: 2px !important;
        overflow: hidden !important;
        background: #F7F8FA !important; /* 整体浅灰色背景 */

        .el-input-number__decrease,
        .el-input-number__increase {
            background: transparent !important; /* 按钮背景透明，融入整体灰色 */
            border: none !important; /* 无分割线 */
            color: #666 !important;
            width: 28px !important;
            height: 28px !important;
            line-height: 28px !important;
            border-radius: 0 !important;
            transition: all 0.1s;

            &:hover {
                color: #333 !important;
                background: rgba(0, 0, 0, 0.05) !important; /* 悬浮时微暗 */
            }

            &.is-disabled {
                color: #ccc !important;
                background: transparent !important;
            }
        }

        .el-input-number__decrease {
            left: 0 !important;
        }

        .el-input-number__increase {
            right: 0 !important;
        }

        .el-input__wrapper {
            padding: 0 !important;
            box-shadow: none !important;
            border: none !important;
            height: 28px !important;
            line-height: 28px !important;
            border-radius: 0 !important;
            background: transparent !important; /* 输入框背景透明 */
        }

        .el-input__inner {
            height: 28px !important;
            line-height: 28px !important;
            color: #333 !important;
            font-weight: 500 !important;
            font-size: 14px !important;
            text-align: center !important;
            background: transparent !important;
        }
    }
}

.picked-summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f2f2f2;
    padding: 16px 0 0;
    font-size: 14px;
    color: #333;
}

.summary-left {
    color: #666;

    em.red {
        color: var(--general);
        font-weight: bold;
        font-style: normal;
    }
}

.summary-amount {
    color: #333;

    em {
        color: var(--price);
        font-weight: bold;
        font-style: normal;
        margin-left: 4px;
        font-size: 18px; /* 稍微加大 */
    }

    :deep(.price) {
        align-items: baseline;
    }
}

/* ===== 新版 UI：与商品信息区(ProductSku / InputNumber)现代风格保持一致 ===== */
.wholesale-sku-selector.new-ui {
    font-family: Inter, "SF Pro Text", Roboto, "Helvetica Neue", Helvetica, Tahoma, Arial, "PingFang SC", "Microsoft YaHei";

    /* 规格组：标题与选项换行竖排，参考普通 SKU(ProductSku 新版)布局 */
    .dim-group {
        flex-direction: column;
        align-items: stretch;
        padding-top: 0px;

        &.last-dim-group {
            padding-top: 24px;
        }
    }

    .dim-title {
        flex: none;
        height: auto;
        line-height: 22px;
        padding: 0;
        margin-bottom: 12px;
        color: #999;
        font-size: 14px;
        font-weight: 500;

        &.last-dim-title {
            height: auto;
            margin-bottom: 8px;
        }
    }

    /* 选项间距对齐普通 SKU：横向 8px、纵向 12px */
    .dim-options {
        gap: 12px 8px;
    }

    /* 规格选项：圆角标签，与 ProductSku 新版无图标签一致 */
    .dim-option {
        height: 40px;
        border: 1px solid rgba(31, 31, 51, 0.1);
        border-radius: 8px;
        background: #fff;

        img {
            width: 38px;
            height: 38px;
            border-radius: 8px 0 0 8px;
        }

        .option-label {
            font-size: 14px;
            color: rgba(9, 9, 10, 0.92);
        }

        &:hover {
            border-color: var(--general);
        }

        &.is-active {
            border-color: var(--general);

            .option-label {
                color: var(--general);
            }
        }
    }

    .dim-badge {
        background: var(--general);
    }

    /* 末维行：文字色与分隔线对齐新版 */
    .last-dim-row {
        border-bottom-color: #f0f0f0;
    }

    .row-name {
        color: rgba(9, 9, 10, 0.92);
        font-size: 14px;
        font-weight: 500;
    }

    .row-price {
        color: rgba(9, 9, 10, 0.92);
    }

    .row-stock {
        color: #999;
    }

    .row-thumb {
        border-radius: 8px;
    }

    /* 步进器：圆角描边风格，与新版 InputNumber(rounded) 一致 */
    .row-stepper {
        flex: 0 0 108px;

        :deep(.el-input-number) {
            width: 108px !important;
            height: 32px !important;
            line-height: 32px !important;
            border: 1px solid rgba(31, 31, 51, 0.1) !important;
            border-radius: 8px !important;
            background: #fff !important;

            .el-input-number__decrease,
            .el-input-number__increase {
                width: 30px !important;
                height: 30px !important;
                line-height: 30px !important;
                background: transparent !important;
                color: rgba(9, 9, 10, 0.92) !important;

                &:hover {
                    color: var(--general) !important;
                    background: transparent !important;
                }

                &.is-disabled {
                    color: #ccc !important;
                }
            }

            .el-input-number__decrease {
                border-right: 1px solid rgba(31, 31, 51, 0.1) !important;
                border-radius: 8px 0 0 8px !important;
            }

            .el-input-number__increase {
                border-left: 1px solid rgba(31, 31, 51, 0.1) !important;
                border-radius: 0 8px 8px 0 !important;
            }

            .el-input__wrapper {
                height: 30px !important;
                line-height: 30px !important;
            }

            .el-input__inner {
                height: 30px !important;
                line-height: 30px !important;
                color: rgba(9, 9, 10, 0.92) !important;
                font-weight: 600 !important;
            }
        }
    }

    /* 合计区：弱化文字色，金额保留价格红 */
    .picked-summary {
        border-top-color: #f0f0f0;
    }

    .summary-left {
        color: #999;

        em.red {
            color: var(--general);
        }
    }

    .summary-amount {
        color: rgba(9, 9, 10, 0.92);
    }
}
</style>
