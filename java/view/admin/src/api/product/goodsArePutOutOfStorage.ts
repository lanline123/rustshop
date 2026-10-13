import request from "@/utils/request";

// 商品出库
export const productStockOut = (data: any) => {
    return request({
        url: "product/stock/stockOut",
        method: "post",
        data
    });
}
