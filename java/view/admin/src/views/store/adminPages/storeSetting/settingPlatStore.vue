<template>
    <a-spin :spinning="loading">
        <div class="content_wrapper">
            <el-form ref="formRef" :model="formState" label-width="auto" style="display: flex; gap: 0.75rem; flex-direction: column">
                <el-form-item :label="$t('门店LOGO')" prop="storeSelfOperatedLogo" :rules="[{ required: true, message: '请上传门店LOGO!' }]">
                    <FormAddGallery v-if="!loading" v-model:photo="formState.storeSelfOperatedLogo"> </FormAddGallery>
                    <div class="extra">{{ $t("建议尺寸300x300") }}</div>
                </el-form-item>

                <el-form-item :label="$t('门店名称')" prop="storeSelfOperatedName" :rules="[{ required: true, message: '门店名称不能为空!' }]">
                    <TigInput width="30%" v-model="formState.storeSelfOperatedName" showWordLimit :maxlength="20" />
                </el-form-item>

                <el-form-item :label="$t('门店公告')" prop="storeSelfOperatedDescription">
                    <TigInput
                        width="90%"
                        :placeholder="$t('请填写门店公告')"
                        v-model="formState.storeSelfOperatedDescription"
                        type="textarea"
                        :maxlength="200"
                        showWordLimit
                        :rows="4"
                    />
                    <div class="extra">{{ $t("请填写门店简介，该简介会显示在用户门店详细中") }}</div>
                </el-form-item>

                <el-form-item :label="$t('门店地址')" prop="storeSelfOperatedAddress" :rules="[{ required: true, message: '请选择门店地址!' }]">
                    <div>
                        <SelectRegion v-model:selectedIds="regionNames" @change="onRegionChange" @clear="onClear" style="width: 260px; margin-bottom: 10px">
                        </SelectRegion>
                        <TigInput width="500px" :placeholder="$t('详细地址')" v-model="formState.storeSelfOperatedAddress" @Input="updateMapInfo" />
                        <div class="map-container">
                            <MapContainer ref="MapRef" :defaultPosition="defaultPosition" :regionNames="regionTextArr" @positionChange="updatePosition">
                            </MapContainer>
                        </div>
                    </div>
                </el-form-item>

                <el-form-item :label="$t('门店标签')" prop="shopTips">
                    <div>
                        <div v-if="selectedshopTips.length">
                            <el-tag v-for="(item, index) in selectedshopTips" :key="index" class="mr10">{{ item.tipName }}</el-tag>
                        </div>
                        <div>
                            <DialogForm
                                :params="{ selectedIds: selectedshopTips }"
                                isDrawer
                                path="store/adminPages/storeTipManage/src/SelectTips"
                                :title="$t('选择标签')"
                                width="600px"
                                @okCallback="selectShopTips"
                            >
                                <el-button link type="primary"> {{ $t("选择标签") }} </el-button>
                            </DialogForm>
                        </div>
                        <div class="extra">
                            {{ $t("门店标签将显示在门店列表及门店首页中") }}
                            <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>{{ $t("查看示例") }}</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/store/shopTips.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </div>
                </el-form-item>

                <el-form-item :label="$t('营业状态')" prop="storeSelfOperatedStatus" required>
                    <el-radio-group v-model="formState.storeSelfOperatedStatus">
                        <el-radio :value="1">{{ $t("开业") }}</el-radio>
                        <el-radio :value="4">{{ $t("停业") }}</el-radio>
                    </el-radio-group>
                </el-form-item>

                <BusinessMobile :formData="formState.shopContactConfig" ref="businessMobileRef"></BusinessMobile>
                <BusinessHours :formData="formState.shopOpenCloseConfig" ref="businessHoursRef"></BusinessHours>
            </el-form>
        </div>

        <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '23.0625rem' : '16.875rem' }">
            <div class="selected-action">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import MapContainer from "@/components/map/MapContainer.vue";
import { matchRegionByNames } from "@/utils/regionMatch";
import { onMounted, ref, shallowRef, computed, nextTick } from "vue";
import { message } from "ant-design-vue";
import { SelfShopDetailResponse } from "@/types/setting/intraCityDelivery";
import { saveSelfShopDetail, getSelfShopDetail } from "@/api/setting/intraCityDelivery";
import { getConfigBasic } from "@/api/setting/config";
import { useThemeStore } from "@/store/theme";
import SelectRegion from "@/components/select/src/SelectRegion.vue";
import { getShop } from "@/api/shop/shop";
import { getShopType } from "@/utils/storage";
import { FormAddGallery } from "@/components/gallery";
import { DialogForm } from "@/components/dialog";
import { selectAllTip } from "@/api/store/storeTipManage";
import BusinessHours from "@/views/store/adminPages/store/src/BusinessHours.vue";
import BusinessMobile from "@/views/store/adminPages/store/src/BusinessMobile.vue";
import { cloneDeep } from "lodash";
import type { Record } from "@/types/store/storeTipManage";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const MapRef = ref<any>();
const businessHoursRef = ref<any>();
const businessMobileRef = ref<any>();
const regionNames = ref<any[]>([]);
const regionTextArr = ref<string[]>([]); // 区域名称数组，传给 MapContainer
const selectedshopTips = ref<Record[]>([]);

