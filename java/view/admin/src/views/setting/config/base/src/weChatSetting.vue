<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="180px" style="margin-left: 22px">
                <div class="content_wrapper">
                    <div class="title">{{ $t('小程序直播/视频号配置') }}</div>
                    <el-form-item :label="$t('小程序AppID')" prop="wechatLiveMiniAppId">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.wechatLiveMiniAppId" />
                            <div class="extra">{{ $t('小程序直播/视频号使用的独立小程序AppID，微信小程序公众平台->设置->开发设置中获取') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('小程序AppSecret')" prop="wechatLiveMiniAppSecret">
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ maskSecret(formState.wechatLiveMiniAppSecret) }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: '小程序AppSecret', content: formState.wechatLiveMiniAppSecret }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('小程序AppSecret')"
                                    width="600px"
                                    @okCallback="formState.wechatLiveMiniAppSecret = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('小程序直播/视频号使用的独立小程序AppSecret，微信小程序公众平台->设置->开发设置中获取') }}</div>
                            <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的小程序AppSecret') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('风控配置') }}</div>
                    <el-form-item :label="$t('图片鉴黄')" prop="miniImgSecCheck">
                        <div>
                            <el-radio-group v-model="formState.miniImgSecCheck" class="itemWidth">
                                <el-radio :value="1">{{ $t('开启') }}</el-radio>
                                <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('开启后，小程序端上传头像时将调用微信内容安全接口检测，违规图片将被拦截') }}</div>
                            <div class="extra">{{ $t('微信限制单个小程序每分钟最多调用 2000 次、每天最多 200000 次，超出限制后本次检测将自动放行') }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 30px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { getWechatLiveSettings, saveWechatLiveSettings } from "@/api/setting/config";
import { getMiniProgramRiskSettings, saveMiniProgramRiskSettings } from "@/api/setting/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);

interface MiniProgramSettingForm {
    wechatLiveMiniAppId: string;
    wechatLiveMiniAppSecret: string;
    miniImgSecCheck?: number;
}

const formState = ref<MiniProgramSettingForm>({
    wechatLiveMiniAppId: "",
    wechatLiveMiniAppSecret: "",
    miniImgSecCheck: 0
});

const maskSecret = (secret: string) => {
    if (!secret) return "";
    if (secret.length <= 8) return "********";
    return secret.slice(0, 4) + "****" + secret.slice(-4);
};

onMounted(async () => {
    await loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const riskResult = await getMiniProgramRiskSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(riskResult));
        const liveResult = await getWechatLiveSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(liveResult));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        await saveWechatLiveSettings({
            wechatLiveMiniAppId: formState.value.wechatLiveMiniAppId,
            wechatLiveMiniAppSecret: formState.value.wechatLiveMiniAppSecret
        });
        await saveMiniProgramRiskSettings({
            miniImgSecCheck: formState.value.miniImgSecCheck
        });
        message.success(t("修改成功"));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>

<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.content_wrapper + .content_wrapper {
    margin-top: 20px;
}

.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-top: 20px;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }
}

.mr8 {
    margin-right: 8px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}
</style>
