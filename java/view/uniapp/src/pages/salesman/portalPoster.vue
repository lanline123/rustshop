<template>
    <tig-layout :title="$t('名片海报')">
        <canvas id="myCanvas" class="canvas-content" canvas-id="myCanvas" @longpress.stop="handleSave" />
        <view class="portalPoster-box">
            <view class="img-box">
                <image v-if="img" :src="img" @longpress.stop="handleSave" />
            </view>
            <view class="canvas-tip">{{ $t("长按保存图片") }}</view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 门户海报页
 * - 生成分销推广门户海报
 * - 包含分销员二维码
 * - 支持保存到相册
 */
import { getCurrentInstance, ref, computed } from "vue";
import { getQrCode } from "@/api/common";
import { useConfigStore } from "@/store/config";
import { staticResource, getImageInfo, saveImageToAlbum } from "@/utils";
import { imageFormat } from "@/utils/format";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const configStore = useConfigStore();

const userInfo = ref(uni.getStorageSync("userInfo"));
const instance = getCurrentInstance();
let context = uni.createCanvasContext("myCanvas", instance);
const qrcodeImg = ref("");

const link = computed(() => {
    let domain = configStore.baseInfo.h5Domain;

    // #ifdef H5
    domain = domain ? domain : `${window.location.origin}/mobile`;
    // #endif

    return `${domain}/pages/salesman/salesmanCard?salesmanId=${userInfo.value?.salesman?.salesmanId}`;
});

const boxInfo = {
    height: 525,
    width: 345
};

function removeBase64Prefix(base64String: string) {
    if (base64String.includes(",")) {
        return base64String.split(",")[1];
    }
    return base64String;
}

// 生成海报
const createPoster = () => {
    uni.showLoading({
        title: t("生成中"),
        mask: true
    });
    const avatarSrc = userInfo.value.avatar ? imageFormat(userInfo.value.avatar) : staticResource("user/defaultAvatar.jpg");
    const nickname = userInfo.value.nickname ?? t("普通会员");
    context.draw(); //清空原来的画图内容
    context.setFillStyle("#ffffff");
    context.fillRect(0, 0, 345, 600);
    try {
        context.draw(true, async () => {
            const avatarRes = await getImageInfo(avatarSrc);
            context.drawImage(avatarRes.path, 0, 0, 345, 345);
            context.draw(true, async () => {
                context.drawImage(qrcodeImg.value, 345 - 160 - 10, 345 + 15, 150, 150);
                context.setFontSize(13);
                context.setFillStyle("#969799");
                context.fillText(nickname, 20, boxInfo.height - 50);
                context.setFillStyle("#000");
                context.setFontSize(22);
                context.fillText(userInfo.value.mobile, 20, 345 + 80);
                setTimeout(() => {
                    context.draw(true);
                    setTimeout(() => {
                        saveToImg();
                    }, 50);
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

const base64ToPath = () => {
    return new Promise((resolve, reject) => {
        const fs = uni.getFileSystemManager();
        let filePath = `${wx.env.USER_DATA_PATH}/temp_image_${new Date().getTime()}.png`;
        fs.writeFile({
            filePath,
            data: removeBase64Prefix(qrcodeImg.value),
            encoding: "base64",
            success: () => {
                console.log("保存二维码图片成功", filePath);
                resolve(filePath);
            },
            fail: reject
        });
    });
};

const getQrCodeData = async () => {
    try {
        const res = await getQrCode(link.value);
        qrcodeImg.value = res;
        // 小程序绘制
        // #ifdef MP-WEIXIN
        const path = await base64ToPath();
        qrcodeImg.value = path;
        //  #endif

        createPoster();
    } catch (error) {
        console.error("获取二维码失败", error);
    }
};

getQrCodeData();
</script>

<style lang="scss" scoped>
.portalPoster-box {
    margin-top: 80rpx;
    display: flex;
    flex-direction: column;
    align-items: center;

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

    .canvas-tip {
        color: #969799;
        font-size: 26rpx;
        margin-top: 35rpx;
        text-align: center;
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
</style>
