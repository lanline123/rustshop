<template>
    <WholesaleSkuPopup
        v-if="ready"
        ref="popupRef"
        list-mode
        :product="product"
        :sku-list="skuList"
        :attr-list="attrList"
        :price-list="wholesalePriceList"
        :current-grade-id="wholesaleStore.identity.gradeId"
        :buy-rule="buyRule"
        @add-success="onAddSuccess"
        @close="onClose"
    />
</template>

<script lang="ts" setup>
/**
 * @description 列表页批发 SKU 半窗宿主（与 specification.vue 的 v-model + loadEnd 约定对齐）
 * - 父组件设置 modelValue=productId 后拉详情，loadEnd 回调里调 handleShowPopup 打开半窗
 * - wholesalePriceList 为空时 emit loadEnd({ wholesale: false })，父组件可回退零售 specification
 */
import { ref, watch, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import WholesaleSkuPopup from "./WholesaleSkuPopup.vue";
import { getProductDetail } from "@/api/product/product";
import { getWholesaleBuyRule } from "@/api/wholesale/product";
import { useWholesaleStore } from "@/store/wholesale";
import type { ProductItem, SkuList, AttrList } from "@/types/product/product";
import type { WholesalePriceTier } from "@/types/wholesale/price";
import type { BuyRule } from "@/types/wholesale/buyRule";

const props = defineProps({
    modelValue: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits<{
    (e: "update:modelValue", value: number): void;
    (e: "loadEnd", payload: { wholesale: boolean }): void;
    (e: "close"): void;
    (e: "add-success", count: number): void;
}>();

const { t } = useI18n();
const wholesaleStore = useWholesaleStore();
const popupRef = ref<InstanceType<typeof WholesaleSkuPopup>>();
const ready = ref(false);
const product = ref<ProductItem>({} as ProductItem);
const skuList = ref<SkuList[]>([]);
const attrList = ref<AttrList>({ normal: [], spe: [] });
const wholesalePriceList = ref<WholesalePriceTier[]>([]);
const buyRule = ref<BuyRule | null>(null);

const reset = () => {
    ready.value = false;
    buyRule.value = null;
    wholesalePriceList.value = [];
};

const loadDetail = async (productId: number) => {
    reset();
    try {
        const result = await getProductDetail(productId);
        product.value = result.item;
        skuList.value = result.skuList || [];
        attrList.value = result.attrList || { normal: [], spe: [] };
        wholesalePriceList.value = Array.isArray(result.wholesalePriceList) ? result.wholesalePriceList : [];

        if (wholesalePriceList.value.length === 0) {
            emit("loadEnd", { wholesale: false });
            return;
        }

        try {
            const rule = await getWholesaleBuyRule(productId);
            buyRule.value = rule as unknown as BuyRule;
        } catch (err) {
            console.error("[wholesale-list-spec] loadBuyRule failed:", err);
        }

        ready.value = true;
        await nextTick();
        emit("loadEnd", { wholesale: true });
    } catch (err: any) {
        uni.showToast({ title: err?.message || t("加载失败"), icon: "none" });
        emit("loadEnd", { wholesale: false });
    }
};

watch(
    () => props.modelValue,
    (id) => {
        if (id && id > 0) {
            loadDetail(id);
        } else {
            reset();
        }
    },
    { immediate: true }
);

const handleShowPopup = (isQuick = false) => {
    popupRef.value?.handleShowPopup(isQuick);
};

const onClose = () => {
    emit("update:modelValue", 0);
    emit("close");
};

const onAddSuccess = (count: number) => {
    emit("add-success", count);
};

defineExpose({ handleShowPopup });
</script>
