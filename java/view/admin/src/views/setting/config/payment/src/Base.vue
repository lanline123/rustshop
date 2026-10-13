<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="160px">
            <div class="content">
                <div class="payment-setting-layout">
                    <div class="payment-setting-left">
                        <div class="title">{{ $t("基础支付设置") }}</div>
                        <el-form-item :label="$t('余额支付')" prop="useSurplus">
                            <el-radio-group v-model="formState.useSurplus" class="itemWidth">
                                <el-radio :value="1">{{ $t("支持") }}</el-radio>
                                <el-radio :value="0">{{ $t("不支持") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("订单结算页面是否支持余额支付功能，不支持则不显示") }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('积分抵扣')" prop="usePoints">
                            <el-radio-group v-model="formState.usePoints" class="itemWidth">
                                <el-radio :value="1">{{ $t("支持") }}</el-radio>
                                <el-radio :value="0">{{ $t("不支持") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("订单结算页面是否支持积分抵扣功能，不支持则不显示") }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('优惠券')" prop="useCoupon">
                            <el-radio-group v-model="formState.useCoupon" class="itemWidth">
                                <el-radio :value="1">{{ $t("支持") }}</el-radio>
                                <el-radio :value="0">{{ $t("不支持") }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t("订单结算页面是否支持优惠券功能，不支持则不显示") }}</div>
                        </el-form-item>
                        <el-form-item v-if="isB2b()" :label="$t('账期支付')" prop="useCredit">
                            <el-radio-group v-model="formState.useCredit" class="itemWidth">
                                <el-radio :value="1">{{ $t("支持") }}</el-radio>
                                <el-radio :value="0">{{ $t("不支持") }}</el-radio>
                            </el-radio-group>
                            <div>
                                <div class="extra">
                                    {{ $t("若要开启此功能，需要先开启实名认证") }} <router-link to="/user/user-authentication">{{ $t("去开启>") }}</router-link>
                                </div>
                                <div class="extra">
                                    {{ $t("在用户端的订单结算页面，对已开通账期支付功能的用户，显示可使用账期支付功能") }}
                                    <el-popover :width="400" placement="right-end" trigger="click">
                                        <template #reference>
                                            <a>{{ $t("查看示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="@/style/images/useCredit.png" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('支付顺序')" prop="paymentOrder">
                            <div class="payment-order-wrap">
                                <draggable
                                    class="payment-order-list"
                                    item-key="code"
                                    :list="paymentOrderList"
                                    ghost-class="sortable-ghost"
                                    chosen-class="chosenClass"
                                    animation="300"
                                    handle=".drag-handle"
                                    @start=""
                                    @end=""
                                >
                                    <template #item="{ element, index }">
                                        <div class="payment-order-item">
                                            <span class="drag-handle" :title="$t('拖拽排序')">⋮⋮</span>
                                            <!-- <span class="payment-order-index">{{ index + 1 }}</span> -->
                                            <span class="payment-order-label">{{ element.label }}</span>
                                            <!-- <span class="payment-order-code">{{ element.code }}</span> -->
                                        </div>
                                    </template>
                                </draggable>
                                <div class="extra">{{ $t("前端展示的支付顺序，拖拽排序后点击提交按钮保存") }}</div>
                            </div>
                        </el-form-item>
                    </div>

                    <PaymentPreviewAside :paymentOrder="previewPaymentOrder" />
                </div>
            </div>
        </el-form>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, shallowRef, watch } from "vue";
import draggable from "vuedraggable";
import { message } from "ant-design-vue";
import type { BasicPaySettingsForm } from "@/types/setting/payment";
import { getBasicSettings, updateBasicSettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import PaymentPreviewAside from "./PaymentPreviewAside.vue";
import { useI18n } from "vue-i18n";
import { isB2b, isOverseas } from "@/utils/version";
const { t } = useI18n();
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<BasicPaySettingsForm>({
    useSurplus: 0,
    usePoints: 0,
    useCoupon: 0,
    useCredit: 0,
    paymentOrder: ""
});

type PaymentOrderCode =
    | "wechat"
    | "alipay"
    | "huifuWechat"
    | "huifuAlipay"
    | "lakalaWechat"
    | "lakalaAlipay"
    | "yeepayWechat"
    | "yeepayAlipay"
    | "paypal"
    | "applepay"
    ;
type PaymentOrderItem = { code: PaymentOrderCode; label: string };

const paymentOrderOptions: PaymentOrderItem[] = [
    { code: "wechat", label: t("微信支付") },
    { code: "alipay", label: t("支付宝") },
    { code: "huifuWechat", label: t("汇付-微信支付") },
    { code: "huifuAlipay", label: t("汇付-支付宝") },
    { code: "lakalaWechat", label: t("拉卡拉-微信支付") },
    { code: "lakalaAlipay", label: t("拉卡拉-支付宝") },
    { code: "yeepayWechat", label: t("易宝-微信支付") },
    { code: "yeepayAlipay", label: t("易宝-支付宝") },
    { code: "paypal", label: "PayPal" },
    { code: "applepay", label: "Apple Pay" },
];

const availablePaymentOptions = paymentOrderOptions.filter((opt) => {
    return true;
});

const paymentOrderList = ref<PaymentOrderItem[]>([...availablePaymentOptions]);

const paymentOrderString = computed(() => paymentOrderList.value.map((x) => x.code).join(","));
type PreviewPayKey = PaymentOrderCode | "yunpay";
const previewPaymentOrder = computed<PreviewPayKey[]>(() => paymentOrderList.value.map((x) => x.code) as PreviewPayKey[]);
const paymentOrderReady = ref(false);
watch(paymentOrderString, (val) => {
    if (!paymentOrderReady.value) return;
    formState.value.paymentOrder = val;
});

const normalizePaymentOrderCode = (code: string): PaymentOrderCode | undefined => {
    const normalized = String(code ?? "")
        .trim()
        .toLowerCase();
    if (!normalized) return undefined;
    if (normalized === "alipay") return "alipay";
    if (normalized === "wechat") return "wechat";
    if (normalized === "huifuwechat") return "huifuWechat";
    if (normalized === "huifualipay") return "huifuAlipay";
    if (normalized === "lakalawechat") return "lakalaWechat";
    if (normalized === "lakalaalipay") return "lakalaAlipay";
    if (normalized === "yeepaywechat") return "yeepayWechat";
    if (normalized === "yeepayalipay") return "yeepayAlipay";
    if (normalized === "paypal") return "paypal";
    if (normalized === "applepay") return "applepay";
    return undefined;
};

// 加载列表
onMounted(async () => {
    loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getBasicSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));

        // 兼容后端下发的逗号分隔字符串：解析后端顺序，未覆盖到的选项按默认顺序补在末尾
        const raw = String(formState.value.paymentOrder ?? "").trim();
        const parts = raw
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean);
        const ordered: PaymentOrderCode[] = [];
        for (const p of parts) {
            const key = normalizePaymentOrderCode(p);
            if (!key) continue;
            if (ordered.includes(key)) continue;
            ordered.push(key);
        }
        for (const opt of availablePaymentOptions) {
            if (!ordered.includes(opt.code)) ordered.push(opt.code);
        }
        const map = new Map(availablePaymentOptions.map((x) => [x.code, x] as const));
        paymentOrderList.value = ordered.map((code) => map.get(code)).filter((x): x is PaymentOrderItem => Boolean(x));

        // 初始化完成后再同步回表单字段（避免加载前 watch 抢写）
        paymentOrderReady.value = true;
        formState.value.paymentOrder = paymentOrderString.value;
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
        // 保障提交的 paymentOrder 为最新的逗号串
        formState.value.paymentOrder = paymentOrderString.value;
        const result = await updateBasicSettings(formState.value);
        message.success(t("修改成功"));
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
    min-width: 620px;
}

@media (max-width: 1400px) {
    .payment-setting-layout {
        flex-direction: column;
    }
    .payment-setting-left {
        min-width: 0;
    }
}

.payment-order-wrap {
    width: 100%;
}

.payment-order-list {
    width: 280px;
    max-width: 100%;
    border-radius: 6px;
    padding: 8px 0;
    background: #fff;
}

.payment-order-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 10px;
    margin-bottom: 8px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fafafa;
    user-select: none;

    &:last-child {
        margin-bottom: 0;
    }
}

.drag-handle {
    cursor: grab;
    padding: 0 4px;
    color: #909399;
    font-weight: 700;
}

.payment-order-index {
    width: 22px;
    text-align: center;
    color: #909399;
    font-variant-numeric: tabular-nums;
}

.payment-order-label {
    font-weight: 600;
    color: #303133;
}

.payment-order-code {
    margin-left: auto;
    color: #909399;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
    font-size: 12px;
}

:deep(.sortable-ghost) {
    opacity: 0.5;
}
</style>
