<template>
    <tig-layout :title="$t('特权详情')">
        <view class="level_container">
            <view class="level_rights_list">
                <up-scroll-list :indicator="false">
                    <template v-for="(item, index) in currentRightUnion" :key="index">
                        <view v-if="item.isChecked === 1" class="right_item" :class="{ active: current === index }" @click="current = index">
                            <view v-if="item.iconImg" class="icon_img" :class="item.iconImg" />
                            <view v-else-if="item.icon" class="icon_wapper">
                                <image class="icon" :src="item.icon" />
                            </view>
                            <view class="name">{{ item.rightName || item.name }}</view>
                        </view>
                    </template>
                </up-scroll-list>
                <view class="active_line" :style="getActiveLineStyle(current)" />
            </view>
            <view v-if="currentRightUnion.length > 0" class="level_info">
                <view class="info">
                    <view class="title">{{ currentRightUnion[current].name }}</view>
                    <view class="tips">{{ currentRightUnion[current].describe }}</view>
                </view>
            </view>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 会员特权详情页
 * - 根据等级ID展示该等级的特权列表
 * - 包含折扣、包邮、积分倍数等固定权益
 * - 以及自定义权益的名称和描述
 */
import { computed, reactive, ref } from "vue";
import { getUserLevelInfo } from "@/api/user/levelCenter";
import type { RightUnion } from "@/types/user/level";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import { onLoad } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();

const { t } = useI18n();

const current = ref(0); //当前会员等级

const currentRightUnion = reactive<RightUnion[]>([]); //当前会员等级的自定义权益和固定权益组合

const __getUserLevelInfo = async (id: number) => {
    try {
        let result = await getUserLevelInfo({ rankId: id });
        if (result.userRank.discount != "0.0") {
            currentRightUnion.push({ iconImg: "discount", isChecked: 1, name: result.userRank.discount + t("折"), describe: t("下单享受打折优惠") });
        }
        if (result.userRank.freeShipping === 1) {
            currentRightUnion.push({ iconImg: "freeShoping", isChecked: 1, name: t("包邮"), describe: t("下单时享受订单包邮") });
        }
        if (result.userRank.rankPoint != "0") {
            currentRightUnion.push({
                iconImg: "point",
                isChecked: 1,
                name: result.userRank.rankPoint + `${t("倍")}${configStore.integralName}`,
                describe: `${t("赠送多倍")}${configStore.integralName}` + result.userRank.rankPoint + t("倍")
            });
        }
        result.userRank.rights.forEach((item: RightUnion) => {
            if (item.isChecked == 1) {
                currentRightUnion.push(item);
            }
        });
    } catch (error) {
        console.error("获取用户等级信息失败", error);
    }
};
onLoad((options: any) => {
    __getUserLevelInfo(options.rankId);
});
const getActiveLineStyle = (index: number) => {
    console.log(index);
    if (current.value === index) {
        return {
            transform: `translateX(${index * 150 + 40}rpx)`,
            transition: "transform 0.3s ease"
        };
    }
    return {};
};

const discountBg = computed(() => {
    return `url(${staticResource("user/discount.png")})`;
});

const freeShopingBg = computed(() => {
    return `url(${staticResource("user/free_shoping.png")})`;
});

const pointBg = computed(() => {
    return `url(${staticResource("user/point.png")})`;
});
</script>
<style scoped lang="scss">
page {
    height: 100%;
    background: #fff;
}
.level_container {
    display: flex;
    flex-direction: column;
    height: 100%;
    width: 100vw;
    background: #fff;
    position: relative;
    padding-bottom: var(--nav-height);

    .level_rights_list {
        display: flex;
        justify-content: space-between;
        background: linear-gradient(to right, #383e59, #191c30);
        position: relative;
        width: 100vw;
        overflow-x: scroll;
        height: 250rpx;
        padding-top: 50rpx;
        .active_line {
            width: 60rpx;
            height: 4rpx;
            background: #efd6bc;
            border-radius: 50rpx;
            position: absolute;
            bottom: 50rpx;
            left: 0rpx;
            transition: transform 0.2s ease;
        }
        .right_item {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            position: relative;
            width: 150rpx;
            .icon_img {
                width: 80rpx;
                height: 80rpx;
                background-repeat: no-repeat;
                background-size: 100% auto;
                &.discount {
                    background-image: v-bind(discountBg);
                }
                &.free_shoping {
                    background-image: v-bind(freeShopingBg);
                }
                &.point {
                    background-image: v-bind(pointBg);
                }
            }

            .icon_wapper {
                width: 80rpx;
                height: 80rpx;
                border-radius: 40rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                background: #111229cc;
                box-shadow: inset 0 0 16rpx 0 #ffffff4d;
                color: #f9e5d0;
                overflow: hidden;

                .icon {
                    width: 50rpx;
                    height: 50rpx;
                }
            }

            .name {
                margin-top: 10rpx;
                font-size: 24rpx;
                color: rgb(255, 232, 206);
            }

            .has_five_right {
                display: flex;
                justify-content: center;
                align-items: baseline;
                width: 80rpx;
                height: 80rpx;
                border-radius: 40rpx;
                background: #111229cc;
                box-shadow: inset 0 0 16rpx 0 #ffffff4d;
                color: #f9e5d0;
                line-height: 80rpx;

                .num {
                    font-size: 40rpx;
                    font-weight: 600;
                }

                .txt {
                    font-size: 20rpx;
                }
            }
        }
    }
    .level_info {
        width: 100%;
        position: absolute;
        background: #fff;
        top: 220rpx;
        border-radius: 30rpx 30rpx 0 0;
        .info {
            padding: 30rpx 0;
            margin: 0 30rpx;
            border-bottom: 1rpx solid #eee;
            .title {
                font-size: 30rpx;
                color: #333;
                margin-bottom: 10rpx;
                font-weight: 600;
            }
            .tips {
                font-size: 26rpx;
                color: #666;
            }
        }
    }
}
</style>
