<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('国家/地区')" prop="regionId" :rules="[{ required: true, message: '请选择国家/地区' }]">
                        <div style="width: 300px">
                            <SelectRegion v-if="!loading" v-model:selectedIds="selectedRegionIds" :check-strictly="true" @change="onRegionChanged" />
                        </div>
                    </el-form-item>
                    <el-form-item
                        :label="$t('税率(%)')"
                        prop="rate"
                        :rules="[
                            { required: true, message: '请输入税率' },
                            { validator: validateRate, trigger: 'blur' }
                        ]"
                    >
                        <TigInput width="300px" v-model="formState.rate" type="decimal" :decimalPlaces="2" :fixedDecimal="false">
                            <template #append>%</template>
                        </TigInput>
                    </el-form-item>
                    <el-form-item :label="$t('是否生效')" prop="isActive">
                        <el-radio-group v-model="formState.isActive">
                            <el-radio :value="1">{{ $t('启用') }}</el-radio>
                            <el-radio :value="0">{{ $t('停用') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('备注')" prop="remark">
                        <TigInput width="300px" v-model="formState.remark" type="textarea" :row="4" />
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
import { SelectRegion } from "@/components/select";
import { useRegionStore } from "@/store/region";
import { getTaxRate, updateTaxRate } from "@/api/setting/taxRate";
import type { TaxRateFormState } from "@/types/setting/taxRate";

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
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const selectedRegionIds = ref<number[]>([]);
const regionStore: any = useRegionStore();

const formState = ref<TaxRateFormState>({
    rate: "",
    isActive: 1,
    remark: ""
});

const getRegionPathById = (regionId?: number): number[] => {
    if (!regionId || !Array.isArray(regionStore.allRegion)) {
        return [];
    }
    const dfs = (nodes: any[], path: number[]): number[] | null => {
        for (const node of nodes) {
            const cur = [...path, node.regionId];
            if (node.regionId === regionId) {
                return cur;
            }
            if (Array.isArray(node.children) && node.children.length > 0) {
                const found = dfs(node.children, cur);
                if (found) return found;
            }
        }
        return null;
    };
    return dfs(regionStore.allRegion, []) || [];
};

const getRegionNameById = (regionId?: number) => {
    if (!regionId || !Array.isArray(regionStore.allRegion)) {
        return "";
    }
    const stack = [...regionStore.allRegion];
    while (stack.length > 0) {
        const node = stack.shift();
        if (!node) {
            continue;
        }
        if (node.regionId === regionId) {
            return node.regionName || "";
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
            stack.push(...node.children);
        }
    }
    return "";
};

const syncSelectedRegion = (regionIds: number[]) => {
    if (!Array.isArray(regionIds) || regionIds.length === 0) {
        selectedRegionIds.value = [];
        formState.value.regionId = undefined;
        formState.value.regionName = "";
        return;
    }
    selectedRegionIds.value = regionIds;
    const leafId = regionIds[regionIds.length - 1];
    formState.value.regionId = leafId;
    formState.value.regionName = getRegionNameById(leafId);
};

const onRegionChanged = () => {
    syncSelectedRegion(selectedRegionIds.value);
};

const validateRate = (_rule: any, value: string | number, callback: (error?: Error) => void) => {
    if (value === "" || value === undefined || value === null) {
        callback(new Error(t('请输入税率')));
        return;
    }
    const rate = Number(value);
    if (Number.isNaN(rate)) {
        callback(new Error(t('税率格式不正确')));
        return;
    }
    if (rate < 0 || rate > 100) {
        callback(new Error(t('税率需在0到100之间')));
        return;
    }
    callback();
};

const fetchTaxRate = async () => {
    try {
        const result: any = await getTaxRate(action.value, { id: id.value, taxRateId: id.value });
        const data = result?.item || result;
        Object.assign(formState.value, data);
        if (data?.regionId) {
            const path = getRegionPathById(data.regionId);
            selectedRegionIds.value = path.length > 0 ? path : [data.regionId];
            formState.value.regionId = data.regionId;
            if (!formState.value.regionName) {
                formState.value.regionName = getRegionNameById(data.regionId);
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    await formRef.value.validate();
    if (!formState.value.regionId || selectedRegionIds.value.length === 0) {
        message.warning(t('请选择地区'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const payload = {
            id: id.value,
            taxRateId: id.value || formState.value.taxRateId,
            ...formState.value,
            rate: Number(formState.value.rate)
        };
        const result = await updateTaxRate(operation, payload);
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

onMounted(() => {
    if (action.value === "detail") {
        fetchTaxRate();
    } else {
        loading.value = false;
    }
});

defineExpose({ onFormSubmit });
</script>
