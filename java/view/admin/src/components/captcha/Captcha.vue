/**
 * @file 图形验证码组件
 * @description 显示可点击刷新的图形验证码，用于登录/注册等场景
 * @features
 * - 点击刷新验证码
 * - 自动获取初始验证码
 * - 返回验证码UUID用于后端校验
 */
<template>
    <span class="captcha-img" :style="'width:'+props.width+'px;height:'+props.height+'px'">
        <img v-if="imageSrc"  @click="onClick" :src="imageSrc" :width="props.width" :height="props.height">
    </span>
</template>
<script setup lang="ts">
import request from '../../utils/request';
import { ref } from 'vue';

const props = defineProps({
    /** 验证码图片宽度 */
    width: { type: Number, default: 100 },
    /** 验证码图片高度 */
    height: { type: Number, default: 25 },
    /** 验证码范围/场景 */
    range: { type: String, default: '' },
})

/** 宽度 */
const width = ref(props.width)
/** 高度 */
const height = ref(props.height)
/** 验证码图片base64 */
const imageSrc = ref('');
/** 验证码UUID */
const captchaUid = ref('');
const emit = defineEmits(['update:modelValue'])

/**
 * 获取/刷新验证码
 * @description 调用API获取新的验证码图片和UUID
 */
const onClick = () => {
    request({
        url: 'verification/tpCaptcha/',
        method: 'get',
        params: {range:props.range}
    }).then((result: any) => {
        imageSrc.value=result.data
        captchaUid.value = result.uuid
        emit('update:modelValue',captchaUid.value)
    })
}

// 组件加载时自动获取验证码
onClick()

defineExpose({
    onClick
});

</script>

<style lang="less" scoped>
.captcha-img{cursor:pointer;display:inline-block;}
</style>
