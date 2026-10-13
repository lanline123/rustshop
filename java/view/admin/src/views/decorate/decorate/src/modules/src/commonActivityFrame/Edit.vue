<template>
    <!-- ====== 内容设置（section: 'all' | 'content'） ====== -->
    <template v-if="section !== 'container'">
        <div class="dec-edit-group dec-edit-group-block" v-if="type != 'product'">
            <slot name="customizationTemplate">
                <div class="dec-edit-group-title">
                    <div class="label">{{ $t('选择模板') }}</div>
                    <div class="value">{{ selectLabel.style[frame.style || ""] }}</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="frame.style">
                        <el-radio-button :value="1">{{ $t('一行一个') }}</el-radio-button>
                        <el-radio-button :value="2">{{ $t('一行两个') }}</el-radio-button>
                        <el-radio-button :value="5">{{ $t('详细列表') }}</el-radio-button>
                        <el-radio-button :value="6">{{ $t('横向滑动') }}</el-radio-button>
                    </el-radio-group>
                </div>
            </slot>
        </div>
        <div class="dec-divider-line" v-if="type != 'product'"></div>
        <CommonProductEdit v-if="type == 'product'" v-model="frame.products"></CommonProductEdit>
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品卡样式') }}</div>
                <div class="value">{{ selectLabel.goodsStyle[frame.goodsStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.goodsStyle">
                    <el-radio-button :value="1">{{ $t('标准') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('投影') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('描边') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('透明底') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="frame.style === 2">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('瀑布流') }}</div>
                <div class="value">{{ selectLabel.waterfall[frame.waterfall || 0] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.waterfall">
                    <el-radio-button :value="1">{{ $t('启用') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('否') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="!useGoodsRadiusSlider">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品卡边角') }}</div>
                <div class="value">{{ selectLabel.goodsRadioStyle[frame.goodsRadioStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.goodsRadioStyle">
                    <el-radio-button :value="1">{{ $t('直角') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('圆角') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-else>
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品卡圆角') }}</div>
                <div class="value">{{ frame.goodsRadius ?? 0 }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.goodsRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="!hideTextAlign">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文本对齐') }}</div>
                <div class="value">{{ selectLabel.textAlign[frame.textAlign || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.textAlign">
                    <el-radio-button :value="1">{{ $t('左对齐') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('居中对齐') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字粗细') }}</div>
                <div class="value">{{ selectLabel.textWeight[frame.textWeight || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.textWeight">
                    <el-radio-button :value="1">{{ $t('常规体') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('加粗体') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品名称行数') }}</div>
                <div class="value">{{ selectLabel.goodsNameRow[frame.goodsNameRow || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.goodsNameRow">
                    <el-radio-button :value="1">{{ $t('1行') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('2行') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品信息边距') }}</div>
                <div class="value">{{ selectLabel.goodsNamePadding[frame.goodsNamePadding || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.goodsNamePadding">
                    <el-radio-button :value="1">{{ $t('有边距') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('无边距') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品名称') }}</div>
                <div class="value">{{ selectLabel.showName[frame.showName || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.showName">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <slot name="afterShowName"></slot>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('显示商品价格') }}</div>
                <div class="value">{{ selectLabel.showPrice[frame.showPrice || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.showPrice">
                    <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <slot name="afterShowPrice"></slot>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('商品边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.goodsPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                </div>
            </div>
        </div>
        <slot name="afterGoodsPadding"></slot>
        <div class="dec-edit-group dec-edit-group-block" v-if="!hideBuyBtnStyle">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('购买按钮样式') }}</div>
                <div class="value">{{ selectLabel.buyBtnStyle[frame.buyBtnStyle || ""] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="frame.buyBtnStyle">
                    <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                    <el-radio-button :value="5">{{ $t('样式五') }}</el-radio-button>
                    <el-radio-button :value="6">{{ $t('样式六') }}</el-radio-button>
                    <el-radio-button :value="7">{{ $t('样式七') }}</el-radio-button>
                    <el-radio-button :value="8">{{ $t('样式八') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
    </template>

    <!-- ====== 容器设置（section: 'all' | 'container'） ====== -->
    <template v-if="section !== 'content'">
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group" need-refresh="true">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="frame.backgroundColor = transparentBackground ? '' : defaultFrame.backgroundColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="frame.backgroundColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('圆角') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.boxRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('内边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.innerPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('左右边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.boxPadding" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('上边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.boxPaddingTop" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('下边距') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="frame.boxPaddingBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="30" />
                </div>
            </div>
        </div>
    </template>
</template>
<script lang="ts" setup>
import { SelectColor } from "@/components/select";
import CommonProductEdit from "../commonProduct/Edit.vue";
import { ref } from "vue";
import { ModuleActivityType, ModuleType } from "@/types/decorate/decorate.d";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const frame = defineModel<ModuleActivityType & ModuleType>("modelValue", { default: () => ({}) });
defineProps({
    type: {
        type: String,
        default: ""
    },
    useGoodsRadiusSlider: {
        type: Boolean,
        default: false
    },
    transparentBackground: {
        type: Boolean,
        default: false
    },
    hideTextAlign: {
        type: Boolean,
        default: false
    },
    // productA1 等模块的 buyBtnStyle 为对象类型且自带 BuyBtnEdit，需隐藏此处的数字型按钮样式单选
    hideBuyBtnStyle: {
        type: Boolean,
        default: false
    },
    section: {
        type: String as () => "all" | "content" | "container",
        default: "all"
    }
});
const selectLabel = ref<any>({
    style: {
        1: t('一行一个'),
        2: t('一行两个'),
        5: t('详细列表'),
        6: t('横向滑动'),
    },
    goodsStyle: {
        1: t('标准'),
        2: t('投影'),
        3: t('描边'),
        4: t('透明底'),
    },
    goodsRadioStyle: {
        1: t('直角'),
        2: t('圆角'),
    },
    waterfall: {
        1: t('启用'),
        0: t('否'),
    },
    textAlign: {
        1: t('左对齐'),
        2: t('居中对齐'),
    },
    textWeight: {
        1: t('常规体'),
        2: t('加粗体'),
    },
    goodsNameRow: {
        1: t('1行'),
        2: t('2行'),
    },
    goodsNamePadding: {
        1: t('有边距'),
        0: t('无边距'),
    },
    showName: {
        1: t('显示'),
        0: t('不显示'),
    },
    showBrief: {
        1: t('显示'),
        0: t('不显示'),
    },
    showPrice: {
        1: t('显示'),
        0: t('不显示'),
    },
    buyBtnStyle: {
        0: t('不显示'),
        1: t('样式一'),
        2: t('显示'),
        3: t('显示'),
        4: t('显示'),
        5: t('显示'),
        6: t('显示'),
        7: t('显示'),
        8: t('显示'),
    },
});
</script>
