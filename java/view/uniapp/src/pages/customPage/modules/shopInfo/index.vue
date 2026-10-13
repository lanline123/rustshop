<template>
    <view :class="'module-ad-con module-store_info ad-store_style__' + module.style">
        <view v-if="module" class="module-ad-content">
            <view class="cap-store-banner">
                <view class="cap-store-banner__cover" :style="'background-image: url(' + backgroundImg + ');'">
                    <view class="cap-store-banner__cover-mask" />
                </view>
                <view class="cap-store-nav-warp">
                    <view class="cap-store-nav-con">
                        <view class="store-search-con">
                            <view id="search" class="flex align-center" @click="toSearchTransferPage">
                                <text class="iconfont-h5 icon-sousuo" />
                                <input type="text" class="search-input" :placeholder="$t('搜商品')" />
                            </view>
                        </view>
                        <view class="store-nav-con">
                            <view class="store-nav-item active">
                                <a class="store-nav-item-a">{{ $t("精选") }}</a>
                            </view>
                            <view class="store-nav-item">
                                <a class="store-nav-item-a" @click="toSearch">{{ $t("商品") }}</a>
                            </view>
                            <view class="store-nav-item">
                                <a class="store-nav-item-a" @click="toCategory">{{ $t("分类") }}</a>
                            </view>
                        </view>
                    </view>
                </view>

                <view class="cap-store-banner__inner">
                    <view class="cap-store-banner__content">
                        <view class="cap-store-banner__logo">
                            <tig-image :src="shopInfo.shopLogo" mode="widthFix" />
                        </view>
                        <view class="cap-store-banner__right-content">
                            <view class="cap-store-banner__right-content-title--middle" @click="toShopInfo">{{ shopInfo.shopTitle }}</view>
                            <view class="cap-store-banner__sum-content">
                                <view class="cap-store-banner__sum-content-total">{{ $t("全部商品") }} {{ shopInfo.productCount }}</view>

                                <view class="cap-store-banner__sum-content-total">{{ $t("新品") }} {{ shopInfo.newProductCount }}</view>
                            </view>
                        </view>
                        <view class="store-collect-button-con" @click="handleShopCollection">
                            <view class="store-collect-button">{{ collectText }}</view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import type { ShopDetailItem } from "@/types/shop/shop";
import { shopCollection } from "@/api/shop/shop";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface Props {
    module: any;
    shopInfo: ShopDetailItem;
}
const props = defineProps<Props>();
const emit = defineEmits(["refreshShopDetail"]);

const imgList = computed(() => {
    let arr = [];
    for (let i = 1; i <= 10; i++) {
        arr.push(`https://lyecs2.oss-cn-zhangjiakou.aliyuncs.com/static/mini/bg/${i}.jpg`);
    }
    return arr;
});
const backgroundImg = computed(() => {
    let url;
    if (props.module.backgroundDefault == 0 && props.module.customPic.picUrl) {
        url = imageFormat(props.module.customPic.picUrl);
    } else {
        url = imgList.value[props.module.backgroundDefault - 1];
    }
    return url;
});

const collectText = computed(() => {
    return props.shopInfo.collectShop ? t("已收藏") : t("收藏");
});

const handleShopCollection = () => {
    if (props.shopInfo.collectShop) {
        cancelCollect();
    } else {
        collect();
    }
};
const collect = async () => {
    try {
        const result = await shopCollection({ shopId: props.shopInfo.shopId });
        uni.showToast({
            title: t("收藏成功")
        });
        emit("refreshShopDetail", props.shopInfo.shopId);
    } catch (error) {
        console.error(error);
    }
};
const cancelCollect = async () => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要取消收藏店铺吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                try {
                    const result = await shopCollection({ shopId: props.shopInfo.shopId });
                    uni.showToast({
                        title: t("操作成功")
                    });
                    emit("refreshShopDetail", props.shopInfo.shopId);
                } catch (error) {
                    console.error(error);
                }
            }
        }
    });
};

const toSearchTransferPage = () => {
    uni.navigateTo({
        url: `/pages/search/index?shopId=${props.shopInfo.shopId}`
    });
};

