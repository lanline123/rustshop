<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品提示') }}</h3>
            <div class="dec-edit-desc">{{ $t('添加店铺的多个商品提示，支持自定义图标、标题和链接') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('基本设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-desc">
                    <div class="pic-change-desc">{{ $t('提示：您可以通过拖拽进行排序，最多添加4个') }}</div>
                </div>
                <div class="dec-edit-group-con" :class="{ 'hide-add-btn': module.picList && module.picList.length >= 4 }">
                    <PicList
                        :isMultiple="true"
                        v-model:photos="module.picList"
                        :title="$t('添加提示')"
                        decorateType="mobile"
                    ></PicList>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每行显示') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="1">{{ $t('1个') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('2个') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('3个') }}</el-radio-button>
                        <el-radio-button :value="4">{{ $t('4个') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('提示间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.itemGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('间隔竖线') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showDivider">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('隐藏') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('标题颜色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.titleColor = '#333333'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.titleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('提示背景色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.itemBackgroundColor = ''">{{ $t('重置') }}</a>
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
import { ref, watch } from "vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";

import { ModuleType } from "@/types/decorate/decorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref([
    { label: t('提示设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const module = defineModel<ModuleType & Record<string, any>>("module", { default: () => ({}) });

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
