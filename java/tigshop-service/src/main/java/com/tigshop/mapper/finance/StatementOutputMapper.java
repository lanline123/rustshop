
// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.finance;

import com.tigshop.bean.model.finance.StatementOutput;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出账单 Mapper 接口
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
public interface StatementOutputMapper extends BaseMapper<StatementOutput> {
    /** 用售后单号和 ID 双重关联，避免与订单 ID 撞号；每条出账最多返回一次。 */
    @Select("""
            SELECT so.* FROM statement_output so
            WHERE so.record_type = #{feeType}
              AND so.shop_id = #{shopId} AND so.vendor_id = #{vendorId}
              AND EXISTS (
                SELECT 1 FROM aftersales a JOIN refund_apply ra ON ra.aftersale_id = a.aftersale_id
                WHERE a.order_id = #{orderId} AND a.status = 6
                  AND ra.order_id = a.order_id AND ra.refund_status = 2
                  AND so.record_id = a.aftersale_id AND so.record_sn = a.aftersales_sn
              )
            """)
    List<StatementOutput> selectConfirmedRefundFees(@Param("orderId") Long orderId,
            @Param("shopId") Long shopId, @Param("vendorId") Long vendorId, @Param("feeType") int feeType);

    /** 零费用也有出账行；缺行不能当零冲回，否则可能按退款前服务费多分。 */
    @Select("""
            SELECT COUNT(*) FROM aftersales a
            WHERE a.order_id = #{orderId} AND a.status = 6
              AND EXISTS (SELECT 1 FROM refund_apply ra WHERE ra.aftersale_id = a.aftersale_id
                AND ra.order_id = a.order_id AND ra.refund_status = 2)
              AND NOT EXISTS (SELECT 1 FROM statement_output so
                WHERE so.record_id = a.aftersale_id AND so.record_sn = a.aftersales_sn
                  AND so.record_type = #{feeType} AND so.shop_id = #{shopId} AND so.vendor_id = #{vendorId})
            """)
    long countMissingConfirmedRefundFees(@Param("orderId") Long orderId,
            @Param("shopId") Long shopId, @Param("vendorId") Long vendorId, @Param("feeType") int feeType);
}
