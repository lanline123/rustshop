<template>
    <view>
        <template v-if="!props.id || !loadStatus">
            <steps :current-step="currentStep - 1" :step-list="stepList" />
            <template v-if="currentStep == 1 || currentStep == 2">
                <view class="form-box">
                    <up-form ref="firstFormRef" label-position="left" :model="firstForm" :rules="firstFormRules" label-width="100" error-type="toast">
                        <template v-if="currentStep == 1">
                            <up-form-item :label="$t('主体类型') + '：'">
                                <view class="type-text">{{ $t("企业") }}</view>
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('企业名称') + '：'" prop="companyName">
                                <up-input v-model="firstForm.companyName" input-align="right" :placeholder="$t('请输入企业名称')" border="none" />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('注册地址') + '：'" prop="licenseAddrProvince" @click="handleShowRegisteredAddress">
                                <up-input
                                    v-model="registeredAddress"
                                    input-align="right"
                                    disabled
                                    disabled-color="#ffffff"
                                    :placeholder="$t('请选择注册地址')"
                                    border="none"
                                />
                                <template #right>
                                    <up-icon name="arrow-right" />
                                </template>
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('详细地址') + '：'" prop="businessLicenseAddress">
                                <up-input v-model="firstForm.businessLicenseAddress" input-align="right" :placeholder="$t('请输入详细地址')" border="none" />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('经营范围') + '：'" prop="businessScope" class="textarea">
                                <up-textarea
                                    v-model="firstForm.businessScope"
                                    input-align="right"
                                    height="48"
                                    :placeholder="$t('请输入经营范围')"
                                    border="none"
                                />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('社会信用代码') + '：'" prop="businessLicenseId">
                                <up-input v-model="firstForm.businessLicenseId" input-align="right" :placeholder="$t('请输入统一社会信用代码')" border="none" />
                            </up-form-item>
                            <up-form-item :required="true" class="special-item" label-position="top" prop="operatingTermType" :label="$t('营业期限') + '：'">
                                <view>
                                    <view class="certificate-validity-item">
                                        <up-radio-group v-model="firstForm.operatingTermType" placement="column" @change="getValidity">
                                            <up-radio active-color="var(--general)" shape="circle" :name="1" :label="$t('区间有效')" />
                                        </up-radio-group>
                                        <view class="validity-item-text">
                                            <datetimeItem
                                                :type="2"
                                                :model-value="firstForm.operatingTermTypeDate"
                                                :disabled="firstForm.operatingTermType == 2"
                                                @click="!(firstForm.operatingTermType == 2) && handleShowSelectDate('operatingTermTypeDate')"
                                            />
                                        </view>
                                    </view>
                                    <view class="certificate-validity-item">
                                        <up-radio-group v-model="firstForm.operatingTermType" placement="column" @change="getValidity">
                                            <up-radio active-color="var(--general)" shape="circle" :name="2" :label="$t('长期有效')" />
                                        </up-radio-group>
                                        <view class="period_end-box">
                                            <datetimeItem
                                                :model-value="firstForm.operatingTermTypeEnd"
                                                :disabled="firstForm.operatingTermType == 1"
                                                @click="!(firstForm.operatingTermType == 1) && handleShowDatetime('operatingTermTypeEnd')"
                                            />
                                            <view>- {{ $t("长期") }}</view>
                                        </view>
                                    </view>
                                </view>
                            </up-form-item>
                        </template>

                        <template v-if="currentStep == 2">
                            <up-form-item :required="true" :label="$t('姓名') + '：'" prop="corporateName">
                                <up-input v-model="firstForm.corporateName" input-align="right" :placeholder="$t('请输入姓名')" border="none" />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('居住地址') + '：'" prop="residentialAddress">
                                <up-input v-model="firstForm.residentialAddress" input-align="right" :placeholder="$t('请输入居住地址')" border="none" />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('性别') + '：'" prop="sex">
                                <view class="sex-box">
                                    <up-radio-group v-model="firstForm.sex" style="width: 485rpx; justify-content: end" placement="row">
                                        <up-radio style="padding-right: 15rpx" active-color="var(--general)" shape="circle" :name="1" :label="$t('男')" />
                                        <up-radio active-color="var(--general)" shape="circle" :name="2" :label="$t('女')" />
                                    </up-radio-group>
                                </view>
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('出生日期') + '：'" prop="birthday">
                                <datetimeItem class="datetime-item-long" :model-value="firstForm.birthday" @click="handleShowDatetime('birthday')" />
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('证件类型') + '：'" prop="documentType" @click="showDocumentType = true">
                                <up-input
                                    v-model="documentTypeText"
                                    input-align="right"
                                    disabled
                                    disabled-color="#ffffff"
                                    :placeholder="$t('请选择证件类型')"
                                    border="none"
                                />
                                <template #right>
                                    <up-icon name="arrow-right" />
                                </template>
                            </up-form-item>
                            <up-form-item :required="true" :label="$t('证件号码') + '：'" prop="documentNumber">
                                <up-input v-model="firstForm.documentNumber" input-align="right" :placeholder="$t('请输入证件号码')" border="none" />
                            </up-form-item>

                            <up-form-item
                                :required="true"
                                class="special-item"
                                label-position="top"
                                prop="certificateValidityPeriod"
                                :label="$t('证件有效期') + '：'"
                            >
                                <view>
                                    <view class="certificate-validity-item">
                                        <up-radio-group v-model="firstForm.certificateValidityPeriod" placement="column" @change="getValidity">
                                            <up-radio active-color="var(--general)" shape="circle" :name="1" :label="$t('区间有效')" />
                                        </up-radio-group>
                                        <view class="validity-item-text">
                                            <datetimeItem
                                                class="datetime-item-long"
                                                :type="2"
                                                :model-value="firstForm.certificateValidityPeriodDate"
                                                :disabled="firstForm.certificateValidityPeriod == 2"
                                                @click="!(firstForm.certificateValidityPeriod == 2) && handleShowSelectDate('certificateValidityPeriodDate')"
                                            />
                                        </view>
                                    </view>

                                    <view class="certificate-validity-item">
                                        <up-radio-group v-model="firstForm.certificateValidityPeriod" placement="column" @change="getValidity">
                                            <up-radio active-color="var(--general)" shape="circle" :name="2" :label="$t('长期有效')" />
                                        </up-radio-group>
                                        <view class="period_end-box">
                                            <datetimeItem
                                                class="datetime-item-long"
                                                :model-value="firstForm.certificateValidityPeriodEnd"
                                                :disabled="firstForm.certificateValidityPeriod == 1"
                                                @click="!(firstForm.certificateValidityPeriod == 1) && handleShowDatetime('certificateValidityPeriodEnd')"
                                            />

                                            <view>- {{ $t("长期") }}</view>
                                        </view>
                                    </view>
                                </view>
                            </up-form-item>

                            <up-form-item :required="true" prop="frontOfPhoto" class="special-item" label-position="top" :label="$t('证件照正面') + '：'">
                                <view class="upload-box">
                                    <upload v-model="firstForm.frontOfPhoto" :is-value-array="true" />
                                </view>
                            </up-form-item>
                            <up-form-item :required="true" prop="backOfPhoto" class="special-item" label-position="top" :label="$t('证件照反面') + ':'">
                                <view class="upload-box">
                                    <upload v-model="firstForm.backOfPhoto" :is-value-array="true" />
                                </view>
                            </up-form-item>
                        </template>
                    </up-form>
                </view>
            </template>
            <template v-if="currentStep == 3">
                <up-form ref="secondFormRef" label-position="left" :model="secondForm" :rules="secondFormRules" label-width="100" error-type="toast">
                    <view class="form-box">
                        <view class="form-title">{{ $t("联系人信息") }}</view>

                        <up-form-item :required="true" :label="$t('联系人姓名') + '：'" prop="merchantData.contactName">
                            <up-input v-model="secondForm.merchantData.contactName" input-align="right" :placeholder="$t('请输入姓名')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('联系人手机') + '：'" prop="merchantData.contactPhone">
                            <up-input v-model="secondForm.merchantData.contactPhone" input-align="right" :placeholder="$t('请输入联系人手机')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('常用邮箱') + '：'" prop="merchantData.contactEmail">
                            <up-input v-model="secondForm.merchantData.contactEmail" input-align="right" :placeholder="$t('请输入常用邮箱')" border="none" />
                        </up-form-item>
                    </view>

                    <view class="form-box">
                        <view class="form-title">{{ $t("结算账户信息") }}</view>
                        <up-form-item :required="true" :label="$t('开户银行') + '：'" prop="merchantData.bankDeposit">
                            <up-input v-model="secondForm.merchantData.bankDeposit" input-align="right" :placeholder="$t('请输入开户银行')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('开户支行') + '：'" prop="merchantData.bankBranch">
                            <up-input v-model="secondForm.merchantData.bankBranch" input-align="right" :placeholder="$t('请输入开户支行')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('对公银行账户')" prop="merchantData.bankCard">
                            <up-input v-model="secondForm.merchantData.bankCard" input-align="right" :placeholder="$t('请输入对公银行账户')" border="none" />
                        </up-form-item>
                        <up-form-item prop="merchantData.accountOpeningPermit" class="special-item" label-position="top" :label="$t('开户许可证') + '：'">
                            <view class="upload-box">
                                <upload v-model="secondForm.merchantData.accountOpeningPermit" :limit="3" />
                            </view>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("商户信息") }}</view>
                        <up-form-item :required="true" :label="$t('商户名称') + '：'" prop="merchantData.merchantName">
                            <up-input v-model="secondForm.merchantData.merchantName" input-align="right" :placeholder="$t('请输入商户名称')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('客服电话') + '：'" prop="merchantData.customerServicePhone">
                            <up-input
                                v-model="secondForm.merchantData.customerServicePhone"
                                input-align="right"
                                :placeholder="$t('请输入客服电话')"
                                border="none"
                            />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('经营地址') + '：'" prop="merchantData.businessAddress" @click="handleShowSelectRegion">
                            <up-input
                                v-model="regionNames"
                                input-align="right"
                                disabled
                                disabled-color="#ffffff"
                                :placeholder="$t('请选择经营地址')"
                                border="none"
                            />
                            <template #right>
                                <up-icon name="arrow-right" />
                            </template>
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('详细地址') + '：'" prop="merchantData.detailedAddress">
                            <up-input v-model="secondForm.merchantData.detailedAddress" input-align="right" :placeholder="$t('请输入详细地址')" border="none" />
                        </up-form-item>
                        <up-form-item
                            :required="true"
                            prop="merchantData.businessLicenseImg"
                            class="special-item"
                            label-position="top"
                            :label="$t('营业执照') + '：'"
                        >
                            <view class="upload-box">
                                <upload
                                    v-model="secondForm.merchantData.businessLicenseImg"
                                    :is-value-array="true"
                                    api-url="merchant/merchant/uploadLicense"
                                    response-data-type="url"
                                    @uploading-change="licenseUploading = $event"
                                />
                            </view>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("门店信息") }}</view>
                        <up-form-item :required="true" :label="$t('门店名称') + '：'" prop="shopData.shopTitle">
                            <up-input v-model="secondForm.shopData.shopTitle" input-align="right" :placeholder="$t('请输入门店名称')" border="none" />
                        </up-form-item>
                        <up-form-item :required="true" :label="$t('联系电话') + '：'" prop="shopData.contactMobile">
                            <up-input v-model="secondForm.shopData.contactMobile" input-align="right" :placeholder="$t('请输入客服电话')" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('门店简介') + '：'" class="textarea">
                            <up-textarea
                                v-model="secondForm.shopData.description"
                                input-align="right"
                                height="48"
                                :placeholder="$t('请输入门店简介')"
                                border="none"
                            />
                        </up-form-item>
                        <up-form-item :required="true" prop="shopData.shopLogo" class="special-item" label-position="top" :label="$t('门店logo') + '：'">
                            <view class="upload-box">
                                <upload v-model="secondForm.shopData.shopLogo" :is-value-array="true" />
                            </view>
                        </up-form-item>
                        <up-form-item class="special-item supplement" label-position="top" :label="$t('补充信息') + '：'">
                            <view class="upload-box">
                                <upload v-model="firstForm.supplementaryInformation" :limit="3" />
                            </view>
                        </up-form-item>
                    </view>
                </up-form>
            </template>
            <template v-if="currentStep === 4">
                <view class="status-box">
                    <view class="status-text">
                        <template v-if="formData.status == 1">
                            <view> {{ $t("申请已受理") }}（{{ formData.addTime }}），{{ $t("将在1-3个工作日内完成审核。以下为本次申请内容") }}：</view>
                        </template>
                        <template v-if="formData.status == 20">
                            <view>
                                {{ $t("很遗憾") }}，{{ $t("您的申请审核") }}<text class="specaial-text">{{ $t("未通过") }}</text
                                >。</view
                            >
                            <div v-if="formData.auditRemark">{{ $t("备注") }}：{{ formData.auditRemark }}</div>
                            <view>{{ $t("重新审核") }} </view>
                        </template>
                    </view>
                </view>
                <up-form label-position="left" label-width="115">
                    <view class="form-box">
                        <view class="form-title">{{ $t("联系人信息") }}</view>
                        <up-form-item :label="$t('联系人姓名') + '：'">
                            <up-input v-model="formData.merchantData.contactName" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('手机号') + '：'">
                            <up-input v-model="formData.merchantData.contactPhone" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('常用邮箱') + '：'">
                            <up-input v-model="formData.merchantData.contactEmail" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("结算账户信息") }}</view>
                        <up-form-item :label="$t('对公账户') + '：'">
                            <up-input v-model="formData.baseData.companyName" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('开户银行') + '：'">
                            <up-input v-model="formData.merchantData.bankDeposit" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('开户支行') + '：'">
                            <up-input v-model="formData.merchantData.bankBranch" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('对公银行账户') + '：'">
                            <up-input v-model="formData.merchantData.bankCard" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item v-if="formData.merchantData.accountOpeningPermit.length > 0" :label="$t('开户许可证') + '：'" label-position="top">
                            <template v-for="(item, index) in formData.merchantData.accountOpeningPermit" :key="index">
                                <tig-image class="image-box" :src="item.picUrl" />
                            </template>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("商户信息") }}</view>
                        <up-form-item :label="$t('商户名称') + '：'">
                            <up-input v-model="formData.merchantData.merchantName" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('客服电话') + '：'">
                            <up-input
                                v-model="formData.merchantData.customerServicePhone"
                                input-align="right"
                                disabled
                                disabled-color="#ffffff"
                                border="none"
                            />
                        </up-form-item>
                        <up-form-item :label="$t('经营地址') + '：'">
                            <view class="input-text">
                                <view class="input-content">
                                    {{ formData.merchantData.businessAddressName + formData.merchantData.detailedAddress }}
                                </view>
                            </view>
                        </up-form-item>
                        <up-form-item v-if="formData.merchantData.businessLicenseImg.length > 0" :label="$t('营业执照') + '：'" label-position="top">
                            <template v-for="(item, index) in formData.merchantData.businessLicenseImg" :key="index">
                                <tig-image class="image-box" :src="item.picUrl" />
                            </template>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("门店信息") }}</view>
                        <up-form-item :label="$t('门店名称') + '：'">
                            <up-input v-model="formData.shopData.shopTitle" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('联系电话') + '：'">
                            <up-input v-model="formData.shopData.contactMobile" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('门店简介') + '：'">
                            <view class="input-text">
                                <view class="input-content">
                                    {{ formData.shopData.description }}
                                </view>
                            </view>
                        </up-form-item>
                        <up-form-item v-if="formData.shopData.shopLogo.length > 0" :label="$t('门店logo') + '：'" label-position="top">
                            <template v-for="item in formData.shopData.shopLogo">
                                <tig-image class="image-box" :src="item.picUrl" />
                            </template>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("企业信息") }}</view>
                        <up-form-item :label="$t('企业名称') + '：'">
                            <up-input v-model="formData.baseData.companyName" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('注册地址') + '：'">
                            <view class="input-text">
                                <view class="input-content">
                                    {{ formData.baseData.licenseAddrProvinceName }} {{ formData.baseData.businessLicenseAddress }}
                                </view>
                            </view>
                        </up-form-item>
                        <up-form-item :label="$t('经营范围') + '：'">
                            <view class="input-text">
                                <view class="input-content">
                                    {{ formData.baseData.businessScope }}
                                </view>
                            </view>
                        </up-form-item>
                        <up-form-item :label="$t('社会信用代码') + '：'">
                            <up-input v-model="formData.baseData.businessLicenseId" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('营业期限') + '：'">
                            <view class="type-text" style="font-size: 15px">
                                <template v-if="formData.baseData.operatingTermType == 1">
                                    {{ formData.baseData.operatingTermTypeDate[0] + " ～  " + formData.baseData.operatingTermTypeDate[1] }}
                                </template>
                                <template v-else> {{ formData.baseData.operatingTermTypeEnd }} ～{{ $t("长期") }} </template>
                            </view>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("主体信息") }}</view>
                        <up-form-item :label="$t('个人姓名') + '：'">
                            <up-input v-model="formData.baseData.corporateName" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('证件类型') + '：'">
                            <up-input v-model="documentTypeText" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('证件号码') + '：'">
                            <up-input v-model="formData.baseData.documentNumber" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('证件有效期') + '：'">
                            <up-input v-model="certificateValidityPeriodText" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('性别') + '：'">
                            <up-input v-model="sexText" input-align="right" disabled disabled-color="#ffffff" border="none" />
                        </up-form-item>
                        <up-form-item :label="$t('居住地址') + '：'">
                            <view class="input-text">
                                <view class="input-content">
                                    {{ formData.baseData.residentialAddress }}
                                </view>
                            </view>
                        </up-form-item>
                    </view>
                    <view class="form-box">
                        <view class="form-title">{{ $t("证件照") }}</view>
                        <up-form-item class="noflex" :label="$t('证件照正面') + '：'" label-position="top">
                            <image class="image-box" :src="staticResource('common/lock@3x.png')" />
                            <view style="padding-top: 10rpx; width: 100%">
                                <up-alert show-icon type="info" :description="$t('为了保护个人隐私信息不泄露，该照片不予展示')" />
                            </view>
                        </up-form-item>
                        <up-form-item class="noflex" :label="$t('证件照反面') + '：'" label-position="top">
                            <image class="image-box" :src="staticResource('common/lock@3x.png')" />
                            <view style="padding-top: 10rpx; width: 100%">
                                <up-alert show-icon type="info" :description="$t('为了保护个人隐私信息不泄露，该照片不予展示')" />
                            </view>
                        </up-form-item>
                        <up-form-item
                            class="supplement"
                            v-if="formData.baseData.supplementaryInformation.length > 0"
                            :label="$t('补充信息') + '：'"
                            label-position="top"
                        >
                            <template v-for="(item, index) in formData.baseData.supplementaryInformation" :key="index">
                                <tig-image class="image-box" :src="item.picUrl" preview />
                            </template>
                        </up-form-item>
                    </view>
                </up-form>
            </template>
            <template v-if="currentStep == 5">
                <success :initial-user-info="formData.initialUserInfo" />
            </template>

            <template v-if="currentStep < stepList?.length - 1">
                <tig-fixed-placeholder background-color="#fff">
                    <template v-if="currentStep == 1">
                        <view class="btn-box">
                            <tig-button :loading="buttonPending" :disabled="buttonPending" @click="handlBtn('next')">{{ $t("下一步") }} </tig-button>
                        </view>
                    </template>
                    <template v-else>
                        <view class="btn-box-tow">
                            <template v-if="currentStep > 1">
                                <tig-button plain style="width: 45%; font-size: 28rpx" :disabled="buttonPending" @click="handlBtn('pre')">
                                    {{ $t("上一步") }}
                                </tig-button>
                            </template>
                            <template v-if="currentStep > 1 && currentStep < stepList?.length - 2">
                                <tig-button style="width: 45%; font-size: 28rpx" :loading="buttonPending" :disabled="buttonPending" @click="handlBtn('next')">
                                    {{ $t("下一步") }}
                                </tig-button>
                            </template>
                            <template v-else>
                                <tig-button style="width: 45%; font-size: 28rpx" :loading="buttonPending" :disabled="buttonPending" @click="handlBtn('submit')">
                                    {{ $t("提交") }}
                                </tig-button>
                            </template>
                        </view>
                    </template>
                </tig-fixed-placeholder>
            </template>

            <template v-if="formData.status == 20 && !isReapply">
                <tig-fixed-placeholder background-color="#fff">
                    <view class="btn-box">
                        <tig-button @click="handlReapply">{{ $t("重新申请") }} </tig-button>
                    </view>
                </tig-fixed-placeholder>
            </template>
        </template>

        <up-datetime-picker
            ref="datetimePickerRef"
            :cancel-text="$t('取消')"
            :confirm-text="$t('确定')"
            :show="showDatetime"
            :model-value="Date.now()"
            mode="date"
            :formatter="formatter"
            format="YYYY-MM-DD"
            confirm-color="var(--general)"
            :min-date="1"
            @cancel="showDatetime = false"
            @confirm="getBirthday"
        />

        <selectDate v-model="showSelectDate" @confirm="handleSelectDateconfirm" />

        <!-- firstForm -->
        <up-action-sheet
            :round="true"
            :show="showDocumentType"
            :actions="documentType"
            :title="$t('证件类型')"
            @close="showDocumentType = false"
            @select="documentTypeSelect"
        />
        <!-- secondForm -->
        <selectRegion v-model:show="showSelectRegion" v-model="secondForm.merchantData.businessAddress" @send-region-names="getRegionNames" />
        <selectRegion v-model:show="showRegisteredAddress" v-model="firstForm.licenseAddrProvince" @send-region-names="getregisteredAddress" />
    </view>
