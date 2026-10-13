import { message } from "ant-design-vue";
import { getWholesaleBuyRule } from "@/api/wholesale/product";
import { addToCart, getProductAmount } from "@/api/product/product";
import type { ProductAmountItem } from "@/types/product/product.d";
import { asyncGetCartCount } from "@/api/cart";
import { useCartStore } from "@/store/cart";
import { useWholesaleStore } from "@/store/wholesale";
import { useWholesaleIdentity } from "@/hooks/useWholesaleIdentity";
import type { ProductItem, SkuList, AttrList as ProductAttrList, Spe, WholesalePriceTier } from "@/types/product/product.d";
import type { WholesaleBuyRule } from "@/types/wholesale/buyRule";
import type { WholesaleSpecDimension, WholesaleSkuPick, WholesaleQtyChangePayload } from "@/types/wholesale/sku";
import type { WholesaleCartTip } from "@/types/wholesale/cart";
import { WHOLESALE_MOQ_NOT_MET_HINT } from "@/utils/wholesale";
import {
    buildWholesaleAddToCartPayload,
    buildWholesaleAmountPayload,
    createLatestPreviewRunner,
    createLatestRequestRunner,
    normalizeWholesaleAmountText,
    resetWholesaleSelectionState,
    refreshWholesaleIdentityAfterError
} from "@/utils/wholesaleContract";

/**
 * 批发下单编排 composable
 * @description
 * - 从零售详情页（ProductInfo.vue）剥离的批发态业务逻辑宿主
 * - 负责：起批规则拉取 / 价格阶梯（消费后端 wholesalePriceList）/ N 维规格派生 / 本地校验(moq/倍数) / 加车
 * - 加车策略：同商品多 SKU 只调用一次普通 addToCart，并携带 skuItem；不调用跨商品 addBatch
 * - 入参用 getter，保持对 props 的响应式追踪
 */
interface UseWholesaleOrderOptions {
    /** 商品 ID */
    productId: () => number;
    /** 商品主体 */
    product: () => ProductItem;
    /** SKU 列表 */
    skuList: () => SkuList[];
    /** 规格属性（含 spe[] 维度） */
    attrList: () => ProductAttrList;
    /** 详情接口下发的批发阶梯价表 */
    wholesalePriceList: () => WholesalePriceTier[] | null;
}

/**
 * 横向价格阶梯条单档
 * @description
 * - 后端 wholesalePriceList 是「每个 SKU × 每个等级」的扁平阶梯，前端按 tierMinQty 去重成一份阶梯条
 * - 同一档跨 SKU 价不同时取 min/max 形成区间；一级阶梯（仅 1 档）的区间只是它的特例
 */
export interface WholesaleLadder {
    /** 该档起量门槛 */
    tierMinQty: number;
    /** 数量区间文案（如「1件起批」/「2-5件」/「≥6件」） */
    qtyText: string;
    /** 该档跨 SKU 最低展示价（后端原始 displayPrice，避免精度漂移）；一级阶梯时即「阶梯价」 */
    minPrice: string;
    /** 该档跨 SKU 最高展示价；一级阶梯时为「不满足阶梯时的零售价」 */
    maxPrice: string;
    /** min !== max（需展示为区间） */
    isRange: boolean;
}

