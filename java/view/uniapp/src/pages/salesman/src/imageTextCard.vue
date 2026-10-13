<template>
    <view class="image-text-promote-main">
        <view class="image-text-promote-box">
            <rich-text class="image-text-promote-text" :nodes="promoteText" />
            <view class="image-text-promote-image">
                <scroll-view scroll-x="true">
                    <view class="image-text-promote-image-box">
                        <template v-for="item in currentData.pics" :key="item.picId">
                            <view class="promote-image-item" @click="imagePreview(item.picUrl)">
                                <tig-image :src="item.picUrl" />
                            </view>
                        </template>
                    </view>
                </scroll-view>

                <view class="image-text-btn">
                    <view class="image-text-btn-box" @click="sendShowSaveImageTextCard">
                        <text class="iconfont-h5 icon-xiazai" />
                        {{ $t("保存图文") }}
                    </view>
                </view>
            </view>
        </view>

        <tig-fixed-placeholder height="180rpx" background-color="#fff">
            <view class="bottom-menu">
                <view v-for="(item, index) in menuList" :key="index" class="bottom-menu-item" @click="handleMenu(index)">
                    <view class="item-image">
                        <tig-image :src="staticResource(item.img)" />
                    </view>
                    <text>{{ $t(item.name) }}</text>
                </view>
            </view>
        </tig-fixed-placeholder>
    </view>
</template>

<script setup lang="ts">
/**
 * 图文卡片组件
 * - 展示商品图文分享内容
 * - 支持复制文案
 */
import { imageFormat } from "@/utils/format";
import { copy, staticResource } from "@/utils";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useCurrency } from "@/hooks/useCurrency";
const { t } = useI18n();
const { currencyName } = useCurrency();

const props = defineProps({
    currentData: {
        type: Object,
        default: () => {}
    },
    link: {
        type: String,
        default: ""
    }
});
const promoteText = computed(() => {
    return `📢${t("现在下单超划算")}\n👍${props.currentData.productName}\n💰【${t("售价")}】${props.currentData.productPrice + currencyName.value} \n👉 ${t(
        "购买链接"
    )}： ${props.link}`;
});
const emit = defineEmits(["showSaveImageTextCard", "showShareQrcode", "close"]);
const sendShowSaveImageTextCard = () => {
    copy(promoteText.value, () => {
        emit("showSaveImageTextCard");
    });
};
const handleMenu = (index: number) => {
    switch (index) {
        case 0:
            copy(promoteText.value, () => {
                emit("close");
            });
            break;
        case 1:
            emit("showShareQrcode");
            break;
        case 2:
            uni.navigateTo({
                url: "/pages/salesman/index"
            });
            break;
        case 3:
            uni.navigateTo({
                url: "/pages/salesman/material"
            });
            break;
    }

    if (index === 0)
        copy(promoteText.value, () => {
            emit("close");
        });

    if (index === 1) emit("showShareQrcode");
};
const menuList = [
    {
        name: t("复制链接"),
        img: "salesman/salesmanCopy.png"
    },
    {
        name: t("二维码"),
        img: "salesman/salesmanQrcode.png"
    },
    {
        name: t("分销员中心"),
        img: "salesman/salesman.png"
    },
    {
        name: t("素材中心"),
        img: "salesman/salesmanMeterial.png"
    }
];
const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [imageFormat(url)]
    });
};
</script>

<style lang="scss" scoped>
.image-text-promote-main {
    padding: 0 32rpx;

    .image-text-promote-box {
        position: relative;
    }
}

.image-text-promote-text {
    margin: 24rpx 0 0;
    padding: 18rpx;
    border-radius: 8rpx;
    background-color: #f7f8fa;
    color: #323233;
    font-size: 24rpx;
    line-height: 40rpx;
    overflow: scroll;
    line-break: anywhere;
    white-space: pre-line;
    display: block;
}

.image-text-promote-image {
    position: absolute;
    bottom: -24rpx;
    left: 0;
    width: 100%;
    transform: translateY(100%);

    .image-text-promote-image-box {
        // display: flex;
        white-space: nowrap;
    }

    .promote-image-item {
        display: inline-block;
        width: 200rpx;
        height: 200rpx;
        border-radius: 6rpx;
        margin-right: 16rpx;
        overflow: hidden;
    }
}
.image-text-btn {
    display: flex;
    justify-content: flex-end;
    padding-top: 20rpx;
    .image-text-btn-box {
        width: 162rpx;
        height: 52rpx;
        font-size: 24rpx;
        color: #fff;
        background-color: #f44;
        border-radius: 32rpx;
        display: flex;
        justify-content: center;
        align-items: center;

        .icon-xiazai {
            padding-right: 8rpx;
        }
    }
}
.bottom-menu {
    box-sizing: content-box;
    display: flex;
    align-items: center;
    height: 100%;
    .bottom-menu-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        font-size: 24rpx;
        color: #323233;
        .item-image {
            width: 96rpx;
            height: 96rpx;
            margin-bottom: 10rpx;
        }
    }
}
</style>
