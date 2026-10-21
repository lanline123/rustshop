import request from "@/utils/request";
import type {
    ProductFilterParams,
    ProductFilterResult,
    ProductFormState,
    ProductParticipleFormResult,
    ProductFormResult,
    SkuProductFilterResult
} from "@/types/product/product.d";

/**
 * 获取商品列表
 * @description 分页获取商品数据，支持多条件筛选
 * @param params - 筛选和分页参数
 * @returns 商品列表
 */
export const getProductList = (params: any) => {
    return request<ProductFilterResult>({
        url: "product/product/list",
        method: "get",
        params
    });
};

/**
 * 获取待审核商品数量
 * @description 获取等待审核的商品总数
 * @returns 待审核数量
 */
export const getProductWaitingCheckedCount = () => {
    return request<any>({
        url: "product/product/getWaitingCheckedCount",
        method: "get"
    });
};

/**
 * 获取商品配置项
 * @description 获取商品编辑页的配置选项
 * @param params - 查询参数
 * @returns 商品配置
 */
export const getProductConfig = (params: any) => {
    return request<ProductFormResult>({
        url: "product/product/config",
        method: "get",
        params
    });
};
/**
 * 批量操作商品
 * @description 对商品进行批量操作（上架、下架、删除等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/product/batch",
        method: "post",
        data: { type, ...data }
    });
};

/**
 * 批量导入商品
 * @description 从供应商批量导入商品数据
 * @param data - 导入数据
 * @returns 导入结果
 */
export const batchImport = (data: object) => {
    return request({
        url: "product/product/vendorProductImport",
        method: "post",
        data
    });
};

/**
 * 移至回收站
 * @description 将商品移至回收站（软删除）
 * @param data - 商品ID
 * @returns 操作结果
 */
export const recycleProduct = (data: object) => {
    return request({
        url: "product/product/recycle",
        method: "post",
        data
    });
};

/**
 * 删除商品
 * @description 彻底删除商品（硬删除）
 * @param data - 商品ID
 * @returns 操作结果
 */
export const delProduct = (data: object) => {
    return request({
        url: "product/product/del",
        method: "post",
        data
    });
};
/**
 * 更新商品字段
 * @description 更新商品的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateProductFiled = (data: object) => {
    return request({
        url: "product/product/updateField",
        method: "post",
        data
    });
};

/**
 * 获取商品详情
 * @description 获取商品的完整信息
 * @param action - 操作类型（detail等）
 * @param params - 商品ID等参数
 * @returns 商品详情
 */
export const getProduct = (action: string, params: object) => {
    return request<ProductFormState>({
        url: "product/product/" + action,
        method: "get",
        params
    });
};
/**
 * 获取商品搜索关键词
 * @description 对商品名称进行分词，生成搜索关键词
 * @param data - 商品名称
 * @returns 分词结果
 */
export const getParticiple = (data: object) => {
    return request<string>({
        url: "product/product/getParticiple",
        method: "post",
        data
    });
};

/**
 * 更新商品
 * @description 创建或更新商品信息
 * @param operation - 操作类型（create/update）
 * @param data - 商品数据
 * @returns 操作结果
 */
export const updateProduct = (operation: string, data: object) => {
    return request({
        url: "product/product/" + operation,
        method: "post",
        data
    });
};
/**
 * 复制商品
 * @description 复制已有商品创建新商品
 * @param data - 原商品ID
 * @returns 操作结果
 */
export const copyProduct = (data: object) => {
    return request({
        url: "product/product/copy",
        method: "post",
        data
    });
};

/**
 * 获取运费模板列表
 * @description 获取可用的运费模板
 * @returns 运费模板列表
 */
export const getShippingTplList = () => {
    return request<any>({
        url: "product/product/shippingTplList",
        method: "get"
    });
};

/**
 * 批量修改商品
 * @description 批量编辑多个商品的属性
 * @param data - 批量编辑数据
 * @returns 操作结果
 */
export const BatchProduct = (data: object) => {
    return request({
        url: "product/productBatch/productBatchEdit",
        method: "post",
        data
    });
};

