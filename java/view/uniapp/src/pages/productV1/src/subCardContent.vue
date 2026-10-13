<template>
    <view v-if="hasCycleInfo" class="cycleBuyContent">
        <view class="topBg" />
        <view class="content">
            <view class="header" @click="openPopup">
                <view class="title">{{ $t("次卡兑换") }}</view>
                <view class="tip">{{ $t("一次下单 按需兑换") }}</view>
                <view class="iconfont-h5 icon-a-gengduo20 right" />
            </view>
            <view class="form">
                <view v-for="item in summaryPreviewItems" :key="item.label" class="formItem">
                    <view class="label">{{ item.label }}：</view>
                    <view class="value" :class="{ priceValue: item.isPrice, multilineValue: !item.isPrice }">
                        <template v-if="item.isPrice">
                            <format-price :font-style="{ fontSize: '24rpx' }" :price-data="item.value" />
                            <text>/</text>
                            <text>{{ $t("次") }}</text>
                        </template>
                        <template v-else>
                            <template v-for="(segment, index) in getDateAwareSegments(item.value)" :key="`${item.label}-${index}`">
                                <text :class="{ valueSegmentNowrap: segment.noWrap }">{{ segment.text }}</text>
                            </template>
                        </template>
                    </view>
                </view>
            </view>
        </view>
    </view>

    <tig-popup v-model:show="showPopup" :z-index="999" :show-close="true" @close="handlePopupClose">
        <scroll-view scroll-y="true" class="popupContent">
            <view class="popupTitle">{{ $t("次卡兑换") }}</view>
            <view class="form">
                <view v-if="specDisplayName" class="formItem">
                    <view class="label">{{ $t("规格说明") }}</view>
                    <view class="value">{{ specDisplayName }}</view>
                </view>
                <view v-for="item in summaryItems" :key="item.label" class="formItem">
                    <view class="label">{{ item.label }}</view>
                    <view class="value" :class="{ priceValue: item.isPrice }">
                        <template v-if="item.isPrice">
                            <format-price :font-style="{ fontSize: '25rpx' }" :price-data="item.value" />
                            <text>/</text>
                            <text>{{ $t("次") }}</text>
                        </template>
                        <template v-else>
                            <template v-for="(segment, index) in getDateAwareSegments(item.value)" :key="`${item.label}-${index}`">
                                <text :class="{ valueSegmentNowrap: segment.noWrap }">{{ segment.text }}</text>
                            </template>
                        </template>
                    </view>
                </view>
            </view>
        </scroll-view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 次卡内容组件
 * - 展示次卡兑换次数、折算单价、有效期等信息
 * - 优先展示当前选中的兑换规格
 */
import type { CycleConfig } from "@/types/product/product";
import { computed, ref } from "vue";

const { t } = useI18n();
interface SummaryItem {
    label: string;
    value: string | number;
    isPrice?: boolean;
}

interface TextSegment {
    text: string;
    noWrap: boolean;
}

const props = defineProps<{
    cycleInfo?: CycleConfig;
    productPrice?: string | number;
}>();

const showPopup = ref(false);

const hasCycleInfo = computed(() => {
    return !!props.cycleInfo && Object.keys(props.cycleInfo).length > 0;
});

const cycleInfo = computed(() => props.cycleInfo || {});

const totalTimes = computed(() => {
    const count = Number(
        cycleInfo.value.totalTimes ?? cycleInfo.value.redeemTotalTimes ?? cycleInfo.value.specs?.[0]?.totalTimes ?? cycleInfo.value.specs?.[0]?.periodNum ?? 0
    );
    return Number.isFinite(count) ? count : 0;
});

const totalTimesText = computed(() => {
    return totalTimes.value > 0 ? `${totalTimes.value}${t("次")}` : "-";
});

const unitPriceValue = computed(() => {
    const currentPrice = Number(props.productPrice ?? 0);
    if (totalTimes.value > 0 && Number.isFinite(currentPrice) && currentPrice > 0) {
        return (currentPrice / totalTimes.value).toFixed(2);
    }

    const unitPrice = cycleInfo.value.unitPrice;
    if (unitPrice !== undefined && unitPrice !== null && unitPrice !== "") {
        return unitPrice;
    }

    const fallbackPrice = Number(cycleInfo.value.cyclePrice ?? cycleInfo.value.specs?.[0]?.cyclePrice ?? 0);
    if (totalTimes.value > 0 && Number.isFinite(fallbackPrice) && fallbackPrice > 0) {
        return (fallbackPrice / totalTimes.value).toFixed(2);
    }

    return "0";
});

const specDisplayName = computed(() => {
    return cycleInfo.value.displayName || cycleInfo.value.specs?.[0]?.displayName || "";
});

