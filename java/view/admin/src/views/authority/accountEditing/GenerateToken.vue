<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <template v-if="!generatedToken">
                    <el-form ref="formRef" :model="generateForm" :rules="formRules" label-width="auto">
                        <el-form-item :label="$t('名称/备注')" prop="name">
                            <el-input v-model="generateForm.name" :placeholder="$t('请输入 Token 用途备注')" maxlength="50" />
                        </el-form-item>
                        <el-form-item :label="$t('有效期')" prop="expiresInDays">
                            <el-select v-model="generateForm.expiresInDays" :placeholder="$t('请选择有效期')" style="width: 100%">
                                <el-option :label="$t('7 天')" :value="7" />
                                <el-option :label="$t('30 天')" :value="30" />
                                <el-option :label="$t('60 天')" :value="60" />
                                <el-option :label="$t('90 天')" :value="90" />
                                <el-option :label="$t('永不过期')" :value="null" />
                            </el-select>
                        </el-form-item>
                    </el-form>
                </template>
                <template v-else>
                    <el-alert
                        type="warning"
                        :closable="false"
                        show-icon
                        :title="$t('请立即复制并保存此 Token，关闭后将无法再次查看完整内容。')"
                        style="margin-bottom: 16px"
                    />
                    <div class="token-display">
                        <code class="token-text">{{ generatedToken }}</code>
                        <el-button type="primary" size="small" @click="copyToken">
                            {{ copied ? $t('已复制') : $t('复制') }}
                        </el-button>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { message } from "ant-design-vue";
import { generateApiToken } from "@/api/authority/accountEditing";
import type { ApiTokenGenerateParams } from "@/types/authority/accountEditing.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    isDialog: Boolean
});

const formRef = ref();
const generatedToken = ref("");
const copied = ref(false);

const generateForm = ref<ApiTokenGenerateParams>({
    name: "",
    expiresInDays: 30
});

const formRules = {
    name: [{ required: true, message: t('请输入名称/备注'), trigger: "blur" }]
};

const copyToken = async () => {
    try {
        await navigator.clipboard.writeText(generatedToken.value);
        copied.value = true;
        message.success(t('已复制到剪贴板'));
    } catch {
        message.error(t('复制失败，请手动复制'));
    }
};

const onSubmit = async () => {
    if (generatedToken.value) {
        emit("submitCallback", {});
        return;
    }
    await formRef.value.validate();
    emit("update:confirmLoading", true);
    try {
        const params: ApiTokenGenerateParams = {};
        if (generateForm.value.name) {
            params.name = generateForm.value.name;
        }
        if (generateForm.value.expiresInDays != null) {
            params.expiresInDays = generateForm.value.expiresInDays;
        }
        const result = await generateApiToken(params);
        generatedToken.value = result.token;
        message.success(t('Token 生成成功'));
    } catch (error: any) {
        message.error(error.message || t('生成 Token 失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.token-display {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
}

.token-text {
    flex: 1;
    word-break: break-all;
    font-size: 14px;
    color: #303133;
    user-select: all;
}
</style>
