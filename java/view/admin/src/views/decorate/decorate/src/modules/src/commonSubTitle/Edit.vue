<template>
    <!-- 标题设置 -->
    <div class="dec-edit-group" v-if="type !== 'categoryRec'">
        <div class="dec-edit-group-title">
            <div class="label"> {{ $t('{title}文字', { title }) }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-radio-group">
                <el-radio-group class="dec-radio-group" v-model="titleData.showTitle">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleData.showTitle == 1">
        <div class="dec-edit-group-title">
            <div class="label"> {{ $t('{title}颜色', { title }) }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="titleData.titleColor = defaultSubTitle.titleColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="titleData.titleColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleData.showTitle == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('{title}大小', { title }) }}</div>
            <div class="value">{{ selectLabel.titleFontSize[titleData.titleFontSize as TitleFontSize] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleData.titleFontSize">
                <el-tooltip effect="light" placement="bottom" :content="$t('小(12号)')" :show-after="100">
                    <el-radio-button :value="12"><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold;"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('中(14号)')" :show-after="100">
                    <el-radio-button :value="14"><i class="iconfont-admin icon-zitiyanse" style="font-size: 14px; font-weight: bold;"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('大(16号)')" :show-after="100">
                    <el-radio-button :value="16"><i class="iconfont-admin icon-zitiyanse" style="font-size: 16px; font-weight: bold;"></i></el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="titleData.showTitle == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('{title}粗细', { title }) }}</div>
            <div class="value">{{ selectLabel.titleFontBold[titleData.titleFontBold as TitleFontBaold] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="titleData.titleFontBold">
                <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                    <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px;"></i></el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                    <el-radio-button :value="700"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold;"></i></el-radio-button>
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
import { ModuleSubTitleType } from "@/types/decorate/decorate.d";
import { imageFormat } from "@/utils/format";
import { defaultSubTitle } from "@/views/decorate/decorate/src/modules/";
import { TitleFontSize, TitleFontBaold } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const titleData = defineModel<ModuleSubTitleType>("modelValue", { default: () => ({
}) });
const props = defineProps({
    title: {
        type: String,
        default: ""
    },
    type: {
        type: String,
        default: ""
    }
});
</script>
