<template>
    <view class="poster-main">
        <template v-if="img">
            <image :src="img" class="img-box" :style="{ height: boxInfo.boxHeight + 'px' }" @longpress.stop="handleSave" />
        </template>
    </view>
    <canvas
        :id="id"
        class="canvas-content"
        :style="{ height: boxInfo.boxHeight + 'px', width: boxInfo.boxWidth + 'px' }"
        :width="boxInfo.boxWidth"
        :height="boxInfo.boxHeight"
        :canvas-id="id"
    />
</template>

<script setup lang="ts">
import { getCurrentInstance, nextTick, reactive, ref, watch } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource, getImageInfo, saveImageToAlbum } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const instance = getCurrentInstance();
const instanceProxy = instance?.proxy;
const userInfo = uni.getStorageSync("userInfo");
const props = defineProps({
    id: {
        type: String,
        default: "myCanvas"
    },
    currentData: {
        type: Object,
        default: () => {}
    },
    imgHeight: {
        type: Number,
        default: 0
    },
    currentPic: {
        type: String,
        default: ""
    },
    activeType: {
        type: String,
        default: ""
    },
    qrcodePath: {
        type: String,
        default: () => ""
    }
});
const img = ref("");
const isGenerating = ref(false);

const calculateProportionalHeight = (originalWidth: number, originalHeight: number, containerWidth = 260) => {
    let newHeight = (originalHeight * containerWidth) / originalWidth;
    return { width: containerWidth, height: newHeight };
};

const boxInfo = reactive({
    boxHeight: 0,
    boxWidth: 260,
    imgHeight: 0,
    imgWidth: 0
});

const measureTextWidth = async (canvas: any, text: string) => {
    try {
        const res = canvas.measureText(text);
        // 兼容某些端返回 Promise 的情况
        if (res && typeof res.then === "function") {
            const r = await res;
            return Number(r?.width ?? 0);
        }
        return Number(res?.width ?? 0);
    } catch {
        return 0;
    }
};

const truncateTextToWidth = async (canvas: any, text: string, maxWidth: number) => {
    if (!text) return { text: "", width: 0 };
    if (maxWidth <= 0) return { text: "", width: 0 };
    const fullWidth = await measureTextWidth(canvas, text);
    if (fullWidth <= maxWidth) return { text, width: fullWidth };

    const ellipsis = "...";
    const ellipsisWidth = await measureTextWidth(canvas, ellipsis);
    const targetWidth = Math.max(0, maxWidth - ellipsisWidth);

    let result = "";
    for (let i = 0; i < text.length; i++) {
        const next = result + text[i];
        const w = await measureTextWidth(canvas, next);
        if (w > targetWidth) break;
        result = next;
    }
    const finalText = result ? result + ellipsis : ellipsis;
    const finalWidth = await measureTextWidth(canvas, finalText);
    return { text: finalText, width: finalWidth };
};

