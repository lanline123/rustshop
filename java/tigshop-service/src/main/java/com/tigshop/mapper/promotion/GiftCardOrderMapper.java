package com.tigshop.mapper.promotion;

import com.tigshop.bean.model.promotion.GiftCardOrder;
import com.tigshop.bean.vo.promotion.card.GiftCardOrderVO;
import com.tigshop.mapper.common.BaseMapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author tigshop
 * @description 针对表【order_card】的数据库操作Mapper
 */
public interface GiftCardOrderMapper extends BaseMapper<GiftCardOrder> {
    List<GiftCardOrderVO.GiftCardOrderInfo> getGiftCardOrderInfo(@Param("orderId") Integer orderId);
}
