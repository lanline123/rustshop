<template>
    <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('图片缓动效果') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="animation.type">
                <el-tooltip effect="light" placement="bottom" :content="$t('放大')" :show-after="100">
                    <el-radio-button :value="'scale'">{{ $t('放大') }}</el-radio-button>
                </el-tooltip>
                <el-tooltip v-if="type !== 'categoryA1'" effect="light" placement="bottom" :content="$t('翻转')" :show-after="100">
                    <el-radio-button :value="'flip'">{{ $t('翻转') }}</el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('左入')" :show-after="100">
                    <el-radio-button :value="'slideLeft'">{{ $t('左入') }}</el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('右入')" :show-after="100">
                    <el-radio-button :value="'slideRight'">{{ $t('右入') }}</el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('上滑')" :show-after="100">
                    <el-radio-button :value="'slideTop'">{{ $t('上滑') }}</el-radio-button>
                </el-tooltip>
                <el-tooltip effect="light" placement="bottom" :content="$t('关闭')" :show-after="100">
                    <el-radio-button :value="'close'">{{ $t('关闭') }}</el-radio-button>
                </el-tooltip>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group" v-if="animation.type != 'close'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('缓动效果速度') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="animation.duration" show-input :show-input-controls="false" size="small" input-size="default" :max="3.0" :step="0.1" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="animation.type != 'close'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('缓动效果时长') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="animation.delay" show-input :show-input-controls="false" size="small" input-size="default" :max="2.0" :step="0.1" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="animation.type != 'flip' && animation.type != 'close'">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('缓动效果幅度') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="animation.range" show-input :show-input-controls="false" size="small" input-size="default" :max="2.0" :step="0.1" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, PropType } from "vue";
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { AnimationType } from "@/types/decorate/decorate.d";
import { defaultContentStyle } from "@/views/decorate/decorate/src/modules/";
const animation = defineModel<AnimationType>("modelValue", { default: () => ({}) });
type typeString = "categoryA1" | "category_A2";
const props = defineProps({
    type: {
        type: String as PropType<typeString>,
        default: "categoryA1"
    }
});
</script>
