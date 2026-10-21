<template>
    <view class="top_card_container">
        <view class="margin">
            <swiper next-margin="16rpx" class="card_swiper" :current="currentIndex" @animationfinish="handleAnimationFinish">
                <template v-for="(item, index) in list" :key="item?.rankId">
                    <!-- 颜色背景 -->
                    <swiper-item v-if="item?.rankCardType === 1" class="card_swiper_item">
                        <view class="card_item" :class="item?.rankIco">
                            <view class="level_name_wrap">
                                <view class="level_name">{{ item?.rankName }}</view>
                                <view class="level_expiry">{{ $t("当前等级有效期至") + " " + rankExpireTime }}</view>
                            </view>
                            <view v-if="item?.rankLevel === userInfo.rankLevel" class="card_badge">{{ $t("当前等级") }}</view>
                            <!-- <view class="card_code_icon" v-if="item?.rank_level === userInfo.rank_level"></view> -->
                            <view class="card_bottom">
                                <!-- 成长值模式 -->
                                <template v-if="userInfo.rankType === 1">
                                    <view v-if="item?.rankLevel === userInfo.rankLevel" class="level_value_wapper">
                                        <text class="level_value">{{ userInfo?.growthPoints }}</text>
                                        <text class="desc">{{ $t("成长值") }}</text>
                                    </view>
                                    <view v-else class="upgrades_wapper">
                                        <text class="desc">{{ $t("再获得") }}</text>
                                        <text class="level_value">{{
                                            item?.minGrowthPoints - userInfo.growth < 0 ? 0 : item?.minGrowthPoints - userInfo.growth
                                        }}</text>
                                        <text class="desc"> {{ $t("成长值升级为该等级") }}</text>
                                    </view>
                                </template>
                                <!-- 消费模式 -->
                                <template v-if="userInfo.rankType === 2">
                                    <view v-if="item?.rankLevel === userInfo.rankLevel" class="level_value_wapper">
                                        <text class="level_value">{{ userInfo?.growthPoints }}</text>
                                        <text class="desc">{{ $t("成长值") }}</text>
                                    </view>
                                    <view v-else class="upgrades_wapper">
                                        <text class="desc">{{ $t("再消费") }}</text>
                                        <text class="level_value">{{
                                            item?.minGrowthPoints - userInfo.growth < 0 ? 0 : item?.minGrowthPoints - userInfo.growth
                                        }}</text>
                                        <text class="desc"> {{ $t("元升级为该等级") }}</text>
                                    </view>
                                </template>
                            </view>
                        </view>
                    </swiper-item>
                    <!-- 自定义背图片景模式 -->
                    <swiper-item v-if="item?.rankCardType === 2" class="card_swiper_item">
                        <view class="card_item" :style="`background-image: url(${item?.rankBg});`">
                            <view class="level_name_wrap">
                                <view class="level_name">{{ item?.rankName }}</view>
                                <view class="level_expiry">{{ $t("当前等级有效期至") + " " + rankExpireTime }}</view>
                            </view>
                            <view v-if="item?.rankLevel === userInfo.rankLevel" class="card_badge">{{ $t("当前等级") }}</view>
                            <view v-if="item?.rankLevel === userInfo.rankLevel" class="card_code_icon" />
                            <view class="card_bottom">
                                <!-- 成长值模式 -->
                                <template v-if="userInfo.rankType === 1">
                                    <view v-if="item?.rankLevel === userInfo.rankLevel" class="level_value_wapper">
                                        <text class="level_value">{{ userInfo?.growthPoints }}</text>
                                        <text class="desc">{{ $t("成长值") }}</text>
                                    </view>
                                    <view v-else class="upgrades_wapper">
                                        <text class="desc">{{ $t("再获得") }}</text>
                                        <text class="level_value">{{
                                            item?.minGrowthPoints - userInfo.growth < 0 ? 0 : item?.minGrowthPoints - userInfo.growth
                                        }}</text>
                                        <text class="desc"> {{ $t("成长值升级为该等级") }}</text>
                                    </view>
                                </template>
                                <!-- 消费模式 -->
                                <template v-if="userInfo.rankType === 2">
                                    <view v-if="item?.rankLevel === userInfo.rankLevel" class="level_value_wapper">
                                        <text class="level_value">{{ userInfo?.growthPoints }}</text>
                                        <text class="desc">{{ $t("成长值") }}</text>
                                    </view>
                                    <view v-else class="upgrades_wapper">
                                        <text class="desc">{{ $t("再消费") }}</text>
                                        <text class="level_value">{{
                                            item?.minGrowthPoints - userInfo.growth < 0 ? 0 : item?.minGrowthPoints - userInfo.growth
                                        }}</text>
                                        <text class="desc"> {{ $t("元升级为该等级") }}</text>
                                    </view>
                                </template>
                            </view>
                        </view>
                    </swiper-item>
                </template>
            </swiper>
        </view>
        <view class="level_rights_list">
            <template v-for="(item, index) in currentRightUnion.slice(0, 4)" :key="index">
                <!-- 固定权益 -->
                <view v-if="item.iconImg" class="right_item" @click="toRightDetail">
                    <view class="icon_img" :class="item.iconImg" />
                    <view class="name">{{ item.rightName }}</view>
                </view>
                <!-- 自定义权益 -->
                <view v-else-if="item.isChecked === 1" class="right_item" @click="toRightDetail">
                    <view class="icon_wapper">
                        <image class="icon" :src="item?.icon" />
                    </view>
                    <view class="name">{{ item?.name }}</view>
                </view>
            </template>
            <!-- 权益长度超过4个 -->
            <view v-if="currentRightUnion.length > 4" class="right_item" @click="toRightDetail">
                <view class="icon_img has_five_right">
                    <view class="num">{{ currentRightUnion.length }}</view>
                    <view class="txt">{{ $t("项") }}</view>
                </view>
                <view class="name">{{ $t("全部权益") }}</view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch } from "vue";
