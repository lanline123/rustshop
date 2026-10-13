<template>
    <div ref="containerRef" class="form-container">
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="110px" name="form_in_modal">
            <FormCard :title="$t('基本信息')">
                <el-form-item :label="$t('出库组织')" prop="outboundOrganizationId" :rules="[{ required: true, message: '请选择出库组织' }]">
                    <el-select
                        @change="changeOutboundOrganizationId"
                        filterable
                        :disabled="getAdminType() !== 'admin'"
                        v-model="formState.outboundOrganizationId"
                        style="width: 200px"
                    >
                        <el-option v-for="item in orgList" :key="item.shopId" :label="item.shopName" :value="item.shopId"> </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item :label="$t('入库组织')" prop="warehousingOrganizationId" :rules="[{ required: true, message: '请选择入库组织' }]">
                    <el-select @change="changeWarehousingOrganizationId" filterable v-model="formState.warehousingOrganizationId" style="width: 200px">
                        <el-option v-for="item in orgList" :key="item.shopId" :label="item.shopName" :value="item.shopId"> </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item :label="$t('出库备注')">
                    <TigInput
                        v-model="formState.comment"
                        :rows="2"
                        :placeholder="$t('请输入出库备注')"
                        type="textarea"
                        classType="tig-form-input"
                        show-word-limit
                        :maxlength="200"
                    />
                </el-form-item>
            </FormCard>
            <FormCard :title="$t('商品信息')">
                <SelectInventory
                    v-if="!loading"
                    v-model:productList="formState.productStockAllocateRecordDetailList"
                    importTitle="导入商品"
                    :apiList="getAllocateProductStockPage"
                    v-model:inventoryFilterParams="inventoryFilterParams"
                    :templateFileApi="downloadAllocateTemplate"
                    :importSubmitApi="batchImportAllocate"
                    :importErrorData="getAllocateProductErrorData"
                ></SelectInventory>
            </FormCard>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, watch, shallowRef, onMounted, provide, computed } from "vue";
import FormCard from "@/components/form/src/FormCard.vue";
import { FormState } from "@/types/product/inventoryTransfer";
import SelectInventory from "@/components/select/src/SelectInventory.vue";
import {
    createAllocate,
    getOrgList,
    getAllocateProductStockPage,
    downloadAllocateTemplate,
    batchImportAllocate,
    getAllocateProductErrorData
} from "@/api/product/inventoryTransfer";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { useInventoryStore } from "@/store/inventory";
import { getAdminType } from "@/utils/storage";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
const inventoryStore = useInventoryStore();
const formRef = shallowRef();
import { message } from "ant-design-vue";
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    act: {
        type: String,
        default: "add"
    }
});

const inventoryFilterParams = ref<any>({
    page: 1,
    size: (config as any).get("pageSize")
});
// 切换出库组织
const changeOutboundOrganizationId = async (value: number) => {
    inventoryFilterParams.value.shopId = value;
    inventoryStore.setOutboundOrganizationId(value);
    // 清除选择的商品
    formState.value.productStockAllocateRecordDetailList = [];
};

const changeWarehousingOrganizationId = async (value: number) => {
    inventoryFilterParams.value.targetShopId = value;
    inventoryStore.setWarehousingOrganizationId(value);
};

const loading = ref(false);
const formState = ref<FormState>({
    productStockAllocateRecordDetailList: []
});
const userInfo: any = computed(() => useUserStore().userInfo);
const shopId = computed(() => {
    return Number(localStorage.getItem("shopId")) || userInfo.value.shopId;
});
watch(
    userInfo,
    async () => {
        formState.value.outboundOrganizationId = shopId.value;
        inventoryFilterParams.value.shopId = shopId.value;
    },
    { immediate: true, deep: true }
);

const orgList = ref<any>([]);
const handleOrgList = async () => {
    const data = await getOrgList({});
    orgList.value = data || [];
};

onMounted(() => {
    handleOrgList();
    useInventoryStore()?.setOutboundOrganizationId(shopId.value);
    if (props.act === "detail") {
        // getDetail();
    }
});

const onFormSubmit = async () => {
    await formRef.value.validate();
    const productStockAllocateRecordDetailList = formState.value.productStockAllocateRecordDetailList.map((item: any) => {
        return {
            productId: item.productId,
            skuId: item.productSkuId,
            quantity: item.quantity
        };
    });
    try {
        emit("update:confirmLoading", true);
        const result = await createAllocate({ ...formState.value, productStockAllocateRecordDetailList });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less"></style>
