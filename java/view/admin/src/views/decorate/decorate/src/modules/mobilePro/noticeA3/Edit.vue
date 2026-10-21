<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('店铺公告') }}</h3>
            <div class="dec-edit-desc">{{ $t('可以发布店铺的公告，滚动播放') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('基本设置') }}</div>
            </div>
            <div class="notice-collapse-wrap">
                <draggable item-key="" :list="module.noticeList" ghost-class="ghost" chosen-class="chosenClass" animation="300">
                    <template #item="{ element, index }">
                        <div class="collapse-item">
                            <div class="notice-card-body">
                                <div class="notice-field">
                                    <div class="notice-field-label">{{ $t('标题') }}</div>
                                    <div class="notice-field-value">
                                        <el-input v-model="element.title" :placeholder="$t('请输入公告标题')" />
                                    </div>
                                </div>
                                <div class="notice-field">
                                    <div class="notice-field-label">{{ $t('前缀描述') }}</div>
                                    <div class="notice-field-value">
                                        <el-input v-model="element.prefix" :placeholder="$t('请输入前缀')" />
                                    </div>
                                </div>
                                <div class="notice-field">
                                    <div class="notice-field-label">{{ $t('链接') }}</div>
                                    <div class="notice-field-value">
                                        <SelectLink v-model="element.link" decorateType="mobile"></SelectLink>
                                    </div>
                                </div>
                            </div>
                            <div class="del-item" @click="removeNotice(index)" v-if="module.noticeList.length > 1">
                                <i class="ico-decorate icon-dec-cha"></i>
                            </div>
                        </div>
                    </template>
                </draggable>
                <div class="dec-pic-group" v-if="module.noticeList && module.noticeList.length < MAX_NOTICE_COUNT">
                    <div class="dec-pic-add-con">
                        <a class="dec-pic-add-btn" @click="addNotice"><i class="ico-decorate icon-dec-jia"></i>{{ $t('添加公告') }}</a>
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告头部区域') }}</div>
                    <div class="value">{{ module.showHeader === 1 ? $t('显示') : $t('隐藏') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch v-model="module.showHeader" :active-value="1" :inactive-value="0" />
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showHeader === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('头部图宽度') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.headerImageWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="200" />
                    </div>
                </div>
            </div>

            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告文字颜色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.textColor = '#666666'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.textColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告头部图') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.headerImage"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告边框色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.borderColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.borderColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告内容背景色') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.contentBgColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.contentBgColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告前缀') }}</div>
                    <div class="value">{{ module.showPrefix === 1 ? $t('显示') : $t('隐藏') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch v-model="module.showPrefix" :active-value="1" :inactive-value="0" />
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('公告边框') }}</div>
                    <div class="value">{{ module.showBorder === 1 ? $t('显示') : $t('隐藏') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-switch v-model="module.showBorder" :active-value="1" :inactive-value="0" />
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="noticeA3"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="noticeA3"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectLink, SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import draggable from "vuedraggable";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import { ModuleType } from "@/types/decorate/decorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref([
    { label: t('基本设置'), name: "content" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("content");
const module = defineModel<ModuleType & Record<string, any>>("module", { default: () => ({}) });

const MAX_NOTICE_COUNT = 12;

const addNotice = () => {
    if (!module.value.noticeList) {
        module.value.noticeList = [];
    }
    module.value.noticeList.push({
        title: "",
        prefix: "",
        link: {}
    });
};

const removeNotice = (index: number) => {
    module.value.noticeList.splice(index, 1);
};
</script>
<style lang="less" scoped>
.notice-collapse-wrap {
    padding: 0 10px 15px 10px;

    .collapse-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);

        .notice-card-body {
            padding: 12px 10px;
        }

        .notice-field {
            display: flex;
            align-items: center;
            margin-bottom: 10px;

            &:last-child {
                margin-bottom: 0;
            }

            .notice-field-label {
                width: 60px;
                flex-shrink: 0;
                font-size: 13px;
                color: #333;
                text-align: right;
                margin-right: 10px;
            }

            .notice-field-value {
                flex: 1;
            }
        }

        .del-item {
            position: absolute;
            cursor: pointer;
            right: -10px;
            top: -10px;
            color: #fff;
            background: #bbb;
            border-radius: 50%;
            z-index: 2;
            width: 18px;
            height: 18px;
            text-align: center;
            line-height: 18px;
            display: none;

            .ico-decorate {
                font-size: 12px;
            }
        }
    }

    .collapse-item:hover .del-item {
        display: block;
    }
}
</style>
