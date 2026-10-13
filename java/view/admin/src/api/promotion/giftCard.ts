import request from "@/utils/request";
import type {
    GiftCardDetail,
    GiftCardFilterParams,
    GiftCardFilterResult,
    GiftCardGroupFilterParams,
    GiftCardGroupFilterResult,
    GiftCardGroupFormState,
    GiftCardGroupItem,
    GiftCardGroupProducts,
    GiftCardImportResult
} from "@/types/promotion/giftCard";

/**
 * 礼品卡分组列表数据获取
 * @param params 查询参数
 * @returns
 */
export const getGiftCardGroupList = (params: GiftCardGroupFilterParams) => {
    return request<GiftCardGroupFilterResult>({
        url: "promotion/giftCardGroups/list",
        method: "get",
        params
    });
};

/**
 * 获取礼品卡分组
 * @param params 获取礼品卡参数
 * @returns
 */
export const getGiftCardGroups = (params: { cardType: number; cardsGroupId?: number }) => {
    return request<GiftCardGroupItem[]>({
        url: "promotion/giftCardGroups/getGiftCardGroups",
        method: "get",
        params
    });
};

/**
 * 获取礼品卡详情
 * @param params 获取礼品卡参数
 * @returns
 */
export const getGiftCardGroupDetail = (params: { cardGroupsId: number }) => {
    return request<GiftCardGroupFormState>({
        url: "promotion/giftCardGroups/detail",
        method: "get",
        params
    });
};

/**
 * 更新礼品卡分组状态
 * @param data
 * @returns
 */
export const updateGiftCardGroupStatus = (data: object) => {
    return request({
        url: "promotion/giftCardGroups/updateStatus",
        method: "post",
        data
    });
};

/**
 * 更新礼品卡分组
 * @description 创建或更新礼品卡分组
 * @param operation - 操作类型（create/update）
 * @param data - 分组数据
 * @returns 操作结果
 */
export const updateGiftCardGroup = (operation: string, data: object) => {
    return request({
        url: `promotion/giftCardGroups/${operation}`,
        method: "post",
        data
    });
};

/**
 * 删除礼品卡分组
 * @description 删除指定礼品卡分组
 * @param data - 分组ID
 * @returns 操作结果
 */
export const delGiftGroupCard = (data: object) => {
    return request({
        url: "promotion/giftCardGroups/delete",
        method: "post",
        data
    });
};

/**
 * 批量操作礼品卡
 * @description 批量处理礼品卡
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmitGiftCard = (data: object) => {
    return request({
        url: "promotion/giftCard/batch",
        method: "post",
        data
    });
};

/**
 * 导出礼品卡列表
 * @description 导出礼品卡数据为Excel文件
 * @param params - 筛选参数
 * @returns 文件流
 */
export const giftCardsExport = (params: object) => {
    return request({
        url: "promotion/giftCards/exportList",
        method: "get",
        responseType: "arraybuffer",
        params
    });
};

/**
 * 导出礼品卡导入模板
 * @description 下载礼品卡批量导入的Excel模板
 * @returns 文件流
 */
export const giftCardExportTemplate = () => {
    return request({
        url: "promotion/giftCards/importTemplate",
        method: "get",
        responseType: "arraybuffer"
    });
};

/**
 * 批量导入礼品卡
 * @description 通过Excel文件批量导入礼品卡
 * @param params - 查询参数
 * @param data - 文件数据
 * @returns 导入结果
 */
export const importGiftCard = (params: object, data: object) => {
    return request<GiftCardImportResult>({
        url: "promotion/giftCards/importList",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        params,
        data
    });
};

/**
 * 批量生成礼品卡
 * @param data
 * @returns
 */
export const giftCardBatchGenerate = (data: object) => {
    return request({
        url: "promotion/giftCards/createBatch",
        method: "post",
        data
    });
};

/**
 * 删除礼品卡分组下卡片
 * @param data
 * @returns
 */
export const delGiftCard = (data: object) => {
    return request({
        url: "promotion/giftCards/delete",
        method: "post",
        data
    });
};

/**
 * 更新礼品卡分组下卡片
 * @param params
 * @returns
 */
export const updateGiftCard = (operation: string, data: object) => {
    return request({
        url: `promotion/giftCards/${operation}`,
        method: "post",
        data
    });
};

/**
 * 获取礼品卡分组下卡片列表
 * @param params
 * @returns
 */
export const getGiftCardList = (params: GiftCardFilterParams) => {
    return request<GiftCardFilterResult>({
        url: "promotion/giftCards/list",
        method: "get",
        params
    });
};

/**
 * 获取礼品卡分组下卡片详情
 * @param params
 * @returns
 */
export const getGiftCard = (params: object) => {
    return request({
        url: "promotion/giftCards/detail",
        method: "get",
        params
    });
};

/**
 * 获取礼品卡详细信息（含兑换信息）
 * @param params
 * @returns
 */
export const getGiftCardDetailInfo = (params: { giftCardsId: number }) => {
    return request<GiftCardDetail>({
        url: "promotion/giftCards/giftCardExchangeDetail",
        method: "get",
        params
    });
};

/**
 * 获取礼品卡密码
 * @param params
 * @returns
 */
export const getGiftCardPasswrod = (params: object) => {
    return request<string>({
        url: "promotion/giftCards/showCardPassword",
        method: "get",
        params
    });
};

/**
 * 更新礼品卡分组关联商品
 * @param params
 * @returns
 */
export const updpateiftCardGroupProducts = (data: GiftCardGroupProducts) => {
    return request({
        url: "promotion/giftCardGroups/giftCardGroupProductsUpdate",
        method: "post",
        data
    });
};

/**
 * 更新礼品卡状态
 * @param data
 * @returns
 */
export const updateGiftCardStatus = (data: object) => {
    return request({
        url: "promotion/giftCards/updateCardStatus",
        method: "post",
        data
    });
};
