/** * @file 弹窗表单组件 * @description 核心弹窗组件，支持动态加载views目录下的Vue组件 * @features * - 支持 Modal/Drawer 两种模式 * - 动态异步加载组件 * -
内置图片/视频相册弹窗 * - 关闭前二次确认 * - 表单提交与回调 */
<template>
    <div @click.stop.shop="show" :class="'dialog-link ' + props.class" :style="props.style">
        <slot></slot>
    </div>
    <MyModal
        v-if="loaded"
        :class="dialogClass"
        v-model:open="visible"
        :confirm-loading="confirmLoading"
        :width="_width"
        :maskClosable="maskClosable"
        :title="showHeader && !hasTitleSlot ? (_type ? smTitle : title) : null"
        :okText="footerOkText"
        :cancelText="cancelText"
        :destroyOnClose="true"
        @ok="onOk"
        :ok-button-props="{ disabled: okDisabled }"
        :footer-style="{ textAlign: 'right', ...footerStyle }"
        v-bind="bindParams"
        :bodyStyle="bodyStyle"
        :centered="centered"
        :closable="showModalClose"
        @close="close"
        :style="modalStyle"
        :headerStyle="headerStyle"
        :keyboard="keyboard"
    >
        <!-- 自定义标题插槽 -->
        <template v-if="showHeader && ($slots.title || $slots.mytitle)" #title>
            <slot name="title"></slot>
        </template>
        <a-config-provider :locale="zhCN">
            <Component
                ref="submitForm"
                :visible="visible"
                @submitCallback="submitCallback"
                @callback="callback"
                @show="show"
                @close="close"
                @okType="okType"
                @okText="onOkText"
                @closeConfirm="onCloseConfirm"
                v-bind="params"
                v-model:confirmLoading="confirmLoading"
            >
            </Component>
        </a-config-provider>
        <!-- 自定义底部插槽：提供默认按钮作为 fallback -->
        <template v-if="props.showFooter !== false" #footer>
            <slot
                v-if="$slots.footer"
                name="footer"
                :close="close"
                :onOk="onOk"
                :confirmLoading="confirmLoading"
                :okDisabled="okDisabled"
                :visible="visible"
            ></slot>
            <template v-else>
                <el-button v-show="showClose" @click="close">{{ cancelText || $t('取消') }}</el-button>
                <el-button v-show="showOnOk" :disabled="okDisabled" :loading="confirmLoading" type="primary" @click="onOk">{{ $t(footerOkText || '确认') }}</el-button>
            </template>
        </template>
    </MyModal>
</template>
<script setup lang="ts">
import { ref, defineAsyncComponent, shallowRef, computed, onMounted, watch, createVNode, useSlots } from "vue";
import zhCN from "ant-design-vue/es/locale/zh_CN";
import { Drawer, Modal } from "ant-design-vue";
import { ExclamationCircleOutlined } from "@ant-design/icons-vue";
import { DRAWER_NESTED_Z_INDEX } from "@/utils/overlayZIndex";

