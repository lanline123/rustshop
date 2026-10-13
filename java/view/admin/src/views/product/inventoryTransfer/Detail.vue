<template>
    <div ref="containerRef" class="form-container">
        <div class="header" v-if="props.act === 'detail'">
            <div class="status">
                <span v-if="[1, 2, 3].includes(formState.status as number)" class="iconfont-h5 icon-shijian" style="color: rgb(250, 173, 20)"></span>
                <span v-else-if="formState.status === 4" class="iconfont-h5 icon-dagou" style="color: rgb(82, 196, 26)"></span>
                <span v-else-if="[5, 6].includes(formState.status as number)" class="iconfont-h5 icon-jiaochacross80" style="color: rgb(255, 0, 0)"></span>
                {{ getStatusName(formState)}}
            </div>
        </div>
        <div class="header" v-else>
            <div class="status">
                <span v-if="[1, 2, 3].includes(formState.status as number)" class="iconfont-h5 icon-shijian" style="color: rgb(250, 173, 20)"></span>
                <span v-else-if="formState.status === 4" class="iconfont-h5 icon-dagou" style="color: rgb(82, 196, 26)"></span>
                <span v-else-if="[5, 6].includes(formState.status as number)" class="iconfont-h5 icon-jiaochacross80" style="color: rgb(255, 0, 0)"></span>
                {{ getStatusName(formState)}}
            </div>
            <div v-if="[1, 2, 5].includes(formState.status as number)">
                <el-popconfirm :title="$t('您确认要作废吗？')"
                               @confirm="onCancelAllocate">
                    <template #reference>
                        <el-button>{{ $t('作废') }}</el-button>
                    </template>
                </el-popconfirm>
            </div>
            <DialogForm v-if="[1].includes(formState.status as number)  && +shopId === rowDetail.warehousingOrganizationId"
                        :maskClose="false"
                        :isDrawer="false"
                        :params="{
                            id: props.id,
                            auditType: 5,
                            disabled: true
                        }" path="product/inventoryTransfer/src/auditDialog" :title="$t('库存调拨驳回')" width="500px"
                        @okCallback="loadFilter">
                <el-button>{{ $t('审核驳回') }}</el-button>
            </DialogForm>
            <el-popconfirm v-if="[1].includes(formState.status as number)  && +shopId === rowDetail.warehousingOrganizationId" :title="$t('您确认要审核通过吗？')"
                           @confirm="handleauditAllocate(2, '')">
                <template #reference>
                    <el-button type="primary">{{ $t('审核通过') }}</el-button>
                </template>
            </el-popconfirm>
<!--            <DialogForm v-if="[1].includes(formState.status as number)  && +shopId === rowDetail.warehousingOrganizationId"-->
<!--                        :maskClose="false"-->
<!--                        :params="{-->
<!--                            id: props.id,-->
<!--                            auditType: 2,-->
<!--                            disabled: true-->
<!--                        }" isDrawer path="product/inventoryTransfer/src/auditDialog" title="库存调拨审核" width="650px"-->
<!--                        @okCallback="loadFilter">-->
<!--                <el-button type="primary">审核通过</el-button>-->
<!--            </DialogForm>-->
        </div>
        <FormCard :title="$t('基本信息')">
            <div class="fromBox">
                <div class="formItem">
                    <span class="label">{{ $t('出库门店') }}</span>
                    <span class="value">{{ formState.outboundOrganizationName || '--' }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ $t('入库门店') }}</span>
                    <span class="value">{{ formState.warehousingOrganizationName || '--' }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ $t('出库备注') }}</span>
                    <span class="value">{{ formState.comment || '--' }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ $t('调拨出库备注') }}</span>
                    <span class="value">{{ formState.stockOutComment || '--' }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ $t('调拨入库备注') }}</span>
                    <span class="value">{{ formState.stockInComment || '--' }}</span>
                </div>
            </div>
        </FormCard>
        <FormCard :title="$t('调拨商品')">
            <SelectInventory v-if="!loading" v-model:productList="formState.productAllocateDetailVOList" :act="[1].includes(formState.status as number) ? 'detail' : props.act" :import-title="'导入商品'" :apiList="getInventoryList"  v-model:inventoryFilterParams="inventoryFilterParams">
                <template #table="{ searchList }">
                    <div class="tableHeader">
                        <div><span class="label">{{ $t('调拨数量：') }}</span> <span class="value">{{ quantitys }}</span></div>
                        <div><span class="label">{{ $t('已出库数量：') }}</span> <span class="value">{{ stockOutQuantity }}</span></div>
                        <div><span class="label">{{ $t('已入库数量：') }}</span> <span class="value">{{ stockInQuantity }}</span></div>
                    </div>
                    <el-table :data="searchList" style="width: 100%;margin-top: 20px;">
                        <el-table-column :label="$t('商品名称')" prop="productName" min-width="250">
                            <template #default="{ row }">
                                <div class="productNameBox">
                                    <div v-if="row.productPicThumb" class="span-pic" style="margin-right: 8px;">
                                        <img :src="imageFormat(row.productPicThumb)" height="40" width="40" />
                                    </div>
                                    <div><span class="span-text">{{ row.productName }}</span></div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('商品规格')" prop="skuName" :formatter="(row: any) => row.skuName || '--'"></el-table-column>
