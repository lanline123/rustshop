<template>
    <view class="video-feed">
        <!-- 返回按钮 -->
        <view class="nav-back" :style="{ top: saveTop + 8 + 'px' }" @click="goBack">
            <view class="iconfont-h5 icon-zhankai nav-back-icon" />
        </view>

        <swiper
            v-if="list.length > 0"
            class="feed-swiper"
            :style="feedContentStyle"
            vertical
            :current="current"
            :circular="false"
            :duration="250"
            @change="onSwiperChange"
            @touchstart="onFeedTouchStart"
            @touchend="onFeedTouchEnd"
        >
            <swiper-item v-for="(item, index) in list" :key="item.buyerShowId">
                <view class="feed-item">
                    <!-- 无附图：保持单视频结构，不引入内层容器 -->
                    <video
                        v-if="!hasMultiAsset(index) && shouldMountVideo(index)"
                        :id="videoId(index)"
                        class="feed-video"
                        :src="shouldRenderVideo(index) ? playSrc(index) : ''"
                        :poster="item.video.videoCover"
                        :initial-time="initialTimeOf(index)"
                        :controls="false"
                        :autoplay="index === current"
                        :muted="shouldMuteVideo(index)"
                        preload="auto"
                        :loop="true"
                        :show-center-play-btn="false"
                        :enable-progress-gesture="false"
                        object-fit="cover"
                        @play="onVideoPlay(index)"
                        @pause="onVideoPause(index)"
                        @timeupdate="readVideoTime(index, $event)"
                        @click="togglePlay"
                    />
                    <image v-else-if="!hasMultiAsset(index)" class="feed-video" :src="item.video.videoCover" mode="aspectFill" />

                    <!-- 有附图：视频占第 1 位，附图依次跟后，可横滑切换 -->
                    <swiper v-else class="asset-swiper" :current="assetIndexOf(index)" :circular="false" :duration="200" @change="onAssetChange(index, $event)">
                        <swiper-item>
                            <video
                                v-if="shouldMountVideo(index)"
                                :id="videoId(index)"
                                class="feed-video"
                                :src="shouldRenderVideo(index) ? playSrc(index) : ''"
                                :poster="item.video.videoCover"
                                :initial-time="initialTimeOf(index)"
                                :controls="false"
                                :autoplay="index === current"
                                :muted="shouldMuteVideo(index)"
                                preload="auto"
                                :loop="true"
                                :show-center-play-btn="false"
                                :enable-progress-gesture="false"
                                object-fit="cover"
                                @play="onVideoPlay(index)"
                                @pause="onVideoPause(index)"
                                @timeupdate="readVideoTime(index, $event)"
                                @click="togglePlay"
                            />
                            <image v-else class="feed-video" :src="item.video.videoCover" mode="aspectFill" />
                        </swiper-item>
                        <swiper-item v-for="(img, j) in imagesOf(index)" :key="j">
                            <image class="feed-image" :src="img.picUrl" mode="aspectFit" />
                        </swiper-item>
                    </swiper>

                    <!-- 暂停时的播放按钮；图片位不显示 -->
                    <image
                        v-if="index === current && currentPaused && isVideoAsset(index)"
                        class="play-btn"
                        :src="playIcon"
                        mode="aspectFit"
                        @click.stop="togglePlay"
                    />

                    <view
                        v-if="index === current && isVideoAsset(index) && !hasMultiAsset(index)"
                        class="video-progress-wrap"
                        :style="progressBarStyle"
                        @touchstart.stop
                        @touchmove.stop
                    >
                        <slider
                            class="video-progress"
                            :value="Math.min(currentTimeOf(index), Math.max(durationOf(index), 1))"
                            :max="Math.max(durationOf(index), 1)"
                            :block-size="12"
                            active-color="#ffffff"
                            background-color="rgba(255, 255, 255, 0.35)"
                            @changing="previewSeek(index, $event)"
                            @change="commitSeek(index, $event)"
                        />
                    </view>

                    <!-- 素材分段进度条：段数 = 1 视频 + N 附图 -->
                    <view v-if="hasMultiAsset(index)" class="asset-seg-bar" :style="assetSegBarStyle" @touchstart.stop @touchmove.stop>
                        <view v-for="seg in assetCount(index)" :key="seg" class="asset-seg">
                            <slider
                                v-if="seg === 1 && isVideoAsset(index)"
                                class="asset-video-progress"
                                :value="Math.min(currentTimeOf(index), Math.max(durationOf(index), 1))"
                                :max="Math.max(durationOf(index), 1)"
                                :block-size="10"
                                active-color="#ffffff"
                                background-color="rgba(255, 255, 255, 0.3)"
                                @changing="previewSeek(index, $event)"
                                @change="commitSeek(index, $event)"
                            />
                            <view v-else class="asset-seg-line" :class="{ 'is-active': seg - 1 === assetIndexOf(index) }" />
                        </view>
                    </view>

                    <!-- 右侧互动栏 -->
                    <view class="side-bar" :style="sideBarStyle">
                        <view class="side-item" @click.stop="toggleLike(item)">
                            <view class="iconfont-h5 side-icon" :class="item.isLike ? 'icon-yiguanzhu is-like' : 'icon-yishoucang1'" />
                            <text class="side-num">{{ formatCount(item.likeCount) }}</text>
                        </view>
                        <view class="side-item" @touchstart="prepareCommentTap" @click.stop="openComment(index)">
                            <view class="iconfont-h5 icon-pinglun1 side-icon" />
                            <text class="side-num">{{ formatCount(item.commentCount) }}</text>
                        </view>
                        <view class="side-item" @click.stop="handleShare">
                            <view class="iconfont-h5 icon-fenxiang2 side-icon" />
                        </view>
                    </view>

                    <!-- 底部信息 -->
                    <view class="bottom-bar" :style="bottomBarStyle">
                        <view v-if="item.productList && item.productList.length" class="product-card" @click.stop="goProduct(item.productList[0])">
                            <view class="pc-icon">
                                <view class="iconfont-h5 icon-gouwucheman1 pc-icon-img" />
                            </view>
                            <text class="pc-name">{{ item.productList[0].productName }}</text>
                        </view>

                        <view class="author-row">
                            <tig-image :src="item.author.buyerPhoto" class="author-avatar" mode="aspectFill" />
                            <text class="author-name">{{ item.author.buyerName }}</text>
                            <view v-if="item.author.userId && !item.author.isFollow" class="follow-btn" @click.stop="toggleFollow(item)">
                                {{ $t("关注") }}
                            </view>
                        </view>

                        <view v-if="item.content" class="content-box">
                            <view v-if="isLongText(item.content) && !contentExpanded" class="content-collapsed-row">
                                <text class="content-text is-collapsed">{{ collapsedContent(item.content) }}</text>
                                <text class="content-toggle" @click.stop="contentExpanded = true">{{ $t("展开") }}</text>
                            </view>
                            <template v-else-if="isLongText(item.content)">
                                <scroll-view class="content-scroll" scroll-y @touchstart.stop @touchmove.stop>
                                    <text class="content-text">{{ item.content }}</text>
                                </scroll-view>
                                <text class="content-toggle content-toggle--expanded" @click.stop="contentExpanded = false">{{ $t("收起") }}</text>
                            </template>
                            <text v-else class="content-text">{{ item.content }}</text>
                        </view>
                    </view>
                </view>
            </swiper-item>
        </swiper>

        <view v-if="list.length === 0 && loading" class="feed-status">
            <up-loading-icon color="#ffffff" />
        </view>
        <view v-if="list.length === 0 && !loading" class="feed-status">
            <up-empty icon="" :text="$t('暂无视频买家秀')" text-color="#ffffff" />
        </view>

        <view v-if="currentItem" class="feed-footer" :style="footerStyle">
            <view class="comment-entry" @touchstart="prepareCommentTap" @click.stop="openComment()">
                <text class="comment-placeholder">{{ $t(configStore.buyerShowGuidanceContent || "说点什么吧~") }}</text>
            </view>
        </view>

        <commentSheet v-if="currentItem" v-model:show="commentVisible" v-model:comment-count="commentCountModel" :buyer-show-id="currentItem.buyerShowId" />
        <buyerShowShare
            v-if="currentItem"
            v-model="showShare"
            :buyer-show-id="currentItem.buyerShowId"
            :content="currentItem.content"
            :buyer-name="currentItem.author.buyerName"
            :buyer-photo="currentItem.author.buyerPhoto"
            :cover="currentItem.video.videoCover"
        />
    </view>
