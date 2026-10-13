<template>
    <tig-popup v-model:show="show" position="bottom" :round="16" :show-close="false" background-color="#fff">
        <view class="comment-sheet" :style="sheetStyle">
            <view class="sheet-header">
                <text class="title">{{ isOverseas() ? $t("共{0}条评论", [displayTotal]) : `共${displayTotal}条评论` }}</text>
                <view class="close" @click="show = false">
                    <view class="iconfont-h5 icon-chacha close-icon" />
                </view>
            </view>

            <scroll-view scroll-y="true" enable-flex="true" class="sheet-body" :style="bodyStyle" @scrolltolower="loadMore">
                <view v-for="(item, index) in list" :key="item.buyerShowCommentId || index" class="c-item">
                    <image class="c-avatar" :src="item.buyerPhoto || ''" mode="aspectFill" />
                    <view class="c-main">
                        <view class="c-top">
                            <view class="c-name-box">
                                <text class="c-name">{{ item.buyerName }}</text>
                                <text v-if="Number(item.isMe) === 1" class="c-tag">{{ $t("我") }}</text>
                                <text v-else-if="Number(item.isAuthor) === 1" class="c-tag">{{ $t("作者") }}</text>
                            </view>
                            <view class="c-like" @click="handleLike(item)">
                                <view class="iconfont-h5 c-like-icon" :class="item.isLike ? 'icon-yiguanzhu is-like' : 'icon-guanzhu'" />
                                <text class="c-like-num">{{ Number(item.likeCount) > 0 ? item.likeCount : "" }}</text>
                            </view>
                        </view>
                        <text class="c-content">{{ item.content }}</text>
                        <view v-if="item.images && item.images.length" class="c-images">
                            <image
                                v-for="(img, i) in item.images"
                                :key="i"
                                class="c-img"
                                :src="img"
                                mode="aspectFill"
                                @click="previewImage(item.images as string[], img)"
                            />
                        </view>
                        <view class="c-bottom">
                            <text class="c-time">{{ item.addTime }}</text>
                            <text class="c-reply" @click="handleReply(item)">{{ $t("回复") }}</text>
                        </view>

                        <view v-if="item.showAllReplies && item.replies && item.replies.length" class="replies">
                            <view v-for="(reply, rIndex) in item.replies" :key="reply.buyerShowCommentId || rIndex" class="reply-item">
                                <image class="c-avatar c-avatar--sm" :src="reply.buyerPhoto || ''" mode="aspectFill" />
                                <view class="c-main">
                                    <view class="c-top">
                                        <view class="c-name-box">
                                            <text class="c-name">{{ reply.buyerName }}</text>
                                            <text v-if="Number(reply.isMe) === 1" class="c-tag">{{ $t("我") }}</text>
                                            <text v-else-if="Number(reply.isAuthor) === 1" class="c-tag">{{ $t("作者") }}</text>
                                        </view>
                                        <view class="c-like" @click="handleLike(reply)">
                                            <view class="iconfont-h5 c-like-icon" :class="reply.isLike ? 'icon-yiguanzhu is-like' : 'icon-guanzhu'" />
                                            <text class="c-like-num">{{ Number(reply.likeCount) > 0 ? reply.likeCount : "" }}</text>
                                        </view>
                                    </view>
                                    <text class="c-content">{{ reply.content }}</text>
                                    <view class="c-bottom">
                                        <text class="c-time">{{ reply.addTime }}</text>
                                        <text class="c-reply" @click="handleReply(item)">{{ $t("回复") }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>

                        <view v-if="Number(item.replyCount) > 0" class="view-more" @click="handleToggleReplies(item)">
                            <text class="view-more-text">
                                {{ item.toggleText || (isOverseas() ? $t("展开{0}条回复", [item.replyCount]) : `展开${item.replyCount}条回复`) }}
                            </text>
                        </view>
                    </view>
                </view>

                <view v-if="loading" class="c-tip">{{ $t("加载中...") }}</view>
                <view v-else-if="!hasMore && list.length > 0" class="c-tip">{{ $t("没有更多评论了") }}</view>
                <view v-else-if="!loading && list.length === 0" class="c-empty">{{ $t("暂无评论") }}</view>
            </scroll-view>

            <view class="sheet-footer">
                <up-input
                    ref="inputRef"
                    v-model="content"
                    class="footer-input"
                    shape="circle"
                    :placeholder="replyPlaceholder"
                    placeholder-style="font-size: 26rpx;"
                    :focus="isFocus"
                    border="surround"
                    :cursor-spacing="60"
                    :hold-keyboard="true"
                    @focus="isFocus = true"
                    @blur="isFocus = false"
                    @confirm="handleSend"
                />
                <view class="footer-send" :class="{ active: content }" @click="handleSend">{{ $t("发送") }}</view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 买家秀视频流评论弹层
 * - 一级评论分页加载，二级回复按需展开
 * - 点赞、回复、发送均复用 useVideoComments
 */
import { computed, nextTick, ref, watch } from "vue";

import type { VideoFeedCommentRow } from "@/types/buyerShow/videoFeed";
import { isOverseas } from "@/utils";
import { useVideoComments } from "../composables/useVideoComments";

const show = defineModel<boolean>("show", { default: false });
const props = defineProps<{
    buyerShowId: string | number;
    commentCount?: number;
}>();

const emit = defineEmits<{
    "update:commentCount": [count: number];
}>();

const { list, total, loading, hasMore, replyPlaceholder, reload, loadMore, like, send, setReplyTarget, clearReplyTarget, toggleReplies } = useVideoComments();
const content = ref("");
const inputRef = ref<{ onFocus?: () => void; doFocus?: () => void } | null>(null);
const isFocus = ref(false);

const displayTotal = computed(() => total.value || props.commentCount || 0);

const windowHeight = uni.getSystemInfoSync().windowHeight || 667;
const sheetHeight = Math.round(windowHeight * 0.7);
const sheetStyle = { height: `${sheetHeight}px` };
const bodyStyle = { flex: 1, height: "0px", minHeight: "0px" };

const focusInput = () => {
    isFocus.value = false;
    nextTick(() => {
        inputRef.value?.onFocus?.();
        inputRef.value?.doFocus?.();
        setTimeout(() => {
            isFocus.value = true;
        }, 100);
    });
};

watch(
    () => show.value,
    (visible) => {
        if (visible) {
            reload(props.buyerShowId);
        } else {
            clearReplyTarget();
            content.value = "";
            isFocus.value = false;
        }
    },
    { immediate: true }
);

const handleLike = (item: VideoFeedCommentRow) => like(item);

const handleReply = (item: VideoFeedCommentRow) => {
    setReplyTarget(item);
    focusInput();
};

const handleToggleReplies = (item: VideoFeedCommentRow) => toggleReplies(item);

const handleSend = async () => {
    const ok = await send(content.value);
    if (ok) {
        content.value = "";
        emit("update:commentCount", total.value);
    }
};

const previewImage = (images: string[], current: string) => {
    uni.previewImage({ urls: images, current, indicator: "number", loop: true });
};
</script>

<style scoped lang="scss">
.comment-sheet {
    display: flex;
    flex-direction: column;
    width: 100%;
    background: #fff;
}

.sheet-header {
    position: relative;
    flex-shrink: 0;
    height: 90rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1rpx solid #f5f5f5;
}

.title {
    font-weight: 500;
    font-size: 28rpx;
    color: #2a2a2a;
}

.close {
    position: absolute;
    right: 30rpx;
    top: 50%;
    transform: translateY(-50%);
    padding: 10rpx;
}

.close-icon {
    color: #333333;
    font-size: 32rpx;
    line-height: 1;
}

.sheet-body {
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    width: 100%;
    padding: 20rpx 30rpx;
}

.c-item {
    display: flex;
    flex-direction: row;
    width: 100%;
    margin-bottom: 36rpx;
}

.c-avatar {
    flex-shrink: 0;
    width: 70rpx;
    height: 70rpx;
    margin-right: 20rpx;
    border-radius: 50%;
    background: #f0f0f0;
}

.c-avatar--sm {
    width: 48rpx;
    height: 48rpx;
}

.c-main {
    flex: 1;
    width: 0;
}

.c-top {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;
}

.c-name-box {
    display: flex;
    flex-direction: row;
    align-items: center;
    flex: 1;
    width: 0;
}

.c-name {
    font-size: 26rpx;
    color: #9a9a9a;
}

.c-tag {
    margin-left: 12rpx;
    font-size: 20rpx;
    color: #ff0037;
    background: #feecf0;
    padding: 2rpx 10rpx;
    border-radius: 14rpx;
}

.c-like {
    display: flex;
    flex-direction: row;
    align-items: center;
    flex-shrink: 0;
    margin-left: 16rpx;
}

.c-like-icon {
    color: #999999;
    font-size: 32rpx;
    line-height: 1;

    &.is-like {
        color: #ff4444;
    }
}

.c-like-num {
    margin-left: 8rpx;
    font-size: 24rpx;
    color: #9a9a9a;
}

.c-content {
    display: block;
    font-size: 28rpx;
    color: #2a2a2a;
    line-height: 42rpx;
    word-break: break-all;
}

.c-images {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    margin-top: 16rpx;
}

.c-img {
    width: 220rpx;
    height: 220rpx;
    margin-right: 12rpx;
    margin-bottom: 12rpx;
    border-radius: 12rpx;
    background: #f0f0f0;
}

.c-bottom {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-top: 14rpx;
}

.c-time {
    font-size: 24rpx;
    color: #9a9a9a;
}

.c-reply {
    margin-left: 30rpx;
    font-size: 24rpx;
    color: #576b95;
}

.replies {
    margin-top: 20rpx;
}

.reply-item {
    display: flex;
    flex-direction: row;
    width: 100%;
    margin-bottom: 24rpx;
}

.view-more {
    margin-top: 12rpx;
}

.view-more-text {
    font-size: 24rpx;
    color: #576b95;
}

.c-tip,
.c-empty {
    text-align: center;
    font-size: 24rpx;
    color: #999;
    padding: 30rpx 0;
}

.sheet-footer {
    flex-shrink: 0;
    height: 110rpx;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 0 30rpx;
    box-sizing: border-box;
    border-top: 1rpx solid #f5f5f5;
}

.footer-input {
    flex: 1;
    background: #f5f5f5;
}

.footer-send {
    flex-shrink: 0;
    margin-left: 20rpx;
    font-size: 28rpx;
    color: #bbb;
}

.footer-send.active {
    color: var(--general, #ff4444);
}
</style>