</template>

<script setup lang="ts">
import { watch, reactive, ref } from "vue";
import selectRegion from "@/components/region/selectRegion.vue";
import upload from "@/components/upload/index.vue";
import steps from "./steps.vue";
import success from "./success.vue";
import datetimeItem from "@/pages/user/src/datetimeItem.vue";
import selectDate from "@/pages/user/src/selectDate.vue";
import { applyMerchant, getMerchantInfo } from "@/api/user/merchantEnter";
import type { ApplyDetailItem } from "@/types/user/merchantEnter";
import { isDemo, staticResource } from "@/utils";
import { useI18n } from "vue-i18n";
import { validateEnterpriseStep } from "./enterpriseFormNavigation";

const { t } = useI18n();

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    isReapply: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(["sendRefresh"]);

const stepList = ref([t("企业信息"), t("法定代表人"), t("经营信息"), t("审核及公示"), t("完成入驻")]);
const currentStep = ref(1);

const formatter = (type: string, value: string) => {
    if (type === "year") {
        return `${value}${t("年")}`;
    }
    if (type === "month") {
        return `${value}${t("月")}`;
    }
    if (type === "day") {
        return `${value}${t("日")}`;
    }
    return value;
};

const datetimePickerRef = ref();
const showDatetime = ref(false);
const drteTimeKey = ref("");
const handleShowDatetime = (key: string) => {
    drteTimeKey.value = key;
    showDatetime.value = true;
};
const getBirthday = (val?: any) => {
    if (val && val.value) {
        firstForm[drteTimeKey.value] = uni.$u.timeFormat(val.value, "yyyy-mm-dd");
    }
    showDatetime.value = false;
};

