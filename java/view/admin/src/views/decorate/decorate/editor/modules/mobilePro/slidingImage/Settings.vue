<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>小图滑动</h3>
            <div class="dec-edit-desc">图片可横向轮播显示, 尺寸不限, 可设置背景图片</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" title="添加图片+链接" showDesc showColor decorateType="mobile"></PicList>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">图片样式设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">背景渐变方向</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.backgroundGradientType">
                        <el-tooltip effect="light" placement="bottom" content="上下" :show-after="100">
                            <el-radio-button :value="'upDown'">上下</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="左右" :show-after="100">
                            <el-radio-button :value="'leftRight'">左右</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="斜向" :show-after="100">
                            <el-radio-button :value="'diagonal'">斜向</el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="单色" :show-after="100">
                            <el-radio-button :value="'purity'">单色</el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片宽度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picWidth" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picMargin" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片内距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">缩小比例</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.picReducePercent" show-input :show-input-controls="false" size="small" input-size="default" :max="1.00" :step="0.01" />
                    </div>
                </div>
            </div>
            <CommonSubTitleEdit v-model="module.subTitle" title="标题"></CommonSubTitleEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> 描述文字 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.showDesc">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.showDesc == 1">
                <div class="dec-edit-group-title">
                    <div class="label"> 描述文字颜色 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.descColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.descColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle"></ModuleStyleEdit>
            <div class="dec-edit-title-box">
                <div class="title">轮播扩展设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> 自动轮播 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-radio-group">
                        <el-radio-group class="dec-radio-group" v-model="module.autoPlay">
                            <el-radio-button :value="1">开启</el-radio-button>
                            <el-radio-button :value="0">关闭</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label"> 轮播时间间隔 </div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.interval" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, reactive, computed } from "vue";
import CommonSubTitleEdit from "@/views/decorate/decorate/src/modules/src/commonSubTitle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { SlidingImageModule } from "./types";
const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as SlidingImageModule);
</script>
