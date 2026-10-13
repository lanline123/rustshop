import request from "@/utils/request";
import type {
    CategoryFeeRuleCreateParams,
    CategoryFeeRuleDeleteParams,
    CategoryFeeRuleFilterParams,
    CategoryFeeRuleFilterResult,
    CategoryFeeRuleFormState,
    CategoryFeeRuleMutationResult,
    CategoryFeeRuleUpdateParams,
    CategoryFeeRuleUpdateStatusParams
} from "@/types/finance/categoryFeeRule";

export const getCategoryFeeRuleList = (params: CategoryFeeRuleFilterParams) => {
    return request<CategoryFeeRuleFilterResult>({
        url: "finance/categoryFeeRule/list",
        method: "post",
        data: params
    });
};

export const getCategoryFeeRule = (params: { id: number }) => {
    return request<CategoryFeeRuleFormState>({
        url: "finance/categoryFeeRule/detail",
        method: "get",
        params
    });
};

export const createCategoryFeeRule = (data: CategoryFeeRuleCreateParams) => {
    return request<CategoryFeeRuleMutationResult>({
        url: "finance/categoryFeeRule/save",
        method: "post",
        data
    });
};

export const updateCategoryFeeRule = (data: CategoryFeeRuleUpdateParams) => {
    return request<CategoryFeeRuleMutationResult>({
        url: "finance/categoryFeeRule/update",
        method: "post",
        data
    });
};

export const updateCategoryFeeRuleStatus = (data: CategoryFeeRuleUpdateStatusParams) => {
    return request<CategoryFeeRuleMutationResult>({
        url: "finance/categoryFeeRule/updateStatus",
        method: "post",
        data
    });
};

export const delCategoryFeeRule = (data: CategoryFeeRuleDeleteParams) => {
    return request({
        url: "finance/categoryFeeRule/del",
        method: "post",
        data
    });
};
