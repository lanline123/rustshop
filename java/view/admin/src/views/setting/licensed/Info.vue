<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                    <el-form ref="formRef" :model="formData" label-width="auto">
                        <el-form-item :label="$t('授权码：')" prop="license" :rules="[{ required: true, message: '请输入授权码!' }]">
                            <div class="input-wrapper">
                                <TigInput v-model="formData.license" type="textarea" :rows="5" :placeholder="$t('请输入授权码')"> </TigInput>
                            </div>
                        </el-form-item>
                    </el-form>
                </a-spin>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { licensedUpdate, getLicensed } from "@/api/setting/licensed";
import { formatDomain } from "@/utils/format";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const loading = ref<boolean>(false);

const formData = ref({
    license: ""
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const result = await licensedUpdate({ ...formData.value, domain: formatDomain(window.location.origin) });
        message.success(t('操作成功'));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.input-wrapper {
    width: 95%;
}
</style>
