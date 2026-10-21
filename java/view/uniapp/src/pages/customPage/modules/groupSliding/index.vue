<template>
    <view
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
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view>
                <view class="groupsliding">
                    <template v-for="(group, index) in module.groupList" :key="index">
                        <view
                            class="slide-box"
                            :style="`background-color: ${group.backgroundColor}; border-radius: ${module.groupRadius}px;width: ${module.groupWidth}%;flex-shrink: 0;`"
                        >
                            <view class="bg_title" @click="urlFormat(group.titleLink) && redirect({ url: urlFormat(group.titleLink) })">
                                <view class="title_box">
                                    <image class="img" mode="heightFix" :src="imageFormat(module.moduleColor.titlePic?.picUrl)" alt="" srcset="" />
                                    <view class="p line1" :style="`color: ${module.moduleColor.titleColor}; font-size: ${module.moduleColor.titleSize}px`">
                                        {{ $t(group.title) }}
                                    </view>
                                </view>
                                <view class="p line1" :style="`color: ${module.moduleColor.subTitleColor}; font-size: ${module.moduleColor.subTitleSize}px`">
                                    {{ $t(group.subTitle) }}
                                </view>
                            </view>
                            <view class="item-box" :style="`border-radius: ${module.groupRadius}px; background:${module.moduleColor.productBackgroundColor}`">
                                <template v-for="(item, itemIndex) in productGroupsList[index]" :key="itemIndex">
                                    <view class="li" @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })">
                                        <view
                                            class="goods-item"
                                            :style="`display: flex; border-radius: ${
                                                module.productPicRightRadius
                                                    ? module.productPicRadius + 'px'
                                                    : `${module.productPicRadius}px 0 0 ${module.productPicRadius}px`
                                            }`"
                                        >
                                            <view
                                                class="goods-img"
                                                :style="`flex-shrink: 0; width: ${module.productPicRatio}%; padding-top: ${module.productPicRatio}%`"
                                            >
                                                <view
                                                    class="img"
                                                    :style="`border-radius: ${
                                                        module.productPicRightRadius
                                                            ? module.productPicRadius + 'px'
                                                            : `${module.productPicRadius}px 0 0 ${module.productPicRadius}px`
                                                    };
                                                    background-size: cover;
                                                    background-image: url(${productImage(item)});`"
                                                />
                                                <image
                                                    v-if="itemIndex <= 2 && module.showRanking"
                                                    mode="widthFix"
                                                    class="ranking"
                                                    :src="rankingImgs[itemIndex]"
                                                />
                                            </view>
                                            <view class="goods-info" :style="`justify-content: center; padding: 0px 0px 0px 10px`">
                                                <view
                                                    v-if="module.showProductName"
                                                    class="goods-title"
                                                    :style="`margin-top: 0px;
                                                        height: ${module.productNameRow * 18}px;
                                                        line-height: 18px;
                                                        -webkit-line-clamp: ${module.productNameRow};
                                                        font-weight: ${module.productNameWeight};
                                                        color: rgb(51, 51, 51);
                                                        font-size: ${module.productNameSize}px;`"
                                                >
                                                    {{ item.productName }}
                                                </view>
                                                <view v-if="module.showProductLabel && formatLabel(group.label)[itemIndex]" class="tag">
                                                    <view
                                                        class="span line1"
                                                        :style="`background-color: ${module.moduleColor.labelBackgroundColor};color: ${module.moduleColor.labelTextColor};`"
                                                    >
                                                        {{ $t(trim(formatLabel(group.label)[itemIndex])) }}
                                                    </view>
                                                </view>
                                                <view class="goods-price-box" :style="`margin-top: ${module.productPriceMarginTop}px`">
                                                    <view class="price-origin-sale">
                                                        <view class="price-origin-icon">
                                                            <view class="price-icon">
                                                                <view
                                                                    v-if="module.showProductPrice"
                                                                    class="price"
                                                                    :style="`color: ${module.moduleColor.productPriceColor}; font-weight: 700; font-family: font1`"
                                                                >
                                                                    <format-price
                                                                        :font-style="{ fontSize: '28rpx' }"
                                                                        :currency-style="{
                                                                            fontSize: '22rpx'
                                                                        }"
                                                                        :decimals-style="{
                                                                            fontSize: '23rpx'
                                                                        }"
                                                                        :price-data="item.productPrice"
                                                                    />
                                                                </view>
                                                            </view>
                                                            <view
                                                                v-if="
                                                                    module.crossedOutPrice && item.marketPrice && Number(item.marketPrice) > 0 && !showB2bText
                                                                "
                                                                class="origin"
                                                                :style="`color: ${module.moduleColor.crossedOutPriceColor}`"
                                                            >
                                                                <format-price
                                                                    :font-style="{ fontSize: '22rpx', 'text-decoration': 'line-through' }"
                                                                    :price-data="item.marketPrice"
                                                                />
                                                            </view>
                                                        </view>
                                                    </view>
                                                    <commonBuyButton
                                                        v-if="module.buyBtnStyle && Number(module.buyBtnStyle.showBtn) === 1"
                                                        :style-options="module.buyBtnStyle"
                                                    />
                                                </view>
                                            </view>
                                        </view>
                                    </view>
                                </template>
                            </view>
                        </view>
                    </template>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat, productImage } from "@/utils/format";
import { redirect, trim } from "@/utils";
import { getProductsList } from "@/api/common";
import commonBuyButton from "@/pages/customPage/modules/commonBuyButton/index.vue";
import { isShowB2bPriceText } from "@/utils/b2bPrice";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});

const showB2bText = computed(() => isShowB2bPriceText());

const rankingImgs = [
    "https://oss.tigshop.com/img/gallery/202411/1732094008g4ZMmuQbNcBlYrNCuz.png",
    "https://oss.tigshop.com/img/gallery/202411/1732094013OJOLUek96BizcTvwmU.png",
    "https://oss.tigshop.com/img/gallery/202411/17320940168dzp9YD5LbpcMMjqe9.png"
];

const formatLabel = (label: string) => {
    return label.split("\n");
};

const productGroupsList = ref<any>({});
const getProductsListData = async () => {
    if (props.module.groupList && props.module.groupList.length > 0) {
        props.module.groupList.forEach(async (group: any, index: number) => {
            if (group.productList && group.productList.length > 0) {
                try {
                    const ids = group.productList.map((item: any) => item.productId);
                    const result = await getProductsList({ ids: ids.join(","), size: ids.length });
                    productGroupsList.value[index] = result.records;
                } catch (error) {
                    console.error(error);
                }
            }
        });
    }
};
getProductsListData();
</script>

<style lang="scss" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}

.groupsliding {
    display: flex;
    width: 100%;
    overflow: hidden;
    overflow-x: auto;
    align-items: stretch;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
}

.slide-box {
    margin: 0 5px;
    overflow: hidden;
    padding: 10px 8px 8px;
    &:first-child {
        margin-left: 0;
    }
}
.bg_title {
    display: block;
    overflow: hidden;
    margin-bottom: 10px;

    .title_box {
        display: flex;
        align-items: center;
        padding-bottom: 5px;

        .img {
            height: 20px;
            margin-right: 3px;
        }
    }
    .p {
        line-height: 20px;
        font-weight: 600;
    }
}

.item-box {
    padding: 4px 8px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .li {
        flex-shrink: 0;
        overflow: hidden;
        margin: 4px 0;

        .goods-item {
            overflow: hidden;
            display: flex;

            .goods-img {
                position: relative;
                max-height: 500px;
                overflow: hidden;
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

            .ranking {
                position: absolute;
                top: 4px;
                left: 4px;
                width: 20px;
            }
        }

        .goods-info {
            padding: 0 5px;
            flex-grow: 1;
            flex-shrink: 1;
            display: flex;
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

            .tag {
                margin-top: 5px;
                overflow: hidden;

                .span {
                    height: 20px;
                    line-height: 20px;
                    font-size: 12px;
                    padding: 0 5px;
                    border-radius: 3px;
                    display: inline-block;
                    overflow: hidden;
                    max-width: 130px;
                }
            }

            .goods-price-box {
                display: flex;
                position: relative;
                flex-wrap: nowrap;
                align-items: center;
                justify-content: space-between;

                .price-origin-sale {
                    display: flex;
                    -webkit-box-align: baseline;
                    align-items: baseline;
                    display: flex;
                    flex-wrap: wrap;
                    overflow: hidden;
                    -webkit-box-flex: 1;
                    flex: 1;

                    .price-origin-icon {
                        margin: 3px 0;
                        display: flex;
                        flex-wrap: wrap;
                        -webkit-box-align: baseline;
                        align-items: baseline;
                        overflow: hidden;

                        .price-icon {
                            display: flex;
                            flex-wrap: wrap;
                            align-items: center;
                            overflow: hidden;
                            .price {
                                margin-right: 5px;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                                overflow: hidden;
                                display: flex;
                                align-items: baseline;
                            }
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
</style>
