<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品提示</h3>
            <div class="dec-edit-desc">添加店铺的多个商品提示，支持自定义图标、标题和链接</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">基本设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-desc">
                    <div class="pic-change-desc">提示：您可以通过拖拽进行排序，最多添加4个</div>
                </div>
                <div class="dec-edit-group-con" :class="{ 'hide-add-btn': module.picList && module.picList.length >= 4 }">
                    <PicList
                        :isMultiple="true"
                        v-model:photos="module.picList"
                        title="添加提示"
                        decorateType="mobile"
                    ></PicList>
                </div>
            </div>

            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">每行显示</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="1">1个</el-radio-button>
                        <el-radio-button :value="2">2个</el-radio-button>
                        <el-radio-button :value="3">3个</el-radio-button>
                        <el-radio-button :value="4">4个</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">提示间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.itemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">间隔竖线</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showDivider">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">隐藏</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题颜色</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.titleColor = '#333333'">重置</a>
                            <SelectColor v-model:color="module.titleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">提示背景色</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.itemBackgroundColor = ''">重置</a>
                            <SelectColor v-model:color="module.itemBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="shopTips"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { PicList } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed, watch } from "vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";

import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ShopTipsModule } from "./types";

const tabs = ref([
    { label: "提示设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ShopTipsModule);

const MAX_TIPS_COUNT = 4;
watch(
    () => module.value.picList,
    (list) => {
        if (list && list.length > MAX_TIPS_COUNT) {
            module.value.picList = list.slice(0, MAX_TIPS_COUNT);
        }
    },
    { deep: true }
);
</script>
<style lang="less" scoped>
.hide-add-btn {
    :deep(.dec-pic-add-con) {
        display: none;
    }
}
</style>
