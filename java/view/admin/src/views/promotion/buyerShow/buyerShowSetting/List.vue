<template>
    <div v-loading="loading">
        <el-form ref="formRef" :model="formState" label-width="auto">
            <el-form-item :label="$t('功能开关')">
                <div class="flex flex-justify-center">
                    <el-switch
                        v-model="formState.buyerShowEnabled"
                        class="mb-2"
                        style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ccc"
                    />
                    <div class="ml5">{{ formState.buyerShowEnabled ? $t('已开启') : $t('已关闭') }}</div>
                </div>
            </el-form-item>
            <el-form-item :label="$t('手机端展示名称')" prop="buyerShowMobileShowName" :rules="[{ required: true, message: '请填写手机端展示名称!' }]">
                <div class="flex flex-align-center" style="transform: translateY(-4px)">
                    <TigInput :placeholder="$t('请输入')" class="mr10" v-model="formState.buyerShowMobileShowName" maxlength="10" show-word-limit></TigInput>
                    <el-popover
                        class="box-item"
                        title=""
                        placement="bottom"
                        width="320"
                    >
                        <img src="../assets/nameDisplay.png" alt="" width="300">
                        <template #reference>
                            <span class="btn-link">{{ $t('示例') }}</span>
                        </template>
                    </el-popover>
                </div>
            </el-form-item>
            <el-form-item :label="$t('评论引导文案')" prop="buyerShowGuidanceContent">
                <TigInput :placeholder="$t('请输入')" v-model="formState.buyerShowGuidanceContent" maxlength="15" show-word-limit></TigInput>
            </el-form-item>
            <el-form-item :label="$t('模板样式')" prop="buyerShowTemplateStyle" :rules="[{ required: true, message: '请选择模板样式!' }]">
                <div class="flex flex-align-center" style="transform: translateY(-4px)">
                    <el-radio-group v-model="formState.buyerShowTemplateStyle" class="mr10">
                        <el-radio :value="0" size="large">{{ $t('双栏列表') }}</el-radio>
                        <el-radio :value="1" size="large">{{ $t('单栏列表') }}</el-radio>
                    </el-radio-group>
                    <el-popover
                        class="box-item"
                        title=""
                        placement="bottom"
                        width="420"
                    >
                        <img src="../assets/doubleBar.png" alt="" width="400">
                        <template #reference>
                            <span class="btn-link ml5">{{ $t('示例') }}</span>
                        </template>
                    </el-popover>
                </div>
            </el-form-item>
        </el-form>
        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('保存设置') }}</el-button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted } from "vue";
import { buyerShowSettingFormState } from "@/types/promotion/buyerShowSetting";
import { useThemeStore } from "@/store/theme";
import { saveBuyerShowConfig, getBuyerShowConfig } from '@/api/promotion/buyShow'
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef<any>();
const loading = ref<boolean>(false);
const formState = ref<buyerShowSettingFormState>({
    buyerShowTemplateStyle: 0,
});

const { themeInfo } = useThemeStore();
const confirmLoading = ref<boolean>(false);

const onSubmit = async ()=>{
    await formRef.value.validate();
    try {
        confirmLoading.value = true;
        loading.value = true;
        await saveBuyerShowConfig({ ...formState.value, buyerShowEnabled: formState.value.buyerShowEnabled ? 1 : 0 })
        confirmLoading.value = false;
        loading.value = false;
        message.success(t('保存成功'));
        loadData()
    } catch (error: any) {
        message.error(error.message);
        confirmLoading.value = false;
        loading.value = false;
    }
}

const loadData = async () => {
    try {
        loading.value = true;
        const data: any = await getBuyerShowConfig();
        formState.value = {
            ...data,
            buyerShowEnabled: data.buyerShowEnabled === 1
        }
        loading.value = false;
    } catch (error: any) {
        message.error(error.message);
        loading.value = false;
    }
}
onMounted(() => {
    loadData();
});
</script>

<style scoped lang="less">

</style>
