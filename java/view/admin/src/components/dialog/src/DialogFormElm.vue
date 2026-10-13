/** Element Plus版弹窗表单 - 使用ElDialog/ElDrawer实现的弹窗表单组件 */
<template>
    <div @click="show" :class="'dialog-link ' + props.class" :style="'display:inline-flex;'">
        <slot></slot>
    </div>
    <MyModal :modal-class="dialogClass" v-if="loaded" append-to-body v-model="visible" :confirm-loading="confirmLoading" :size="_width" :width="_width" :title="_title"
        :destroy-on-close="true" @ok="onOk">
        <Component ref="submitForm" @submitCallback="submitCallback" @show="show" @close="close" @okType="okType" v-bind="params" v-model:confirmLoading="confirmLoading">
        </Component>
        <template #footer>
            <a-button style="margin-right: 12px" @click="close">{{ $t('取消') }}</a-button>
            <a-button :disabled="okDisabled" :loading="confirmLoading" type="primary" @click="onOk">{{ $t('确认') }}</a-button>
        </template>
    </MyModal>
</template>
<script setup lang="ts">
import { ref, defineAsyncComponent, shallowRef } from 'vue';
import { ElDrawer, ElDialog } from 'element-plus'

/** 组件是否已加载 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const loaded = ref(false)

const props = defineProps({
    /** 组件路径（相对于views目录） */
    path: String,
    /** 触发器样式类 */
    class: { type: String, default: '' },
    /** 弹窗样式类 */
    dialogClass: { type: String, default: '' },
    /** 传递给内部组件的参数 */
    params: { type: Object, default: {} },
    /** 弹窗宽度 */
    width: { type: String, default: '800px' },
    /** 弹窗标题 */
    title: { type: String, default: '' },
    /** 是否使用抽屉模式 */
    isDrawer: { type: Boolean, default: false },
    /** 弹窗类型：gallery-相册 */
    type: String,
})

const emit = defineEmits(["okCallback"])

/** 根据isDrawer决定使用ElDrawer或ElDialog */
const MyModal = props.isDrawer == true ? ElDrawer : ElDialog
/** 动态组件 */
const Component = shallowRef();
const _class = ref(props.class);
/** 弹窗显示状态 */
const visible = ref<boolean>(false);
/** 确认按钮loading状态 */
const confirmLoading = ref<boolean>(false);
/** 确认按钮禁用状态 */
const okDisabled = ref<boolean>(false)
const dialogClass = ref(props.dialogClass)
const _width = ref(props.width)
const _title = ref(props.title)
let _path = props.path

/** 传递给内部组件的参数 */
const params = Object.assign(props.params, { isDialog: true });

// 相册弹窗配置
if (props.type == 'gallery') {
    dialogClass.value += ' ' + 'lyecs-modal-gallery noPadding'
    _width.value = '811px';
    _path = 'gallery/Gallery';
    _title.value = t('相册');
}

/** 显示弹窗 */
function show(e:any) {
    if (loaded.value == false) {
        loadComponent()
    }
    loaded.value = true;
    visible.value = true;
}

/** 关闭弹窗 */
function close(e:any) {
    visible.value = false;
}

/** 子组件表单引用 */
const submitForm = ref();

/** 动态加载组件 */
async function loadComponent() {
    return new Promise((resolve, reject) => {
        const components = import.meta.glob("../../../views/**/*.vue");
        Component.value = defineAsyncComponent((components as any)['../../../views/' + _path + '.vue'])
    });
}

/**
 * 表单提交回调
 * @param {any} result - 提交结果
 */
const submitCallback = (result: any) => {
    if (!result.code) {
        visible.value = false;
        confirmLoading.value = false;
        emit('okCallback', result)
    }
}

/** 确认按钮点击 */
const onOk = () => {
    submitForm.value.onFormSubmit()
};

/**
 * 设置确认按钮状态
 * @param {boolean} type - 是否可点击
 */
const okType = (type: boolean) => {
    okDisabled.value = !type
};


</script>
