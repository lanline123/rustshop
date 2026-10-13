<template>
    <view
        v-if="productList?.length > 0"
        :class="
            'module-ad-con module-goods_ad ad-style__' +
            module.style +
            ' ad-buy_btn_style__' +
            module.buyBtnStyle +
            ' ad-goods_style__' +
            module.goodsStyle +
            ' ad-goods_radio_style__' +
            module.goodsRadioStyle +
            ' ad-text_align__' +
            module.textAlign +
            ' ad-text_weight__' +
            module.textWeight +
            ' ad-goods_name_row__' +
            module.goodsNameRow +
            ' ad-goods_name_padding__' +
            module.goodsNamePadding +
            ' ad-goods-title_style__' +
            module.title?.titleStyle +
            (isWaterfall ? ' ad-waterfall__1' : '')
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <view
            :class="'module-ad-content ad-goods-waterfall-' + module.waterfall"
            :style="
                frameFormat.backgroundColor +
                ' ' +
                frameFormat.boxRadius +
                allFormat.backgroundColor +
                allFormat.boxPadding +
                allFormat.boxPaddingBottom +
                allFormat.boxPaddingTop +
                allFormat.boxRadius +
                allFormat.innerPadding
            "
        >
            <commonTitle v-if="module.title.showTitle == 1" :module="module.title" />
            <template v-if="productList.length > 0">
                <template v-if="module.style == 7">
                    <view class="goods-ad-warp">
                        <view class="goods-ad-con">
                            <tig-swiper
                                v-model="swiperList"
                                :class="{ 'uni-swiper-dot-box': module.picPageType == 3 }"
                                mode="default"
                                :color="module.picPageType == 3 ? '#fff' : ''"
                                :select-color="selectColor"
                                :height="480"
                                :bottom="15"
                            >
                                <template #default="{ item }">
                                    <view v-for="(subItem, index) in item" :key="index" class="goods-ad-item">
                                        <view class="item-content" :style="allFormat.goodsPadding">
                                            <view class="item-con">
                                                <view class="item-photo">
                                                    <navigator :url="urlFormat({ path: 'product', data: { id: subItem.productId } })" class="item-image-a">
                                                        <image :src="productImage(subItem)" mode="widthFix" />
                                                    </navigator>
                                                    <image
                                                        v-if="styleMap[subItem.productId]?.borderImage"
                                                        class="product-style-border"
                                                        :src="styleMap[subItem.productId].borderImage"
                                                        mode="scaleToFill"
                                                    />
                                                </view>
                                                <view class="item-info">
                                                    <template v-if="module.showName">
                                                        <view class="item-name">
                                                            <navigator
                                                                :url="urlFormat({ path: 'product', data: { id: subItem.productId } })"
                                                                class="item-name-a"
                                                            >
                                                                <text>{{ subItem.productName ?? "" }}</text>
                                                            </navigator>
                                                            <template v-if="module.showBrief">
                                                                <navigator
                                                                    :url="urlFormat({ path: 'product', data: { id: subItem.productId } })"
                                                                    class="item-brief"
                                                                >
                                                                    {{ $t(subItem.productBrief ?? "商品描述") }}
                                                                </navigator>
                                                            </template>
                                                        </view>
                                                    </template>
                                                    <view v-if="module?.showPrice" class="item-action">
                                                        <view class="item-price">
                                                            <format-price
                                                                :font-style="{ fontSize: '32rpx' }"
                                                                :currency-style="{
                                                                    fontSize: '22rpx'
                                                                }"
                                                                :decimals-style="{
                                                                    fontSize: '24rpx'
                                                                }"
                                                                :price-data="subItem.deliveryMode === 2 ? subItem.cyclePrice : subItem.productPrice"
                                                            />
                                                        </view>
                                                        <view class="item-buy" />
                                                    </view>
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                </template>
                            </tig-swiper>
                        </view>
                    </view>
                </template>
                <template v-else-if="isWaterfall">
                    <view class="goods-ad-warp">
                        <view class="goods-ad-con">
                            <view v-for="(col, colIndex) in columnList" :id="'wf-col-' + colIndex" :key="'wf-col-' + colIndex" class="goods-ad-col">
                                <view v-for="item in col" :key="item.productId" class="goods-ad-item">
                                    <view class="item-content" :style="allFormat.goodsPadding">
                                        <view class="item-con">
                                            <view class="item-photo">
                                                <navigator :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-image-a">
                                                    <tig-image :src="pickPic(item)" mode="widthFix" />
                                                </navigator>
                                                <image
                                                    v-if="styleMap[item.productId]?.borderImage"
                                                    class="product-style-border"
                                                    :src="styleMap[item.productId].borderImage"
                                                    mode="scaleToFill"
                                                />
                                            </view>
                                            <view class="item-info">
                                                <template v-if="module.showName">
                                                    <view class="item-name">
                                                        <navigator :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-name-a">
                                                            <text> {{ $t(item.productName ?? "商品名称") }} </text>
                                                        </navigator>
                                                    </view>
                                                </template>
                                                <template v-if="module.showPrice">
                                                    <view class="item-action">
                                                        <view class="item-price">
                                                            <format-price
                                                                :font-style="{ fontSize: '32rpx' }"
                                                                :currency-style="{ fontSize: '22rpx', position: 'relative', top: '-3rpx' }"
                                                                :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                                            />
                                                        </view>
                                                        <view class="item-buy" @click="redirect({ url: `/pages/product/index?id=${item.productId}` })">
                                                            <view class="buy-btn">
                                                                <template v-if="module.buyBtnStyle == 5 || module.buyBtnStyle == 6">
                                                                    <view>{{ $t("购买") }}</view>
                                                                </template>
                                                                <template v-else-if="module.buyBtnStyle == 7 || module.buyBtnStyle == 8">
                                                                    <view>{{ $t("马上抢") }}</view>
                                                                </template>
                                                                <template v-else>
                                                                    <view class="iconfont-h5" />
                                                                </template>
                                                            </view>
                                                        </view>
                                                    </view>
                                                </template>
                                                <template v-else>
                                                    <view class="blank" style="height: 20rpx" />
                                                </template>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                </template>
                <template v-else>
                    <view class="goods-ad-warp">
                        <view class="goods-ad-con">
                            <template v-for="item in productList" :key="item.productId">
                                <view class="goods-ad-item">
                                    <view class="item-content" :style="allFormat.goodsPadding">
                                        <view class="item-con">
                                            <view class="item-photo">
                                                <navigator :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-image-a">
                                                    <tig-image :src="pickPic(item)" mode="widthFix" />
                                                </navigator>
                                                <image
                                                    v-if="styleMap[item.productId]?.borderImage"
                                                    class="product-style-border"
                                                    :src="styleMap[item.productId].borderImage"
                                                    mode="scaleToFill"
                                                />
                                            </view>
                                            <view class="item-info">
                                                <template v-if="module.showName">
                                                    <view class="item-name">
                                                        <navigator :url="urlFormat({ path: 'product', data: { id: item.productId } })" class="item-name-a">
                                                            <text> {{ $t(item.productName ?? "商品名称") }} </text>
                                                        </navigator>
                                                        <!-- <template v-if="module.showBrief">
                                                            <navigator :url="''" class="item-brief">
                                                                {{ item.productBrief ?? "商品描述" }}
                                                            </navigator>
                                                        </template> -->
                                                    </view>
                                                </template>
                                                <template v-if="module.showPrice">
                                                    <view class="item-action">
                                                        <view class="item-price">
                                                            <format-price
                                                                :font-style="{ fontSize: '32rpx' }"
                                                                :currency-style="{ fontSize: '22rpx', position: 'relative', top: '-3rpx' }"
                                                                :price-data="item.deliveryMode === 2 ? item.cyclePrice : item.productPrice"
                                                            />
                                                        </view>
                                                        <view class="item-buy" @click="redirect({ url: `/pages/product/index?id=${item.productId}` })">
                                                            <view class="buy-btn">
                                                                <template v-if="module.buyBtnStyle == 5 || module.buyBtnStyle == 6">
                                                                    <view>{{ $t("购买") }}</view>
                                                                </template>
                                                                <template v-else-if="module.buyBtnStyle == 7 || module.buyBtnStyle == 8">
                                                                    <view>{{ $t("马上抢") }}</view>
                                                                </template>
                                                                <template v-else>
                                                                    <view class="iconfont-h5" />
                                                                </template>
                                                            </view>
                                                        </view>
                                                    </view>
                                                </template>
                                                <template v-else>
                                                    <view class="blank" style="height: 20rpx" />
                                                </template>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </template>
            </template>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, watch, watchEffect, getCurrentInstance, nextTick } from "vue";
