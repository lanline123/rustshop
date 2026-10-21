<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>组合推荐</h3>
            <div class="dec-edit-desc">图片与商品自由组合成展示，样式丰富</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <ComboGroupEdit v-model="module.comboGroups" title="添加一组" :isMultiple="true"></ComboGroupEdit>
            <div class="dec-edit-title-box">
                <div class="title">商品参数设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">常规价格颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.regularPriceColor = ''">重置</a>
                            <SelectColor v-model:color="module.regularPriceColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">价格背景颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.regularPriceBgColor = ''">重置</a>
                            <SelectColor v-model:color="module.regularPriceBgColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">价格圆角半径</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.regularPriceRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">价格文字大小</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.regularPriceSize"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :min="10"
                            :max="24"
                        />
                    </div>
                </div>
            </div>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">活动价标签背景颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <div class="flex flex-align-center">
                                <div><SelectColor v-model:color="module.saleTagBackgroundColorA"></SelectColor></div>
                                <div style="margin: 0 10px">-</div>
                                <div><SelectColor v-model:color="module.saleTagBackgroundColorB"></SelectColor></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div> -->
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">活动价标签文字颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.saleTagFontColor = ''">重置</a>
                            <SelectColor v-model:color="module.saleTagFontColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div> -->
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">活动价背景颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.salePriceBackgroundColor = ''">重置</a>
                            <SelectColor v-model:color="module.salePriceBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div> -->
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">活动价文字颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.salePriceFontColor = ''">重置</a>
                            <SelectColor v-model:color="module.salePriceFontColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div> -->
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">活动价小数</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.salePriceDecimal">
                        <el-radio-button :value="1">显示1位</el-radio-button>
                        <el-radio-button :value="2">显示2位</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品圆角半径</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.imageRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">售罄图标</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.soldOutIcon">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">样式设置</div>
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
                    <div class="label">背景颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <div class="flex flex-align-center">
                                <div><SelectColor v-model:color="module.gradientColorA"></SelectColor></div>
                                <div style="margin: 0 10px">-</div>
                                <div><SelectColor v-model:color="module.gradientColorB"></SelectColor></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分组大标题颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.groupTitleColor = ''">重置</a>
                            <SelectColor v-model:color="module.groupTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">轮播图标题颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.bannerTittleColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.bannerTittleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">轮播图指示数颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.bannerDotColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.bannerDotColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">内容圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.groupRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">内容投影</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.groupShadow">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="ComboSuggestionsEdit"></ModuleStyleEdit>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">轮播方向</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.carouselDirection">
                        <el-radio-button :value="'horizontal'">横向</el-radio-button>
                        <el-radio-button :value="'vertical'">纵向</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">自动轮播</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.autoPlay">
                        <el-radio-button :value="1">开启</el-radio-button>
                        <el-radio-button :value="0">关闭</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">轮播间隔</div>
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
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import ComboGroupEdit from "@/views/decorate/decorate/src/modules/src/comboGroup/Edit.vue";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { ComboSuggestionsModule } from "./types";
const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");
const selectLabel = ref<any>({
    listStyleType: {
        list: "固定",
        slide: "横向滑动"
    }
});
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as ComboSuggestionsModule);
</script>
