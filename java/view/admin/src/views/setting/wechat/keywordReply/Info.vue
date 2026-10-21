<template>
    <div :class="['keyword-rule-info', { 'keyword-rule-info--dialog': props.isDialog }]" v-loading="loading">
        <el-breadcrumb v-if="!props.isDialog" separator=">" class="crumb">
            <el-breadcrumb-item>{{ $t('关键字回复') }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ isEdit ? $t('编辑回复') : $t('添加回复') }}</el-breadcrumb-item>
        </el-breadcrumb>

        <el-form v-if="!loading" ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item :label="$t('规则名称')" prop="ruleName">
                <el-input v-model="form.ruleName" maxlength="60" :placeholder="$t('请输入规则名称')" class="rule-name-input" />
                <div class="extra">{{ $t('规则名最多60个字') }}</div>
            </el-form-item>

            <el-form-item :label="$t('关键字')" required>
                <KeywordList ref="keywordListRef" v-model="form.keywords" />
            </el-form-item>

            <el-form-item :label="$t('回复内容')" required>
                <ReplyList ref="replyListRef" v-model="form.replies" />
            </el-form-item>

            <el-form-item :label="$t('回复方式')">
                <el-radio-group v-model="form.replyMode">
                    <el-radio :value="1">{{ $t('随机回复一条') }}</el-radio>
                    <el-radio :value="2">{{ $t('回复全部') }}</el-radio>
                </el-radio-group>
                <div v-if="showKefuDowngradeTip" class="downgrade-tip">{{ $t('当前公众号未检测到客服消息能力，该方式将降级为仅发送首条') }}</div>
            </el-form-item>

            <el-form-item :label="$t('是否启用')">
                <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" inline-prompt />
            </el-form-item>

            <el-form-item v-if="!props.isDialog">
                <el-button type="primary" :loading="submitting" @click="onSubmit">{{ $t('保存') }}</el-button>
                <el-button @click="onCancel">{{ $t('取消') }}</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, shallowRef } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { getKefuCapability, getKeywordRule, saveKeywordRule } from "@/api/setting/wechat";
import type { KeywordRuleKeyword, KeywordRuleSaveDTO, ReplyPayload } from "@/types/setting/wechat.d";
import KeywordList from "./components/KeywordList.vue";
import ReplyList from "./components/ReplyList.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const LIST_PATH = "/setting/wechat/keyword-reply/list";

const props = defineProps({
    /** 规则 id（编辑时必填；抽屉模式由父组件传入） */
    id: { type: Number, default: 0 },
    /** 动作：add / detail；抽屉模式由父组件传入 */
    act: { type: String, default: "add" },
    /** 是否作为抽屉/弹窗子组件使用；true 时由外层 DialogForm 驱动提交与关闭 */
    isDialog: { type: Boolean, default: false }
});

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const route = useRoute();
const router = useRouter();

const act = computed(() => (props.isDialog ? props.act : String(route.query.act ?? "add")));
const id = computed(() => {
    if (props.isDialog) return props.id || undefined;
    return route.query.id ? Number(route.query.id) : undefined;
});
const isEdit = computed(() => act.value === "detail" && !!id.value);

const formRef = shallowRef<FormInstance>();
const keywordListRef = ref<InstanceType<typeof KeywordList>>();
const replyListRef = ref<InstanceType<typeof ReplyList>>();

interface KeywordRuleFormState {
    id?: number;
    ruleName: string;
    replyMode: 1 | 2;
    enabled: 0 | 1;
    keywords: KeywordRuleKeyword[];
    replies: ReplyPayload[];
}

const form = reactive<KeywordRuleFormState>({
    id: undefined,
    ruleName: "",
    replyMode: 1,
    enabled: 1,
    keywords: [{ keyword: "", matchType: 1 }],
    replies: [{ msgType: 1, content: "" }]
});

const rules: FormRules = {
    ruleName: [
        { required: true, message: t('请输入规则名称'), trigger: "blur" },
        { max: 60, message: t('规则名最多 60 个字'), trigger: "blur" }
    ]
};

