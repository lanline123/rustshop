package com.tigshop.bean.param.order;

import cn.hutool.core.lang.Assert;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.AftersalesTypeEnum;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Tigshop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "售后管理参数")
public class AftersalesEditParam {

    @Schema(description = "售后管理ID")
    private Integer id;

    @Schema(description = "状态")
    private Integer status;

    @Min(value = 0, message = "退款金额不能小于0")
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "回复")
    private String reply;

    @Schema(description = "退货地址")
    private String returnAddress;

    @Schema(description = "售后退货提示")
    private String returnGoodsTip;

    public void validParam(Aftersales aftersales) {

        if (AftersalesStatusEnum.REFUSE.getCode() == this.status) {
            Assert.notBlank(this.reply, () -> new GlobalException("拒绝请填原因"));
        }

        if (AftersalesStatusEnum.APPROVED_FOR_PROCESSING.getCode() == this.status) {
            Integer vendorId = aftersales.getVendorId();
            boolean isVendor = vendorId != null && vendorId < 1;
            if (AftersalesTypeEnum.RETURN.getCode() == aftersales.getAftersaleType() && isVendor) {
                Assert.notBlank(this.returnAddress, () -> new GlobalException("同意退货退款请填写详细退货地址：姓名、联系方式、具体地址"));
            }
        }

    }

    /**
     * @param vendorReviewRequired 该售后是否还需经供应商人工审核，由 service 层按供应商自身的
     *                             「售后审核」开关判定；为 true 时审核通过后流转至「待供应商审核」
     */
    public Aftersales createUpdateAftersales(Aftersales aftersales, boolean vendorReviewRequired) {

        Aftersales updateAftersales = Aftersales.builder()
                .aftersaleId(aftersales.getAftersaleId())
                .auditTime(StringUtils.getCurrentTime())
                .returnGoodsTip(this.returnGoodsTip)
                .refundAmount(this.refundAmount)
                .reply(this.reply)
                .returnAddress(this.returnAddress)
                .build();

        // 退货退款
        if (AftersalesTypeEnum.RETURN.getCode() == aftersales.getAftersaleType()) {
            if (vendorReviewRequired && aftersales.getStatus() != AftersalesStatusEnum.WAIT_FOR_SUPPLIER_AUDIT.getCode()){
                updateAftersales.setStatus(AftersalesStatusEnum.WAIT_FOR_SUPPLIER_AUDIT.getCode());
            } else {
                updateAftersales.setStatus(AftersalesStatusEnum.SEND_BACK.getCode());
            }
        }

        // 仅退款：不经供应商审核，管理员审批通过后直接完成，流转到财务审核
        if (AftersalesTypeEnum.PAYRETURN.getCode() == aftersales.getAftersaleType()) {
            updateAftersales.setStatus(AftersalesStatusEnum.COMPLETE.getCode());
            updateAftersales.setFinalTime(StringUtils.getCurrentTime());
        }

        return updateAftersales;
    }
}
