<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>商品2</h3>
        </div>
        <CommonProductEdit v-model="module.products"></CommonProductEdit>
        <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">图片背景颜色</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-color-group" need-refresh="true">
                <div class="dec-color-button">
                    <a class="dec-color-reset" @click="module.imageBackgroundColor = defaultFrame.backgroundColor">重置</a>
                    <SelectColor v-model:color="module.imageBackgroundColor"></SelectColor>
                </div>
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">商品卡圆角</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="module.goodsRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <!-- <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-title">
            <div class="label">购买按钮样式</div>
            <div class="value">{{ selectLabel.buyBtnStyle[module.buyBtnStyle || ""] }}</div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="module.buyBtnStyle">
                <el-radio-button :value="0">不显示</el-radio-button>
                <el-radio-button :value="1">样式一</el-radio-button>
                <el-radio-button :value="2">样式二</el-radio-button>
                <el-radio-button :value="3">样式三</el-radio-button>
                <el-radio-button :value="4">样式四</el-radio-button>
            </el-radio-group>
        </div>
    </div> -->
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">圆角</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="module.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">内边距</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="module.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">左右边距</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="module.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">上边距</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-range-group">
                <el-slider v-model="module.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
            </div>
        </div>
    </div>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">下边距</div>
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
import { ref, computed } from "vue";
import { SelectColor } from "@/components/select";
import CommonProductEdit from "@/views/decorate/decorate/src/modules/src/commonProduct/Edit.vue";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { Product2Module } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as Product2Module);
const selectLabel = ref<any>({
    buyBtnStyle: {
        0: "不显示",
        1: "样式一",
        2: "显示",
        3: "显示",
        4: "显示",
    },
});
</script>
