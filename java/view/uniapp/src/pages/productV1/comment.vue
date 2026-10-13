<template>
    <tig-layout :title="$t('商品评价')">
        <view class="product-comment-card">
            <view v-for="(item, index) in commentList" :key="index" class="comment-item">
                <view class="user-info flex justify-between align-center">
                    <view class="user-name flex align-center">
                        <view class="head_logo">
                            <image :src="imageFormat(item?.avatar || '')" />
                        </view>
                        <view>
                            <text>{{ item.username }}</text>
                            <uni-rate :value="item.commentRank" :readonly="true" size="14" color="#bfbfbf" active-color="#ffa800" />
                        </view>
                    </view>
                    <view class="time">{{ item.addTime }}</view>
                </view>
                <view class="tags flex-wrap">
                    <view v-for="(tag, tagIndex) in item.commentTag" :key="tagIndex" class="tag">{{ tag }}</view>
                </view>
                <view class="user-content">
                    {{ item.content }}
                </view>
                <view class="user-imgs flex-wrap align-center">
                    <image
                        v-for="(pic, picIndex) in item.showPics"
                        :key="picIndex"
                        mode="aspectFit"
                        :src="imageFormat(pic.picThumb)"
                        @click="imagePreview(imageFormat(pic?.picUrl || ''))"
                    />
                </view>
                <view v-if="item.replies && item.replies.length > 0" class="reply-section">
                    <view class="reply-card">
                        <view :id="'reply-m-' + item.commentId" class="reply-measure">
                            <text>{{ $t("客服回复") }}：{{ item.replies[0]?.content }}</text>
                        </view>
                        <view :id="'reply-b-' + item.commentId" class="reply-body" :class="{ 'is-collapsed': !expandedReplies.has(item.commentId!) }">
                            <text class="reply-label">{{ $t("客服回复") }}：</text>
                            <text class="reply-text">{{ item.replies[0]?.content }}</text>
                            <text
                                v-if="overflowReplies.has(item.commentId!) && expandedReplies.has(item.commentId!)"
                                class="reply-toggle"
                                @click.stop="toggleReplies(item.commentId!)"
                                >{{ $t("收起") }}</text
                            >
                        </view>
                        <view
                            v-if="overflowReplies.has(item.commentId!) && !expandedReplies.has(item.commentId!)"
                            class="reply-expand"
                            @click.stop="toggleReplies(item.commentId!)"
                        >
                            <text class="reply-expand-text">...</text> {{ $t("展开") }}</view
                        >
                    </view>
                </view>
            </view>
        </view>
        <view v-if="page > 1" class="loading-box">
            <view v-if="loading" class="bottomLoading"><image lazy-load class="loading" :src="staticResource('common/loading.gif')" /></view>
        </view>
        <view class="loading-box">
            <view v-if="commentList.length < 1">{{ $t("暂无评价") }}</view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 商品评论列表页
 * - 分页展示商品的所有用户评论
 * - 支持按好评/中评/差评/有图筛选
 * - 显示评论内容、图片、追评和商家回复
 */
import { ref, nextTick } from "vue";
import { imageFormat } from "@/utils/format";
import { onLoad, onReachBottom, onUnload } from "@dcloudio/uni-app";
import { getCommentList } from "@/api/product/product";
import type { CommentItem } from "@/types/product/product";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const expandedReplies = ref<Set<number>>(new Set());
const overflowReplies = ref<Set<number>>(new Set());

const toggleReplies = (commentId: number) => {
    const next = new Set(expandedReplies.value);
    if (next.has(commentId)) {
        next.delete(commentId);
    } else {
        next.add(commentId);
    }
    expandedReplies.value = next;
};

