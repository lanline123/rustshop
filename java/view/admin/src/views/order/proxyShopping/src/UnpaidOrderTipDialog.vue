<template>
    <Modal
        :open="open"
        :centered="false"
        :footer="null"
        :width="width"
        :maskClosable="maskClosable"
        :closable="closable"
        :keyboard="keyboard"
        :destroyOnClose="true"
        wrapClassName="proxy-unpaid-order-tip-pop"
    >
        <div class="content">
            <div class="warn-title">
                <ExclamationCircleFilled class="warn-title-icon" />
                <span class="warn-title-text">{{ props.title }}</span>
            </div>
            <div class="tip-text">
                <div>
                    <span>{{ $t('订单') }}</span>
                    <span class="order-sn"> {{ orderSn || "-" }} </span>
                    <span>{{ $t('已提交成功，') }}</span>
                </div>
                <div>
                    <span>{{ $t('请先核对货款到账，再将此订单设置为已支付，避免损失。') }}</span>
                    <el-popover :width="400" placement="right-end" trigger="click">
                        <template #reference>
                            <a>{{ $t('示例') }}</a>
                        </template>
                        <template #default>
                            <el-image
                                class="example-img"
                                :src="proxyShoppingExampleImg"
                                :preview-src-list="examplePreviewSrcList"
                                fit="contain"
                                preview-teleported
                                alt="示例"
                            />
                        </template>
                    </el-popover>
                </div>
            </div>

            <div class="actions">
                <el-button class="btn btn-ok" type="primary" @click="handleOk">{{ $t('确定') }}</el-button>
            </div>
        </div>
    </Modal>
</template>

<script setup lang="ts">
import { Modal } from "ant-design-vue";
import { ExclamationCircleFilled } from "@ant-design/icons-vue";
import proxyShoppingExampleImg from "@/assets/proxyShopping/proxyShopping_example.png";

const examplePreviewSrcList = [proxyShoppingExampleImg];

const props = withDefaults(
    defineProps<{
        open: boolean;
        orderSn?: string;
        title?: string;
        width?: string | number;
        maskClosable?: boolean;
        closable?: boolean;
        keyboard?: boolean;
    }>(),
    {
        open: false,
        title: "提示",
        width: "520px",
        maskClosable: false,
        closable: false,
        keyboard: false
    }
);

const emit = defineEmits<{
    (e: "update:open", val: boolean): void;
    (e: "ok"): void;
}>();

const handleOk = () => {
    emit("update:open", false);
    emit("ok");
};

</script>

<style scoped lang="less">
.warn-title {
    display: inline-flex;
    align-items: center;
    gap: 8px;
}
.warn-title-icon {
    color: #faad14; // ant warning color
    font-size: 18px;
    line-height: 1;
}
.warn-title-text {
    font-weight: 500;
    font-size: 16px;
    color: rgba(0, 0, 0, 0.88);
}
.content {
    line-height: 22px;
}
.order-sn {
    color: var(--el-color-primary);
    font-weight: 600;
    padding: 0 3px;
}
.example-link {
    margin-left: 8px;
    color: var(--el-color-primary);
    cursor: pointer;
}
.example-img {
    display: block;
    width: 100%;
    height: auto;
    border-radius: 8px;
    cursor: zoom-in;
    overflow: hidden;
}
:deep(.proxy-example-tooltip .ant-tooltip-inner) {
    padding: 8px;
}
.actions {
    padding-top: 18px;
    display: flex;
    justify-content: flex-end;
}
.btn {
    border-radius: 6px;
    padding: 0 18px;
}
.tip-text {
    margin-top: 20px;
}
</style>
