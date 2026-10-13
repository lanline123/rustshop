<template>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('图片填充') }}</div>
            <div class="value">{{ selectLabel.picFillType[picFillType as PicFillType] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="picFillType">
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
import { PicFillType } from "@/types/decorate/decorate.d";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
const picFillType = defineModel<any>("modelValue", { default: () => ({
}) });
const props = defineProps({
    type: {
        type: String,
        default: ""
    }
});
</script>
