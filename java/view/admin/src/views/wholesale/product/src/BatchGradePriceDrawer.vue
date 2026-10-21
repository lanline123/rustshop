/**
 * @file 子抽屉：批量设置类型售价 / 等级差异化
 * @description 选择规格 + 按等级分组阶梯；确定后套用（覆盖）到所选规格的等级类型售价（前端表单态，主抽屉统一保存）
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
                    <el-checkbox v-for="sku in skus" :key="sku.skuId" :value="sku.skuId">{{ sku.skuName }}</el-checkbox>
                </el-checkbox-group>
            </div>
        </div>
        <div class="ladder-wrap">
            <GradeLadderTable
                v-model="gradeLadders"
                :gradeOptions="gradeOptions"
                :priceMode="priceMode"
                :retailPrice="retailPrice"
                :errors="gradeErrors"
                errorKeyPrefix="grade"
                @clear-error="clearError"
                @blur="validate(true)"
            />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, computed, reactive, nextTick } from "vue";
import { Modal, message } from "ant-design-vue";
import GradeLadderTable from "./GradeLadderTable.vue";
import type { GradeLadder, LadderError } from "@/types/wholesale/price";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";
import { validateLadderGroup } from "./ladderValidate";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    skus: { skuId: number; skuName: string }[];
    gradeOptions: WholesalerGradeVO[];
    priceMode: "discount" | "fixed";
    retailPrice?: number;
}>();
const emit = defineEmits(["submitCallback"]);

const checkedSkuIds = ref<number[]>([]);
const gradeLadders = ref<GradeLadder[]>([]);

const onSelectAll = () => {
    checkedSkuIds.value = props.skus.map((s) => s.skuId);
};

const onClearAll = () => {
    checkedSkuIds.value = [];
};

// ===== 校验：行级错误，仿 el-form-item 标红 + 行下方提示（与 PriceDrawer 一致） =====
const errors = reactive<Record<string, LadderError>>({});
const errKey = (gradeId: number, index: number) => `${gradeId}:${index}`;
const gradeErrors = computed<Record<number, (LadderError | undefined)[]>>(() => {
    const map: Record<number, (LadderError | undefined)[]> = {};
    gradeLadders.value.forEach((g) => {
        map[g.gradeId] = g.ladders.map((_, i) => errors[errKey(g.gradeId, i)]);
    });
    return map;
});

/** 子表通知：用户修改了某行某字段，清除该字段错误 */
const clearError = (p: { gradeId: number; index: number; field: "qty" | "value" }) => {
    const k = errKey(p.gradeId, p.index);
    const cur = errors[k];
    if (!cur) return;
    delete cur[p.field];
    if (!cur.qty && !cur.value) delete errors[k];
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
 * 校验类型售价；silent=true 时只标红、不弹 toast、不滚动（失焦时用）
 * @returns true=通过
 */
const validate = (silent = false): boolean => {
    Object.keys(errors).forEach((k) => delete errors[k]);
    if (gradeLadders.value.length === 0) {
        if (!silent) message.error(t('请至少为一个等级配置阶梯'));
        return false;
    }
    let firstErr: { msg: string; key: string } | null = null;
    const mark = (msg: string, key: string) => {
        if (!firstErr) firstErr = { msg, key };
    };
    gradeLadders.value.forEach((g) => {
        if (g.ladders.length === 0) return;
        const merger = (i: number, patch: LadderError) => {
            const k = errKey(g.gradeId, i);
            errors[k] = { ...errors[k], ...patch };
        };
        if (g.ladders.length > 20) {
            merger(0, { qty: t('阶梯不能超过 20 段') });
            mark(t('{gradeName}阶梯不能超过 20 段', { gradeName: g.gradeName }), `grade:${g.gradeId}:0:qty`);
        }
        const err = validateLadderGroup(g.ladders, props.priceMode, props.retailPrice ?? 0, merger);
        if (err) mark(`${g.gradeName}${err.msg}`, `grade:${g.gradeId}:${err.index}:${err.field}`);
    });
    const fe = firstErr as { msg: string; key: string } | null;
    if (fe) {
        if (!silent) {
            message.error(fe.msg);
            scrollToFirstError(fe.key);
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
        content: t('将覆盖所选规格的类型售价，是否继续？'),
        onOk() {
            emit("submitCallback", {
                skuIds: [...checkedSkuIds.value],
                gradeLadders: gradeLadders.value.map((g) => ({
                    gradeId: g.gradeId,
                    gradeName: g.gradeName,
                    ladders: g.ladders.map((l) => ({ qty: l.qty, value: l.value }))
                }))
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
                display: flex;
                flex-wrap: wrap;
                gap: 16px;
            }
            :deep(.el-checkbox) {
                margin-right: 0;
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
