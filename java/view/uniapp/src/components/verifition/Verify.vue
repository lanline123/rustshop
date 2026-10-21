<template>
    <tig-popup
        v-model:show="visible"
        :mask-click="false"
        :custom-style="{ width: imgSize.width, 'box-sizing': 'content-box', padding: '10rpx 30rpx' }"
        position="center"
    >
        <view class="content">
            <view class="title">{{ $t("请完成安全验证") }}</view>
            <template v-if="visible">
                <VerifySlide
                    ref="instance"
                    :captcha-type="captchaType"
                    type="2"
                    :figure="figure"
                    :arith="arith"
                    :mode="mode"
                    :v-space="vSpace"
                    :explain="explain"
                    :img-size="imgSize"
                    :block-size="blockSize"
                    :bar-size="barSize"
                    @close="closeBox"
                    @success="success"
                />
            </template>
        </view>
    </tig-popup>
</template>
<script lang="ts" setup>
/**
 * Verify 验证码组件
 * @description 分发验证码使用
 * */
import VerifySlide from "./Verify/VerifySlide.vue";
import { ref, toRefs } from "vue";

const props = defineProps({
    captchaType: {
        type: String,
        required: true
    },
    figure: {
        type: Number
    },
    arith: {
        type: Number
    },
    mode: {
        type: String,
        default: "pop"
    },
    vSpace: {
        type: Number
    },
    explain: {
        type: String
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
    blockSize: {
        type: Object
    },
    barSize: {
        type: Object
    },
    close: {
        type: Function
    }
});
const { captchaType, figure, arith, mode, vSpace, explain, imgSize, blockSize, barSize } = toRefs(props);

const instance = <any>ref({});
const emit = defineEmits([
    "okCallback" //确认后回调
]);
const visible = ref(false);
/**
 * refresh
 * @description 刷新
 * */
const refresh = () => {
    if (instance.value.refresh) {
        instance.value.refresh();
    }
};
const closeBox = () => {
    visible.value = false;
    refresh();
};
const show = () => {
    visible.value = true;
};
const success = (res: any) => {
    emit("okCallback", res);
};
defineExpose({
    show
});
</script>
<style lang="scss" scoped>
.content {
    width: 100%;
    height: 280px;

    .title {
        width: 97%;
        font-size: 18px;
        font-weight: bold;
        text-align: left;
        background: #fff;
        padding: 15px 0 10px;
        border-bottom: 1px solid #eee;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
}
</style>
