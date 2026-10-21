package com.tigshop.mapper.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionItemPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPromotionItemPageVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_item(拼团活动商品)】的数据库操作Mapper
 * @createDate 2025-08-28 11:18:30
 * @Entity com.tigshop.bean.model.promotion.GrouponPromotionItem
 */
public interface GrouponPromotionItemMapper extends BaseMapper<GrouponPromotionItem> {

    /**
     * 分页查询
     */
    Page<GrouponPromotionItemPageVO> page(@Param("page") Page<GrouponPromotionItem> page, @Param("pageQuery") GrouponPromotionItemPageQuery pageQuery);

    /**
     * 统计商品冲突数量
     */
    int countProductConflict(@Param("productId") Long productId, @Param("promotionId") Long promotionId,
                             @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    long getInGroupOnProductCount(@Param("productId") Long productId, @Param("promotionId") Long promotionId,
                                  @Param("currentTimeMillis") Long currentTimeMillis,
                                  @Param("endTime") Long endTime);
}