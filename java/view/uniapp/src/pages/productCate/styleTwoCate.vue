<template>
    <view :style="decorateVars">
        <tig-navbar v-if="!isOverseasSkin" :title="$t('产品分类')" :show-left="false" />
        <search v-if="!isOverseasSkin" :needMenu="true" :placeholder="searchPlaceholder" @menu="handleMenu" @search="handleSearch" />
        <view class="scroll-box">
            <view class="content-box">
                <view class="side">
                    <menuBox v-model:current-cate-id="currentCateId" @change="reset" />
                </view>
                <view class="list-box">
                    <selectCate
                        v-model:menu-list="menuList"
                        v-model:sub-current-cate-id="subCurrentCateId"
                        :current-cate-id="currentCateId"
                        :module="moduleConfig"
                        @change="handleChange"
                    />
                    <view class="list-content">
                        <template v-if="!isLoading && total > 0">
                            <scroll-view :scroll-y="true" class="list-scroll" @scrolltolower="reachBottom">
                                <masonry
                                    :commodity-list="list"
                                    :style-map="styleMap"
                                    :text-align="cateV2TextAlign"
                                    :text-weight="cateV2TextWeight"
                                    :goods-name-row="cateV2GoodsNameRow"
                                    :show-name="cateV2ShowName"
                                    :buy-btn-style="cateV2BuyBtnStyle"
                                    :price-color="cateV2PriceColor"
                                    :product-title-color="cateV2ProductTitleColor"
                                    :theme-color="cateV2BuyBtnColor"
                                    :item-background-color="cateV2GoodsListBgColor"
                                />
                            </scroll-view>
                        </template>
                        <template v-if="!isLoading && total === 0">
                            <view class="empty-box">
                                <up-empty :icon="staticResource('salesman/no_order.png')" :text="$t('暂无数据')" />
                            </view>
                        </template>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";

/**
 * 分类样式二组件
 * - 顶部分类Tab+商品瀑布流
 * - 支持分类筛选和商品搜索
 * - 支持 cateCenterV2 装修配置
 */
import { computed, reactive, ref, type PropType } from "vue";
import search from "./src/search.vue";
import menuBox from "./src/menu.vue";
import selectCate from "./src/selectCate.vue";
import masonry from "@/components/masonry/masonry.vue";
import { getCateProduct } from "@/api/home/home";
import { useList } from "@/hooks";
import { useProductStyle } from "@/hooks/useProductStyle";
import { staticResource, redirect } from "@/utils";
import type { filterSeleted } from "@/types/productCate/productCate";
import type { CateCenterV2Data } from "@/components/modules/cate-center/cate-center-v2/types";
import { buildCateCenterV2Vars, cateCenterV2Defaults } from "@/components/modules/cate-center/cate-center-v2/config";
import { deepMerge } from "@/utils/deepMerge";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();

const currentCateId = ref(0);

const subCurrentCateId = ref(0);

const menuList = ref<filterSeleted[]>([]);

const props = defineProps({
    height: {
        type: [Number, String],
        default: 0
    },
    /** 装修配置 */
    module: {
        type: Object as PropType<CateCenterV2Data>,
        default: () => ({})
    }
});

const moduleConfig = computed<CateCenterV2Data>(() => {
    return deepMerge({ ...cateCenterV2Defaults }, props.module || {});
});

const searchPlaceholder = computed(() => moduleConfig.value.searchPlaceholder || t("搜索商品"));
const cateV2TextAlign = computed(() => moduleConfig.value.textAlign ?? cateCenterV2Defaults.textAlign ?? 1);
const cateV2TextWeight = computed(() => moduleConfig.value.textWeight ?? cateCenterV2Defaults.textWeight ?? 1);
const cateV2GoodsNameRow = computed(() => moduleConfig.value.goodsNameRow ?? cateCenterV2Defaults.goodsNameRow ?? 2);
const cateV2ShowName = computed(() => (moduleConfig.value.showName ?? cateCenterV2Defaults.showName ?? 1) !== 0);
const cateV2BuyBtnStyle = computed(() => moduleConfig.value.buyBtnStyle ?? cateCenterV2Defaults.buyBtnStyle ?? 1);

const decorateVars = computed(() => buildCateCenterV2Vars(moduleConfig.value));

// 商品卡由 masonry 内部注入 --pi-* 变量，这里按「专用色 → 主题色 → 不下发」逐级取值
const cateV2PriceColor = computed(() => moduleConfig.value.priceColor || moduleConfig.value.themeColor || undefined);
const cateV2BuyBtnColor = computed(() => moduleConfig.value.buyBtnColor || moduleConfig.value.themeColor || undefined);
const cateV2ProductTitleColor = computed(() => moduleConfig.value.productTitleColor || undefined);
const cateV2GoodsListBgColor = computed(() => moduleConfig.value.goodsListBgColor || undefined);

const params = reactive({
    categoryId: 0,
    page: 1,
    size: 10
});

const {
    data: list,
    getList,
    reachBottom,
    isLoading,
    total
} = useList(getCateProduct, {
    params,
    path: {
        dataKey: "records"
    },
    needReachBottom: false
});

const { styleMap } = useProductStyle(list);

const handleChange = () => {
    list.value = [];
    params.page = 1;
    if (subCurrentCateId.value === 0) {
        params.categoryId = currentCateId.value;
    } else {
        params.categoryId = subCurrentCateId.value;
    }
    getList();
};

defineExpose({
    refreshProductList: handleChange
});

const reset = () => {
    subCurrentCateId.value = 0;
    list.value = [];
    menuList.value = [];
    params.page = 1;
};

const handleMenu = () => {
    redirect({
        url: "/pages/index/index",
        success: function success() {}
    });
};
const handleSearch = () => {
    redirect({
        url: "/pages/search/index"
    });
};

const height = computed(() => {
    return `calc(${props.height} - 90rpx)`;
});
</script>
<style>
/* 兼容小程序 */
page {
    background-color: #fff !important;
}
</style>
<style lang="scss" scoped>
.main-header-spacer {
    width: 100%;
    flex-shrink: 0;
}

.scroll-box {
    height: v-bind("height");
    overflow: hidden;
}

.content-box {
    height: 100%;
    width: 100%;
    display: flex;

    .side {
        width: 90px;
        height: 100%;
        background-color: var(--cate-side-bg, #f7f7f7);
    }

    .list-box {
        width: calc(100% - 90px);
        height: 100%;

        .list-content {
            height: calc(100% - 100rpx);
            padding: 12rpx 12rpx 0;
            background-color: var(--cate-goods-bg, transparent);

            .list-scroll {
                height: 100%;
            }

            .empty-box {
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100%;
                width: 100%;
            }
        }
    }
}
</style>
