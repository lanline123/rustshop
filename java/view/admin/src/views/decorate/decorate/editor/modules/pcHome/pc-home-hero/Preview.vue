<template>
    <div class="dec-pc-home-hero" :style="cssVars">
        <div class="dec-pc-home-hero__layout">
            <!-- Left -->
            <section class="dec-pc-home-hero__panel dec-pc-home-hero__left">
                <div v-if="displayCategories.length" class="dec-pc-home-hero__cate-list">
                    <div v-for="(item, idx) in displayCategories" :key="String(item.picId ?? idx)" class="dec-pc-home-hero__cate-item">
                        <img v-if="isImageIcon(item)" class="dec-pc-home-hero__cate-icon" :src="getImageIconUrl(item)" alt="" @error="handleImgError" />
                        <em v-else-if="item.floorIcoFont" :class="item.floorIcoFont" class="dec-pc-home-hero__cate-icon-font"></em>
                        <span v-else class="dec-pc-home-hero__cate-icon-font">-</span>
                        <div class="dec-pc-home-hero__cate-text">
                            <div class="dec-pc-home-hero__cate-title" :title="item.picTitle || ''">{{ item.picTitle || "类目" }}</div>
                        </div>
                    </div>
                </div>
                <div v-else class="dec-pc-home-hero__empty">
                    <div class="dec-pc-home-hero__empty-text">点击配置左侧类目</div>
                </div>
            </section>

            <!-- Center -->
            <section class="dec-pc-home-hero__center">
                <div class="dec-pc-home-hero__center-top">
                    <!-- Banner -->
                    <div class="dec-pc-home-hero__banner">
                        <div v-if="banners.length" class="dec-pc-home-hero__banner-inner">
                            <Swiper :swiper-option="swiperOption" :model-value="banners">
                                <template #default="{ item }">
                                    <div class="dec-pc-home-hero__banner-slide">
                                        <a
                                            class="dec-pc-home-hero__banner-link"
                                            :href="item.picLink?.link || 'javascript:;'"
                                            target="_blank"
                                            rel="noopener noreferrer"
                                        >
                                            <img class="dec-pc-home-hero__banner-img" :src="formatImage(item.picUrl)" alt="" @error="handleImgError" />
                                        </a>
                                    </div>
                                </template>
                            </Swiper>
                        </div>
                        <div v-else class="dec-pc-home-hero__banner-empty">
                            <div class="dec-pc-home-hero__empty-text">点击添加轮播</div>
                        </div>
                    </div>

                    <!-- Promo Group (Items 0-1) -->
                    <div class="dec-pc-home-hero__promo-group">
                        <a
                            v-for="(card, idx) in promoCards"
                            :key="idx"
                            class="dec-pc-home-hero__panel dec-pc-home-hero__promo-card"
                            :href="card.link?.link || 'javascript:;'"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <div class="dec-pc-home-hero__card-header">
                                <div class="dec-pc-home-hero__card-title" :title="card.moduleTitle || ''">{{ card.moduleTitle || "促销" }}</div>
                                <div v-if="card.moduleSubTitle" class="dec-pc-home-hero__card-badge" :style="{ background: getSubTitleBg(card) }">
                                    {{ card.moduleSubTitle }}
                                </div>
                            </div>

                            <div class="dec-pc-home-hero__card-products">
                                <template v-if="card.products && card.products.length">
                                    <div v-for="(p, pIdx) in card.products.slice(0, 2)" :key="pIdx" class="dec-pc-home-hero__card-product">
                                        <div class="dec-pc-home-hero__card-img-box">
                                            <img class="dec-pc-home-hero__card-img" :src="formatImage(pickPic(p.pic))" alt="" @error="handleImgError" />
                                            <div class="dec-pc-home-hero__card-label" v-if="p.label" :style="getLabelStyle(p)">
                                                {{ p.label }}
                                            </div>
                                        </div>
                                        <div class="dec-pc-home-hero__card-price" v-if="String(card.showPrice) !== '0'">{{ p.price || "¥0" }}</div>
                                    </div>
                                </template>
                                <template v-else>
                                    <div class="dec-pc-home-hero__empty-text" style="font-size: 12px">配置商品</div>
                                </template>
                            </div>
                        </a>
                    </div>
                </div>

                <div class="dec-pc-home-hero__center-bottom">
                    <!-- 频道卡片 (Items 2-5) -->
                    <div v-if="productCards.length" class="dec-pc-home-hero__product-grid">
                        <a
                            v-for="(card, idx) in productCards"
                            :key="idx"
                            class="dec-pc-home-hero__channel-card"
                            :href="card.link?.link || 'javascript:;'"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <div class="dec-pc-home-hero__card-header">
                                <div class="dec-pc-home-hero__card-title" :title="card.moduleTitle || ''">{{ card.moduleTitle || "推荐" }}</div>
                                <div v-if="card.moduleSubTitle" class="dec-pc-home-hero__card-badge" :style="{ background: getSubTitleBg(card) }">
                                    {{ card.moduleSubTitle }}
                                </div>
                            </div>

                            <div class="dec-pc-home-hero__card-products">
                                <template v-if="card.products && card.products.length">
                                    <div v-for="(p, pIdx) in card.products.slice(0, 2)" :key="pIdx" class="dec-pc-home-hero__card-product">
                                        <div class="dec-pc-home-hero__card-img-box">
                                            <img class="dec-pc-home-hero__card-img" :src="formatImage(pickPic(p.pic))" alt="" @error="handleImgError" />
                                            <div class="dec-pc-home-hero__card-label" v-if="p.label" :style="getLabelStyle(p)">
                                                {{ p.label }}
                                            </div>
                                        </div>
                                        <div class="dec-pc-home-hero__card-price" v-if="String(card.showPrice) !== '0'">{{ p.price || "¥0" }}</div>
                                    </div>
                                </template>
                                <template v-else>
                                    <div class="dec-pc-home-hero__empty-text" style="font-size: 12px">配置商品</div>
                                </template>
                            </div>
                        </a>
                    </div>

                    <div v-else class="dec-pc-home-hero__empty dec-pc-home-hero__cards-empty">
                        <div class="dec-pc-home-hero__empty-text">点击配置推荐卡片</div>
                    </div>
                </div>
            </section>

            <!-- Right -->
            <section class="dec-pc-home-hero__right">
                <!-- 1. User Panel -->
                <div class="dec-pc-home-hero__panel dec-pc-home-hero__user-card">
                    <a
                        v-if="rightConfig.premiumMember?.show === true"
                        :class="['dec-pc-home-hero__premium-member', { 'is-title-only': !rightConfig.premiumMember?.description }]"
                        :style="{ backgroundImage: `url(${premiumMemberEntry})` }"
                        :href="rightConfig.premiumMember?.link?.link || 'javascript:;'"
                        :target="rightConfig.premiumMember?.link?.link ? '_blank' : undefined"
                        rel="noopener noreferrer"
                        :title="rightConfig.premiumMember?.description || rightConfig.premiumMember?.title || '会员'"
                    >
                        <span class="dec-pc-home-hero__premium-member-title">{{ rightConfig.premiumMember?.title || "会员" }}</span>
                        <span v-if="rightConfig.premiumMember?.description" class="dec-pc-home-hero__premium-member-desc">
                            {{ rightConfig.premiumMember.description }}
                        </span>
                    </a>
                    <div class="dec-pc-home-hero__user-top">
                        <img class="dec-pc-home-hero__avatar" :src="defaultAvatar" alt="" />
                        <div class="dec-pc-home-hero__user-info">
                            <div class="dec-pc-home-hero__user-name">{{ rightUser.nickname || "欢迎来商城" }}</div>
                            <div class="dec-pc-home-hero__user-links">
                                <span>注册</span>
                                <span class="divider"></span>
                                <span>入驻</span>
                            </div>
                        </div>
                    </div>

                    <div class="dec-pc-home-hero__user-desc">
                        <div class="title">{{ rightUser.welcomeText || "登录后更多精彩" }}</div>
                        <div class="subtitle">一键登录，福利好物全拥有</div>
                    </div>

                    <a class="dec-pc-home-hero__login-btn" href="javascript:;">立即登录</a>

                    <div class="dec-pc-home-hero__user-actions">
                        <div class="dec-pc-home-hero__action-item">
                            <i class="dec-pc-home-hero__action-icon dec-pc-home-hero__action-icon--order"></i>
                            <span>我的订单</span>
                        </div>
                        <div class="dec-pc-home-hero__action-item">
                            <i class="dec-pc-home-hero__action-icon dec-pc-home-hero__action-icon--history"></i>
                            <span>浏览记录</span>
                        </div>
                        <div class="dec-pc-home-hero__action-item">
                            <i class="dec-pc-home-hero__action-icon dec-pc-home-hero__action-icon--collect"></i>
                            <span>商品收藏</span>
                        </div>
                        <div class="dec-pc-home-hero__action-item">
                            <i class="dec-pc-home-hero__action-icon dec-pc-home-hero__action-icon--shop"></i>
                            <span>关注店铺</span>
                        </div>
                    </div>
                </div>

                <!-- 2. Shops Panel -->
                <div class="dec-pc-home-hero__panel dec-pc-home-hero__shops">
                    <div class="dec-pc-home-hero__shops-header">
                        <div class="title">
                            {{ shopsConfig.title }}
                            <span v-if="shopsConfig.subTitle" class="hot" :style="{ background: shopsConfig.subTitleBg }">{{ shopsConfig.subTitle }}</span>
                        </div>
                        <a
                            class="more"
                            :href="shopsConfig.moreLink || 'javascript:;'"
                            :target="shopsConfig.moreLink ? '_blank' : undefined"
                            rel="noopener noreferrer"
                        >
                            {{ shopsConfig.moreText }}
                            <el-icon :size="9" class="more-icon"><ArrowDown /></el-icon>
                        </a>
                    </div>
                    <div v-if="rightShortcuts.length" class="dec-pc-home-hero__shop-list">
                        <a
                            v-for="(sc, idx) in rightShortcuts"
                            :key="String(sc.picId ?? idx)"
                            class="dec-pc-home-hero__shop-item"
                            :href="sc.picLink?.link || 'javascript:;'"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <div class="dec-pc-home-hero__shop-img-box">
                                <img class="dec-pc-home-hero__shop-icon" :src="formatImage(pickPic(sc))" alt="" @error="handleImgError" />
                            </div>
                            <span class="dec-pc-home-hero__shop-enter" :style="{ color: shopsConfig.textColor }">{{ sc.picTitle || "进入店铺" }}</span>
                        </a>
                    </div>
                    <div v-else class="dec-pc-home-hero__empty">
                        <div class="dec-pc-home-hero__empty-text">点击配置店铺</div>
                    </div>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 营销通栏（pc-home-hero） - 预览组件
 * - 三列布局：左类目 / 中轮播+频道卡 / 右会员+店铺街
 * - 左侧类目优先取 decorate/pcCatFloor/list（PC 分类抽屉）真实数据
 * - 卡位商品来源为"商品分组"时：分组详情拿 productIds，再查商品列表取前 2 个
 */