import type { PropType } from "vue";
import type { Rank, Right, RightUnion } from "@/types/user/level";
import { useI18n } from "vue-i18n";
import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { staticResource } from "@/utils";

const { t } = useI18n();

const configStore = useConfigStore();

const { userInfo } = useUserStore();

const props = defineProps({
    list: {
        type: Array as PropType<Rank[]>,
        default: () => []
    }
});

let rightList: any = [];
const currentRight = reactive<Right[]>([]); //当前会员等级的自定义权益
const currentIndex = ref(0); //自动展示当前会员等级卡片索引
const currentCard = reactive<Partial<Rank>>({
    //当前会员卡片信息
    discount: "0.0",
    freeShipping: 0,
    rankPoint: "0"
});
const currentRightUnion = reactive<RightUnion[]>([]); //当前会员等级的自定义权益和固定权益组合
const rankId = ref(0);
watch(
    () => props.list,
    (newVal) => {
        rightList = newVal.map((item) => item.rights);
        rankId.value = newVal[0]?.rankId;
        Object.assign(currentRight, newVal[0]?.rights);
        currentIndex.value = newVal.findIndex((item) => item?.rankLevel === userInfo?.rankLevel);
        if (currentIndex.value === -1) {
            return;
        }
        Object.assign(currentCard, newVal[currentIndex.value]);

        // 合并权益数组
        if (currentCard.discount != "0.0") {
            currentRightUnion.push({ iconImg: "discount", isChecked: 1, rightName: currentCard.discount + t("折") });
        }
        if (currentCard.freeShipping === 1) {
            currentRightUnion.push({ iconImg: "freeShoping", isChecked: 1, rightName: t("包邮") });
        }
        if (currentCard.rankPoint != "0") {
            currentRightUnion.push({ iconImg: "point", isChecked: 1, rightName: currentCard.rankPoint + `${t("倍")}${configStore.integralName}` });
        }
        console.log("currentCard", currentCard);
        currentCard.rights?.forEach((item: Right) => {
            if (item.isChecked == 1) {
                currentRightUnion.push(item);
            }
        });
        console.log("currentCard", currentCard);
        console.log("currentRightUnion", currentRightUnion);
    },
    { deep: true, immediate: true }
);

const rankExpireTime = computed(() => {
    if (userInfo.rankExpireTime == 0) {
        return t("永久");
    } else {
        return userInfo.rankExpireTime?.slice(0, 10);
    }
});
// 滑动事件
const handleAnimationFinish = (e: any) => {
    currentRight.length = 0;
    currentRightUnion.length = 0;
    let index = e.detail.current;
    Object.assign(currentRight, rightList[index]);
    Object.assign(currentCard, props.list[index]);
    rankId.value = props.list[index].rankId;
    // 合并权益数组
    if (currentCard.discount != "0.0") {
        currentRightUnion.push({ iconImg: "discount", isChecked: 1, rightName: currentCard.discount + t("折") });
    }
    if (currentCard.freeShipping == 1) {
        currentRightUnion.push({ iconImg: "freeShoping", isChecked: 1, rightName: t("包邮") });
    }
    if (currentCard.rankPoint != "0") {
        currentRightUnion.push({ iconImg: "point", isChecked: 1, rightName: currentCard.rankPoint + `${t("倍")}${configStore.integralName}` });
    }
    currentCard.rights.forEach((item: Right) => {
        if (item.isChecked == 1) {
            currentRightUnion.push(item);
        }
    });
};

