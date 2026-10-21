<template>
    <view class="product-group-title-box" :class="{ 'product-group-title-box--vortmall': vortmall, 'product-group-title-box--overseas': overseas }">
        <view class="product-group-title flex justify-between align-center" :style="vortmall ? undefined : groupTitleStyle">
            <view v-if="!vortmall" class="activity-overlay" />
            <view class="group-price-box">
                <view class="title-top-price">
                    <view v-if="!vortmall" class="num-group">
                        {{ $t("拼团优惠") }}<text class="separator">|</text>{{ grouponValue.currentGroupNum + $t("人团") }}
                    </view>
                    <view class="price-box">
                        <view class="price">
                            <format-price
                                :font-style="{
                                    fontSize: vortmall ? '49rpx' : '48rpx',
                                    fontWeight: vortmall ? '600' : 'bold',
                                    color: mainPriceColor
                                }"
                                :decimals-style="{
                                    fontSize: vortmall ? '49rpx' : '32rpx',
                                    fontWeight: vortmall ? '600' : 'bold',
                                    color: mainPriceColor
                                }"
                                :currency-style="{
                                    fontSize: vortmall ? '30rpx' : '28rpx',
                                    fontWeight: vortmall ? '600' : 'bold',
                                    color: mainPriceColor
                                }"
                                :price-data="grouponValue.grouponPrice"
                            />
                        </view>
                    </view>
                    <view class="title-top-market_price flex align-center">
                        <view v-if="!showB2bText" class="originalprice-box">
                            <view>
                                <format-price
                                    :is-bottom="false"
                                    :price-data="productOriginPrice"
                                    :font-style="{
                                        textDecoration: 'line-through',
                                        fontSize: vortmall ? '28rpx' : undefined,
                                        color: originPriceColor
                                    }"
                                />
                            </view>
                        </view>
                        <view v-if="grouponValue.groupSuccessCount > 0 && !vortmall" class="discountStock">
                            <view> {{ $t("已成团") }}{{ grouponValue.groupSuccessCount }} {{ $t("件") }}</view>
                        </view>
                    </view>
                </view>
            </view>
            <view class="group-time-box">
                <view class="countdown-box flex align-center flex-column">
                    <view class="title-top-time">{{ $t(timeText) }}</view>
                    <!-- <tig-countdown background-color="#FFF" :end-time="targetTime" :font-size="11" color="#FB424E" /> -->
                    <view class="countdown">
                        <commonCountdown countdown-type="specifyTime" :specify-time="dateStringToTimestamp(targetTime)">
                            <template #default="{ days, hours, minutes, seconds }">
                                <view class="countdown-text">
                                    <text v-if="days" class="countdown-bg">{{ formatTime(days) }}</text>
                                    <text v-if="days" class="splitor">{{ $t("天") }}</text>
                                    <text class="countdown-bg">{{ formatTime(hours) }}</text>
                                    <text class="splitor">:</text>
                                    <text class="countdown-bg">{{ formatTime(minutes) }}</text>
                                    <text class="splitor">:</text>
                                    <text class="countdown-bg">{{ formatTime(seconds) }}</text>
                                </view>
                            </template>
                        </commonCountdown>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 拼团商品标题组件
 * - 显示拼团价格和原价对比
 * - 显示成团人数要求
 * - 倒计时显示拼团活动剩余时间
 */
import type { ProductItem } from "@/types/product/product";
import { computed } from "vue";
import commonCountdown from "@/components/commonCountdown/index.vue";
import { staticResource } from "@/utils";
import { isShowB2bPriceText } from "@/utils/b2bPrice";

const { t } = useI18n();
interface Props {
    productInfo: ProductItem;
    productOriginPrice: string | number;
    productPrice: string | number;
    productStock: number;
    grouponValue: Record<string, any>;
    activityBgImage?: string;
    vortmall?: boolean;
    overseas?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    activityBgImage: "",
    vortmall: false,
    overseas: false
});
// console.log('productGroupTITLE', props.grouponValue);

const showB2bText = computed(() => isShowB2bPriceText());
const mainPriceColor = "#fff";
const originPriceColor = "#fff";

/**
 * 背景图样式
 */
