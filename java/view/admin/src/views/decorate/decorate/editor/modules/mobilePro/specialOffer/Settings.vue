<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>天天特价</h3>
            <div class="dec-edit-desc">带倒计时的商品滑动组件, 支持多种风格选择, 倒计时支持每日定时</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'specialOffer'">
            <div class="dec-edit-title-box">
                <div class="title">商品设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品来源</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productSource">
                        <el-radio-button :value="'products'">手动选择</el-radio-button>
                        <el-radio-button :value="'productGroup'">商品分组</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block" v-if="module.productSource == 'products'">
                <div class="dec-edit-group-title">
                    <div class="label">选择商品</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-goods-group">
                        <ProductInfoSelect v-model:productList="module.productList" :isMultiple="true"></ProductInfoSelect>
                    </div>
                </div>
                <div class="dec-edit-group-desc">
                    <div>提示：您可以通过拖拽进行商品排序</div>
                </div>
            </div>
            <div class="dec-edit-group-con" v-if="module.productSource == 'productGroup'">
                <ProductGroupSelect v-model:productGroups="module.productGroups"></ProductGroupSelect>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">模块颜色风格</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.moduleColorStyle" @change="resetColor">
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
            <template v-if="module.showProdcutTitles === 1">
                <div v-for="(item, index) in module.prodcutTitles" :key="index">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">商品{{ index + 1 }}标题</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="item[`title${index + 1}`]" placeholder="请输入标题内容"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">商品{{ index + 1 }}卖点</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="item[`sellingPoints${index + 1}`]" placeholder="请输入卖点内容"></el-input>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
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
                        <div class="label">顶部右侧展示</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.moduleTitle.topRightType">
                            <el-radio-button :value="'countdown'">倒计时</el-radio-button>
                            <el-radio-button :value="'button'">按钮</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <template v-if="module.moduleTitle.topRightType === 'countdown'">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">倒计时文案</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="module.moduleTitle.countdownTitle" placeholder="请输入倒计时文案"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">倒计时类型</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group" v-model="module.moduleTitle.countdownType">
                                <el-radio-button :value="'dailyCycle'">每日循环</el-radio-button>
                                <el-radio-button :value="'specifyTime'">指定时间</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                    <div class="dec-edit-group dec-edit-group-block" v-if="module.moduleTitle.countdownType === 'dailyCycle'">
                        <div class="dec-edit-group-title">
                            <div class="label">每日活动开始时间</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-time-picker
                                v-model="module.moduleTitle.dailyTime"
                                is-range
                                arrow-control
                                range-separator="至"
                                start-placeholder="开始时间"
                                end-placeholder="结束时间"
                                style="width: 330px"
                                @change="handleTimeChange"
                            />
                        </div>
                    </div>
                    <div class="dec-edit-group" v-if="module.moduleTitle.countdownType === 'specifyTime'">
                        <div class="dec-edit-group-title">
                            <div class="label">每日活动开始时间</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-date-picker v-model="module.moduleTitle.specifyTime" type="datetime" placeholder="请选择日期" @change="handleTimeChange" />
                        </div>
                    </div>
                </template>
                <template v-if="module.moduleTitle.topRightType === 'button'">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">按钮文案</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="module.moduleTitle.buttonText" placeholder="请输入按钮文案"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">按钮跳转链接</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-link-group">
                                <div class="lyecs-link-select">
                                    <SelectLink v-model="module.moduleTitle.buttonLink" decorateType="mobile"></SelectLink>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </div>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
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
                    <div class="label">商品宽度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productWidth"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="50"
                            :min="25"
                        />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>

            <div class="dec-edit-title-box">
                <div class="title">自定义商品标题/卖点</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">是否显示</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProdcutTitles">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">商品名称</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1 && module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">商品名称大小</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productNameSize"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="16"
                            :min="12"
                        />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1 && module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">商品名称粗细</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.productNameWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productNameWeight">
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
            <div class="dec-edit-group" v-if="module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">商品卖点</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSellingPoints">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
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
            <div class="dec-edit-title-box">
                <div class="title">商品价格设置</div>
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
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">颜色设置</div>
            </div>
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
                    <div class="label">倒计时颜色</div>
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
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">倒计时文案颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.countdownTitleColor = ''">重置</a>
                            <SelectColor v-model:color="module.moduleColor.countdownTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.moduleTitle.topRightType === 'button'">
                <div class="dec-edit-group-title">
                    <div class="label">按钮渐变颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetButtonGradient">重置</a>
                            <div class="flex flex-align-center">
                                <div><SelectColor v-model:color="module.moduleColor.buttonGradientColorA"></SelectColor></div>
                                <div style="margin: 0 10px">-</div>
                                <div><SelectColor v-model:color="module.moduleColor.buttonGradientColorB"></SelectColor></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="button-gradient-tip" v-if="module.moduleTitle.topRightType === 'button'">提示：留空即跟随「模块颜色风格」自动变色</div>
            <div class="dec-edit-group" v-if="module.moduleTitle.topRightType === 'button'">
                <div class="dec-edit-group-title">
                    <div class="label">按钮文字颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.buttonTextColor = '#ffffff'">重置</a>
                            <SelectColor v-model:color="module.moduleColor.buttonTextColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品名称颜色</div>
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
                    <div class="label">商品卖点颜色</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.sellingPointsColor = ''">重置</a>
                            <SelectColor v-model:color="module.moduleColor.sellingPointsColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="specialOffer"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="specialOffer"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor, SelectLink } from "@/components/select";
