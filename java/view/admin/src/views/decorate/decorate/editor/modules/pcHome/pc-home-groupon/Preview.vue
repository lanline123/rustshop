<template>
    <div class="dec-pc-home-groupon" :style="cssVars" :class="rootClassList">
        <div class="mod_groupon">
            <!-- 标题区域：title.showTitle=1 时显示 -->
            <div v-if="moduleConfig.title?.showTitle === 1" class="groupon-top" :style="titleBarStyle">
                <div class="groupon-title-wrapper">
                    <div v-if="moduleConfig.title?.titleStyle === 3" class="line"></div>
                    <img
                        v-if="moduleConfig.title?.titleBackgroundPic?.picUrl"
                        class="image-size"
                        :src="imageFormat(moduleConfig.title.titleBackgroundPic.picUrl)"
                    />
                    <div class="groupon-title" :style="{ color: moduleConfig.title?.titleColor || '#1a1a1a' }">
                        {{ moduleConfig.title?.titleText || "拼团" }}
                    </div>
                    <div
                        v-if="moduleConfig.title?.descText"
                        class="groupon-desc"
                        :class="[moduleConfig.title?.titleStyle === 2 ? 'big-sm' : 'sm']"
                        :style="{ color: moduleConfig.title?.descColor || '#1a1a1a' }"
                    >
                        {{ moduleConfig.title.descText }}
                    </div>
                </div>
                <!-- 顶部倒计时：无箭头、不跳转 -->
                <div
                    v-if="titleMore.visible && titleMore.mode === MORE_TYPE_COUNTDOWN"
                    class="groupon-count-down"
                    :style="{ '--countdownTextColor': moduleConfig.title?.moreColor || '#1a1a1a' }"
                >
                    <div class="countdown-box">
                        <span class="countdown-text">{{ topCountdownLabel }}: </span>
                        <template v-if="topCountdownDays > 0">
                            <span class="countdown-item" :class="{ days: String(topCountdownDays).length > 2 }">{{ topCountdownDays }}</span>
                            <span class="countdown-separator-text">天</span>
                        </template>
                        <span class="countdown-item">{{ formatNum(topCountdownHours) }}</span>
                        <span class="countdown-separator">:</span>
                        <span class="countdown-item">{{ formatNum(topCountdownMinutes) }}</span>
                        <span class="countdown-separator">:</span>
                        <span class="countdown-item">{{ formatNum(topCountdownSeconds) }}</span>
                    </div>
                </div>

                <!-- 更多链接：文字 + 箭头（预览态不跳转） -->
                <div v-else-if="titleMore.visible" class="groupon-more-link" :style="{ color: moduleConfig.title?.moreColor || '#1a1a1a' }">
                    <span class="more-text">{{ moreLinkText }}</span>
                    <svg class="icon" viewBox="0 0 16 16" width="16" height="16">
                        <path d="M5.5 2.5l5 5.5-5 5.5" stroke="currentColor" stroke-width="1.5" fill="none" />
                    </svg>
                </div>
            </div>

            <!-- 商品列表区域 -->
            <div class="groupon_list_wrap">
                <!-- style=6 横向滚动布局 -->
                <template v-if="isScrollLayout">
                    <div class="groupon_list is-scroll" :style="listStyle">
                        <div ref="listRef" class="scroll-container" :style="scrollContainerStyle">
                            <div v-for="(item, index) in displayProducts" :key="index" class="proditem goods-ad-item" :style="{ width: scrollItemWidth }">
                                <GrouponItemCard :item="item" :module-config="moduleConfig" :now="now" />
                            </div>
                        </div>
                        <div class="swiper-button-prev" @click="scrollPrev"></div>
                        <div class="swiper-button-next" @click="scrollNext"></div>
                    </div>
                </template>

                <!-- style=2/3 Grid 布局 -->
                <template v-else>
                    <div class="goods-ad-warp" :style="wrapStyle">
                        <div class="goods-ad-con" :style="conStyle">
                            <div v-for="(item, index) in displayProducts" :key="index" class="proditem goods-ad-item" :style="{ width: itemWidth }">
                                <div class="item-content">
                                    <div class="item-con">
                                        <GrouponItemCard :item="item" :module-config="moduleConfig" :now="now" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 首页拼团模块 - 预览组件（移植自 Vort pc-home-groupon）
 * API 适配：拼团商品列表走 _shared/groupon 的 useGrouponItemList（promotion/groupon/item/list）
 */
