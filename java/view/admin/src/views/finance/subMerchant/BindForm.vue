<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="120px">
                    <el-form-item
                        :label="shopTypeName"
                        prop="shopId"
                        :rules="[{ required: true, message: selectShopMessage }]"
                    >
                        <SelectStore
                            v-if="isStore()"
                            v-model:storeId="formState.shopId"
                            :placeholder="shopNamePlaceholder"
                            style="width: 300px"
                        ></SelectStore>
                        <SelectShop
                            v-else
                            v-model:shopId="formState.shopId"
                            :placeholder="shopNamePlaceholder"
                            style="width: 300px"
                        ></SelectShop>
                    </el-form-item>
                    <el-form-item :label="$t('支付渠道')" prop="payChannel" :rules="[{ required: true, message: $t('请选择支付渠道') }]">
                        <el-select v-model="formState.payChannel" :placeholder="$t('请选择')" style="width: 300px">
                        </el-select>
                    </el-form-item>
                    <el-form-item :label="$t('子商户号')" prop="subMchId" :rules="[{ required: true, message: $t('请输入子商户号') }]">
                        <div>
                            <TigInput v-model="formState.subMchId" :placeholder="$t('请输入子商户号')" width="300px" />
                            <div class="extra">{{ $t('微信在服务商平台获取；汇付为进件回写的 huifu_id') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('子商户AppID')" prop="subAppid">
                        <div>
                            <TigInput v-model="formState.subAppid" :placeholder="$t('选填')" width="300px" />
                            <div class="extra">{{ $t('选填，用于小程序支付场景') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('商户名称')" prop="merchantName" :rules="[{ required: true, message: $t('请输入商户名称') }]">
                        <TigInput v-model="formState.merchantName" :placeholder="$t('请输入商户名称')" width="300px" />
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog">
                        <el-button class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { SelectShop, SelectStore } from "@/components/select";
import { isStore } from "@/utils/version";
import { bindSubMerchant } from "@/api/finance/subMerchant";
import type { SubMerchantBindForm } from "@/types/finance/subMerchant.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isStore() ? t("门店") : t("店铺");
const shopNamePlaceholder = isStore() ? t("请输入门店名称") : t("请输入店铺名称");
const selectShopMessage = isStore() ? t("请选择门店") : t("请选择店铺");
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    act: { type: String, default: "" },
    isDialog: Boolean
});

const formRef = shallowRef();
const formState = ref<SubMerchantBindForm>({
    shopId: "",
    payChannel: "WECHAT",
    subMchId: "",
    subAppid: "",
    merchantName: ""
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await bindSubMerchant(formState.value);
        emit("submitCallback", result);
        message.success(t('绑定成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
