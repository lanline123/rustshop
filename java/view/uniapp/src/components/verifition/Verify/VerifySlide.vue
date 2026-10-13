<template>
    <view class="verifyslide">
        <view class="verify-img-out" :style="{ height: parseInt(imgSize.height) + 'px' }">
            <view
                class="verify-img-panel"
                :style="{
                    width: imgSize.width,
                    height: imgSize.height
                }"
            >
                <image v-if="backImgBase" :src="'data:image/png;base64,' + backImgBase" class="img" />

                <view v-if="backImgBase" v-show="showRefresh" class="verify-refresh" @click="refresh"><i class="iconfont-pc icon-refresh" /></view>
                <up-transition :show="!!tipWords" mode="fade-up">
                    <text :class="'verify-tips ' + (passFlag ? 'suc-bg' : 'err-bg')">{{ tipWords }}</text>
                </up-transition>
            </view>
        </view>
        <!-- 公共部分 -->
        <view
            class="verify-bar-area"
            :style="{
                width: imgSize.width,
                height: barSize.height,
                'line-height': barSize.height,
                marginTop: barMarginTop + 'px'
            }"
        >
            <text class="verify-msg">{{ text }}</text>
            <view
                class="verify-left-bar"
                :style="{
                    width: leftBarWidth !== undefined ? leftBarWidth : barSize.height,
                    height: barSize.height,
                    'border-color': leftBarBorderColor,
                    'background-color': leftBarBackgroundColor,
                    transition: transitionWidth
                }"
            >
                <text class="verify-msg">{{ text }}</text>
                <view
                    class="verify-move-block"
                    :style="{
                        width: barSize.height,
                        height: barSize.height,
                        'background-color': moveBlockBackgroundColor,
                        left: moveBlockLeft,
                        transition: transitionLeft
                    }"
                    @touchstart="start"
                    @touchmove.stop.prevent="move"
                    @touchend="end"
                    @mousedown="startByMouse"
                >
                    <i :class="['verify-icon iconfont-pc', iconClass]" :style="{ color: iconColor }" />
                    <view
                        class="verify-sub-block"
                        :style="{
                            width: Math.floor((parseInt(imgSize.width) * 47) / 310) + 'px',
                            height: imgSize.height,
                            top: '-' + (parseInt(imgSize.height) + barMarginTop) + 'px',
                            'background-size': imgSize.width + ' ' + imgSize.height
                        }"
                    >
                        <image v-if="blockBackImgBase" :src="'data:image/png;base64,' + blockBackImgBase" class="img" />
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { aesEncrypt } from "@/utils/crypto";
import { onMounted, onUnmounted, ref, getCurrentInstance, computed } from "vue";
import { verificationCaptcha, verificationCheck } from "@/api/verification";
import { getElementRect } from "@/utils";

const { t } = useI18n();
const props = defineProps({
    captchaType: {
        type: String,
        default: "blockPuzzle"
    },
    imgSize: {
        type: Object,
        default() {
            return {
                width: "310px",
                height: "155px"
            };
        }
    },
    explain: {
        type: String,
        default: ""
    },
    blockSize: {
        type: Object,
        default() {
            return {
                width: 50,
                height: 50
            };
        }
    },
    barSize: {
        type: Object,
        default() {
            return {
                width: "310px",
                height: "40px"
            };
        }
    },
    barMarginTop: {
        type: Number,
        default: 8
    }
});

const displayExplain = computed(() => props.explain || t("向右拖动滑块填充拼图"));

const emit = defineEmits(["close", "success"]);

const passFlag = ref(false);
const backImgBase = ref("");
const blockBackImgBase = ref("");
const backToken = ref("");
const secretKey = ref("");
const tipWords = ref("");
const startMoveTime = ref(0); //移动开始的时间

const getPictrue = async () => {
    try {
        const res = await verificationCaptcha({ captchaType: props.captchaType });
        backImgBase.value = res.originalImageBase64;
        blockBackImgBase.value = res.jigsawImageBase64;
        backToken.value = res.token;
        secretKey.value = res.secretKey;
    } catch (error: any) {
        console.error(error);
        tipWords.value = error.message;
    }
};

