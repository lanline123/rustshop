<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('店铺状态')" prop="status">
                        <div class="tig-radio">
                            <el-radio-group v-model="formState.dateType">
                                <div class="flex mb20">
                                    <el-radio :value="1"></el-radio>
                                    <div class="flex">
                                        <p class="mr10">{{ $t('订单收货后') }}</p>
                                        <div class="check-box">
                                            <TigInput type="integer" v-model="formState.useDay" class="mr10" width="100px" :disabled="formState.dateType == 0">
                                            </TigInput>
                                        </div>
                                        <p class="mr10">{{ $t('天结算') }}</p>
                                    </div>
                                </div>
                                <div class="flex mb20">
                                    <el-radio :value="0"></el-radio>
                                    <p class="mr10">{{ $t('确认收货后立即不可售后') }}</p>
                                </div>
                            </el-radio-group>
                        </div>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { ShopSettlementFormState } from "@/types/shop/shop.d";
import { getShopSettlement, updateShopSettlement } from "@/api/shop/shop";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const loading = ref<boolean>(false);
const query = useRouter().currentRoute.value.query;
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<ShopSettlementFormState>({
    dateType: 0
});
const fetchShopInfo = async () => {
    try {
        const result = await getShopSettlement({ shopId: id.value, code: "orderConfig" });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    fetchShopInfo();
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateShopSettlement({ shopId: id.value, ...formState.value, code: "orderConfig" });
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
<style scoped lang="less">
.tig-radio {
    :deep(.el-radio-group) {
        display: block;
        font-size: 12px;
    }
    :deep(.el-radio) {
        margin-right: 0px;
    }
    .check-box {
        cursor: pointer;
        :deep(.el-input-group__append) {
            background-color: #fff !important;
            padding: 0;
            box-shadow: none;
        }
    }
}
</style>
