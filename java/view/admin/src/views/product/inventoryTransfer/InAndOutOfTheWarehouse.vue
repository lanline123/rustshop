<template>
    <div ref="containerRef" class="form-container">
        <el-form ref="formRef" :model="formState" label-width="100px" name="form_in_modal">
            <FormCard :title="$t('基本信息')">
                <div class="fromBox">
                    <div class="formItem">
                        <span class="label">{{ $t('出库组织') }}</span>
                        <span class="value">{{ formState.outboundOrganizationName || '--' }}</span>
                    </div>
                    <div class="formItem">
                        <span class="label">{{ $t('入库组织') }}</span>
                        <span class="value">{{ formState.warehousingOrganizationName || '--' }}</span>
                    </div>
                    <div class="formItem">
                        <span class="label">{{ $t('出库备注') }}</span>
                        <span class="value">{{ formState.comment || '--' }}</span>
                    </div>
                    <div class="formItem" v-if="inOrOut === 2">
                        <el-form-item :label="$t('调拨出库备注')" >
                            <TigInput
                                v-model="formState.stockOutComment"
                                :rows="2"
                                :placeholder="$t('请输入调拨出库备注')"
                                type="textarea"
                                show-word-limit
                                :maxlength="200"
                                classType="tig-form-input"
                            />
                        </el-form-item>
                    </div>
                    <div class="formItem" v-else>
                        <span class="label">{{ $t('调拨出库备注') }}</span>
                        <span class="value">{{ formState.stockOutComment || '--' }}</span>
                    </div>
                    <div class="formItem" v-if="inOrOut === 1">
                        <el-form-item :label="$t('调拨入库备注')" >
                            <TigInput
                                v-model="formState.stockInComment"
                                :rows="2"
                                :placeholder="$t('请输入调拨入库备注')"
                                type="textarea"
                                show-word-limit
                                :maxlength="200"
                                classType="tig-form-input"
                            />
                        </el-form-item>
                    </div>
                    <div class="formItem" v-else>
                        <span class="label">{{ $t('调拨入库备注') }}</span>
                        <span class="value">{{ formState.stockInComment || '--' }}</span>
                    </div>
                </div>
            </FormCard>
            <FormCard :title="$t('调拨商品')">
                <SelectInventory v-if="!loading" v-model:productList="formState.productAllocateDetailVOList" :act="props.act" :import-title="'导入商品'" :apiList="getInventoryList"  v-model:inventoryFilterParams="inventoryFilterParams">
                    <template #table="{ searchList }">
                        <el-table :data="searchList" style="width: 100%;margin-top: 20px;">
                            <el-table-column :label="$t('商品名称')" prop="productName" width="250">
                                <template #default="{ row }">
                                    <div class="productNameBox">
                                        <div v-if="row.productPicThumb" class="span-pic">
                                            <img :src="imageFormat(row.productPicThumb)" height="40" width="40" />
                                        </div>
                                        <div><span class="span-text">{{ row.productName }}</span></div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商品规格')" prop="skuName" :formatter="(row: any) => row.skuName || '--'"></el-table-column>
<!--                            <el-table-column label="库存单位" prop="productUnit" :formatter="(row: any) => row.productUnit || '&#45;&#45;'" width="100"></el-table-column>-->
                            <el-table-column :label="$t('调拨数量')" prop="quantity" width="100" align="center"></el-table-column>
                            <el-table-column :label="$t('出库数量')" prop="stockOutQuantity" align="center">
                                <template #default="{ row }">
                                    <el-input-number style="width: 120px" v-if="inOrOut === 2" v-model="row.stockOutQuantity" :min="0" :max="row.quantity" :step="1" />
                                    <span v-else>{{ row.stockOutQuantity }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('入库数量')" prop="stockInQuantity" v-if="inOrOut === 1" align="center">
                                <template #default="{ row }">
                                    <el-input-number style="width: 120px" v-if="inOrOut === 1" v-model="row.stockInQuantity" :min="0" :max="row.quantity" :step="1" />
                                    <span v-else>{{ row.stockInQuantity }}</span>
                                </template>
                            </el-table-column>
                        </el-table>
                    </template>
                </SelectInventory>
            </FormCard>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, computed, shallowRef, onMounted } from "vue";
import FormCard from '@/components/form/src/FormCard.vue'
import { FormDetailState } from '@/types/product/inventoryTransfer'
import SelectInventory from "@/components/select/src/SelectInventory.vue";
import { getInventoryList } from '@/api/product/inventoryQueryList'
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
import { message } from 'ant-design-vue';
import { productStockAllocateRecordDetail, stockOutAllocate, stockInAllocate } from "@/api/product/inventoryTransfer";
import { imageFormat } from "@/utils/format";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    inOrOut: {
        type: Number,
        default: 1
    },
    act: {
        type: String,
        default: 'add'
    },
    id: {
        type: Number,
        default: null
    },
    rowDetail: {
        type: Object,
        default: () => {
            return {}
        }
    },
})


const inventoryFilterParams = ref({
    page: 1,
    size: (config as any).get("pageSize"),
})

const loading = ref(false)
const formState = ref<FormDetailState>({
    productAllocateDetailVOList: []
})

const getDetail = async ()=>{
    loading.value = true
    try {
        const data = await productStockAllocateRecordDetail({id: props.id})
        formState.value = {
            ...props.rowDetail,
            ...data,
        }
    } catch (error: any) {
        message.error(error.message)
    } finally {
        loading.value = false
    }
}
const formRef = ref();

const onFormSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const apiFn = props.inOrOut === 1 ? stockInAllocate : stockOutAllocate;
        const params = {
            allocateId: props.id,
            comment: props.inOrOut === 1 ? formState.value.stockInComment : formState.value.stockOutComment,
            stockAllocateRecordDetails: formState.value.productAllocateDetailVOList
        }
        const result = await apiFn(params)
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
}
defineExpose({ onFormSubmit });


onMounted(()=>{
    getDetail();
})
</script>



<style scoped lang="less">
:deep(.form-content){
    padding: 0 10px;
}
.fromBox{
    display: flex;
    flex-direction: column;
    gap: 30px;
    box-sizing: border-box;
    margin-bottom: 20px;
    :deep(.el-form-item) {
        margin-bottom: 0;
    }
    .formItem{
        font-size: 14px;
        display: flex;
        line-height: 20px;
        .label{
            display: inline-block;
            min-width: 90px;
            color: #9598a6;
            flex-shrink: 0;
            text-align: right;
        }
        :deep(.el-form-item__label) {
            color: #9598a6;
        }
        .value{
            margin-left: 20px;
            color: #1f2026;
        }
    }
}

.header{
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;
}

.tableHeader{
    display: flex;
    align-items: center;
    gap: 20px;
    margin: 20px 0;
    background: #f5f7fa;
    padding: 20px 15px;
    .label{
        display: inline-block;
        min-width: 56px;
        color: #9598a6;
        flex-shrink: 0;
    }
    .value{
        color: #1f2026;
    }
}

.productNameBox{
    display: flex;
    align-items: center;
    .span-pic{
        margin-right: 8px;
    }
}

.iconfont-h5{
    font-size: 14px;
}
.icon-shijian{
    font-size: 16px;
}
</style>
