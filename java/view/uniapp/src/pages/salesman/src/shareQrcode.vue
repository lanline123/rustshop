<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff" position="center">
        <view class="qrcode-box" @longpress.stop="handleSave">
            <image :src="qrcodePath" :margin="20" mode="aspectFit" style="width: 245px; height: 245px" />
            <view class="qrcode-tip"> {{ $t("长按图片保存至相册") }} </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 分享二维码组件
 * - 展示推广二维码图片
 * - 支持保存到相册
 */
import { computed } from "vue";
import { saveImageToAlbum } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    link: {
        type: String,
        default: ""
    },
    qrcodePath: {
        type: String,
        default: ""
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

const handleSave = () => {
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存二维码吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: function (res) {
            if (res.confirm) {
                // 保存本地
                // #ifdef H5
                uni.previewImage({
                    urls: [props.qrcodePath]
                });
                // #endif
                // #ifdef APP-PLUS||MP-WEIXIN
                saveImageToAlbum(props.qrcodePath)
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
                // #endif
            }
        }
    });
};
</script>

<style lang="scss" scoped>
.qrcode-box {
    height: inherit;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    padding: 10rpx;
    .qrcode-tip {
        position: absolute;
        bottom: -80rpx;
        color: #e5e5e5;
        width: 100%;
        text-align: center;
        font-size: 24rpx;
    }
}
</style>
