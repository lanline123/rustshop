/**
 * @file 库存预警设置弹窗
 * @description 单个与批量共用；targets 长度为 1 走单个保存，大于 1 走批量保存
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form ref="formRef" :model="formState" :rules="rules" label-width="100px" @submit.prevent>
                <el-form-item :label="$t('库存预警')">
                    <div>
                        <div class="switch-line">
                            <el-switch v-model="formState.warnEnabled" :active-value="1" :inactive-value="0" />
                            <span class="switch-state">{{ formState.warnEnabled === 1 ? $t('开启') : $t('关闭') }}</span>
                        </div>
                        <div class="extra">{{ $t('开启后，该商品达到库存上/下限的商品将会自动推送消息') }}</div>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('库存下限')" prop="lowerLimit" required>
                    <div class="form-field">
                        <TigInput v-model="formState.lowerLimit" type="integer" :placeholder="$t('请输入库存下限')" />
                        <div class="extra">{{ $t('当实时库存 < 库存下限，将显示低库存') }}</div>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('库存上限')" prop="upperLimit" required>
                    <div class="form-field">
                        <TigInput v-model="formState.upperLimit" type="integer" :placeholder="$t('请输入库存上限')" />
                        <div class="extra">{{ $t('当实时库存 > 库存上限，将显示高库存') }}</div>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n";
import { batchSaveStockWarning, saveStockWarning } from "@/api/product/stockWarning";
import type { StockWarningTarget } from "@/types/product/stockWarning";

const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading"]);

const props = defineProps({
    targets: {
        type: Array as () => StockWarningTarget[],
        default: () => []
    },
    warnEnabled: {
        type: [Number, null] as unknown as () => number | null,
        default: 1
    },
    lowerLimit: {
        type: [Number, String, null] as unknown as () => number | string | null,
        default: null
    },
    upperLimit: {
        type: [Number, String, null] as unknown as () => number | string | null,
        default: null
    }
});

const formRef = ref();
const formState = reactive({
    // 未配置的商品 warnEnabled 为 null，弹窗按开启预置
    warnEnabled: props.warnEnabled === 0 ? 0 : 1,
    lowerLimit: props.lowerLimit === 0 || props.lowerLimit ? String(props.lowerLimit) : "",
    upperLimit: props.upperLimit === 0 || props.upperLimit ? String(props.upperLimit) : ""
});

const isBatch = computed(() => props.targets.length > 1);

const isNonNegativeInteger = (value: string) => /^\d+$/.test(value);

// 后端要求上下限始终必填，关闭开关时也照样保存，重新打开即恢复
const validateLower = (rule: any, value: string, callback: any) => {
    if (!value) {
        return callback(new Error(t("请输入库存下限")));
    }
    if (!isNonNegativeInteger(value)) {
        return callback(new Error(t("库存下限须为非负整数")));
    }
    callback();
};

const validateUpper = (rule: any, value: string, callback: any) => {
    if (!value) {
        return callback(new Error(t("请输入库存上限")));
    }
    if (!isNonNegativeInteger(value)) {
        return callback(new Error(t("库存上限须为非负整数")));
    }
    if (formState.lowerLimit !== "" && Number(value) <= Number(formState.lowerLimit)) {
        return callback(new Error(t("库存上限必须大于库存下限")));
    }
    callback();
};

const rules = {
    lowerLimit: [{ validator: validateLower, trigger: "blur" }],
    upperLimit: [{ validator: validateUpper, trigger: "blur" }]
};

const toSubmitValue = (value: string) => (value === "" ? null : Number(value));

const notifyBatchResult = (result: any) => {
    const successCount = Number(result?.successCount || 0);
    const failItems = result?.failItems || [];
    if (successCount > 0) {
        message.success(t("成功设置{count}条", { count: successCount }));
    }
    // 后端失败项只回传定位信息，没有商品名
    failItems.forEach((item: any) => {
        message.error(`${item.productId}：${item.reason}`);
    });
    if (successCount === 0 && failItems.length === 0) {
        message.success(t("操作成功"));
    }
};

const onSubmit = async () => {
    const payload = {
        warnEnabled: formState.warnEnabled as 0 | 1,
        lowerLimit: toSubmitValue(formState.lowerLimit),
        upperLimit: toSubmitValue(formState.upperLimit)
    };
    try {
        emit("update:confirmLoading", true);
        if (isBatch.value) {
            const result = await batchSaveStockWarning({ targets: props.targets, ...payload });
            notifyBatchResult(result);
        } else {
            await saveStockWarning({ ...props.targets[0], ...payload });
            message.success(t("操作成功"));
        }
        emit("submitCallback", true);
    } catch (error: any) {
        message.error(error.message || t("操作失败"));
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    formRef.value?.validate((valid: boolean) => {
        if (!valid) return;
        onSubmit();
    });
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.switch-line {
    display: flex;
    align-items: center;
    gap: 10px;
}

.switch-state {
    color: #333;
}

.form-field {
    width: 100%;
}

.extra {
    margin-top: 4px;
    color: #999;
    font-size: 13px;
    line-height: 1.6;
}
</style>
