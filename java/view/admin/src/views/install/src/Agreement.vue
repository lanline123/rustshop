<template>
    <div class="agreement-container">
        <h2 class="tit"> {{ $t('软件许可协议') }} </h2>
        <div class="content" v-html="formattedText"></div>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { getAgreement } from "@/api/install/install";
import { message } from "ant-design-vue";// 获取当前的 URL
const currentUrl = window.location.href;
// 检查是否已经有查询参数
const separator = currentUrl.includes('?') ? '&' : '?';
const emit = defineEmits(["onDisabled"]);
const formattedText = ref('');
const loading = ref(false);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAgreement();
        if(result.errcode == -1){
            emit("onDisabled", 'over');
        }else{
            formattedText.value = '&nbsp;&nbsp;&nbsp;&nbsp;' + result.text.replace(/\n/g, '</br>');
        }
    } catch (error:any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}
onMounted(() => {
    emit("onDisabled", false);
    loadFilter();
});
</script>
<style lang="less" scoped>
.agreement-container{
    width: 1190px;
    .tit{
        padding: 30px 0;
        color: #333;
        text-align: center;
    }
    .content{
        font-size: 14px;
        padding: 0 15%;
        line-height: 28px;
    }
}
</style>
