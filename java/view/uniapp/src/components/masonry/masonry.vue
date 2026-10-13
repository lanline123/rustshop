<template>
    <view class="masonry">
        <template v-for="(column, columnIndex) in columnList" :key="columnIndex">
            <view class="masonry-item-column">
                <!-- 每张卡片外包一层 masonry-cell 供测量单卡真实高度；列表可能出现重复商品，key 需带上原始下标 -->
                <view v-for="entry in column" :key="`${entry.index}-${entry.key}`" class="masonry-cell" :class="`masonry-cell-${columnIndex}`">
                    <slot name="product" :item="entry.item">
                        <product-item
                            :item="entry.item"
                            :promotion-list="promotionList"
                            :style-map="styleMap"
                            :type="type"
                            :show-shop="showShop"
                            :is-cycle-buy="entry.item.deliveryMode === 2 && entry.item.deliveryType === 1"
                            :is-sub-card="entry.item.deliveryMode === 2 && entry.item.deliveryType === 2"
                            :text-align="textAlign"
                            :text-weight="textWeight"
                            :goods-name-row="goodsNameRow"
                            :product-title-line="productTitleLine"
                            :show-name="showName"
                            :buy-btn-style="buyBtnStyle"
                            :show-coupon="showCoupon"
                            :product-title-color="productTitleColor"
                            :price-color="priceColor"
                            :theme-color="themeColor"
                            :item-background-color="itemBackgroundColor"
                            :product-image-radius="productImageRadius"
                            @click-item="handleClickItem"
                            @click-buy="handleBuy"
                            @click-shop="handleToShop"
                        />
                    </slot>
                </view>
            </view>
        </template>
    </view>
    <specification
        ref="specificationRef"
        v-model="productId"
        v-model:is-cycle-buy="isCycleBuy"
        v-model:is-sub-card="isSubCard"
        :shop-id="shopId"
        :type="type"
        :check-query-extra="checkQueryExtra"
        @add-card-succeed="getCallback"
        @load-end="loadEnd"
        @close="handleClose"
    />
    <WholesaleListSpec
        ref="wholesaleSpecRef"
        v-model="wholesaleProductId"
        @load-end="wholesaleLoadEnd"
        @close="handleWholesaleClose"
        @add-success="getCallback"
    />
</template>

<script lang="ts" setup>
import { ref, type PropType, watch, getCurrentInstance, nextTick, computed, onUnmounted } from "vue";
import specification from "@/components/product/specification.vue";
import WholesaleListSpec from "@/components/wholesale/WholesaleListSpec.vue";
import { useProductCardBuy } from "@/hooks/useProductCardBuy";
import ProductItem from "./product-item.vue";
import { getElementRect, redirect, delay } from "@/utils";
import { onShow, onHide } from "@dcloudio/uni-app";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";

const props = defineProps({
    commodityList: {
        type: Array as PropType<any[]>,
        default: () => []
    },
    promotionList: {
        type: Object as PropType<any>,
        default: () => ({})
    },
    styleMap: {
        type: Object as PropType<Record<number, ProductActiveStyleVO>>,
        default: () => ({})
    },
    type: {
        type: String,
        default: ""
    },
    showShop: {
        type: Boolean,
        default: true
    },
    shopId: {
        type: Number,
        default: null
    },
    // ===== 装修配置（可选，默认 undefined 表示沿用原有视觉） =====
    /** 文本对齐：1=左对齐，2=居中对齐 */
    textAlign: {
        type: Number,
        default: undefined
    },
    /** 文字粗细：1=常规体，2=加粗体 */
    textWeight: {
        type: Number,
        default: undefined
    },
    /** 商品名称行数：1=1行，2=2行 */
    goodsNameRow: {
        type: Number,
        default: undefined
    },
    /** 商品标题行数（兼容字段，与 goodsNameRow 等价） */
    productTitleLine: {
        type: Number,
        default: undefined
    },
    /** 是否显示商品名称 */
    showName: {
        type: Boolean,
        default: true
    },
    /** 购买按钮样式：undefined=沿用旧图标，0=不显示，1~8=对应样式 */
    buyBtnStyle: {
        type: Number,
        default: undefined
    },
    /** 是否显示优惠券活动；undefined 表示沿用其他页面原有行为 */
    showCoupon: {
        type: Boolean,
        default: undefined
    },
    productTitleColor: { type: String, default: undefined },
    priceColor: { type: String, default: undefined },
    themeColor: { type: String, default: undefined },
    itemBackgroundColor: { type: String, default: undefined },
    productImageRadius: { type: Number, default: undefined },
    /** 立即购买跳结算页时附加的 query（门店自提 tab 锁定配送方式），透传给规格弹窗 */
    checkQueryExtra: {
        type: String,
        default: ""
    }
});