// 基本参数定义
const confirmLoading = ref<boolean>(false);
const formState = ref<SelfShopDetailResponse>({
    storeSelfOperatedName: "",
    storeSelfOperatedLogo: "",
    storeSelfOperatedRegion: "",
    storeSelfOperatedAddress: "",
    storeSelfOperatedLongitude: "",
    storeSelfOperatedLatitude: "",
    storeSelfOperatedRegions: [],
    storeSelfOperatedPhone: "",
    storeSelfOperatedDescription: "",
    storeSelfOperatedStatus: 1,
    shopTips: [],
    shopContactConfig: [],
    shopOpenCloseConfig: undefined
});

const loading = ref<boolean>(true);

// 兼容旧数据：将单一电话字段转为联系方式配置
const buildContactConfigFromPhone = (phone?: string) => {
    if (!phone) return [];
    return [{ type: 3, values: phone }];
};

// 获取商城LOGO，作为门店LOGO缺省回退
const getMallLogo = async () => {
    try {
        const cfg = await getConfigBasic();
        return cfg?.shopLogo || "";
    } catch {
        return "";
    }
};

// 标签接口只返回 tipId，名称需按 id 反查
const loadTipsByIds = async (tipIds: number[]) => {
    if (!Array.isArray(tipIds) || !tipIds.length) return [];
    try {
        const list = await selectAllTip({ keyword: "" } as any);
        return (list || []).filter((item: any) => tipIds.includes(item.tipId));
    } catch {
        return [];
    }
};

