import request from "@/utils/request";
import { buyerShowGroupFilterResult } from "@/types/promotion/buyerShowGroup";
import type { buyerShowCommentFilterResult } from "@/types/promotion/buyerShowComment";
import type { buyerShowListFilterResult } from "@/types/promotion/buyerShowListings";

// 获取买家秀分组列表
export const getBuyerShowGroupList = (params: object) =>{
    return request<buyerShowGroupFilterResult>({
        url: "/promotion/buyerShow/group/list",
        method: "get",
        params,
    });
}

// 更新买家秀分组
export const updateBuyerShowGroup = (action: string, data: object) =>{
    return request({
        url: "/promotion/buyerShow/group/" + action,
        method: "post",
        data,
    });
}

// 删除买家秀分组
export const deleteBuyerShowGroup = (data: any) =>{
    return request({
        url: "/promotion/buyerShow/group/del",
        method: "post",
        data,
    });
}

// 获取买家秀分组详情
export const getBuyerShowGroupDetail = (params: object) =>{
    return request({
        url: "/promotion/buyerShow/group/detail",
        method: "get",
        params,
    });
}

// 获取买家秀评价列表
export const getBuyerShowEvaluationList = (params: object) =>{
    return request<buyerShowCommentFilterResult>({
        url: "/promotion/buyerShow/commentInfo/list",
        method: "get",
        params,
    })
}

// 评论是否公开隐藏
export const commentInfoShowOrHide = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/commentInfo/showOrHide",
        method: "post",
        data,
    });
}

// 评论是否置顶
export const commentInfoTop = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/commentInfo/isTop",
        method: "post",
        data,
    })
}

// 评价批量通过审核或者拒绝
export const commentInfoBatch = (type: string, data: object) =>{
    return request({
        url: "/promotion/buyerShow/commentInfo/batch",
        method: "post",
        data: {type, ...data},
    })
}

// 评价通过审核或者拒绝
export const commentInfoAudit = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/commentInfo/audit",
        method: "post",
        data,
    })
}

// 获取买家秀列表
export const getBuyerShowList = (params: object) =>{
    return request<buyerShowListFilterResult>({
        url: "/promotion/buyerShow/list",
        method: "get",
        params,
    })
}

// 获取买家信息随机
export const getBuyerInfo = (params: object) =>{
    return request({
        url: "/promotion/buyerShow/getBuyerInfo",
        method: "get",
        params,
    })
}

// 获取买家秀详情
export const getBuyerShowDetail = (params: object) =>{
    return request({
        url: "/promotion/buyerShow/detail",
        method: "get",
        params,
    })
}

// 删除买家秀
export const deleteBuyerShow = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/del",
        method: "post",
        data,
    })
}

// 更新买家秀
export const updateBuyerShow = (action: string, data: object) =>{
    return request({
        url: "/promotion/buyerShow/" + action,
        method: "post",
        data,
    });
}

//买家秀批量通过审核或者拒绝
export const batchBuyerShow = (type: string, data: object) =>{
    return request({
        url: "/promotion/buyerShow/batch",
        method: "post",
        data: {type, ...data},
    })
}

//买家秀通过审核或者拒绝
export const buyerShowAudit = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/audit",
        method: "post",
        data,
    })
}

// 买家秀字段更改
export const updateBuyerShowField = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/updateField",
        method: "post",
        data,
    })
}

// 买家秀公开隐藏
export const buyerShowShowOrHide = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/showOrHide",
        method: "post",
        data,
    })
}

// 评论买家秀
export const commentBuyerShow = (data: object) =>{
    return request({
        url: "/promotion/buyerShow/comment",
        method: "post",
        data,
    })
}

// 保存买家秀配置
export const saveBuyerShowConfig = (data: object) =>{
    return request({
        url: "/setting/config/saveBuyerShow",
        method: "post",
        data,
    })
}

// 获取买家秀配置
export const getBuyerShowConfig = () =>{
    return request({
        url: "/setting/config/buyerShowSettings",
        method: "get",
    })
}
