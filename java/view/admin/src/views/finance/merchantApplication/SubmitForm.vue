<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table merchant-application-form">
                <div class="application-layout">
                    <div class="form-main">
                <el-form ref="formRef" :model="formState" label-width="150px" require-asterisk-position="right">
                    <!-- 主体信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('主体信息') }}</div>

                        <el-form-item :label="$t('支付渠道')" prop="payChannel" :rules="[{ required: true, message: $t('请选择支付渠道') }]">
                            <div>
                                <el-select v-model="formState.payChannel" style="width: 300px">
                                </el-select>
                                <div class="extra">{{ $t('选择进件的支付分账渠道，需与平台「分账设置」中的线上分账渠道一致') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('主体类型')" prop="organizationType" :rules="[{ required: true, message: $t('请选择主体类型') }]">
                            <div>
                                <el-radio-group v-model="formState.organizationType">
                                    <el-radio value="2">{{ $t('企业') }}</el-radio>
                                    <el-radio value="4">{{ $t('个体工商户') }}</el-radio>
                            <el-radio value="2401">{{ $t('个人') }}</el-radio>
                                </el-radio-group>
                                <div class="extra">
                                    <span v-if="formState.organizationType === '4'">{{ $t('“个体工商户”在营业执照上的主体类型一般为：个体工商户。') }}</span>
                                    <span v-else-if="formState.organizationType === '2401'">{{ $t('“个人”为根据法律法规和相关规定免于办理工商登记，无营业执照的商家。') }}</span>
                                    <span v-else>{{ $t('“企业”在营业执照上的主体类型一般为：有限公司、有限责任公司等。') }}</span>
                                </div>
                            </div>
                        </el-form-item>

                        <el-form-item
                            v-if="!hideShopSelect"
                            :label="selectShopLabel"
                            prop="shopId"
                            :rules="[{ required: true, message: selectShopMessage }]"
                        >
                            <el-input v-if="isEdit" :model-value="shopDisplay" disabled style="width: 300px" />
                            <SelectShop
                                v-else
                                v-model:shopId="formState.shopId"
                                :type="isStore() ? 2 : 1"
                                :api-list="getShopList"
                                :extra-params="{ includeSelfOperated: 1 }"
                                :placeholder="shopNamePlaceholder"
                                style="width: 300px"
                                @onChange="onShopChange"
                            />
                        </el-form-item>

                        <el-form-item v-if="!isPersonal" :label="$t('营业执照号')" prop="businessLicenseNumber" :rules="[{ required: true, message: $t('请输入营业执照号') }]">
                            <TigInput v-model="formState.businessLicenseNumber" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="merchantNameLabel" prop="merchantName" :rules="[{ required: true, message: merchantNameMessage }]">
                            <TigInput v-model="formState.merchantName" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item v-if="!isPersonal" :label="$t('法人/经营者姓名')" prop="legalPerson" :rules="[{ required: true, message: $t('请输入法人/经营者姓名') }]">
                            <TigInput v-model="formState.legalPerson" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item v-if="!isPersonal" :label="$t('营业执照有效期开始')" prop="licensePeriodBegin" >
                            <el-date-picker v-model="formState.licensePeriodBegin" type="date" value-format="YYYY-MM-DD" :placeholder="$t('请选择日期')" style="width: 300px" />
                        </el-form-item>

                        <el-form-item v-if="!isPersonal" :label="$t('营业执照有效期结束')" prop="licensePeriodEnd" >
                            <div class="flex flex-align-center" style="gap: 12px">
                                <el-date-picker
                                    v-model="formState.licensePeriodEnd"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                    :placeholder="$t('请选择日期')"
                                    :disabled="licensePeriodLongTerm"
                                    style="width: 300px"
                                />
                                <el-checkbox v-model="licensePeriodLongTerm" @change="onLicenseLongTermChange">{{ $t('长期') }}</el-checkbox>
                            </div>
                        </el-form-item>

                        <el-form-item v-if="!isPersonal" :label="$t('营业执照照片')" prop="licenseCopy" :rules="[{ required: true, message: $t('请上传营业执照照片') }]">
                            <div>
                                <FormAddGallery v-model:photo="formState.licenseCopy" :isMultiple="false" />
                                <div class="extra">{{ $t('必须为彩色图片且小于{imageUploadMaxSize}M，若为复印件，需加盖公司红章。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 法人/经营者身份信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('法人/经营者身份信息') }}</div>

                        <el-form-item :label="$t('身份证姓名')" prop="idCardName" :rules="[{ required: true, message: $t('请输入身份证姓名') }]">
                            <div class="flex flex-align-center" style="gap: 12px">
                                <TigInput v-model="formState.idCardName" :placeholder="$t('请输入')" width="300px" />
                                <a v-if="!isPersonal" class="btn-link" @click="syncLegalPersonName">{{ $t('同步法人姓名') }}</a>
                            </div>
                        </el-form-item>

                        <el-form-item :label="$t('身份证号码')" prop="idCardNumber" :rules="[{ required: true, message: $t('请输入身份证号码') }]">
                            <TigInput v-model="formState.idCardNumber" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('身份证有效期开始')" prop="cardPeriodBegin" :rules="[{ required: true, message: $t('请选择日期') }]">
                            <el-date-picker v-model="formState.cardPeriodBegin" type="date" value-format="YYYY-MM-DD" :placeholder="$t('请选择日期')" style="width: 300px" />
                        </el-form-item>

                        <el-form-item :label="$t('身份证有效期结束')" prop="cardPeriodEnd" :rules="[{ required: !cardPeriodLongTerm, message: $t('请选择日期') }]">
                            <div class="flex flex-align-center" style="gap: 12px">
                                <el-date-picker
                                    v-model="formState.cardPeriodEnd"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                    :placeholder="$t('请选择日期')"
                                    :disabled="cardPeriodLongTerm"
                                    style="width: 300px"
                                />
                                <el-checkbox v-model="cardPeriodLongTerm" @change="onCardLongTermChange">{{ $t('长期') }}</el-checkbox>
                            </div>
                        </el-form-item>

                        <el-form-item :label="$t('身份证人像面')" prop="idCardCopy" :rules="[{ required: true, message: $t('请上传身份证人像面') }]">
                            <div>
                                <FormAddGallery v-model:photo="formState.idCardCopy" :isMultiple="false" />
                                <div class="extra">{{ $t('请上传身份证人像面，若为复印件，需加盖公司鲜章。') }}</div>
                            </div>
                        </el-form-item>

                        <el-form-item :label="$t('身份证国徽面')" prop="idCardNational" :rules="idCardNationalRules">
                            <div>
                                <FormAddGallery v-model:photo="formState.idCardNational" :isMultiple="false" />
                                <div class="extra">{{ $t('请上传身份证国徽面，若为复印件，需加盖公司鲜章。') }}</div>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 经营信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('经营信息') }}</div>

                        <el-form-item :label="$t('商户简称')" prop="merchantShortName" :rules="[{ required: true, message: $t('请输入商户简称') }]">
                            <TigInput v-model="formState.merchantShortName" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('客服电话')" prop="servicePhone">
                            <div>
                                <TigInput v-model="formState.servicePhone" :placeholder="$t('请输入')" width="300px" />
                                <div class="extra">{{ $t('注：该字段仅做内部记录，不会提交到微信支付') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="shopUrlLabel" prop="storeUrl" :rules="storeUrlRules">
                            <div class="flex flex-align-center" style="gap: 12px">
                                <TigInput v-model="formState.storeUrl" width="300px" />
                                <el-button type="primary" link @click="onAutoGetShopUrl">{{ $t('自动获取') }}</el-button>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 结算信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('结算信息') }}</div>

                        <el-form-item :label="$t('结算规则ID')" prop="settlementId">
                            <TigInput v-model="formState.settlementId" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('所属行业')" prop="qualificationType">
                            <TigInput v-model="formState.qualificationType" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('银行账户类型')" prop="bankAccountType" :rules="[{ required: true, message: $t('请选择银行账户类型') }]">
                            <div>
                                <el-radio-group v-model="formState.bankAccountType">
                                    <el-radio value="74" :disabled="isPersonal">{{ $t('对公') }}</el-radio>
                                    <el-radio value="75" :disabled="isEnterprise">{{ $t('对私') }}</el-radio>
                                </el-radio-group>
                                <div v-if="isEnterprise" class="extra">{{ $t('企业主体仅支持对公账户') }}</div>
                                <div v-else-if="isPersonal" class="extra">{{ $t('个人主体仅支持对私账户') }}</div>
                            </div>
                        </el-form-item>

                        <el-form-item :label="bankAccountNameLabel" prop="bankAccountName" :rules="[{ required: true, message: bankAccountNameMessage }]">
                                    <TigInput v-model="formState.bankAccountName" :placeholder="$t('请输入')" width="300px" :disabled="isPersonal" />
                        </el-form-item>

                        <el-form-item :label="$t('银行账号')" prop="bankAccountNumber" :rules="[{ required: true, message: $t('请输入银行账号') }]">
                            <TigInput v-model="formState.bankAccountNumber" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('开户银行')" prop="bankName" :rules="[{ required: true, message: $t('请输入开户银行') }]">
                            <TigInput v-model="formState.bankName" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('开户支行')" prop="bankBranchName">
                            <TigInput v-model="formState.bankBranchName" :placeholder="$t('选填')" width="300px" />
                        </el-form-item>
                    </div>

                    <!-- 超级管理员信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('超级管理员信息') }}</div>

                        <el-form-item :label="$t('超管类型')" prop="contactType" :rules="[{ required: true, message: $t('请选择超管类型') }]">
                            <el-radio-group v-model="formState.contactType">
                                <el-radio value="65">{{ $t('经营者/法人') }}</el-radio>
                            </el-radio-group>
                        </el-form-item>

                        <el-form-item :label="$t('联系人姓名')" prop="contactName" :rules="[{ required: true, message: $t('请输入联系人姓名') }]">
                            <TigInput v-model="formState.contactName" :placeholder="$t('请输入')" width="300px" :disabled="isPersonal" />
                        </el-form-item>

                        <el-form-item :label="$t('超管身份证号')" prop="contactIdNumber" :rules="[{ required: true, message: $t('请输入超管身份证号') }]">
                            <TigInput v-model="formState.contactIdNumber" :placeholder="$t('请输入')" width="300px" :disabled="isPersonal" />
                        </el-form-item>

                        <el-form-item :label="$t('联系人手机')" prop="contactPhone" :rules="[{ required: true, message: $t('请输入联系人手机') }]">
                            <TigInput v-model="formState.contactPhone" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>

                        <el-form-item :label="$t('联系人邮箱')" prop="contactEmail">
                            <TigInput v-model="formState.contactEmail" :placeholder="$t('请输入')" width="300px" />
                        </el-form-item>
                    </div>

                    <el-form-item v-show="!props.isDialog && !props.hideSubmitButton">
                        <el-button class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                    </div>
                    <aside class="example-aside">
                        <div v-if="!isPersonal" class="example-block">
                            <div class="example-title">{{ $t('营业执照上传范本') }}</div>
                            <div class="example-images">
                                <img :src="businessLicenseExample" :alt="$t('营业执照范本')" />
                                <img :src="businessLicenseCopyExample" :alt="$t('营业执照副本范本')" />
                            </div>
                            <div class="example-tips">
                                <p>{{ $t('通过审批的必要条件：') }}</p>
                                <p>{{ $t('1、复印件需加盖开店公司红章。') }}</p>
                                <p>{{ $t('2、需上传最新的变更后的营业执照。') }}</p>
                                <p>{{ $t('3、不能切边，需露出四周完整花纹。') }}</p>
                            </div>
                        </div>
                        <div class="example-block">
                            <div class="example-title">{{ $t('身份证上传范本') }}</div>
                            <div class="example-images">
                                <img :src="idCardFrontExample" :alt="$t('身份证人像面范本')" />
                                <img :src="idCardBackExample" :alt="$t('身份证国徽面范本')" />
                            </div>
                            <div class="example-tips">
                                <p>{{ $t('通过审批的必要条件：') }}</p>
                                <p>{{ $t('1、需上传清晰有效的证件。') }}</p>
                                <p>{{ $t('2、正反面均需上传，正反面不能切边。') }}</p>
                                <p>{{ $t('3、复印件需加盖开店公司的红章。') }}</p>
                            </div>
                        </div>
                    </aside>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef, computed, watch, onMounted } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { SelectShop } from "@/components/select";
