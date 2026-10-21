<template>
    <canvas :id="id" :canvas-id="id" :style="{ height: size + 'px', width: size + 'px' }" :width="size" :height="size" :class="{ show: show === false }" />
</template>

<script setup lang="ts">
import { getCurrentInstance } from "vue";
//  @ts-expect-error: uqrcodejs may not have TypeScript types available
import UQRCode from "uqrcodejs";
const instance = getCurrentInstance();
const props = defineProps({
    modelValue: {
        type: String,
        default: ""
    },
    id: {
        type: String,
        default: "qrcode"
    },
    value: {
        type: String,
        default: "https://www.tigshop.com/"
    },
    size: {
        type: Number,
        default: 200
    },
    fileType: {
        type: String,
        default: "png"
    },
    show: {
        type: Boolean,
        default: true
    },
    margin: {
        type: Number,
        default: 5
    },
    immediatelyCreate: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(["update:modelValue", "success"]);

const getFilePath = () => {
    uni.canvasToTempFilePath(
        {
            canvasId: props.id,
            fileType: props.fileType,
            width: props.size,
            height: props.size,
            success: (res) => {
                emit("success", res.tempFilePath);
            },
            fail: (err) => {
                console.error(err);
            }
        },
        instance
    );
};

const drawQrcode = () => {
    const qr = new UQRCode();
    const canvasContext = uni.createCanvasContext(props.id, instance); // 如果是组件，this必须传入
    // 设置二维码内容
    qr.data = props.value;
    // 设置二维码大小，必须与canvas设置的宽高一致
    qr.size = props.size;
    qr.margin = props.margin;
    // 调用制作二维码方法
    qr.make();
    // 设置uQRCode实例的canvas上下文
    qr.canvasContext = canvasContext;
    // 调用绘制方法将二维码图案绘制到canvas上
    qr.drawCanvas();
    if (props.immediatelyCreate) {
        //  调用完ctx.draw()方法后不能第一时间导出，小程序真机调试会异常，需要有一定的延时
        setTimeout(() => {
            getFilePath();
        }, 100);
    }
};

defineExpose({
    getFilePath,
    drawQrcode
});
</script>

<style lang="scss" scoped>
.show {
    position: fixed;
    top: -9999px;
    left: -9999px;
    z-index: -100;
}
</style>
