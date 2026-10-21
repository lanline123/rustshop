<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('公告混排') }}</h3>
            <div class="dec-edit-desc">{{ $t('支持双列选择显示公告。') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'contentSetting'">
            <ComboGroupNoticeEdit :title="$t('新增公告')" v-model="module.noticeGroups" :activeNames="[1,2,3,4]"> </ComboGroupNoticeEdit>
            <!-- <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片比例</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picScale">
                        <el-radio-button :value="'3:2'">3:2</el-radio-button>
                        <el-radio-button :value="'1:1'">1:1</el-radio-button>
                        <el-radio-button :value="'3:4'">3:4</el-radio-button>
                        <el-radio-button :value="'16:9'">16:9</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
            <!-- <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit> -->
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('大标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.titleColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.titleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('小标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.sellingPointColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.sellingPointColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="mixedProductDisplay"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="mixedProductDisplay"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import ComboGroupNoticeEdit from "../../src/comboGroup/NoticeEdit.vue";
import { PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { ModuleType, ModuleMixedNoticeDisplayType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('内容设置'), name: "contentSetting" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("contentSetting");
const module = defineModel<ModuleType & ModuleMixedNoticeDisplayType>("module", { default: () => ({}) });
</script>
