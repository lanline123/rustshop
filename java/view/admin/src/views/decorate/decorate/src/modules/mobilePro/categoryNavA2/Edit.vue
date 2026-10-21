<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('类目导航A2') }}</h3>
            <div class="dec-edit-desc">{{ $t('选择图片和链接, 可编辑文字, 方便您快速定制类目导航') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'category'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('内容设置') }}</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.categoryList" :title="$t('添加一组')" decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('列表样式') }}</div>
                    <div class="value">{{ selectLabel.listStyleType[module.listStyleType as any] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.listStyleType">
                        <el-tooltip effect="light" placement="bottom" :content="$t('固定')" :show-after="100">
                            <el-radio-button :value="'list'"><i class="iconfont-admin icon-guding-guding"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('单列滑动')" :show-after="100">
                            <el-radio-button :value="'slide'"><i class="iconfont-admin icon-kuaijiebiaoqian-youhuadong"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('双排滑动')" :show-after="100">
                            <el-radio-button :value="'slide2'"><i class="iconfont-admin icon-kuaijiebiaoqian-youhuadong"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每屏显示数量') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="2">{{ $t('2个') }}</el-radio-button>
                        <el-radio-button :value="3">{{ $t('3个') }}</el-radio-button>
                        <el-radio-button :value="4">{{ $t('4个') }}</el-radio-button>
                        <el-radio-button :value="5">{{ $t('5个') }}</el-radio-button>
                        <el-radio-button :value="'custom'">{{ $t('自定义') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.rowNum === 'custom'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片占比宽度') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.rowPicPercent" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                    </div>
                </div>
            </div>
            <AnimationEdit v-model="module.animation" type="category_A2"></AnimationEdit>
            <div class="dec-divider-line"></div>
            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片百分比') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picPercent" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('分类直角-圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.radius" show-input :show-input-controls="false" size="small" input-size="default" :max="200" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('分类间距') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.categoryPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <CommonSubTitleEdit v-model="module.subTitle" :title="$t('分类标题')"></CommonSubTitleEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('底部指示条') }} </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.indicatorBar">
                            <el-radio-button :value="1">{{ $t('滚动条') }}</el-radio-button>
                            <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.indicatorBar == 1">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('指示条宽度') }} </div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.barWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="100" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.indicatorBar == 1">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('指示条高度') }} </div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.barHeight" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.indicatorBar == 1">
                <div class="dec-edit-group-title">
                    <div class="label"> {{ $t('选中指示条宽度') }} </div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.barWidthOn" show-input :show-input-controls="false" size="small" input-size="default" :max="60" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.indicatorBar == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('默认颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.barColor = '#eeeeee'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.barColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.indicatorBar == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('进度颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.barColorOn = '#ff6600'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.barColorOn"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="category_A2"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, reactive } from "vue";
import CommonSubTitleEdit from "../../src/commonSubTitle/Edit.vue";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import AnimationEdit from "../../src/animation/Edit.vue";
import { ModuleType, ModuleCategoryA2Type } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([{ label: t('导航内容'), name: 'category' }, { label: t('扩展设置'), name: 'extend' }]);
const activeName = ref("category");
const selectLabel = ref<any>({
    listStyleType: {
        "list": t('固定'),
        "slide": t('单列滑动'),
        "slide2": t('双排滑动')
    }
});
const module = defineModel<ModuleType & ModuleCategoryA2Type>("module", { default: () => ({}) });
</script>
