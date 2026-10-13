/**
 * @file 短信通知设置组件
 * @description 多服务商短信参数配置（阿里云 / 腾讯云 / 华为云 / 火山引擎）
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form :model="formState" label-width="140px">
                <div class="content_wrapper">
                    <div class="title">{{ $t('短信通知设置') }}</div>
                    <el-form-item :label="$t('服务商')" required>
                        <el-radio-group v-model="formState.smsProvider" class="itemWidth">
                            <el-radio :value="1">{{ $t('阿里云') }}</el-radio>
                            <el-radio :value="2">{{ $t('腾讯云') }}</el-radio>
                            <el-radio :value="3">{{ $t('华为云') }}</el-radio>
                            <el-radio :value="4">{{ $t('火山引擎') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <template v-if="formState.smsProvider === 1">
                        <el-form-item :label="$t('短信帐户用户名')" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsKeyId }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '短信帐户用户名', content: formState.smsKeyId }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('短信帐户用户名')"
                                        width="600px"
                                        @okCallback="formState.smsKeyId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('您所申请的短信服务用户名，不填则表示不开通') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信帐户密码')" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsKeySecret }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: '短信帐户密码', content: formState.smsKeySecret }"
                                        path="setting/config/src/EditSecret"
                                        :title="$t('短信帐户密码')"
                                        width="600px"
                                        @okCallback="formState.smsKeySecret = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后点击下方提交保存；请确保密码正确') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信签名')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsSignName" />
                                <div class="extra">{{ $t('阿里短信此处填短信签名') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('商家短信号码')">
                            <div>
                                <TigInput classType="tig-form-input" type="integer" v-model="formState.smsShopMobile" />
                                <div class="extra">{{ $t('用于接收会员订单等信息') }}</div>
                            </div>
                        </el-form-item>
                    </template>

                    <template v-if="formState.smsProvider === 2">
                        <el-form-item label="SecretId" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsTencentSecretId }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretId', content: formState.smsTencentSecretId }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretId"
                                        width="600px"
                                        @okCallback="formState.smsTencentSecretId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('腾讯云账户的 API 密钥ID，用于标识 API 调用者的身份。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="SecretKey" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsTencentSecretKey }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretKey', content: formState.smsTencentSecretKey }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretKey"
                                        width="600px"
                                        @okCallback="formState.smsTencentSecretKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('腾讯云账户的API密钥，用于加密签名字符串和服务器端验证签名字符串的密钥。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="SDK AppID" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsTencentSdkAppId" />
                                <div class="extra">{{ $t('腾讯云短信应用的ID，在短信服务的应用管理中获取。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信签名')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsTencentSignName" />
                                <div class="extra">{{ $t('短信发送时的签名，需在腾讯云短信控制台创建并通过审核。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('商家短信号码')">
                            <div>
                                <TigInput classType="tig-form-input" type="integer" v-model="formState.smsTencentShopMobile" />
                                <div class="extra">{{ $t('用于商家接收会员订单等信息') }}</div>
                            </div>
                        </el-form-item>
                    </template>

                    <template v-if="formState.smsProvider === 3">
                        <el-form-item label="APP Key" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsHuaweiAppKey }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'APP Key', content: formState.smsHuaweiAppKey }"
                                        path="setting/config/src/EditSecret"
                                        title="APP Key"
                                        width="600px"
                                        @okCallback="formState.smsHuaweiAppKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('华为云短信应用的 APP Key，用于接口鉴权，请在短信应用管理中获取。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="APP Secret" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsHuaweiAppSecret }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'APP Secret', content: formState.smsHuaweiAppSecret }"
                                        path="setting/config/src/EditSecret"
                                        title="APP Secret"
                                        width="600px"
                                        @okCallback="formState.smsHuaweiAppSecret = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('华为云短信应用的 APP Secret，用于接口鉴权，请妥善保管。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信通道号')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsHuaweiChannel" />
                                <div class="extra">{{ $t('华为云短信通道号，请在短信控制台对应应用配置中获取。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信签名')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsHuaweiSignName" />
                                <div class="extra">{{ $t('短信发送时的签名，需在华为云短信控制台申请并审核通过。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('接入地址')" required>
                            <div>
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="formState.smsHuaweiEndpoint"
                                    placeholder="https://smsapi.cn-north-4.myhuaweicloud.com:443"
                                />
                                <div class="extra">{{ $t('示例：https://smsapi.cn-north-4.myhuaweicloud.com:443') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('商家短信号码')">
                            <div>
                                <TigInput classType="tig-form-input" type="integer" v-model="formState.smsHuaweiShopMobile" />
                                <div class="extra">{{ $t('用于商家接收会员订单等信息') }}</div>
                            </div>
                        </el-form-item>
                    </template>

                    <template v-if="formState.smsProvider === 4">
                        <el-form-item label="AccessKey ID" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsVolcengineAccessKey }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AccessKey ID', content: formState.smsVolcengineAccessKey }"
                                        path="setting/config/src/EditSecret"
                                        title="AccessKey ID"
                                        width="600px"
                                        @okCallback="formState.smsVolcengineAccessKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('火山引擎账户的 AccessKey ID，用于标识 API 调用者身份。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="SecretAccessKey" required>
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">{{ formState.smsVolcengineSecretKey }}</div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretAccessKey', content: formState.smsVolcengineSecretKey }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretAccessKey"
                                        width="600px"
                                        @okCallback="formState.smsVolcengineSecretKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary">{{ $t('编辑') }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('火山引擎账户的 SecretAccessKey，用于接口签名鉴权，请妥善保管。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信应用 ID')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsVolcengineSmsAccount" />
                                <div class="extra">{{ $t('火山引擎短信应用 ID，请在短信服务控制台的应用管理中获取。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('短信签名')" required>
                            <div>
                                <TigInput classType="tig-form-input" v-model="formState.smsVolcengineSignName" />
                                <div class="extra">{{ $t('短信发送时的签名，需在火山引擎短信控制台创建并审核通过。') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('商家短信号码')">
                            <div>
                                <TigInput classType="tig-form-input" type="integer" v-model="formState.smsVolcengineShopMobile" />
                                <div class="extra">{{ $t('用于商家接收会员订单等信息') }}</div>
                            </div>
                        </el-form-item>
                    </template>
                </div>
            </el-form>
            <div style="height: 20px"></div>
        </a-spin>
    </div>
    <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import type { BaseNotice, SmsProviderType } from "@/types/setting/config";
import { getConfigNotify, saveConfigNotify } from "@/api/setting/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { isOverseas } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const confirmLoading = ref(false);
const DEFAULT_SMS_PROVIDER: SmsProviderType = 1;
const currentSmsProvider = ref<SmsProviderType>(DEFAULT_SMS_PROVIDER);
// 阿里云通道开关（复选，仅跨境版）：1=+86国内通道 2=其他海外通道
const smsAliyunChannel = ref<string[]>(["1"]);

function defaultForm(): Partial<BaseNotice> {
    return {
        smsProvider: DEFAULT_SMS_PROVIDER,
        smsKeyId: "",
        smsKeySecret: "",
        smsSignName: "",
        smsShopMobile: "",
        smsTencentSecretId: "",
        smsTencentSecretKey: "",
        smsTencentSdkAppId: "",
        smsTencentSignName: "",
        smsTencentShopMobile: "",
        smsHuaweiAppKey: "",
        smsHuaweiAppSecret: "",
        smsHuaweiChannel: "",
        smsHuaweiSignName: "",
        smsHuaweiEndpoint: "",
        smsHuaweiShopMobile: "",
        smsVolcengineAccessKey: "",
        smsVolcengineSecretKey: "",
        smsVolcengineSmsAccount: "",
        smsVolcengineSignName: "",
        smsVolcengineShopMobile: "",
        smsAliyunChannel: "1",
        smsSenderId: "",
        serviceEmail: "",
        sendConfirmEmail: 0,
        orderPayEmail: 0,
        sendServiceEmail: 0,
        sendShipEmail: 0
    };
}

const formState = ref<Partial<BaseNotice>>(defaultForm());

const SUPPORTED_SMS_PROVIDERS: readonly SmsProviderType[] = [1, 2, 3, 4];

function normalizeSmsProvider(raw: unknown): SmsProviderType {
    let provider: number;
    if (typeof raw === "number") {
        provider = raw;
    } else if (typeof raw === "string" && raw.trim() !== "") {
        provider = parseInt(raw, 10);
    } else {
        return DEFAULT_SMS_PROVIDER;
    }
    if (!Number.isInteger(provider)) return DEFAULT_SMS_PROVIDER;
    return (SUPPORTED_SMS_PROVIDERS as readonly number[]).includes(provider) ? (provider as SmsProviderType) : DEFAULT_SMS_PROVIDER;
}

function isMaskedOrFilled(v: unknown): boolean {
    if (v === null || v === undefined) return false;
    const s = String(v);
    if (s.includes("*")) return true;
    return s.trim().length > 0;
}

function textFilled(v: unknown): boolean {
    if (v === null || v === undefined) return false;
    return String(v).trim().length > 0;
}

function validateCurrent(): string | null {
    const p = formState.value.smsProvider as SmsProviderType;
    if (p === 1) {
        if (!isMaskedOrFilled(formState.value.smsKeyId)) return t('请填写短信帐户用户名');
        if (!isMaskedOrFilled(formState.value.smsKeySecret)) return t('请填写短信帐户密码');
        if (!textFilled(formState.value.smsSignName)) return t('请填写短信签名');
    } else if (p === 2) {
        if (!isMaskedOrFilled(formState.value.smsTencentSecretId)) return t('请填写 SecretId');
        if (!isMaskedOrFilled(formState.value.smsTencentSecretKey)) return t('请填写 SecretKey');
        if (!textFilled(formState.value.smsTencentSdkAppId)) return t('请填写 SDK AppID');
        if (!textFilled(formState.value.smsTencentSignName)) return t('请填写短信签名');
    } else if (p === 3) {
        if (!isMaskedOrFilled(formState.value.smsHuaweiAppKey)) return t('请填写 APP Key');
        if (!isMaskedOrFilled(formState.value.smsHuaweiAppSecret)) return t('请填写 APP Secret');
        if (!textFilled(formState.value.smsHuaweiChannel)) return t('请填写短信通道号');
        if (!textFilled(formState.value.smsHuaweiSignName)) return t('请填写短信签名');
        if (!textFilled(formState.value.smsHuaweiEndpoint)) return t('请填写接入地址');
    } else if (p === 4) {
        if (!isMaskedOrFilled(formState.value.smsVolcengineAccessKey)) return t('请填写 AccessKey ID');
        if (!isMaskedOrFilled(formState.value.smsVolcengineSecretKey)) return t('请填写 SecretAccessKey');
        if (!textFilled(formState.value.smsVolcengineSmsAccount)) return t('请填写短信应用 ID');
        if (!textFilled(formState.value.smsVolcengineSignName)) return t('请填写短信签名');
    }
    return null;
}

const loading = ref(true);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getConfigNotify();
        const merged = convertNullsToEmptyStrings(result) as Record<string, unknown>;
        Object.assign(formState.value, defaultForm(), merged);
        const provider = normalizeSmsProvider(formState.value.smsProvider);
        formState.value.smsProvider = provider;
        currentSmsProvider.value = provider;
        // 解析阿里云通道开关字符串（"1" / "2" / "1,2"）为复选数组
        const channelStr = typeof formState.value.smsAliyunChannel === "string" ? formState.value.smsAliyunChannel : "1";
        const channels = channelStr.split(",").map((s) => s.trim()).filter((s) => s === "1" || s === "2");
        smsAliyunChannel.value = channels.length > 0 ? channels : ["1"];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadFilter();
});

const onSubmit = async () => {
    const tip = validateCurrent();
    if (tip) {
        message.warning(tip);
        return;
    }
    const provider = normalizeSmsProvider(formState.value.smsProvider);
    // 跨境版：阿里云通道至少勾选一个
    if (isOverseas() && provider === 1 && smsAliyunChannel.value.length === 0) {
        message.warning("请至少选择一个服务商通道");
        return;
    }
    // 跨境版：勾选「其他海外通道」时，国际短信 SenderID 必填
    if (isOverseas() && provider === 1 && smsAliyunChannel.value.includes("2") && !String(formState.value.smsSenderId ?? "").trim()) {
        message.warning("勾选其他海外通道时，请填写国际短信SenderID");
        return;
    }
    confirmLoading.value = true;
    try {
        const providerChanged = currentSmsProvider.value !== provider;
        const payload = {
            ...defaultForm(),
            ...formState.value,
            smsProvider: provider,
            smsAliyunChannel: smsAliyunChannel.value.join(",")
        };
        await saveConfigNotify(payload as BaseNotice);
        currentSmsProvider.value = provider;
        await loadFilter();
        message.success(
            providerChanged
                ? t('服务商已切换，请确认各消息类型的短信模板 ID 已更新为对应平台的模板')
                : t('保存成功')
        );
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
.extra {
    color: #999;
    font-size: 12px;
    line-height: 1.5;
    margin-top: 4px;
}
.itemWidth {
    width: 100%;
}
</style>
