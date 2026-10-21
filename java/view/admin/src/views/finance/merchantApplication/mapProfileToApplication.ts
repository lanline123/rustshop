import type { MerchantApplicationSubmitForm } from "@/types/finance/merchantApplication.d";

export const LONG_TERM_VALUE = "长期";

const MAINLAND_ID_TYPE = 1;
const ORG_ENTERPRISE = "2";
const ORG_INDIVIDUAL = "4";
const BANK_CORPORATE = "74";
const BANK_PERSONAL = "75";
const CONTACT_LEGAL = "65";

type UnknownRecord = Record<string, any>;

export type MerchantProfileSource = {
    type?: number | string;
    companyName?: string;
    corporateName?: string;
    shopTitle?: string;
    shopContactPhone?: string;
    baseData?: UnknownRecord | null;
    merchantData?: UnknownRecord | null;
    shopData?: UnknownRecord | null;
};

export type VendorProfileSource = {
    type?: number | string;
    vendorName?: string;
    kefuPhone?: string;
    vendorData?: UnknownRecord | null;
    personData?: UnknownRecord | null;
};

export type ProfileMapOptions = {
    longTermLabel?: string;
};

const isMasked = (value: string) => value.includes("*");

export const usableString = (value: unknown): string => {
    if (value === undefined || value === null) return "";
    const str = String(value).trim();
    if (!str || isMasked(str)) return "";
    return str;
};

export const toDateString = (value: unknown): string => {
    const str = usableString(value);
    if (!str) return "";
    const normalized = str.replace(/[./]/g, "-");
    return normalized.slice(0, 10);
};

export const pickPhotoUrl = (value: unknown): string => {
    if (value === undefined || value === null || value === "") return "";
    if (typeof value === "string") {
        const trimmed = value.trim();
        if (!trimmed || isMasked(trimmed)) return "";
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            try {
                return pickPhotoUrl(JSON.parse(trimmed));
            } catch {
                return trimmed;
            }
        }
        return trimmed;
    }
    if (Array.isArray(value)) {
        return pickPhotoUrl(value[0]);
    }
    if (typeof value === "object") {
        const item = value as UnknownRecord;
        return usableString(item.picUrl || item.url);
    }
    return "";
};

const firstOf = (...values: unknown[]) => {
    for (const value of values) {
        const str = usableString(value);
        if (str) return str;
    }
    return "";
};

const resolveTerm = (
    type: unknown,
    range: unknown,
    longTermStart: unknown,
    longTermLabel: string
) => {
    const dates = Array.isArray(range) ? range : [];
    const isRange = Number(type) === 1;
    if (isRange) {
        return {
            begin: toDateString(dates[0]),
            end: toDateString(dates[1])
        };
    }
    return {
        begin: toDateString(longTermStart) || toDateString(dates[0]),
        end: longTermLabel
    };
};

const toOrganizationType = (type: unknown) => (Number(type) === 2 ? ORG_ENTERPRISE : ORG_INDIVIDUAL);

const compactForm = (form: MerchantApplicationSubmitForm): MerchantApplicationSubmitForm => {
    const next: MerchantApplicationSubmitForm = {};
    (Object.keys(form) as (keyof MerchantApplicationSubmitForm)[]).forEach((key) => {
        const value = form[key];
        if (value === undefined || value === null || value === "") return;
        (next as UnknownRecord)[key] = value;
    });
    return next;
};

/** 店铺/门店：把商户入驻资料映射为进件表单可回填字段 */
export const mapMerchantProfileToApplication = (
    profile: MerchantProfileSource | null | undefined,
    options: ProfileMapOptions = {}
): MerchantApplicationSubmitForm => {
    if (!profile) return {};
    const longTermLabel = options.longTermLabel || LONG_TERM_VALUE;
    const base = profile.baseData || {};
    const merchant = profile.merchantData || {};
    const shop = profile.shopData || {};
    const type = profile.type ?? base.type;
    const organizationType = toOrganizationType(type);
    const legalPerson = firstOf(base.corporateName, profile.corporateName, merchant.corporateName);
    const merchantName = firstOf(merchant.merchantName, profile.companyName, base.companyName, profile.shopTitle);
    const license = resolveTerm(base.operatingTermType, base.operatingTermTypeDate, base.operatingTermTypeEnd, longTermLabel);
    const card = resolveTerm(
        base.certificateValidityPeriod,
        base.certificateValidityPeriodDate,
        base.certificateValidityPeriodEnd,
        longTermLabel
    );
    const idCardNumber = Number(base.documentType) === MAINLAND_ID_TYPE || base.documentType == null
        ? firstOf(base.documentNumber)
        : "";
    const contactName = firstOf(merchant.contactName, legalPerson);
    const contactIsLegal = !contactName || contactName === legalPerson;

    return compactForm({
        organizationType,
        businessLicenseNumber: firstOf(base.businessLicenseId),
        merchantName,
        legalPerson,
        licensePeriodBegin: license.begin,
        licensePeriodEnd: license.end,
        licenseCopy: pickPhotoUrl(merchant.businessLicenseImg || merchant.businessLicenseImgTemp),
        idCardName: firstOf(base.corporateName, legalPerson),
        idCardNumber,
        cardPeriodBegin: card.begin,
        cardPeriodEnd: card.end,
        idCardCopy: pickPhotoUrl(base.frontOfPhoto || base.frontOfPhotoTemp),
        idCardNational: pickPhotoUrl(base.backOfPhoto || base.backOfPhotoTemp),
        merchantShortName: firstOf(profile.shopTitle, shop.shopTitle, merchant.merchantName, merchantName),
        servicePhone: firstOf(
            merchant.customerServicePhone,
            profile.shopContactPhone,
            shop.contactMobile,
            shop.customerServicePhone,
            merchant.contactPhone
        ),
        bankAccountType: organizationType === ORG_ENTERPRISE ? BANK_CORPORATE : BANK_PERSONAL,
        bankAccountName: organizationType === ORG_ENTERPRISE
            ? firstOf(base.companyName, profile.companyName, merchantName, legalPerson)
            : firstOf(legalPerson, merchant.contactName),
        bankAccountNumber: firstOf(merchant.bankCard),
        bankName: firstOf(merchant.bankDeposit),
        bankBranchName: firstOf(merchant.bankBranch),
        contactType: CONTACT_LEGAL,
        contactName,
        contactIdNumber: contactIsLegal ? idCardNumber : "",
        contactPhone: firstOf(merchant.contactPhone, profile.shopContactPhone),
        contactEmail: firstOf(merchant.contactEmail)
    });
};

