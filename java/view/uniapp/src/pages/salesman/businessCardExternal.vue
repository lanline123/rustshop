<template>
    <view>
        <tig-layout :need-safe-top="true" safe-top-bg-color="#f5f5f5">
            <view class="businessCard-box">
                <view class="card-box">
                    <view class="card-img">
                        <tig-image :src="userInfo.avatar" />
                    </view>
                    <view class="card-box-content">
                        <view class="card-box-content-text">
                            <view class="card-box-content-text-mobile">{{ userInfo.mobile }}</view>
                            <view class="card-box-content-text-name">{{ userInfo.nickname }}</view>
                        </view>
                        <view class="card-box-content-qrcode">
                            <image style="height: 100%; width: 100%" :src="qrcodeImg" />
                        </view>
                    </view>
                </view>
                <view class="card-menu">
                    <block v-for="(item, index) in menuList" :key="index">
                        <view class="card-menu-item" @click="handleMenuClick(index)">
                            <image class="card-menu-item-icon" :src="item.icon" />
                            <view class="card-menu-item-text">{{ $t(item.name) }}</view>
                        </view>
                    </block>
                </view>
            </view>
        </tig-layout>
        <guide v-model="showGuide" />
        <businessCardPoster v-if="showPoster" v-model="showPoster" :qrcode-img="qrcodeImg" :user-info="userInfo" />
        <tig-qrcode ref="qrcodeRef" :immediately-create="true" :value="link" :margin="10" :show="false" @success="qrcodeSuccess" />
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 外部名片页
 * - 展示分销员名片（外部访问）
 * - 供非分销员用户查看
 * - 支持分享和保存海报
 */
import { computed, ref } from "vue";
import { onReady, onShareAppMessage, onShareTimeline } from "@dcloudio/uni-app";
import guide from "@/components/product/guide.vue";
import businessCardPoster from "./src/businessCardPoster.vue";
import { useConfigStore } from "@/store/config";
import { staticResource } from "@/utils";

const { t } = useI18n();
const configStore = useConfigStore();

const userInfo = ref(uni.getStorageSync("userInfo"));

const link = computed(() => {
    let domain = configStore.baseInfo.h5Domain;

    // #ifdef H5
    domain = domain ? domain : `${window.location.origin}/mobile`;
    // #endif

    return `${domain}/pages/salesman/salesmanCard?&salesmanId=${userInfo.value?.salesman?.salesmanId}`;
});

const qrcodeRef = ref();
const qrcodeImg = ref<string>("");
const showGuide = ref(false);
const showPoster = ref(false);
const menuList = [
    {
        name: "编辑名片",
        icon: staticResource("salesman/businessCardEdit.png")
    },
    {
        name: "发送给朋友",
        icon: staticResource("salesman/businessCardWechat.png")
    },
    {
        name: "保存到相册",
        icon: staticResource("salesman/businessCardPic.png")
    }
];

onReady(() => {
    qrcodeRef.value?.drawQrcode();
});

const qrcodeSuccess = (e: string) => {
    qrcodeImg.value = e;
};
const handleMenuClick = (index: number) => {
    switch (index) {
        case 0:
            uni.navigateTo({
                url: "/pages/salesman/personalInfo"
            });
            break;
        case 1:
            showGuide.value = true;
            break;
        case 2:
            showPoster.value = true;
            break;
    }
};

onShareAppMessage(() => {
    return {
        title: `${userInfo.value.nickname}${t("的名片")}`
    };
});

onShareTimeline(() => {
    return {
        title: `${userInfo.value.nickname}${t("的名片")}`
    };
});
</script>

<style lang="scss" scoped>
.businessCard-box {
    padding: 25rpx;

    .card-box {
        padding: 40rpx;
        background-color: #fff;
        border-radius: 20rpx;
        box-shadow: 0 0 10rpx rgba(0, 0, 0, 0.1);

        .card-img {
            width: 100%;
            height: 550rpx;
        }

        .card-box-content {
            padding: 72rpx 0 30rpx 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            .card-box-content-text {
                .card-box-content-text-mobile {
                    margin-bottom: 18px;
                    line-height: 30px;
                    font-size: 24px;
                    font-weight: 500;
                }
                .card-box-content-text-name {
                    font-size: 12px;
                    line-height: 16px;
                    color: #969799;
                }
            }

            .card-box-content-qrcode {
                width: 260rpx;
                height: 260rpx;
            }
        }
    }

    .card-menu {
        display: flex;
        justify-content: space-around;
        margin-top: 70rpx;

        .card-menu-item {
            display: flex;
            flex-direction: column;
            align-items: center;

            .card-menu-item-icon {
                width: 68rpx;
                height: 68rpx;
            }

            .card-menu-item-text {
                margin-top: 12rpx;
                font-size: 24rpx;
            }
        }
    }
}
</style>
