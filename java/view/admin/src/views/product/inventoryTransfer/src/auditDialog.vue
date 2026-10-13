<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('处理申请：')" prop="checkStatus">
                        <div>{{ formState.auditType === 2 ? $t('审核通过') : $t('审核驳回') }}</div>
<!--                        <el-radio-group style="width: 100%" v-model="formState.auditType" :disabled="disabled">-->
<!--                            <el-radio :value="2">审核通过</el-radio>-->
<!--                            <el-radio :value="5">审核驳回</el-radio>-->
<!--                        </el-radio-group>-->
                    </el-form-item>
                    <el-form-item v-if="formState.auditType === 5" :label="$t('审核驳回原因：')" prop="remark" :rules="[{ required: true, message: '审核驳回原因不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.remark" :rows="2" type="textarea" :placeholder="$t('请输入审核驳回原因')"></TigInput>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { AuditAllocate } from "@/types/product/inventoryTransfer.d";
import { auditAllocate } from "@/api/product/inventoryTransfer";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    auditType: {
        type: Number,
        default: 2
    },
    disabled: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(false);
const formRef = shallowRef();
const formState = ref<AuditAllocate>({
    auditType: props.auditType,
    remark: "",
    id: props.id
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await auditAllocate({ ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.content_wrapper {
    padding: 10px 28px;
}
</style>