import { isStore } from "@/utils/version";
import { getAdminType } from "@/utils/storage";
import { getShop, getShopList } from "@/api/shop/shop";
import { getMerchant } from "@/api/adminMerchant/merchant";
import { getShopInfo } from "@/api/authority/accountEditing";
import { vendorSetting } from "@/api/vendor/setting";
import { applyMerchantApplication, submitMerchantApplication, getMerchantApplicationDetail } from "@/api/finance/merchantApplication";
import type { MerchantApplicationItem, MerchantApplicationSubmitForm } from "@/types/finance/merchantApplication.d";
import {
    applyProfileToForm,
    mapMerchantProfileToApplication,
    mapVendorProfileToApplication,
    replaceProfileOnForm
} from "./mapProfileToApplication";
import {
    applyOrganizationConstraints,
    buildMerchantApplicationPayload,
    isDuplicateIdCardPhotos,
    isHttpStoreUrl,
    isPersonalOrg,
    syncPersonalDerivedFields
} from "./onboardingForm";
import businessLicenseExample from "@/assets/setting/business-liscence.jpg";
import businessLicenseCopyExample from "@/assets/setting/business-liscence-c.jpg";
import idCardFrontExample from "@/assets/setting/idcard-f.png";
import idCardBackExample from "@/assets/setting/idcard-r.jpg";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const selectShopLabel = isStore() ? t("选择门店") : t("选择店铺");
const selectShopMessage = isStore() ? t("请选择门店") : t("请选择店铺");
const shopNamePlaceholder = isStore() ? t("请输入门店名称") : t("请输入店铺名称");
const shopUrlLabel = isStore() ? t("门店Url") : t("店铺Url");
const configStore = useConfigStore();
const userStore = useUserStore();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    act: { type: String, default: "" },
    id: { type: [Number, String], default: "" },
    isDialog: Boolean,
    // 店铺/供应商端申请：隐藏"选择店铺"，主体由登录态推导
    hideShopSelect: Boolean,
    // 外层页面自带提交按钮时，隐藏表单内部的提交按钮，避免重复
    hideSubmitButton: Boolean
});