const showRefresh = ref(false);
const finishText = ref("");
const moveBlockLeft = ref("");
const leftBarWidth = ref("");
const moveBlockBackgroundColor = ref("");
const leftBarBorderColor = ref("#ddd");
const leftBarBackgroundColor = ref("#d1e0f1");
const iconColor = ref("");
const iconClass = ref("icon-right");
const status = ref(false); //鼠标状态
const isEnd = ref(false); //是够验证完成
const transitionLeft = ref("");
const transitionWidth = ref("");
const startLeft = ref(0);
const text = ref("");
const barArea = ref({
    left: 0,
    top: 0,
    width: 0,
    height: 0
});
const endMovetime = ref(0);
const instance = getCurrentInstance();

const getBarArea = async () => {
    try {
        const res = await getElementRect(".verify-bar-area", instance);
        if (res) {
            barArea.value = res;
        }
    } catch (error) {
        console.error(error);
    }
};

const STD_WIDTH = 310;
const DEFAULT_Y_POS = 5.0;

const UI_COLORS = {
    active: {
        block: "#1991fa",
        border: "#1991fa",
        background: "#d1e0f1",
        icon: "#fff"
    },
    success: {
        block: "#52ccba",
        border: "#52ccba",
        background: "#d2f4ef",
        icon: "#fff"
    },
    error: {
        block: "#d9534f",
        border: "#d9534f",
        background: "#fce1e1",
        icon: "#fff"
    },
    default: {
        block: "#fff",
        border: "#ddd",
        background: "#d1e0f1",
        icon: "#666"
    }
};

const applyUIColors = (type: keyof typeof UI_COLORS) => {
    const colors = UI_COLORS[type];
    moveBlockBackgroundColor.value = colors.block;
    leftBarBorderColor.value = colors.border;
    leftBarBackgroundColor.value = colors.background;
    iconColor.value = colors.icon;
};

const start = (e: TouchEvent) => {
    try {
        if (isEnd.value) return;

        const x = e.touches[0].clientX;
        startLeft.value = Math.floor(x - barArea.value.left);
        startMoveTime.value = Date.now();

        text.value = "";
        applyUIColors("active");
        status.value = true;
    } catch (err) {
        console.error("滑块验证初始化错误:", err);
    }
};

const move = (e: TouchEvent) => {
    try {
        if (!status.value || isEnd.value) return;

        const x = e.touches[0].clientX;
        const barAreaLeft = barArea.value?.left || 0;

        const blockWidth = parseInt(props.blockSize.width);
        const halfBlockWidth = blockWidth / 2;
        const maxWidth = (barArea.value?.width || 0) - halfBlockWidth - 2;

        let movePos = x - barAreaLeft;
        movePos = Math.max(halfBlockWidth, Math.min(movePos, maxWidth));

        const offset = movePos - startLeft.value;

        moveBlockLeft.value = `${offset}px`;
        leftBarWidth.value = `${offset + 40}px`;
    } catch (err) {
        console.error("滑块移动处理错误:", err);
    }
};

const end = () => {
    try {
        endMovetime.value = Date.now();

        if (!status.value || isEnd.value) return;

        const moveDistance = getScaledDistance();

        __verificationCheck(moveDistance);
    } catch (err) {
        console.error("滑块验证结束处理错误:", err);
        refresh();
    }
};

// PC端鼠标事件
const startByMouse = (e: MouseEvent) => {
    try {
        if (isEnd.value) return;

        const x = e.clientX;
        startLeft.value = Math.floor(x - barArea.value.left);
        startMoveTime.value = Date.now();

        text.value = "";
        applyUIColors("active");
        status.value = true;

        // 添加全局鼠标移动和松开事件监听
        document.addEventListener("mousemove", moveByMouse);
        document.addEventListener("mouseup", endByMouse);
    } catch (err) {
        console.error("滑块验证初始化错误:", err);
    }
};

const moveByMouse = (e: MouseEvent) => {
    try {
        if (!status.value || isEnd.value) return;

        const x = e.clientX;
        const barAreaLeft = barArea.value?.left || 0;

        const blockWidth = parseInt(props.blockSize.width);
        const halfBlockWidth = blockWidth / 2;
        const maxWidth = (barArea.value?.width || 0) - halfBlockWidth - 2;

        let movePos = x - barAreaLeft;
        movePos = Math.max(halfBlockWidth, Math.min(movePos, maxWidth));

        const offset = movePos - startLeft.value;

        moveBlockLeft.value = `${offset}px`;
        leftBarWidth.value = `${offset + 40}px`;
    } catch (err) {
        console.error("滑块移动处理错误:", err);
    }
};

