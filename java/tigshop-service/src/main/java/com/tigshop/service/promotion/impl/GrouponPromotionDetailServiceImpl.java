package com.tigshop.service.promotion.impl;


import cn.hutool.core.lang.Assert;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.promotion.GrouponPromotionDetailMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.GrouponPromotionDetailService;
import org.springframework.stereotype.Service;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_detail(拼团信息)】的数据库操作Service实现
 * @createDate 2025-08-28 13:37:26
 */
@Service
public class GrouponPromotionDetailServiceImpl extends BaseServiceImpl<GrouponPromotionDetailMapper, GrouponPromotionDetail> implements GrouponPromotionDetailService {

    @Override
    public void decLimitNum(Integer quantity, Long grouponDetailId) {
        GrouponPromotionDetail detail = this.getById(grouponDetailId);
        Assert.notNull(detail, () -> new GlobalException("拼团活动不存在或已下架"));
        Assert.isTrue(detail.getLimitNum() != null && detail.getLimitNum() >= quantity,
                () -> new GlobalException("拼团商品可售数量不足"));

        detail.setLimitNum(detail.getLimitNum() - quantity);

        this.updateById(detail);
    }

    @Override
    public void incLimitNum(Integer quantity, Long grouponDetailId) {
        GrouponPromotionDetail detail = this.getById(grouponDetailId);
        if (detail == null) {
            return;
        }
        detail.setLimitNum((detail.getLimitNum() != null ? detail.getLimitNum() : 0) + quantity);
        this.updateById(detail);
    }
}




