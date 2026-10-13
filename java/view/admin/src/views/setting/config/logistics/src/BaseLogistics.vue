<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="auto" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('快递发货设置') }}</div>
                    <el-form-item :label="$t('自定义名称')" prop="defaultLogisticsName" :rules="[{ required: true, message: '自定义名称不能为空' }]">
                        <div>
                            <TigInput width="260px" v-model="formState.defaultLogisticsName" :maxlength="10" showWordLimit />
                            <div class="extra">{{ $t('该名称会显示在买家可选择的配送方式中，默认名称“商家配送”') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('快递接口设置') }}</div>
                    <el-form-item :label="$t('快递接口')" prop="logisticsProvider">
                        <el-radio-group v-model="formState.logisticsProvider">
                            <el-radio :value="1">{{ $t('快递鸟') }}</el-radio>
                            <el-radio :value="2">{{ $t('快递100') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item
                        v-if="isKdniao"
                        :label="$t('快递鸟API Key')"
                        prop="kdniaoApiKey"
                    >
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.kdniaoApiKey }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: '快递鸟API Key', content: formState.kdniaoApiKey }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('快递鸟API Key')"
                                    width="600px"
                                    @okCallback="formState.kdniaoApiKey = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的API Key') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        v-if="isKdniao"
                        :label="$t('快递鸟服务ID')"
                        prop="kdniaoBusinessId"
                    >
                        <!-- <TigInput classType="tig-form-input" v-model="formState.kdniaoBusinessId" type="text" /> -->
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.kdniaoBusinessId }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: '快递鸟服务ID', content: formState.kdniaoBusinessId }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('快递鸟服务ID')"
                                    width="600px"
                                    @okCallback="formState.kdniaoBusinessId = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        v-if="!isKdniao"
                        :label="$t('key')"
                        prop="kuaidi100Key"
                    >
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.kuaidi100Key }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: 'key', content: formState.kuaidi100Key }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('key')"
                                    width="600px"
                                    @okCallback="formState.kuaidi100Key = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('快递100授权码，请前往快递100后台获取') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        v-if="!isKdniao"
                        :label="$t('secret')"
                        prop="kuaidi100Secret"
                    >
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.kuaidi100Secret }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: 'secret', content: formState.kuaidi100Secret }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('secret')"
                                    width="600px"
                                    @okCallback="formState.kuaidi100Secret = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('快递100面单签名密钥，请前往快递100后台获取') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item v-if="!isKdniao" :label="$t('customer')" prop="kuaidi100Customer">
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <div class="secret-txt line1" style="max-width: 380px">
                                    {{ formState.kuaidi100Customer }}
                                </div>
                                <DialogForm
                                    :maskClose="false"
                                    :isDrawer="false"
                                    :params="{ title: 'customer', content: formState.kuaidi100Customer }"
                                    path="setting/config/src/EditSecret"
                                    :title="$t('customer')"
                                    width="600px"
                                    @okCallback="formState.kuaidi100Customer = $event"
                                >
                                    <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra">{{ $t('快递100轨迹查询专用授权码，请前往快递100后台获取') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('发货信息设置') }}</div>
                    <el-form-item :label="$t('发货人')" prop="sender">
                        <TigInput classType="tig-form-input" v-model="formState.sender" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('发货人联系方式')" prop="mobile">
                        <TigInput classType="tig-form-input" v-model="formState.mobile" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('发货地省份')" prop="provinceName">
                        <TigInput classType="tig-form-input" v-model="formState.provinceName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('发货城市')" prop="cityName">
                        <TigInput classType="tig-form-input" v-model="formState.cityName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('发货地区')" prop="areaName">
                        <TigInput classType="tig-form-input" v-model="formState.areaName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('发货详细地址')" prop="address">
                        <TigInput classType="tig-form-input" v-model="formState.address" type="text" />
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('收货信息设置') }}</div>
                    <el-form-item :label="$t('收货人')" prop="receiveSender">
                        <TigInput classType="tig-form-input" v-model="formState.receiveSender" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('收货人联系方式')" prop="receiveMobile">
                        <TigInput classType="tig-form-input" v-model="formState.receiveMobile" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('收货地省份')" prop="receiveProvinceName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveProvinceName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('收货城市')" prop="receiveCityName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveCityName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('收货地区')" prop="receiveAreaName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveAreaName" type="text" />
                    </el-form-item>
                    <el-form-item :label="$t('收货详细地址')" prop="receiveAddress">
                        <TigInput classType="tig-form-input" v-model="formState.receiveAddress" type="text" />
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 20px"></div>
            <div v-if="!loading" class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { isStore } from "@/utils/version";
import { DialogForm } from "@/components/dialog";
import { computed, onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import type { BaseLogistics } from "@/types/setting/baseLogistics.d";
import { clearLogisticsProviderCache, getShippingSettings, LOGISTICS_PROVIDER_KDNIAO, updateShippingSettings } from "@/api/setting/baseLogistics";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const confirmLoading = ref(false);

const loading = ref<boolean>(true);
const formRef = shallowRef();
const formState = ref<BaseLogistics>({
    defaultLogisticsName: "",
    logisticsProvider: LOGISTICS_PROVIDER_KDNIAO,
    kdniaoApiKey: "",
    kdniaoBusinessId: "",
    kuaidi100Key: "",
    kuaidi100Secret: "",
    kuaidi100Customer: "",
    mobile: "",
    provinceName: "",
    cityName: "",
    areaName: "",
    address: "",
    sender: "",
    receiveSender: "",
    receiveMobile: "",
    receiveProvinceName: "",
    receiveCityName: "",
    receiveAreaName: "",
    receiveAddress: ""
});
const isKdniao = computed(() => Number(formState.value.logisticsProvider) !== 2);

const loadFilter = async () => {
    try {
        const result = await getShippingSettings();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        // 后端未返回时按快递鸟兜底，避免单选无选中态
        formState.value.logisticsProvider = Number(result?.logisticsProvider) === 2 ? 2 : LOGISTICS_PROVIDER_KDNIAO;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    loadFilter();
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateShippingSettings(formState.value);
        // 物流公司页依赖 provider 渲染 tab 与编号列，保存后必须失效缓存
        clearLogisticsProviderCache();
        // 重新拉取以刷新后端返回的掩码串，避免下次提交把旧掩码当新明文
        await loadFilter();
        emit("submitCallback", result);
        message.success(t('保存成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}
// 字段名与输入框垂直齐平（含下方说明文案时，标签对齐首行输入框）
.content_wrapper {
    :deep(.el-form-item) {
        align-items: flex-start;
    }
    :deep(.el-form-item__label) {
        min-height: 32px;
        height: 32px;
        line-height: 32px;
        align-items: center;
    }
}
</style>