const endByMouse = () => {
    try {
        // 移除全局事件监听
        document.removeEventListener("mousemove", moveByMouse);
        document.removeEventListener("mouseup", endByMouse);

        endMovetime.value = Date.now();

        if (!status.value || isEnd.value) return;

        const moveDistance = getScaledDistance();

        __verificationCheck(moveDistance);
    } catch (err) {
        console.error("滑块验证结束处理错误:", err);
        refresh();
    }
};

const getScaledDistance = () => {
    const moveLeftDistance = parseInt((moveBlockLeft.value || "0").replace("px", ""));

    const imgWidth = parseInt(props.imgSize.width) || STD_WIDTH;
    return (moveLeftDistance * STD_WIDTH) / imgWidth;
};

const createPointJson = (x: number) => {
    const point = { x, y: DEFAULT_Y_POS };
    return secretKey.value ? aesEncrypt(JSON.stringify(point), secretKey.value) : JSON.stringify(point);
};

const __verificationCheck = async (moveLeftDistance: number) => {
    try {
        const data = {
            captchaType: props.captchaType,
            pointJson: createPointJson(moveLeftDistance),
            token: backToken.value
        };

        const result = await verificationCheck(data);
        console.log("验证结果:", result);
        if (result.token) {
            applyUIColors("success");
            iconClass.value = "icon-check";
            showRefresh.value = false;
            isEnd.value = true;
            passFlag.value = true;

            const verifyTime = ((endMovetime.value - startMoveTime.value) / 1000).toFixed(2);
            tipWords.value = t("{0}s验证成功", [verifyTime]);

            const point = { x: moveLeftDistance, y: DEFAULT_Y_POS };
            const pointStr = JSON.stringify(point);
            const captchaVerification = secretKey.value ? aesEncrypt(backToken.value + "---" + pointStr, secretKey.value) : backToken.value + "---" + pointStr;

            setTimeout(() => {
                tipWords.value = "";
                emit("close");
                emit("success", { verifyToken: captchaVerification });
            }, 1000);
        } else {
            handleVerificationFailure();
        }
    } catch (error) {
        console.error(error);
        handleVerificationFailure();
    } finally {
        status.value = false;
    }
};

const handleVerificationFailure = () => {
    applyUIColors("error");
    iconClass.value = "icon-close";
    passFlag.value = false;

    setTimeout(() => refresh(), 1000);

    tipWords.value = t("验证失败");
    setTimeout(() => {
        tipWords.value = "";
    }, 1000);
};

const refresh = () => {
    showRefresh.value = true;
    finishText.value = "";
    transitionLeft.value = "left .3s";
    moveBlockLeft.value = "";
    leftBarWidth.value = "";
    transitionWidth.value = "width .3s";

    applyUIColors("default");
    iconClass.value = "icon-right";
    isEnd.value = false;

    getPictrue();

    setTimeout(() => {
        transitionWidth.value = "";
        transitionLeft.value = "";
        text.value = displayExplain.value;
    }, 300);
};

onMounted(() => {
    getPictrue();
    setTimeout(() => {
        getBarArea();
    }, 350);
});

onUnmounted(() => {
    // 清理全局事件监听器，防止内存泄漏
    document?.removeEventListener("mousemove", moveByMouse);
    document?.removeEventListener("mouseup", endByMouse);
});

defineExpose({
    refresh
});
</script>

<style lang="scss" scoped>
.verifyslide {
    position: relative;
    padding-top: 25rpx;

    .img {
        width: 100%;
        height: 100%;
        display: block;
        border-radius: 3px;
        overflow: hidden;
    }
}

.verifybox .ant-modal-content {
    padding: 0;
}

.verifybox .ant-modal-body {
    padding: 15px !important;
}

.verifybox .ant-modal-header {
    padding: 16px 24px;
    border-bottom: 1px solid rgba(5, 5, 5, 0.06);
    margin-bottom: 0;
}

.verifybox .ant-modal-header {
    padding: 16px 15px !important;
}

.verifybox-top {
    padding: 0 15px;
    height: 50px;
    line-height: 50px;
    text-align: left;
    font-size: 16px;
    color: #45494c;
    border-bottom: 1px solid #e4e7eb;
    box-sizing: border-box;
}

.verifybox-bottom {
    padding: 15px;
    box-sizing: border-box;
}

.verifybox-close {
    position: absolute;
    top: 13px;
    right: 9px;
    width: 24px;
    height: 24px;
    text-align: center;
    cursor: pointer;
}

