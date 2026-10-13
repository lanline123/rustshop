<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>分组滑动</h3>
            <div class="dec-edit-desc">多组商品切换展示，可单独定义各分组背景颜色</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'product'">
            <div class="dec-edit-title-box">
                <div class="title">内容设置</div>
            </div>
            <GroupListEdit v-model="module.groupList" :isMultiple="true" title="添加一组"></GroupListEdit>
            <div class="dec-edit-title-box">
                <div class="title">参数设置</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分组展示宽度</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.groupWidth"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="100"
                            :min="60"
                        />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">分组上下圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.groupRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品图片占比</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productPicRatio"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="50"
                            :min="20"
                        />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品图片圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productPicRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片右侧圆角</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPicRightRadius">
                        <el-radio-button :value="1">圆角</el-radio-button>
                        <el-radio-button :value="0">直角</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">排名标签</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showRanking">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品标签</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showProductLabel">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
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
            <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">标题行数</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productNameRow">
                        <el-radio-button :value="1">一行</el-radio-button>
                        <el-radio-button :value="2">两行</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">标题粗细</div>
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
            <div class="dec-edit-group bg-grey" v-if="module.showProductName === 1">
                <div class="dec-edit-group-title">
                    <div class="label">标题大小</div>
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
            <div class="dec-edit-group bg-grey" v-if="module.showProductPrice == 1">
                <div class="dec-edit-group-title">
                    <div class="label">价格文字粗细</div>
                    <div class="value">{{ selectLabel.titleFontBold[module.productPriceWeight as TitleFontBaold] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPriceWeight">
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
            <div class="dec-edit-group bg-grey" v-if="module.showProductPrice == 1">
                <div class="dec-edit-group-title">
                    <div class="label">价格文字大小</div>
                    <div class="value">{{ selectLabel.bigTitleFontSize[module.productPriceSize as BigTitleFontSize] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productPriceSize">
                        <el-tooltip effect="light" placement="bottom" content="小(12号)" :show-after="100">
                            <el-radio-button :value="12"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="小(14号)" :show-after="100">
                            <el-radio-button :value="14"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 12px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="中(16号)" :show-after="100">
                            <el-radio-button :value="16"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 14px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                        <el-tooltip effect="light" placement="bottom" content="大(18号)" :show-after="100">
                            <el-radio-button :value="18"
                                ><i class="iconfont-admin icon-zitiyanse" style="font-size: 16px; font-weight: bold"></i
                            ></el-radio-button>
                        </el-tooltip>
                    </el-radio-group>
                </div>
            </div>
            <!-- <div class="dec-edit-group bg-grey" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">优惠价标签</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showDiscountLabel">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
            <div class="dec-edit-group bg-grey" v-if="module.showProductPrice === 1">
                <div class="dec-edit-group-title">
                    <div class="label">价格上距</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider
                            v-model="module.productPriceMarginTop"
                            show-input
                            :show-input-controls="false"
                            size="small"
                            input-size="default"
                            :max="30"
                            :min="5"
                        />
                    </div>
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
            <BuyBtnEdit v-model="module.buyBtnStyle"></BuyBtnEdit>
        </div>
        <div v-if="activeName === 'extend'">
            <div class="dec-edit-title-box">
                <div class="title">颜色设置</div>
            </div>
            <div v-if="module.moduleColor">
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">分组大标题大小</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.moduleColor.titleSize"
                                show-input
                                :show-input-controls="false"
                                size="small"
                                input-size="default"
                                :max="24"
                                :min="12"
                            />
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">分组副标题大小</div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-range-group">
                            <el-slider
                                v-model="module.moduleColor.subTitleSize"
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
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">大标题颜色</div>
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
                        <div class="label">副标题颜色</div>
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
                        <div class="label">标题图标：宽不限, 高40</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-pic-group dec-picSingle-group">
                            <PicSelect v-model:photo="module.moduleColor.titlePic"> </PicSelect>
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
                <div class="dec-edit-group">
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
                        <div class="label">标签背景颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.labelBackgroundColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.labelBackgroundColor"></SelectColor>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-edit-group">
                    <div class="dec-edit-group-title">
                        <div class="label">标签文字颜色</div>
                        <div class="value"></div>
                    </div>
                    <div class="dec-edit-group-con">
                        <div class="dec-color-group">
                            <div class="dec-color-button">
                                <a class="dec-color-reset" @click="module.moduleColor.labelTextColor = ''">重置</a>
                                <SelectColor v-model:color="module.moduleColor.labelTextColor"></SelectColor>
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
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="categoryRec"></ModuleStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import Tabs from "@/components/tabs/Index.vue";
import { ref, computed } from "vue";
import ModuleStyleEdit from "@/views/decorate/decorate/src/modules/src/moduleStyle/Edit.vue";
import BuyBtnEdit from "@/views/decorate/decorate/src/modules/src/buyBtn/Edit.vue";
import GroupListEdit from "@/views/decorate/decorate/src/modules/mobilePro/groupSliding/src/GroupListEdit.vue";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
import { TitleFontBaold, BigTitleFontSize } from "@/types/decorate/decorate.d";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { GroupSlidingModule } from "./types";
const tabs = ref<any[]>([
    { label: "商品设置", name: "product" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("product");
// store 单一数据源：activeModuleData 即 moduleList 内模块对象的引用，模板 v-model 直接写该 reactive 对象
const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as GroupSlidingModule);
</script>
