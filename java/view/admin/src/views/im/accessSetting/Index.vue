/**
 * @file 接入规则设置页
 * @description 配置客服会话的接入/派单规则
 * @features
 * - 选择接入方式（全部自动接入 / 全部手动接入）
 * - 回头客接入（优先复用上次接待客服）
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form
                ref="formRef"
                :model="formState"
                label-position="left"
                label-width="100px"
                style="display: flex; gap: 12px; flex-direction: column"
            >
                <div class="content_wrapper">
                    <div class="title">接入规则</div>
                    <el-form-item label="选择接入方式" prop="autoAccessMode">
                        <div class="access-mode-row">
                            <el-radio-group v-model="formState.autoAccessMode">
                                <el-radio :value="0">全部自动接入</el-radio>
                                <el-radio :value="1">全部手动接入</el-radio>
                            </el-radio-group>
                            <div class="extra" v-if="formState.autoAccessMode === 0">客户发起会话时，自动分配给在线客服中接待数最少的客服</div>
                            <div class="extra" v-else>客户发起会话进入待接入列表，由客服在工作台手动接入</div>
                        </div>
                    </el-form-item>
                    <el-form-item label="回头客接入" prop="returnCustomerAccess">
                        <div class="flex flex-align-center" style="gap: 12px">
                            <el-switch v-model="formState.returnCustomerAccess" :active-value="1" :inactive-value="0" />
                            <span class="extra" style="margin: 0">
                                {{ formState.returnCustomerAccess ? "已开启，上次接待客服在线时优先接入给上次接待客服" : "已关闭" }}
                            </span>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">提 交</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { useThemeStore } from "@/store/theme";
import type { KefuAccessConfig } from "@/types/im/kefu";
import { getKefuAccessConfig, saveKefuAccessConfig } from "@/api/im/shopKefuConfig";

const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const loading = ref<boolean>(true);
const confirmLoading = ref<boolean>(false);
const formState = ref<KefuAccessConfig>({
    autoAccessMode: 0,
    returnCustomerAccess: 0
});

onMounted(async () => {
    await loadFilter();
});

const loadFilter = async () => {
    try {
        const result = await getKefuAccessConfig();
        Object.assign(formState.value, result || {});
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        await saveKefuAccessConfig({
            autoAccessMode: formState.value.autoAccessMode,
            returnCustomerAccess: formState.value.returnCustomerAccess ? 1 : 0
        });
        message.success("修改成功");
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.extra {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
}

.access-mode-row {
    display: flex;
    align-items: center;
    gap: 12px;

    .extra {
        margin-top: 0;
    }
}

.content_wrapper :deep(.el-form-item) {
    margin-left: 30px;
}
</style>