const checkReplyOverflow = () => {
    nextTick(() => {
        setTimeout(() => {
            const items = commentList.value.filter((item) => item.replies?.length && item.commentId);
            if (items.length === 0) return;
            const newSet = new Set<number>(overflowReplies.value);
            let count = 0;
            items.forEach((item) => {
                const id = item.commentId!;
                const query = uni.createSelectorQuery();
                query.select(`#reply-m-${id}`).boundingClientRect();
                query.select(`#reply-b-${id}`).boundingClientRect();
                query.exec((res: any[]) => {
                    const measureRect = res[0];
                    const bodyRect = res[1];
                    if (measureRect && bodyRect && measureRect.height > bodyRect.height + 1) {
                        newSet.add(id);
                    }
                    count++;
                    if (count === items.length) {
                        overflowReplies.value = newSet;
                    }
                });
            });
        }, 200);
    });
};

const loading = ref(false);
const commentList = ref<CommentItem[]>([]);
const page = ref<number>(1);
const size = ref(10);
const total = ref(0);
const loaded = ref(false);
const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [url]
    });
};
const _getCommentList = async () => {
    if (page.value > 1) {
        loaded.value = true;
    }
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getCommentList(productId.value, {
            type: 1,
            size: size.value,
            page: page.value
        });
        commentList.value = [...commentList.value, ...result.records];
        total.value = result.total;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        loaded.value = false;
        uni.hideLoading();
        checkReplyOverflow();
    }
};
const productId = ref<any>("");
onLoad((option) => {
    if (option) {
        const { id } = option;
        if (id) {
            productId.value = id;
            _getCommentList();
        }
    }
});
onReachBottom(() => {
    if (page.value < Math.ceil(total.value / size.value)) {
        page.value++;
        _getCommentList();
    }
});

// 关闭预览图片
onUnload(() => {
    // #ifdef  H5 || APP
    uni.closePreviewImage();
    // #endif
});
</script>

<style lang="scss" scoped>
.product-comment-card {
    background-color: #fff;
    padding: 20rpx 20rpx 0 20rpx;
    border-top: 1rpx solid #eee;
    .comment-item {
        border-bottom: 1rpx solid #eee;
        padding-bottom: 20rpx;
        padding-top: 20rpx;
        .user-info {
            .user-name {
                font-size: 24rpx;
                .head_logo {
                    margin-right: 20rpx;
                    image {
                        width: 60rpx;
                        height: 60rpx;
                        border-radius: 100rpx;
                    }
                }
            }
            .time {
                font-size: 24rpx;
                color: #c3c0c0;
            }
        }
        .tags {
            margin-top: 20rpx;
            .tag {
                background: #eee none repeat scroll 0 0;
                margin-bottom: 12rpx;
                margin-right: 8rpx;
                padding: 6rpx 12rpx;
                border-radius: 2px;
                font-size: 11px;
                color: #666;
            }
        }
        .user-content {
            margin: 20rpx 0;
            font-size: 26rpx;
        }
        .user-imgs {
            image {
                width: 150rpx;
                height: 150rpx;
                margin-right: 15rpx;
                margin-bottom: 15rpx;
                border: 1px solid #eee;
            }
        }
        .reply-section {
            margin-top: 15rpx;
        }
        .reply-card {
            position: relative;
            background-color: #f7f7f7;
            border-radius: 12rpx;
            padding: 20rpx 24rpx;
            font-size: 24rpx;
            line-height: 1.6;

            .reply-measure {
                position: absolute;
                left: 24rpx;
                right: 24rpx;
                visibility: hidden;
                pointer-events: none;
            }

            .reply-body {
                word-wrap: break-word;

                &.is-collapsed {
                    max-height: 1.6em;
                    overflow: hidden;
                }
            }

            .reply-label {
                color: #999;
            }

            .reply-text {
                word-wrap: break-word;
            }

            .reply-toggle {
                color: #000;
                margin-left: 10rpx;
            }

            .reply-expand {
                position: absolute;
                right: 24rpx;
                bottom: 20rpx;
                color: #000;
                background: #f7f7f7;
                padding-left: 0;
                .reply-expand-text {
                    color: #333;
                }
            }
        }
    }
}
.loading-box {
    padding: 30rpx;
    font-size: 24rpx;
}
</style>
