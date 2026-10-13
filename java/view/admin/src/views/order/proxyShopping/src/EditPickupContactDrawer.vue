<template>
    <div class="pickup-contact-drawer">
        <el-form v-if="!loading" ref="formRef" label-width="88px" :model="formState" :rules="rules" autocomplete="off">
            <el-form-item :label="$t('姓名')" prop="contactName">
                <el-input v-model="formState.contactName" :maxlength="20" :placeholder="$t('请输入姓名')" />
            </el-form-item>
            <el-form-item :label="$t('电话号码')" prop="contactPhone">
                <el-input v-model="formState.contactPhone" :maxlength="20" :placeholder="$t('请输入电话号码')" />
            </el-form-item>
        </el-form>
        <a-spin :spinning="loading" style="width: 100%; margin-top: 40px" />
    </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { createProxyPickupContact } from "@/api/proxyShopping/orderCheck";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps<{
    userId: number;
    isDialog?: boolean;
}>();

const loading = ref(false);
const formRef = shallowRef();

const formState = reactive({
    userId: 0,
    contactName: "",
    contactPhone: ""
});

onMounted(() => {
    formState.userId = Number(props.userId || 0);
});

const rules = reactive<any>({
    contactName: [{ required: true, message: t('请填写姓名'), trigger: "blur" }],
    contactPhone: [{ required: true, message: t('请填写电话号码'), trigger: "blur" }]
});

const onFormSubmit = async () => {
    await (formRef.value as any)?.validate?.();
    try {
        emit("update:confirmLoading", true);
        loading.value = true;
        const payload = {
            userId: Number(formState.userId || 0),
            contactName: String(formState.contactName || "").trim(),
            contactPhone: String(formState.contactPhone || "").trim()
        };
        const contactId = await createProxyPickupContact(payload as any);
        message.success(t('操作成功'));
        emit("submitCallback", {
            ...payload,
            id: Number(contactId || 0) || undefined,
            contactId: Number(contactId || 0) || undefined
        });
    } catch (e: any) {
        message.error(e?.message || t('保存失败，请检查必填项'));
    } finally {
        loading.value = false;
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less"></style>
