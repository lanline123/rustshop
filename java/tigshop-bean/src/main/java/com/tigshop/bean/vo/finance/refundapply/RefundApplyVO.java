package com.tigshop.bean.vo.finance.refundapply;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.finance.OrderFinanceAftersales;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.tigshop.common.constant.finance.RefundApplyConstants.REFUND_STATUS_NAME;
import static com.tigshop.common.constant.finance.RefundApplyConstants.REFUND_TYPE_NAME;

/**
 * 退款申请VO
 *
 * @author kidd
 * @create 2025/7/7
 */
@Data
@Schema(description = "退款申请参数")
public class RefundApplyVO {

    // *** RefundApply ***

    @Schema(description = "退款申请ID")
    private Integer refundId;

    @Schema(description = "退款类型：1:订单,2:商品")
    private Integer refundType;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款状态：0:未处理,1:处理中，2:已处理,3:取消,4:已拒绝")
    private Integer refundStatus;

    @Schema(description = "回复内容")
    private String refundNote;

    @Schema(description = "线上退款金额")
    private BigDecimal onlineBalance;

    @Schema(description = "线下退款金额")
    private BigDecimal offlineBalance;

    @Schema(description = "退款余额")
    private BigDecimal refundBalance;

    @Schema(description = "线上金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOnline;

    @Schema(description = "线下金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOffline;

    @Schema(description = "打款凭证")
    private String paymentVoucher;

    @Schema(description = "余额是否到账:0 无需处理1处理中 2已处理")
    private Integer isReceive;

    @Schema(description = "线上退款的关联ID")
    private Integer paylogRefundId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    // *** Other ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "退款类型")
    private String  refundTypeName;

    @Schema(description = "退款状态")
    private String  refundStatusName;

    @Schema(description = "退款申请")
    private OrderFinanceAftersales aftersales;

    @Schema(description = "是否账期支付订单：0-否，1-是")
    private Integer isCreditOrder;

    @Schema(description = "账期支付金额（订单使用的账期额度）")
    private BigDecimal creditAmount;

    @Schema(description = "账期退款金额（本次售后应退的账期额度）")
    private BigDecimal creditRefundAmount;

    public void assembleData(List<OrderFinanceAftersales> orderFinanceAftersalesList) {
        this.refundStatusName = REFUND_STATUS_NAME.get(this.refundStatus);
        this.refundTypeName = REFUND_TYPE_NAME.get(this.refundType);

        this.aftersales = orderFinanceAftersalesList.stream()
                .filter(orderFinanceAftersales -> orderFinanceAftersales.getAftersaleId().equals(this.aftersaleId))
                .findFirst()
                .orElse(null);
    }

}
