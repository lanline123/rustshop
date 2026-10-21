<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>图片橱窗</h3>
            <div class="dec-edit-desc">可以快速的添加图片广告，推广营销常用模块</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>

        <!-- 图片设置 Tab -->
        <div v-if="activeName === 'picture'">
            <div class="dec-edit-title-box">
                <div class="title">基本设置</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <PicList :isMultiple="true" v-model:photos="module.picList" :showText="false" decorateType="mobile"></PicList>
                </div>
            </div>

            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">列表样式</div>
                    <div class="value">{{ selectLabel.listStyle[module.listStyle] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.listStyle">
                        <el-radio-button value="fixed">固定</el-radio-button>
                        <el-radio-button value="scroll">滑动</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">排列布局</div>
                    <div class="value">每行{{ module.rowNum }}个</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button v-for="n in 6" :key="n" :value="n">每行{{ n }}个</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">图片比例</div>
                    <div class="value">{{ selectLabel.picRatio[module.picRatio] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picRatio">
                        <el-radio-button value="default">默认</el-radio-button>
                        <el-radio-button value="3:2">3:2</el-radio-button>
                        <el-radio-button value="1:1">1:1</el-radio-button>
                        <el-radio-button value="3:4">3:4</el-radio-button>
                        <el-radio-button value="2:1">2:1</el-radio-button>
                        <el-radio-button value="16:9">16:9</el-radio-button>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block" v-if="module.picRatio !== 'default'">
                <div class="dec-edit-group-title">
                    <div class="label">图片模式</div>
                    <div class="value">{{ selectLabel.picFillType[module.picFillType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picFillType">
                        <el-tooltip effect="light" placement="bottom" content="充满" :show-after="100">
                            <el-radio-button value="cover"><i class="iconfont-admin icon-tianchong"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="周边留白" :show-after="100">
                            <el-radio-button value="contain"><i class="iconfont-admin icon-liubai"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.picRadius"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                        />
                    </div>
                </div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.imgPadding"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                        />
                    </div>
                </div>
            </div>

            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">模块投影</div>
                    <div class="value">{{ selectLabel.boxShadow[module.boxShadow] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.boxShadow">
                        <el-radio-button :value="0">无</el-radio-button>
                        <el-radio-button :value="1">常规</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>

        <!-- 扩展设置 Tab（对齐图文介绍模块） -->
        <div v-if="activeName === 'extend'">
            <ModuleStyleEdit v-model="module.moduleStyle" type="customProduct"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="imageTextComponent"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { PicList } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ImageShowcaseModule } from "./types";

const tabs = ref<any[]>([
    { label: "图片设置", name: "picture" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("picture");

// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ImageShowcaseModule);

const selectLabel = ref<Record<string, Record<string, string>>>({
    listStyle: {
        fixed: "固定",
        scroll: "滑动"
    },
    picRatio: {
        default: "默认比例",
        "3:2": "3:2",
        "1:1": "1:1",
        "3:4": "3:4",
        "2:1": "2:1",
        "16:9": "16:9"
    },
    picFillType: {
        cover: "充满",
        contain: "周边留白"
    },
    boxShadow: {
        0: "无",
        1: "常规"
    }
});
</script>
