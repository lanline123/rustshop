import { ref } from "vue";

import { buyerShowLike } from "@/api/buyerShow/buyerShow";
import { buyerShowFavorite, buyerShowFollowAuthor, buyerShowVideoFeed } from "@/api/buyerShow/videoFeed";
import type { VideoFeedItem } from "@/types/buyerShow/videoFeed";
import { getErrorMessage } from "@/utils/error";
import { resolvePlayableUrl, takeFeedBootstrap, takePrefetchedFeed } from "./videoFeedPrefetch";

/** video 组件 id 前缀，供 createVideoContext 定位 */
const VIDEO_ID_PREFIX = "bs-feed-video-";

/** 同时挂载/预热的半径：当前 ±N，兼顾预加载与内存 */
const PRELOAD_RADIUS = 2;

/** 单条最多展示的附图数，与发布端上限一致 */
const MAX_IMAGE_COUNT = 8;

/** 图片位自动轮播间隔（ms） */
const AUTO_PLAY_INTERVAL = 3000;

export interface UseVideoFeedOptions {
    /** 每页条数 */
    size?: number;
    /**
     * nvue 端 createVideoContext 需要传入组件实例（this）；
     * H5 / 小程序端可不传。
     */
    instance?: unknown;
}

/**
 * 买家秀视频流共用逻辑
 *
 * 视图无关：H5/小程序的 videoFeed.vue 与 App 的 videoFeed.nvue 均复用本 composable，
 * 只在视图层各自渲染，逻辑一次维护。
 */