import { urlFormat, pickPic, productImage } from "@/utils/format";
import { formatFrame } from "@/components/modules";
import { getProductsList } from "@/api/common";
import commonTitle from "@/pages/customPage/modules/commonTitle/index.vue";
import { redirect, delay, getElementRect } from "@/utils";
import { useProductStyle } from "@/hooks/useProductStyle";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    },
    moduleIndex: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: -1
    }
});
const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        backgroundColor: `background-color: ${props.module.backgroundColor};`,
        boxPadding: `margin-left: ${props.module.boxPadding}px;margin-right: ${props.module.boxPadding}px;`,
        boxPaddingBottom: `margin-bottom: ${props.module.boxPaddingBottom}px;`,
        boxPaddingTop: `margin-top: ${props.module.boxPaddingTop}px;`,
        boxRadius: `border-radius: ${props.module.boxRadius}px;`,
        innerPadding: `padding: ${props.module.innerPadding}px;`,
        goodsPadding: `padding: ${props.module.goodsPadding}px;`
    };
});

const paramsValueMap: { [key: number]: string[] } = {
    1: ["productIds"],
    2: ["productCategoryId", "productNumber"],
    3: ["productTag", "productNumber"]
};
const paramsKeyMap: { [key: string]: string } = {
    productIds: "ids",
    productCategoryId: "categoryId",
    productNumber: "size",
    productTag: "introType"
};
const productList = ref<any[]>([]);
const { styleMap } = useProductStyle(productList);

