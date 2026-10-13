<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" :rules="formRules" label-width="180px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">Apple Pay 苹果支付设置</div>
                        <div class="notice-warp">
                            <p>配置前请了解以下几点：</p>
                            <p>1. 展示范围：Apple Pay 只会在 iPhone、Mac 的 Safari 浏览器且设备已开通 Apple Pay 时，才在收银台出现；微信内、小程序、安卓、Chrome 等环境会自动隐藏，属正常现象，无需处理。</p>
                            <p>2. 参数来源：下方的商户信息、证书与收单参数均需从 Apple 开发者后台和收单机构处获取；证书和密钥涉及资金安全，请妥善保管、切勿外泄。</p>
                            <p>3. 上线前提：保存并开启后，还需在 Apple 开发者后台完成各前台域名验证，并将验证文件部署到对应域名的 /.well-known/ 目录，否则用户端将无法唤起 Apple Pay。</p>
                        </div>
                        <el-form-item label="是否开启" prop="useApplePay">
                            <el-radio-group v-model="formState.useApplePay" class="itemWidth">
                                <el-radio :value="1">开启</el-radio>
                                <el-radio :value="0">关闭</el-radio>
                            </el-radio-group>
                            <div class="extra">是否开启Apple Pay支付，如关闭则不显示Apple Pay支付选项</div>
                        </el-form-item>

                        <div v-show="Number(formState.useApplePay) === 1">
                            <div class="title">基础参数</div>
                            <el-form-item label="商户标识 Merchant ID" prop="applePayMerchantId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.applePayMerchantId" placeholder="merchant.com.yourshop" />
                                    <div class="extra">Apple开发者后台创建的Merchant ID，形如 merchant.com.yourshop</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户展示名" prop="applePayDisplayName">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.applePayDisplayName" placeholder="展示在Apple Pay面板中的商户名称" />
                                    <div class="extra">展示在Apple Pay支付面板中的商户名称</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="国家/地区" prop="applePayCountryCode">
                                <div>
                                    <el-select v-model="formState.applePayCountryCode" style="width: 450px">
                                        <el-option v-for="item in countryOptions" :key="item.value" :value="item.value" :label="item.name" />
                                    </el-select>
                                    <div class="extra">收单商户注册地国家码（ISO 3166 两位码），影响支付面板合规展示</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="货币" prop="applePayCurrency">
                                <div>
                                    <el-select v-model="formState.applePayCurrency" style="width: 450px">
                                        <el-option v-for="item in currencyOptions" :key="item.value" :value="item.value" :label="item.name" />
                                    </el-select>
                                    <div class="extra">结算币种（ISO 4217 三位码），须与收单通道商户号支持币种一致。{{ $t('请保持和商城默认货币一致，否则会导致下单和结账货币不一致') }}</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="支持卡组织" prop="applePaySupportedNetworks">
                                <div>
                                    <el-checkbox-group v-model="supportedNetworks">
                                        <el-checkbox v-for="item in networkOptions" :key="item.value" :value="item.value" :label="item.label" />
                                    </el-checkbox-group>
                                    <div class="extra">用户钱包中仅这些卡组织的卡可用于支付；境内收单请勾选银联</div>
                                </div>
                            </el-form-item>

                            <div class="title">证书配置</div>
                            <el-form-item label="商户身份证书" prop="applePayMerchantIdentityCert">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=4'"
                                        :headers="requestUrl.headers()"
                                        @change="(info: any) => handleCertChange(info, 'applePayMerchantIdentityCert')"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.applePayMerchantIdentityCert ? "重新上传" : "点击上传" }}</el-button>
                                        <span class="ml10">apple_pay_merchant_id.pem</span>
                                    </Upload>
                                    <div class="extra">与Apple服务器建立商户校验会话所需证书（Merchant Identity Certificate）</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户身份证书私钥" prop="applePayMerchantIdentityKey">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=5'"
                                        :headers="requestUrl.headers()"
                                        @change="(info: any) => handleCertChange(info, 'applePayMerchantIdentityKey')"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.applePayMerchantIdentityKey ? "重新上传" : "点击上传" }}</el-button>
                                        <span class="ml10">apple_pay_merchant_id_key.pem</span>
                                    </Upload>
                                    <div class="extra">商户身份证书对应私钥</div>
                                </div>
                            </el-form-item>
                            <el-form-item v-if="isGeneric" label="支付处理证书私钥" prop="applePayProcessingPrivateKey">
                                <div style="width: 100%">
                                    <Upload
                                        name="file"
                                        :action="requestUrl.prefix + '/setting/config/uploadFile?type=6'"
                                        :headers="requestUrl.headers()"
                                        @change="(info: any) => handleCertChange(info, 'applePayProcessingPrivateKey')"
                                        :showUploadList="false"
                                        :multiple="false"
                                    >
                                        <el-button>{{ formState.applePayProcessingPrivateKey ? "重新上传" : "点击上传" }}</el-button>
                                        <span class="ml10">apple_pay_payment_processing_key.pem</span>
                                    </Upload>
                                    <div class="extra">用于解密Apple Pay支付Token的私钥（Payment Processing Certificate 私钥）</div>
                                </div>
                            </el-form-item>
                            <el-form-item v-else label="支付处理证书私钥">
                                <div class="extra">当前收单机构由对方解密支付凭据，无需上传支付处理证书私钥。</div>
                            </el-form-item>

                            <div class="title">收单通道</div>
                            <el-form-item label="收单机构" prop="applePayAcquirerProvider">
                                <div>
                                    <el-select v-model="formState.applePayAcquirerProvider" style="width: 450px">
                                        <el-option v-for="item in providerOptions" :key="item.value" :value="item.value" :label="item.label" />
                                    </el-select>
                                    <div class="extra">收单机构：通用自解密由我方解密卡凭据（需支付处理证书）；Adyen/银联商务由对方解密（无需支付处理证书）</div>
                                </div>
                            </el-form-item>
                            <el-form-item v-if="isUms" label="银联签名类型" prop="applePayUmsSignType">
                                <div>
                                    <el-select v-model="formState.applePayUmsSignType" style="width: 450px">
                                        <el-option v-for="item in umsSignTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
                                    </el-select>
                                    <div class="extra">银联商务报文签名算法，默认 SHA256，可选 SM3（需与收单商户开通的算法一致）</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="收单网关地址" prop="applePayAcquirerGateway">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.applePayAcquirerGateway" placeholder="https://gateway.example.com" />
                                    <div class="extra">收单机构提供的支付网关地址（测试/生产不同）</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="收单商户号" prop="applePayAcquirerMerchantNo">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.applePayAcquirerMerchantNo }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '收单商户号', content: formState.applePayAcquirerMerchantNo }"
                                            path="setting/config/src/EditSecret"
                                            title="收单商户号"
                                            width="600px"
                                            @okCallback="formState.applePayAcquirerMerchantNo = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">收单机构分配的商户号。未修改时请勿清空，原样提交即可（后端识别掩码后跳过更新）。</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="收单密钥" prop="applePayAcquirerKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.applePayAcquirerKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '收单密钥', content: formState.applePayAcquirerKey }"
                                            path="setting/config/src/EditSecret"
                                            title="收单密钥"
                                            width="600px"
                                            @okCallback="formState.applePayAcquirerKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">收单机构分配的API密钥，编辑完成后点击下方提交按钮确认修改。</div>
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
import { message, Upload } from "ant-design-vue";
import type { FormRules } from "element-plus";
import type { ApplePaySettingsForm } from "@/types/setting/payment";
import { requestUrl } from "@/utils/request";
import { getApplePaySettings, updateApplePaySettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";

const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);

