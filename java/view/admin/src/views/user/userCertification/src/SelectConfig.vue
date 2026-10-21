/** 认证状态筛选器 - 切换全部/待审核/审核通过/审核未通过 */
<template>
    <TigTabs v-model="value" :tabs="options" @onTabChange="onChange"></TigTabs>
</template>
<script setup lang="ts">
import { ref } from "vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["callback"]);
const value = ref(0);
const onChange = (status: number) => {
    value.value = status;
    emit("callback", value);
};
const options: any = [
    {
        value: 0,
        label: t('全部'),
        isShow: true
    },
    {
        value: 1,
        label: t('待审核'),
        isShow: true
    },
    {
        value: 2,
        label: t('审核通过'),
        isShow: true
    },
    {
        value: 3,
        label: t('审核未通过'),
        isShow: true
    }
];
</script>
