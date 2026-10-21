<template>
    <div class="reply-payload-form" :class="{ 'no-preview': !showPreview }">
        <div class="form-col">
            <el-form ref="formRef" :model="local" :rules="rules" :disabled="disabled" :validate-on-rule-change="false" label-width="100px">
                <el-form-item :label="msgTypeLabel || $t('消息类型')" prop="msgType">
                    <el-radio-group v-model="local.msgType" @mousedown.capture="beginMsgTypeSwitch" @change="onMsgTypeChange">
                        <el-radio :value="1">{{ $t('文字') }}</el-radio>
                        <el-radio v-if="!textOnly" :value="2">{{ $t('图文') }}</el-radio>
                    </el-radio-group>
                </el-form-item>

                <template v-if="local.msgType === 1">
                    <el-form-item :label="$t('内容')" prop="content">
                        <el-input
                            ref="contentRef"
                            v-model="local.content"
                            type="textarea"
                            :rows="6"
                            :maxlength="contentMaxLength"
                            :validate-event="!suppressFieldValidation"
                            :placeholder="$t('请输入文本内容')"
                        />
                        <div class="var-toolbar">
                            <div class="var-toolbar__btns">
                                <span
                                    v-for="item in VARIABLE_TOKENS"
                                    :key="item.token"
                                    class="var-toolbar__btn"
                                    :class="{ 'is-disabled': disabled }"
                                    @mousedown.prevent
                                    @click="insertVariable('content', item.token)"
                                    >{{ item.label }}</span
                                >
                            </div>
                            <div class="var-toolbar__tip">{{ VARIABLE_TIP }}</div>
                        </div>
                    </el-form-item>
                </template>

                <template v-else>
                    <el-form-item :label="$t('图文标题')" prop="newsTitle">
                        <el-input
                            ref="newsTitleRef"
                            v-model="local.newsTitle"
                            :maxlength="64"
                            :validate-event="!suppressFieldValidation"
                            :placeholder="$t('请输入图文标题')"
                        />
                        <div class="var-toolbar">
                            <div class="var-toolbar__btns">
                                <span
                                    v-for="item in VARIABLE_TOKENS"
                                    :key="item.token"
                                    class="var-toolbar__btn"
                                    :class="{ 'is-disabled': disabled }"
                                    @mousedown.prevent
                                    @click="insertVariable('newsTitle', item.token)"
                                    >{{ item.label }}</span
                                >
                            </div>
                            <div class="var-toolbar__tip">{{ VARIABLE_TIP }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('图文摘要')" prop="newsDescription">
                        <el-input
                            ref="newsDescRef"
                            v-model="local.newsDescription"
                            type="textarea"
                            :rows="3"
                            :maxlength="120"
                            :validate-event="!suppressFieldValidation"
                            :placeholder="$t('请输入图文摘要')"
                        />
                        <div class="var-toolbar">
                            <div class="var-toolbar__btns">
                                <span
                                    v-for="item in VARIABLE_TOKENS"
                                    :key="item.token"
                                    class="var-toolbar__btn"
                                    :class="{ 'is-disabled': disabled }"
                                    @mousedown.prevent
                                    @click="insertVariable('newsDescription', item.token)"
                                    >{{ item.label }}</span
                                >
                            </div>
                            <div class="var-toolbar__tip">{{ VARIABLE_TIP }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('封面图')" prop="newsCoverUrl">
                        <FormAddGallery v-model:photo="local.newsCoverUrl" @update:photo="handleCoverChange" />
                        <div class="extra">{{ $t('建议比例 2.35 : 1（如 900 × 383）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('跳转链接')" prop="newsUrl">
                        <el-input v-model="local.newsUrl" :validate-event="!suppressFieldValidation" placeholder="https://..." />
                    </el-form-item>
                </template>
            </el-form>
        </div>

        <div v-if="showPreview" class="preview-col">
            <PhonePreview mode="chat" :messages="previewMessages" :nickname="previewNickname" :shop-name="previewShopName" :shop-logo="shopLogo" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from "vue";
import { FormAddGallery } from "@/components/gallery";
import PhonePreview from "@/components/PhonePreview/index.vue";
import { getConfigBasic } from "@/api/setting/config";
import type { ReplyPayload, PreviewMessage } from "@/types/setting/wechat.d";
import type { FormInstance, FormRules, InputInstance } from "element-plus";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopLogo = ref("");
onMounted(async () => {
    try {
        const cfg = await getConfigBasic();
        shopLogo.value = cfg?.shopLogo ?? "";
    } catch {
        // 获取失败时降级为空，头像显示默认灰色背景
    }
});

interface Props {
    modelValue: ReplyPayload;
    previewNickname?: string;
    previewShopName?: string;
    showPreview?: boolean;
    disabled?: boolean;
    msgTypeLabel?: string;
    // 文本内容最大长度：关键字回复传 300，自动回复默认 600
    contentMaxLength?: number;
    // 仅文字回复：隐藏消息类型切换、强制 msgType=1、不渲染图文分支
    textOnly?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    previewNickname: undefined,
    previewShopName: undefined,
    showPreview: true,
    disabled: false,
    msgTypeLabel: undefined,
    contentMaxLength: 600,
    textOnly: false
});

const emit = defineEmits<{
    (e: "update:modelValue", v: ReplyPayload): void;
    (e: "validate", valid: boolean): void;
}>();

const formRef = shallowRef<FormInstance>();

// 文本类字段支持的变量占位符；token 必须与后端 WxReplyRenderer 约定保持一致
type VariableField = "content" | "newsTitle" | "newsDescription";

const VARIABLE_TOKENS: ReadonlyArray<{ label: string; token: string }> = [
    { label: t('插入昵称'), token: "{nickname}" },
    { label: t('插入商城名称'), token: "{shopName}" },
    { label: t('插入日期'), token: "{date}" }
];

const VARIABLE_TIP = t('已插入的变量将在发送时自动替换为真实内容，请勿改动大括号内字符，否则会被原样发送给用户。');

const contentRef = shallowRef<InputInstance>();
const newsTitleRef = shallowRef<InputInstance>();
const newsDescRef = shallowRef<InputInstance>();
const suppressFieldValidation = ref(false);
let suppressValidationTimer: ReturnType<typeof setTimeout> | null = null;

// 各字段最大长度，用于变量插入时的超限保护，与下方 maxlength 属性保持一致。
// content 由 contentMaxLength prop 决定（关键字回复 300 / 自动回复 600）。
const FIELD_MAX_LEN = computed<Record<VariableField, number>>(() => ({
    content: props.contentMaxLength,
    newsTitle: 64,
    newsDescription: 120
}));

function getInputRef(field: VariableField) {
    if (field === "content") return contentRef;
    if (field === "newsTitle") return newsTitleRef;
    return newsDescRef;
}

// el-input 在多行时通过 textarea 暴露原生 DOM、单行通过 input 暴露
function getNativeInputEl(inst: InputInstance | undefined): HTMLInputElement | HTMLTextAreaElement | null {
    if (!inst) return null;
    const anyInst = inst as unknown as {
        textarea?: HTMLTextAreaElement;
        input?: HTMLInputElement;
    };
    return anyInst.textarea ?? anyInst.input ?? null;
}

async function insertVariable(field: VariableField, token: string) {
    if (props.disabled) return;
    const inst = getInputRef(field).value;
    const el = getNativeInputEl(inst);
    const current = (local.value[field] ?? "") as string;
    const max = FIELD_MAX_LEN.value[field];

    // 没拿到原生 DOM（极端场景）退化为追加到末尾
    let start = current.length;
    let end = current.length;
    if (el) {
        start = el.selectionStart ?? current.length;
        end = el.selectionEnd ?? current.length;
    }

    // 超过 maxlength 时直接放弃，避免与 el-input 的 maxlength 截断冲突
    if (current.length - (end - start) + token.length > max) return;

    const next = current.slice(0, start) + token + current.slice(end);
    local.value[field] = next;

    await nextTick();
    const newEl = getNativeInputEl(getInputRef(field).value);
    if (newEl) {
        newEl.focus();
        const pos = start + token.length;
        newEl.setSelectionRange(pos, pos);
    }
}

function normalize(p: ReplyPayload | undefined | null): ReplyPayload {
    const src = p || ({} as ReplyPayload);
    return {
        msgType: props.textOnly ? 1 : src.msgType === 2 ? 2 : 1,
        content: src.content ?? "",
        newsTitle: src.newsTitle ?? "",
        newsCoverUrl: src.newsCoverUrl ?? "",
        newsDescription: src.newsDescription ?? "",
        newsUrl: src.newsUrl ?? ""
    };
}

const local = ref<ReplyPayload>(normalize(props.modelValue));

function sameShape(a: ReplyPayload, b: ReplyPayload): boolean {
    return JSON.stringify(a) === JSON.stringify(b);
}

watch(
    () => props.modelValue,
    (val) => {
        const next = normalize(val);
        if (sameShape(next, local.value)) return;
        local.value = next;
        // 数据被父组件重置时清掉旧的校验态
        formRef.value?.clearValidate();
    },
    { deep: true }
);

watch(
    local,
    (val) => {
        // 与父组件当前值相同则跳过，避免因 props 同步触发回环 emit
        if (sameShape(val, normalize(props.modelValue))) return;
        emit("update:modelValue", { ...val });
    },
    { deep: true }
);

async function onMsgTypeChange(_val: string | number | boolean | undefined) {
    // 不要在这里重置隐藏分支的字段值。
    // 原因：编辑态下 newsCoverUrl 等是真实值，切换时若被赋成 ""，
    // el-form-item 的内部 value watcher 会按规则里的 trigger: "change"（newsCoverUrl 即是）
    // 立刻触发一次异步 required 校验，导致切换瞬间出现"请上传封面图"等红字。
    // 隐藏分支的数据不会污染提交（getCleanPayload 只保留当前 msgType 的字段），
    // 保留它还能支持用户切回时看到之前填的内容。
    // 切换按钮会让当前输入框先 blur，再触发 radio change。
    // 这里临时抑制字段级校验，并在切换完成后把竞态产生的旧红字清掉。
    beginMsgTypeSwitch();
    formRef.value?.clearValidate();
    await nextTick();
    formRef.value?.clearValidate();
    await nextTick();
    formRef.value?.clearValidate();
}

// FormAddGallery 是自定义组件，不会走 el-form-item 的子组件校验通道，
// 上传 / 清空后需要主动触发 newsCoverUrl 字段的校验，使旧的错误提示能正常消除。
function handleCoverChange() {
    if (suppressFieldValidation.value) return;
    formRef.value?.validateField("newsCoverUrl");
}

function beginMsgTypeSwitch() {
    suppressFieldValidation.value = true;
    if (suppressValidationTimer) {
        clearTimeout(suppressValidationTimer);
    }
    suppressValidationTimer = setTimeout(() => {
        suppressFieldValidation.value = false;
        suppressValidationTimer = null;
    }, 0);
}

onBeforeUnmount(() => {
    if (suppressValidationTimer) {
        clearTimeout(suppressValidationTimer);
    }
});

const rules = computed<FormRules>(() => {
    const contentMax = props.contentMaxLength;
    if (local.value.msgType === 1) {
        return {
            content: [
                { required: true, message: t('请输入文本内容'), trigger: "blur" },
                {
                    max: contentMax,
                    message: t('内容不可超过 {contentMax} 字符', { contentMax }),
                    trigger: "blur"
                }
            ]
        };
    }
    return {
        newsTitle: [
            { required: true, message: t('请输入图文标题'), trigger: "blur" },
            { max: 64, message: t('标题不可超过 64 字符'), trigger: "blur" }
        ],
        newsDescription: [
            { required: true, message: t('请输入图文摘要'), trigger: "blur" },
            { max: 120, message: t('摘要不可超过 120 字符'), trigger: "blur" }
        ],
        newsCoverUrl: [{ required: true, message: t('请上传封面图'), trigger: "change" }],
        newsUrl: [
            { required: true, message: t('请输入跳转链接'), trigger: "blur" },
            {
                pattern: /^https?:\/\//i,
                message: "链接需以 http:// 或 https:// 开头",
                trigger: "blur"
            }
        ]
    };
});

const previewMessages = computed<PreviewMessage[]>(() => [
    { role: "user", payload: { msgType: 1, content: t('你好') } },
    { role: "mp", payload: { ...local.value } }
]);

async function validate(): Promise<boolean> {
    if (!formRef.value) return true;
    try {
        await formRef.value.validate();
        emit("validate", true);
        return true;
    } catch {
        emit("validate", false);
        return false;
    }
}

function clearValidate() {
    formRef.value?.clearValidate();
}

// 仅保留与当前 msgType 相关的字段，避免冗余字段污染后端校验/落库
function getCleanPayload(): ReplyPayload {
    const src = local.value;
    if (src.msgType === 1) {
        return { msgType: 1, content: src.content ?? "" };
    }
    return {
        msgType: 2,
        newsTitle: src.newsTitle ?? "",
        newsCoverUrl: src.newsCoverUrl ?? "",
        newsDescription: src.newsDescription ?? "",
        newsUrl: src.newsUrl ?? ""
    };
}

defineExpose({ validate, clearValidate, getCleanPayload });
</script>

<style lang="less" scoped>
.reply-payload-form {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 320px;
    gap: 24px;
    align-items: flex-start;

    &.no-preview {
        grid-template-columns: minmax(0, 1fr);
    }
}

.form-col {
    min-width: 0;
}

.preview-col {
    position: sticky;
    top: 12px;
}

.extra {
    margin-top: 4px;
    color: #999;
    font-size: 12px;
    line-height: 1.6;

    code {
        margin: 0 2px;
        padding: 0 4px;
        background: #f5f5f5;
        border-radius: 3px;
        color: #d56161;
        font-family: inherit;
        font-size: 12px;
    }
}

.var-toolbar {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 6px;

    &__btns {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 12px;
        line-height: 1.6;
    }

    &__btn {
        color: var(--tig-primary);
        font-size: 12px;
        cursor: pointer;
        user-select: none;
        transition: color 0.2s;

        &:hover {
            color: var(--tig-button-hover-bg);
        }

        &.is-disabled {
            color: #c0c4cc;
            cursor: not-allowed;

            &:hover {
                color: #c0c4cc;
            }
        }
    }

    &__tip {
        color: #999;
        font-size: 12px;
        line-height: 1.6;
    }
}

@media (max-width: 960px) {
    .reply-payload-form {
        grid-template-columns: minmax(0, 1fr);
    }

    .preview-col {
        position: static;
    }
}
</style>
