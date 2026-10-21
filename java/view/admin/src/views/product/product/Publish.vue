<!--
 * @file 发布商品整页
 * @description 商家端 app web-view「发布商品」入口的独立整页承载。
 *   - 复用商品编辑组件 Info.vue（act=add），页面自身只提供内容区 + 底部提交栏
 *   - 不自带顶部标题栏：标题与返回由外层 webview 外壳（H5 自绘头部 / App 原生导航栏）提供
 *   - 提交成功后通过 postMessage 通知承载它的容器返回上一页（H5 生效；App/小程序回退到容器返回键）
-->
<template>
    <div class="publish-page">
        <div class="publish-body">
            <Info
                ref="infoRef"
                is-dialog
                act="add"
                @submitCallback="onSubmitted"
                @update:confirmLoading="onConfirmLoading"
                @close="notifyContainerBack"
            />
        </div>
        <div class="publish-footer">
            <el-button type="primary" size="large" :loading="confirmLoading" class="submit-btn" @click="onSubmit">提交</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import Info from "@/views/product/product/Info.vue";

/** 商品编辑组件引用，用于触发其内部提交 */
const infoRef = ref();
/** 提交按钮 loading 状态（由 Info 提交流程回传） */
const confirmLoading = ref(false);

/**
 * 通知承载容器返回上一页。
 * H5：iframe 内向父窗口发返回指令，seller webview 收到后 navigateBack；
 * App/小程序：无 postMessage 返回桥，回退到容器原生返回键。
 */
const notifyContainerBack = () => {
    try {
        window.parent?.postMessage({ source: "tigshop-seller-webview", action: "navigateBack" }, "*");
    } catch (e) {
        // ignore
    }
};

/** 点击底部提交：委托给 Info 的表单提交入口 */
const onSubmit = () => {
    infoRef.value?.onFormSubmit?.();
};

/** Info 回传提交 loading 状态 */
const onConfirmLoading = (val: boolean) => {
    confirmLoading.value = val;
};

/** 提交成功：成功提示已由 Info 内部处理，这里通知容器返回 */
const onSubmitted = () => {
    notifyContainerBack();
};
</script>

<style lang="less" scoped>
.publish-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #fff;
}

.publish-body {
    flex: 1;
    min-height: 0;
    overflow: hidden;
    padding: 10px;
}

.publish-footer {
    flex-shrink: 0;
    padding: 10px 16px;
    border-top: 1px solid #eee;
    background-color: #fff;

    .submit-btn {
        width: 100%;
    }
}
</style>
