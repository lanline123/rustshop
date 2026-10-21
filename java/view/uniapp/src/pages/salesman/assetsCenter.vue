<template>
    <tig-layout>
        <view class="assets-block">
            <view class="head-info">
                <view class="rmb_logo" />
                <view class="useful-money">{{ $t("可提现金额") }}</view>
                <view class="profit-sum">
                    <format-price
                        base
                        :font-style="{ fontSize: '48rpx', lineHeight: 1 }"
                        :currency-style="{ selfAlign: 'end', fontSize: '24rpx' }"
                        :price-data="3265.35"
                    />
                </view>
            </view>
            <view class="btn-block">
                <tig-button class="apply" style="width: 100%; font-size: 30rpx" @click="goPages('/pages/salesman/withdraw')">{{ $t("提现") }}</tig-button>
            </view>
            <view class="van-cell-group van-hairline--top-bottom">
                <view role="button" class="van-cell van-cell--clickable" @click="goPages('/pages/salesman/inOutRecordList')">
                    <view class="van-cell__title">
                        <text>{{ $t("收支明细") }}</text>
                    </view>
                    <up-icon name="arrow-right" color="#c8c9cc" size="16" />
                </view>
                <view role="button" class="van-cell van-cell--clickable" @click="goPages('/pages/salesman/withdrawRecordList')">
                    <view class="van-cell__title">
                        <text>{{ $t("提现记录") }}</text>
                    </view>
                    <up-icon name="arrow-right" color="#c8c9cc" size="16" />
                </view>
            </view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 资产中心页
 * - 展示分销员资产统计
 * - 提供佣金明细、提现等入口
 */
const goPages = (url: string) => {
    uni.navigateTo({
        url
    });
};
</script>

<style lang="scss" scoped>
.assets-block {
    .head-info {
        padding: 50px 0 0;
        border-width: 0;
        -webkit-border-image: none;
        border-image: none;
        background-color: initial;
        .rmb_logo {
            width: 160px;
            height: 70px;
            margin: 0 auto;
            // background-image: url('@/static/images/salesman/rmb@2x.png');
            background-size: cover;
            background-repeat: no-repeat;
        }
        .useful-money {
            text-align: center;
            padding: 16px 0 8px;
            font-size: 16px;
        }
        .profit-sum {
            text-align: center;
            font-weight: 500;
        }
    }
    .btn-block {
        padding: 20px 10px;
        margin-top: 20px;
        display: block;
        .apply {
            border-radius: 2px;
            background-color: #07c160 !important;
            border: 1px solid #07c160 !important;
            height: 50px;
        }
    }
}

.van-cell-group {
    position: relative;
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
</style>