import { computed, ref, watch } from "vue";
import { ArrowDown } from "@element-plus/icons-vue";
import type { PcHomeHeroCenterCard, PcHomeHeroModule } from "./types";
import { pcHomeHeroDefaults, defaultAvatar } from ".";
import { Swiper } from "@/components/swiper";
import { deepMerge } from "../../../utils/object";
import { getProductList } from "@/api/product/product";
import { getProductGroup } from "@/api/product/productGroup";
import { getPcCatFloorList } from "@/api/decorate/pcCatFloor";
import { imageFormat, pickPic } from "@/utils/format";
import emptyImgBg from "@/assets/decorate/empty-img-bg.png";
import premiumMemberEntry from "@/assets/decorate/pcHome/premium-member-entry.png";

defineOptions({ name: "PcHomeHeroPreview" });

const props = defineProps<{
    module: Record<string, unknown>;
}>();

const moduleConfig = computed<PcHomeHeroModule>(() => {
    return deepMerge(pcHomeHeroDefaults, props.module as Partial<PcHomeHeroModule>);
});

type LeftCategoryItem = {
    picId?: string | number;
    picTitle?: string;
    picThumb?: string;
    picUrl?: string;
    floorIcoFont?: string;
    useImage?: number;
    imageUrl?: string;
    picLink?: {
        link?: string;
    };
};

