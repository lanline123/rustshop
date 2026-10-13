const {
    VITE_IS_MERCHANT,
    VITE_IS_PRO,
    VITE_IS_OVERSEAS,
    VITE_IS_B2B,
    VITE_IS_VENDOR,
    VITE_IS_STORE,
    VITE_IS_O2O_POS,
    VITE_IS_O2O_CYCLE,
    VITE_IS_OVERSEAS_LANG,
    VITE_IS_OVERSEAS_DECOR,
    VITE_IS_OVERSEAS_PC_STYLE,
    VITE_IS_OVERSEAS_H5_STYLE,
    VITE_IS_B2B_ENQUIRY,
    VITE_IS_B2B_MULTI_SPEC,
    VITE_IS_B2B_BATCH_PURCHASE,
    VITE_IS_OVERSEAS_THIRD_LOGIN
} = import.meta.env;
//是否多商户
export function isMerchant() {
    return VITE_IS_MERCHANT == 1;
}
//是否企业版
export function isPro() {
    return VITE_IS_PRO == 1;
}
//是否跨境版
export function isOverseas() {
    return VITE_IS_OVERSEAS == 1;
}
//是否B2B版
export function isB2b() {
    return VITE_IS_B2B == 1;
}
//是否S2B2C(供应商)版
export function isS2b2c() {
    return VITE_IS_VENDOR == 1;
}
//是否多门店
export function isStore() {
    return VITE_IS_STORE == 1;
}

// 是否单商户
export function isB2CPro() {
    return !isMerchant() && !isStore() && !isS2b2c() && !isOverseas() && !isB2b() && isPro();
}
