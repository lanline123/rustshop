<template>
    <div class="div2">
        <div class="notice-warp">
            <p>{{ $t('提示：该分类进入的是{slide}页面。', { slide: slide ? "滑动切换分类商品瀑布流" : "按分类搜索的结果" }) }}</p>
        </div>{{ $t('选择分类页面{slide}：', { slide: slide ? "（滑动）" : "" }) }}<SelectCategory v-model:categoryId="categoryId" v-model:categoryName="categoryName"></SelectCategory>
    </div>
</template>
<script lang="ts" setup>
import { SelectCategory } from "@/components/select";
import { ref, watch } from "vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    modelValue: { type: Number, default: 0 },
    slide: { type: Boolean, default: false }
});
const emit = defineEmits(["update:modelValue"]);
const categoryName = ref("");
const categoryId = ref(0);
const linkSelectData = defineModel("linkSelectData");
watch(
    () => props.modelValue,
    (newValue) => {
        if (newValue) {
            categoryId.value = newValue;
        }
    },
    { immediate: true }
);
watch(
    () => linkSelectData.value,
    (newValue) => {
        if (newValue?.path === "list") {
            categoryId.value = Number(newValue.id || 0);
            categoryName.value = newValue.name || "";
        }
    },
    { deep: true, immediate: true }
);
watch(categoryId, (newValue) => {
    emit("update:modelValue", newValue);
    if (newValue) {
        linkSelectData.value = {
            path: "list",
            label: t('分类'),
            id: newValue,
            name: categoryName.value,
            data: {
                id: newValue,
            }
        };
    }
});
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
