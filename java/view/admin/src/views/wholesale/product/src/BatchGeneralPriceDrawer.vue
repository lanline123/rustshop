/**
 * @file 子抽屉：批量设置通用售价
 * @description 选择规格 + 阶梯表；确定后套用（覆盖）到所选规格的通用售价（前端表单态，主抽屉统一保存）
 * 接入 DialogForm 作为容器：
 *   - 不再自带抽屉外壳与 footer，由 DialogForm 统一提供
 *   - DialogForm 通过 ref 调用本组件 expose 出的 onFormSubmit()
 *   - 成功后 emit submitCallback(payload) 由 DialogForm 转为 okCallback 抛给父级
 */
<template>
    <div class="batch-body">
        <div class="sku-select">
            <div class="header-row">
                <span class="label">{{ $t('选择规格：') }}</span>
                <el-button link type="primary" @click="onSelectAll" class="btn-link">{{ $t('全选') }}</el-button>
                <el-button link type="danger" @click="onClearAll" class="btn-link">{{ $t('清空') }}</el-button>
            </div>
            <div class="checkbox-list">
                <el-checkbox-group v-model="checkedSkuIds">
                    <el-checkbox v-for="sku in skus" :key="sku.skuId" :value="sku.skuId">
                        <el-tooltip :content="sku.skuName" placement="top" :show-after="300">
                            <span class="sku-name-text">{{ sku.skuName }}</span>
                        </el-tooltip>
                    </el-checkbox>
                </el-checkbox-group>
            </div>
        </div>
        <div class="ladder-wrap">
            <LadderTable
                v-model="ladders"
                :priceMode="priceMode"
                :retailPrice="retailPrice"
                :errors="ladderErrors"
                errorKeyPrefix="general"
                @clear-error="clearError"
                @blur="validate(true)"
            />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, computed, reactive, nextTick } from "vue";
import { Modal, message } from "ant-design-vue";
import LadderTable from "./LadderTable.vue";
import type { Ladder, LadderError } from "@/types/wholesale/price";
import { validateLadderGroup } from "./ladderValidate";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    skus: { skuId: number; skuName: string }[];
    priceMode: "discount" | "fixed";
    retailPrice?: number;
}>();
const emit = defineEmits(["submitCallback"]);

const checkedSkuIds = ref<number[]>([]);
// 不预填默认值，让用户自行录入（保留 1 个空档以便展示新增按钮）
const ladders = ref<Ladder[]>([{ qty: undefined, value: undefined } as unknown as Ladder]);

const onSelectAll = () => {
    checkedSkuIds.value = props.skus.map((s) => s.skuId);
};

const onClearAll = () => {
    checkedSkuIds.value = [];
};

// ===== 校验：行级错误，仿 el-form-item 标红 + 行下方提示（与 PriceDrawer 一致） =====
const errors = reactive<Record<number, LadderError>>({});
const ladderErrors = computed<(LadderError | undefined)[]>(() => ladders.value.map((_, i) => errors[i]));

/** 子表通知：用户修改了某行某字段，清除该字段错误 */
const clearError = (p: { index: number; field: "qty" | "value" }) => {
    const cur = errors[p.index];
    if (!cur) return;
    delete cur[p.field];
    if (!cur.qty && !cur.value) delete errors[p.index];
};

/** 把命中错误的输入框滚到视口中央并聚焦 */
const scrollToFirstError = (key: string) => {
    nextTick(() => {
        const el = document.querySelector<HTMLElement>(`[data-error-key="${key}"]`);
        if (!el) return;
        el.scrollIntoView({ behavior: "smooth", block: "center" });
        el.querySelector<HTMLInputElement>("input")?.focus({ preventScroll: true });
    });
};

/**
 * 校验阶梯；silent=true 时只标红、不弹 toast、不滚动（失焦时用）
 * @returns true=通过
 */
const validate = (silent = false): boolean => {
    Object.keys(errors).forEach((k) => delete errors[Number(k)]);
    if (ladders.value.length === 0) {
        if (!silent) message.error(t('通用售价至少配置 1 档'));
        return false;
    }
    const merger = (i: number, patch: LadderError) => {
        errors[i] = { ...errors[i], ...patch };
    };
    if (ladders.value.length > 20) {
        merger(0, { qty: t('阶梯不能超过 20 段') });
        if (!silent) {
            message.error(t('通用售价阶梯不能超过 20 段'));
            scrollToFirstError("general:0:qty");
        }
        return false;
    }
    const err = validateLadderGroup(ladders.value, props.priceMode, props.retailPrice ?? 0, merger);
    if (err) {
        if (!silent) {
            message.error(err.msg);
            scrollToFirstError(`general:${err.index}:${err.field}`);
        }
        return false;
    }
    return true;
};

/** DialogForm 点击「确认」时通过 ref 调用 */
const onFormSubmit = () => {
    if (checkedSkuIds.value.length === 0) {
        message.error(t('请先选择规格'));
        return;
    }
    if (!validate()) return;
    Modal.confirm({
        // 抽屉打开时确认框必须置于抽屉之上
        zIndex: 100000,
        title: t('批量套用'),
        content: t('将覆盖所选规格的通用售价，是否继续？'),
        onOk() {
            emit("submitCallback", {
                skuIds: [...checkedSkuIds.value],
                ladders: ladders.value.map((l) => ({ qty: l.qty, value: l.value }))
            });
        }
    });
};

defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
.batch-body {
    .sku-select {
        margin-bottom: 16px;
        .header-row {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
            .label {
                font-weight: 600;
                color: #303133;
                font-size: 14px;
            }
            .btn-link {
                padding: 0;
                height: auto;
                font-size: 14px;
            }
        }
        .checkbox-list {
            :deep(.el-checkbox-group) {
                display: grid;
                grid-template-columns: repeat(4, minmax(0, 1fr));
                gap: 16px;
            }
            :deep(.el-checkbox) {
                margin-right: 0;
                width: 100%;
                display: flex;
                align-items: center;
                // 超长规格名单行省略，完整内容靠 el-tooltip 悬停查看，保持各列各行对齐
                .el-checkbox__label {
                    flex: 1;
                    min-width: 0;
                    overflow: hidden;
                    .sku-name-text {
                        display: block;
                        max-width: 100%;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                    }
                }
                &.is-checked {
                    .el-checkbox__label {
                        color: var(--el-checkbox-text-color, var(--el-text-color-regular));
                    }
                }
            }
        }
    }
}
</style>
