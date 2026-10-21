/**
 * @file 会员余额提现设置
 * @description 配置会员余额提现总开关与到账方式（D1107，与分销佣金提现独立）
 * @features
 * - 余额提现配置：开启/关闭会员余额提现（默认关闭，关闭后用户端余额提现入口隐藏）
 * - 余额提现方式：银行卡/支付宝/微信
 * - 不影响分销佣金提现
 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form
                v-if="!loading"
                ref="formRef"
                :model="formState"
                label-width="140px"
                style="display: flex; gap: 12px; flex-direction: column"
                require-asterisk-position="right"
            >
                <div class="content_wrapper">
                    <el-form-item :label="$t('余额提现配置')" prop="memberWithdrawalEnabled">
                        <div>
                            <div class="flex flex-align-center">
                                <el-switch v-model="formState.memberWithdrawalEnabled" :active-value="1" :inactive-value="0" />
                                <div class="tips ml10">{{ formState.memberWithdrawalEnabled ? $t('已开启') : $t('已关闭') }}</div>
                            </div>
                            <div class="extra"> {{ $t('配置会员余额提现权限，关闭提现后会员无法发起提现；开启后仅展示已勾选的收款方式。注意：若营销中的会员充值含赠送余额，建议关闭会员余额提现功能，避免资金套利。该开关不影响分销佣金提现。') }} </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('余额提现方式')" prop="memberWithdrawalReceiptMethod">
                        <div>
                            <el-checkbox-group v-model="formState.memberWithdrawalReceiptMethod">
                                <el-checkbox :label="$t('银行卡')" :value="1" />
                                <el-checkbox :label="$t('支付宝')" :value="2" />
                                <el-checkbox :label="$t('微信')" :value="3" />
                            </el-checkbox-group>
                            <div class="extra">{{ $t('勾选后，用户会员余额提现仅展示已选的提现方式。') }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '370px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { getMemberWithdrawalConfig, saveMemberWithdrawalConfig, MemberWithdrawalSetting } from "@/api/finance/userWithdrawApply";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const loading = ref<boolean>(true);
const formState = ref<MemberWithdrawalSetting>({
    memberWithdrawalEnabled: 0,
    memberWithdrawalReceiptMethod: []
});

onMounted(async () => {
    await loadFilter();
});

const loadFilter = async () => {
    try {
        const result = await getMemberWithdrawalConfig();
        if (result) {
            formState.value.memberWithdrawalEnabled = result.memberWithdrawalEnabled ?? 0;
            formState.value.memberWithdrawalReceiptMethod = Array.isArray(result.memberWithdrawalReceiptMethod)
                ? result.memberWithdrawalReceiptMethod
                : [];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        await saveMemberWithdrawalConfig(formState.value);
        message.success(t('修改成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.extra {
    color: #999;
    font-size: 12px;
    margin-top: 6px;
    max-width: 560px;
    line-height: 20px;
}
</style>
