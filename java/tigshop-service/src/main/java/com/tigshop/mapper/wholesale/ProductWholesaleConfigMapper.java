// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.wholesale;

import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品级批发配置 Mapper
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Mapper
public interface ProductWholesaleConfigMapper extends BaseMapper<ProductWholesaleConfig> {

    /**
     * 含已软删行的存在性计数（绕开 {@code @TableLogic} 自动过滤）。
     * <p>用途：在 INSERT 前判定唯一索引 {@code uk_product_id} 是否会冲突；
     * {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 配合
     * {@code @TableLogic} 会自动追加 {@code is_delete = 0}，导致软删行查不到、
     * 落到 INSERT 时撞 DB 唯一索引 → {@code DataIntegrityViolationException}。
     *
     * @param productId product.product_id
     * @return 含已软删的行数
     */
    int countAnyByProductId(@Param("productId") Integer productId);

    /**
     * 物理删除某批商品的批发配置（绕开 {@code @TableLogic} 软删改写）。
     * <p>策略依据：表上 {@code uk_product_id} 唯一索引不区分 {@code is_delete}，
     * 软删后无法重新添加同一商品的批发能力（与 price 表同坑，见
     * {@link ProductWholesalePriceMapper#deleteByProductId}）。
     *
     * @param productIds product.product_id 列表（调用方保证非空）
     * @return 物理删除行数
     */
    int hardDeleteByProductIds(@Param("productIds") List<Integer> productIds);

    /**
     * 批量按 product_id 查询**未删除**的 wholesale config（filter is_delete=0）。
     * <p>用途：CR-20260603-1720 / CD-035（CR-20260604-0937 简化）— C 端列表卡片
     * wholesaleCardMoq 派生时取 {@code moq}（统一字段），不再需要 sku_count / sku_config 聚合。
     * <p>仅返回当前有效配置（is_delete=0），与读路径口径一致；与 {@link #selectAllConfiguredProductIds}
     * （含已软删历史残留）的"剔除已配置"语义**不同**，前者用于运行时回填判定。
     *
     * @param productIds product.product_id 列表（调用方保证非空）
     * @return 未删除的 wholesale config 列表（按 product_id 1:1，空时返回空列表）
     */
    List<ProductWholesaleConfig> selectByProductIds(@Param("productIds") List<Integer> productIds);

    /**
     * 列出**全表已配置过批发能力**的 product_id 集合（含 {@code is_delete=1} 历史残留行）。
     * <p>用途：admin 端「可选批发商品列表」({@code /adminapi/wholesale/product/selectable/page})
     * 需要剔除已存在 config 行的 product，避免重复添加引发 5311 / 数据脏。
     * <p>口径与 {@link #countAnyByProductId} 一致（与 {@code existsAnyByProductId} 同步），
     * 防御历史 / 迁移产生的脏行。
     * <p>性能说明：CR-20260603-1700 一期场景下 wholesale 商品数 ≪ 平台商品总数（百量级），
     * 全表一次性拉取作为 NOT IN 集合的开销可接受；二期商品规模上升时改 {@code NOT EXISTS} 子查询。
     *
     * @return 已配置批发的 product_id（去重）
     */
    List<Integer> selectAllConfiguredProductIds();
}
