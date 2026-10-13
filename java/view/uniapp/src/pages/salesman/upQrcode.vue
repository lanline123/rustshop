<template>
    <!-- 上传二维码 -->
    <tig-layout>
        <view class="qrcode-wrap">
            <view class="desc">{{ $t("请尽快上传微信二维码，方便客户加你微信联系哦") }}</view>
            <view class="tips">（温馨提示：建议上传正方形、默认黑白样式二维码，方便客户查看）</view>
            <template v-if="personalData.avatar">
                <view class="preview">
                    <view v-show="loading === true" class="preview__overlay" :class="{ preview__overlay_loading: loading === true }">二维码加载中...</view>
                    <view class="preview__overlay">
                        <tig-image :src="personalData.avatar" class="van-image__img" />
                    </view>
                    <view class="van-image" style="width: 560rpx; height: 688rpx">
                        <!-- <img :src="defaultQrcode" class="van-image__img"> -->
                    </view>
                </view>
            </template>
            <template v-else>
                <view class="preview">
                    <view v-show="loading === true" class="preview__overlay">二维码加载中...</view>
                    <view class="van-image" style="width: 560rpx; height: 688rpx">
                        <!-- <img :src="defaultQrcode" class="van-image__img"> -->
                    </view>
                </view>
            </template>
            <tig-fixed-placeholder background-color="#fff">
                <tig-upload request-url="user/user/uploadImg" @change="onUpload">
                    <view class="btn-box">
                        <tig-button class="salesman-btn">上传二维码</tig-button>
                    </view>
                </tig-upload>
            </tig-fixed-placeholder>
        </view>
    </tig-layout>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 上传二维码页
 * - 上传和管理分销员专属二维码
 * - 支持微信、企微等多种二维码
 */
import { ref, reactive } from "vue";
import { getSalesmanPersonal, updateSalesmanPersonal } from "@/api/salesman/salesmanPersonal";
import { onLoad } from "@dcloudio/uni-app";

const { t } = useI18n();

const personalData = reactive<any>({
    qrcode: ""
});

const _getSalesmanPersonal = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getSalesmanPersonal();
        Object.assign(personalData, result);
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const loading = ref(false);
const onUpload = () => {
    loading.value = true;
    saveQrcode();
};

const saveQrcode = async () => {
    try {
        await updateSalesmanPersonal({ ...personalData });
        setTimeout(() => {
            loading.value = false;
        }, 1000);
    } catch (error: any) {
        console.error("表单错误信息：", error);
        uni.$u.toast(error.message);
    }
};

onLoad(() => {
    _getSalesmanPersonal();
});
</script>

<style lang="scss" scoped>
.desc,
.tips {
    width: 100%;
    display: flex;
    justify-content: center;
}
.desc {
    line-height: 48rpx;
    margin: 60rpx auto 16rpx;
    color: #969799;
    font-size: 28rpx;
}
.tips {
    line-height: 34rpx;
    margin: 0 auto 34rpx;
    font-size: 24rpx;
    color: #c8c9cc;
}
.preview {
    position: relative;
    margin: 0 auto 80rpx;
    width: 560rpx;
    box-sizing: border-box;
    height: 688rpx;
    background-color: #fff;
    border: 2rpx solid #fafafa;
    box-shadow: 0 0 60rpx 0 rgba(50, 50, 51, 0.05);
    .preview__overlay {
        opacity: 0.95;
        position: absolute;
        z-index: 99;
        top: 32rpx;
        left: 32rpx;
        background: #fff;
        font-size: 24rpx;
        color: #969799;
        text-align: center;
        width: 496rpx;
        height: 496rpx;
        line-height: 496rpx;
        &.preview__overlay_loading {
            z-index: 100;
        }
    }
}
.van-image {
    position: relative;
    display: inline-block;
}
.van-image__error,
.van-image__img,
.van-image__loading {
    display: block;
    width: 100%;
    height: 100%;
}
.btn-box {
    padding: 25rpx;
    height: 100%;
    .salesman-btn {
        background: linear-gradient(45deg, rgb(255, 170, 30), rgb(255, 114, 13));
        border-color: #fff !important;
        font-size: 28rpx;
    }
}
</style>
