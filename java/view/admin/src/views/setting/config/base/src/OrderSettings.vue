/** * @file 订单设置页 * @description 配置订单相关的业务规则 * @features * - 设置自动确认收货时间 * - 配置自动同意退货申请 * - 设置售后申请时间限制 * -
配置未付款订单自动取消时间 * - 设置库存预警阈值 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="180px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t("基础设置") }}</div>
                    <el-form-item :label="$t('发货后自动确认收货时间')" prop="autoDeliveryDays">
                        <div>
                            <div class="flex">
                                <p class="mr10">{{ $t("卖家发货后") }}</p>
                                <TigInput type="integer" v-model="formState.autoDeliveryDays" width="110px"></TigInput>
                                <p class="ml10">{{ $t("天，自动确认收货") }}</p>
                            </div>
                            <div class="extra extra-tooltip">
                                <span>{{ $t("修改后将对新产生的订单立即生效，已产生的订单自动确认收货时间将以发货时的设置为准。") }}</span>
                                <el-tooltip class="box-item" effect="light" placement="bottom" show-after="300">
                                    <template #content>
                                        <div style="padding: 5px 10px">{{ $t("交付模式为按需兑换商品除外") }}</div>
                                    </template>
                                    <div class="flex flex-align-center">
                                        <el-icon size="14" color="#999">
                                            <QuestionFilled />
                                        </el-icon>
                                    </div>
                                </el-tooltip>
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('虚拟商品自动发货')" prop="virtualOrderAutoDeliver">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.virtualOrderAutoDeliver" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">
                                {{
                                    $t(
                                        "开启后，整单商品均为卡密、虚拟商品、付费内容或电子礼品卡的订单，支付成功后由系统自动以「无需配送」发货，买家立即可见卡密与虚拟内容；含实物商品的混合订单仍需手动发货。"
                                    )
                                }}
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('未付款订单')" prop="autoCancelOrderMinute">
                        <div class="itemWidth flex">
                            <span class="mr8">{{ $t("订单超") }}</span>
                            <TigInput type="integer" v-model="formState.autoCancelOrderMinute" width="60px" />
                            <span class="ml8">{{ $t("分钟未支付自动取消") }}</span>
                        </div>
                        <div class="extra">{{ $t("未支付的订单会自动取消，请填写分钟，比如10则代表10分钟未付款则取消订单，为0或空则不限制") }}</div>
                    </el-form-item>
                    <!-- <el-form-item label="订单流转设置" prop="isChangeOrderStatus">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.isChangeOrderStatus" class="itemWidth">
                                    <el-radio :value="1">开启</el-radio>
                                    <el-radio :value="0">关闭</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">开启后可对订单状态进行更改，更改完成后请及时关闭此功能，以免数据错乱。</div>
                        </div>
                    </el-form-item> -->
                </div>

                <div class="content_wrapper">
                    <div class="title">{{ $t("售后设置") }}</div>

                    <el-alert
                        v-if="showReviewWarning"
                        :title="$t('当前售后审核与财务审核均处于关闭状态，易引发资金安全风险，强烈建议开启任意一项审核。')"
                        type="warning"
                        show-icon
                        :closable="false"
                        style="margin-bottom: 20px"
                    />

                    <el-form-item :label="$t('买家申请售后限制')" prop="afterSalesLimitDays">
                        <div>
                            <div class="flex">
                                <p class="mr10">{{ $t("买家确认收货") }}</p>
                                <TigInput type="integer" v-model="formState.afterSalesLimitDays" width="110px"> </TigInput>
                                <p class="ml10">{{ $t("天后，不支持买家申请售后") }}</p>
                            </div>
                            <div class="extra">{{ $t("注：不适用虚拟商品、卡密商品、付费内容等特殊类型订单。") }}</div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('售后审核')" prop="afterSalesReview">
                        <div>
                            <div>
                                <el-checkbox v-model="formState.afterSalesReview" :label="$t('开启售后审核')" :true-value="1" :false-value="0" />
                            </div>
                            <div v-if="afterSalesReviewTip" class="extra">
                                {{ afterSalesReviewTip }}
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('自动同意退货申请')" prop="autoReturnGoods" v-if="formState.afterSalesReview">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.autoReturnGoods" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <el-form-item
                                label=""
                                prop="autoReturnGoodsDays"
                                :rules="[{ required: formState.autoReturnGoods == 1, message: '请输入卖家自动同意售后天数' }]"
                            >
                                <div class="flex">
                                    <p class="mr10">{{ $t("买家申请") }}</p>
                                    <TigInput
                                        type="integer"
                                        v-model="formState.autoReturnGoodsDays"
                                        width="110px"
                                        :disabled="formState.autoReturnGoods == 0"
                                    ></TigInput>
                                    <p class="ml10">{{ $t("天后，自动同意客户售后申请") }}</p>
                                </div>
                            </el-form-item>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('买家退货')" prop="autoConfirmReceiptDays">
                        <div>
                            <div class="flex">
                                <TigInput type="integer" v-model="formState.autoConfirmReceiptDays" width="110px"> </TigInput>
                                <p class="ml10">{{ $t("天后，系统将自动确认售后") }}</p>
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('财务审核')" prop="financialReview">
                        <div>
                            <div>
                                <el-checkbox v-model="formState.financialReview" :label="$t('开启财务审核')" :true-value="1" :false-value="0" />
                            </div>
                            <div class="extra">
                                {{ $t("开启后，售后申请经售后人员处理完毕，需由财务人员再次审核并执行退款操作。若关闭，线下退款的订单金额，需商家处理退款。") }}
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('售后原因')" prop="afterSalesReasonList">
                        <div class="reason-setting">
                            <el-checkbox v-model="formState.afterSalesReasonEnabled" :label="$t('开启售后原因功能')" :true-value="1" :false-value="0" />
                            <div class="extra">
                                {{ $t("开启后，买家申请售后和取消未支付订单时需选择原因，最多添加10项。") }}
                            </div>
                            <div class="reason-list">
                                <el-tag
                                    v-for="(reason, index) in formState.afterSalesReasonList"
                                    :key="`${reason}-${index}`"
                                    closable
                                    @close="removeReason(index)"
                                >
                                    {{ reason }}
                                </el-tag>
                            </div>
                            <div v-if="reasonInputVisible" class="reason-input">
                                <el-input
                                    ref="reasonInputRef"
                                    v-model="reasonInputValue"
                                    maxlength="50"
                                    show-word-limit
                                    @keyup.enter="confirmReasonInput"
                                    @blur="handleReasonInputBlur"
                                />
                            </div>
                            <el-button v-else type="primary" link :disabled="(formState.afterSalesReasonList?.length || 0) >= 10" @click="showReasonInput">
                                {{ $t("+ 新增原因") }}
                            </el-button>
                        </div>
                    </el-form-item>
                </div>

                <div class="content_wrapper">
                    <div class="title">{{ $t("平台管理订单流转设置") }}</div>
                    <el-form-item :label="$t('已支付订单可取消订单')" prop="isPlatformCancelPaidOrder">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.isPlatformCancelPaidOrder" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">
                                {{ $t("开启后，客户已支付的订单（待备货/待发货状态），管理员可以操作取消支付，已付款金额会流转至财务退款售后处理") }}
                                <el-popover :width="540" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t("查看示例") }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/isPlatformCancelPaidOrder.png" style="width: 500px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('已发货订单可取消发货')" prop="isPlatformCancelDeliverOrder">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.isPlatformCancelDeliverOrder" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">
                                {{ $t("开启后，已发货订单平台管理员可取消发货，已填写的物流信息将会清空") }}
                                <el-popover :width="540" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t("查看示例") }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/isPlatformCancelDeliverOrder.png" style="width: 500px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <div class="content_wrapper" v-if="isMerchant()">
                    <div class="title">{{ $t("店铺后台订单流转设置") }}</div>
                    <el-form-item :label="$t('已发货订单可取消发货')" prop="isShopCancelDeliverOrder">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.isShopCancelDeliverOrder" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">
                                {{ $t("开启后，店铺后台已发货订单可取消发货，已填写的物流信息将会清空") }}
                                <el-popover :width="540" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t("查看示例") }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/isShopCancelDeliverOrder.png" style="width: 500px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <div class="content_wrapper">
                    <div class="title">{{ $t("平台代客下单设置") }}</div>
                    <el-form-item :label="$t('使用积分')" prop="adminShoppingUsePoint">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.adminShoppingUsePoint" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">{{ $t("开启后平台端代客下单允许使用积分") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('使用余额')" prop="adminShoppingUseBalance">
                        <div>
                            <div>
                                <el-radio-group v-model="formState.adminShoppingUseBalance" class="itemWidth">
                                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                                </el-radio-group>
                            </div>
                            <div class="extra">{{ $t("开启后平台端代客下单允许使用余额") }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, nextTick, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import type { OrderConfig } from "@/types/setting/config";
import { getConfigOrder, saveConfigOrder } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useRoute } from "vue-router";
import { useThemeStore } from "@/store/theme";
import { isB2CPro, isMerchant, isS2b2c, isStore } from "@/utils/version";
import { getAdminType } from "@/utils/storage";
import { QuestionFilled } from "@element-plus/icons-vue";
import { useI18n } from "vue-i18n";
import { normalizeReason, type ReasonValidationError, validateReasonList } from "./orderReason";

const { t } = useI18n();
type OrderConfigFormState = Partial<OrderConfig> & {
    afterSalesReview?: number;
    financialReview?: number;
};

const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const formState = ref<OrderConfigFormState>({
    autoDeliveryDays: undefined,
    autoReturnGoods: undefined,
    autoReturnGoodsDays: undefined,
    afterSalesLimitDays: undefined,
    afterSalesReview: undefined,
    financialReview: undefined,
    virtualOrderAutoDeliver: 0,
    afterSalesReasonEnabled: 0,
    afterSalesReasonList: [],
    autoCancelOrderMinute: undefined,
    cycleOrderAdvanceDays: undefined,
    cycleOrderAdvanceHours: undefined,
    isPlatformCancelPaidOrder: undefined,
    isPlatformCancelDeliverOrder: undefined,
    isShopCancelDeliverOrder: undefined,
    adminShoppingUsePoint: 0,
    adminShoppingUseBalance: 0,
    autoConfirmReceiptDays: undefined
});

const showPlatformProxyShoppingSetting = computed(() => {
    return getAdminType() === "admin";
});
const showReviewWarning = computed(() => {
    if (loading.value) return false;
    return !Number(formState.value.afterSalesReview) && !Number(formState.value.financialReview);
});
const afterSalesReviewTip = computed(() => {
    if (isB2CPro()) return "";
    if (isStore()) {
        return t("开启后，客户提交的售后申请需由售后人员先行审核；关闭后，系统将自动通过售后审核环节，门店订单将跳过门店审核，请通知各门店完善收货地址。");
    }
    return t("开启后，客户提交的售后申请需由售后人员先行审核；关闭后，系统将自动通过售后审核环节，店铺订单将跳过店铺审核，请通知各店铺完善收货地址。");
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const reasonInputVisible = ref(false);
const reasonInputValue = ref("");
const reasonInputRef = ref<{ focus: () => void }>();
const reasonErrorMessages: Record<ReasonValidationError, string> = {
    required: "开启原因功能时至少保留1项原因",
    empty: "原因内容不能为空",
    tooLong: "原因内容不能超过50个字符",
    duplicate: "原因内容不能重复",
    tooMany: "原因最多添加10项"
};
const loadFilter = async () => {
    try {
        const result = await getConfigOrder();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        formState.value.afterSalesReasonEnabled = Number(result.afterSalesReasonEnabled || 0);
        formState.value.afterSalesReasonList = Array.isArray(result.afterSalesReasonList) ? result.afterSalesReasonList : [];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
watch(
    () => formState.value.afterSalesReview,
    (value) => {
        if (Number(value) === 1) return;
        formState.value.autoReturnGoods = 0;
        formState.value.autoReturnGoodsDays = undefined;
    }
);
const showReasonInput = async () => {
    reasonInputVisible.value = true;
    await nextTick();
    reasonInputRef.value?.focus();
};
const closeReasonInput = () => {
    reasonInputValue.value = "";
    reasonInputVisible.value = false;
};
const handleReasonInputBlur = () => {
    if (!reasonInputVisible.value) return;
    if (!normalizeReason(reasonInputValue.value)) {
        closeReasonInput();
        return;
    }
    confirmReasonInput();
};
const confirmReasonInput = () => {
    if (!reasonInputVisible.value) return;
    const reason = normalizeReason(reasonInputValue.value);
    if (!reason) {
        message.error(t(reasonErrorMessages.empty));
        return;
    }
    const validation = validateReasonList([...(formState.value.afterSalesReasonList || []), reason], false);
    if (!validation.valid) {
        message.error(t(reasonErrorMessages[validation.error!]));
        return;
    }
    formState.value.afterSalesReasonList = validation.reasons;
    closeReasonInput();
};
const removeReason = (index: number) => {
    formState.value.afterSalesReasonList = (formState.value.afterSalesReasonList || []).filter((_, itemIndex) => itemIndex !== index);
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    const validation = validateReasonList(formState.value.afterSalesReasonList || [], Number(formState.value.afterSalesReasonEnabled) === 1);
    if (!validation.valid) {
        message.error(t(reasonErrorMessages[validation.error!]));
        return;
    }
    formState.value.afterSalesReasonList = validation.reasons;
    confirmLoading.value = true;
    try {
        const result = await saveConfigOrder(formState.value as OrderConfig);
        message.success(t("修改成功"));
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

.extra-tooltip {
    display: flex;
    &::before {
        display: none;
    }
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

.reason-setting {
    width: 600px;
}

.reason-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin: 12px 0;
}

.reason-input {
    width: 360px;
}
</style>
