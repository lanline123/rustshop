<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>倒计时</h3>
            <div class="dec-edit-desc">带倒计时的商品组件，支持天/时/分/秒/毫秒单独设置和显示</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'product'">
            <div class="dec-edit-title-box">
                <div class="title">请点击添加选择商品</div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-con">
                    <div class="dec-goods-group">
                        <ProductInfoSelect v-model:productList="module.pruductList" :isMultiple="true"></ProductInfoSelect>
                    </div>
                </div>
                <div class="dec-edit-group-desc">
                    <div>提示：您可以通过拖拽进行商品排序</div>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">模块颜色风格</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.moduleColorStyle">
                        <el-radio :value="'orange'">番茄橙</el-radio>
                        <el-radio :value="'red'">中国红</el-radio>
                        <el-radio :value="'blue'">天空蓝</el-radio>
                        <el-radio :value="'green'">森林绿</el-radio>
                        <el-radio :value="'purple'">紫罗兰</el-radio>
                        <el-radio :value="'grey'">深灰色</el-radio>
                        <el-radio :value="'chocolate'">巧克力</el-radio>
                        <el-radio :value="'brown'">马鞍棕</el-radio>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">模块标题设置</div>
            </div>
            <div v-if="module.moduleTitle">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">模块大标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                            <el-input v-model="module.moduleTitle.title" placeholder="请输入标题内容"></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">模块小标题</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                            <el-input v-model="module.moduleTitle.subTitle" placeholder="请输入标题内容"></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">右侧提示信息</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                            <el-input v-model="module.moduleTitle.moreText" placeholder="请输入右侧提示信息"></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">跳转链接</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-link-group">
                            <div class="lyecs-link-select">
                                <SelectLink v-model="module.moduleTitle.titleLink" decorateType="mobile"></SelectLink>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div v-if="module.countdownData">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">倒计时</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.countdownData.showCountdown">
                            <el-radio-button :value="1">显示</el-radio-button>
                            <el-radio-button :value="0">不显示</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block" v-if="module.countdownData.showCountdown == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">倒计时图片: 宽750, 高度不限</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-pic-group dec-picSingle-group">
                            <PicSelect v-model:photo="module.countdownData.countdownBackgroundPic"> </PicSelect>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.countdownData.showCountdown === 1">
                    <div class="dec-edit-group-title">
                        <div class="label">倒计时类型</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.countdownData.countdownType">
                            <el-radio-button :value="'dailyCycle'">每日循环</el-radio-button>
                            <el-radio-button :value="'specifyTime'">指定时间</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.countdownData.countdownType === 'dailyCycle' && module.countdownData.showCountdown == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">每日循环时间</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-time-picker v-model="module.countdownData.dailyTime" placeholder="请选择时间" @change="handleTimeChange" />
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.countdownData.countdownType === 'specifyTime' && module.countdownData.showCountdown === 1">
                    <div class="dec-edit-group-title">
                        <div class="label">指定时间</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-date-picker v-model="module.countdownData.specifyTime" type="datetime" placeholder="请选择日期" @change="handleTimeChange" />
                    </div>
                </div>
                <div class="dec-edit-group dec-edit-group-block" v-if="module.countdownData.showCountdown == 1">
                    <div class="dec-edit-group-title">
                        <div class="label">选择时间显示内容</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-checkbox-group :modelValue="module.countdownData.timeContent" @change="handleChange">
                            <el-checkbox label="天" value="天" />
                            <el-checkbox label="时" value="时" />
                            <el-checkbox label="分" value="分" />
                            <el-checkbox label="秒" value="秒" />
                        </el-checkbox-group>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">顶部距离</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.countdownData.marginTop"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="100"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">倒计时图片上浮距离</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.countdownData.countdownPicBottom"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="40"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">每屏显示数量</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.rowNum">
                        <el-radio-button :value="2">2个</el-radio-button>
                        <el-radio-button :value="2.5">2个半</el-radio-button>
                        <el-radio-button :value="3">3个</el-radio-button>
                        <el-radio-button :value="3.5">3个半</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片比例</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picScale">
                        <el-radio-button :value="'3:2'">3:2</el-radio-button>
                        <el-radio-button :value="'1:1'">1:1</el-radio-button>
                        <el-radio-button :value="'3:4'">3:4</el-radio-button>
                        <el-radio-button :value="'16:9'">16:9</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <CommonImageFillEdit v-model="module.picFillType"></CommonImageFillEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">标题对齐方式</div>
                    <div class="value">{{ selectLabel.textAlignment[module.textAlignment as TextAlignmentType] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.textAlignment">
                        <el-tooltip effect="light" placement="bottom" content="左对齐" :show-after="100">
                            <el-radio-button :value="'left'"><i class="iconfont-admin icon-alignLeft"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="居中对齐" :show-after="100">
                            <el-radio-button :value="'center'"><i class="iconfont-admin icon-center"></i></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品内部间距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品圆角半径</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片底部圆角</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.picBottomRadius">
                        <el-radio-button :value="1">直角</el-radio-button>
                        <el-radio-button :value="2">圆角</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品标题</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品价格</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductPrice">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">商品价格粗细</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.priceWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.priceWeight">
                        <el-tooltip effect="light" placement="bottom" content="常规体" :show-after="100">
                            <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="加粗体" :show-after="100">
                            <el-radio-button :value="700"
                                ><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品划线价</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.crossedOutPrice">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">颜色设置</div>
            </div>
            <div v-if="module.moduleColor">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">模块大标题颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.titleColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.titleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">模块小标题颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.subTitleColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.subTitleColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">提示信息颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.tipsColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.tipsColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品背景颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.productBackgroundColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.productBackgroundColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group" v-if="module.showProductName === 1">
                    <div class="dec-edit-group-title">
                        <div class="label">商品标题颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.productNameColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.productNameColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品价格颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.productPriceColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.productPriceColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">商品划线价颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.crossedOutPriceColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.crossedOutPriceColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-divider-line"></div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">倒计时文本颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.countdownColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.countdownColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="countdown"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor, SelectLink } from "@/components/select";
import { ProductInfoSelect, PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed, watch } from "vue";
import CommonImageFillEdit from "@/views/decorate/decorate/src/modules/src/commonImageFill/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import { selectLabel, ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { TextAlignmentType, TitleFontBaold } from "@/types/decorate/decorate.d";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { CountdownModule } from "./types";
const tabs = ref<any[]>([
    { label: "商品设置", name: "product" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("product");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as CountdownModule);
const handleTimeChange = (value: any) => {
    const timestamp = new Date(value).getTime();
    if (module.value.countdownData.countdownType === "dailyCycle") {
        module.value.countdownData.dailyTime = timestamp;
    } else {
        const timestamp = new Date(value).getTime();
        module.value.countdownData.specifyTime = timestamp;
    }
};
type ModuleThemeKey = keyof typeof ModuleTheme;
watch(
    () => module.value.moduleColorStyle,
    (val: ModuleThemeKey | undefined) => {
        if (val) {
            console.log(ModuleTheme[val]);
            module.value.countdownData.countdownBackgroundPic = ModuleTheme[val].countdownBackgroundPic;
            module.value.moduleColor.productPriceColor = ModuleTheme[val].textColor;
            module.value.contentStyle.gradientColorA = ModuleTheme[val].gradientColorA;
            module.value.contentStyle.gradientColorB = ModuleTheme[val].gradientColorB;
        }
    },
    { deep: true, immediate: true }
);
const handleChange = (value: any[]) => {
    const order = ["天", "时", "分", "秒"];
    module.value.countdownData.timeContent = value.sort((a, b) => {
        return order.indexOf(a) - order.indexOf(b);
    });
};
</script>
