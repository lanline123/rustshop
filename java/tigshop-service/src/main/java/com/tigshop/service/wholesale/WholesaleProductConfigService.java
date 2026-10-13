package com.tigshop.service.wholesale;

import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.vo.wholesale.WholesaleProductBatchAddVO;

import java.util.List;
import java.util.Map;

/**
 * 商品级批发配置 Service（{@code product_wholesale_config}）。
 *
 * <p>方法签名以 {@code .ai-os/specs/wholesaler-tasks-w2-steps.md § TASK-WS-201} 为准，
 * 该签名同时被 T-202（WholesalePriceService 调 {@link #isConfigured} / {@link #updateMultiSpecMode}）
 * 与 T-203/T-204 等并行任务消费。
 *
 * <p>错误码：消费 {@link com.tigshop.common.constant.HttpStatus} 中的 {@code WHOLESALE_*} 常量
 * （5310 商品不存在 / 5311 已配置批发）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesaleProductConfigService {

    /** 是否已配置且启用批发能力（W3 购物车壳判定 / T-202 save 前置校验）。 */
    boolean isConfigured(Integer productId);

    /** 取有效配置（不存在或已禁用返回 {@code null}）。 */
    ProductWholesaleConfig getActiveByProductId(Integer productId);

    /** 批量取有效配置（购物车 B2 壳预加载），key=productId。 */
    Map<Integer, ProductWholesaleConfig> mapByProductIds(List<Integer> productIds);

    /**
     * 创建商品级批发配置（添加批发商品入口）。
     *
     * @param multiSpecPriceMode 多规格价格模式：1=商品级混批 / 2=SKU 级独立
     * @param moqDimension        起批维度：1=SPU / 2=SKU
     * @return 新记录主键 id
     */
    Integer create(Integer productId, Integer multiSpecPriceMode, Integer moqDimension);

    /** 更新 {@code multi_spec_price_mode}（仅多规格商品；1 或 2）。 */
    void updateMultiSpecMode(Integer productId, Integer mode);

    /**
     * 更新 {@code moq_dimension} + {@code moq}（CR-20260604-0937 统一字段）。
     *
     * @param dimension 起批维度：1=SPU / 2=SKU
     * @param moq       起批数（{@code null} 表示不更新该字段）；维度无关，含义随 dimension 解释
     */
    void updateMoq(Integer productId, Integer dimension, Integer moq);

    /**
     * 批量更新 {@code moq_dimension} + {@code moq}（CR-20260604-1042）。
     *
     * <p>一次给 N 个 product 应用同一对 {@code dimension + moq}（admin 批发商品列表 — 批量设置起批量）。
     *
     * <p>语义：
     * <ul>
     *   <li><b>整批事务</b>：任一 productId 在 config 表不存在 → 抛 {@code 5310 WHOLESALE_PRODUCT_NOT_FOUND}
     *       整批回滚（all-or-nothing；admin 应看到错误并修正选择）</li>
     *   <li><b>去重保序</b>：productIds 内部去重，仅 update 一次（与 {@link #batchAdd} 一致）</li>
     *   <li><b>校验复用</b>：{@code dimension} / {@code moq} 校验与 {@link #updateMoq} 同源</li>
     * </ul>
     *
     * @param productIds 商品 ID 列表（≥1，已由 Controller @Valid 限制上限 50）
     * @param dimension  起批维度：1=SPU / 2=SKU（非法 → BAD_REQUEST）
     * @param moq        起批数（≥ 1，否则 BAD_REQUEST）
     * @return 实际更新行数（受 mybatis-plus updateById 影响数返回）
     */
    int batchUpdateMoq(List<Integer> productIds, Integer dimension, Integer moq);

    /**
     * 批量删除商品级批发配置（删除批发商品入口）。
     *
     * <p>本方法在自身边界内级联软删 {@code config} 与 {@code sku_config}；
     * {@code price} 行的级联归 T-202 WholesalePriceService 范围，见实现注释。
     *
     * @return 软删的商品级配置行数
     */
    int batchDelete(List<Integer> productIds);

    /**
     * 批量添加批发商品（admin 一次选多个普通商品转为批发商品）。
     *
     * <p>契约见 api-contract §4.3，记忆见 CD-023：
     * <ul>
     *   <li>仅创建 {@code product_wholesale_config} 默认配置，<b>不创建</b> {@code product_wholesale_price}；</li>
     *   <li>价格仍需逐商品在 {@code /price/save} 录入；</li>
     *   <li>已存在配置（active）的 productId 进入 {@code skippedIds[]}，不报错（幂等）；</li>
     *   <li>单 {@code @Transactional}：未跳过项创建失败（例如 5310 商品不存在）会回滚整批，
     *       保证不留半成品；productIds 的合法性（1-50、正整数）由 Controller 层 {@code @Valid} 拦截。</li>
     * </ul>
     *
     * @param productIds 商品 ID 列表（调用前已由 {@code @Valid} 保证 1-50 个、元素为正整数）
     * @return {@link WholesaleProductBatchAddVO}（{@code successCount} + {@code skippedIds}）
     */
    WholesaleProductBatchAddVO batchAdd(List<Integer> productIds);

    /**
     * 归属校验（CR-20260610-162600 / CD-039 多租户）：按已落库 {@code config.shop_id} 与当前操作者
     * （{@code HeaderUtils.getShopId()}，平台 admin=0 / 商家=自己 shopId）比对，跨店抛 5314。
     *
     * <p>覆盖含 {@code status=0} 的行（防止跨店操作他店已禁用配置）；productId 无任何配置行时
     * 不抛（交由业务方法自身的 5310 处理）。admin 价格/起批/倍数等写侧端点统一前置调用。
     */
    void requireOwnership(Integer productId);

    /** 批量版 {@link #requireOwnership(Integer)}：任一行跨店即抛 5314。 */
    void requireOwnership(List<Integer> productIds);
}
