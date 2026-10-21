<template>
    <div class="link-style">
        <div class="left box">
            <div v-for="item in menuList" :key="item.key" :class="{ active: selectedKey === item.key }" class="list-title" @click="selectItem(item.key)">
                <div>{{ item.label }}</div>
                <div class="right-ico"><em class="main_pel_m iconfont">&#xe630;</em></div>
            </div>
        </div>

        <div class="right box" :validate-event="false">
            <template v-if="selectedKey === 'product'">
                <SelectProduct
                    v-model:linkSelectData="selectData"
                    :modelValue="selectData.data || {}"
                    :isMultiple="false"
                    :excludeShopId="excludeShopId"
                ></SelectProduct>
            </template>
            <template v-if="selectedKey === 'category'">
                <LinkCategory v-model:linkSelectData="selectData" :slide="false"></LinkCategory>
            </template>
            <template v-if="selectedKey === 'brand'">
                <SelectBrand v-model:linkSelectData="selectData" :isMultiple="false"></SelectBrand>
            </template>
            <template v-if="selectedKey === 'coupon'">
                <SelectCoupon v-model:linkSelectData="selectData" :isMultiple="false"></SelectCoupon>
            </template>
            <template v-if="selectedKey === 'article'">
                <SelectArticle v-model:linkSelectData="selectData" :isMultiple="false"></SelectArticle>
            </template>
            <template v-if="selectedKey === 'custom'">
                <LinkCustom v-model:linkSelectData="selectData"></LinkCustom>
            </template>
            <template v-if="selectedKey === 'base'">
                <LinkBase v-model:linkSelectData="selectData" :type="type"></LinkBase>
            </template>
            <template v-if="selectedKey === 'shop'">
                <SelectShop v-model:linkSelectData="selectData" :isMultiple="false" :includeSelfOperated="type === 'mobile'"></SelectShop>
            </template>
            <template v-if="selectedKey === 'decorate'">
                <LinkDecorate v-model:linkSelectData="selectData" :isMultiple="false"></LinkDecorate>
            </template>
            <template v-if="selectedKey === 'customPcPage'">
                <LinkCustomPcPage v-model:linkSelectData="selectData" :isMultiple="false"></LinkCustomPcPage>
            </template>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from "vue";
import LinkBase from "@/views/link/src/LinkBase.vue";
import LinkDecorate from "@/views/link/src/LinkDecorate.vue";
import LinkCategory from "@/views/link/src/LinkCategory.vue";
import LinkCustom from "@/views/link/src/LinkCustom.vue";
import LinkCustomPcPage from "@/views/link/src/LinkCustomPcPage.vue";
import SelectProduct from "@/views/product/product/src/SelectProduct.vue";
import SelectBrand from "@/views/product/brand/src/SelectBrand.vue";
import SelectArticle from "@/views/content/article/src/SelectArticle.vue";
import SelectShop from "@/views/shop/shop/src/SelectShop.vue";
import SelectCoupon from "@/views/promotion/coupon/src/SelectCoupon.vue";
import type { LinkType } from "@/types/decorate/decorate";
import { isMerchant, isStore } from "@/utils/version";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const adminType = ref(localStorage.getItem("adminType"));
const props = defineProps({
    type: { type: String, default: "" },
    currentValue: { type: Object, default: () => ({}) },
    // 装修选链：列表查询不带 shopId；非装修入口保持默认 false
    excludeShopId: { type: Boolean, default: false }
});
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "okType"]);
emit("okType", false);
const selectData = ref(<LinkType>{});

const menuList = computed(() => {
    const list = [
        { key: "product", label: t("商品详情页") },
        { key: "category", label: t("分类搜索") },
        { key: "brand", label: t("品牌搜索") },
        { key: "coupon", label: t("优惠券") },
        { key: "article", label: t("文章资讯") },
        { key: "custom", label: t("自定义链接") },
        { key: "base", label: t("其它页面") }
    ];
    if (adminType.value === "admin" && (isMerchant() || isStore())) {
        list.push({ key: "shop", label: isStore() ? t("门店搜索") : t("店铺搜索") });
    }
    if (props.type === "mobile") {
        list.push({ key: "decorate", label: t("装修页面") });
    } else {
        list.push({ key: "customPcPage", label: t("自定义装修页") });
    }
    return list;
});

const selectedKey = ref<string>("product");
const pathToKey: Record<string, string> = {
    product: "product",
    list: "category",
    brand: "brand",
    coupon: "coupon",
    article: "article",
    custom: "custom",
    base: "base",
    shop: "shop",
    store: "shop",
    decorate: "decorate"
};
const normalizeLink = (link: unknown): string => String(link ?? "").replace(/^\/+|\/+$/g, "");
const getSelectedKey = (value: LinkType): string => {
    if (!value || Object.keys(value).length === 0) {
        return "product";
    }
    if (value.label === t("自定义装修页") || value.path?.startsWith("activity?id=")) {
        return "customPcPage";
    }
    // 历史装修数据中的基础页面链接有时只保存 link/name，没有 path。
    // 先按 URL 识别会员页面，避免把“我的优惠券”（member/coupon/list）
    // 误判为“优惠券”商品选择器。
    const normalizedLink = normalizeLink(value.link);
    if (value.path === "home" || value.path === "balance") {
        return "base";
    }
    if (
        normalizedLink === "user/account/index" ||
        normalizedLink === "member" ||
        normalizedLink.startsWith("member/")
    ) {
        return "base";
    }
    return pathToKey[value.path ?? ""] ?? "product";
};
const initCurrentValue = (value: LinkType) => {
    const currentValue = value && Object.keys(value).length > 0 ? { ...value } : {};
    // 兼容旧配置：基础页面链接可能未保存 path，补齐后 LinkBase 才能
    // 根据 name 回显对应按钮（例如“我的优惠券”）。
    const normalizedLink = normalizeLink(currentValue.link);
    if (currentValue.path === "home" || currentValue.path === "balance") {
        currentValue.path = "base";
    } else if (
        !currentValue.path &&
        (normalizedLink === "user/account/index" || normalizedLink === "member" || normalizedLink.startsWith("member/"))
    ) {
        currentValue.path = "base";
    }
    selectData.value = currentValue;
    selectedKey.value = getSelectedKey(currentValue);
    emit("okType", Object.keys(currentValue).length > 0);
};
const selectItem = (key: string) => {
    if (selectedKey.value !== key) {
        selectData.value = {};
    }
    selectedKey.value = key;
};
watch(
    () => props.currentValue,
    (newValue) => {
        initCurrentValue(newValue as LinkType);
    },
    { deep: true, immediate: true }
);
watch(
    () => selectData.value,
    (newValue) => {
        if (Object.keys(newValue).length !== 0) {
            emit("okType", true);
        } else {
            emit("okType", false);
        }
    }
);
const onSubmit = () => {
    emit("submitCallback", selectData.value);
};
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.link-style {
    display: flex;
    gap: 10px;
    min-height: 100%;

    .box {
        border: 1px solid #eee;
        border-radius: 3px;
        padding: 0 0 4px 0;
        white-space: nowrap;
    }

    .left {
        min-width: 150px;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .list-title {
            width: 100%;
            height: 40px;
            display: flex;
            box-sizing: border-box;
            padding: 0 15px 5px 15px;
            justify-content: start;
            line-height: 40px;
            cursor: pointer;

            .right-ico {
                flex: 1;
                text-align: right;
            }
        }

        .list-title.active {
            background-color: #eef2ff;
            color: #428ce8;
        }
    }

    .right {
        padding: 10px;
        flex: 1;
    }
}
</style>