/** 供应商：把供应商入驻资料映射为进件表单可回填字段 */
export const mapVendorProfileToApplication = (
    profile: VendorProfileSource | null | undefined,
    options: ProfileMapOptions = {}
): MerchantApplicationSubmitForm => {
    if (!profile) return {};
    const longTermLabel = options.longTermLabel || LONG_TERM_VALUE;
    const vendor = profile.vendorData || {};
    const person = profile.personData || {};
    const type = profile.type ?? vendor.type;
    const organizationType = toOrganizationType(type);
    const legalPerson = firstOf(person.documentName, vendor.companyName, profile.vendorName);
    const merchantName = firstOf(vendor.companyName, vendor.vendorName, profile.vendorName);
    const license = resolveTerm(vendor.operatingTermType, vendor.operatingTermTypeDate, vendor.operatingTermTypeEnd, longTermLabel);
    const card = resolveTerm(
        person.certificateValidityPeriod,
        person.certificateValidityPeriodDate,
        person.certificateValidityPeriodEnd,
        longTermLabel
    );
    const idCardNumber = Number(person.documentType) === MAINLAND_ID_TYPE || person.documentType == null
        ? firstOf(person.documentNumber)
        : "";
    const contactName = firstOf(vendor.contactName, legalPerson);
    const contactIsLegal = !contactName || contactName === legalPerson;

    return compactForm({
        organizationType,
        businessLicenseNumber: firstOf(vendor.businessLicenseId),
        merchantName,
        legalPerson,
        licensePeriodBegin: license.begin,
        licensePeriodEnd: license.end,
        idCardName: firstOf(person.documentName, legalPerson),
        idCardNumber,
        cardPeriodBegin: card.begin,
        cardPeriodEnd: card.end,
        idCardCopy: pickPhotoUrl(person.frontOfPhoto),
        idCardNational: pickPhotoUrl(person.backOfPhoto),
        merchantShortName: firstOf(profile.vendorName, vendor.vendorName, merchantName),
        servicePhone: firstOf(vendor.customerServicePhone, profile.kefuPhone, vendor.contactPhone),
        bankAccountType: organizationType === ORG_ENTERPRISE ? BANK_CORPORATE : BANK_PERSONAL,
        bankAccountName: organizationType === ORG_ENTERPRISE
            ? firstOf(vendor.companyName, merchantName, legalPerson)
            : firstOf(legalPerson, vendor.contactName),
        bankAccountNumber: firstOf(vendor.bankCard),
        bankName: firstOf(vendor.bankDeposit),
        bankBranchName: firstOf(vendor.bankBranch),
        contactType: CONTACT_LEGAL,
        contactName,
        contactIdNumber: contactIsLegal ? idCardNumber : "",
        contactPhone: firstOf(vendor.contactPhone, profile.kefuPhone),
        contactEmail: firstOf(vendor.contactEmail)
    });
};

export const isEmptyFormValue = (value: unknown) => value === undefined || value === null || value === "";

/** 可由商户/供应商资料回填的字段；不含支付渠道和店铺 ID */
export const PROFILE_FILL_KEYS: (keyof MerchantApplicationSubmitForm)[] = [
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

/** 切换店铺时整份替换商户回填字段，避免留下上一家的证件和账户 */
export const replaceProfileOnForm = (form: MerchantApplicationSubmitForm, profile: MerchantApplicationSubmitForm) => {
    PROFILE_FILL_KEYS.forEach((key) => {
        const nextValue = profile[key];
        (form as UnknownRecord)[key] = isEmptyFormValue(nextValue) ? "" : nextValue;
    });
    if (!form.organizationType) form.organizationType = ORG_INDIVIDUAL;
    if (!form.bankAccountType) form.bankAccountType = BANK_CORPORATE;
    if (!form.contactType) form.contactType = CONTACT_LEGAL;
    return form;
};

/** 把映射结果写入进件表单。新建覆盖空值与默认主体类型；再次编辑只补空缺。 */
export const applyProfileToForm = (
    form: MerchantApplicationSubmitForm,
    profile: MerchantApplicationSubmitForm,
    options: { overwrite?: boolean } = {}
) => {
    const overwrite = options.overwrite === true;
    (Object.keys(profile) as (keyof MerchantApplicationSubmitForm)[]).forEach((key) => {
        if (key === "payChannel" || key === "shopId") return;
        const nextValue = profile[key];
        if (isEmptyFormValue(nextValue)) return;
        const current = form[key];
        if (overwrite || isEmptyFormValue(current)) {
            (form as UnknownRecord)[key] = nextValue;
        }
    });
    return form;
};
