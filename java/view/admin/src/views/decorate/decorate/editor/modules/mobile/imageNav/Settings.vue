<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图文导航</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示模式</div>
                <div class="value">{{ selectLabel.navType[module.navType || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.navType">
                    <el-radio-button :value="1">固定显示</el-radio-button>
                    <el-radio-button :value="2">滑动分页</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">样式</div>
                <div class="value">{{ selectLabel.navStyle[module.navStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.navStyle">
                    <el-radio-button :value="1">图片文字</el-radio-button>
                    <el-radio-button :value="2">仅图片</el-radio-button>
                    <el-radio-button :value="3">仅文字</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group pic-change-desc">
            <div class="dec-edit-group-title">
                <div class="label">一行显示</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.rowNum" style="width: 80px">
                        <el-option :value="3" label="3个" />
                        <el-option :value="4" label="4个" />
                        <el-option :value="5" label="5个" />
                        <el-option :value="6" label="6个" />
                    </el-select>
                </div>
            </div>
        </div>
        <div class="dec-edit-group pic-change-desc">
            <div class="dec-edit-group-title">
                <div class="label">显示行数</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.colNum" style="width: 80px">
                        <el-option :value="1" label="1行" />
                        <el-option :value="2" label="2行" />
                        <el-option :value="3" label="3行" />
                    </el-select>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加图片</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">提示：您可以通过拖拽进行图片排序</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList" decorateType="mobile"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">元素边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imgPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">图片边角</div>
                <div class="value">{{ selectLabel.radioStyle[module.radioStyle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.radioStyle">
                    <el-radio-button :value="1">直角</el-radio-button>
                    <el-radio-button :value="2">圆角</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">指示器样式</div>
                <div class="value">{{ selectLabel.picPageType[module.picPageType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picPageType">
                    <el-tooltip effect="light" placement="bottom" content="样式一" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="样式二" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">指示器颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset">重置</a>
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
import { ref, computed } from "vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ImageNavModule } from "./types";
const selectLabel = ref<any>({
    navType: {
        1: "固定显示",
        2: "滑动分页",
    },
    navStyle: {
        1: "图片文字",
        2: "仅图片",
        3: "仅文字",
    },
    radioStyle: {
        1: "直角",
        2: "圆角",
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
    }
});
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ImageNavModule);
</script>
