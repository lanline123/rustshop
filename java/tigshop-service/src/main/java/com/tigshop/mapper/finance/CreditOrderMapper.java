package com.tigshop.mapper.finance;

import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 账期支付订单Mapper
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditOrderMapper extends BaseMapper<CreditOrder> {

    /**
     * 根据订单ID查询账期订单
     *
     * @param orderId 订单ID
     * @return 账期订单
     */
    @Select("SELECT * FROM credit_order WHERE order_id = #{orderId}")
    CreditOrder selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 统计账期内待结算订单数
     *
     * @param userId      用户ID
     * @param periodStart 账期开始时间（毫秒时间戳）
     * @param periodEnd   账期结束时间（毫秒时间戳）
     * @return 待结算订单数
     */
    @Select("SELECT COUNT(*) FROM credit_order WHERE user_id = #{userId} " +
            "AND settlement_status = 0 " +
            "AND order_time >= #{periodStart} " +
            "AND order_time < #{periodEnd}")
    int countUnsettledOrdersInPeriod(@Param("userId") Integer userId,
                                     @Param("periodStart") Long periodStart,
                                     @Param("periodEnd") Long periodEnd);
}