</template>

<script setup lang="ts">
/**
 * 买家秀视频流页（H5 / 小程序）
 * - 外层竖向 swiper 翻内容，内层横向 swiper 翻当前条的附图
 * - 播放、分页与互动逻辑收敛在 useVideoFeed，与 App 端 nvue 共用
 */
import { computed, ref, watch } from "vue";
import { onHide, onLoad, onReady, onUnload } from "@dcloudio/uni-app";

import commentSheet from "./components/commentSheet.vue";
import buyerShowShare from "./components/buyerShowShare.vue";
import type { VideoFeedProduct } from "@/types/buyerShow/videoFeed";
import { redirect } from "@/utils";
import { useSafeAreaInsets } from "@/hooks";
import { useConfigStore } from "@/store/config";
import i18n from "@/i18n";
import { ensureShareLogin } from "@/utils/shareLogin";
import { useVideoFeed } from "./composables/useVideoFeed";
import playIcon from "./assets/videoFeed/play.png";

const { saveTop, safeBottom } = useSafeAreaInsets();
const configStore = useConfigStore();
const footerContentHeight = uni.upx2px(96);
const footerHeight = computed(() => safeBottom.value + footerContentHeight);
const feedContentStyle = computed(() => ({ height: `calc(100% - ${footerHeight.value}px)` }));
const progressBarStyle = computed(() => ({ bottom: -uni.upx2px(8) + "px" }));
const bottomBarStyle = computed(() => ({ bottom: uni.upx2px(30) + "px" }));
const assetSegBarStyle = computed(() => ({ bottom: -uni.upx2px(8) + "px" }));
const sideBarStyle = computed(() => ({ bottom: Math.max(0, 200 - footerContentHeight) + "px" }));
const footerStyle = computed(() => ({ height: footerHeight.value + "px", paddingBottom: safeBottom.value + "px" }));

