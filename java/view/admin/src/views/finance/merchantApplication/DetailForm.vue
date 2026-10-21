<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <div class="merchant-application-form">
                    <!-- 状态信息 -->
                    <div class="form-section">
                        <div class="section-title"><span class="title-bar"></span>{{ $t('状态信息') }}</div>
                        <el-form label-width="150px">
                            <el-form-item :label="$t('状态')">
                                <el-tag :type="getStatusTagType(detail.status)" disable-transitions>{{ detail.statusDesc || detail.status }}</el-tag>
                            </el-form-item>
                            <el-form-item :label="$t('支付渠道')">
                                <span>{{ formatPayChannel(detail.payChannel) }}</span>
                            </el-form-item>
                            <el-form-item :label="$t('进件编号')">
                                <span>{{ detail.businessCode || "-" }}</span>
                            </el-form-item>
                            <el-form-item :label="$t('申请单号')">
                                <span>{{ detail.applymentId || "-" }}</span>
                            </el-form-item>
                            <el-form-item v-if="detail.subMchId" :label="$t('子商户号')">
                                <span>{{ detail.subMchId }}</span>
                            </el-form-item>
                            <el-form-item v-if="detail.signUrl" :label="$t('签约链接')">
                                <a :href="detail.signUrl" target="_blank" rel="noopener" class="btn-link break-all">{{ detail.signUrl }}</a>
                                <a class="btn-link" style="margin-left: 8px" @click="copySignUrl">{{ $t('复制') }}</a>
                            </el-form-item>
                            <el-form-item v-if="detail.rejectReason && detail.status !== 'FINISH'" :label="detail.reasonLabel || $t('失败原因')">
                                <span class="reject-reason">{{ detail.rejectReason }}</span>
                            </el-form-item>
                        </el-form>
                    </div>

                    <el-form label-width="150px">
                        <!-- 主体信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('主体信息') }}</div>
                            <el-form-item :label="$t('主体类型')">
                                <span>{{ organizationTypeText }}</span>
                            </el-form-item>                            <el-form-item :label="shopTypeName">
                                <span>{{ detail.shopName || formState.shopId || "-" }}</span>
                            </el-form-item>
                            <template v-if="!isPersonal">
                            <el-form-item :label="$t('营业执照号')"><span>{{ formState.businessLicenseNumber || "-" }}</span></el-form-item>
                            </template>
                            <el-form-item :label="merchantNameLabel"><span>{{ formState.merchantName || "-" }}</span></el-form-item>
                            <template v-if="!isPersonal">
                            <el-form-item :label="$t('法人/经营者姓名')"><span>{{ formState.legalPerson || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('营业执照有效期开始')"><span>{{ formState.licensePeriodBegin || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('营业执照有效期结束')"><span>{{ formState.licensePeriodEnd || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('营业执照照片')">
                                <FormAddGallery v-model:photo="formState.licenseCopy" :isMultiple="false" disabled />
                            </el-form-item>
                            </template>
                        </div>

                        <!-- 法人/经营者身份信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('法人/经营者身份信息') }}</div>
                            <el-form-item :label="$t('身份证姓名')"><span>{{ formState.idCardName || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('身份证号码')"><span>{{ formState.idCardNumber || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('身份证有效期开始')"><span>{{ formState.cardPeriodBegin || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('身份证有效期结束')"><span>{{ formState.cardPeriodEnd || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('身份证人像面')">
                                <FormAddGallery v-model:photo="formState.idCardCopy" :isMultiple="false" disabled />
                            </el-form-item>
                            <el-form-item :label="$t('身份证国徽面')">
                                <FormAddGallery v-model:photo="formState.idCardNational" :isMultiple="false" disabled />
                            </el-form-item>
                        </div>

                        <!-- 经营信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('经营信息') }}</div>
                            <el-form-item :label="$t('商户简称')"><span>{{ formState.merchantShortName || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('客服电话')"><span>{{ formState.servicePhone || "-" }}</span></el-form-item>
                            <el-form-item :label="shopUrlLabel"><span>{{ formState.storeUrl || "-" }}</span></el-form-item>
                        </div>

                        <!-- 结算信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('结算信息') }}</div>
                            <el-form-item :label="$t('结算规则ID')"><span>{{ formState.settlementId || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('所属行业')"><span>{{ formState.qualificationType || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('银行账户类型')"><span>{{ formatBankAccountType(formState.bankAccountType) }}</span></el-form-item>
                            <el-form-item :label="formState.bankAccountType === '74' ? $t('对公账户名称') : $t('银行账户名称')"><span>{{ formState.bankAccountName || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('银行账号')"><span>{{ formState.bankAccountNumber || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('开户银行')"><span>{{ formState.bankName || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('开户支行')"><span>{{ formState.bankBranchName || "-" }}</span></el-form-item>
                        </div>

                        <!-- 超级管理员信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('超级管理员信息') }}</div>
                            <el-form-item :label="$t('超管类型')"><span>{{ formatContactType(formState.contactType) }}</span></el-form-item>
                            <el-form-item :label="$t('联系人姓名')"><span>{{ formState.contactName || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('超管身份证号')"><span>{{ formState.contactIdNumber || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('联系人手机')"><span>{{ formState.contactPhone || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('联系人邮箱')"><span>{{ formState.contactEmail || "-" }}</span></el-form-item>
                        </div>

                        <!-- 时间信息 -->
                        <div class="form-section">
                            <div class="section-title"><span class="title-bar"></span>{{ $t('时间信息') }}</div>
                            <el-form-item :label="$t('创建时间')"><span>{{ detail.createTime || "-" }}</span></el-form-item>
                            <el-form-item :label="$t('更新时间')"><span>{{ detail.modifyTime || "-" }}</span></el-form-item>
                        </div>
                    </el-form>
                </div>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { isStore } from "@/utils/version";
import { getMerchantApplicationDetail } from "@/api/finance/merchantApplication";
import type { MerchantApplicationItem, MerchantApplicationSubmitForm } from "@/types/finance/merchantApplication.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isStore() ? t("门店") : t("店铺");
const shopUrlLabel = isStore() ? t("门店Url") : t("店铺Url");
const emit = defineEmits(["close"]);
const props = defineProps({
    id: { type: [Number, String], default: 0 },
    act: { type: String, default: "" },
    isDialog: Boolean
});

const query = useRouter().currentRoute.value.query;
const id = computed(() => (props.isDialog ? props.id : query.id));

const loading = ref(true);
const detail = ref<Partial<MerchantApplicationItem>>({});
const formState = ref<Partial<MerchantApplicationSubmitForm>>({});
const organizationTypeText = computed(() => {
    if (formState.value.organizationType === "2") return t("企业");
    if (formState.value.organizationType === "4") return t("个体工商户");
    if (formState.value.organizationType === "2401") return t("个人");
    return "-";
});
const isPersonal = computed(() => formState.value.organizationType === "2401");
const merchantNameLabel = computed(() =>
    isPersonal.value ? (isStore() ? t("门店全称") : t("店铺全称")) : t("商户名称")
);

const getStatusTagType = (status?: string) => {
    const map: Record<string, string> = {
        DRAFT: "info",
        PENDING_REVIEW: "warning",
        SUBMITTED: "primary",
        CHECKING: "primary",
        AUDITING: "primary",
        ACCOUNT_NEED_VERIFY: "warning",
        NEED_SIGN: "warning",
        FINISH: "success",
        REJECTED: "danger"
    };
    return map[status ?? ""] || "info";
};
const formatPayChannel = (val?: string) => {
    const map: Record<string, string> = { WECHAT: t("微信"), ALIPAY: t("支付宝"), HUIFU: t("汇付天下") };
    return map[val ?? ""] || val || t("微信");
};
const formatBankAccountType = (val?: string) => (val === "74" ? t('对公') : val === "75" ? t('对私') : val || "-");
const formatContactType = (val?: string) => (val === "65" ? t('经营者/法人') : val === "66" ? t('经办人') : val || "-");

const copySignUrl = async () => {
    if (!detail.value.signUrl) return;
    try {
        await navigator.clipboard.writeText(detail.value.signUrl);
        message.success(t('已复制到剪贴板'));
    } catch {
        message.error(t('复制失败'));
    }
};

const SNAPSHOT_FIELDS: string[] = [
    "shopId",
    "organizationType",    "merchantName",
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

const pickField = (data: Record<string, any>, snapshot: Record<string, any> | undefined, key: string) => {
    const direct = data[key];
    if (direct !== undefined && direct !== null && direct !== "") return String(direct);
    if (snapshot && snapshot[key] !== undefined && snapshot[key] !== null) return String(snapshot[key]);
    return undefined;
};

const fetchDetail = async () => {
    if (!id.value) {
        loading.value = false;
        return;
    }
    try {
        const result = await getMerchantApplicationDetail({ id: id.value });
        let snapshot = result.requestSnapshot;
        if (typeof snapshot === "string") {
            try {
                snapshot = JSON.parse(snapshot);
            } catch {
                snapshot = undefined;
            }
        }
        detail.value = result;
        const next: Record<string, any> = {};
        SNAPSHOT_FIELDS.forEach((key) => {
            next[key] = pickField(result as Record<string, any>, snapshot as Record<string, any> | undefined, key);
        });
        formState.value = next;
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchDetail();
});
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
            background-color: #f5f7fa;
            padding: 12px 10px;
            margin-bottom: 20px;
            .title-bar {
                width: 3px;
                height: 14px;
                background-color: var(--theme-color, #1456f0);
                margin-right: 5px;
            }
        }
    }
    .reject-reason {
        color: #ef4444;
        font-weight: 500;
    }
    .break-all {
        word-break: break-all;
    }
}
</style>

