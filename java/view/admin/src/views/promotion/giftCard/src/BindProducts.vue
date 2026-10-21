<template>
    <DialogForm
        isDrawer
        @okCallback="onOk"
        :title="$t('管理商品')"
        width="1000px"
        path="promotion/giftCard/src/SelectProduct"
        :params="{
            selectedIds: products,
            excludeProductTypes: [7, 8]
        }"
    >
        <a class="btn-link">{{ $t('管理商品') }}</a>
    </DialogForm>
</template>
<script setup lang="ts">
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import { updpateiftCardGroupProducts } from "@/api/promotion/giftCard";
import type { GiftCardGroupProducts, GiftCardGroupProduct } from "@/types/promotion/giftCard";
import { onMounted, ref } from "vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    cardGroupsId: number;
    products: GiftCardGroupProduct[];
}>();
const emit = defineEmits(["okCallback"]);
const onOk = async (data: any) => {
    if (!data || !data.length) {
        message.error(t('请选择商品'));
        return;
    }
    try {
        const formData: GiftCardGroupProducts = {
            cardGroupsId: props.cardGroupsId,
            giftCardProducts: data.map((item: any) => {
                return {
                    cardGroupsId: props.cardGroupsId,
                    productId: item.productId,
                    skuId: item.skuId
                };
            })
        };
        await updpateiftCardGroupProducts(formData);
        message.success(t('操作成功'));
        emit("okCallback");
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>
