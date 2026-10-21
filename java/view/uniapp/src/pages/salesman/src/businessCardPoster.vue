<template>
    <view class="my-canvas-box" :class="{ show }" @click="handleClose">
        <view class="canvas-content-box">
            <view class="img-box">
                <image v-if="img" :src="img" @longpress.stop="handleSave" />
            </view>
            <view class="canvas-tip">{{ $t("长按保存图片") }}</view>
        </view>
        <canvas id="myCanvas" class="canvas-content" canvas-id="myCanvas" @longpress.stop="handleSave" />
    </view>
</template>

<script setup lang="ts">
/**
 * 名片海报组件
 * - Canvas绘制分销员名片海报
 * - 包含头像、昵称、二维码
 * - 支持保存到相册
 */
import { getCurrentInstance, onMounted, ref } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource, getImageInfo, saveImageToAlbum } from "@/utils";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const instance = getCurrentInstance();
let context = uni.createCanvasContext("myCanvas", instance);
const props = defineProps<{
    modelValue: boolean;
    userInfo: any;
    qrcodeImg: string;
}>();
const emit = defineEmits(["update:modelValue"]);
const show = ref(false);
const avatarSrc = props.userInfo.avatar ? imageFormat(props.userInfo.avatar) : staticResource("user/defaultAvatar.jpg");

const boxInfo = {
    height: 525,
    width: 345
};

// 生成海报
const createPoster = () => {
    uni.showLoading({
        title: t("生成中"),
        mask: true
    });
    const nickname = props.userInfo.nickname ?? t("普通会员");
    context.draw(); //清空原来的画图内容
    context.setFillStyle("#ffffff");
    context.fillRect(0, 0, 345, 600);
    try {
        context.draw(true, async () => {
            const avatarRes = await getImageInfo(avatarSrc);
            context.drawImage(avatarRes.path, 0, 0, 345, 345);
            context.draw(true, async () => {
                const qrcodeRes = await getImageInfo(props.qrcodeImg);
                // 绘制二维码
                context.drawImage(qrcodeRes.path, 345 - 160 - 10, 345 + 15, 150, 150);
                // 绘制文字
                context.setFontSize(13);
                context.setFillStyle("#969799");
                context.fillText(nickname, 20, boxInfo.height - 50);
                context.setFillStyle("#000");
                context.setFontSize(22);
                context.fillText(props.userInfo.mobile, 20, 345 + 80);
                setTimeout(() => {
                    context.draw(true);
                    setTimeout(() => {
                        saveToImg();
                    }, 50);
                    show.value = props.modelValue;
                }, 50);
            });
        });
    } catch (error) {
        console.error(error);
    } finally {
        uni.hideLoading();
    }
};

const img = ref("");

const saveToImg = () => {
    uni.canvasToTempFilePath(
        {
            fileType: "jpg",
            canvasId: "myCanvas",
            quality: 1,
            destHeight: boxInfo.height * 2,
            destWidth: boxInfo.width * 2,
            success: function (res) {
                img.value = res.tempFilePath;
            },
            fail: (err) => {
                uni.showToast({
                    title: JSON.stringify(err),
                    icon: "none",
                    duration: 3000
                });
                console.log(err);
            }
        },
        instance
    );
};

const handleSave = () => {
    // #ifdef APP-PLUS||MP-WEIXIN
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存图片吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: function (res) {
            if (res.confirm) {
                saveImageToAlbum(img.value)
                    .then(() => {
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
    });
    // #endif
};

const handleClose = () => {
    emit("update:modelValue", false);
};

onMounted(async () => {
    createPoster();
});
</script>

<style lang="scss" scoped>
.my-canvas-box {
    width: 100vw;
    height: 100vh;
    position: fixed;
    background-color: rgba(0, 0, 0, 0.8);
    z-index: 88;
    top: 0;
    left: 0;
    opacity: 0;
    transition: all 0.1s;
    &.show {
        opacity: 1;
    }

    .canvas-content-box {
        display: flex;
        flex-direction: column;
        align-items: center;
        position: fixed;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        width: 100%;

        .canvas-tip {
            color: #ffffff;
            font-size: 24rpx;
            margin-top: 30rpx;
            text-align: center;
        }
    }

    .img-box {
        width: 100%;
        padding: 0 60rpx;
        height: v-bind("boxInfo.height + 'px'");

        image {
            width: 100%;
            height: 100%;
            border-radius: 20rpx;
            overflow: hidden;
        }
    }

    .canvas-content {
        width: 345px;
        height: v-bind("boxInfo.height + 'px'");
        background-color: #ffffff;
        overflow: visible;
        position: fixed;
        top: 9999px;
    }
}
</style>
