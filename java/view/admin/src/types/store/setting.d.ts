/**
 * GetStoreVO
 */
export interface Response {
    /**
     * 分配商品名称 0不允许，1允许
     */
    storeAssignProductName?: number;
    /**
     * 分配商品价格 0使用总部/区域统一售价，1使用门店独立售价
     */
    storeAssignProductPrice?: number;
    /**
     * 门店独立商品 0不允许，1允许
     */
    storeIndependentGoods?: number;
    /**
     * 商品分配后状态 0下架，1上架
     */
    storePostAllocationStatus?: number;
    /**
     * 库存模式 0不使用门店独立库存，1使用门店独立库存
     */
    storeUseSoloProductStock?: number;
    /**
     * 库存模式 0不使用平台库存，1使用平台库存
     */
    storeUseTotalProductStock?: number;
    storeCustomSubmitShippingType?: number;
    storeShowOtherCityStore?: number;
    /**
     * 分配商品服务费规则 0使用总部统一配置，1使用门店独立配置
     */
    storeAssignServerServiceFee?: number;
    /**
     * 分配商品配送费规则 0使用总部统一配置，1使用门店独立配置
     */
    storeAssignDeliveryServiceFee?: number;
    /**
     * 门店可修改商品主图、详情页、相册 0不允许，1允许
     */
    storeAssignProductImage?: number;

    shopProductNeedCheck?: number;
    [property: string]: any;
}