const emit = defineEmits(["callback"]);

const getCallback = () => {
    emit("callback");
};

const instance = getCurrentInstance();

// ========== 状态定义 ==========
/** 列内条目：index 为在原列表中的下标，key 为高度缓存标识 */
interface ColumnEntry {
    item: any;
    index: number;
    key: string;
}

const columnList = ref<ColumnEntry[][]>([[], []]);
const isPageVisible = ref(true);
/** 已测得的卡片高度缓存，key 为条目唯一标识，避免重复测量 */
const heightCache = new Map<string, number>();
/** 重测任务序号：仅让最后一次触发的异步重测生效，避免翻页/切分类时旧任务回填错乱 */
let remeasureToken = 0;
/** 未测得高度时的预估值（px），保证首屏能左右均衡分列，再由真实测量纠偏 */
const ESTIMATED_ITEM_HEIGHT = 300;
/** 两列高度差在该阈值（px）内视为基本持平，此时优先放左列，避免观感上总偏向右列 */
const COLUMN_HEIGHT_TOLERANCE = 20;

const cloneData = (data: any) => {
    return JSON.parse(JSON.stringify(data));
};

const copyCommodityList = computed(() => {
    if (props.commodityList && props.commodityList.length > 0) {
        return cloneData(props.commodityList);
    }
    return [];
});

// ========== 核心瀑布流算法 ==========
/** 条目唯一标识：兑换列表用 id，商品列表用 productId，两者都缺时退化为下标 */
const getItemKey = (item: any, index: number): string => {
    const id = item?.id ?? "";
    const productId = item?.productId ?? "";
    return id === "" && productId === "" ? `idx-${index}` : `${id}-${productId}`;
};

/**
 * 按列表顺序把每项放入当前累计高度较小的列。
 * 高度优先取缓存，未测得的用预估值参与累计，保证一次性铺完首屏而不是逐条追加。
 * 同一份缓存下算法是确定的：翻页追加时前面的条目落列不变，不会整屏重排。
 */
const buildColumns = () => {
    const list = copyCommodityList.value;
    const columns: ColumnEntry[][] = [[], []];
    const columnHeights = [0, 0];
    list.forEach((item: any, index: number) => {
        const key = getItemKey(item, index);
        const height = heightCache.get(key) ?? ESTIMATED_ITEM_HEIGHT;
        // 仅当左列高出容差时才放右列；两列基本持平时优先放左列
        const target = columnHeights[0] <= columnHeights[1] + COLUMN_HEIGHT_TOLERANCE ? 0 : 1;
        columns[target].push({ item, index, key });
        columnHeights[target] += height;
    });
    columnList.value = columns;
};

/** 查询某一列所有卡片的真实高度 */
const queryCellHeights = async (columnIndex: number): Promise<number[]> => {
    try {
        const rects = await getElementRect(`.masonry-cell-${columnIndex}`, instance, true);
        return rects.map((rect) => Number(rect?.height) || 0);
    } catch {
        return [];
    }
};

const isSameHeights = (a: number[], b: number[]) => a.length === b.length && a.every((value, index) => value === b[index]);

/**
 * 异步测量已渲染卡片的真实高度，回填缓存后重排一次。
 * 图片是懒加载的，卡片高度会随图片到位而变化，因此连续两次测得一致才视为稳定。
 */
