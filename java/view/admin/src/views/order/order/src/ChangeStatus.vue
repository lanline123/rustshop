/**
 * @file 订单状态变更组件
 * @description 用于取消订单或取消发货的表单组件
 * @features
 * - 支持取消订单（待付款/备货状态）
 * - 支持取消发货（已发货状态，会清空物流信息）
 * - 可添加操作备注
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="mb20">
                    <el-alert v-if="preOrderStatus == 3" type="warning" :description="$t('提示：该订单已完成支付为备货/待发货状态，取消后该订单自动变为取消状态；取消订单后，该订单已付款金额或使用的余额，可通过订单->售后管理->售后申请处理')" show-icon :closable="false" />
                    <el-alert v-if="preOrderStatus == 1" type="warning" :description="$t('提示：已发货订单状态修改为取消发货状态，已填写的物流信息将会清空 ，请谨慎操作！')" show-icon :closable="false" />
                </div>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('备注')">
                        <TigInput
                            width="100%"
                            v-model="formState.adminNote"
                            :rows="4"
                            :placeholder="$t('请输入备注')"
                            type="textarea"
                            show-word-limit
                            :maxlength="200"
                        />
                    </el-form-item>
                    <el-form-item v-if="false" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { changeOrderStatus } from "@/api/order/order";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    preOrderStatus: {
        // 3 取消订单  1取消发货
        type: Number,
        default: 3
    }
});
console.log(props.preOrderStatus);
const loading = ref<boolean>(false);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.act);
const id = ref<number>(props.id);
const formRef = shallowRef();
const formState = ref<any>({
    field: "orderStatus",
    val: props.preOrderStatus
});

onMounted(() => {});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await changeOrderStatus({ id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.lyecs-form-table{
  :deep(.el-alert--warning.is-light .el-alert__description){
    line-height: 20px;
    color: #333;
  }
  :deep(.el-icon){
    font-size: 20px !important;
  }
}
</style>