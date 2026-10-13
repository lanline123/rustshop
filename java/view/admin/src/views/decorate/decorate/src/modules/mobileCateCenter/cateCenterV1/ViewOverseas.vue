<template>
    <div
        class="module-ad-con dec-cate-center-v1-overseas"
        :style="[frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom, decorateVars]"
    >
        <!-- 搜索栏 -->
        <div class="search-bar-box-overseas">
            <div class="back-btn">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                    <path
                        d="M685.248 104.704a32 32 0 0 1 0 45.248L318.176 512l367.072 362.048a32 32 0 1 1-45.248 45.248l-392-384.64a32 32 0 0 1 0-45.248l392-384.64a32 32 0 0 1 45.248 0z"
                        fill="#333"
                    />
                </svg>
            </div>
            <div class="search-input-wrap">
                <span class="search-placeholder">{{ module.searchPlaceholder ?? $t('搜索') }}</span>
                <i class="iconfont-h5 icon-sousuo"></i>
            </div>
        </div>

        <!-- 主内容区域 (左分类 aside + 右内容 conter) -->
        <div class="productSort">
            <!-- 左侧菜单 -->
            <div class="aside">
                <div v-if="showRecommendEntry" class="item acea-row row-center-wrapper" :class="{ on: catId === '' }">
                    <span>{{ recommendText }}</span>
                </div>
                <div v-for="item in cateList" :key="item.id" class="item acea-row row-center-wrapper" :class="{ on: catId === item.id }">
                    <span>{{ item.catShortName || item.categoryName }}</span>
                </div>
            </div>

            <!-- 右侧内容 -->
            <div class="conter">
                <!-- 推荐分类 -->
                <template v-if="catId === '' && showRecommendEntry">
                    <div class="listw">
                        <div class="category-title">{{ $t('推荐分类') }}</div>
                        <div class="list acea-row">
                            <a v-for="hot in hotCat" :key="hot.id" class="item acea-row row-column row-middle" href="javascript:;">
                                <div class="picture">
                                    <img :src="hot.categoryPic" alt="" />
                                </div>
                                <div v-if="module.showCategoryName !== false" class="name line1">{{ hot.categoryName }}</div>
                            </a>
                        </div>
                    </div>
                </template>

                <!-- 子分类 -->
                <template v-if="catId !== ''">
                    <div v-for="(cat, index) in childCat" :key="index" class="listw">
                        <template v-if="cat.children && cat.children.length > 0">
                            <!-- 二级分类标题 -->
                            <div class="category-title">{{ cat.categoryName }}</div>
                            <div class="list acea-row">
                                <a v-for="child in cat.children" :key="child.id" class="item acea-row row-column row-middle" href="javascript:;">
                                    <div v-if="child.categoryPic" class="picture">
                                        <img :src="child.categoryPic" alt="" />
                                    </div>
                                    <div v-if="module.showCategoryName !== false" class="name line1">{{ child.categoryName }}</div>
                                </a>
                            </div>
                        </template>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, Ref } from "vue";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<any>("module") as Ref<any>;
const defaultModule = {
    searchPlaceholder: t('搜索'),
    showCategoryName: true,
    showRecommend: true,
    recommendText: t('推荐'),
    // 颜色空串：跟随主题色 / 原默认色
    sideBgColor: "",
    sideTextColor: "",
    sideActiveTextColor: "",
    sideActiveBgColor: "",
    groupTitleColor: "",
    categoryNameColor: "",
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0 }
};
mergeDefaultModule(module.value, defaultModule);
const frameFormat = computed(() => formatFrame(module.value.frame ?? {}));

const COLOR_VARS: Array<[string, string]> = [
    ["sideBgColor", "--dec-cate-side-bg"],
    ["sideTextColor", "--dec-cate-side-text"],
    ["sideActiveTextColor", "--dec-cate-side-active-text"],
    ["sideActiveBgColor", "--dec-cate-side-active-bg"],
    ["groupTitleColor", "--dec-cate-group-title"],
    ["categoryNameColor", "--dec-cate-name"]
];

const decorateVars = computed(() => {
    const vars: Record<string, string> = {};
    COLOR_VARS.forEach(([field, varName]) => {
        const value = module.value?.[field];
        if (value) vars[varName] = String(value);
    });
    return vars;
});

