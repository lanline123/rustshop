<template>
    <view>
        <view class="commentsTotal">{{ $t("共") }}{{ total }}{{ $t("条评论") }}</view>
        <Comment
            v-model:comments="buyerShowCommentListData"
            v-model:reply-list-data="replyListData"
            :loading="loading"
            :has-more="hasMore"
            @like-comment="handleLikeComment"
            @like-reply="handleLikeReply"
            @submit-reply="handleSubmitReply"
            @submit-reply-to="handleSubmitReplyTo"
            @load-more="loadMoreComments"
            @show-reply-input="showReplyInput"
            @get-reply-list="getReplyList"
        />
        <tig-loadingpage v-model="isLoading" />
        <loading-box v-model="isLoadMore" :page="filterParams.page" :length="buyerShowCommentListData.length" />
    </view>
</template>

<script setup lang="ts">
/**
 * 买家秀评论组件
 * - 展示评论列表
 * - 支持评论点赞
 * - 分页加载更多评论
 */
import { ref, defineExpose, defineEmits } from "vue";
import Comment from "./buyerShowCompnents/CommentList.vue";
import { onLoad } from "@dcloudio/uni-app";
import { buyerShowCommentLike, buyerShowCommentList } from "@/api/buyerShow/buyerShow";
import type { BuyerShowCommentListFilter, BuyerShowCommentListParams } from "@/types/buyerShow/buyerShow";
import { useList } from "@/hooks";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const emits = defineEmits(["showReplyInput", "getCommentList"]);

const buyerShowId = ref("");

const filterParams = ref<BuyerShowCommentListParams>({
    page: 1,
    size: 10,
    parentId: 0
});

const filterReplyParams = ref<BuyerShowCommentListParams>({
    page: 1,
    size: 3,
    parentId: 0
});
// 获取一级回复列表
const {
    getList,
    data: buyerShowCommentListData,
    isLoading,
    isLoadMore,
    total
} = useList<BuyerShowCommentListFilter>(buyerShowCommentList, {
    params: filterParams,
    path: {
        dataKey: "records"
    },
    immediate: false
});
// 获取二级回复列表
const { getList: replyList, data: replyListData } = useList<BuyerShowCommentListFilter>(buyerShowCommentList, {
    params: filterReplyParams,
    path: {
        dataKey: "records"
    },
    immediate: false,
    needReachBottom: false
});

defineExpose({
    getList,
    total
});

onLoad((options: any) => {
    buyerShowId.value = options.buyerShowId;
    if (buyerShowId.value) {
        getList({ buyerShowId: buyerShowId.value, parentId: 0 });
    }
});

const showReplyInput = (data: any) => {
    emits("showReplyInput", data);
};

const getReplyList = async (comment: any) => {
    const res: any = await replyList({ buyerShowId: buyerShowId.value, parentId: comment.buyerShowCommentId, size: 3, page: Number(comment.page || 1) });
    (buyerShowCommentListData.value[comment.index] as any).replies = replyListData.value;
    (buyerShowCommentListData.value[comment.index] as any).page = (comment.page || 1) + 1;
    // 判断是否还要加载
    if (res.current * res.size < res.total) {
        (buyerShowCommentListData.value[comment.index] as any).toggleText = `${t("展开")}${res.total - res.current * res.size}${t("条回复")}`;
        console.log(buyerShowCommentListData.value[comment.index], "buyerShowCommentListData.value[comment.index]");
    } else {
        (buyerShowCommentListData.value[comment.index] as any).toggleText = t("收起");
    }
};

const loading = ref(false);
const hasMore = ref(true);

const handleLikeComment = (data: any) => {
    // 处理评论点赞
    likeComment({ ...data, level: 1 });
};

const handleLikeReply = (data: any) => {
    // 处理回复点赞
    likeComment({ ...data, level: 2 });
};

const likeComment = async (data: any) => {
    // 处理评论点赞
    try {
        await buyerShowCommentLike({
            buyerShowCommentId: data.buyerShowCommentId,
            isLike: !data.isLike ? 1 : 0
        });
        uni.showToast({
            title: !data.isLike ? t("点赞成功") : t("取消点赞"),
            icon: "none"
        });
        if (data.level === 1) {
            const index = buyerShowCommentListData.value.findIndex((item: any) => item.buyerShowCommentId === data.buyerShowCommentId);
            if (index === -1) return;
            (buyerShowCommentListData.value[index] as any).isLike = !data.isLike;
            (buyerShowCommentListData.value[index] as any).likeCount = !data.isLike ? data.likeCount + 1 : data.likeCount - 1;
            // getList({ buyerShowId: buyerShowId.value, parentId: 0 });
        } else {
            const index = replyListData.value.findIndex((item: any) => item.buyerShowCommentId === data.buyerShowCommentId);
            if (index === -1) return;
            (replyListData.value[index] as any).isLike = !data.isLike;
            (replyListData.value[index] as any).likeCount = !data.isLike ? data.likeCount + 1 : data.likeCount - 1;
            // getReplyList({ buyerShowId: buyerShowId.value, parentId: data.buyerShowCommentId });
        }
    } catch (error: any) {
        uni.showToast({
            title: error?.message
        });
    }
};

const handleSubmitReply = (comment: any, content: string) => {
    // 处理提交回复
    if (!comment.replies) {
        comment.replies = [];
    }

    comment.replies.push({
        id: Date.now(),
        username: t("当前用户"),
        content: content,
        createTime: Date.now(),
        likeCount: 0,
        isLiked: false
    });
};

const handleSubmitReplyTo = (comment: any, reply: any, content: string) => {
    // 处理回复某人
    if (!comment.replies) {
        comment.replies = [];
    }

    comment.replies.push({
        id: Date.now(),
        username: t("当前用户"),
        content: content,
        createTime: Date.now(),
        likeCount: 0,
        isLiked: false,
        replyTo: reply.username
    });
};

const loadMoreComments = () => {
    // 加载更多评论
    if (loading.value || !hasMore.value) return;

    loading.value = true;
    // 模拟加载数据
    setTimeout(() => {
        // 添加更多评论数据
        loading.value = false;
    }, 1000);
};
</script>

<style scoped lang="scss"></style>
