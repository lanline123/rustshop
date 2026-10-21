<template>
    <div>
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
            <el-form-item prop="buyerName" :label="$t('买家信息')" :rules="[{ required: true, message: '请选择买家信息!' }]">
                <BuyerInfo v-model:formState="formState" :act="props.act"></BuyerInfo>
            </el-form-item>
            <el-form-item prop="content" :label="$t('文字内容')" :rules="[{ required: true, message: '请填写文字内容!' }]">
                <TigInput classType="tig-form-input" type="textarea" :placeholder="$t('请输入评价内容')" v-model="formState.content" maxlength="500" show-word-limit />
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import BuyerInfo from "@/views/promotion/buyerShow/buyerShowListings/src/BuyerInfo.vue";
import { message } from "ant-design-vue";
import { commentBuyerShow } from '@/api/promotion/buyShow'
import { defineProps, ref, shallowRef, defineEmits } from "vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
});
const loading = ref(false);
const formState = ref({
    content: "",
    buyerName: "",
    buyerPhoto: ""
});
const formRef = shallowRef();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const params: any = {
            buyerShowId: props.id,
            userName: formState.value.buyerName,
            userPhoto: formState.value.buyerPhoto,
            content: formState.value.content
        };
        emit('update:confirmLoading', true);
        const result = await commentBuyerShow(params);
        emit('submitCallback', result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">

</style>
