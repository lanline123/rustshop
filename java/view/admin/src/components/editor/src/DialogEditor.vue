/** 弹窗编辑器组件 - 在弹窗中显示表单组件，支持相册等特殊类型 */
<template>
    <div @click="show" :class="'dialog-link ' + props.class" :style="'display:inline-flex;'">
        <slot></slot>
    </div>
    <MyModal
        v-if="loaded"
        :class="dialogClass"
        v-model:open="visible"
        :confirm-loading="confirmLoading"
        :width="_width"
        :title="_title"
        :okText="$t('确认')"
        :cancelText="$t('取消')"
        :destroyOnClose="true"
        @ok="onOk"
        :ok-button-props="{ disabled: okDisabled }"
        :footer-style="{ textAlign: 'right' }"
    >
        <a-config-provider :locale="zhCN">
            <Component
                ref="submitForm"
                @submitCallback="submitCallback"
                @show="show"
                @close="close"
                @okType="okType"
                v-bind="params"
                v-model:confirmLoading="confirmLoading"
            >
            </Component>
        </a-config-provider>
        <template #footer>
            <a-button style="margin-right: 12px" @click="close">{{ $t('取消') }}</a-button>
            <a-button :disabled="okDisabled" :loading="confirmLoading" type="primary" @click="onOk">{{ $t('确认') }}</a-button>
        </template>
    </MyModal>
</template>
<script setup lang="ts">
import { ref, defineAsyncComponent, shallowRef, computed } from "vue";
import zhCN from "ant-design-vue/es/locale/zh_CN";
import { Drawer, Modal } from "ant-design-vue";

/** 组件是否已加载 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loaded = ref(false);

const props = defineProps({
    /** 组件路径 */
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
    /** 是否使用抽屉模式 */
    isDrawer: { type: Boolean, default: false },
    /** 额外传参 */
    data: { type: Object, default: {} },
    /** 弹窗类型：gallery-相册 */
    type: String
});

const emit = defineEmits(["okCallback"]);

/** 根据isDrawer决定使用Drawer或Modal */
const MyModal = props.isDrawer == true ? Drawer : Modal;
/** 动态组件 */
const Component = shallowRef();
/** 弹窗显示状态 */
const visible = ref<boolean>(false);
/** 确认按钮loading状态 */
const confirmLoading = ref<boolean>(false);
/** 确认按钮禁用状态 */
const okDisabled = ref<boolean>(false);
const dialogClass = ref(props.dialogClass);
const _title = ref(props.title);
let _path = props.path;

/** 计算传递给内部组件的参数 */
const params = computed(() => {
    return Object.assign(props.params, { isDialog: true });
});

/** 计算弹窗宽度 */
const _width = computed(() => {
    if (props.type == "gallery") {
        return "811px";
    } else {
        return props.width;
    }
});

// 相册弹窗配置
if (props.type == "gallery") {
    dialogClass.value += " " + "lyecs-modal-gallery noPadding";
    _path = "gallery/Gallery";
    _title.value = t('相册');
}

/** 显示弹窗 */
function show(e: any) {
    if (loaded.value == false) {
        loadComponent();
    }
    loaded.value = true;
    visible.value = true;
}

/** 关闭弹窗 */
function close(e: any) {
    visible.value = false;
}

/** 子组件表单引用 */
const submitForm = ref();

/** 动态加载组件 */
async function loadComponent() {
    return new Promise((resolve, reject) => {
        const components = import.meta.glob("../../../views/**/*.vue");
        Component.value = defineAsyncComponent((components as any)["../../../views/" + _path + ".vue"]);
    });
}

/**
 * 表单提交回调
 * @param {any} result - 提交结果
 */
const submitCallback = (result: any) => {
    visible.value = false;
    confirmLoading.value = false;
    emit("okCallback", result, props.data);
};

/** 确认按钮点击 */
const onOk = () => {
    submitForm.value.onFormSubmit();
};

/**
 * 设置确认按钮状态
 * @param {boolean} type - 是否可点击
 */
const okType = (type: boolean) => {
    okDisabled.value = !type;
};

defineExpose({
    show,
    close
});
</script>
