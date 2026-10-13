<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                    <el-form ref="formRef" :model="formData" label-width="auto">
                        <el-form-item prop="key" :rules="[{ required: true, validator: validateKey }]">
                            <div class="input-wrapper">
                                <TigInput v-model="formData.key" type="textarea" :rows="5" :placeholder="$t('请输入{title}', { title })" width="550px">
                                </TigInput>
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
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    title: {
        type: String,
        default: ""
    },
    content: {
        type: String,
        default: ""
    }
});
// 父组件回调
const formRef = shallowRef();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const loading = ref<boolean>(false);
const formData = ref({
    key: props.content
});
const validateKey = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t("请输入{title}", { title: props.title })));
        return;
    }
    if (value.includes("*")) {
        callback(new Error(t("输入的{title}不能包含*号", { title: props.title })));
        return;
    } else {
        callback();
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    emit("submitCallback", formData.value.key);
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