const remeasureAndBalance = async () => {
    const token = ++remeasureToken;
    let previous: number[][] = [[], []];
    for (let i = 0; i < 30; i++) {
        await delay(i === 0 ? 80 : 150);
        // 期间发生了新的分配（翻页/切分类）或页面已隐藏，放弃本次过期任务
        if (token !== remeasureToken || !isPageVisible.value) return;
        const measured = await Promise.all([queryCellHeights(0), queryCellHeights(1)]);
        if (token !== remeasureToken || !isPageVisible.value) return;

        const stable = measured.every(
            (heights, columnIndex) => heights.length === columnList.value[columnIndex].length && isSameHeights(heights, previous[columnIndex])
        );
        if (stable) {
            measured.forEach((heights, columnIndex) => {
                columnList.value[columnIndex].forEach((entry, cellIndex) => {
                    if (heights[cellIndex] > 0) heightCache.set(entry.key, heights[cellIndex]);
                });
            });
            buildColumns();
            return;
        }
        previous = measured;
    }
};

/** 先按已知高度立即铺满，再按真实高度纠偏 */
const distribute = async () => {
    buildColumns();
    await nextTick();
    remeasureAndBalance();
};

// ========== Watch 监听数据变化 ==========
watch(
    () => copyCommodityList.value,
    async (newVal: any[]) => {
        if (!newVal || newVal.length === 0) {
            remeasureToken++;
            columnList.value = [[], []];
            return;
        }
        await distribute();
    },
    { immediate: true }
);

// ========== 生命周期钩子 ==========
let isFirstShow = true;

onShow(async () => {
    isPageVisible.value = true;

    // 首次 onShow 跳过
    if (isFirstShow) {
        isFirstShow = false;
        return;
    }

    if (copyCommodityList.value.length === 0) return;

    // 页面隐藏期间无法测高，回到前台后补测一次
    await delay(100);
    if (!isPageVisible.value) return;
    remeasureAndBalance();
});

onHide(() => {
    isPageVisible.value = false;
    remeasureToken++; // 中断当前测量任务
});

onUnmounted(() => {
    remeasureToken++;
    heightCache.clear();
});

const handleClickItem = (item: any) => {
    let promotionItem, skuId, params;
    promotionItem = props.promotionList[item.productId];
    if (promotionItem && promotionItem.activityInfo && promotionItem.activityInfo.length > 0 && promotionItem.activityInfo[0].skuIds) {
        skuId = promotionItem.activityInfo[0].skuIds[0];
    }

    switch (props.type) {
        case "exchange":
            params = `id=${item.id}&type=exchange`;
            break;
        default:
            params = `id=${item.productId}`;
    }

    params = skuId ? `${params}&skuId=${skuId}` : params;

    if (props.shopId > 0) {
        params = `${params}&shopId=${props.shopId}`;
    }

    redirect({ url: "/pages/product/index?" + params });
};
const handleToShop = (id: number) => {
    redirect({
        url: `/pages/shop/index?shopId=${id}`
    });
};

const productId = ref(0);
const specificationRef = ref();
const currentItem = ref<any>({});

// 是否是周期购
const isCycleBuy = computed(() => {
    return currentItem.value.deliveryMode === 2 && currentItem.value.deliveryType === 1;
});

// 是否是次卡
const isSubCard = computed(() => {
    return currentItem.value.deliveryMode === 2 && currentItem.value.deliveryType === 2;
});

const handleBuy = (item: any) => {
    if (item.isCheck) return;
    item.isCheck = true;
    currentItem.value = item;
    switch (props.type) {
        case "exchange":
            productId.value = item.id;
            break;
        default: {
            // 批发卡片：走独立批发列表规格弹层（与零售 specification 互斥）
            if (isWholesaleCard(item)) {
                wholesaleProductId.value = cardProductId(item);
                return;
            }
            productId.value = item.productId;
        }
    }
};
const loadEnd = () => {
    specificationRef.value.handleShowPopup();
    currentItem.value.isCheck = false;
};
const handleClose = () => {
    productId.value = 0;
};

