<template>
    <!-- 一级阶梯（仅 1 档） -->
    <div
        class="wholesale-price-range"
        :class="{ 'new-ui': uiVersion === 'new', 'is-range': ladders[0].isRange }"
        v-if="ladders.length === 1"
    >
        <!-- 多规格价格不同：展示「最低价 ~ 最高价」区间，价格与数量同行 -->
        <div v-if="ladders[0].isRange" class="range-inner">
            <FormatPrice
                :model-value="ladders[0].minPrice"
                :font-style="cellFontStyle"
                :currency-style="cellCurrencyStyle"
                :decimals-style="cellDecimalsStyle"
                currency-alignment="bottom"
            />
            <span class="range-sep">~</span>
            <FormatPrice
                :model-value="ladders[0].maxPrice"
                :font-style="cellFontStyle"
                :currency-style="cellCurrencyStyle"
                :decimals-style="cellDecimalsStyle"
                currency-alignment="bottom"
            />
            <span v-if="ladders[0].qtyText" class="moq-hint">{{ ladders[0].qtyText }}</span>
        </div>
        <!-- 单价（单规格 / 多规格价格相同）：与多档一致，上价格、下「≥N件」竖排 -->
        <div v-else class="ladder-cell single-cell">
            <div class="cell-price">
                <FormatPrice
                    :model-value="ladders[0].minPrice"
                    :font-style="cellFontStyle"
                    :currency-style="cellCurrencyStyle"
                    :decimals-style="cellDecimalsStyle"
                    currency-alignment="bottom"
                />
            </div>
            <div v-if="ladders[0].qtyText" class="cell-qty">{{ ladders[0].qtyText }}</div>
        </div>
    </div>

    <!-- 多级阶梯：横向价格阶梯条；阶梯少时居中，超宽时两端透明→白渐变 + 指示器，仅在该端可滚动时出现 -->
    <div class="wholesale-ladder-bar" :class="{ 'new-ui': uiVersion === 'new' }" v-else-if="ladders.length > 1">
        <div ref="scrollRef" class="ladder-scroll" :class="{ 'is-overflow': overflow }" @scroll="onScroll">
            <div v-for="l in ladders" :key="l.tierMinQty" class="ladder-cell">
                <div class="cell-price">
                    <FormatPrice
                        :model-value="l.minPrice"
                        :font-style="cellFontStyle"
                        :currency-style="cellCurrencyStyle"
                        :decimals-style="cellDecimalsStyle"
                        currency-alignment="bottom"
                    />
                    <template v-if="l.isRange">
                        <span class="range-sep">~</span>
                        <FormatPrice
                            :model-value="l.maxPrice"
                            :font-style="cellFontStyle"
                            :currency-style="cellCurrencyStyle"
                            :decimals-style="cellDecimalsStyle"
                            currency-alignment="bottom"
                        />
                    </template>
                </div>
                <div class="cell-qty">{{ l.qtyText }}</div>
            </div>
        </div>
        <div v-if="canLeft" class="ladder-edge left">
            <div class="ladder-arrow" @click="scrollByDir(-1)">
                <el-icon><ArrowLeftBold /></el-icon>
            </div>
        </div>
        <div v-if="canRight" class="ladder-edge right">
            <div class="ladder-arrow" @click="scrollByDir(1)">
                <el-icon><ArrowRightBold /></el-icon>
            </div>
        </div>
    </div>
    <!-- 兜底：无阶梯数据时不渲染 -->
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { ArrowLeftBold, ArrowRightBold } from "@element-plus/icons-vue";
import type { WholesaleLadder } from "@/hooks/useWholesaleOrder";

interface Props {
    /** 去重后的横向阶梯档位（已按 tierMinQty 升序，每档含数量区间文案 + min/max 价） */
    ladders: WholesaleLadder[];
    /** 当前已选总数量（PC 详情不做数量高亮，保留入参兼容） */
    currentQty?: number;
    /** 起批量（兜底入参，数量文案已由 hook 内 qtyText 计算） */
    moq?: number;
    /** UI 版本：old 老版，new 新版（新版与商品信息区现代风格保持一致） */
    uiVersion?: string;
}

const props = withDefaults(defineProps<Props>(), {
    currentQty: 0,
    moq: 0,
    uiVersion: "old"
});