const toSearch = () => {
    uni.navigateTo({
        url: `/pages/search/result?&shopId=${props.shopInfo.shopId}`
    });
};

const toCategory = () => {
    uni.navigateTo({
        url: `/pages/shop/category?shopId=${props.shopInfo.shopId}`
    });
};

/** 点击店铺名称进入店铺信息页（无快照时仍可打开，只是没有资质区块） */
const toShopInfo = () => {
    if (!props.shopInfo.shopId) return;
    uni.navigateTo({
        url: `/pages/shop/info?shopId=${props.shopInfo.shopId}`
    });
};
</script>
<style lang="scss" scoped>
/*店铺信息*/
.module-store_info {
    position: relative;
    font-size: 24rpx;
    background-color: #fff;
}
.module-store_info .cap-store-banner .cap-store-banner__cover {
    position: relative;
    height: 290rpx;
    background-repeat: no-repeat;
    background-position: 50%;
    background-size: cover;
}
.module-store_info .cap-store-banner .cap-store-banner__cover-mask {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background-color: rgba(0, 0, 0, 0.15);
}
.module-store_info .cap-store-banner .cap-store-banner__inner {
    position: absolute;
    top: 30rpx;
    width: 100%;
    color: #fff;
}
.module-store_info .cap-store-banner .cap-store-banner__content {
    position: relative;
    padding-left: 30rpx;
    overflow: hidden;
}
.module-store_info .cap-store-banner .cap-store-banner__logo {
    float: left;
    width: 116rpx;
    height: 116rpx;
    overflow: hidden;
    vertical-align: bottom;
    background-color: #fff;
    border-radius: 50%;
}
.module-store_info .cap-store-banner .cap-store-banner__logo image {
    max-width: 100%;
    height: auto;
    vertical-align: middle;
    border: none;
}
.module-store_info .cap-store-banner .cap-store-banner__right-content {
    margin-left: 140rpx;
}
.module-store_info .cap-store-banner .cap-store-banner__right-content .cap-store-banner__right-content-title--middle {
    max-width: 440rpx;
    margin-top: 20rpx;
    color: #fff;
    font-weight: 700;
    font-size: 36rpx;
    line-height: 44rpx;
    text-shadow: 0 2rpx 30rpx rgba(0, 0, 0, 0.5);
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    cursor: pointer;
}
.module-store_info .cap-store-banner .cap-store-banner__sum-content {
    margin-top: 30rpx;
    line-height: 24rpx;
    display: flex;
}
.module-store_info .cap-store-banner .cap-store-banner__sum-content-total {
    display: inline-block;
    vertical-align: middle;
    padding-right: 20rpx;
    position: relative;
    &:first-child::after {
        position: absolute;
        top: 0;
        right: 0;
        width: 4rpx;
        height: 24rpx;
        color: #e5e5e5;
        font-size: 20rpx;
        content: "|";
    }
    &:last-child {
        padding-right: 0;
        padding-left: 20rpx;
    }
}
.module-store_info .cap-store-banner .store-collect-button-con {
    position: absolute;
    top: 14rpx;
    right: 20rpx;
    display: block;
    text-align: center;
}
.module-store_info .cap-store-banner .store-collect-button {
    position: relative;
    display: block;
    background: #e93b3d;
    border: 2rpx solid #e93b3d;
    border-radius: 30rpx;
    overflow: hidden;
    box-sizing: border-box;
    height: 56rpx;
    line-height: 56rpx;
    padding: 0 20rpx;
    white-space: nowrap;
    font-size: 28rpx;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
}
.module-store_info .cap-store-banner .store-collect-button.store-collect-button-collected {
    border-color: #fff;
    background: none;
}
.module-store_info .cap-store-banner .store-collect-button text {
    font-size: 28rpx;
}
.module-store_info .cap-store-banner .cap-store-nav-warp {
    position: absolute;
    height: 100rpx;
    top: 180rpx;
    z-index: 9;
    width: 100%;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .cap-store-nav-con {
    display: flex;
    flex-wrap: nowrap;
    height: 100rpx;
    align-items: center;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-search-con {
    width: 164rpx;
    height: 60rpx;
    margin: 0 20rpx;
    padding: 0 10rpx 0 20rpx;
    background: rgba(250, 250, 255, 0.8);
    line-height: 60rpx;
    border-radius: 60rpx;
    position: relative;
    .icon-sousuo {
        margin-right: 6rpx;
        color: gray;
    }
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-search-con .search-input {
    background: transparent;
    border: 0;
    padding: 0;
    height: 60rpx;
    line-height: 60rpx;
    display: block;
    font-size: 24rpx;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-search-con .search-input::-webkit-input-placeholder {
    color: #666;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-search-con .search-input:focus {
    border: 0;
    box-shadow: none;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-nav-con {
    display: flex;
    flex-wrap: nowrap;
    padding-left: 20rpx;
    overflow-x: auto;
    margin-right: 30rpx;
    height: 60rpx;
    position: relative;
    top: 6rpx;
    -webkit-overflow-scrolling: touch;
    flex: 1;
    color: #fff;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-nav-con .store-nav-item {
    margin-right: 40rpx;
    flex: none;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-nav-con .store-nav-item.active:after {
    content: "";
    display: block;
    width: 100%;
    height: 4rpx;
    position: absolute;
    left: 0;
    bottom: -10rpx;
    background: #fff;
    border-radius: 10rpx;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-nav-con .store-nav-item {
    position: relative;
    display: inline-block;
    height: 42rpx;
    font-size: 28rpx;
    line-height: 42rpx;
    font-weight: 700;
    color: #fff;
}
.module-store_info .cap-store-banner .cap-store-nav-warp .store-all-cat-con {
    width: 52rpx;
    text-align: center;
    color: #fff;
}
.ad-store_style__2 {
    height: 476rpx;
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__cover-mask {
    background-color: initial;
    background-image: -webkit-linear-gradient(bottom, #000, transparent);
    background-image: linear-gradient(0deg, #000, transparent);
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__inner {
    top: 210rpx;
}
.ad-store_style__2 .cap-store-banner .cap-store-nav-warp {
    top: 0;
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__logo {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__logo image {
    border-radius: 50%;
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__right-content {
    margin-left: 164rpx;
}

.ad-store_style__3 {
    height: 516rpx;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__cover {
    height: 300rpx;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__inner {
    top: 136rpx;
}
.ad-store_style__3 .cap-store-banner .cap-store-nav-warp {
    top: 0;
}
.ad-store_style__3 .cap-store-banner .store-collect-button-con {
    top: 80rpx;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__cover-mask {
    background-color: initial;
    background-image: -webkit-linear-gradient(bottom, #000, transparent);
    background-image: linear-gradient(0deg, #000, transparent);
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__content {
    padding-left: 0;
    text-align: center;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__logo {
    display: inline-block;
    float: none;
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__right-content {
    margin-left: 0;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__right-content .cap-store-banner__right-content-title--middle {
    display: inline-block;
    max-width: 100%;
    margin-top: 40rpx;
    color: #333;
    text-shadow: none;
}
.ad-store_style__3 .cap-store-banner .cap-store-banner__sum-content {
    margin-top: 20rpx;
    color: #999;
    justify-content: center;
}
.ad-store_style__2 .cap-store-banner .cap-store-banner__sum-content {
    color: #999;
}
.ad-store_style__4 .cap-store-banner {
    height: 440rpx;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__cover {
    height: 100%;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__inner {
    top: 30rpx;
}
.ad-store_style__4 .cap-store-banner .cap-store-nav-warp {
    top: 340rpx;
}
.ad-store_style__4 .cap-store-banner .store-collect-button-con {
    top: 0;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__content {
    padding-left: 0;
    text-align: center;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__logo {
    display: inline-block;
    float: none;
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__right-content {
    margin-left: 0;
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__right-content .cap-store-banner__right-content-title--middle {
    display: inline-block;
    max-width: 100%;
    margin-top: 40rpx;
    padding-bottom: 20rpx;
    color: #fff;
    text-shadow: none;
    border-bottom: 2rpx solid hsla(0, 0%, 100%, 0.5);
}
.ad-store_style__4 .cap-store-banner .cap-store-banner__sum-content {
    margin-top: 10rpx;
    justify-content: center;
}
</style>
