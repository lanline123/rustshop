<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <el-form ref="formRef" :model="formState" label-width="120px">
                    <div class="title">{{ $t('基本设置') }}</div>
                    <el-form-item :label="$t('店铺商品审核')" prop="shopProductNeedCheck">
                        <div>
                            <!-- <el-radio-group v-model="formState.shopProductNeedCheck" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group> -->
                        <el-checkbox v-model="formState.shopProductNeedCheck" :label="$t('商品默认审核通过')" :true-label="0" :false-label="1" />
                        <div class="extra">{{ $t('店铺发布商品的时候是否需要审核才能显示商品') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('推荐商品数量')" prop="maxRecommendProductCount">
                        <div>
                            <TigInput type="integer" classType="tig-form-input" v-model="formState.maxRecommendProductCount" />
                            <div class="extra">{{ $t('商城中新品、推荐列表显示的商品数量') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('最大管理员数')" prop="maxSubAdministrator">
                        <div>
                            <TigInput type="integer" classType="tig-form-input" v-model="formState.maxSubAdministrator" />
                            <div class="extra">{{ $t('当前店铺可配置的最大子管理员数') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('店铺名称')" prop="defaultShopName">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.defaultShopName" />
                            <div class="extra">{{ $t('当商品没有归属店铺时商品卡片展示的自定义店铺名称，默认：官方自营店') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button :loading="confirmLoading" ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { PaymentFormState } from "@/types/setting/config";
import { getConfigShopSetting, saveConfigShopSetting } from "@/api/setting/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<PaymentFormState>({
    shopProductNeedCheck: 1,
    maxRecommendProductCount: "",
    maxSubAdministrator: "5",
    defaultShopName: ""
});

// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigShopSetting();
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
        const result = await saveConfigShopSetting(formState.value);
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
