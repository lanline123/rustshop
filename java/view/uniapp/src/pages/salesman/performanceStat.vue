<template>
    <!-- 业绩统计 -->
    <tig-layout>
        <timeFilter />
        <view class="salesman-performance-statistics">
            <view class="profit">
                <view class="profit-all van-hairline--bottom">
                    <view class="profit-all__commision">0.00元</view>
                    <view class="desc">
                        <text>{{ $t("累计收益") }}</text>
                        <view class="asset-icon" @click="showTipClick">
                            <up-icon name="question-circle" color="#969799" size="14" />
                            <up-toast ref="uToastRef" />
                        </view>
                    </view>
                    <view class="wait">
                        <text>{{ $t("含待结算:") }}</text>
                        <text>0.00元</text>
                    </view>
                </view>
                <view class="profit-kind">
                    <view class="profit-kind__commision van-hairline--right">
                        <view class="main">0.00元</view>
                        <view class="desc">{{ $t("商品佣金") }}</view>
                        <view class="wait">
                            <text>{{ $t("含待结算:") }}</text>
                            <text>0.00元</text>
                        </view>
                    </view>
                    <view class="profit-kind__invite">
                        <view class="main">0.00元</view>
                        <view class="desc">{{ $t("邀请奖励") }}</view>
                        <view class="wait">
                            <text>{{ $t("含待结算:") }}</text>
                            <text>0.00元</text>
                        </view>
                    </view>
                </view>
            </view>
            <view class="customer">
                <view class="customer-cus van-hairline--right">
                    <view class="main">0.00</view>
                    <view class="desc">累计销售额(元)</view>
                </view>
                <view class="customer-cus van-hairline--right">
                    <view class="main">0</view>
                    <view class="desc">累计订单(笔)</view>
                </view>
                <view class="customer-cus van-hairline--right">
                    <view class="main">0</view>
                    <view class="desc">累计客户(人)</view>
                </view>
            </view>
            <view class="index-noun" @click="showActionSheet = true">{{ $t("查看业绩指标说明") }}</view>
        </view>
        <up-action-sheet :show="showActionSheet" :title="$t('业绩指标说明')" :round="10" @close="showActionSheet = false">
            <view class="explaination">
                <view v-for="perItem in performanceDescList" :key="perItem.perid" class="explaination__item van-cell">
                    <view class="van-cell__title">
                        <view>
                            <view class="explaination__item-title">{{ perItem.name }}</view>
                            <view class="explaination__item-desc">{{ perItem.desc }}</view>
                        </view>
                    </view>
                </view>
            </view>
            <view class="btn-box">
                <tig-button class="salesman-btn" style="width: 100%" @click="showActionSheet = false">{{ $t("知道了") }}</tig-button>
            </view>
        </up-action-sheet>
    </tig-layout>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 业绩统计页
 * - 展示分销业绩数据统计
 * - 支持按时间筛选查看
 * - 显示佣金、订单、客户等统计
 */
import { ref } from "vue";
import timeFilter from "@/pages/salesman/src/timeFilter.vue";

const { t } = useI18n();

const uToastRef = ref<any>(null);
const showTipClick = () => {
    uToastRef.value.show({
        type: "default",
        message: "累计收益 = 商品佣金 + 邀请奖励",
        position: "top"
    });
};

const dateTabActive = ref(0);
const dateTab = (val: any) => {
    dateTabActive.value = val;
    calendarVal.value = "";
};
const showCalendar = ref(false);
const calendarVal = ref();
const confirm = (e: any) => {
    calendarVal.value = e;
    showCalendar.value = false;
    dateTabActive.value = 0;
};

const showActionSheet = ref(false);
const performanceDescList = ref([
    {
        perid: "1",
        name: "销售额",
        desc: t("销售额仅包含推广订单的销售总额")
    },
    {
        perid: "2",
        name: "客户",
        desc: t("和分销员绑定了客户关系的买家或分销员")
    },
    {
        perid: "3",
        name: "邀请",
        desc: t("分销员成功邀请的下级分销员")
    },
    {
        perid: "4",
        name: t("商品佣金"),
        desc: t("分销员推荐客户购买后，分销员商品获得的佣金")
    },
    {
        perid: "5",
        name: t("邀请奖励"),
        desc: t("下级分销员推广商品后，上级获得的奖励")
    }
]);