const showSelectDate = ref(false);
const handleShowSelectDate = (key: string) => {
    showSelectDate.value = true;
    drteTimeKey.value = key;
};
const handleSelectDateconfirm = (val: string[]) => {
    firstForm[drteTimeKey.value] = JSON.parse(JSON.stringify(val));
};

/*              firstForm              */
const firstFormRef = ref();
const firstFormRules = {
    certificateValidityPeriod: [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (firstForm.certificateValidityPeriodDate.length > 0 || firstForm.certificateValidityPeriodEnd) {
                    return true;
                }
                return false;
            },
            message: t("请选择证件有效期!"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],
    birthday: {
        required: true,
        message: t("请选择出生日期!"),
        trigger: ["blur", "change"]
    },
    frontOfPhoto: [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length > 0 && value[0].picUrl) {
                    return true;
                }
                return false;
            },
            message: t("请上传证件正面照"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],

    backOfPhoto: [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length > 0 && value[0].picUrl) {
                    return true;
                }
                return false;
            },
            message: t("请上传证件反面照"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],
    documentType: {
        validator: (rule: any, value: any, callback: any) => {
            return value > 0;
        },
        message: t("请选择证件类型"),
        // 触发器可以同时用blur和change
        trigger: ["change", "blur"]
    },
    corporateName: {
        required: true,
        message: t("请输入姓名!"),
        trigger: ["blur", "change"]
    },
    documentNumber: {
        required: true,
        message: t("请输入证件号码!"),
        trigger: ["blur", "change"]
    },
    residentialAddress: {
        required: true,
        message: t("请输入居住地址!"),
        trigger: ["blur", "change"]
    },
    companyName: {
        required: true,
        message: t("请输入企业名称!"),
        trigger: ["blur", "change"]
    },
    businessLicenseAddress: {
        required: true,
        message: t("请输入详细地址!"),
        trigger: ["blur", "change"]
    },
    licenseAddrProvince: [
        {
            required: true,
            message: t("请选择注册地址")
        },
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length === 0) {
                    return false;
                }
                return true;
            },
            message: t("请选择注册地址"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],
    businessScope: {
        required: true,
        message: t("请输入经营范围!"),
        trigger: ["blur", "change"]
    },
    businessLicenseId: {
        required: true,
        message: t("请输入统一社会信用代码!"),
        trigger: ["blur", "change"]
    },
    operatingTermType: [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (firstForm.operatingTermTypeDate.length > 0 || firstForm.operatingTermTypeEnd) {
                    return true;
                }
                return false;
            },
            message: t("请选择营业期限!"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ]
};
const firstForm = reactive<any>({
    type: 2,
    companyName: "",
    licenseAddrProvince: [],
    businessLicenseAddress: "",
    businessScope: "",
    businessLicenseId: "",
    operatingTermType: 1,
    operatingTermTypeDate: [] as string[],
    operatingTermTypeEnd: "",
    certificateValidityPeriod: 1,
    certificateValidityPeriodDate: [] as string[],
    certificateValidityPeriodEnd: "",
    frontOfPhoto: [],
    backOfPhoto: [],
    documentType: 0,
    corporateName: "",
    documentNumber: "",
    birthday: "",
    sex: 1,
    residentialAddress: "",
    supplementaryInformation: []
});
const documentType = [
    {
        value: 1,
        name: t("大陆身份证")
    },
    {
        value: 2,
        name: t("护照（限境外人士）")
    },
    {
        value: 3,
        name: t("来往内地通行证（中国香港居民）")
    },
    {
        value: 4,
        name: t("来往内地通行证（中国澳门居民）")
    },
    {
        value: 5,
        name: t("来往大陆通行证（中国台湾居民）")
    }
];
const showDocumentType = ref(false);
const documentTypeText = ref("");
const documentTypeSelect = (val: { value: number; name: string }) => {
    firstForm.documentType = val.value;
    documentTypeText.value = val.name;
};
const getValidity = (e: any) => {
    firstForm.certificateValidityPeriodDate = [new Date().toISOString().split("T")[0], new Date().toISOString().split("T")[0]];
    firstForm.certificateValidityPeriodEnd = new Date().toISOString().split("T")[0];
};
const showRegisteredAddress = ref(false);
const handleShowRegisteredAddress = () => {
    showRegisteredAddress.value = true;
};
const registeredAddress = ref("");
const getregisteredAddress = (val: string[]) => {
    registeredAddress.value = val.join(" ");
};

