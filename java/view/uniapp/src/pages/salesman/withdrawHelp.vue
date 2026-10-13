<template>
    <!-- 帮助中心 -->
    <tig-layout>
        <view class="help-section">
            <view class="help-section__header">
                <view class="help-section__icon" />
                <view class="help-section__title">{{ $t("提现申请提交成功后，多久能入账？") }}</view>
            </view>
            <view class="help-section__content">预计在1-3个工作日到账，请注意查收</view>
        </view>
        <view class="help-section">
            <view class="help-section__header">
                <view class="help-section__icon" />
                <view class="help-section__title">{{ $t("提现失败的主要原因是什么？") }}</view>
            </view>
            <view class="help-section__content">
                1.银行卡：提现银行卡信息有误或者发卡行不支持此类交易，请检查信息或者更换银行卡后重新申请提现
                <br />
                2.微信钱包：请确认您的微信账户是否正常，若已注销需要解除原微信授权，重新添加新的微信授权。请确认您的微信钱包是否实名认证，若未实名认证需要您进行认证
            </view>
        </view>
        <view class="help-section">
            <view class="help-section__header">
                <view class="help-section__icon" />
                <view class="help-section__title">{{ $t("提现金额的限制是多少？") }}</view>
            </view>
            <view class="help-section__content">{{ $t("目前最低提现金额为1元") }}</view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 提现帮助页
 * - 展示提现相关的帮助说明
 * - 常见问题解答
 */
</script>

<style lang="scss" scoped>
.help-section {
    .help-section__header {
        padding: 48rpx 32rpx 0;
        display: flex;
        align-items: baseline;
        .help-section__icon {
            display: inline-block;
            width: 32rpx;
            height: 32rpx;
            margin-right: 16rpx;
            // background: url('@/static/images/salesman/question@2x.png') 0/100% no-repeat;
        }
        .help-section__title {
            font-size: 32rpx;
            line-height: 32rpx;
            color: #333;
        }
    }
    .help-section__content {
        padding: 20rpx 44rpx 40rpx 80rpx;
        font-size: 26rpx;
        line-height: 40rpx;
        color: #666;
    }
}
</style>
