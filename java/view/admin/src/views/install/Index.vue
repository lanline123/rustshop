<template>
    <div class="installation-wizard-index">
        <div class="head-row">
            <div class="img-box">
                <img :src="logo_blue" alt="Logo" />
                <Steps
                    :current="current"
                    type="navigation"
                    size="default"
                    :style="stepStyle"
                    :items="steps"
                />
            </div>
        </div>

        <div class="content-row">
            <component ref="currentComponentRef" :is="currentComponent" @onDisabled="onDisabled" />
        </div>

        <div class="btn-row">
            <div class="btns">
                <el-button style="width: 90px;" v-if="current != 3" size="large" :icon="ArrowLeft" :disabled="current === 0" @click="prevStep">{{ $t('上一步') }}</el-button>
                <el-button style="width: 90px;" v-if="current == 2" type="primary" size="large" @click="onSubmit">{{ $t('提 交') }}</el-button>
                <el-button style="width: 90px;" v-if="current < 2" size="large" type="primary" @click="nextStep" :disabled="disabled"> {{ $t('下一步') }}<el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
            </div>
            <div class="tips" v-show="current === 0"> {{ $t('点击下一步, 表示您已经阅读并同意了') }}<a>{{ $t('《软件许可协议》') }}</a>{{ $t('的内容。') }} </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { Steps } from "ant-design-vue";
import logo_blue from "@/style/images/logo_blue2.png";
import { ref , computed, watch } from "vue";
import Agreement from "./src/Agreement.vue";
import Detection from "./src/Detection.vue";
import CreateData from "./src/CreateData.vue";
import Success from "./src/Success.vue";
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue';
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const query = useRouter().currentRoute.value.query;
const currentComponentRef = ref<any>();
const current = ref<any>(query.current || 0);
const stepStyle = {
    width: "900px",
    marginBottom: "0px",
};
const disabled = ref<boolean>(false);

const steps = [
    { title: t('软件许可协议') },
    { title: t('系统环境检测') },
    { title: t('创建数据') },
    { title: t('安装结果') }
];

const currentComponent = computed(() => {
    if(current.value == 0){
        return Agreement;
    }
    if(current.value == 1){
        return Detection;
    }
    if(current.value == 2){
        return CreateData;
    }
    if(current.value == 3){
        return Success;
    }
});
// 获取当前的 URL
const currentUrl = window.location.href;
// 检查是否已经有查询参数
const separator = currentUrl.includes('?') ? '&' : '?';
const changeUrl = () => {
    // 检查 current 参数是否已存在
    const url = new URL(currentUrl);
    url.searchParams.set('current', current.value); // 使用 set 方法替换现有参数或添加新参数
    window.history.pushState({}, '', url.toString());
}
const nextStep = () => {
    if (current.value < 3) {
        current.value++;
    }
    changeUrl()
}

const prevStep = () => {
    if (current.value > 0) {
        current.value--;
    }
    changeUrl()
}
const onDisabled = (data?: any) => {
    if(data == 'over'){
        current.value = 3;
        nextStep()
    }else{
        disabled.value = data;
    }
}
// 表单通过验证后提交
const onSubmit = async () => {
    currentComponentRef.value.onSubmit()
};
</script>

<style lang="less" scoped>
.installation-wizard-index {
    .head-row {
        position: fixed;
        top: 0;
        left: 0;
        background-color: #fff;
        z-index: 999;
    }
    .head-row, .content-row {
        width: 100%;
        border-bottom: 1px solid #ddd;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        .img-box {
            height: 90px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            width: 1190px;
            img {
                width: 150px;
            }
            :deep(:where(.css-dev-only-do-not-override-174zhm8).ant-steps.ant-steps-navigation .ant-steps-item::before){
                bottom: -17px !important;
            }
        }
    }
    .content-row {
        border: none;
        padding: 90px 0 100px 0;
    }
    .btn-row {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        border-top: 1px solid #ddd;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        background-color: #fff;
        .btns {
            width: 1190px;
            padding: 30px 0;
        }
        .tips {
            position: absolute;
            bottom: 8px;
        }
    }
}
</style>
