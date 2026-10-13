/**
 * @file 售后详情操作按钮组件
 * @description 根据售后状态和用户角色显示相应的操作按钮
 * @features
 * - 退货退款：同意/拒绝按钮（根据S2B2C模式和状态显示不同文案）
 * - 仅退款：同意/拒绝按钮
 * - 确认收货 / 拒收：退货待收货节点二选一
 * @remark 审核未通过（status=3）为终态，不提供任何操作入口
 */
<template>
    <div class="btn flex" v-if="type != 2">
        <!-- 退货退款按钮 -->
        <template v-if="formState.aftersaleType == 1 && canHandleReturnRefund">
            <DialogForm
                :params="{ status: 2, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button type="primary">
                    {{ returnRefundAgreeText }}
                </el-button>
            </DialogForm>
            <DialogForm
                :params="{ status: 3, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button>{{ $t('拒绝退货退款') }}</el-button>
            </DialogForm>
        </template>

        <!-- 仅退款按钮 -->
        <template v-if="formState.aftersaleType == 2 && canHandleRefund">
            <DialogForm
                :params="{ status: 2, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button type="primary">{{ $t('同意仅退款') }}</el-button>
            </DialogForm>
            <DialogForm
                :params="{ status: 3, formData: formState }"
                isDrawer
                path="order/aftersales/Handle"
                :title="$t('处理售后')"
                width="600px"
                :showClose="false"
                :showOnOk="false"
                @okCallback="emit('callback')"
            >
                <el-button>{{ $t('拒绝仅退款') }}</el-button>
            </DialogForm>
        </template>

        <!-- 确认收货按钮 -->
        <DialogForm
            v-if="formState.aftersaleType == 1 && canConfirmReceipt"
            :params="{ formData: formState }"
            isDrawer
            path="order/aftersales/ConfirmReceive"
            :title="$t('确认收货')"
            width="600px"
            @okCallback="emit('callback')"
        >
            <el-button type="primary">{{ $t('确认收货') }}</el-button>
        </DialogForm>

        <!--
            拒收按钮：与「确认收货」同节点二选一
            可见性复用 canConfirmReceipt（status=5 商家 / status=22 供应商）
        -->
        <DialogForm
            v-if="formState.aftersaleType == 1 && canConfirmReceipt"
            :params="{ formData: formState }"
            isDrawer
            path="order/aftersales/RejectReceive"
            :title="$t('拒收商品')"
            width="600px"
            :showClose="false"
            :showOnOk="false"
            @okCallback="emit('callback')"
        >
            <el-button>{{ $t('拒收') }}</el-button>
        </DialogForm>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, toRef } from "vue";
import { DialogForm } from "@/components/dialog";
import { isS2b2c } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = ref(localStorage.getItem("adminType"));
const emit = defineEmits(["callback", "confirmReceipt"]);

const props = defineProps({
    formState: {
        type: Object,
        default: {}
    },
    type: {
        type: Number,
        default: 0
    }
});

const formState = toRef(props, "formState");
const isS2b2cMode = computed(() => isS2b2c());
const isVendor = computed(() => adminType.value === "vendor");

const canHandleReturnRefund = computed(() => {
    const status = formState.value.status;
    if (isS2b2cMode.value) {
        return (status === 1 && !isVendor.value) || (status === 21 && isVendor.value);
    }
    return status === 1;
});

const canHandleRefund = computed(() => {
    const status = formState.value.status;
    return status === 1 || (isS2b2cMode.value && status === 21);
});

const canConfirmReceipt = computed(() => {
    const status = formState.value.status;
    if (isS2b2cMode.value && formState.value.vendorId) {
        return (status === 5 && !isVendor.value) || (status === 22 && isVendor.value);
    }
    return status === 5;
});

const returnRefundAgreeText = computed(() => {
    if (!isS2b2cMode.value) {
        return t('同意并发送退货地址');
    }
    return isVendor.value ? t('同意并发送退货地址') : t('同意退货退款');
});
</script>

<style lang="less" scoped>
.btn {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
}
</style>