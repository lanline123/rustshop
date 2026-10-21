import request from "@/utils/request";
import type {PromotionFilterResult, RimeTypeCount} from "@/types/promotion/manage";

// 获取活动总览
export const getPromotionList = (params: object) => {
    return request<PromotionFilterResult>({
        url: "promotion/promotion/list",
        method: "get",
        params,
    });
}

// 获取活动数量
export const getPromotionCount = () => {
    return request<RimeTypeCount>({
        url: "promotion/promotion/getPromotionCount",
        method: "get"
    });
}