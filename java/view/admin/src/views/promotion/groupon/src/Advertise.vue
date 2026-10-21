<template>
    <div class="btn" @click="handleShow">
        <slot> </slot>
    </div>
    <template v-if="show">
        <el-dialog
            class="sign-dialog"
            :append-to-body="true"
            :show-close="false"
            v-model="showDialog"
            width="900"
            top="12vh"
            v-bind="$attrs"
            @close="handleClose"
            @closed="show = false"
        >
            <template #header>
                <div class="header">
                    <div class="header-title">{{ $attrs.title || $t('推广') }}</div>
                    <div class="header-close" @click="showDialog = false">
                        <i class="iconfont-admin icon-cha1"></i>
                    </div>
                </div>
            </template>
            <a-spin :spinning="loading" style="width: 100%; height: 100%">
                <el-row class="orfanize-content">
                    <el-col :span="4">
                        <div class="tabs-row">
                            <div class="tabs" v-for="item in tabs" :class="{ active: item.value === currentTab }" @click="handleTabChange(item.value)">
                                {{ item.name }}
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="20">
                        <div class="content">
                            <div class="poster-img-container">
                                <el-image class="poster-img" :src="srcList[0]" fit="cover" />
                                <div class="poster-enlarge" @click="showPreview = true">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                        <g fill="none" fill-rule="evenodd">
                                            <path d="M0 0h16v16H0z"></path>
                                            <path
                                                d="M7.333 2a5.333 5.333 0 014.109 8.734l2.578 2.58-.707.706-2.579-2.578A5.333 5.333 0 117.333 2zm0 1a4.333 4.333 0 100 8.667 4.333 4.333 0 000-8.667zm.5 2v1.833h1.834v1H7.833v1.834h-1V7.833H5v-1h1.833V5h1z"
                                                fill="currentColor"
                                            ></path>
                                        </g>
                                    </svg>
                                </div>
                            </div>
                            <div class="poster-info">
                                <div class="poster-title" style="margin-top: 0">{{ $t('素材下载') }}</div>
                                <div class="poster-actions">
                                    <el-button bg class="buttonColor mr10" size="large" text type="primary" @click="handlePosterDownload">{{ $t('下载海报') }}</el-button>
                                    <el-button bg class="buttonColor mr10" size="large" text type="primary" @click="handleQRCode">{{ $t('下载二维码') }}</el-button>
                                </div>
                                <div class="poster-title">{{ $t('路径复制') }}</div>
                                <div class="poster-paths">
                                    <div class="poster-item" style="border-bottom: 1px solid #e9ecf0">
                                        <span class="poster-item-text">{{ $t('适用于微信内会话/朋友圈等场景') }}</span>
                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                                            <template #content>
                                                <div style="max-width: 300px; padding: 5px 10px">
                                                    {{ shortLink }}
                                                </div>
                                            </template>
                                            <a class="btn-link" @click="handleCopy(shortLink)">{{ $t('复制短链接') }}</a>
                                        </el-tooltip>
                                    </div>
                                    <div v-if="currentTab === 1" class="poster-item">
                                        <span class="poster-item-text">{{ $t('适用于微信内会话/朋友圈等场景') }}</span>
                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                                            <template #content>
                                                <div style="max-width: 300px; padding: 5px 10px">
                                                    {{ originalLink }}
                                                </div>
                                            </template>
                                            <a class="btn-link" @click="handleCopy(originalLink)">{{ $t('复制原链接') }}</a>
                                        </el-tooltip>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </a-spin>
            <el-image-viewer v-if="showPreview" :url-list="srcList" :initial-index="0" show-progress @close="showPreview = false" />
        </el-dialog>
    </template>
</template>

<script setup lang="ts">
import { GrouponFilterState } from "@/types/promotion/groupon";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineOptions({
    inheritAttrs: false
});

import { computed, onUnmounted, PropType, ref } from "vue";
import { getGrouponPromoteData } from "@/api/promotion/groupon";
import requestExport from "@/utils/export";
import { message } from "ant-design-vue";
import { urlWapFormat } from "@/utils/format";

const props = defineProps({
    item: {
        type: Object as PropType<GrouponFilterState>,
        default: () => ({})
    }
});