/*      secondForm           */
const secondFormRef = ref();
const secondFormRules = {
    "merchantData.contactName": {
        required: true,
        message: t("请输入姓名!"),
        trigger: ["blur", "change"]
    },
    "merchantData.contactPhone": {
        required: true,
        message: t("请输入手机号码!"),
        trigger: ["blur", "change"]
    },
    "merchantData.contactEmail": {
        required: true,
        message: t("请输入常用邮箱!"),
        trigger: ["blur", "change"]
    },
    "merchantData.merchantName": {
        required: true,
        message: t("请输入商户名称!"),
        trigger: ["blur", "change"]
    },
    "merchantData.customerServicePhone": {
        required: true,
        message: t("请输入客服电话!"),
        trigger: ["blur", "change"]
    },
    "merchantData.bankDeposit": {
        required: true,
        message: t("请输入开户银行!"),
        trigger: ["blur", "change"]
    },
    "merchantData.bankBranch": {
        required: true,
        message: t("请输入开户支行!"),
        trigger: ["blur", "change"]
    },
    "merchantData.bankCard": {
        required: true,
        message: t("请输入对公银行账号!"),
        trigger: ["blur", "change"]
    },
    "shopData.shopTitle": {
        required: true,
        message: t("请输入门店名称!"),
        trigger: ["blur", "change"]
    },
    "shopData.contactMobile": {
        required: true,
        message: t("请输入联系电话!"),
        trigger: ["blur", "change"]
    },
    "merchantData.businessAddress": [
        {
            required: true,
            message: t("请选择经营地址")
        },
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length === 0) {
                    return false;
                }
                return true;
            },
            message: t("请选择经营地址"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],
    "merchantData.detailedAddress": {
        required: true,
        message: t("请输入详细地址!"),
        trigger: ["blur", "change"]
    },
    "merchantData.businessLicenseImg": [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length > 0 && value[0].picUrl) {
                    return true;
                }
                return false;
            },
            message: t("请上传营业执照!"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ],
    "shopData.shopLogo": [
        {
            validator: (rule: any, value: any, callback: any) => {
                if (value.length > 0 && value[0].picUrl) {
                    return true;
                }
                return false;
            },
            message: t("请上传门店logo"),
            // 触发器可以同时用blur和change
            trigger: ["change", "blur"]
        }
    ]
};
const secondForm = reactive({
    merchantData: {
        contactName: "",
        contactPhone: "",
        contactEmail: "",
        bankDeposit: "",
        bankBranch: "",
        bankCard: "",
        merchantName: "",
        customerServicePhone: "",
        detailedAddress: "",
        businessAddress: [] as number[],
        businessLicenseImg: [],
        accountOpeningPermit: []
    },
    shopData: {
        shopTitle: "",
        customerServicePhone: "",
        detailedAddress: "",
        contactMobile: "",
        description: "",
        shopLogo: []
    }
});
const licenseUploading = ref(false);
const showSelectRegion = ref(false);
const handleShowSelectRegion = () => {
    showSelectRegion.value = true;
};
const regionNames = ref("");
const getRegionNames = (val: string[]) => {
    regionNames.value = val.join(" ");
};