// ========== 瀑布流（仅“一行两个”且开启瀑布流时生效，按列高分配实现真瀑布流） ==========
const instance = getCurrentInstance();
const isWaterfall = computed(() => Number(props.module.style) === 2 && Number(props.module.waterfall) === 1);
const columnList = ref<any[][]>([[], []]);
let waterfallTaskId = 0;
const distributeWaterfall = async (items: any[]) => {
    waterfallTaskId++;
    const taskId = waterfallTaskId;
    columnList.value = [[], []];
    await nextTick();
    const columnHeights = [0, 0];
    for (let i = 0; i < items.length; i++) {
        if (taskId !== waterfallTaskId) return;
        let idx: number;
        if (columnHeights[0] === columnHeights[1]) {
            // 高度相等（含首次测高失败两列都为 0）时按已渲染数量交替，避免全堆一列
            idx = columnList.value[0].length > columnList.value[1].length ? 1 : 0;
        } else {
            idx = columnHeights[0] < columnHeights[1] ? 0 : 1;
        }
        columnList.value[idx].push(items[i]);
        await delay(80);
        await nextTick();
        if (taskId !== waterfallTaskId) return;
        try {
            const rect: any = await getElementRect(`#wf-col-${idx}`, instance);
            columnHeights[idx] = rect?.height || columnHeights[idx] + 300;
        } catch {
            columnHeights[idx] += 300;
        }
    }
};
watch(productList, (val) => {
    if (isWaterfall.value && val && val.length) {
        distributeWaterfall(val);
    }
});
const _getproductList = async () => {
    let param: any = {};
    if (typeof props.module.products === "object") {
        if (props.module.products.productSelectType) {
            const type = props.module.products.productSelectType;
            const keys = paramsValueMap[type];
            if (type == 1) {
                param[paramsKeyMap[keys[0]]] = props.module.products.productIds.join(",");
                param.size = props.module.products.productIds.length;
            } else {
                param.page = 1;
                keys.forEach((key) => {
                    param[paramsKeyMap[key]] = props.module.products[key];
                });
            }
        }
    }
    if (props.shopId && props.shopId != -1) {
        param.shopId = props.shopId;
    }
    try {
        const result = await getProductsList(param);
        productList.value = result.records;
    } catch (error) {
        console.log(error);
    }
};
_getproductList();