/**
 * 审核商品
 * @description 审核商家提交的商品
 * @param data - 审核参数（通过/拒绝、原因）
 * @returns 审核结果
 */
export const AuditProduct = (data: any) => {
    return request({
        url: "product/product/audit",
        method: "post",
        data
    });
};

/**
 * 重新提交审核
 * @description 商品审核失败后重新提交审核
 * @param data - 商品ID
 * @returns 操作结果
 */
export const AuditAgainProduct = (data: any) => {
    return request({
        url: "product/product/auditAgain",
        method: "post",
        data
    });
};

/**
 * 分配门店商品
 * @description O2O场景下将商品分配到门店
 * @param data - 分配参数
 * @returns 操作结果
 */
export const allocationProduct = (data: any) => {
    return request({
        url: "o2o/storeProduct/allocationProduct",
        method: "post",
        data
    });
};

/**
 * 获取sku商品列表
 * @param params 
 * @returns 
 */
export const getSkuProductList = (params: any) => {
    return request<SkuProductFilterResult>({
        url: "product/product/selectProductList",
        method: "get",
        params
    });
};


/**
 * 供应商商品重新导入
 * @description 重新从供应商同步商品数据
 * @param data - 导入参数
 * @returns 导入结果
 */
export const vendorProductReImport = (data: any) => {
    return request({
        url: "product/product/vendorProductReImport",
        method: "post",
        data,
    });
};

/** 商品推广数据返回结构 */
export interface ProductPromoteData {
    /** 商品名称 */
    productName?: string;
    /** 商品价格 */
    productPrice?: number | string | null;
    /** 商品主图 URL */
    picUrl?: string;
    /** 商品简介 */
    productBrief?: string;
    /** 商品编号 */
    productSn?: string;
    /** 店铺名称 */
    shopName?: string;
    /** H5 短链完整 URL */
    h5ShortLink?: string;
    /** H5 原始链接 */
    h5OriginalLink?: string;
    /** PC 短链完整 URL */
    pcShortLink?: string;
    /** PC 原始链接 */
    pcOriginalLink?: string;
    /** 小程序二维码（Base64 data URL） */
    miniCode?: string;
    /** 微信小程序短链；生成失败时为 null */
    wechatShortLink?: string | null;
    /** 微信短链失败原因，成功时为 null */
    wechatShortLinkError?: string | null;
}

/**
 * 获取商品推广数据
 * @description 获取商品的推广海报等数据
 * @param params - 商品ID
 * @returns 推广数据
 */
export const getProductPromoteData = (params: { productId: number | string; shopId?: number }) => {
    return request<ProductPromoteData>({
        url: "product/product/post",
        method: "get",
        params
    });
};

/** 商品分享信息结构 */
export interface ProductShareInfo {
    /** 商品 ID */
    productId: number | string;
    /** 自定义商品名称 */
    productName?: string | null;
    /** 自定义商品价格 */
    productPrice?: number | string | null;
    /** 自定义商品图片 URL */
    picUrl?: string | null;
}

/**
 * 查询商品分享信息
 * @description 未保存时返回商品本身的名称、价格、图片
 * @param productId - 商品 ID
 * @returns 商品分享信息
 */
export const getProductShareInfo = (productId: number | string) => {
    return request<ProductShareInfo>({
        url: "product/product/shareInfo",
        method: "get",
        params: { productId }
    });
};

/**
 * 保存商品分享信息
 * @param data - 包含 productId 及可选的自定义字段
 * @returns 保存结果
 */
export const saveProductShareInfo = (data: ProductShareInfo) => {
    return request<boolean>({
        url: "product/product/saveShareInfo",
        method: "post",
        data
    });
};

/**
 * 获取首页优惠券
 * @description 获取可在首页展示的优惠券列表
 * @param shopId - 店铺ID（可选）
 * @returns 优惠券列表
 */
export const getHomeCoupon = (shopId?: number) => {
    const params = typeof shopId === "number" && shopId > -1 ? { shopId } : undefined;
    return request<any[]>({
        url: "home/home/getCoupon",
        method: "get",
        params
    });
};
