<template>
    <div class="theme-toolbar">
        <div class="theme-menu-con show">
            <div class="theme-con-box">
                <el-tabs v-model="activeName" class="demo-tabs">
                    <el-tab-pane label="基础模块" name="toolList"></el-tab-pane>
                </el-tabs>
                <div class="inside">
                    <el-collapse v-model="activeNames">
                        <el-collapse-item title="搜索结果模块" name="tool" v-if="activeName === 'toolList'">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>搜索结果模块</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="toolList"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                :sort="false"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="clone"
                            >
                                <template #item="{ element, index }">
                                    <div class="list-item tool-list-item" @click="onAdd(index, 'toolList')" v-if="element.isShow">
                                        <span v-if="element.content" class="pic" v-html="element.content"> </span>
                                        <span v-if="element.label" class="content">{{ element.label }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                    </el-collapse>
                    <div style="height: 50px"></div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import draggable from "vuedraggable";
import { cloneDeep } from "lodash";
import { pcSearchResultDefaults } from "./modules/pcSearch/pcSearchResult/types";

const activeNames = ref(["tool"]);
const props = defineProps({
    modules: {
        type: Object,
        default: () => ({})
    }
});
const activeName = ref("toolList");

const modules = ref(props.modules);

// 搜索结果模块图标：内联 SVG（放大镜 + 网格），跟随 currentColor
const searchResultIcon = `
<svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
    <rect x="3" y="3" width="7" height="7" rx="1"/>
    <rect x="14" y="3" width="7" height="7" rx="1"/>
    <rect x="3" y="14" width="7" height="7" rx="1"/>
    <circle cx="17.5" cy="17.5" r="3"/>
    <line x1="20" y1="20" x2="21.5" y2="21.5"/>
</svg>
`;

const toolList = ref([
    {
        type: "pcSearchResult",
        url: "pcSearch",
        label: "搜索结果",
        content: searchResultIcon,
        module: { ...pcSearchResultDefaults },
        isShow: true
    }
]);

const clone = (obj: any) => {
    const newObj = cloneDeep(obj);
    newObj.moduleIndex = Date.now();
    delete newObj.content;
    return newObj;
};

const emit = defineEmits(["item-added"]);
const onAdd = (index: number, type = "toolList") => {
    const listMap = {
        toolList: toolList.value
    };
    const selectedList = listMap[type as "toolList"];
    if (selectedList && selectedList[index]) {
        const addItem = cloneDeep(selectedList[index]) as any;
        addItem.moduleIndex = Date.now();
        delete addItem.content;
        modules.value.moduleList.push(addItem);
        emit("item-added", { newIndex: modules.value.moduleList.length - 1 });
    } else {
        throw new Error("无效的类型或索引");
    }
};
</script>
<style lang="scss" scoped>
.theme-menu-con {
    width: 295px !important;
}
.theme-con-box {
    .inside {
        padding-right: 8px;
    }
    :deep(.el-tabs__item) {
        padding: 0;
        margin: 5px 20px;
    }
    :deep(.el-collapse) {
        border: none !important;
    }
    :deep(.el-collapse-item__header) {
        border: none !important;
    }
    :deep(.el-collapse-item__wrap) {
        border: none !important;
    }
    :deep(.el-collapse-item__content) {
        padding-bottom: 10px;
        line-height: 0.8;
    }
    :deep(.el-collapse-icon-position-right .el-collapse-item__header) {
        padding-right: 0 !important;
    }
}
/* SVG 走 currentColor；悬浮时背景为主题色，图标需变白（对齐全局 .pic i:hover） */
.list-item.tool-list-item {
    .pic {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #7d7e80;
    }
    &:hover .pic {
        color: #fff;
    }
}
</style>
