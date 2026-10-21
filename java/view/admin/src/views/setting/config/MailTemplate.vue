/**
 * @file 邮件模板设置页
 * @description 编辑系统邮件通知模板
 * @features
 * - 选择邮件模板类型
 * - 编辑邮件主题和内容
 * - 富文本编辑器支持
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading">
                    <el-form ref="formRef" :model="formState" label-width="auto">
                        <el-form-item :label="$t('邮件模板')" prop="selTemplate">
                            <el-select v-model="formState.templateId" style="width: 100%">
                                <el-option v-for="item in filterState" :key="item.templateId" :label="item.templateName" :value="item.templateId" />
                            </el-select>
                        </el-form-item>
                        <el-form-item :label="$t('邮件主题')" prop="templateSubject">
                            <TigInput width="100%" v-model="formState.templateSubject" type="text" />
                        </el-form-item>
                        <el-form-item :label="$t('邮件内容')" prop="templateContent">
                            <Editor v-model:html="formState.templateContent"></Editor>
                        </el-form-item>
                    </el-form>
                </a-spin>
            </div>
        </div>
    </div>
    <div v-if="!loading" class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import type { MailTemplateFilterState } from "@/types/setting/mailTemplate.d";
import { getMailTemplateList, updateMailTemplate } from "@/api/setting/mailTemplate";
import { Editor } from "@/components/editor/index";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();

/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const confirmLoading = ref(false);
const loading = ref<boolean>(true);
const formRef = shallowRef();

/** 当前编辑的模板数据 */
const formState = ref<MailTemplateFilterState>({
    templateName: "",
    templateId: ""
});

/** 所有邮件模板列表 */
const filterState = ref<MailTemplateFilterState[]>([]);

/**
 * @description 获取邮件模板列表，默认选中第一个模板
 */
const loadFilter = async () => {
    try {
        const result = await getMailTemplateList();
        Object.assign(filterState.value, convertNullsToEmptyStrings(result));
        // 默认选中第一个模板
        if (filterState.value && filterState.value?.length > 0) {
            formState.value.templateId = filterState.value[0].templateId;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * @description 监听模板选择变化，切换模板时更新表单内容
 */
watch(
    () => formState.value.templateId,
    (newTemplateId) => {
        const template = filterState.value.find((item: MailTemplateFilterState) => item.templateId == newTemplateId);
        if (template) {
            Object.assign(formState.value, template);
        }
    }
);

onMounted(() => {
    loadFilter();
});

/**
 * @description 提交邮件模板修改
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateMailTemplate({ id: formState.value.templateId, ...formState.value });
        emit("submitCallback", result);
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
</style>