const leftApiCategories = ref<LeftCategoryItem[]>([]);

function isRecord(value: unknown): value is Record<string, unknown> {
    return !!value && typeof value === "object" && !Array.isArray(value);
}

function getFirstString(obj: Record<string, unknown>, keys: string[]): string {
    for (const key of keys) {
        const value = obj[key];
        if (typeof value === "string" && value.trim()) {
            return value.trim();
        }
    }
    return "";
}

function getFirstId(obj: Record<string, unknown>, keys: string[]): string | number | undefined {
    for (const key of keys) {
        const value = obj[key];
        if (typeof value === "string" || typeof value === "number") {
            return value;
        }
    }
    return undefined;
}

function normalizeLink(rawLink: string): string {
    if (!rawLink) return "javascript:;";
    return rawLink;
}

function isImageIcon(item: LeftCategoryItem): boolean {
    const useImage = Number(item.useImage ?? (item.imageUrl ? 1 : 0));
    return useImage === 1 && Boolean(item.imageUrl);
}

function getImageIconUrl(item: LeftCategoryItem): string {
    return imageFormat(item.imageUrl || pickPic(item));
}

function toLeftCategoryItem(item: unknown): LeftCategoryItem {
    if (!isRecord(item)) {
        return {
            picLink: { link: "javascript:;" }
        };
    }
    const imageUrl = typeof item.imageUrl === "string" ? item.imageUrl.trim() : "";
    const picThumb = typeof item.picThumb === "string" ? item.picThumb.trim() : imageUrl;
    const picUrl = typeof item.picUrl === "string" ? item.picUrl.trim() : picThumb;
    const picId = typeof item.picId === "string" || typeof item.picId === "number" ? item.picId : undefined;
    const picTitle = typeof item.picTitle === "string" && item.picTitle.trim() ? item.picTitle.trim() : "类目";
    const floorIcoFont = typeof item.floorIcoFont === "string" ? item.floorIcoFont.trim() : "";
    const link = isRecord(item.picLink) ? getFirstString(item.picLink, ["link"]) : "";
    const useImage = Number(item.useImage ?? (imageUrl ? 1 : 0));

    return {
        picId,
        picTitle,
        picThumb,
        picUrl,
        floorIcoFont,
        useImage,
        imageUrl,
        picLink: {
            link: normalizeLink(link)
        }
    };
}

