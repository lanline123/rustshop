<script setup lang="ts">
import { defineAsyncComponent, ref, computed } from "vue";
import type { TabsPaneContext } from "element-plus";
import ErrorComponent from "@/components/ErrorComponent.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabList = [
    {
        name: 'buyerShowListings',
        label: t('买家秀列表'),
    },
    {
        name: 'buyerShowComments',
        label: t('评论管理'),
    },
    {
        name: 'buyerShowGroup',
        label: t('分组管理'),
    },
    {
        name: 'buyerShowSetting',
        label: t('功能设置'),
    }
];

const activeName = ref('buyerShowListings');

// 使用计算属性动态生成组件
const AsyncComp = computed(() => {
    console.log(activeName.value);
    return defineAsyncComponent({
        loader: () => import(`./${activeName.value}/List.vue`),
        errorComponent: ErrorComponent,
        timeout: 3000,
    });
});

const handleClick = (tab: TabsPaneContext, event: Event) => {

};

// 为组件添加唯一的key，确保切换时正确重新渲染
const componentKey = computed(() => `${activeName.value}-${Date.now()}`);
</script>

<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
                <el-tab-pane v-for="item in tabList" :key="item.name" :label="item.label" :name="item.name">
                    <component
                        :is="AsyncComp"
                        :key="componentKey"
                        v-if="activeName === item.name"
                    />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>
