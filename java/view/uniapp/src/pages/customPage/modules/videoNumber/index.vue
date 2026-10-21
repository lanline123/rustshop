<template>
    <view class="module-ad-con module-video-number" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <view class="module-ad-content" :style="frameFormat.backgroundColor + frameFormat.boxRadius">
            <view class="video-number-container" :style="frameFormat.innerPadding">
                <template v-if="isVideoType">
                    <view class="video-card video-card--video" :style="cardRadiusStyle">
                        <channel-video
                            v-if="hasVideoInfo"
                            class="channel-video"
                            :style="cardRadiusStyle"
                            :finder-user-name="finderUserName"
                            :feed-id="videoFeedId"
                            object-fit="cover"
                            :autoplay="false"
                            :loop="false"
                            :muted="false"
                            @error="handleBinderror"
                        />
                        <view v-else class="video-placeholder" :style="cardRadiusStyle">
                            <view class="video-placeholder__tag">{{ $t("视频号") }}</view>
                            <view class="video-placeholder__title">请在后台配置视频号视频</view>
                            <view class="video-placeholder__desc">需填写视频号ID与视频ID</view>
                        </view>
                    </view>
                </template>

                <template v-else>
                    <view class="video-card video-card--live" :style="cardRadiusStyle" @click="handleOpenLive">
                        <view class="live-cover" :style="cardRadiusStyle">
                            <channel-live
                                :style="cardRadiusStyle"
                                :finder-user-name="finderUserName"
                                :feed-id="videoFeedId"
                                object-fit="cover"
                                :autoplay="false"
                                :loop="false"
                                :muted="false"
                            />
                        </view>

                        <view class="live-content">
                            <view class="live-title line2" :style="{ color: liveTitleColor }">
                                {{ liveTitle }}
                            </view>

                            <view class="product-list">
                                <template v-if="productImageList.length > 0">
                                    <image
                                        v-for="(item, productIndex) in productImageList"
                                        :key="`${item}-${productIndex}`"
                                        class="product-item"
                                        :src="item"
                                        mode="aspectFill"
                                    />
                                </template>
                                <template v-else>
                                    <view v-for="item in 3" :key="item" class="product-item product-item--placeholder" />
                                </template>
                            </view>

                            <view class="live-footer">
                                <view class="live-author line1">{{ liveDescription || "" }}</view>
                                <view class="live-button">{{ $t("去观看") }}</view>
                            </view>
                        </view>
                    </view>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif

import { computed, ref, watch } from "vue";
import { formatFrame } from "@/components/modules";
import { imageFormat, pickPic } from "@/utils/format";
import { getConversation } from "@/api/videoNumber/videoNumber";

const { t } = useI18n();
declare const wx: any;

interface ProductImageItem {
    picThumb?: string;
    picUrl?: string;
}

interface VideoNumberModule {
    contentType?: "video" | "live";
    videoNumberId?: string;
    videoId?: string;
    liveTitle?: string;
    titleColorType?: "default" | "theme" | "custom";
    titleColor?: string;
    productImages?: ProductImageItem[];
    frame?: {
        backgroundColor?: string;
        innerPadding?: number;
        boxRadius?: number;
        boxPadding?: number;
        boxPaddingTop?: number;
        boxPaddingBottom?: number;
    };
}

const props = defineProps({
    module: {
        type: Object as () => VideoNumberModule,
        default: () => ({})
    }
});

const frameFormat = computed(() => {
    return formatFrame(
        props.module.frame ?? {
            backgroundColor: "#ffffff",
            innerPadding: 12,
            boxRadius: 8,
            boxPadding: 10,
            boxPaddingTop: 5,
            boxPaddingBottom: 5
        }
    );
});
const coverName = ref("");
const liveDescription = ref("");
const finderUserName = computed(() => props.module.videoNumberId?.trim() || "");
const videoFeedId = computed(() => props.module.videoId?.trim() || "");
const isVideoType = computed(() => props.module.contentType !== "live");
const hasVideoInfo = computed(() => Boolean(finderUserName.value && videoFeedId.value));
const liveTitle = computed(() => props.module.liveTitle?.trim() || t("这场直播特别有料"));

const liveTitleColor = computed(() => {
    if (props.module.titleColorType === "theme") {
        return "var(--general)";
    }
    if (props.module.titleColorType === "custom" && props.module.titleColor) {
        return props.module.titleColor;
    }
    return "#2A2A2A";
});