const getDateAwareSegments = (value: string | number): TextSegment[] => {
    const text = String(value ?? "");
    if (!text) return [{ text: "-", noWrap: false }];

    const splitPattern = /(\d{4}[./-]\d{1,2}[./-]\d{1,2}(?:\s+\d{2}:\d{2}(?::\d{2})?)?)/g;
    const dateTokenPattern = /^\d{4}[./-]\d{1,2}[./-]\d{1,2}(?:\s+\d{2}:\d{2}(?::\d{2})?)?$/;
    const parts = text.split(splitPattern).filter((part) => part !== "");

    return parts.map((part) => {
        const isDateToken = dateTokenPattern.test(part);
        const displayText = isDateToken
            ? part.replace(/^\d{2}(\d{2})(?=[./-])/, "$1").replace(/(\s+\d{2}:\d{2}):\d{2}$/, "$1")
            : part.replace(/\s*至\s*/g, ` ${t("至")} `);

        return {
            text: displayText,
            noWrap: isDateToken
        };
    });
};

const summaryItems = computed<SummaryItem[]>(() => {
    return [
        { label: t("兑换次数"), value: totalTimesText.value },
        { label: t("折算单价"), value: unitPriceValue.value, isPrice: true },
        { label: t("有效期"), value: cycleInfo.value.validityDesc || "-" },
        { label: t("生效时间"), value: cycleInfo.value.effectiveTypeDesc || "-" }
    ];
});

const summaryPreviewItems = computed<SummaryItem[]>(() => {
    return summaryItems.value.slice(0, 3);
});

const handlePopupClose = () => {
    showPopup.value = false;
};

const openPopup = () => {
    showPopup.value = true;
};
</script>

<style scoped lang="scss">
.cycleBuyContent {
    width: 100%;
    min-height: 227rpx;
    border-radius: 8px;
    position: relative;
    background: #fff;
    overflow: hidden;
    margin-top: 20rpx;

    .topBg {
        position: absolute;
        width: 100%;
        height: 50rpx;
        background: linear-gradient(to bottom, rgba(251, 66, 78, 0.2) 0%, #fff 100%);
    }

    .content {
        padding: 20rpx;
        box-sizing: border-box;
        position: relative;
        width: 100%;
        z-index: 9;

        .header {
            display: flex;
            align-items: center;
            line-height: 40rpx;

            .title {
                font-weight: bold;
                font-size: 28rpx;
                color: #2a2a2a;
                margin-right: 20rpx;
            }

            .tip {
                font-weight: 500;
                font-size: 24rpx;
                color: #9a9a9a;
            }

            .right {
                margin-left: auto;
                font-size: 20rpx;
                color: #9a9a9a;
            }
        }

        .form {
            display: flex;
            flex-direction: column;
            gap: 16rpx;
            margin-top: 28rpx;

            .formItem {
                display: flex;
                align-items: flex-start;
                box-sizing: border-box;
                min-width: 0;
                &:last-child {
                    .label {
                        letter-spacing: 3.8px;
                    }
                }

                .label {
                    font-weight: 500;
                    font-size: 24rpx;
                    color: #9a9a9a;
                    flex-shrink: 0;
                    // white-space: nowrap;
                }

                .value {
                    font-weight: 500;
                    font-size: 24rpx;
                    color: #2a2a2a;
                    line-height: 1.5;
                    flex: 1;
                    min-width: 0;
                    text-align: left;

                    &.priceValue {
                        display: flex;
                        align-items: baseline;
                        justify-content: flex-start;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }

                    &.multilineValue {
                        white-space: normal;
                        word-break: keep-all;
                        overflow-wrap: break-word;
                    }
                }
            }
        }
    }
}

.popupContent {
    height: 900rpx;
    padding: 30rpx;
    box-sizing: border-box;

    .popupTitle {
        text-align: center;
        color: #000;
        font-weight: 600;
        font-size: 30rpx;
    }

    .form {
        display: flex;
        flex-direction: column;
        margin-top: 70rpx;
        gap: 60rpx;

        .formItem {
            display: flex;
            gap: 30rpx;
            align-items: baseline;

            .label {
                font-weight: 500;
                font-size: 25rpx;
                color: #9a9a9a;
                flex-shrink: 0;
                min-width: 4em;
            }

            .value {
                font-weight: 500;
                font-size: 25rpx;
                color: #2a2a2a;
                line-height: 1.6;
                // word-break: keep-all;
                overflow-wrap: break-word;

                &.priceValue {
                    display: flex;
                    align-items: center;
                }
            }
        }
    }
}

.valueSegmentNowrap {
    white-space: nowrap;
}
</style>
