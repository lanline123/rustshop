import { computed, ref } from "vue";

import { buyerShowComment, buyerShowCommentLike } from "@/api/buyerShow/buyerShow";
import { buyerShowVideoComments } from "@/api/buyerShow/videoFeed";
import i18n from "@/i18n";
import type { VideoFeedCommentResponse, VideoFeedCommentRow, VideoFeedId } from "@/types/buyerShow/videoFeed";
import { getErrorMessage } from "@/utils/error";

// 直接取 i18n 实例而非 useI18n()，App 端 nvue 的 setup 上下文拿不到注入的实例
const t = (key: string, args?: unknown[]): string => (i18n.global as unknown as { t: (k: string, a?: unknown[]) => string }).t(key, args);

// 不走 @/utils 桶，避免 nvue 额外拉进 request/store 循环依赖
const isOverseasEnv = () => import.meta.env.VITE_IS_OVERSEAS == 1;
const replyToText = (name: string) => (isOverseasEnv() ? t("回复{0}", [name]) : `回复${name}`);
const expandRepliesText = (count: number) => (isOverseasEnv() ? t("展开{0}条回复", [count]) : `展开${count}条回复`);

function pickRows(res: VideoFeedCommentResponse | null | undefined): VideoFeedCommentRow[] {
    return res?.records ?? [];
}

/**
 * 买家秀视频流评论逻辑（H5/小程序 commentSheet 与 App nvue 共用）
 * 回复交互对齐详情页：两级扁平，回复二级仍挂在一级评论下。
 */
export function useVideoComments() {
    const list = ref<VideoFeedCommentRow[]>([]);
    const total = ref(0);
    const loading = ref(false);
    const hasMore = ref(true);
    /** 当前回复目标，始终是一级评论 */
    const replyTarget = ref<VideoFeedCommentRow | null>(null);

    const size = 10;
    const replySize = 3;
    let page = 1;
    let buyerShowId: VideoFeedId = "";

    const replyPlaceholder = computed(() => {
        if (replyTarget.value?.buyerName) return replyToText(replyTarget.value.buyerName);
        return t("说点什么吧~");
    });

    /** 加载下一页一级评论 */
    const loadMore = async () => {
        if (loading.value || !hasMore.value || !buyerShowId) return;
        loading.value = true;
        try {
            const res = await buyerShowVideoComments({ buyerShowId, page, size, parentId: 0 });
            list.value.push(...pickRows(res));
            total.value = Number(res?.total) || list.value.length;
            hasMore.value = list.value.length < Number(res?.total || 0);
            page += 1;
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        } finally {
            loading.value = false;
        }
    };

    /** 切换买家秀并重新加载 */
    const reload = (id: VideoFeedId) => {
        buyerShowId = id;
        list.value = [];
        total.value = 0;
        page = 1;
        hasMore.value = true;
        replyTarget.value = null;
        return loadMore();
    };

    /** 评论点赞 / 取消，一级或二级通用 */
    const like = async (item: VideoFeedCommentRow) => {
        const next = item.isLike ? 0 : 1;
        try {
            await buyerShowCommentLike({ buyerShowCommentId: item.buyerShowCommentId, isLike: next });
            item.isLike = next;
            item.likeCount = Math.max(0, (Number(item.likeCount) || 0) + (next ? 1 : -1));
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        }
    };

    /** 选中回复目标，传一级评论；回复二级时也传其所属一级 */
    const setReplyTarget = (comment: VideoFeedCommentRow) => {
        replyTarget.value = comment;
    };

    const clearReplyTarget = () => {
        replyTarget.value = null;
    };

    /** 拉取 / 续拉某条一级评论的二级回复 */
    const loadReplies = async (comment: VideoFeedCommentRow) => {
        if (!buyerShowId || !comment?.buyerShowCommentId) return;
        try {
            const replyPage = Number(comment.page || 1);
            const res = await buyerShowVideoComments({
                buyerShowId,
                page: replyPage,
                size: replySize,
                // 雪花 id 不可 Number()，会丢精度导致挂不到父评论
                parentId: comment.buyerShowCommentId
            });
            const rows = pickRows(res);
            comment.replies = replyPage === 1 ? rows : [...(comment.replies || []), ...rows];
            comment.page = replyPage + 1;
            comment.showAllReplies = true;
            const loaded = comment.replies?.length || 0;
            const replyTotal = Number(res?.total) || 0;
            comment.toggleText = loaded < replyTotal ? expandRepliesText(replyTotal - loaded) : t("收起");
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        }
    };

    /**
     * 展开 / 续拉 / 收起回复，对齐详情页 CommentList
     * 文案为「收起」时再次点击即收起，否则展开或续拉
     */
    const toggleReplies = async (comment: VideoFeedCommentRow) => {
        if (comment.toggleText === t("收起")) {
            comment.showAllReplies = false;
            comment.toggleText = expandRepliesText(Number(comment.replyCount) || 0);
            comment.page = 1;
            comment.replies = [];
            return;
        }
        await loadReplies(comment);
    };

    /** 发送评论 / 回复，成功返回 true */
    const send = async (content: string): Promise<boolean> => {
        const text = content.trim();
        if (!text) {
            uni.showToast({ title: t("评论不能为空"), icon: "none" });
            return false;
        }
        // 雪花 id 不可 Number()：2081929394093555712 会变成 2081929394093555700
        const parentId = replyTarget.value?.buyerShowCommentId ?? 0;
        try {
            await buyerShowComment({ buyerShowId, content: text, parentId });
            uni.showToast({ title: parentId ? t("回复成功") : t("评论成功") });
            clearReplyTarget();
            await reload(buyerShowId);
            // 回复后重新展开对应一级评论，避免列表被 reload 收起
            if (parentId) {
                const parent = list.value.find((item) => String(item.buyerShowCommentId) === String(parentId));
                if (parent) {
                    parent.page = 1;
                    parent.replies = [];
                    parent.toggleText = undefined;
                    await loadReplies(parent);
                }
            }
            return true;
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
            return false;
        }
    };

    return {
        list,
        total,
        loading,
        hasMore,
        replyTarget,
        replyPlaceholder,
        reload,
        loadMore,
        like,
        send,
        setReplyTarget,
        clearReplyTarget,
        toggleReplies,
        loadReplies
    };
}
