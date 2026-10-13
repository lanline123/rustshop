/**
 * @file 入库/出库详情
 * @description 查看入库或出库单详细信息
 * @features
 * - 展示入库/出库类型和时间
 * - 展示商品列表和数量
 * - 显示备注信息
 */
<template>
    <div ref="containerRef" class="form-container">
        <FormCard :title="$t('基本信息')">
            <div class="fromBox">
                <div class="formItem">
                    <span class="label">{{ props.inOrOut === 1 ? $t('入库类型') : $t('出库类型') }}</span>
                    <span class="value">{{ typeLabel }}</span>
                </div>
<!--                <div class="formItem">-->
<!--                    <span class="label">供应商</span>-->
<!--                    <span class="value">{{ formState.addBy || '&#45;&#45;' }}</span>-->
<!--                </div>-->
                <div class="formItem">
                    <span class="label">{{ props.inOrOut === 1 ? $t('入库时间') : $t('出库时间') }}</span>
                    <span class="value">{{ formState.addTime || '--' }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ props.inOrOut === 1 ? $t('入库数量') : $t('出库数量') }}</span>
                    <span class="value">{{ quantitys }}</span>
                </div>
                <div class="formItem">
                    <span class="label">{{ $t('备注') }}</span>
                    <span class="value">{{ formState.comment || '--' }}</span>
                </div>
            </div>
        </FormCard>
        <FormCard :title="props.inOrOut === 1 ? $t('入库商品') : $t('出库商品')">
            <SelectInventory v-if="!loading" v-model:productList="formState.productList" :inOrOut="props.inOrOut" :act="props.act" :apiList="getInventoryList"  v-model:inventoryFilterParams="inventoryFilterParams"></SelectInventory>
        </FormCard>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, computed, shallowRef, onMounted } from "vue";
import FormCard from '@/components/form/src/FormCard.vue'
import { FormState } from '@/types/product/goodsArePutIntoStorage'
import SelectInventory from "@/components/select/src/SelectInventory.vue";
import { getInventoryList } from '@/api/product/inventoryQueryList'
import { getRecordDetail } from '@/api/product/goodsArePutIntoStorage'
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
const formRef = shallowRef()
import { message } from 'ant-design-vue';

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
    }
})

const inventoryFilterParams = ref({
    page: 1,
    size: (config as any).get("pageSize")
})

const loading = ref(false)
const formState = ref<FormState>({
    type: props.inOrOut === 1 ? 11 : 21,
    productList: []
})
const list = [
    {
        label: t('退货入库'),
        value: 12,
        inOrOut: 1
    },
    {
        label: t('其他入库'),
        value: 10,
        inOrOut: 1
    },
    {
        label: t('调拨入库'),
        value: 13,
        inOrOut: 1
    },
    {
        label: t('过期出库'),
        value: 21,
        inOrOut: 2
    },
    {
        label: t('订单出库'),
        value: 22,
        inOrOut: 2
    },
    {
        label: t('其他出库'),
        value: 23,
        inOrOut: 2
    },
    {
        label: t('调拨出库'),
        value: 24,
        inOrOut: 2
    },
];

const typeLabel = computed(() => {
    return list.find(item => item.value === formState.value.type)?.label || '--'
})

const quantitys = computed(() => {
    return formState.value.productList?.reduce((pre, cur) => {
        return pre + cur.quantity
    }, 0) || '--'
})

const getDetail = async ()=>{
    loading.value = true
    try {
        formState.value = await getRecordDetail({id: props.id})
    } catch (error) {
        message.error(error.message)
    } finally {
        loading.value = false
    }
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
            min-width: 56px;
            color: #9598a6;
            flex-shrink: 0;
        }
        .value{
            margin-left: 20px;
            color: #1f2026;
        }
    }
}
</style>
