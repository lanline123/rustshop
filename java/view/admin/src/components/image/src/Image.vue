/**
 * @file 图片展示组件
 * @description 封装el-image，支持预览大图和图片格式化
 * @features
 * - 点击预览大图
 * - 自动格式化图片URL
 * - 支持本地/远程图片
 */
<template>
    <el-image
        v-if="_image"
        :src="_image"
        :preview-src-list="[_imageBig]"
        :fit="props.fit"
        preview-teleported
        :style="'height:' + _height + ';' + 'width:' + _width + ';cursor:pointer'"
        hide-on-click-modal
        :class="props.class"
    />
</template>
<script setup lang="ts">
import { toRefs, ref, computed } from "vue";
import { imageFormat } from "@/utils/format";

/** 预览弹窗显示状态 */
const visible = ref<boolean>(false);
/** 图片是否已加载 */
const loaded = ref<boolean>(false);

const props = defineProps({
    /** 图片源地址 */
    src: { type: String, default: "" },
    /** 大图地址（预览时使用） */
    big: { type: String, default: "" },
    /** 样式类 */
    class: { type: String, default: "" },
    /** 图片宽度 */
    width: { type: [Number, String], default: "auto" },
    /** 图片高度 */
    height: { type: [Number, String], default: "auto" },
    /** 图片填充模式 */
    fit: { type: String, default: "contain" },
    /** 是否为本地图片 */
    isLocal: { type: Boolean, default: false }
});

const { src, big } = toRefs(props);

/** 计算宽度（带px单位） */
const _width = computed(() => {
    if (props.width === undefined) {
        return "auto";
    } else {
        return props.width + "px";
    }
});

/** 计算高度（带px单位） */
const _height = computed(() => {
    if (props.height === undefined) {
        return "auto";
    } else {
        return props.height + "px";
    }
});

/** 计算格式化后的图片URL */
const _image = computed(() => {
    return imageFormat(src.value, props.isLocal);
});

/** 计算格式化后的大图URL */
const _imageBig = computed(() => {
    return big.value ? imageFormat(big.value, props.isLocal) : imageFormat(src.value, props.isLocal);
});

/**
 * 设置预览弹窗显示状态
 * @param {any} value - 显示状态
 */
const setVisible = (value: any): void => {
    loaded.value = true;
    visible.value = value;
};
</script>
