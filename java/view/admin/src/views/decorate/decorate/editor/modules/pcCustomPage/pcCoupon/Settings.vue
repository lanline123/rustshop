<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>优惠券</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">显示标题栏</div>
                <div class="value">{{ selectLabel.showTitle[module.showTitle || ''] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.showTitle">
                    <el-radio-button :value="1">显示</el-radio-button>
                    <el-radio-button :value="0">不显示</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showTitle == 1">
            <div class="dec-edit-group-title dec-edit-group-block">
                <div class="label">标题内容</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.title" style="width: 240px" placeholder="请输入标题内容" />
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.showTitle == 1">
            <div class="dec-edit-group-title dec-edit-group-block">
                <div class="label">描述内容</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input type="textarea" v-model="module.desc" :maxlength="500" placeholder="请输入描述内容, 字数不超过500" />
            </div>
        </div>
<!--        <div class="dec-edit-group">-->
<!--            <div class="dec-edit-group-title">-->
<!--                <div class="label">背景颜色</div>-->
<!--                <div class="value"></div>-->
<!--            </div>-->
<!--            <div class="dec-edit-group-con">-->
<!--                <div class="dec-color-group" need-refresh="true">-->
<!--                    <div class="dec-color-button">-->
<!--                        <a class="dec-color-reset" @click="module.backgroundColor = defaultFrame.backgroundColor">重置</a>-->
<!--                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>-->
<!--                    </div>-->
<!--                </div>-->
<!--            </div>-->
<!--        </div>-->
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">组件边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.itemPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
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
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcCouponModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcCouponModule);
const selectLabel = ref<any>({
    showTitle: {
        1: "显示",
        0: "不显示",
    },
    colorStyle: {
        1: "红色",
        2: "黄色",
        3: "黑色",
        4: "绿色",
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
