/** * @file 入库/出库表单 * @description 商品入库或出库操作表单 * @features * - 选择入库/出库类型 * - 选择入库/出库商品及数量 * - 支持导入入库/出库单 * -
添加备注说明 */
<template>
    <div ref="containerRef" class="form-container">
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="110px" name="form_in_modal">
            <FormCard :title="$t('基本信息')">
                <el-form-item :label="props.inOrOut === 1 ? $t('入库类型') : $t('出库类型')">
                    <el-radio-group v-model="formState.type" @change="onTypeChange">
                        <el-radio v-for="item in typeList" :label="item.value">{{ item.label }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item :label="$t('备注')">
                    <TigInput
                        v-model="formState.comment"
                        :rows="2"
                        :placeholder="$t('请输入备注')"
                        type="textarea"
                        show-word-limit
                        :maxlength="200"
                        classType="tig-form-input"
                    />
                </el-form-item>
            </FormCard>
            <FormCard :title="props.inOrOut === 1 ? $t('入库商品') : $t('出库商品')">
                <SelectInventory
                    v-if="!loading"
                    v-model:productList="formState.productStockRecordDetailList"
                    :inOrOut="props.inOrOut"
                    :importTitle="props.inOrOut === 1 ? '导入入库单' : '导入出库单'"
                    :apiList="getInventoryList"
                    v-model:inventoryFilterParams="inventoryFilterParams"
                ></SelectInventory>
            </FormCard>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, shallowRef, onMounted, nextTick } from "vue";
import FormCard from "@/components/form/src/FormCard.vue";
import { FormState } from "@/types/product/goodsArePutIntoStorage";
import SelectInventory from "@/components/select/src/SelectInventory.vue";
import { getInventoryList } from "@/api/product/inventoryQueryList";
import { productStockIn } from "@/api/product/goodsArePutIntoStorage";
import { productStockOut } from "@/api/product/goodsArePutOutOfStorage";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
const formRef = shallowRef();
import { message } from "ant-design-vue";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    inOrOut: {
        type: Number,
        default: 1
    },
    act: {
        type: String,
        default: "add"
    }
});

const inventoryFilterParams = ref({
    page: 1,
    size: (config as any).get("pageSize"),
    productType: 1
});

const loading = ref(false);
const formState = ref<FormState>({
    type: props.inOrOut === 1 ? 11 : 21,
    productStockRecordDetailList: []
});
const list = [
    {
        label: t('采购入库'),
        value: 11,
        inOrOut: 1
    },
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
    }
];

const typeList = computed(() => {
    return list.filter((item) => item.inOrOut === props.inOrOut);
});

// 选择 radio 后立即让当前元素失焦：避免扫码枪输入被 radio 内部 input 吃掉，同时去掉 focus 加粗样式
const onTypeChange = async () => {
    await nextTick();
    (document.activeElement as HTMLElement | null)?.blur?.();
};

const onFormSubmit = async () => {
    await formRef.value.validate();
    const productStockRecordDetailList = formState.value.productStockRecordDetailList.map((item) => {
        return {
            productId: item.productId,
            skuId: item.productSkuId,
            quantity: item.quantity
        };
    });
    try {
        emit("update:confirmLoading", true);
        const apiFn = props.inOrOut === 1 ? productStockIn : productStockOut;
        const result = await apiFn({ ...formState.value, productStockRecordDetailList });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const getDetail = () => {};

onMounted(() => {
    if (props.act === "detail") {
        getDetail();
    }
});

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less"></style>
