package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.StockWarningListDTO;
import com.tigshop.bean.model.product.StockWarningRule;
import com.tigshop.bean.vo.product.StockWarningEvaluateRowVO;
import com.tigshop.bean.vo.product.StockWarningRowVO;
import com.tigshop.bean.vo.product.StockWarningSnTargetVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 库存预警规则映射类
 *
 * @author Tigshop团队
 */
@Mapper
public interface StockWarningRuleMapper extends BaseMapper<StockWarningRule> {

    /**
     * 库存预警列表分页。库存口径与列表状态计算一并在 SQL 完成，避免内存过滤破坏分页数量。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 列表行
     */
    Page<StockWarningRowVO> selectWarningPage(Page<StockWarningRowVO> page, @Param("query") StockWarningListDTO query);

    /**
     * 导出用的不分页查询，与 {@link #selectWarningPage} 复用同一份 SQL，保证导出内容与页面所见一致。
     *
     * @param query 查询条件
     * @return 全部匹配行
     */
    List<StockWarningRowVO> selectWarningList(@Param("query") StockWarningListDTO query);

    /**
     * 导入时按编码批量解析商品/SKU 行。规格编码与商品编码都可能重复或跨表撞车，
     * 这里返回全部命中行，由服务层判断唯一性并区分单规格与多规格。
     *
     * @param shopId 归属主体，主体范围与列表一致
     * @param snList 待解析的编码，需非空且已去重
     * @return 命中行；无命中时为空集合
     */
    List<StockWarningSnTargetVO> selectTargetsBySn(@Param("shopId") Integer shopId,
                                                  @Param("snList") Collection<String> snList);

    /**
     * 批量查询当前主体可见的商品 ID，供库存预警的相关商品字段按主体过滤。
     *
     * @param shopId     当前主体；0 表示平台自营
     * @param productIds 待过滤的相关商品 ID
     * @return 当前主体可见的商品 ID
     */
    List<Integer> selectVisibleProductIds(@Param("shopId") Integer shopId,
                                          @Param("productIds") Collection<Integer> productIds);

    /**
     * 按主体口径查询单个商品/SKU 的库存与商品快照，供预警判定使用。
     *
     * @param shopId        归属主体
     * @param productId     商品ID
     * @param skuId         SKU ID；0 表示单规格
     * @param useStoreStock 是否使用门店独立库存口径
     * @param useTotalStock 是否同时启用总库存（与门店独立库存并存时取较小值）
     * @return 判定行；商品不存在时返回 null
     */
    StockWarningEvaluateRowVO selectEvaluateRow(@Param("shopId") Integer shopId,
                                               @Param("productId") Integer productId,
                                               @Param("skuId") Integer skuId,
                                               @Param("useStoreStock") boolean useStoreStock,
                                               @Param("useTotalStock") boolean useTotalStock);
}
