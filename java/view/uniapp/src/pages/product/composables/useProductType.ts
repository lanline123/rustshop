import { computed, ref, type Ref, type ComputedRef } from "vue";
import { isReservationProductType, isSubCardReservationProduct as checkIsSubCardReservationProduct } from "@/utils/productSoldOut";
import type { ProductItem } from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";

/**
 * 商品类型判断 Composable
 *
 * @description 从商品详情派生的类型判断（预约 / 虚拟 / 付费内容 / 预售 / 次卡预约）
 * 判断逻辑与 pages/product/index.vue 保持一致，纯派生、无副作用
 */
export interface UseProductTypeOptions {
    /** 商品详情数据 */
    product: Ref<ProductItem>;
    /** 是否次卡（默认 false） */
    isSubCard?: Ref<boolean>;
}

export interface UseProductTypeReturn {
    /** 是否预约商品（productType 5 / 6） */
    isReservation: ComputedRef<boolean>;
    /** 是否虚拟 / 卡密商品（productType 2 / 3） */
    isVirtual: ComputedRef<boolean>;
    /** 是否付费内容商品（productType 4） */
    isPaidContent: ComputedRef<boolean>;
    /** 是否预售商品（saleMode > 0） */
    isPresale: ComputedRef<boolean>;
    /** 预售信息（saleMode > 0 时存在） */
    productPresale: ComputedRef<PresaleInfo | null>;
    /** 是否次卡预约商品 */
    isSubCardReservationProduct: ComputedRef<boolean>;
    /** 预约方式文案 */
    reserveModeText: ComputedRef<string>;
    /** 预约提示文案 */
    reserveTipText: ComputedRef<string>;
}

export function useProductType(options: UseProductTypeOptions): UseProductTypeReturn {
    const { product, isSubCard = ref(false) } = options;

    const productPresale = computed<PresaleInfo | null>(() => {
        const info = (product.value as any)?.presaleInfo;
        const saleMode = Number((product.value as any)?.saleMode ?? info?.saleMode ?? 0);
        if (saleMode > 0 && info) return info as PresaleInfo;
        return null;
    });

    const isPresale = computed(() => Number(productPresale.value?.saleMode ?? 0) > 0);

    const isReservation = computed(() => {
        const productType = Number(product.value?.productType ?? 0);
        const flowType = Number(product.value?.flowType ?? 0);
        return isReservationProductType(productType) || [9, 10].includes(flowType);
    });

    const isVirtual = computed(() => [2, 3].includes(Number(product.value?.productType ?? 0)));

    const isPaidContent = computed(() => Number(product.value?.productType ?? 0) === 4);

    const isSubCardReservationProduct = computed(() => checkIsSubCardReservationProduct(product.value as Record<string, unknown>, isSubCard.value));

    const reserveModeText = computed(() => {
        const flowType = Number(product.value?.flowType ?? 0);
        if (flowType === 9) return "到店服务";
        if (flowType === 10) return "上门服务";
        const productType = Number(product.value?.productType ?? 0);
        if (productType === 5) return "到店服务";
        if (productType === 6) return "上门服务";
        return "预约服务";
    });

    const reserveTipText = computed(() => {
        if (reserveModeText.value === "上门服务") return "下单时需选择预约时间段，建议提前与商家沟通上门安排";
        if (reserveModeText.value === "到店服务") return "下单时需选择预约时间段，请按预约时间到店消费";
        return "下单时需选择预约时间段";
    });

    return {
        isReservation,
        isVirtual,
        isPaidContent,
        isPresale,
        productPresale,
        isSubCardReservationProduct,
        reserveModeText,
        reserveTipText
    };
}

export default useProductType;
