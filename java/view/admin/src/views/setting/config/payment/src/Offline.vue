<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" :rules="formRules" label-width="160px">
            <div class="content">
                <div class="title">{{ $t('线下支付设置') }}</div>
                <el-form-item :label="$t('是否开启')" prop="useOffline">
                    <el-radio-group v-model="formState.useOffline" class="itemWidth">
                        <el-radio :value="1">{{ $t('开启') }}</el-radio>
                        <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                    </el-radio-group>
                    <div class="extra ml8">{{ $t('是否开启线下支付，如关闭则不显示线下支付选项') }}</div>
                </el-form-item>
                <el-form-item :label="$t('汇款方式')" prop="paymentMethods" v-if="formState.useOffline === 1">
                    <el-checkbox-group v-model="selectedPaymentMethods" class="itemWidth">
                        <el-checkbox label="thirdParty">{{ $t('第三方支付转账') }}</el-checkbox>
                        <el-checkbox label="bank">{{ $t('银行汇款') }}</el-checkbox>
                        <el-checkbox label="company">{{ $t('企业汇款') }}</el-checkbox>
                        <el-checkbox label="other">{{ $t('其他汇款方式') }}</el-checkbox>
                    </el-checkbox-group>
                    <div class="extra ml8">{{ $t('选择可用的汇款方式，至少选择一种') }}</div>
                </el-form-item>
                <el-form-item :label="$t('汇款说明')" v-if="formState.useOffline === 1 && hasSelectedPaymentMethods">
                    <el-tabs type="border-card" v-model="activeName" v-if="!loading">
                        <el-tab-pane :label="$t('第三方支付转账')" :name="1" v-if="formState.thirdPartyPaymentTransferEnable === 1">
                            <div class="relative">
                                <Editor v-model:html="formState.thirdPartyPaymentTransfer" :border="false"></Editor>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('银行汇款')" :name="2" v-if="formState.offlinePayBankEnable === 1">
                            <div class="relative">
                                <Editor v-model:html="formState.offlinePayBank" :border="false"></Editor>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('企业汇款')" :name="3" v-if="formState.offlinePayCompanyEnable === 1">
                            <div class="relative">
                                <Editor v-model:html="formState.offlinePayCompany" :border="false"></Editor>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane :label="$t('其他汇款方式')" :name="4" v-if="formState.otherPaymentRemittanceEnable === 1">
                            <div class="relative">
                                <Editor v-model:html="formState.otherPaymentRemittance" :border="false"></Editor>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </el-form-item>
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
import { onMounted, ref, shallowRef, computed, watch } from "vue";
import { Editor } from "@/components/editor/index";
import { message } from "ant-design-vue";
import type { OfflinePaySettingsForm } from "@/types/setting/payment";
import { getOfflinePaySettings, updateOfflinePaySettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { isOverseas } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
// 基本参数定义
const activeName = ref<number>(1);
const confirmLoading = ref<boolean>(false);

// 表单验证规则
const validatePaymentMethods = (rule: any, value: any, callback: any) => {
    // 如果线下支付未开启，则不需要验证
    if (formState.value.useOffline !== 1) {
        callback();
        return;
    }
    // 如果线下支付开启，则至少需要选择一项汇款方式
    if (selectedPaymentMethods.value.length === 0) {
        callback(new Error(t('请至少选择一种汇款方式')));
        return;
    }
    callback();
};

const formRules = {
    paymentMethods: [{ validator: validatePaymentMethods, trigger: "change" }]
};
const formState = ref<Partial<OfflinePaySettingsForm>>({
    useOffline: undefined,
    thirdPartyPaymentTransferEnable: 0,
    offlinePayBankEnable: 0,
    offlinePayCompanyEnable: 0,
    otherPaymentRemittanceEnable: 0,
    thirdPartyPaymentTransfer: "",
    offlinePayBank: "",
    offlinePayCompany: "",
    otherPaymentRemittance: ""
});

// 汇款方式选项
const selectedPaymentMethods = ref<string[]>([]);

// 计算是否有选中的汇款方式
const hasSelectedPaymentMethods = computed(() => {
    return selectedPaymentMethods.value.length > 0;
});

// 更新激活的标签页
const updateActiveTab = () => {
    if (formState.value.thirdPartyPaymentTransferEnable === 1) {
        activeName.value = 1;
    } else if (formState.value.offlinePayBankEnable === 1) {
        activeName.value = 2;
    } else if (formState.value.offlinePayCompanyEnable === 1) {
        activeName.value = 3;
    } else if (formState.value.otherPaymentRemittanceEnable === 1) {
        activeName.value = 4;
    }
};

// 监听选中的汇款方式变化，同步到表单状态
watch(selectedPaymentMethods, (newVal, oldVal) => {
    formState.value.thirdPartyPaymentTransferEnable = newVal.includes("thirdParty") ? 1 : 0;
    formState.value.offlinePayBankEnable = newVal.includes("bank") ? 1 : 0;
    formState.value.offlinePayCompanyEnable = newVal.includes("company") ? 1 : 0;
    formState.value.otherPaymentRemittanceEnable = newVal.includes("other") ? 1 : 0;
});

// 监听表单状态变化，同步到选中的汇款方式（用于从服务器加载数据时）
watch(
    () => [
        formState.value.thirdPartyPaymentTransferEnable,
        formState.value.offlinePayBankEnable,
        formState.value.offlinePayCompanyEnable,
        formState.value.otherPaymentRemittanceEnable
    ],
    (newVal, oldVal) => {
        const methods: string[] = [];
        
        // 定义选项映射
        const options = [
            { key: 'thirdParty', index: 1 },
            { key: 'bank', index: 2 },
            { key: 'company', index: 3 },
            { key: 'other', index: 4 }
        ];
        
        // 检测哪个选项发生了变化
        let changedOption: { key: string; index: number } | null = null;
        let isSelected = false; // true表示选中，false表示取消选中
        
        if (oldVal && oldVal.length === 4) {
            // 比较新旧值，找出变化的选项
            for (let i = 0; i < options.length; i++) {
                const oldEnable = oldVal[i] ?? 0;
                const newEnable = newVal[i] ?? 0;
                
                if (oldEnable !== newEnable) {
                    changedOption = { key: options[i].key, index: options[i].index };
                    isSelected = newEnable === 1; // 新值为1表示选中，0表示取消选中
                    break; // 只处理第一个变化的选项
                }
            }
        }
        
        // 构建选中的汇款方式数组
        options.forEach((option, index) => {
            if ((newVal[index] ?? 0) === 1) {
                methods.push(option.key);
            }
        });
        
        // 根据变化情况更新activeName
        if (changedOption) {
            if (isSelected) {
                // 如果是选中，显示对应选项的汇款说明
                activeName.value = changedOption.index;
            } else {
                // 如果是取消选中,且之前选中的就是当前选项，则更新标签页
                if (changedOption.index === activeName.value) {
                    updateActiveTab();
                }
            }
        } else {
            // 初始化时，设置第一个可用的标签页
            updateActiveTab();
        }
        
        selectedPaymentMethods.value = methods;
    },
    { immediate: true }
);

// 加载列表
onMounted(async () => {
    loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getOfflinePaySettings();
        const convertedResult = convertNullsToEmptyStrings(result);
        // 确保 enable 字段被正确初始化为 0 或 1
        if (convertedResult.thirdPartyPaymentTransferEnable === undefined || convertedResult.thirdPartyPaymentTransferEnable === null) {
            convertedResult.thirdPartyPaymentTransferEnable = 0;
        }
        if (convertedResult.offlinePayBankEnable === undefined || convertedResult.offlinePayBankEnable === null) {
            convertedResult.offlinePayBankEnable = 0;
        }
        if (convertedResult.offlinePayCompanyEnable === undefined || convertedResult.offlinePayCompanyEnable === null) {
            convertedResult.offlinePayCompanyEnable = 0;
        }
        if (convertedResult.otherPaymentRemittanceEnable === undefined || convertedResult.otherPaymentRemittanceEnable === null) {
            convertedResult.otherPaymentRemittanceEnable = 0;
        }
        Object.assign(formState.value, convertedResult);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch (error) {
        // 验证失败，不提交
        return;
    }
    confirmLoading.value = true;
    try {
        const submitData: OfflinePaySettingsForm = {
            useOffline: formState.value.useOffline ?? 0,
            thirdPartyPaymentTransferEnable: formState.value.thirdPartyPaymentTransferEnable ?? 0,
            offlinePayBankEnable: formState.value.offlinePayBankEnable ?? 0,
            offlinePayCompanyEnable: formState.value.offlinePayCompanyEnable ?? 0,
            otherPaymentRemittanceEnable: formState.value.otherPaymentRemittanceEnable ?? 0,
            thirdPartyPaymentTransfer: formState.value.thirdPartyPaymentTransfer ?? "",
            offlinePayBank: formState.value.offlinePayBank ?? "",
            offlinePayCompany: formState.value.offlinePayCompany ?? "",
            otherPaymentRemittance: formState.value.otherPaymentRemittance ?? ""
        };
        await updateOfflinePaySettings(submitData);
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

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}
</style>
