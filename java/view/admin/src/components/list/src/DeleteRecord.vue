/** * @file 删除确认组件 * @description 封装删除操作，弹出确认框后调用API执行删除 * @features * - 二次确认弹窗 * - 自定义确认文案 * - 支持禁用状态 */
<template>
    <a @click="delRecord" :class="{ disabled: disabled }">
        <slot></slot>
    </a>
</template>
<script setup lang="ts">
import { message, Modal } from "ant-design-vue";
import { h } from "vue";
import type { PropType } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
    /** 删除请求的API函数 */
    requestApi: { type: Function, default: () => {} },
    /** 传递给API的参数 */
    params: { type: Object, default: {} as any },
    /** 确认弹窗标题 */
    title: { type: String, default: "" },
    /** 确认弹窗内容（正文） */
    content: { type: [String, Object, Function] as PropType<any>, default: "" },
    /** 删除成功提示语 */
    message: { type: String, default: "" },
    /** 是否禁用 */
    disabled: { type: Boolean, default: false },
    /** 确认按钮文案 */
    okText: { type: String, default: "" },
    /** 确认按钮类型（primary / danger / default） */
    okType: { type: String as PropType<"primary" | "default" | "dashed" | "link" | "text" | "danger">, default: "danger" },
    /** 正文区域样式（用于控制高度、滚动等） */
    contentStyle: { type: Object as PropType<Record<string, any>>, default: () => ({}) },
    /** 弹窗宽度 */
    modalWidth: { type: [String, Number], default: undefined },
    /** 弹窗层级 */
    zIndex: { type: Number, default: 100000 },
    /** 弹窗容器样式类 */
    modalClass: { type: String, default: "" }
});

const emit = defineEmits(["afterDelete"]);

/**
 * 执行删除操作
 * @description 显示确认弹窗，确认后调用API删除数据
 */
const delRecord = () => {
    if (props.disabled) return;
    const content =
        props.contentStyle && Object.keys(props.contentStyle).length > 0
            ? h("div", { style: props.contentStyle }, [props.content as any])
            : (props.content as any) || undefined;
    Modal.confirm({
        zIndex: props.zIndex,
        getContainer: () => document.body,
        wrapClassName: props.modalClass || undefined,
        icon: props.modalClass ? null : undefined,
        title: props.title || t("您确认要删除该数据吗？"),
        content,
        width: props.modalWidth,
        okText: props.okText || t("确定"),
        cancelText: t("取消"),
        okType: props.okType,
        maskClosable: true,
        async onOk() {
            await props
                .requestApi(props.params)
                .then((result: any) => {
                    message.success(props.message || t("操作成功"));
                    emit("afterDelete", true);
                })
                .catch((error: any) => {
                    message.error(error.message);
                });
        }
    });
};
</script>
<style scoped>
.disabled {
    cursor: not-allowed;
    color: rgb(199.5, 201, 204);
}
</style>
