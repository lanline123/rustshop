<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div class="productgrouping">
                <template v-if="module.groupTitle.showTitle || module.groupTitle.showSubTitle">
                    <div class="tablist">
                        <template v-for="(item, index) in tabList">
                            <div class="tablist-item">
                                <div class="tab-text">
                                    <div class="nav-item">
                                        <div v-if="index > 0" class="line" :style="`border-color:${module.groupTitle.splitLineColor}`"></div>
                                        <div class="nav-item-box" :style="`padding: 0px ${module.groupTitle.marginLeftRight}px;`">
                                            <template v-if="module.groupTitle.showTitle">
                                                <div
                                                    class="tab-title"
                                                    :style="tablistItemStyle.title + `${index === 0 ? `color:${module.groupTitle.activeTitleColor};` : ''}`"
                                                >
                                                    {{ item.title }}
                                                </div>
                                            </template>
                                            <template v-if="module.groupTitle.showSubTitle && item.subTitle">
                                                <div
                                                    class="tab-sub"
                                                    :style="
                                                        tablistItemStyle.subTitle +
                                                        `${index === 0 ? `color:${module.groupTitle.activeSubTitleColor};background-image: linear-gradient(${gradientMap[module.groupTitle.subTitleGradientType as GradientType]}, ${module.groupTitle.gradientColorA}, ${module.groupTitle.gradientColorB});` : ''}`
                                                    "
                                                >
                                                    {{ item.subTitle }}
                                                </div>
                                            </template>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </div>
                </template>

                <div class="productlist">
                    <div class="productlist-item">
                        <div class="" style="margin-top: 5px">
                            <div class="item-box">
                                <template v-for="(item, index) in displayList" :key="index">
                                    <div
                                        class="li"
                                        :style="`opacity: 1; width: ${module.listStyle < 5 ? 100 / module.listStyle : index % 3 === 0 ? 100 : 50}%;`"
                                    >
                                        <div class="goods-item" :style="goodsItemStyle + (item.__isBanner ? '; position: relative;' : '')">
                                            <div
                                                v-if="item.__isBanner"
                                                class="banner-fill"
                                                :style="`position: absolute; top: 0; right: 0; bottom: 0; left: 0;
                                                    background-image: url(${productImage(item)});
                                                    background-size: ${module.picFillType};
                                                    background-position: center;
                                                    background-repeat: no-repeat;
                                                    border-radius: ${module.cardRadius}px;
                                                    z-index: 1;`"
                                            ></div>
                                            <div class="goods-img" :style="`padding-top: ${picScaleMap[module.picScale!]}`">
                                                <div
                                                    v-if="!item.__isBanner"
                                                    class="img"
                                                    :style="`border-radius: ${module.picBottomRadius ? `${module.cardRadius}px` : `${module.cardRadius}px ${module.cardRadius}px 0 0`};
                                                        background-size: ${module.picFillType};
                                                        background-image: url(${productImage(item)});`"
                                                ></div>
                                            </div>
                                            <div
                                                class="goods-info"
                                                :style="`text-align: left; padding: ${module.productColor.infoTopPadding}px ${module.productColor.infoLeftRightPadding}px ${module.productColor.infoBottomPadding}px; ${item.__isBanner ? 'visibility: hidden;' : ''}`"
                                            >
                                                <div
                                                    v-if="module.showProductName"
                                                    class="goods-title"
                                                    :style="`margin-top: 3px;
                                                        height: ${module.productNameRow * 18}px;
                                                        line-height: 18px;
                                                        -webkit-line-clamp: ${module.productNameRow};
                                                        font-weight: ${module.productNameWeight};
                                                        color: ${module.productColor.titleColor};
                                                        font-size:  ${module.productNameSize}px;`"
                                                >
                                                    {{ item.productName }}
                                                </div>
                                                <div
                                                    class="goods-price-box"
                                                    :style="`margin-top: ${module.productPriceMarginTop}px; justify-content: left; display: flex; ${hasBuyBtnText ? 'flex-wrap: wrap;' : ''}`"
                                                >
                                                    <div class="price-origin-sale" :style="`justify-content: left; ${hasBuyBtnText ? 'flex-basis: 100%;' : ''}`">
                                                        <div class="price-origin-icon">
                                                            <div class="price-icon">
                                                                <img
                                                                    v-if="module.productPricePic && module.productPricePic.picUrl"
                                                                    class="icon"
                                                                    :src="module.productPricePic.picUrl"
                                                                    alt=""
                                                                />
                                                                <div
                                                                    v-if="module.showProductPrice"
                                                                    class="price"
                                                                    :style="`color: ${module.productColor.priceColor}; font-weight: ${module.productPriceWeight}; font-family: inherit;font-size:${module.productPriceSize}px;`"
                                                                >
                                                                    ￥{{ item.productPrice }}
                                                                </div>
                                                                <div
                                                                    v-if="module.crossedOutPrice"
                                                                    class="origin"
                                                                    :style="`color: ${module.productColor.crossedOutPriceColor}`"
                                                                >
                                                                    ￥{{ item.marketPrice }}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <CommonBuyButton
                                                        v-if="Number(module.buyBtnStyle?.showBtn) === 1"
                                                        :style="hasBuyBtnText ? 'margin: 5px auto 0' : 'margin-top: 0px'"
                                                        :styleOptions="module.buyBtnStyle"
                                                    ></CommonBuyButton>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </template>
                            </div>
                            <div class="more-box" v-if="module.bottomMoreBtn !== 'close'">
                                <div
                                    v-if="module.bottomMoreBtnText && module.bottomMoreBtn === 'text'"
                                    class="more-txt"
                                    :style="`margin: 10px 3px; background-color:${module.productColor.moreBtnBgColor}; border-color:${module.productColor.moreBtnBorderColor}; color: ${module.productColor.moreBtnTextColor}`"
                                >
                                    {{ module.bottomMoreBtnText }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, watchEffect } from "vue";
import { GradientType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, formatCommonStyle, gradientMap, CommonBuyButton } from "@/views/decorate/decorate/src/modules/index";
import { getProductList } from "@/api/product/product";
import { productImage } from "@/utils/format";
import { getDemoList } from "@/views/decorate/decorate/src/modules/index";
import { productGroupingDefaults } from "./index";
import type { ProductGroupingModule } from "./types";

const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};
const props = defineProps<{ module: ProductGroupingModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, productGroupingDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const tablistItemStyle = computed(() => {
    return {
        title: `color: ${module.groupTitle.titleColor}; font-size: ${module.groupTitle.bigTitleFontSize}px;`,
        subTitle: `color: ${module.groupTitle.subTitleColor}; font-size: ${module.groupTitle.titleFontSize}px;border-radius:${module.groupTitle.subTitleRadius}px;`
    };
});
const hasBuyBtnText = computed(() => {
    return Number(module.buyBtnStyle?.showBtn) === 1 && !!module.buyBtnStyle?.text && Number(module.buyBtnStyle?.wrap) === 1;
});
const goodsItemStyle = computed(() => {
    let str = `margin: ${module.cardMargin / 2}px;padding: ${module.cardPadding}px;border: ${module.productCardStyle === 3 ? 1 : 0}px solid rgb(238, 238, 238);border-radius: ${module.cardRadius}px;`;
    if (module.productCardStyle !== 4) {
        str += `background-color:${module.productColor.backgroundColor};`;
    }
    if (module.productCardStyle === 2) {
        str += `box-shadow: rgba(93, 113, 127, 0.08) 0px 2px 8px;`;
    }
    return str;
});

const tabList = computed(() => {
    return module.productSource === "productGroup" ? module.groupList : module.productGroups;
});
const currentIndex = ref(0);

const activeGroup = computed<any>(() => {
    const list = tabList.value;
    return Array.isArray(list) && list.length > 0 ? list[currentIndex.value] : null;
});

const activeBannerPic = computed<any>(() => {
    const pic = activeGroup.value?.bannerPic;
    return pic && (pic.picThumb || pic.picUrl) ? pic : null;
});

const list = ref<any>([]);

const displayList = computed<any[]>(() => {
    if (activeBannerPic.value) {
        // banner 卡填充与商品卡同形的占位字段，确保 goods-info 骨架高度与邻接商品卡对齐
        // 真实图片由模板里 .banner-fill 绝对定位覆盖整张卡片；占位字段被外层 visibility: hidden 整体隐藏
        const placeholder = list.value[0] ?? { productName: "_", productPrice: "0", marketPrice: "0" };
        const bannerItem = {
            __isBanner: true,
            picUrl: activeBannerPic.value.picUrl || activeBannerPic.value.picThumb,
            picThumb: activeBannerPic.value.picThumb || activeBannerPic.value.picUrl,
            productName: placeholder.productName,
            productPrice: placeholder.productPrice,
            marketPrice: placeholder.marketPrice
        };
        return [bannerItem, ...list.value];
    }
    return list.value;
});
const currentGroupData = ref<any>({});
const getGroupProductList = async (arr = []) => {
    try {
        const result = await getProductList(currentGroupData.value);
        list.value = [...arr, ...result.records];
    } catch (error) {
        console.error(error);
    }
};
watchEffect(() => {
    currentGroupData.value = {};
    if (module.productSource) {
        if (module.productSource === "productGroup") {
            if (module.groupList.length > 0) {
                const { productGroupId, productNum, productNumType } = module.groupList[currentIndex.value];
                currentGroupData.value.productGroupId = productGroupId;
                currentGroupData.value.size = productNumType === "number" ? productNum : 100;
                getGroupProductList();
            } else {
                list.value = [];
            }
        } else {
            if (module.productGroups && module.productGroups.length > 0) {
                currentGroupData.value.ids = module.productGroups[currentIndex.value].productList.map((item: any) => item.productId).join(",");
                getGroupProductList(getDemoList(module.productGroups[currentIndex.value].productList));
            }
        }
    }
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.tablist {
    overflow-x: auto;
    overflow-y: hidden;
    box-sizing: initial;
    height: 100%;
    position: relative;
    display: flex;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
    .tablist-item {
        padding: 0;
        flex: 1 0 auto;

        &.active {
            color: #323233;
            font-weight: 500;
        }

        .tab-text {
            width: 100%;

            .nav-item {
                text-align: center;
                overflow: hidden;
                position: relative;
                .line {
                    content: "";
                    border-right: #dadada solid 1px;
                    position: absolute;
                    left: 0;
                    top: 0;
                    bottom: 0;
                    -webkit-transform: scale(0.5);
                    transform: scale(0.5);
                }

                .nav-item-box {
                    display: inline-grid;
                    .tab-title {
                        margin: 0;
                        font-size: 18px;
                        font-weight: 700;
                        color: #333333;
                    }
                    .tab-sub {
                        margin-top: 5px;
                        padding: 3px 8px;
                        font-size: 14px;
                        color: #454545;
                    }
                }
            }
        }
    }
}
.productlist {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;

    .productlist-item {
        flex-shrink: 0;
        box-sizing: border-box;
        width: 100%;

        .item-box {
            display: flex;
            flex-wrap: wrap;
            flex-direction: row;
            align-items: stretch;

            .goods-item {
                display: block;
                overflow: hidden;

                .goods-img {
                    position: relative;
                    max-height: 500px;
                    overflow: hidden;
                    width: 100%;
                    height: 0;
                    background-repeat: no-repeat;
                    background-position: center;
                    .img {
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        left: 0;
                        background-repeat: no-repeat;
                        background-position: center;
                        background-size: 100% auto;
                        overflow: hidden;
                    }
                }

                .goods-info {
                    padding: 0 5px;
                    flex-grow: 1;
                    flex-shrink: 1;
                    display: flex;
                    -webkit-box-orient: vertical;
                    flex-direction: column;
                    overflow: hidden;
                    font-size: 12px;
                    .goods-title {
                        line-height: 18px;
                        font-size: 14px;
                        overflow: hidden;
                        display: -webkit-box;
                        -webkit-line-clamp: 1;
                        -webkit-box-orient: vertical;
                    }

                    .goods-price-box {
                        display: flex;
                        position: relative;
                        flex-wrap: nowrap;
                        align-items: center;
                        justify-content: space-between;
                        .price-origin-sale {
                            display: flex;
                            align-items: baseline;
                            display: flex;
                            flex-wrap: wrap;
                            overflow: hidden;
                            flex: 1;

                            .price-origin-icon {
                                margin: 3px 0;
                                display: flex;
                                flex-wrap: wrap;
                                align-items: baseline;
                                overflow: hidden;

                                .price-icon {
                                    display: flex;
                                    flex-wrap: wrap;
                                    align-items: self-end;
                                    overflow: hidden;
                                    .icon {
                                        height: 14px;
                                        margin-right: 3px;
                                        display: inline-block;
                                    }
                                    .price {
                                        margin-right: 5px;
                                        text-overflow: ellipsis;
                                        white-space: nowrap;
                                        overflow: hidden;
                                        display: flex;
                                        -webkit-box-align: baseline;
                                        align-items: baseline;
                                    }
                                    .origin {
                                        margin-right: 10px;
                                        text-overflow: ellipsis;
                                        white-space: nowrap;
                                        overflow: hidden;
                                        text-decoration: line-through;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    .more-box {
        display: flex;
        align-items: center;
        justify-content: center;

        .more-txt {
            width: 100%;
            height: 40px;
            line-height: 40px;
            font-size: 14px;
            color: #333333;
            border: 1px solid #eee;
            border-radius: 5px;
            display: inline-block;
            text-align: center;
        }
    }
}
</style>