const groupTitleStyle = computed(() => {
    const url = props.activityBgImage || staticResource("groupon/titleBg.png");
    return {
        backgroundImage: `url(${url})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat"
    };
});

/**
 * 获取时间文本
 */
const timeText = computed(() => {
    if (!isActivityStart(props.grouponValue.startTime)) return t("距开始");
    return props.vortmall ? t("距活动结束") : t("距离结束仅剩");
});

/**
 * 计时器用的目标时间
 */
const targetTime = computed(() => {
    return isActivityStart(props.grouponValue.startTime) ? props.grouponValue.endTime : props.grouponValue.startTime;
});

const formatTime = (time: string): string => {
    return time?.toString().padStart(2, "0") || "00";
};

/*转时间搓 */
const dateStringToTimestamp = (dateString: any) => {
    const dateParts = dateString.split(" ");
    const [year, month, day] = dateParts[0].split("-");
    const [hour, minute, second] = dateParts[1].split(":");
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.getTime();
};

/**
 * 是否活动开始
 * @param item 团购活动信息
 */
const isActivityStart = (datetime: string) => {
    const startTime = dateStringToTimestamp(datetime);
    return startTime < new Date().getTime();
};
</script>

<style lang="scss" scoped>
.product-group-title-box {
    padding-bottom: 20rpx;

    &.product-group-title-box--overseas {
        padding-bottom: 0;

        .product-group-title {
            padding: 20rpx 28rpx;
            border-radius: 0;
            overflow: hidden;
        }
    }
}
.product-group-title {
    position: relative;
    min-height: 140rpx;
    padding: 20rpx 20rpx 35rpx 25rpx;

    .activity-overlay {
        position: absolute;
        inset: 0;
        z-index: 0;
    }

    .group-price-box,
    .group-time-box {
        position: relative;
        z-index: 1;
    }

    .title-top-price {
        .num-group {
            color: #fff;
            font-size: 28rpx;

            .separator {
                padding: 0 16rpx;
            }
        }

        .price-box {
            display: flex;
        }

        .price {
            color: #fff;
            font-size: 48rpx;
            display: flex;
            align-items: center;
            margin-top: 8rpx;
        }

        .title-top-market_price {
            font-size: 24rpx;

            .originalprice-box {
                display: flex;
                font-size: 24rpx;
                color: #fff;
            }

            .discountStock {
                color: #fff;
                position: relative;
                padding: 0 18rpx;
                margin-left: 18rpx;

                &::before {
                    position: absolute;
                    content: "";
                    left: 0;
                    top: 0;
                    width: 2rpx;
                    height: 100%;
                    background-color: #fff;
                }
            }
        }
    }

    .group-time-box {
        align-self: center;
        padding-top: 0;

        .countdown-box {
            align-items: flex-end;
        }

        .title-top-time {
            text-align: right;
            width: 100%;
            font-size: 24rpx;
            color: #fff;
            padding-bottom: 15rpx;
        }

        .countdown {
            .countdown-text {
                font-size: 11px;
                line-height: 11px;
                display: flex;
                align-items: center;
                color: #fff;
                font-variant-numeric: tabular-nums;
                font-feature-settings: "tnum";
                font-family: monospace;
                white-space: nowrap;
            }

            .splitor {
                padding: 0 2.8px;
                font-size: 9.42857px;
            }

            .countdown-bg {
                color: var(--general);
                background-color: #fff;
                padding: 2.4px;
                border-radius: 2.5px;
                display: inline-flex;
                overflow: hidden;
                justify-content: center;
                align-items: center;
            }
        }
    }
}

.product-group-title-box--vortmall {
    padding-bottom: 0;
    background: var(--product-content-bg, #fff);

    .product-group-title {
        min-height: auto;
        padding: 14rpx 28rpx 38rpx;
        background: var(--product-content-bg, #fff);

        .title-top-price {
            .price {
                margin-top: 0;
                color: var(--general);
                font-size: 49rpx;
            }

            .title-top-market_price {
                color: #000;
                font-size: 28rpx;

                .originalprice-box,
                .discountStock {
                    color: #000;
                }

                .discountStock::before {
                    background-color: #000;
                }
            }
        }

        .group-time-box {
            padding-top: 0;

            .title-top-time {
                color: #000;
                padding-bottom: 14rpx;
            }

            .countdown {
                .countdown-text {
                    color: #000;
                    font-size: 24rpx;
                    line-height: 1;
                }

                .splitor {
                    font-size: 24rpx;
                }

                .countdown-bg {
                    padding: 5rpx;
                    color: #fff;
                    background-color: #000;
                    border-radius: 5rpx;
                }
            }
        }
    }
}
</style>