const feed = useVideoFeed({ size: 5 });
const {
    list,
    current,
    loading,
    hasMore,
    inited,
    currentTimeOf,
    initialTimeOf,
    durationOf,
    readVideoTime,
    previewSeek,
    commitSeek,
    videoId,
    playSrc,
    shouldRenderVideo,
    imagesOf,
    assetCount,
    hasMultiAsset,
    assetIndexOf,
    isVideoAsset,
    setAssetIndex,
    clearAutoPlay,
    scheduleAutoPlay,
    init,
    onChange,
    playIndex,
    pauseAll,
    warmNeighbors,
    toggleLike,
    toggleFollow
} = feed;

const currentPaused = ref(false);
const h5AutoplayMuted = ref(false);
// H5 直接访问或刷新时没有用户手势，浏览器只允许静音自动播放
// #ifdef H5
h5AutoplayMuted.value = typeof navigator === "undefined" || !navigator.userActivation?.hasBeenActive;
// #endif
const shouldMuteVideo = (index: number) => index !== current.value || h5AutoplayMuted.value;
let shouldManuallyStartFirstVideo = true;
// 微信小程序由当前 video 的 autoplay 负责首播，避免与生命周期里的 play() 重复启动
// #ifdef MP-WEIXIN
shouldManuallyStartFirstVideo = false;
// #endif

/** 微信小程序只挂载当前屏 video，避免原生视频实例在后台自行播放串音 */
const shouldMountVideo = (index: number) => {
    let shouldMount = shouldRenderVideo(index);
    // #ifdef MP-WEIXIN
    shouldMount = index === current.value && isVideoAsset(index);
    // #endif
    return shouldMount;
};

const contentExpanded = ref(false);
// 文案超过 15 字显示展开/收起
const isLongText = (content?: string) => !!content && content.length > 15;
const collapsedContent = (content: string) => (content.length > 15 ? content.slice(0, 15) + "..." : content);

const commentVisible = ref(false);
const isFeedSwiping = ref(false);
const feedTouchStartY = ref<number | null>(null);
const feedTouchStartIndex = ref<number | null>(null);
const showShare = ref(false);
const currentItem = computed(() => list.value[current.value]);
const commentCountModel = computed({
    get: () => currentItem.value?.commentCount ?? 0,
    set: (val: number) => {
        if (currentItem.value) currentItem.value.commentCount = val;
    }
});

onLoad((options?: Record<string, string | undefined>) => {
    init(options?.buyerShowId);
});

onReady(() => {
    if (shouldManuallyStartFirstVideo && inited.value) {
        playIndex(current.value);
        setTimeout(() => warmNeighbors(current.value), 120);
    }
});

