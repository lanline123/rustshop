<template>
    <view v-if="showTermPay" class="delivery-card" :style="cardBgStyle">
        <!-- 标题栏 -->
        <view class="delivery-card__title acea-row row-between-wrapper">
            <view class="delivery-card__title-left">
                <view class="delivery-card__title-text">{{ $t("账期支付") }}</view>
                <view class="iconfont-h5 toggle-eye" :class="showAmount ? 'icon-xianshi' : 'icon-buxianshi'" @click.stop="toggleAmount" />
            </view>
            <view class="delivery-card__title-more" @click.stop="handleEnterTermPay">
                <view class="delivery-card__title-more-text">{{ $t("查看详情") }}</view>
                <view class="iconfont icon-xiangyou" />
            </view>
        </view>

        <!-- 内容区域 -->
        <view class="delivery-card__content">
            <view class="delivery-card__wrap">
                <view class="delivery-card__item">
                    <view class="delivery-card__item-number">{{ showAmount ? creditInfo?.creditLimit : "******" }}</view>
                    <view class="delivery-card__item-text">{{ $t("信用额度") }}</view>
                </view>
                <view class="delivery-card__item">
                    <view class="delivery-card__item-number">{{ showAmount ? creditInfo?.usedAmount : "******" }}</view>
                    <view class="delivery-card__item-text">{{ $t("已用额度") }}</view>
                </view>
                <view class="delivery-card__item">
                    <view class="delivery-card__item-number">{{ showAmount ? creditInfo?.availableAmount : "******" }}</view>
                    <view class="delivery-card__item-text">{{ $t("剩余额度") }}</view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 配送订单入口卡片组件
 * @description 显示在用户中心的配送订单入口,展示待取货、配送中、已完成订单数量
 */
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { staticResource } from "@/utils";
import { useCreditInfo } from "@/hooks/useCreditInfo";
const { t } = useI18n();

const userStore = useUserStore();

// 与装修版外壳 fixedFunctionalArea 共用同一份判断，避免外壳把本卡片挡住
const { creditInfo, showTermPay } = useCreditInfo();

/**
 * 响应式数据
 */
const showAmount = ref(false); // 默认不显示金额

const toggleAmount = () => {
    showAmount.value = !showAmount.value;
};

/**
 * 计算属性
 */
// 是否已登录
const isLoggedIn = computed(() => {
    return !!userStore.token || !!uni.getStorageSync("token");
});

// 卡片背景样式
const cardBgStyle = computed(() => {
    const bg = "termPay/termPay_card.png";
    return {
        backgroundImage: `url(${staticResource(bg)})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat"
    };
});

/**
 * 跳转账期详情
 */
const handleEnterTermPay = () => {
    // 检查登录状态
    if (!isLoggedIn.value) {
        uni.showToast({
            title: t("请先登录"),
            icon: "none"
        });
        return;
    }
    uni.navigateTo({
        url: "/pages/user/termPay/detail"
    });
};
</script>

<style lang="scss" scoped>
.delivery-card {
    margin-top: 20rpx;
    margin-bottom: 20rpx;
    margin-left: 0;
    margin-right: 0;
    border-radius: 18rpx;
    overflow: hidden;

    /**
     * 标题栏
     */
    .delivery-card__title {
        height: 88rpx;
        padding-top: 0;
        padding-right: 30rpx;
        padding-bottom: 0;
        padding-left: 30rpx;
        font-size: 26rpx;
        font-weight: bold;
        border-bottom-width: 1px;
        border-bottom-style: solid;
        border-bottom-color: rgba(0, 0, 0, 0.05);

        &.delivery-card__title--no-border {
            border-bottom: none;
        }

        .delivery-card__title-text {
            color: #ae5d34;
            font-size: 26rpx;
            font-weight: 600;
        }

        .delivery-card__title-left {
            display: flex;
            align-items: center;
            column-gap: 12rpx;
        }

        .toggle-eye {
            font-size: 30rpx;
            color: #ae5d34;
        }

        .delivery-card__title-more {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 8rpx 20rpx;
            background-color: #fefdf4;
            border-radius: 40rpx;
            font-size: 22rpx;
            color: #ae5d34;
            font-weight: normal;
            cursor: pointer;

            .delivery-card__title-more-text {
                margin-right: 8rpx;
            }

            .iconfont {
                font-size: 20rpx;
                padding-top: 2rpx;
            }
        }
    }

    /**
     * 内容区域
     */
    .delivery-card__content {
        position: relative;
        min-height: 120rpx;
        padding-top: 30rpx;
        padding-bottom: 30rpx;
        padding-left: 0;
        padding-right: 0;
    }

    /**
     * 订单状态列表
     */
    .delivery-card__wrap {
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        justify-content: space-around;
        align-items: center;
    }

    /**
     * 订单状态项
     */
    .delivery-card__item {
        position: relative;
        flex-grow: 1;
        flex-shrink: 1;
        flex-basis: 0%;
        text-align: center;
        cursor: pointer;
        transition-property: opacity;
        transition-duration: 0.2s;

        &:active {
            opacity: 0.7;
        }

        .delivery-card__item-number {
            font-size: 28rpx;
            font-weight: 600;
            color: #ae5d34;
            margin-bottom: 8rpx;
            line-height: 1.2;
            min-height: 44rpx;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .delivery-card__item-text {
            font-size: 24rpx;
            color: #ae5d34;
            line-height: 1.5;
        }
    }
}
</style>