//加载店铺信息
const loadShopInfo = async () => {
    loading.value = true;
    selectedshopTips.value = [];
    try {
        const mallLogo = await getMallLogo();
        if (getShopType() && JSON.parse(localStorage.getItem("shopInfo") || "{}").shopId) {
            //门店的
            const result = await getShop("detail", { shopId: JSON.parse(localStorage.getItem("shopInfo") || "{}").shopId });
            const { shopTitle, shopLogo, shopLongitude, shopLatitude } = result || {};
            Object.assign(formState.value, {
                storeSelfOperatedName: shopTitle || "",
                storeSelfOperatedLogo: shopLogo || mallLogo || "",
                storeSelfOperatedLongitude: shopLongitude || "",
                storeSelfOperatedLatitude: shopLatitude || "",
                storeSelfOperatedAddress: result?.shopDetailedAddress || "",
                storeSelfOperatedRegion: result?.shopRegionIds?.join(",") || "",
                storeSelfOperatedRegions: result?.shopRegionNames || [],
                storeSelfOperatedDescription: result?.description || "",
                storeSelfOperatedStatus: result?.status ?? 1,
                shopContactConfig: result?.shopContactConfig || [],
                shopOpenCloseConfig: result?.shopOpenCloseConfig || undefined,
                shopTips: result?.shopTips || []
            });
            if (result?.tips && result.tips.length > 0) {
                selectedshopTips.value = result.tips;
            }
        } else {
            //自营的
            const result = await getSelfShopDetail();
            const selfLogo = (result as any)?.storeSelfOperatedLogo || "";
            const shopTips = (result as any)?.shopTips || [];
            formState.value = {
                ...formState.value,
                ...(result as unknown as SelfShopDetailResponse),
                storeSelfOperatedLogo: selfLogo || mallLogo || "",
                storeSelfOperatedStatus: (result as any)?.storeSelfOperatedStatus ?? 1,
                storeSelfOperatedDescription: (result as any)?.storeSelfOperatedDescription || "",
                shopTips,
                shopContactConfig: (result as any)?.shopContactConfig || buildContactConfigFromPhone((result as any)?.storeSelfOperatedPhone),
                shopOpenCloseConfig: (result as any)?.shopOpenCloseConfig || undefined
            };
            selectedshopTips.value = await loadTipsByIds(shopTips);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

//地址联动
const defaultPosition = computed(() => {
    if (formState.value.storeSelfOperatedLatitude && formState.value.storeSelfOperatedLongitude) {
        return formState.value.storeSelfOperatedLongitude + "," + formState.value.storeSelfOperatedLatitude;
    } else {
        return "";
    }
});
//拼接地址
const onRegionChange = (regionObjects: any[]) => {
    const latestRegionNames = regionObjects?.map((item: any) => item.regionName) || [];
    regionTextArr.value = latestRegionNames;
    formState.value.storeSelfOperatedAddress = "";
    // 等待 regionNames 传递到子组件后再搜索，避免使用上一次地区前缀
    nextTick(() => {
        if (MapRef.value?.search && latestRegionNames.length) {
            MapRef.value.search(latestRegionNames.join(""));
        }
    });
};

const onClear = () => {
    regionNames.value = [];
    regionTextArr.value = [];
    formState.value.storeSelfOperatedRegion = "";
    formState.value.storeSelfOperatedAddress = "";
    formState.value.storeSelfOperatedLongitude = "";
    formState.value.storeSelfOperatedLatitude = "";
    MapRef.value?.clearSearch();
};

const updateMapInfo = () => {
    // 直接传详细地址，MapContainer 会自动添加区域前缀
    if (MapRef.value?.search && formState.value.storeSelfOperatedAddress.trim()) {
        MapRef.value.search(formState.value.storeSelfOperatedAddress);
    }
};
const updatePosition = (data: any) => {
    formState.value.storeSelfOperatedLongitude = data.position[0];
    formState.value.storeSelfOperatedLatitude = data.position[1];
    formState.value.storeSelfOperatedAddress = data.poiAddress || data.name;

    if (data.addressComponent) {
        const { province, city, district, adcode } = data.addressComponent;
        const match = matchRegionByNames(province, city, district, adcode);
        if (match.ids.length > 0) {
            regionNames.value = match.ids;
            regionTextArr.value = match.names;
        }
    }
};

const selectShopTips = (data: any) => {
    selectedshopTips.value = data.value;
};

const businessMobileFrom = (data: any) => {
    let formData: any = [];
    data.forEach((item: any) => {
        if (item.checked) {
            item.mobiles.forEach((mobile: any) => {
                formData.push({
                    type: item.type,
                    values: item.type !== 3 ? `${mobile.code}-${mobile.value}` : mobile.value
                });
            });
        }
    });
    return formData;
};

const businessHoursForm = (data: any) => {
    // 子组件 prop 同名 formData 为空时，expose 可能读到 null，兜底为全天
    const copiedVal = cloneDeep(data) || { type: 1, times: [], timesWeek: [] };
    let formData: any = {};
    formData.type = copiedVal.type ?? 1;
    if (formData.type === 1) {
        formData.times = [];
    } else if (formData.type === 2) {
        formData.times = cloneDeep(copiedVal.times) || [];
    } else if (formData.type === 3) {
        formData.times = cloneDeep(copiedVal.timesWeek) || [];
        if (Array.isArray(formData.times) && formData.times.length > 0) {
            formData.times.forEach((item: any) => {
                if (item.dayOfWeek && Array.isArray(item.dayOfWeek)) {
                    item.dayOfWeek = item.dayOfWeek.join(",");
                } else {
                    item.dayOfWeek = [];
                }
            });
        }
    }
    return formData;
};

// 从表单联系方式中提取首个电话，兼容旧字段
const pickPrimaryPhone = (contactConfig: any[]) => {
    if (!Array.isArray(contactConfig) || !contactConfig.length) return "";
    const mobileItem = contactConfig.find((item) => item.type === 3) || contactConfig[0];
    return mobileItem?.values || "";
};

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        await formRef.value.validate();
        await businessHoursRef.value.submit();
        formState.value.shopContactConfig = businessMobileFrom(businessMobileRef.value?.formData);
        // 优先取子组件内部编辑态，避免与同名 prop 为 null 时读到空值
        const hoursData = businessHoursRef.value?.formData ?? formState.value.shopOpenCloseConfig;
        formState.value.shopOpenCloseConfig = businessHoursForm(hoursData);
        formState.value.storeSelfOperatedPhone = pickPrimaryPhone(formState.value.shopContactConfig || []);
        formState.value.shopTips = selectedshopTips.value.map((item: any) => item.tipId);
        confirmLoading.value = true;
        await saveSelfShopDetail({
            storeSelfOperatedName: formState.value.storeSelfOperatedName,
            storeSelfOperatedLogo: formState.value.storeSelfOperatedLogo,
            storeSelfOperatedRegion: regionNames.value?.join(","),
            storeSelfOperatedAddress: formState.value.storeSelfOperatedAddress,
            storeSelfOperatedLongitude: formState.value.storeSelfOperatedLongitude,
            storeSelfOperatedLatitude: formState.value.storeSelfOperatedLatitude,
            storeSelfOperatedPhone: formState.value.storeSelfOperatedPhone,
            storeSelfOperatedDescription: formState.value.storeSelfOperatedDescription,
            storeSelfOperatedStatus: formState.value.storeSelfOperatedStatus,
            shopContactConfig: formState.value.shopContactConfig,
            shopOpenCloseConfig: formState.value.shopOpenCloseConfig,
            shopTips: formState.value.shopTips
        });
        message.success(t("修改成功"));
    } catch (error: any) {
        if (error && error.message) {
            message.error(error.message);
        }
    } finally {
        confirmLoading.value = false;
    }
};

onMounted(async () => {
    await loadShopInfo();
    // 初始化区域ID数组（给SelectRegion用）
    if (formState.value.storeSelfOperatedRegion) {
        regionNames.value = formState.value.storeSelfOperatedRegion.split(",").map(Number);
    }
    // 直接使用后端返回的区域名称数组（给MapContainer用）
    regionTextArr.value = formState.value.storeSelfOperatedRegions || [];
});
</script>
<style lang="less" scoped>
.content_wrapper {
    min-height: calc(100vh - 260px);
}

.map-container {
    margin-top: 20px;
}
</style>
