import request from "@/utils/request";
import type { TaxRateFilterParams, TaxRateFilterResult, TaxRateFormState } from "@/types/setting/taxRate";

type TaxRateWriteAction = "create" | "update";

export const getTaxRateList = (params: TaxRateFilterParams) => {
    return request<TaxRateFilterResult>({
        url: "setting/taxRate/list",
        method: "get",
        params
    });
};

export const getTaxRate = (action: "detail" | "add" | (string & {}), params: { id: number; taxRateId?: number }) => {
    return request<TaxRateFormState>({
        url: `setting/taxRate/${action}`,
        method: "get",
        params
    });
};

export const updateTaxRate = (operation: TaxRateWriteAction, data: TaxRateFormState & { id?: number; taxRateId?: number }) => {
    return request({
        url: `setting/taxRate/${operation}`,
        method: "post",
        data
    });
};

export const delTaxRate = (data: { id: number | string; taxRateId?: number | string }) => {
    return request({
        url: "setting/taxRate/del",
        method: "post",
        data
    });
};
