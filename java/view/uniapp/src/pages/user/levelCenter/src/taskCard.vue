<template>
    <view class="task_container">
        <view class="task_head">
            <view class="title">{{ $t("成长任务") }}</view>
            <!-- <view class="iconfont icon-xiangyou"></view> -->
        </view>
        <view class="task_list">
            <view v-if="taskData?.evpi === 1" class="task_item">
                <view class="img" :style="`background-image: url(${staticResource('user/evpi.png')});`" />
                <view class="task_info">
                    <view class="name">{{ $t("完善信息") }}</view>
                    <view class="growth_value">{{ isOverseas() ? $t("+{0}成长值", [taskData?.evpiGrowth]) : `+${taskData?.evpiGrowth}成长值` }}</view>
                </view>
                <navigator url="/pages/user/profile/index" class="do_task_btn">{{ $t("去完成") }}</navigator>
            </view>
            <view v-if="taskData?.bindPhone === 1" class="task_item">
                <view class="img" :style="`background-image: url(${staticResource('user/bind_phone.png')});`" />
                <view class="task_info">
                    <view class="name">{{ $t("绑定手机号") }}</view>
                    <view class="growth_value">{{ isOverseas() ? $t("+{0}成长值", [taskData?.bindPhoneGrowth]) : `+${taskData?.bindPhoneGrowth}成长值` }}</view>
                </view>
                <navigator url="/pages/user/profile/index" class="do_task_btn">{{ $t("去完成") }}</navigator>
            </view>
            <view v-if="taskData?.buyGoods === 1" class="task_item">
                <view class="img" :style="`background-image: url(${staticResource('user/buy_goods.png')});`" />
                <view class="task_info">
                    <view class="name">{{ $t("购买商品") }}</view>
                    <view class="growth_value">
                        {{
                            isOverseas()
                                ? $t("每消费{0}元，获得{1}点成长值", [taskData?.buyGoodsMoney, taskData?.buyGoodsGrowth])
                                : `每消费${taskData?.buyGoodsMoney}元，获得${taskData?.buyGoodsGrowth}点成长值`
                        }}
                    </view>
                </view>
                <navigator url="/" open-type="switchTab" class="do_task_btn">{{ $t("去完成") }}</navigator>
            </view>
            <view v-if="taskData?.buyOrder === 1" class="task_item">
                <view class="img" :style="`background-image: url(${staticResource('user/buy_order.png')});`" />
                <view class="task_info">
                    <view class="name">{{ $t("完成下单") }}</view>
                    <view class="growth_value">{{
                        isOverseas()
                            ? $t("每完成{0}笔订单，获得{1}点成长值", [taskData?.buyOrderNumber, taskData?.buyOrderGrowth])
                            : `每完成${taskData?.buyOrderNumber}笔订单，获得${taskData?.buyOrderGrowth}点成长值`
                    }}</view>
                </view>
                <navigator url="/pages/cart/index" open-type="switchTab" class="do_task_btn">{{ $t("去完成") }}</navigator>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed, reactive } from "vue";
import { isOverseas, staticResource } from "@/utils";
import { getBaseConfig } from "@/api/common";

const taskData = reactive({});

const __getBaseConfig = async () => {
    try {
        let result = await getBaseConfig();
        Object.assign(taskData, result?.growUpSetting);
        console.log(taskData);
    } catch (error) {}
};
__getBaseConfig();

const taskBg = computed(() => {
    return `url(${staticResource("user/task_bg.png")})`;
});
</script>
<style scoped lang="scss">
.task_container {
    width: 100%;
    height: fit-content;
    padding: 24rpx;
    border-radius: 24rpx;
    background-color: #fff;
    background-image: v-bind(taskBg);
    background-repeat: no-repeat;
    background-size: 100% auto;
    .task_head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .title {
            font-size: 32rpx;
            color: #333;
            font-weight: 600;
        }
        .iconfont {
            color: #9d9d9d;
        }
    }
    .task_list {
        margin-top: 24rpx;
        .task_item {
            display: flex;
            align-items: center;
            height: 80rpx;
            margin-bottom: 46rpx;
            margin-right: 10rpx;
            &:last-child {
                margin-bottom: 10rpx;
            }
            .img {
                width: 80rpx;
                height: 80rpx;
                background-repeat: no-repeat;
                background-size: 100% 100%;
            }
            .task_info {
                display: flex;
                flex-direction: column;
                height: 100%;
                margin-left: 20rpx;
                .name {
                    color: #323233;
                    font-size: 28rpx;
                    font-weight: bold;
                    margin-bottom: auto;
                }
                .growth_value {
                    color: #7d7e80;
                    font-size: 24rpx;
                }
            }
            .do_task_btn {
                width: 120rpx;
                height: 48rpx;
                margin-left: auto;
                color: #fff;
                line-height: 48rpx;
                overflow: hidden;
                text-align: center;
                color: #463c32;
                font-size: 24rpx;
                background-color: #e7c5a5;
                border-radius: 52rpx;
            }
        }
    }
}
</style>
