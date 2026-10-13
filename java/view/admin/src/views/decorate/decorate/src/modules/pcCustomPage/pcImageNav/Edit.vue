<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('图文导航') }}</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示模式') }}</div>
                <div class="value">{{ selectLabel.navType[module.navType || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.navType">
                    <el-radio-button :value="1">{{ $t('固定显示') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('滑动分页') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('样式') }}</div>
                <div class="value">{{ selectLabel.navStyle[module.navStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.navStyle">
                    <el-radio-button :value="1">{{ $t('图片文字') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('仅图片') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('仅文字') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group pic-change-desc">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('一行显示') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.rowNum" style="width: 80px">
                        <el-option :value="3" :label="$t('3个')" />
                        <el-option :value="4" :label="$t('4个')" />
                        <el-option :value="5" :label="$t('5个')" />
                        <el-option :value="6" :label="$t('6个')" />
                    </el-select>
                </div>
            </div>
        </div>
        <div class="dec-edit-group pic-change-desc">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示行数') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.colNum" style="width: 80px">
                        <el-option :value="1" :label="$t('1行')" />
                        <el-option :value="2" :label="$t('2行')" />
                        <el-option :value="3" :label="$t('3行')" />
                    </el-select>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('添加图片') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">{{ $t('提示：您可以通过拖拽进行图片排序') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList" decorateType="pc"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = defaultFrame.backgroundColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('元素边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imgPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图片边角') }}</div>
                <div class="value">{{ selectLabel.radioStyle[module.radioStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.radioStyle">
                    <el-radio-button :value="1">{{ $t('直角') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('圆角') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器样式') }}</div>
                <div class="value">{{ selectLabel.picPageType[module.picPageType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picPageType">
                    <el-tooltip effect="light" placement="bottom" :content="$t('样式一')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('样式二')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.swiperPageColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>

        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { ref, reactive } from "vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { defaultFrame } from "@/views/decorate/decorate/src/modules";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectLabel = ref<any>({
    navType: {
        1: t('固定显示'),
        2: t('滑动分页'),
    },
    navStyle: {
        1: t('图片文字'),
        2: t('仅图片'),
        3: t('仅文字'),
    },
    radioStyle: {
        1: t('直角'),
        2: t('圆角'),
    },
    picPageType: {
        1: t('样式一'),
        2: t('样式二'),
    }
});
const module = defineModel<ModuleType & ModuleImageType>("module", { default: () => ({}) });
console.log(module.value);
</script>