const previewHiddenKeys = computed(() => (Number(formState.value.useApplePay) === 1 ? ([] as const) : (["applepay"] as const)));
const previewSelected = computed(() => "applepay" as const);

const networkOptions = [
    { value: "chinaUnionPay", label: "银联" },
    { value: "visa", label: "Visa" },
    { value: "masterCard", label: "MasterCard" },
    { value: "amex", label: "American Express" },
    { value: "jcb", label: "JCB" },
    { value: "discover", label: "Discover" }
];

const providerOptions = [
    { value: "generic", label: "通用自解密(generic)" },
    { value: "adyen", label: "Adyen(adyen)" },
    { value: "ums", label: "银联商务(ums)" }
];

const umsSignTypeOptions = [
    { value: "SHA256", label: "SHA256" },
    { value: "SM3", label: "SM3" }
];

// type6 支付处理证书仅 generic 需要；银联签名类型仅 ums 显示
const isGeneric = computed(() => formState.value.applePayAcquirerProvider === "generic");
const isUms = computed(() => formState.value.applePayAcquirerProvider === "ums");

const formState = ref<Partial<ApplePaySettingsForm>>({
    useApplePay: undefined,
    applePayMerchantId: "",
    applePayDisplayName: "",
    applePayCountryCode: "CN",
    applePayCurrency: "CNY",
    applePaySupportedNetworks: "chinaUnionPay,visa,masterCard",
    applePayMerchantIdentityCert: 0,
    applePayMerchantIdentityKey: 0,
    applePayProcessingPrivateKey: 0,
    applePayAcquirerProvider: "generic",
    applePayUmsSignType: "SHA256",
    applePayAcquirerGateway: "",
    applePayAcquirerMerchantNo: "",
    applePayAcquirerKey: "",
    applePayCountryList: [],
    applePayCurrencyList: []
});

