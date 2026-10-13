import { useWholesaleQuantity } from "@/hooks/useWholesaleQuantity";
import { urlFormat } from "@/utils/format";

/**
 * 卡片商品的最小入参（各卡片 product/item 类型不一，故只约束用到的字段）
 * @description 字段全可选，调用方按需提供
 */
type CardProduct = {
    /**
     * 批发首价；仅批发商命中商品时后端返回。
     * 用 `firstWholesalePrice != null` 判定卡片是否走批发态
     */
    firstWholesalePrice?: string | number | null;
    /** 列表商品所属销售门店；批发详情必须沿用该作用域 */
    shopId?: string | number | null;
    productSku?: unknown[];
    /**
     * 列表卡片起批量（类型严格 `int | null`）
     * `null` = 不能在卡片直接加购（多规格 / 配置缺失），应跳详情
     */
    wholesaleCardMoq?: number | null;
    /** 倍数购买的倍数；1=不限制 */
    wholesaleMultiple?: number;
    /** 列表计步器当前数量（带计步器的列表卡片用，如搜索/分类结果页） */
    number?: number;
    /** 限购数（0/未定义=不限购） */
    limitNumber?: number;
    /** 库存 */
    productStock?: number;
};

/**
 * 商品卡片（列表/推荐/装修模块）的购买行为辅助
 * @description
 * - 各方法在非批发场景下回退零售默认值，调用方可直接 import 使用（含模板属性绑定）。
 * - 批发态判定改为「字段存在即可」：后端在总开关关 / 非批发商 / 非平台商品时不返 firstWholesalePrice，
 *   前端无需再叠加 useWholesaleIdentity 判定，列表数据到位即可同步渲染（无身份异步竞态）。
 * - 卡片直接加购 / 跳详情：以 `wholesaleCardMoq != null` 为真理源，
 *   前端不再用 SKU 数自己判单规格——多规格 / 配置缺失的场景后端会直接返 null。
 */
