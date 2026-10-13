<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff">
        <view class="share-popup">
            <view class="share-cube">
                <view class="share-cube-tip">
                    <view><text class="iconfont-h5 icon-dagou" />{{ $t("文案/链接已复制") }}</view>
                </view>
                <view class="share-cube-desc">{{ $t("依次长按以下图片保存到相册") }}</view>
                <view class="share-cube-img">
                    <block v-for="(item, index) in currentData.pics" :key="index">
                        <view class="share-cube-img-item" @click="swiperImagePreview(index)">
                            <tig-image :src="item.picUrl" mode="widthFix" @longpress.stop="handleSave(item.picUrl)" />
                        </view>
                    </block>
                </view>
            </view>
            <tig-fixed-placeholder height="100rpx" background-color="#fff">
                <view class="share-cube-btn" @click="$emit('update:modelValue', false)">{{ $t("我知道了") }}</view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 保存图文卡片组件
 * - 保存图文素材到本地
 * - 下载图片到相册
 */
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import { isUrl, getDownloadFileInfo, saveImageToAlbum } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    currentData: {
        type: Object,
        default: () => {}
    }
});
const emit = defineEmits(["update:modelValue"]);
const show = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});
const swiperImagePreview = (index: number) => {
    const images = props.currentData.pics.map((item: any) => imageFormat(item.picUrl || ""));
    uni.previewImage({
        current: images[index],
        urls: images
    });
};
const handleSave = (src: string) => {
    let srcFormat = imageFormat(src);
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存图片吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
                // 保存本地
                // #ifdef H5
                uni.previewImage({
                    urls: [srcFormat]
                });
                // #endif
                // #ifdef APP-PLUS||MP-WEIXIN
                if (isUrl(srcFormat)) {
                    const res = await getDownloadFileInfo(srcFormat);
                    if (res.statusCode === 200) {
                        saveImageToAlbum(res.tempFilePath)
                            .then((res) => {
                                uni.showToast({
                                    title: t("保存成功"),
                                    icon: "none"
                                });
                            })
                            .catch((err) => {
                                uni.showToast({
                                    title: t("保存失败"),
                                    icon: "none"
                                });
                                console.error(err);
                            });
                    }
                }

                // #endif
            }
        }
    });
};
</script>

<style lang="scss" scoped>
.share-popup {
    height: 45vh;
    padding-bottom: 50px;
    overflow: hidden;
    overflow-y: auto;
}
.share-cube {
    padding: 0 15rpx;
    padding-top: 48rpx;
    .share-cube-tip {
        display: flex;
        justify-content: center;
        align-items: center;
        margin: 0 94rpx 52rpx;
        font-size: 26rpx;
        line-height: 32rpx;
        color: #969799;
        .icon-dagou {
            padding-right: 5rpx;
            font-size: 25rpx;
        }
    }

    .share-cube-desc {
        display: flex;
        justify-content: center;
        margin-bottom: 32rpx;
        font-size: 26rpx;
        line-height: 36rpx;
    }
    .share-cube-img {
        width: 100vw;
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        grid-gap: 10rpx;

        .share-cube-img-item {
            border-radius: 4rpx;
            overflow: hidden;
        }
    }
}
.share-cube-btn {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
}
</style>
