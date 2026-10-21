import request from "@/utils/request";
import type {
    ProductFilterParams,
    ProductFilterResult,
    ProductFormState,
    ProductParticipleFormResult,
    ProductFormResult
} from "@/types/vendor/product.d";

// 获取商品列表
export const getProductList = (params: any) => {
    return request<ProductFilterResult>({
        url: "vendor/product/pageList",
        method: "get",
        params,
    });
};

// 待审核商品数量
export const getProductWaitingCheckedCount = () => {
    return request<any>({
        url: "vendor/product/platform/getWaitingCheckedVendorCount",
        method: "get"
    });
};
// 获取商品配置项
export const getProductConfig = (params: any) => {
    return request<ProductFormResult>({
        url: "vendor/product/config",
        method: "get",
        params
    });
};
// 商品列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "vendor/product/batch",
        method: "post",
        data: {type, ...data}
    });
};

// 商品列表页面单个操作
export const updateProductData = (data: object) => {
    return request({
        url: "vendor/product/batch",
        method: "post",
        data: data
    });
};
// 商品列表移至回收站
export const recycleProduct = (data: object) => {
    return request({
        url: "vendor/product/recycle",
        method: "post",
        data,
    });
};
// 商品列表页面删除项
export const delProduct = (data: object) => {
    return request({
        url: "vendor/product/del",
        method: "post",
        data,
    });
};
// 商品列表页面更新项
export const updateProductFiled = (data: object) => {
    return request({
        url: "vendor/product/updateField",
        method: "post",
        data,
    });
};
// 获取商品详情
export const getProduct = (action: string, params: object) => {
    return request<ProductFormState>({
        url: "vendor/product/" + action,
        method: "get",
        params,
    });
};
// 获取商品搜索关键词
export const getParticiple = (data: object) => {
    return request<string>({
        url: "vendor/product/getParticiple",
        method: "post",
        data,
    });
};
// 更新商品
export const updateProduct = (operation: string, data: object) => {
    return request({
        url: "vendor/product/" + operation,
        method: "post",
        data,
    });
};
// 复制商品
export const copyProduct = (data: object) => {
    return request({
        url: "vendor/product/copy",
        method: "post",
        data,
    });
};

// 获取运费模板
export const getShippingTplList = () => {
    return request<any>({
        url: "vendor/product/shippingTplList",
        method: "get"
    });
};

// 供应商商品批量导出
export const vendorProductBatchExportSubmit = (params: object) => {
    return request({
        url: "vendor/productBatch/vendorProductBatchDeal",
        method: "get",
        responseType: "arraybuffer",
        params,
    });
};

// 供应商下载模板文件
export const vendorProductBatchDownloadTemplateSubmit = () => {
    return request({
        url: "vendor/productBatch/downloadTemplate",
        method: "get",
        responseType: "arraybuffer",
    });
};

// 供应商商品批量上传
export const vendorProductBatchModifySubmit = (data: object) => {
    return request<any>({
        url: "vendor/productBatch/vendorProductBatchModify",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        data,
    });
};

// 批量修改商品
export const BatchProduct = (data: object) => {
    return request({
        url: "vendor/productBatch/vendorProductBatchEdit",
        method: "post",
        data,
    });
};


// 重新提交审核
export const AuditAgainProduct = (data: any) => {
    return request({
        url: "vendor/product/auditAgain",
        method: "post",
        data,
    });
};



// 修改状态商品
export const ProductChangeState = (data: object) => {
    return request({
        url: "vendor/product/changeState",
        method: "post",
        data,
    });
};



// 供应商商品列表
export const getPlatformProductList = (params: any) => {
    return request<ProductFilterResult>({
        url: "vendor/product/platform/pageList",
        method: "get",
        params,
    });
};

// 获取供应商商品详情
export const getPlatformProduct = (action: string, params: object) => {
    return request<ProductFormState>({
        url: "vendor/product/platform/" + action,
        method: "get",
        params,
    });
};

// 审核商品
export const AuditProduct = (data: any) => {
    return request({
        url: "vendor/product/platform/audit",
        method: "post",
        data,
    });
};


// 供应商商品客户端列表
export const getProductClientPage = (params: any) => {
    return request<ProductFilterResult>({
        url: "vendor/product/clientPage",
        method: "get",
        params,
    });
};

// 供应商商品导入  vendorProductIds
export const vendorProductImport = (data: any) => {
    return request({
        url: "product/product/vendorProductImport",
        method: "post",
        data,
    });
};


// 获取供应商的最高价  vendorProductSkuId
export const getVendorMaxPrice = (params: any) => {
    return request<ProductFilterResult>({
        url: "product/product/getVendorMaxPrice",
        method: "get",
        params,
    });
};
