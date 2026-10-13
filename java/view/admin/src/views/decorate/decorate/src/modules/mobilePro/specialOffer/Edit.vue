<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('天天特价') }}</h3>
            <div class="dec-edit-desc">{{ $t('带倒计时的商品滑动组件, 支持多种风格选择, 倒计时支持每日定时') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'specialOffer'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品来源') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productSource">
                        <el-radio-button :value="'products'">{{ $t('手动选择') }}</el-radio-button>
                        <el-radio-button :value="'productGroup'">{{ $t('商品分组') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block" v-if="module.productSource == 'products'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('选择商品') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-goods-group">
                        <ProductInfoSelect v-model:productList="module.productList" :isMultiple="true"></ProductInfoSelect>
                    </div>
                </div>
                <div class="dec-edit-group-desc">
                    <div>{{ $t('提示：您可以通过拖拽进行商品排序') }}</div>
                </div>
            </div>
            <div class="dec-edit-group-con" v-if="module.productSource == 'productGroup'">
                <ProductGroupSelect v-model:productGroups="module.productGroups"></ProductGroupSelect>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块颜色风格') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.moduleColorStyle" @change="resetColor">
                        <el-radio :value="'orange'">{{ $t('番茄橙') }}</el-radio>
                        <el-radio :value="'red'">{{ $t('中国红') }}</el-radio>
                        <el-radio :value="'blue'">{{ $t('天空蓝') }}</el-radio>
                        <el-radio :value="'green'">{{ $t('森林绿') }}</el-radio>
                        <el-radio :value="'purple'">{{ $t('紫罗兰') }}</el-radio>
                        <el-radio :value="'grey'">{{ $t('深灰色') }}</el-radio>
                        <el-radio :value="'chocolate'">{{ $t('巧克力') }}</el-radio>
                        <el-radio :value="'brown'">{{ $t('马鞍棕') }}</el-radio>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('模块标题设置') }}</div>
            </div>
            <div v-if="module.moduleTitle">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('模块大标题') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                            <el-input v-model="module.moduleTitle.title" :placeholder="$t('请输入标题内容')"></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('模块小标题') }}</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                            <el-input v-model="module.moduleTitle.subTitle" :placeholder="$t('请输入标题内容')"></el-input>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">{{ $t('顶部右侧展示') }}</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <el-radio-group class="dec-radio-group" v-model="module.moduleTitle.topRightType">
                            <el-radio-button :value="'countdown'">{{ $t('倒计时') }}</el-radio-button>
                            <el-radio-button :value="'button'">{{ $t('按钮') }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
                <template v-if="module.moduleTitle.topRightType === 'countdown'">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('倒计时文案') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="module.moduleTitle.countdownTitle" :placeholder="$t('请输入倒计时文案')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('倒计时类型') }}</div>
                            <div class="value"></div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-radio-group class="dec-radio-group" v-model="module.moduleTitle.countdownType">
                                <el-radio-button :value="'dailyCycle'">{{ $t('每日循环') }}</el-radio-button>
                                <el-radio-button :value="'specifyTime'">{{ $t('指定时间') }}</el-radio-button>
                            </el-radio-group>
                        </div>
                    </div>
                    <div class="dec-edit-group dec-edit-group-block" v-if="module.moduleTitle.countdownType === 'dailyCycle'">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('每日活动开始时间') }}</div>
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
                            <div class="label">{{ $t('每日活动开始时间') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <el-date-picker v-model="module.moduleTitle.specifyTime" type="datetime" :placeholder="$t('请选择日期')" @change="handleTimeChange" />
                        </div>
                    </div>
                </template>
                <template v-if="module.moduleTitle.topRightType === 'button'">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('按钮文案') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="module.moduleTitle.buttonText" :placeholder="$t('请输入按钮文案')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('按钮跳转链接') }}</div>
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
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('参数设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('图片比例') }}</div>
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
                    <div class="label">{{ $t('商品宽度') }}</div>
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
                    <div class="label">{{ $t('商品圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>

            <div class="dec-spread-title">
                <div class="title">{{ $t('自定义商品标题/卖点') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('是否显示') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProdcutTitles">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <template v-if="module.showProdcutTitles === 1">
                <div v-for="(item, index) in module.prodcutTitles" :key="index">
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('商品{index1}标题', { index1: index + 1 }) }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="item[`title${index + 1}`]" :placeholder="$t('请输入标题内容')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('商品{index1}卖点', { index1: index + 1 }) }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group" dynamic-class=".image-ad-title-d">
                                <el-input v-model="item[`sellingPoints${index + 1}`]" :placeholder="$t('请输入卖点内容')"></el-input>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
            <div class="dec-edit-group" v-if="module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品名称') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductName">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductName === 1 && module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品名称大小') }}</div>
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
            <div class="dec-edit-group" v-if="module.showProductName === 1 && module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品名称粗细') }}</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.productNameWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productNameWeight">
                        <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                            <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                            <el-radio-button :value="700"
                                ><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProdcutTitles === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品卖点') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showSellingPoints">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
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
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('商品价格设置') }}</div>
            </div>

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品价格') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品价格粗细') }}</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.priceWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.priceWeight">
                        <el-tooltip effect="light" placement="bottom" :content="$t('常规体')" :show-after="100">
                            <el-radio-button :value="400"><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px"></i></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" :content="$t('加粗体')" :show-after="100">
                            <el-radio-button :value="700"
                                ><i class="iconfont-admin icon-bianji-ziti" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-spread-title">
                <div class="title">{{ $t('颜色设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块大标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.titleColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.titleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('模块小标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.subTitleColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.subTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.countdownColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.countdownColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时文案颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.countdownTitleColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.countdownTitleColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.moduleTitle.topRightType === 'button'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('按钮渐变颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="resetButtonGradient">{{ $t('重置') }}</a>
                            <div class="flex flex-align-center">
                                <div><SelectColor v-model:color="module.moduleColor.buttonGradientColorA"></SelectColor></div>
                                <div style="margin: 0 10px">-</div>
                                <div><SelectColor v-model:color="module.moduleColor.buttonGradientColorB"></SelectColor></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="button-gradient-tip" v-if="module.moduleTitle.topRightType === 'button'">{{ $t('提示：留空即跟随「模块颜色风格」自动变色') }}</div>
            <div class="dec-edit-group" v-if="module.moduleTitle.topRightType === 'button'">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('按钮文字颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.buttonTextColor = '#ffffff'">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.buttonTextColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品名称颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.productNameColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.productNameColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品卖点颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.sellingPointsColor = ''">{{ $t('重置') }}</a>
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
import { ref, defineModel, watch } from "vue";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { selectLabel, ModuleTheme } from "@/views/decorate/decorate/src/modules/editIndex";
import { ModuleType, ModuleSpecialOfferType, TitleFontBaold, ModuleColorStyle } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('商品设置'), name: "specialOffer" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("specialOffer");
const module = defineModel<ModuleType & ModuleSpecialOfferType>("module", { default: () => ({}) });
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
            [`title${index + 1}`]: t('自定义大标题'),
            [`sellingPoints${index + 1}`]: t('自定义卖点')
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
