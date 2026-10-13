<template>
    <view>
        <tig-layout :need-safe-top="true" safe-top-bg-color="#f5f5f5">
            <view class="my-card">
                <view class="my-card--info-wrap">
                    <view class="user-info-card">
                        <view class="user-info-card--info-wrap van-hairline--bottom">
                            <view class="user-info-card--avatar-wrap">
                                <span class="avatar avatar--type-image avatar--size-default avatar--shape-circle" style="width: 96px; height: 96px">
                                    <tig-image :src="userInfo.avatar" />
                                </span>
                            </view>
                            <view class="user-info-card--info-intr">
                                <view class="user-info-card--section">
                                    <view class="user-info-card--info-intr-name">
                                        <text>{{ userInfo.mobile }}</text>
                                    </view>
                                    <view class="user-info-card--info-intr-office">
                                        <text>{{ userInfo.nickname }}</text>
                                    </view>
                                </view>
                                <!-- <view class="user-info-card--info-intr-wechat">
                                <view class="user-info-card--info-intr-wechat-title">微信:</view>
                                <view class="user-info-card--info-intr-wechat-num ellipsis-content">
                                    <text>1231231</text>
                                </view>
                            </view> -->
                            </view>
                        </view>
                        <view
                            role="button"
                            tabindex="0"
                            class="van-cell van-cell--clickable user-info-card--cell-wrap"
                            @click="handleLink(`/pages/salesman/businessCardExternal`)"
                        >
                            <view class="van-cell__title gray-color">
                                <span>{{ $t("对外名片") }}</span>
                            </view>
                            <view class="van-cell__value qr-icon">
                                <view class="iconfont-h5 icon-erweima" />
                                <up-icon name="arrow-right" color="#969799" size="16" />
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </tig-layout>
    </view>
</template>

<script lang="ts" setup>
/**
 * 分销员名片页
 * - 展示分销员电子名片
 * - 显示用户信息
 */
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
const userInfo = ref<any>({});
onShow(() => {
    userInfo.value = uni.getStorageSync("userInfo");
});

const handleLink = (url: string) => {
    uni.navigateTo({
        url
    });
};
</script>

<style lang="scss" scoped>
.my-card {
    padding: 30rpx;
}
.my-card--info-wrap {
    background: #fff;
    border-radius: 20rpx;
    box-shadow: 0 0 20rpx 0 rgba(0, 0, 0, 0.05);
}
.user-info-card {
    background: #fff;
    border-radius: 20rpx;
    .gray-color {
        color: #969799;
    }
    .qr-icon {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        color: #969799;
    }
}
.user-info-card--info-wrap {
    position: relative;
    display: flex;
    align-items: center;
    padding: 44rpx;
    .user-info-card--avatar-wrap {
        margin-right: 44rpx;
        line-height: 0;
    }
    .user-info-card--info-intr {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        .user-info-card--section {
            margin-bottom: 40rpx;
            .user-info-card--info-intr-name {
                line-height: 40rpx;
                font-size: 36rpx;
                font-weight: 500;
                margin-bottom: 12rpx;
            }
            .user-info-card--info-intr-office {
                font-size: 24rpx;
                color: #969799;
            }
        }
        .user-info-card--info-intr-wechat {
            display: flex;
            font-size: 24rpx;
            line-height: 32rpx;
            .user-info-card--info-intr-wechat-title {
                min-width: 72rpx;
            }
            .user-info-card--info-intr-wechat-num {
                max-width: 242rpx;
            }
        }
    }
}
.user-info-card--cell-wrap {
    border-radius: 20rpx;
}

.ellipsis-content {
    overflow: hidden;
    word-break: break-word;
}
.avatar {
    display: inline-block;
    box-sizing: border-box;
    text-align: center;
    color: #fff;
    cursor: default;
    overflow: hidden;
    position: relative;
    font-size: 32rpx;
    &.avatar--size-default {
        width: 64rpx;
        height: 64rpx;
        line-height: 64rpx;
    }
    &.avatar--shape-circle {
        border-radius: 50%;
        overflow: hidden;
    }
    &.avatar--type-image {
        background: transparent;
        .avatar-image {
            width: 100%;
            height: 100%;
            display: block;
        }
    }
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
    -webkit-transform: scale(0.5);
    transform: scale(0.5);
}
.van-hairline--bottom:after {
    border-bottom-width: 2rpx;
}
</style>