const formRef = shallowRef();
const licensePeriodLongTerm = ref(false);
const cardPeriodLongTerm = ref(false);

const isEdit = computed(() => props.act === "edit" && !!props.id);
const shopName = ref("");
const shopDisplay = computed(() => shopName.value || String(formState.value.shopId ?? ""));

const formState = ref<MerchantApplicationSubmitForm>({
    shopId: "",
    payChannel: "WECHAT",
    organizationType: "4",
    merchantName: "",
    merchantShortName: "",
    businessLicenseNumber: "",
    legalPerson: "",
    licensePeriodBegin: "",
    licensePeriodEnd: "",
    licenseCopy: "",
    idCardName: "",
    idCardNumber: "",
    idCardCopy: "",
    idCardNational: "",
    cardPeriodBegin: "",
    cardPeriodEnd: "",
    servicePhone: "",
    storeUrl: "",
    settlementId: "",
    qualificationType: "",
    bankAccountType: "74",
    bankAccountName: "",
    bankAccountNumber: "",
    bankName: "",
    bankBranchName: "",
    contactType: "65",
    contactName: "",
    contactIdNumber: "",
    contactPhone: "",
    contactEmail: ""
});
const shopUrlMessage = isStore() ? t("请输入门店Url") : t("请输入店铺Url");
const storeUrlInvalidMessage = t("店铺主页须为可访问的 HTTP/HTTPS 链接");
const duplicateIdCardMessage = t("身份证人像面与国徽面不可使用同一张图片");

