<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item
                        v-if="getAdminType() == 'admin' && action == 'add'"
                        :label="$t('所属商户')"
                        prop="merchantId"
                        :rules="[{ required: true, message: '请选择所属商户!' }]"
                    >
                        <DialogForm
                            v-if="!isSpecified"
                            :params="{ isMultiple: false, selectedIds: [formState.merchantId] }"
                            isDrawer
                            path="adminMerchant/merchant/src/SelectMerchant"
                            :title="$t('选择商户')"
                            width="1100px"
                            @okCallback="callback"
                        >
                            <el-button type="primary">{{ $t('选择商户') }}</el-button>
                        </DialogForm>
                        <el-table v-if="formTable.length > 0" :data="formTable" row-key="merchantId" class="mt10">
                            <el-table-column :label="$t('商户所属单位')">
                                <template #default="{ row }">
                                    <div class="ssdw">
                                        <template v-if="row.type == 1">
                                            <Tag :transparent="true" :text="$t('个人')"></Tag>
                                        </template>
                                        <template v-else>
                                            <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                                        </template>
                                        {{ row.type == 1 ? "" + row.corporateName : "" + row.companyName }}
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户所属会员(ID)')">
                                <template #default="{ row }"> {{ row.user?.username }}{{ row.user?.userId ? "(" + row.user?.userId + ")" : "" }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户类型')" prop="typeText"></el-table-column>
                            <el-table-column :label="$t('管理员')">
                                <template #default="{ row }">
                                    {{ row.admin?.username || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('认证状态')" prop="statusText" sortable="custom">
                                <template #default="{ row }">
                                    <template v-if="row.status == 1">
                                        <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 2">
                                        <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                    </template>
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                    <el-form-item :label="$t('门店LOGO')" prop="shopLogo" :rules="[{ required: true, message: '请上传门店LOGO!' }]">
                        <FormAddGallery v-if="!loading" v-model:photo="formState.shopLogo"> </FormAddGallery>
                        <div class="extra">{{ $t('建议尺寸300x300') }}</div>
                    </el-form-item>

                    <el-form-item
                        :label="$t('门头封面')"
                        prop="shopCoverPicture"
                        :rules="[{ required: true, message: '请上传门头封面!' }]"
                        v-if="getAdminType() == 'admin'"
                    >
                        <FormAddGallery v-if="!loading" v-model:photo="formState.shopCoverPicture"> </FormAddGallery>
                        <div class="extra"> {{ $t('您上传的图片，将作为用户端门店首页的头图，建议尺寸 1500x1500') }} <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>{{ $t('查看示例') }}</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/store/shopCoverPicture.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('门店名称')" prop="shopTitle" :rules="[{ required: true, message: '门店名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.shopTitle" :maxlength="100" showWordLimit />
                    </el-form-item>

                    <el-form-item :label="$t('门店公告')" prop="description">
                        <TigInput
                            width="90%"
                            :placeholder="$t('请填写门店公告')"
                            v-model="formState.description"
                            type="textarea"
                            :maxlength="200"
                            showWordLimit
                            :rows="4"
                        />
                        <div class="extra">{{ $t('请填写门店简介，该简介会显示在用户门店详细中') }}</div>
                    </el-form-item>

                    <el-form-item :label="$t('门店地址')" prop="shopRegionIds" :rules="[{ required: true, message: '请选择门店地址!' }]">
                        <div>
                            <SelectRegion
                                v-if="action == 'add' || !loading"
                                v-model:selectedIds="formState.shopRegionIds"
                                @change="onRegionChange"
                                @clear="onClear"
                                style="width: 260px; margin-bottom: 10px"
                            ></SelectRegion>
                            <TigInput width="500px" :placeholder="$t('详细地址')" v-model="formState.shopDetailedAddress" @Input="updateMapInfo" />
                            <div class="map-container">
                                <MapContainer ref="MapRef" :defaultPosition="defaultPosition" :regionNames="regionTextArr" @positionChange="updatePosition">
                                </MapContainer>
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('门店标签')" prop="shopTips">
                        <div>
                            <div v-if="selectedshopTips">
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
                                    <el-button link type="primary"> {{ $t('选择标签') }} </el-button>
                                </DialogForm>
                            </div>
                            <div class="extra"> {{ $t('门店标签将显示在门店列表及门店首页中') }} <el-popover :width="400" placement="right-end" trigger="click">
                                    <template #reference>
                                        <a>{{ $t('查看示例') }}</a>
                                    </template>
                                    <template #default>
                                        <img src="@/style/images/store/shopTips.png" style="width: 380px" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('营业状态')" prop="status" required>
                        <el-radio-group v-model="formState.status">
                            <el-radio :value="1">{{ $t('开业') }}</el-radio>
                            <el-radio :value="4">{{ $t('停业') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <BusinessMobile :formData="formState.shopContactConfig" ref="businessMobileRef"></BusinessMobile>
                    <BusinessHours :formData="formState.shopOpenCloseConfig" ref="businessHoursRef"></BusinessHours>

                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { ShopFormState } from "@/types/shop/shop.d";
import { getMerchantList } from "@/api/adminMerchant/merchant";
import { getShop, updateShop } from "@/api/shop/shop";
import { SelectRegion } from "@/components/select";
import MapContainer from "@/components/map/MapContainer.vue";
import { matchRegionByNames } from "@/utils/regionMatch";
import BusinessHours from "./src/BusinessHours.vue";
import BusinessMobile from "./src/BusinessMobile.vue";
import { cloneDeep } from "lodash";
import { getAdminType } from "@/utils/storage";
import StatusDot from "@/components/form/src/StatusDot.vue";
import type { Record } from "@/types/store/storeTipManage";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const MapRef = ref<any>();
const businessHoursRef = ref<any>();
const businessMobileRef = ref<any>();
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const isSpecified = ref<boolean>(false);
const selectedshopTips = ref<Record[]>([]);
const formState = ref<ShopFormState>({
    shopType: 2,
    status: 1,
    shopTips: [],
    shopTitle: "",
    merchantId: (query.id as string) ?? ""
});

const defaultPosition = computed(() => {
    if (formState.value.shopLatitude && formState.value.shopLongitude) {
        return formState.value.shopLongitude + "," + formState.value.shopLatitude;
    } else {
        return "";
    }
});

const regionTextArr = ref<string[]>([]); // 区域名称数组，传给 MapContainer

const onRegionChange = (regionObjects: any[]) => {
    const latestRegionNames = regionObjects?.map((item: any) => item.regionName) || [];
    regionTextArr.value = latestRegionNames;
    formState.value.shopDetailedAddress = "";
    // 等待 regionNames 传递到子组件后再搜索，避免使用上一次地区前缀
    nextTick(() => {
        if (MapRef.value?.search && latestRegionNames.length) {
            MapRef.value.search(latestRegionNames.join(""));
        }
    });
};

const updateMapInfo = () => {
    // 直接传详细地址，MapContainer 会自动添加区域前缀
    if (MapRef.value?.search && formState.value.shopDetailedAddress?.trim()) {
        MapRef.value.search(formState.value.shopDetailedAddress);
    }
};

const onClear = () => {
    regionTextArr.value = [];
    formState.value.shopRegionIds = [];
    formState.value.shopDetailedAddress = "";
    MapRef.value?.clearSearch();
};

const updatePosition = (data: any) => {
    formState.value.shopLongitude = data.position[0];
    formState.value.shopLatitude = data.position[1];
    formState.value.shopDetailedAddress = data.poiAddress || data.name;

    if (data.addressComponent) {
        const { province, city, district, adcode } = data.addressComponent;
        const match = matchRegionByNames(province, city, district, adcode);
        if (match.ids.length > 0) {
            formState.value.shopRegionIds = match.ids;
            regionTextArr.value = match.names;
        }
    }
};

const fetchShopInfo = async () => {
    selectedshopTips.value = [];
    try {
        const result = await getShop(action.value, { shopId: id.value });
        formState.value = result;
        // 直接使用后端返回的区域名称数组
        regionTextArr.value = result.shopRegionNames || [];
        if (result.tips && result.tips.length > 0) {
            selectedshopTips.value = result.tips;
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const _getMerchantList = async () => {
    try {
        const result = await getMerchantList({ merchantId: id.value });
        formState.value.merchantId = id.value;
        formTable.value = result.records;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    }
};
const formTable = ref<any[]>([]);
const callback = async (data: any) => {
    formTable.value = data.merchant;
    formState.value.merchantId = data.merchantId;
};

const selectShopTips = (data: any) => {
    selectedshopTips.value = data.value;
};

onMounted(() => {
    // 获取详情数据
    if (operation === "update") {
        fetchShopInfo();
    } else {
        loading.value = false;
    }
    formTable.value = [];
    formState.value.merchantId = 0;
    if (id.value != 0) {
        isSpecified.value = true;
        _getMerchantList();
    }
});

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
    const copiedVal = cloneDeep(data);
    let formData: any = {};
    formData.type = copiedVal.type;
    if (copiedVal.type === 1) {
        formData.times = [];
    } else if (copiedVal.type === 2) {
        formData.times = cloneDeep(copiedVal.times);
    } else if (copiedVal.type === 3) {
        formData.times = cloneDeep(copiedVal.timesWeek);
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

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    await businessHoursRef.value.submit();
    formState.value.shopContactConfig = businessMobileFrom(businessMobileRef.value.formData);
    formState.value.shopOpenCloseConfig = businessHoursForm(businessHoursRef.value.formData);
    if (formState.value.merchantId == 0 || formState.value.merchantId == null || !formState.value.merchantId) {
        message.error(t('请选择所属商户!'));
        return;
    }
    if (selectedshopTips.value.length > 0) {
        formState.value.shopTips = [];
        selectedshopTips.value.forEach((item: any) => {
            formState.value.shopTips.push(item.tipId);
        });
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updateShop(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
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
<style scoped lang="less">
.map-container {
    margin-top: 20px;
}
</style>
