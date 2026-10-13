<template>
    <template v-if="buyerShowEnabled === 1">
        <tig-layout :title="$t('详情')">
            <!-- 图片/视频加载前的占位骨架（首张媒体就绪前避免空白 video 壳闪烁） -->
            <view
                v-if="!swiperList.length || isLoading || !mediaReady"
                class="swiper-skeleton"
                :style="{ height: swiperHeight, minHeight: minImageHeight + 'px' }"
            >
                <view class="skeleton-loading">
                    <view class="skeleton-icon">
                        <text class="iconfont-h5 icon-tupian" />
                    </view>
                </view>
            </view>
            <up-swiper
                v-if="!isLoading && swiperList.length > 0 && mediaReady"
                class="swiperBox"
                key-name="image"
                :list="swiperList"
                :height="swiperHeight"
                indicator
                :autoplay="false"
                indicator-mode="dot"
                indicator-active-color="#FF4444"
                :style="{ height: swiperHeight, minHeight: minImageHeight + 'px' }"
                :duration="300"
                @change="onSwiperChange"
            >
                <template #default="{ item, index }">
                    <!-- 小程序端：slot 根节点可能拿不到 swiper 内部高度，这里显式给每一项定高兜底 -->
                    <view class="swiper-item-root" :style="{ height: swiperHeight, minHeight: minImageHeight + 'px' }">
                        <view
                            v-if="item.type === 'image'"
                            class="media-preview-wrapper"
                            @touchstart.passive="onTouchStart"
                            @click="() => handleImageClick(item)"
                        >
                            <tig-image :src="item.image" class="media-preview-image" mode="aspectFit" />
                        </view>
                        <video
                            v-else-if="item.type === 'video'"
                            :id="'video-' + index"
                            :src="current === index ? item.image : ''"
                            :poster="item.poster"
                            :controls="true"
                            :autoplay="false"
                            :muted="false"
                            :show-center-play-btn="true"
                            :enable-progress-gesture="false"
                            object-fit="contain"
                            class="media-preview-video"
                            @play="() => onVideoPlay(index)"
                        />
                    </view>
                </template>
                <template #indicator>
                    <view v-if="swiperList.length > 1">
                        <view class="indicator">
                            <template v-for="(item, index) in swiperList" :key="index">
                                <view class="indicator__dot" :class="[index === current && 'indicator__dot--active']" />
                            </template>
                        </view>
                    </view>
                </template>
            </up-swiper>
            <view class="container">
                <view class="userBox">
                    <view class="photo">
                        <tig-image :src="buyerShowDetailData.buyerPhoto" class="content-image" mode="wid" />
                    </view>
                    <view class="userName">{{ buyerShowDetailData.buyerName || "" }}</view>
                </view>
                <view class="content">{{ buyerShowDetailData.content || "" }}</view>
                <view class="addTime">{{ $t("发布于") }}{{ buyerShowDetailData.addTime }}</view>
                <scroll-view :scroll-x="true">
                    <view class="productListBox">
                        <template v-for="(item, index) in buyerShowDetailData.productList" :key="index">
                            <view class="productList" @click="toProductDetail(item)">
                                <view class="productListInfo">
                                    <view class="productImg">
                                        <up-image :src="imageFormat(item.picUrl)" width="200rpx" height="200rpx" />
                                        <image
                                            v-if="buyerShowProductStyleMap[item.productId]?.borderImage"
                                            class="product-style-border"
                                            :src="buyerShowProductStyleMap[item.productId].borderImage"
                                            mode="scaleToFill"
                                        />
                                        <view v-if="shouldShowSoldOut(item, { stockField: 'storeProductStock' })" class="outsale">{{ $t("已售罄") }}</view>
                                    </view>
                                    <view class="productRight">
                                        <view class="title">{{ $t(item.productName) }}</view>
                                        <view v-if="item.productPrice" class="price">¥{{ item.productPrice }}</view>
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>
                </scroll-view>
                <up-divider text="" />
                <view class="comments">
                    <comments ref="commentsRef" v-model:buyer-show-id="buyerShowId" @show-reply-input="showReplyInput" />
                </view>
            </view>
            <tig-fixed-placeholder :height="isFocus ? '200rpx' : '100rpx'" background-color="#fff">
                <view class="footer">
                    <up-input
                        ref="inputRef"
                        v-model="content"
                        class="input"
                        shape="circle"
                        :placeholder="$t(configStore.buyerShowGuidanceContent || '说点什么吧~')"
                        :placeholder-style="{ fontSize: '26rpx' }"
                        :focus="isFocus"
                        border="surround"
                        auto-blur
                        :cursor-spacing="60"
                        :hold-keyboard="true"
                        @focus="inputFocus"
                        @confirm="confirm"
                        @blur="inputBlur"
                    />
                    <view class="countBox">
                        <view class="LikeBox" @click.stop="likeComment">
                            <view class="iconfont-h5" :class="buyerShowDetailData.isLike ? 'icon-yiguanzhu' : 'icon-guanzhu'" />
                            <view class="likes" :class="{ liked: buyerShowDetailData.isLike }">
                                {{ Number(buyerShowDetailData.likeCount) > 0 ? buyerShowDetailData.likeCount : 0 }}
                            </view>
                        </view>
                        <view class="LikeBox">
                            <view class="iconfont-h5 icon-pinglun" />
                            <view class="numberOfMessages">{{ buyerShowDetailData.commentCount || 0 }}</view>
                        </view>
                        <view class="LikeBox shareBox" @click.stop="handleShare">
                            <view class="iconfont-h5 icon-af" />
                        </view>
                    </view>
                </view>
                <emojiPicker v-if="isFocus" ref="emojiPickerRef" @insert-emoji="insertEmoji" />
            </tig-fixed-placeholder>
            <buyerShowShare
                v-model="showShare"
                :buyer-show-id="buyerShowId ?? ''"
                :content="buyerShowDetailData.content"
                :buyer-name="buyerShowDetailData.buyerName"
                :buyer-photo="buyerShowDetailData.buyerPhoto"
                :cover="shareCover"
            />
            <tig-loadingpage v-model="isLoading" />
        </tig-layout>
    </template>
    <template v-else>
        <tig-layout :title="$t('详情')" :show-left="false" style="height: 100vh">
            <buyerShowCloseTip v-model:buyer-show-enabled="buyerShowEnabled" />
        </tig-layout>
    </template>