const isEnterprise = computed(() => formState.value.organizationType === "2");
const isPersonal = computed(() => isPersonalOrg(formState.value.organizationType));
const isCorporateAccount = computed(() => formState.value.bankAccountType === "74");
const bankAccountNameLabel = computed(() => (isCorporateAccount.value ? t("对公账户名称") : t("银行账户名称")));
const bankAccountNameMessage = computed(() => (isCorporateAccount.value ? t("请输入对公账户名称") : t("请输入银行账户名称")));
const merchantNameLabel = computed(() =>
    isPersonal.value ? (isStore() ? t("门店全称") : t("店铺全称")) : t("商户名称")
);
const merchantNameMessage = computed(() =>
    isPersonal.value ? (isStore() ? t("请输入门店全称") : t("请输入店铺全称")) : t("请输入商户名称")
);
const storeUrlRules = computed(() => [
    { required: true, message: shopUrlMessage },
    {
        validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
            if (!value || isHttpStoreUrl(value)) callback();
            else callback(new Error(storeUrlInvalidMessage));
        }
    }
]);
const idCardNationalRules = computed(() => [
    { required: true, message: t("请上传身份证国徽面") },
    {
        validator: (_rule: unknown, _value: string, callback: (error?: Error) => void) => {
            if (isDuplicateIdCardPhotos(formState.value.idCardCopy, formState.value.idCardNational)) {
                callback(new Error(duplicateIdCardMessage));
            } else {
                callback();
            }
        }
    }
]);

