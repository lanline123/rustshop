<template>
    <div class="btn" @click="handleShow">
        <slot> </slot>
    </div>
    <template v-if="show">
        <el-dialog
            class="sign-dialog"
            :append-to-body="true"
            :show-close="false"
            :z-index="1000"
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
                            <div class="tabs-title">{{ $t('推广渠道') }}</div>
                            <div
                                class="tabs"
                                v-for="item in tabs"
                                :key="item.value"
                                :class="{ active: item.value === currentTab && !isSettingTab }"
                                @click="handleTabChange(item.value)"
                            >
                                {{ item.name }}
                            </div>
                            <div class="tabs-title tabs-title-setting">{{ $t('推广设置') }}</div>
                            <div class="tabs" :class="{ active: isSettingTab }" @click="handleSettingTabChange">{{ $t('分享海报') }}</div>
                        </div>
                    </el-col>
                    <el-col :span="20">
                        <div v-if="!isSettingTab" class="content">
                            <div class="poster-img-container">
                                <img v-if="srcList[0]" class="poster-img" :src="srcList[0]" />
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
                                    <el-button v-if="!isOverseas()" bg class="buttonColor mr10" size="large" text type="primary" @click="handlePosterDownload"> {{ $t('下载海报') }} </el-button>
                                    <el-button
                                        bg
                                        class="buttonColor mr10"
                                        size="large"
                                        text
                                        type="primary"
                                        :disabled="currentTab === 2 && !hasWechatQrCode"
                                        @click="handleQRCode"
                                    > {{ $t('下载二维码') }} </el-button>
                                </div>
                                <div class="poster-title">{{ $t('路径复制') }}</div>
                                <div class="poster-paths">
                                    <div class="poster-item" style="border-bottom: 1px solid #e9ecf0">
                                        <span class="poster-item-text">{{ currentTab === 3 ? $t('适用于限时活动等字符有限的场景') : $t('适用于微信内会话/朋友圈等场景') }}</span>
                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                                            <template #content>
                                                <div style="max-width: 300px; padding: 5px 10px">
                                                    <template v-if="currentTab === 2 && wechatShortLinkError">
                                                        <span style="color: #ed5b56">{{ wechatShortLinkError }}</span>
                                                    </template>
                                                    <template v-else>{{ shortLink || "—" }}</template>
                                                </div>
                                            </template>
                                            <a class="btn-link" :class="{ disabled: !shortLink }" @click="shortLink && handleCopy(shortLink)">{{ $t('复制短链接') }}</a>
                                        </el-tooltip>
                                    </div>
                                    <div v-if="currentTab === 1 || currentTab === 3" class="poster-item">
                                        <span class="poster-item-text">{{ currentTab === 3 ? $t('适用于分享长期活动等场景') : $t('适用于微信内会话/朋友圈等场景') }}</span>
                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                                            <template #content>
                                                <div style="max-width: 300px; padding: 5px 10px">
                                                    {{ originalLink || "—" }}
                                                </div>
                                            </template>
                                            <a class="btn-link" :class="{ disabled: !originalLink }" @click="originalLink && handleCopy(originalLink)"
                                                >{{ $t('复制原链接') }}</a
                                            >
                                        </el-tooltip>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="content">
                            <div class="poster-img-container">
                                <img v-if="srcList[0]" class="poster-img" :src="srcList[0]" />
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
                                <div class="poster-title" style="margin-top: 0">{{ $t('分享信息') }}</div>
                                <div class="share-info-form">
                                    <div class="share-form-item">
                                        <label class="share-form-label">{{ $t('商品名称') }}</label>
                                        <div class="share-input-wrap">
                                            <BusinessData
                                                v-model:modelValue="shareForm.productName"
                                                :placeholder="defaultProductName"
                                                :maxlength="60"
                                                :dataType="18"
                                                :dataId="Number(productId) || 0"
                                                type="input"
                                            />
                                        </div>
                                    </div>
                                    <div class="share-form-item">
                                        <label class="share-form-label">{{ $t('商品价格') }}</label>
                                        <div class="share-input-wrap">
                                            <TigInput
                                                v-model="shareForm.productPrice"
                                                type="decimal"
                                                :placeholder="defaultProductPrice"
                                                :min="0"
                                                :decimalPlaces="2"
                                                :fixedDecimal="false"
                                                :allowEmptyDecimal="true"
                                                width="100%"
                                            />
                                        </div>
                                    </div>
                                    <div class="share-form-item share-form-item-image">
                                        <label class="share-form-label">{{ $t('商品图片') }}</label>
                                        <div class="share-image-wrap">
                                            <FormAddGallery
                                                :photo="displayShareImage"
                                                :page-type="isOverseas() ? 'overseas' : ''"
                                                :dataType="18"
                                                @update:photo="(value: string) => (shareForm.picUrl = value)"
                                            />
                                            <a v-if="isShareImageCustom" class="share-reset-btn" @click="handleResetShareImage">{{ $t('重置') }}</a>
                                        </div>
                                    </div>
                                    <div class="share-form-actions">
                                        <el-button type="primary" :loading="shareLoading" @click="handleSaveShareInfo">{{ $t('应用') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </a-spin>
            <el-image-viewer
                v-if="showPreview"
                :url-list="previewSrcList.length ? previewSrcList : srcList"
                :initial-index="0"
                show-progress
                @close="showPreview = false"
            />
        </el-dialog>
    </template>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineOptions({
    inheritAttrs: false
});

import { computed, onUnmounted, PropType, reactive, ref, watch } from "vue";
import requestExport from "@/utils/export";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { getProductPromoteData, getProductShareInfo, saveProductShareInfo, type ProductPromoteData, type ProductShareInfo } from "@/api/product/product";
import { FormAddGallery } from "@/components/gallery";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { debounce } from "@/utils/util";
import { isOverseas } from "@/utils/version";

const config = useConfigStore();

const props = defineProps({
    item: {
        type: Object as PropType<any>,
        default: () => ({})
    }
});

const emit = defineEmits(["callBack"]);
const showPreview = ref(false);
const show = ref(false);
const showDialog = ref(false);
const loading = ref<boolean>(false);
const drawLoading = ref<boolean>(false);
const srcList = ref<string[]>([]);
const previewSrcList = ref<string[]>([]);
const promoteData = ref<ProductPromoteData>({});
const localesList = ref<any[]>([]);

const tabs = ref([
    { name: "PC", value: 3 },
    { name: t('H5/公众号'), value: 1 },
    { name: t('小程序'), value: 2 }
]);

const currentTab = ref(3);
const isSettingTab = ref(false);
const dataLoaded = ref(false);

const productId = computed(() => props.item?.productId ?? props.item?.id ?? "");

const shareLoading = ref(false);
const shareForm = reactive({
    productName: "",
    productPrice: "" as string,
    picUrl: ""
});
const appliedShareForm = reactive({
    productName: "",
    productPrice: "" as string,
    picUrl: ""
});

const defaultProductName = computed(() => promoteData.value.productName || props.item?.productName || "");

const defaultProductPrice = computed(() => {
    const price = promoteData.value.productPrice ?? props.item?.productPrice;
    return price !== undefined && price !== null ? String(price) : "";
});

const defaultShareImage = computed(() => imageFormat(promoteData.value.picUrl || props.item?.picLarge || props.item?.picThumb || ""));

const displayShareImage = computed(() => shareForm.picUrl || defaultShareImage.value);

const isShareImageCustom = computed(() => !!shareForm.picUrl);

const handleResetShareImage = () => {
    shareForm.picUrl = "";
};

const shortLink = computed(() => {
    if (currentTab.value === 1) {
        return promoteData.value.h5ShortLink ?? "";
    }
    if (currentTab.value === 2) {
        return promoteData.value.wechatShortLink ?? "";
    }
    return promoteData.value.pcShortLink ?? "";
});

const originalLink = computed(() => {
    if (currentTab.value === 1) {
        return promoteData.value.h5OriginalLink ?? "";
    }
    if (currentTab.value === 2) {
        return "";
    }
    return promoteData.value.pcOriginalLink ?? "";
});

const wechatShortLinkError = computed(() => promoteData.value.wechatShortLinkError ?? "");

const hasWechatQrCode = computed(() => {
    const code = promoteData.value.miniCode;
    if (typeof code === "string" && code.startsWith("data:image") && code.length > 500) {
        return true;
    }
    return !!promoteData.value.wechatShortLink;
});

/**
 * 生成二维码
 * @param url 二维码内容地址
 * @param colorDark 二维码颜色
 * @param colorLight 二维码背景色
 */
const generateQRCodeImage = async (url: string, colorDark: string = "#000", colorLight: string = "#FFF") => {
    const opts = {
        margin: 2,
        width: 300,
        color: { dark: colorDark, light: colorLight }
    };
    const QRCodeModule = await import("qrcode");
    return await QRCodeModule.default.toDataURL(url, opts);
};

const drawCurrentPoster = async () => {
    drawLoading.value = true;
    try {
        const source = isSettingTab.value ? shareForm : appliedShareForm;
        const productImgUrl = imageFormat(source.picUrl || promoteData.value.picUrl || props.item?.picLarge || "");
        let qrcodeContent = "";
        if (isSettingTab.value) {
            const miniCode = promoteData.value.miniCode;
            if (miniCode && miniCode.startsWith("data:image") && miniCode.length > 500) {
                qrcodeContent = miniCode;
            } else {
                qrcodeContent = promoteData.value.wechatShortLink || promoteData.value.h5ShortLink || promoteData.value.h5OriginalLink || "";
            }
        } else if (currentTab.value === 1) {
            qrcodeContent = promoteData.value.h5ShortLink || promoteData.value.h5OriginalLink || "";
        } else if (currentTab.value === 2) {
            const miniCode = promoteData.value.miniCode;
            if (miniCode && miniCode.startsWith("data:image") && miniCode.length > 500) {
                qrcodeContent = miniCode;
            } else {
                qrcodeContent = promoteData.value.wechatShortLink || "";
            }
        } else {
            qrcodeContent = promoteData.value.pcShortLink || promoteData.value.pcOriginalLink || "";
        }
        const trimmedPrice = source.productPrice !== null && source.productPrice !== undefined ? String(source.productPrice).trim() : "";
        const overrides = {
            productName: source.productName || defaultProductName.value,
            productPrice: trimmedPrice !== "" ? Number(trimmedPrice) : Number(defaultProductPrice.value) || undefined
        };
        const posterUrl = await drwaPoster(productImgUrl, qrcodeContent, overrides);
        const prevSmall = srcList.value[0];
        const prevMain = previewSrcList.value[0];
        srcList.value[0] = posterUrl.small;
        previewSrcList.value[0] = posterUrl.main;
        requestAnimationFrame(() => {
            if (prevSmall && prevSmall.startsWith("blob:") && prevSmall !== posterUrl.small) {
                URL.revokeObjectURL(prevSmall);
            }
            if (prevMain && prevMain.startsWith("blob:") && prevMain !== posterUrl.main && prevMain !== prevSmall) {
                URL.revokeObjectURL(prevMain);
            }
        });
    } finally {
        drawLoading.value = false;
    }
};

const loadData = async () => {
    try {
        if (!productId.value) {
            message.error(t('商品ID不存在'));
            return;
        }
        const promoteParams: { productId: number | string; shopId?: number } = { productId: productId.value };
        // 多门店环境传 shopId：优先当前登录门店，其次商品所属门店，自营为 0；由后端把 shopId 补进返回的推广链接
        promoteData.value = await getProductPromoteData(promoteParams);
        if (promoteData.value.wechatShortLinkError) {
            message.warning(promoteData.value.wechatShortLinkError);
        }
    } catch (error: any) {
        console.error(error?.message);
    }
};

const loadShareInfo = async () => {
    if (!productId.value) return;
    try {
        const data = await getProductShareInfo(productId.value);
        shareForm.productName = data?.productName ?? defaultProductName.value;
        const price = data?.productPrice;
        shareForm.productPrice = price !== null && price !== undefined && price !== "" ? String(price) : defaultProductPrice.value;
        const incomingPic = data?.picUrl ?? "";
        const defaultPic = promoteData.value.picUrl ?? "";
        shareForm.picUrl = incomingPic && incomingPic !== defaultPic ? incomingPic : "";
    } catch {
        shareForm.productName = defaultProductName.value;
        shareForm.productPrice = defaultProductPrice.value;
        shareForm.picUrl = "";
    }
    appliedShareForm.productName = shareForm.productName;
    appliedShareForm.productPrice = shareForm.productPrice;
    appliedShareForm.picUrl = shareForm.picUrl;
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

const handleShow = async () => {
    show.value = true;
    showDialog.value = true;
    if (dataLoaded.value) return;
    dataLoaded.value = true;
    loading.value = true;
    try {
        await loadData();
        await loadShareInfo();
        await drawCurrentPoster();
    } catch (error) {
        dataLoaded.value = false;
        throw error;
    } finally {
        loading.value = false;
    }
};

const handleClose = () => {
    showDialog.value = false;
};

const getFileBlob = async (url: string): Promise<Blob> => {
    const response = await fetch(url);
    return await response.blob();
};

const drwaPoster = async (
    imgUrl: string,
    qrcode: string,
    overrides?: { shopName?: string; productName?: string; productPrice?: number | string; scanHintText?: string }
): Promise<{ main: string; small: string }> => {
    try {
        const img = new Image();
        img.crossOrigin = "anonymous";

        let imageLoadSuccess = false;
        await new Promise((resolve) => {
            img.onload = () => {
                imageLoadSuccess = true;
                resolve(true);
            };
            img.onerror = () => {
                imageLoadSuccess = false;
                resolve(false);
            };
            img.src = imgUrl;
        });

        const canvasWidth = 750;
        const horizontalPadding = 50;
        const topMargin = 210;
        const bottomMargin = 130;
        const imageAreaWidth = canvasWidth - 2 * horizontalPadding;
        const imageAreaHeight = imageAreaWidth;

        const innerTopPadding = 65;
        const innerBottomPadding = 305;
        const qrCodeSize = 130;

        const drawWidth = imageAreaWidth;
        const drawHeight = imageAreaWidth;
        const drawX = 0;
        const drawY = 0;

        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        if (!ctx) {
            throw new Error(t('无法获取canvas上下文'));
        }

        const contentHeight = innerTopPadding + imageAreaHeight + innerBottomPadding;
        canvas.width = canvasWidth;
        canvas.height = topMargin + contentHeight + bottomMargin;

        const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
        gradient.addColorStop(0, "#F2F2F6");
        gradient.addColorStop(1, "#E6E6EE");

        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.fillStyle = "#333";
        ctx.font = "30px Arial";
        ctx.textAlign = "center";
        ctx.fillText(overrides?.shopName || promoteData.value.shopName || props.item?.shop?.shopTitle || t('商城自营'), canvas.width / 2, topMargin / 2 + 20);

        const contentX = horizontalPadding;
        const contentY = topMargin;
        const contentWidth = imageAreaWidth;
        const contentHeightTotal = contentHeight;

        const shadowBlur = 20;
        const shadowOffsetX = 0;
        const shadowOffsetY = 20;
        ctx.save();
        ctx.shadowColor = "rgba(0, 0, 0, 0.1)";
        ctx.shadowBlur = shadowBlur;
        ctx.shadowOffsetX = shadowOffsetX;
        ctx.shadowOffsetY = shadowOffsetY;

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

        ctx.fillStyle = "#FFFFFF";
        ctx.fill();
        ctx.restore();

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

        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(contentX, contentY, contentWidth, innerTopPadding);

        if (imageLoadSuccess) {
            ctx.drawImage(img, contentX + drawX, contentY + innerTopPadding + drawY, drawWidth, drawHeight);
        } else {
            const placeholderX = contentX + drawX;
            const placeholderY = contentY + innerTopPadding + drawY;

            ctx.fillStyle = "#F5F5F5";
            ctx.fillRect(placeholderX, placeholderY, drawWidth, drawHeight);

            ctx.fillStyle = "#999999";
            ctx.font = "24px Arial";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            const placeholderText = t('图片加载失败');
            ctx.fillText(placeholderText, placeholderX + drawWidth / 2, placeholderY + drawHeight / 2);
        }

        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(contentX, contentY + innerTopPadding + imageAreaHeight, contentWidth, innerBottomPadding);

        ctx.restore();

        const infoAreaStartY = contentY + innerTopPadding + imageAreaHeight;

        let qrCodeUrl = "";
        if (qrcode.startsWith("data:image") && qrcode.length > 500) {
            qrCodeUrl = qrcode;
        } else if (qrcode) {
            qrCodeUrl = await generateQRCodeImage(qrcode, "#333", "#ffffff00");
        }

        const qrImg = new Image();
        let qrLoadSuccess = false;

        if (qrCodeUrl) {
            await new Promise((resolve) => {
                qrImg.onload = () => {
                    qrLoadSuccess = true;
                    resolve(true);
                };
                qrImg.onerror = () => {
                    qrLoadSuccess = false;
                    resolve(false);
                };
                qrImg.src = qrCodeUrl;
            });
        }

        const productNameX = contentX + 30;
        const lineHeight = 36;
        const fontSize = 26;
        const priceFontSize = 28;
        const priceNameSpacing = 20;

        ctx.fillStyle = "#333";
        ctx.font = `${fontSize}px Arial`;
        ctx.textAlign = "left";

        const productName = overrides?.productName || promoteData.value.productName || props.item?.productName || t('商品');
        const availableWidth = contentWidth - 30 - qrCodeSize - 20 - 40;
        const ellipsis = "...";
        const ellipsisWidth = ctx.measureText(ellipsis).width;

        const lines: string[] = [];
        let textIndex = 0;

        let firstLine = "";
        while (textIndex < productName.length) {
            const char = productName[textIndex];
            const testLine = firstLine + char;
            if (ctx.measureText(testLine).width > availableWidth) {
                break;
            }
            firstLine = testLine;
            textIndex++;
        }
        if (firstLine.length > 0) {
            lines.push(firstLine);
        }

        if (textIndex < productName.length && lines.length < 2) {
            let secondLine = "";
            const remainingText = productName.substring(textIndex);
            let needsEllipsis = false;

            if (ctx.measureText(remainingText).width <= availableWidth - ellipsisWidth) {
                secondLine = remainingText;
            } else {
                needsEllipsis = true;
                for (let i = 0; i < remainingText.length; i++) {
                    const testLine = secondLine + remainingText[i];
                    if (ctx.measureText(testLine + ellipsis).width > availableWidth) {
                        break;
                    }
                    secondLine = testLine;
                }
            }

            if (secondLine.length > 0) {
                lines.push(needsEllipsis ? secondLine + ellipsis : secondLine);
            } else if (needsEllipsis && lines.length > 0) {
                let truncatedFirstLine = lines[0];
                while (ctx.measureText(truncatedFirstLine + ellipsis).width > availableWidth && truncatedFirstLine.length > 0) {
                    truncatedFirstLine = truncatedFirstLine.slice(0, -1);
                }
                lines[0] = truncatedFirstLine + ellipsis;
            }
        }

        if (lines.length === 1) {
            const lineWidth = ctx.measureText(lines[0]).width;
            if (lineWidth > availableWidth - 2) {
                let truncatedLine = lines[0];
                while (ctx.measureText(truncatedLine + ellipsis).width > availableWidth && truncatedLine.length > 0) {
                    truncatedLine = truncatedLine.slice(0, -1);
                }
                lines[0] = truncatedLine + ellipsis;
            }
        }

        const productPrice = overrides?.productPrice ?? promoteData.value.productPrice ?? props.item?.productPrice;
        const hasPrice = productPrice !== undefined && productPrice !== null && productPrice !== "";
        const priceHeight = hasPrice ? priceFontSize : 0;
        const spacing = hasPrice ? priceNameSpacing : 0;

        const startY = infoAreaStartY + innerBottomPadding - qrCodeSize - 75;

        if (hasPrice) {
            ctx.fillStyle = "#ff4444";
            ctx.textAlign = "left";

            const priceStr = String(productPrice);
            const integerFontSize = priceFontSize + 8;
            const decimalFontSize = priceFontSize;

            let integerPart = "";
            let decimalPart = "";
            let hasDecimal = false;

            if (priceStr.includes(".")) {
                const parts = priceStr.split(".");
                integerPart = parts[0];
                decimalPart = "." + parts[1];
                hasDecimal = true;
            } else {
                integerPart = priceStr;
            }

            const basePriceY = startY + decimalFontSize;
            const integerPriceY = basePriceY;

            let currentX = productNameX;

            ctx.font = `bold ${priceFontSize}px Arial`;
            ctx.fillText(String(config.get("dollarSign") ?? ""), currentX, basePriceY);
            currentX += ctx.measureText(String(config.get("dollarSign") ?? "")).width;

            ctx.font = `bold ${integerFontSize}px Arial`;
            ctx.fillText(integerPart, currentX + 2, integerPriceY);
            currentX += ctx.measureText(integerPart).width;

            if (hasDecimal) {
                ctx.font = `bold ${decimalFontSize}px Arial`;
                ctx.fillText(decimalPart, currentX + 2, basePriceY);
            }
        }

        const productNameY = startY + priceHeight + spacing + fontSize;
        lines.forEach((line, index) => {
            ctx.fillStyle = "#333333";
            ctx.font = `${fontSize}px Arial`;
            ctx.textAlign = "left";
            ctx.fillText(line, productNameX, productNameY + index * lineHeight);
        });

        if (qrLoadSuccess) {
            const qrX = contentX + contentWidth - qrCodeSize - 30;
            const qrY = infoAreaStartY + innerBottomPadding - qrCodeSize - 80;
            ctx.drawImage(qrImg, qrX, qrY, qrCodeSize, qrCodeSize);

            ctx.fillStyle = "#666";
            ctx.font = "18px Arial";
            ctx.textAlign = "center";
            const qrCenterX = qrX + qrCodeSize / 2;
            const hintText = overrides?.scanHintText || t('长按识别查看');
            const hintMaxWidth = qrCodeSize + 20;
            if (ctx.measureText(hintText).width <= hintMaxWidth) {
                ctx.fillText(hintText, qrCenterX, qrY + qrCodeSize + 20);
            } else {
                const words = hintText.split(/\s+/);
                const hintLines: string[] = [];
                let currentLine = "";
                for (const word of words) {
                    const testLine = currentLine ? currentLine + " " + word : word;
                    if (ctx.measureText(testLine).width > hintMaxWidth && currentLine) {
                        hintLines.push(currentLine);
                        currentLine = word;
                    } else {
                        currentLine = testLine;
                    }
                }
                if (currentLine) hintLines.push(currentLine);
                hintLines.forEach((line, i) => {
                    ctx.fillText(line, qrCenterX, qrY + qrCodeSize + 20 + i * 22);
                });
            }
        }

        return new Promise((resolve) => {
            const urlFactory: any = window.URL || window.webkitURL;
            canvas.toBlob(async (blob) => {
                if (blob) {
                    const mainPosterUrl = urlFactory.createObjectURL(blob);
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

const download = (blobUrl: string, filename: string) => {
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
};

const handlePosterDownload = async () => {
    try {
        loading.value = true;
        const url = previewSrcList.value[0];
        if (!url) {
            message.warning(t('暂无可下载海报'));
            return;
        }
        const productName = (appliedShareForm.productName || promoteData.value.productName || props.item?.productName || t('商品')).substring(0, 20);
        download(url, t('{productName}海报.png', { productName }));
    } catch (error) {
        console.error("下载失败:", error);
    } finally {
        loading.value = false;
    }
};


const handleQRCode = async () => {
    try {
        loading.value = true;
        const productName = (appliedShareForm.productName || promoteData.value.productName || props.item?.productName || t('商品')).substring(0, 20);
        if (currentTab.value === 1) {
            if (!shortLink.value) {
                message.warning(t('暂无可下载二维码'));
                return;
            }
            const h5QRCodeUrl = await generateQRCodeImage(shortLink.value);
            const h5QRCodeBlob = await getFileBlob(h5QRCodeUrl);
            requestExport(h5QRCodeBlob, t('{productName}H5/公众号二维码', { productName }), ".png");
        } else if (currentTab.value === 2) {
            if (promoteData.value.miniCode && promoteData.value.miniCode.startsWith("data:")) {
                download(promoteData.value.miniCode, t('{productName}小程序二维码.png', { productName }));
            } else if (shortLink.value) {
                const miniQRCodeUrl = await generateQRCodeImage(shortLink.value);
                const miniQRCodeBlob = await getFileBlob(miniQRCodeUrl);
                requestExport(miniQRCodeBlob, t('{productName}小程序二维码', { productName }), ".png");
            } else {
                message.warning(t('暂无可下载二维码'));
            }
        } else {
            if (!shortLink.value) {
                message.warning(t('暂无可下载二维码'));
                return;
            }
            const pcQRCodeUrl = await generateQRCodeImage(shortLink.value);
            const pcQRCodeBlob = await getFileBlob(pcQRCodeUrl);
            requestExport(pcQRCodeBlob, t('{productName}PC二维码', { productName }), ".png");
        }
    } catch (error) {
        console.error("下载失败:", error);
    } finally {
        loading.value = false;
    }
};

const handleCopy = (text: string) => {
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
    if (currentTab.value !== value || isSettingTab.value) {
        currentTab.value = value;
        isSettingTab.value = false;
        drawCurrentPoster();
    }
};

const handleSettingTabChange = () => {
    if (!isSettingTab.value) {
        isSettingTab.value = true;
        drawCurrentPoster();
    }
};

// 分享信息表单变更时实时预览（防抖 600ms）
const debouncedRedrawPoster = debounce(() => {
    if (isSettingTab.value) {
        drawCurrentPoster();
    }
}, 600);

watch(() => [shareForm.productName, shareForm.productPrice, shareForm.picUrl], debouncedRedrawPoster);

const handleSaveShareInfo = async () => {
    if (!productId.value) return;
    shareLoading.value = true;
    try {
        const trimmedSavePrice = shareForm.productPrice !== null && shareForm.productPrice !== undefined ? String(shareForm.productPrice).trim() : "";
        await saveProductShareInfo({
            productId: productId.value,
            productName: shareForm.productName || defaultProductName.value,
            productPrice: trimmedSavePrice !== "" ? Number(trimmedSavePrice) : Number(defaultProductPrice.value) || undefined,
            picUrl: shareForm.picUrl || promoteData.value.picUrl || ""
        });
        appliedShareForm.productName = shareForm.productName;
        appliedShareForm.productPrice = shareForm.productPrice;
        appliedShareForm.picUrl = shareForm.picUrl;
        message.success(t('保存成功'));
        await drawCurrentPoster();
        emit("callBack");
    } catch (error: any) {
        message.error(error?.message || t('保存分享信息失败'));
    } finally {
        shareLoading.value = false;
    }
};

</script>

<style lang="less" scoped>
:deep(.gallery-pic) {
    width: auto !important;
}
</style>

<style lang="less">
.ant-message {
    z-index: 3000 !important;
}

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
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        height: 500px;
        padding: 20px 16px 16px;
        position: relative;

        .poster-img-container {
            position: relative;
            flex-shrink: 0;
            margin-right: 20px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            width: 240px;
            aspect-ratio: 750 / 1360;
            background-color: #f5f7fa;
            border-radius: 6px;
            overflow: hidden;

            .poster-img {
                border-radius: 6px;
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
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
            min-width: 0;

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

                    .btn-link.disabled {
                        color: #b3b3b3;
                        cursor: not-allowed;
                    }
                }
            }

            .share-info-form {
                .share-form-item {
                    display: flex;
                    align-items: flex-start;
                    margin-bottom: 12px;

                    .share-form-label {
                        flex-shrink: 0;
                        width: 70px;
                        line-height: 32px;
                        font-size: 14px;
                        color: #666;
                    }

                    .share-input-wrap {
                        flex: 1;
                        min-width: 0;
                    }

                    &.share-form-item-image {
                        .share-image-wrap {
                            flex: 1;
                            display: flex;
                            align-items: flex-end;
                        }
                    }

                    .share-reset-btn {
                        flex-shrink: 0;
                        margin-left: 10px;
                        font-size: 14px;
                        color: var(--tig-primary, var(--el-color-primary));
                        cursor: pointer;
                        line-height: 32px;

                        &:hover {
                            opacity: 0.8;
                        }
                    }
                }

                .share-form-actions {
                    position: absolute;
                    right: 24px;
                    bottom: 24px;
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

        .tabs-title {
            color: #a2a2a2;
            font-size: 14px;
            margin-bottom: 10px;
            padding-left: 15px;
        }

        .tabs-title-setting {
            margin-top: 10px;
        }

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

.poster-lang-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
    border: 1px solid #e4e4e4;
    border-radius: 4px;

    th,
    td {
        padding: 10px 16px;
        text-align: left;
        border-bottom: 1px solid #e4e4e4;
    }

    thead th {
        font-weight: 600;
        color: #333;
        background-color: #fafafa;
    }

    tbody tr:last-child td {
        border-bottom: none;
    }

    tbody tr:hover {
        background-color: #f5f7fa;
    }

    .poster-lang-action {
        color: var(--tig-primary, var(--el-color-primary));
        cursor: pointer;
        white-space: nowrap;

        &:hover {
            opacity: 0.8;
        }
    }
}
</style>