function getTitleAndLinkFromCategoryNames(raw: unknown): { title: string; link: string } {
    const emptyResult = { title: "", link: "" };
    if (!raw) return emptyResult;

    const parseRecord = (item: Record<string, unknown>) => ({
        title: getFirstString(item, ["categoryTitleName"]),
        link: getFirstString(item, ["categoryLink"])
    });

    const parseRecordList = (list: unknown[]) => {
        const parsedList = list.filter((item): item is Record<string, unknown> => isRecord(item)).map((item) => parseRecord(item));
        const title = parsedList
            .map((item) => item.title)
            .filter((item) => !!item)
            .join(" / ");
        const link = parsedList.find((item) => !!item.link)?.link || "";
        return { title, link };
    };

    if (isRecord(raw)) {
        return parseRecord(raw);
    }

    if (Array.isArray(raw)) {
        return parseRecordList(raw);
    }

    if (typeof raw === "string" && raw.trim()) {
        const text = raw.trim();
        if (!text.startsWith("{") && !text.startsWith("[")) {
            return { title: text, link: "" };
        }
        try {
            const parsed: unknown = JSON.parse(text);
            if (Array.isArray(parsed)) {
                return parseRecordList(parsed);
            }
            if (isRecord(parsed)) {
                return parseRecord(parsed);
            }
        } catch {
            return emptyResult;
        }
    }

    return emptyResult;
}

