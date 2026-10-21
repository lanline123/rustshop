<template>
    <div class="module-ad-con module-video-number" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content" :style="frameFormat.backgroundColor + frameFormat.boxRadius">
            <div class="video-number-container" :style="frameFormat.innerPadding + frameFormat.boxRadius">
                <div class="video-card video-card--video" v-if="module.contentType === 'video'" :style="videoCardStyle">
                    <img class="video-card__cover" :style="videoCoverStyle" :src="currentVideoNumber?.cover || defaultCover" alt="视频号封面" />
                    <div class="video-card__mask">
                        <div class="video-card__author">
                            <img src="@/assets/live/videoNumber.svg" alt="视频号头像" width="16" />
                            <span>{{ currentVideoNumber?.nickname || $t('视频号名称') }}</span>
                        </div>
                        <div class="video-card__likes">♥ {{ currentVideoNumber?.likes || 158 }}</div>
                    </div>
                </div>

                <div class="video-card video-card--live" v-else :style="liveCardStyle">
                    <div class="coverBox" :style="[{ borderRadius: videoBorderRadius + 'px'}]">
                        <img class="video-card__cover" :style="videoCoverStyle" :src="currentVideoNumber?.cover || defaultCover" alt="视频号封面" />
                    </div>
                    <div class="video-card__live-content">
                        <div class="video-card__live-title" :style="{ color: liveTitleColor }">
                            {{ module.liveTitle || currentVideoNumber?.liveTitle }}
                        </div>
                        <div class="video-card__live-main" v-if="displayProductImages?.length">
                            <img
                                v-for="(item, index) in displayProductImages"
                                :key="`${item}-${index}`"
                                class="video-card__product"
                                :src="item"
                                alt="商品图片"
                            />
                        </div>
                        <div class="video-card__live-footer">
                            <div class="video-card__author video-card__author--light">
                                <img src="@/assets/live/videoNumber.svg" alt="视频号头像" width="12" />
                                <span>{{ currentVideoNumber?.nickname || $t('视频号名称') }}</span>
                                <div class="video-card__cta"><i class="iconfont-tig icon-rili"></i> {{ $t('立即预约') }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import type { ModuleType } from "@/types/decorate/decorate.d";
import { getWechatVideoAccountList } from "@/api/content/wechatVideoAccount";
import type { WechatVideoAccountState } from "@/types/content/wechatVideoAccount.d";
import { pickPic } from "@/utils/format";
import { defaultFrame, formatFrame, mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";
import defaultCover from "@/assets/decorate/mobilePromotion/videoNumberDefault.png";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface ProductImageItem {
    picThumb?: string;
    picUrl?: string;
}

interface VideoNumberOption {
    videoNumberId: string;
    nickname: string;
    cover: string;
    productImage: string;
    likes: number;
    liveTitle: string;
}

interface VideoNumberDecorateModule extends ModuleType {
    contentType?: "video" | "live";
    videoNumberId?: string;
    videoId?: string;
    liveTitle?: string;
    titleColorType?: "default" | "theme" | "custom";
    titleColor?: string;
    productImages?: ProductImageItem[];
}

const defaultTitleColor = "#2A2A2A";
const themeTitleColor = "#155BD4";
const defaultNickname = t('视频号名称');
const defaultLikes = 158;
const defaultLiveTitle = "";
const module = defineModel<VideoNumberDecorateModule>("module", { default: () => ({}) });
const videoNumberOptions = ref<VideoNumberOption[]>([]);

mergeDefaultModule(module.value, {
    titleText: t('视频号'),
    contentType: "video",
    videoNumberId: "",
    videoId: "",
    liveTitle: defaultLiveTitle,
    titleColorType: "default",
    titleColor: defaultTitleColor,
    productImages: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        innerPadding: 12,
        boxRadius: 8,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
});

const frameFormat = computed(() => formatFrame(module.value.frame || {}));

const currentVideoNumber = computed<VideoNumberOption | undefined>(() => {
    return videoNumberOptions.value.find((item) => item.videoNumberId === module.value.videoNumberId) || videoNumberOptions.value[0];
});

const loadVideoNumberOptions = async () => {
    try {
        const result = await getWechatVideoAccountList({
            page: 1,
            size: 100,
            keyword: "",
            finderId: "",
            accountStatus: undefined,
            relationTimeStart: "",
            relationTimeEnd: "",
            sortField: "",
            sortOrder: ""
        });
        videoNumberOptions.value = result.records.map((item: WechatVideoAccountState, index: number) => ({
            videoNumberId: item.finderId,
            nickname: item.nickName || defaultNickname,
            cover: item.cover,
            productImage: item.productImage || "",
            likes: defaultLikes,
            liveTitle: defaultLiveTitle
        }));

        if (!module.value.videoNumberId && videoNumberOptions.value.length > 0) {
            module.value.videoNumberId = videoNumberOptions.value[0].videoNumberId;
        }
        if (!module.value.liveTitle) {
            module.value.liveTitle = currentVideoNumber.value?.liveTitle || defaultLiveTitle;
        }
    } catch (error: any) {
        message.error(error.message || t('获取视频号列表失败'));
    }
};

const liveTitleColor = computed(() => {
    if (module.value.titleColorType === "theme") {
        return themeTitleColor;
    }
    if (module.value.titleColorType === "custom") {
        return module.value.titleColor || defaultTitleColor;
    }
    return defaultTitleColor;
});

const displayProductImages = computed(() => {
    const productImages = (module.value.productImages || [])
        .map((item) => pickPic(item))
        .filter((item): item is string => Boolean(item))
        .slice(0, 3);
    return productImages.length ? productImages : currentVideoNumber.value?.productImage;
});

const videoBorderRadius = computed(() => Number(module.value.frame?.boxRadius || 0));

const videoCardStyle = computed(() => ({
    borderRadius: `${videoBorderRadius.value}px`
}));

const liveCardStyle = computed(() => ({
    borderRadius: `${videoBorderRadius.value}px`,
    backgroundColor: module.value.frame?.backgroundColor || "#ffffff"
}));

const videoCoverStyle = computed(() => ({
    borderRadius: `${videoBorderRadius.value}px`
}));

onMounted(() => {
    loadVideoNumberOptions();
});
</script>

<style lang="less" scoped>
.module-video-number {
    .video-card--video {
        position: relative;
        aspect-ratio: 1 / 1;
        background: #f3f4f6;
        overflow: hidden;
    }


    .video-card__cover {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .video-card__mask {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 10px 10px;
        background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.72) 100%);
        color: #fff;
        font-size: 12px;
    }

    .video-card__author {
        display: flex;
        align-items: center;
        gap: 4px;
        min-width: 0;
    }

    .video-card__author--light {
        color: #a7a7a7;
        display: flex;
    }

    .video-card__author-icon {
        width: 20px;
        height: 20px;
    }

    .video-card__likes {
        flex-shrink: 0;
    }

    .video-card--live {
        display: flex;
        align-items: stretch;
        gap: 10px;
        overflow: visible;
        .coverBox{
            background: #f3f4f6;
            overflow: hidden;
            width: 92px;
            height: 92px;
        }
    }

    .video-card__live-cover {
        width: 92px;
        height: 92px;
        object-fit: cover;
        flex-shrink: 0;
    }

    .video-card__live-content {
        min-width: 0;
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .video-card__live-title {
        font-size: 14px;
        line-height: 20px;
        word-break: break-all;
    }

    .video-card__live-main {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-top: 8px;
        flex-wrap: wrap;
    }

    .video-card__product {
        width: 44px;
        height: 44px;
        border-radius: 4px;
        object-fit: cover;
        background: #f3f4f6;
    }

    .video-card__live-side {
        min-width: 0;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
    }

    .video-card__badge {
        padding: 1px 6px;
        border-radius: 999px;
        background: #25c26e;
        color: #fff;
        font-size: 10px;
        line-height: 16px;
    }

    .video-card__cta {
        padding: 4px 10px;
        border-radius: 3px;
        background: var(--tig-primary);
        color: #fff;
        font-size: 11px;
        line-height: 16px;
        white-space: nowrap;
        margin-left: auto;
        display: flex;
        align-items: center;
        gap: 2px;
    }

    .video-card__live-footer {
        margin-top: auto;
        padding-top: 8px;
    }
}
</style>
