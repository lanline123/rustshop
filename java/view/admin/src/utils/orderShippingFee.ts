type ProductLike = {
    productType?: number | string;
};

type OrderLike = {
    shippingTypeId?: number | string;
    isPickup?: boolean;
    productType?: number | string;
    items?: ProductLike[] | null;
    deliveryOrderInfo?: {
        type?: number | string;
    } | null;
};

const HOME_SERVICE_PRODUCT_TYPES = new Set([5, 6]);

const toNumber = (value: unknown): number | undefined => {
    const result = Number(value);
    return Number.isNaN(result) ? undefined : result;
};

const hasHomeServiceProduct = (order: OrderLike): boolean => {
    const itemProductTypes = Array.isArray(order.items) ? order.items.map((item) => toNumber(item?.productType)) : [];
    const productTypes = itemProductTypes.length > 0 ? itemProductTypes : [toNumber(order.productType)];
    return productTypes.some((type) => type !== undefined && HOME_SERVICE_PRODUCT_TYPES.has(type));
};

/**
 * 获取订单运费展示名称
 * - 预约上门订单：上门服务费
 * - 同城配送订单：配送费
 * - 其他订单：运费
 */
export const getOrderShippingFeeLabel = (order?: OrderLike | null): string => {
    if (!order) return "运费";

    const deliveryType = toNumber(order.deliveryOrderInfo?.type);
    if (deliveryType === 2) return "上门服务费";
    if (deliveryType === 1) return "配送费";

    if (order.isPickup && hasHomeServiceProduct(order)) {
        return "上门服务费";
    }

    if (toNumber(order.shippingTypeId) === 3) {
        return "配送费";
    }

    return "运费";
};
