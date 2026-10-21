/**
 * @file 售后服务设置页
 * @description 配置商品详情页的售后服务说明内容
 * @features
 * - 控制售后服务选项显示/隐藏
 * - 富文本编辑售后服务内容
 * - 支持多语言版本（海外版）
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="auto" style="display: flex; gap: 12px; flex-direction: column">
                <!-- 第一个盒子：开关控制 -->
                <div class="content_wrapper">
                    <el-form-item :label="$t('售后服务')">
                        <div>
                            <el-switch v-model="formState.showTemplateContent" :active-value="true" :inactive-value="false" />
                            <div class="extra"> {{ $t('此功能控制商品详情页,售后服务选项的显隐。') }} <el-popover :width="320" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/assets/setting/aftersales-service.png" style="width: 300px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <!-- 第二个盒子：富文本编辑器 -->
                <div class="content_wrapper">
                    <el-form-item :label="$t('售后服务')" prop="templateContent">
                        <Editor v-if="!loading" v-model:html="formState.templateContent"></Editor>
                    </el-form-item>
                </div>
            </el-form>

            <div v-if="!loading" class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '370px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, reactive } from "vue";
import { message } from "ant-design-vue";
import type { AfterSalesServiceFilterState } from "@/types/setting/afterSalesService.d";
import { getAfterSalesSettings, updateAfterSalesSettings } from "@/api/setting/afterSalesService";
import { Editor } from "@/components/editor/index";
import { useThemeStore } from "@/store/theme";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { isOverseas } from "@/utils/version";

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

/** 表单数据：售后服务内容和显示开关 */
const formState = ref<AfterSalesServiceFilterState>({
    templateContent: "",
    showTemplateContent: true
});

/**
 * @description 获取售后服务设置数据
 */
const loadFilter = async () => {
    try {
        const result = await getAfterSalesSettings();
        formState.value = {
            templateContent: result.templateContent || "",
            // 接口返回 undefined 时默认显示售后服务
            showTemplateContent: result.showTemplateContent !== undefined ? result.showTemplateContent : true
        };
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadFilter();
});

/**
 * @description 提交售后服务设置
 */
const onSubmit = async () => {
    console.log(formState.value);
    try {
        emit("update:confirmLoading", true);
        const result = await updateAfterSalesSettings(formState.value);
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
.content_wrapper:last-child {
    flex: 1;
    min-height: calc(100vh - 390px);
}
</style>
