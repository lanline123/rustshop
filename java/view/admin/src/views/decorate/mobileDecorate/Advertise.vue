<template>
    <div class="advertise-container">
        <a-spin :spinning="loading">
            <div class="advertise-content">
                <!-- 左侧标签页 -->
                <div class="tabs-panel">
                    <div class="title">{{ $t('推广渠道') }}</div>
                    <div
                        v-for="item in tabs"
                        :key="item.value"
                        class="tab-item"
                        :class="{ active: currentTab === item.value && !isSettingTab }"
                        @click="handleTabChange(item.value)"
                    >
                        {{ item.name }}
                    </div>
                    <div class="title title-setting">{{ $t('推广设置') }}</div>
                    <div class="tab-item" :class="{ active: isSettingTab }" @click="handleSettingTabChange">{{ $t('分享海报') }}</div>
                </div>

                <!-- 右侧内容：推广渠道 -->
                <div v-if="!isSettingTab" class="content-panel">
                    <!-- 海报预览 -->
                    <div class="poster-preview">
                        <img v-if="srcList[0]" class="poster-img" :src="srcList[0]" alt="poster" />
                        <div class="poster-enlarge" @click="handlePreview">
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

                    <!-- 操作信息 -->
                    <div class="poster-info">
                        <div class="info-title" style="margin-top: 0">{{ $t('素材下载') }}</div>
                        <div class="info-actions">
                            <el-button v-if="!isOverseas()" @click="handlePosterDownload">{{ $t('下载海报') }}</el-button>
                            <el-button @click="handleQRCodeDownload">{{ $t('下载二维码') }}</el-button>
                        </div>

                        <div class="info-title">{{ $t('路径复制') }}</div>
                        <div class="path-list">
                            <div class="path-item" style="border-bottom: 1px solid #e4e4e4">
                                <span class="path-text">{{ $t('适用于微信内会话/朋友圈等场景') }}</span>
                                <el-tooltip placement="bottom">
                                    <template #content>
                                        <div style="max-width: 300px; padding: 5px 10px">
                                            <template v-if="currentShortLinkError">
                                                <span>{{ currentShortLinkError }}</span>
                                            </template>
                                            <template v-else>{{ currentShortLink || "—" }}</template>
                                        </div>
                                    </template>
                                    <el-button :disabled="!currentShortLink" @click="handleCopy(currentShortLink)">{{ $t('复制短链接') }}</el-button>
                                </el-tooltip>
                            </div>
                            <div v-if="currentTab === 1" class="path-item">
                                <span class="path-text">{{ $t('适用于微信内会话/朋友圈等场景') }}</span>
                                <el-tooltip placement="bottom">
                                    <template #content>
                                        <div style="max-width: 300px; padding: 5px 10px">{{ longLink }}</div>
                                    </template>
                                    <el-button :disabled="!longLink" @click="handleCopy(longLink)">{{ $t('复制原链接') }}</el-button>
                                </el-tooltip>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 右侧内容：分享海报 -->
                <div v-else class="content-panel">
                    <!-- 海报预览 -->
                    <div class="poster-preview">
                        <img v-if="srcList[0]" class="poster-img" :src="srcList[0]" alt="poster" />
                        <div class="poster-enlarge" @click="handlePreview">
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

                    <!-- 分享信息 -->
                    <div class="poster-info">
                        <div class="info-title" style="margin-top: 0">{{ $t('分享信息') }}</div>
                        <div class="share-info-form">
                            <div class="share-form-item">
                                <label class="share-form-label">{{ $t('分享标题') }}</label>
                                <BusinessData
                                    v-model="shareForm.shareTitle"
                                    :placeholder="$t('若不设置，默认为该页面名称')"
                                    :maxlength="30"
                                    :data-type="17"
                                    :data-id="Number(decorateId) || 0"
                                    type="input"
                                />
                            </div>
                            <div class="share-form-item">
                                <label class="share-form-label">{{ $t('分享描述') }}</label>
                                <div class="share-textarea-wrap">
                                    <el-input
                                        v-model="shareForm.shareDesc"
                                        type="textarea"
                                        :placeholder="$t('这家店有好货，一起来看看吧~')"
                                        :maxlength="30"
                                        :rows="3"
                                    />
                                    <BusinessData
                                        v-model="shareForm.shareDesc"
                                        :data-type="17"
                                        :data-id="Number(decorateId) || 0"
                                        type="textarea"
                                        translation-button-text="多语言"
                                    />
                                </div>
                            </div>
                            <div class="share-form-item share-form-item-image">
                                <label class="share-form-label">{{ $t('分享图片') }}</label>
                                <div class="share-image-wrap">
                                    <FormAddGallery
                                        :photo="displayShareImage"
                                        :page-type="isOverseas() ? 'overseas' : ''"
                                        :data-type="17"
                                        @update:photo="shareForm.shareImage = $event"
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
            </div>
        </a-spin>
    </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, onUnmounted, reactive, ref, render, watch } from "vue";
