<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :rules="rules" :model="form" label-width="auto">
                    <el-form-item prop="promotionTypeData" v-if="rulesType == 1">
                        <div class="flex mb10">
                            <div class="flex">
                                <el-button type="primary" :icon="Plus" link @click="addPreferential" :disabled="form.promotionTypeData.length >= 5">
                                    {{ form.promotionTypeData.length >= 5 ? $t('多添加5级优惠') : $t('添加下一级优惠') }}
                                </el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="promotionTypeData">
                        <el-table :data="form.promotionTypeData" style="width: 100%">
                            <el-table-column :label="$t('层级')" width="55" v-if="rulesType == 1">
                                <template #default="{ $index }">
                                    {{ $index + 1 }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门槛')" prop="minAmount" width="170">
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'promotionTypeData.' + $index + '.minAmount'" :rules="rules.minAmount">
                                        <div class="flex flex-align-center" v-if="rulesType == 1">
                                            <p class="mr10">{{ $t('满') }}</p>
                                            <TigInput :type="unit == 1 ? 'decimal' : 'integer'" v-model="row.minAmount" width="90px"></TigInput>
                                            <p class="ml10">{{ unit == 1 ? config.config.amountUnit : $t('件') }}</p>
                                        </div>
                                        <div class="flex flex-align-center" v-if="rulesType == 0">
                                            <p class="mr10" style="flex-shrink: 0">{{ $t('每满') }}</p>
                                            <TigInput :type="unit == 1 ? 'decimal' : 'integer'" v-model="row.minAmount" width="90px"></TigInput>
                                            <p class="ml10">{{ unit == 1 ? config.config.amountUnit : $t('件') }}</p>
                                        </div>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column prop="reduce" min-width="150" v-if="promotionType == 1 || promotionType == 2">
                                <template #header>
                                    <div>
                                        <el-popover placement="top" :width="200" trigger="click">
                                            <template #reference>
                                                <el-button :disabled="formState.productStatus == '活动进行中'" type="info" link style="font-size: 12px; font-weight: bold">
                                                    {{ $t('{rulesType}已选: ({promotionType1})', { rulesType: rulesType ? "减价/折扣" : "减价", promotionType1: promotionType == 1 ? "减价" : "折扣" }) }}<el-icon
                                                        class="el-icon--right"
                                                        ><ArrowDownBold
                                                    /></el-icon>
                                                </el-button>
                                            </template>
                                            <template #default>
                                                <div class="check_list">
                                                    <el-radio-group v-model="promotionType">
                                                        <div class="flex flex-align-center flex-justify-center">
                                                            <div style="margin-right: 30px">
                                                                <el-radio :label="$t('减价')" :value="1" />
                                                            </div>
                                                            <div v-if="rulesType">
                                                                <el-radio :label="$t('折扣')" :value="2" />
                                                            </div>
                                                        </div>
                                                    </el-radio-group>
                                                </div>
                                            </template>
                                        </el-popover>
                                    </div>
                                </template>
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'promotionTypeData.' + $index + '.reduce'" :rules="rules.reduce">
                                        <div class="flex flex-align-center" v-if="promotionType == 1">
                                            <p class="mr10">{{ $t('减') }}</p>
                                            <TigInput type="decimal" v-model="row.reduce" width="90px" :disabled="row.minAmount <= 0"></TigInput>
                                            <p class="ml10">{{ config.config.amountUnit }}</p>
                                        </div>
                                        <div class="flex flex-align-center" v-if="promotionType == 2">
                                            <p class="mr10">{{ $t('打') }}</p>
                                            <TigInput type="decimal" v-model="row.reduce" width="90px" :disabled="row.minAmount <= 0"></TigInput>
                                            <p class="ml10">{{ $t('折') }}</p>
                                        </div>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('送赠品')" prop="date" v-if="promotionType == 3" width="150">
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'promotionTypeData.' + $index + '.giftId'" :rules="rules.giftId">
                                        <SelectGift
                                            v-model:giftId="row.giftId"
                                            v-model:giftName="row.giftName"
                                            v-model:giftStock="row.giftStock"
                                            :isMultiple="false"
                                            @update:giftId="(val) => onGiftIdChange($index, val)"
                                        ></SelectGift>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('赠品数量')" prop="num" v-if="promotionType == 3">
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'promotionTypeData.' + $index + '.num'" :rules="rules.num">
                                        <TigInput
                                            type="integer"
                                            v-model="row.num"
                                            :max="row.giftStock"
                                            width="90px"
                                            :disabled="row.giftId == ''"
                                        ></TigInput>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" width="100" v-if="rulesType == 1">
                                <template #default="{ row, $index }">
                                    <el-button v-if="$index > 0" type="primary" link @click="del($index)">{{ $t('删除') }}</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { Plus, ArrowDownBold } from "@element-plus/icons-vue";
import { nextTick, onMounted, reactive, watch, shallowRef } from "vue";
import type { FormRules } from "element-plus";
import { SelectGift } from "@/components/select";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
const props = defineProps({
    unit: {
        type: Number,
        default: 0
    },
    rulesType: {
        type: Number,
        default: 0
    },
    promotionTypeData: {
        type: Array,
        default: []
    },
    formState: {
        type: Object,
        default: () => ({})
    }
});
const formRef = shallowRef();
// 监听 rulesType 的变化
watch(
    () => props.rulesType,
    (newVal) => {
        if (promotionType.value != 3) {
            if(newVal == 0){
                promotionType.value = 1;
            }
            if (promotionType.value == 1 || promotionType.value == 2) {
                if (form.promotionTypeData.length <= 5) {
                    form.promotionTypeData = [
                        {
                            minAmount: "",
                            reduce: ""
                        }
                    ];
                }
            }
        } else {
            form.promotionTypeData = [
                {
                    minAmount: "",
                    reduce: "",
                    giftId: "",
                    giftStock: 0,
                    giftName: "",
                    num: 1
                }
            ];
        }
    }
);
const promotionType = defineModel("promotionType", { type: Number, default: 0 });
const productGiftList: any[] = (props.formState as any)?.productGift ?? [];
const form = reactive({
    promotionTypeData: props.promotionTypeData.map((item: any) => {
        const matched = productGiftList.find((g: any) => g.giftId === item.giftId);
        return {
            ...item,
            giftStock: matched?.giftStock ?? item.giftStock ?? 0
        };
    })
});
const onGiftIdChange = (rowIndex: number, val: any) => {
    // 选择赠品后，清除该行 giftId 的表单校验提示
    if (val === "" || val === 0 || val === null || val === undefined) return;
    nextTick(() => {
        formRef.value?.clearValidate?.([`promotionTypeData.${rowIndex}.giftId`]);
    });
};
const validateReduce = (rule: any, value: any, callback: any, minAmount: any, rowIndex: number) => {
    if (promotionType.value == 1 && props.unit == 1) {
        if (value === "") {
            callback(new Error(t('请输入金额数')));
        } else if (value >= Number(minAmount)) {
            callback(new Error(t('应小于门槛金额')));
            (form.promotionTypeData[rowIndex] as any).reduce = minAmount - 0.01;
        } else if (value < 0.01 || value > 99999) {
            callback(new Error(t('输入范围为0.01-99999')));
            (form.promotionTypeData[rowIndex] as any).reduce = 0.01;
        } else {
            callback();
        }
    } else {
        callback();
    }
    if (promotionType.value == 2) {
        if (value === "") {
            callback(new Error(t('请输入折扣')));
        } else if (value < 0.1 || value > 9.9) {
            callback(new Error(t('折扣范围为0.1-9.9')));
            (form.promotionTypeData[rowIndex] as any).reduce = 0.1;
        } else {
            callback();
        }
    }
};
const validateMinAmount = (rule: any, value: any, callback: any) => {
    if (value === "") {
        callback(new Error(t('请输入门槛数值')));
    } else if (Number(value) < 0) {
        callback(new Error(t('门槛数值必须为正数')));
    } else {
        callback();
    }
};
const validateGiftId = (rule: any, value: any, callback: any) => {
    if (value === "") {
        callback(new Error(t('请选择赠品')));
    } else {
        callback();
    }
};
const validateNum = (rule: any, value: any, callback: any, giftStock: any, rowIndex: number) => {
    if (value === "") {
        callback(new Error(t('请输入赠品数量')));
    } else if (Number(value) <= 0) {
        callback(new Error(t('赠品数量必须为正数')));
        (form.promotionTypeData[rowIndex] as any).num = 1;
    } else if (Number(value) > Number(giftStock)) {
        callback(new Error(t('赠品数量不能大于库存')));
        (form.promotionTypeData[rowIndex] as any).num = giftStock;
    } else {
        callback();
    }
};
interface RuleForm {
    minAmount: number;
    reduce: number;
    num: number;
    giftId: number;
}
const rules = reactive<FormRules<RuleForm>>({
    minAmount: [{ required: true, validator: validateMinAmount, trigger: "blur" }],
    giftId: [{ required: true, validator: validateGiftId, trigger: "blur" }],
    reduce: [
        {
            required: true,
            validator: (rule: any, value, callback) => {
                const regex = /\.(\d+)\./;
                const match: any = rule?.field.match(regex);
                const rowIndex = match[1];
                const minAmount = (form.promotionTypeData[rowIndex] as any).minAmount;
                validateReduce(rule, value, callback, minAmount, rowIndex);
            },
            trigger: "blur"
        }
    ],
    num: [
        {
            required: true,
            validator: (rule: any, value, callback) => {
                const regex = /\.(\d+)\./;
                const match: any = rule?.field.match(regex);
                const rowIndex = match[1];
                const giftStock = (form.promotionTypeData[rowIndex] as any).giftStock;
                validateNum(rule, value, callback, giftStock, rowIndex);
            },
            trigger: "blur"
        }
    ]
});
const addPreferential = () => {
    if (promotionType.value == 1 || promotionType.value == 2) {
        if (form.promotionTypeData.length <= 5) {
            form.promotionTypeData.push({
                minAmount: "",
                reduce: ""
            });
        }
    } else {
        form.promotionTypeData.push({
            minAmount: "",
            reduce: "",
            giftId: "",
            giftStock: 0,
            giftName: "",
            num: 1
        });
    }
};
const del = (index: number) => {
    form.promotionTypeData.splice(index, 1);
};
onMounted(() => {
    if (props.rulesType == 1 && props.promotionTypeData.length == 0) {
        addPreferential();
    }
});
defineExpose({ formRef, form });
</script>
<style lang="less" scoped>
.lyecs-form-table {
    width: 100%;
    max-width: 650px;
    margin-top: 15px;
    min-width: 0;
    :deep(.el-table .cell) {
        overflow: visible !important;
    }
    :deep(.el-form-item__error) {
        position: absolute !important;
        z-index: 99;
        top: 32px !important;
    }
}
</style>
