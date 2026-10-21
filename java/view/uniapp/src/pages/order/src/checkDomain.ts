import type {
    CheckoutShippingType,
    CheckoutShippingTypeMap,
    DeliveryOption,
    DeliveryPickupGroup,
    OrderSubmitPayload,
    ShopDeliveryDatum
} from "@/types/order/check";

export interface DeliveryCapabilities {
    hasStoreDelivery: boolean;
    hasPickupDelivery: boolean;
    hasLocalDelivery: boolean;
    pickupProducts: DeliveryPickupGroup[];
    shopDeliveryData: ShopDeliveryDatum[];
}

export interface PickupSelection {
    shopId: number;
    pickupId: number;
    pickupTitle: string;
}

interface CheckoutFormState {
    shippingType: CheckoutShippingTypeMap;
    addressId?: number;
    productReservationTimeId?: number;
    reservationDate?: string;
    deliveryType?: number;
}

interface CartDeliveryItem {
    shopId: number;
    noShipping: number;
}

export interface BuildOrderSubmitPayloadOptions {
    formState: CheckoutFormState;
    deliveryType: number;
    cartList: CartDeliveryItem[];
    isSubCard: boolean;
    isReserve: boolean;
    isCycleBuy: boolean;
    reserveInfo: {
        productReservationTimeId: number;
        reservationDate: string;
    };
    pickupTypeName: string;
    noShippingTypeName: string;
    now?: Date;
}

const asArray = <T>(value: T[] | null | undefined): T[] => (Array.isArray(value) ? [...value] : []);

const createEmptyDeliveryCapabilities = (): DeliveryCapabilities => ({
    hasStoreDelivery: false,
    hasPickupDelivery: false,
    hasLocalDelivery: false,
    pickupProducts: [],
    shopDeliveryData: []
});

const filterDeliveryCapabilities = (capabilities: DeliveryCapabilities, isAllowed: (deliveryType: number) => boolean): DeliveryCapabilities => {
    const allowStoreDelivery = isAllowed(1);
    const allowPickupDelivery = isAllowed(2);
    const allowLocalDelivery = isAllowed(3);

    return {
        hasStoreDelivery: allowStoreDelivery && capabilities.hasStoreDelivery,
        hasPickupDelivery: allowPickupDelivery && capabilities.hasPickupDelivery,
        hasLocalDelivery: allowLocalDelivery && capabilities.hasLocalDelivery,
        pickupProducts: allowPickupDelivery ? [...capabilities.pickupProducts] : [],
        shopDeliveryData: allowLocalDelivery ? [...capabilities.shopDeliveryData] : []
    };
};

/**
 * 根据最新配送选项生成完整能力快照。
 */
export const resolveDeliveryCapabilities = (
    deliveryOption: DeliveryOption | null | undefined,
    isAllowed: (deliveryType: number) => boolean = () => true
): DeliveryCapabilities => {
    return filterDeliveryCapabilities(
        {
            hasStoreDelivery: asArray(deliveryOption?.deliveryProducts).length > 0,
            hasPickupDelivery: asArray(deliveryOption?.pickupProducts).length > 0,
            hasLocalDelivery: asArray(deliveryOption?.shopDeliveryProducts).length > 0,
            pickupProducts: asArray(deliveryOption?.pickupProducts),
            shopDeliveryData: asArray(deliveryOption?.shopDeliveryData)
        },
        isAllowed
    );
};

const getExclusiveDeliveryType = (deliveryOptionType: number | null | undefined): number | undefined => {
    if (deliveryOptionType === 3) return 1;
    if (deliveryOptionType === 4) return 2;
    if (deliveryOptionType === 5) return 3;
    return undefined;
};

/**
 * 合并配送能力。完整查询替换全部能力，指定配送方式的查询只更新该方式。
 */
export const mergeDeliveryCapabilities = (
    previous: DeliveryCapabilities | null | undefined,
    deliveryOption: DeliveryOption | null | undefined,
    requestedDeliveryType: number | null | undefined,
    isAllowed: (deliveryType: number) => boolean = () => true
): DeliveryCapabilities => {
    const incoming = resolveDeliveryCapabilities(deliveryOption);

    if (requestedDeliveryType == null) {
        return filterDeliveryCapabilities(incoming, isAllowed);
    }

    const exclusiveDeliveryType = getExclusiveDeliveryType(deliveryOption?.deliveryType);
    if (exclusiveDeliveryType) {
        const exclusive = createEmptyDeliveryCapabilities();
        exclusive.hasStoreDelivery = exclusiveDeliveryType === 1;
        exclusive.hasPickupDelivery = exclusiveDeliveryType === 2;
        exclusive.hasLocalDelivery = exclusiveDeliveryType === 3;
        exclusive.pickupProducts = exclusiveDeliveryType === 2 ? incoming.pickupProducts : [];
        exclusive.shopDeliveryData = incoming.shopDeliveryData;
        return filterDeliveryCapabilities(exclusive, isAllowed);
    }

    const merged: DeliveryCapabilities = {
        ...(previous ?? createEmptyDeliveryCapabilities()),
        pickupProducts: [...(previous?.pickupProducts ?? [])],
        shopDeliveryData: incoming.shopDeliveryData
    };

    if (requestedDeliveryType === 1) {
        merged.hasStoreDelivery = incoming.hasStoreDelivery;
    } else if (requestedDeliveryType === 2) {
        merged.hasPickupDelivery = incoming.hasPickupDelivery;
        merged.pickupProducts = incoming.pickupProducts;
    } else if (requestedDeliveryType === 3) {
        merged.hasLocalDelivery = incoming.hasLocalDelivery;
    }

    return filterDeliveryCapabilities(merged, isAllowed);
};

