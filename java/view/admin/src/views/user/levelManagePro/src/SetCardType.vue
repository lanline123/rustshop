/**
 * @file 会员卡面设置
 * @description 用于设置会员卡背景样式的弹窗组件，支持预设背景色和自定义图片两种模式。
 * @features
 * - 预设背景色选择（9种颜色方案）
 * - 自定义图片上传
 * - 实时预览会员卡效果
 * - 支持点击选择背景色
 */
<template>
    <div class="container flex">
        <el-form ref="formRef" :model="data" label-width="auto">
            <el-form-item prop="name" :label="$t('背景设置：')">
                <el-radio-group v-model="data.rankCardType">
                    <el-radio :value="1" size="large">{{ $t('背景色') }}</el-radio>
                </el-radio-group>
                <div class="card-check" :class="{ active: isShow }" ref="buttonRef" v-click-outside="onClickOutside">
                    <div class="card" :class="data.rankIco"></div>
                </div>
                <el-popover ref="popoverRef" width="200px" :virtual-ref="buttonRef" trigger="click" virtual-triggering @show="onShow" @hide="onHide">
                    <div class="card-list">
                        <div class="card-item" v-for="item in cardList" :class="item" @click="changeCard(item)"></div>
                    </div>
                </el-popover>
            </el-form-item>
            <el-form-item prop="name" label=" ">
                <el-radio-group v-model="data.rankCardType">
                    <el-radio :value="2" size="large">{{ $t('自定义图片') }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item prop="name" label=" ">
                <FormAddGallery v-model:photo="data.rankBg"></FormAddGallery>
                <div class="extra">{{ $t('尺寸：1000*525像素，小于{imageUploadMaxSize}M，支持jpg、png、jpeg格式', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
            </el-form-item>
        </el-form>
        <div v-if="data.rankBg !== '' && data.rankCardType == 2" class="card-bg" :style="{backgroundImage: 'url('+data.rankBg+')'}"> {{ $t('黄金会员') }} </div>
        <div v-else class="card-bg" :class="data.rankIco"> {{ $t('黄金会员') }} </div>
    </div>
</template>
<script setup lang="ts">
import "./card.less";
import { FormAddGallery } from "@/components/gallery";
import { ref, unref } from "vue";
import { ClickOutside as vClickOutside } from "element-plus";
import { useConfigStore } from "@/store/config";
/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const configStore = useConfigStore();
/** 背景色选择按钮引用 */
const buttonRef = ref();
/** 颜色选择弹出框引用 */
const popoverRef = ref();
/**
 * @description 点击外部时关闭弹出框
 */
const onClickOutside = () => {
    unref(popoverRef).popperRef?.delayHide?.();
};
/**
 * @property {Object} data - 会员卡面数据，包含rankCardType、rankIco、rankBg等字段
 */
const props = defineProps({
    data: {
        type: Object,
        default: {}
    }
});
/** 预设背景色列表 */
const cardList = ["card1", "card2", "card3", "card4", "card5", "card6", "card7", "card8", "card9"];
/** 颜色选择弹出框是否显示 */
const isShow = ref(false);
/**
 * @description 弹出框显示时的回调
 */
const onShow = () => {
    isShow.value = true;
};
/**
 * @description 弹出框隐藏时的回调
 */
const onHide = () => {
    isShow.value = false;
};
/**
 * @description 切换会员卡背景色
 * @param item 背景色类名
 */
const changeCard = (item: string) => {
    props.data.rankIco = item;
};
/**
 * @description 表单提交，返回修改后的卡面数据
 */
const onSubmit = async () => {
    emit("submitCallback", props.data);
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
body .el-form-item {
    margin-bottom: 0px;
}
.card-bg{
    width: 343px;
    height: 180px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: bold;
    box-sizing: border-box;
    padding: 17px 20px;
    color: #fff;
    position: relative;
    overflow: visible;
    background-repeat: no-repeat;
    background-position: 0% 0%;
    background-size: cover;
}
.card-check {
    width: 55px;
    height: 28px;
    border: 1px solid #ccc;
    border-radius: 2px;
    margin-left: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    .card {
        width: 45px;
        height: 20px;
    }
    .card1 {
        background: #75b06d;
    }
    .card2 {
        background: #47a779;
    }
    .card3 {
        background: #6ab7de;
    }
    .card4 {
        background: #6385bd;
    }
    .card5 {
        background: #9b6fc8;
    }
    .card6 {
        background: #c0996f;
    }
    .card7 {
        background: #eec154;
    }
    .card8 {
        background: #de5850;
    }
    .card9 {
        background: #798795;
    }
}
.card-list {
    width: 180px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    .card-item {
        width: 20px;
        height: 20px;
        background-color: red;
        border: 1px solid #ccc;
        &:hover {
            border: 1px solid #409eff;
        }
    }
    .card1 {
        background: #75b06d;
    }
    .card2 {
        background: #47a779;
    }
    .card3 {
        background: #6ab7de;
    }
    .card4 {
        background: #6385bd;
    }
    .card5 {
        background: #9b6fc8;
    }
    .card6 {
        background: #c0996f;
    }
    .card7 {
        background: #eec154;
    }
    .card8 {
        background: #de5850;
    }
    .card9 {
        background: #798795;
    }
}
.active {
    border: 1px solid #409eff;
}
</style>
