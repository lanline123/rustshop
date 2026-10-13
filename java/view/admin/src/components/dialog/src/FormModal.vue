/** 表单模态框组件 - 无触发器的弹窗，通过ref调用show/close方法控制 */
<template>
    <MyModal
        v-model:open="visible"
        :confirm-loading="confirmLoading"
        width="800px"
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
            <a-button style="margin-right: 8px" @click="close">{{ $t('取消') }}</a-button>
            <a-button :disabled="okDisabled" :loading="confirmLoading" type="primary" @click="onOk">{{ $t('确认') }}</a-button>
        </template>
    </MyModal>
</template>
<script setup lang="ts">
import { ref, defineAsyncComponent, h, toRaw, Comment } from "vue";
import zhCN from "ant-design-vue/es/locale/zh_CN";
import { Modal } from "ant-design-vue";
import { Drawer } from "ant-design-vue";

const props = defineProps({
    /** 组件路径（相对于views目录） */
    path: { type: String, default: "" },
    /** 传递给内部组件的参数 */
    params: { type: Object, default: () => ({}) },
    /** 确认回调函数 */
    okCallback: { type: Function, default: () => {} },
    /** 是否使用抽屉模式 */
    isDrawer: { type: Boolean, default: false }
});

/** 根据isDrawer决定使用Drawer或Modal */
const MyModal = props.isDrawer == true ? Drawer : Modal;

// 动态加载组件
const components = import.meta.glob("../../../views/**/*.vue");
const Component = defineAsyncComponent((components as any)["../../../views/" + props.path + ".vue"]);

/** 弹窗显示状态 */
const visible = ref<boolean>(false);
/** 确认按钮loading状态 */
const confirmLoading = ref<boolean>(false);
/** 确认按钮禁用状态 */
const okDisabled = ref<boolean>(false);
/** 传递给内部组件的参数 */
const params = Object.assign(props.params, { isDialog: true });

/** 显示弹窗 */
function show(e: any) {
    visible.value = true;
}

/** 关闭弹窗 */
function close(e: any) {
    visible.value = false;
}

/** 子组件表单引用 */
const submitForm = ref();

/**
 * 表单提交回调
 * @param {any} result - 提交结果
 */
const submitCallback = (result: any) => {
    visible.value = false;
    props.okCallback(result);
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
