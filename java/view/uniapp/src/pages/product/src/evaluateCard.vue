<template>
    <view class="evaluate-card-box" :class="{ 'evaluate-card-box--overseas': overseas }" @click="handleToComment(hasComment)">
        <view class="evaluate-card">
            <view class="evaluate-card-header" :class="{ empty: !hasComment }">
                <view class="evaluate-title">
                    <view class="title-left">
                        <view class="title-text">{{ $t("评价") }}</view>
                        <view class="title-desc">
                            <image :src="staticResource('common/safety.png')" />
                            <text class="title-desc-text line1">{{ $t("真实用户评价") }}</text>
                        </view>
                    </view>
                    <view class="evaluate-title-right" @click.stop="handleToComment(hasComment)">
                        <text class="title-right-text">{{ $t(hasComment ? "全部" : "暂时无评论") }}</text>
                        <text v-if="hasComment" class="iconfont-h5 icon-a-gengduo20" />
                    </view>
                </view>
                <view v-if="hasComment" class="evaluate-score-box">
                    <view class="evaluate-score">
                        <uni-rate :readonly="true" :value="score" size="16" color="#e0e1e5" active-color="#ffb21c" />
                        <view class="score-num">{{ score }}</view>
                        <view class="comment-total">({{ Number(commentInfo.total) || 0 }} {{ $t("条评价") }})</view>
                    </view>
                </view>
            </view>
            <view v-if="hasComment && commentList.length > 0" class="evaluate-items">
                <commentItem v-for="(item, index) in commentList" :key="index" class="evaluate-item" :item="item" />
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from "vue";
import commentItem from "./commentItem.vue";
import { getComment, getCommentList } from "@/api/product/product";
import type { CommentDetail, CommentItem } from "@/types/product/product";
import { staticResource } from "@/utils";

const props = defineProps({
    productId: {
        type: Number,
        required: true
    },
    overseas: {
        type: Boolean,
        default: false
    }
});

const commentInfo = ref<CommentDetail>({ total: 0 });
const commentList = ref<CommentItem[]>([]);
const hasComment = computed(() => Number(commentInfo.value.total) > 0);
const score = computed(() => {
    const value = Math.max(0, Math.min(100, Number(commentInfo.value.goodPercent) || 0)) / 20;
    return Number(value.toFixed(1));
});

const fetchComment = async () => {
    try {
        commentInfo.value = await getComment(props.productId);
    } catch (error) {
        console.error(error);
    }
};

const fetchCommentList = async () => {
    try {
        const result = await getCommentList(props.productId, { type: 1, page: 1 });
        commentList.value = result.records.slice(0, 2);
    } catch (error) {
        console.error(error);
    }
};

watchEffect(() => {
    if (props.productId) {
        fetchComment();
        fetchCommentList();
    }
});

const handleToComment = (isHasComment: boolean) => {
    if (isHasComment) {
        uni.navigateTo({ url: `/pages/product/comment?id=${props.productId}` });
    }
};
</script>

<style lang="scss" scoped>
.icon-a-gengduo20 {
    display: flex;
    align-items: center;
    color: #888b94;
    font-size: 16rpx;
}

.evaluate-card-box {
    padding: 28rpx;
    margin-bottom: 14rpx;
    background-color: var(--product-content-bg, #fff);

    &.evaluate-card-box--overseas {
        margin-bottom: 0;
        border-top: 14rpx solid var(--product-page-bg, #f5f5f5);
        border-radius: 0;
    }

    .evaluate-card-header {
        margin-bottom: 14rpx;
        border-bottom: 2rpx solid rgba(0, 0, 0, 0.059);

        &.empty {
            margin-bottom: 0;
            border-bottom: none;
        }

        .evaluate-title {
            display: flex;
            align-items: baseline;
            justify-content: space-between;

            .title-left {
                display: flex;
                align-items: center;
                min-width: 0;
                margin-right: 11rpx;
                overflow: hidden;
                gap: 11rpx;

                .title-text {
                    font-size: 28rpx;
                    font-weight: 700;
                    line-height: 35rpx;
                }

                .title-desc {
                    display: flex;
                    flex: 1;
                    align-items: center;
                    overflow: hidden;

                    image {
                        width: 19rpx;
                        height: 23rpx;
                    }

                    .title-desc-text {
                        display: inline-block;
                        flex: 1;
                        width: calc(100% - 37rpx);
                        margin-left: 9rpx;
                        color: #888b94;
                        font-size: 21rpx;
                        line-height: 28rpx;
                    }
                }
            }

            .evaluate-title-right {
                display: flex;
                align-items: center;
                gap: 7rpx;

                .title-right-text {
                    color: #888b94;
                    font-size: 21rpx;
                    line-height: 28rpx;
                    white-space: nowrap;
                }
            }
        }

        .evaluate-score-box {
            display: flex;
            align-items: center;
            width: 100%;
            padding: 21rpx 0 28rpx;

            .evaluate-score {
                display: flex;
                align-items: center;
                margin-bottom: 21rpx;

                .score-num {
                    padding-left: 7rpx;
                    font-size: 28rpx;
                    font-weight: 700;
                    line-height: 35rpx;
                }

                .comment-total {
                    margin: 0 11rpx;
                    color: #888b94;
                    font-size: 21rpx;
                }
            }
        }
    }

    .evaluate-items .evaluate-item:last-child {
        padding-bottom: 0;

        :deep(.comment-con .con-text) {
            margin-bottom: 0;
        }
    }
}
</style>
