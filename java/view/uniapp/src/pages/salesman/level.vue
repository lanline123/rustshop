<template>
    <tig-layout :need-safe-top="true">
        <view class="level">
            <view class="level-dashboard">
                <view class="container">
                    <view class="header">
                        <template v-if="member.avatar">
                            <tig-image class="avatar" :src="member.avatar" />
                        </template>
                        <template v-else>
                            <image :src="staticResource('salesman/avatar.png')" class="avatar" />
                        </template>
                        <view class="level-name">{{ salesmanData.levelName }}</view>
                    </view>
                    <view class="level-stepper">
                        <view class="level-stepper-wrap less">
                            <template v-if="nextLevelData.name">
                                <view class="level-stepper-wrap__point active reach">
                                    <view class="inner-text">{{ salesmanData.levelName }}</view>
                                </view>
                                <view class="level-stepper-wrap__line">
                                    <view class="line-inner progress" :style="`width: ${nextLevelGapData.totalPerc}%;`" />
                                </view>
                                <view class="level-stepper-wrap__point">
                                    <view class="inner-text">{{ nextLevelData.name }}</view>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>
                <view v-if="tuiGuangRate(salesmanData.level) > 0" class="rights-and-interests">
                    <view class="item">
                        <view class="title-icon tui" />
                        <view>
                            <view>{{ tuiGuangRate(salesmanData.level) }}%</view>
                            <view>{{ $t("推广佣金比") }}</view>
                        </view>
                    </view>
                </view>
                <view class="level-dashboard__rate-text">
                    <up-icon name="info-circle" color="#999" size="12" />
                    {{ $t("部分商品的佣金比例可能因商家设置而有所不同，请以商品中所显示的为准") }}
                </view>
            </view>
            <view v-if="nextLevelData" class="level-schedule">
                <view class="title">{{ $t("满足以下规则可升级为") }}{{ nextLevelData.name }}</view>
                <view class="level-schedule__status">
                    <view v-if="nextLevelData.condition?.salesAmount.checked" class="level-schedule-item">
                        <view class="tag-wrap">
                            <view
                                class="level-schedule-item__tag van-tag van-tag--plain van-tag--default"
                                :class="{ 'level-schedule-item__reach': nextLevelGapData.salesAmountInfo.salesAmountReach }"
                            >
                                {{ $t(nextLevelGapData.salesAmountInfo.salesAmountReach ? "已达标" : "未达标") }}
                            </view>
                        </view>
                        <view class="tag-desc"
                            >{{ $t("推广金额达") }} {{ nextLevelData.condition?.salesAmount.value + displayCurrencyName }}
                            <block v-if="nextLevelGapData.salesAmountInfo.salesAmountReach === false">
                                (还差<text class="level-schedule-item__left">{{ nextLevelGapData.salesAmountInfo.salesAmountGap }}</text>
                                {{ displayCurrencyName }})
                            </block>
                        </view>
                    </view>
                    <view v-if="nextLevelData.condition?.salesInviteUsers.checked" class="level-schedule-item">
                        <view class="tag-wrap">
                            <view
                                class="level-schedule-item__tag van-tag van-tag--plain van-tag--default"
                                :class="{ 'level-schedule-item__reach': nextLevelGapData.salesInviteUsersInfo.salesInviteUsersReach }"
                            >
                                {{ nextLevelGapData.salesInviteUsersInfo.salesInviteUsersReach ? $t("已达标") : $t("未达标") }}
                            </view>
                        </view>
                        <view class="tag-desc"
                            >{{ $t("累计客户数达") }} {{ nextLevelData.condition?.salesInviteUsers.value }} {{ $t("人") }}
                            <block v-if="nextLevelGapData.salesInviteUsersInfo.salesInviteUsersReach === false">
                                (还差<text class="level-schedule-item__left">{{ nextLevelGapData.salesInviteUsersInfo.salesInviteUsersGap }}</text> 人)
                            </block>
                        </view>
                    </view>
                    <view v-if="nextLevelData.condition?.selfBuyAmount.checked" class="level-schedule-item">
                        <view class="tag-wrap">
                            <view
                                class="level-schedule-item__tag van-tag van-tag--plain van-tag--default"
                                :class="{ 'level-schedule-item__reach': nextLevelGapData.selfBuyAmountInfo.selfBuyAmountReach }"
                            >
                                {{ nextLevelGapData.selfBuyAmountInfo.selfBuyAmountReach ? $t("已达标") : $t("未达标") }}
                            </view>
                        </view>
                        <view class="tag-desc"
                            >{{ $t("自购金额达") }} {{ nextLevelData.condition?.selfBuyAmount.value + displayCurrencyName }}
                            <block v-if="nextLevelGapData.selfBuyAmountInfo.selfBuyAmountReach === false">
                                ({{ $t("还差") }}<text class="level-schedule-item__left">{{ nextLevelGapData.selfBuyAmountInfo.selfBuyAmountGap }}</text>
                                {{ displayCurrencyName }})
                            </block>
                        </view>
                    </view>
                </view>
            </view>
            <view v-if="levelRules" class="level-rules">
                <block v-for="rule in levelRules" :key="rule.id">
                    <view class="rules-item">
                        <view class="label">{{ rule.name }}</view>
                        <view class="desc">
                            <view class="desc-item">
                                <view class="desc-item__title">{{ $t("规则介绍") }}</view>
                                <view v-if="rule.condition.salesAmount.checked"
                                    >{{ rule.condition.salesAmount.title + $t("满") || $t("推广金额满") }} {{ rule.condition.salesAmount.value }}
                                    {{ amountRuleUnit(rule.condition.salesAmount.unit) }}</view
                                >
                                <view v-if="rule.condition.salesInviteUsers.checked"
                                    >{{ rule.condition.salesInviteUsers.title + $t("满") || $t("发展客户数满") }} {{ rule.condition.salesInviteUsers.value }}
                                    {{ rule.condition.salesInviteUsers.unit || $t("人") }}</view
                                >
                                <view v-if="rule.condition.selfBuyAmount.checked"
                                    >{{ rule.condition.selfBuyAmount.title + $t("满") || $t("自购金额满") }} {{ rule.condition.selfBuyAmount.value }}
                                    {{ amountRuleUnit(rule.condition.selfBuyAmount.unit) }}</view
                                >
                            </view>
                            <view class="desc-item">
                                <view class="desc-item__title">{{ $t("权益介绍") }}</view>
                                <view>{{ $t("推广佣金比为") }}{{ rule.rate }}%</view>
                            </view>
                        </view>
                    </view>
                </block>
            </view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 分销员等级页
 * - 展示当前等级和升级进度
 * - 显示等级权益对比
 * - 提供升级条件说明
 */
