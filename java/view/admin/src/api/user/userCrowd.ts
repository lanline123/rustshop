import request from "@/utils/request";
import type { MemberAudience } from "@/types/user/memberAudience";

/**
 * 预估人群命中人数
 * @description 会员标签二期：按人群圈选条件预估触达人数
 * @param data - 人群圈选条件
 * @returns 命中人数
 */
export const estimateAudience = (data: MemberAudience) => {
    return request<{ count: number }>({
        url: "user/memberAudience/estimate",
        method: "post",
        data
    });
};

/**
 * 人群条件文字描述
 * @description 将人群圈选条件转为可读文案（用于群发记录等展示）
 * @param data - 人群圈选条件
 * @returns 描述文本
 */
export const describeAudience = (data: MemberAudience) => {
    return request<{ text: string }>({
        url: "user/memberAudience/describe",
        method: "post",
        data
    });
};
