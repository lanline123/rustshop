/** * @file 提现设置页 * @description 配置会员提现功能参数 * @features * - 设置线下收款方式（银行卡/支付宝/微信） * - 开启/关闭提现功能 * -
设置最低提现金额和手续费 * - 配置提现审核流程 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <div class="content_wrapper" style="padding-bottom: 0">
                <el-tabs v-model="activeTab">
                    <el-tab-pane :label="$t('线下提现')" name="offline" />
                    <el-tab-pane :label="$t('线上提现')" name="online" />
                </el-tabs>
            </div>
            <el-form
                ref="formRef"
                :model="formState"
                label-width="140px"
                style="display: flex; gap: 12px; flex-direction: column"
                require-asterisk-position="right"
            >
                <div class="content_wrapper" v-show="activeTab === 'offline'">
                    <div class="title">{{ $t('提现设置') }}</div>
                    <el-form-item :label="$t('线下收款方式')" prop="withdrawalReceiptMethod" :rules="[{ required: true, message: $t('请选择线下收款方式') }]">
                        <div>
                            <el-checkbox-group v-model="formState.withdrawalReceiptMethod">
                                <el-checkbox :label="$t('银行卡')" :value="1" />
                                <el-checkbox :label="$t('支付宝')" :value="2" />
                                <el-checkbox :label="$t('微信')" :value="3" />
                            </el-checkbox-group>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('提现配置')" prop="withdrawalEnabled">
                        <div>
                            <el-radio-group v-model="formState.withdrawalEnabled" class="itemWidth">
                                <el-radio :value="0">{{ $t('不开通提现') }}</el-radio>
                                <el-radio :value="1">{{ $t('开通提现') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('不开通提现适用于由平台完全线下结算的场景') }}</div>
                        </div>
                    </el-form-item>
                    <div class="web-content" v-if="formState.withdrawalEnabled == 1">
                        <el-form-item label="" prop="">
                            <div class="wechat-config">
                                <el-form :model="formState" label-width="130px">
                                    <el-form-item :label="$t('提现限额')">
                                        <div class="flex align-center flex-wrap" style="gap: 15px">
                                            <TigInput type="integer" v-model="formState.minAmount" width="120px">
                                                <template #append>{{ configStore.config.amountUnit }}</template>
                                            </TigInput>
                                            <div>~</div>
                                            <TigInput type="integer" v-model="formState.maxAmount" width="120px">
                                                <template #append>{{ configStore.config.amountUnit }}</template>
                                            </TigInput>
                                        </div>
                                    </el-form-item>
                                    <el-form-item :label="$t('提现频次')" prop="withdrawalLimit">
                                        <div class="flex align-center flex-wrap" style="gap: 15px">
                                            <el-select v-model="formState.withdrawalFrequencyUnit" style="width: 120px">
                                                <el-option :label="$t('每日')" :value="1" />
                                                <el-option :label="$t('每月')" :value="2" />
                                                <el-option :label="$t('每年')" :value="3" />
                                            </el-select>
                                            <div>{{ $t('可申请提现') }}</div>
                                            <TigInput type="integer" v-model="formState.withdrawalFrequencyCount" width="120px">
                                                <template #append>{{ $t('次') }}</template>
                                            </TigInput>
                                        </div>
                                    </el-form-item>
                                    <el-form-item prop="collectionMethod" :rules="[{ required: true }]">
                                        <template #label>
                                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                                <template #content>
                                                    <div style="width: 150px; padding: 5px 10px">{{ $t('入账时间设置周期结束，资金会自动入账结算') }}</div>
                                                </template>
                                                <div class="flex flex-align-center">
                                                    <div>{{ $t('提现审核') }}</div>
                                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                                </div>
                                            </el-tooltip>
                                        </template>
                                        <div>
                                            <el-radio-group v-model="formState.withdrawalReviewMethod" class="itemWidth">
                                                <el-radio :value="1">{{ $t('人工审核（平台在提现管理页人工确认审核）') }}</el-radio>
                                            </el-radio-group>
                                        </div>
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-form-item>
                    </div>
                    <el-form-item :label="$t('提现说明')" prop="withdrawalDescription">
                        <div>
                            <TigInput
                                type="textarea"
                                v-model="formState.withdrawalDescription"
                                width="450px"
                                :placeholder="$t('请输入内容')"
                                :maxlength="800"
                                :rows="5"
                                showWordLimit
                            ></TigInput>
                            <div class="extra">{{ $t('注释：展示在商户添加账户的详情页，建议填写结算周期，打款方式等') }}</div>
                        </div>
                    </el-form-item>
                </div>

                <!-- ========== 线上提现 ========== -->
                <div class="content_wrapper" v-show="activeTab === 'online'">
                    <div class="title">{{ $t('线上提现设置') }}</div>
                    <el-alert
                        type="primary"
                        :closable="false"
                        show-icon
                        style="margin-bottom: 20px"
                        :title="$t('线上提现允许商户将线上分账到账的资金提现到银行账户。需先开启线上分账并完成子商户绑定。')"
                    />
                    <el-form-item :label="$t('线上收款方式')" prop="onlineWithdrawChannels">
                        <div>
                            <el-checkbox-group v-model="formState.onlineWithdrawChannels">
                            </el-checkbox-group>
                            <div class="extra">{{ $t('勾选后对应渠道的线上提现才对商户开放，可多选；提现限额/频次/审核等参数三渠道共用下方配置') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('提现配置')" prop="onlineWithdrawEnabled">
                        <div>
                            <el-radio-group v-model="formState.onlineWithdrawEnabled" class="itemWidth">
                                <el-radio :value="0">{{ $t('不开通提现') }}</el-radio>
                                <el-radio :value="1">{{ $t('开通提现') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('开启后，商户端提现页面将显示微信余额，商户可选择线上提现') }}</div>
                        </div>
                    </el-form-item>
                    <div class="web-content" v-if="formState.onlineWithdrawEnabled == 1">
                        <el-form-item label="" prop="">
                            <div class="wechat-config">
                                <el-form :model="formState" label-width="130px">
                                    <el-form-item :label="$t('提现限额')">
                                        <div class="flex align-center flex-wrap" style="gap: 15px">
                                            <TigInput type="integer" v-model="formState.onlineMinAmount" width="120px">
                                                <template #append>{{ $t('元') }}</template>
                                            </TigInput>
                                            <div>~</div>
                                            <TigInput type="integer" v-model="formState.onlineMaxAmount" width="120px">
                                                <template #append>{{ $t('元') }}</template>
                                            </TigInput>
                                        </div>
                                    </el-form-item>
                                    <el-form-item :label="$t('提现频次')">
                                        <div class="flex align-center flex-wrap" style="gap: 15px">
                                            <el-select v-model="formState.onlineWithdrawalFrequencyUnit" style="width: 120px">
                                                <el-option :label="$t('每日')" :value="1" />
                                                <el-option :label="$t('每月')" :value="2" />
                                                <el-option :label="$t('每年')" :value="3" />
                                            </el-select>
                                            <div>{{ $t('可申请提现') }}</div>
                                            <TigInput type="integer" v-model="formState.onlineWithdrawalFrequencyCount" width="120px">
                                                <template #append>{{ $t('次') }}</template>
                                            </TigInput>
                                        </div>
                                    </el-form-item>
                                    <el-form-item :label="$t('提现审核')">
                                        <el-radio-group v-model="formState.onlineWithdrawalReviewMethod" class="itemWidth">
                                            <el-radio :value="1">{{ $t('人工审核（平台在提现管理页人工确认审核）') }}</el-radio>
                                        </el-radio-group>
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-form-item>
                    </div>
                    <el-form-item :label="$t('提现说明')" prop="onlineWithdrawalDescription">
                        <div>
                            <TigInput
                                type="textarea"
                                v-model="formState.onlineWithdrawalDescription"
                                width="450px"
                                :placeholder="$t('请输入内容')"
                                :maxlength="800"
                                :rows="5"
                                showWordLimit
                            ></TigInput>
                            <div class="extra">{{ $t('注释：展示在商户添加账户的详情页，建议填写结算周期，打款方式等') }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '370px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, nextTick, watch } from "vue";
import { message } from "ant-design-vue";
import { withdrawalSetting } from "@/types/finance/profitsharingSetting.d";
import { getwithdrawalConfig, savewithdrawalConfig } from "@/api/finance/profitsharingSetting";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const activeTab = ref<string>("offline");
const formState = ref<Partial<withdrawalSetting>>({
    withdrawalReceiptMethod: [], //线下收款方式  1 银行卡  2 支付宝  3 微信
    withdrawalEnabled: undefined, //提现配置  0 不开通提现  1 开通提现
    minAmount: undefined, //提现限额  最小值
    maxAmount: undefined, //提现限额  最大值
    withdrawalFrequencyUnit: undefined, //提现频次  单位  1 每日  2 每月  3 每年
    withdrawalFrequencyCount: undefined, //提现频次  数量
    withdrawalReviewMethod: undefined, //提现审核  方式  1 人工审核
    withdrawalDescription: "", //提现说明
    onlineWithdrawEnabled: 0, //线上提现开关 0 关 1 开
    onlineMinAmount: undefined, //线上提现限额 最小值
    onlineMaxAmount: undefined, //线上提现限额 最大值
    onlineWithdrawalFrequencyUnit: undefined, //线上提现频次 单位
    onlineWithdrawalFrequencyCount: undefined, //线上提现频次 数量
    onlineWithdrawalReviewMethod: undefined, //线上提现审核 方式
    onlineWithdrawalDescription: "", //线上提现说明
    onlineWithdrawChannels: [] //线上收款方式（启用渠道，多选）WECHAT/HUIFU/ALIPAY
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getwithdrawalConfig();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        if (formState.value.withdrawalReceiptMethod == "" || formState.value.withdrawalReceiptMethod == null) {
            formState.value.withdrawalReceiptMethod = [];
        }
        if (!Array.isArray(formState.value.onlineWithdrawChannels) || formState.value.onlineWithdrawChannels.length === 0) {
            formState.value.onlineWithdrawChannels = ["WECHAT"];
        }
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
        const result = await savewithdrawalConfig(formState.value);
        message.success(t('修改成功'));
        configStore.updateConfig();
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

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
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

.error {
    color: red;
}

.wechat-config {
    background: #f5f7fa;
    width: 80%;
    padding: 20px 0px 10px 10px;
    .extra {
        margin: 0px 0 10px 0;
    }
    :deep(.el-form-item) {
        margin-bottom: 24px !important;
    }
    :deep(.el-select) {
        min-width: 120px;
    }
}
@media (max-width: 768px) {
    .web-content {
        :deep(.el-form-item__content) {
            margin-left: 0 !important;
        }
        .wechat-config {
            width: 100%;
            padding-right: 10px;
        }
    }
}
</style>