export const useWholesaleOrder = (opts: UseWholesaleOrderOptions) => {
    const { t } = useI18n();
    const cartStore = useCartStore();
    const wholesaleStore = useWholesaleStore();
    const wholesaleIdentity = useWholesaleIdentity();

    const productId = computed(() => opts.productId());
    const product = computed(() => opts.product());
    const effectiveShopId = computed(() => Number(product.value?.shopId ?? 0));
    const skuList = computed(() => opts.skuList());
    const attrList = computed(() => opts.attrList());
    const priceTiers = computed<WholesalePriceTier[]>(() => opts.wholesalePriceList() ?? []);

    /** 末维选购器组件实例（加车成功后清空选数） */
    const skuSelectorRef = ref<any>(null);

    /** 起批规则 */
    const buyRule = ref<WholesaleBuyRule | null>(null);

    /** 前置维度选择态：specId -> optionId（由 SkuSelector 上抛同步） */
    const frontSelection = ref<Record<number, number>>({});
    /** 全局选数 */
    const pickedQty = ref<Record<number, number>>({});
    const pickedTotal = ref(0);
    const pickedKinds = ref(0);

    /** 后端实时预览金额（getProductAmount）；批发命中后 total 即批发口径，前端不自算 */
    const productAmount = ref<ProductAmountItem>({ total: "0", count: 0 });

    const refreshIdentityAfterError = async (error: any) => {
        try {
            await refreshWholesaleIdentityAfterError(error, wholesaleIdentity);
        } catch (refreshError) {
            console.error("[wholesale] identity refresh failed", refreshError);
        }
    };

    const amountPreview = createLatestPreviewRunner<Record<string, unknown>, ProductAmountItem>(
        (payload) => getProductAmount(payload),
        (state) => {
            if (state.status === "success") {
                productAmount.value = state.value ?? { total: "0", count: 0 };
            }
            if (state.status === "error") {
                void refreshIdentityAfterError(state.error);
            }
        }
    );

    const buyRuleRequest = createLatestRequestRunner(
        (scope: { productId: number; shopId: number }) => getWholesaleBuyRule(scope.productId, scope.shopId),
        (rule) => {
            buyRule.value = rule;
        },
        async (error) => {
            console.error("[wholesale] load buyRule failed", error);
            buyRule.value = null;
            await refreshIdentityAfterError(error);
        }
    );

    const loadingAdd = ref(false);
    const loadingBuy = ref(false);

    const formatPrice = (price: string | number | undefined) => {
        if (price === undefined || price === null || price === "") return "¥--";
        const n = typeof price === "number" ? price : Number(price);
        if (Number.isNaN(n)) return String(price);
        return `¥${n.toFixed(2)}`;
    };

    /**
     * 派生 N 维规格维度结构（前 N-1 维 + 末维）
     * @description 从 attrList.spe[] 提取：每个 Spe 即一个维度，AttrList 是其选项
     */
    const dimensions = computed<WholesaleSpecDimension[]>(() => {
        const speList = (attrList.value?.spe ?? []) as unknown as Spe[];
        // 注意：spe.attrName 实为规格名字符串（如「尺码」「颜色」），不能 Number() 当 ID（会得到 NaN）。
        // 维度唯一标识改用「定义顺序索引」做稳定 key；与 SKU 的匹配走 specName + 选项值（见 lastDimPicks）。
        return speList.map((spe, idx) => {
            const options = (spe.attrList || []).map((opt: any) => ({
                optionId: Number(opt.attributesId),
                label: String(opt.attrValue ?? ""),
                image: opt.attrPicThumb || opt.attrPic || undefined
            }));
            const hasImage = options.some((o) => !!o.image);
            return {
                specId: idx,
                specName: String(spe.attrName ?? ""),
                hasImage,
                options
            } as WholesaleSpecDimension;
        });
    });

    const buildPick = (sku: SkuList): WholesaleSkuPick => {
        // 末维行名只取末维自身的选项值（如「天空蓝」），不叠加前置已选维度（前置维度已在上方选择区体现）
        const lastDim = dimensions.value[dimensions.value.length - 1];
        const skuData = sku.skuData || [];
        let skuName = "";
        if (lastDim) {
            const seg = skuData.find((sd) => String(sd.name) === lastDim.specName);
            if (seg) skuName = String(seg.value);
        }
        // 兜底：拿不到末维分段时退回完整 skuValue；仍为空（单 SKU 无规格）时回退商品名，与统一规格合成行保持一致
        if (!skuName) skuName = (sku.skuValue || "").replace(/\|/g, " / ");
        if (!skuName) skuName = product.value?.productName || t("默认");
        return {
            skuId: sku.skuId,
            skuName,
            thumb: undefined,
            unitPriceText: formatPrice(sku.skuPrice || sku.price),
            stock: sku.skuStock ?? sku.stock ?? 0,
            stockText: (sku.skuStock ?? sku.stock ?? 0) > 0 ? t("库存充足") : t("无货")
        };
    };

    /**
     * 当前前置维度选择下，末维可选数 SKU 行
     * @description
     * - 统一规格（无 SKU 记录 skuList=[]）：合成 1 行默认行（skuId=0，用商品级价格/库存）
     * - 单 SKU 商品：直接返回 skuList[0]
     * - N=1（仅末维）：返回全部 SKU
     * - N>1：用 frontSelection 过滤 SKU（匹配前置维度的所有选项）
     */
    const lastDimPicks = computed<WholesaleSkuPick[]>(() => {
        if (skuList.value.length === 0) {
            // 统一规格：无 SKU 记录，价格挂在 skuId=0（通用行），合成单行使其可填数量
            const p = product.value;
            const stock = Number(p?.productStock ?? 0);
            return [
                {
                    skuId: 0,
                    skuName: p?.productName || t("默认"),
                    thumb: undefined,
                    unitPriceText: formatPrice(p?.productPrice),
                    stock,
                    stockText: stock > 0 ? t("库存充足") : t("无货")
                }
            ];
        }
        if (dimensions.value.length <= 1) {
            return skuList.value.map((sku) => buildPick(sku));
        }
        const frontDims = dimensions.value.slice(0, -1);
        return skuList.value
            .filter((sku) => {
                const skuData = sku.skuData || [];
                return frontDims.every((dim) => {
                    // 当前选中项；未显式选择时回退到该维度首项（与组件默认高亮一致，避免初始/选后列表为空）
                    const selectedOptId = frontSelection.value[dim.specId] ?? dim.options[0]?.optionId;
                    if (!selectedOptId) return true;
                    const opt = dim.options.find((o) => o.optionId === selectedOptId);
                    if (!opt) return false;
                    // 用「规格名 + 选项值」双重匹配，避免不同维度同值串台
                    return skuData.some((sd) => String(sd.name) === dim.specName && String(sd.value) === String(opt.label));
                });
            })
            .map((sku) => buildPick(sku));
    });

    /** 当前驱动阶梯展示的 SKU（默认前置选中组首个；点击某 SKU 加购后切到该 SKU） */
    const activeSkuId = ref<number | null>(null);

    /** 实际用于展示阶梯的 SKU：activeSkuId 在当前可见组内则用之，否则回退到组内首个（切换前置规格自动复位为首个） */
    const displaySkuId = computed<number | null>(() => {
        const picks = lastDimPicks.value;
        if (!picks.length) return null;
        if (activeSkuId.value != null && picks.some((p) => p.skuId === activeSkuId.value)) return activeSkuId.value;
        return picks[0].skuId;
    });

    /** 当前前置选中组的可见 SKU id 集合（多档阶梯展示联动用） */
    const visibleSkuIds = computed<Set<number>>(() => new Set(lastDimPicks.value.map((p) => p.skuId)));

    /** 商品全部 SKU id（多规格单阶梯区间在此全集内求 min~max，不随前置规格变化） */
    const productSkuIds = computed<number[]>(() =>
        skuList.value.map((s) => s.skuId).filter((id): id is number => id != null && id !== 0)
    );

    /** 商品是否多规格（用于多档阶梯首档起批文案 / 单档区间判定） */
    const isMultiSpec = computed(() => skuList.value.length > 1);

    /** 当前用户批发等级 ID（专享价优先用之，否则回退通用价 gradeId=0） */
    const currentGradeId = computed(() => Number(wholesaleStore.identity.gradeId || 0));

    /**
     * 代表性起批量（用于价格区"N 件起批"展示，数据源 buyRule）
     * - SPU 维度：取整体起订量 moq
     * - SKU 维度：取各 SKU 起订量的最小值（>0）作为代表值
     * - 未配置/无有效值返回 0（由阶梯首档回退到 tierMinQty）
     */
    const moq = computed(() => {
        const r = buyRule.value;
        if (!r || !r.configured) return 0;
        // 后端无论 SPU/SKU 维度都在 moq 字段下发代表性起批量，优先取用
        if (r.moq && r.moq > 0) return r.moq;
        // 兜底：moq 缺省时按各 SKU 起订量最小值（>0）作为代表值
        const moqs = (r.skuRules || []).map((s) => s.moqSku || 0).filter((n) => n > 0);
        return moqs.length ? Math.min(...moqs) : 0;
    });

    /**
     * 阶梯数量文案
     * - 第一级：已设起批量 → 「{moq}件起批」；未设 → 正常件数（单档「{qty}件」/ 多档「a-b件」）
     * - 中间档：「a-b件」；末档：「≥N件」
     * - 区间展示（单档多规格）同样取第一级文案，跟在区间价后展示
     */
    const buildLadderQtyText = (idx: number, qtys: number[]): string => {
        const qty = qtys[idx];
        const isFirst = idx === 0;
        const isLast = idx === qtys.length - 1;
        if (isFirst) {
            const m = moq.value;
            if (m > 0) return t("{0}件起批", [m]);
            if (isLast) return t("{0}件", [qty]);
            const upper = qtys[idx + 1] - 1;
            return upper > qty ? t("{0}-{1}件", [qty, upper]) : t("{0}件", [qty]);
        }
        if (isLast) return t("≥{0}件", [qty]);
        const upper = qtys[idx + 1] - 1;
        return upper > qty ? t("{0}-{1}件", [qty, upper]) : t("{0}件", [qty]);
    };

    /** 等级过滤：专享价优先，回退通用价，再兜底全部（避免数据异常时完全不显示） */
    const filterByGrade = (rows: typeof priceTiers.value): typeof priceTiers.value => {
        const gid = currentGradeId.value;
        let graded = gid > 0 ? rows.filter((t) => t.gradeId === gid) : [];
        if (!graded.length) graded = rows.filter((t) => t.gradeId === 0);
        if (!graded.length) graded = rows.slice();
        return graded;
    };

    /** 批发定价模式：1=混批（按全规格总数匹配阶梯）/ 2=SKU 级独立 / null=未配置 */
    const wholesalePriceMode = computed<number | null>(() => {
        const m = (product.value as ProductItem & { wholesalePriceMode?: number | null })?.wholesalePriceMode;
        return m == null ? null : Number(m);
    });

    /**
     * 末维行单价：按当前数量命中的阶梯批发价（随量联动）
     * @description
     * - 取该 SKU 阶梯行（含通用行 skuId=null/0），等级过滤 + 同档去重取最低，按 tierMinQty 升序
     * - qty<=0 → 首档批发价；qty>0 → 命中「tierMinQty ≤ qty 的最大档」（不足首档门槛仍取首档）
     * - 无阶梯数据 → 返回空串（调用方回退零售价兜底）
     * - 混批/SKU 级由调用方决定传入的 qty（混批传全规格总数，SKU 级传本行数量）
     */
    const resolveRowWholesalePrice = (skuId: number, qty: number): string => {
        const rows = filterByGrade(priceTiers.value.filter((t) => t.skuId == null || t.skuId === 0 || t.skuId === skuId));
        const byQty = new Map<number, string>();
        for (const t of rows) {
            if (t.tierMinQty == null || t.displayPrice == null) continue;
            const n = Number(t.displayPrice);
            if (Number.isNaN(n)) continue;
            const exist = byQty.get(t.tierMinQty);
            if (exist == null || n < Number(exist)) byQty.set(t.tierMinQty, t.displayPrice);
        }
        const qtys = Array.from(byQty.keys()).sort((a, b) => a - b);
        if (!qtys.length) return "";
        let hitQty = qtys[0];
        if (qty > 0) {
            for (const q of qtys) {
                if (qty >= q) hitQty = q;
            }
        }
        return formatPrice(byQty.get(hitQty)!);
    };

    /**
     * 横向价格阶梯条
     * @description
     * - 多档：仅展示 displaySkuId 这一个 SKU 的阶梯（含通用行 skuId=null/0），默认前置组首个、加购某 SKU 后切到该 SKU；每档取最低批发价
     * - 单档 + 多规格：商品级每 SKU 恰好一档 → 跨全商品 SKU 取 min~max（不限 tierMinQty、不随前置规格变化）；价格相同则单价
     * - 单档 + 单规格：单价
     * - 数量文案见 buildLadderQtyText
     */
    const ladders = computed<WholesaleLadder[]>(() => {
        const all = priceTiers.value;
        if (!all.length) return [];
        const multiSpec = isMultiSpec.value;
        // 当前展示 SKU 的阶梯（驱动多档展示）
        const sid = displaySkuId.value;
        const graded = filterByGrade(all.filter((t) => t.skuId == null || t.skuId === 0 || t.skuId === sid));
        const byQty = new Map<number, string>();
        for (const t of graded) {
            if (t.tierMinQty == null || t.displayPrice == null) continue;
            const n = Number(t.displayPrice);
            if (Number.isNaN(n)) continue;
            const exist = byQty.get(t.tierMinQty);
            if (exist == null || n < Number(exist)) byQty.set(t.tierMinQty, t.displayPrice);
        }
        const qtys = Array.from(byQty.keys()).sort((a, b) => a - b);
        if (!qtys.length) return [];

        // 单档 + 多规格：商品级每 SKU 恰好一档 → 跨全商品 SKU 取 min~max（不限 tierMinQty、不随前置规格变化）
        if (qtys.length === 1 && multiSpec) {
            const qty = qtys[0];
            const pSkuIds = productSkuIds.value;
            const allGraded = filterByGrade(all);
            const productSkuTierRows = allGraded.filter(
                (t) =>
                    t.skuId != null &&
                    t.skuId !== 0 &&
                    pSkuIds.includes(t.skuId as number) &&
                    t.tierMinQty != null &&
                    t.displayPrice != null
            );
            const tierCountBySku = new Map<number, number>();
            for (const t of productSkuTierRows) {
                const skuId = t.skuId as number;
                tierCountBySku.set(skuId, (tierCountBySku.get(skuId) || 0) + 1);
            }
            const isProductSingleTier = pSkuIds.length > 0 && pSkuIds.every((skuId) => tierCountBySku.get(skuId) === 1);

            let minStr = byQty.get(qty)!;
            let minN = Number(minStr);
            let maxStr = minStr;
            let maxN = minN;
            const visible = visibleSkuIds.value;
            const rowsForRange = isProductSingleTier
                ? productSkuTierRows
                : filterByGrade(all.filter((t) => t.skuId == null || t.skuId === 0 || visible.has(t.skuId as number))).filter(
                      (t) => t.tierMinQty === qty && t.displayPrice != null && !Number.isNaN(Number(t.displayPrice))
                  );
            for (const t of rowsForRange) {
                const n = Number(t.displayPrice);
                if (Number.isNaN(n)) continue;
                if (n < minN) {
                    minN = n;
                    minStr = t.displayPrice as string;
                }
                if (n > maxN) {
                    maxN = n;
                    maxStr = t.displayPrice as string;
                }
            }
            const isRange = maxN > minN;
            return [
                {
                    tierMinQty: qty,
                    qtyText: buildLadderQtyText(0, qtys),
                    minPrice: minStr,
                    maxPrice: isRange ? maxStr : minStr,
                    isRange
                }
            ];
        }

        // 多档 / 单规格单档：单 SKU 单价
        return qtys.map((qty, idx) => {
            const price = byQty.get(qty)!;
            return {
                tierMinQty: qty,
                qtyText: buildLadderQtyText(idx, qtys),
                minPrice: price,
                maxPrice: price,
                isRange: false
            };
        });
    });

    /**
     * 商品总金额原始值：取后端 getProductAmount 预览口径（批发命中即批发价小计），前端不自算
     * - 交给 FormatPrice 渲染（统一币种符号），未选/后端未回有效值时回退 0
     */
    const totalAmountRaw = computed<string>(() => {
        if (pickedTotal.value <= 0) return "0";
        const tot = productAmount.value?.total;
        if (tot == null || tot === "") return "";
        return normalizeWholesaleAmountText(tot);
    });
    /** 金额始终为预览/计算口径（非交易点） */
    const isAmountEstimated = computed(() => true);

    /**
     * 聚合提示对象
     * - SPU 维度（dimension=1）：合计数量 vs moq
     * - SKU 维度（dimension=2）：取首个 PENDING 行作为聚合主提示（购物车里再按行展示）
     */
    const aggregateTip = computed<WholesaleCartTip | null>(() => {
        if (!buyRule.value || !buyRule.value.configured) return null;
        if (pickedTotal.value === 0) return null;
        if (buyRule.value.dimension === 1) {
            const moq = buyRule.value.moq || 0;
            const gap = Math.max(0, moq - pickedTotal.value);
            return {
                productId: productId.value,
                dimension: 1,
                currentQty: pickedTotal.value,
                minOrderQty: moq,
                gap,
                status: gap > 0 ? "PENDING" : "OK",
                message: gap > 0 ? t(WHOLESALE_MOQ_NOT_MET_HINT) : t("已满足起订要求")
            };
        }
        const ruleMap = new Map((buyRule.value.skuRules || []).map((x) => [x.skuId, x]));
        const repMoq = buyRule.value.moq || 0;
        for (const [skuIdStr, q] of Object.entries(pickedQty.value)) {
            const qty = q || 0;
            if (qty === 0) continue;
            const skuId = Number(skuIdStr);
            const rule = ruleMap.get(skuId);
            // 每规格起订量：优先该 SKU 配置，缺省回退到代表性 moq
            const moq = rule?.moqSku && rule.moqSku > 0 ? rule.moqSku : repMoq;
            const mul = Math.max(1, rule?.multiple || 1);
            if (qty < moq) {
                return {
                    productId: productId.value,
                    skuId,
                    dimension: 2,
                    currentQty: qty,
                    minOrderQty: moq,
                    gap: moq - qty,
                    step: mul > 1 ? mul : undefined,
                    status: "PENDING",
                    message: t(WHOLESALE_MOQ_NOT_MET_HINT)
                };
            }
            // 倍数口径：该 SKU 数量需为 multiple 的整数倍（与起批规则/后端一致，不以 moq 为基数）
            if (mul > 1 && qty % mul !== 0) {
                return {
                    productId: productId.value,
                    skuId,
                    dimension: 2,
                    currentQty: qty,
                    minOrderQty: moq,
                    gap: 0,
                    step: mul,
                    status: "PENDING",
                    message: t("请按 {0} 件倍数购买", [mul])
                };
            }
        }
        return {
            productId: productId.value,
            dimension: 2,
            currentQty: pickedTotal.value,
            minOrderQty: 0,
            gap: 0,
            status: "OK",
            message: t("已满足起订要求")
        };
    });

    const loadBuyRule = () => buyRuleRequest.run({ productId: productId.value, shopId: effectiveShopId.value });

    const onFrontSelect = (_specId: number, _optionId: number, selection: Record<number, number>) => {
        frontSelection.value = { ...selection };
    };

    const onQtyChange = (payload: WholesaleQtyChangePayload) => {
        pickedQty.value = payload.pickedQty;
        pickedKinds.value = payload.pickedKinds;
        pickedTotal.value = payload.pickedTotal;
        // 加购某 SKU → 阶梯条切换为该 SKU 的价格阶梯
        if (payload.changedSkuId != null) activeSkuId.value = payload.changedSkuId;
        // 选数变化 → 防抖拉取后端实时金额
        scheduleFetchAmount();
    };

    /**
     * 本地校验
     */
    const validateLocal = (): { ok: boolean; reason?: string } => {
        if (pickedTotal.value === 0) return { ok: false, reason: t("请选择购买数量") };
        const r = buyRule.value;
        if (!r || !r.configured) return { ok: true };
        const ruleMap = new Map((r.skuRules || []).map((x) => [x.skuId, x]));
        const repMoq = r.moq || 0;
        // SPU 维度：合计数量需达整体起订量
        if (r.dimension === 1) {
            if (pickedTotal.value < repMoq) return { ok: false, reason: t(WHOLESALE_MOQ_NOT_MET_HINT) };
        }
        // 逐个有量规格校验（遍历已选 SKU，兼容后端只下发 moq、不下发 skuRules 的情况）
        for (const [skuIdStr, q] of Object.entries(pickedQty.value)) {
            const qty = q || 0;
            if (qty === 0) continue;
            const rule = ruleMap.get(Number(skuIdStr));
            // SKU 维度每规格起订量：优先该 SKU 配置，缺省回退到代表性 moq
            const moqSku = r.dimension === 2 ? (rule?.moqSku && rule.moqSku > 0 ? rule.moqSku : repMoq) : 0;
            if (r.dimension === 2 && qty < moqSku) {
                return { ok: false, reason: t(WHOLESALE_MOQ_NOT_MET_HINT) };
            }
            const mul = Math.max(1, rule?.multiple || 1);
            // 倍数口径：该 SKU 数量需为 multiple 的整数倍（与起批规则/后端一致，不以 moq 为基数）
            if (mul > 1 && qty % mul !== 0) {
                return { ok: false, reason: t("请按 {0} 件倍数购买", [mul]) };
            }
        }
        return { ok: true };
    };

    interface PickedItem {
        skuId: number;
        quantity: number;
    }
    const buildPickedItems = (): PickedItem[] => {
        const items: PickedItem[] = [];
        for (const [skuIdStr, qty] of Object.entries(pickedQty.value)) {
            if (qty > 0) items.push({ skuId: Number(skuIdStr), quantity: qty });
        }
        return items;
    };

    /**
     * 实时预览金额（getProductAmount）
     * - 入参复用批量 skuItem，由后端按身份返回批发口径 total / wholesale / hint，前端不自算
     * - 100ms 防抖：连续点 +/- 时只发最后一次请求，规避频繁请求
     */
    let amountTimer: ReturnType<typeof setTimeout> | null = null;
    const fetchProductAmount = async () => {
        const items = buildPickedItems();
        if (items.length === 0) {
            amountPreview.invalidate();
            return;
        }
        try {
            await amountPreview.run(buildWholesaleAmountPayload(productId.value, effectiveShopId.value, items));
        } catch (err) {
            console.error("[wholesale] getProductAmount failed", err);
        }
    };
    const scheduleFetchAmount = () => {
        if (amountTimer) clearTimeout(amountTimer);
        amountPreview.invalidate();
        amountTimer = setTimeout(() => {
            amountTimer = null;
            void fetchProductAmount();
        }, 200);
    };

    const clearAmountTimer = () => {
        if (!amountTimer) return;
        clearTimeout(amountTimer);
        amountTimer = null;
    };

    const invalidateScopeRequests = () => {
        clearAmountTimer();
        buyRuleRequest.invalidate();
        buyRule.value = null;
        amountPreview.invalidate();
    };

    const resetProductSelection = () => {
        resetWholesaleSelectionState({
            skuSelector: skuSelectorRef,
            frontSelection,
            activeSkuId,
            pickedQty,
            pickedTotal,
            pickedKinds
        });
    };

    const reloadCurrentScope = () => {
        invalidateScopeRequests();
        if (productId.value <= 0 || !wholesaleStore.identityLoaded) return;
        void loadBuyRule();
        if (buildPickedItems().length > 0) void fetchProductAmount();
    };

    const refreshCartBadge = async () => {
        try {
            const result: any = await asyncGetCartCount();
            if (typeof result === "number") cartStore.cartCount = result;
            else if (result && typeof result.totalCount === "number") cartStore.cartCount = result.totalCount;
        } catch (err) {
            console.error("[wholesale] refresh cart count failed", err);
        }
    };

    /**
     * 多规格批量加车
     * - 同一商品的多个 SKU 合并为一次 addToCart 请求，由后端统一校验 SPU 起批与混批阶梯
     * - 顶层 number 为 skuItem[].num 之和
     * - 后端业务码由请求层抛出，message 直接 toast 透传
     */
    const doAddCart = async (isQuick = 0): Promise<boolean> => {
        const items = buildPickedItems();
        if (items.length === 0) return false;
        await addToCart(buildWholesaleAddToCartPayload(productId.value, effectiveShopId.value, items, isQuick));
        return true;
    };

    const onAddToCart = async () => {
        const v = validateLocal();
        if (!v.ok) {
            message.error(v.reason || t("请选择购买数量"));
            return;
        }
        loadingAdd.value = true;
        try {
            const ok = await doAddCart(0);
            if (ok) {
                message.success(t("已加入购物车"));
                await refreshCartBadge();
                skuSelectorRef.value?.resetPicked();
            }
        } catch (err: any) {
            await refreshIdentityAfterError(err);
            message.error(err?.message || t("加入购物车失败"));
        } finally {
            loadingAdd.value = false;
        }
    };

    const onBuyNow = async () => {
        const v = validateLocal();
        if (!v.ok) {
            message.error(v.reason || t("请选择购买数量"));
            return;
        }
        loadingBuy.value = true;
        try {
            const ok = await doAddCart(1);
            if (ok) {
                await refreshCartBadge();
                navigateTo("/order/check");
            }
        } catch (err: any) {
            await refreshIdentityAfterError(err);
            message.error(err?.message || t("立即购买失败"));
        } finally {
            loadingBuy.value = false;
        }
    };

    watch(
        productId,
        () => {
            resetProductSelection();
            reloadCurrentScope();
        },
        { immediate: true }
    );

    watch([effectiveShopId, () => wholesaleStore.identityRevision], () => {
        resetProductSelection();
        reloadCurrentScope();
    });

    onScopeDispose(() => {
        clearAmountTimer();
        amountPreview.invalidate();
        buyRuleRequest.invalidate();
    });

    return {
        skuSelectorRef,
        buyRule,
        moq,
        dimensions,
        lastDimPicks,
        ladders,
        priceTiers,
        wholesalePriceMode,
        resolveRowWholesalePrice,
        totalAmountRaw,
        isAmountEstimated,
        aggregateTip,
        pickedTotal,
        loadingAdd,
        loadingBuy,
        onFrontSelect,
        onQtyChange,
        onAddToCart,
        onBuyNow
    };
};
