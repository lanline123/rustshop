import request from "@/utils/request";
import type { KefuAccessConfig, KefuAccessConfigSaveParams } from "@/types/im/kefu";

/**
 * 获取接入规则
 * @description 获取当前店铺的客服接入规则配置
 * @param params - shopId（可选，不传取当前店铺）
 * @returns 接入规则配置
 */
export const getKefuAccessConfig = (params?: object) => {
    return request<KefuAccessConfig>({
        url: "im/shopKefuConfig/accessConfig/detail",
        method: "get",
        params
    });
};

/**
 * 保存接入规则
 * @param data - 接入规则配置
 * @returns 操作结果
 */
export const saveKefuAccessConfig = (data: KefuAccessConfigSaveParams) => {
    return request({
        url: "im/shopKefuConfig/accessConfig/save",
        method: "post",
        data
    });
};