/** 组件是否已加载 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loaded = ref(false);

const props = defineProps({
    /** 组件路径（相对于views目录） */
    path: String,
    /** 触发器样式类 */
    class: { type: String, default: "" },
    /** 弹窗样式类 */
    dialogClass: { type: String, default: "" },
    /** 传递给内部组件的参数 */
    params: { type: Object, default: {} },
    /** 弹窗宽度 */
    width: { type: String, default: "800px" },
    /** 弹窗标题 */
    title: { type: String, default: "" },
    /** 确认按钮文案，留空时用 i18n 的「确认」 */
    okText: { type: String, default: "" },
    /** 取消按钮文案，留空时用 i18n 的「取消」 */
    cancelText: { type: String, default: "" },
    /** 特殊类型时的标题 */
    smTitle: { type: String, default: "" },
    /** 是否居中显示 */
    centered: { type: Boolean, default: false },
    /** 是否使用抽屉模式 */
    isDrawer: { type: Boolean, default: false },
    /** 是否显示取消按钮 */
    showClose: { type: Boolean, default: true },
    /** 是否显示右上角关闭图标 */
    showModalClose: { type: Boolean, default: true },
    /** 是否显示确认按钮 */
    showOnOk: { type: Boolean, default: true },
    /** 是否显示底部 */
    showFooter: { type: Boolean, default: true },
    /** 点击蒙层是否关闭 */
    maskClose: { type: Boolean, default: true },
    /** 额外传参，回调时会回传 */
    data: { type: Object, default: {} },
    /** 弹窗内容区样式 */
    bodyStyle: { type: Object, default: {} },
    /** 弹窗整体样式 */
    modalStyle: { type: Object, default: {} },
    /** 底部样式 */
    footerStyle: { type: Object, default: {} },
    /** 头部样式 */
    headerStyle: { type: Object, default: {} },
    /** 触发器样式 */
    style: { type: [String, Object], default: "display:inline-flex;" },
    /** 是否显示头部 */
    showHeader: { type: Boolean, default: true },
    /** 点击蒙层是否允许关闭 */
    maskClosable: { type: Boolean, default: true },
    /** 是否禁用打开 */
    disabled: { type: Boolean, default: false },
    /** 是否支持ESC关闭 */
    keyboard: { type: Boolean, default: true },
    /** 是否启用关闭确认 */
    closeConfirmProp: { type: Boolean, default: false },
    /** 弹窗类型：gallery-图片相册 galleryVideo-视频相册 */
    type: String
});
const emit = defineEmits([
    "okCallback", //确认后回调
    "callback",
    "closeCallback",
    "close",
    "closeConfirmProp"
]);
const hasTitleSlot = computed(() => {
    const s = useSlots();
    return !!(s.title || s.mytitle);
});

/**
 * 嵌入态（商家端 app web-view 打开）：把侧边抽屉/弹窗统一渲染为全屏页样式，
 * 改善移动端编辑体验。仅影响嵌入访问，admin 网页端不受影响。
 * sessionStorage 由 SSO 中转页写入；并兼容直接带 ?embed=1 的访问。
 */
const isEmbed = (() => {
    try {
        if (sessionStorage.getItem("ssoEmbed") === "1") return true;
    } catch (e) {
        // ignore
    }
    return new URLSearchParams(window.location.search).get("embed") === "1";
})();
// 嵌入态统一用 Modal 承载并铺满视口，规避 Drawer 侧边定位
const MyModal = props.isDrawer == true && !isEmbed ? Drawer : Modal;
/** 动态组件 */
const Component = shallowRef();
/** 弹窗显示状态 */
const visible = defineModel("visible", { type: Boolean, default: false });
/** 确认按钮loading状态 */
const confirmLoading = ref<boolean>(false);
/** 底部主按钮文案，子组件可通过 okText 事件覆盖 */
const footerOkText = ref(props.okText);
/** 确认按钮禁用状态 */
const okDisabled = ref<boolean>(false);
/** 是否需要关闭确认 */
const closeConfirm = ref<boolean>(false);
/** 关闭确认文案 */
const closeConfirmText = ref<string>(t('退出后，当前修改的内容不会被保存'));
/** 弹窗样式类 */
const dialogClass = ref(props.dialogClass + (isEmbed ? " embed-fullscreen" : ""));

/** 绑定参数（控制footer显示） */
const bindParams = ref<{ footer?: string }>({});
if (props.showFooter === false) {
    bindParams.value.footer = "";
}
let _path = props.path;

/** 计算传递给内部组件的参数 */
const params = computed(() => {
    return Object.assign(props.params, { isDialog: true });
});

/** 计算弹窗宽度 */
const _width = computed(() => {
    if (isEmbed) {
        return "100vw";
    }
    if (props.type == "gallery" || props.type == "galleryVideo") {
        return "970px";
    } else {
        return props.width;
    }
});

/** 是否为特殊类型弹窗 */
const _type = ref(false);
/** 特殊类型标题 */
const smTitle = ref("");