import { message } from "ant-design-vue";
import { ElImageViewer } from "element-plus";
import { useConfigStore } from "@/store/config";
import { generateShortLink, getShareInfo, saveShareInfo, type DecoratePromoteShortLink, type DecorateShareInfo } from "@/api/decorate/mobileDecorate";
import { getConfigBasic } from "@/api/setting/config";
import { imageFormat } from "@/utils/format";
import { FormAddGallery } from "@/components/gallery";
import { debounce } from "@/utils/util";
import { isOverseas } from "@/utils/version";
import BusinessData from "@/components/multilingual/BusinessData.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineOptions({
    name: "MobileDecorateAdvertise"
});

interface DecorateRow {
    decorateId?: number | string;
    id?: number | string;
    shopId?: number | string;
    decorateTitle?: string;
    updateTime?: string;
    pageType?: number;
    [key: string]: any;
}

interface Props {
    decorateItem?: DecorateRow;
    decorateType?: number;
    toPath?: string;
    title?: string;
    shopId?: number | string;
}

const props = withDefaults(defineProps<Props>(), {
    decorateItem: () => ({}),
    decorateType: 1,
    toPath: "",
    title: "",
    shopId: ""
});

const loading = ref(false);
const srcList = ref<string[]>([]);
const previewSrcList = ref<string[]>([]);
/** 海报背景图（商城 LOGO，来自基础设置） */
const posterBgUrl = ref("");
const localesList = ref<any[]>([]);

// 分享信息表单（编辑态）
const shareLoading = ref(false);
const shareForm = reactive({
    shareTitle: "",
    shareDesc: "",
    shareImage: ""
});
// 已应用的分享信息（用于海报绘制；点击"应用"后才更新）
const appliedShareForm = reactive({ shareTitle: "", shareDesc: "", shareImage: "" });

const tabs = computed(() => {
    return Number(props?.decorateType ?? 1) === 2
        ? [{ name: t('H5/公众号'), value: 1 }]
        : [
              { name: t('H5/公众号'), value: 1 },
              { name: t('小程序'), value: 2 }
          ];
});

const currentTab = ref(1);
const isSettingTab = ref(false);
/** H5 短链（接口返回的完整 URL） */
const h5ShortPath = ref("");
/** 微信小程序短链；接口失败时为空 */
const wechatShortLink = ref("");
/** 微信短链失败原因 */
const wechatShortLinkError = ref("");
/** 小程序二维码（Base64 data URL） */
const miniCode = ref("");

const currentShortLink = computed(() => (currentTab.value === 1 ? h5ShortPath.value : wechatShortLink.value));
const currentShortLinkError = computed(() => (currentTab.value === 2 ? wechatShortLinkError.value : ""));

const decorateId = computed(() => props?.decorateItem?.decorateId ?? props?.decorateItem?.id ?? "");