const buttonPending = ref(false);
const handlBtn = async (val: string) => {
    if (buttonPending.value) return;

    const step = currentStep.value;
    if (val === "pre") {
        currentStep.value = Math.max(1, step - 1);
        return;
    }

    buttonPending.value = true;
    try {
        await validateEnterpriseStep(step, firstFormRef.value, secondFormRef.value);
        if (val === "next" && currentStep.value === step) {
            currentStep.value = step + 1;
        }
        if (val === "submit") {
            await handleSubmit();
        }
    } finally {
        buttonPending.value = false;
    }
};

const handleSubmit = async () => {
    if (licenseUploading.value) {
        uni.showToast({
            title: t("正在上传..."),
            icon: "none"
        });
        return;
    }
    if (isDemo()) {
        uni.showToast({
            title: t("权限不足"),
            icon: "none"
        });
        return;
    }
    try {
        const data = {
            shopTitle: secondForm.shopData.shopTitle,
            type: firstForm.type,
            merchantData: secondForm.merchantData,
            baseData: firstForm,
            shopData: secondForm.shopData
        };
        const result = await applyMerchant(data);
        uni.showToast({
            title: t("提交成功")
        });
        emit("sendRefresh", result.merchantApplyId);
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    }
};

const formData = reactive<ApplyDetailItem>({} as ApplyDetailItem);
const loadStatus = ref(true);
const sexText = ref("");
const certificateValidityPeriodText = ref("");

