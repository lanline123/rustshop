<template>
    <!-- 结构设置 -->
    <div class="dec-divider-line" v-if="type != 'live'"></div>

    <div class="dec-spread-title" v-if="type != 'notice' && type != 'searchBar' && type != 'live'">
        <div class="title">{{ $t('模块设置') }}</div>
    </div>
    <div class="dec-edit-group" v-if="type == 'notice' || type == 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('文字颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="frame.textColor = defaultFrame.textColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="frame.textColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'notice' || type == 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('组件背景') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="frame.itemBackgroundColor = defaultFrame.itemBackgroundColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="frame.itemBackgroundColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'notice' || type == 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('组件圆角') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.itemRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'notice' || type == 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('组件高度') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.itemHeight" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="!hideBg">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('背景颜色') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="frame.backgroundColor = defaultFrame.backgroundColor">{{ $t('重置') }}</a>
                    <SelectColor v-model:color="frame.backgroundColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="!hideBoxRadius && type != 'line' && type != 'notice' && type != 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('圆角') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group"  v-if="!hidePadding && !splitInnerPadding && type != 'line' && type != 'notice' && type != 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('内边距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="!hidePadding && splitInnerPadding && type != 'line' && type != 'notice' && type != 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">左右内边距</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.innerPaddingLeftRight" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="!hidePadding && splitInnerPadding && type != 'line' && type != 'notice' && type != 'searchBar'">
        <div class="dec-edit-group-title">
            <div class="label">上下内边距</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.innerPaddingTopBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="type == 'titleAd'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('左右内边距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.leftAndRightPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="!hideBoxPadding">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('左右边距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('上边距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :min="allowNegativeTop ? -100 : 0" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('下边距') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="frame.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { ModuleFrameType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/";
const frame = defineModel<ModuleFrameType>("modelValue", { default: () => ({}) });
// 传值
const props = defineProps({
    type: {
        type: String,
        default: ''
    },
    hideBg: {
        type: Boolean,
        default: false
    },
    hidePadding: {
        type: Boolean,
        default: false
    },
    // 将单个"内边距"拆分为"左右内边距"和"上下内边距"两个控制项
    splitInnerPadding: {
        type: Boolean,
        default: false
    },
    hideBoxPadding: {
        type: Boolean,
        default: false
    },
    hideBoxRadius: {
        type: Boolean,
        default: false
    },
    // 允许上边距为负值（用于组件整体上移贴合上一模块）
    allowNegativeTop: {
        type: Boolean,
        default: false
    }
})
</script>