import { computed, ref } from "vue";
import { getUser, getSalesman } from "@/api/salesman/salesmanCenter";
import type { UserItem, SalesmanItem } from "@/types/salesman/salesmanCenter";
import { onLoad } from "@dcloudio/uni-app";
import { isOverseas, staticResource } from "@/utils";
import { useCurrency } from "@/hooks/useCurrency";

const { t } = useI18n();

const { baseCurrencyName, currencyName } = useCurrency();
const displayCurrencyName = computed(() => (isOverseas() ? baseCurrencyName.value : currencyName.value));
const amountRuleUnit = (unit: string) => (isOverseas() ? baseCurrencyName.value || unit : unit || currencyName.value);
const member = ref<UserItem>({} as UserItem);
const _getUser = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getUser();
        member.value = result;
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const levelRules = ref<any>([]);
const salesmanData = ref<SalesmanItem>({} as SalesmanItem);
const nextLevelData = ref<any>({}); // 下个等级的数据
const nextLevelGapData = ref<any>({}); // 下个等级相差数据
const _getSalesman = async () => {
    try {
        const result = await getSalesman();
        salesmanData.value = result;
        levelRules.value = result.salesmanConfig.level;
        nextLevelData.value = salesmanData.value.salesmanConfig.level[salesmanData.value.level];
        nextLevelGapData.value = nextLevelInfo(salesmanData.value.order, nextLevelData.value);
    } catch (error) {
        console.error(error);
    }
};

const tuiGuangRate = (curLevel: number) => {
    return levelRules.value[curLevel - 1]?.rate;
};

