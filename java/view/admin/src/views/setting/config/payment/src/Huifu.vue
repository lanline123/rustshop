<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" :rules="formRules" label-width="180px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">汇付天下支付设置</div>
                        <div class="notice-warp">
                            <p>汇付斗拱是汇付天下旗下的聚合支付品牌，一次接入即可同时支持微信、支付宝收款。开启后，支付页面将展示「微信支付」「支付宝支付」两个选项，用户付款体验与原生一致、对汇付渠道无感知。以下参数需先在汇付开通商户后获取，密钥相关请妥善保管、切勿外泄。</p>
                        </div>
                        <el-form-item label="是否开启" prop="useHuifu">
                            <el-radio-group v-model="formState.useHuifu" class="itemWidth">
                                <el-radio :value="1">开启</el-radio>
                                <el-radio :value="0">关闭</el-radio>
                            </el-radio-group>
                            <div class="extra">开启后，用户在支付页可使用「微信支付」「支付宝支付」；关闭则不展示这两个选项</div>
                        </el-form-item>

                        <div v-show="Number(formState.useHuifu) === 1">
                            <el-form-item label="系统号" prop="huifuSysId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.huifuSysId" placeholder="汇付系统号 sys_id" />
                                    <div class="extra">登录汇付商户控台（dashboard.huifu.com）→开发设置→开发者信息中获取</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="产品号" prop="huifuProductId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.huifuProductId" placeholder="汇付产品号 product_id" />
                                    <div class="extra">登录汇付商户控台→开发设置→开发者信息中获取，产品号对同一商户固定</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户号" prop="huifuMerchantId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.huifuMerchantId" placeholder="汇付商户号 huifu_id" />
                                    <div class="extra">商户进件成功后由汇付分配，在汇付商户控台→商户管理→商户信息查询中获取</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="商户RSA私钥" prop="huifuRsaPrivateKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.huifuRsaPrivateKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '商户RSA私钥（用于请求加签）', content: formState.huifuRsaPrivateKey }"
                                            path="setting/config/src/EditSecret"
                                            title="商户RSA私钥"
                                            width="600px"
                                            @okCallback="formState.huifuRsaPrivateKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">在汇付商户控台→开发设置→密钥管理中自助生成并下载（私钥仅首次生成时可下载，请妥善保管、不要外泄）。如无需更换，保持原样直接提交即可。</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="汇付公钥" prop="huifuPublicKey">
                                <div>
                                    <div class="flex flex-align-center" style="gap: 10px">
                                        <div class="secret-txt line1" style="max-width: 380px">
                                            {{ formState.huifuPublicKey }}
                                        </div>
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            :params="{ title: '汇付公钥（用于验签）', content: formState.huifuPublicKey }"
                                            path="setting/config/src/EditSecret"
                                            title="汇付公钥"
                                            width="600px"
                                            @okCallback="formState.huifuPublicKey = $event"
                                        >
                                            <el-button style="margin-top: 3px" link type="primary"> 编辑 </el-button>
                                        </DialogForm>
                                    </div>
                                    <div class="extra">在汇付商户控台→开发设置→密钥管理中获取的「汇付公钥」，用于校验汇付返回结果。如无需更换，保持原样直接提交即可。</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="网关地址" prop="huifuGateway">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.huifuGateway" placeholder="https://api.huifu.com" />
                                    <div class="extra">汇付接口地址，正式环境为 https://api.huifu.com；测试环境地址不同，请以汇付对接文档为准</div>
                                </div>
                            </el-form-item>
                            <el-form-item label="托管项目号" prop="huifuHostingProjectId">
                                <div>
                                    <TigInput classType="tig-form-input" v-model="formState.huifuHostingProjectId" placeholder="统一收银台托管项目号" />
                                    <div class="extra">PC 端收银台使用的托管项目号，在汇付商户控台中获取；不使用 PC 收银台时可留空</div>
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
import type { HuifuSettingsForm } from "@/types/setting/payment";
import { getHuifuSettings, updateHuifuSettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";

const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);

const previewHiddenKeys = computed(() => (Number(formState.value.useHuifu) === 1 ? ([] as const) : (["huifuWechat", "huifuAlipay"] as const)));
const previewSelected = computed(() => "huifuWechat" as const);

const formState = ref<Partial<HuifuSettingsForm>>({
    useHuifu: undefined,
    huifuSysId: "",
    huifuProductId: "",
    huifuMerchantId: "",
    huifuRsaPrivateKey: "",
    huifuPublicKey: "",
    huifuGateway: "",
    huifuHostingProjectId: ""
});

const validateRequiredWhenEnabled = (label: string) => {
    return (_rule: unknown, value: unknown, callback: (error?: Error) => void) => {
        if (Number(formState.value.useHuifu) !== 1) {
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
    if (Number(formState.value.useHuifu) !== 1) {
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
    huifuMerchantId: [{ validator: validateRequiredWhenEnabled("商户号"), trigger: "blur" }],
    huifuRsaPrivateKey: [{ validator: validateRequiredWhenEnabled("商户RSA私钥"), trigger: "blur" }],
    huifuPublicKey: [{ validator: validateRequiredWhenEnabled("汇付公钥"), trigger: "blur" }],
    huifuGateway: [{ validator: validateGateway, trigger: "blur" }]
}));

onMounted(async () => {
    loadFilter();
});

const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getHuifuSettings();
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
        await updateHuifuSettings({ ...formState.value } as HuifuSettingsForm);
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
