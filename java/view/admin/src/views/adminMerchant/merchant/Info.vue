<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                    <el-tab-pane :key="1" :label="$t('基础信息')" name="sm"></el-tab-pane>
                    <el-tab-pane v-if="formState.type == 2" :key="2" :label="$t('企业信息')" name="company"></el-tab-pane>
                    <el-tab-pane :key="3" :label="formState.type == 1 ? $t('个人信息') : $t('法人代表信息')" name="base"></el-tab-pane>
                    <el-tab-pane :key="4" :label="$t('商户信息')" name="merchant"></el-tab-pane>
                    <el-tab-pane :key="5" :label="isStore() ? $t('门店信息') : $t('店铺信息')" name="shop"></el-tab-pane>
                </el-tabs>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <template v-if="activeKey == 'sm'">
                        <el-form-item :label="$t('认证状态')">
                            <Switch
                                @updateData="updateDataWithList"
                                :switchingCode="[2, 1]"
                                v-model:checked="formState.status"
                                :requestApi="updateMerchantField"
                                :params="{ id: formState.merchantId, field: 'status' }"
                            />
                        </el-form-item>
                        <el-form-item :label="isStore() ? $t('预期门店名称') : $t('预期店铺名称')">
                            <div class="width100">{{ formState.shopData?.shopTitle }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('入驻资质')" prop="status">
                            <div class="width100">{{ formState.type == 1 ? $t('个人入驻') : $t('企业入驻') }}</div>
                        </el-form-item>
                        <template v-if="formState.type == 1"></template>
                        <template v-if="formState.type == 2">
                            <el-form-item :label="$t('企业名称')">
                                <div class="width100">{{ formState.baseData.companyName }}</div>
                            </el-form-item>
                        </template>
                        <el-form-item :label="$t('联系人姓名')">
                            <div class="width100">{{ formState.merchantData.contactName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('联系人手机')">
                            <div class="width100">
                                <MobileCard :mobile="formState.merchantData.contactPhone"></MobileCard>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('联系人邮箱')">
                            <div class="width100">{{ formState.merchantData.contactEmail }}</div>
                        </el-form-item>
                    </template>
                    <template v-if="activeKey == 'company'">
                        <el-form-item :label="$t('企业名称')">
                            <div class="width100">{{ formState.baseData.companyName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('注册地址')">
                            <div class="width100">{{ formState.baseData.fullBusinessLicenseAddress }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('经营范围')">
                            <div class="width100">{{ formState.baseData.businessScope }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('统一社会信用代码')">
                            <div class="width100">{{ formState.baseData.businessLicenseId }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('营业期限')">
                            <div class="width100">
                                <template v-if="formState.baseData.operatingTermType == 1">
                                    {{ formState.baseData.operatingTermTypeDate[0] + " ～ " + formState.baseData.operatingTermTypeDate[1] }}
                                </template>
                                <template v-else> {{ $t('{operatingTermTypeEnd}～长期', { operatingTermTypeEnd: formState.baseData.operatingTermTypeEnd }) }}</template>
                            </div>
                        </el-form-item>
                    </template>
                    <template v-if="activeKey == 'base'">
                        <el-form-item :label="formState.type == 1 ? $t('个人姓名') : $t('法定代表人姓名')">
                            <div class="width100">{{ formState.baseData.corporateName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('证件类型')">
                            <div class="width100">
                                {{ documentTypeOptions.find((option) => option.value === formState.baseData.documentType)?.label || "" }}
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件号码')">
                            <div class="width100">{{ formState.baseData.documentNumber }}</div>
                        </el-form-item>
                        <el-form-item v-if="formState.type == 1" :label="$t('性别')">
                            <div class="width100">{{ formState.baseData.sex == 1 ? $t('男') : $t('女') }}</div>
                        </el-form-item>
                        <el-form-item v-if="formState.type == 2" :label="$t('居住地址')">
                            <div class="width100">{{ formState.baseData.residentialAddress }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('证件有效期')">
                            <div class="width100" v-if="formState.baseData.certificateValidityPeriodDate">
                                <template v-if="formState.baseData.certificateValidityPeriod == 1">
                                    {{ formState.baseData.certificateValidityPeriodDate[0] + " ～ " + formState.baseData.certificateValidityPeriodDate[1] }}
                                </template>
                                <template v-else> {{ $t('{certificateValidityPeriodEnd}～ 长期', { certificateValidityPeriodEnd: formState.baseData.certificateValidityPeriodEnd }) }}</template>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件照正面')">
                            <div class="width100">
                                <template v-if="!Array.isArray(formState.baseData?.frontOfPhoto[0])">
                                    <el-image
                                        :src="imageFormat(formState.baseData.frontOfPhoto[0].picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(formState.baseData.frontOfPhoto[0].picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件照反面')">
                            <div class="width100">
                                <template v-if="!Array.isArray(formState.baseData?.backOfPhoto[0])">
                                    <el-image
                                        :src="imageFormat(formState.baseData.backOfPhoto[0].picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(formState.baseData.frontOfPhoto[0].picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                        <el-form-item v-if="formState.baseData.supplementaryInformation.length > 0" :label="$t('补充信息')">
                            <div class="width100">
                                <template v-for="item in formState.baseData.supplementaryInformation">
                                    <el-image
                                        :src="imageFormat(item.picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(item.picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                    </template>
                    <template v-if="activeKey == 'merchant'">
                        <template v-if="formState.type == 1"> </template>
                        <template v-if="formState.type == 2">
                            <el-form-item :label="$t('开户许可证')">
                                <div class="width100">
                                    <template v-if="formState.merchantData.accountOpeningPermit?.length > 0">
                                        <el-image
                                            :src="imageFormat(formState.merchantData.accountOpeningPermit[0].picUrl)"
                                            class="image-size"
                                            fit="cover"
                                            @click="showImage(imageFormat(formState.merchantData.accountOpeningPermit[0].picUrl))"
                                        ></el-image>
                                    </template>
                                </div>
                            </el-form-item>
                        </template>
                        <el-form-item :label="$t('商户名称')">
                            <div class="width100">{{ formState.merchantData.merchantName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('经营地区')">
                            <div class="width100">{{ formState.merchantData.fullDetailedAddress }}</div>
                        </el-form-item>
                        <el-form-item v-if="formState.type == 2" :label="$t('营业执照')">
                            <div class="width100">
                                <template v-if="formState.merchantData.businessLicenseImg?.length > 0">
                                    <el-image
                                        :src="imageFormat(formState.merchantData.businessLicenseImg[0].picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(formState.merchantData.businessLicenseImg[0].picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('客服电话')">
                            <div class="width100">{{ formState.merchantData.contactPhone }}</div>
                        </el-form-item>
                    </template>
                    <template v-if="activeKey == 'shop'">
                        <el-form-item :label="isStore() ? $t('门店名称') : $t('店铺名称')">
                            <div class="width100">{{ formState.shopData?.shopTitle }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('联系电话')">
                            <div class="width100">
                                <MobileCard :mobile="formState?.shopData?.contactMobile"></MobileCard>
                            </div>
                        </el-form-item>
                        <el-form-item :label="isStore() ? $t('门店logo') : $t('店铺logo')">
                            <div class="width100">
                                <template v-if="formState.shopData?.shopLogo?.length > 0">
                                    <el-image
                                        :src="imageFormat(formState.shopData?.shopLogo[0].picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(formState.shopData?.shopLogo[0].picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                        <el-form-item v-if="formState.shopData?.description" :label="isStore() ? $t('门店简介') : $t('店铺简介')">
                            <div class="width100">{{ formState.shopData?.description }}</div>
                        </el-form-item>
                    </template>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
    <el-dialog v-model="dialogVisible">
        <div class="image">
            <el-image :src="dialogImageUrl" alt="" fit="cover" />
        </div>
    </el-dialog>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { Switch } from "@/components/list";
import { MerchantFormState } from "@/types/adminMerchant/merchant";
import { getMerchant, updateMerchantField } from "@/api/adminMerchant/merchant";
import { imageFormat } from "@/utils/format";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { isStore } from "@/utils/version";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "callback", "close"]);
const activeKey = ref<string>("sm");
const onTabChange = (val: string) => {
    activeKey.value = val;
};
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
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<MerchantFormState>({} as MerchantFormState);
const fetchMerchant = async () => {
    try {
        const result = await getMerchant(action.value, { id: id.value });
        formState.value = result;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const documentTypeOptions = ref([
    {
        value: 1,
        label: t('大陆身份证')
    },
    {
        value: 2,
        label: t('护照（限境外人士）')
    },
    {
        value: 3,
        label: t('来往内地通行证（中国香港居民）')
    },
    {
        value: 4,
        label: t('来往内地通行证（中国澳门居民）')
    },
    {
        value: 5,
        label: t('来往大陆通行证（中国台湾居民）')
    }
]);
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchMerchant();
    } else {
        loading.value = false;
    }
});
// 会同时更新列表数据
const updateDataWithList = () => {
    emit("callback");
};

const dialogImageUrl = ref("");
const dialogVisible = ref(false);
const showImage = (url: string) => {
    dialogImageUrl.value = url;
    dialogVisible.value = true;
};

const authentication = () => {};
</script>
<style lang="less" scoped>
.width100 {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    word-break: break-word;
}

:deep(.el-tabs__content) {
    display: none;
}

.image-size {
    width: 80px;
    height: 80px;
    border: 1px solid #e0e0e0;
    cursor: pointer;
}

.image {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
