<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="120px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper">
                    <div class="title">{{ $t('发货信息设置') }}</div>
                    <el-form-item :label="$t('发货人')" prop="shipInfo.sender">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.sender" type="text" :placeholder="$t('请输入发货人')" />
                    </el-form-item>
                    <el-form-item :label="$t('发货人联系方式')" prop="shipInfo.mobile">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.mobile" type="text" :placeholder="$t('请输入发货人联系方式')" />
                    </el-form-item>
                    <el-form-item :label="$t('发货地省份')" prop="shipInfo.provinceName">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.provinceName" type="text" :placeholder="$t('请输入发货地省份')" />
                    </el-form-item>
                    <el-form-item :label="$t('发货城市')" prop="shipInfo.cityName">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.cityName" type="text" :placeholder="$t('请输入发货城市')" />
                    </el-form-item>
                    <el-form-item :label="$t('发货地区')" prop="shipInfo.areaName">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.areaName" type="text" :placeholder="$t('请输入发货地区')" />
                    </el-form-item>
                    <el-form-item :label="$t('发货详细地址')" prop="shipInfo.address">
                        <TigInput classType="tig-form-input" v-model="formState.shipInfo.address" type="textarea" :rows="3" :placeholder="$t('请输入发货详细地址')" />
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('收货信息设置') }}</div>
                    <el-form-item :label="$t('收货人')" prop="receiveInfo.sender">
                        <TigInput classType="tig-form-input" v-model="formState.receiveInfo.sender" type="text" :placeholder="$t('请输入收货人')" />
                    </el-form-item>
                    <el-form-item :label="$t('收货人联系方式')" prop="receiveInfo.mobile">
                        <TigInput classType="tig-form-input" v-model="formState.receiveInfo.mobile" type="text" :placeholder="$t('请输入收货人联系方式')" />
                    </el-form-item>
                    <el-form-item :label="$t('收货地省份')" prop="receiveInfo.provinceName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveInfo.provinceName" type="text" :placeholder="$t('请输入收货地省份')" />
                    </el-form-item>
                    <el-form-item :label="$t('收货城市')" prop="receiveInfo.cityName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveInfo.cityName" type="text" :placeholder="$t('请输入收货城市')" />
                    </el-form-item>
                    <el-form-item :label="$t('收货地区')" prop="receiveInfo.areaName">
                        <TigInput classType="tig-form-input" v-model="formState.receiveInfo.areaName" type="text" :placeholder="$t('请输入收货地区')" />
                    </el-form-item>
                    <el-form-item :label="$t('收货详细地址')" prop="receiveInfo.address">
                        <TigInput
                            classType="tig-form-input"
                            v-model="formState.receiveInfo.address"
                            type="textarea"
                            :rows="3"
                            :placeholder="$t('请输入收货详细地址')"
                        />
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
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRoute } from "vue-router";
import { message } from "ant-design-vue";
import { getShopInfo } from "@/api/authority/accountEditing";
import { saveShopSetting } from "@/api/merchant/setting/team";
import { saveVendorSetting, vendorSetting } from "@/api/vendor/setting";
import type { ShipReceiveContactInfo, ShipReceiveInfoFormState } from "@/types/setting/shipInfo";
import { useThemeStore } from "@/store/theme";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const route = useRoute();
const formRef = shallowRef();
const loading = ref(true);
const confirmLoading = ref(false);
const originalFormData = ref<Record<string, any>>({});

const createEmptyContactInfo = (): ShipReceiveContactInfo => ({
    sender: "",
    mobile: "",
    provinceName: "",
    cityName: "",
    areaName: "",
    address: ""
});

const mode = computed(() => (route.meta.shipInfoMode === "vendor" ? "vendor" : "shop"));

const formState = ref<Required<ShipReceiveInfoFormState>>({
    shopId: 0,
    shipInfo: createEmptyContactInfo(),
    receiveInfo: createEmptyContactInfo()
});

const normalizeContactInfo = (info?: ShipReceiveContactInfo | null): ShipReceiveContactInfo => ({
    sender: info?.sender ?? "",
    mobile: info?.mobile ?? "",
    provinceName: info?.provinceName ?? "",
    cityName: info?.cityName ?? "",
    areaName: info?.areaName ?? "",
    address: info?.address ?? ""
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = mode.value === "vendor" ? await vendorSetting() : await getShopInfo();
        originalFormData.value = { ...result };
        formState.value = {
            shopId: result?.shopId ?? 0,
            shipInfo: normalizeContactInfo(result?.shipInfo),
            receiveInfo: normalizeContactInfo(result?.receiveInfo)
        };
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const payload = {
            ...originalFormData.value,
            shipInfo: formState.value.shipInfo,
            receiveInfo: formState.value.receiveInfo
        };
        if (mode.value === "vendor") {
            await saveVendorSetting(payload);
        } else {
            await saveShopSetting(payload);
        }
        message.success(t('保存成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.content_wrapper {
    min-height: auto;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
</style>
