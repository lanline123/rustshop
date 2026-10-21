<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeTab" class="merchant-application-tabs">
                <el-tab-pane :label="$t('进件管理')" name="manage">
                    <ManageList v-if="loadedTabs.has('manage')" />
                </el-tab-pane>
                <el-tab-pane :label="$t('进件审核')" name="audit">
                    <Audit v-if="loadedTabs.has('audit')" />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from "vue";
import ManageList from "./ManageList.vue";
import Audit from "./Audit.vue";

const activeTab = ref("manage");
const loadedTabs = reactive(new Set<string>(["manage"]));

watch(
    activeTab,
    (tab) => {
        loadedTabs.add(tab);
    },
    { immediate: true }
);
</script>

<style lang="less" scoped>
.merchant-application-tabs {
    :deep(.el-tabs__header) {
        margin-bottom: 12px;
    }
}
</style>
