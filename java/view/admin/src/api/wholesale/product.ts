import request from "@/utils/request";
import type {
    WholesaleProductPageQuery,
    WholesaleProductPageResult,
    SelectableWholesaleProductQuery,
    SelectableWholesaleProductPageResult,
    WholesaleProductAddDTO,
    WholesaleProductBatchAddDTO,
    WholesaleProductBatchAddVO,
    WholesaleProductBuyRuleVO,
    WholesaleBuyRuleSaveDTO,
    WholesaleBuyRuleBatchSaveDTO,
    WholesaleMultipleSaveDTO
} from "@/types/wholesale/product";
import type { WholesalePriceDetailVO, WholesalePriceSaveDTO, BatchCommonPriceDTO, BatchGradePriceDTO } from "@/types/wholesale/price";

/** 批发商品分页 GET /product/page */
export const getWholesaleProductPage = (params: WholesaleProductPageQuery) =>
    request<WholesaleProductPageResult>({ url: "wholesale/product/page", method: "get", params });

/** 可选批发商品分页（平台商品 + 剔除已配置批发） GET /product/selectable/page */
export const getSelectableWholesaleProductPage = (params: SelectableWholesaleProductQuery) =>
    request<SelectableWholesaleProductPageResult>({ url: "wholesale/product/selectable/page", method: "get", params });

/** 添加批发商品 POST /product/add */
export const addWholesaleProduct = (data: WholesaleProductAddDTO) => request<{ productId: number }>({ url: "wholesale/product/add", method: "post", data });

/** 批量添加批发商品 POST /product/batchAdd */
export const batchAddWholesaleProduct = (data: WholesaleProductBatchAddDTO) =>
    request<WholesaleProductBatchAddVO>({ url: "wholesale/product/batchAdd", method: "post", data });

/** 批量删除 POST /product/batchDelete */
export const batchDeleteWholesaleProduct = (data: { productIds: number[] }) =>
    request<{ deletedCount: number }>({ url: "wholesale/product/batchDelete", method: "post", data });

/** 批发价详情 GET /product/price/detail */
export const getWholesalePriceDetail = (params: { productId: number }) =>
    request<WholesalePriceDetailVO>({ url: "wholesale/product/price/detail", method: "get", params });

/** 批发价保存（全量替换） POST /product/price/save */
export const saveWholesalePrice = (data: WholesalePriceSaveDTO) => request({ url: "wholesale/product/price/save", method: "post", data });

/** 批量设置通用售价 POST /product/price/batchSetCommon */
export const batchSetCommonPrice = (data: BatchCommonPriceDTO) =>
    request<{ affectedRows: number }>({ url: "wholesale/product/price/batchSetCommon", method: "post", data });

/** 批量设置类型售价 POST /product/price/batchSetByGrade */
export const batchSetGradePrice = (data: BatchGradePriceDTO) =>
    request<{ affectedRows: number }>({ url: "wholesale/product/price/batchSetByGrade", method: "post", data });

/** 起批量保存 POST /product/buyRule/save */
export const saveBuyRule = (data: WholesaleBuyRuleSaveDTO) => request({ url: "wholesale/product/buyRule/save", method: "post", data });

/** 批量起批量保存 POST /product/buyRule/batchSave（对所选商品统一设置同一起批维度+起批数） */
export const batchSaveBuyRule = (data: WholesaleBuyRuleBatchSaveDTO) =>
    request<{ affectedRows: number }>({ url: "wholesale/product/buyRule/batchSave", method: "post", data });

/** 起批规则详情 GET /product/buyRule/detail（含起批维度/起批数/各 SKU 倍数回填） */
export const getBuyRuleDetail = (params: { productId: number }) =>
    request<WholesaleProductBuyRuleVO>({ url: "wholesale/product/buyRule/detail", method: "get", params });

/** 倍数购买保存 POST /product/multiple/save */
export const saveMultiple = (data: WholesaleMultipleSaveDTO) => request({ url: "wholesale/product/multiple/save", method: "post", data });