export const useProductCardBuy = () => {
    const wq = useWholesaleQuantity();

    /**
     * 是否按批发渲染该卡片
     * @description 非批发卡片返回 false
     */
    const isWholesaleCard = (item?: CardProduct | null): boolean => {
        if (item?.firstWholesalePrice != null) return true;
        return false;
    };

    /**
     * 商品是否单规格（通用商品属性，与批发无关）
     * @description productSku 为空即无需选规格；批发态下「能否直接加购」请用 canCardBuyWholesale
     */
    const isSingleSpec = (item?: CardProduct | null): boolean => {
        return !item?.productSku || item.productSku.length === 0;
    };

    /**
     * 批发卡片能否在卡片上直接加购
     * @description
     * - `true` → 可直接调 addToCart，默认数量 = `wholesaleCardMoq`
     * - `false`（批发但 cardMoq=null：多规格 / 配置缺失） → 卡片"加购"按钮应跳详情走 buyRule
     * - 非批发卡片 → false（按零售路径，由调用方维持原有行为）
     */
    const canCardBuyWholesale = (item?: CardProduct | null): boolean => {
        return isWholesaleCard(item) && item?.wholesaleCardMoq != null;
        return false;
    };

    /**
     * 批发起批量（仅在 canCardBuyWholesale=true 时调用有效；为安全起见 null 回退 1）
     */
    const wholesaleMoq = (item?: CardProduct | null): number => {
        const v = item?.wholesaleCardMoq;
        return typeof v === "number" && v > 0 ? v : 1;
    };

    /**
     * 卡片默认加购数量 / 计步器初值（起购数量）
     * @description 以 `canCardBuyWholesale`（wholesaleCardMoq != null）为真理源，按 起批量 + 倍数 计算（见 useWholesaleQuantity.startQtyOf）：
     * - 同时有起批量与倍数：向上取到 ≥ 起批量的最小倍数
     * - 仅起批量：起批量；仅倍数：倍数；都没有：1
     * - 不可卡片直加（cardMoq=null）/ 零售 → 1
     */
    const cardBuyNumber = (item?: CardProduct | null): number => {
        if (canCardBuyWholesale(item)) {
            return wq.startQtyOf(item?.wholesaleCardMoq, item?.wholesaleMultiple);
        }
        return 1;
    };

    /**
     * 卡片价格取值
     * @description 批发卡片用 firstWholesalePrice 直接替换零售字段；不加任何「批发价」标签——价格位永远一个数，按身份切换值
     * @param item 卡片商品（任意形状，至少含 firstWholesalePrice）
     * @param fallback 零售价 fallback（不同卡片用的字段不同：productPrice / price，由调用方传入）
     * @returns 用于 FormatPrice :model-value 的字符串；非批发态返回 fallback
     */
    const cardPrice = (item?: CardProduct | null, fallback?: string | number | null): string | undefined => {
        if (isWholesaleCard(item) && item?.firstWholesalePrice != null) {
            return String(item.firstWholesalePrice);
        }
        if (fallback == null) return undefined;
        return String(fallback);
    };

    /**
     * 商品卡片详情链接
     * @description 返回批发首价时强制携带该商品的 shopId，避免进入默认门店后丢失批发报价；零售商品保留调用方原路由参数。
     */
    const cardProductLink = (
        item: CardProduct | null | undefined,
        params: Exclude<Parameters<typeof urlFormat>[0], string | null | undefined>
    ): string => {
        const routeParams =
            item?.firstWholesalePrice != null && item?.shopId != null
                ? { ...params, shopId: item.shopId }
                : params;
        return urlFormat(routeParams);
    };

    /**
     * 该卡片是否走批发计步器逻辑
     * @description 与 canCardBuyWholesale 同口径：wholesaleCardMoq != null 即走倍数步长 + 起购数量下限
     */
    const isWholesaleBuyRow = (item?: CardProduct | null): boolean => canCardBuyWholesale(item);

    /**
     * 行数量上限：限购与库存取小（均 >0 才计入），无则不限制（null）
     */
    const cardQtyMax = (item?: CardProduct | null): number | null => {
        const lims: number[] = [];
        const lim = item?.limitNumber ?? 0;
        if (lim > 0) lims.push(lim);
        const stock = item?.productStock ?? 0;
        if (stock > 0) lims.push(stock);
        return lims.length ? Math.min(...lims) : null;
    };

    /**
     * 计步器点「+」：先吸附后按倍数步进、库存/限购封顶
     * @returns value=目标数量；不可再加时 value=null，reason 区分 limit/stock 供调用方提示
     */
    const cardPlus = (item: CardProduct): { value: number | null; reason?: "limit" | "stock" } => {
        const cur = item.number ?? 0;
        const target = wq.plusTarget(cur, item.wholesaleMultiple, cardQtyMax(item));
        if (target == null) {
            const lim = item.limitNumber ?? 0;
            return { value: null, reason: lim > 0 && lim <= cur ? "limit" : "stock" };
        }
        return { value: target };
    };

    /** 计步器点「−」：先吸附再按倍数递减，下限夹到起购数量（不能再往下减） */
    const cardMinus = (item: CardProduct): number => Math.max(cardBuyNumber(item), wq.minusTarget(item.number ?? 0, item.wholesaleMultiple));

    /** 手输失焦：就近吸附到合法倍数并夹到 [起购数量, 上限] */
    const cardNormalize = (item: CardProduct, raw: number): number => Math.max(cardBuyNumber(item), wq.normalize(raw, item.wholesaleMultiple, cardQtyMax(item)));

    /** 减号是否禁用：已到起购数量下限 */
    const cardDecDisabled = (item: CardProduct): boolean => (item.number ?? 0) <= cardBuyNumber(item);

    return {
        isWholesaleCard,
        isSingleSpec,
        canCardBuyWholesale,
        wholesaleMoq,
        cardBuyNumber,
        cardPrice,
        cardProductLink,
        isWholesaleBuyRow,
        cardQtyMax,
        cardPlus,
        cardMinus,
        cardNormalize,
        cardDecDisabled
    };
};
