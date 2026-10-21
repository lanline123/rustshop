package com.tigshop.bean.vo.order.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Apple Pay 支付确认返回。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "Apple Pay 支付确认返回")
public class ApplePayConfirmVO {

    @Schema(description = "支付状态；0-未支付/失败，1-已支付，2-处理中(异步待通知)")
    private Integer payStatus;

    @Schema(description = "收单通道交易号（成功/受理时）")
    private String transactionId;

    @Schema(description = "失败/处理中提示")
    private String failReason;

    public static ApplePayConfirmVO success(String transactionId) {
        ApplePayConfirmVO vo = new ApplePayConfirmVO();
        vo.setPayStatus(1);
        vo.setTransactionId(transactionId);
        return vo;
    }

    public static ApplePayConfirmVO fail(String failReason) {
        ApplePayConfirmVO vo = new ApplePayConfirmVO();
        vo.setPayStatus(0);
        vo.setFailReason(failReason);
        return vo;
    }

    /**
     * 处理中（异步结算受理成功，最终结果以异步通知为准）。
     */
    public static ApplePayConfirmVO processing(String transactionId, String message) {
        ApplePayConfirmVO vo = new ApplePayConfirmVO();
        vo.setPayStatus(2);
        vo.setTransactionId(transactionId);
        vo.setFailReason(message);
        return vo;
    }
}
