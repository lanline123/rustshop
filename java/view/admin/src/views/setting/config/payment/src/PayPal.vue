<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="160px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">{{ $t('PayPal支付设置') }}</div>
                        <el-form-item :label="$t('是否开启')" prop="usePaypal">
                            <el-radio-group v-model="formState.usePaypal" class="itemWidth">
                                <el-radio :value="1">{{ $t('开启') }}</el-radio>
                                <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('是否开启PayPal支付，如关闭则不显示PayPal支付选项') }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('货币')" prop="paypalCurrency">
                            <div>
                                <el-select v-model="formState.paypalCurrency" style="width: 450px">
                                    <el-option :value="0" :label="$t('不选择')" />
                                    <el-option v-for="item in formState.paypalCurrencyList" :key="item.id" :value="item.value" :label="item.name" />
                                </el-select>
                                <div class="extra"> {{ $t('如果选择了默认国家，则会员在选择地址时默认会显示该国家下的省份或地区，不再显示国家选择（修改设置对已经添加的会员地址不生效）') }} {{ $t('请保持和商城默认货币一致，否则会导致下单和结账货币不一致') }} </div>
                            </div>
                        </el-form-item>
                        <el-form-item label="Client ID" prop="paypalClientId">
                            <TigInput classType="tig-form-input" v-model="formState.paypalClientId" />
                        </el-form-item>
                        <el-form-item label="Secret" prop="paypalSecret">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.paypalSecret }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'Secret', content: formState.paypalSecret }"
                                        path="setting/config/src/EditSecret"
                                        title="Secret"
                                        width="600px"
                                        @okCallback="formState.paypalSecret = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的Secret') }}</div>
                            </div>
                        </el-form-item>
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
import { message } from "ant-design-vue";
import type { PayPalSettingsForm } from "@/types/setting/payment";
import { getPayPalSettings, updatePayPalSettings } from "@/api/setting/payment";
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
const previewHiddenKeys = computed(() => (Number(formState.value.usePaypal) === 1 ? ([] as const) : (["paypal"] as const)));
const previewSelected = computed(() => "paypal" as const);
const formState = ref<Partial<PayPalSettingsForm>>({
    usePaypal: undefined,
    paypalCurrency: "paypalCurrency_86d4b0562507",
    paypalClientId: "paypalClientId_ade0fded0622",
    paypalSecret: "paypalSecret_e23291d5b03b"
});
// 加载列表
onMounted(async () => {
    loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getPayPalSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
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
        const result = await updatePayPalSettings({
            ...formState.value
        });
        message.success(t('修改成功'));
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
        // padding-top: 20px;
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