// 图片相册弹窗配置
if (props.type == "gallery") {
    dialogClass.value += " " + "lyecs-modal-gallery noPadding";
    _path = "gallery/Gallery";
    _type.value = true;
    smTitle.value = t('素材管理-图片');
}
// 视频相册弹窗配置
if (props.type == "galleryVideo") {
    dialogClass.value += " " + "lyecs-modal-gallery noPadding";
    _path = "gallery/video/GalleryVideo";
    _type.value = true;
    smTitle.value = t('素材管理-视频');
}

/**
 * 显示弹窗
 * @description 首次显示时加载组件
 */
const show = (e: any) => {
    if (props.disabled) return;
    ensureLoaded();
    confirmLoading.value = false;
    visible.value = true;
};

/**
 * 关闭弹窗
 * @description 如果启用了关闭确认，会弹出确认框
 */
const close = (e: any) => {
    if (props.closeConfirmProp) {
        visible.value = true;
        emit("closeConfirmProp");
        return;
    }
    if (closeConfirm.value == true) {
        visible.value = true;
        Modal.confirm({
            // 抽屉默认 1000，关闭确认需盖在当前抽屉之上
            zIndex: DRAWER_NESTED_Z_INDEX,
            title: t('退出当前编辑？'),
            icon: createVNode(ExclamationCircleOutlined),
            content: createVNode("div", { style: "color:red;" }, closeConfirmText.value),
            onOk() {
                visible.value = false;
                closeConfirm.value = false;
            },
            onCancel() {
                console.log("Cancel");
            }
        });
        return;
    }
    emit("close");
    visible.value = false;
};

/**
 * 设置关闭确认状态
 * @param {boolean} value - 是否需要确认
 * @param {string} text - 确认提示文案
 */
const onCloseConfirm = (value: boolean, text?: string) => {
    closeConfirm.value = value;
    if (text) {
        closeConfirmText.value = text;
    }
};

// 监听弹窗关闭，触发回调
watch(
    () => visible.value,
    (val) => {
        if (val) {
            footerOkText.value = props.okText;
            ensureLoaded();
        } else {
            emit("closeCallback");
        }
    }
);

/** 子组件表单引用 */
const submitForm = ref();

/**
 * 动态加载组件
 * @description 根据path异步加载views目录下的组件
 */
async function loadComponent() {
    return new Promise((resolve, reject) => {
        const components = import.meta.glob("../../../views/**/*.vue");
        Component.value = defineAsyncComponent((components as any)["../../../views/" + _path + ".vue"]);
    });
}

function ensureLoaded() {
    if (loaded.value == false) {
        loadComponent();
        loaded.value = true;
    }
}

onMounted(() => {});

/**
 * 表单提交回调
 * @param {any} result - 提交结果
 * @param {boolean} isClose - 是否关闭弹窗
 */
const submitCallback = (result: any, isClose: boolean = true) => {
    if (isClose) {
        visible.value = false;
    }
    confirmLoading.value = false;
    emit("okCallback", result, props.data);
};

/**
 * 通用回调
 * @param {any} result - 回调数据
 */
const callback = (result: any) => {
    emit("callback", result, props.data);
};

/**
 * 确认按钮点击
 * @description 调用子组件的onFormSubmit方法
 */
const onOk = () => {
    submitForm.value?.onFormSubmit?.();
};

/**
 * 设置确认按钮状态
 * @param {boolean} type - 是否可点击
 */
const okType = (type: boolean) => {
    okDisabled.value = !type;
};

/** 子组件更新底部主按钮文案 */
const onOkText = (text: string) => {
    if (text) {
        footerOkText.value = text;
    }
};

defineExpose({
    show,
    close,
    onCloseConfirm,
    callback
});
</script>

<style lang="less">
/* 嵌入态（商家端 app web-view）：弹窗铺满视口，呈现整页编辑样式 */
.embed-fullscreen.ant-modal {
    top: 0;
    max-width: 100vw;
    margin: 0;
    padding: 0;
    width: 100vw !important;
    height: 100vh;
}
.embed-fullscreen.ant-modal .ant-modal-content {
    display: flex;
    flex-direction: column;
    height: 100vh;
    border-radius: 0;
}
.embed-fullscreen.ant-modal .ant-modal-body {
    flex: 1;
    overflow-y: auto;
}
</style>