export function useVideoFeed(options: UseVideoFeedOptions = {}) {
    const size = options.size ?? 5;

    const list = ref<VideoFeedItem[]>([]);
    const current = ref(0);
    const loading = ref(false);
    const hasMore = ref(true);
    const inited = ref(false);
    /** 每屏视频的当前播放秒数 */
    const videoCurrentTimes = ref<number[]>([]);
    /** 每屏视频的总时长秒数 */
    const videoDurations = ref<number[]>([]);
    /** 正在拖动进度条的屏，拖动期间忽略 timeupdate 回写 */
    const seekingScreens = new Set<number>();
    /** 原生 video 重挂载时使用的起播位置；Map 非响应式，避免 timeupdate 持续改写 initial-time */
    const videoResumeTimes = new Map<number, number>();
    /** 每屏当前素材下标：0 为视频位，1..N 为附图位 */
    const assetIndexes = ref<number[]>([]);
    /** 分页游标 */
    let cursor: string | null = null;
    /** 预热定时器，避免快速滑屏堆积 play/pause */
    const warmTimers = new Map<number, ReturnType<typeof setTimeout>>();
    /** 图片位自动轮播定时器；仅当前屏需要，故为单例 */
    let autoPlayTimer: ReturnType<typeof setTimeout> | null = null;

    /** 生成第 index 屏的 video id */
    const videoId = (index: number) => `${VIDEO_ID_PREFIX}${index}`;

    /** 播放地址：优先用进页前 downloadFile 预热的本地临时文件 */
    const playSrc = (index: number) => {
        const url = list.value[index]?.video?.videoUrl || "";
        return resolvePlayableUrl(url);
    };

    /** 获取 video 上下文，兼容 nvue 需要实例 */
    const getContext = (index: number) => {
        const id = videoId(index);
        let ctx: ReturnType<typeof uni.createVideoContext>;
        // #ifdef APP-NVUE
        ctx = uni.createVideoContext(id, options.instance as never);
        // #endif
        // #ifndef APP-NVUE
        ctx = uni.createVideoContext(id);
        // #endif
        return ctx;
    };

    /** 挂载当前及相邻屏真实视频源，供原生层缓冲下一条 */
    const shouldRenderVideo = (index: number) => Math.abs(index - current.value) <= PRELOAD_RADIUS;

    const clearWarmTimer = (index: number) => {
        const timer = warmTimers.get(index);
        if (timer) {
            clearTimeout(timer);
            warmTimers.delete(index);
        }
    };

    const playIndex = (index: number) => {
        clearWarmTimer(index);
        const ctx = getContext(index);
        ctx?.play?.();
    };

    const pauseIndex = (index: number) => {
        clearWarmTimer(index);
        const ctx = getContext(index);
        ctx?.pause?.();
    };

    /** 停止并销毁前保留当前进度，避免原生 video 残留音轨 */
    const stopIndex = (index: number, preserveTime = false) => {
        clearWarmTimer(index);
        if (preserveTime) {
            videoResumeTimes.set(index, currentTimeOf(index));
            seekingScreens.add(index);
        }
        getContext(index)?.stop?.();
        if (preserveTime) {
            setTimeout(() => seekingScreens.delete(index), 200);
        }
    };

    const pauseAll = () => {
        clearAutoPlay();
        list.value.forEach((_item, index) => pauseIndex(index));
    };

    const currentTimeOf = (index: number) => videoCurrentTimes.value[index] ?? 0;

    const initialTimeOf = (index: number) => videoResumeTimes.get(index) ?? 0;

    const durationOf = (index: number) => videoDurations.value[index] || Number(list.value[index]?.video?.duration) || 0;

    const readVideoTime = (index: number, event: { detail?: { currentTime?: number; duration?: number } }) => {
        const currentTime = Number(event?.detail?.currentTime);
        const duration = Number(event?.detail?.duration);
        if (Number.isFinite(duration) && duration > 0) videoDurations.value[index] = duration;
        if (!seekingScreens.has(index) && Number.isFinite(currentTime) && currentTime >= 0) {
            videoCurrentTimes.value[index] = currentTime;
        }
    };

    const previewSeek = (index: number, event: { detail?: { value?: number } }) => {
        seekingScreens.add(index);
        const value = Number(event?.detail?.value);
        if (Number.isFinite(value) && value >= 0) videoCurrentTimes.value[index] = value;
    };

    const commitSeek = (index: number, event: { detail?: { value?: number } }) => {
        const duration = durationOf(index);
        const value = Math.min(Math.max(Number(event?.detail?.value) || 0, 0), duration || 0);
        videoCurrentTimes.value[index] = value;
        getContext(index)?.seek?.(value);
        setTimeout(() => seekingScreens.delete(index), 150);
    };

    /** 素材位数组与 list 对齐；已有屏保留当前位，新增屏从视频位开始 */
    const syncAssetIndexes = () => {
        assetIndexes.value = list.value.map((_item, index) => assetIndexes.value[index] ?? 0);
    };

    /** 第 index 屏的附图，先剔除空地址再按上限截断 */
    const imagesOf = (index: number) => (list.value[index]?.images || []).filter((img) => !!img?.picUrl).slice(0, MAX_IMAGE_COUNT);

    /** 第 index 屏的素材总数：1 个视频 + N 张附图 */
    const assetCount = (index: number) => imagesOf(index).length + 1;

    /** 第 index 屏是否需要横向素材容器与进度条 */
    const hasMultiAsset = (index: number) => assetCount(index) > 1;

    /** 第 index 屏当前素材位 */
    const assetIndexOf = (index: number) => assetIndexes.value[index] ?? 0;

    /** 第 index 屏是否停在视频位 */
    const isVideoAsset = (index: number) => assetIndexOf(index) === 0;

    const clearAutoPlay = () => {
        if (autoPlayTimer) {
            clearTimeout(autoPlayTimer);
            autoPlayTimer = null;
        }
    };

    /** 图片位每 AUTO_PLAY_INTERVAL 前进一张；视频位不调度，末张停住不循环 */
    const scheduleAutoPlay = () => {
        clearAutoPlay();
        const screen = current.value;
        const pos = assetIndexOf(screen);
        if (pos <= 0 || pos >= assetCount(screen) - 1) return;
        autoPlayTimer = setTimeout(() => {
            autoPlayTimer = null;
            // 回调期间可能已切屏，此时不得改动其它屏的素材位
            if (current.value !== screen) return;
            setAssetIndex(screen, assetIndexOf(screen) + 1);
        }, AUTO_PLAY_INTERVAL);
    };

    /**
     * 设置第 screen 屏的素材位，并同步视频播放态与轮播定时器。
     * 横滑事件与自动轮播都走这里，保证状态单一出口。
     */
    const setAssetIndex = (screen: number, next: number) => {
        const total = assetCount(screen);
        const safe = Math.min(Math.max(next, 0), total - 1);
        assetIndexes.value[screen] = safe;
        if (screen !== current.value) return;
        if (safe === 0) {
            playIndex(screen);
        } else {
            // 微信小程序内层 swiper 切到图片时 pause 偶尔失效，停止并卸载可避免音轨残留
            // #ifdef MP-WEIXIN
            stopIndex(screen, true);
            // #endif
            // #ifndef MP-WEIXIN
            pauseIndex(screen);
            // #endif
        }
        scheduleAutoPlay();
    };

    /**
     * 静音预热相邻屏：短时 play 触发缓冲后 pause。
     * 优先预热下一条与下下条（上滑方向），减少切屏黑屏等待。
     */
    const warmNeighbors = (center: number) => {
        // 微信小程序动态 muted 偶有失效，播放式预热会导致相邻视频串音；保留挂载预加载即可
        // #ifdef MP-WEIXIN
        return;
        // #endif
        const targets = [center + 1, center + 2, center - 1].filter(
            (index) => index >= 0 && index < list.value.length && index !== center && shouldRenderVideo(index)
        );
        targets.forEach((index) => {
            clearWarmTimer(index);
            try {
                getContext(index)?.play?.();
            } catch {
                return;
            }
            warmTimers.set(
                index,
                setTimeout(() => {
                    warmTimers.delete(index);
                    if (current.value !== index) {
                        try {
                            getContext(index)?.pause?.();
                        } catch {
                            /* ignore */
                        }
                    }
                }, 450)
            );
        });
    };

    /**
     * 接口回填后补播当前屏：首屏占位无附图，回填 images 后模板会从单层 video
     * 切到内层 swiper，节点重挂载；此时 inited 已为 true，视图层 watch 不会再触发。
     */
    const resumeCurrentVideo = () => {
        let shouldResume = true;
        // 微信小程序依赖 video 挂载后的 autoplay，避免数据回填时重复调用 play 产生双音轨
        // #ifdef MP-WEIXIN
        shouldResume = false;
        // #endif
        if (!shouldResume) return;
        if (!list.value.length || !isVideoAsset(current.value)) return;
        setTimeout(() => {
            playIndex(current.value);
            setTimeout(() => warmNeighbors(current.value), 120);
        }, 80);
    };

    /** 首次加载，可指定起始买家秀 id 保证首屏是用户点击的那条 */
    const init = async (startId?: string | number) => {
        if (inited.value || loading.value) return;

        // 列表点击带入的首屏：先上屏再等接口，缩短打开时的等待
        const boot = takeFeedBootstrap(startId);
        let shouldUseBoot = Boolean(boot);
        // 微信小程序不先挂载简化版首屏，避免接口回填附图后重建原生 video 留下旧音轨
        // #ifdef MP-WEIXIN
        shouldUseBoot = false;
        // #endif
        if (boot && shouldUseBoot) {
            list.value = [boot];
            syncAssetIndexes();
            inited.value = true;
        } else {
            loading.value = true;
        }

        try {
            const prefetched = takePrefetchedFeed(startId);
            const res = prefetched ? await prefetched : await buyerShowVideoFeed({ size, startId });
            const records = res?.records || [];
            // 首条已在播时尽量保持同 id，减少整表替换导致的闪断
            const bootId = list.value[0]?.buyerShowId;
            const bootUrl = list.value[0]?.video?.videoUrl;
            if (bootId != null && records[0] && String(records[0].buyerShowId) === String(bootId) && bootUrl) {
                records[0].video.videoUrl = bootUrl;
            }
            list.value = records;
            syncAssetIndexes();
            cursor = res?.nextCursor ?? null;
            hasMore.value = Boolean(res?.hasMore && res?.nextCursor);
            inited.value = true;
            resumeCurrentVideo();
        } catch (error) {
            if (!list.value.length) {
                uni.showToast({ title: getErrorMessage(error), icon: "none" });
            }
        } finally {
            loading.value = false;
        }
        // 首屏若只返回起始一条，只有一屏无法上划、续拉将永不触发；此处主动补拉一屏
        if (inited.value && list.value.length < 2 && hasMore.value) {
            await loadMore();
        }
    };

    /** 续拉下一页 */
    const loadMore = async () => {
        if (loading.value || !hasMore.value || !cursor) return;
        loading.value = true;
        try {
            const res = await buyerShowVideoFeed({ size, cursor });
            const records = res?.records || [];
            const existingIds = new Set(list.value.map((item) => String(item.buyerShowId)));
            const uniqueRecords = records.filter((item) => {
                const id = String(item.buyerShowId);
                if (existingIds.has(id)) return false;
                existingIds.add(id);
                return true;
            });
            list.value.push(...uniqueRecords);
            syncAssetIndexes();
            cursor = res?.nextCursor ?? null;
            hasMore.value = Boolean(res?.hasMore && res?.nextCursor);
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        } finally {
            loading.value = false;
        }
    };

    /** 切屏：暂停上一屏、播放当前屏、预热相邻、临近末尾续拉 */
    const onChange = (index: number) => {
        const prev = current.value;
        if (prev === index) return;
        clearAutoPlay();
        // 微信小程序原生 video 偶有后台续播，切屏时明确暂停所有非目标实例
        // #ifdef MP-WEIXIN
        list.value.forEach((_item, itemIndex) => {
            if (itemIndex !== index) stopIndex(itemIndex, true);
        });
        // #endif
        // #ifndef MP-WEIXIN
        pauseIndex(prev);
        // #endif
        // 切走与切入的两屏都回到视频位
        assetIndexes.value[prev] = 0;
        current.value = index;
        assetIndexes.value[index] = 0;
        playIndex(index);
        // 等当前屏 video 挂载后再预热相邻
        setTimeout(() => warmNeighbors(index), 80);
        if (index >= list.value.length - 3) {
            loadMore();
        }
    };

    /** 点赞 / 取消，复用买家秀点赞接口 */
    const toggleLike = async (item: VideoFeedItem) => {
        const next = item.isLike ? 0 : 1;
        try {
            await buyerShowLike({ buyerShowId: item.buyerShowId, isLike: next });
            item.isLike = next;
            item.likeCount = Math.max(0, item.likeCount + (next ? 1 : -1));
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        }
    };

    /** 收藏 / 取消 */
    const toggleFavorite = async (item: VideoFeedItem) => {
        const next = item.isFavorite ? 0 : 1;
        try {
            await buyerShowFavorite({ buyerShowId: item.buyerShowId, isFavorite: next });
            item.isFavorite = next;
            item.favoriteCount = Math.max(0, item.favoriteCount + (next ? 1 : -1));
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        }
    };

    /** 关注 / 取消关注作者 */
    const toggleFollow = async (item: VideoFeedItem) => {
        const next = item.author.isFollow ? 0 : 1;
        try {
            await buyerShowFollowAuthor({ userId: item.author.userId, isFollow: next });
            item.author.isFollow = next;
        } catch (error) {
            uni.showToast({ title: getErrorMessage(error), icon: "none" });
        }
    };

    return {
        list,
        current,
        loading,
        hasMore,
        inited,
        assetIndexes,
        currentTimeOf,
        initialTimeOf,
        durationOf,
        readVideoTime,
        previewSeek,
        commitSeek,
        videoId,
        playSrc,
        shouldRenderVideo,
        playIndex,
        pauseIndex,
        stopIndex,
        pauseAll,
        warmNeighbors,
        imagesOf,
        assetCount,
        hasMultiAsset,
        assetIndexOf,
        isVideoAsset,
        setAssetIndex,
        clearAutoPlay,
        scheduleAutoPlay,
        init,
        loadMore,
        onChange,
        toggleLike,
        toggleFavorite,
        toggleFollow
    };
}