function mapCatFloorToLeftItems(source: unknown): LeftCategoryItem[] {
    if (!isRecord(source)) return [];

    const records = Array.isArray(source.records) ? source.records : [];

    return records.reduce<LeftCategoryItem[]>((acc, floor) => {
        if (!isRecord(floor)) return acc;
        if (Number(floor.isShow ?? 1) === 0) return acc;

        const floorId = getFirstId(floor, ["catFloorId"]);
        const iconUrl = getFirstString(floor, ["imageUrl"]);
        const floorIcoFont = getFirstString(floor, ["floorIcoFont"]);
        const useImage = Number(floor.useImage ?? (iconUrl ? 1 : 0));
        const titleAndLink = getTitleAndLinkFromCategoryNames(floor.categoryNames);

        const title = (getFirstString(floor, ["catFloorName"]) || titleAndLink.title || "类目")
            .replace(/[，,、]/g, " / ")
            .replace(/\s*\/\s*/g, " / ")
            .trim();
        const categoryLink = titleAndLink.link;
        const directLink = getFirstString(floor, ["categoryLink"]);
        const link = normalizeLink(categoryLink || directLink);

        acc.push({
            picId: floorId,
            picTitle: title,
            picThumb: iconUrl,
            picUrl: iconUrl,
            floorIcoFont,
            useImage,
            imageUrl: iconUrl,
            picLink: {
                link
            }
        });
        return acc;
    }, []);
}

async function loadLeftApiCategories() {
    try {
        // 排序须与 PC 前台 common/pc/getCatFloor 一致；不传排序参数时后端默认按主键倒序，预览顺序会与前台相反
        const result = await getPcCatFloorList({ page: 1, size: 50, sortField: "sortOrder", sortOrder: "asc" });
        leftApiCategories.value = mapCatFloorToLeftItems(result);
    } catch {
        leftApiCategories.value = [];
    }
}

void loadLeftApiCategories();

const displayCategories = computed<LeftCategoryItem[]>(() => {
    const list = leftApiCategories.value.length ? leftApiCategories.value : (moduleConfig.value.left?.categories || []).map((item) => toLeftCategoryItem(item));
    const raw = Number(moduleConfig.value.left?.showCount ?? 10);
    const count = Number.isFinite(raw) ? Math.max(0, Math.floor(raw)) : 10;
    return list.slice(0, count);
});

const banners = computed(() => moduleConfig.value.center?.banners || []);

// 模块化卡片 (6个)
const allModules = computed(() => {
    const raw = moduleConfig.value.center?.modules || [];
    if (raw.length === 0 && pcHomeHeroDefaults.center?.modules) {
        return pcHomeHeroDefaults.center.modules;
    }
    const list = [...raw];
    while (list.length < 6) list.push({});
    return list.slice(0, 6);
});

// ==================== 商品分组数据获取 ====================
type CardProduct = NonNullable<PcHomeHeroCenterCard["products"]>[number];

// 存储每个卡位通过商品分组获取到的商品数据
const groupProductsMap = ref<Record<number, CardProduct[]>>({});

