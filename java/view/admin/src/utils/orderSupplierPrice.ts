/**
 * 供应商订单商品金额计算。
 *
 * 供应商订单需要使用下单时快照的供货价；后端字段尚未返回时回退到原销售价，
 * 这样旧订单和后端灰度期间仍能正常展示。
 */
export interface SupplierOrderItemPriceData {
    supplyPrice?: number | string | null;
    supplySubtotal?: number | string | null;
    price?: number | string | null;
    quantity?: number | string | null;
    subtotal?: number | string | null;
}

/** 数值字段解析：空值与非数值统一视为缺失，交由调用方决定回退口径 */
function parseAmount(value: number | string | null | undefined): number | null {
    if (value === undefined || value === null || value === "") {
        return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

export function getSupplierOrderItemPrice(item: SupplierOrderItemPriceData | null | undefined): number {
    return parseAmount(item?.supplyPrice) ?? parseAmount(item?.price) ?? 0;
}

/**
 * 供货价小计取值顺序：
 * 1. supplySubtotal —— 供货价小计
 * 2. supplyPrice × quantity —— 有供货价快照时自行换算
 * 3. subtotal —— 无供货价快照时退回商品小计
 */
export function getSupplierOrderItemSubtotal(item: SupplierOrderItemPriceData | null | undefined): number {
    const supplySubtotal = parseAmount(item?.supplySubtotal);
    if (supplySubtotal !== null) {
        return supplySubtotal;
    }

    const supplyPrice = parseAmount(item?.supplyPrice);
    const quantity = parseAmount(item?.quantity) ?? 0;
    if (supplyPrice !== null) {
        return supplyPrice * quantity;
    }

    return parseAmount(item?.subtotal) ?? (parseAmount(item?.price) ?? 0) * quantity;
}

/** 售后展示金额按本次售后数量计算，不使用原购买数量。 */
export function getSupplierAftersalesAmount(items: (SupplierOrderItemPriceData & { number?: number | string | null })[] = []): number {
    const cents = items.reduce((sum, item) => {
        const number = Number(item.number ?? 0);
        const unitCents = Math.round(getSupplierOrderItemPrice(item) * 100);
        return sum + unitCents * (Number.isFinite(number) ? number : 0);
    }, 0);
    return cents / 100;
}