const emit = defineEmits(["callBack"]);
const showPreview = ref(false);
const show = ref(false);
const showDialog = ref(false);
const loading = ref<boolean>(false);
const srcList = ref<string[]>([]);
const previewSrcList = ref<string[]>([]);
const promoteData = ref<any>({});
const tabs = ref([
    {
        name: t('H5/公众号'),
        value: 1
    },
    {
        name: t('小程序'),
        value: 2
    }
]);

const currentTab = ref(1);

const qrcodeTypes = [{ id: 0, name: "H5" }];

// 短链
const shortLink = computed(() => {
    return currentTab.value === 1 ? promoteData.value.shortLink : promoteData.value.wechatShortLink;
});

// 原链
const originalLink = computed(() => {
    return promoteData.value.originalLink;
});

/**
 * 生成二维码
 * @param url 二维码内容地址
 * @param colorDark 二维码颜色
 * @param colorLight 二维码背景色
 * @returns 二维码图片地址
 */
const generateQRCodeImage = async (url: string, colorDark: string = "#000", colorLight: string = "#FFF") => {
    const data = url; // 替换为你要转换的链接
    let opts = {
        margin: 2, //二维码留白边距,
        width: 300, // 设置二维码尺寸
        color: { dark: colorDark, light: colorLight }
    };
    const QRCodeModule = await import("qrcode");
    return await QRCodeModule.default.toDataURL(data, opts);
};

const drawCurrentPoster = async () => {
    const posterImgUrl = promoteData.value.promotionPicUrl;
    const qrcodeContent = currentTab.value === 1 ? promoteData.value.shortLink : promoteData.value.miniCode;
    const posterUrl = await drwaPoster(posterImgUrl, qrcodeContent);
    srcList.value[0] = posterUrl.small;
    previewSrcList.value[0] = posterUrl.main;
};

