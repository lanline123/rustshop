<template>
    <template v-if="type !== 'storeNav'">
        <div class="dec-divider-line"></div>
        <div class="dec-spread-title">
            <div class="title">{{ $t('模块样式设置') }}</div>
        </div>
    </template>
    <div class="dec-edit-group" v-if="type === 'noticeA2'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块上下间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.boxPaddingTopBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type !== 'noticeA2'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块顶部间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="topPaddingMax" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type !== 'noticeA2'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块底部间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type === 'categoryA1' || type === 'noticeA2' || type === 'noticeA3' || type === 'specialOffer' || type === 'mixedProductDisplay' || type === 'categoryRec' || type === 'productGrouping' || type === 'customProduct' || type === 'shopTips'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块左右间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type === 'shopTips'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块左右外间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.boxMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type === 'banner'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块顶部内间距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleStyle.bannerMarginTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块背景颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="moduleStyle.backgroundColor = defaultModuleStyle.backgroundColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="moduleStyle.backgroundColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type !== 'article' && type !== 'banner' && type !== 'ComboSuggestionsEdit' && type !== 'noticeA2' && type !== 'mixedProductDisplay' && type !== 'storeNav'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块背景图片') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <PicSelect v-model:photo="moduleStyle.backgroundPic" v-model:defaultValue="defaultModuleStyle.backgroundPic"></PicSelect>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type !== 'article' && type !== 'banner' && type !== 'ComboSuggestionsEdit' && type !== 'noticeA2' && type !== 'mixedProductDisplay' && type !== 'storeNav'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('图片填充') }}</div>
            <div class="value">{{ selectLabel.picFillType[moduleStyle.backgroundPicFillType as PicFillType] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="moduleStyle.backgroundPicFillType">
                <el-tooltip effect="light" placement="bottom" :content="$t('填充')" :show-after="100">
                    <el-radio-button :value="'cover'"><i class="iconfont-admin icon-tianchong"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('周边留白')" :show-after="100">
                    <el-radio-button :value="'contain'"><i class="iconfont-admin icon-liubai"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref } from "vue";
import { SelectColor } from '@/components/select'
import { PicSelect } from '@/components/decorate'
import { ModuleStyleType } from "@/types/decorate/decorate.d";
import { defaultModuleStyle } from "@/views/decorate/decorate/src/modules/";
import {PicFillType } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const moduleStyle = defineModel<ModuleStyleType>("modelValue", { default: () => ({
}) });
const props = defineProps({
    type: {
        type: String,
        default: ""
    },
    topPaddingMax: {
        type: Number,
        default: 30
    }
});
</script>
