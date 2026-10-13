<template>
    <view class="comment-item">
        <view class="comment-item-header">
            <view class="header-left">
                <view class="header-picture">
                    <image :src="imageFormat(item.avatar || '')" />
                </view>
                <view class="user-info">
                    <view class="header-name">{{ hideUserName(item.username || $t("匿名用户")) }}</view>
                </view>
            </view>
            <view class="header-time">{{ item.addTime }}</view>
        </view>
        <view class="comment-con">
            <view class="con-text">{{ item.content }}</view>
        </view>
    </view>
</template>

<script setup lang="ts">
import type { CommentItem } from "@/types/product/product";
import { hideUserName, imageFormat } from "@/utils/format";

defineProps({
    item: {
        type: Object as () => CommentItem,
        required: true
    }
});
</script>

<style lang="scss" scoped>
.comment-item {
    width: 100%;
    padding: 28rpx 0;
    background-color: var(--product-content-bg, #fff);

    .comment-item-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;

        .header-left {
            display: flex;
            column-gap: 14rpx;

            .header-picture {
                overflow: hidden;
                background-color: #e1e3e6;
                border-radius: 50%;

                image {
                    display: block;
                    width: 70rpx;
                    height: 70rpx;
                }
            }

            .user-info {
                display: flex;
                flex-direction: column;
                justify-content: space-between;

                .header-name {
                    color: #1a1a1a;
                    font-size: 25rpx;
                    line-height: 32rpx;
                }
            }
        }

        .header-time {
            color: #888b94;
            font-size: 21rpx;
            line-height: 28rpx;
        }
    }

    .comment-con .con-text {
        display: -webkit-box;
        overflow: hidden;
        margin: 25rpx 0;
        color: #1a1a1a;
        font-size: 23rpx;
        line-height: 32rpx;
        text-overflow: ellipsis;
        white-space: pre-wrap;
        word-break: break-word;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 3;
    }
}
</style>