const getDetail = async (id: number) => {
    loadStatus.value = true;
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getMerchantInfo(id);
        Object.assign(formData, result);
        if (!props.isReapply) {
            currentStep.value = result.status === 10 ? stepList.value.length : stepList.value.length - 1;
        }
        sexText.value = formData.baseData.sex == 1 ? t("男") : t("女");
        documentTypeText.value = documentType.find((item) => item.value == formData.baseData.documentType)?.name!;
        certificateValidityPeriodText.value =
            result.baseData.certificateValidityPeriod == 1
                ? result.baseData.certificateValidityPeriodDate.join("-")
                : result.baseData.certificateValidityPeriodEnd + " ~ " + t("长期");
        if (props.isReapply) {
            regionNames.value = formData.merchantData.businessAddressName;
            registeredAddress.value = formData.baseData.licenseAddrProvinceName;
            formData.baseData.licenseAddrProvince = formData.baseData.licenseAddrProvince || [];
            Object.assign(firstForm, formData.baseData);
            Object.assign(secondForm.merchantData, formData.merchantData);
            Object.assign(secondForm.shopData, formData.shopData);
        }
        firstForm.type = 2;
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none"
        });
    } finally {
        loadStatus.value = false;
        uni.hideLoading();
    }
};
watch(
    () => props.id,
    (newVal) => {
        if (newVal > 0) {
            getDetail(newVal);
        }
    },
    {
        immediate: true
    }
);

