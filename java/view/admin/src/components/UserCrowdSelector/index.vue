<template>
    <div class="user-crowd-selector">
        <el-radio-group v-model="audienceType" @change="onTypeChange">
            <el-radio :value="0">全部会员</el-radio>
            <el-radio :value="1">按会员等级</el-radio>
            <el-radio :value="2">按会员标签</el-radio>
        </el-radio-group>

        <div v-if="audienceType === 1" class="crowd-sub">
            <el-select
                v-model="rankIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                filterable
                placeholder="请选择会员等级"
                style="width: 360px"
                @change="emitChange"
            >
                <el-option v-for="r in rankOptions" :key="r.rankId" :label="r.rankName" :value="r.rankId" />
            </el-select>
        </div>

        <div v-if="audienceType === 2" class="crowd-sub tag-sub">
            <span v-if="tagIds.length" class="tag-summary">
                已选 {{ tagIds.length }} 个标签（{{ tagMatchMode === 1 ? "满足全部" : "满足任一" }}）
            </span>
            <span v-else class="placeholder">未选择标签</span>
            <el-button link type="primary" @click="openTagDialog">选择标签</el-button>
        </div>

        <div v-if="audienceType !== 0" class="crowd-estimate">
            <span v-if="estimating">预估中...</span>
            <span v-else-if="estimateError" class="estimate-error">预估失败，请重试</span>
            <span v-else-if="estimateCount !== null">预估触达 {{ estimateCount }} 人</span>
        </div>

        <el-dialog v-model="tagDialogVisible" title="选择会员标签" width="900px" append-to-body destroy-on-close>
            <FilterTagSelector
                ref="tagSelectorRef"
                :initial-tag-ids="tagIds"
                :initial-match-mode="tagMatchMode === 1 ? 'all' : 'any'"
            />
            <template #footer>
                <el-button @click="tagDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmTags">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from "vue";
import FilterTagSelector from "@/views/user/user/FilterTagSelector.vue";
import { getUserRankList } from "@/api/user/levelManage";
import { estimateAudience } from "@/api/user/userCrowd";
import type { MemberAudience } from "@/types/user/memberAudience";

const props = defineProps<{
    modelValue?: MemberAudience | null;
}>();

const emit = defineEmits<{
    (e: "update:modelValue", value: MemberAudience): void;
}>();

const audienceType = ref<number>(0);
const rankIds = ref<number[]>([]);
const tagIds = ref<number[]>([]);
const tagMatchMode = ref<number>(0);

const rankOptions = ref<any[]>([]);
const tagDialogVisible = ref(false);
const tagSelectorRef = ref<any>(null);

const estimateCount = ref<number | null>(null);
const estimating = ref(false);
const estimateError = ref(false);
let estimateTimer: ReturnType<typeof setTimeout> | null = null;
let estimateSeq = 0;

const buildValue = (): MemberAudience => ({
    audienceType: audienceType.value,
    rankIds: audienceType.value === 1 ? [...rankIds.value] : [],
    tagIds: audienceType.value === 2 ? [...tagIds.value] : [],
    tagMatchMode: tagMatchMode.value
});

const emitChange = () => {
    emit("update:modelValue", buildValue());
    scheduleEstimate();
};

const onTypeChange = () => {
    emitChange();
};

const openTagDialog = () => {
    tagDialogVisible.value = true;
};

const confirmTags = () => {
    const result = tagSelectorRef.value?.getResult?.();
    if (result) {
        tagIds.value = [...result.tagIds];
        tagMatchMode.value = result.matchMode === "all" ? 1 : 0;
    }
    tagDialogVisible.value = false;
    emitChange();
};

const scheduleEstimate = () => {
    if (estimateTimer) clearTimeout(estimateTimer);
    if (audienceType.value === 0) {
        estimateCount.value = null;
        estimateError.value = false;
        return;
    }
    if (audienceType.value === 1 && rankIds.value.length === 0) {
        estimateCount.value = null;
        return;
    }
    if (audienceType.value === 2 && tagIds.value.length === 0) {
        estimateCount.value = null;
        return;
    }
    estimateTimer = setTimeout(doEstimate, 500);
};

const doEstimate = async () => {
    const seq = ++estimateSeq;
    estimating.value = true;
    estimateError.value = false;
    try {
        const res = await estimateAudience(buildValue());
        if (seq === estimateSeq) estimateCount.value = res.count;
    } catch (_) {
        if (seq === estimateSeq) {
            estimateError.value = true;
            estimateCount.value = null;
        }
    } finally {
        if (seq === estimateSeq) estimating.value = false;
    }
};

const syncFromModel = (v?: MemberAudience | null) => {
    audienceType.value = v?.audienceType ?? 0;
    rankIds.value = v?.rankIds ? [...v.rankIds] : [];
    tagIds.value = v?.tagIds ? [...v.tagIds] : [];
    tagMatchMode.value = v?.tagMatchMode ?? 0;
};

/** 供父表单提交前调用：校验「按等级/按标签」必选项 */
const validate = (): { valid: boolean; message?: string } => {
    if (audienceType.value === 1 && rankIds.value.length === 0) {
        return { valid: false, message: "请选择会员等级" };
    }
    if (audienceType.value === 2 && tagIds.value.length === 0) {
        return { valid: false, message: "请选择会员标签" };
    }
    return { valid: true };
};

watch(
    () => props.modelValue,
    (v) => {
        syncFromModel(v);
        scheduleEstimate();
    },
    { immediate: true }
);

onMounted(async () => {
    try {
        const res: any = await getUserRankList({ page: 1, size: 100, keyword: "" } as any);
        rankOptions.value = res?.records || res || [];
    } catch (_) {
        /* ignore */
    }
});

defineExpose({ validate });
</script>

<style lang="less" scoped>
.user-crowd-selector {
    .crowd-sub {
        margin-top: 12px;
    }

    .tag-sub {
        display: flex;
        align-items: center;
        gap: 12px;

        .placeholder {
            color: #909399;
        }

        .tag-summary {
            color: #606266;
        }
    }

    .crowd-estimate {
        margin-top: 8px;
        font-size: 13px;
        color: #909399;

        .estimate-error {
            color: var(--el-color-danger);
        }
    }
}
</style>