</template>

<script setup lang="ts">
/**
 * 买家秀详情页
 * - 展示图片/视频轮播、用户信息、内容和关联商品
 * - 支持点赞和评论功能，评论支持表情输入
 * - 点赞状态变化时通过 Storage 标记，返回列表时刷新
 * - 页面隐藏时自动暂停视频播放
 */
import TigLayout from "@/components/tig/tig-layout/tig-layout.vue";
import { ref, computed, shallowRef, watch, nextTick } from "vue";
import TigImage from "@/components/tig/tig-image/tig-image.vue";
import TigFixedPlaceholder from "@/components/tig/tig-fixed-placeholder/tig-fixed-placeholder.vue";
import buyerShowCloseTip from "./src/buyerShowCloseTip.vue";
import buyerShowShare from "./components/buyerShowShare.vue";
import comments from "./comments.vue";
import { getImageInfo } from "@/utils";
import { onLoad, onHide, onUnload } from "@dcloudio/uni-app";
import { buyerShowDetail, buyerShowComment, buyerShowLike } from "@/api/buyerShow/buyerShow";
import type { BuyerShowDetailState } from "@/types/buyerShow/buyerShow";
import emojiPicker from "./buyerShowCompnents/emojiPicker.vue";
import { useConfigStore } from "@/store/config";
const configStore = useConfigStore();
import { redirect } from "@/utils";
import { ensureShareLogin } from "@/utils/shareLogin";
import { useI18n } from "vue-i18n";
import { imageFormat } from "@/utils/format";
import { useProductStyle } from "@/hooks/useProductStyle";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
const { t } = useI18n();

