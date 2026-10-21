<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('图片广告') }}</h3>
        </div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('选择模板') }}</div>
                <div class="value">{{ selectLabel.picType[module.picType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picType">
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行一个')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-up2end"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('轮播')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-carousel "></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('横向滑动-大图')" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-big-slide"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('横向滑动-小图')" :show-after="100">
                        <el-radio-button :value="4"><i class="ico-decorate icon-dec-small-slide"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行两个')" :show-after="100">
                        <el-radio-button :value="5"><i class="ico-decorate icon-dec-cuberow"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行三个')" :show-after="100">
                        <el-radio-button :value="6"><i class="ico-decorate icon-dec-cuberow2"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('一行四个')" :show-after="100">
                        <el-radio-button :value="7"><i class="ico-decorate icon-dec-cuberow1"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="title">{{ $t('添加图片') }}</div>
            </div>
            <div class="dec-edit-group-desc">
                <div class="pic-change-desc" v-if="module.picType == 1">{{ $t('提示：您可以通过拖拽进行图片排序，单图占屏幕宽为100%，建议宽750px，高度自适应') }}</div>
                <div class="pic-change-desc" v-if="module.picType == 2">{{ $t('提示：您可以通过拖拽进行图片排序，建议手机端尺寸750x350px，高度统一；PC端宽不小于1500px（PC端选择时轮播默认通宽）') }}</div>
                <div class="pic-change-desc" v-if="module.picType == 3">{{ $t('提示：您可以通过拖拽进行图片排序，单图占屏幕宽为80%，建议宽度600px，高度统一') }}</div>
                <div class="pic-change-desc" v-if="module.picType == 4">{{ $t('提示：您可以通过拖拽进行图片排序，单图占屏幕宽为40%，建议宽度300px，高度统一') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <PicList :isMultiple="true" v-model:photos="module.picList" decorateType="mobile"></PicList>
            </div>
        </div>
        <div class="dec-divider-line"></div>
        <div class="dec-edit-group" v-if="module.picType == 2">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器样式') }}</div>
                <div class="value">{{ selectLabel.picPageType[module.picPageType] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picPageType">
                    <el-tooltip effect="light" placement="bottom" :content="$t('样式一')" :show-after="100">
                        <el-radio-button :value="1"><i class="ico-decorate icon-dec-indicator-1"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('样式二')" :show-after="100">
                        <el-radio-button :value="2"><i class="ico-decorate icon-dec-indicator-2"></i></el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('样式三')" :show-after="100">
                        <el-radio-button :value="3"><i class="ico-decorate icon-dec-indicator-3"></i></el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="module.picType == 2">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="module.swiperPageColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group pic-change-desc" v-if="module.picType == 2">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('轮播一行显示') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-select-group" need-refresh="true">
                    <el-select v-model="module.swiperPreView" style="width: 80px">
                        <el-option :value="1" :label="$t('1个')" />
                        <el-option :value="2" :label="$t('2个')" />
                        <el-option :value="3" :label="$t('3个')" />
                        <el-option :value="4" :label="$t('4个')" />
                    </el-select>
                </div>
            </div>
        </div>

        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图片边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="module.imgPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图片边角') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.picRadioStyle">
                    <el-radio-button :value="1">{{ $t('直角') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('圆角') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('是否通宽排版(仅PC端有效)') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.isFluxWidth">
                    <el-radio-button :value="1">{{ $t('是') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('否') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        <CommonTitleEdit v-model="module.title" decorateType="mobile"></CommonTitleEdit>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import { ref, reactive } from "vue";
import CommonTitleEdit from "../../src/commonTitle/Edit.vue";
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectLabel = ref<any>({
    picType: {
        1: t('一行一个'),
        2: t('轮播'),
        3: t('横向滑动-大图'),
        4: t('横向滑动-小图'),
        5: t('一行两个'),
        6: t('一行三个'),
        7: t('一行四个')
    },
    picPageType: {
        1: t('样式一'),
        2: t('样式二'),
        3: t('样式三 ')
    }
});
const module = defineModel<ModuleType & ModuleImageType>("module", { default: () => ({}) });
</script>
