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

import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 三维批发价（SKU × Grade × Tier） Mapper
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Mapper
public interface ProductWholesalePriceMapper extends BaseMapper<ProductWholesalePrice> {

    /**
     * 列出某商品全部未删除的批发价记录（含通用价 + 所有等级 + 所有阶梯）。
     *
     * @param productId product.product_id
     * @return 批发价列表
     */
    List<ProductWholesalePrice> listByProductId(@Param("productId") Integer productId);

    /**
     * 物理删除某商品的全部批发价记录。
     * <p>
     * 用于「全量替换」策略：先 DELETE 旧记录再批量 INSERT 新记录。
     * 这里刻意使用物理删除（绕过 is_delete 软删），避免旧版本累积；
     * 决策依据见 task prompt（与 model 上的 @TableLogic 形成例外，仅 mapper 层使用）。
     *
     * @param productId product.product_id
     * @return 物理删除行数
     */
    int deleteByProductId(@Param("productId") Integer productId);

    /**
     * 批量预取每个商品的「第一阶梯」批发价（CR-20260613-1629：起量最低阶梯，替换原 CD-021 的 MIN(id) 录入序首条）。
     * <p>
     * 用于 C 端商品列表 backfill `firstWholesalePrice` 字段（TASK-WS-209）。
     * <ul>
     *   <li>过滤：{@code is_delete=0 AND status=1 AND (grade_id=0 OR grade_id=#{gradeId})}
     *       — 即「通用价 ∪ 当前等级专属价」候选集</li>
     *   <li>按 {@code (product_id, grade_id)} 分组，每组取最低 {@code tier_min_qty}（同阶梯 tiebreak {@code MIN(id)}）</li>
     *   <li>每个 productId 最多返回 2 行（通用第一阶梯 + 当前等级第一阶梯）；
     *       「当前等级优先于通用」由 {@code WholesalePriceServiceImpl.resolveFirstWholesalePriceMap} 在 Java 侧聚合</li>
     *   <li>禁 N+1：单 SQL IN + 分组 + 相关子查询批量查询（不用窗口函数，5.7/8.x 通用）</li>
     * </ul>
     *
     * @param productIds 商品 ID 列表（调用方保证非空）
     * @param gradeId    当前批发商等级 ID（&gt;0；调用方保证已校验 isWholesaleBuyer=true）
     * @return 第一阶梯批发价记录列表（每商品最多 2 行：通用 + 当前等级；未命中商品不返回）
     */
    List<ProductWholesalePrice> selectFirstPriceByProductIds(
            @Param("productIds") List<Integer> productIds,
            @Param("gradeId") Integer gradeId);
}