const loadData = async () => {
    loading.value = true;
    try {
        // 加载数据
        promoteData.value = await getGrouponPromoteData({ grouponPromotionId: props.item.grouponPromotionId! });
        // promoteData.value.shortLink = urlWapFormat({ path: promoteData.value.shortLink });
        // if (promoteData.value.originalLink && promoteData.value.originalLink.startsWith("/mobile")) {
        //     promoteData.value.originalLink = promoteData.value.originalLink.substring(7);
        // }
        // promoteData.value.originalLink = urlWapFormat({ path: promoteData.value.originalLink });
        //绘制当前海报
        drawCurrentPoster();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onUnmounted(() => {
    if (srcList.value.length) {
        srcList.value.forEach((item) => {
            URL.revokeObjectURL(item);
        });
        srcList.value = [];
    }
    if (previewSrcList.value.length) {
        previewSrcList.value.forEach((item) => {
            URL.revokeObjectURL(item);
        });
        previewSrcList.value = [];
    }
});

const handleShow = () => {
    show.value = true;
    showDialog.value = true;
    loadData();
};

const handleClose = () => {
    showDialog.value = false;
};

const getFileBlob = async (url: string): Promise<Blob> => {
    // 使用 fetch 获取图片数据
    const response = await fetch(url);
    return await response.blob();
};

const drwaPoster = async (imgUrl: string, qrcode: string): Promise<{ main: string; small: string }> => {
    try {
        const img = new Image();
        img.crossOrigin = "anonymous";

        // 等待图片加载完成
        await new Promise((resolve, reject) => {
            img.onload = resolve;
            img.onerror = reject;
            img.src = imgUrl;
        });

        // 设置固定canvas宽度
        const canvasWidth = 750;
        const horizontalPadding = 50; // 左右边距
        const topMargin = 210; // 上边距
        const bottomMargin = 130; // 下边距
        const imageAreaWidth = canvasWidth - 2 * horizontalPadding; // 图片区域宽度

        const innerTopPadding = 65;
        const innerBottomPadding = 305;
        const qrCodeSize = 130;

        // 计算图片的缩放比例
        const scale = imageAreaWidth / img.width;
        const imgHeight = img.height * scale;

        // 创建canvas
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        if (!ctx) {
            throw new Error(t('无法获取canvas上下文'));
        }
        // 设置canvas尺寸（图片尺寸 + 留白）
        const contentHeight = innerTopPadding + imgHeight + innerBottomPadding;
        canvas.width = canvasWidth;
        canvas.height = topMargin + contentHeight + bottomMargin;

        // 创建渐变背景 (从 F2F2F6 到 E6E6EE)
        const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
        gradient.addColorStop(0, "#F2F2F6");
        gradient.addColorStop(1, "#E6E6EE");

        // 填充渐变背景
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // 头部文字
        ctx.fillStyle = "#333";
        ctx.font = "30px Arial";
        ctx.textAlign = "center";
        ctx.fillText("拼团", canvas.width / 2, topMargin / 2 + 20);

        // 绘制主要内容区域（白色背景）
        const contentX = horizontalPadding;
        const contentY = topMargin;
        const contentWidth = imageAreaWidth;
        const contentHeightTotal = contentHeight;

        // 绘制阴影效果
        const shadowBlur = 20;
        const shadowOffsetX = 0;
        const shadowOffsetY = 20;
        ctx.save();
        ctx.shadowColor = "rgba(0, 0, 0, 0.1)";
        ctx.shadowBlur = shadowBlur;
        ctx.shadowOffsetX = shadowOffsetX;
        ctx.shadowOffsetY = shadowOffsetY;

        // 绘制圆角背景区域（带阴影）
        const contentRadius = 30;
        ctx.beginPath();
        ctx.moveTo(contentX + contentRadius, contentY);
        ctx.lineTo(contentX + contentWidth - contentRadius, contentY);
        ctx.quadraticCurveTo(contentX + contentWidth, contentY, contentX + contentWidth, contentY + contentRadius);
        ctx.lineTo(contentX + contentWidth, contentY + contentHeightTotal - contentRadius);
        ctx.quadraticCurveTo(contentX + contentWidth, contentY + contentHeightTotal, contentX + contentWidth - contentRadius, contentY + contentHeightTotal);
        ctx.lineTo(contentX + contentRadius, contentY + contentHeightTotal);
        ctx.quadraticCurveTo(contentX, contentY + contentHeightTotal, contentX, contentY + contentHeightTotal - contentRadius);
        ctx.lineTo(contentX, contentY + contentRadius);
        ctx.quadraticCurveTo(contentX, contentY, contentX + contentRadius, contentY);
        ctx.closePath();

        // 填充白色背景
        ctx.fillStyle = "#FFFFFF";
        ctx.fill();
        ctx.restore(); // 恢复，移除阴影效果

        // 剪切区域，使内容具有圆角效果
        ctx.save();
        ctx.beginPath();
        ctx.moveTo(contentX + contentRadius, contentY);
        ctx.lineTo(contentX + contentWidth - contentRadius, contentY);
        ctx.quadraticCurveTo(contentX + contentWidth, contentY, contentX + contentWidth, contentY + contentRadius);
        ctx.lineTo(contentX + contentWidth, contentY + contentHeightTotal - contentRadius);
        ctx.quadraticCurveTo(contentX + contentWidth, contentY + contentHeightTotal, contentX + contentWidth - contentRadius, contentY + contentHeightTotal);
        ctx.lineTo(contentX + contentRadius, contentY + contentHeightTotal);
        ctx.quadraticCurveTo(contentX, contentY + contentHeightTotal, contentX, contentY + contentHeightTotal - contentRadius);
        ctx.lineTo(contentX, contentY + contentRadius);
        ctx.quadraticCurveTo(contentX, contentY, contentX + contentRadius, contentY);
        ctx.closePath();
        ctx.clip();

        // 绘制顶部留白区域
        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(contentX, contentY, contentWidth, innerTopPadding);

        // 绘制图片
        ctx.drawImage(img, contentX, contentY + innerTopPadding, contentWidth, 500);

        // 绘制底部留白区域
        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(contentX, contentY + innerTopPadding + imgHeight, contentWidth, innerBottomPadding);

        // 恢复绘图上下文
        ctx.restore();

        // 在内部底部留白区域添加信息
        const infoAreaStartY = contentY + innerTopPadding + imgHeight;

        // 生成二维码
        // 判断下是否是base64图片
        const qrCodeUrl = qrcode.startsWith("data:")
            ? qrcode
            : await generateQRCodeImage(
                  qrcode,
                  "#333", // 蓝色前景（二维码点）
                  "#ffffff00" // 白色背景
              );

        const qrImg = new Image();

        await new Promise((resolve, reject) => {
            qrImg.onload = resolve;
            qrImg.onerror = reject;
            qrImg.src = qrCodeUrl;
        });

        const groupNameX = contentX + 30;
        const groupNameY = infoAreaStartY + innerBottomPadding - qrCodeSize - 10;

        // 绘制团名
        ctx.fillStyle = "#333";
        ctx.font = "30px Arial";
        ctx.textAlign = "left";
        ctx.fillText(promoteData.value.promotionName, groupNameX, groupNameY);

        // 绘制拼团时间
        const timeY = infoAreaStartY + innerBottomPadding - 30;
        ctx.fillStyle = "#888";
        ctx.font = "22px Arial";
        ctx.textAlign = "left";
        ctx.fillText(promoteData.value.promotionDateTime, groupNameX, timeY);

        // 在底部右侧绘制二维码
        const qrX = contentX + contentWidth - qrCodeSize - 20;
        const qrY = infoAreaStartY + innerBottomPadding - qrCodeSize - 50;
        ctx.drawImage(qrImg, qrX, qrY, qrCodeSize, qrCodeSize);

        // 添加文字说明
        ctx.fillStyle = "#666";
        ctx.font = "16px Arial";
        ctx.textAlign = "right";
        ctx.fillText("长按识别查看", qrX + qrCodeSize - 18, qrY + qrCodeSize + 20);

        // 转换为blob并下载
        return new Promise((resolve) => {
            let url: any = window.URL || window.webkitURL; // 兼容不同浏览器的 URL 对象
            canvas.toBlob(async (blob) => {
                if (blob) {
                    const mainPosterUrl = url.createObjectURL(blob);
                    // 生成小尺寸海报
                    // const smallPosterUrl = await generateSmallPoster(canvas);
                    resolve({
                        main: mainPosterUrl,
                        small: mainPosterUrl
                    });
                }
            }, "image/png");
        });
    } catch (error) {
        console.error("生成海报失败:", error);
        return Promise.reject(error);
    }
};

/**
 * 生成小尺寸海报
 * @param originalCanvas 原始海报画布
 * @returns 缩小后的海报URL
 */
const generateSmallPoster = (originalCanvas: HTMLCanvasElement): Promise<string> => {
    return new Promise((resolve) => {
        // 计算缩放比例
        const targetWidth = 467;
        const scale = targetWidth / originalCanvas.width;
        const targetHeight = originalCanvas.height * scale;

        // 创建新的小尺寸画布
        const smallCanvas = document.createElement("canvas");
        smallCanvas.width = targetWidth;
        smallCanvas.height = targetHeight;
        const ctx = smallCanvas.getContext("2d");

        if (!ctx) {
            throw new Error(t('无法获取canvas上下文'));
        }

        // 绘制缩小后的图像
        ctx.drawImage(originalCanvas, 0, 0, originalCanvas.width, originalCanvas.height, 0, 0, targetWidth, targetHeight);

        // 转换为blob并返回URL
        smallCanvas.toBlob((blob) => {
            if (blob) {
                const url = URL.createObjectURL(blob);
                resolve(url);
            }
        }, "image/png");
    });
};

const download = async (blobUrl: string, filename: string) => {
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
};

// 下载海报
const handlePosterDownload = async () => {
    try {
        loading.value = true;
        // 获取图片扩展名
        const url = previewSrcList.value[0];
        download(url, t('{promotionName}海报.png', { promotionName: promoteData.value.promotionName }));
        // const ext = url.split('.').pop() || 'jpg';
        // const blob = await getFileBlob(url);
        // requestExport(blob, "海报", "." + ext);
    } catch (error) {
        console.error("下载失败:", error);
    } finally {
        loading.value = false;
    }
};

const base64ToBlob = (base64: string) => {
    const byteCharacters = btoa(base64.split(",")[1]);
};

// 下载二维码
const handleQRCode = async () => {
    try {
        loading.value = true;
        if (currentTab.value === 1) {
            // H5/公众号二维码
            const h5QRCodeUrl = await generateQRCodeImage(promoteData.value.shortLink);
            const h5QRCodeBlob = await getFileBlob(h5QRCodeUrl);
            requestExport(h5QRCodeBlob, t('{promotionName}H5/公众号二维码', { promotionName: promoteData.value.promotionName }), ".png");
        } else {
            // 直接下载base64图片
            download(promoteData.value.miniCode, t('{promotionName}小程序二维码.png', { promotionName: promoteData.value.promotionName }));
        }
    } catch (error) {
        console.error("下载失败:", error);
    } finally {
        loading.value = false;
    }
};

// 复制链接功能
const handleCopy = (text: string) => {
    // 复制短链
    if (navigator.clipboard) {
        navigator.clipboard
            .writeText(text)
            .then(() => {
                message.success(t('复制成功'));
            })
            .catch((err) => {
                console.error("复制失败:", err);
            });
    } else {
        // 兼容性处理：使用传统的 execCommand 方法
        const textarea = document.createElement("textarea");
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        try {
            const successful = document.execCommand("copy");
            if (successful) {
                message.success(t('复制成功'));
            }
        } catch (err) {
            console.error("复制失败:", err);
        }
        document.body.removeChild(textarea);
    }
};

/**
 * tab切换
 * @param value 值
 */
const handleTabChange = (value: number) => {
    if (currentTab.value !== value) {
        currentTab.value = value;
        // 重新绘制海报
        drawCurrentPoster();
    }
};
</script>

<style lang="less">
.el-overlay-dialog {
    .sign-dialog {
        padding: 20px 24px;
        border-radius: 4px;

        .el-dialog__header {
            padding: 0;
            border: none;
        }

        .el-form-item:last-child {
            margin-bottom: 0;
        }
    }

    .header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;

        .header-title {
            color: #1e2226;
            font-weight: 600;
            font-size: 16px;
            line-height: 24px;
        }

        .icon-cha1 {
            cursor: pointer;
            font-size: 12px;
            padding: 6px 0px 6px 8px;
            font-weight: normal;
            color: #8a9099;
            transition: color 0.2s;

            &:hover {
                color: #1e2226;
            }
        }
    }

    .content {
        // border: solid 1px #ebebeb;
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        height: 500px;
        padding: 20px 16px 16px;

        .poster-img-container {
            position: relative;
            flex-shrink: 0;
            margin-right: 20px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            max-height: 390px;
            width: 240px;

            .poster-img {
                border-radius: 6px;
                width: 100%;
                height: 100%;
                // box-shadow: 0px 3px 6px 0px rgb(0 0 0 / 30%);
            }

            .poster-enlarge {
                cursor: pointer;
                position: absolute;
                top: 0;
                right: 0;
                background-color: #1f2026;
                opacity: 0.7;
                padding: 3px;
                display: flex;
                justify-content: center;
                align-items: center;
                border-bottom-left-radius: 8px;
                border-top-right-radius: 6px;
                color: #fff;
                font-size: 23px;
                font-weight: 600;
            }
        }

        .poster-info {
            width: 100%;

            .poster-actions {
                display: flex;
                justify-content: flex-start;
                align-items: center;
                width: 100%;
            }

            .poster-title {
                color: #1f2026;
                font-weight: 600;
                margin: 16px 0 8px;
            }

            .poster-paths {
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: flex-start;
                background-color: #f5f7fa;
                border-radius: 4px;
                padding: 0 12px;
                flex: 1;

                .poster-item {
                    display: flex;
                    justify-content: flex-start;
                    align-items: center;
                    flex-wrap: nowrap;
                    font-size: 14px;
                    height: 47px;
                    line-height: 47px;
                    width: 100%;

                    .poster-item-text {
                        flex-grow: 1;
                        color: #666;
                    }
                }
            }
        }
    }
}

.orfanize-content {
    border: 1px solid #e4e4e4;

    .tabs-row {
        box-sizing: border-box;
        height: 100%;
        border-right: 1px solid #e4e4e4;
        padding-left: 10px;
        padding-top: 20px;

        .tabs {
            padding: 10px 15px;
            cursor: pointer;
        }

        .active {
            color: var(--tig-primary);
            background-color: var(--tig-item-active-bg);
            font-weight: 600;
        }
    }

    .table-row {
        height: calc(100vh - 126px);
        overflow-y: auto;
        padding: 10px;
        border-right: 1px solid #e4e4e4;

        &::-webkit-scrollbar {
            display: none;
        }

        .list-table-form {
            margin-bottom: 20px;
            display: flex;
            gap: 10px;
        }
    }

    .select-row {
        height: calc(100vh - 126px);
        overflow-y: auto;
        padding: 10px;

        &::-webkit-scrollbar {
            display: none;
        }

        .select-num-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0 20px 0;
        }

        .select-list {
            .select-item {
                display: flex;
                align-items: center;
                justify-content: space-between;
                color: var(--tig-primary);
                background-color: var(--tig-item-active-bg);
                padding: 12px;
                margin-bottom: 10px;
            }
        }
    }
}
</style>
