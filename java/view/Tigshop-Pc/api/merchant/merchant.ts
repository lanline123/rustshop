export const applyMerchant = (data: object) => {
    return asyncRequest({
        url: "merchant/merchant/apply",
        method: "post",
        data
    });
};

export const getMerchantInfo = (id: any) => {
    return asyncRequest<any>({
        url: "merchant/merchant/applyDetail",
        method: "get",
        params: { id }
    });
};

export const getMyMerchant = () => {
    return asyncRequest<any>({
        url: "merchant/merchant/myApply",
        method: "get"
    });
};

export const getApplyShopAgreement = () => {
    return asyncRequest<any>({
        url: "merchant/merchant/applyShopAgreement",
        method: "get"
    });
};
