<template>
    <div class="gift-type-modal">
        <div class="bonus-wrap">
            <RadioType v-model:modelValue="groupType" :radioList="radioList" width="210px" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from "vue";
import { getAdminType, getShopType } from "@/utils/storage";
import { RadioType } from "@/components/radio";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "okType"]);

// 是否是店铺
const isShop = computed(() => {
    return getAdminType() === "shop" && [1, 2].includes(getShopType());
});

const groupType = ref<number>();

const radioList = computed(() => {
    const list = [
        {
            key: 1,
            title: t('储值卡'),
            desc: t('例：储值卡购买可进行兑换\\n金额直接充入账户余额中')
        },
        {
            key: 2,
            title: t('兑换卡'),
            desc: t('例：可兑换该卡绑定的商品\\n增强品牌与营销效果')
        }
    ];
    return isShop.value ? list.filter((x) => x.key !== 1) : list;
});

const canSubmit = computed(() => groupType.value === 1 || groupType.value === 2);

watchEffect(() => {
    // 默认选中第一个可选项（仅在未选择时初始化，避免覆盖用户操作）
    if (groupType.value !== 1 && groupType.value !== 2) {
        const first = radioList.value?.[0]?.key;
        if (first === 1 || first === 2) groupType.value = first;
    }
    emit("okType", canSubmit.value);
});

const onFormSubmit = async () => {
    if (!canSubmit.value) return;
    emit("submitCallback", { groupType: groupType.value }, true);
};

defineExpose({ onFormSubmit });
</script>

<style lang="scss" scoped>
.gift-type-modal {
    padding: 8px 4px;
}

.bonus-wrap {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-bottom: 15px;
    :deep(.radio-style) {
        gap: 20px;
    }

    .subtitle {
        font-weight: 500;
        font-size: 14px;
        color: #101010;
        line-height: 17px;
    }
}
</style>
