<template>
    <div class="reply-list">
        <div v-if="canAdd && !disabled" class="reply-list__toolbar">
            <el-button type="primary" link @click="addReply">
                <el-icon class="reply-list__add-icon"><Plus /></el-icon>
                <span>{{ $t('添加内容') }}</span>
            </el-button>
            <span class="reply-list__count">{{ replies.length }} / {{ MAX_REPLIES }}</span>
        </div>
        <div v-else-if="!disabled" class="reply-list__toolbar reply-list__toolbar--full">
            <span class="reply-list__count">{{ $t('已达上限{MAX_REPLIES}条', { MAX_REPLIES }) }}</span>
        </div>

        <div v-for="(item, i) in replies" :key="i" class="reply-card">
            <div class="reply-card__head">
                <span class="reply-card__title">{{ $t('回复{i1}', { i1: i + 1 }) }}</span>
                <div class="reply-card__actions">
                    <el-button v-if="canRemove && !disabled" text type="danger" @click="removeReply(i)">{{ $t('删除') }}</el-button>
                </div>
            </div>

            <div class="reply-card__body">
                <ReplyPayloadForm
                    :ref="(el) => setCardRef(i, el as ReplyPayloadFormInstance | null)"
                    v-model="replies[i]"
                    :content-max-length="TEXT_MAX_LEN"
                    :show-preview="false"
                    :disabled="disabled"
                />
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { Plus } from "@element-plus/icons-vue";
import type { ReplyPayload } from "@/types/setting/wechat.d";
import ReplyPayloadForm from "@/views/setting/wechat/components/ReplyPayloadForm.vue";

type ReplyPayloadFormInstance = InstanceType<typeof ReplyPayloadForm>;

const MIN_REPLIES = 1;
const MAX_REPLIES = 5;
// 关键字回复上下文文本上限（需求 §5.3），比默认 600 更严格
const TEXT_MAX_LEN = 300;

interface Props {
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false
});

const replies = defineModel<ReplyPayload[]>({ required: true });

// 每张卡片的 ReplyPayloadForm 实例，索引与 replies 对齐
const cardRefs = ref<Array<ReplyPayloadFormInstance | null>>([]);

function setCardRef(index: number, instance: ReplyPayloadFormInstance | null) {
    cardRefs.value[index] = instance;
}

// replies 长度缩短后修剪多余 ref 槽位（新增时 Vue 会在模板挂载后自动写入对应索引）
watch(
    () => replies.value?.length ?? 0,
    (len) => {
        if (cardRefs.value.length > len) {
            cardRefs.value.length = len;
        }
    },
    { flush: "post" }
);

const canAdd = computed(() => (replies.value?.length ?? 0) < MAX_REPLIES);
const canRemove = computed(() => (replies.value?.length ?? 0) > MIN_REPLIES);

function createBlankReply(): ReplyPayload {
    return { msgType: 1, content: "" };
}

function addReply() {
    if (!canAdd.value) return;
    const arr = [...(replies.value ?? []), createBlankReply()];
    replies.value = arr;
}

function removeReply(i: number) {
    if (!canRemove.value) return;
    const arr = [...(replies.value ?? [])];
    arr.splice(i, 1);
    replies.value = arr;
    cardRefs.value.splice(i, 1);
}

interface ReplyListValidateResult {
    ok: boolean;
    firstErrorIndex?: number;
}

// T6-4 可能把 validate 返回从 boolean 改成 { ok, messages }，此处做宽松兼容
function interpretValidate(v: unknown): boolean {
    if (v === true) return true;
    if (v === false) return false;
    if (v && typeof v === "object" && "ok" in (v as Record<string, unknown>)) {
        return Boolean((v as { ok: unknown }).ok);
    }
    // undefined / 其他情况按通过处理（ReplyPayloadForm 内部自行管校验态）
    return true;
}

async function validate(): Promise<ReplyListValidateResult> {
    const list = replies.value ?? [];
    if (list.length < MIN_REPLIES) {
        return { ok: false, firstErrorIndex: 0 };
    }
    for (let i = 0; i < cardRefs.value.length; i++) {
        const inst = cardRefs.value[i];
        if (!inst || typeof inst.validate !== "function") continue;
        const result = await inst.validate();
        if (!interpretValidate(result)) {
            return { ok: false, firstErrorIndex: i };
        }
    }
    return { ok: true };
}

function clearValidate() {
    for (const inst of cardRefs.value) {
        inst?.clearValidate?.();
    }
}

// T6-4 若把方法改名为 getPayload 亦兼容；缺失时退化为返回 modelValue 当前快照
function getCleanPayloads(): ReplyPayload[] {
    const list = replies.value ?? [];
    return list.map((raw, i) => {
        const inst = cardRefs.value[i] as
            | (ReplyPayloadFormInstance & {
                  getCleanPayload?: () => ReplyPayload;
                  getPayload?: () => ReplyPayload;
              })
            | null
            | undefined;
        if (inst?.getCleanPayload) return inst.getCleanPayload();
        if (inst?.getPayload) return inst.getPayload();
        return { ...raw };
    });
}

defineExpose({ validate, clearValidate, getCleanPayloads });
</script>

<style lang="less" scoped>
.reply-list {
    flex: 1;
    max-width: 953px;
    display: flex;
    flex-direction: column;
    gap: 12px;

    &__toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;

        &--full {
            justify-content: flex-end;
        }
    }

    &__count {
        color: #999;
        font-size: 12px;
    }
}

.reply-card {
    border-radius: 4px;
    padding: 12px 16px;
    background: var(--el-fill-color-light);

    &__head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
    }

    &__title {
        font-weight: 600;
        color: var(--el-text-color-primary);
    }

    &__actions {
        display: flex;
        gap: 4px;
    }

    &__body {
        background: var(--el-bg-color);
        border-radius: 4px;
        padding: 12px;

        :deep(.el-form-item) {
            margin-bottom: 24px;
        }
    }
}
</style>
