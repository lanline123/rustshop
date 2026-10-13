<template>
    <div class="form-container">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item :label="$t('回款流水单号')" prop="settlementSn">
                <div class="field-warp">
                    <TigInput v-model="form.settlementSn" :placeholder="$t('请输入回款流水单号')" clearable />
                    <div class="help-text">{{ $t('注：汇款单流水号，建议填写网银中回款单上的流水号。') }}</div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('结算时间')" prop="settlementTime">
                <el-date-picker
                    v-model="form.settlementTime"
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    format="YYYY-MM-DD HH:mm:ss"
                    :placeholder="$t('请选择结算时间')"
                    style="width: 240px"
                />
            </el-form-item>

            <el-form-item :label="$t('备注')">
                <el-input
                    v-model="form.remark"
                    type="textarea"
                    :rows="4"
                    maxlength="200"
                    show-word-limit
                    :placeholder="$t('请输入备注')"
                />
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watchEffect } from "vue";
import dayjs from "dayjs";
import { message } from "ant-design-vue";
import { submitCreditSettlement } from "@/api/termPay/creditSettlement";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    userId?: number;
    orderIds?: number[];
    isDialog?: boolean;
    visible?: boolean;
    confirmLoading?: boolean;
}>();

const emit = defineEmits(["submitCallback", "callback", "okType", "update:confirmLoading"]);

const formRef = ref<any>();

const form = reactive({
    settlementSn: "",
    settlementTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
    remark: ""
});

const rules = {
    settlementSn: [{ required: true, message: t('请输入回款流水单号'), trigger: "blur" }],
    settlementTime: [{ required: true, message: t('请选择结算时间'), trigger: "change" }]
};

const userId = computed(() => props.userId || 0);
const orderIds = computed(() => props.orderIds || []);
const remark = computed(() => (form.remark || "").trim() || undefined);

const canSubmit = computed(() => {
    return !!form.settlementSn && !!form.settlementTime && userId.value > 0 && orderIds.value.length > 0;
});

watchEffect(() => {
    emit("okType", canSubmit.value);
});

const onFormSubmit = async () => {
    if (!orderIds.value.length) {
        message.warning(t('缺少订单ID'));
        return;
    }
    if (!userId.value) {
        message.warning(t('缺少会员ID'));
        return;
    }
    if (!form.settlementSn) {
        message.warning(t('请输入回款流水单号'));
        return;
    }
    if (!form.settlementTime) {
        message.warning(t('请选择结算时间'));
        return;
    }

    emit("update:confirmLoading", true);
    try {
        const settlementTime = form.settlementTime;
        await submitCreditSettlement({
            userId: userId.value,
            settlementSn: form.settlementSn,
            settlementTime,
            remark: remark.value,
            orderIds: orderIds.value
        });
        emit(
            "submitCallback",
            {
                userId: userId.value,
                orderIds: orderIds.value,
                settlementSn: form.settlementSn,
                settlementTime,
                remark: remark.value
            },
            true
        );
        emit("callback", { type: "settlementSuccess", userId: userId.value, orderIds: orderIds.value });
    }  catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.form-container {
    padding: 12px 16px 4px 16px;
}

.field-warp {
    width: 100%;
}

.help-text {
    margin-top: 6px;
    color: #999;
    font-size: 12px;
    line-height: 18px;
}
</style>
