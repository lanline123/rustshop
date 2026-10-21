/**
 * @file 修改订单金额
 * @description 订单金额调整表单，用于修改折扣优惠
 * @features
 * - 展示订单金额明细（商品/优惠券/积分/余额/配送费）
 * - 支持调整折扣优惠金额（平摊到商品）
 * - 未付款订单支持变更订单号
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('商品总金额')" prop="productAmount">
                        {{ priceFormat(formState.productAmount) }}
                    </el-form-item>
                    <el-form-item :label="$t('优惠券')" prop="couponAmount">
                        {{ priceFormat(formState.couponAmount) }}
                    </el-form-item>
                    <el-form-item :label="$t('积分抵扣')" prop="pointsAmount">
                        {{ priceFormat(formState.pointsAmount) }}
                    </el-form-item>
                    <el-form-item :label="$t('使用余额')" prop="balance">
                        {{ priceFormat(formState.balance) }}
                    </el-form-item>
                    <el-form-item :label="$t('配送费用')" prop="shippingFee">
                        <!-- <TigInput classType="tig-form-input" v-model="formState.shippingFee" type="decimal" /> -->
                         {{ priceFormat(formState.shippingFee) }}
                    </el-form-item>
                    <!-- <el-form-item label="发票税额" prop="invoiceFee">
                        <TigInput classType="tig-form-input" v-model="formState.invoiceFee" type="decimal" />
                    </el-form-item>
                    <el-form-item label="服务费" prop="serviceFee">
                        <TigInput classType="tig-form-input" v-model="formState.serviceFee" type="decimal" />
                    </el-form-item> -->
                    <el-form-item :label="$t('折扣优惠')" prop="discountAmount">
                        <TigInput classType="tig-form-input" v-model="formState.discountAmount" type="decimal" >
                            <template #prefix>
                               <span>-</span>
                            </template>
                        </TigInput>
                         <div class="extra">{{ $t('折扣优惠将平摊到商品中') }}</div>
                    </el-form-item>
                    <el-form-item v-if="isUnpaid" :label="$t('订单号')" prop="updateOrderSn">
                        <Checkbox v-model.modelValue="formState.updateOrderSn">
                            <template #default> {{ $t('变更订单号') }} </template>
                        </Checkbox>
                    </el-form-item>
                    <el-form-item :label="$t('订单总金额')" prop="totalAmount">
                        {{ priceFormat(formState.totalAmount) }}
                    </el-form-item>
                    <el-form-item :label="$t('已付款金额')" prop="paidAmount">
                        {{ priceFormat(formState.paidAmount) }}
                    </el-form-item>
                    <el-form-item :label="$t('未付款金额')" prop="unpaidAmount">
                        {{ priceFormat(formState.unpaidAmount) }}
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px"/>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue"
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { OrderFormState } from '@/types/order/order.d';
import { getOrder, modifyOrderMoney } from "@/api/order/order";
import { priceFormat } from "@/utils/format";
import { Checkbox } from "@/components/radio";

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
        default: ''
    },
    form: {
        type: Object as () => OrderFormState,
        default: () => ({})
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<OrderFormState>(props.form);

// 判断订单是否未支付（payStatus !== 2 表示未付款）
const isUnpaid = computed(() => {
    return (formState.value as any)?.payStatus !== 2;
});

onMounted(() => {
    // 获取详情数据
    fetchOrder();
});
const fetchOrder = async () => {
    try {
        const result = await getOrder(action.value, { id: id.value });
        Object.assign(
            formState.value,
            result
        )
    } catch (error:any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await modifyOrderMoney({ id: id.value, ...formState.value });
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error:any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
