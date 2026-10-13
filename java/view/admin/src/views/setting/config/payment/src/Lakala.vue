<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" :rules="formRules" label-width="180px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">拉卡拉支付设置</div>
                        <div class="notice-warp">
                            <p>拉卡拉聚合收单，一次接入即可支持微信、支付宝收款。开启后，支付页面将展示「微信」「支付宝」两个选项。下方参数请在拉卡拉开放平台 / 商户平台获取或按说明自行生成；密钥相关请妥善保管、切勿外泄。</p>
                        </div>
                        <el-form-item label="是否开启" prop="useLakala">
                            <el-radio-group v-model="formState.useLakala" class="itemWidth">
                                <el-radio :value="1">开启</el-radio>
                                <el-radio :value="0">关闭</el-radio>
                            </el-radio-group>
                            <div class="extra">开启后，用户在支付页可使用「微信」「支付宝」；关闭则不展示这两个选项</div>
                        </el-form-item>

                        <div v-show="Number(formState.useLakala) === 1">
                            <div class="title">基础参数</div>
                            <el-form-item label="开放平台 AppId" prop="lakalaAppId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaAppId" placeholder="拉卡拉开放平台 AppId" />
                                    <div class="extra">登录拉卡拉开放平台（open.lakala.com）→应用管理，创建/查看应用后获取；也可由拉卡拉对接人员开通后提供</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户号" prop="lakalaMerchantNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaMerchantNo" placeholder="商户号 merchantNo" />
                                    <div class="extra">商户进件成功后由拉卡拉分配，在拉卡拉商户平台→商户信息中查看</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="终端号" prop="lakalaTermNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaTermNo" placeholder="终端号 termNo" />
                                    <div class="extra">与商户号一并分配，在拉卡拉商户平台→终端管理中查看</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="证书序列号" prop="lakalaSerialNo">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaSerialNo" placeholder="商户证书序列号 serialNo" />
                                    <div class="extra">用拉卡拉提供的密钥/证书生成工具生成商户证书后获得（也可用 OpenSSL 查看 .cer 的 serial）</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="网关地址" prop="lakalaGatewayUrl">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaGatewayUrl" placeholder="https://s2.lakala.com" />
                                    <div class="extra">以拉卡拉对接文档为准；生产环境一般为 https://s2.lakala.com，沙箱地址不同请勿混用</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="环境" prop="lakalaSandbox">
                                <el-radio-group v-model="formState.lakalaSandbox" class="itemWidth">
                                    <el-radio :value="0">生产环境</el-radio>
                                    <el-radio :value="1">沙箱环境</el-radio>
                                </el-radio-group>
                                <div class="extra">沙箱用于联调，生产用于真实收款；上线前务必切生产，并与上方网关地址一致</div>
                            </el-form-item>

                            <div class="title">密钥与证书</div>
                            <el-form-item label="商户私钥" prop="lakalaMerchantPrivateKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.lakalaMerchantPrivateKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '商户私钥（PKCS8 Base64）', content: formState.lakalaMerchantPrivateKey }"
                                            path="setting/config/src/EditSecret"
                                            title="商户私钥"
                                            width="600px"
                                            @okCallback="formState.lakalaMerchantPrivateKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">用拉卡拉密钥生成工具生成（PKCS8 Base64），私钥仅本地保存、勿外泄。如无需更换，保持原样直接提交即可</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="平台证书" prop="lakalaPlatformCert">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.lakalaPlatformCert }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '拉卡拉开放平台证书（.cer 内容）', content: formState.lakalaPlatformCert }"
                                            path="setting/config/src/EditSecret"
                                            title="平台证书"
                                            width="600px"
                                            @okCallback="formState.lakalaPlatformCert = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">在拉卡拉开放平台→应用管理中下载平台公钥证书（.cer），粘贴文件全部内容。如无需更换，保持原样直接提交即可</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="SM4 密钥" prop="lakalaSm4Key">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.lakalaSm4Key }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '报文对称加密 SM4 密钥', content: formState.lakalaSm4Key }"
                                            path="setting/config/src/EditSecret"
                                            title="SM4 密钥"
                                            width="600px"
                                            @okCallback="formState.lakalaSm4Key = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">开通报文对称加密后由拉卡拉提供；未开通可留空</div>
                                </div>
                            </el-form-item>

                            <div class="title">微信支付参数</div>
                            <el-form-item label="微信子 AppId" prop="lakalaWxSubAppid">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaWxSubAppid" placeholder="wxsubxxxxxxxx" />
                                    <div class="extra">微信公众平台→设置与开发→开发者ID 中的 AppID（公众号内 H5/JSAPI 必填，且需在拉卡拉侧完成绑定）</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="微信小程序 AppId" prop="lakalaWxMiniAppid">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.lakalaWxMiniAppid" placeholder="wxminixxxxxxxx" />
                                    <div class="extra">微信小程序后台→设置→开发设置中的 AppID（小程序场景必填，且需在拉卡拉侧完成绑定）</div>
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
import type { LakalaSettingsForm } from "@/types/setting/payment";
import { getLakalaSettings, updateLakalaSettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";

const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);

const previewHiddenKeys = computed(() => (Number(formState.value.useLakala) === 1 ? ([] as const) : (["lakalaWechat", "lakalaAlipay"] as const)));
const previewSelected = computed(() => "lakalaWechat" as const);

const formState = ref<Partial<LakalaSettingsForm>>({
    useLakala: undefined,
    lakalaAppId: "",
    lakalaMerchantNo: "",
    lakalaTermNo: "",
    lakalaSerialNo: "",
    lakalaMerchantPrivateKey: "",
    lakalaPlatformCert: "",
    lakalaSm4Key: "",
    lakalaGatewayUrl: "",
    lakalaSandbox: 0,
    lakalaWxSubAppid: "",
    lakalaWxMiniAppid: ""
});

const validateRequiredWhenEnabled = (label: string) => {
    return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        if (Number(formState.value.useLakala) !== 1) {
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
    if (Number(formState.value.useLakala) !== 1) {
        callback();
        return;
    }
    const v = String(value ?? "").trim();
    if (!v) {
        callback(new Error("请填写网关地址"));
        return;
    }
    if (!/^https?:\/\//i.test(v)) {
        callback(new Error("网关地址需以 http:// 或 https:// 开头"));
        return;
    }
    callback();
};

const formRules = computed<FormRules>(() => ({
    lakalaAppId: [{ validator: validateRequiredWhenEnabled("开放平台 AppId"), trigger: "blur" }],
    lakalaMerchantNo: [{ validator: validateRequiredWhenEnabled("商户号"), trigger: "blur" }],
    lakalaTermNo: [{ validator: validateRequiredWhenEnabled("终端号"), trigger: "blur" }],
    lakalaSerialNo: [{ validator: validateRequiredWhenEnabled("证书序列号"), trigger: "blur" }],
    lakalaMerchantPrivateKey: [{ validator: validateRequiredWhenEnabled("商户私钥"), trigger: "blur" }],
    lakalaPlatformCert: [{ validator: validateRequiredWhenEnabled("平台证书"), trigger: "blur" }],
    lakalaGatewayUrl: [{ validator: validateGateway, trigger: "blur" }]
}));

onMounted(async () => {
    loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getLakalaSettings();
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
        await updateLakalaSettings({ ...formState.value } as LakalaSettingsForm);
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
