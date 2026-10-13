<template>
    <div v-if="!isOverseas()" class="module-ad-con dec-cate-center-v1" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <!-- 搜索栏 -->
        <div class="search-bar-box">
            <div class="search-input-wrap">
                <i class="iconfont-h5 icon-sousuo"></i>
                <span class="search-placeholder">{{ module.searchPlaceholder ?? $t('点击搜索商品信息') }}</span>
            </div>
        </div>

        <div class="productSort">
            <!-- 左侧菜单 -->
            <div class="aside">
                <div v-if="showRecommendEntry" class="item acea-row row-center-wrapper" :class="{ on: catId === '' }">
                    <span>{{ recommendText }}</span>
                </div>
                <div
                    v-for="item in cateList"
                    :key="item.id"
                    class="item acea-row row-center-wrapper"
                    :class="{ on: catId === item.id }"
                >
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
import { isOverseas } from "@/utils/version";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<any>("module") as Ref<any>;
const defaultModule = {
    searchPlaceholder: t('点击搜索商品信息'),
    showCategoryName: true,
    showRecommend: true,
    recommendText: t('推荐'),
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0 }
};
mergeDefaultModule(module.value, defaultModule);
const { frame } = module.value;
const frameFormat = computed(() => formatFrame(frame ?? {}));

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
    {
        id: "1",
        categoryName: t('女装')
    },
    {
        id: "2",
        categoryName: t('男装')
    },
    {
        id: "3",
        categoryName: t('内衣')
    },
    {
        id: "4",
        categoryName: t('鞋包')
    },
    {
        id: "5",
        categoryName: t('母婴')
    },
    {
        id: "6",
        categoryName: t('运动')
    },
    {
        id: "7",
        categoryName: t('美妆')
    },
    {
        id: "8",
        categoryName: t('配饰')
    },
    {
        id: "9",
        categoryName: t('家电')
    },
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
    {
        id: "11",
        categoryName: t('家装')
    },
    {
        id: "12",
        categoryName: t('食品')
    },
    {
        id: "13",
        categoryName: t('国际')
    },
    {
        id: "14",
        categoryName: t('医药')
    },
    {
        id: "15",
        categoryName: t('图书')
    }
]);

const hotCat = ref<CategoryItem[]>([
    { id: "21", categoryName: t('衬衫'), categoryPic: BASE_URL + "img/gallery/demo/16800815487NuMIvmCERTjSOwgXQ!!pic.jpeg" },
    { id: "22", categoryName: t('T恤'), categoryPic: BASE_URL + "img/gallery/demo/1680081548dyg4S6CsdAZuIFW8eg!!pic.jpeg" },
    { id: "24", categoryName: t('外套装'), categoryPic: BASE_URL + "img/gallery/demo/16800817010xgA0W1WnXpiD5PMM0!!pic.jpeg" },
    { id: "92", categoryName: t('高跟单鞋'), categoryPic: BASE_URL + "img/gallery/demo/1680144052QlKYoadOHNt3TiASwk!!pic.png" },
    { id: "98", categoryName: t('短筒靴'), categoryPic: BASE_URL + "img/gallery/demo/1680144199CHjBeEZ7vOiFAOsbuX!!pic.png" },
    { id: "723", categoryName: t('绿植'), categoryPic: BASE_URL + "img/gallery/202403/1710297151fr82IXdfqPtTM5qBrx.jpg" }
]);

const catId = ref<string>("10"); // 默认选中"家居" (对应 cateList 的第10项，索引10的 id 为"10")
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

<style lang="less" scoped>
.dec-cate-center-v1 {
    box-sizing: border-box;
    --rpx: 0.5px;
    --dec-height: 700px;
    --primary-color: var(--tig-primary, #e93b3d);

    display: flex;
    flex-direction: column;
    height: var(--dec-height);
    background-color: #fff;
    overflow: hidden;
    position: relative;

    // ========== 搜索栏 ==========
    .search-bar-box {
        display: flex;
        align-items: center;
        padding: 0 calc(24 * var(--rpx)) calc(16 * var(--rpx));
        background: #fff;
        flex-shrink: 0;

        .search-input-wrap {
            display: flex;
            align-items: center;
            // justify-content: center;
            width: 100%;
            height: calc(68 * var(--rpx));
            background-color: #f5f6fa;
            border-radius: calc(34 * var(--rpx));
            box-sizing: border-box;
            cursor: pointer;
            padding-left: 10px;

            .icon-sousuo {
                font-size: calc(28 * var(--rpx));
                color: #888b94;
                margin-right: calc(10 * var(--rpx));
            }

            .search-placeholder {
                font-size: calc(24 * var(--rpx));
                color: #888b94;
            }
        }
    }

    // ========== 跨境版搜索栏 ==========
    .search-bar-box-overseas {
        display: flex;
        align-items: center;
        padding: 0 calc(24 * var(--rpx)) calc(16 * var(--rpx));
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
            border: 1px solid #cccccc;
            background-color: #fff;
            border-radius: calc(12 * var(--rpx));
            box-sizing: border-box;
            cursor: pointer;
            padding: 0 calc(24 * var(--rpx));

            .icon-sousuo {
                font-size: calc(28 * var(--rpx));
                color: #333333;
            }

            .search-placeholder {
                font-size: calc(24 * var(--rpx));
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

    // ========== 主容器 ==========
    .productSort {
        display: flex;
        flex: 1;
        min-height: 0;
        width: 100%;
        overflow: hidden;
        border-top: 1px solid #f5f6fa;
    }

    // ========== 左侧菜单 ==========
    .productSort .aside {
        height: 100%;
        width: calc(180 * var(--rpx));
        background-color: #f5f6fa;
        overflow-y: auto;
        overflow-x: hidden;
        box-sizing: border-box;
        flex-shrink: 0;

        &::-webkit-scrollbar {
            display: none;
        }
    }
    .productSort .aside .item {
        padding: calc(32 * var(--rpx)) 0;
        width: 100%;
        font-size: calc(26 * var(--rpx));
        color: #666;
        text-align: center;
        cursor: pointer;
        position: relative;
        transition: all 0.2s ease;
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
        background-color: #fff;
        color: var(--primary-color);
        font-weight: bold;
    }

    // ========== 右侧内容 ==========
    .productSort .conter {
        flex: 1;
        height: 100%;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        background-color: #fff;
        box-sizing: border-box;

        &::-webkit-scrollbar {
            display: none;
        }

        .listw {
            width: 100%;
            background-color: #fff;

            .category-title {
                font-size: calc(26 * var(--rpx));
                color: #1a1a1a;
                font-weight: bold;
                padding: calc(32 * var(--rpx)) calc(24 * var(--rpx)) calc(12 * var(--rpx));
                text-align: left;
            }
        }
    }
    .productSort .conter .list {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        padding: calc(12 * var(--rpx)) calc(12 * var(--rpx));
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
        width: calc(140 * var(--rpx));
        height: calc(140 * var(--rpx));
        overflow: hidden;
        position: relative;
        border-radius: 8px;
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
        color: #333;
        font-size: calc(22 * var(--rpx));
        text-align: center;
        width: 100%;
        padding: 0 calc(8 * var(--rpx));
        box-sizing: border-box;
    }
}

.modules-item.module-item-active .module-ad-con:before {
    left: 0px;
    right: 0px;
}
</style>
