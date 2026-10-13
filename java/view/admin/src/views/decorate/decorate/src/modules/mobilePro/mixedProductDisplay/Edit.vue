<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('商品混排') }}</h3>
            <div class="dec-edit-desc">{{ $t('支持双列选择显示商品，并支持倒计时') }}</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'productStting'">
            <ComboGroupEdit
                v-model="module.productGroups"
                :isMultiple="true"
                :showTitlePic="false"
                :showDesc="false"
                :showColor="false"
                :showClose="false"
                :showAdd="false"
                :showGroupType="false"
                :activeNames="[1, 2]"
            >
            </ComboGroupEdit>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('右侧: 自定小图: 宽200内, 高48') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-pic-group dec-picSingle-group">
                        <PicSelect v-model:photo="module.rightPic"> </PicSelect>
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-spread-title">
                <div class="title">{{ $t('倒计时设置') }}</div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.showCountdown">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.showCountdown === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时类型') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.countdownType">
                        <el-radio-button :value="'dailyCycle'">{{ $t('每日循环') }}</el-radio-button>
                        <el-radio-button :value="'specifyTime'">{{ $t('指定时间') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block" v-if="module.countdownType === 'dailyCycle' && module.showCountdown == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每日活动开始时间') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-time-picker
                        v-model="module.dailyTime"
                        is-range
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                        style="width: 330px"
                        @change="handleTimeChange"
                    />
                </div>
            </div>
            <div class="dec-edit-group" v-if="module.countdownType === 'specifyTime' && module.showCountdown === 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('每日活动开始时间') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-date-picker v-model="module.specifyTime" type="datetime" :placeholder="$t('请选择日期')" @change="handleTimeChange" />
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block" v-if="module.showCountdown == 1">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('选择显示内容') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-checkbox-group :modelValue="module.timeContent" @change="handleChange">
                        <el-checkbox :label="$t('时')" value="时" />
                        <el-checkbox :label="$t('分')" value="分" />
                        <el-checkbox :label="$t('秒')" value="秒" />
                    </el-checkbox-group>
                </div>
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
                    <div class="label">{{ $t('商品圆角') }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.productRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="40" />
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('更多链接占位') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.morePosition">
                        <el-radio-button :value="'top'">{{ $t('顶部标题区') }}</el-radio-button>
                        <el-radio-button :value="'auto'">{{ $t('标题+商品区') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-divider-line"></div>
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

            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品划线价') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.crossedOutPrice">
                        <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                        <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <!-- <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">商品销量</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.productSales">
                        <el-radio-button :value="1">显示</el-radio-button>
                        <el-radio-button :value="0">不显示</el-radio-button>
                    </el-radio-group>
                </div>
            </div> -->
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
                    <div class="label">{{ $t('卖点小标题颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.sellingPointColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.sellingPointColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时背景颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.countdownBackgroundColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.countdownBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('倒计时数字颜色') }}</div>
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
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('价格背景颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.priceBackgroundColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.priceBackgroundColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品价格颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.priceColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.priceColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('商品划线价颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.crossedOutPriceColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.crossedOutPriceColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('中间分割线颜色') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-color-group">
                        <div class="dec-color-button">
                            <a class="dec-color-reset" @click="module.moduleColor.lineColor = ''">{{ $t('重置') }}</a>
                            <SelectColor v-model:color="module.moduleColor.lineColor"></SelectColor>
                        </div>
                    </div>
                </div>
            </div>
            <ModuleStyleEdit v-model="module.moduleStyle" type="mixedProductDisplay"></ModuleStyleEdit>
            <ContentStyleEdit v-model="module.contentStyle" type="mixedProductDisplay"></ContentStyleEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import ComboGroupEdit from "../../src/comboGroup/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { SelectColor } from "@/components/select";
import CommonImageFillEdit from "../../src/commonImageFill/Edit.vue";
import Tabs from "@/components/tabs/Index.vue";
import { ref } from "vue";
import ContentStyleEdit from "../../src/contentStyle/Edit.vue";
import ModuleStyleEdit from "../../src/moduleStyle/Edit.vue";
import { selectLabel } from "@/views/decorate/decorate/src/modules/editIndex";
import { ModuleType, ModuleMixedProductDisplayType, TitleFontBaold } from "@/types/decorate/decorate.d";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const tabs = ref<any[]>([
    { label: t('商品设置'), name: "productStting" },
    { label: t('扩展设置'), name: "extend" }
]);
const activeName = ref("productStting");
const module = defineModel<ModuleType & ModuleMixedProductDisplayType>("module", { default: () => ({}) });
const handleTimeChange = (value: any) => {
    if (value.length === 2) {
        const startTimestamp = new Date(value[0]).getTime();
        const endTimestamp = new Date(value[1]).getTime();
        module.value.dailyTime = [startTimestamp, endTimestamp];
    } else {
        const timestamp = new Date(value).getTime();
        module.value.specifyTime = timestamp;
    }
};
const handleChange = (value: any[]) => {
    const order = [t('时'), t('分'), t('秒')];
    module.value.timeContent = value.sort((a, b) => {
        return order.indexOf(a) - order.indexOf(b);
    });
};
</script>
