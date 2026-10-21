<template>
    <div class="keyword-list">
        <div v-for="(row, index) in rows" :key="index" class="kw-row">
            <div class="kw-row__fields">
                <el-select :model-value="row.matchType" class="kw-row__match" :disabled="disabled" @change="(val: number) => onMatchTypeChange(index, val)">
                    <el-option :value="1" :label="$t('全匹配')" />
                    <el-option :value="2" :label="$t('半匹配')" />
                </el-select>

                <el-input
                    :model-value="row.keyword"
                    class="kw-row__keyword"
                    :maxlength="KEYWORD_MAX_LEN"
                    :disabled="disabled"
                    :placeholder="$t('请输入关键字')"
                    @update:model-value="(val: string) => onKeywordChange(index, val)"
                />

                <div class="kw-row__actions">
                    <el-button
                        v-if="index === rows.length - 1 && rows.length < MAX_ROWS"
                        :icon="Plus"
                        circle
                        size="small"
                        :disabled="disabled"
                        :aria-label="'添加关键字'"
                        @click="addRow"
                    />
                    <el-button
                        v-if="rows.length > MIN_ROWS"
                        :icon="Minus"
                        circle
                        size="small"
                        :disabled="disabled"
                        :aria-label="'删除关键字'"
                        @click="removeRow(index)"
                    />
                </div>
            </div>

            <div v-if="errors[index]" class="kw-row__error">{{ errors[index] }}</div>
        </div>

        <div v-if="globalError" class="kw-global-error">{{ globalError }}</div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { ElButton, ElInput, ElOption, ElSelect } from "element-plus";
import { Minus, Plus } from "@element-plus/icons-vue";
import type { KeywordRuleKeyword } from "@/types/setting/wechat.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    modelValue: KeywordRuleKeyword[];
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false
});

const emit = defineEmits<{
    (e: "update:modelValue", value: KeywordRuleKeyword[]): void;
}>();

const MIN_ROWS = 1;
const MAX_ROWS = 10;
const KEYWORD_MAX_LEN = 30;

const rows = computed<KeywordRuleKeyword[]>(() => props.modelValue ?? []);

const errors = ref<string[]>([]);
const globalError = ref<string>("");

function cloneRows(list: KeywordRuleKeyword[]): KeywordRuleKeyword[] {
    return list.map((item) => ({
        keyword: item.keyword ?? "",
        matchType: item.matchType === 1 ? 1 : 2
    }));
}

function commit(next: KeywordRuleKeyword[]): void {
    emit("update:modelValue", next);
}

function onMatchTypeChange(index: number, value: number): void {
    const next = cloneRows(rows.value);
    if (!next[index]) return;
    next[index].matchType = value === 1 ? 1 : 2;
    commit(next);
}

function onKeywordChange(index: number, value: string): void {
    const next = cloneRows(rows.value);
    if (!next[index]) return;
    next[index].keyword = value;
    commit(next);
}

function addRow(): void {
    if (props.disabled) return;
    const next = cloneRows(rows.value);
    if (next.length >= MAX_ROWS) return;
    next.push({ keyword: "", matchType: 1 });
    commit(next);
}

function removeRow(index: number): void {
    if (props.disabled) return;
    const next = cloneRows(rows.value);
    if (next.length <= MIN_ROWS) return;
    next.splice(index, 1);
    commit(next);
}

/**
 * 计算行级错误：空、超长、同 matchType 下重复（大小写不敏感）。
 * 首次出现时不标错，仅后续重复行标红。
 */
function computeRowErrors(list: KeywordRuleKeyword[]): string[] {
    const msgs: string[] = new Array(list.length).fill("");
    list.forEach((row, idx) => {
        const kw = (row.keyword ?? "").trim();
        if (!kw) {
            msgs[idx] = t('关键字不能为空');
            return;
        }
        if (kw.length > KEYWORD_MAX_LEN) {
            msgs[idx] = t('关键字长度不能超过 {KEYWORD_MAX_LEN} 字', { KEYWORD_MAX_LEN });
            return;
        }
        const lower = kw.toLowerCase();
        const hasDup = list.some((other, j) => j < idx && other.matchType === row.matchType && (other.keyword ?? "").trim().toLowerCase() === lower);
        if (hasDup) {
            msgs[idx] = t('该关键字已存在（同匹配方式不可重复）');
        }
    });
    return msgs;
}

function clearValidate(): void {
    errors.value = [];
    globalError.value = "";
}

function validate(): { ok: boolean; errors: string[] } {
    const list = rows.value;
    const rowMsgs = computeRowErrors(list);
    errors.value = rowMsgs;
    if (list.length < MIN_ROWS) {
        globalError.value = t('至少需要 {MIN_ROWS} 个关键字', { MIN_ROWS });
    } else if (list.length > MAX_ROWS) {
        globalError.value = t('最多支持 {MAX_ROWS} 个关键字', { MAX_ROWS });
    } else {
        globalError.value = "";
    }
    const allMsgs: string[] = [];
    if (globalError.value) allMsgs.push(globalError.value);
    rowMsgs.forEach((m) => {
        if (m) allMsgs.push(m);
    });
    return { ok: allMsgs.length === 0, errors: allMsgs };
}

watch(
    () => props.modelValue,
    () => {
        // 父组件回填/重置时不立即触发红字提示，仅保持 errors 长度同步
        if (errors.value.length > 0) {
            errors.value = computeRowErrors(rows.value);
        }
    },
    { deep: true }
);

onMounted(() => {
    if (rows.value.length === 0) {
        commit([{ keyword: "", matchType: 1 }]);
    }
});

defineExpose({
    validate,
    clearValidate
});
</script>

<style lang="less" scoped>
.keyword-list {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.kw-row {
    display: flex;
    flex-direction: column;
}

.kw-row__fields {
    display: flex;
    align-items: center;
    gap: 8px;
}

.kw-row__match {
    width: 100px;
    min-width: 100px;
    flex: 0 0 auto;
}

.kw-row__keyword {
    flex: 1 1 auto;
    min-width: 0;
    max-width: 845px;
}

.kw-row__actions {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 0 0 auto;
    padding-left: 10px;
}

.kw-row__error,
.kw-global-error {
    color: #f56c6c;
    font-size: 12px;
    line-height: 1.4;
}

.kw-global-error {
    padding-left: 0;
}
</style>
