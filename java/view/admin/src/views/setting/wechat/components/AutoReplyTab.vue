<template>
    <div class="auto-reply-tab">
        <div class="hint">{{ hint }}</div>

        <a-spin :spinning="loading">
            <template v-if="!loading">
                <ReplyPayloadForm ref="formRef" v-model="payload" :text-only="textOnly" />
            </template>
            <div v-else class="loading-placeholder" />
        </a-spin>

        <div style="height: 20px"></div>
        <div
            class="selected-action-warp selected-warp-left"
            :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }"
        >
            <div class="selected-action">
                <el-button
                    :loading="saving"
                    :disabled="loading"
                    class="form-submit-btn"
                    size="large"
                    type="primary"
                    @click="onSave"
                > {{ $t('保 存') }} </el-button>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import ReplyPayloadForm from "./ReplyPayloadForm.vue";
import { useThemeStore } from "@/store/theme";
import type { ReplyPayload } from "@/types/setting/wechat.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();

interface Props {
    hint: string;
    fetcher: () => Promise<ReplyPayload | null | undefined>;
    saver: (payload: ReplyPayload) => Promise<unknown>;
    // 仅文字回复：透传给表单，隐藏图文分支
    textOnly?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    textOnly: false
});

const loading = ref<boolean>(true);
const saving = ref<boolean>(false);
const formRef = shallowRef<InstanceType<typeof ReplyPayloadForm> | null>(null);

const payload = ref<ReplyPayload>({
    msgType: 1,
    content: "",
    newsTitle: "",
    newsCoverUrl: "",
    newsDescription: "",
    newsUrl: ""
});

async function fetchData() {
    loading.value = true;
    try {
        const result = await props.fetcher();
        if (result) {
            payload.value = {
                msgType: props.textOnly ? 1 : result.msgType === 2 ? 2 : 1,
                content: result.content ?? "",
                newsTitle: result.newsTitle ?? "",
                newsCoverUrl: result.newsCoverUrl ?? "",
                newsDescription: result.newsDescription ?? "",
                newsUrl: result.newsUrl ?? ""
            };
        }
    } catch (error: any) {
        message.error(error?.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
}

async function onSave() {
    const valid = await formRef.value?.validate();
    if (!valid) return;
    saving.value = true;
    try {
        // 提交前剪枝：仅保留与当前 msgType 匹配的字段，避免 bizVal JSON 残留另一组数据
        const clean = formRef.value?.getCleanPayload() ?? payload.value;
        await props.saver(clean);
        message.success(t('保存成功'));
    } catch (error: any) {
        message.error(error?.message || t('保存失败'));
    } finally {
        saving.value = false;
    }
}

onMounted(fetchData);
</script>

<style lang="less" scoped>
.auto-reply-tab {
    padding: 16px 4px;
}

.hint {
    margin-bottom: 16px;
    color: #666;
    font-size: 13px;
    line-height: 1.6;
}

.loading-placeholder {
    min-height: 240px;
}
</style>