.mask {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1001;
    width: 100%;
    height: 100vh;
    background: rgba(0, 0, 0, 0.3);
    /* display: none; */
    transition: all 0.5s;
}

.verify-tips {
    position: absolute;
    left: 0px;
    bottom: 0px;
    width: 100%;
    height: 30px;
    transition: all 0.2s linear;
    line-height: 30px;
    text-indent: 10px;
    color: #fff;
}

.verify-tips.show {
    height: 30px;
}

.suc-bg {
    background-color: rgba(92, 184, 92, 0.5);
    filter: progid:DXImageTransform.Microsoft.gradient(startcolorstr=#7f5CB85C, endcolorstr=#7f5CB85C);
}

.err-bg {
    background-color: rgba(217, 83, 79, 0.5);
    filter: progid:DXImageTransform.Microsoft.gradient(startcolorstr=#7fD9534F, endcolorstr=#7fD9534F);
}

.tips-enter,
.tips-leave-to {
    bottom: -30px;
}

.tips-enter-active,
.tips-leave-active {
    transition: bottom 0.5s;
}

/* ---------------------------- */
/*常规验证码*/
.verify-code {
    font-size: 20px;
    text-align: center;
    cursor: pointer;
    margin-bottom: 5px;
    border: 1px solid #ddd;
}

.cerify-code-panel {
    height: 100%;
    overflow: hidden;
}

.verify-code-area {
    float: left;
}

.verify-input-area {
    float: left;
    width: 60%;
    padding-right: 10px;
}

.verify-change-area {
    line-height: 30px;
    float: left;
}

.varify-input-code {
    display: inline-block;
    width: 100%;
    height: 25px;
}

.verify-change-code {
    color: #337ab7;
    cursor: pointer;
}

.verify-btn {
    width: 200px;
    height: 30px;
    background-color: #337ab7;
    color: #ffffff;
    border: none;
    margin-top: 10px;
}

/*滑动验证码*/
.verify-bar-area {
    position: relative;
    background: #f7f9fa;
    text-align: center;
    box-sizing: border-box;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.verify-bar-area .verify-move-block {
    position: absolute;
    top: 0px;
    left: 0;
    background: #fff;
    color: #666;
    cursor: pointer;
    box-sizing: content-box;
    box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
    transition: background 0.2s linear;
    border-radius: 1px;
    border: 0;
}

.verify-bar-area .verify-move-block:hover {
    background-color: #1991fa;
    color: #ffffff;
}

.verify-bar-area .verify-left-bar {
    position: absolute;
    top: -1px;
    left: -1px;
    background: #d1e0f1;
    cursor: pointer;
    box-sizing: content-box;
    border: 1px solid #ddd;
    border-radius: 0 2px 2px 0;
}

.verify-img-panel {
    margin: 0;
    box-sizing: content-box;
    position: relative;
}

.verify-img-panel .verify-refresh {
    width: 25px;
    height: 25px;
    text-align: center;
    padding: 5px;
    cursor: pointer;
    position: absolute;
    top: 0;
    right: 0;
    z-index: 2;
}

.verify-img-panel .icon-refresh {
    font-size: 20px;
    color: #fff;
    text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.65);
}

.verify-img-panel .verify-gap {
    background-color: #fff;
    position: relative;
    z-index: 2;
    border: 1px solid #fff;
}

.verify-bar-area .verify-move-block .verify-sub-block {
    position: absolute;
    text-align: center;
    z-index: 3;

    .img {
        width: 100%;
        height: 100%;
        display: block;
    }
}

.verify-bar-area .verify-move-block .verify-icon {
    font-size: 18px;
}

.verify-bar-area .verify-msg {
    z-index: 3;
}

.icon-check:before {
    content: "\e6e9";
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    position: absolute;
    margin: auto;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 9999;
    background-size: contain;
}

.icon-close:before {
    content: "\e6bb";
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    position: absolute;
    margin: auto;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 9999;
    background-size: contain;
}

.icon-right:before {
    content: "\e676";
    display: block;
    width: 16px;
    height: 16px;
    position: absolute;
    line-height: 16px;
    margin: auto;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    background-size: cover;
    z-index: 9999;
    background-size: contain;
}

.icon-refresh:before {
    content: "\e616";
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    position: absolute;
    margin: auto;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 9999;
    background-size: contain;
}
</style>