import { ProductGroupSelect, ProductInfoSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed, watch } from "vue";
import CommonImageFillEdit from "@/views/decorate/decorate/src/modules/src/commonImageFill/Edit.vue";
import ContentStyleEdit from "@/views/decorate/decorate/src/modules/src/contentStyle/Edit.vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import { selectLabel, ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { TitleFontBaold, ModuleColorStyle } from "@/types/decorate/decorate.d";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { SpecialOfferModule } from "./types";
const tabs = ref<any[]>([
    { label: "商品设置", name: "specialOffer" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("specialOffer");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，
// 模板内 v-model 直接写该 reactive 对象（与旧 defineModel 等价，公共子组件零改造复用）
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as SpecialOfferModule);
const pushProductTitle = (num: number) => {
    // 初始化数组（如果不存在）
    if (!module.value.prodcutTitles) {
        module.value.prodcutTitles = [];
    }
    
    // 如果当前数组长度大于所需数量，则截断数组
    if (module.value.prodcutTitles.length > num) {
        module.value.prodcutTitles = module.value.prodcutTitles.slice(0, num);
    }
    
    // 如果当前数组长度小于所需数量，则添加新的项
    for (let index = module.value.prodcutTitles.length; index < num; index++) {
        module.value.prodcutTitles.push({
            [`title${index + 1}`]: "自定义大标题",
            [`sellingPoints${index + 1}`]: "自定义卖点"
        });
    }
};
// const visibleChange = (value: any) => {
//     let data = [];
//     if(value){
//         data = JSON.parse(JSON.stringify(module.value.moduleTitle.dailyTime));
//     }else{

//     }
//     console.log(111,data, module.value.moduleTitle.dailyTime);
// }
const handleTimeChange = (value: any) => {
    if (value.length === 2) {
        const startTimestamp = new Date(value[0]).getTime();
        const endTimestamp = new Date(value[1]).getTime();
        module.value.moduleTitle.dailyTime = [startTimestamp, endTimestamp];
    } else {
        const timestamp = new Date(value).getTime();
        module.value.moduleTitle.specifyTime = timestamp;
    }
};
watch(
    () => [module.value.productList, module.value.productSource, module.value.productGroups, module.value.moduleColorStyle],
    ([productList, productSource, productGroups, moduleColorStyle]) => {
        // 处理 productList
        if (productList && Array.isArray(productList)) {
            if (productList.length > 0) {
                pushProductTitle(productList.length);
            }
        }
        // 处理 productSource
        if (productSource === "productGroup") {
            pushProductTitle(6);
        }
        // 处理 productGroups
        if (productGroups && productGroups.length > 0) {
            const firstGroup: any = productGroups[0];
            if (firstGroup && typeof firstGroup === 'object' && firstGroup.hasOwnProperty('productNumType')) {
                if (firstGroup.productNumType === "number") {
                    pushProductTitle(firstGroup.productNum);
                } else {
                    pushProductTitle(firstGroup.productIds?.length);
                }
            }
        }
        // 处理 moduleColorStyle
        type ModuleThemeKey = keyof typeof ModuleTheme;
        if (moduleColorStyle && module.value.contentStyle.gradientColorA == '' && module.value.contentStyle.gradientColorB == '') {
            module.value.moduleColor.countdownTitleColor = ModuleTheme[moduleColorStyle as ModuleColorStyle].textColor;
            module.value.contentStyle.gradientColorA = ModuleTheme[moduleColorStyle as ModuleColorStyle].gradientColorA;
            module.value.contentStyle.gradientColorB = ModuleTheme[moduleColorStyle as ModuleColorStyle].gradientColorB;
        }
    },
    { deep: true, immediate: true }
);

const resetColor = (e: ModuleColorStyle) => {
    module.value.moduleColor.countdownTitleColor = ModuleTheme[e as ModuleColorStyle].textColor;
    module.value.contentStyle.gradientColorA = ModuleTheme[e as ModuleColorStyle].gradientColorA;
    module.value.contentStyle.gradientColorB = ModuleTheme[e as ModuleColorStyle].gradientColorB;
};
const resetButtonGradient = () => {
    module.value.moduleColor.buttonGradientColorA = "";
    module.value.moduleColor.buttonGradientColorB = "";
};
</script>
<style lang="less" scoped>
.button-gradient-tip {
    padding: 0 16px 10px;
    font-size: 12px;
    line-height: 18px;
    color: #999;
}
</style>
