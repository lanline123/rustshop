package com.tigshop.mapper.finance;

import com.tigshop.bean.model.finance.PaylogRefund;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author kidd
*/
@Mapper
public interface PaylogRefundMapper extends BaseMapper<PaylogRefund> {

    /** 渠道退款成功但业务退款/费用冲回尚未完成时，不允许按旧费用发起分账。 */
    @Select("""
            SELECT COUNT(*) FROM paylog_refund p
            WHERE p.order_id = #{orderId} AND p.pay_code = 'wechat' AND p.status = 1 AND p.refund_amount > 0
              AND NOT EXISTS (
                SELECT 1 FROM refund_apply ra JOIN aftersales a ON a.aftersale_id = ra.aftersale_id
                WHERE ra.order_id = p.order_id AND a.order_id = p.order_id
                  AND ra.refund_status = 2 AND ra.is_online = 2 AND a.status = 6
                  AND NOT EXISTS (
                    SELECT 1 FROM paylog_refund pending WHERE pending.status = 0
                      AND (pending.paylog_refund_id = ra.paylog_refund_id OR EXISTS (
                        SELECT 1 FROM refund_apply_paylog_refund pr
                        WHERE pr.refund_apply_id = ra.refund_id AND pr.paylog_refund_id = pending.paylog_refund_id
                      ))
                  )
                  AND ra.online_balance = (
                    SELECT COALESCE(SUM(done.refund_amount), 0) FROM paylog_refund done WHERE done.status = 1
                      AND (done.paylog_refund_id = ra.paylog_refund_id OR EXISTS (
                        SELECT 1 FROM refund_apply_paylog_refund dr
                        WHERE dr.refund_apply_id = ra.refund_id AND dr.paylog_refund_id = done.paylog_refund_id
                      ))
                  )
                  AND (ra.paylog_refund_id = p.paylog_refund_id OR EXISTS (
                    SELECT 1 FROM refund_apply_paylog_refund rel
                    WHERE rel.refund_apply_id = ra.refund_id AND rel.paylog_refund_id = p.paylog_refund_id
                  ))
              )
            """)
    long countUnsettledSuccessfulRefunds(@Param("orderId") Long orderId);

}


