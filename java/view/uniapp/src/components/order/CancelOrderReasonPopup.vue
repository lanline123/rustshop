<template>
    <tig-popup v-model:show="show" position="bottom" :show-close="false" :round="20" :mask-click="!submitting">
        <view class="cancel-reason-popup">
            <view class="popup-title">{{ $t("请选择取消订单原因") }}</view>
            <radio-group class="reason-list" @change="onReasonChange">
                <label v-for="reason in reasons" :key="reason" class="reason-item">
                    <text>{{ reason }}</text>
                    <radio :value="reason" :checked="selectedReason === reason" color="var(--general)" />
                </label>
            </radio-group>
            <view class="popup-actions">
                <button class="action-button secondary" :disabled="submitting" @click="show = false">{{ $t("取消") }}</button>
                <button class="action-button primary" :disabled="!selectedReason || submitting" @click="submitWithReason">
                    {{ $t("确定") }}
                </button>
            </view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { cancelOrder, getOrderReasonConfig } from "@/api/user/order";
import { buildCancelOrderPayload, keepValidSelection } from "@/utils/orderReason.mjs";

const emit = defineEmits(["success"]);
const { t } = useI18n();
const show = ref(false);
const submitting = ref(false);
const orderId = ref(0);
const reasons = ref<string[]>([]);
const selectedReason = ref("");

const showError = (error: any) => {
    uni.showToast({ title: error?.message || t("操作失败"), icon: "none" });
};

const loadConfig = async () => {
    const result = await getOrderReasonConfig();
    reasons.value = Array.isArray(result.afterSalesReasonList) ? result.afterSalesReasonList : [];
    selectedReason.value = keepValidSelection(selectedReason.value, reasons.value);
    return Number(result.afterSalesReasonEnabled) === 1;
};

const cancelWithoutReason = () => {
    uni.showModal({
        title: t("提示"),
        content: t("确认要取消该订单吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (res) => {
            if (!res.confirm) return;
            try {
                await cancelOrder(buildCancelOrderPayload(orderId.value));
                uni.showToast({ title: t("取消成功"), icon: "none" });
                emit("success");
            } catch (error: any) {
                showError(error);
            }
        }
    });
};

const open = async (id: number) => {
    orderId.value = id;
    selectedReason.value = "";
    try {
        if (await loadConfig()) show.value = true;
        else cancelWithoutReason();
    } catch (error: any) {
        showError(error);
    }
};

const onReasonChange = (event: any) => {
    selectedReason.value = event.detail.value;
};

const submitWithReason = async () => {
    if (!selectedReason.value || submitting.value) return;
    submitting.value = true;
    try {
        await cancelOrder(buildCancelOrderPayload(orderId.value, selectedReason.value));
        uni.showToast({ title: t("取消成功"), icon: "none" });
        show.value = false;
        emit("success");
    } catch (error: any) {
        showError(error);
        try {
            if (!(await loadConfig())) show.value = false;
        } catch {
            // 配置刷新失败时保留弹层，由买家决定重试或关闭。
        }
    } finally {
        submitting.value = false;
    }
};

defineExpose({ open });
</script>

<style lang="scss" scoped>
.cancel-reason-popup {
    padding: 32rpx;
    background: #fff;
}
.popup-title {
    margin-bottom: 24rpx;
    font-size: 32rpx;
    font-weight: 600;
    text-align: center;
}
.reason-list {
    display: flex;
    flex-direction: column;
}
.reason-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 88rpx;
    border-bottom: 1rpx solid #eee;
    font-size: 28rpx;
}
.popup-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 32rpx;
}
.action-button {
    flex: 1;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    line-height: 80rpx;
}
.action-button::after {
    border: 0;
}
.secondary {
    color: #333;
    background: #f5f5f5;
}
.primary {
    color: #fff;
    background: var(--general);
}
.primary[disabled] {
    color: #fff;
    opacity: 0.5;
}
</style>
