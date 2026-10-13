<template>
    <view>
        <!-- 生成海报的canvas -->
        <canvas id="myCanvas" class="canvas-content" canvas-id="myCanvas" />
        <tig-popup v-model:show="show" background-color="transparent" position="center" :show-close="false" @close="$emit('update:modelValue', false)">
            <view class="poster-box" @touchmove.stop.prevent @longpress.stop="handleSave">
                <image :src="img" style="height: 100%; width: 100%" />
            </view>
            <view class="canvas-tip">{{ $t("长按可保存海报") }}</view>
        </tig-popup>
        <!-- <view v-show="show" class="my-canvas-box" @touchmove.stop.prevent @click="handleClose">
            <view class="canvas-tip">{{ $t("长按可保存海报") }}</view>
        </view> -->
    </view>
</template>

<script setup lang="ts">
import { getCurrentInstance, onMounted, ref } from "vue";
import { imageFormat, pickPic } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useConfigStore } from "@/store/config";
import type { ProductItem } from "@/types/product/product";
import { getMiniCode, getQrCode } from "@/api/common";
import { staticResource, currRoute, getImageInfo, appendQueryParam } from "@/utils";
import { getCurrentSalesmanId } from "@/utils/salesman";
import { useI18n } from "vue-i18n";
import { useCurrency } from "@/hooks/useCurrency";

// 兼容 MP-WEIXIN 环境下的全局 wx（避免 TS 报“找不到名称 wx”）
declare const wx: any;

const { t } = useI18n();
const { currency } = useCurrency();

const instance = getCurrentInstance();
const props = defineProps<{
    modelValue: boolean;
    productId: number;
    productInfo: ProductItem;
    productPrice: string | number;
}>();

const emit = defineEmits(["update:modelValue"]);

const qrImg = ref("");
const show = ref(false);
const powerw = ref(0);

/**
 * 若链接未携带 shopId，则补 shopId=0 代表自营；已有 shopId 时保持不变
 */
const ensureShopId = (url: string): string => {
    if (!url) return url;
    return /[?&]shopId=/.test(url) ? url : appendQueryParam(url, "shopId", 0);
};

onMounted(async () => {
    const { windowWidth } = uni.getSystemInfoSync();
    powerw.value = windowWidth / 375;
    await __getQrCode();
    generatePoster();
});

