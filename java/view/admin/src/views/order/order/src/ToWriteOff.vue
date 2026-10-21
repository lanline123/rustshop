/**
 * @file 订单核销确认组件
 * @description 自提订单确认收货/核销
 * @features
 * - 展示待核销订单商品信息
 * - 确认收货完成核销流程
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-table :data="formState.items" style="width: 100%">
                        <el-table-column :label="$t('商品名称')" prop="productId">
                            <template #default="{ row }">
                                <ProductCard :picThumb="row.picThumb" :productId="row.productId"
                                    :productName="row.productName" :productType="row.productType" :skuData="row.skuData"
                                    :url="row.url"></ProductCard>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('附加规格')" prop="extraSkuData" :width="200">
                            <template #default="{ row }">
                                <div v-if="row.extraSkuData && row.extraSkuData.length > 0" class="extraskudata"
                                    v-for="item in row.extraSkuData" :key="item">
                                    {{ item.attrName + item.attrValue + priceFormat(item.totalAttrPrice) }}
                                </div>
                                <div v-else>{{ $t('无附加规格') }}</div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('数量')" prop="quantity" :width="100">
                            <template #default="{ row }">
                                <div>x{{ row.quantity }}</div>
                            </template>
                        </el-table-column>
                    </el-table>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message, Modal } from "ant-design-vue";
import type { OrderToShipFormState } from "@/types/order/order.d";
import { getOrder, operationOrder, orderDeliver } from "@/api/order/order";//
import { ProductCard } from "@/components/list";
import { priceFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: { type: Number, default: 0 },
    act: { type: String, default: "" },
    isDialog: Boolean,
});
const pickupSnInputRef = ref();
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<OrderToShipFormState>({
    items: [],
    shippingMethod: 1,
    shippingTypeId: 1
});
onMounted(() => {
    // 获取详情数据
    fetchOrder();
});
const fetchOrder = async () => {
    try {
        loading.value = true;
        const result = await getOrder(action.value, { id: id.value });
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const pickupSn = ref("");
// 表单通过验证后提交
const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        const result = await operationOrder("confirmReceipt", { id: id.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
        pickupSn.value = "";
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
.button-card {
    margin-top: 12px;
    margin-bottom: 12px;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    /* 允许换行 */
    gap: 8px;
    line-height: 24px;
}

.babyBlue {
    background-color: rgba(61, 127, 255, 0.06);
    border-radius: 9px;
}

.pd15 {
    padding: 15px;
}

.input-number {
    width: 85px;
}
</style>