import { computed, ref, onMounted, onUnmounted, defineComponent, h } from "vue";
import type { PcHomeGrouponModule } from "./types";
import { pcHomeGrouponDefaults } from ".";
import type { GrouponItemFilterState } from "@/types/promotion/groupon";
import { imageFormat, pickPic } from "@/utils/format";
import { deepMerge } from "../../../utils/object";
import { MORE_TYPE_COUNTDOWN, resolveCountdownLabel, resolveTitleMore } from "../../../utils/titleMore";
import { ModuleBuyButton, useGrouponItemList, useScrollControl, dateStringToTimestamp, formatNum } from "../_shared/groupon";

const EMPTY_IMG_SVG =
    'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100"><rect fill="%23f5f5f5" width="100" height="100"/></svg>';

/** picThumb 多为相对路径，需补 storageUrl 域名前缀（http 链接原样返回） */
function formatImage(url?: string): string {
    return url ? imageFormat(url) : EMPTY_IMG_SVG;
}

function handleImageError(e: Event) {
    const target = e.target as HTMLImageElement | null;
    if (!target || target.src === EMPTY_IMG_SVG) return;
    target.onerror = null;
    target.src = EMPTY_IMG_SVG;
}

defineOptions({ name: "PcHomeGrouponPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

/** 合并后的模块配置 */
const moduleConfig = computed<PcHomeGrouponModule>(() => {
    return deepMerge(pcHomeGrouponDefaults, props.module as Partial<PcHomeGrouponModule>);
});

// ==================== 基础计算属性 ====================
const styleValue = computed(() => Number(moduleConfig.value.style ?? 4));
const isScrollLayout = computed(() => styleValue.value === 6);

// ==================== 容器样式 ====================
const cssVars = computed(() => {
    const data = moduleConfig.value;
    return {
        "--dec-bg-color": data.backgroundColor ?? "#ffffff",
        "--dec-box-radius": `${data.boxRadius ?? 0}px`,
        "--dec-inner-padding": `${data.innerPadding ?? 0}px`,
        "--dec-box-padding": `${data.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${data.boxPaddingTop ?? 5}px`,
        "--dec-box-padding-bottom": `${data.boxPaddingBottom ?? 5}px`,
        "--dec-goods-padding": `${data.goodsPadding ?? 0}px`
    };
});

const rootClassList = computed(() => {
    const style = styleValue.value;
    const cardStyle = Number(moduleConfig.value.cardStyle ?? 1);
    const textAlign = Number(moduleConfig.value.textAlign ?? 1);
    const buyBtnStyle = Number(moduleConfig.value.buyBtnStyle ?? 0);
    return [
        `ad-style__${[2, 3, 4, 6].includes(style) ? style : 4}`,
        `ad-card_style__${cardStyle}`,
        `ad-text_align__${textAlign}`,
        `ad-buy_btn_style__${buyBtnStyle}`
    ];
});

// ==================== 标题右侧区（更多链接 / 倒计时） ====================
const titleMore = computed(() => resolveTitleMore(moduleConfig.value.title, "countdown"));
const moreLinkText = computed(() => moduleConfig.value.title?.moreLink?.title?.trim() || "更多");

// ==================== 标题区域样式 ====================
const titleBarStyle = computed(() => {
    const title = moduleConfig.value.title;
    if (!title) return {};
    const style: Record<string, string> = {};
    if (title.titleBackground2) {
        style.background = `linear-gradient(to right, ${title.titleBackground || ""}, ${title.titleBackground2})`;
    } else if (title.titleBackground) {
        style.background = title.titleBackground;
    }
    if (title.titleRadius) {
        style.borderTopLeftRadius = `${title.titleRadius}px`;
        style.borderTopRightRadius = `${title.titleRadius}px`;
    }
    style.padding = `${title.titlePaddingTop ?? 20}px 0 ${title.titlePaddingBottom ?? 20}px`;
    return style;
});

// ==================== 列表布局样式 ====================
const goodsPaddingValue = computed(() => Number(moduleConfig.value.goodsPadding ?? 0));
const gapValue = computed(() => goodsPaddingValue.value);

const wrapStyle = computed(() => ({
    padding: `${goodsPaddingValue.value}px`
}));

const conStyle = computed(() => ({
    gap: `${gapValue.value}px`
}));

const scrollContainerStyle = computed(() => ({
    gap: `${gapValue.value}px`,
    padding: `${goodsPaddingValue.value}px`
}));

const listStyle = computed(() => ({}));

// 横向滚动布局（style=6）：一屏固定展示 6 个
const scrollItemWidth = computed(() => {
    const gap = gapValue.value;
    if (gap > 0) {
        const totalGap = (gap * 5) / 6;
        return `calc(16.6667% - ${totalGap}px)`;
    }
    return "16.6667%";
});

const itemWidth = computed(() => {
    const style = styleValue.value;
    const gap = gapValue.value;
    if (style === 2) {
        // 一行4个
        if (gap > 0) {
            const totalGap = (gap * 3) / 4;
            return `calc(25% - ${totalGap}px)`;
        }
        return "25%";
    }
    // style=3，一行5个；style=4，一行6个
    if (style === 4) {
        if (gap > 0) {
            const totalGap = (gap * 5) / 6;
            return `calc(16.6667% - ${totalGap}px)`;
        }
        return "16.6667%";
    }

    if (gap > 0) {
        const totalGap = (gap * 4) / 5;
        return `calc(20% - ${totalGap}px)`;
    }
    return "20%";
});

// ==================== 横向滚动 ====================
const listRef = ref<HTMLElement | null>(null);

// 使用共享滚动工具
const { scrollPrev, scrollNext } = useScrollControl(listRef, ".proditem");

// ==================== 预览商品数据 ====================
interface PreviewProduct {
    productId: string;
    productName: string;
    picThumb: string;
    picUrl?: string;
    minGroupPrice: string;
    productPrice: string;
    minGroupNum: string;
    startTime?: string;
    endTime?: string;
}

/** Mock 商品数据（无活动时展示） */
const mockProducts: PreviewProduct[] = [
    {
        productId: "1",
        productName: "拼团商品示例一号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=1",
        minGroupPrice: "99.00",
        productPrice: "199.00",
        minGroupNum: "2"
    },
    {
        productId: "2",
        productName: "拼团商品示例二号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=2",
        minGroupPrice: "128.00",
        productPrice: "256.00",
        minGroupNum: "3"
    },
    {
        productId: "3",
        productName: "拼团商品示例三号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=3",
        minGroupPrice: "68.00",
        productPrice: "168.00",
        minGroupNum: "2"
    },
    {
        productId: "4",
        productName: "拼团商品示例四号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=4",
        minGroupPrice: "188.00",
        productPrice: "388.00",
        minGroupNum: "5"
    },
    {
        productId: "5",
        productName: "拼团商品示例五号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=5",
        minGroupPrice: "58.00",
        productPrice: "118.00",
        minGroupNum: "2"
    },
    {
        productId: "6",
        productName: "拼团商品示例六号",
        picThumb: "https://dummyimage.com/200x200/f5f5f5/999&text=6",
        minGroupPrice: "88.00",
        productPrice: "168.00",
        minGroupNum: "3"
    }
];

/** 当前活动 */
const grouponActivityItem = computed(() => moduleConfig.value.grouponActivityItem);
const grouponPromotionId = computed(() => grouponActivityItem.value?.grouponPromotionId || "");

/** 活动时间（用于填充到商品项） */
const activityTime = computed(() => ({
    startTime: grouponActivityItem.value?.startTime,
    endTime: grouponActivityItem.value?.endTime
}));

/** 根据 style 设置获取数量 */
const fetchSize = computed(() => {
    const style = styleValue.value;
    if (style === 6) return 18;
    return 20;
});

/** 自定义映射函数：将接口返回项转换为 PreviewProduct */
function mapGrouponItem(item: GrouponItemFilterState, actTime?: { startTime?: string; endTime?: string }): PreviewProduct {
    return {
        productId: String(item.productId || ""),
        productName: item.productName || "",
        picThumb: item.picThumb || "",
        picUrl: item.picUrl || "",
        minGroupPrice: item.minGroupPrice || "0",
        productPrice: item.productPrice || "0",
        minGroupNum: String(item.minGroupNum || "2"),
        startTime: actTime?.startTime || "",
        endTime: actTime?.endTime || ""
    };
}

// 使用共享数据拉取 composable
const { list: fetchedProducts } = useGrouponItemList<PreviewProduct>({
    promotionId: grouponPromotionId,
    size: fetchSize,
    mapItem: mapGrouponItem,
    activityTime
});

/** 最终展示的商品列表 */
const displayProducts = computed<PreviewProduct[]>(() => {
    if (fetchedProducts.value.length > 0) {
        return fetchedProducts.value;
    }
    // 无活动时根据 style 返回 mock 数据
    const style = styleValue.value;
    if (style === 2) return mockProducts.slice(0, 4);
    if (style === 4) return mockProducts.slice(0, 6);
    if (style === 6) return mockProducts;
    return mockProducts; // style=3，默认 5 个
});

// ==================== 倒计时逻辑 ====================
const now = ref(Date.now());
let countdownTimer: number | null = null;

onMounted(() => {
    countdownTimer = window.setInterval(() => {
        now.value = Date.now();
    }, 1000);
});

onUnmounted(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
});

/** 顶部倒计时：取活动结束时间 */
const topCountdownTargetTime = computed(() => {
    const activity = grouponActivityItem.value;
    if (!activity?.endTime) return 0;
    return dateStringToTimestamp(activity.endTime);
});

const topCountdownRemaining = computed(() => {
    const target = topCountdownTargetTime.value;
    if (!target) return 0;
    const diff = target - now.value;
    return diff > 0 ? diff : 0;
});

const topCountdownLabel = computed(() => {
    const customText = moduleConfig.value.title?.countdownLabel?.trim();
    if (customText) return customText;
    if (!topCountdownTargetTime.value) return resolveCountdownLabel(moduleConfig.value.title, "距结束");
    const activity = grouponActivityItem.value;
    if (activity?.startTime) {
        const startTs = dateStringToTimestamp(activity.startTime);
        if (startTs > now.value) return "距开始";
    }
    if (topCountdownRemaining.value <= 0) return "已结束";
    return resolveCountdownLabel(moduleConfig.value.title, "距结束");
});

const topCountdownDays = computed(() => Math.floor(topCountdownRemaining.value / (1000 * 60 * 60 * 24)));
const topCountdownHours = computed(() => Math.floor((topCountdownRemaining.value % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
const topCountdownMinutes = computed(() => Math.floor((topCountdownRemaining.value % (1000 * 60 * 60)) / (1000 * 60)));
const topCountdownSeconds = computed(() => Math.floor((topCountdownRemaining.value % (1000 * 60)) / 1000));

// ==================== 商品卡片子组件 ====================
const GrouponItemCard = defineComponent({
    name: "GrouponItemCard",
    props: {
        item: { type: Object as () => PreviewProduct, required: true },
        moduleConfig: { type: Object as () => PcHomeGrouponModule, required: true },
        now: { type: Number, required: true }
    },
    setup(props) {
        const isCardStyle1 = computed(() => Number(props.moduleConfig.cardStyle ?? 1) === 1);
        const showName = computed(() => Number(props.moduleConfig.showName ?? 1) === 1);
        const showPrice = computed(() => Number(props.moduleConfig.showPrice ?? 1) === 1);
        const textAlign = computed(() => Number(props.moduleConfig.textAlign ?? 1));
        const buyBtnStyle = computed(() => Number(props.moduleConfig.buyBtnStyle ?? 0));

        // 图片宽度
        const imageStyle = computed(() => ({
            width: isCardStyle1.value ? "90%" : "100%"
        }));

        // 名称区域样式
        const nameStyle = computed(() => ({
            textAlign: textAlign.value === 1 ? "left" : "center",
            marginBottom: isCardStyle1.value ? "0" : "12px"
        }));

        // 价格区域样式（cardStyle!=1 时）
        const priceStyle = computed(() => ({
            flexDirection: textAlign.value === 1 ? "row" : "column"
        }));

        // 商品倒计时
        const itemStartTime = computed(() => dateStringToTimestamp(props.item.startTime));
        const itemEndTime = computed(() => dateStringToTimestamp(props.item.endTime));

        const isActivityStarted = computed(() => {
            if (!itemStartTime.value) return true;
            return itemStartTime.value < props.now;
        });

        const isActivityEnded = computed(() => {
            if (!itemEndTime.value) return false;
            return itemEndTime.value < props.now;
        });

        const itemCountdownTarget = computed(() => {
            if (isActivityEnded.value) return 0;
            if (!isActivityStarted.value) return itemStartTime.value;
            return itemEndTime.value;
        });

        const itemCountdownRemaining = computed(() => {
            const target = itemCountdownTarget.value;
            if (!target) return 0;
            const diff = target - props.now;
            return diff > 0 ? diff : 0;
        });

        const countdownLabel = computed(() => {
            if (isActivityEnded.value) return "已结束";
            if (!isActivityStarted.value) return "距开始";
            return "距结束";
        });

        const countdownText = computed(() => {
            if (isActivityEnded.value) return "";
            const remaining = itemCountdownRemaining.value;
            if (!remaining) return "";
            const days = Math.floor(remaining / (1000 * 60 * 60 * 24));
            const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((remaining % (1000 * 60)) / 1000);
            if (days > 0) {
                return `${days}天${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
            }
            return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
        });

        return () => {
            return h("div", { class: "g-item-warp" }, [
                // 图片区域
                h("div", { class: "pic" }, [
                    h("div", { class: "groupon-item-image-wrapper" }, [
                        h("div", { class: "groupon-item-image", style: imageStyle.value }, [
                            h("img", {
                                src: formatImage(pickPic(props.item)),
                                alt: props.item.productName,
                                class: "pro_pic",
                                onError: handleImageError
                            })
                        ])
                    ]),
                    // cardStyle=1 时显示倒计时条
                    isCardStyle1.value
                        ? h("div", { class: "countdown" }, [
                              h("div", { class: "title" }, "拼团"),
                              countdownText.value
                                  ? h("div", { class: "countdown-time" }, [
                                        h("div", { class: "time" }, countdownLabel.value),
                                        h("div", { class: "text" }, countdownText.value)
                                    ])
                                  : h("div", { class: "countdown-time" }, [h("div", { class: "time" }, countdownLabel.value)])
                          ])
                        : null
                ]),
                // 商品名称区域
                showName.value
                    ? h("div", { class: "titleBox groupon-item__name", style: nameStyle.value }, [
                          h(
                              "div",
                              {
                                  class: "title product-name-text",
                                  "data-group-label": `${props.item.minGroupNum}人团`
                              },
                              props.item.productName
                          )
                      ])
                    : null,
                // 价格区域
                showPrice.value
                    ? isCardStyle1.value
                        ? // cardStyle=1 的 priceBox 样式
                          h("div", { class: "priceBox" }, [
                              h("div", { class: "priceBoxNum" }, [
                                  h("span", { class: "minGrouponPrice" }, `¥${props.item.minGroupPrice}`),
                                  h("div", { class: "old" }, `¥${props.item.productPrice}`)
                              ]),
                              h("div", { class: "text" }, "拼")
                          ])
                        : // cardStyle!=1 的 groupon-item__price 样式
                          h("div", { class: "groupon-item__price", style: priceStyle.value }, [
                              h("div", { class: "price-wrapper" }, [
                                  h("span", { class: "price price-miaosha" }, `¥${props.item.minGroupPrice}`),
                                  h("span", { class: "price price-origin" }, `¥${props.item.productPrice}`)
                              ]),
                              buyBtnStyle.value > 0
                                  ? h("div", { class: "buy" }, [h(ModuleBuyButton, { styleType: buyBtnStyle.value })])
                                  : h("div", { class: "buy" }, [h("div", { class: "view_detail" }, [h("i", { class: "iconfont-pc icon-tianjia" })])])
                          ])
                    : null
            ]);
        };
    }
});
</script>

<style lang="less" src="./preview.less" />