/**
 * 根据商品分组ID获取商品数据
 * Tigshop 商品列表接口不支持 productGroupId 过滤，改为：分组详情拿 productIds → 按 ids 查商品
 */
async function fetchGroupProducts(moduleIndex: number, productGroupId: string) {
    try {
        const group = (await getProductGroup("detail", { id: productGroupId })) as Record<string, unknown>;
        const productIds = Array.isArray(group?.productIds) ? (group.productIds as Array<number | string>).slice(0, 2) : [];
        if (!productIds.length) {
            groupProductsMap.value = { ...groupProductsMap.value, [moduleIndex]: [] };
            return;
        }
        const result = await getProductList({
            ids: productIds.join(","),
            size: 2,
            page: 1
        });
        const records = result.records || [];
        groupProductsMap.value = {
            ...groupProductsMap.value,
            [moduleIndex]: records.map(
                (item) =>
                    ({
                        productId: item.productId,
                        productSn: item.productSn || "",
                        productName: item.productName || "",
                        pic: {
                            picUrl: item.picUrl || "",
                            picThumb: item.picThumb || ""
                        },
                        price: item.productPrice ? `¥${item.productPrice}` : "¥0"
                    }) as CardProduct
            )
        };
    } catch {
        groupProductsMap.value = {
            ...groupProductsMap.value,
            [moduleIndex]: []
        };
    }
}

// 监听模块配置变化，当卡位使用商品分组时自动获取商品
watch(
    () =>
        allModules.value.map((m) => ({
            productSource: m.productSource,
            productGroupId: m.productGroupId
        })),
    (newVal) => {
        newVal.forEach((item, idx) => {
            if (item.productSource === "productGroup" && item.productGroupId) {
                fetchGroupProducts(idx, item.productGroupId);
            }
        });
    },
    { immediate: true, deep: true }
);

// 增强后的模块列表：商品分组卡位使用从 API 获取的商品数据
const enhancedModules = computed(() => {
    return allModules.value.map((card, idx) => {
        if (card.productSource === "productGroup" && groupProductsMap.value[idx]?.length) {
            return {
                ...card,
                products: groupProductsMap.value[idx]
            };
        }
        return card;
    });
});

// 前2个是 Banner 右侧推荐位
const promoCards = computed(() => enhancedModules.value.slice(0, 2));

// 后4个是 下方商品位
const productCards = computed(() => enhancedModules.value.slice(2, 6));

const rightUser = computed(() => {
    return (
        moduleConfig.value.right?.user || {
            avatar: undefined,
            nickname: "欢迎来商城",
            welcomeText: "登录后更多精彩"
        }
    );
});
const rightConfig = computed(() => moduleConfig.value.right || {});
const rightShortcuts = computed(() => moduleConfig.value.right?.shortcuts || []);

// 店铺街配置
const shopsConfig = computed(() => {
    const right = moduleConfig.value.right;
    return {
        title: right?.shopsTitle || "店铺街",
        subTitle: right?.shopsSubTitle || "",
        subTitleBg: right?.shopsSubTitleBg || "#FF4E4E",
        moreText: right?.shopsMoreText || "查看全部",
        moreLink: right?.shopsMoreLink?.link || "",
        textColor: right?.shopsTextColor || "#a06a38"
    };
});

