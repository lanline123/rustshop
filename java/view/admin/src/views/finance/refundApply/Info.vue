/** * @file 退款申请详情/处理页 * @description 处理售后退款申请，设置最终退款金额和处理状态 * @features * - 显示订单商品信息和退款明细 * -
设置实际退款金额（支持按商品分配） * - 选择退款方式（原路返回/余额/线下） * - 设置处理状态（未处理/已处理/取消） * - 填写处理备注 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="notice-warp">
                    <p>
                        {{
                            $t(
                                "最终退款金额可根据实际需要填写，该金额会展示在会员用户的订单详情页面中（如果退款类型为商品，订单会统计所有该订单下商品的最终退款总额）；"
                            )
                        }}
                    </p>
                    <p>{{ $t("处理状态为未处理或已处理时，佣金等相关功能不会计算最终退款金额；") }}</p>
                    <p>{{ $t("处理状态为取消时，佣金等相关功能会忽略该退款；") }}</p>
                    <p>{{ $t("积分、优惠券等退还与本操作无关，取消订单或商品退货操作确认后会自动退还！") }}</p>
                </div>
                <el-table :data="formState.items" style="width: 100%; margin-bottom: 20px" border>
                    <el-table-column :label="$t('商品信息')" width="400">
                        <template #default="{ row }">
                            <div class="flex">
                                <ProductCard :picThumb="row.picThumb" :productId="row.productId" :productName="row.productName"> </ProductCard>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('商品价格')" align="center">
                        <template #default="{ row }">
                            <div>{{ priceFormat(row.price) }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="quantity" :label="$t('商品数量')" align="center" />
                    <el-table-column prop="number" :label="$t('退货数量')" align="center" />
                </el-table>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('售后编号')" prop="orderSn">
                        <DialogForm
                            :params="{ act: 'detail', id: formState.aftersales?.aftersaleId }"
                            isDrawer
                            path="order/aftersales/Info"
                            :title="$t('查看售后信息')"
                            width="700px"
                            :showClose="false"
                            :showOnOk="false"
                        >
                            <a class="btn-link">{{ formState.aftersales?.aftersalesSn }}</a>
                        </DialogForm>
                    </el-form-item>
                    <el-form-item :label="$t('订单编号')" prop="orderSn">
                        <DialogForm
                            :params="{ act: 'detail', id: formState.orderId }"
                            isDrawer
                            :showClose="false"
                            :showOnOk="false"
                            path="order/order/Info"
                            :title="$t('查看订单信息')"
                            width="880px"
                        >
                            <a class="btn-link">{{ formState.orderSn }}</a>
                        </DialogForm>
                    </el-form-item>
                    <el-form-item :label="$t('订单金额')" prop="aftersales" v-if="formState.aftersales">
                        <OrderMoney style="width: 100%" v-model="formState.aftersales.orders"></OrderMoney>
                    </el-form-item>
                    <el-form-item :label="$t('余额退款金额')" prop="refundBalance">
                        <TigInput
                            width="100%"
                            type="decimal"
                            v-model="formState.refundBalance"
                            @input="checkMax('refundBalance', formState.refundAmount)"
                            :disabled="formState.refundStatus != 0 || formState.isOffline == 1"
                        />
                        <div class="extra">
                            {{ $t("请根据实际情况填写金额，最多可操作总金额{priceFormat}，", { priceFormat: priceFormat(formState.refundAmount) })
                            }}<a class="btn-link" href="#" @click.prevent="fillMax('refundBalance', formState.refundAmount)">{{ $t("点击填入") }}</a>
                            {{ $t("最大退款金额") }}
                        </div>
                    </el-form-item>
                    <el-form-item
                        :label="$t('线上退款金额')"
                        prop="onlineBalance"
                        v-if="Number(formState.onlineBalance || 0) > 0 || Number(formState.effectiveOnlineBalance || 0) > 0"
                    >
                        <TigInput
                            width="100%"
                            type="decimal"
                            v-model="formState.onlineBalance"
                            @input="checkMax('onlineBalance', formState.effectiveOnlineBalance)"
                            :disabled="formState.refundStatus != 0 || formState.isOffline == 1"
                        />
                        <div class="extra">
                            {{ $t("请根据实际情况填写金额，最多可操作金额{priceFormat}，", { priceFormat: priceFormat(formState.effectiveOnlineBalance) })
                            }}<a class="btn-link" href="#" @click.prevent="fillMax('onlineBalance', formState.effectiveOnlineBalance)">{{ $t("点击填入") }}</a>
                            {{ $t("最大退款金额") }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('线下退款金额')" prop="offlineBalance">
                        <TigInput
                            width="100%"
                            type="decimal"
                            v-model="formState.offlineBalance"
                            @input="checkMax('offlineBalance', formState.refundAmount)"
                            :disabled="formState.refundStatus != 0 || formState.isOffline == 1"
                        />
                        <div class="extra">
                            {{ $t("请根据实际情况填写金额，最多可操作总金额{priceFormat}，", { priceFormat: priceFormat(formState.refundAmount) })
                            }}<a class="btn-link" href="#" @click.prevent="fillMax('offlineBalance', formState.refundAmount)">{{ $t("点击填入") }}</a>
                            {{ $t("最大退款金额") }}
                        </div>
                    </el-form-item>
                    <el-form-item v-if="formState.isCreditOrder === 1" :label="$t('账期退款金额')" prop="creditRefundAmount">
                        <TigInput
                            width="100%"
                            type="decimal"
                            v-model="formState.creditRefundAmountOrgin"
                            :max="formState.creditRefundAmount"
                            @input="checkMax('creditRefundAmountOrgin', formState.creditRefundAmount)"
                        />
                        <div class="extra">
                            {{ $t("请根据实际情况填写金额, 最多可操作总金额{priceFormat}", { priceFormat: priceFormat(formState.creditRefundAmount) }) }}
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('上传附件')" prop="paymentVoucher">
                        <FormAddGallery v-if="!loading" v-model:photos="paymentVoucher" :isMultiple="true" :disabled="isVoucherDisabled"> </FormAddGallery>
                        <div class="extra">{{ $t("您可以通过拖拽来调整相册图片顺序，第一张图将作为商品主图展示") }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('本次操作备注')" prop="refundNote">
                        <TigInput
                            width="100%"
                            v-model="formState.refundNote"
                            :rows="4"
                            type="textarea"
                            :disabled="formState.refundStatus != 0 || formState.isOffline == 1"
                        />
                    </el-form-item>
                    <el-form-item>
                        <div class="btn-box">
                            <el-button
                                v-if="showAgreeRefundBtn"
                                ref="submitBtn"
                                class="form-submit-btn"
                                type="primary"
                                :loading="submitLoading"
                                :disabled="submitLoading"
                                @click="onSubmit(1)"
                                >{{ onlineRefundPolling ? $t("处理中...") : $t("同意退款") }}</el-button
                            >
                            <el-button
                                v-if="Number(formState.refundStatus) === 0"
                                class="form-submit-btn"
                                type="danger"
                                :loading="submitLoading"
                                :disabled="submitLoading"
                                @click="onSubmit(4)"
                                >{{ $t("拒绝退款") }}</el-button
                            >
                            <el-button
                                v-if="showOfflineAuditBtn"
                                ref="submitBtn"
                                class="form-submit-btn"
                                type="primary"
                                :loading="offlineLoading"
                                :disabled="offlineLoading"
                                @click="offlineAudit()"
                                >{{ $t("确认已线下转账") }}</el-button
                            >
                        </div>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";

import { RefundApplyFormState } from "@/types/finance/refundApply.d";
import { getRefundApply, updateRefundApply, updateOfflineAudit } from "@/api/finance/refundApply";
import { DialogForm } from "@/components/dialog";
import { OrderMoney } from "@/components/order";
import { ProductCard } from "@/components/list";
import { priceFormat } from "@/utils/format";
import { FormAddGallery } from "@/components/gallery";
import { buildRefundRejectPayload, isRefundReadOnlyStatus, validateRefundRejectReason } from "./refundReject";
// 父组件回调
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const paymentVoucher = ref<any[]>([]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const submitLoading = ref<boolean>(false);
const offlineLoading = ref<boolean>(false);
const onlineRefundPolling = ref<boolean>(false);
const onlinePollingTimer = ref<ReturnType<typeof setTimeout> | null>(null);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<RefundApplyFormState>({
    effectiveBalance: 0,
    refundBalance: "",
    effectiveOnlineBalance: 0,
    onlineBalance: "",
    offlineBalance: "",
    refundAmount: 0,
    paidAmount: 0,
    onlinePaidAmount: 0,
    creditRefundAmount: 0
});
const hasOnlineRefundPart = computed(() => Number(formState.value.onlineBalance || 0) > 0);
const isOnlineProcessing = computed(() => {
    if (onlineRefundPolling.value) return true;
    if (!hasOnlineRefundPart.value) return false;
    return Number(formState.value.isOnline) !== 2;
});
const showAgreeRefundBtn = computed(() => {
    // 已处理、已取消或已拒绝的申请只读展示
    if (isRefundReadOnlyStatus(formState.value.refundStatus)) return false;
    if (isOnlineProcessing.value) return true;
    return Number(formState.value.refundStatus) === 0 && Number(formState.value.isOffline) !== 1;
});
const showOfflineAuditBtn = computed(() => Number(formState.value.isOffline) === 1 && !isOnlineProcessing.value);
const isVoucherDisabled = computed(() => !showOfflineAuditBtn.value && Number(formState.value.refundStatus) !== 0);
type FormStateKey = keyof typeof formState.value;

const normalizeAmountEmpty = (key: FormStateKey) => {
    const v = formState.value[key] as any;
    if (v === null || v === undefined || v === "") return;
    if (Number(v) === 0) formState.value[key] = "" as any;
};

const checkMax = (value: FormStateKey, max: any) => {
    if (Number(formState.value[value]) > Number(max)) {
        message.error(t("退款金额不能超过最多可操作金额"));
    }
};
const fillMax = (value: FormStateKey, max: any) => {
    if (formState.value.refundStatus != 0 || formState.value.isOffline == 1) return;
    formState.value[value] = Number(max).toFixed(2) as any;
    // 填入当前项时，其他两项金额置为 0
    if (value === "refundBalance") {
        formState.value.onlineBalance = "" as any;
        formState.value.offlineBalance = "" as any;
    } else if (value === "onlineBalance") {
        formState.value.refundBalance = "" as any;
        formState.value.offlineBalance = "" as any;
    } else if (value === "offlineBalance") {
        formState.value.refundBalance = "" as any;
        formState.value.onlineBalance = "" as any;
    }
};
const normalizePaymentVoucher = (voucher?: string[] | string | null) => {
    if (!voucher) return [];
    const list = Array.isArray(voucher) ? voucher : String(voucher).split(",");
    return list
        .map((item) => String(item || "").trim())
        .filter(Boolean)
        .map((url, index) => ({
            picId: `${index}-${url}`,
            picThumb: url,
            picUrl: url,
            picName: ""
        }));
};
const getPaymentVoucherUrls = () => paymentVoucher.value.map((item) => item?.picUrl).filter(Boolean);
const fetchRefundApply = async () => {
    try {
        const result = await getRefundApply(action.value, { id: id.value });
        Object.assign(formState.value, result);
        paymentVoucher.value = normalizePaymentVoucher(result.paymentVoucher as any);
        // 默认展示为空，不显示 0
        normalizeAmountEmpty("refundBalance");
        normalizeAmountEmpty("onlineBalance");
        normalizeAmountEmpty("offlineBalance");
        if (result.aftersales && result.aftersales.refundAmount) {
            formState.value.refundAmount = result.aftersales.refundAmount;
        }
        formState.value.creditRefundAmountOrgin = result.creditRefundAmount;
        if (Number(result.isOnline) === 1) {
            startOnlineRefundPolling();
        } else if (onlineRefundPolling.value) {
            stopOnlineRefundPolling();
        }
        if(Number(formState.value.refundStatus) == 1 || Number(formState.value.refundStatus) == 0){
          formState.value.refundNote = ""
        }

    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const stopOnlineRefundPolling = () => {
    if (onlinePollingTimer.value) {
        clearTimeout(onlinePollingTimer.value);
        onlinePollingTimer.value = null;
    }
    onlineRefundPolling.value = false;
    submitLoading.value = false;
    emit("update:confirmLoading", false);
};

const pollOnlineRefundStatus = async () => {
    if (!onlineRefundPolling.value) return;
    try {
        const result = await getRefundApply("detail", { id: id.value });
        Object.assign(formState.value, result);
        paymentVoucher.value = normalizePaymentVoucher(result.paymentVoucher as any);
        normalizeAmountEmpty("refundBalance");
        normalizeAmountEmpty("onlineBalance");
        normalizeAmountEmpty("offlineBalance");
        if (result.aftersales && result.aftersales.refundAmount) {
            formState.value.refundAmount = result.aftersales.refundAmount;
        }
        formState.value.creditRefundAmountOrgin = result.creditRefundAmount;
        if (Number(result.isOnline) === 2) {
            message.success(t("退款成功"));
            stopOnlineRefundPolling();
            emit("submitCallback", result);
            emit("close");
            return;
        }
        if (Number(result.isOnline) !== 1) {
            stopOnlineRefundPolling();
            return;
        }
        onlinePollingTimer.value = setTimeout(() => {
            pollOnlineRefundStatus();
        }, 2000);
    } catch (error: any) {
        message.error(error.message || t("获取退款状态失败"));
        stopOnlineRefundPolling();
    }
};

const startOnlineRefundPolling = () => {
    if (onlineRefundPolling.value) return;
    onlineRefundPolling.value = true;
    submitLoading.value = true;
    emit("update:confirmLoading", true);
    pollOnlineRefundStatus();
};

const offlineAudit = async () => {
    if (offlineLoading.value) return; // 防抖
    offlineLoading.value = true;
    try {
        const result = await updateOfflineAudit({
            refundId: id.value,
            refundNote: formState.value.refundNote,
            paymentVoucher: getPaymentVoucherUrls().join(",")
        });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        offlineLoading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    fetchRefundApply();
});

onBeforeUnmount(() => {
    stopOnlineRefundPolling();
});

// 表单通过验证后提交
const onSubmit = async (status: number) => {
    if (submitLoading.value) return; // 防抖
    //refundStatus 1同意 2拒绝
    await formRef.value.validate();
    if (status === 4) {
        const reasonError = validateRefundRejectReason(formState.value.refundNote || "");
        if (reasonError === "required") {
            message.error(t("拒绝原因不能为空"));
            return;
        }
        if (reasonError === "tooLong") {
            message.error(t("拒绝原因不能超过255个字符"));
            return;
        }
    }
    if (status == 1) {
        if (Number(formState.value.refundBalance) > Number(formState.value.refundAmount)) {
            //余额
            message.error(t("退款余额不能大于最多可操作金额"));
            return;
        }
        if (Number(formState.value.onlineBalance) > Number(formState.value.onlinePaidAmount)) {
            //线上
            message.error(t("线上退款金额不能大于最多可操作金额"));
            return;
        }
        if (Number(formState.value.offlineBalance) > Number(formState.value.refundAmount)) {
            //线下
            message.error(t("线下退款金额不能大于实收金额"));
            return;
        }
        let total =
            Number(formState.value.refundBalance) +
            Number(formState.value.onlineBalance) +
            Number(formState.value.offlineBalance) +
            Number(formState.value.creditRefundAmount);
        if (total != (formState.value.refundAmount || formState.value.creditRefundAmount)) {
            message.error(t("总退款金额必须等于最多可操作金额"));
            return;
        }
    }
    const imgList = getPaymentVoucherUrls();
    submitLoading.value = true;
    try {
        emit("update:confirmLoading", true);
        const obj =
            status === 4
                ? buildRefundRejectPayload(id.value, formState.value.refundNote || "")
                : {
                      refundId: id.value,
                      refundNote: formState.value.refundNote,
                      onlineBalance: Number(formState.value.onlineBalance || 0),
                      offlineBalance: Number(formState.value.offlineBalance || 0),
                      refundBalance: Number(formState.value.refundBalance || 0),
                      creditRefundAmount: formState.value.creditRefundAmountOrgin,
                      paymentVoucher: imgList.join(","),
                      refundStatus: status
                  };
        const result = await updateRefundApply(obj);
        if (status === 1 && Number(formState.value.onlineBalance || 0) > 0) {
            message.success(t("已提交在线退款申请，处理中..."));
            startOnlineRefundPolling();
            return;
        }
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        if (!onlineRefundPolling.value) {
            submitLoading.value = false;
            emit("update:confirmLoading", false);
        }
    }
};
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
.btn-box {
    width: 100%;
    text-align: right;
}
</style>
