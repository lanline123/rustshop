package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.AddToCartDTO;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJoinMemberQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOpenJonPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordOrderPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponRecordPageQuery;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_record(拼团记录)】的数据库操作Service
 * @createDate 2025-08-28 11:18:30
 */
public interface GrouponPromotionRecordService extends BaseService<GrouponPromotionRecord> {

    Page<GrouponRecordOpenJonPageVO> openJoinList(GrouponRecordOpenJonPageQuery pageQuery);

    Page<GrouponRecordOrderPageVO> orderList(GrouponRecordOrderPageQuery pageQuery);

    List<GrouponRecordOpenJoinMemberVO> groupMemberList(GrouponRecordOpenJoinMemberQuery pageQuery);

    void manualGroup(List<Long> grouponPromotionRecordIds);

    void manualGroup(Long grouponPromotionRecordId);

    Page<GrouponRecordVO> clientList(GrouponRecordPageQuery pageQuery);

    GrouponRecordDetailVO clientDetails(Long grouponPromotionRecordId);

    void saveRecord(OrderItem orderItem, AddToCartDTO.GrouponData grouponData);

    void expirationTime(Long grouponRecordId);

    /**
     * 团长退款整团解散：将同团所有待成团记录标记为拼团失败，并为其他团员自动创建售后
     *
     * @param captainOrderId 团长的订单ID
     */
    void dissolveGroupByCaptainRefund(Integer captainOrderId);
}