// ===== 横向阶梯条左右点击滚动 =====
const scrollRef = ref<HTMLElement | null>(null);
/** 内容是否超出容器（决定左右箭头是否出现） */
const overflow = ref(false);
/** 是否可向左 / 向右滚动（到端禁用对应箭头） */
const canLeft = ref(false);
const canRight = ref(false);

const updateArrows = () => {
    const el = scrollRef.value;
    if (!el) {
        overflow.value = false;
        return;
    }
    const { scrollLeft, scrollWidth, clientWidth } = el;
    overflow.value = scrollWidth - clientWidth > 1;
    canLeft.value = scrollLeft > 1;
    canRight.value = scrollLeft < scrollWidth - clientWidth - 1;
};

const onScroll = () => updateArrows();

/** 点击箭头滚动约 80% 视口宽度（至少 120px） */
const scrollByDir = (dir: number) => {
    const el = scrollRef.value;
    if (!el) return;
    const step = Math.max(120, el.clientWidth * 0.8);
    el.scrollBy({ left: dir * step, behavior: "smooth" });
};

// 用 ResizeObserver 兜底：批发态是异步开启的，阶梯条挂载后容器宽度可能才最终确定，
// 仅靠 onMounted / watch(ladders) 检测会拿到过期宽度，导致本应出现的左右箭头不显示。
let resizeObserver: ResizeObserver | null = null;

const observeResize = () => {
    if (typeof ResizeObserver === "undefined") return;
    if (resizeObserver || !scrollRef.value) return;
    resizeObserver = new ResizeObserver(() => updateArrows());
    resizeObserver.observe(scrollRef.value);
};

onMounted(() =>
    nextTick(() => {
        updateArrows();
        observeResize();
    })
);
onBeforeUnmount(() => {
    resizeObserver?.disconnect();
    resizeObserver = null;
});
watch(
    () => props.ladders,
    () =>
        nextTick(() => {
            updateArrows();
            observeResize();
        }),
    { deep: true }
);

const isNewUi = computed(() => props.uiVersion === "new");

// 新版 UI 价格主色与商品信息区零售价一致（var(--general)）；老版沿用价格红 var(--price)
const PRICE_COLOR = computed(() => (isNewUi.value ? "var(--general)" : "var(--price)"));

// 阶梯单档（单档/多档共用）：
// - 老版：主字号 26px 加粗，符号和小数 15px（全红，无高亮）
// - 新版：与零售价一致 28px / 600 字重 / -0.02em 字距，符号小数 14px
const cellFontStyle = computed(() => ({
    fontSize: isNewUi.value ? "28px" : "26px",
    fontWeight: isNewUi.value ? "600" : "bold",
    color: PRICE_COLOR.value,
    lineHeight: "1",
    ...(isNewUi.value ? { letterSpacing: "-0.02em" } : {})
}));
const cellCurrencyStyle = computed(() => ({
    fontSize: isNewUi.value ? "14px" : "15px",
    fontWeight: isNewUi.value ? "600" : "bold",
    color: PRICE_COLOR.value,
    lineHeight: "1"
}));
const cellDecimalsStyle = computed(() => ({
    fontSize: isNewUi.value ? "14px" : "15px",
    fontWeight: isNewUi.value ? "600" : "bold",
    color: PRICE_COLOR.value,
    lineHeight: "1"
}));
</script>

<style scoped lang="less">
.wholesale-price-range {
    display: flex;
    align-items: center; /* 单元格在等高区域内垂直居中 */
    justify-content: flex-start; /* 价格一律左对齐，任何情况都不居中 */
    width: 100%;
    min-height: 66px; /* 与多档阶梯条等高，单价/阶梯切换时价格卡不跳动 */

    /* 多规格区间：价格区间 + 数量同行（基线对齐） */
    .range-inner {
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 14px;

        :deep(.price) {
            margin: 0 !important;
            align-items: baseline !important;
            line-height: 1 !important;
        }

        .range-sep {
            color: var(--price);
            font-size: 16px;
            font-weight: 600;
        }

        .moq-hint {
            font-size: 12px;
            color: #999;
            align-self: baseline;
        }
    }

    /* 单价：复用多档单元格的竖排结构（上价格、下数量），改为左对齐、数量居中 */
    .single-cell {
        align-items: flex-start;

        .cell-price {
            justify-content: flex-start;
            width: auto;

            :deep(.price) {
                justify-content: flex-start !important;
                width: auto;
            }
        }

        .cell-qty {
            width: auto;
            align-self: center; /* 数量在价格宽度内居中（价格左对齐，数量居中） */
        }
    }
}