const defaultShareTitle = computed(() => String(props?.decorateItem?.decorateTitle ?? ""));

const resolvedPath = computed(() => String(props?.toPath || ""));

const decorateType = computed(() => Number(props?.decorateType ?? 1));

const fileBaseName = computed(() => String(props?.title ?? "").trim());

const configStore = useConfigStore();

const defaultShareImage = computed(() => posterBgUrl.value || (configStore.get("adminLightLogo") as string) || "");

const displayShareImage = computed(() => shareForm.shareImage || defaultShareImage.value);

const isShareImageCustom = computed(() => !!shareForm.shareImage);

const handleResetShareImage = () => {
    shareForm.shareImage = "";
};

watch(
    tabs,
    (tabList) => {
        if (!tabList.some((item) => item.value === currentTab.value)) {
            currentTab.value = tabList[0]?.value ?? 1;
        }
    },
    { immediate: true }
);

const getLocalShopId = () => {
    try {
        const shopInfo = JSON.parse(localStorage.getItem("shopInfo") || "{}");
        return shopInfo?.shopId || localStorage.getItem("shopId") || "";
    } catch {
        return localStorage.getItem("shopId") || "";
    }
};

const formatPromoteUrl = (raw: unknown) => {
    const input = String(raw || "").trim();
    if (!input) return "";
    if (/^https?:\/\//i.test(input)) return input;

    let path = input;

    if (!path.startsWith("/")) {
        path = "/" + path;
    }

    if (decorateType.value === 2) {
        const pcDomain = String(configStore.get("pcDomain") || window.location.origin).replace(/\/$/, "");
        return pcDomain + path;
    }

    const h5Domain = String(configStore.get("h5Domain") || "").replace(/\/$/, "");
    const fallbackDomain = window.location.origin.replace(/\/$/, "") + "/mobile";
    const domain = h5Domain || fallbackDomain;

    if (domain.endsWith("/mobile") && path.startsWith("/mobile/")) {
        path = path.substring(7);
    }

    return domain + path;
};

// 按页面类型生成推广链接
const longLink = computed(() => {
    const baseUrl = formatPromoteUrl(resolvedPath.value);
    const shopId = getLocalShopId() || props?.shopId || props?.decorateItem?.shopId;
    const query = new URLSearchParams();
    const currentDecorateId = String(props?.decorateItem?.decorateId ?? props?.decorateItem?.id ?? "");
    const pageType = props?.decorateItem?.pageType;

    if (resolvedPath.value === "/pages/store/index" && shopId) {
        query.set("id", String(shopId));
    } else if (resolvedPath.value === "/pages/shop/index" && shopId) {
        query.set("shopId", String(shopId));
        if (currentDecorateId) {
            query.set("previewId", currentDecorateId);
        }
    } else {
        if (props?.decorateItem?.isHome && resolvedPath.value === "/") {
            return baseUrl;
        }
        const decorateIdKey = props?.decorateItem?.isHome ? "id" : "previewId";
        if (currentDecorateId) {
            query.set(decorateIdKey, currentDecorateId);
        }
    }

    if (pageType !== undefined && pageType !== null) {
        query.set("pageType", String(pageType));
    }

    const queryString = query.toString();
    return queryString ? `${baseUrl}?${queryString}` : baseUrl;
});

/**
 * 生成二维码
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

/**
 * 绘制当前海报
 * - 分享海报 Tab 下：使用编辑态 shareForm 实时预览
 * - 其他 Tab 下：使用已应用的 appliedShareForm
 * 替换 srcList 之前先把新图预解码到位，避免 <img> 切 src 出现空白闪烁
 */
const drawCurrentPoster = async () => {
    const source = isSettingTab.value ? shareForm : appliedShareForm;
    // 分享图片可能是相对存储路径，绘制前统一补齐存储域名；完整 URL 保持不变。
    const posterImgUrl = imageFormat(source.shareImage || defaultShareImage.value);
    const h5Link = h5ShortPath.value || longLink.value;
    const wechatLink = wechatShortLink.value || h5Link;
    let qrcodeContent = "";
    if (currentTab.value === 1) {
        qrcodeContent = h5Link;
    } else {
        // 小程序优先使用后端返回的二维码图片（base64 data URL），否则用短链生成
        if (miniCode.value && miniCode.value.startsWith("data:image") && miniCode.value.length > 500) {
            qrcodeContent = miniCode.value;
        } else {
            qrcodeContent = wechatLink;
        }
    }
    if (!posterImgUrl || !qrcodeContent) {
        srcList.value = [];
        previewSrcList.value = [];
        return;
    }
    const posterUrl = await drawPoster(posterImgUrl, qrcodeContent);
    // 仅替换数组首位元素而非整个数组，避免触发 <img> 重新挂载或 v-if 闪烁
    srcList.value[0] = posterUrl.small;
    previewSrcList.value[0] = posterUrl.main;
};

/**
 * 分享海报 Tab 下，编辑表单内容时实时预览（防抖 600ms）
 * 非分享海报 Tab 不触发，避免无关重绘
 */
const debouncedRedrawPoster = debounce(() => {
    if (isSettingTab.value) {
        drawCurrentPoster();
    }
}, 600);

watch(() => [shareForm.shareTitle, shareForm.shareDesc, shareForm.shareImage], debouncedRedrawPoster);

/**
 * 生成装修推广短链接（H5 + 微信 + 小程序码）
 */
const loadShortLink = async () => {
    h5ShortPath.value = "";
    wechatShortLink.value = "";
    wechatShortLinkError.value = "";
    miniCode.value = "";
    const result: DecoratePromoteShortLink = await generateShortLink(longLink.value);
    h5ShortPath.value = decorateType.value === 2 ? (result?.pcShortLink ?? result?.shortLink ?? "") : (result?.shortLink ?? "");
    wechatShortLink.value = result?.wechatShortLink ?? "";
    wechatShortLinkError.value = result?.wechatShortLinkError ?? "";
    miniCode.value = result?.miniCode ?? "";
    if (result?.wechatShortLinkError && decorateType.value !== 2) {
        message.warning(result.wechatShortLinkError);
    }
};

/**
 * 拉取商城基础设置中的 LOGO 作为海报背景图与分享图片默认值
 */
const loadPosterBg = async () => {
    try {
        const result = await getConfigBasic();
        posterBgUrl.value = imageFormat(String(result?.shopLogo || ""));
    } catch (error) {
        posterBgUrl.value = "";
    }
};

const loadData = async () => {
    loading.value = true;
    try {
        await Promise.all([loadShortLink(), loadPosterBg()]);
        await drawCurrentPoster();
    } catch (error: any) {
        message.error(error?.message || t('加载推广数据失败'));
    } finally {
        loading.value = false;
    }
};

/**
 * 加载分享信息
 */
const loadShareInfo = async () => {
    if (!decorateId.value) {
        shareForm.shareTitle = defaultShareTitle.value;
        appliedShareForm.shareTitle = defaultShareTitle.value;
        return;
    }
    try {
        const data: DecorateShareInfo = await getShareInfo(decorateId.value);
        shareForm.shareTitle = data?.shareTitle || defaultShareTitle.value;
        shareForm.shareDesc = data?.shareDesc || "";
        shareForm.shareImage = data?.shareImage || "";
        appliedShareForm.shareTitle = shareForm.shareTitle;
        appliedShareForm.shareDesc = shareForm.shareDesc;
        appliedShareForm.shareImage = shareForm.shareImage;
    } catch {
        shareForm.shareTitle = defaultShareTitle.value;
        appliedShareForm.shareTitle = defaultShareTitle.value;
    }
};

/**
 * 保存分享信息（点击"应用"后才更新海报）
 */
const handleSaveShareInfo = async () => {
    if (!decorateId.value) return;
    shareLoading.value = true;
    try {
        await saveShareInfo({
            decorateId: decorateId.value,
            shareTitle: shareForm.shareTitle,
            shareDesc: shareForm.shareDesc,
            shareImage: shareForm.shareImage || defaultShareImage.value
        });
        appliedShareForm.shareTitle = shareForm.shareTitle;
        appliedShareForm.shareDesc = shareForm.shareDesc;
        appliedShareForm.shareImage = shareForm.shareImage;
        message.success(t('保存成功'));
        await drawCurrentPoster();
    } catch (error: any) {
        message.error(error?.message || t('保存分享信息失败'));
    } finally {
        shareLoading.value = false;
    }
};

/**
 * 绘制海报核心逻辑（支持外部传入标题/描述/扫码提示/页头标题，便于多语言海报）
 */
const drawPosterCore = async (
    imgUrl: string,
    qrcode: string,
    title: string,
    desc: string,
    scanHintText?: string,
    headerTitle?: string
): Promise<{ main: string; small: string }> => {
    try {
        const img = new Image();
        img.crossOrigin = "anonymous";

        await new Promise((resolve, reject) => {
            img.onload = resolve;
            img.onerror = reject;
            img.src = imgUrl;
        });

        const canvasWidth = 750;
        const horizontalPadding = 50;
        const topMargin = 210;
        const bottomMargin = 130;
        const imageAreaWidth = canvasWidth - 2 * horizontalPadding;

        const innerTopPadding = 0;
        const imageTopPadding = 40;
        const innerBottomPadding = 305;
        const qrCodeSize = 130;
        const contentInnerHorizontalPadding = 30;
        const contentInnerWidth = imageAreaWidth - contentInnerHorizontalPadding * 2;

        const boxSize = contentInnerWidth;
        const fitScale = Math.min(boxSize / img.width, boxSize / img.height);
        const drawWidth = img.width * fitScale;
        const drawHeight = img.height * fitScale;

        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        if (!ctx) {
            throw new Error(t('无法获取 canvas 上下文'));
        }

        const contentHeight = innerTopPadding + imageTopPadding + boxSize + innerBottomPadding;
        canvas.width = canvasWidth;
        canvas.height = topMargin + contentHeight + bottomMargin;

        const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
        gradient.addColorStop(0, "#F2F2F6");
        gradient.addColorStop(1, "#E6E6EE");

        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // 顶部标题：默认装修标题；多语言下载时支持覆盖
        ctx.fillStyle = "#333";
        ctx.font = "36px Arial";
        ctx.textAlign = "center";
        ctx.fillText(headerTitle || String(props?.decorateItem?.decorateTitle ?? fileBaseName.value ?? ""), canvas.width / 2, topMargin / 2 + 20);

        const contentX = horizontalPadding;
        const contentY = topMargin;
        const contentWidth = imageAreaWidth;
        const contentHeightTotal = contentHeight;

        ctx.save();
        ctx.shadowColor = "rgba(0, 0, 0, 0.1)";
        ctx.shadowBlur = 20;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 20;

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

        ctx.fillStyle = "#ffffff";
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

        ctx.fillStyle = "#ffffff";
        ctx.fillRect(contentX, contentY, contentWidth, innerTopPadding);

        const boxX = contentX + contentInnerHorizontalPadding;
        const boxY = contentY + innerTopPadding + imageTopPadding;

        const imgDrawX = boxX + (boxSize - drawWidth) / 2;
        const imgDrawY = boxY + (boxSize - drawHeight) / 2;
        ctx.drawImage(img, imgDrawX, imgDrawY, drawWidth, drawHeight);

        ctx.fillStyle = "#ffffff";
        ctx.fillRect(contentX, boxY + boxSize, contentWidth, innerBottomPadding);

        ctx.restore();

        const infoAreaStartY = boxY + boxSize;

        const qrCodeUrl = qrcode.startsWith("data:") ? qrcode : await generateQRCodeImage(qrcode, "#333", "#ffffff00");

        const qrImg = new Image();
        await new Promise((resolve, reject) => {
            qrImg.onload = resolve;
            qrImg.onerror = reject;
            qrImg.src = qrCodeUrl;
        });

        const groupNameX = contentX + contentInnerHorizontalPadding;
        const groupNameY = infoAreaStartY + innerBottomPadding - qrCodeSize - 10;

        // 分享标题
        ctx.fillStyle = "#333";
        ctx.font = "34px Arial";
        ctx.textAlign = "left";
        ctx.fillText(title, groupNameX, groupNameY);

        // 分享描述（限宽自动截断）
        if (desc) {
            ctx.fillStyle = "#666";
            ctx.font = "26px Arial";
            ctx.textAlign = "left";
            const descMaxWidth = contentInnerWidth - qrCodeSize - 20;
            let descText = desc;
            while (ctx.measureText(descText).width > descMaxWidth && descText.length > 0) {
                descText = descText.slice(0, -1);
            }
            if (descText.length < desc.length) {
                descText = desc.slice(0, descText.length - 1) + "...";
            }
            ctx.fillText(descText, groupNameX, groupNameY + 42, descMaxWidth);
        }

        const qrX = contentX + contentInnerHorizontalPadding + contentInnerWidth - qrCodeSize;
        const qrY = infoAreaStartY + innerBottomPadding - qrCodeSize - 50;
        ctx.drawImage(qrImg, qrX, qrY, qrCodeSize, qrCodeSize);

        ctx.fillStyle = "#666";
        ctx.font = "20px Arial";
        ctx.textAlign = "right";
        ctx.fillText(scanHintText || t('长按识别查看'), qrX + qrCodeSize - 10, qrY + qrCodeSize + 24);

        return new Promise((resolve) => {
            const url = window.URL || window.webkitURL;
            canvas.toBlob((blob) => {
                if (blob) {
                    const mainPosterUrl = url.createObjectURL(blob);
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
 * 绘制海报（使用当前分享信息）
 */
const drawPoster = async (imgUrl: string, qrcode: string): Promise<{ main: string; small: string }> => {
    const posterSource = isSettingTab.value ? shareForm : appliedShareForm;
    const title = String(posterSource.shareTitle || (props?.decorateItem?.decorateTitle ?? ""));
    const desc = posterSource.shareDesc || "";
    return drawPosterCore(imgUrl, qrcode, title, desc);
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
        download(url, t('{value}海报.png', { value: fileBaseName.value }));
    } catch (error) {
        console.error("下载失败:", error);
        message.error(t('下载海报失败'));
    } finally {
        loading.value = false;
    }
};


const handleQRCodeDownload = async () => {
    try {
        loading.value = true;
        const h5Link = h5ShortPath.value || longLink.value;
        if (currentTab.value === 1) {
            const h5QRCodeUrl = await generateQRCodeImage(h5Link);
            download(h5QRCodeUrl, t('{value}H5公众号二维码.png', { value: fileBaseName.value }));
        } else {
            // 小程序优先使用后端返回的 miniCode（base64 data URL）
            if (miniCode.value && miniCode.value.startsWith("data:image") && miniCode.value.length > 500) {
                download(miniCode.value, t('{value}小程序二维码.png', { value: fileBaseName.value }));
                return;
            }
            const wechatLink = wechatShortLink.value || h5Link;
            const miniQRCodeUrl = await generateQRCodeImage(wechatLink);
            download(miniQRCodeUrl, t('{value}小程序二维码.png', { value: fileBaseName.value }));
        }
    } catch (error) {
        console.error("下载失败:", error);
        message.error(t('下载二维码失败'));
    } finally {
        loading.value = false;
    }
};

const handleCopy = (text: string) => {
    if (!text) {
        message.warning(t('暂无可复制内容'));
        return;
    }
    if (navigator.clipboard) {
        navigator.clipboard
            .writeText(text)
            .then(() => {
                message.success(t('复制成功'));
            })
            .catch((err) => {
                console.error("复制失败:", err);
                message.error(t('复制失败'));
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
            message.error(t('复制失败'));
        }
        document.body.removeChild(textarea);
    }
};

/** 当前打开的预览容器，用于关闭后销毁挂载节点 */
let previewContainer: HTMLDivElement | null = null;

const handlePreview = () => {
    const previewUrl = previewSrcList.value[0] || srcList.value[0];
    if (!previewUrl) return;
    // 已有打开的预览先关掉，避免叠加
    if (previewContainer) {
        render(null, previewContainer);
        previewContainer.remove();
        previewContainer = null;
    }
    const container = document.createElement("div");
    document.body.appendChild(container);
    previewContainer = container;
    const vnode = h(ElImageViewer, {
        urlList: [previewUrl],
        initialIndex: 0,
        hideOnClickModal: true,
        onClose: () => {
            render(null, container);
            container.remove();
            if (previewContainer === container) {
                previewContainer = null;
            }
        }
    });
    render(vnode, container);
};

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

onMounted(() => {
    loadData();
    loadShareInfo();
});

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
    if (previewContainer) {
        render(null, previewContainer);
        previewContainer.remove();
        previewContainer = null;
    }
});

// 兼容 DialogForm 的 onFormSubmit 调用（本弹窗不显示底部按钮，仅作占位）
const onFormSubmit = () => {};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.advertise-container {
    min-height: 400px;
}

.advertise-content {
    display: flex;
    border: 1px solid #e4e4e4;
    border-radius: 4px;
}

.tabs-panel {
    width: 120px;
    border-right: 1px solid #e4e4e4;
    padding: 20px 10px;
    flex-shrink: 0;

    .title {
        color: #a2a2a2;
        font-size: 14px;
        margin-bottom: 10px;
        padding-left: 15px;
    }

    .title-setting {
        margin-top: 10px;
    }

    .tab-item {
        padding: 5px 15px;
        cursor: pointer;
        transition: all 0.2s;

        &.active {
            color: var(--el-color-primary);
            background-color: var(--el-color-primary-light-9);
            font-weight: 600;
        }
    }
}

.content-panel {
    flex: 1;
    display: flex;
    padding: 20px;
    gap: 20px;
    position: relative;
}

.poster-preview {
    position: relative;
    flex-shrink: 0;
    width: 240px;
    height: 390px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 6px;

    .poster-img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        border-radius: 6px;
        cursor: zoom-in;
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

        &:hover {
            opacity: 0.9;
        }
    }
}

.poster-info {
    flex: 1;
    min-width: 0;

    .info-title {
        font-weight: 600;
        margin: 16px 0 8px;
    }

    .info-actions {
        display: flex;
        gap: 10px;
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

            .share-textarea-wrap {
                flex: 1;
                position: relative;
                :deep(.multilingual-textarea-box) {
                    right: 10px;
                }
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
                color: var(--el-color-primary);
                cursor: pointer;
                line-height: 32px;

                &:hover {
                    opacity: 0.8;
                }
            }
        }

        .share-form-actions {
            position: absolute;
            right: 20px;
            bottom: 20px;
        }
    }

    .path-list {
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 0 12px;

        .path-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            height: 47px;

            .path-text {
                flex-grow: 1;
                color: #999;
            }
        }
    }
}

:deep(.gallery-pic) {
    width: auto;
}
</style>

<style lang="less">
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
        color: var(--el-color-primary, #1677ff);
        cursor: pointer;
        white-space: nowrap;

        &:hover {
            opacity: 0.8;
        }
    }
}
</style>
