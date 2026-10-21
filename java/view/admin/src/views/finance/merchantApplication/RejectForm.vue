<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form ref="formRef" :model="formState" label-width="90px">
                <el-form-item :label="$t('驳回原因')" prop="rejectReason" :rules="[{ required: true, message: $t('请填写驳回原因'), trigger: 'blur' }]">
                    <el-input
                        v-model="formState.rejectReason"
                        type="textarea"
                        :rows="4"
                        maxlength="500"
                        show-word-limit
                        :placeholder="$t('请填写驳回原因，将展示给申请方')"
                    />
                </el-form-item>
                <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                    <el-button class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("确认拒绝") }}</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { rejectMerchantApplication } from "@/api/finance/merchantApplication";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: { type: [Number, String], default: 0 },
    isDialog: Boolean
});

const query = useRouter().currentRoute.value.query;
const id = ref<number | string>(props.isDialog ? props.id : String(query.id));

const formRef = shallowRef();
const formState = ref<{ rejectReason: string }>({ rejectReason: "" });

const onSubmit = async () => {
    await formRef.value.validate();
    if (!id.value) return;
    try {
        emit("update:confirmLoading", true);
        const result = await rejectMerchantApplication({ id: id.value, rejectReason: formState.value.rejectReason.trim() });
        message.success(t("已拒绝"));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message || t("操作失败"));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
