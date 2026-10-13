<template>
    <view class="module-ad-con module-live" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <view class="module-ad-content" :style="frameFormat.backgroundColor + frameFormat.boxRadius">
            <view class="live-container" :style="frameFormat.innerPadding">
                <template v-if="liveList.length > 0">
                    <view v-if="isBigImageMode" class="live-list live-list--bigImage">
                        <view
                            v-for="item in liveList"
                            :key="item.wechatLiveId || item.roomId"
                            class="live-card live-card--bigImage"
                            :style="cardStyle"
                            @click="handleOpenLive(item)"
                        >
                            <view class="live-card__cover live-card__cover--full" :style="cardRadiusStyle + (getCover(item) ? 'background:none;' : '')">
                                <image v-if="getCover(item)" class="live-card__cover-image" :src="getCover(item)" mode="aspectFill" />
                                <view v-else class="live-card__cover-placeholder">
                                    <text class="live-card__cover-placeholder-text">{{ $t("直播预告") }}</text>
                                </view>
                                <view class="live-card__status-tag" :class="getLiveStatusClass(item)">{{ $t(getLiveStatusText(item) || t("直播中")) }}</view>
                                <view class="live-card__mask">
                                    <view class="live-card__mask-title">{{ $t(item.name || item.wechatLiveTitle || "") }}</view>
                                    <view class="live-card__mask-meta">
                                        <text>{{ $t("主播") }}：{{ item.anchorName || item.anchorOpenid }}</text>
                                        <view class="live-card__action">{{ $t("进入直播间") }}</view>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>

                    <view v-else class="live-list live-list--list">
                        <view
                            v-for="item in liveList"
                            :key="item.wechatLiveId || item.roomId"
                            class="live-card live-card--list"
                            :style="cardStyle"
                            @click="handleOpenLive(item)"
                        >
                            <view class="live-card__cover live-card__cover--list" :style="cardRadiusStyle + (getCover(item) ? 'background:none;' : '')">
                                <image v-if="getCover(item)" class="live-card__cover-image" :src="getCover(item)" mode="aspectFill" />
                                <view v-else class="live-card__cover-placeholder">
                                    <text class="live-card__cover-placeholder-text">{{ $t("直播预告") }}</text>
                                </view>
                                <view class="live-card__badge">{{ $t("小程序直播") }}</view>
                                <view class="live-card__status">{{ $t(item.liveStatusText || "直播中") }}</view>
                            </view>

                            <view class="live-card__content">
                                <view class="live-card__title">{{ $t(item.name || item.wechatLiveTitle || "") }}</view>
                                <view class="live-card__goods">
                                    <template v-if="getLiveGoodsImages(item).length > 0">
                                        <image
                                            v-for="(goodsImg, goodsIndex) in getLiveGoodsImages(item)"
                                            :key="`${item.roomId}-${goodsIndex}`"
                                            class="live-card__goods-item"
                                            :src="goodsImg"
                                            mode="aspectFill"
                                        />
                                    </template>
                                </view>
                                <view class="live-card__footer">
                                    <view class="live-card__meta">{{ $t("主播") }}：{{ $t(item.anchorName || item.anchorOpenid || "官方直播间") }}</view>
                                    <view class="live-card__action">{{ $t("进入直播间") }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                </template>

                <view v-else class="live-empty" :style="cardRadiusStyle">
                    <view class="live-empty__title">{{ $t("暂无可展示直播间") }}</view>
                    <view class="live-empty__desc">{{ $t("请在后台先配置小程序直播内容") }}</view>
                </view>
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

import { computed } from "vue";
import { formatFrame } from "@/components/modules";
import { imageFormat, pickPic } from "@/utils/format";
import type { ModuleFrameType } from "@/types/decorate/decorate.d";

const { t } = useI18n();
declare const wx: any;

interface LiveProductItem {
    coverImg?: string;
    picThumb?: string;
    picUrl?: string;
    productThumb?: string;
    productImage?: string;
    image?: string;
}

interface LiveRoomState {
    wechatLiveId?: number;
    roomId?: number | string;
    roomIdStr?: string;
    name?: string;
    wechatLiveTitle?: string;
    coverImg?: string;
    feedsImg?: string;
    shareImg?: string;
    thumbImg?: string;
    liveCardCover?: string;
    backgroundWall?: string;
    anchorName?: string;
    anchorOpenid?: string;
    anchorWechat?: string;
    liveStatus?: number | string;
    liveStatusText?: string;
    productData?: LiveProductItem[];
}

interface LiveDecorateModule {
    titleText?: string;
    displayMode?: "list" | "bigImage";
    sourceType?: "auto" | "manual";
    autoLiveNum?: number;
    selectedLiveList?: LiveRoomState[];
    frame?: ModuleFrameType;
}

const defaultFrame: ModuleFrameType = {
    backgroundColor: "#ffffff",
    innerPadding: 12,
    boxRadius: 8,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5
};

const liveStatusTextMap: Record<number, string> = {
    101: "直播中",
    102: t("未开始"),
    103: t("已结束"),
    104: t("禁播"),
    105: t("暂停"),
    106: t("异常"),
    107: t("已过期")
};

const props = defineProps({
    module: {
        type: Object as () => LiveDecorateModule,
        default: () => ({})
    }
});

const frameFormat = computed(() => formatFrame(props.module.frame ?? defaultFrame));
const cardRadiusStyle = computed(() => `border-radius:${Number(props.module.frame?.boxRadius ?? defaultFrame.boxRadius)}px;`);
const cardStyle = computed(() => {
    const backgroundColor = props.module.frame?.backgroundColor || defaultFrame.backgroundColor;
    return `${cardRadiusStyle.value}background-color:${backgroundColor};`;
});
const isBigImageMode = computed(() => (props.module.displayMode || "list") === "bigImage");

const getLiveTitle = (item: LiveRoomState) => item.name || item.wechatLiveTitle;
const getAnchorName = (item: LiveRoomState) => item.anchorName || item.anchorOpenid || item.anchorWechat;
const getLiveStatusText = (item: LiveRoomState) => item.liveStatusText || liveStatusTextMap[Number(item.liveStatus)];

const getLiveStatusClass = (item: LiveRoomState) => {
    const status = Number(item.liveStatus);
    if (status === 101) return "live-card__status-tag--live";
    if (status === 102) return "live-card__status-tag--pending";
    return "live-card__status-tag--ended";
};

const normalizeLiveItem = (item: LiveRoomState): LiveRoomState => ({
    ...item,
    roomId: Number(item.roomId || 0),
    roomIdStr: String(item.roomIdStr || item.roomId || ""),
    name: getLiveTitle(item),
    anchorName: getAnchorName(item),
    coverImg: item.coverImg || item.thumbImg || item.shareImg || item.feedsImg || item.liveCardCover || item.backgroundWall || "",
    liveStatusText: getLiveStatusText(item)
});

const liveList = computed(() => {
    const selectedLiveList = props.module.selectedLiveList || [];
    return selectedLiveList.map(normalizeLiveItem).slice(0, 10);
});

const getCover = (item: LiveRoomState) => {
    return imageFormat(item.feedsImg || "");
};

const getLiveGoodsImages = (item: LiveRoomState) => {
    return (item.productData || [])
        .map((goods) => imageFormat(goods.coverImg || pickPic(goods) || goods.productThumb || goods.productImage || goods.image || ""))
        .filter((img): img is string => Boolean(img))
        .slice(0, 3);
};

const handleOpenLive = (item: LiveRoomState) => {
    const roomId = Number(item.roomId || 0);
    if (!roomId) {
        uni.showToast({
            title: t("直播间信息不完整"),
            icon: "none"
        });
        return;
    }

    wx.navigateTo({
        url: `plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=${roomId}`,
        fail: (error: any) => {
            uni.showToast({
                title: error?.errMsg || t("打开直播间失败"),
                icon: "none"
            });
        }
    });
};
</script>

<style scoped lang="scss">
.module-live {
    .module-ad-content {
        overflow: hidden;
    }

    .live-container {
        box-sizing: border-box;
    }

    .live-list--bigImage {
        display: flex;
        flex-direction: column;
        gap: 20rpx;
    }

    .live-card--bigImage {
        position: relative;
        width: 100%;
        overflow: hidden;
        box-sizing: border-box;
    }

    .live-card__cover--full {
        position: relative;
        width: 100%;
        height: 0;
        padding-bottom: 80%;
        overflow: hidden;
        background: linear-gradient(180deg, #7d6bff 0%, #3d2fc5 100%);
    }

    .live-card__cover--full .live-card__cover-image {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
    }

    .live-card__cover--full .live-card__cover-placeholder {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .live-card__mask {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        flex-direction: column;
        gap: 12rpx;
        padding: 32rpx;
        height: 140rpx;
        box-sizing: border-box;
        background: #fff;
        z-index: 2;
    }

    .live-card__status-tag {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        padding: 6rpx 14rpx;
        border-radius: 999rpx;
        font-size: 20rpx;
        line-height: 1.4;
        color: #fff;
        z-index: 3;
        background: rgba(0, 0, 0, 0.45);
    }

    .live-card__status-tag--live {
        background: rgba(255, 68, 68, 0.9);
    }

    .live-card__status-tag--pending {
        background: rgba(121, 114, 255, 0.95);
    }

    .live-card__status-tag--ended {
        background: rgba(0, 0, 0, 0.45);
    }

    .live-card__mask-title {
        font-size: 28rpx;
        line-height: 40rpx;
        word-break: break-all;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex-shrink: 0;
    }

    .live-card__mask-meta {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 24rpx;
        color: #a7a7a7;
    }

    .live-list--list {
        display: flex;
        flex-direction: column;
        gap: 20rpx;
    }

    .live-card {
        box-sizing: border-box;
        background: #fff;
        overflow: hidden;
    }

    .live-card--list {
        display: flex;
        align-items: stretch;
        gap: 20rpx;
        padding: 20rpx;
    }

    .live-card__cover {
        position: relative;
        overflow: hidden;
        background: linear-gradient(180deg, #7d6bff 0%, #3d2fc5 100%);
    }

    .live-card__cover--list {
        width: 188rpx;
        min-width: 188rpx;
        height: 188rpx;
    }

    .live-card__cover-image {
        width: 100%;
        height: 100%;
        display: block;
    }

    .live-card__cover-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24rpx;
        box-sizing: border-box;
        background: linear-gradient(180deg, #7d6bff 0%, #3d2fc5 100%);
    }

    .live-card__cover-placeholder-text {
        color: rgba(255, 255, 255, 0.92);
        font-size: 28rpx;
        font-weight: 600;
    }

    .live-card__badge,
    .live-card__status {
        position: absolute;
        left: 16rpx;
        padding: 6rpx 14rpx;
        border-radius: 999rpx;
        color: #fff;
        font-size: 20rpx;
        line-height: 1.4;
        z-index: 1;
    }

    .live-card__badge {
        top: 16rpx;
        background: rgba(244, 48, 29, 0.92);
    }

    .live-card__status {
        bottom: 16rpx;
        background: rgba(0, 0, 0, 0.45);
    }

    .live-card__content {
        min-width: 0;
        flex: 1;
        padding: 20rpx;
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
    }

    .live-card--list .live-card__content {
        padding: 0;
    }

    .live-card__title {
        color: #2a2a2a;
        font-size: 28rpx;
        font-weight: 500;
        line-height: 40rpx;
        word-break: break-all;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
    }

    .live-card__goods {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-top: 16rpx;
    }

    .live-card__goods-item {
        width: 72rpx;
        height: 72rpx;
        border-radius: 8rpx;
        flex-shrink: 0;
        overflow: hidden;
    }

    .live-card__goods-item--placeholder {
        //border: 2rpx dashed #e5e7eb;
        box-sizing: border-box;
        //background: #fafafa;
    }

    .live-card__footer {
        margin-top: auto;
        padding-top: 20rpx;
        display: flex;
        align-items: center;
        gap: 16rpx;
    }

    .live-card__meta {
        min-width: 0;
        flex: 1;
        color: #7a7a7a;
        font-size: 24rpx;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .live-card__action {
        padding: 10rpx 22rpx;
        border-radius: 999rpx;
        background: var(--general);
        color: #fff;
        font-size: 24rpx;
        line-height: 1.4;
        flex-shrink: 0;
    }

    .live-empty {
        padding: 56rpx 32rpx;
        background: #fff;
        text-align: center;
    }

    .live-empty__title {
        color: #2a2a2a;
        font-size: 28rpx;
        font-weight: 500;
        line-height: 1.5;
    }

    .live-empty__desc {
        margin-top: 12rpx;
        color: #999;
        font-size: 24rpx;
        line-height: 1.5;
    }
}
</style>