const recommendText = computed(() => (module.value.recommendText ?? "").toString().trim());
const showRecommendEntry = computed(() => module.value.showRecommend !== false && recommendText.value.length > 0);

interface CategoryItem {
    id: string;
    categoryName: string;
    catShortName?: string;
    categoryPic?: string;
    children?: CategoryItem[];
}

const BASE_URL = "https://oss.tigshop.com/";

const cateList = ref<CategoryItem[]>([
    { id: "1", categoryName: t('女装') },
    { id: "2", categoryName: t('男装') },
    { id: "3", categoryName: t('内衣') },
    { id: "4", categoryName: t('鞋包') },
    { id: "5", categoryName: t('母婴') },
    { id: "6", categoryName: t('运动') },
    { id: "7", categoryName: t('美妆') },
    { id: "8", categoryName: t('配饰') },
    { id: "9", categoryName: t('家电') },
    {
        id: "10",
        categoryName: t('家居'),
        children: [
            {
                id: "10-1",
                categoryName: t('家纺床品'),
                children: [
                    { id: "21-home", categoryName: t('床上套件'), categoryPic: BASE_URL + "img/gallery/demo/16800815487NuMIvmCERTjSOwgXQ!!pic.jpeg" },
                    { id: "23-home", categoryName: t('被芯'), categoryPic: BASE_URL + "img/gallery/demo/16800815488spO1YEwiLHQGaxDOe!!pic.jpeg" },
                    { id: "22-home", categoryName: t('电热毯'), categoryPic: BASE_URL + "img/gallery/demo/1680081548dyg4S6CsdAZuIFW8eg!!pic.jpeg" },
                    { id: "25-home", categoryName: t('枕头'), categoryPic: BASE_URL + "img/gallery/demo/16800817406z1c2Mfya4mXBPeWK5!!pic.jpeg" },
                    { id: "26-home", categoryName: t('毛毯'), categoryPic: BASE_URL + "img/gallery/demo/16800817642vqqI5WOyFZx7tzWNt!!pic.jpeg" },
                    { id: "24-home", categoryName: t('床垫床褥'), categoryPic: BASE_URL + "img/gallery/demo/16800817010xgA0W1WnXpiD5PMM0!!pic.jpeg" }
                ]
            },
            {
                id: "10-2",
                categoryName: t('杯壶餐具'),
                children: [
                    { id: "37-cup", categoryName: t('保温杯壶'), categoryPic: BASE_URL + "img/gallery/demo/1680141635lnLO9uOYtOjc1FmHO0!!pic.png" },
                    { id: "38-cup", categoryName: t('冷水壶'), categoryPic: BASE_URL + "img/gallery/demo/1680141705SgdjGmlBdXbkX0PqZ6!!pic.png" },
                    { id: "39-cup", categoryName: t('玻璃杯'), categoryPic: BASE_URL + "img/gallery/demo/16801417468CllLXOUOaVKrdmObR!!pic.png" },
                    { id: "40-cup", categoryName: t('碗碟盘'), categoryPic: BASE_URL + "img/gallery/demo/1680141775phNAAEQVURV773coZb!!pic.png" },
                    { id: "41-cup", categoryName: t('餐具套装'), categoryPic: BASE_URL + "img/gallery/demo/1680141801c6z5Jif2dD43MZgqG7!!pic.png" },
                    { id: "92-cup", categoryName: "保鲜/饭盒", categoryPic: BASE_URL + "img/gallery/demo/1680144052QlKYoadOHNt3TiASwk!!pic.png" }
                ]
            },
            {
                id: "10-3",
                categoryName: t('烹饪厨房'),
                children: [
                    { id: "98-cook", categoryName: t('锅具'), categoryPic: BASE_URL + "img/gallery/demo/1680144199CHjBeEZ7vOiFAOsbuX!!pic.png" },
                    { id: "99-cook", categoryName: t('厨房置物架'), categoryPic: BASE_URL + "img/gallery/demo/1680144198CJWq3VM74hNGFI0Rq9!!pic.png" },
                    { id: "100-cook", categoryName: "刀具/剪刀", categoryPic: BASE_URL + "img/gallery/demo/1680144198DHJTmBSFVBEGXfCF16!!pic.png" }
                ]
            }
        ]
    },
    { id: "11", categoryName: t('家装') },
    { id: "12", categoryName: t('食品') },
    { id: "13", categoryName: t('国际') },
    { id: "14", categoryName: t('医药') }
]);

