<template>
    <div>
        <!-- 派单抽屉（DialogForm） -->
        <DialogForm
            ref="dispatchDialogRef"
            :params="{
                staffType: currentStaffType,
                shopId: shopId,
                orderId: orderId
            }"
            :title="dialogTitle"
            path="organize/src/SelectStaff"
            width="450px"
            centered
            @okCallback="handleDispatchSuccess"
        >
            <!-- 隐藏的触发器，用于触发DialogForm显示 -->
            <slot :handle-click="handleStockUp"></slot>
        </DialogForm>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { message } from "ant-design-vue";
import { orderDeliver } from "@/api/order/order";
import { dispatchDeliveryOrder } from "@/api/organize/serviceStaff";
import { DialogForm } from "@/components/dialog";

/**
 * 带派单功能的备货组件
 * 功能：在备货时，如果是同城配送或上门服务且为手动派单模式，则使用DialogForm抽屉模式打开派单功能
 */

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    /** 订单ID */
    orderId: number;
    /** 门店ID */
    shopId?: number;
    /** 订单配送类型ID（shippingTypeId） */
    shippingTypeId?: number;
    /** 员工类型：1=配送员，2=服务员 */
    staffType?: number;
    /** 备货参数 */
    stockUpParams?: {
        id: number;
        shippingMethod: number;
        deliverData: any[];
        LogisticsId?: number;
        trackingNo?: string;
    };
}

const props = defineProps<Props>();

const emit = defineEmits<{
    success: [];
    error: [error: any];
}>();

// ===== 响应式状态 =====
const dispatchDialogRef = ref<any>(null);

// ===== 计算属性 =====
const shopId = computed(() => props.shopId);
const orderId = computed(() => props.orderId);

const currentStaffType = computed(() => props.staffType || 1);

const dialogTitle = computed(() => {
    return currentStaffType.value === 2 ? t('选择服务员') : t('选择配送员');
});

// ===== 方法 =====

/**
 * 处理备货点击
 */
const handleStockUp = async () => {
    if (dispatchDialogRef.value && dispatchDialogRef.value.show) {
        dispatchDialogRef.value.show();
    }
};

/**
 * 处理派单成功回调
 */
const handleDispatchSuccess = async (result: any) => {
    if (!result || !result.staffId) {
        return;
    }

    try {
        // 执行派单 - 使用新的手动派单接口
        await dispatchDeliveryOrder({
            orderId: props.orderId,
            staffId: result.staffId
        });

        message.success(t('派单成功'));

        // 派单成功后执行备货
        // await doStockUp();

        emit("success");
    } catch (error: any) {
        message.error(error.message || t('派单失败'));
    }
};

/**
 * 执行备货操作
 */
const doStockUp = async () => {
    if (!props.stockUpParams) {
        message.warning(t('缺少备货参数'));
        return;
    }

    try {
        await orderDeliver(props.stockUpParams);
        message.success(t('备货成功'));
        emit("success");
    } catch (error: any) {
        message.error(error.message || t('备货失败'));
        emit("error", error);
        throw error;
    }
};
</script>

<style lang="less" scoped>
// 样式可根据需要添加
</style>