const swiperList = ref<any[]>([]);
const getSwiperList = () => {
    swiperList.value = [];
    if (props.module.style == 7 && productList.value) {
        let swiperListNum = Math.ceil(productList.value.length / 3);
        for (let index = 0; index < swiperListNum; index++) {
            swiperList.value.push(productList.value.slice(index * 3, (index + 1) * 3));
        }
    }
};
watchEffect(() => {
    getSwiperList();
});

const selectColor = computed(() => {
    return props.module.swiperPageColor || "#333";
});
</script>
<style lang="scss" scoped>
.goods-ad-warp .goods-ad-item .item-info .item-name .item-brief {
    line-height: 40rpx;
    height: 40rpx;
    overflow: hidden;
    display: block;
    color: #aaa;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -moz-line-clamp: 1;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
    font-size: 24rpx;
    font-weight: bold;
}

.goods-ad-warp .goods-ad-item .item-info .item-action {
    padding-bottom: 10rpx;
}

.ad-style__7 .goods-ad-warp {
    padding-bottom: 5rpx;
}
.ad-style__7 .goods-ad-warp .goods-ad-item .item-info {
    .item-name {
        font-size: 26rpx;
        padding-bottom: 15rpx;
    }
}
.ad-style__7 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: nowrap;
}
.ad-style__7 .goods-ad-warp .goods-ad-con {
    display: block;
    flex-wrap: nowrap;
    padding: 0 !important;
    height: 100%;
}
.ad-style__7 .goods-ad-warp swiper {
    height: 400rpx;
    position: relative;
    overflow: hidden;
}
.ad-style__7 .goods-ad-warp .goods-ad-con .goods-ad-item {
    width: 33.333%;
}

.ad-style__7 .goods-ad-warp .goods-ad-item .item-info .item-buy {
    display: none;
}
.ad-style__7 .goods-ad-warp .swiper-pagination-con {
    position: absolute;
    bottom: 30rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}
.ad-style__7 .goods-ad-warp .swiper-pagination {
    display: inline-block;
    width: 24rpx;
    border-radius: 0;
    height: 4rpx;
    margin: 0 2rpx;
    background: #000;
    opacity: 0.2;
}
.ad-style__7 .goods-ad-warp .swiper-pagination.active {
    opacity: 1;
    background-color: #fff;
}
.ad-goods-title_style__3 .goods-ad-warp {
    position: relative;
}
.ad-waterfall__1.ad-style__2 .goods-ad-warp .goods-ad-con {
    display: flex;
    flex-wrap: nowrap;
    align-items: flex-start;
}
.ad-waterfall__1.ad-style__2 .goods-ad-warp .goods-ad-con .goods-ad-col {
    width: 50%;
    display: flex;
    flex-direction: column;
}
.ad-waterfall__1.ad-style__2 .goods-ad-warp .goods-ad-con .goods-ad-col .goods-ad-item {
    width: 100%;
}

.cycleBuyTag {
    display: inline-block;
    color: var(--general);
    position: relative;
    padding-right: 20rpx;
    vertical-align: baseline;
    &::before {
        position: absolute;
        content: "";
        right: 9rpx;
        top: 0;
        width: 2rpx;
        height: 70%;
        transform: translateY(22%);
        background-color: var(--general);
    }
}

.item-photo {
    position: relative;
}
.product-style-border {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    z-index: 5;
    pointer-events: none;
}
</style>
