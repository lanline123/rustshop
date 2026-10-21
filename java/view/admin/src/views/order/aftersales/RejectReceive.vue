/**
 * @file 商家拒收表单
 * @description 售后退货场景下，商家拒收退回商品（售后终止且不发起退款）
 * @features
 * - 顶部风险提示：拒收后售后终止不退款，需线下协商
 * - 必填拒收理由，支持快捷理由标签
 * - 提交后调用拒收接口并回调刷新详情
 */
<template>
    <div class="reject-receive-form">
        <el-alert
            class="mb-4"
            :closable="false"
            show-icon
            type="warning"
            title="拒收后售后将终止且不发起退款，请谨慎操作。货品由买卖双方线下协商处理。"
        />
        <el-form ref="formRef" :model="formState" label-width="90px">
            <el-form-item label="售后方式：">
                <span>{{ formData.aftersalesTypeName }}</span>
            </el-form-item>
            <el-form-item label="售后状态：">
                <span>{{ formData.statusName }}</span>
            </el-form-item>
            <el-form-item
                label="拒收理由："
                prop="reply"
                :rules="[{ required: true, message: '拒收请填写理由' }]"
            >
                <TigInput
                    classType="tig-form-input"
                    v-model="formState.reply"
                    :rows="4"
                    :maxlength="200"
                    show-word-limit
                    type="textarea"
                    placeholder="请输入拒收理由，将作为拒绝理由展示给买家"
                />
                <div v-if="formData.refuseReason?.length" class="quick-reasons">
                    <el-tag v-for="item in formData.refuseReason" :key="item" type="info" class="reason-tag" @click="onChangeReason(item)">
                        {{ item }}
                    </el-tag>
                </div>
            </el-form-item>
            <el-form-item>
                <div class="btn-box">
                    <el-button type="primary" @click="onSubmit">确认拒收</el-button>
                    <el-button @click="close">取消</el-button>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, PropType } from "vue";
import { message } from "ant-design-vue";
import { FormState } from "@/types/order/aftersales";
import { rejectReceive } from "@/api/order/aftersales";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    formData: {
        type: Object as PropType<FormState>,
        default: () => ({})
    }
});

const formRef = shallowRef();
const formState = ref<{ reply: string }>({
    reply: ""
});

const onChangeReason = (reason: string) => {
    formState.value.reply = reason;
};

const close = () => {
    emit("close");
};

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        await rejectReceive({
            id: props.formData?.aftersaleId,
            reply: formState.value.reply.trim()
        });
        message.success("操作成功");
        emit("submitCallback");
    } catch (error: any) {
        if (error?.message) {
            message.error(error.message);
        }
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.reject-receive-form {
    padding: 16px 0;
}
.reject-receive-form :deep(.el-form-item__label) {
    white-space: nowrap;
}
.mb-4 {
    margin-bottom: 16px;
}
.btn-box {
    display: flex;
    justify-content: flex-end;
    width: 100%;
}
.quick-reasons {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}
.reason-tag {
    cursor: pointer;
}
</style>