const handlReapply = () => {
    uni.navigateTo({
        url: "/pages/user/merchantEnter/principalType?type=isReapply"
    });
};
</script>

<style>
.form-box .special-item :deep(.u-form-item__body) {
    padding-bottom: 5px;
}
.form-box .noflex :deep(.u-form-item__body__right__content__slot) {
    display: block;
}
</style>

<style lang="scss" scoped>
.supplement {
    :deep(.u-form-item__body__right__content) {
        overflow: auto;
    }
}
:deep(.uni-progress-bar) {
    display: none;
}
.textarea {
    :deep(.u-form-item__body) {
        align-items: start;
        .u-textarea {
            padding: 0;
        }
        .uni-textarea-placeholder {
            text-align: right;
        }
    }
}
.input-text {
    font-size: 15px;
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    justify-content: end;
    .input-content {
        width: 450rpx;
        word-wrap: break-word;
        text-align: right;
    }
}

.upload-box {
    width: 80%;
    display: flex;
    padding-top: 20rpx;
}
.btn-box {
    padding: 25rpx;
    font-size: 28rpx;
}

.btn-box-tow {
    padding: 25rpx 15rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    font-size: 28rpx;
}

.status-box {
    background: none repeat scroll 0 0 #fffdee;
    border: 1px solid #edd28b;
    padding: 20rpx;
    font-size: 28rpx;
    margin: 20rpx 20rpx;
    .status-text {
        padding-bottom: 15rpx;
        &:last-child {
            padding-bottom: 0;
        }
    }
}

