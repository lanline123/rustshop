import request from "@/utils/request";
import { SeckillFilterResult, SeckillFormState ,SeckillFilterParams, SeckillPreviewResult} from "@/types/promotion/seckill";

/**
 * 获取秒杀活动列表
 * @description 分页获取秒杀活动数据
 * @param params - 筛选和分页参数
 * @returns 秒杀活动列表
 */
export const getSeckillList = (params: SeckillFilterParams) => {
    return request<SeckillFilterResult>({
        url: "promotion/seckill/list",
        method: "get",
        params,
    });
}
/**
 * 获取秒杀商品列表（PC 首页装修预览用）
 * @description 与 C 端首页 home/home/getSeckill 复用同一服务端逻辑，保证预览与前台选中同一个活动。
 *              promotion/seckill/list 会按 shop_id = 0 过滤、且排序方向与 C 端相反，不能用于预览取数。
 * @param params - 分页参数，size 须固定传 15 以对齐 C 端默认分页窗口
 * @returns 秒杀商品列表（含商品图与价格，无需再查商品接口补全）
 */
export const getSeckillListForHomePreview = (params: { page: number; size: number }) => {
    return request<SeckillPreviewResult>({
        url: "promotion/seckill/listForHomePreview",
        method: "get",
        params,
    });
}

/**
 * 批量操作秒杀活动
 * @description 批量处理秒杀活动（启用/禁用等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/seckill/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 删除秒杀活动
 * @description 删除指定秒杀活动
 * @param data - 活动ID
 * @returns 操作结果
 */
export const delSeckill = (data: object) => {
    return request({
        url: "promotion/seckill/del",
        method: "post",
        data,
    });
}
/**
 * 更新秒杀活动字段
 * @description 更新秒杀活动的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateSeckillFiled = (data: object) => {
    return request({
        url: "promotion/seckill/updateField",
        method: "post",
        data,
    });
}

/**
 * 获取秒杀活动详情
 * @description 获取秒杀活动的完整信息
 * @param action - 操作类型
 * @param params - 活动ID等参数
 * @returns 秒杀活动详情
 */
export const getSeckill = (action: string, params: object) => {
    return request<SeckillFormState[]>({
        url: "promotion/seckill/" + action,
        method: "get",
        params
    });
}
/**
 * 获取秒杀活动详情（装修预览用）
 * @description 活动列表接口不返回秒杀价，装修预览需按活动逐个取 seckillItem 补价格；
 *              并发调用失败时不弹错误提示，避免同时刷出多条相同报错
 * @param params - 活动ID
 * @returns 秒杀活动详情
 */
export const getSeckillDetailForDecorate = (params: object) => {
    return request<SeckillFormState>({
        url: "promotion/seckill/detail",
        method: "get",
        params,
        noErrorTip: true
    });
}

/**
 * 更新秒杀活动
 * @description 创建或更新秒杀活动
 * @param operation - 操作类型（create/update）
 * @param data - 活动数据
 * @returns 操作结果
 */
export const updateSeckill = (operation: string, data: object) => {
    return request({
        url: "promotion/seckill/" + operation,
        method: "post",
        data
    });
}
