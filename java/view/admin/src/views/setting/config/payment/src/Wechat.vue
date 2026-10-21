<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="160px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">{{ $t('微信支付设置') }}</div>
                        <el-form-item :label="$t('是否开启')" prop="useWechat">
                            <el-radio-group v-model="formState.useWechat" class="itemWidth">
                                <el-radio :value="1">{{ $t('开启') }}</el-radio>
                                <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('是否开启微信支付，如关闭则不显示微信支付选项') }}</div>
                        </el-form-item>

                        <div v-show="Number(formState.useWechat) === 1">
                            <el-form-item :label="$t('微信商户类型')" prop="wechatMchidType">
                                <el-radio-group v-model="formState.wechatMchidType" class="itemWidth">
                                    <el-radio :value="1">{{ $t('普通商户模式') }}</el-radio>
                                    <el-radio :value="2">{{ $t('服务商模式') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item :label="$t('微信商户号')" prop="wechatPayMchid">
                                <TigInput classType="tig-form-input" v-model="formState.wechatPayMchid" />
                            </el-form-item>
                            <el-form-item :label="$t('微信子商户号')" prop="wechatPaySubMchid" v-if="formState.wechatMchidType === 2">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.wechatPaySubMchid" />
                                    <div class="extra">{{ $t('请填写由服务商提供的子商户号') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('商户APIv3密钥')" prop="wechatPayKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.wechatPayKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '商户APIv3密钥', content: formState.wechatPayKey }"
                                            path="setting/config/src/EditSecret"
                                            :title="$t('商户APIv3密钥')"
                                            width="600px"
                                            @okCallback="formState.wechatPayKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">{{ $t('请使用APIv3密钥，编辑完成后，点击下方提交按钮确认修改，请确保填写正确的商户APIv3密钥') }}</div>
                                </div>
                            </el-form-item>

                            <el-form-item :label="$t('商户APIV2密钥')" prop="wechatPayV2Key">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.wechatPayV2Key }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '商户APIV2密钥', content: formState.wechatPayV2Key }"
                                            path="setting/config/src/EditSecret"
                                            :title="$t('商户APIV2密钥')"
                                            width="600px"
                                            @okCallback="formState.wechatPayV2Key = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">{{ $t('请使用APIv2密钥，编辑完成后，点击下方提交按钮确认修改，请确保填写正确的商户APIV2密钥') }}</div>
                                </div>
                            </el-form-item>

                            <el-form-item :label="$t('验证微信支付方式')" prop="wechatMchidType">
                                <el-radio-group v-model="formState.wechatPayCheckType" class="itemWidth">
                                    <el-radio :value="1">{{ $t('平台证书') }}</el-radio>
                                    <el-radio :value="2">{{ $t('微信支付公钥') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>

                            <el-form-item :label="$t('商户证书序列号')" prop="wechatPaySerialNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.wechatPaySerialNo" />
                                    <div class="extra">{{ $t('请使用API证书序列号') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('商户API证书')" prop="wechatPayCertificate">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=1'"
                                        :headers="requestUrl.headers()"
                                        @change="handleChange"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.wechatPayCertificate ? $t('重新上传') : $t('点击上传') }}</el-button>
                                        <span class="ml10">apiclient_cert.pem</span>
                                    </Upload>
                                </div>
                                <div class="extra">{{ $t('调用微信商户接口时需要用到的API证书，文件名称一般为apiclient_cert.pem，具体操作请参考教程') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('商户API证书密钥')" prop="wechatPayPrivateKey">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=2'"
                                        :headers="requestUrl.headers()"
                                        @change="handleChange"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.wechatPayPrivateKey ? $t('重新上传') : $t('点击上传') }}</el-button>
                                        <span class="ml10">apiclient_key.pem</span>
                                    </Upload>
                                </div>
                                <div class="extra">{{ $t('调用微信商户接口时需要用到的API证书密钥，文件名称一般为apiclient_key.pem，具体操作请参考教程') }}</div>
                            </el-form-item>

                            <el-form-item :label="$t('平台证书')" prop="" v-if="formState.wechatPayCheckType === 1">
                                <div style="width: 100%">
                                    <el-button :loading="confirmLoading" class="form-submit-btn" type="success" @click="getPlatformCertificate">
                                        {{ formState.wechatPayPlatformCertificate ? $t('更新证书') : $t('生成证书') }}
                                    </el-button>
                                </div>
                                <div class="extra">{{ $t('调用微信商户接口时需要用到的平台证书,上传API证书后需要更新平台证书，具体操作请参考教程') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('微信支付公钥ID')" prop="wechatPayPublicKeyId" v-if="formState.wechatPayCheckType === 2">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.wechatPayPublicKeyId" />
                                    <div class="extra">{{ $t('请使用微信支付公钥ID') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('微信支付公钥文件')" prop="wechatPayPublicKey" v-if="formState.wechatPayCheckType === 2">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=3'"
                                        :headers="requestUrl.headers()"
                                        @change="handleChange"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.wechatPayPublicKey ? $t('重新上传') : $t('点击上传') }}</el-button>
                                        <span class="ml10">{{ $t('微信支付公钥文件（如：pub_key.pem）') }}</span>
                                    </Upload>
                                </div>
                                <div class="extra">{{ $t('调用微信商户接口时需要用到的公钥证书，文件名称一般为public.pem，具体操作请参考教程') }}</div>
                            </el-form-item>
                            <div class="title">{{ $t('微信小程序支付') }}</div>
                            <el-form-item :label="$t('微信小程序AppID')" prop="wechatMiniProgramAppId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.wechatMiniProgramAppId" />
                                    <div class="extra">{{ $t('微信小程序公众平台->设置->开发设置中获取，开通小程序必填') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('微信小程序AppSecret')" prop="wechatMiniProgramSecret">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.wechatMiniProgramSecret }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '微信小程序AppSecret', content: formState.wechatMiniProgramSecret }"
                                            path="setting/config/src/EditSecret"
                                            :title="$t('微信小程序AppSecret')"
                                            width="600px"
                                            @okCallback="formState.wechatMiniProgramSecret = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">{{ $t('微信小程序公众平台->设置->开发设置中获取，开通小程序必填') }}</div>
                                    <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的微信小程序AppSecret') }}</div>
                                </div>
                            </el-form-item>
                            <div class="title">{{ $t('微信APP支付') }}</div>
                            <el-form-item :label="$t('应用APP ID')" prop="wechatPayAppId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.wechatPayAppId" />
                                    <div class="extra">{{ $t('需开通微信开发者平台，创建APP应用（未开通本系统APP商城可忽略）') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('应用APP SECRET')" prop="wechatPayAppSecret">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.wechatPayAppSecret }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '应用APP SECRET', content: formState.wechatPayAppSecret }"
                                            path="setting/config/src/EditSecret"
                                            :title="$t('应用APP SECRET')"
                                            width="600px"
                                            @okCallback="formState.wechatPayAppSecret = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">{{ $t('需通微信开发者平台获取（未开通本系统APP商城可忽略）') }}</div>
                                    <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的应用APP SECRET') }}</div>
                                </div>
                            </el-form-item>
                        </div>
                    </div>

                    <PaymentPreviewAside :selected="previewSelected" :hiddenKeys="previewHiddenKeys" />
                </div>
            </div>
        </el-form>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { computed, onMounted, ref, shallowRef } from "vue";
import { message, Upload } from "ant-design-vue";
import type { WechatPaySettingsForm } from "@/types/setting/payment";
import { requestUrl } from "@/utils/request";
import { createPlatformCertificate, getWechatSettings, updateWechatSettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
// 未加载到明确状态前（undefined/null），先隐藏预览，避免加载过程中“闪一下”
const previewHiddenKeys = computed(() => (Number(formState.value.useWechat) === 1 ? ([] as const) : (["wechat"] as const)));
const previewSelected = computed(() => "wechat" as const);
const formState = ref<Partial<WechatPaySettingsForm>>({
    useWechat: undefined,
    wechatMchidType: undefined,
    wechatPayMchid: "wechatPayMchid_46f1aec6bf75",
    wechatPaySubMchid: 0,
    wechatPayKey: "wechatPayKey_de5a4632fe4a",
    wechatPaySerialNo: "wechatPaySerialNo_4a29ab342bc1",
    wechatPayCertificate: 0,
    wechatPayPrivateKey: 0,
    wechatPayCheckType: undefined,
    wechatPayPlatformCertificate: 0,
    wechatPayPublicKeyId: 0,
    wechatPayPublicKey: 0,
    wechatMiniProgramAppId: "",
    wechatMiniProgramSecret: "",
    wechatPayAppId: "",
    wechatPayAppSecret: "",
    wechatPayV2Key: ""
});
// 加载列表
onMounted(async () => {
    loadFilter();
});
const loading = ref<boolean>(true);
const getPlatformCertificate = async () => {
    try {
        const result = await createPlatformCertificate();
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const loadFilter = async () => {
    try {
        const wechat = await getWechatSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(wechat));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const payload: any = { ...(formState.value as any) };
        // 该字段已移除，不再提交给后端
        delete payload.wechatUseYabanpay;
        await updateWechatSettings(payload as WechatPaySettingsForm);
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
const handleChange = (info: any) => {
    if (info.file.status == "uploading") {
    }
    if (info.file.status !== "uploading") {
    }
    if (info.file.status === "done") {
        // 上传完成
        if (info.file.response.code !== 0) {
            info.file.status = "error";
            message.error(info.file.response.data.message);
        } else {
            for (let index in info.fileList) {
                if (info.file.uid == info.fileList[index].uid) {
                    info.fileList[index] = Object.assign(info.fileList[index], info.fileList[index].response.data);
                }
            }
            message.success(t('文件上传成功！'));
            // loadFilter();
        }
    } else if (info.file.status === "error") {
        message.error(t('{name} 文件上传失败！', { name: info.file.name }));
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }
    :deep(.el-tabs--border-card > .el-tabs__content) {
        padding: 0;
    }
}

.payment-setting-layout {
    display: flex;
    gap: 18px;
    /* 右侧预览使用 sticky 时，右侧列需要跟随左侧表单撑满高度，sticky 才会生效 */
    align-items: stretch;
}
.payment-setting-left {
    min-width: 620px;
}
.preview-title {
    font-weight: 600;
    color: #333;
    font-size: 13px;
    margin: 0 0 10px;
}
.radio-with-tip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
}
.question-mark {
    width: 16px;
    height: 16px;
    border-radius: 999px;
    border: 1px solid #cfd3dc;
    color: #666;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: help;
    line-height: 16px;
}
.pay-setting-tip {
    max-width: 320px;
    line-height: 18px;
}

@media (max-width: 1400px) {
    .payment-setting-layout {
        flex-direction: column;
    }
    .payment-setting-left {
        min-width: 0;
    }
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}
.itemWidth {
    width: 100%;
}
</style>
