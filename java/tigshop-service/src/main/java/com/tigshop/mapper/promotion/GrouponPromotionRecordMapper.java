package com.tigshop.mapper.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOrderPageQuery;
import com.tigshop.bean.vo.promotion.GrouponRecordOrderPageVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_record(拼团记录)】的数据库操作Mapper
 * @createDate 2025-08-28 11:18:30
 * @Entity com.tigshop.bean.model.promotion.GrouponPromotionRecord
 */
public interface GrouponPromotionRecordMapper extends BaseMapper<GrouponPromotionRecord> {

    /**
     * 拼团订单列表
     */
    Page<GrouponRecordOrderPageVO> orderList(@Param("page") Page<GrouponPromotionRecord> page, @Param("pageQuery") GrouponRecordOrderPageQuery pageQuery);
}