// 商品名称文字换行，最多三行，超出显示省略号
const drawText = (text: string, x: number, y: number, canvas: any) => {
    canvas.setFontSize(12);
    const lineHeight = 15;
    // 计算可用宽度（总宽度减去左边距和右边距，右边有二维码占80px）
    const maxWidth = boxInfo.boxWidth - x - 80;
    const ellipsis = "...";
    // 估算字符宽度：中文字符约12px，英文字符约6px（12px字体）
    const getCharWidth = (char: string) => {
        // 判断是否为中文字符
        return /[\u4e00-\u9fa5]/.test(char) ? 12 : 6;
    };
    const ellipsisWidth = 18; // 三个点约18px

    // 第一行
    let firstLine = "";
    let firstLineWidth = 0;
    for (let i = 0; i < text.length; i++) {
        const char = text[i];
        const charWidth = getCharWidth(char);
        if (firstLineWidth + charWidth <= maxWidth) {
            firstLine += char;
            firstLineWidth += charWidth;
        } else {
            break;
        }
    }

    // 如果第一行就显示完了，直接绘制
    if (firstLine.length === text.length) {
        canvas.fillText(firstLine, x, y);
        return y;
    }

    // 绘制第一行
    canvas.fillText(firstLine, x, y);

    // 第二行
    let remainingText = text.slice(firstLine.length);
    let secondLine = "";
    let secondLineWidth = 0;
    for (let i = 0; i < remainingText.length; i++) {
        const char = remainingText[i];
        const charWidth = getCharWidth(char);
        if (secondLineWidth + charWidth <= maxWidth) {
            secondLine += char;
            secondLineWidth += charWidth;
        } else {
            break;
        }
    }

    // 如果第二行就显示完了，直接绘制
    if (secondLine.length === remainingText.length) {
        y += lineHeight;
        canvas.fillText(secondLine, x, y);
        return y;
    }

    // 绘制第二行
    y += lineHeight;
    canvas.fillText(secondLine, x, y);

    // 第三行（需要加上省略号）
    remainingText = remainingText.slice(secondLine.length);
    let thirdLine = "";
    let thirdLineWidth = 0;
    const thirdLineMaxWidth = maxWidth - ellipsisWidth;

    for (let i = 0; i < remainingText.length; i++) {
        const char = remainingText[i];
        const charWidth = getCharWidth(char);
        if (thirdLineWidth + charWidth <= thirdLineMaxWidth) {
            thirdLine += char;
            thirdLineWidth += charWidth;
        } else {
            break;
        }
    }

    // 绘制第三行（带省略号）
    y += lineHeight;
    canvas.fillText(thirdLine + ellipsis, x, y);

    return y;
};

const createPoster = async () => {
    if (isGenerating.value) return;
    if (!props.currentPic) return;
    if (!props.qrcodePath) {
        // 二维码通常是异步生成的（tig-qrcode 导出 tempFilePath），小程序下为空会导致 getImageInfo 直接报错
        return;
    }
    isGenerating.value = true;
    const nickname = userInfo.nickname ?? t("普通会员");
    const avatarSrc = userInfo.avatar ? imageFormat(userInfo.avatar) : staticResource("user/defaultAvatar.jpg");
    let context = uni.createCanvasContext(props.id, instanceProxy);
    context.draw();
    context.setFillStyle("#ffffff");

    try {
        const currentPicRes = await getImageInfo(imageFormat(props.currentPic));

        const { height, width } = calculateProportionalHeight(currentPicRes.width, currentPicRes.height);

        boxInfo.boxHeight = height + 150;
        boxInfo.imgHeight = height;
        boxInfo.imgWidth = width;

        context.fillRect(0, 0, boxInfo.boxWidth, boxInfo.boxHeight);

        context.draw(false, async () => {
            const avatarRes = await getImageInfo(avatarSrc);
            context.save();
            context.beginPath();
            context.arc(35, boxInfo.imgHeight + 25, 15, 0, 2 * Math.PI);
            context.clip();
            context.drawImage(avatarRes.path, 15, boxInfo.imgHeight + 5, 40, 40);
            context.restore();
            setTimeout(() => {
                context.draw(true, async () => {
                    context.drawImage(currentPicRes.path, 0, 0, boxInfo.imgWidth, boxInfo.imgHeight);
                    context.draw(true, async () => {
                        const qrcodeRes = await getImageInfo(props.qrcodePath);
                        // 绘制二维码
                        context.drawImage(qrcodeRes.path, boxInfo.boxWidth - 80, boxInfo.imgHeight + 50, 70, 70);
                        // 绘制文字
                        const baseX = 55;
                        const baseY = boxInfo.imgHeight + 30;
                        const maxRight = boxInfo.boxWidth - 80; // 右侧给二维码留白
                        const availableWidth = maxRight - baseX;

                        context.setFontSize(13);
                        // 先测“为您推荐”宽度，决定 nickname 如何裁剪/是否换行
                        const recommendText = t("为您推荐");
                        const recommendWidth = await measureTextWidth(context, recommendText);

                        // 默认同一行展示；如果空间不足则“为您推荐”换到第二行
                        const showRecommendSameLine = recommendWidth + 6 <= availableWidth;
                        const nicknameMaxWidth = showRecommendSameLine ? availableWidth - recommendWidth - 6 : availableWidth;
                        const nicknameFit = await truncateTextToWidth(context, nickname, nicknameMaxWidth);

                        context.setFillStyle("#000000");
                        context.fillText(nicknameFit.text, baseX, baseY);

                        context.setFillStyle("#999999");
                        if (showRecommendSameLine) {
                            context.fillText(recommendText, baseX + nicknameFit.width + 6, baseY);
                        } else {
                            // 放到下一行，避免被挤出画布
                            context.fillText(recommendText, baseX, baseY + 18);
                        }

                        context.setFontSize(12);
                        const y = drawText(props.currentData.productName, 20, boxInfo.imgHeight + 60, context);
                        context.setFontSize(18);
                        context.setFillStyle("#ea122c");
                        context.fillText(formatPrice(props.currentData.productPrice), 20, y + 30);
                        // 不加延迟小程序会出现诡异问题
                        setTimeout(() => {
                            context.draw(true);
                            setTimeout(() => {
                                saveToImg();
                            }, 50);
                        }, 50);
                    });
                });
            }, 50);
        });
    } catch (error) {
        console.error(error);
    } finally {
        isGenerating.value = false;
    }
};

