/**
 * @file 钉钉通知设置组件
 * @description 配置钉钉群机器人通知
 * @features
 * - 设置钉钉群机器人Token
 * - 配置钉钉AppSecret
 * - 订单通知推送到钉钉群
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="title">{{ $t('钉钉通知设置') }}</div>
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('钉钉群机器人token')" prop="dingTalkRobotToken">
                        <TigInput classType="tig-form-input" v-model="formState.dingTalkRobotToken" />
                    </el-form-item>
                    <el-form-item :label="$t('钉钉appsecret')" prop="dingAppSecret">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.dingAppSecret" />
                            <div class="extra"> {{ $t('可在“钉钉开放平台-应用开发-选择应用-机器人”页面获取。') }}<el-popover :width="930" placement="bottom" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('查看示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/ding.png" style="width: 900px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                        <!-- <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.dingAppSecret }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: '短信帐户密码', content: formState.dingAppSecret }"
                                    path="setting/config/src/EditSecret"
                                    title="短信帐户密码"
                                    width="600px"
                                    @okCallback="formState.dingAppSecret = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">您所申请的短信服务密码，编辑完成后，点击下方提交按钮确认修改，请确保填写正确的短信帐户密码</div>
                        </div> -->
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
import { Image } from "@/components/image";
import { onMounted, ref, shallowRef } from "vue";
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import type { DingFormState } from "@/types/setting/mail.d";
import { getDingConfig, saveDingConfig } from "@/api/setting/mail";
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
const formState = ref<Partial<DingFormState>>({});
const loadFilter = async () => {
    try {
        const result = await getDingConfig();
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
        const result = await saveDingConfig(formState.value);
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
