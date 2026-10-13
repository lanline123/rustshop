/**
 * @file 企业微信通知设置组件
 * @description 配置企业微信群机器人通知
 * @features
 * - 设置企业微信群消息机器人Webhook地址
 * - 订单通知推送到企业微信群
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="title">{{ $t('企业微信通知设置') }}</div>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('企业微信群消息机器人')" prop="wxworkWebhookUrl">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.wxworkWebhookUrl" />
                            <div class="extra"> {{ $t('可在“企业微信-创建机器人-机器人名片”页面获取。') }}<el-popover :width="630" placement="right" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('查看示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/wxwork.png" style="width: 600px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </el-form>
            </a-spin>
        </div>
    </div>
    <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { WXWorkFormState } from "@/types/setting/mail.d";
import { getWXWorkConfig, saveWXWorkConfig } from "@/api/setting/mail";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();

// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const confirmLoading = ref(false);

const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<Partial<WXWorkFormState>>({});
const loadFilter = async () => {
    try {
        const result = await getWXWorkConfig();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    loadFilter();
});

// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveWXWorkConfig(formState.value);
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.itemWidth {
    width: 100%;
}

.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
</style>
