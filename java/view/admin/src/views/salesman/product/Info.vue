<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="isJoin" :label="$t('推广：')">
                        <el-radio-group v-model="formState.isJoin">
                            <el-radio :value="1">{{ $t('参与') }}</el-radio>
                            <el-radio :value="0">{{ $t('不参与') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item prop="commissionType" :label="$t('佣金计算：')" v-if="formState.isJoin === 1">
                        <el-radio-group v-model="formState.commissionType" @change="changeCommissionType">
                            <el-radio style="width: 100%" :value="1">{{ $t('默认佣金比例') }}</el-radio>
                            <el-radio style="width: 100%" :value="2">{{ $t('自定义佣金比例') }}</el-radio>
                            <el-radio style="width: 100%" :value="3">{{ $t('自定义佣金金额') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-table :data="formState.commissionData" style="width: 100%" v-if="formState.isJoin === 1">
                        <template v-for="(item, index) in tableConfig">
                            <el-table-column v-if="item.children && item.children.length" :label="item.label" :prop="item.label" align="center">
                                <template v-for="(child, index2) in item.children">
                                    <el-table-column min-width="130" align="center" :label="child.label" :prop="child.prop" v-if="saleType == 2 && child.prop == 'downSalesmanRate'">
                                        <template #default="{ row, $index }">
                                            <el-form-item :prop="child.prop">
                                                <TigInput
                                                    type="decimal"
                                                    v-model="row.levelArr[index][child.prop]"
                                                    style="width: 90px"
                                                    :min="0"
                                                    :disabled="formState.commissionType === 1"
                                                >
                                                    <template v-if="formState.commissionType < 3" #append>%</template>
                                                    <template v-else #prepend>{{ config.config.dollarSign }}</template>
                                                </TigInput>
                                                <div v-if="row.levelArr[index][child.prop] == ''" style="position: absolute; bottom: -30px; color: red"> {{ $t('请输入') }} </div>
                                            </el-form-item>
                                        </template>
                                    </el-table-column>
                                    <el-table-column min-width="130" align="center" :label="child.label" :prop="child.prop" v-if="saleType <= 2 && child.prop == 'rate'">
                                        <template #default="{ row, $index }">
                                            <el-form-item :prop="child.prop">
                                                <TigInput
                                                    type="decimal"
                                                    v-model="row.levelArr[index][child.prop]"
                                                    style="width: 90px"
                                                    :min="0"
                                                    :disabled="formState.commissionType === 1"
                                                >
                                                    <template v-if="formState.commissionType < 3" #append>%</template>
                                                    <template v-else #prepend>{{ config.config.dollarSign }}</template>
                                                </TigInput>
                                                <div v-if="row.levelArr[index][child.prop] == ''" style="position: absolute; bottom: -30px; color: red"> {{ $t('请输入') }} </div>
                                            </el-form-item>
                                        </template>
                                    </el-table-column>
                                </template>
                            </el-table-column>
                            <el-table-column v-else :label="item.label" :prop="item.prop" align="center"></el-table-column>
                        </template>
                    </el-table>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive } from "vue";
import RadioType from "@/components/radio/src/RadioType.vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { productFormState } from "@/types/salesman/product";
import { getSalesmanProduct, updateSalesmanProduct } from "@/api/salesman/product";
import { getSalesmanConfig } from "@/api/salesman/promoteMode";
import type { FormInstance, FormRules } from "element-plus";
import { useConfigStore } from "@/store/config";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    salesmanProduct: {
        type: Object,
        default: null
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});
// const detailTableData = ref<any>({});
const tableConfig = ref<any>([]);
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const salesmanProduct = ref<any>(props.isDialog ? props.salesmanProduct : query.salesmanProduct);
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<productFormState>({
    isJoin: 0,
    commissionType: 1,
    commissionData: []
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchProduct();
    } else {
        _getSalesmanConfig()
        loading.value = false;
    }
});
const fetchProduct = async () => {
    try {
        const result = await getSalesmanProduct(action.value, {
            id: id.value
        });
        Object.assign(formState.value, result);
        // if(formState.value.commissionData && formState.value.commissionData.length > 0){
        //     detailTableData.value = formState.value.commissionData;
        // }
        _getSalesmanConfig()
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const saleType = ref<number>(1);
const salesmanConfig = ref<any[]>([]);
const isDefault = ref<boolean>(false);
const changeCommissionType = (item: any) => {
    if(item === 1){
        isDefault.value = true;
        _getSalesmanConfig()
    }else{
        isDefault.value = false;
        // formState.value.commissionData = detailTableData.value;
    }
}
const _getSalesmanConfig = async () => {
    try {
        const result = await getSalesmanConfig();
        if(result.saleType &&  result.saleType != null){
            saleType.value = result.saleType;
        }
        formatTable(result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const formatTable = (data: any) => {
    let saleType = data.saleType || 1;
    let level = data.level;
    let levelTit: any[] = [];
    let levelArr: any[] = [];
    level.forEach((item: any) => {
        let tit = {
            label: item.name,
            prop: "",
            children: [
                {
                    label: t('商品佣金'),
                    prop: "rate"
                },
                {
                    label: t('邀请佣金'),
                    prop: "downSalesmanRate"
                }
            ]
        };
        levelTit.push(tit);
        if (formState.value.commissionData && formState.value.commissionData.length > 0 && !isDefault.value) {
            return;
        }
        let obj = {
            level: item.id,
            downSalesmanRate: saleType == 2 ? item.downSalesmanRate : null,
            rate: item.rate
        };
        levelArr.push(obj);
    });
    if (!formState.value.commissionData || formState.value.commissionData.length <= 0 || isDefault.value) {
        formState.value.commissionData = [{ levelArr: levelArr }];
    }
    tableConfig.value = levelTit;
};
const validateCommissionData = () => {
    let tableData = formState.value.commissionData;
    let flag = true;
    tableData.forEach((item: any) => {
        item.levelArr.forEach((child: any) => {
            if (item.rate == "" || item.downSalesmanRate == "") {
                flag = false;
            }
        });
    });
    if (!flag) {
        message.error(t('请填写完整的佣金比例'));
    }
    return flag;
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    if (!validateCommissionData()) {
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updateSalesmanProduct(operation, { productId: id.value, ...formState.value });
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
<style lang="less" scoped>
.container {
    :deep(.el-input-group__append, .el-input-group__prepend) {
        padding: 0 10px;
    }
    :deep(.el-input-group__prepend) {
        padding: 0 10px;
    }
}
</style>
