<template>
    <view class="product-comment-card" @click="toComment">
        <view class="comment-title flex justify-between align-center">
            <view class="txt">
                {{ $t("评价") }}<text>({{ comment.total ?? 0 }})</text>
            </view>
            <view class="more" @click.stop="toPage(`/pages/product/comment?id=${productId}`)">
                {{ $t("好评") }}<text>{{ comment.goodPercent ?? 0 }}%</text>
                <view class="more-arrows"><text class="iconfont-h5 icon-gengduo1" style="font-size: 12px; color: #bfbfbf" /></view>
            </view>
        </view>
        <block v-if="commentList.length > 0">
            <view v-for="(item, index) in commentList.slice(0, 2)" :key="index" class="comment-item">
                <view class="user-info flex justify-between align-center">
                    <view class="user-name flex align-center">
                        <view class="head_logo">
                            <image :src="imageFormat(item?.avatar || '')" mode="aspectFill" />
                        </view>
                        <view>
                            <text>{{ item.username }}</text>
                            <uni-rate :readonly="true" :value="item.commentRank" size="14" color="#bfbfbf" active-color="#ffa800" />
                        </view>
                    </view>
                    <view class="time">{{ item.addTime }}</view>
                </view>
                <view class="user-content">
                    {{ item.content }}
                </view>
                <view class="user-imgs flex-wrap align-center">
                    <image
                        v-for="(pic, index) in item.showPics"
                        :key="index"
                        :src="imageFormat(pic.picThumb)"
                        @click="imagePreview(imageFormat(pic?.picUrl || ''))"
                    />
                </view>
            </view>
        </block>
        <block v-else>
            <view class="no-data"> {{ $t("暂无评价") }}~ </view>
        </block>

        <view class="comment-btn-box flex align-center justify-around">
            <view class="btn flex align-center" @click.stop="toPage(`/pages/product/comment?id=${productId}`)">
                <text class="iconfont-h5 icon-chat" style="font-size: 36rpx; height: 26rpx; color: #333" />
                <text>{{ $t("全部评价") }}({{ comment.total ?? 0 }})</text>
            </view>
            <view class="btn flex align-center" @click.stop="toConsultation">
                <text class="iconfont-h5 icon-chatboxes" style="font-size: 36rpx; height: 26rpx; color: #333" />
                <text>{{ $t("购买咨询") }}({{ consultationTotal ?? 0 }})</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 商品评论/咨询Tab组件
 * - Tab切换展示评论和咨询
 * - 评论显示用户头像、内容、图片、星级
 * - 咨询显示问答内容
 */
import { ref, watch } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource } from "@/utils";
import { onLoad } from "@dcloudio/uni-app";
import { getComment, getCommentList, getProductConsultationList } from "@/api/product/product";
import type { CommentDetail, CommentItem, ProductConsultationItem } from "@/types/product/product";
interface Props {
    productId: number;
    shopId?: number;
}
const props = defineProps<Props>();
const loading = ref(false);
const comment = ref<CommentDetail>({});
const commentList = ref<CommentItem[]>([]);
const page = ref<number>(1);
const size = ref(10);
const toComment = () => {
    if (comment.value.total && comment.value.total > 0) {
        uni.navigateTo({
            url: `/pages/product/comment?id=${props.productId}`
        });
    }
};
const toPage = (url: string) => {
    uni.navigateTo({
        url: url
    });
};
const toConsultation = () => {
    if (props.productId <= 0 || props.shopId === undefined) return;
    toPage(`/pages/product/consult?id=${props.productId}&shopId=${props.shopId}`);
};
const imagePreview = (url: string) => {
    uni.previewImage({
        urls: [url]
    });
};
const _getComment = async (id: any) => {
    loading.value = true;
    try {
        const result = await getComment(id);
        comment.value = result;
    } catch (error) {
        console.log(error);
    } finally {
        loading.value = false;
    }
};
const _getCommentList = async (id: any) => {
    loading.value = true;
    try {
        const result = await getCommentList(id, {
            type: 1,
            page: page.value
        });
        commentList.value = result.records;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        loading.value = false;
    }
};
const consultationList = ref<ProductConsultationItem[]>([]);
const consultationTotal = ref(0);
const fetchConsultation = async (productId: number, shopId: number) => {
    try {
        const result = await getProductConsultationList({ productId, shopId, page: page.value, size: size.value });
        consultationList.value = result.records;
        consultationTotal.value = result.total;
    } catch (error: any) {
        console.log(error);
    } finally {
        loading.value = false;
    }
};
onLoad((option) => {
    if (option) {
        const { id } = option;
        if (id) {
            _getComment(id);
            _getCommentList(id);
        }
    }
});
watch(
    () => [props.productId, props.shopId] as const,
    ([productId, shopId]) => {
        if (productId > 0 && shopId !== undefined) {
            fetchConsultation(productId, shopId);
        }
    },
    { immediate: true }
);
</script>

<style lang="scss" scoped>
.product-comment-card {
    background-color: var(--product-content-bg, #fff);
    padding: 20rpx;
    border-radius: 20rpx;
    margin: 20rpx 0;
    .comment-title {
        padding-bottom: 20rpx;
        border-bottom: 1rpx solid #eee;
        .txt {
            font-size: 26rpx;
            font-weight: bold;
            text {
                margin-left: 5rpx;
                font-size: 22rpx;
                color: #c3c0c0;
            }
        }
        .more {
            font-size: 26rpx;
            align-items: center;
            display: flex;

            text {
                color: var(--general);
            }

            .more-arrows {
                position: relative;
                top: 0;
            }
        }
    }
    .comment-item {
        margin: 20rpx 0;
        border-bottom: 1rpx solid #eee;
        padding-bottom: 20rpx;
        .user-info {
            .user-name {
                font-size: 24rpx;
                .head_logo {
                    width: 60rpx;
                    height: 60rpx;
                    margin-right: 20rpx;
                    border-radius: 50%;
                    overflow: hidden;
                    flex-shrink: 0;
                    image {
                        width: 100%;
                        height: 100%;
                        display: block;
                    }
                }
            }
            .time {
                font-size: 24rpx;
                color: #c3c0c0;
            }
        }
        .user-content {
            margin: 20rpx 0;
            font-size: 26rpx;
        }
        .user-imgs {
            image {
                width: 90rpx;
                height: 90rpx;
                margin-right: 10rpx;
                margin-bottom: 10rpx;
                border: 1px solid #eee;
                border-radius: 5rpx;
            }
        }
    }
    .comment-btn-box {
        padding: 10rpx;
        .btn {
            color: #333;
            font-size: 24rpx;
            padding: 8rpx 30rpx;
            border: 1px solid #c3c0c0;
            border-radius: 36px;
        }
    }
    .no-data {
        font-size: 26rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 40rpx 0;
    }
}
</style>
