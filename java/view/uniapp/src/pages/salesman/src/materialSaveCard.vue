<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff">
        <view class="share-popup">
            <view class="share-cube">
                <scroll-view class="list-box" scroll-x="true">
                    <view class="list-content">
                        <block v-for="item in currentPics" :key="item.picId">
                            <view class="list-item" @longpress.stop="handleSave(item.picUrl)">
                                <tig-image :src="item.picUrl" />
                            </view>
                        </block>
                    </view>
                </scroll-view>
            </view>
            <tig-fixed-placeholder height="100rpx" background-color="#fff">
                <view class="share-cube-btn" @click="$emit('update:modelValue', false)">{{ $t("我知道了") }}</view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 素材保存卡片组件
 * - 展示素材详情并提供保存功能
 * - 支持图片和视频素材
 */
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource } from "@/utils";
import { isUrl, getDownloadFileInfo, saveImageToAlbum } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    currentPics: {
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
const handleSave = (src: string) => {
    // #ifndef  H5
    let srcFormat = imageFormat(src);
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存图片吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (res.confirm) {
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
    // #endif
};
const materialPopupBg = computed(() => {
    return `url(${staticResource("salesman/materialPopup.png")})`;
});
</script>

<style lang="scss" scoped>
.share-cube {
    height: 732rpx;
    background: v-bind(materialPopupBg) no-repeat 50%;
    background-size: 100% 100%;

    .list-box {
        height: 200rpx;
        width: 100%;
        box-sizing: content-box;
        padding-top: 190rpx;

        .list-content {
            width: 100vw;
            height: 100%;
            white-space: nowrap;
            .list-item {
                display: inline-block;
                width: 216rpx;
                height: 200rpx;
                padding-right: 16rpx;
            }
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