const BUYER_SHOW_LIST_NEED_REFRESH_KEY = "buyerShow:listNeedRefresh";

const buyerShowEnabled = ref<number | undefined>(1);
watch(
    () => configStore,
    (val) => {
        if (val.initLoading) {
            buyerShowEnabled.value = val.buyerShowEnabled;
        }
    },
    { deep: true, immediate: true }
);

const commentsRef = shallowRef();
const inputRef = shallowRef();
const emojiPickerRef = shallowRef();
const buyerShowId = ref(null);
const showShare = ref(false);
const buyerShowDetailData = ref<BuyerShowDetailState>({
    videoList: [],
    imgList: []
});
const buyerShowProductList = computed(() => buyerShowDetailData.value?.productList ?? []);
const {
    styleMap: buyerShowProductStyleMap,
    fetchStyles: fetchBuyerShowProductStyles,
    resetStyles: resetBuyerShowProductStyles
} = useProductStyle(buyerShowProductList);

// 进入详情时的点赞状态（用于判断离开时是否发生变化）
const initialIsLike = ref<boolean | null>(null);
// 仅当用户在详情里实际操作过点赞/取消点赞后，才触发返回刷新
const hasLikeToggled = ref(false);
const isLikeDirty = computed(() => {
    if (initialIsLike.value === null) return false;
    return Boolean(buyerShowDetailData.value?.isLike) !== initialIsLike.value;
});
const isLoading = ref(false);
// 首张媒体（图片/视频封面）就绪后再展示轮播，避免空白 video 组件闪一下
const mediaReady = ref(false);
const swiperList = computed(() => {
    const list = [...(buyerShowDetailData.value?.videoList ?? []), ...(buyerShowDetailData.value?.imgList ?? [])].filter(Boolean);
    list.forEach((item: any) => {
        // 有 videoUrl 视为视频；封面用 videoCover，没有则用 picUrl 兜底
        if (item.videoUrl) {
            item.type = "video";
            item.image = item.videoUrl;
            item.poster = imageFormat(item.videoCover || item.picUrl || "");
        } else {
            item.type = "image";
            item.image = imageFormat(item.picUrl || "");
        }
    });
    return list || [];
});

// 海报封面：优先取首个视频封面，否则取首张图片（原始地址，交由海报组件统一 imageFormat）
const shareCover = computed(() => {
    const video = (buyerShowDetailData.value?.videoList ?? [])[0];
    if (video) {
        return video.videoCover || video.picUrl || "";
    }
    const image = (buyerShowDetailData.value?.imgList ?? [])[0];
    return image?.picUrl || "";
});

const handleShare = () => {
    if (!ensureShareLogin()) {
        return;
    }
    showShare.value = true;
};

const toProductDetail = (item: any) => {
    redirect({
        url: "/pages/product/index",
        param: {
            id: item.productId
        }
    });
};

const current = ref(0);

// 触摸判断相关 - 用于区分滑动和点击
const touchStartX = ref(0);
const isSwiping = ref(false);

const onTouchStart = (e: any) => {
    const touch = e.touches[0];
    touchStartX.value = touch.clientX;
    isSwiping.value = false;
};

// 当前播放的视频索引
const playingVideoIndex = ref<number | null>(null);

// 是否开启“切到视频自动播放”
const autoPlayVideo = ref(true);
let autoPlayTimer: ReturnType<typeof setTimeout> | null = null;

// 暂停所有视频
const pauseAllVideos = () => {
    swiperList.value.forEach((_: any, index: number) => {
        const videoContext = uni.createVideoContext(`video-${index}`);
        if (videoContext) {
            videoContext.pause();
        }
    });
    playingVideoIndex.value = null;
};

