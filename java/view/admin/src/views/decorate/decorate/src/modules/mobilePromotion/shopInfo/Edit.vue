<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>{{ $t('店铺信息') }}</h3>
        </div>
        <div class="dec-edit-group special">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('指示器样式') }}</div>
                <div class="value">{{ shopInfoStyle[module.style as ModuleTypeStyle] }}</div>
            </div>
            <div class="dec-edit-group-con">
                <el-radio-group class="dec-radio-group" v-model="module.style">
                    <el-radio-button :value="1">{{ $t('样式一') }}</el-radio-button>
                    <el-radio-button :value="2">{{ $t('样式二') }}</el-radio-button>
                    <el-radio-button :value="3">{{ $t('样式三') }}</el-radio-button>
                    <el-radio-button :value="4">{{ $t('样式四') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="dec-edit-group special">
            <div class="dec-edit-group-title">
                <div class="label">{{ $t('背景图') }}</div>
                <div class="value">{{ picText[module.backgroundDefault as backgroundDefault] }}</div>
            </div>
            <div class="default-pic-box">
                <div
                    class="default-pic-box-item"
                    @click="handleChangeBg(index + 1)"
                    :class="{ active: module.backgroundDefault == index + 1 }"
                    v-for="(item, index) in imgList"
                    :key="index"
                >
                    <img class="img-box" :src="item" />
                </div>
            </div>
            <div class="custom-btn">
                <el-radio-group class="dec-radio-group" v-model="module.backgroundDefault">
                    <el-radio-button :value="0">{{ $t('自定义') }}</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <template v-if="module.backgroundDefault == 0">
            <div class="shop-info-title">{{ $t('自定义背景图片') }}</div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label special">{{ $t('背景图片') }}</div>
                    <div class="value"></div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.customPic"></PicSelect>
                </div>
            </div>
        </template>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import CommonFrameEdit from "../../src/commonFrame/Edit.vue";
import { PicSelect } from "@/components/decorate";
import { ModuleType, backgroundDefault, ModuleTypeStyle } from "@/types/decorate/decorate.d";
import { ref, watch } from "vue";
import img1 from "@/views/decorate/decorate/src/img/bg/1.jpg";
import img2 from "@/views/decorate/decorate/src/img/bg/2.jpg";
import img3 from "@/views/decorate/decorate/src/img/bg/3.jpg";
import img4 from "@/views/decorate/decorate/src/img/bg/4.jpg";
import img5 from "@/views/decorate/decorate/src/img/bg/5.jpg";
import img6 from "@/views/decorate/decorate/src/img/bg/6.jpg";
import img7 from "@/views/decorate/decorate/src/img/bg/7.jpg";
import img8 from "@/views/decorate/decorate/src/img/bg/8.jpg";
import img9 from "@/views/decorate/decorate/src/img/bg/9.jpg";
import img10 from "@/views/decorate/decorate/src/img/bg/10.jpg";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const imgList = ref([img1, img2, img3, img4, img5, img6, img7, img8, img9, img10]);
const module = defineModel<ModuleType>("module", { default: () => ({}) });
console.log("module", module.value);
const shopInfoStyle = ref({
    1: t('样式一'),
    2: t('样式二'),
    3: t('样式三'),
    4: t('样式四')
});

const picText = ref({
    0: t('自定义'),
    1: t('风格一'),
    2: t('风格二'),
    3: t('风格三'),
    4: t('风格四'),
    5: t('风格五'),
    6: t('风格六'),
    7: t('风格七'),
    8: t('风格八'),
    9: t('风格九'),
    10: t('风格十')
});
const handleChangeBg = (val: number) => {
    module.value.backgroundDefault = val;
};
</script>
<style lang="less">
.dec-edit-group {
    &.special {
        flex-direction: column;

        .dec-edit-group-title {
            margin-bottom: 10px;
        }

        .dec-radio-group {
            width: 100%;
        }
    }
}

.label {
    &.special {
        position: relative;
        top: 10px;
    }
}

.shop-info-title {
    padding: 16px;
    font-size: 14px;
    color: #969799;
    white-space: nowrap;
}

.default-pic-box {
    box-sizing: border-box;
    width: inherit;
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    grid-template-rows: repeat(2, 1fr);
    gap: 5px 10px;
    .default-pic-box-item {
        height: 35px;
        border: 1px solid #ebedf0;
        padding: 1px;
        &.active {
            border-color: #155bd4;
            background: #e0edff;
            color: #3773da;
        }
        &:hover {
            border-color: #155bd4;
            background: #e0edff;
            color: #3773da;
        }
        .img-box {
            width: 100%;
            height: 100%;
        }
    }
}
.custom-btn {
    margin-top: 10px;
    width: 25%;
}
</style>