.specaial-text {
    color: var(--general);
}

.form-box {
    background-color: #fff;
    border-radius: 20rpx;
    padding: 20rpx 30rpx;
    margin: 20rpx;
    .type-text {
        width: 100%;
        display: flex;
        justify-content: end;
    }

    .form-upload {
        width: 149rpx;
        height: 150rpx;
        background: #fafafa;
        border-radius: 20rpx;
        margin-top: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .certificate-validity-item {
        display: flex;
        height: 100rpx;
        align-items: center;
        padding-top: 10px;
        &:first-child {
            padding-top: 10px;
        }
    }

    .validity-item-text {
        display: flex;
        align-items: center;
        justify-content: end;
        width: 485rpx;
        padding-left: 20rpx;
    }
    .period_end-box {
        display: flex;
        align-items: center;
        width: 485rpx;
        padding-left: 20rpx;
        column-gap: 15rpx;
    }

    .form-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333333;
        display: flex;
        align-items: center;
        padding: 20rpx 0;
        &::before {
            content: "";
            display: inline-block;
            width: 6rpx;
            height: 30rpx;
            background: var(--general);
            border-radius: 3rpx;
            margin-right: 10rpx;
        }
    }
    .image-box {
        padding-top: 30rpx;
        height: 200rpx;
        width: 200rpx;
    }
}

.datetime-item-long {
    flex: 1;
}
</style>
