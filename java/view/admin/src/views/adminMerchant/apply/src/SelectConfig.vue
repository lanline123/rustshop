<template>
    <TigTabs v-model="value" :tabs="options" @onTabChange="onChange"></TigTabs>
</template>
<script setup lang="ts">
// 父组件回调
import {onMounted, ref} from "vue";
import {getApplyConfig, getApplyList} from "@/api/adminMerchant/apply";
import {message} from "ant-design-vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits([
    "callback",
])
onMounted(() => {
    loadFilter();
})
const value = ref(-1)
const onChange = (status: number)=>{
    value.value = status;
    emit('callback', value)
}
const options:any = ref([
    {
        label: t('全部'),
        value: -1,
        isShow: true
    },
])
const loadFilter = async () => {
    try {
        const result = await getApplyConfig();
        result.map((item: any) => {
            let obj = {
                label: item.statusText,
                value: item.status,
                isShow: true
            }
            options.value.push(obj);
        });
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
</script>
<style scoped lang="less">
.tabs {
    flex-wrap: wrap;
    gap: 10px !important;
    .item {
        padding: 0px 10px;
        text-align: center;
        height: 25px;
        line-height: 25px;
        color: #333;
        margin-right: 5px;
        font-size: 14px;
        border-radius: 2px;
        border: 1px solid #fff;
        cursor: pointer;
        &:hover {
            color: var(--tig-primary);
        }
    }
    .active {
        color: var(--tig-primary);
        background: var(--tig-item-active-bg);
        border: 1px solid var(--tig-primary);
    }
}
</style>