/* 区间价与起批提示同行时由内容自然撑高，避免 66px 等高占位产生额外留白。 */
.wholesale-price-range.is-range {
    min-height: 0;
    align-items: flex-start;
}

.wholesale-ladder-bar {
    position: relative; /* 作为左右渐变指示器的定位容器 */
    width: 100%;
}

/* 两端渐变遮罩：仅在该端可继续滚动时出现，滚到头随指示器一起隐藏 */
.ladder-edge {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 70px;
    display: flex;
    align-items: center;
    pointer-events: none; /* 渐变层不拦截滚动/滑动 */
    z-index: 1;
    margin: -15px 0;

    &.left {
        left: 0;
        justify-content: flex-start;
        background: linear-gradient(to right, #fff 35%, rgba(255, 255, 255, 0));
    }

    &.right {
        right: 0;
        justify-content: flex-end;
        background: linear-gradient(to left, #fff 35%, rgba(255, 255, 255, 0));
    }
}

.ladder-arrow {
    pointer-events: auto; /* 指示器可点击，渐变层其余区域放行滚动 */
    flex: 0 0 auto;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    background: #ffffff80;
    color: #999;
    cursor: pointer;
    transition: all 0.15s;
    user-select: none;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

    .el-icon {
        font-size: 14px;
    }

    &:hover {
        border-color: var(--general);
        color: var(--general);
    }
}

.ladder-scroll {
    width: 100%;
    display: flex;
    overflow-x: auto;
    overflow-y: hidden;
    justify-content: flex-start; /* 阶梯条一律左对齐，任何情况都不居中 */
    align-items: center;
    gap: 40px; /* 单元格间距宽敞，没有外边框 */
    background: transparent;
    padding: 8px 0;

    /* 隐藏横向滚动条（保留可滑动） */
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE/Edge */
    &::-webkit-scrollbar {
        display: none; /* Chrome/Safari */
    }
}

.ladder-cell {
    flex: 0 0 auto;
    text-align: center;
    white-space: nowrap;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center; /* 保证内部元素高度撑开时也能上下居中 */
}

.cell-price {
    display: inline-flex;
    align-items: baseline; /* 强制底边对齐 */
    justify-content: center; /* 强制水平居中 */
    margin-bottom: 10px; /* 间距加宽 */
    width: 100%;

    // 彻底重置 FormatPrice 的子元素布局，消除内部伸缩和 line-height 对齐偏差，确保居中
    :deep(.price) {
        align-items: baseline !important; /* 强制子元素按基线/底边对齐 */
        justify-content: center !important; /* 强制在 FormatPrice 内部水平居中 */
        line-height: 1 !important;
        width: 100%;
        margin: 0 !important; /* 覆盖 ProductInfo 中的 margin-right，彻底解决水平居中问题 */

        .util, .num, .decimal {
            display: inline-block !important;
            vertical-align: baseline !important; /* 确保在 baseline 上对齐 */
            line-height: 1 !important;
        }

        .util {
            display: inline-flex !important;
            align-items: baseline !important;
            align-self: baseline !important;
            margin-right: 2px !important;
            margin-bottom: 0 !important;
        }
    }

    .range-sep {
        margin: 0 4px;
        font-size: 16px;
        color: inherit;
        align-self: baseline;
    }
}

.cell-qty {
    font-size: 14px; /* 数量字号稍微加大、清晰 */
    color: #9a9a9a;
    line-height: 14px;
    text-align: center;
    width: 100%;
}

/* ===== 新版 UI：与商品信息区现代风格保持一致 ===== */
.wholesale-price-range.new-ui,
.wholesale-ladder-bar.new-ui {
    font-family: Inter, "SF Pro Text", Roboto, "Helvetica Neue", Helvetica, Tahoma, Arial, "PingFang SC", "Microsoft YaHei";

    /* 价格区间（最低价 ~ 最高价 + 起批量）强制单行展示，不换行 */
    .range-inner {
        flex-wrap: nowrap;
        white-space: nowrap;
    }

    .range-sep {
        color: var(--general);
    }

    .moq-hint,
    .cell-qty {
        color: #999;
        font-weight: 500;
    }

    .ladder-arrow {
        border-radius: 8px;

        &:hover {
            border-color: var(--general);
            color: var(--general);
        }
    }
}
</style>
