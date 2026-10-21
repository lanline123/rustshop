/**
 * @file 修改订单商品
 * @description 订单商品编辑表单，支持增删改商品
 * @features
 * - 修改商品价格、数量、属性
 * - 添加新商品（使用商品基础售价，不计算促销）
 * - 删除订单商品
 * - 已付款或已发货订单无法修改
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-table :data="formState.items" style="width: 100%">
                        <el-table-column type="selection" width="50" />
                        <el-table-column :label="$t('商品名称')" prop="productId" width="320">
                            <template #default="{ row }">
                                <ProductCard :picThumb="row.picThumb" :productId="row.productId" :productName="row.productName" :productType="row.productType" :url="row.url">
                                </ProductCard>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('货号')" prop="productSn" />
                        <el-table-column :label="$t('价格')" prop="price">
                            <template #default="{ row }">
                                <TigInput type="decimal" v-model="row.price"></TigInput>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('数量')" prop="quantity" width="110">
                            <template #default="{ row }">
                                <el-input-number class="input-number" controls-position="right" v-model="row.quantity" type="number" :min="1" />
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('属性')" prop="productAttr">
                            <template #default="{ row }">
                                <TigInput :rows="1" v-model="row.productAttr" type="textarea"></TigInput>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('小计')" prop="subtotal">
                            <template #default="{ row }">
                                {{ priceFormat(row.quantity * row.price) }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('操作')" prop="name">
                            <template #default="scope">
                                <a @click="onDelRecord(scope.$index,)">{{ $t('删除') }}</a>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div class="operating-style">
                        <div class="operating-left">
                            <div class="left">
                                <el-button disabled>{{ $t('拆分订单') }}</el-button>
                                <DialogForm isDrawer @okCallback="onOk" :title="$t('选择商品')" width="600px" path="product/product/src/SelectProduct" :params="{ isMultiple: true }">
                                    <el-button>{{ $t('添加商品') }}</el-button>
                                </DialogForm>
                            </div>
                            <div> {{ $t('提示：手动添加商品时，商品价格为商品基础售价，不会判断促销、满减等活动；已付款或发货的订单无法修改、添加商品。') }} </div>
                        </div>
                        <div class="operating-right">{{ $t('合计：{priceFormat}', { priceFormat: priceFormat(total) }) }}</div>
                    </div>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, computed } from "vue"
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { OrderFormState } from '@/types/order/order.d';
import { imageFormat, priceFormat } from "@/utils/format";
import { Image } from "@/components/image";
import { DeleteRecord, ProductCard } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { getOrder, getAddProductInfoByIds, modifyOrderProduct } from "@/api/order/order";

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
const operation = action.value === 'add' ? 'insert' : 'update';
const formRef = shallowRef();
const formState = ref<OrderFormState>(props.form);

onMounted(() => {
    // 获取详情数据
    fetchOrder();
});
const fetchOrder = async () => {
    try {
        loading.value = true;
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

const total = computed(() => {
    let total = 0;
    formState.value.items.forEach((element:any) => {
        total += element.quantity * element.price;
    });
    return total
})

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit('update:confirmLoading', true);
        const result = await modifyOrderProduct({ id: id.value, items: formState.value.items });
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
const onOk = async (ids: number[]) => {
    try {
        const result = await getAddProductInfoByIds({ ids });
        console.log(result);
        result.forEach(element => {
            element.quantity = 1;
            element.itemId = 0;
            formState.value.items.push(element)
        });
    } catch (error) {
    } finally {
        emit('update:confirmLoading', false);
    }
}
const onDelRecord = (index:number) => {
    formState.value.items.splice(index, 1);
}
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.operating-style {
    margin-top: 20px;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;

    .operating-left {
        flex: 1;
        display: flex;
        flex-direction: column;
        color: #999999;
        gap: 16px;

        .left {
            display: flex;
            flex-direction: row;
            gap: 8px;
        }
    }

    .operating-right {
        width: 300px;
        font-weight: 500;
        text-align: right;
    }
}

.input-number {
    width: 85px;
}

.lyecs-form-table{
    :deep(.el-input-number.is-controls-right .el-input__wrapper){
        padding-left: 5px !important;
    }

}
</style>