<!--                        <el-table-column label="库存单位" prop="productUnit" :formatter="(row: any) => row.productUnit || '&#45;&#45;'"></el-table-column>-->
                        <el-table-column :label="$t('调拨数量')" prop="quantity"></el-table-column>
                        <el-table-column :label="$t('出库数量')" prop="stockOutQuantity"></el-table-column>
                        <el-table-column :label="$t('入库数量')" prop="stockInQuantity"></el-table-column>
                    </el-table>
                </template>
            </SelectInventory>
        </FormCard>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, computed, shallowRef, onMounted, h } from "vue";
import FormCard from '@/components/form/src/FormCard.vue'
import { FormDetailState } from '@/types/product/inventoryTransfer'
import SelectInventory from "@/components/select/src/SelectInventory.vue";
import { getInventoryList } from '@/api/product/inventoryQueryList'
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
import { message } from 'ant-design-vue';
import { cancelAllocate, productStockAllocateRecordDetail, auditAllocate } from "@/api/product/inventoryTransfer";
import { imageFormat } from "@/utils/format";
import { DialogForm } from "@/components/dialog";
import { useUserStore } from "@/store/user";
import { ElInput, ElMessageBox } from "element-plus";

const shopId: any = localStorage.getItem("shopId")
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
    }
})

const getStatusName = (row: any) => {
    switch (row.status) {
        case 1:
            return t('待审核');
        case 2:
            return t('待调出');
        case 3:
            return t('待调入');
        case 4:
            return t('已完成');
        case 5:
            return t('审核驳回');
        case 6:
            return t('已作废');
    }
};

const inventoryFilterParams = ref({
    page: 1,
    size: (config as any).get("pageSize")
})

const loading = ref(false)
const formState = ref<FormDetailState>({
    productAllocateDetailVOList: []
})


const quantitys = computed(() => {
    return formState.value.productAllocateDetailVOList?.reduce((pre, cur) => {
        return pre + cur.quantity
    }, 0)
})

// 调拨出库数量
const stockOutQuantity = computed(() => {
    return formState.value.productAllocateDetailVOList?.reduce((pre, cur) => {
        return pre + cur.stockOutQuantity
    }, 0)
})

const stockInQuantity = computed(() => {
    return formState.value.productAllocateDetailVOList?.reduce((pre, cur) => {
        return pre + cur.stockInQuantity
    }, 0)
})

const getDetail = async ()=>{
    loading.value = true
    try {
        const data = await productStockAllocateRecordDetail({id: props.id})
        formState.value = {
            ...props.rowDetail,
            ...data,
        }
        console.log(formState.value, 999);
    } catch (error: any) {
        message.error(error.message)
    } finally {
        loading.value = false
    }
}

const onCancelAllocate = async () => {
    try {
        await cancelAllocate([props.id]);
        message.success(t('操作成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('操作失败'));
    }
};
// 审核
const handleauditAllocate = async (auditType: number, remark: string = '') => {
    try {
        emit("update:confirmLoading", true);
        const result = await auditAllocate({ auditType, id: props.id, remark });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

// 审核通过
const loadFilter = async () => {
    emit("submitCallback");
}

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
    .formItem{
        font-size: 14px;
        display: flex;
        line-height: 20px;
        .label{
            display: inline-block;
            min-width: 85px;
            color: #9598a6;
            flex-shrink: 0;
            text-align: right;
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
}

.iconfont-h5{
    font-size: 14px;
}
.icon-shijian{
    font-size: 16px;
}
</style>
