import request from "@/utils/request";
import { ProfitsharingSetting, withdrawalSetting } from "@/types/finance/profitsharingSetting.d";
//获取分账设置
export const getProfitsharingConfig = () => {
    return request<ProfitsharingSetting>({
        url: "setting/config/profitSharingSettings",
        method: "get"
    });
};
export const saveProfitsharingConfig = (data: ProfitsharingSetting) => {
    return request({
        url: "setting/config/saveProfitSharing",
        method: "post",
        data
    });
};

//获取提现设置
export const getwithdrawalConfig = () => {
    return request<withdrawalSetting>({
        url: "setting/config/withdrawalSettings",
        method: "get"
    });
};
export const savewithdrawalConfig = (data: withdrawalSetting) => {
    return request({
        url: "setting/config/saveWithdrawal",
        method: "post",
        data
    });
};