watch(
    () => formState.value.organizationType,
    () => {
        applyOrganizationConstraints(formState.value);
        if (isPersonal.value) {
            licensePeriodLongTerm.value = false;
        }
    }
);
watch(
    () => [formState.value.idCardName, formState.value.idCardNumber, formState.value.organizationType],
    () => {
        syncPersonalDerivedFields(formState.value);
    }
);
watch(
    () => formState.value.idCardCopy,
    () => {
        if (formState.value.idCardNational) {
            formRef.value?.validateField?.("idCardNational");
        }
    }
);

const onLicenseLongTermChange = () => {
    formState.value.licensePeriodEnd = licensePeriodLongTerm.value ? t('长期') : "";
};
const onCardLongTermChange = () => {
    formState.value.cardPeriodEnd = cardPeriodLongTerm.value ? t('长期') : "";
};
const onShopChange = (shopId: number | string | undefined) => {
    const normalizedShopId = String(shopId ?? "").trim();
    if (!normalizedShopId) {
        formState.value.storeUrl = "";
        return;
    }
    formState.value.storeUrl = "";
    prefillFromSelectedShop(normalizedShopId);
};
const onAutoGetShopUrl = () => {
    const normalizedShopId = String(formState.value.shopId ?? "").trim();
    if (!normalizedShopId) {
        message.warning(isStore() ? t("请先选择门店") : t("请先选择店铺"));
        return;
    }

    const domain = String(configStore.get("pcDomain") || configStore.get("h5Domain") || "")
        .trim()
        .replace(/\/+$/, "");
    if (!domain) return;

    const origin = /^https?:\/\//i.test(domain) ? domain : `https://${domain}`;
    const path = isStore() ? `/store/?id=${encodeURIComponent(normalizedShopId)}` : `/shop/${encodeURIComponent(normalizedShopId)}`;
    formState.value.storeUrl = `${origin}${path}`;
};
const syncCurrentSubjectShop = () => {
    if (!props.hideShopSelect || formState.value.shopId) return;
    const currentShopId = localStorage.getItem("shopId") || userStore.userInfo.shopId || userStore.shopInfo?.shopId || userStore.vendorInfo?.shopId;
    const normalizedShopId = String(currentShopId ?? "").trim();
    if (normalizedShopId) {
        formState.value.shopId = normalizedShopId;
    }
};
watch(
    () => [userStore.userInfo.shopId, userStore.shopInfo?.shopId, userStore.vendorInfo?.shopId],
    () => syncCurrentSubjectShop()
);
const syncLegalPersonName = () => {
    if (formState.value.legalPerson) {
        formState.value.idCardName = formState.value.legalPerson;
    }
};