const hotCat = ref<CategoryItem[]>([
    { id: "21", categoryName: t('衬衫'), categoryPic: BASE_URL + "img/gallery/demo/16800815487NuMIvmCERTjSOwgXQ!!pic.jpeg" },
    { id: "22", categoryName: t('T恤'), categoryPic: BASE_URL + "img/gallery/demo/1680081548dyg4S6CsdAZuIFW8eg!!pic.jpeg" },
    { id: "24", categoryName: t('外套装'), categoryPic: BASE_URL + "img/gallery/demo/16800817010xgA0W1WnXpiD5PMM0!!pic.jpeg" },
    { id: "92", categoryName: t('高跟单鞋'), categoryPic: BASE_URL + "img/gallery/demo/1680144052QlKYoadOHNt3TiASwk!!pic.png" },
    { id: "98", categoryName: t('短筒靴'), categoryPic: BASE_URL + "img/gallery/demo/1680144199CHjBeEZ7vOiFAOsbuX!!pic.png" },
    { id: "723", categoryName: t('绿植'), categoryPic: BASE_URL + "img/gallery/202403/1710297151fr82IXdfqPtTM5qBrx.jpg" }
]);

const catId = ref<string>("10"); // 默认选中"家居" (完美展示图二内容)
const childCat = ref<CategoryItem[]>([]);

function changeCat(id: string, item?: CategoryItem) {
    catId.value = id;
    childCat.value = item?.children ?? [];
}

// 寻找初始化分类
const initialCat = cateList.value.find((item) => item.id === catId.value);
if (initialCat) {
    childCat.value = initialCat.children ?? [];
}

watch(
    () => showRecommendEntry.value,
    (enabled) => {
        if (!enabled && catId.value === "") {
            const first = cateList.value[0];
            if (first) changeCat(first.id, first);
        }
    },
    { immediate: true }
);
</script>

<script lang="ts">
export default {
    name: "CateCenterV1OverseasView"
};
</script>

