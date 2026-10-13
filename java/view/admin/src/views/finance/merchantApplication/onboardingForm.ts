import type { MerchantApplicationSubmitForm } from "@/types/finance/merchantApplication.d";

export const ORG_ENTERPRISE = "2";
export const ORG_INDIVIDUAL = "4";
export const ORG_PERSONAL = "2401";
export const BANK_CORPORATE = "74";
export const BANK_PERSONAL = "75";
export const CONTACT_LEGAL = "65";

const PERSONAL_OMIT_KEYS: (keyof MerchantApplicationSubmitForm)[] = [
    "businessLicenseNumber",
    "legalPerson",
    "licensePeriodBegin",
    "licensePeriodEnd",
    "licenseCopy"
];

const OPTIONAL_OMIT_IF_EMPTY: (keyof MerchantApplicationSubmitForm)[] = [
    "contactEmail",
    "servicePhone",
    "settlementId",
    "qualificationType",
    "bankBranchName"
];

export const isPersonalOrg = (type?: string) => type === ORG_PERSONAL;

export const isHttpStoreUrl = (value: unknown) => /^https?:\/\/\S+/i.test(String(value ?? "").trim());

export const isDuplicateIdCardPhotos = (copy: unknown, national: unknown) => {
    const front = String(copy ?? "").trim();
    const back = String(national ?? "").trim();
    return !!front && !!back && front === back;
};

export const clearPersonalLicenseFields = (form: MerchantApplicationSubmitForm) => {
    PERSONAL_OMIT_KEYS.forEach((key) => {
        (form as Record<string, unknown>)[key] = "";
    });
    return form;
};

export const syncPersonalDerivedFields = (form: MerchantApplicationSubmitForm) => {
    if (!isPersonalOrg(form.organizationType)) return form;
    const name = form.idCardName || "";
    form.bankAccountType = BANK_PERSONAL;
    form.contactType = CONTACT_LEGAL;
    form.bankAccountName = name;
    form.contactName = name;
    form.contactIdNumber = form.idCardNumber || "";
    return form;
};

/** 按主体类型收口银行账户、超管类型，个人还要清空执照并不提交这些字段。 */
export const applyOrganizationConstraints = (form: MerchantApplicationSubmitForm) => {
    if (form.organizationType === ORG_ENTERPRISE) {
        form.bankAccountType = BANK_CORPORATE;
    }
    if (isPersonalOrg(form.organizationType)) {
        clearPersonalLicenseFields(form);
        syncPersonalDerivedFields(form);
    }
    return form;
};

export const buildMerchantApplicationPayload = (form: MerchantApplicationSubmitForm): MerchantApplicationSubmitForm => {
    const next: MerchantApplicationSubmitForm = { ...form };
    applyOrganizationConstraints(next);
    if (isPersonalOrg(next.organizationType)) {
        PERSONAL_OMIT_KEYS.forEach((key) => {
            delete next[key];
        });
    }
    OPTIONAL_OMIT_IF_EMPTY.forEach((key) => {
        const value = next[key];
        if (value === undefined || value === null || String(value).trim() === "") {
            delete next[key];
        }
    });
    return next;
};
