<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('优惠券') }}</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示标题栏') }}</div>
                <div class="value">{{ selectLabel.showTitle[module.showTitle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showTitle">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showTitle == 1">
            <div class="dec-edit-group-title dec-edit-group-block">
                <div class="label">{{ $t('标题内容') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.title" style="width: 240px" :placeholder="$t('请输入标题内容')" />
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showTitle == 1">
            <div class="dec-edit-group-title dec-edit-group-block">
                <div class="label">{{ $t('描述内容') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input type="textarea" v-model="module.desc" :maxlength="500" :placeholder="$t('请输入描述内容, 字数不超过500')" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示标题栏') }}</div>
                <div class="value">{{ selectLabel.colorStyle[module.colorStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group dec-radio-color-group" v-model="module.colorStyle">
                    <el-radio :value="1">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 1}"><span class="dec-radio-color-button-con"><i style="background-color: #e74c2c;"></i></span></div>
                    </el-radio>
                    <el-radio :value="2">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 2}"><span class="dec-radio-color-button-con"><i style="background-color: #ffe9b7;"></i></span></div>
                    </el-radio>
                    <el-radio :value="3">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 3}"><span class="dec-radio-color-button-con"><i style="background-color: #383b3e;"></i></span></div>
                    </el-radio>
                    <el-radio :value="4">
                        <div class="dec-radio-button dec-tip" :class="{'selected': module.colorStyle == 4}"><span class="dec-radio-color-button-con"><i style="background-color: #e0f4e4;"></i></span></div>
                    </el-radio>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('组件背景') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.itemBackgroundColor = defaultFrame.itemBackgroundColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.itemBackgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
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
                <div class="label">{{ $t('组件边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.itemPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('圆角') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('左右边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('左右外间距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('上边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('下边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from '@/components/select'
import { PicSelect } from '@/components/decorate'
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { ModuleCouponType } from "@/types/decorate/decorate.d";
import { ref } from "vue";
import { defaultFrame, defaultIcoPic } from '@/views/decorate/decorate/src/modules/'

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleCouponType>("module", { default: () => ({}) });
const selectLabel = ref<any>({
    showTitle: {
        1: t('显示'),
        0: t('不显示'),
    },
    colorStyle: {
        1: t('红色'),
        2: t('黄色'),
        3: t('黑色'),
        4: t('绿色'),
    },
});
</script>
<style lang="less" scoped>
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
.dec-edit-group :deep(.el-radio){
    margin-right: 0px;
}
.dec-edit-group :deep(.el-radio .el-radio__input){
    display: none;
}
</style>
