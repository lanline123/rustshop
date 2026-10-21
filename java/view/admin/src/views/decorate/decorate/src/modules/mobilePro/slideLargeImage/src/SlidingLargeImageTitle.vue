<template>
    <div class="dec-spread-title">
        <div class="title">{{ $t('模块标题设置') }}</div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块大标题') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-input-group">
                <el-input v-model="moduleTitle.titleText" :placeholder="$t('请输入标题内容')"></el-input>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('模块小标题') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-input-group">
                <el-input v-model="moduleTitle.subTitleText" :placeholder="$t('请输入标题内容')"></el-input>
            </div>
        </div>
    </div>
    <div class="dec-divider-line"></div>
    <div class="dec-spread-title">
        <div class="title">{{ $t('标题样式设置') }}</div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题位置') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="moduleTitle.titlePosition">
                <el-radio-button :value="'top'">{{ $t('居上') }}</el-radio-button>
                <el-radio-button :value="'bottom'">{{ $t('居下') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题对齐方式') }}</div>
            <div class="value">{{ selectLabel.textAlignment[moduleTitle.textAlignment as TextAlignmentType] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="moduleTitle.textAlignment">
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
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题顶部距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleTitle.titleMarginTop" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题底部距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleTitle.titleMarginBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('标题侧边距离') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleTitle.titleMarginLeft" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
            </div>
        </div>
    </div>
    <div class="dec-divider-line"></div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('大标题文字颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="moduleTitle.titleColor = '#ffffff'">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="moduleTitle.titleColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('大标题文字大小') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleTitle.titleFontSize" show-input :show-input-controls="false" size="small" input-size="default" :max="28" :min="12" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('大标题文字粗细') }}</div>
            <div class="value">{{ selectLabel.titleFontBold[moduleTitle.titleFontWeight as TitleFontBaold] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="moduleTitle.titleFontWeight">
                <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                    <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                    <el-radio-button :value="700"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('小标题文字颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="moduleTitle.subFontTitleColor = '#ffffff'">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="moduleTitle.subFontTitleColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('小标题文字大小') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="moduleTitle.subFontTitleSize" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('小标题文字粗细') }}</div>
            <div class="value">{{ selectLabel.titleFontBold[moduleTitle.subFontTitleWeight as TitleFontBaold] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="moduleTitle.subFontTitleWeight">
                <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                    <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                    <el-radio-button :value="700"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>

</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { ref, onMounted, ComputedRef, computed } from "vue";
import { SelectLink } from "@/components/select";
import { imageFormat } from "@/utils/format";
import { defaultSubTitle } from "@/views/decorate/decorate/src/modules/";
import { SlidingLargeImageTitleType, TitleFontBaold, TextAlignmentType } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const moduleTitle = defineModel<SlidingLargeImageTitleType>("modelValue", { default: () => ({}) });
const props = defineProps({
    title: {
        type: String,
        default: ""
    }
});
</script>
