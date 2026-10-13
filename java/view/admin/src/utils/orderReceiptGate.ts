/**
 * 订单确认收货入口开关
 *
 * 收货由买家在前台完成或由系统按「发货后自动确认收货时间」自动完成，
 * 后台各端不提供代确认入口。置为 true 即恢复全部「确认收货 / 确认已收货」按钮。
 */
export const ORDER_RECEIPT_CONFIRM_ENABLED: boolean = false;

/**
 * 列表中「确认收货 / 完成配送」为同一按钮双文案，
 * 同城配送（shippingTypeId = 3）显示「完成配送」，不受上述开关影响。
 */
export const canShowOrderReceiptEntry = (shippingTypeId?: number | string | null): boolean => {
    return ORDER_RECEIPT_CONFIRM_ENABLED || Number(shippingTypeId) === 3;
};