const productImageList = computed(() => {
    return (props.module.productImages || [])
        .map((item) => pickPic(item))
        .filter((item) => Boolean(item))
        .slice(0, 3)
        .map((item) => imageFormat(item));
});

const handleBinderror = (e: any) => {
    console.log(e, "视频号报错");
};

const cardRadiusStyle = computed(() => {
    return `border-radius:${Number(props.module.frame?.boxRadius || 0)}px;`;
});

const handleOpenLive = () => {
    if (!finderUserName.value) {
        uni.showToast({
            title: "请先配置视频号ID",
            icon: "none"
        });
        return;
    }
    wx.openChannelsLive({
        finderUserName: finderUserName.value,
        fail: (error: any) => {
            uni.showToast({
                title: error?.errMsg || "打开视频号直播失败",
                icon: "none"
            });
        }
    });
};

// 通过视频号id获取直播记录
const fetchLiveRecord = async (id: string) => {
    if (!id) return;
    try {
        const res = await getConversation(id);
        coverName.value = res?.nickname || "";
        liveDescription.value = res?.description || "";
    } catch (e) {
        console.error("获取直播记录失败", e);
    }
};

watch(
    () => props.module.videoNumberId,
    (val) => {
        if (val) fetchLiveRecord(val);
    },
    { immediate: true }
);
</script>

<style lang="scss" scoped>
.module-video-number {
    .module-ad-content {
        overflow: hidden;
    }

    .video-number-container {
        position: relative;
    }

    .video-card {
        overflow: hidden;
        background: #fff;
    }

    .video-card--video {
        aspect-ratio: 1 / 1;
        background: #f5f5f5;
    }

    .channel-video {
        width: 100%;
        height: 100%;
        display: block;
        overflow: hidden;
    }

    .video-placeholder {
        width: 100%;
        height: 100%;
        padding: 40rpx;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        background: linear-gradient(180deg, #646464 0%, #1f1f1f 100%);
        color: #fff;
    }

    .video-placeholder__tag {
        display: inline-flex;
        align-self: flex-start;
        padding: 6rpx 14rpx;
        border-radius: 999rpx;
        background: rgba(255, 255, 255, 0.16);
        font-size: 22rpx;
        line-height: 1.4;
    }

    .video-placeholder__title {
        margin-top: auto;
        font-size: 32rpx;
        font-weight: 600;
        line-height: 1.5;
    }

    .video-placeholder__desc {
        margin-top: 12rpx;
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.72);
        line-height: 1.5;
    }

    .video-card--live {
        display: flex;
        align-items: stretch;
        gap: 20rpx;
        padding: 20rpx;
        box-sizing: border-box;
    }

    .live-cover {
        width: 184rpx;
        min-width: 184rpx;
        height: 184rpx;
        padding: 18rpx;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        background: #f3f4f6;
        color: #fff;
    }

    .live-cover__badge {
        display: inline-flex;
        align-self: flex-start;
        padding: 6rpx 14rpx;
        border-radius: 999rpx;
        background: rgba(255, 255, 255, 0.18);
        font-size: 22rpx;
        line-height: 1.4;
    }

    .live-cover__name {
        font-size: 24rpx;
        line-height: 1.5;
    }

    .live-content {
        min-width: 0;
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .live-title {
        font-size: 28rpx;
        font-weight: 500;
        line-height: 40rpx;
        word-break: break-all;
    }

    .live-description {
        margin-top: 8rpx;
        font-size: 24rpx;
        color: #999;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .product-list {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-top: 16rpx;
    }

    .product-item {
        width: 72rpx;
        height: 72rpx;
        border-radius: 8rpx;
        background: #f3f4f6;
        flex-shrink: 0;
    }

    .product-item--placeholder {
        border: 2rpx dashed #e5e7eb;
        box-sizing: border-box;
        background: #fafafa;
    }

    .live-footer {
        margin-top: auto;
        padding-top: 20rpx;
        display: flex;
        align-items: center;
        gap: 16rpx;
    }

    .live-author {
        min-width: 0;
        flex: 1;
        font-size: 24rpx;
        color: #7a7a7a;
        line-height: 1.5;
    }

    .live-button {
        padding: 10rpx 22rpx;
        border-radius: 999rpx;
        background: #f4301d;
        color: #fff;
        font-size: 24rpx;
        line-height: 1.4;
        flex-shrink: 0;
    }
}
</style>