// 数据就绪后播放首屏并预热相邻
watch(inited, (ready) => {
    if (shouldManuallyStartFirstVideo && ready) {
        setTimeout(() => {
            playIndex(current.value);
            warmNeighbors(current.value);
        }, 80);
    }
});

// 评论弹层遮住画面期间不推进轮播，关闭后若仍停在图片位则续上
watch(commentVisible, (visible) => {
    if (visible) {
        clearAutoPlay();
    } else if (!isVideoAsset(current.value)) {
        scheduleAutoPlay();
    }
});

// pauseAll 内部已清理轮播定时器
onHide(() => pauseAll());
onUnload(() => pauseAll());

const onSwiperChange = (e: { detail: { current: number } }) => {
    const next = e.detail.current;
    isFeedSwiping.value = true;
    onChange(next);
    currentPaused.value = false;
    contentExpanded.value = false;
};

type FeedTouchEvent = {
    touches?: ArrayLike<{ clientY: number }>;
    changedTouches?: ArrayLike<{ clientY: number }>;
};

const onFeedTouchStart = (e: FeedTouchEvent) => {
    feedTouchStartY.value = e.touches?.[0]?.clientY ?? null;
    feedTouchStartIndex.value = current.value;
};

const onFeedTouchEnd = (e: FeedTouchEvent) => {
    const startY = feedTouchStartY.value;
    const startIndex = feedTouchStartIndex.value;
    feedTouchStartY.value = null;
    feedTouchStartIndex.value = null;
    if (startY === null || startIndex !== list.value.length - 1 || current.value !== list.value.length - 1 || hasMore.value) return;

    const endY = e.changedTouches?.[0]?.clientY;
    if (typeof endY !== "number" || startY - endY < uni.upx2px(80)) return;
    uni.showToast({ title: i18n.global.t("暂时没有更多了"), icon: "none" });
};

/** 内层横滑：切换当前屏的素材位，播放态与轮播定时器由 setAssetIndex 统一处理 */
const onAssetChange = (screen: number, e: { detail: { current: number } }) => {
    setAssetIndex(screen, e.detail.current);
};

const togglePlay = () => {
    // H5 静音首播后的第一次点击用于恢复声音，并保持视频继续播放
    if (h5AutoplayMuted.value) {
        h5AutoplayMuted.value = false;
        feed.playIndex(current.value);
        return;
    }
    if (currentPaused.value) {
        feed.playIndex(current.value);
    } else {
        feed.pauseIndex(current.value);
    }
};

const onVideoPlay = (index: number) => {
    // 微信小程序偶尔会自行恢复已挂载的相邻 video，立即暂停可避免串音
    // #ifdef MP-WEIXIN
    if (index !== current.value) {
        feed.pauseIndex(index);
        return;
    }
    // #endif
    if (index === current.value) currentPaused.value = false;
};

const onVideoPause = (index: number) => {
    if (index === current.value) currentPaused.value = true;
};

const prepareCommentTap = () => {
    isFeedSwiping.value = false;
};

const openComment = (screen = current.value) => {
    if (isFeedSwiping.value || screen !== current.value) return;
    clearAutoPlay();
    commentVisible.value = true;
};

const handleShare = () => {
    if (!ensureShareLogin()) return;
    showShare.value = true;
};

const goProduct = (product: VideoFeedProduct) => {
    redirect({ url: "/pages/product/index", param: { id: product.productId } });
};

const goBack = () => {
    uni.navigateBack({
        fail: () => {
            redirect({ url: "/pages/buyerShow/index" });
        }
    });
};

const formatCount = (num?: number) => {
    const value = Number(num) || 0;
    if (value >= 10000) return (value / 10000).toFixed(1) + "w";
    return String(value);
};
</script>

<style scoped lang="scss">
.video-feed {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background: #000;
    overflow: hidden;
}

