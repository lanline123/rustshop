<template>
    <!-- 标题设置 -->
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题描述文字') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-radio-group">
                <el-radio-group class="dec-radio-group" v-model="titleDesc.show">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题文字颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="titleDesc.titleColor = '#ffffff'">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="titleDesc.titleColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题文字大小') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="titleDesc.titleFontSize" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题文字粗细') }}</div>
            <div class="value">{{ selectLabel.titleFontBold[titleDesc.titleFontWeight as TitleFontBaold] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleDesc.titleFontWeight">
                <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                    <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                    <el-radio-button :value="700"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('描述文字颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="titleDesc.descColor = '#ffffff'">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="titleDesc.descColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('描述文字大小') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="titleDesc.descFontSize" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('描述文字粗细') }}</div>
            <div class="value">{{ selectLabel.titleFontBold[titleDesc.descFontWeight as TitleFontBaold] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleDesc.descFontWeight">
                <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                    <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                    <el-radio-button :value="700"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字展示位置') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleDesc.fontPosition">
                <el-radio-button :value="'internal'">{{ $t('图片内部') }}</el-radio-button>
                <el-radio-button :value="'external'">{{ $t('图片外部') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字对齐方式') }}</div>
            <div class="value">{{ selectLabel.textAlignment[titleDesc.textAlignment as TextAlignmentType] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleDesc.textAlignment">
                <el-tooltip effect="light" placement="bottom" :content="$t('左对齐')" :show-after="100">
                    <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('居中对齐')" :show-after="100">
                    <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('右对齐')" :show-after="100">
                    <el-radio-button :value="'right'"><i class="iconfont-admin icon-alignRight"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1 && titleDesc.fontPosition == 'external'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字顶部距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="titleDesc.marginTop" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1 && titleDesc.fontPosition == 'external'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字底部距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="titleDesc.marginBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1 && titleDesc.fontPosition == 'external'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字侧边距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="titleDesc.marginLeft" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleDesc.show == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('内容背景颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="titleDesc.backgroundColor = ''">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="titleDesc.backgroundColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { ref, onMounted, ComputedRef, computed } from "vue";
import { SelectLink } from "@/components/select";
import { TitleDescType } from "@/types/decorate/decorate.d";
import { imageFormat } from "@/utils/format";
import { defaultSubTitle } from "@/views/decorate/decorate/src/modules/";
import { TitleFontSize, TitleFontBaold, TextAlignmentType } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const titleDesc = defineModel<TitleDescType>("modelValue", { default: () => ({}) });
const props = defineProps({
    title: {
        type: String,
        default: ""
    }
});
</script>
