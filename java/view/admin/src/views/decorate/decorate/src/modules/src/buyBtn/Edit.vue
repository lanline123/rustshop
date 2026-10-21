<template>
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('购买按钮') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="buyBtnStyle.showBtn">
                <el-radio-button :value="0">{{ $t('不显示') }}</el-radio-button>
                <el-radio-button :value="1">{{ $t('显示') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div v-if="buyBtnStyle.showBtn === 1">
        <div class="dec-edit-group dec-edit-group-block">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('购买按钮图标样式') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="buyBtnStyle.btnType">
                    <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                    <el-radio-button :value="5">{{ $t('自定义图片') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="buyBtnStyle.btnType === 5">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('自定义图标图片') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-pic-group dec-picSingle-group">
                    <PicSelect v-model:photo="buyBtnStyle.iconImage"></PicSelect>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="buyBtnStyle.btnType !== 0">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图标尺寸') }}</div>
                <div class="value">{{ buyBtnStyle.iconSize ?? 14 }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="buyBtnStyle.iconSize" show-input :show-input-controls="false" size="small" input-size="default" :min="12" :max="40" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字内容') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-input-group">
                    <el-input v-model="buyBtnStyle.text" :placeholder="$t('请输入按钮文字内容')"></el-input>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="showWrapOption">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('按钮换行') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="buyBtnStyle.wrap">
                    <el-radio-button :value="0">{{ $t('不换行') }}</el-radio-button>
                    <el-radio-button :value="1">{{ $t('换行') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="showWrapOption && buyBtnStyle.wrap === 1">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('换行后对齐') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="buyBtnStyle.wrapAlign">
                    <el-radio-button :value="1">{{ $t('居左') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('居中') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('居右') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group" v-if="buyBtnStyle.text !== ''">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字字号') }}</div>
                <div class="value">{{ buyBtnStyle.textSize ?? 12 }}px</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="buyBtnStyle.textSize" show-input :show-input-controls="false" size="small" input-size="default" :min="10" :max="24" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('文字颜色') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="buyBtnStyle.textColor = defaultBuyButtonStyle.textColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="buyBtnStyle.textColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="buyBtnStyle.btnType !== 0">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('图标颜色') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="buyBtnStyle.iconColor = defaultBuyButtonStyle.iconColor">{{ $t('重置') }}</a>
                        <SelectColor v-model:color="buyBtnStyle.iconColor"></SelectColor>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景渐变方向') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="buyBtnStyle.gradientType">
                    <el-tooltip effect="light" placement="bottom" :content="$t('上下')" :show-after="100">
                        <el-radio-button :value="'upDown'">{{ $t('上下') }}</el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('左右')" :show-after="100">
                        <el-radio-button :value="'leftRight'">{{ $t('左右') }}</el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('斜向')" :show-after="100">
                        <el-radio-button :value="'diagonal'">{{ $t('斜向') }}</el-radio-button>
                    </el-tooltip>
                    <el-tooltip effect="light" placement="bottom" :content="$t('纯色')" :show-after="100">
                        <el-radio-button :value="'purity'">{{ $t('纯色') }}</el-radio-button>
                    </el-tooltip>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景渐变颜色') }}</div>
                <div class="value"></div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-color-group">
                    <div class="dec-color-button">
                        <a class="dec-color-reset" @click="resetColor">{{ $t('重置') }}</a>
                        <div class="flex flex-align-center">
                            <div><SelectColor v-model:color="buyBtnStyle.gradientColorA"></SelectColor></div>
                            <div style="margin: 0 10px">-</div>
                            <div><SelectColor v-model:color="buyBtnStyle.gradientColorB"></SelectColor></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('按钮圆角') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="buyBtnStyle.radius" show-input :show-input-controls="false" size="small" input-size="default" :max="20" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('按钮高度') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="buyBtnStyle.paddingTopBottom" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                </div>
            </div>
        </div>
        <div class="dec-edit-group" v-if="buyBtnStyle.text !== ''">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('按钮宽度') }}</div>
            </div>
            <div class="dec-edit-group-con">
                <div class="dec-range-group">
                    <el-slider v-model="buyBtnStyle.paddingLeftRight" show-input :show-input-controls="false" size="small" input-size="default" :max="10" />
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, PropType, onMounted } from "vue";
import { SelectColor } from "@/components/select";
import { PicSelect } from "@/components/decorate";
import { BuyButtonStyleType } from "@/types/decorate/decorate.d";
import { defaultBuyButtonStyle } from "@/views/decorate/decorate/src/modules/";
const buyBtnStyle = defineModel<BuyButtonStyleType>("modelValue", { default: () => ({}) });
type typeString = "categoryA1" | "category_A2";
const props = defineProps({
    type: {
        type: String as PropType<typeString>,
        default: "categoryA1"
    },
    showWrapOption: {
        type: Boolean,
        default: false
    }
});
const resetColor = () => {
    buyBtnStyle.value.gradientColorA = defaultBuyButtonStyle.gradientColorA;
    buyBtnStyle.value.gradientColorB = defaultBuyButtonStyle.gradientColorB;
};

// 新增字段对历史草稿兜底：mergeDefaultModule 只浅合并，旧 buyBtnStyle 对象可能缺这些子字段，
// 直接绑定 el-slider / PicSelect 前先补默认值，避免控件绑定 undefined
onMounted(() => {
    if (buyBtnStyle.value.iconSize === undefined || buyBtnStyle.value.iconSize === null) {
        buyBtnStyle.value.iconSize = defaultBuyButtonStyle.iconSize;
    }
    if (buyBtnStyle.value.textSize === undefined || buyBtnStyle.value.textSize === null) {
        buyBtnStyle.value.textSize = defaultBuyButtonStyle.textSize;
    }
    if (!buyBtnStyle.value.iconImage || typeof buyBtnStyle.value.iconImage !== "object") {
        buyBtnStyle.value.iconImage = { picUrl: "", picThumb: "" };
    }
    if (buyBtnStyle.value.wrap === undefined || buyBtnStyle.value.wrap === null) {
        buyBtnStyle.value.wrap = defaultBuyButtonStyle.wrap;
    }
    if (buyBtnStyle.value.wrapAlign === undefined || buyBtnStyle.value.wrapAlign === null) {
        buyBtnStyle.value.wrapAlign = defaultBuyButtonStyle.wrapAlign;
    }
});
</script>
