/**
 * @file 认证设置页
 * @description 配置会员实名认证规则
 * @features
 * - 开启/关闭会员认证功能
 * - 选择认证类型（企业/个人+企业）
 * - 配置实名后查看价格限制
 * - 配置认证后购物限制
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="180px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('会员认证设置') }}</div>
                    <el-form-item :label="$t('会员认证')" prop="closeAuth">
                        <el-radio-group v-model="formState.closeAuth" class="itemWidth">
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('认证类型')" prop="type">
                        <el-radio-group v-model="formState.type" class="itemWidth" :disabled="formState.closeAuth == 0">
                            <el-radio :value="1">{{ $t('企业认证') }}</el-radio>
                            <el-radio :value="0">{{ $t('个人认证 + 企业认证') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('是否实名后才能查看价格')" prop="isIdentity">
                        <el-radio-group v-model="formState.isIdentity" class="itemWidth"  :disabled="formState.closeAuth == 0">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('开启后，前台商城的商品价格需要用户实名认证之后才能查看。') }}</div>
                    </el-form-item>
                    <!-- <el-form-item label="是否开启询价功能" prop="isEnquiry">
                        <el-radio-group v-model="formState.isEnquiry" class="itemWidth">
                            <el-radio :value="1">开启</el-radio>
                            <el-radio :value="0">关闭</el-radio>
                        </el-radio-group>
                        <div class="extra">开启后，前台商城的商品价格需要提交询价之后才能查看。</div>
                    </el-form-item> -->
                    <el-form-item :label="$t('是否短信通知')" prop="smsNote">
                        <el-radio-group v-model="formState.smsNote" class="itemWidth">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('审核周期')" prop="tips">
                        <TigInput classType="tig-form-input" v-model="formState.tips"></TigInput>
                    </el-form-item>
                    <!-- <div class="title">商品设置</div>
                    <el-form-item label="商品规格显示批量采购" prop="bulkPurchase">
                        <el-radio-group v-model="formState.bulkPurchase" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">
                            商品详情页将同时支持单品采购和批量采购。<el-popover :width="300" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>查看示例</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/bulkPurchase-mobile.png" style="width: 280px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item> -->
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { BaseAuthenticationSettings, Regions } from "@/types/setting/config";
import { getConfigAuth, saveConfigAuth } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<BaseAuthenticationSettings>({
    closeAuth: 0,
    type: 0,
    isIdentity: 0,
    isEnquiry: 0,
    smsNote: 0,
    bulkPurchase: 0,
    tips: ""
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigAuth();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
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
        const result = await saveConfigAuth(formState.value);
        message.success(t('修改成功'));
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

watch(formState.value, (newVal) => {
    if(newVal.closeAuth == 0){
        formState.value.isIdentity = 0;
    }
})
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-top: 20px;
    padding-bottom: 20px;
    font-size: 14px;
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

.itemWidth {
    width: 100%;
}
.error {
    color: red;
}
</style>
