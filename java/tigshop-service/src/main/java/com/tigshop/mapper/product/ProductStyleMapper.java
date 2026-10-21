package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.product.ProductStyle;
import com.tigshop.bean.query.product.ProductStylePageQuery;
import com.tigshop.bean.vo.product.BrandProductCountVO;
import com.tigshop.bean.vo.product.ProductStyleVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProductStyleMapper extends BaseMapper<ProductStyle> {

    /**
     * 商品样式分页列表（实时统计关联商品数，支持按商品数排序）
     */
    Page<ProductStyleVO> pageList(@Param("page") Page<ProductStyleVO> page,
                                  @Param("query") ProductStylePageQuery query,
                                  @Param("shopId") Integer shopId,
                                  @Param("now") long now,
                                  @Param("startTime") Long startTime,
                                  @Param("endTime") Long endTime);

    /**
     * 获取商品当前生效的样式（边框+活动背景）
     */
    List<Map<String, Object>> selectActiveStylesByProductIds(@Param("productIds") List<Integer> productIds,
                                                             @Param("now") long now);

    /**
     * 批量统计每个样式关联的实际商品数量（通过商品/分组/品牌实时关联product表）
     */
    List<Map<String, Object>> countRealProductsByStyleIds(@Param("styleIds") List<Integer> styleIds);

    /**
     * 查询品牌列表及每个品牌下的商品数量（实时统计）
     */
    List<BrandProductCountVO> selectBrandListWithProductCount(@Param("keyword") String keyword);
}
