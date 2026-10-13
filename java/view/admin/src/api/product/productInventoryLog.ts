import request from "@/utils/request";
import type {ProductInventoryLogFilterParams, ProductInventoryLogFilterResult} from "@/types/product/productInventoryLog";

// 获取库存日志列表
export const getProductInventoryLogList = (params: ProductInventoryLogFilterParams) => {
    return request<ProductInventoryLogFilterResult>({
        url: "product/productInventoryLog/list",
        method: "get",
        params,
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/productInventoryLog/batch",
        method: "post",
        data: {type, ...data},
    });
}