// const themeColor = ref('var(--main-bg)');
</script>

<style lang="scss" scoped>
.salesman-performance-statistics {
    padding: 0 24rpx;
    .main {
        font-size: 36rpx;
        color: #333;
        font-weight: 700;
        line-height: 48rpx;
    }
    .desc {
        display: flex;
        font-size: 24rpx;
        color: #999;
        line-height: 40rpx;
        margin: 8rpx 0;
        align-items: center;
        gap: 8rpx;
        text {
            font-size: 20rpx;
            color: #ccc;
        }
    }
    .wait {
        display: flex;
        font-size: 24rpx;
        color: #333;
        line-height: 40rpx;
        text:first-child {
            flex-basis: 110rpx;
        }
        text:last-child {
            font-weight: 700;
            flex: 1;
        }
    }
    .profit {
        background: #fff;
        border-radius: 8rpx;
        padding: 24rpx 32rpx 0;
        margin-bottom: 24rpx;
        .profit-all {
            position: relative;
            padding-bottom: 32rpx;
            .profit-all__commision {
                font-size: 48rpx;
                color: #333;
                font-weight: 700;
                line-height: 72rpx;
            }
        }
        .profit-kind {
            position: relative;
            display: flex;
            padding: 32rpx 0;
            .profit-kind__commision {
                position: relative;
                flex: 1;
            }
            .profit-kind__invite {
                padding-left: 24rpx;
                display: flex;
                flex-direction: column;
                flex: 1;
            }
        }
    }
    .customer {
        background: #fff;
        border-radius: 8rpx;
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        padding: 32rpx;
        column-gap: 24rpx;
        .customer-cus {
            position: relative;
            padding-top: 32rpx;
            padding-bottom: 32rpx;
        }
    }
    .index-noun {
        margin-top: 400rpx;
        text-align: center;
        line-height: 36rpx;
        font-size: 28rpx;
        color: #1989fa;
    }
}

.explaination {
    background-color: #fff;
    padding: 0 16px;
    height: 400px;
    overflow-y: scroll;
    .explaination__item {
        padding: 16px 8px;
        .van-cell__title {
            display: flex;
            font-size: 16px;
            &:before {
                content: "•";
                color: #ff720d;
                padding-right: 16rpx;
                font-size: 20rpx;
            }
        }
        .explaination__item-title {
            line-height: 22px;
            font-size: 16px;
            font-weight: 700;
            text-align: left;
        }
        .explaination__item-desc {
            font-size: 14px;
            color: #646566;
        }
    }
}
.btn-box {
    padding: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    font-size: 28rpx;
    .salesman-btn {
        background: linear-gradient(45deg, rgb(255, 170, 30), rgb(255, 114, 13));
        border-color: #fff !important;
    }
}
:deep(.u-action-sheet__header__icon-wrap .u-icon) {
    display: none;
}
:deep(.u-action-sheet__item-wrap__item) {
    display: none;
}

.van-cell {
    position: relative;
    display: flex;
    box-sizing: border-box;
    width: 100%;
    padding: 20rpx 32rpx;
    overflow: hidden;
    color: #323233;
    font-size: 28rpx;
    line-height: 48rpx;
    background-color: #fff;
    .van-cell--clickable {
        cursor: pointer;
    }
    .van-cell__title,
    .van-cell__value {
        flex: 1;
    }
    .van-cell__value {
        position: relative;
        overflow: hidden;
        color: #969799;
        text-align: right;
        vertical-align: middle;
        word-wrap: break-word;
    }
}
.van-cell:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    right: 16px;
    bottom: 0;
    left: 16px;
    border-bottom: 1px solid #ebedf0;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
}
.van-cell:last-child:after {
    border: 0;
}
[class*="van-hairline"]:after {
    position: absolute;
    box-sizing: border-box;
    content: " ";
    pointer-events: none;
    top: -50%;
    right: -50%;
    bottom: -50%;
    left: -50%;
    border: 0 solid #ebedf0;
    transform: scale(0.5);
}
.van-hairline--bottom:after {
    border-bottom-width: 2rpx;
}
.van-hairline--right:after {
    border-right-width: 2rpx;
}
</style>