const toRightDetail = () => {
    uni.navigateTo({
        url: "/pages/user/levelCenter/benefit?rankId=" + rankId.value
    });
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
.top_card_container {
    padding-top: 32rpx;
    .margin {
        margin-left: 32rpx;
        .card_swiper {
            width: 100%;
            height: 350rpx;
            .card_swiper_item {
                &:last-child .card_item {
                    margin-right: 16rpx;
                }
                .card_item {
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    position: relative;
                    height: 100%;
                    margin-right: 32rpx;
                    padding: 38rpx 32rpx 32rpx;
                    border-radius: 32rpx;
                    background-repeat: no-repeat;
                    background-size: 100% 100%;

                    .level_name_wrap {
                        .level_name {
                            font-size: 40rpx;
                            color: #fff;
                            font-weight: 700;
                        }
                        .level_expiry {
                            margin-top: 8rpx;
                            font-size: 22rpx;
                            color: #ffffffb3;
                        }
                    }
                    .card_badge {
                        position: absolute;
                        height: 32rpx;
                        line-height: 32rpx;
                        padding: 0 20rpx;
                        color: #fff;
                        font-size: 22rpx;
                        line-height: 28rpx;
                        right: 0rpx;
                        text-align: center;
                        background: rgba(0, 0, 0, 0.3);
                        border-radius: 0 24rpx 0 24rpx;
                        top: 0rpx;
                    }
                    .card_code_icon {
                        position: absolute;
                        top: 64rpx;
                        right: 32rpx;
                        width: 48rpx;
                        height: 48rpx;
                        // background-image: url(@/static/images/user/code_icon.png);
                        background-repeat: no-repeat;
                        background-size: 100%;
                    }
                    .card_bottom {
                        color: #fff;
                        .level_value_wapper {
                            .level_value {
                                font-size: 44rpx;
                            }
                            .desc {
                                margin-left: 15rpx;
                                font-size: 22rpx;
                            }
                        }
                        .upgrades_wapper {
                            display: flex;
                            align-items: baseline;
                            color: #fff;
                            .level_value {
                                margin: 0 10rpx;
                                font-size: 32rpx;
                            }
                            .desc {
                                font-size: 22rpx;
                            }
                        }
                    }
                    &.card1 {
                        color: #75b06d;
                        background:
                            linear-gradient(105.74deg, rgb(117, 176, 109) 19.91%, rgba(117, 176, 109, 0.455) 70.53%, rgb(117, 176, 109) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card2 {
                        color: #47a779;
                        background:
                            linear-gradient(105.74deg, rgb(72, 169, 122) 19.91%, rgba(72, 169, 122, 0.455) 70.53%, rgb(72, 169, 122) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card3 {
                        color: #6ab7de;
                        background:
                            linear-gradient(105.74deg, rgb(106, 183, 222) 19.91%, rgba(106, 183, 222, 0.455) 70.53%, rgb(106, 183, 222) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card4 {
                        color: #6385bd;
                        background:
                            linear-gradient(105.74deg, rgb(99, 133, 189) 19.91%, rgba(99, 133, 189, 0.455) 70.53%, rgb(99, 133, 189) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card5 {
                        color: #9b6fc8;
                        background:
                            linear-gradient(105.74deg, rgb(155, 111, 200) 19.91%, rgba(155, 111, 200, 0.455) 70.53%, rgb(155, 111, 200) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card6 {
                        color: #c0996f;
                        background:
                            linear-gradient(105.74deg, rgb(192, 153, 111) 19.91%, rgba(192, 153, 111, 0.455) 70.53%, rgb(192, 153, 111) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card7 {
                        color: #eec154;
                        background:
                            linear-gradient(105.74deg, rgb(238, 193, 84) 19.91%, rgba(238, 193, 84, 0.455) 70.53%, rgb(238, 193, 84) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card8 {
                        color: #de5850;
                        background:
                            linear-gradient(105.74deg, rgb(222, 88, 80) 19.91%, rgba(222, 88, 80, 0.455) 70.53%, rgb(222, 88, 80) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                    &.card9 {
                        color: #798795;
                        background:
                            linear-gradient(105.74deg, rgb(121, 135, 149) 19.91%, rgba(121, 135, 149, 0.455) 70.53%, rgb(121, 135, 149) 96.44%),
                            linear-gradient(0deg, rgb(255, 255, 255), rgb(255, 255, 255));
                    }
                }
            }
        }
    }

    .level_rights_list {
        display: flex;
        justify-content: center;
        margin: 24rpx 14rpx 16rpx;
        .right_item {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            width: 25%;
            height: 124rpx;
            .icon_img {
                width: 80rpx;
                height: 80rpx;
                background-repeat: no-repeat;
                background-size: 100% 100%;
                &.discount {
                    background-image: v-bind(discountBg);
                }
                &.freeShoping {
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
                text-align: center;
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
}
</style>
