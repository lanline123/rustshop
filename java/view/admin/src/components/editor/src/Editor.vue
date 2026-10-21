/**
 * @file 富文本编辑器组件
 * @description 基于wangEditor封装的富文本编辑器，集成图片相册选择
 * @features
 * - 富文本编辑功能
 * - 从图片相册插入图片
 * - 统一的行高和样式
 */
<template>
    <div :style="border ? 'border: 1px solid #eee' : ''">
        <Toolbar style="border-bottom: 1px solid #eee" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
        <Editor
            :style="'height: ' + height + '; overflow-y: hidden;'"
            @onChange="onChange"
            v-model="html"
            :defaultConfig="editorConfig"
            :mode="mode"
            @onCreated="handleCreated"
        />
    </div>
    <DialogForm type="gallery" ref="galleryDialog" class="item-bg" @okCallback="onEdit" :params="{ isMultiple: true }"> </DialogForm>
</template>
<script setup lang="ts">
import "@wangeditor/editor/dist/css/style.css";
import { ref, onMounted, onBeforeUnmount, shallowRef, toRefs } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    /** HTML内容 */
    html: { type: String, default: "<p><br></p>" },
    /** 编辑器高度 */
    height: { type: String, default: "500px" },
    /** 是否显示边框 */
    border: { type: Boolean, default: true }
});

/** 编辑器HTML内容 */
const html = ref(props.html);
/** 图片相册弹窗引用 */
const galleryDialog = shallowRef();
/** 视频相册弹窗引用 */
const galleryVideoDialog = shallowRef();
const emit = defineEmits(["update:html"]);

/** 编辑器实例，必须用shallowRef */
const editorRef = shallowRef();
/** 编辑器模式 */
const mode = "default";

onMounted(() => {
    emit("update:html", html.value);
});

/** 图片插入函数引用 */
const fn = ref();

/** 工具栏配置 */
const toolbarConfig = {
    excludeKeys: ["fullScreen", "group-video"]
};

/** 编辑器配置 */
const editorConfig = {
    placeholder: t('请输入内容...'),
    lineHeight: 1.5,
    MENU_CONF: {
        uploadImage: {
            /**
             * 自定义图片选择
             * @param {Function} insertFn - 插入图片的回调函数
             */
            customBrowseAndUpload(insertFn: any) {
                galleryDialog.value.show();
                fn.value = insertFn;
            }
        }
    }
};

/** 内容变更回调 */
const onChange = (editor: any) => {
    emit("update:html", editor.getHtml());
};

/**
 * 图片选择回调
 * @param {any} result - 选中的图片列表
 */
const onEdit = (result: any) => {
    for (let idx in result) {
        fn.value(imageFormat(result[idx].picUrl), "", "");
    }
};

// 组件销毁时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value;
    if (editor == null) return;
    editor.destroy();
});

/**
 * 编辑器创建完成回调
 * @param {any} editor - 编辑器实例
 */
const handleCreated = (editor: any) => {
    editorRef.value = editor;
};
</script>
<style lang="less">
.w-e-text-container {
    em {
        font-style: italic !important;
    }

    // 设置默认行高为1.5
    line-height: 1.5 !important;

    // 移除所有元素的默认margin，确保编辑时和前端渲染时格式一致
    * {
        margin: 0 !important;
    }

    // 当没有设置行高样式时，使用默认行高1
    p:not([style*="line-height"]),
    div:not([style*="line-height"]),
    h1:not([style*="line-height"]),
    h2:not([style*="line-height"]),
    h3:not([style*="line-height"]),
    h4:not([style*="line-height"]),
    h5:not([style*="line-height"]),
    h6:not([style*="line-height"]) {
        line-height: 1.5 !important;
    }

    // 移除段落、标题、div等常见元素的margin
    p,
    div,
    h1,
    h2,
    h3,
    h4,
    h5,
    h6,
    ul,
    ol,
    li,
    blockquote,
    pre,
    code {
        margin: 0 !important;
    }
}

// 针对编辑器内容区域，确保样式生效
:deep(.w-e-text-container) {
    line-height: 1.5 !important;

    * {
        margin: 0 !important;
    }

    // 当没有设置行高样式时，使用默认行高1.5
    p:not([style*="line-height"]),
    div:not([style*="line-height"]),
    h1:not([style*="line-height"]),
    h2:not([style*="line-height"]),
    h3:not([style*="line-height"]),
    h4:not([style*="line-height"]),
    h5:not([style*="line-height"]),
    h6:not([style*="line-height"]) {
        line-height: 1.5 !important;
    }

    p,
    div,
    h1,
    h2,
    h3,
    h4,
    h5,
    h6,
    ul,
    ol,
    li,
    blockquote,
    pre,
    code {
        margin: 0 !important;
    }
}
</style>