// 播放指定索引的视频（会等一帧，确保 video 节点已渲染）
const playVideoByIndex = (index: number) => {
    if (!autoPlayVideo.value) return;
    if (!mediaReady.value) return;
    if (swiperList.value[index]?.type !== "video") return;
    if (autoPlayTimer) clearTimeout(autoPlayTimer);
    autoPlayTimer = setTimeout(async () => {
        await nextTick();
        const videoContext = uni.createVideoContext(`video-${index}`);
        // 某些端可能会因为“非用户手势”而禁止自动播放，失败也不影响其他功能
        try {
            videoContext?.play?.();
            playingVideoIndex.value = index;
        } catch {
            // noop
        }
    }, 80);
};

// 视频开始播放时记录
const onVideoPlay = (index: number) => {
    // 暂停其他正在播放的视频
    if (playingVideoIndex.value !== null && playingVideoIndex.value !== index) {
        const videoContext = uni.createVideoContext(`video-${playingVideoIndex.value}`);
        if (videoContext) {
            videoContext.pause();
        }
    }
    playingVideoIndex.value = index;
};

// 监听 swiper 滑动，标记为正在滑动
const onSwiperChange = (e: any) => {
    const prevIndex = current.value;
    current.value = e.current;
    isSwiping.value = true;

    // 切换时暂停之前的视频
    if (swiperList.value[prevIndex]?.type === "video") {
        const videoContext = uni.createVideoContext(`video-${prevIndex}`);
        if (videoContext) {
            videoContext.pause();
        }
    }
    // 切换到视频时自动播放当前视频
    playVideoByIndex(current.value);
};

const handleImageClick = (item: any) => {
    // 如果正在滑动，不触发预览
    if (isSwiping.value) {
        isSwiping.value = false;
        return;
    }
    handleImagePreview(item);
};

const handleImagePreview = (item: any) => {
    const imageUrls = swiperList.value.filter((i: any) => i.type === "image").map((i: any) => i.image);
    uni.previewImage({
        urls: imageUrls,
        current: item.image,
        indicator: "number",
        loop: true
    });
};

const swiperHeight = ref("300rpx");
const maxImageHeight = 600;
const minImageHeight = 300;

const getFirstImageSizeDefault = () => {
    const systemInfo = uni.getSystemInfoSync();
    const width = systemInfo.windowWidth;
    const height = (width * 9) / 16; // 16:9比例
    swiperHeight.value = height + "px";
};

// 获取第一张图片的实际尺寸
const getFirstImageSize = async () => {
    if (swiperList.value.length > 0) {
        try {
            // 预取首张媒体（图片/封面），避免轮播先渲染出空白 video 壳
            mediaReady.value = false;
            await getImageInfo(swiperList.value[0]?.poster || swiperList.value[0]?.image);
            const res = await Promise.all(
                swiperList.value.map(async (item: any) => {
                    return await getImageInfo(item?.poster || item?.image);
                })
            );
            // 返回res.height最大的项
            const maxItem = res.reduce((prev: any, curr: any) => {
                return prev.height > curr.height ? prev : curr;
            });
            // 根据图片实际尺寸计算高度
            const systemInfo = uni.getSystemInfoSync();
            const screenWidth = systemInfo.windowWidth;
            const imageHeight = (screenWidth * maxItem.height) / maxItem.width;
            swiperHeight.value = imageHeight > maxImageHeight ? maxImageHeight + "px" : imageHeight + "px";
            mediaReady.value = true;
            // 首屏如果是视频，媒体就绪后自动播放
            playVideoByIndex(current.value);
        } catch {
            getFirstImageSizeDefault();
            mediaReady.value = true;
            playVideoByIndex(current.value);
        }
    } else {
        getFirstImageSizeDefault();
        mediaReady.value = true;
        playVideoByIndex(current.value);
    }
};

const content = ref("");

const getBuyerShowDetail = () => {
    isLoading.value = true;
    mediaReady.value = false;
    buyerShowDetail({ buyerShowId: buyerShowId.value })
        .then((res: any) => {
            buyerShowDetailData.value = res;
            resetBuyerShowProductStyles();
            fetchBuyerShowProductStyles(buyerShowProductList.value);
            // 记录进入详情时的点赞状态（只记录一次）
            if (initialIsLike.value === null) {
                initialIsLike.value = Boolean(res?.isLike);
            }
            isLoading.value = false;
            current.value = 0;
            getFirstImageSize();
        })
        .catch((error: any) => {
            isLoading.value = false;
            uni.showToast({
                title: error.message,
                icon: "error"
            });
        });
};

