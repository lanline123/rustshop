import request from "@/utils/request";
import type {CountryRegionState, RegionFilterParams, RegionFilterResult, RegionFilterState, RegionFormState} from "@/types/setting/region"

/**
 * 获取地区列表
 * @description 分页获取地区数据
 * @param params - 筛选和分页参数
 * @returns 地区列表
 */
export const getRegionList = (params: Object) => {
    return request<RegionFilterResult>({
        url: "setting/region/list",
        method: "get",
        params,
    });
}
/**
 * 获取下级地区列表
 * @description 获取指定地区的下级地区
 * @param params - 父级地区ID
 * @returns 下级地区列表
 */
export const getChildRegionList = (params: RegionFilterParams) => {
    return request<RegionFilterResult>({
        url: "setting/region/getChildRegion",
        method: "get",
        params,
    });
}

/**
 * 获取地区树
 * @description 获取完整的地区树形结构
 * @param params - 查询参数
 * @returns 地区树
 */
export const getRegionTreeList = (params?: RegionFilterParams) => {
    return request<any>({
        url: "setting/region/getAllRegionTree",
        method: "get",
        params,
    });
}

/** 获取指定地区的子地区（跨境地址逐级选择使用） */
export const getRegionListByRegionId = (regionId: number | string) => {
    return request<RegionFilterState[]>({
        url: "sys/region/getRegionListByRegionId",
        method: "get",
        params: { regionId: Number(regionId) },
        prefix: "/api"
    });
};

/** 获取跨境国家列表。standardCode 是提交用的唯一国家值。 */
export const getCountryList = () => {
    return request<CountryRegionState[]>({
        url: "sys/region/getCountryList",
        method: "get",
        prefix: "/api"
    });
};

/** 获取顶级地区（跨境地址逐级选择的初始层） */
export const getProvinceRegionList = () => {
    return request<any[]>({
        url: "sys/region/getProvinceList",
        method: "get",
        prefix: "/api"
    });
};

/**
 * 删除地区
 * @description 删除指定地区
 * @param data - 地区ID
 * @returns 操作结果
 */
export const delRegion= (data: object) => {
    return request({
        url: "setting/region/del",
        method: "post",
        data,
    });
}

/**
 * 获取地区详情
 * @description 获取地区的完整信息
 * @param action - 操作类型
 * @param params - 地区ID等参数
 * @returns 地区详情
 */
export const getRegion  = (action: string, params: object) => {
    return request<RegionFormState>({
        url: "setting/region/" + action,
        method: "get",
        params
    });
}

/**
 * 更新地区
 * @description 创建或更新地区
 * @param operation - 操作类型（create/update）
 * @param data - 地区数据
 * @returns 操作结果
 */
export const updateRegion = (operation: string, data: object) => {
    return request({
        url: "setting/region/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作地区
 * @description 批量处理地区
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/region/batch",
        method: "post",
        data: {type, ...data},
    });
}
/**
 * 更新地区字段
 * @description 更新地区的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateRegionField = (data:object) => {
    return request({
        url: "setting/region/updateField",
        method: "post",
        data
    });
}

/**
 * 根据地区ID获取经纬度
 * @param regionId - 地区ID
 * @returns 经纬度信息
 */
export const getLocationByRegionId = (regionId: number) => {
    return request<{ regionId: number; regionName: string; centor: string }>({
        url: "setting/region/getLocationByRegionId",
        method: "get",
        params: { regionId }
    });
}
