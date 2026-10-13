<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图片广告</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">选择模板</div>
                <div class="value">{{ selectLabel.picType[module.picType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picType">
                    <el-tooltip effect="light" placement="bottom" content="轮播" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-carousel"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行一个" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行两个" :show-after="100">
                        <el-radio-button :value="5"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行三个" :show-after="100">
                        <el-radio-button :value="6"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" content="一行四个" :show-after="100">
                        <el-radio-button :value="7"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">是否通宽排版</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.isFluxWidth">
                    <el-radio-button :value="1">是</el-radio-button>
                    <el-radio-button :value="0">否</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">添加图片</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc" v-if="module.picType == 1">提示：您可以通过拖拽进行图片排序，单图占屏幕宽为100%，建议宽1500px，高度自适应</div>
                <div class="pic-change-desc" v-if="module.picType == 2">提示：您可以通过拖拽进行图片排序，宽不小于1500px（选择轮播时默认通宽）</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList" decorateType="mobile"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group" v-if="module.picType == 2">
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
                    <el-tooltip effect="light" placement="bottom" content="样式三" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-indicator-3"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.picType == 2">
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
        <div class="dec-edit-group pic-change-desc" v-if="module.picType == 2">
            <div class="dec-edit-group-title">
                <div class="label">轮播一行显示</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.swiperPreView" style="width: 80px">
                        <el-option :value="1" label="1个" />
                        <el-option :value="2" label="2个" />
                        <el-option :value="3" label="3个" />
                        <el-option :value="4" label="4个" />
                    </el-select>
                </div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">图片边距</div>
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
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picRadioStyle">
                    <el-radio-button :value="1">直角</el-radio-button>
                    <el-radio-button :value="2">圆角</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        <CommonTitleEdit v-model="module.title" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import CommonTitleEdit from "@/views/decorate/decorate/src/modules/src/commonTitle/Edit.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { PcImageAdModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as PcImageAdModule);

const selectLabel = {
    picType: {
        1: "一行一个",
        2: "轮播",
        3: "横向滑动-大图",
        4: "横向滑动-小图",
        5: "一行两个",
        6: "一行三个",
        7: "一行四个"
    },
    picPageType: {
        1: "样式一",
        2: "样式二",
        3: "样式三 "
    }
} as Record<string, Record<string | number, string>>;
</script>