const nextLevelInfo = (orderPrams: any, nextLevelPrams: any) => {
    let salesAmount = nextLevelPrams.condition.salesAmount.value;
    let salesInviteUsers = nextLevelPrams.condition.salesInviteUsers.value;
    let selfBuyAmount = nextLevelPrams.condition.selfBuyAmount.value;
    let obj: any = {
        totalPerc: 0,
        salesAmountInfo: {
            salesAmountReach: false,
            salesAmountGap: ""
        },
        salesInviteUsersInfo: {
            salesInviteUsersReach: false,
            salesInviteUsersGap: ""
        },
        selfBuyAmountInfo: {
            selfBuyAmountReach: false,
            selfBuyAmountGap: ""
        }
    };
    let perc = 0;
    let salesAmountGap = orderPrams.allSum - salesAmount;
    let salesInviteUsersGap = orderPrams.allUserCount - salesInviteUsers;
    let selfBuyAmountGap = orderPrams.selfBuySum - selfBuyAmount;
    if (salesAmountGap >= 0) {
        obj.salesAmountInfo.salesAmountReach = true;
        perc += 1;
    }
    if (salesInviteUsersGap >= 0) {
        obj.salesInviteUsersInfo.salesInviteUsersReach = true;
        perc += 1;
    }
    if (selfBuyAmountGap >= 0) {
        obj.selfBuyAmountInfo.selfBuyAmountReach = true;
        perc += 1;
    }
    obj.totalPerc = ((perc / 3) * 100).toFixed(2);
    obj.salesAmountInfo.salesAmountGap = Math.abs(salesAmountGap);
    obj.salesInviteUsersInfo.salesInviteUsersGap = Math.abs(salesInviteUsersGap);
    obj.selfBuyAmountInfo.selfBuyAmountGap = Math.abs(selfBuyAmountGap);
    return obj;
};

const containerBg = computed(() => {
    return `url(${staticResource("salesman/viprankbg.png")})`;
});

const tuiguangBg = computed(() => {
    return `url(${staticResource("salesman/tuiguang.png")})`;
});

onLoad(() => {
    _getUser();
    _getSalesman();
});
</script>