<style lang="less" scoped>
.dec-cate-center-v1-overseas {
    box-sizing: border-box;
    --rpx: 0.5px;
    --dec-height: 820px;
    /* --dec-cate-* 由 Settings 下发，未配置回落到主题色 / 原硬编码 */
    --primary-color: var(--tig-primary, #e93b3d);

    display: flex;
    flex-direction: column;
    height: var(--dec-height);
    background-color: #fff;
    overflow: hidden;
    position: relative;

    // ========== 模拟顶部导航栏 ==========
    .overseas-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: calc(88 * var(--rpx));
        padding: 0 calc(24 * var(--rpx));
        background: #ffffff;
        border-bottom: 1px solid #f2f3f7;
        flex-shrink: 0;

        .header-left {
            display: flex;
            align-items: center;
            gap: calc(16 * var(--rpx));

            .logo-text {
                font-size: calc(32 * var(--rpx));
                font-weight: 800;
                color: #1a1a1a;
                letter-spacing: -0.5px;
            }
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: calc(28 * var(--rpx));
        }

        .header-icon {
            cursor: pointer;
            transition: opacity 0.2s;
            &:hover {
                opacity: 0.7;
            }
        }
    }

    // ========== 跨境版搜索栏 ==========
    .search-bar-box-overseas {
        display: flex;
        align-items: center;
        padding: calc(16 * var(--rpx)) calc(24 * var(--rpx)) calc(16 * var(--rpx));
        background: #fff;
        flex-shrink: 0;
        gap: calc(16 * var(--rpx));

        .back-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: calc(48 * var(--rpx));
            height: calc(48 * var(--rpx));
            cursor: pointer;
        }

        .search-input-wrap {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex: 1;
            height: calc(68 * var(--rpx));
            border: 1.5px solid #1a1a1a;
            background-color: #fff;
            border-radius: 4px;
            box-sizing: border-box;
            cursor: pointer;
            padding: 0 calc(24 * var(--rpx));

            .icon-sousuo {
                font-size: calc(30 * var(--rpx));
                color: #1a1a1a;
                font-weight: bold;
            }

            .search-placeholder {
                font-size: calc(26 * var(--rpx));
                color: #999999;
            }
        }
    }

    // ========== Uniapp 工具类兼容 ==========
    .acea-row {
        display: flex;
    }
    .row-center-wrapper {
        align-items: center;
        justify-content: center;
    }
    .row-column {
        flex-direction: column;
    }
    .row-middle {
        align-items: center;
    }
    .line1 {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    // ========== 主内容容器 ==========
    .productSort {
        display: flex;
        flex: 1;
        min-height: 0;
        width: 100%;
        overflow: hidden;
        border-top: 1px solid #f2f3f7;
    }

    // ========== 左侧菜单 ==========
    .productSort .aside {
        height: 100%;
        width: calc(180 * var(--rpx));
        background-color: var(--dec-cate-side-bg, #f7f8fc);
        overflow-y: auto;
        overflow-x: hidden;
        box-sizing: border-box;
        flex-shrink: 0;
        padding-top: calc(8 * var(--rpx));

        &::-webkit-scrollbar {
            display: none;
        }
    }
    .productSort .aside .item {
        height: calc(72 * var(--rpx));
        margin: calc(16 * var(--rpx)) calc(12 * var(--rpx));
        font-size: calc(26 * var(--rpx));
        color: var(--dec-cate-side-text, #555555);
        text-align: center;
        cursor: pointer;
        position: relative;
        transition: all 0.2s ease;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: calc(12 * var(--rpx));
    }
    .productSort .aside .item > span {
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: normal;
        word-break: break-all;
        padding: 0 calc(12 * var(--rpx));
        line-height: 1.3;
    }
    .productSort .aside .item.on {
        background-color: var(--dec-cate-side-active-bg, #fff);
        color: var(--dec-cate-side-active-text, var(--primary-color, #1a1a1a));
        font-weight: bold;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }

    // ========== 右侧内容 ==========
    .productSort .conter {
        flex: 1;
        height: 100%;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        background-color: #f7f8fc;
        box-sizing: border-box;
        padding: calc(16 * var(--rpx)) calc(16 * var(--rpx)) calc(40 * var(--rpx));
        gap: calc(20 * var(--rpx));

        &::-webkit-scrollbar {
            display: none;
        }

        .listw {
            width: 100%;
            background-color: #fff;
            border-radius: 8px;
            padding: calc(24 * var(--rpx));
            box-sizing: border-box;

            .category-title {
                font-size: calc(26 * var(--rpx));
                color: var(--dec-cate-group-title, #1a1a1a);
                font-weight: bold;
                padding: 0 0 calc(20 * var(--rpx));
                text-align: left;
            }
        }
    }
    .productSort .conter .list {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        padding: 0;
        gap: calc(24 * var(--rpx)) calc(12 * var(--rpx));
        background-color: #fff;
    }
    .productSort .conter .list .item {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-decoration: none;
        width: 100%;
    }
    .productSort .conter .list .item .picture {
        width: calc(120 * var(--rpx));
        height: calc(120 * var(--rpx));
        overflow: hidden;
        position: relative;
        border-radius: 6px;
        background-color: #f9f9f9;
        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }
    .productSort .conter .list .item .name {
        margin-top: calc(12 * var(--rpx));
        line-height: calc(32 * var(--rpx));
        color: var(--dec-cate-name, #333);
        font-size: calc(22 * var(--rpx));
        text-align: center;
        width: 100%;
        padding: 0 calc(4 * var(--rpx));
        box-sizing: border-box;
    }

    // ========== 模拟底部导航栏 ==========
    .overseas-tabbar {
        display: flex;
        align-items: center;
        justify-content: space-around;
        height: calc(100 * var(--rpx));
        border-top: 1px solid #f2f3f7;
        background-color: #ffffff;
        flex-shrink: 0;

        .tabbar-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            gap: calc(6 * var(--rpx));
            flex: 1;

            .tabbar-text {
                font-size: calc(20 * var(--rpx));
                color: #8a8a8a;
                font-weight: 500;
            }

            &.active {
                .tabbar-text {
                    color: var(--primary-color);
                    font-weight: bold;
                }
            }
        }
    }
}

.modules-item.module-item-active .module-ad-con:before {
    left: 0px;
    right: 0px;
}
</style>
