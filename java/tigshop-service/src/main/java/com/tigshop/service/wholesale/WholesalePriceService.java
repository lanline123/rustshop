package com.tigshop.service.wholesale;

import com.tigshop.bean.dto.wholesale.WholesaleBatchCommonDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBatchGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesalePriceSaveDTO;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.vo.wholesale.WholesalePriceTierVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 三维批发价 Service（{@code product_wholesale_price}：SKU × Grade × Tier）。
 *
 * <p>方法签名以 {@code .ai-os/specs/wholesaler-tasks-w2-steps.md § TASK-WS-202}
 * 与 {@code wholesaler-api-contract.md §2.B B4-B7} 为准。
 *
 * <p>核心语义：
 * <ul>
 *   <li>{@link #save} = 全量替换该 {@code productId} 的所有 price 行（事务内物理删 + 批量插入）。</li>
 *   <li>{@link #batchSetCommon} / {@link #batchSetByGrade} = 按维度覆盖式更新；
 *       因 {@code uk_combo} 含 {@code is_delete}，分维度物理删需走「读-合并-整表替换」实现
 *       （见 impl 注释，不直接复用 {@link #save}）。</li>
 *   <li>{@link #deleteByProductIds} = 商品下架/删除时级联物理删 price 行
 *       （{@code WholesaleProductConfigService.batchDelete} 显式委托本 Service 承担）。</li>
 * </ul>
 *
 * <p>折扣为十分制（{@code discount ∈ [0.10, 9.99]}，{@code 5.00 = 5 折}，CD-015）。
 * 校验失败统一抛 {@link com.tigshop.common.exception.GlobalException}，错误码取
 * {@link com.tigshop.common.constant.HttpStatus} 中 {@code WHOLESALE_*} 常量。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesalePriceService {

    /**
     * 列出某商品全部有效批发价行（含通用价 + 所有等级 + 所有阶梯）。
     *
     * <p>供 B4 详情装配与 {@code WholesalePriceResolver} 候选输入使用。
     *
     * @param productId product.product_id
     * @return 批发价行列表（无数据返回空列表）
     */
    List<ProductWholesalePrice> listByProductId(Integer productId);

    /**
     * 批量取多商品的批发价候选行（购物车 B2 壳预加载，design §5 / W3 step §T-300）。
     *
     * <p>仅取 {@code status=1} 且 {@code grade_id ∈ {0, gradeId}} 的有效行
     * （{@code is_delete} 由 {@code @TableLogic} 自动过滤）。结果按 {@code productId}
     * 分组，供 {@link com.tigshop.service.wholesale.resolver.WholesalePriceResolver}
     * 在循环内 0 DB 命中算价消费。
     *
     * <p>性能：1 次 IN 查询；空入参不打 DB。
     *
     * @param productIds 商品 ID 列表（空或 null 返回空 Map）
     * @param gradeId    当前批发等级（null/≤0 视为 0 通用价；非批发商不应调用本方法）
     * @return key=productId，value=该商品下命中等级过滤的候选 price 行列表（不含未命中等级的行）
     */
    Map<Integer, List<ProductWholesalePrice>> mapByProductIdsAndGrade(List<Integer> productIds, Integer gradeId);

    /**
     * 列出「对指定批发等级有效」的全部有批发价商品 ID
     * （CR-20260713-1620：C 端促销列表过滤——批发商只看批发价，有批发价的商品从促销列表隐藏）。
     *
     * <p>口径与 {@link #mapByProductIdsAndGrade} 对齐：仅取 {@code status=1} 且
     * {@code grade_id ∈ {0 通用, gradeId}} 的行（{@code is_delete} 由 {@code @TableLogic} 自动过滤），
     * 取 DISTINCT {@code product_id}。
     *
     * @param gradeId 当前批发商等级（{@code null}/{@code <0} 视为 0 通用价）
     * @return 有批发价的 {@code product_id} 集合（无数据返回空集）
     */
    Set<Integer> listPricedProductIdsByGrade(Integer gradeId);

    /**
     * 判断「某商品对指定批发等级是否有生效批发价」（CR-20260714-1335：商品详情页活动隐藏）。
     *
     * <p>口径与 {@link #listPricedProductIdsByGrade} / {@link #mapByProductIdsAndGrade} 完全一致：
     * 存在 {@code status=1} 且 {@code grade_id ∈ {0 通用, gradeId}} 的行即为命中
     * （{@code is_delete} 由 {@code @TableLogic} 自动过滤）。单商品 {@code LIMIT 1} 存在性查询，
     * 供详情页 / 选 SKU 价预览判定「批发商是否应隐藏该商品活动」。
     *
     * @param productId 商品 ID（null/≤0 直接返回 false，不打 DB）
     * @param gradeId   当前批发商等级（{@code null}/{@code <0} 视为 0 通用价）
     * @return true=该商品对该等级有生效批发价
     */
    boolean hasPricedProduct(Integer productId, Integer gradeId);

    /**
     * 全量替换保存（B5 价格保存）。
     *
     * <p>事务内：物理删该 {@code productId} 全部旧行 → 批量插入入参行。
     * 前置要求商品已配置批发能力（未配置抛 5310）。
     *
     * @param dto 价格保存入参（{@code priceRows} 为该商品的完整新集合）
     */
    void save(WholesalePriceSaveDTO dto);

    /**
     * 批量设置通用售价（B6，{@code grade_id=0}）。
     *
     * @param dto 通用售价批量入参（{@code skuIds} 为空表示全 SKU，落到 SPU 兜底行 {@code sku_id=0}）
     * @return 本次写入的通用价行数
     */
    int batchSetCommon(WholesaleBatchCommonDTO dto);

    /**
     * 批量设置类型售价（B7，{@code grade_id>0}）。
     *
     * @param dto 类型售价批量入参（每个 {@code gradeRows[]} 对应一个启用中等级的阶梯集合）
     * @return 本次写入的类型价行数
     */
    int batchSetByGrade(WholesaleBatchGradeDTO dto);

    /**
     * 批量物理删除多个商品的全部批发价行（商品下架/删除级联）。
     *
     * @param productIds 商品 ID 列表（空列表无操作）
     */
    void deleteByProductIds(List<Integer> productIds);

    /**
     * C 端商品列表 backfill：批量解析每个商品的首条批发价（{@code id ASC = admin 录入顺序首条}）。
     *
     * <p>TASK-WS-209 / CD-021 / api-contract §4.0：
     * <ul>
     *   <li>仅当商品已配置批发能力 + 命中（通用价 OR 当前等级专属价）时返回</li>
     *   <li>价格计算：折扣模式 {@code priceMode=1} → {@code retailPrice × discount / 10}（HALF_UP 2 位，CD-015）；
     *       固定模式 {@code priceMode=2} → {@code wholesale_price}（直接生效，与 retailPrice 无关）</li>
     *   <li>批量 IN + GROUP BY 单 SQL 预取（禁 N+1）</li>
     *   <li>不调用 PriceResolver（Resolver 仅结算场景）</li>
     * </ul>
     *
     * @param productIds     商品 ID 列表；为空或 null 时直接返回空 map
     * @param gradeId        当前批发商等级 ID；&le;0 或 null 时直接返回空 map（调用方应先校验 isWholesaleBuyer）
     * @param retailPriceMap productId → retailPrice（折扣模式所需；某 productId 缺失或价格为 null 则该商品的折扣计算被跳过）
     * @return productId → 首条批发价的 finalPrice；只包含「命中且可计算」的商品（其他直接缺省）
     */
    Map<Integer, BigDecimal> resolveFirstWholesalePriceMap(
            List<Integer> productIds, Integer gradeId, Map<Integer, BigDecimal> retailPriceMap);

    /**
     * C 端商品列表卡片 backfill：批量解析每个商品的「卡片可直接加购起批数」（CR-20260603-1720 / CD-035）。
     *
     * <p>4 条命中规则（AND，缺一为 null，不返回该 productId 即 null）：
     * <ol>
     *   <li>调用方保证传入的 productIds 已是 {@code firstWholesalePrice != null} 命中商品集合</li>
     *   <li>该商品 SKU 数 &le; 1（单规格；多规格跳详情）</li>
     *   <li>{@code product_wholesale_config} 存在且 {@code is_delete=0}</li>
     *   <li>起批数：{@code moqDimension=SPU} → {@code moqSpu}；{@code moqDimension=SKU} →
     *       首条 SKU 的 {@code moqSku}；任一 null 则整体回退 null</li>
     * </ol>
     *
     * <p>SQL 成本：最多 3 次 IN 查询（config + sku_count + sku_config）；
     * 第 3 次仅在存在「SKU 维度的单规格商品」子集时触发；空入参不打 DB。
     * 调用方必须先短路：非批发商 / 无命中商品时不调用本方法（性能基线 CD-017；
     * CR-20260610-162600 起店铺商品已放开，不再要求"非平台"短路）。
     *
     * @param productIds 候选商品 ID 列表（应是 firstPriceMap.keySet()，调用方保证已通过批发命中过滤）
     * @return key=productId，value=卡片起批数（int 非 null）；未命中商品不出现在 map 里
     */
    Map<Integer, Integer> resolveCardMoqMap(List<Integer> productIds);

    /**
     * CR-20260613-0943：批量取命中商品的批发配置（{@code moq > 0}），供 C 端列表/卡片
     * 一次性回填 {@code wholesaleCardMoq}（=moq）与 {@code wholesaleMoqDimension}（=moqDimension）。
     *
     * <p>与 {@link #resolveCardMoqMap} 同口径、同一份 {@code product_wholesale_config} IN 查询
     * （0 额外 SQL）；{@code resolveCardMoqMap} 现委托本方法。
     *
     * @param productIds 候选商品 ID（应是 firstPriceMap.keySet()，调用方保证已通过批发命中过滤）
     * @return key=productId，value=该商品的 config（仅 {@code moq > 0} 的商品出现）
     */
    Map<Integer, ProductWholesaleConfig> resolveCardConfigMap(List<Integer> productIds);

    /**
     * CR-20260613-0943：批量解析 SPU 兜底（{@code sku_id=0}）倍数，供 C 端列表/卡片回填
     * {@code wholesaleMultiple}。列表/卡片无用户选中 SKU，统一取 sku_id=0 兜底口径。
     *
     * <p>SQL 成本：1 次 {@code product_wholesale_sku_config} IN（禁 N+1）；空入参不打 DB。
     * {@code multiple ≤ 1} 视为无倍数约束，不入 map（前端按 null 处理）。
     *
     * @param productIds 候选商品 ID（应是 firstPriceMap.keySet()）
     * @return key=productId，value=SPU 兜底倍数（{@code > 1}）；无有效倍数的商品不出现
     */
    Map<Integer, Integer> resolveSpuMultipleMap(List<Integer> productIds);

    /**
     * C 端商品详情 backfill：构造该商品全等级阶梯价 VO 列表（TASK-WS-210 / CD-022）。
     *
     * <p>规则：
     * <ul>
     *   <li>查询该 productId 全部 {@code is_delete=0 AND status=1} 的 price 行</li>
     *   <li>按 {@code id ASC}（= price_id ASC，admin 录入顺序）排序</li>
     *   <li><b>不按当前等级过滤</b>（CD-022 — 详情返回全等级阶梯，UI 自己高亮当前等级行）</li>
     *   <li>backfill {@code gradeName}：{@code gradeId=0} → "通用"；其余调用 {@link WholesalerGradeService#mapByIds} 批量查询</li>
     *   <li>{@code displayPrice}：折扣模式按 CD-015 计算；固定模式直接取 {@code wholesale_price}</li>
     * </ul>
     *
     * @param productId   商品 ID
     * @param retailPrice 商品当前零售价（折扣模式计算 displayPrice 所需；可为 null，此时折扣行的 displayPrice 留 null）
     * @return 阶梯价 VO 列表，按 priceId ASC 排序；无批发价配置返回空列表
     */
    List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice);

    /**
     * C 端商品详情 backfill：构造该商品全等级阶梯价 VO 列表（按 SKU 取折扣基数版本）。
     *
     * <p>与 {@link #buildWholesalePriceTierList(Integer, BigDecimal)} 一致，但折扣行的 displayPrice
     * 折扣基数按行所属 SKU 取价，与下单算价 {@code WholesalePriceResolver}（基数=SKU 零售价）对齐：
     * <ul>
     *   <li>行 {@code sku_id > 0} 且 {@code skuRetailPriceMap} 命中 → 用该 SKU 零售价做折扣基数</li>
     *   <li>行 {@code sku_id = 0}（SPU 兜底）或映射未命中 → 回退商品级 {@code retailPrice}</li>
     * </ul>
     *
     * @param productId         商品 ID
     * @param retailPrice       商品级零售价（SPU 兜底行折扣基数 / 映射未命中兜底；可为 null）
     * @param skuRetailPriceMap skuId → SKU 零售价 映射（可为 null / 空，此时退化为商品级单基数）
     * @return 阶梯价 VO 列表，按 priceId ASC 排序；无批发价配置返回空列表
     */
    List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap);

    /**
     * C 端商品详情 backfill：构造该商品阶梯价 VO 列表，<b>按当前批发商等级过滤</b>（CR-20260612-1659，修订 CD-022）。
     *
     * <p>与 {@link #buildWholesalePriceTierList(Integer, BigDecimal, Map)} 一致（折扣基数按 SKU 取价），
     * 但在 status 过滤之外额外按等级收窄，口径与成交价 {@code WholesalePriceResolver.matchesGrade} 对齐：
     * <ul>
     *   <li>仅保留 {@code grade_id == 0(通用)} 或 {@code grade_id == gradeId(当前等级)} 的行；其他等级阶梯不返回</li>
     *   <li>{@code gradeId == null || gradeId <= 0} → 返回空列表（与 resolver「无有效等级返回 null」同口径）</li>
     * </ul>
     *
     * @param productId         商品 ID
     * @param retailPrice       商品级零售价（SPU 兜底行折扣基数 / 映射未命中兜底；可为 null）
     * @param skuRetailPriceMap skuId → SKU 零售价 映射（可为 null / 空，此时退化为商品级单基数）
     * @param gradeId           当前批发商等级 ID（null/≤0 → 返回空列表）
     * @return 阶梯价 VO 列表，按 priceId ASC 排序；无匹配返回空列表
     */
    List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap, Integer gradeId);
}
