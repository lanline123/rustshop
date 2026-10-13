import { computed, ref, type Ref, type ComputedRef } from "vue";
import { isOverseas } from "@/utils";

/** 商品详情呈现风格（与后台装修 A1 / A2 / A3 保持一致） */
export type ProductStyle = "A1" | "A2" | "A3";

export interface UseProductStyleOptions {
    /**
     * 后台装修配置的商详风格字段（与 Tigshop-Admin 对齐后传入）
     * 未配置时为空，按模块 type / 运行环境兜底
     */
    decorateStyle?: Ref<ProductStyle | string | undefined>;
    /**
     * 装修模块 type：productDetailA1 / A2 / A3
     * decorateStyle 缺失时用于反推风格
     */
    moduleType?: Ref<string | undefined>;
}

export interface UseProductStyleReturn {
    /** 当前商详风格 */
    productStyle: ComputedRef<ProductStyle>;
    /** 是否 vortmall 标准风 */
    isVortmallStyle: ComputedRef<boolean>;
    /** 是否 o2o 门店风 */
    isO2oStyle: ComputedRef<boolean>;
    /** 是否 overseas 卡片风 */
    isOverseasStyle: ComputedRef<boolean>;
}

/** 模块 type → 后台风格值映射（接口未回 decorateStyle 时用 type 反推） */
const MODULE_TYPE_STYLE_MAP: Record<string, ProductStyle> = {
    productDetailA1: "A1",
    productDetailA2: "A2",
    productDetailA3: "A3"
};

/**
 * 商品详情风格决策 Composable
 *
 * @description 统一商详三风格（A1 / A2 / A3）的选择入口：
 * 1. 优先读后台装修配置指定的风格（decorateStyle）；
 * 2. 其次用模块 type 反推（productDetailA1/A2/A3）；
 * 3. 再无配置时按运行环境兜底——跨境走 A3，其余走 A2 门店风（默认基座）。
 * 平台差异（#ifdef）与风格维度正交，不在此处理。
 */
export function useProductStyle(options: UseProductStyleOptions = {}): UseProductStyleReturn {
    const { decorateStyle = ref(undefined), moduleType = ref(undefined) } = options;

    const productStyle = computed<ProductStyle>(() => {
        const configured = decorateStyle.value;
        if (configured === "A1" || configured === "A2" || configured === "A3") {
            return configured;
        }
        const fromType = moduleType.value ? MODULE_TYPE_STYLE_MAP[moduleType.value] : undefined;
        if (fromType) return fromType;
        if (isOverseas()) return "A3";
        return "A2";
    });

    const isVortmallStyle = computed(() => productStyle.value === "A1");
    const isO2oStyle = computed(() => productStyle.value === "A2");
    const isOverseasStyle = computed(() => productStyle.value === "A3");

    return {
        productStyle,
        isVortmallStyle,
        isO2oStyle,
        isOverseasStyle
    };
}

export default useProductStyle;
