package com.tigshop.mapper.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionClientPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponPromotionPageQuery;
import com.tigshop.bean.vo.promotion.GrouponPromotionClientPageVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionPageVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kidd
 * @description 针对表【groupon_promotion(拼团活动)】的数据库操作Mapper
 * @createDate 2025-08-28 11:18:30
 * @Entity com.tigshop.bean.model.promotion.GrouponPromotion
 */
public interface GrouponPromotionMapper extends BaseMapper<GrouponPromotion> {


    Page<GrouponPromotionPageVO> list(@Param("page") Page<GrouponPromotion> page, @Param("pageQuery") GrouponPromotionPageQuery pageQuery);

    Page<GrouponPromotionClientPageVO> clientList(@Param("page") Page<GrouponPromotion> page, @Param("pageQuery") GrouponPromotionClientPageQuery pageQuery);
}




