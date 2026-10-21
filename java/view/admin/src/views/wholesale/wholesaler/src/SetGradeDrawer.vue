/**
 * @file 设置批发商等级抽屉
 * @description 等级下拉（仅启用中），确定调 changeGrade
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                <el-form-item prop="gradeId" :label="$t('批发商等级')">
                    <el-select v-model="formState.gradeId" :placeholder="$t('请选择批发商等级')" style="width: 300px">
                        <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                    </el-select>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, shallowRef, onMounted } from "vue";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { getGradeList } from "@/api/wholesale/grade";
import { changeWholesalerGrade } from "@/api/wholesale/wholesaler";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    wholesalerId: { type: Number, default: 0 },
    gradeId: { type: Number, default: undefined }
});

const loading = ref<boolean>(true);
const formRef = shallowRef();
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const formState = reactive<{ gradeId: number | undefined }>({ gradeId: props.gradeId });

const rules = reactive<FormRules>({
    gradeId: [{ required: true, message: t('请选择批发商等级'), trigger: "change" }]
});

onMounted(async () => {
    try {
        gradeOptions.value = (await getGradeList({ status: 1, withUserCount: false })) || [];
    } catch (error: any) {
        message.error(error.message || t('等级加载失败'));
    } finally {
        loading.value = false;
    }
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        await changeWholesalerGrade({ wholesalerId: props.wholesalerId, gradeId: formState.gradeId as number });
        message.success(t('操作成功'));
        emit("submitCallback", true);
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();
defineExpose({ onFormSubmit });
</script>
