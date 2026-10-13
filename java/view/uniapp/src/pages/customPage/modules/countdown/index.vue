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
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view class="countdow-warp">
                <view class="mk-top">
                    <view class="mk-top-left">
                        <view class="mk-title line1" :style="`color: ${module.moduleColor.titleColor}`">{{ $t(module.moduleTitle.title) }}</view>
                        <view class="mk-stitle line1" :style="`color: ${module.moduleColor.subTitleColor}`">{{ $t(module.moduleTitle.subTitle) }}</view>
                    </view>
                    <view class="">
                        <view class="mk-notice" :style="`color: ${module.moduleColor.tipsColor}`">{{ $t(module.moduleTitle.moreText) }}</view>
                    </view>
                </view>
                <view class="itemBox">
                    <template v-for="item in list" :key="item.productId">
                        <view
                            class="li"
                            :style="`flex: 0 0 ${100 / module.rowNum}%`"
                            @click="item.productId && redirect({ url: urlFormat({ path: 'product', data: { id: item.productId } }) })"
                        >
                            <view
                                class="item"
                                :style="`padding: ${module.productPadding}px; background-color: ${module.moduleColor.productBackgroundColor}; border-radius: ${module.productRadius}px`"
                            >
                                <view class="imgBox" :style="`padding-top: ${picScaleMap[module.picScale!]}`">
                                    <view
                                        class="img"
                                        :style="` border-radius: ${
                                            module.picBottomRadius === 1
                                                ? `${module.productRadius}px  ${module.productRadius}px 0 0`
                                                : `${module.productRadius}px`
                                        };
                                            background-size: ${module.picFillType};
                                            background-image: url(${productImage(item)});`"
                                    />
                                </view>
                                <view class="goods-info" :style="`padding-bottom: 0px; text-align: ${module.textAlignment}`">
                                    <view v-if="module.showProductName" class="title line2" :style="`color: ${module.moduleColor.productNameColor}`">
                                        {{ item.productName ? item.productName : $t("商品名称") }}
                                    </view>
                                    <view v-if="module.showProductPrice" class="priceBox" :style="`justify-content: ${module.textAlignment}`">
                                        <view
                                            class="price"
                                            :style="`color: ${module.moduleColor.productPriceColor}; font-weight: ${module.priceWeight}; font-family: inherit`"
                                        >
                                            <format-price :font-style="{ fontSize: '22rpx' }" :price-data="item.price" />
                                        </view>
                                        <view
                                            v-if="module.crossedOutPrice && item.marketPrice && item.marketPrice > 0 && !showB2bText"
                                            class="origin"
                                            :style="`color: ${module.moduleColor.crossedOutPriceColor}`"
                                        >
                                            <format-price
                                                :font-style="{ fontSize: '20rpx', 'text-decoration': 'line-through' }"
                                                :price-data="item.marketPrice"
                                            />
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </template>
                </view>
            </view>
            <view
                v-if="module.countdownData.showCountdown"
                class="countdown"
                :style="`margin-top: -${module.countdownData.countdownPicBottom}px; border-radius: 0px 0px 6px 6px`"
            >
                <image class="countdown-img" mode="widthFix" :src="imageFormat(module.countdownData.countdownBackgroundPic?.picUrl)" />
                <view class="count-down" :style="`top: ${module.countdownData.marginTop}%;`">
                    <commonCountdown
                        :countdown-type="module.countdownData.countdownType"
                        :format="formtText"
                        :daily-time="module.countdownData.dailyTime"
                        :specify-time="module.countdownData.specifyTime"
                        :color="module.moduleColor.countdownColor"
                        :zh="true"
                        :time-style="{ fontSize: '16px', fontWeight: '700' }"
                        :unit-style="{ fontSize: '12px', fontWeight: '400', padding: '3px' }"
                    />
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat, productImage } from "@/utils/format";
import { redirect } from "@/utils";
import commonCountdown from "@/pages/customPage/modules/commonCountdown/index.vue";
import { getProductsList } from "@/api/common";
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

const picScaleMap: { [key: string]: string } = {
    "3:2": "calc(100% / 3 * 2)",
    "1:1": "calc(100% / 1 * 1)",
    "3:4": "calc(100% / 3 * 4)",
    "16:9": "calc(100% / 16 * 9)"
};

const formtMap: { [key: string]: string } = {
    天: "DD",
    时: "HH",
    分: "mm",
    秒: "ss"
};
const formtText = computed(() => {
    let str = "";
    if (props.module.countdownData.timeContent && props.module.countdownData.timeContent.length > 0) {
        for (let index = 0; index < props.module.countdownData.timeContent.length; index++) {
            const element = props.module.countdownData.timeContent[index];
            if (element) {
                str += formtMap[element];
                if (index < props.module.countdownData.timeContent.length - 1) {
                    str += ":";
                }
            }
        }
    }
    return str;
});

const list = ref<any>([]);
const getProductsListData = async () => {
    try {
        const ids = props.module.pruductList.map((item: any) => item.productId);
        const result = await getProductsList({ ids: ids.join(","), size: ids.length });
        list.value = result.records;
    } catch (error) {
        console.error(error);
    }
};

if (props.module.pruductList && props.module.pruductList.length > 0) {
    getProductsListData();
}
</script>

<style lang="scss" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}

.countdow-warp {
    position: relative;
    padding: 8px 10px 10px 10px;
    overflow: hidden;
    .mk-top {
        display: flex;
        margin-bottom: 3px;
        justify-content: space-between;
        align-items: baseline;
        overflow: hidden;

        .mk-top-left {
            flex: 1;
            height: 30px;
            display: flex;
            align-items: baseline;
            overflow: hidden;
            padding-right: 5px;

            .mk-title {
                height: 30px;
                line-height: 30px;
                font-size: 20px;
                font-weight: 700;
                overflow: hidden;
            }
            .mk-stitle {
                margin-left: 5px;
                height: 20px;
                line-height: 20px;
                font-size: 14px;
                overflow: hidden;
            }
        }

        .mk-notice {
            font-size: 12px;
        }
    }

    .itemBox {
        margin: 0 -4px;
        display: flex;
        flex-wrap: nowrap;
        flex-direction: row;
        overflow-x: auto;
        overflow-y: hidden;
        &::-webkit-scrollbar {
            display: none;
            height: 0;
        }

        .li {
            flex-shrink: 0;
            overflow: hidden;
            .item {
                margin: 0 4px;
                display: block;
                overflow: hidden;
                .imgBox {
                    position: relative;
                    max-height: 500px;
                    overflow: hidden;
                    .img {
                        position: absolute;
                        top: 0;
                        right: 0;
                        bottom: 0;
                        left: 0;
                        width: 100%;
                        background-repeat: no-repeat;
                        background-position: center;
                        background-size: 100% auto;
                        background-color: #ffffff;
                        overflow: hidden;
                    }
                }

                .goods-info {
                    padding: 3px 0 5px;
                    flex-grow: 1;
                    flex-shrink: 1;
                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                    text-align: center;
                    font-size: 12px;

                    .priceBox {
                        height: 20px;
                        line-height: 20px;
                        overflow: hidden;
                        display: flex;
                        align-items: baseline;
                        font-size: 12px;
                        .origin {
                            text-decoration: line-through;
                            margin-left: 5px;
                        }
                        .title {
                            height: 18px;
                            line-height: 18px;
                            font-size: 12px;
                            overflow: hidden;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            -webkit-box-orient: vertical;
                        }
                    }
                }
            }
        }
    }
}

.countdown {
    margin-top: -20px;
    width: 100%;
    z-index: 1;
    -webkit-transform: translate3d(0, 0, 1px);
    transform: translate3d(0, 0, 1px);
    overflow: hidden;
    .countdown-img {
        position: relative;
        display: block;
        width: 100%;
        height: 100%;
        z-index: 1;
    }
    .count-down {
        width: 100%;
        position: absolute;
        display: flex;
        align-items: baseline;
        justify-content: center;
        z-index: 2;
    }
}
</style>
