<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="160px" >
            <div class="content">
                <div class="title">{{ $t('YaBandPay设置') }}</div>
                <el-form-item :label="$t('是否开启')" prop="useYabanpay">
                    <el-radio-group v-model="formState.useYabanpay" class="itemWidth">
                        <el-radio :value="1">{{ $t('开启') }}</el-radio>
                        <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                    </el-radio-group>
                    <div class="extra">{{ $t('是否开启YaBandPay支付，如关闭则不显示YaBandPay支付选项') }}</div>
                </el-form-item>
                <!-- <el-form-item label="微信开启" prop="useYabanpayWechat">
                    <el-radio-group v-model="formState.useYabanpayWechat" class="itemWidth">
                        <el-radio :value="1">开启</el-radio>
                        <el-radio :value="0">关闭</el-radio>
                    </el-radio-group>
                    <div class="extra">是否开启YaBandPay微信支付，如关闭则不显示YaBandPay微信支付选项</div>
                </el-form-item>
                <el-form-item label="支付宝开启" prop="useYabanpayAlipay">
                    <el-radio-group v-model="formState.useYabanpayAlipay" class="itemWidth">
                        <el-radio :value="1">开启</el-radio>
                        <el-radio :value="0">关闭</el-radio>
                    </el-radio-group>
                    <div class="extra">是否开启YaBandPay支付宝支付，如关闭则不显示YaBandPay支付宝支付选项</div>
                </el-form-item> -->
                <el-form-item :label="$t('货币')" prop="yabanpayCurrency">
                    <el-select v-model="formState.yabanpayCurrency" style="width: 450px">
                        <el-option value="0" :label="$t('不选择')" />
                        <el-option v-for="item in formState.yabanpayCurrencyList" :key="item.id" :value="item.value" :label="item.name" />
                    </el-select>
                    <div class="extra"> {{ $t('如果选择了默认国家，则会员在选择地址时默认会显示该国家下的省份或地区，不再显示国家选择（修改设置对已经添加的会员地址不生效）') }} {{ $t('请保持和商城默认货币一致，否则会导致下单和结账货币不一致') }} </div>
                </el-form-item>
                <el-form-item label="YaBandPay UID" prop="yabandpayUid">
                    <!-- <TigInput classType="tig-form-input" v-model="formState.yabandpayUid" /> -->
                    <div>
                        <div class="flex flex-align-center" style="gap: 10px">
                            <div class="secret-txt line1" style="max-width: 380px">
                                {{ formState.yabandpayUid }}
                            </div>
                            <DialogForm
                                :maskClose="false"
                                :isDrawer="false"
                                :params="{ title: 'YaBandPay UID', content: formState.yabandpayUid }"
                                path="setting/config/src/EditSecret"
                                title="YaBandPay UID"
                                width="600px"
                                @okCallback="formState.yabandpayUid = $event"
                            >
                                <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                            </DialogForm>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="YaBandPay Secret Key" prop="yabandpaySecretKey">
                    <div>
                        <div class="flex flex-align-center" style="gap: 10px">
                            <div class="secret-txt line1" style="max-width: 380px">
                                {{ formState.yabandpaySecretKey }}
                            </div>
                            <DialogForm
                                :maskClose="false"
                                :isDrawer="false"
                                :params="{ title: 'YaBandPay Secret Key', content: formState.yabandpaySecretKey }"
                                path="setting/config/src/EditSecret"
                                title="YaBandPay Secret Key"
                                width="600px"
                                @okCallback="formState.yabandpaySecretKey = $event"
                            >
                                <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                            </DialogForm>
                        </div>
                        <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的YaBandPay Secret Key') }}</div>
                    </div>
                </el-form-item>
            </div>
        </el-form>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { YaBandPaySettingsForm } from "@/types/setting/payment";
import { getYaBandPaySettings, updateYaBandPaySettings } from "@/api/setting/payment";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<Partial<YaBandPaySettingsForm>>({
    useYabanpay: undefined,
    useYabanpayWechat: undefined,
    useYabanpayAlipay: undefined,
    yabanpayCurrency: "yabanpayCurrency_957f5c4b40ce",
    yabandpayUid: "yabandpayUid_59277ac5d616",
    yabandpaySecretKey: "yabandpaySecretKey_2f8cef5492f1"
});

// 加载列表
onMounted(async () => {
    loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getYaBandPaySettings();
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
        const result = await updateYaBandPaySettings({
            ...formState.value
        });
        message.success(t('修改成功'));
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
</style>