const formatPrice = (price: unknown) => {
    // 兼容接口返回 string/number/null，以及可能包含货币符号的字符串
    const num = typeof price === "number" ? price : parseFloat(String(price ?? "").replace(/[^\d.-]/g, ""));
    if (!Number.isFinite(num)) return "0.00";
    return num.toFixed(2);
};

const saveToImg = () => {
    // 微信小程序对导出尺寸有限制，这里做一下上限保护，避免海报过长时 canvasToTempFilePath 失败
    const EXPORT_MAX = 4096;
    const scale = Math.min(2, EXPORT_MAX / boxInfo.boxWidth, EXPORT_MAX / boxInfo.boxHeight);
    uni.canvasToTempFilePath(
        {
            fileType: "jpg",
            canvasId: props.id,
            quality: 1,
            destHeight: Math.max(1, Math.floor(boxInfo.boxHeight * scale)),
            destWidth: Math.max(1, Math.floor(boxInfo.boxWidth * scale)),
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
        instanceProxy
    );
};

const handleSave = () => {
    // #ifdef APP-PLUS||MP-WEIXIN
    uni.showModal({
        title: t("提示"),
        content: t("确定要保存分享海报吗？"),
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

const currentDataCopy = ref<any>();
watch(
    () => [props.activeType, props.qrcodePath, props.currentPic, props.currentData?.productId],
    () => {
        if (props.activeType && props.activeType === "poster") {
            if (!currentDataCopy.value) {
                nextTick(() => {
                    createPoster();
                });
                currentDataCopy.value = JSON.parse(JSON.stringify(props.currentData));
            } else if (currentDataCopy.value.productId !== props.currentData.productId) {
                img.value = "";
                nextTick(() => {
                    createPoster();
                });
                currentDataCopy.value = JSON.parse(JSON.stringify(props.currentData));
            } else {
                // 二维码/图片异步就绪时也需要重绘（否则第一次 getImageInfo 失败后不会再生成）
                if (!img.value) {
                    nextTick(() => {
                        createPoster();
                    });
                }
            }
        }
    },
    {
        immediate: true
    }
);
</script>

<style lang="scss" scoped>
.poster-main {
    display: flex;
    justify-content: center;
    width: 100%;
    height: 100%;
}
.img-box {
    width: 500rpx;
    box-shadow: 0 2px 16px 0 #0000001a;
    box-sizing: content-box;
}
.canvas-content {
    width: 260px;
    position: fixed;
    top: -9999px;
    left: -9999px;
}
</style>
