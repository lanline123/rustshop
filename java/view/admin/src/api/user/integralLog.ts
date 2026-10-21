import request from "@/utils/request";
import { IntegralLogFilterResult, IntegralLogFilterParams} from "@/types/user/integralLog"
// 获取积分日志列表
export const getIntegralLogList = (params: IntegralLogFilterParams) => {
    return request<IntegralLogFilterResult>({
        url: "/user/userPointsLog/list",
        method: "get",
        params,
    });
}