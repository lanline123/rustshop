<template>
    <el-popover
        trigger="click"
        placement="bottom"
        :hide-after="0"
        v-model:visible="visible"
        :teleported="true"
        :persistent="true"
        width="360px"
    >
        <template #reference>
            <span class="batch-fee-trigger iconfont icon-bianji1"></span>
        </template>
        <template #default>
            <div style="width: 100%; padding: 8px 6px 5px; box-sizing: border-box">
                <el-form ref="formRef" :model="form" label-width="70px">
                    <el-form-item :label="feeLabel" prop="serviceFeeValue" :rules="feeRules">
                        <TigInput v-model="form.serviceFeeValue" type="decimal" width="100%">
                            <template #prepend>
                                <el-select
                                    v-model="form.serviceFeeType"
                                    style="width: 76px; min-width: 76px"
                                    :teleported="false"
                                    @change="form.serviceFeeValue = 0"
                                >
                                    <el-option :label="$t('比例')" :value="FeeType.PERCENT" />
                                    <el-option :label="$t('固定')" :value="FeeType.FIXED" />
                                </el-select>
                            </template>
                            <template #append>{{ form.serviceFeeType === FeeType.PERCENT ? "%" : config.dollarSign }}</template>
                        </TigInput>
                    </el-form-item>
                </el-form>
                <div class="flex" style="justify-content: right">
                    <el-button size="small" @click="visible = false">{{ $t('取消') }}</el-button>
                    <el-button size="small" type="primary" @click="onConfirm">{{ $t('确认') }}</el-button>
                </div>
            </div>
        </template>
    </el-popover>
</template>

<script lang="ts" setup>
import { reactive, computed, shallowRef, watch } from "vue";
import { useConfigStore } from "@/store/config";
import { FeeType } from "./serviceFeeConstants";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore().config;
const formRef = shallowRef();

withDefaults(defineProps<{ feeLabel?: string }>(), { feeLabel: "服务费用" });

const visible = defineModel<boolean>("visible", { default: false });

const emit = defineEmits<{
    confirm: [payload: { serviceFeeType: number; serviceFeeValue: number }];
}>();

const form = reactive({
    serviceFeeType: FeeType.FIXED as number,
    serviceFeeValue: 0 as number | string
});

const feeRules = computed(() => [
    {
        validator: (_rule: any, value: any, callback: any) => {
            if (value === "" || value === null || value === undefined) {
                return callback(new Error(t('请输入服务费用')));
            }
            const num = Number(value);
            if (isNaN(num)) return callback(new Error(t('请输入有效数字')));
            if (form.serviceFeeType === FeeType.PERCENT) {
                if (num < 0 || num > 100) return callback(new Error(t('比例范围为 0~100%')));
            } else if (num < 0) {
                return callback(new Error(t('金额不能小于 0')));
            }
            callback();
        },
        trigger: "change"
    }
]);

const reset = () => {
    form.serviceFeeType = FeeType.FIXED;
    form.serviceFeeValue = 0;
};

watch(visible, (val) => {
    if (val) reset();
});

const onConfirm = async () => {
    try {
        await formRef.value?.validate();
    } catch {
        return;
    }
    emit("confirm", {
        serviceFeeType: form.serviceFeeType,
        serviceFeeValue: Number(form.serviceFeeValue) || 0
    });
    visible.value = false;
};

defineExpose({ reset });
</script>

<style scoped>
.batch-fee-trigger {
    cursor: pointer;
    color: #999;
    font-size: 16px;
    padding-left: 2px;
    line-height: 1;
}
</style>
