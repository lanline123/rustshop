import request from "@/utils/request";
import type {UserWithdrawApplyFilterParams, UserWithdrawApplyFilterResult, UserWithdrawApplyFormState} from "@/types/finance/userWithdrawApply.d"

/**
 * 获取用户提现申请列表
 * @description 分页获取用户提现申请数据
 * @param params - 筛选和分页参数
 * @returns 提现申请列表
 */
export const getUserWithdrawApplyList = (params: UserWithdrawApplyFilterParams) => {
    return request<UserWithdrawApplyFilterResult>({
        url: "finance/userWithdrawApply/list",
        method: "get",
        params,
    });
}

/**
 * 删除提现申请
 * @description 删除指定提现申请记录
 * @param data - 申请ID
 * @returns 操作结果
 */
export const delUserWithdrawApply= (data: object) => {
    return request({
        url: "finance/userWithdrawApply/del",
        method: "post",
        data,
    });
}

/**
 * 撤回提现申请
 * @description 撤回指定提现申请
 * @param data - 申请ID
 * @returns 操作结果
 */
export const recallUserWithdrawApply= (data: object) => {
    return request({
        url: "finance/userWithdrawApply/recall",
        method: "post",
        data,
    });
}
/**
 * 获取提现申请详情
 * @description 获取提现申请的完整信息
 * @param action - 操作类型
 * @param params - 申请ID等参数
 * @returns 提现申请详情
 */
export const getUserWithdrawApply  = (action: string, params: object) => {
    return request<UserWithdrawApplyFormState>({
        url: "finance/userWithdrawApply/" + action,
        method: "get",
        params
    });
}

/**
 * 审核提现申请
 * @description 审核处理提现申请（通过/拒绝）
 * @param operation - 操作类型
 * @param data - 审核数据
 * @returns 操作结果
 */
export const updateUserWithdrawApply = (operation: string, data: object) => {
    return request({
        url: "finance/userWithdrawApply/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作提现申请
 * @description 批量处理提现申请
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/userWithdrawApply/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 会员余额提现配置类型（与分账/佣金提现独立，D1107）
 */
export interface MemberWithdrawalSetting {
    memberWithdrawalEnabled?: number; // 0-关闭，1-开通
    memberWithdrawalReceiptMethod?: number[]; // 1-银行卡 2-支付宝 3-微信
    memberWithdrawalDescription?: string;
}

/**
 * 获取会员余额提现配置
 */
export const getMemberWithdrawalConfig = () => {
    return request<MemberWithdrawalSetting>({
        url: "setting/config/memberWithdrawalSettings",
        method: "get",
    });
}

/**
 * 保存会员余额提现配置
 */
export const saveMemberWithdrawalConfig = (data: MemberWithdrawalSetting) => {
    return request({
        url: "setting/config/saveMemberWithdrawal",
        method: "post",
        data,
    });
}
