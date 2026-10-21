<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('分类推荐A1') }}</h3>
            <div class="dec-edit-desc">{{ $t('选择图片和链接，编辑标题和副标题，最多添加4个') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('基本设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList
                        :isMultiple="true"
                        :showText="true"
                        :showDesc="true"
                        :maxCount="4"
                        v-model:photos="module.picList"
                        decorateType="mobile"
                    ></PicList>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>

            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.picRadius"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="40"
                        />
                    </div>
                </div>
            </div>

            <CommonSubTitleEdit v-model="module.subTitle" :title="$t('分类标题')"></CommonSubTitleEdit>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('副标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.subTitleColor = '#999999'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.subTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="categoryA1"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import CommonSubTitleEdit from "../../src/commonSubTitle/Edit.vue";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import { ModuleType, CategoryRecA1Type } from "@/types/decorate/decorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref([
    { label: t('内容设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");

const module = defineModel<ModuleType & CategoryRecA1Type>("module", { default: () => ({}) });
</script>