/**
 * 保留仍然有效的自提点选择，失效时回到该门店首个可用自提点。
 */
export const reconcilePickupSelections = (
    pickupProducts: DeliveryPickupGroup[],
    currentSelections: PickupSelection[] | null | undefined
): PickupSelection[] => {
    const selections = Array.isArray(currentSelections) ? currentSelections : [];

    return pickupProducts.flatMap((group) => {
        const pickupPoints = asArray(group.pickups);
        const current = selections.find((selection) => selection.shopId === group.shopId);
        const selected = pickupPoints.find((pickup) => pickup.shopId === current?.pickupId) ?? pickupPoints[0];

        if (!selected) return [];

        return [
            {
                shopId: group.shopId,
                pickupId: selected.shopId,
                pickupTitle: selected.shopTitle
            }
        ];
    });
};

/**
 * 保留当前可用履约方式；不可用时按商城配送、自提、同城配送的顺序选择。
 */
export const resolveDeliveryType = (currentDeliveryType: number, capabilities: DeliveryCapabilities): number => {
    const availableDeliveryTypes = [
        capabilities.hasStoreDelivery ? 1 : 0,
        capabilities.hasPickupDelivery ? 2 : 0,
        capabilities.hasLocalDelivery ? 3 : 0
    ].filter((deliveryType) => deliveryType > 0);

    if (availableDeliveryTypes.length === 0 || availableDeliveryTypes.includes(currentDeliveryType)) {
        return currentDeliveryType;
    }

    return availableDeliveryTypes[0];
};

const clonePayloadValue = <T>(value: T): T => {
    if (Array.isArray(value)) {
        return value.map((item) => clonePayloadValue(item)) as T;
    }

    if (value && typeof value === "object") {
        const cloned: Record<string, unknown> = {};
        Object.entries(value as Record<string, unknown>).forEach(([key, item]) => {
            cloned[key] = clonePayloadValue(item);
        });
        return cloned as T;
    }

    return value;
};

const formatDateByOffset = (daysOffset: number, now: Date): string => {
    const date = new Date(now.getTime());
    date.setDate(date.getDate() + daysOffset);

    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const day = date.getDate().toString().padStart(2, "0");

    return `${year}-${month}-${day}`;
};

const pickupFields: Array<keyof CheckoutShippingType> = ["expectPickupTime", "pickupId", "pickupTitle", "pickupTime", "pickupDateType"];

const removePickupFields = (shippingInfo: CheckoutShippingType) => {
    pickupFields.forEach((field) => {
        delete shippingInfo[field];
    });
};

/**
 * 创建订单提交快照，不修改结算页长期状态。
 */
export const buildOrderSubmitPayload = (options: BuildOrderSubmitPayloadOptions): OrderSubmitPayload => {
    const { formState, deliveryType, cartList, isSubCard, isReserve, isCycleBuy, reserveInfo, pickupTypeName, noShippingTypeName, now = new Date() } = options;
    const payload = clonePayloadValue(formState) as OrderSubmitPayload;
    const shippingTypeEntries = Object.entries(payload.shippingType);

    if (deliveryType === 2) {
        delete payload.addressId;

        shippingTypeEntries.forEach(([, shippingInfo]) => {
            if (isSubCard) {
                removePickupFields(shippingInfo);
            } else if (shippingInfo.pickupTime) {
                const pickupDateType = shippingInfo.pickupDateType ?? 0;
                shippingInfo.expectPickupTime = `${formatDateByOffset(pickupDateType, now)} ${shippingInfo.pickupTime}`;
            }

            shippingInfo.typeName = pickupTypeName;
            shippingInfo.typeId = deliveryType;
        });
    } else if (deliveryType === 1) {
        shippingTypeEntries.forEach(([, shippingInfo]) => {
            removePickupFields(shippingInfo);
        });

        cartList.forEach((item) => {
            const shippingInfo = payload.shippingType[item.shopId];
            if (item.noShipping === 1 && shippingInfo) {
                shippingInfo.typeName = noShippingTypeName;
            }
        });
    } else if (deliveryType === 3) {
        payload.shippingType = {};
    }

    if (isReserve && !isSubCard) {
        payload.productReservationTimeId = reserveInfo.productReservationTimeId;
        if (isCycleBuy) {
            delete payload.reservationDate;
        } else {
            payload.reservationDate = reserveInfo.reservationDate;
        }
    } else if (!isSubCard) {
        delete payload.productReservationTimeId;
        delete payload.reservationDate;
    }


    payload.deliveryType = deliveryType;
    return payload;
};
