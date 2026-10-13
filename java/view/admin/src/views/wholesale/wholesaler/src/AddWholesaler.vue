/** * @file 手动添加批发商抽屉 * @description 会员(必填，复用 SelectUser 单选) + 等级(必填)；添加即启用 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                <el-form-item prop="gradeId" :label="$t('批发商等级')">
                    <el-select v-model="formState.gradeId" :placeholder="$t('请选择批发商等级')">
                        <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                    </el-select>
                </el-form-item>
                <el-form-item prop="userId" :label="$t('选择会员')">
                        <SelectUser :multiple="false" v-model:userId="formState.userId" @change="onUserChange" />
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, shallowRef, onMounted } from "vue";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import { SelectUser } from "@/components/select";
import { getGradeList } from "@/api/wholesale/grade";
import { addWholesaler } from "@/api/wholesale/wholesaler";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const loading = ref<boolean>(true);
const formRef = shallowRef();
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const formState = reactive<{ userId: number | undefined; gradeId: number | undefined }>({
    userId: undefined,
    gradeId: undefined
});

const rules = reactive<FormRules>({
    userId: [{ required: true, message: t('请选择会员'), trigger: "change" }],
    gradeId: [{ required: true, message: t('请选择批发商等级'), trigger: "change" }]
});

const onUserChange = (user: any) => {
    formState.userId = user?.userId;
};

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
        const result = await addWholesaler({
            userId: formState.userId as number,
            gradeId: formState.gradeId as number
        });
        message.success(t('操作成功'));
        emit("submitCallback", result);
    } catch (error: any) {
        // 失败码 5301/5302/5303/5304/5300 由后端文案透传
        message.error(error.message || t('添加失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => onSubmit();
defineExpose({ onFormSubmit });
</script>
