<template>
    <div class="node-form">
        <div v-if="!node" class="empty">
            <el-empty :description="$t('你未添加自定义菜单，点击左侧添加菜单为公众号创建菜单栏')" :image-size="80" />
        </div>

        <template v-else>
            <div class="form-title">
                <span>{{ level === 1 ? $t('菜单信息') : $t('子菜单信息') }}</span>
                <span v-if="ruleDisabled" class="rule-disabled">{{ $t('已添加子菜单，仅可设置菜单名称') }}</span>
            </div>

            <!-- 主表单：禁止嵌套子 el-form（发送消息区单独一块，避免 label 列错位） -->
            <el-form ref="formRef" :model="local" label-width="100px" @submit.prevent>
                <el-form-item :label="$t('菜单名称')" prop="name" :error="nameError ?? undefined">
                    <el-input v-model="local.name" :maxlength="20" :placeholder="$t('仅支持中英文和数字')" @input="onNameInput" />
                    <div class="extra">{{ nameExtraText }}</div>
                </el-form-item>

                <template v-if="!ruleDisabled">
                    <el-form-item :label="$t('消息类型')" prop="rule.type">
                        <el-radio-group :model-value="ruleType" @change="onRuleTypeChange">
                            <el-radio value="click">{{ $t('发送消息') }}</el-radio>
                            <el-radio value="view">{{ $t('跳转网页') }}</el-radio>
                            <el-radio value="miniprogram">{{ $t('跳转小程序') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <template v-if="ruleType === 'view'">
                        <el-form-item :label="$t('跳转链接')" prop="rule.url" :error="ruleErrorOf('url')">
                            <el-input v-model="local.rule!.url" :maxlength="1024" placeholder="https://..." @input="onRuleInput" />
                        </el-form-item>
                    </template>

                    <template v-else-if="ruleType === 'miniprogram'">
                        <el-form-item :label="$t('小程序 AppId')" prop="rule.appid" :error="ruleErrorOf('appid')">
                            <el-input v-model="local.rule!.appid" :placeholder="$t('请输入小程序 AppId')" @input="onRuleInput" />
                        </el-form-item>
                        <el-form-item :label="$t('小程序路径')" prop="rule.pagepath" :error="ruleErrorOf('pagepath')">
                            <el-input v-model="local.rule!.pagepath" :placeholder="$t('请输入小程序路径')" @input="onRuleInput" />
                        </el-form-item>
                        <el-form-item :label="$t('备用网页')" prop="rule.miniprogramUrl" :error="ruleErrorOf('miniprogramUrl')">
                            <el-input v-model="local.rule!.miniprogramUrl" :placeholder="$t('请输入备用网页链接')" @input="onRuleInput" />
                            <div class="extra">{{ $t('老版本微信不支持小程序时跳转的 http(s) 链接') }}</div>
                        </el-form-item>
                    </template>
                </template>
            </el-form>

            <div v-if="!ruleDisabled && ruleType === 'click'" class="menu-reply-block">
                <ReplyPayloadForm ref="replyPayloadRef" v-model="menuReplyModel" :show-preview="false" msg-type-label="菜单内容" />
            </div>

            <div class="menu-form-footer">
                <el-button type="primary" link @click="onDeleteClick">{{ $t('删除菜单') }}</el-button>
            </div>
        </template>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, shallowRef, watch } from "vue";
import { ElButton, ElEmpty, ElForm, ElFormItem, ElInput, ElRadio, ElRadioGroup } from "element-plus";
import type { FormInstance } from "element-plus";
import type { ReplyPayload, WxMenuNode, WxMenuRule } from "@/types/setting/wechat.d";
import ReplyPayloadForm from "@/views/setting/wechat/components/ReplyPayloadForm.vue";
import { migrateMenuClickRule, validateName, validateRule, validateRuleErrors, type RuleError } from "../composables/useMenuValidators";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface Props {
    node: WxMenuNode | null;
    level: 1 | 2;
}

const props = defineProps<Props>();

const emit = defineEmits<{
    (e: "update:node", node: WxMenuNode): void;
    (e: "delete"): void;
}>();

function onDeleteClick() {
    emit("delete");
}

const formRef = shallowRef<FormInstance>();
const replyPayloadRef = shallowRef<InstanceType<typeof ReplyPayloadForm> | null>(null);

function emptyRule(): WxMenuRule {
    // eventKey 留空，由后端 saveAndPublish 时统一分配并写入 menu_json
    return migrateMenuClickRule({ type: "click", msgType: 1, content: "" })!;
}

function normalize(node: WxMenuNode | null): WxMenuNode {
    if (!node) {
        return { id: "", name: "", sortOrder: 0, rule: emptyRule() };
    }
    const rawRule = node.rule ? { ...node.rule } : node.children?.length ? undefined : emptyRule();
    const rule = rawRule ? (migrateMenuClickRule(rawRule) ?? rawRule) : undefined;
    return {
        id: node.id,
        name: node.name ?? "",
        sortOrder: node.sortOrder ?? 0,
        children: node.children ? [...node.children] : undefined,
        rule
    };
}

const local = ref<WxMenuNode>(normalize(props.node));

const ruleDisabled = computed(() => props.level === 1 && Array.isArray(local.value.children) && local.value.children.length > 0);

const ruleType = computed<WxMenuRule["type"]>(() => local.value.rule?.type ?? "click");

const nameExtraText = computed(() =>
    props.level === 1 ? t('仅支持中英文和数字，字数不超过 4 个汉字或 8 个字母') : t('仅支持中英文和数字，字数不超过 8 个汉字或 16 个字母')
);

const nameError = ref<string | null>(null);
type RuleField = RuleError["field"];
type RuleErrorMap = Partial<Record<RuleField, string>>;
const ruleErrors = ref<RuleErrorMap>({});

const menuReplyModel = computed<ReplyPayload>({
    get() {
        const r = local.value.rule;
        if (!r || r.type !== "click") {
            return { msgType: 1, content: "" };
        }
        return {
            msgType: r.msgType === 2 ? 2 : 1,
            content: r.content ?? "",
            newsTitle: r.newsTitle ?? "",
            newsCoverUrl: r.newsCoverUrl ?? "",
            newsDescription: r.newsDescription ?? "",
            newsUrl: r.newsUrl ?? ""
        };
    },
    set(v) {
        const r = local.value.rule;
        if (!r || r.type !== "click") return;
        r.msgType = v.msgType;
        if (v.msgType === 1) {
            r.content = v.content ?? "";
            delete r.newsTitle;
            delete r.newsCoverUrl;
            delete r.newsDescription;
            delete r.newsUrl;
        } else {
            r.newsTitle = v.newsTitle ?? "";
            r.newsCoverUrl = v.newsCoverUrl ?? "";
            r.newsDescription = v.newsDescription ?? "";
            r.newsUrl = v.newsUrl ?? "";
            delete r.content;
        }
        // 文本/图文校验由 ReplyPayloadForm 在字段下方展示，不在主表单重复提示
        ruleErrors.value = {};
        emitUpdate();
    }
});

function ruleErrorOf(field: RuleError["field"]): string | undefined {
    return ruleErrors.value[field];
}

function setRuleErrors(errors: RuleError[]) {
    const next: RuleErrorMap = {};
    errors.forEach((error) => {
        if (!next[error.field]) {
            next[error.field] = error.message;
        }
    });
    ruleErrors.value = next;
}

function refreshErrors() {
    nameError.value = validateName(local.value.name ?? "", props.level);
    if (ruleDisabled.value) {
        ruleErrors.value = {};
        return;
    }
    // click 的发送内容由 ReplyPayloadForm 自带校验提示，避免红字出现在「事件 Key」与发送区之间
    if (local.value.rule?.type === "click") {
        ruleErrors.value = {};
        return;
    }
    setRuleErrors(validateRuleErrors(local.value.rule));
}

function syncFromProps() {
    local.value = normalize(props.node);
    refreshErrors();
}

watch(() => props.node?.id, syncFromProps);

watch(
    () => props.node,
    (val) => {
        // 同 id 但内部字段被父组件刷新（如 loadFromVO）时也同步
        if (!val) {
            syncFromProps();
            return;
        }
        if (val.id !== local.value.id) return;
        // 同 id 的对象引用变化时按需更新
        const next = normalize(val);
        if (JSON.stringify(next) !== JSON.stringify(local.value)) {
            local.value = next;
            refreshErrors();
        }
    },
    { deep: true }
);

watch(() => props.level, refreshErrors);

function emitUpdate() {
    emit("update:node", JSON.parse(JSON.stringify(local.value)));
}

function onNameInput() {
    nameError.value = validateName(local.value.name ?? "", props.level);
    emitUpdate();
}

function onRuleInput() {
    setRuleErrors(validateRuleErrors(local.value.rule));
    emitUpdate();
}

function onRuleTypeChange(val: string | number | boolean | undefined) {
    const next = (val as WxMenuRule["type"]) ?? "click";
    if (next === "click") {
        // eventKey 不在前端生成；仅保留已有值（编辑态回显），新建时留空由后端分配
        const prevKey = local.value.rule?.type === "click" ? local.value.rule.eventKey : undefined;
        local.value.rule = migrateMenuClickRule({
            type: "click",
            eventKey: prevKey,
            msgType: 1,
            content: ""
        })!;
    } else if (next === "view") {
        local.value.rule = { type: "view", url: "" };
    } else {
        local.value.rule = { type: "miniprogram", appid: "", pagepath: "", miniprogramUrl: "" };
    }
    // 切换响应类型本身不应立即触发目标分支的必填校验；
    // 仅清掉旧错误态，真正校验仍在输入过程中和保存时执行。
    ruleErrors.value = {};
    replyPayloadRef.value?.clearValidate();
    emitUpdate();
}

/**
 * 暴露给父组件做最终校验。返回 null 表示通过；否则返回首条错误。
 */
async function validate(): Promise<string | null> {
    // 未选中任何节点时 local 只是一份空白占位，不参与表单级校验，交给 validateTree 兜底
    if (!props.node) return null;
    refreshErrors();
    if (nameError.value) return nameError.value;
    // 一级菜单已挂子菜单时，rule 会被清空且不参与提交校验。
    if (ruleDisabled.value) return null;
    if (ruleType.value === "click" && replyPayloadRef.value) {
        const ok = await replyPayloadRef.value.validate();
        if (!ok) {
            // 统一一份错误文案来源：失败时借 validateRule 给出具体字段级消息
            // （ReplyPayloadForm 自身也会在对应表单项下方展示红字）
            const detail = validateRule(local.value.rule);
            return detail?.message ?? t('请完善发送消息内容');
        }
        return null;
    }
    const firstRuleError = validateRule(local.value.rule);
    if (firstRuleError) {
        setRuleErrors(validateRuleErrors(local.value.rule));
        return firstRuleError.message;
    }
    return null;
}

function clearValidate() {
    nameError.value = null;
    ruleErrors.value = {};
    formRef.value?.clearValidate();
    replyPayloadRef.value?.clearValidate();
}

defineExpose({ validate, clearValidate });
</script>

<style lang="less" scoped>
.node-form {
    background: #fff;
    border-radius: 4px;
}

.form-title {
    display: flex;
    align-items: baseline;
    gap: 12px;
    font-size: 16px;
    font-weight: 600;
    color: #222;
    margin-bottom: 12px;
    padding-bottom: 8px;
}

.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
}

.extra {
    margin-top: 4px;
    color: #999;
    font-size: 12px;
    line-height: 1.6;
}

.rule-disabled {
    color: #909399;
    font-size: 13px;
    font-weight: normal;
    line-height: 1.6;
}

/* 与主表单并列、同宽，避免嵌套 el-form 导致 label 列与输入区整体偏移 */
.menu-reply-block {
    margin-top: 4px;
}

.menu-reply-block :deep(.reply-payload-form) {
    grid-template-columns: minmax(0, 1fr);
}

.menu-reply-block :deep(.reply-payload-form .form-col) {
    min-width: 0;
}

.menu-form-footer {
    margin-top: 8px;
    width: 100px;
    display: flex;
    justify-content: flex-end;
    padding: 0 12px 0 0;
    box-sizing: border-box;
}

.menu-form-footer .el-button {
    padding-left: 0;
    padding-right: 0;
}
</style>