<style lang="scss" scoped>
.level {
    position: relative;
    .level-dashboard {
        padding: 16rpx 16rpx 16rpx 16rpx;
        background-color: #fff;
        .container {
            background-image: v-bind(containerBg);
            background-size: 100% 100%;
            box-sizing: border-box;
            padding: 8rpx 16rpx 20rpx;
            position: relative;
            .header {
                display: flex;
                align-items: center;
                margin: 34rpx 0 0 40rpx;
                .avatar {
                    display: block;
                    width: 88rpx;
                    height: 88rpx;
                    border-radius: 50%;
                    margin-right: 20rpx;
                }
                .level-name {
                    font-size: 36rpx;
                    color: #fff;
                    font-weight: 700;
                }
            }
            .level-stepper {
                margin: 30rpx 30rpx 0;
                overflow-x: auto;
                height: 120rpx;
                .level-stepper-wrap {
                    display: flex;
                    align-items: center;
                    flex-wrap: nowrap;
                    .level-stepper-wrap__point {
                        background-color: #fff;
                        height: 12rpx;
                        width: 12rpx;
                        margin: 10rpx;
                        border-radius: 50%;
                        flex-shrink: 0;
                        position: relative;
                        opacity: 0.5;
                        &:first-child {
                            margin-left: 52rpx;
                            .inner-text {
                                max-width: 120rpx;
                            }
                        }
                        &:last-child {
                            margin-right: 52rpx;
                        }
                        &.reach {
                            opacity: 1;
                        }
                        &.active {
                            background-color: hsla(0, 0%, 100%, 0.4);
                            height: 24rpx;
                            width: 24rpx;
                            position: relative;
                            .inner-text {
                                top: 32rpx;
                            }
                            &:after {
                                content: "";
                                background-color: #fff;
                                height: 12rpx;
                                width: 12rpx;
                                margin: 10rpx;
                                border-radius: 50%;
                                top: -4rpx;
                                left: -4rpx;
                                position: absolute;
                            }
                        }
                        .inner-text {
                            position: absolute;
                            z-index: 1;
                            transform: translateX(-50%);
                            left: 50%;
                            top: 26rpx;
                            font-size: 20rpx;
                            color: #fff;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }
                    }
                    .level-stepper-wrap__line {
                        background-color: #ba955c;
                        height: 4rpx;
                        width: 200rpx;
                        flex-shrink: 0;
                        flex-grow: 1;
                        width: auto;
                        .line-inner {
                            height: 4rpx;
                            background-color: #efddc2;
                            width: 80%;
                        }
                    }
                }
            }
        }
        .rights-and-interests {
            display: flex;
            margin: 14rpx 0;
            .item {
                width: 50%;
                font-size: 26rpx;
                color: #666;
                line-height: 1.4;
                display: flex;
                align-items: center;
                .title-icon {
                    width: 88rpx;
                    height: 88rpx;
                    background-color: #fcf5e6;
                    justify-content: center;
                    border-radius: 50%;
                    font-size: 40rpx;
                    color: #dfc392;
                    margin-left: 30rpx;
                    margin-right: 20rpx;
                    background-size: 100% 100%;
                    &.tui {
                        background-image: v-bind(tuiguangBg);
                    }
                }
            }
        }
        .level-dashboard__rate-text {
            display: flex;
            align-items: baseline;
            column-gap: 8rpx;
            line-height: 48rpx;
            color: #999;
            font-size: 24rpx;
            padding: 20rpx 16rpx 0 16rpx;
        }
    }
    .level-schedule {
        margin: 20rpx 0;
        background-color: #fff;
        padding: 40rpx 32rpx;
        line-height: 1.5;
        .title {
            font-size: 36rpx;
            font-weight: 700;
            margin-bottom: 40rpx;
        }
        .level-schedule-item {
            color: #999;
            margin-top: 30rpx;
            font-size: 28rpx;
            display: flex;
            align-items: center;
            line-height: 36rpx;
            column-gap: 8rpx;
            .tag-wrap {
                min-width: 96rpx;
                display: flex;
                align-items: center;
                .level-schedule-item__tag {
                    color: rgb(187, 187, 187);
                    border-color: rgb(187, 187, 187);
                    &.level-schedule-item__reach {
                        color: #dbbb87;
                        border-color: #dbbb87;
                    }
                }
            }
            .tag-desc {
            }
            .level-schedule-item__left {
                width: fit-content;
                color: #dbbb87;
                margin: 0 8rpx;
                word-break: break-all;
            }
        }
    }
    .level-rules {
        background-color: #fff;
        padding: 40rpx 30rpx;
        .title {
            font-size: 36rpx;
            font-weight: 700;
            margin-bottom: 40rpx;
        }
        .rules-item {
            position: relative;
            .label {
                background-color: #f6f6f6;
                height: 48rpx;
                line-height: 48rpx;
                padding-left: 60rpx;
                padding-right: 20rpx;
                display: inline-block;
                border-radius: 48rpx;
                font-size: 24rpx;
                background-image: v-bind(containerBg);
                background-size: 48rpx 48rpx;
                background-position: 0 0;
                background-repeat: no-repeat;
            }
            .desc {
                margin-left: 60rpx;
                margin-bottom: 30rpx;
                line-height: 1.5;
                .desc-item {
                    margin-top: 30rpx;
                    color: #999;
                    font-size: 28rpx;
                    .desc-item__title {
                        font-weight: 700;
                        margin: 30rpx 0 10rpx;
                        color: #333;
                    }
                }
            }
        }
    }
}
.van-tag {
    position: relative;
    display: inline-flex;
    align-items: center;
    padding: 0 8rpx;
    color: #fff;
    font-size: 24rpx;
    line-height: 32rpx;
    border-radius: 4rpx;
}
.van-tag--plain {
    background-color: #fff;
    border-color: currentColor;
}
.van-tag--plain:before {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    border: 2rpx solid;
    border-color: inherit;
    border-radius: inherit;
    content: "";
    pointer-events: none;
}
.van-tag--default.van-tag--plain {
    color: #969799;
}
</style>
