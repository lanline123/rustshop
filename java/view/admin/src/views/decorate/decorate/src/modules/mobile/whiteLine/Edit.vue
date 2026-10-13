<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('空白分割') }}</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('分割类型') }}</div>
                <div class="value">{{ selectLabel.lineType[module.lineType || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.lineType">
                    <el-radio-button :value="1">{{ $t('空白') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('分割线') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择样式') }}</div>
                <div class="value">{{ selectLabel.lineStyle[module.lineStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.lineStyle">
                    <el-radio-button :value="1">{{ $t('实线') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('虚线') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('点状线') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame" type="line"></CommonFrameEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { SelectColor } from "@/components/select";
import { ModuleType } from "@/types/decorate/decorate.d";
import { ref } from "vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType>("module", { default: () => ({}) });
const selectLabel = ref<any>({
    lineType: {
        1: t('空白'),
        2: t('分割线'),
    },
    lineStyle: {
        1: t('实线'),
        2: t('虚线'),
        3: t('点状线'),
    }
});
</script>
<style lang="less">
.handle {
    box-sizing: border-box;
    position: absolute;
    background: #fff;
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate {
    touch-action: none;
    position: absolute;
    box-sizing: border-box;
    border: 1px solid transparent;
    background: rgba(21, 91, 212, 0.6);
    color: #fff;
}

.vue-drag-resize-rotate.active {
    background: rgba(21, 91, 212, 0.9);
    border: 1px solid #155bd4;
}

.vue-drag-resize-rotate:hover {
    border: 1px solid #155bd4;
}
</style>
