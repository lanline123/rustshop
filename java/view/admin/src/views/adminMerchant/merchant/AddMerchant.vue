<template>
    <div class="step-section-item">
        <div class="step-section-item__label">{{ $t('主体类型') }}</div>
        <div class="zent-form-control">
            <RadioType
                v-model:modelValue="formState.baseData.type"
                :radioList="[
                    { key: 1, title: '个人' },
                    { key: 2, title: '企业' }
                ]"
            ></RadioType>
            <div class="cert-type-desc-principal">
                <template v-if="formState.baseData.type == 2"> {{ $t('“企业”在营业执照上的主体类型一般为：有限公司、有限责任公司等。') }} </template>
                <template v-else> {{ $t('“个人”为根据法律法规和相关规定免于办理工商登记，无营业执照的商家。') }} </template>
            </div>
        </div>
    </div>
    <el-form v-if="!loading" ref="formStateRef" :model="formState" label-width="auto">
        <el-form-item v-if="action !== 'detail'">
            <template #label> <span class="required">*</span>{{ $t('绑定账号') }} </template>
            <div class="one-item" style="width: 100%">
                <div class="to-time" style="width: 100%">
                    <el-radio-group v-model="adminType">
                        <el-radio :value="true">{{ $t('管理员') }}</el-radio>
                    </el-radio-group>
                    <el-form-item style="width: 100%" :rules="adminType ? [{ required: true, message: '请选择管理员!' }] : []" label="" prop="admin.adminId">
                        <el-select
                            style="width: 100%"
                            v-model="formState.admin.adminId"
                            :disabled="!adminType"
                            filterable
                            remote
                            clearable
                            reserve-keyword
                            :placeholder="$t('可输入手机号查询绑定管理员')"
                            :remote-method="remoteMethod"
                            :loading="loadingVal"
                        >
                            <el-option v-for="item in userInfo" :key="item.key" :label="item.label" :value="item.value" />
                        </el-select>
                    </el-form-item>
                </div>
                <div class="to-time" style="width: 100%">
                    <el-radio-group v-model="adminType">
                        <el-radio :value="false">{{ $t('会    员') }}</el-radio>
                    </el-radio-group>
                    <el-form-item style="width: 100%" :rules="!adminType ? [{ required: true, message: '请选择会员!' }] : []" label="" prop="admin.userId">
                        <el-select
                            style="width: 100%"
                            v-model="formState.admin.userId"
                            :disabled="adminType"
                            filterable
                            remote
                            clearable
                            reserve-keyword
                            :placeholder="$t('可输入手机号查询绑定会员')"
                            :remote-method="remoteMethod"
                            :loading="loadingVal"
                        >
                            <el-option v-for="item in userInfo" :key="item.key" :label="item.label" :value="item.value" />
                        </el-select>
                    </el-form-item>
                </div>
            </div>
            <div class="extra" style="width: 100%; margin-top: 16px">{{ $t('必须绑定一位{adminType}。', { adminType: adminType ? "「管理员」" : "「会员」" }) }}</div>
        </el-form-item>
        <el-tabs v-model="activeKey" tab-position="top" @tab-change="onTabChange">
            <el-tab-pane :key="3" :label="formState.baseData.type == 1 ? $t('个人信息') : $t('法人代表信息')" name="base">
                <el-form-item :label="$t('证件类型')" prop="baseData.documentType">
                    <el-select v-model="formState.baseData.documentType" :placeholder="$t('请选择证件类型')">
                        <el-option v-for="item in documentTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item
                    :rules="[{ required: true, message: '请输入姓名!' }]"
                    :label="formState.baseData.type == 1 ? $t('姓名') : $t('法人姓名')"
                    prop="baseData.corporateName"
                >
                    <TigInput width="100%" v-model="formState.baseData.corporateName"></TigInput>
                    <div v-if="formState.baseData.documentType == 2" class="extra">{{ $t('填写中文名（英文名），如“张三（Zhang San）”') }}</div>
                </el-form-item>
                <el-form-item :label="$t('证件号码')" prop="baseData.documentNumber">
                    <TigInput width="100%" v-model="formState.baseData.documentNumber"></TigInput>
                </el-form-item>
                <el-form-item :label="$t('出生日期')" prop="baseData.birthday">
                    <el-date-picker
                        v-model="formState.baseData.birthday"
                        :placeholder="$t('请选择出生日期')"
                        style="width: 100%"
                        type="date"
                        value-format="YYYY-MM-DD"
                    />
                </el-form-item>
                <el-form-item>
                    <template #label> {{ $t('证件有效期') }} </template>
                    <div class="one-item">
                        <div class="to-time">
                            <el-radio-group v-model="formState.baseData.certificateValidityPeriod">
                                <el-radio :value="1">{{ $t('区间有效') }}</el-radio>
                            </el-radio-group>
                            <el-form-item label="" prop="baseData.certificateValidityPeriodDate">
                                <el-date-picker
                                    v-model="formState.baseData.certificateValidityPeriodDate"
                                    :clearable="false"
                                    :disabled="formState.baseData.certificateValidityPeriod === 2"
                                    end-placeholder="结束日期"
                                    range-separator="～"
                                    start-placeholder="开始日期"
                                    type="daterange"
                                    value-format="YYYY-MM-DD"
                                />
                            </el-form-item>
                        </div>
                        <div class="to-time">
                            <el-radio-group v-model="formState.baseData.certificateValidityPeriod">
                                <el-radio :value="2">{{ $t('长期有效') }}</el-radio>
                            </el-radio-group>
                            <el-form-item label="" prop="baseData.certificateValidityPeriodEnd">
                                <el-date-picker
                                    v-model="formState.baseData.certificateValidityPeriodEnd"
                                    :clearable="false"
                                    :disabled="formState.baseData.certificateValidityPeriod === 1"
                                    :placeholder="$t('请选择日期')"
                                    style="width: 168px"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                />
                                <span class="ml10">{{ $t('～ 长期') }}</span>
                            </el-form-item>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('居住地址')" prop="baseData.residentialAddress">
                    <TigInput
                        width="100%"
                        :autosize="{ minRows: 2, maxRows: 6 }"
                        v-model="formState.baseData.residentialAddress"
                        :placeholder="$t('请输入居住地址')"
                        type="textarea"
                    ></TigInput>
                </el-form-item>
                <el-form-item :label="$t('性别')" prop="sex">
                    <el-radio-group v-model="formState.baseData.sex">
                        <el-radio :value="1">{{ $t('男') }}</el-radio>
                        <el-radio :value="2">{{ $t('女') }}</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item :label="$t('证件照正面')" prop="baseData.frontOfPhotoTemp">
                    <FormAddGallery v-model:photo="formState.baseData.frontOfPhotoTemp"></FormAddGallery>
                    <div class="extra">
                        <p>{{ $t('如你的证件类型是身份证，请在此处上传人像面，若为复印件，需加盖公司鲜章。注意：证件盖章不支持电子章。') }}</p>
                        <p>{{ $t('护照需要增加翻译件，加盖公司鲜章，外籍商家姓名需为中文（英文），如张三（Zhangsan）。') }}</p>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('证件照反面')" prop="baseData.backOfPhotoTemp">
                    <FormAddGallery v-model:photo="formState.baseData.backOfPhotoTemp"></FormAddGallery>
                    <div class="extra">
                        <p>{{ $t('如你的证件类型是身份证，请在此处上传国徽面，若为复印件，需加盖公司鲜章。注意：证件盖章不支持电子章。') }}</p>
                        <p>{{ $t('护照需要增加翻译件，加盖公司鲜章，外籍商家姓名需为中文（英文），如张三（Zhangsan）。') }}</p>
                    </div>
                </el-form-item>
                <el-form-item :label="$t('补充信息')">
                    <FormAddGallery :isMultiple="true" v-model:photos="formState.baseData.supplementaryInformation"></FormAddGallery>
                    <div class="extra">
                        <p>{{ $t('在整个申请流程中，遇到需提供资料但无上传入口的情况，都可在此处上传。') }}</p>
                        <p>{{ $t('必须为彩色图片（文档请截图后上传）。') }}</p>
                    </div>
                </el-form-item>
            </el-tab-pane>
            <el-tab-pane :key="4" :label="$t('商户信息')" name="merchant">
                <el-form-item :label="$t('商户名称')" prop="merchantData.merchantName">
                    <TigInput width="100%" v-model="formState.merchantData.merchantName"></TigInput>
                    <div class="extra">{{ $t('该名称将在微信支付、支付宝支付时向买家展示') }}</div>
                </el-form-item>
                <el-form-item :label="$t('客服电话')" prop="merchantData.customerServicePhone">
                    <TigInput width="100%" v-model="formState.merchantData.customerServicePhone"></TigInput>
                    <div class="extra">{{ $t('请注意填写格式，举例（座机：0755-88880000；手机：13688880000；400电话：4008880000）') }}</div>
                </el-form-item>
                <el-form-item :label="$t('经营地址')" prop="merchantData.businessAddress">
                    <SelectRegion v-if="!loading" v-model:selectedIds="formState.merchantData.businessAddress"></SelectRegion>
                </el-form-item>
                <el-form-item :label="$t('详细地址')" prop="merchantData.detailedAddress">
                    <TigInput width="100%" v-model="formState.merchantData.detailedAddress" :placeholder="$t('请输入详细地址')" type="textarea"></TigInput>
                    <div class="extra">{{ $t('请填写详细的经营场所地址，如有多个场所，选择一个主要场所填写即可') }}</div>
                </el-form-item>
                <el-form-item v-if="formState.baseData.type == 2" :label="$t('营业执照电子版')" prop="merchantData.businessLicenseImgTemp">
                    <FormAddGallery
                        v-model:photo="formState.merchantData.businessLicenseImgTemp"
                        direct-upload
                        :direct-upload-request="uploadMerchantLicense"
                        :direct-upload-max-size="configStore.imageUploadMaxSize"
                        @uploading-change="licenseUploading = $event"
                    ></FormAddGallery>
                    <div class="extra">{{ $t('必须为彩色图片且小于{imageUploadMaxSize}M，若为复印件，需加盖公司红章。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                </el-form-item>
                <el-form-item :label="$t('联系人姓名')" prop="merchantData.contactName">
                    <TigInput width="100%" v-model="formState.merchantData.contactName"></TigInput>
                    <div class="extra">{{ $t('请填写联系人姓名') }}</div>
                </el-form-item>
                <el-form-item :label="$t('联系人手机')" prop="merchantData.contactPhone">
                    <TigInput width="100%" v-model="formState.merchantData.contactPhone" type="integer"></TigInput>
                    <div class="extra">{{ $t('非常重要，此手机号将作为后续登录商户系统的账号') }}</div>
                </el-form-item>
                <el-form-item :label="$t('联系人邮箱')" prop="merchantData.contactEmail">
                    <TigInput width="100%" v-model="formState.merchantData.contactEmail"></TigInput>
                </el-form-item>
                <el-form-item :label="$t('开户银行')" prop="merchantData.bankDeposit">
                    <TigInput width="100%" v-model="formState.merchantData.bankDeposit"></TigInput>
                </el-form-item>
                <el-form-item :label="$t('支行名称')" prop="merchantData.bankBranch">
                    <TigInput width="100%" v-model="formState.merchantData.bankBranch"></TigInput>
                </el-form-item>
                <el-form-item :label="formState.baseData.type == 1 ? $t('银行卡号') : $t('对公银行账户')" prop="merchantData.bankCard">
                    <TigInput width="100%" v-model="formState.merchantData.bankCard"></TigInput>
                </el-form-item>
                <el-form-item :label="$t('开户许可证')" prop="merchantData.accountOpeningPermitTemp">
                    <FormAddGallery v-model:photo="formState.merchantData.accountOpeningPermitTemp"></FormAddGallery>
                    <div class="extra"> {{ $t('开户许可证/基本存款账户任选其一上传，信息需与填写的结算账户信息一致，如无法提供，上传银行对公账户开户业务办理回执，或网银截图，或企业盖章的对公账户信息文件即可') }} </div>
                </el-form-item>
            </el-tab-pane>
            <el-tab-pane v-if="formState.baseData.type == 2" :key="2" :label="$t('企业信息')" name="company">
                <el-form-item :rules="[{ required: true, message: '请输入企业名称!' }]" class="form-item" :label="$t('企业名称')" prop="baseData.companyName">
                    <TigInput width="100%" v-model="formState.baseData.companyName"></TigInput>
                </el-form-item>
                <el-form-item class="form-item" :label="$t('注册地址')" prop="baseData.licenseAddrProvince">
                    <SelectRegion v-if="!loading" v-model:selectedIds="formState.baseData.licenseAddrProvince"></SelectRegion>
                </el-form-item>
                <el-form-item class="form-item" :label="$t('详细地址')" prop="baseData.businessLicenseAddress">
                    <TigInput
                        width="100%"
                        :autosize="{ minRows: 2, maxRows: 6 }"
                        v-model="formState.baseData.businessLicenseAddress"
                        type="textarea"
                    ></TigInput>
                    <div class="extra">{{ $t('注册地址需与营业执照登记住所一致') }}</div>
                </el-form-item>
                <el-form-item class="form-item" :label="$t('经营范围')" prop="baseData.businessScope">
                    <TigInput width="100%" :autosize="{ minRows: 2, maxRows: 6 }" v-model="formState.baseData.businessScope" type="textarea"></TigInput>
                    <div class="extra">{{ $t('与企业工商营业执照上一致') }}</div>
                </el-form-item>
                <el-form-item class="form-item" :label="$t('统一社会信用代码')" prop="baseData.businessLicenseId">
                    <TigInput width="100%" v-model="formState.baseData.businessLicenseId"></TigInput>
                    <div class="extra">{{ $t('请输入营业执照上18位统一社会信用代码') }}</div>
                </el-form-item>
                <el-form-item class="form-item">
                    <template #label> {{ $t('营业期限') }} </template>
                    <div class="one-item">
                        <div class="to-time">
                            <el-radio-group v-model="formState.baseData.operatingTermType">
                                <el-radio :value="1">{{ $t('区间有效') }}</el-radio>
                            </el-radio-group>
                            <el-form-item label="" prop="baseData.operatingTermTypeDate">
                                <el-date-picker
                                    v-model="formState.baseData.operatingTermTypeDate"
                                    :clearable="false"
                                    :disabled="formState.baseData.operatingTermType === 2"
                                    end-placeholder="结束日期"
                                    range-separator="～"
                                    start-placeholder="开始日期"
                                    type="daterange"
                                    value-format="YYYY-MM-DD"
                                />
                            </el-form-item>
                        </div>
                        <div class="to-time">
                            <el-radio-group v-model="formState.baseData.operatingTermType">
                                <el-radio :value="2">{{ $t('长期有效') }}</el-radio>
                            </el-radio-group>
                            <el-form-item label="" prop="baseData.operatingTermTypeEnd">
                                <el-date-picker
                                    v-model="formState.baseData.operatingTermTypeEnd"
                                    :clearable="false"
                                    :disabled="formState.baseData.operatingTermType === 1"
                                    :placeholder="$t('请选择日期')"
                                    style="width: 168px"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                />
                                <span class="ml10">{{ $t('～ 长期') }}</span>
                            </el-form-item>
                        </div>
                    </div>
                </el-form-item>
            </el-tab-pane>
        </el-tabs>
        <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
        </el-form-item>
    </el-form>
    <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
</template>
<script setup lang="ts">
import { ref, shallowRef, watch, onMounted } from "vue";
import { RadioType } from "@/components/radio";

import { MerchantFormState } from "@/types/adminMerchant/merchant";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { SelectRegion } from "@/components/select";
import { updateMerchant, getMerchant, uploadMerchantLicense } from "@/api/adminMerchant/merchant";
import { useRouter } from "vue-router";
import { getAdminUserList } from "@/api/authority/adminUser";
import { getUserList } from "@/api/user/user";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const activeKey = ref<string>("base");
const onTabChange = (val: string) => {
    activeKey.value = val;
};
const loading = ref<boolean>(false);

const formState = ref<MerchantFormState>({
    admin: {
        userId: null,
        adminId: null,
        type: 1
    },
    baseData: {
        type: 1,
        certificateValidityPeriod: 1,
        operatingTermType: 1,
        frontOfPhoto: [],
        backOfPhoto: [],
        licenseAddrProvince: [],
        supplementaryInformation: []
    },
    merchantData: {
        businessAddress: [],
        accountOpeningPermit: [],
        businessLicenseImg: []
    }
});
const loadingVal = ref(false);
const licenseUploading = ref(false);
const adminType = ref(true);
const userInfo = ref<any>([]);
const remoteMethod = async (query: string) => {
    try {
        if (query) {
            loadingVal.value = true;
            const params = { keyword: query };
            // 使用公共方法来获取数据
            const result = await getUserFetch(params);
            // 设置用户信息
            result.records.forEach((item: any) => {
                if (adminType.value) {
                    item.value = item.adminId;
                    item.key = item.adminId;
                } else {
                    item.value = item.userId;
                    item.key = item.userId;
                }
                item.label = item.username;
            });
            userInfo.value = result.records;
        } else {
            userInfo.value = [];
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loadingVal.value = false;
    }
};
const getUserFetch = async (params: { keyword: string }) => {
    if (adminType.value) {
        return await getAdminUserList(params);
    } else {
        return await getUserList(params);
    }
};
//证件类型
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
const formStateRef = shallowRef();
//日期选择校验重置
watch(
    () => adminType.value,
    (val) => {
        if (val) {
            formStateRef.value?.clearValidate("admin.userId");
            formState.value.admin.userId = null;
        } else {
            formStateRef.value?.clearValidate("admin.adminId");
            formState.value.admin.adminId = null;
            console.log(formState.value.admin);
        }
    },
    { deep: true }
);
watch(
    () => formState.value.baseData.certificateValidityPeriod,
    (val) => {
        if (val == 1) {
            formStateRef.value?.clearValidate("baseData.certificateValidityPeriodEnd");
        } else {
            formStateRef.value?.clearValidate("baseData.certificateValidityPeriodDate");
        }
    },
    { deep: true }
);
watch(
    () => formState.value.baseData.operatingTermType,
    (val) => {
        if (val == 1) {
            formStateRef.value?.clearValidate("baseData.operatingTermTypeEnd");
        } else {
            formStateRef.value?.clearValidate("baseData.operatingTermTypeDate");
        }
    },
    { deep: true }
);
//身份证自动填充
watch(
    () => formState.value.baseData.documentNumber,
    (val) => {
        if (formState.value.baseData.documentType == 1 && val && val.length >= 18) {
            let temp = val.substr(6, 8);
            formState.value.baseData.birthday = temp.slice(0, 4) + "-" + temp.slice(4, 6) + "-" + temp.slice(6, 8);
            let genderIndicator = parseInt(val.substr(val.length - 2, 1), 10);
            formState.value.baseData.sex = genderIndicator % 2 === 0 ? 2 : 1;
        }
    },
    { deep: true }
);
watch(
    () => formState.value.baseData.type,
    (val) => {
        if (activeKey.value == "company") {
            if (val == 1) {
                activeKey.value = "base";
            }
        }
    }
);
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
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const fetchMerchant = async () => {
    try {
        const result = await getMerchant(action.value, { id: id.value });
        formState.value = result;
        if (formState.value.baseData.frontOfPhoto && formState.value.baseData.frontOfPhoto.length > 0) {
            formState.value.baseData.frontOfPhotoTemp = formState.value.baseData.frontOfPhoto[0].picUrl;
        }
        if (formState.value.baseData.backOfPhoto && formState.value.baseData.backOfPhoto.length > 0) {
            formState.value.baseData.backOfPhotoTemp = formState.value.baseData.backOfPhoto[0].picUrl;
        }
        if (formState.value.merchantData.businessLicenseImg && formState.value.merchantData.businessLicenseImg.length > 0) {
            formState.value.merchantData.businessLicenseImgTemp = formState.value.merchantData.businessLicenseImg[0].picUrl;
        }
        if (formState.value.merchantData.accountOpeningPermit && formState.value.merchantData.accountOpeningPermit.length > 0) {
            formState.value.merchantData.accountOpeningPermitTemp = formState.value.merchantData.accountOpeningPermit[0].picUrl;
        }
        if (formState.value.merchantData.businessAddress) {
            formState.value.merchantData.businessAddress = normalizeRegionIds(formState.value.merchantData.businessAddress);
        }
        if (formState.value.baseData.licenseAddrProvince) {
            formState.value.baseData.licenseAddrProvince = normalizeRegionIds(formState.value.baseData.licenseAddrProvince);
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const normalizeRegionIds = (ids: any): (number|string)[] => {
    let result: (number | string)[] = [];
    
    if (ids && typeof ids === 'object' && !Array.isArray(ids)) {
        result = Object.values(ids);
    } else if (Array.isArray(ids)) {
        result = ids;
    } else if (ids !== undefined && ids !== null) {
        result = [ids];
    }
    return result.map(id => {
        const num = Number(id);
        return isNaN(num) ? id : num;
    });
};
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchMerchant();
    } else {
        adminType.value = true;
        loading.value = false;
    }
});
// 表单通过验证后提交
const onSubmit = async () => {
    if (licenseUploading.value) {
        message.warning(t("正在上传..."));
        return;
    }
    await formStateRef.value.validate();
    try {
        emit("update:confirmLoading", true);

        formState.value.baseData.frontOfPhoto = [{ picUrl: formState.value.baseData.frontOfPhotoTemp }];
        formState.value.baseData.backOfPhoto = [{ picUrl: formState.value.baseData.backOfPhotoTemp }];
        const licenseUrl = formState.value.merchantData.businessLicenseImgTemp;
        formState.value.merchantData.businessLicenseImg = licenseUrl
            ? [{ picUrl: licenseUrl, picThumb: licenseUrl, picName: "营业执照", url: licenseUrl }]
            : [];
        formState.value.merchantData.accountOpeningPermit = [{ picUrl: formState.value.merchantData.accountOpeningPermitTemp }];
        let tempData: any = {
            ...formState.value,
            shopTitle: formState.value.merchantData.merchantName,
            type: formState.value.baseData.type
        };
        if (action.value == "add") {
            tempData.admin = {
                type: adminType.value ? 2 : 1,
                userId: formState.value.admin.userId,
                adminId: formState.value.admin.adminId
            };
        }
        //处理图片
        const result = await updateMerchant(operation, { ...tempData });
        emit("submitCallback", result);
        message.success(t('添加成功'));
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
.step-section-item {
    display: flex;
    margin-bottom: 24px;

    .step-section-item__label {
        margin-right: 16px;
        min-width: 64px;
        font-size: 14px;
        line-height: 40px;
        color: #646566;
        text-align: right;
    }

    .zent-form-control {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .cert-type-desc-principal {
            padding: 16px;
            font-size: 14px;
            background-color: #f7f8fa;
            color: #646566;
            line-height: 20px;
            box-sizing: border-box;
        }
    }

    .zent-grid-table {
        border-collapse: collapse;

        colgroup {
            display: table-column-group;
            unicode-bidi: isolate;

            col {
                display: table-column;
                unicode-bidi: isolate;
            }
        }

        .zent-grid-thead {
            .zent-grid-tr {
                background-color: #f7f7f7;
                display: table-row;
                height: 56px;
            }
        }

        tr {
            border: 1px solid #ebedf0;

            th {
                border: 1px solid #ebedf0;
                padding: 18px 16px;
            }

            td {
                border: 1px solid #ebedf0;
                padding: 18px 16px;
                font-size: 14px;
                line-height: 1.8;
            }
        }

        .intro-column-title {
            font-size: 14px;
            line-height: 22px;
            color: #39393a;
            font-weight: bold;

            .sub-title {
                font-size: 14px;
                line-height: 20px;
                color: #9b9c9d;
                font-weight: normal;
            }
        }
    }
}

.one-item {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .to-time {
        display: flex;
        align-items: center;
        gap: 20px;
    }
}

.required {
    color: var(--el-color-danger);
    margin-right: 4px;
}

.font-color {
    color: var(--tig-primary);
    cursor: pointer;
}
</style>
