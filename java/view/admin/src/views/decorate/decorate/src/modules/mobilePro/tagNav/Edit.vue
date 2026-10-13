<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('标签导航') }}</h3>
            <div class="dec-edit-desc">{{ $t('添加店铺的多个标签等，任意勾选，方便快捷') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <!-- ========== Tab 1: 分组设置 ========== -->
        <div v-if="activeName === 'group'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('基本设置') }}</div>
            </div>
            <GroupListEdit v-model="module.groupList" :title="$t('添加分组推荐')"></GroupListEdit>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('文本样式') }}</div>
                    <div class="value">{{ module.textWeight === 700 ? $t('加粗体') : $t('常规体') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textWeight">
                        <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                            <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                            <el-radio-button :value="700"
                                ><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('分组间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.groupGap" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('分组左右边距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.groupPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
        </div>
        <!-- ========== Tab 2: 扩展设置 ========== -->
        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <template v-for="(c, index) in module.categoryColors" :key="index">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('第{index1}个分类：', { index1: index + 1 }) }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-gradient">
                            <SelectColor v-model:color="c.colorA"></SelectColor>
                            <span class="gradient-sep">~</span>
                            <SelectColor v-model:color="c.colorB"></SelectColor>
                        </div>
                    </div>
                </div>
            </template>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('分组标题色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.titleColor = '#ffffff'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.titleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import GroupListEdit from "./src/GroupListEdit.vue";
import { ModuleType, ModuleTagNavType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('分组设置'), name: "group" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("group");
const module = defineModel<ModuleType & ModuleTagNavType>("module", { default: () => ({}) });
</script>
<style scoped lang="less">
.dec-color-gradient {
    display: flex;
    align-items: center;
    .gradient-sep {
        margin: 0 8px;
        color: #969799;
    }
}
</style>