const wholesaleProductId = ref(0);
const wholesaleSpecRef = ref<InstanceType<typeof WholesaleListSpec>>();
const { isWholesaleCard, cardProductId } = useProductCardBuy();
const wholesaleLoadEnd = (payload: { wholesale: boolean }) => {
    if (!payload.wholesale) {
        // 非批发商品或加载失败：回退零售规格半窗
        productId.value = wholesaleProductId.value;
        wholesaleProductId.value = 0;
        return;
    }
    wholesaleSpecRef.value?.handleShowPopup();
    currentItem.value.isCheck = false;
};
const handleWholesaleClose = () => {
    wholesaleProductId.value = 0;
    currentItem.value.isCheck = false;
};
</script>
<style lang="scss" scoped>
.masonry {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-gap: 14rpx;
    .masonry-item-column {
        width: 100%;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    /* flex 容器可阻断子元素的 margin 折叠，测得的高度才包含卡片间距 */
    .masonry-cell {
        width: 100%;
        display: flex;
        flex-direction: column;
    }
}

.activity-box {
    height: 55rpx;
}

/* 注意：Vue scoped 会作用于子组件根节点。
 * product-item 根节点也是 .item-li，这里若写 background:#fff
 * 会盖掉子组件 --pi-item-bg（商品项背景色），表现为只在 .info 生效。
 * 卡片样式以 product-item.vue 为准，此处不再写 background。 */
.item-li {
    width: 100%;
    margin-bottom: 15rpx;
    border-radius: var(--pi-image-radius, 12rpx);
    overflow: hidden;
    padding-bottom: 10rpx;
}
.item-li .photo {
    // padding: 20rpx;
    text-align: center;
    position: relative;
}
.item-li .photo image {
    width: 100%;
}

.item-li .info .detail {
    margin: 10rpx 16rpx 10rpx;
    display: block;
    line-height: 40rpx;
    height: 80rpx;
    overflow: hidden;
    font-size: 26rpx;
    display: block;
    font-weight: bold;
    color: #2a3145;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}

.item-li .info .detail .name {
    line-height: 40rpx;
    overflow: hidden;
    font-size: 26rpx;
    display: block;
    font-weight: bold;
    color: #2a3145;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}
.item-li .info .shop {
    height: 50rpx;
    font-size: 26rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16rpx 10rpx;

    .shop-name {
        width: 55%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .shop-btn {
        display: flex;
        align-items: center;
        justify-content: center;

        .icon-youjiantou {
            font-size: 32rpx;
        }
    }
}
.item-li .info .action {
    padding: 0 16rpx 10rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.item-li .info .price {
    font-weight: bold;
    display: inline-block;
    font-size: 30rpx;
}
.item-li .info .price text {
    font-size: 20rpx;
}

.item-li .buy_btn {
    // margin-right: 10rpx;
    // margin-top: 6rpx;
    color: var(--general);
    display: flex;
    align-items: center;
    justify-content: center;
    .buy_btn_icon {
        font-size: 42rpx;
    }

    .exchange-btn {
        font-size: 24rpx;
        background-color: var(--main-bg);
        border-radius: 20rpx;
        padding: 5rpx 10rpx;
        color: var(--main-text);
    }
}
.item-li .buy_btn image {
    width: 100%;
    height: 100%;
}

.list-cart .buy_btn {
    display: none;
}
.list-overseas {
    background: #fff;
    -webkit-box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.05);
    box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.05);
}

.product-status-box {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;

    .outsale {
        width: 50%;
        height: 50%;
        line-height: 180rpx;
        background: rgba(0, 0, 0, 0.4);
        font-size: 38rpx;
        color: #ffffff;
        letter-spacing: 0;
        border-radius: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}

.pricenum {
    color: var(--general);
    height: inherit;

    .exchange-box {
        display: flex;
        align-items: center;
        .exchange-img {
            width: 30rpx;
            height: 30rpx;
            padding-right: 5rpx;
        }
        .exchange-icon {
            padding: 0 5rpx;
        }
        .exchange-text {
            font-size: 28rpx;
        }
    }
}
</style>
