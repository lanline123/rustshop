<template>
    <!-- 不嵌套 form-item：使用最外层 form-item 承载 memberMarks 的校验提示 -->
    <el-form-item :label="$t('适用会员')" prop="memberMarks">
        <div class="member-range">
            <el-radio-group v-model="range" @change="onRangeChange">
                <el-radio :value="0">{{ $t('全部会员') }}</el-radio>
                <el-radio :value="1">{{ $t('部分会员') }}</el-radio>
            </el-radio-group>
            <div class="extra">{{ $t('选择“部分会员”后，可指定多个会员标记，仅对这些会员生效') }}</div>

            <!-- 选择“部分会员”时，直接在同一个 form-item 内展示选择会员标记 -->
            <div v-if="range === 1" class="inline-marks">
                <div class="member-marks">
                    <DialogForm
                        isDrawer
                        :title="$t('选择会员')"
                        width="800px"
                        path="promotion/src/SelectMemberMarks"
                        :params="{ selectedIds: marks }"
                        @okCallback="onMarksOk"
                    >
                        <el-button type="primary">{{ $t('选择会员标记') }}</el-button>
                    </DialogForm>
                    <div class="selected">
                        <div v-if="marks.length > 0" class="tags">
                            <el-tag v-for="v in marks" :key="v" class="tag" closable disable-transitions @close="onRemoveMark(v)">
                                <span class="tag-content">
                                    <SignTag :value="v" />
                                </span>
                            </el-tag>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </el-form-item>
</template>

<script setup lang="ts">
import { computed, nextTick } from "vue";
import { DialogForm } from "@/components/dialog";
import SignTag from "@/views/user/user/src/SignTag.vue";

const props = defineProps<{
    // 传入 el-form 的 ref 或实例，用于在选择会员标记后仅校验 memberMarks 这一项
    formRef?: any;
}>();

const rangeModel = defineModel("memberRange", { type: Number, default: 0 });
const marksModel = defineModel("memberMarks", { type: Array as () => number[], default: () => [] });

const validateMemberMarksField = async () => {
    const maybeRef = props.formRef;
    const form = maybeRef?.value ?? maybeRef;
    if (!form?.validateField) return;
    try {
        await form.validateField(["memberMarks"]);
    } catch {
        // validateField 失败时不抛出，避免影响选择流程（只用于刷新这一项的校验提示）
    }
};

const range = computed({
    get: () => Number(rangeModel.value ?? 0),
    set: (v: number) => (rangeModel.value = Number(v))
});

const marks = computed({
    get: () => (Array.isArray(marksModel.value) ? (marksModel.value as number[]) : []),
    set: (v: number[]) => (marksModel.value = v)
});

const onRangeChange = (v: number) => {
    if (Number(v) === 0) {
        marks.value = [];
        // 切回“全部会员”时，清理 memberMarks 后刷新一次该项校验提示（只校验 memberMarks）
        nextTick(() => validateMemberMarksField());
    }
};

const onMarksOk = (values: number[]) => {
    marks.value = Array.isArray(values) ? [...values] : [];
    // 选择完成后自动触发一次表单校验，仅校验会员标记这一项
    nextTick(() => validateMemberMarksField());
};

const onRemoveMark = (id: number) => {
    marks.value = marks.value.filter((v) => v !== id);
    // 删除标记后同步刷新这一项校验提示
    nextTick(() => validateMemberMarksField());
};
</script>

<style lang="less" scoped>
.member-range {
    .extra {
        margin-top: 6px;
        color: #999;
        font-size: 12px;
    }
}
.inline-marks {
    margin-top: 10px;
}
.member-marks {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    .selected {
        min-width: 320px;
        max-width: 560px;
    }
    .empty-tip {
        padding: 4px 0;
        color: #999;
        font-size: 12px;
    }
    .tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 10px;
    }
    .tag-content {
        display: inline-flex;
        align-items: center;
    }
    .ml6 {
        margin-left: 6px;
    }
}
</style>
