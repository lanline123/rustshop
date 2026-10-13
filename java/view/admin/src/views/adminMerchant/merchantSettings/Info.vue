<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <el-form ref="formRef" :model="formState" label-width="160px">
                    <div class="title">{{ $t('基本设置') }}</div>
                    <el-form-item :label="$t('是否允许个人入驻')" prop="personApplyEnabled">
                        <el-radio-group v-model="formState.personApplyEnabled" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('商户入驻是否需要审核')" prop="merchantApplyNeedCheck">
                        <el-radio-group v-model="formState.merchantApplyNeedCheck" class="itemWidth">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item prop="maxShopCount">
                        <template #label>
                            <span v-if="!isStore()">{{ $t('单个商户最大店铺数量') }}</span>
                        </template>
                        <TigInput type="integer" v-model="formState.maxShopCount" />
                    </el-form-item>
                    <div class="title">{{ $t('入驻协议内容') }}</div>
                    <Editor v-model:html="formState.shopAgreement"></Editor>
                    <BusinessData v-model:modelValue="formState.shopAgreement" :dataType="12" type="editor"></BusinessData>
                </el-form>
                <div style="height: 20px"></div>
                <div class="selected-action-warp selected-warp-left"
                    :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                    <div class="selected-action">
                        <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary"
                            @click="onSubmit">{{ $t('提 交') }}</el-button>
                    </div>
                </div>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { Editor } from "@/components/editor/index"
import { message } from "ant-design-vue";
import type { PaymentFormState, Regions } from "@/types/setting/config";
import { getConfigMerchantSetting, saveConfigMerchantSetting } from "@/api/setting/config";
import { useThemeStore } from "@/store/theme";
import { isStore } from "@/utils/version";
import BusinessData from "@/components/multilingual/BusinessData.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<PaymentFormState>({
    personApplyEnabled: 1,
    merchantApplyNeedCheck: 1,
    maxShopCount: 10,
    shopAgreement: "",
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigMerchantSetting();
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await saveConfigMerchantSetting(formState.value);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-top: 20px;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }

    :deep(.el-tabs--border-card > .el-tabs__content) {
        padding: 0;
    }
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}
</style>
<style>
.itemWidth {
    width: 100%;
}

.title {
    font-weight: bold;
    padding-top: 20px;
    padding-bottom: 20px;
    font-size: 14px;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
}
</style>