const __getQrCode = async () => {
    try {
        // 分享人是分销员且商品参与分销时，二维码/小程序码携带分销员ID用于佣金归因
        const isDistribution = props.productInfo.isDistribution;
        const salesmanId = getCurrentSalesmanId();
        const attachSalesman = isDistribution && salesmanId;
        // #ifdef H5
        const shareUrl = attachSalesman ? appendQueryParam(location.href, "salesmanId", salesmanId) : location.href;
        const result = await getQrCode(ensureShopId(shareUrl));
        qrImg.value = result;
        // #endif
        // #ifdef MP-WEIXIN
        const page = currRoute();
        const res = await getMiniCode(page ?? "", props.productId, attachSalesman ? salesmanId : undefined);
        qrImg.value = res;
        // #endif
        // #ifdef APP-PLUS
        const h5Domain = useConfigStore().baseInfo.h5Domain || "";
        const baseUrl = `${h5Domain}/pages/product/index?id=${props.productId}`;
        const appShareUrl = attachSalesman ? appendQueryParam(baseUrl, "salesmanId", salesmanId) : baseUrl;
        const appQr = await getQrCode(ensureShopId(appShareUrl));
        qrImg.value = appQr;
        // #endif
        uni.showToast({
            title: t("正在生成海报，请稍后"),
            icon: "none",
            duration: 1000
        });
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

// 生成海报
const generatePoster = async () => {
    const userInfo = uni.getStorageSync("userInfo");
    const themeStore = useThemeStore();
    // 画布尺寸随屏幕宽度等比缩放，所有坐标、字号统一乘以该系数，
    // 避免大屏（如 PC 端 iframe 嵌入 H5，宽度可达 460px）时头像与商品图重叠
    const s = powerw.value;
    let context = uni.createCanvasContext("myCanvas", instance);
    context.draw();
    context.save();
    context.setFillStyle("#ffffff");
    context.fillRect(0, 0, 500 * s, 900 * s);
    context.draw();
    context.setFontSize(14 * s);
    context.setFillStyle("#000000");
    // 会员名过长时显示省略号，避免溢出/遮挡
    const usernameText = String(userInfo?.username ?? t("普通会员"));
    const nameX = 100 * s;
    const nameY = 48 * s;
    const canvasWidth = 275 * s;
    const nameMaxWidth = Math.max(0, canvasWidth - nameX - 20 * s);
    const usernameEllipsis = await ellipsisText(context, usernameText, nameMaxWidth);
    context.fillText(usernameEllipsis, nameX, nameY);
    context.setFontSize(12 * s);
    context.setFillStyle("#999999");
    context.fillText(t("的分享"), 100 * s, 68 * s);
    // 绘制商品价格
    context.setFillStyle(themeStore.themeStyle["--general"]);
    context.setFontSize(14 * s);
    context.fillText(`${currency.value}`, 20 * s, 366 * s);
    context.setFontSize(18 * s);
    context.fillText(`${props.productPrice}`, 30 * s, 366 * s);
    // 绘制商品名称
    await drawText(props.productInfo.productName || "", 20 * s, 379 * s, context);
    context.save();
    try {
        const avatarRes = await getImageInfo(userInfo.avatar ? imageFormat(userInfo.avatar) : staticResource("user/default_avatar.jpg"));
        // 这个就是绘制圆形头像（按 powerw 等比缩放，并确保图片在圆内居中）
        const avatarCenterX = 50 * s;
        const avatarCenterY = 50 * s;
        const avatarRadius = 28 * s;
        const avatarSize = avatarRadius * 2;

        context.beginPath();
        context.arc(avatarCenterX, avatarCenterY, avatarRadius, 0, 2 * Math.PI);
        context.clip();
        context.drawImage(avatarRes.path, avatarCenterX - avatarRadius, avatarCenterY - avatarRadius, avatarSize, avatarSize);
        context.restore();
        context.draw(true, async () => {
            context.save();
            const picRes = await getImageInfo(
                imageFormat((props.productInfo.picLarge || props.productInfo.picOriginal || pickPic(props.productInfo)) as string)
            );
            // 商品图高度固定为方框高度，按宽高比算宽度并水平居中，完整显示不裁剪；
            // 仅极端宽图时退回以宽度为准，避免超出方框
            const picBox = 245 * s;
            const picBoxX = 15 * s;
            const picBoxY = 92 * s;
            const picRatio = picRes.width && picRes.height ? picRes.width / picRes.height : 1;
            let picH = picBox;
            let picW = picH * picRatio;
            if (picW > picBox) {
                picW = picBox;
                picH = picW / picRatio;
            }
            context.drawImage(picRes.path, picBoxX + (picBox - picW) / 2, picBoxY + (picBox - picH) / 2, picW, picH);
            context.draw(true, () => {
                // 绘制小程序码
                // #ifdef MP-WEIXIN
                const fs = uni.getFileSystemManager();
                let codeimg = wx.env.USER_DATA_PATH + "/" + new Date().getTime() + ".png";
                fs.writeFile({
                    filePath: codeimg,
                    data: qrImg.value.slice(22),
                    encoding: "base64",
                    success: () => {
                        console.log("codeimg", codeimg);
                        context.drawImage(codeimg, 165 * s, 360 * s, 100 * s, 100 * s);
                        context.draw(true, () => {
                            saveToImg();
                        });
                    }
                });

                // #endif
                // #ifdef H5
                context.drawImage(qrImg.value, 165 * s, 360 * s, 100 * s, 100 * s);
                context.draw(true, () => {
                    saveToImg();
                });
                // #endif
            });
        });
    } catch (error) {
        console.error(error);
    }
};

// 封装 measureText 方法，统一返回 Promise
const measureTextAsync = (canvas: any, text: string): Promise<{ width: number }> => {
    return new Promise((resolve) => {
        // #ifdef APP-HARMONY
        // HarmonyOS Next 系统上 measureText 需要传递回调函数作为第二个参数
        canvas.measureText(text, (result: { width: number }) => {
            resolve(result);
        });
        // #endif

        // #ifndef APP-HARMONY
        // 其他平台（H5、小程序等）使用同步方式，包装成 Promise
        const result = canvas.measureText(text);
        if (result instanceof Promise) {
            result.then(resolve);
        } else {
            resolve(result);
        }
        // #endif
    });
};

// 单行文本按最大宽度截断并追加省略号（用于会员名等）
const ellipsisText = async (canvas: any, text: string, maxWidth: number, ellipsis = "…") => {
    if (!text) return "";
    if (maxWidth <= 0) return "";

    const full = await measureTextAsync(canvas, text);
    if (full.width <= maxWidth) return text;

    const ell = await measureTextAsync(canvas, ellipsis);
    // 连省略号都放不下时，直接返回空串（避免绘制出界）
    if (ell.width >= maxWidth) return "";

    // 二分查找可容纳的最大字符数
    let left = 0;
    let right = text.length;
    while (left < right) {
        const mid = Math.ceil((left + right) / 2);
        const sub = text.slice(0, mid);
        const w = await measureTextAsync(canvas, sub);
        if (w.width + ell.width <= maxWidth) {
            left = mid;
        } else {
            right = mid - 1;
        }
    }

    return text.slice(0, left) + ellipsis;
};

// 商品名称文字换行
const drawText = async (text: string, x: number, y: number, canvas: any) => {
    // 与 generatePoster 保持同一套等比缩放系数
    const s = powerw.value;
    let strArr = [];
    // let n = 11;
    // for (let i = 0, l = context.length; i < l / n; i++) {
    //     let a = context.slice(n * i, n * (i + 1));
    //     strArr.push(a);
    // }
    const availableWidth = (375 - 10 - 165) * s;
    let textIndex = 0;
    for (let index = 0; index < 4; index++) {
        let linestr = "";
        while (textIndex < text.length) {
            const char = text[textIndex];
            const testLine = linestr + char;
            // 使用封装的异步方法
            const measureResult = await measureTextAsync(canvas, testLine);
            if (measureResult.width > availableWidth) {
                break;
            }
            linestr = testLine;
            textIndex++;
        }
        strArr.push(linestr);
        linestr = "";
    }
    strArr.forEach((item, index) => {
        // 限制只能显示4行文字
        if (index > 3) {
            return;
        }
        y += 20 * s;
        canvas.setFontSize(12 * s);
        canvas.setFillStyle("#333333");
        canvas.fillText(item, x, y);
    });
};

const img = ref("");
const saveToImg = () => {
    uni.canvasToTempFilePath(
        {
            // 这里修改保存的图片格式
            fileType: "jpg",
            canvasId: "myCanvas",
            quality: 1,
            success: function (res) {
                img.value = res.tempFilePath;
                show.value = true;
                // console.log("img", img.value);
            },
            fail: (err) => {
                uni.showToast({
                    title: JSON.stringify(err),
                    icon: "none",
                    duration: 3000
                });
                show.value = false;
                console.log(err);
            }
        },
        instance
    );
};

const handleSave = () => {
    // #ifdef APP-PLUS||MP-WEIXIN
    uni.saveImageToPhotosAlbum({
        filePath: img.value,
        success: function () {
            uni.showToast({
                title: t("保存海报成功"),
                icon: "none",
                duration: 1500
            });
            setTimeout(() => {
                emit("update:modelValue", false);
            }, 1500);
        },
        fail: function (err) {
            console.error(err);
            uni.showToast({
                title: t("保存海报失败"),
                icon: "none",
                duration: 3000
            });
        }
    });
    // #endif
};
</script>

<style lang="scss" scoped>
.my-canvas-box {
    width: 100vw;
    height: 100vh;
    position: fixed;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 88;
    top: 0;
    left: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.canvas-tip {
    color: #ffffff;
    font-size: 24rpx;
    margin-top: 30rpx;
    text-align: center;
}

.canvas-content {
    width: 550rpx;
    height: 950rpx;
    background-color: #ffffff;
    position: fixed;
    top: -9999px;
    left: -9999px;
}
.poster-box {
    width: 550rpx;
    height: 950rpx;
    border-radius: 20rpx;
    overflow: hidden;
}
</style>
