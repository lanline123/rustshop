/**
 * @file 售后列表操作栏组件
 * @description 售后列表行内「处理动作」，与表头入口（售后详情/订单详情/标记）分离
 * @features
 * - 退货退款：同意并发送退货地址 / 拒绝（aftersaleType==1 且待处理）
 * - 仅退款：同意仅退款 / 拒绝仅退款（aftersaleType==2 且待处理）
 * - 确认收货 / 拒收：退货待收货节点（status=5 商家 / status=22 供应商）二选一
 * - 无可处理动作时展示占位符「-」
 * - 可见性判定与详情抽屉 InfoBtn.vue 完全一致（S2B2C + adminType）
 * @remark 内联动作复用 Handle/ConfirmReceive/RejectReceive，依赖列表接口返回
 *         suggestRefundAmount / refundAmount / refuseReason 等字段（见后端对接文档）
 */
<template>
    <div v-if="hasAnyAction" class="list-btn-wrapper">
        <!-- 退货退款：同意 / 拒绝 -->
        <template v-if="aftersaleType === 1 && canHandleReturnRefund">
            <DialogForm
                :params="{ status: 2, formData: item }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text type="danger">{{ returnRefundAgreeText }}</el-button>
            </DialogForm>
            <DialogForm
                :params="{ status: 3, formData: item }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text>{{ $t('拒绝退货退款') }}</el-button>
            </DialogForm>
        </template>

        <!-- 仅退款：同意 / 拒绝 -->
        <template v-if="aftersaleType === 2 && canHandleRefund">
            <DialogForm
                :params="{ status: 2, formData: item }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text type="danger">{{ $t('同意仅退款') }}</el-button>
            </DialogForm>
            <DialogForm
                :params="{ status: 3, formData: item }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text>{{ $t('拒绝仅退款') }}</el-button>
            </DialogForm>
        </template>

        <!-- 确认收货 / 拒收（同节点二选一） -->
        <template v-if="aftersaleType === 1 && canConfirmReceipt">
            <DialogForm
                :params="{ formData: item }"
                isDrawer
                path="order/aftersales/ConfirmReceive"
                :title="$t('确认收货')"
                width="600px"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text type="danger">{{ $t('确认收货') }}</el-button>
            </DialogForm>
            <DialogForm
                :params="{ formData: item }"
                isDrawer
                path="order/aftersales/RejectReceive"
                :title="$t('拒收商品')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button size="small" text>{{ $t('拒收') }}</el-button>
            </DialogForm>
        </template>
    </div>
    <span v-else class="list-btn-placeholder">-</span>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { isS2b2c } from "@/utils/version";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const adminType = ref(localStorage.getItem("adminType"));
const emit = defineEmits(["callback"]);

const props = defineProps({
    item: {
        type: Object,
        default: () => ({})
    }
});

const isS2b2cMode = computed(() => isS2b2c());
const isVendor = computed(() => adminType.value === "vendor");
const aftersaleType = computed(() => Number(props.item.aftersaleType));
const status = computed(() => Number(props.item.status));

/** 是否可处理退货退款（同意/拒绝） */
const canHandleReturnRefund = computed(() => {
    if (isS2b2cMode.value) {
        return (status.value === 1 && !isVendor.value) || (status.value === 21 && isVendor.value);
    }
    return status.value === 1;
});

/** 是否可处理仅退款（同意/拒绝） */
const canHandleRefund = computed(() => {
    return status.value === 1 || (isS2b2cMode.value && status.value === 21);
});

/** 是否可确认收货 / 拒收 */
const canConfirmReceipt = computed(() => {
    if (isS2b2cMode.value && props.item.vendorId) {
        return (status.value === 5 && !isVendor.value) || (status.value === 22 && isVendor.value);
    }
    return status.value === 5;
});

/** 退货退款「同意」按钮文案 */
const returnRefundAgreeText = computed(() => {
    if (!isS2b2cMode.value) {
        return t("同意并发送退货地址");
    }
    return isVendor.value ? t("同意并发送退货地址") : t("同意退货退款");
});

/** 是否有任何可处理动作（无则渲染占位符 -） */
const hasAnyAction = computed(() => {
    if (aftersaleType.value === 1 && (canHandleReturnRefund.value || canConfirmReceipt.value)) {
        return true;
    }
    if (aftersaleType.value === 2 && canHandleRefund.value) {
        return true;
    }
    return false;
});
</script>

<style lang="less" scoped>
.list-btn-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 4px 12px;
    width: 100%;
}
.list-btn-placeholder {
    display: block;
    width: 100%;
    text-align: center;
    color: #999;
}

/* 操作栏文字按钮去掉 hover / focus 背景高亮 */
.list-btn-wrapper {
    :deep(.el-button.is-text:not(.is-disabled)) {
        &:hover,
        &:focus {
            background-color: transparent;
        }
    }
}
</style>
