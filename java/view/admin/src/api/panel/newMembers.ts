import request from "@/utils/request";
import type { newMemberFilterParams, FilterResult } from "@/types/panel/newMembers.d";

// 新增会员数据
export const getAddUserTrends = (params: newMemberFilterParams) => {
    return request<FilterResult>({
        url: "panel/statisticsUser/addUserTrends",
        method: "get",
        params,
    });
}

export const exportAddUserTrends = (params: newMemberFilterParams) => {
   return request({
       url: "panel/statisticsUser/addUserTrends",
       method: "get",
       responseType: "arraybuffer",
       params,
   })
}