const cssVars = computed(() => {
    const frame = moduleConfig.value.frame;
    const style = moduleConfig.value.style;
    const left = moduleConfig.value.left;
    const right = moduleConfig.value.right;

    // 计算用户卡片渐变背景
    let userCardGradient = "none";
    if (right?.userCardGradientColorA && right?.userCardGradientColorB) {
        const direction = gradientDirectionMap[right.userCardGradientType || "upDown"] || "to bottom";
        userCardGradient = `linear-gradient(${direction}, ${right.userCardGradientColorA}, ${right.userCardGradientColorB})`;
    }

    return {
        "--dec-box-padding": `${frame?.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 0}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor || "transparent",

        "--dec-height": `${style?.height ?? 336}px`,
        "--dec-gap": `${style?.gap ?? 16}px`,
        "--dec-left-width": `${style?.leftWidth ?? 220}px`,
        "--dec-right-width": `${style?.rightWidth ?? 260}px`,

        "--dec-panel-bg": style?.panelBackgroundColor ?? "#ffffff",
        "--dec-panel-radius": `${style?.panelRadius ?? 8}px`,
        "--dec-left-bg": left?.backgroundColor ?? "#ffffff",
        "--dec-left-text-color": left?.textColor ?? "#333333",
        "--dec-user-card-bg": right?.userCardBackground || style?.panelBackgroundColor || "#ffffff",
        "--dec-user-card-gradient": userCardGradient,

        "--dec-title-size": `${style?.titleFontSize ?? 14}px`,
        "--dec-title-color": style?.titleColor ?? "#333333",
        "--dec-muted-color": style?.mutedColor ?? "#999999",
        "--dec-price-color": style?.priceColor ?? "#e1251b",

        "--dec-banner-radius": `${style?.bannerRadius ?? 12}px`,
        "--dec-card-radius": `${style?.cardRadius ?? 12}px`
    };
});

const swiperOption = computed(() => ({
    autoplay: {
        delay: 4500,
        disableOnInteraction: false,
        pauseOnMouseEnter: true
    },
    pagination: {
        clickable: true
    },
    loop: true,
    navigation: true
}));

function formatImage(url?: string): string {
    return imageFormat(url) || emptyImgBg;
}

function handleImgError(e: Event) {
    const img = e.target as HTMLImageElement | null;
    if (!img) return;
    if (img.src === emptyImgBg) return;
    img.onerror = null;
    img.src = emptyImgBg;
}

// 渐变方向映射
const gradientDirectionMap: Record<string, string> = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "to bottom right"
};

// 默认子标题/标签背景渐变色（与 Settings.vue 保持一致）
const DEFAULT_SUBTITLE_BG_COLOR_A = "#FF4E4E";
const DEFAULT_SUBTITLE_BG_COLOR_B = "#E1251B";
const DEFAULT_LABEL_BG_COLOR_A = "#FF4E4E";
const DEFAULT_LABEL_BG_COLOR_B = "#E1251B";

// 获取子标题背景色（支持渐变）
function getSubTitleBg(card: PcHomeHeroCenterCard): string {
    const colorA = card.moduleSubTitleBgColorA;
    const colorB = card.moduleSubTitleBgColorB;
    const type = card.moduleSubTitleBgType || "leftRight";

    if (colorA && colorB) {
        const direction = gradientDirectionMap[type] || "to right";
        return `linear-gradient(${direction}, ${colorA}, ${colorB})`;
    }

    return `linear-gradient(to right, ${DEFAULT_SUBTITLE_BG_COLOR_A}, ${DEFAULT_SUBTITLE_BG_COLOR_B})`;
}

// 获取标签背景样式（支持渐变）
function getLabelStyle(product: Record<string, unknown>): Record<string, string | undefined> {
    const colorA = product.labelBgColorA as string | undefined;
    const colorB = product.labelBgColorB as string | undefined;
    const type = (product.labelBgType as string) || "leftRight";

    let background: string;

    if (colorA && colorB) {
        const direction = gradientDirectionMap[type] || "to right";
        background = `linear-gradient(${direction}, ${colorA}, ${colorB})`;
    } else {
        background = `linear-gradient(to right, ${DEFAULT_LABEL_BG_COLOR_A}, ${DEFAULT_LABEL_BG_COLOR_B})`;
    }

    // 背景恒为深色渐变（自定义或默认红渐变），文字统一用白色，避免漏配背景色时回退成红底红字
    return {
        background,
        color: "#fff"
    };
}
</script>

<style lang="less" src="./preview.less" />
