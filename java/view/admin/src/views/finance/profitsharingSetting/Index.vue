/** * @file 分账设置页 * @description 配置订单分账规则和参数 * @features * - 设置出账节点（支付成功/售后完成） * - 配置分账比例和接收方 * - 设置自动分账时间 * -
分账日志查询 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form
                ref="formRef"
                :model="formState"
                label-width="150px"
                style="display: flex; gap: 12px; flex-direction: column"
                require-asterisk-position="right"
            >
                <div class="content_wrapper">
                    <div class="title">{{ $t('分账基础设置') }}</div>
                    <el-form-item :label="$t('分账功能')" prop="profitSharingEnabled">
                        <div>
                            <el-switch
                                :model-value="formState.profitSharingEnabled === 1"
                                @change="(val: any) => (formState.profitSharingEnabled = val ? 1 : 0)"
                            />
                            <div class="extra">{{ $t('关闭后全部分账停用；开启后按下方设置的分账方式与渠道生效') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="billingNode" :rules="[{ required: true, message: $t('请选择出账节点') }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 300px; padding: 5px 10px">{{ $t('订单支付成功或售后单退款完成，分账方能查看可获得的分账金额') }}</div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('出账节点') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <el-radio-group v-model="formState.billingNode" class="itemWidth" :disabled="true">
                                <el-radio :value="1">{{ $t('下单成功/售后申请') }}</el-radio>
                                <!-- <el-radio :value="2">售后申请</el-radio> -->
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item prop="collectionNode" :rules="[{ required: true, message: $t('请选择入账节点') }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 100px; padding: 5px 10px">{{ $t('分账方获得金额的节点') }}</div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('入账节点') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <el-radio-group v-model="formState.collectionNode" class="itemWidth" :disabled="true">
                                <el-radio :value="1">{{ $t('订单完成/确认收货') }}</el-radio>
                                <!-- <el-radio :value="2">确认收货</el-radio> -->
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item prop="collectionTimeSetting" :rules="[{ required: true, validator: validateCollectionTimeSetting }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 300px; padding: 5px 10px"> {{ $t('入账时间满足，对应分账账单 触发入账结算 入账节点是订单完成（确认收货），入账时间设置10天，则代表结算时间是订单完成10天后结算') }} </div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('入账时间') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <TigInput type="integer" v-model="formState.collectionTimeSetting" width="120px" :min="0" :max="15">
                                <template #append>{{ $t('天') }}</template>
                            </TigInput>
                            <div class="extra">{{ $t('限制 0-15 天，默认0天') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="collectionMethod" :rules="[{ required: true }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 150px; padding: 5px 10px">{{ $t('入账时间设置周期结束，资金会自动入账结算') }}</div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('入账方式') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <div v-if="formState.collectionNode === 1">{{ $t('自动入账') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="collectionAccountType" :rules="[{ required: true }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 150px; padding: 5px 10px">{{ $t('资金会通过店铺/门店/供应商账户余额结算') }}</div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('入账账户类型') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <div v-if="formState.collectionAccountType === 1">{{ $t('余额') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="splitPaymentMethod" :rules="[{ required: true }]">
                        <template #label>
                            <el-tooltip class="box-item" effect="light" placement="right" show-after="300">
                                <template #content>
                                    <div style="width: 220px; padding: 5px 10px">{{ $t('线下分账：商家通过对账单线下对账结算；线上分账：通过线上支付分账能力，订单资金由渠道直接分账到子商户账户') }}</div>
                                </template>
                                <div class="flex flex-align-center">
                                    <div>{{ $t('分账方式') }}</div>
                                    <el-icon style="margin-left: 5px" size="14" color="#999"><QuestionFilled /></el-icon>
                                </div>
                            </el-tooltip>
                        </template>
                        <div>
                            <el-radio-group v-model="formState.splitPaymentMethod" @change="handleSplitPaymentMethodChange">
                                <el-radio :value="1">{{ $t('线下分账') }}</el-radio>
                            </el-radio-group>
                            <div
                                v-if="formState.splitPaymentMethod === 2 && availableOnlineProfitSharingChannels.length > 0"
                                class="extra extra-inline"
                            >
                                <span>{{ $t('自营店铺不支持服务商收款。若要启用线上支付分账，需先新建子商户，订单资金将由所选渠道直接分账至子商户账户。') }}</span>
                                <el-button link type="primary" size="small" @click="goToMerchantApplication">{{ $t('去创建') }}</el-button>
                            </div>
                            <div v-else class="extra">{{ $t('商家通过对账单线下对账结算') }}</div>
                        </div>
                    </el-form-item>
                    <!-- 线上分账配置（分账为通用能力，渠道选项按各自 feature 切割） -->
                </div>
                <div class="content_wrapper" v-if="isMerchant()">
                    <div class="title">{{ $t('店铺分账设置') }}</div>
                    <el-form-item :label="$t('通用平台服务费率')" prop="storeGeneralServiceFeeRate" :rules="[{ required: true, validator: validateGeneralSetting }]">
                        <div>
                            <TigInput type="decimal" v-model="formState.storeGeneralServiceFeeRate" width="150px" :min="0" :max="100">
                                <template #append>%</template>
                            </TigInput>
                            <div class="extra extra-inline">
                                <span>{{ $t('店铺订单入账时平台抽取的服务费，您也可按品类单独设置，品类规则将优先生效。') }}</span>
                                <el-button link type="primary" size="small" @click="goToCategoryServiceFeeRule('store')">{{ $t('去设置>>') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('店铺提现手续费率')" prop="storeWithdrawalFeeRate" :rules="[{ required: true, validator: validateGeneralSetting }]">
                        <div>
                            <TigInput type="decimal" v-model="formState.storeWithdrawalFeeRate" width="150px" :min="0" :max="100">
                                <template #append>%</template>
                            </TigInput>
                            <div class="extra">{{ $t('店铺提现时，平台将抽取的提现手续费') }}</div>
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
import { Rank, QuestionFilled } from "@element-plus/icons-vue";
import { onMounted, ref, shallowRef, nextTick, watch, computed } from "vue";
import { message, Modal } from "ant-design-vue";
import { ProfitsharingSetting } from "@/types/finance/profitsharingSetting.d";
import { getProfitsharingConfig, saveProfitsharingConfig } from "@/api/finance/profitsharingSetting";
import { useConfigStore } from "@/store/config";
import { useRoute, useRouter } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { isS2b2c, isStore, isMerchant } from "@/utils/version";
import { getOnlineChannelExtraKey } from "./onlineChannelExtra";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const router = useRouter();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<ProfitsharingSetting>>({
    billingNode: undefined, // 出账节点，值为 1（下单成功） 值为 2（售后申请）
    collectionNode: undefined, // 入账节点，值为 1（订单完成） 值为 2（确认收货）
    collectionTimeSetting: undefined, // 入账时间，值为 10 天
    collectionMethod: undefined, // 入账方式 1-自动入账 2-手动入账
    collectionAccountType: undefined, // 入账账户类型 1-余额
    profitSharingEnabled: 1, // 分账总开关 0-关 1-开（默认 1）
    splitPaymentMethod: 1, // 分账方式 1-线下分账 2-线上分账
    profitSharingOnlineChannel: "wechat", // 线上分账渠道 wechat-微信 huifu-汇付
    autoProfitSharingEnabled: 0, // 是否启用自动分账 0-否 1-是
    autoProfitSharingDays: undefined, // 自动分账等待天数

    storeGeneralServiceFeeRate: undefined, // 店铺通用平台服务费率，5%
    storeWithdrawalFeeRate: undefined, // 店铺提现手续费率，0%

    storefrontGeneralServiceFeeRate: undefined, // 门店通用平台服务费率，5%
    storefrontWithdrawalFeeRate: undefined, // 门店提现手续费率，0%

    supplierGeneralServiceFeeRate: undefined, // 供应商通用平台服务费率，5%
    supplierWithdrawalFeeRate: undefined // 供应商提现手续费率，0%
});
const availableOnlineProfitSharingChannels = computed(() => {
    const channels: string[] = [];
    return channels;
});
// 线上分账渠道说明文案
const onlineChannelExtra = computed(() => {
    return t(getOnlineChannelExtraKey(formState.value.profitSharingOnlineChannel, isStore()));
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getProfitsharingConfig();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        // 分账总开关为空时兜底默认开启（对齐后端默认 1）
        if (formState.value.profitSharingEnabled !== 0 && formState.value.profitSharingEnabled !== 1) {
            formState.value.profitSharingEnabled = 1;
        }
        const availableChannels = availableOnlineProfitSharingChannels.value;
        if (availableChannels.length === 0) {
            formState.value.splitPaymentMethod = 1;
            formState.value.profitSharingOnlineChannel = undefined;
        } else if (!availableChannels.includes(formState.value.profitSharingOnlineChannel || "")) {
            formState.value.profitSharingOnlineChannel = availableChannels[0];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const validateCollectionTimeSetting = (rule: any, value: number, callback: any) => {
    if (value < 0 || value > 15) {
        callback(new Error(t('限制 0-15 天')));
    } else {
        callback();
    }
};
const validateGeneralSetting = (rule: any, value: number, callback: any) => {
    if (value < 0 || value > 100) {
        callback(new Error(t('限制 0-100%')));
    } else {
        callback();
    }
};
const goToCategoryServiceFeeRule = (tab: "store" | "supplier" = "store") => {
    router.push({
        name: "categoryFeeRuleManage",
        query: { tab }
    });
};
const goToMerchantApplication = () => {
    router.push({
        name: "merchantApplicationManage"
    });
};
const validateAutoDays = (rule: any, value: number, callback: any) => {
    if (formState.value.autoProfitSharingEnabled !== 1) {
        callback();
        return;
    }
    if (value === undefined || value === null || (value as any) === "") {
        callback(new Error(t('请设置自动分账等待天数')));
    } else if (value < 0 || value > 30) {
        callback(new Error(t('限制 0-30 天')));
    } else {
        callback();
    }
};
// 切换分账方式提示
const handleSplitPaymentMethodChange = () => {
    Modal.info({
        title: t('切换分账方式'),
        content: t('切换后，已支付订单不受影响，后续新订单将按新分账规则执行')
    });
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    confirmLoading.value = true;
    try {
        const result = await saveProfitsharingConfig(formState.value as ProfitsharingSetting);
        message.success(t('修改成功'));
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
</style>
