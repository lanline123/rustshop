/**
 * @file 批发商等级新增/编辑抽屉
 * @description 字段：gradeName(必填1-50唯一)；无 code / 无 isDefault
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item prop="gradeName" :label="$t('批发商等级')">
                        <TigInput v-model="formState.gradeName" :maxlength="50" width="350px" :placeholder="$t('请输入批发商等级')" />
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, shallowRef, onMounted } from "vue";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { saveGrade } from "@/api/wholesale/grade";
import type { WholesalerGradeSaveDTO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: { type: Number, default: 0 },
    detail: { type: Object, default: () => ({}) }
});

const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<WholesalerGradeSaveDTO>({
    gradeId: props.id || undefined,
    gradeName: ""
});

const rules = reactive<FormRules>({
    gradeName: [
        { required: true, message: t('请输入批发商等级'), trigger: "blur" },
        { min: 1, max: 50, message: t('长度为 1-50 个字符'), trigger: "blur" }
    ]
});

onMounted(() => {
    if (props.id && props.detail) {
        formState.value = {
            gradeId: props.id,
            gradeName: props.detail.gradeName || ""
        };
    }
    loading.value = false;
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await saveGrade({
            gradeId: formState.value.gradeId,
            gradeName: formState.value.gradeName
        });
        message.success(t('操作成功'));
        emit("submitCallback", result);
    } catch (error: any) {
        // 409 等级名重复等错误由后端文案透传
        message.error(error.message || t('保存失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
