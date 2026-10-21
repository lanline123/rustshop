import request from "@/utils/request";
import type {AccountPanelFilterParams, AccountPanelFilterState} from "@/types/finance/accountPanel.d"
// 获取商品分类列表
export const getAccountPanelList = (params: AccountPanelFilterParams) => {
    return request<AccountPanelFilterState>({
        url: "finance/accountPanel/list",
        method: "get",
        params,
    });
}

