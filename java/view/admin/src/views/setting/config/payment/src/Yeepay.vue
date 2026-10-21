<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" :rules="formRules" label-width="180px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">易宝支付设置</div>
                        <div class="notice-warp">
                            <p>易宝支付（YeePay）聚合收单，一次接入即可同时支持微信、支付宝收款。开启后，支付页面将展示「微信支付」「支付宝支付」两个选项，用户付款体验与原生一致、对易宝渠道无感知。以下参数需先在易宝商户平台开通商户后获取，密钥相关请妥善保管、切勿外泄。</p>
                        </div>
                        <el-form-item label="是否开启" prop="useYeepay">
                            <el-radio-group v-model="formState.useYeepay" class="itemWidth">
                                <el-radio :value="1">开启</el-radio>
                                <el-radio :value="0">关闭</el-radio>
                            </el-radio-group>
                            <div class="extra">开启后，用户在支付页可使用「微信支付」「支付宝支付」；关闭则不展示这两个选项</div>
                        </el-form-item>

                        <div v-show="Number(formState.useYeepay) === 1">
                            <el-form-item label="应用 AppKey" prop="yeepayAppKey">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.yeepayAppKey" placeholder="易宝应用 appKey" />
                                    <div class="extra">登录易宝商户平台（merchant.yeepay.com）→开发者中心→应用管理中获取，形如 OPR:1000xxxxxxx</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="发起方商户号" prop="yeepayParentMerchantNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.yeepayParentMerchantNo" placeholder="发起方商户编号 parentMerchantNo" />
                                    <div class="extra">易宝分配的发起方（父）商户编号，在易宝商户平台→账户信息中获取</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="收款商户号" prop="yeepayMerchantNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.yeepayMerchantNo" placeholder="收款商户编号 merchantNo" />
                                    <div class="extra">实际收款商户编号；直连商户与发起方一致时可留空，留空则默认取发起方商户号</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户私钥" prop="yeepayIsvPrivateKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.yeepayIsvPrivateKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '商户私钥（PKCS8 Base64，用于请求加签）', content: formState.yeepayIsvPrivateKey }"
                                            path="setting/config/src/EditSecret"
                                            title="商户私钥"
                                            width="600px"
                                            @okCallback="formState.yeepayIsvPrivateKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">用易宝提供的密钥生成工具生成 RSA 密钥对，私钥填此处、公钥上传至易宝商户平台（私钥请妥善保管、不要外泄）。如无需更换，保持原样直接提交即可。</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="接口地址" prop="yeepayServerRoot">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.yeepayServerRoot" placeholder="https://openapi.yeepay.com/yop-center" />
                                    <div class="extra">选填，易宝接口地址；留空使用易宝默认生产地址，联调沙箱时按易宝对接文档填写</div>
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
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">提 交</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { FormRules } from "element-plus";
import type { YeepaySettingsForm } from "@/types/setting/payment";
import { getYeepaySettings, updateYeepaySettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";

const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);

const previewHiddenKeys = computed(() => (Number(formState.value.useYeepay) === 1 ? ([] as const) : (["yeepayWechat", "yeepayAlipay"] as const)));
const previewSelected = computed(() => "yeepayWechat" as const);

const formState = ref<Partial<YeepaySettingsForm>>({
    useYeepay: undefined,
    yeepayAppKey: "",
    yeepayParentMerchantNo: "",
    yeepayMerchantNo: "",
    yeepayIsvPrivateKey: "",
    yeepayServerRoot: ""
});

const validateRequiredWhenEnabled = (label: string) => {
    return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        if (Number(formState.value.useYeepay) !== 1) {
            callback();
            return;
        }
        if (!String(value ?? "").trim()) {
            callback(new Error(`请填写${label}`));
            return;
        }
        callback();
    };
};

const validateServerRoot = (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
    if (Number(formState.value.useYeepay) !== 1) {
        callback();
        return;
    }
    const v = String(value ?? "").trim();
    if (!v) {
        callback();
        return;
    }
    if (!/^https?:\/\//i.test(v)) {
        callback(new Error("接口地址需以 http:// 或 https:// 开头"));
        return;
    }
    callback();
};

const formRules = computed<FormRules>(() => ({
    yeepayAppKey: [{ validator: validateRequiredWhenEnabled("应用 AppKey"), trigger: "blur" }],
    yeepayParentMerchantNo: [{ validator: validateRequiredWhenEnabled("发起方商户号"), trigger: "blur" }],
    yeepayIsvPrivateKey: [{ validator: validateRequiredWhenEnabled("商户私钥"), trigger: "blur" }],
    yeepayServerRoot: [{ validator: validateServerRoot, trigger: "blur" }]
}));

onMounted(async () => {
    loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getYeepaySettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    try {
        await formRef.value?.validate();
    } catch {
        return;
    }
    confirmLoading.value = true;
    try {
        await updateYeepaySettings({ ...formState.value } as YeepaySettingsForm);
        message.success("修改成功");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
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
}

.payment-setting-layout {
    display: flex;
    gap: 18px;
    align-items: stretch;
}
.payment-setting-left {
    min-width: 720px;
}

@media (max-width: 1440px) {
    .payment-setting-layout {
        flex-direction: column;
    }
    .payment-setting-left {
        min-width: 0;
    }
}

.itemWidth {
    width: 100%;
}
</style>
