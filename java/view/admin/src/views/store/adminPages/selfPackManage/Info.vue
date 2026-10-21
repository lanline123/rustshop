<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item v-if="getAdminType() == 'admin' && action == 'add'" :label="$t('所属类型')" prop="status" required>
                        <el-radio-group v-model="type">
                            <el-radio :value="1">{{ $t('平台自营') }}</el-radio>
                            <el-radio :value="2">{{ $t('门店自营') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item
                        v-if="getAdminType() == 'admin' && action == 'add' && type == 2"
                        :label="$t('所属门店')"
                        prop="storeParentId"
                        :rules="[{ required: true, message: '请选择所属门店!' }]"
                    >
                        <DialogForm
                            :params="{ isMultiple: false, selectedIds: [formState.storeParentId] }"
                            isDrawer
                            path="store/adminPages/selfPackManage/src/SelectStoreList"
                            :title="$t('选择门店')"
                            width="700px"
                            @okCallback="callback"
                        >
                            <el-button type="primary">{{ $t('选择门店') }}</el-button>
                        </DialogForm>
                        <el-table v-if="formTable.length > 0" :data="formTable" row-key="storeParentId" class="mt10">
                            <el-table-column :label="$t('门店名称')">
                                <template #default="{ row }">
                                    {{ row.shopTitle || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店LOGO')">
                                <template #default="{ row }">
                                    <Image :src="row.shopLogo" fit="contain" style="height: 25px; width: 60px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商户名称')" :width="200">
                                <template #default="{ row }">
                                    <div v-if="row.merchant">
                                        <span v-if="row.merchant.type == 2">{{ row.merchant.merchantData?.merchantName || "--" }}</span>
                                        <span v-if="row.merchant.type == 1">{{ row.merchant.corporateName || "--" }}</span>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('门店状态')" sortable="custom">
                                <template #default="{ row }">
                                    <template v-if="row.status == 10">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 4">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 1">
                                        <StatusDot color="green" :flicker="true"></StatusDot>
                                    </template>
                                    <span v-if="row.status === 10 || row.status === 4" style="color: red">{{ row.statusText }}</span>
                                    <span v-else-if="row.status === 1" style="color: green">{{ row.statusText }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('认证日期')" prop="addTime" sortable="custom"></el-table-column>
                        </el-table>
                    </el-form-item>
                    <el-form-item :label="$t('自提点LOGO')" prop="shopLogo" :rules="[{ required: true, message: '请上传自提点LOGO!' }]">
                        <FormAddGallery v-if="!loading" v-model:photo="formState.shopLogo"> </FormAddGallery>
                        <div class="extra">{{ $t('建议尺寸100x100') }}</div>
                    </el-form-item>

                    <!-- <el-form-item label="门头封面" prop="shopCoverPicture" :rules="[{ required: true, message: '请上传门头封面!' }]">
                        <FormAddGallery v-if="!loading" v-model:photo="formState.shopCoverPicture"> </FormAddGallery>
                        <div class="extra">
                            您上传的图片，将作为用户端门店首页的头图，建议尺寸 375x150
                            <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>查看示例</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/store/shopCoverPicture.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item> -->

                    <!-- <el-form-item label="自提点图片" prop="shopShowPicture">
                        <FormAddGallery isMultiple v-if="!loading" v-model:photos="formState.shopShowPicture"> </FormAddGallery>
                        <div class="extra">
                           支持拖动排序，建议尺寸100x100像素，jpg、jpeg、png格式
                        </div>
                    </el-form-item> -->

                    <el-form-item :label="$t('自提点名称')" prop="shopTitle" :rules="[{ required: true, message: '自提点名称不能为空!' }]">
                        <TigInput classType="tig-form-input" v-model="formState.shopTitle" :maxlength="100" showWordLimit>
                            <template #append> {{ $t('（自提点）') }} </template>
                        </TigInput>
                    </el-form-item>

                    <!-- <el-form-item label="自提点公告" prop="description">
                        <TigInput
                            width="90%"
                            placeholder="请填写自提点公告"
                            v-model="formState.description"
                            type="textarea"
                            :maxlength="200"
                            showWordLimit
                            :rows="4"
                        />
                        <div class="extra">
                            请填写自提点简介，该简介会显示在用户端门店详细中
                            <el-popover :width="400" placement="right-end" trigger="click">
                                <template #reference>
                                    <a>查看示例</a>
                                </template>
                                <template #default>
                                    <img src="@/style/images/store/shopTips.png" style="width: 380px" />
                                </template>
                            </el-popover>
                        </div>
                    </el-form-item> -->

                    <el-form-item :label="$t('营业状态')" prop="status" required>
                        <el-radio-group v-model="formState.status">
                            <el-radio :value="1">{{ $t('启用') }}</el-radio>
                            <el-radio :value="10">{{ $t('禁用') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <el-form-item :label="$t('自提点地址')" prop="shopRegionIds" :rules="[{ required: true, message: '请选择自提点地址!' }]">
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
                                <MapContainer
                                    ref="MapRef"
                                    :defaultPosition="defaultPosition"
                                    :regionNames="regionTextArr"
                                    @positionChange="updatePosition"
                                ></MapContainer>
                            </div>
                        </div>
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
import { Image } from "@/components/image";
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
import BusinessHours from "@/views/store/adminPages/store/src/BusinessHours.vue";
import BusinessMobile from "@/views/store/adminPages/store/src/BusinessMobile.vue";
import { cloneDeep } from "lodash";
import { getAdminType } from "@/utils/storage";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const type = ref<number>(1);
// 父组件回调
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
// const isSpecified = ref<boolean>(false);
const formState = ref<ShopFormState>({
    shopType: 3,
    status: 1,
    shopTitle: ""
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
    formState.value.shopLongitude = "";
    formState.value.shopLatitude = "";
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
    try {
        const result = await getShop(action.value, { shopId: id.value });
        result.shopTitle = result.shopTitle.replace(t('（自提点）'), "");
        Object.assign(formState.value, result);
        // 直接使用后端返回的区域名称数组
        regionTextArr.value = result.shopRegionNames || [];
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
// const _getMerchantList = async () => {
//     try {
//         const result = await getMerchantList({ storeParentId: id.value });
//         formState.value.storeParentId = id.value;
//         formTable.value = result.records;
//     } catch (error: any) {
//         message.error(error.message);
//         emit("close");
//     } finally {
//         loading.value = false;
//     }
// };
const formTable = ref<any[]>([]);
const callback = async (data: any) => {
    formTable.value = data.shopInfo;
    formState.value.storeParentId = data.shopId;
};

onMounted(() => {
    // 获取详情数据
    if (operation === "update") {
        fetchShopInfo();
    } else {
        loading.value = false;
    }
    formTable.value = [];
    formState.value.storeParentId = 0;
    // if (id.value != 0) {
    //     isSpecified.value = true;
    //     _getMerchantList();
    // }
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
    if (type.value == 2 && (formState.value.storeParentId == 0 || formState.value.storeParentId == null || !formState.value.storeParentId)) {
        message.error(t('请选择所属商户!'));
        return;
    }
    formState.value.shopTitle += t('（自提点）');
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
