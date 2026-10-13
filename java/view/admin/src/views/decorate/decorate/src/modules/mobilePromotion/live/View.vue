<template>
    <div class="module-ad-con module-live" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content" :style="moduleContentStyle">
            <div class="live-container">
                <template v-if="liveList.length">
                    <div class="live-list live-list--bigImage" v-if="module.displayMode === 'bigImage'">
                        <div :style="liveItemStyle" class="live-card live-card--bigImage" v-for="item in liveList" :key="item.wechatLiveId || item.roomId">
                            <div class="live-card__cover live-card__cover--full" :style="{ background: item.feedsImg ? 'none' : '' }">
                                <img v-if="item.feedsImg" :src="item.feedsImg" alt="直播封面" />
                                <div class="live-card__status-tag" :class="getLiveStatusClass(item)">{{ getLiveStatusText(item) }}</div>
                                <div class="live-card__mask">
                                    <div class="live-card__mask-title">{{ item.wechatLiveTitle || "-" }}</div>
                                    <div class="live-card__mask-meta">
                                        <span>{{ $t('主播：{anchorName}', { anchorName: item.anchorName || "-" }) }}</span>
                                        <span class="live-card__action">{{ $t('进入直播间') }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="live-list live-list--list" v-else>
                        <div :style="liveItemStyle" class="live-card live-card--list" v-for="item in liveList" :key="item.wechatLiveId || item.roomId">
                            <div class="live-card__cover live-card__cover--large" :style="{ background: item.feedsImg ? 'none' : '' }">
                                <img v-if="item.feedsImg" :src="item.feedsImg" alt="直播封面" style="height: 100%" />
                                <div class="live-card__badge">{{ $t('小程序直播') }}</div>
                                <div class="live-card__status">{{ item.liveStatusText || "-" }}</div>
                            </div>
                            <div class="live-card__content" :style="liveContentStyle">
                                <div class="live-card__title">{{ item.wechatLiveTitle || "-" }}</div>
                                <div class="live-card__goods">
                                    <img
                                        v-for="(goodsImg, goodsIndex) in getLiveGoodsImages(item)"
                                        :key="`${item.roomId}-${goodsIndex}`"
                                        class="live-card__goods-item"
                                        :src="goodsImg"
                                        alt="商品图片"
                                    />
                                </div>
                                <div class="live-card__footer">
                                    <div class="live-card__meta">{{ $t('主播：{anchorName}', { anchorName: item.anchorName || "-" }) }}</div>
                                    <div class="live-card__action">{{ $t('进入直播间') }}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>

                <div class="live-empty" v-else>{{ $t('暂无可展示直播间') }}</div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import type { WechatLiveGoodsState, WechatLiveRoomState } from "@/types/content/wechatLive.d";
import type { ModuleType } from "@/types/decorate/decorate.d";
import { getWechatLiveGoods, getWechatLiveRoomList } from "@/api/content/wechatLive";
import { defaultFrame, formatFrame, mergeDefaultModule } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface LiveDecorateModule extends ModuleType {
    titleText?: string;
    displayMode?: "list" | "bigImage";
    sourceType?: "auto" | "manual";
    autoLiveNum?: number;
    selectedLiveList?: WechatLiveRoomState[];
}
const autoLiveList = ref<WechatLiveRoomState[]>([]);
const liveGoodsMap = ref<Record<number, string[]>>({});

const liveStatusTextMap: Record<number, string> = {
    101: t('直播中'),
    102: t('未开始'),
    103: t('已结束'),
    104: t('禁播'),
    105: t('暂停'),
    106: t('异常'),
    107: t('已过期')
};

const getLiveStatusText = (item: WechatLiveRoomState) => {
    return item.liveStatusText || liveStatusTextMap[Number(item.liveStatus)] || t('未知');
};

const getLiveStatusClass = (item: WechatLiveRoomState) => {
    const status = Number(item.liveStatus);
    if (status === 101) return "live-card__status-tag--live";
    if (status === 102) return "live-card__status-tag--pending";
    return "live-card__status-tag--ended";
};
const module = defineModel<LiveDecorateModule>("module", { default: () => ({}) });

const defaultModule = ref({
    titleText: t('小程序直播'),
    displayMode: "list",
    sourceType: "auto",
    autoLiveNum: 10,
    selectedLiveList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        innerPadding: 12,
        boxRadius: 8,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
});

mergeDefaultModule(module.value, defaultModule.value);

const frameFormat = computed(() => formatFrame(module.value.frame || {}));
const moduleContentStyle = computed(() => {
    const radius = frameFormat.value.boxRadius;
    return `${radius}overflow:hidden;`;
});
const liveItemStyle = computed(() => {
    const radiusStyle = module.value.displayMode === "bigImage" ? frameFormat.value.boxRadius : "";
    return `${frameFormat.value.innerPadding}${radiusStyle}${frameFormat.value.backgroundColor}`;
});
const liveContentStyle = computed(() => (module.value.displayMode === "bigImage" ? frameFormat.value.boxRadius : ""));

const liveList = computed(() => {
    const autoLiveNum = Math.min(Math.max(Number(module.value.autoLiveNum || 10), 1), 10);
    const sourceList = module.value.sourceType === "manual" ? module.value.selectedLiveList || [] : autoLiveList.value;
    return sourceList.slice(0, module.value.sourceType === "manual" ? 10 : autoLiveNum);
});

const getLiveGoodsImages = (item: WechatLiveRoomState) => {
    return liveGoodsMap.value[item.roomId];
};

const loadAutoLives = async () => {
    try {
        const result = await getWechatLiveRoomList({
            keyword: "",
            page: 1,
            size: 10
        });
        autoLiveList.value = result.records.filter((item) => Number(item.liveStatus) !== 4).slice(0, 10);
    } catch (error) {
        console.error(error);
        autoLiveList.value = [];
    }
};

const loadLiveGoods = async () => {
    const roomIds = liveList.value.map((item) => Number(item.roomId)).filter((roomId) => roomId > 0);
    const goodsEntries = await Promise.all(
        roomIds.map(async (roomId) => {
            try {
                const goodsList = await getWechatLiveGoods(roomId);
                const goodsImages = (goodsList || [])
                    .map((goods: WechatLiveGoodsState) => goods.coverImg || "")
                    .filter((img) => !!img)
                    .slice(0, 3);
                return [roomId, goodsImages.length ? goodsImages : defaultGoodsImages] as const;
            } catch (error) {
                console.error(error);
                return [roomId, defaultGoodsImages] as const;
            }
        })
    );
    liveGoodsMap.value = Object.fromEntries(goodsEntries);
};

onMounted(() => {
    if (module.value.sourceType === "auto") {
        loadAutoLives();
    }
    loadLiveGoods();
});

watch(
    () => module.value.sourceType,
    (value) => {
        if (value === "auto" && autoLiveList.value.length === 0) {
            loadAutoLives();
        }
    }
);

watch(
    liveList,
    () => {
        loadLiveGoods();
    },
    {
        immediate: true
    }
);
</script>

<style lang="less" scoped>
.module-live {
    .live-empty {
        padding: 24px 0;
        text-align: center;
        color: #999;
        font-size: 12px;
    }

    .live-list--bigImage {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .live-card--bigImage {
        position: relative;
        width: 100%;
        overflow: hidden;
        box-sizing: border-box;
    }

    .live-card__cover--full {
        position: relative;
        width: 100%;
        height: auto;
        aspect-ratio: 5 / 4;
        background: linear-gradient(180deg, #7977b0 0%, #282655 100%);
        overflow: hidden;
        border-radius: inherit;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }
    }

    .live-card__status-tag {
        position: absolute;
        top: 8px;
        left: 8px;
        padding: 2px 8px;
        border-radius: 999px;
        font-size: 10px;
        color: #fff;
        z-index: 1;
        background: rgba(0, 0, 0, 0.35);
    }

    .live-card__status-tag--live {
        background: rgba(255, 68, 68, 0.9);
    }

    .live-card__status-tag--pending {
        background: rgba(121, 114, 255, 0.95);
    }

    .live-card__status-tag--ended {
        background: rgba(0, 0, 0, 0.35);
    }

    .live-card__mask {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        flex-direction: column;
        gap: 6px;
        padding: 16px;
        height: 70px;
        box-sizing: border-box;
        background: #fff;
    }

    .live-card__mask-title {
        font-size: 14px;
        line-height: 20px;
        word-break: break-all;
    }

    .live-card__mask-meta {
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 12px;
        color: #a7a7a7;
    }

    .live-list--list {
        display: flex;
        flex-direction: column;
        gap: 0px;
    }

    .live-card {
        overflow: hidden;
        box-sizing: border-box;
    }

    .live-card--list {
        display: flex;
        gap: 12px;
        padding: 12px;
    }

    .live-card__cover {
        position: relative;
        background: linear-gradient(180deg, #7977b0 0%, #282655 100%);
        overflow: hidden;

        img {
            width: 100%;
            height: calc(100% - 70px);
            object-fit: cover;
        }
    }

    .live-card__cover--large {
        width: 92px;
        height: 92px;
        flex: 0 0 92px;
        border-radius: 4px;
    }

    .live-card__badge,
    .live-card__status {
        position: absolute;
        left: 8px;
        padding: 2px 8px;
        border-radius: 999px;
        font-size: 10px;
        color: #fff;
    }

    .live-card__badge {
        top: 8px;
        background: rgba(121, 114, 255, 0.95);
    }

    .live-card__status {
        bottom: 8px;
        background: rgba(0, 0, 0, 0.35);
    }

    .live-card__content {
        min-width: 0;
        flex: 1;
    }

    .live-card--list .live-card__content {
        display: flex;
        flex-direction: column;
        justify-content: center;
    }

    .live-card__title {
        color: rgb(42, 42, 42);
        font-size: 14px;
        word-break: break-all;
    }

    .live-card__footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        margin-top: 8px;
    }

    .live-card__meta {
        color: #a7a7a7;
        font-size: 12px;
        line-height: 18px;
    }

    .live-card__goods {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-top: 8px;
    }

    .live-card__goods-item {
        width: 44px;
        height: 44px;
        border-radius: 4px;
        object-fit: cover;
        background: #f3f4f6;
        border: 1px solid #f0f0f0;
        flex-shrink: 0;
    }

    .live-card__action {
        padding: 4px 10px;
        border-radius: 3px;
        background: var(--tig-primary);
        color: #fff;
        font-size: 11px;
        line-height: 16px;
        white-space: nowrap;
        margin-left: auto;
        display: flex;
        align-items: center;
        gap: 2px;
    }
}
</style>