const firstShopContactPhone = (shop: Record<string, any> | undefined) => {
    const contacts = shop?.shopContactConfig;
    if (Array.isArray(contacts)) {
        const phone = contacts.find((item: any) => item?.values)?.values;
        if (phone) return String(phone);
    }
    return shop?.kefuPhone || shop?.shopData?.contactMobile || "";
};

const resolveMerchantId = async () => {
    const fromStore = Number(userStore.shopInfo?.merchantId);
    if (fromStore) return fromStore;
    try {
        const cached = JSON.parse(localStorage.getItem("shopInfo") || "{}");
        if (Number(cached?.merchantId)) return Number(cached.merchantId);
    } catch {
        // 缓存损坏时走接口
    }
    try {
        const detail = await getShopInfo();
        return Number(detail?.merchantId) || 0;
    } catch {
        return 0;
    }
};

const loadMerchantFormByShop = async (shopLike: Record<string, any> | undefined): Promise<MerchantApplicationSubmitForm> => {
    const longTermLabel = t("长期");
    const merchantId = Number(shopLike?.merchantId);
    let merchant: Record<string, any> = {};
    if (merchantId) {
        try {
            merchant = (await getMerchant("detail", { id: merchantId })) as Record<string, any>;
        } catch {
            merchant = {};
        }
    }
    if (!merchant?.baseData && shopLike?.merchant) {
        merchant = { ...shopLike.merchant, ...merchant };
    }
    if (!merchant?.merchantId && !merchant?.merchantData && !merchant?.baseData) return {};
    return mapMerchantProfileToApplication(
        {
            ...merchant,
            shopTitle: shopLike?.shopTitle || merchant.shopName,
            shopContactPhone: firstShopContactPhone(shopLike)
        },
        { longTermLabel }
    );
};

const loadSubjectProfileForm = async (): Promise<MerchantApplicationSubmitForm> => {
    const longTermLabel = t("长期");
    if (getAdminType() === "vendor") {
        try {
            const detail = (await vendorSetting()) as Record<string, any>;
            return mapVendorProfileToApplication(detail, { longTermLabel });
        } catch {
            return mapVendorProfileToApplication(userStore.vendorInfo, { longTermLabel });
        }
    }
    const shop = userStore.shopInfo || {};
    const merchantId = (await resolveMerchantId()) || shop.merchantId;
    return loadMerchantFormByShop({ ...shop, merchantId });
};

const syncLongTermFlags = () => {
    licensePeriodLongTerm.value = formState.value.licensePeriodEnd === t("长期");
    cardPeriodLongTerm.value = formState.value.cardPeriodEnd === t("长期");
};

const prefillFromSubjectProfile = async () => {
    if (!props.hideShopSelect) return;
    try {
        const profile = await loadSubjectProfileForm();
        applyProfileToForm(formState.value, profile, { overwrite: !isEdit.value });
        applyOrganizationConstraints(formState.value);
        syncLongTermFlags();
    } catch {
        // 商户资料缺失时保持空白表单，由用户手工填写
    }
};

let selectedShopPrefillSeq = 0;
const prefillFromSelectedShop = async (shopId: string) => {
    if (props.hideShopSelect || isEdit.value) return;
    const seq = ++selectedShopPrefillSeq;
    try {
        const shop = (await getShop("detail", { shopId })) as Record<string, any>;
        if (seq !== selectedShopPrefillSeq) return;
        const profile = await loadMerchantFormByShop(shop);
        if (seq !== selectedShopPrefillSeq) return;
        replaceProfileOnForm(formState.value, profile);
        applyOrganizationConstraints(formState.value);
        syncLongTermFlags();
    } catch {
        // 选中店铺无商户资料时保持可手工填写
    }
};

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        // 店铺/供应商端(hideShopSelect)：提交申请进入平台审核；平台端：直接提交微信进件
        const submitApi = props.hideShopSelect ? applyMerchantApplication : submitMerchantApplication;
        const result = await submitApi(buildMerchantApplicationPayload(formState.value));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
        throw error;
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    return onSubmit();
};