.nav-back {
    position: fixed;
    left: 20rpx;
    z-index: 20;
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.nav-back-icon {
    color: #ffffff;
    font-size: 44rpx;
    line-height: 1;
}

.feed-swiper {
    width: 100%;
    height: 100%;
}

.feed-item {
    position: relative;
    width: 100%;
    height: 100%;
}

.feed-video {
    width: 100%;
    height: 100%;
    background: #000;
}

.asset-swiper {
    width: 100%;
    height: 100%;
}

.feed-image {
    width: 100%;
    height: 100%;
    background: #000;
}

.asset-seg-bar {
    position: absolute;
    left: 30rpx;
    right: 30rpx;
    display: flex;
    flex-direction: row;
    align-items: center;
    z-index: 5;
}

.asset-seg {
    flex: 1;
    height: 48rpx;
    margin-right: 8rpx;
    display: flex;
    align-items: center;

    &:last-child {
        margin-right: 0;
    }
}

.asset-seg-line {
    width: 100%;
    height: 2px;
    border-radius: 1px;
    background: rgba(255, 255, 255, 0.3);

    &.is-active {
        background: #ffffff;
    }
}

.asset-video-progress {
    width: 100%;
    margin: 0;
}

.video-progress-wrap {
    position: absolute;
    left: 12rpx;
    right: 12rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    z-index: 16;
}

.video-progress {
    width: 100%;
    margin: 0;
}

.play-btn {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 112rpx;
    height: 112rpx;
    opacity: 0.85;
    z-index: 10;
}

.side-bar {
    position: absolute;
    right: 24rpx;
    display: flex;
    flex-direction: column;
    z-index: 10;

    .side-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 44rpx;

        &:last-child {
            margin-bottom: 0;
        }
    }

    .side-icon {
        width: 56rpx;
        height: 56rpx;
        margin-bottom: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        font-size: 56rpx;
        line-height: 1;

        &.is-like {
            color: #ff4444;
        }

    }

    .side-num {
        font-size: 24rpx;
        color: #ffffff;
    }
}

.bottom-bar {
    position: absolute;
    left: 0;
    /* 让出右侧互动栏的点击区，避免长文案盒子横向盖住点赞/评论/收藏 */
    right: 120rpx;
    padding: 0 0 20rpx 30rpx;
    box-sizing: border-box;
    z-index: 10;

    .product-card {
        display: inline-flex;
        align-items: center;
        max-width: 520rpx;
        padding: 12rpx 20rpx 12rpx 12rpx;
        margin-bottom: 24rpx;
        background: rgba(0, 0, 0, 0.4);
        border-radius: 40rpx;

        .pc-icon {
            flex-shrink: 0;
            width: 40rpx;
            height: 40rpx;
            margin-right: 12rpx;
            border-radius: 50%;
            background: var(--general, #ff6b00);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .pc-icon-img {
            width: 24rpx;
            height: 24rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-size: 24rpx;
            line-height: 1;
        }

        .pc-name {
            flex: 1;
            font-size: 24rpx;
            color: #ffffff;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }

    .author-row {
        display: flex;
        align-items: center;
        margin-bottom: 18rpx;

        .author-avatar {
            width: 64rpx;
            height: 64rpx;
            margin-right: 16rpx;
            border-radius: 50%;
            overflow: hidden;
            border: 2rpx solid rgba(255, 255, 255, 0.8);
        }

        .author-name {
            font-size: 30rpx;
            font-weight: 500;
            color: #ffffff;
        }

        .follow-btn {
            margin-left: 16rpx;
            padding: 6rpx 24rpx;
            font-size: 24rpx;
            color: #ffffff;
            background: var(--general, #ff4444);
            border-radius: 40rpx;
        }
    }

    .content-box {
        display: flex;
        flex-direction: column;
    }

    .content-collapsed-row {
        display: flex;
        align-items: center;
        min-width: 0;
        height: 42rpx;
        overflow: hidden;
    }

    .content-text {
        display: block;
        font-size: 28rpx;
        color: #ffffff;
        line-height: 42rpx;
        word-break: break-all;

        &.is-collapsed {
            max-width: calc(100% - 80rpx);
            height: 42rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }

    .content-scroll {
        max-height: 630rpx;
    }

    .content-toggle {
        flex-shrink: 0;
        margin-left: 8rpx;
        font-size: 26rpx;
        color: rgba(255, 255, 255, 0.75);
        white-space: nowrap;
    }

    .content-toggle--expanded {
        align-self: flex-start;
        margin-top: 8rpx;
        margin-left: 0;
    }
}

.feed-footer {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 18;
    display: flex;
    align-items: center;
    padding-left: 20rpx;
    padding-right: 20rpx;
    box-sizing: border-box;
    background: rgba(12, 12, 12, 0.92);
}

.comment-entry {
    flex: 1;
    height: 68rpx;
    display: flex;
    align-items: center;
    padding: 0 28rpx;
    border-radius: 36rpx;
    background: rgba(255, 255, 255, 0.12);
}

.comment-placeholder {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.46);
}

.feed-status {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}
</style>
