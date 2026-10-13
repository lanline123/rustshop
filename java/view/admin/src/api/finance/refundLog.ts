import request from "@/utils/request";
import type { RedundLogFilterParams, RedundLogFilterResult } from "@/types/finance/refundLog.d"
// 获取退款记录列表
export const getRedundLogList = (params: RedundLogFilterParams) => {
    return request<RedundLogFilterResult>({
        url: "finance/refundLog/list",
        method: "get",
        params,
    });
}