// 国家下拉：后端下发为准，空则兜底 CN
const countryOptions = computed(() => {
    const list = formState.value.applePayCountryList ?? [];
    return list.length ? list : [{ name: "中国", value: "CN" }];
});
const currencyOptions = computed(() => {
    const list = formState.value.applePayCurrencyList ?? [];
    return list.length ? list : [{ name: "人民币", value: "CNY" }];
});

// 卡组织：逗号串 <-> 数组
const supportedNetworks = computed<string[]>({
    get() {
        return String(formState.value.applePaySupportedNetworks ?? "")
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean);
    },
    set(val) {
        formState.value.applePaySupportedNetworks = (val ?? []).join(",");
    }
});

const validateRequiredWhenEnabled = (label: string) => {
    return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        if (Number(formState.value.useApplePay) !== 1) {
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

const validateGateway = (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
    if (Number(formState.value.useApplePay) !== 1) {
        callback();
        return;
    }
    const v = String(value ?? "").trim();
    if (!v) {
        callback(new Error("请填写收单网关地址"));
        return;
    }
    if (!/^https?:\/\//i.test(v)) {
        callback(new Error("网关地址需以 http:// 或 https:// 开头"));
        return;
    }
    callback();
};

const validateNetworks = (_rule: unknown, _value: unknown, callback: (error?: Error) => void) => {
    if (Number(formState.value.useApplePay) !== 1) {
        callback();
        return;
    }
    if (!supportedNetworks.value.length) {
        callback(new Error("请至少选择一个卡组织"));
        return;
    }
    callback();
};

const validateCert = (label: string, onlyGeneric = false) => {
    return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        if (Number(formState.value.useApplePay) !== 1) {
            callback();
            return;
        }
        // type6 支付处理证书仅 generic 需要，adyen/ums 由对方解密无需上传
        if (onlyGeneric && !isGeneric.value) {
            callback();
            return;
        }
        if (Number(value) !== 1) {
            callback(new Error(`请上传${label}`));
            return;
        }
        callback();
    };
};

const validateUmsSignType = (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
    if (Number(formState.value.useApplePay) !== 1 || !isUms.value) {
        callback();
        return;
    }
    if (!String(value ?? "").trim()) {
        callback(new Error("请选择银联签名类型"));
        return;
    }
    callback();
};

const formRules = computed<FormRules>(() => ({
    applePayMerchantId: [{ validator: validateRequiredWhenEnabled("商户标识 Merchant ID"), trigger: "blur" }],
    applePayDisplayName: [{ validator: validateRequiredWhenEnabled("商户展示名"), trigger: "blur" }],
    applePaySupportedNetworks: [{ validator: validateNetworks, trigger: "change" }],
    applePayMerchantIdentityCert: [{ validator: validateCert("商户身份证书"), trigger: "change" }],
    applePayMerchantIdentityKey: [{ validator: validateCert("商户身份证书私钥"), trigger: "change" }],
    applePayProcessingPrivateKey: [{ validator: validateCert("支付处理证书私钥", true), trigger: "change" }],
    applePayUmsSignType: [{ validator: validateUmsSignType, trigger: "change" }],
    applePayAcquirerGateway: [{ validator: validateGateway, trigger: "blur" }],
    applePayAcquirerMerchantNo: [{ validator: validateRequiredWhenEnabled("收单商户号"), trigger: "blur" }],
    applePayAcquirerKey: [{ validator: validateRequiredWhenEnabled("收单密钥"), trigger: "blur" }]
}));

onMounted(async () => {
    loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getApplePaySettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const handleCertChange = (info: any, field: keyof ApplePaySettingsForm) => {
    if (info.file.status === "done") {
        if (info.file.response?.code !== 0) {
            info.file.status = "error";
            message.error(info.file.response?.data?.message || "证书文件格式不正确");
        } else {
            (formState.value as any)[field] = 1;
            message.success("上传成功");
        }
    } else if (info.file.status === "error") {
        message.error(`${info.file.name} 文件上传失败！`);
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
        await updateApplePaySettings({ ...formState.value } as ApplePaySettingsForm);
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
