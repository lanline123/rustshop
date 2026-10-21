<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分类导航</h3>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">分类名宽度(%)</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.itemWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">文字颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.textColor = defaultFrame.textColor">重置</a>
                        <SelectColor v-model:color="module.textColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.backgroundColor = defaultFrame.backgroundColor">重置</a>
                        <SelectColor v-model:color="module.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">背景图</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group dec-picSingle-group">
                    <PicSelect v-model:photo="module.navBackgroundPic" v-model:defaultValue="defaultBackgroundPic"> </PicSelect>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">设置分类</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group dec-picSingle-group">
                    <router-link :to="{ path: '/decorate-info/mobile-cat-nav/list' }" target="_blank" class="btn-link">前往设置</router-link>
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索联动</div>
                <div class="value">{{ selectLabel.isGanged[module.isGanged || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.isGanged">
                    <el-radio-button :value="1">联动</el-radio-button>
                    <el-radio-button :value="0">不联动</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">
                    设置为联动时，顶部会替换为LOGO栏+搜索栏+分类导航。 屏幕下滑时LOGO栏会消失，搜索栏会替换至LOGO栏位置（请不要在该组件前再添加其它组件）。
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">LOGO图</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group dec-picSingle-group">
                    <PicSelect v-model:photo="module.logoPic" v-model:defaultValue="defaultIcoPic"> </PicSelect>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc">
                    提示：LOGO图建议尺寸为 1374px*324px，否则可能显示异常。
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">LOGO高度</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.logoHeight" show-input :show-input-controls="false" size="small" input-size="default" :max="200" />
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索框文字</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-input v-model="module.searchText" style="width: 240px" placeholder="请输入搜索框文字" />
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索框边距</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索文字颜色</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.searchTextColor = defaultFrame.textColor">重置</a>
                        <SelectColor v-model:color="module.searchTextColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索组件背景</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="module.itemBackgroundColor = defaultFrame.itemBackgroundColor">重置</a>
                        <SelectColor v-model:color="module.itemBackgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">搜索组件圆角</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.itemRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                </div>
            </div>
        </div>
        <div class="dec-divider-line"></div>
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
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { ref, computed } from "vue";
import { defaultFrame, defaultIcoPic, defaultBackgroundPic } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { CatNavModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CatNavModule);
const selectLabel = ref<any>({
    isGanged: {
        1: "联动",
        0: "不联动"
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