const SNAPSHOT_FIELDS: (keyof MerchantApplicationSubmitForm)[] = [
    "shopId",
    "payChannel",
    "organizationType",
    "merchantName",
    "merchantShortName",
    "businessLicenseNumber",
    "legalPerson",
    "licensePeriodBegin",
    "licensePeriodEnd",
    "licenseCopy",
    "idCardName",
    "idCardNumber",
    "idCardCopy",
    "idCardNational",
    "cardPeriodBegin",
    "cardPeriodEnd",
    "servicePhone",
    "storeUrl",
    "settlementId",
    "qualificationType",
    "bankAccountType",
    "bankAccountName",
    "bankAccountNumber",
    "bankName",
    "bankBranchName",
    "contactType",
    "contactName",
    "contactIdNumber",
    "contactPhone",
    "contactEmail"
];

// 优先取顶层字段，其次取快照；脱敏字段（含 * 掩码）不回填，强制重新录入
const pickField = (data: Record<string, any>, snapshot: Record<string, any> | undefined, key: string) => {
    let value = data[key];
    if (value === undefined || value === null || value === "") {
        value = snapshot ? snapshot[key] : undefined;
    }
    if (value === undefined || value === null) return undefined;
    const str = String(value);
    if (str.includes("*")) return undefined;
    return str;
};

const prefillFromDetail = async () => {
    if (!isEdit.value) return;
    try {
        emit("update:confirmLoading", true);
        const result = (await getMerchantApplicationDetail({ id: props.id })) as MerchantApplicationItem;
        shopName.value = result.shopName || "";
        let snapshot: any = result.requestSnapshot;
        if (typeof snapshot === "string") {
            try {
                snapshot = JSON.parse(snapshot);
            } catch {
                snapshot = undefined;
            }
        }
        SNAPSHOT_FIELDS.forEach((key) => {
            const val = pickField(result as Record<string, any>, snapshot as Record<string, any> | undefined, key as string);
            if (val !== undefined) {
                (formState.value as Record<string, any>)[key] = val;
            }
        });
        applyOrganizationConstraints(formState.value);
        syncLongTermFlags();
    } catch (error: any) {
        message.error(error.message || t('加载草稿数据失败'));
    } finally {
        emit("update:confirmLoading", false);
    }
};

onMounted(() => {
    prefillFromDetail()
        .then(() => syncCurrentSubjectShop())
        .then(() => prefillFromSubjectProfile());
});

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.merchant-application-form {
    .form-section {
        margin-top: 10px;
        &:first-child {
            margin-top: 0;
        }
        .section-title {
            display: flex;
            align-items: center;
            font-size: 14px;
            font-weight: 600;
            padding: 0;
            margin-bottom: 20px;
            .title-bar {
                width: 3px;
                height: 14px;
                background-color: var(--theme-color, #1456f0);
                margin-right: 5px;
            }
        }
    }
    .extra {
        margin-top: 4px;
        font-size: 12px;
        color: #6b7280;
    }
    .application-layout {
        display: flex;
        align-items: flex-start;
        gap: 120px;
        max-width: 1060px;
        margin: 0 auto;
    }
    .form-main {
        flex: 1 1 auto;
        min-width: 0;
        max-width: 640px;
    }
    .example-aside {
        flex: 0 0 300px;
        width: 300px;
        .example-block {
            margin-top: 40px;
            &:first-child {
                margin-top: 0;
            }
        }
        .example-title {
            text-align: center;
            font-size: 14px;
            color: #909399;
            margin-bottom: 16px;
        }
        .example-images {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 16px;
            img {
                display: block;
                width: 100%;
                max-width: 260px;
                border: 1px solid #e5e7eb;
                border-radius: 4px;
            }
        }
        .example-tips {
            margin-top: 16px;
            font-size: 12px;
            color: #6b7280;
            line-height: 1.9;
            p {
                margin: 0;
            }
        }
    }
}
</style>





