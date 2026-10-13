<template>
    <Agreement v-model:form-state="formState" @submitCallback="onSubmit" :confirmLoading="confirmLoading" :loading="loading"></Agreement>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { AgreementConfig } from "@/types/content/agreement";
import { saveLoginProtocol, getLoginProtocolContent } from "@/api/vendor/setting";
import Agreement from "./src/Agreement.vue";
// 基本参数定义
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = ref<boolean>(false);
const formState = ref<AgreementConfig>({
    show: 0,
    content: ""
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getLoginProtocolContent({ code: "afterSalesService" });
        formState.value = result
        loading.value = false;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveLoginProtocol({...formState.value, code: "afterSalesService" });
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>

</style>
