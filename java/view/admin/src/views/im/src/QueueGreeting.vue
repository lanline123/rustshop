<template>
    <div class="index-m_preferenceSwitch_1TP3q">
        <div class="index-m_header_2mdaa">
            <span class="index-m_title_180SF">{{ $t('客服排队中提醒') }}</span>
            <el-switch class="switch-pl" v-model="formState.activate" />
        </div>
        <div class="index-m_desc_Erwpt">{{ $t('所有客服都不在线或没有接待余量（最大接待客户数-正在接待客户数）时，会发送这个回复') }}</div>
    </div>
    <div class="zent-block-header">
        <div class="zent-block-header__title zent-block-header__title-ribbon"><h3>{{ $t('回复内容') }}</h3></div>
        <div class="zent-block-header__content">{{ $t('(若都勾选则自动回复文字消息)') }}</div>
    </div>
    <div class="info-box">
        <div class="right-box">
            <el-form ref="formRef" :model="formState" label-width="auto">
                <el-form-item label="" prop="brandLogo">
                    <el-checkbox style="width: 100%" v-model="formState.sendText">
                        <span style="font-size: 14px">{{ $t('发送文字消息') }}</span>
                    </el-checkbox>
                    <div style="width: 100%; padding-left: 25px; font-size: 14px">
                        <div style="display: flex; flex-direction: column">
                            <div style="display: flex">
                                <div style="min-width: 70px">{{ $t('回复内容：') }}</div>
                                <div v-if="!editText" v-html="formState.replyContent"></div>
                                <div v-else style="width: 100%">
                                    <TextEditor v-model:html="formStateText.replyContentTemp" height="300px"></TextEditor>
                                </div>
                            </div>
                            <div style="display: flex">
                                <div style="width: 70px"></div>
                                <div v-if="!editText">
                                    <el-button @click="openEdit()" style="font-size: 14px" type="primary" link>{{ $t('编辑') }}</el-button>
                                </div>
                                <div style="margin-top: 14px" v-else>
                                    <el-button @click="saveEdit()" style="font-size: 14px" type="primary">{{ $t('保存') }}</el-button>
                                    <el-button @click="editText = false" style="font-size: 14px">{{ $t('取消') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import TextEditor from "@/views/im/src/TextEditor.vue";
import { message } from "ant-design-vue";
import { getImConfig, saveImConfig } from "@/api/im/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const editText = ref(false);
const formState: any = ref({
    activate: false,
    sendText: false,
    replyContent: t('抱歉，目前客服都在忙呢，请再稍等一会儿')
});
const formStateText: any = ref({
    replyContentTemp: ""
});

const stripHtml = (html: string) => {
    if (!html) return "";
    // 尽量保留简单换行语义
    const normalized = html
        .replace(/<\s*br\s*\/?>/gi, "\n")
        .replace(/<\/\s*p\s*>/gi, "\n")
        .replace(/<\/\s*div\s*>/gi, "\n");
    const doc = new DOMParser().parseFromString(normalized, "text/html");
    return (doc.body.textContent || "").replace(/\u00a0/g, " ").trim();
};

const openEdit = () => {
    const userInfo = JSON.parse(localStorage.getItem("user") || "{}")?.userInfo;
    if (userInfo?.adminId === 2) return message.error(t('权限不足'));
    formStateText.value.replyContentTemp = formState.value.replyContent;
    editText.value = true;
};
const saveEdit = () => {
    // 保存到表单时即去掉富文本标签，避免最终落库带 HTML
    formState.value.replyContent = stripHtml(formStateText.value.replyContentTemp);
    editText.value = false;
};

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        const payload = {
            ...formState.value,
            replyContent: stripHtml(formState.value.replyContent)
        };
        const result = await saveImConfig({ code: "waiting", data: payload });
    } catch (error: any) {
        message.error(error.message);
    }
};

const isFetching = ref(false);
// 在数据变化时调用的方法
const handleFormStateChange = () => {
    if (isFetching.value) {
        // 如果不是因为fetchData引起的变更
        onSubmit(); // 调用onSubmit方法
    }
    if (!isFetching.value) {
        isFetching.value = true;
    }
};
// 使用watch监听formState的变化
watch(
    () => formState.value,
    () => {
        handleFormStateChange();
    },
    { deep: true }
);

const fetchData = async () => {
    try {
        const result = await getImConfig({ code: "waiting" });
        if (result) {
            Object.assign(formState.value, result);
        } else {
            isFetching.value = true;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
onMounted(() => {
    fetchData();
});
</script>
<style scoped lang="less">
.index-m_preferenceSwitch_1TP3q {
    padding: 16px 20px;
    background-color: #f5f7fa;
    border: 1px solid #f0f2f5;
    border-radius: 4px;
    margin-bottom: 16px;

    .index-m_header_2mdaa {
        display: flex;
        align-items: center;

        .index-m_title_180SF {
            font-size: 16px;
            line-height: 20px;
            font-weight: 500;
        }

        .switch-pl {
            margin-left: 10px;
        }
    }

    .index-m_desc_Erwpt {
        padding-top: 10px;
        font-size: 14px;
        color: #999;
        line-height: 21px;
    }
}

.zent-block-header {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-bottom: 16px;
    padding: 10px 12px;
    min-height: 40px;
    box-sizing: border-box;
    background-color: #f5f7fa;
    border-radius: 4px;

    .zent-block-header__title-ribbon:before {
        background-color: var(--tig-primary);
        content: "";
        width: 3px;
        height: 14px;
        margin-right: 8px;
    }

    .zent-block-header__title {
        display: flex;
        align-items: center;

        h3 {
            margin: 0;
            font-size: 14px;
            font-weight: 600;
        }
    }

    .zent-block-header__content {
        margin-left: 8px;
        display: flex;
        align-items: center;
        color: #999;
        font-size: 13px;
    }
}

.info-box {
    .right-box {
        padding-left: 4px;
    }
}

.extra {
    width: 100%;
    padding-left: 25px;
    margin-top: 4px;
    color: #999;
    font-size: 12px;
    line-height: 1.5;
}
</style>