const confirm = async () => {
    if (!content.value) {
        uni.showToast({
            title: t("评论不能为空"),
            icon: "none"
        });
        return;
    }
    buyerShowComment({ buyerShowId: buyerShowId.value, content: content.value, parentId: replyData.value.buyerShowCommentId || 0 })
        .then(() => {
            content.value = "";
            uni.showToast({
                title: t("评论成功")
            });
            commentsRef.value.getList({ buyerShowId: buyerShowId.value });
            buyerShowDetailData.value.commentCount = (buyerShowDetailData.value.commentCount || 0) + 1;
            // 清除回复数据
            replyData.value = {};
        })
        .catch((error: any) => {
            uni.showToast({
                title: error.message,
                icon: "error"
            });
        });
};

const isFocus = ref(false);
// 输入框聚焦
const inputFocus = () => {
    isFocus.value = true;
};
// 输入框失去焦点
const inputBlur = () => {
    isFocus.value = false;
};
// 获取输入表情
const insertEmoji = (emoji: string) => {
    isFocus.value = true;
    content.value += emoji;
    inputRef.value?.onFocus();
};

/**
 * 买家秀点赞/取消点赞
 * - 记录初始点赞状态，用于判断离开时是否需要刷新列表
 * - 本地乐观更新点赞数和状态
 */
const likeComment = async () => {
    try {
        // 兜底：如果详情数据还没初始化完，先记录当前状态作为“进入时”状态
        if (initialIsLike.value === null) {
            initialIsLike.value = Boolean(buyerShowDetailData.value?.isLike);
        }
        await buyerShowLike({
            buyerShowId: buyerShowDetailData.value.buyerShowId,
            isLike: !buyerShowDetailData.value.isLike ? 1 : 0
        });
        uni.showToast({
            title: !buyerShowDetailData.value.isLike ? t("点赞成功") : t("取消点赞"),
            icon: "none"
        });
        buyerShowDetailData.value.isLike = !buyerShowDetailData.value.isLike;
        const currentLikeCount = buyerShowDetailData.value.likeCount || 0;
        buyerShowDetailData.value.likeCount = currentLikeCount + (buyerShowDetailData.value.isLike ? 1 : -1);
        hasLikeToggled.value = true;

        // getBuyerShowDetail();
    } catch (error: any) {
        uni.showToast({
            title: error?.message
        });
    }
};
// 上级回复的信息
const replyData = ref<any>({});

// 点击回复
const showReplyInput = (data: any) => {
    replyData.value = data;
    inputRef.value.onFocus();
    // #ifndef H5
    setTimeout(() => {
        isFocus.value = true;
    }, 100);
    // #endif
};

onLoad((options: any) => {
    buyerShowId.value = options.buyerShowId;
    // 每次进入详情重置对比基准
    initialIsLike.value = null;
    hasLikeToggled.value = false;
    mediaReady.value = false;
    if (buyerShowId.value) {
        getBuyerShowDetail();
    }
});

onUnload(() => {
    if (autoPlayTimer) {
        clearTimeout(autoPlayTimer);
        autoPlayTimer = null;
    }
    // 仅当用户在详情中确实操作过点赞，且最终状态与进入时不同，返回列表才刷新
    if (hasLikeToggled.value && isLikeDirty.value) {
        uni.setStorageSync(BUYER_SHOW_LIST_NEED_REFRESH_KEY, 1);
    }
});

// 页面隐藏时暂停所有视频
onHide(() => {
    if (autoPlayTimer) {
        clearTimeout(autoPlayTimer);
        autoPlayTimer = null;
    }
    pauseAllVideos();
});
</script>

<style scoped lang="scss">
.swiper__item {
    width: 100%;
}

.content-wrapper {
    width: 100%;
    height: 100%;
}

.content-image,
.content-video {
    width: 100%;
    height: 100%;
}

