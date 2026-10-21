package com.tigshop.service.wholesale;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.query.wholesale.WholesaleProductPageQuery;
import com.tigshop.bean.query.wholesale.WholesaleSelectableProductPageQuery;
import com.tigshop.bean.vo.wholesale.WholesaleProductBuyRuleVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductVO;

/**
 * 批发商品列表查询 Service（读模型）。
 *
 * <p>设计依据：{@code .ai-os/specs/wholesaler-tasks-w2-steps.md § TASK-WS-204}、
 * {@code wholesaler-api-contract.md §2.B1}。将「商品级配置 + SKU 级配置 + 三维批发价 + 商品基础信息」
 * 聚合为列表 VO，承担多表读侧聚合，避免污染各写侧 Service。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesaleProductQueryService {

    /**
     * 批发商品分页查询。
     *
     * <p>聚合 {@code product} + {@code product_wholesale_config}
     * + {@code product_wholesale_sku_config} + {@code product_wholesale_price}。
     *
     * @param query 分页 + keyword（商品名/货号）+ moqDimension 筛选
     * @return 批发商品列表 VO 分页
     */
    IPage<WholesaleProductVO> page(WholesaleProductPageQuery query);

    /**
     * 可选批发商品分页查询（admin / 商家选择名下商品加入批发的列表）。
     *
     * <p>过滤条件（CR-20260603-1700；CR-20260610-162600 多租户修订）：
     * <ul>
     *   <li>{@code product.shop_id = 当前操作者 shopId}（平台 admin=0 / 商家=自己店铺，CD-039；原固定 0 已废）</li>
     *   <li>{@code product.is_delete = 0}（未软删商品）</li>
     *   <li>{@code product.product_status = query.productStatus ?? 1}（默认仅上架）</li>
     *   <li>剔除 {@code product_wholesale_config} 表中已存在 row 的 product_id（含 {@code is_delete=1} 历史残留，与 {@code existsAnyByProductId} 同口径）</li>
     *   <li>可选 {@code categoryId / brandId / keyword(productName|productSn|productTsn)}</li>
     * </ul>
     *
     * <p>返回字段（最小集）：{@code productId / productName / productSn / productTsn /
     * picUrl / picThumb / productPrice / marketPrice / productStatus / categoryId / brandId}。
     *
     * @param query 分页 + 过滤入参
     * @return 可选商品列表分页（VO 复用 {@link ProductListResDTO}）
     */
    IPage<ProductListResDTO> selectablePage(WholesaleSelectableProductPageQuery query);

    /**
     * C 端商品起批规则查询（TASK-WS-208 / api-contract §3.C2）。
     *
     * <p>未配置批发能力时返回 {@code configured=false}、{@code skuRules=[]}、{@code moq=null}；
     * 已配置时返回 {@code dimension + moq + skuRules(multiple) + hint}。CR-20260604-0937 后
     * 起批数统一从 {@code config.moq} 读取，{@code dimension} 仅决定文案解释。
     *
     * <p>性能：最多 2 次 DB 读（{@code config} + {@code sku_config}），不查 price。
     * <p>权限：契约规定非批发商也可查（前端预览），不做身份判定。
     *
     * @param productId 商品 ID（{@code null} → 抛 BAD_REQUEST，由 Controller @Valid 拦截）
     * @return 起批规则 VO（必返回非 null）
     */
    WholesaleProductBuyRuleVO buildBuyRule(Integer productId);
}