const loading = ref<boolean>(false);
const submitting = ref<boolean>(false);

const kefuEnabled = ref<boolean>(true);
const showKefuDowngradeTip = computed(() => !kefuEnabled.value && form.replyMode === 2);

async function loadCapability() {
    try {
        const res = await getKefuCapability();
        kefuEnabled.value = !!res?.kefuEnabled;
    } catch {
        kefuEnabled.value = true;
    }
}

async function loadDetail() {
    if (!isEdit.value || !id.value) return;
    loading.value = true;
    try {
        const detail = await getKeywordRule(id.value);
        form.id = detail.id;
        form.ruleName = detail.ruleName ?? "";
        form.replyMode = detail.replyMode === 2 ? 2 : 1;
        form.enabled = detail.enabled === 0 ? 0 : 1;
        form.keywords = (detail.keywords ?? []).map((k) => ({
            keyword: k.keyword ?? "",
            matchType: k.matchType === 2 ? 2 : 1
        }));
        form.replies = (detail.replies ?? []).map((r) => ({ ...r }));
        if (form.keywords.length === 0) {
            form.keywords = [{ keyword: "", matchType: 1 }];
        }
        if (form.replies.length === 0) {
            form.replies = [{ msgType: 1, content: "" }];
        }
    } catch (error: any) {
        ElMessage.error(error?.message ?? t('加载规则详情失败'));
        onCancel();
    } finally {
        loading.value = false;
    }
}

async function onSubmit() {
    if (!formRef.value) return;

    // 全量触发各段校验，使错误在对应组件位置一并标红；再按优先级汇报第一条错误
    const ruleNameValid = await formRef.value
        .validate()
        .then(() => true)
        .catch(() => false);
    const kw = keywordListRef.value?.validate();
    const rp = await replyListRef.value?.validate();

    if (!ruleNameValid || !kw?.ok || !rp?.ok) {
        if (!ruleNameValid) {
            ElMessage.error(t('请完善规则名称'));
        } else if (!kw?.ok) {
            ElMessage.error(kw?.errors?.find(Boolean) ?? t('关键字配置不合法'));
        } else if (!rp?.ok) {
            const idx = (rp?.firstErrorIndex ?? 0) + 1;
            ElMessage.error(t('回复 {idx} 配置不合法', { idx }));
        }
        return;
    }

    const payloads = replyListRef.value?.getCleanPayloads() ?? form.replies;
    const dto: KeywordRuleSaveDTO = {
        id: form.id,
        ruleName: form.ruleName.trim(),
        replyMode: form.replyMode,
        enabled: form.enabled,
        keywords: form.keywords.map((k) => ({
            keyword: (k.keyword ?? "").trim(),
            matchType: k.matchType
        })),
        replies: payloads
    };

    submitting.value = true;
    if (props.isDialog) emit("update:confirmLoading", true);
    try {
        const result = await saveKeywordRule(dto);
        ElMessage.success(t('操作成功'));
        if (props.isDialog) {
            emit("submitCallback", result);
        } else {
            onCancel();
        }
    } catch (error: any) {
        ElMessage.error(error?.message ?? t('操作失败'));
    } finally {
        submitting.value = false;
        if (props.isDialog) emit("update:confirmLoading", false);
    }
}

function onCancel() {
    if (props.isDialog) {
        emit("close");
        return;
    }
    router.push({ path: LIST_PATH });
}

// 供 DialogForm 点击「确认」时调用
function onFormSubmit() {
    onSubmit();
}

defineExpose({ onFormSubmit });

onMounted(() => {
    loadCapability();
    loadDetail();
});
</script>

<style lang="less" scoped>
.keyword-rule-info {
    &:not(.keyword-rule-info--dialog) {
        padding: 16px;
    }

    .rule-name-input {
        max-width: 953px;
    }

    .crumb {
        margin-bottom: 16px;
    }

    .downgrade-tip {
        color: var(--el-color-warning);
        font-size: 12px;
        line-height: 1.6;
        margin-top: 6px;
    }
}
</style>