.indicator {
    display: flex;
    align-items: center;
    gap: 10rpx;

    .indicator__dot {
        width: 11rpx;
        height: 11rpx;
        background: #e6e6e6;
        border-radius: 50%;
    }

    .indicator__dot--active {
        width: 11rpx;
        height: 11rpx;
        background: #ff4444;
    }
}

:deep(.u-swiper__wrapper),
:deep(.u-swiper__wrapper__item__wrapper),
:deep(.u-swiper__wrapper__item__wrapper__image),
:deep(.u-swiper__wrapper__item__wrapper__video) {
    height: 100% !important;
}
.container {
    padding: 20rpx;
    box-sizing: border-box;
    background: #fff;
    .content {
        font-weight: 500;
        font-size: 28rpx;
        color: #2a2a2a;
        line-height: 42rpx;
        margin-bottom: 44rpx;
        word-break: break-all;
    }
    .addTime {
        font-weight: 500;
        font-size: 24rpx;
        color: #9a9a9a;
        line-height: 42rpx;
        margin-bottom: 44rpx;
    }
}
.userBox {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-top: 40rpx;
    margin-bottom: 50rpx;
    .content-image {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        overflow: hidden;
    }
    .userName {
        font-weight: bold;
        font-size: 32rpx;
        color: #000000;
    }
}

.productListBox {
    display: flex;
    flex-wrap: nowrap;
    gap: 30rpx;
    .productListInfo {
        display: flex;
        width: 600rpx;
        flex-shrink: 0;
        gap: 10rpx;
        background: #fafafa;
        padding: 20rpx 17rpx;
        .productImg {
            position: relative;
            width: 200rpx;
            height: 200rpx;
            flex-shrink: 0;
            .outsale {
                position: absolute;
                top: 50%;
                left: 50%;
                width: 70%;
                height: 70%;
                //line-height: 180rpx;
                transform: translate(-50%, -50%);
                background: rgba(0, 0, 0, 0.4);
                font-size: 24rpx;
                color: #ffffff;
                letter-spacing: 0;
                border-radius: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9;
            }
            .product-style-border {
                position: absolute;
                left: 0;
                top: 0;
                width: 100%;
                height: 100%;
                z-index: 5;
                pointer-events: none;
            }
        }
        .productRight {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            .title {
                font-weight: bold;
                font-size: 32rpx;
                color: #000000;
                // 添加以下样式实现两行省略号
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                overflow: hidden;
                text-overflow: ellipsis;
                word-wrap: break-word;
                word-break: break-all;
            }
            .price {
                font-size: 32rpx;
                color: var(--price);
            }
        }
    }
}

.footer {
    //line-height: 100rpx;
    display: flex;
    align-items: center;
    padding: 15rpx 25rpx;
    box-sizing: border-box;
    .input {
        flex: 3;
        background: #f5f5f5;
    }
    .countBox {
        flex: 2;
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: space-around;
        .LikeBox {
            display: flex;
            align-items: center;
            gap: 20rpx;
        }
    }
}

.icon-yiguanzhu {
    color: var(--general);
}

:deep(.u-swiper__wrapper__item__wrapper__image) {
    & > div {
        background-size: 100% !important;
    }
}

.media-preview-wrapper,
.media-preview-image,
.media-preview-video {
    width: 100%;
    height: 100%;
}

.swiper-item-root {
    width: 100%;
    height: 100%;
}

.media-preview-image {
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center;
    object-fit: cover;
}

.icon-pinglun {
    font-size: 32rpx !important;
}

.swiper-skeleton {
    background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 50%, #f5f5f5 100%);
    background-size: 200% 200%;
    animation: skeleton-loading 1.5s ease infinite;
    display: flex;
    align-items: center;
    justify-content: center;

    .skeleton-loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 20rpx;

        .skeleton-icon {
            font-size: 80rpx;
            color: #ccc;
        }
    }
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}

.swiperBox {
    background-color: #f5f5f5 !important;
    border-radius: 0 !important;
}
</style>
