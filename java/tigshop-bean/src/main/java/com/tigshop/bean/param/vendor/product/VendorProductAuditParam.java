package com.tigshop.bean.param.vendor.product;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.vendor.VendorProductAuditStateEnum;
import com.tigshop.bean.model.vendor.product.VendorProductAuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商商品审核参数
 *
 * @author kidd
 * @since 2025/7/14 17:28
 */
@Data
public class VendorProductAuditParam {

    @NotNull(message = "供应商商品ID不能为空")
    @Schema(description = "供应商商品ID")
    private Long vendorProductId;

    @NotNull(message = "商品审核状态不能为空")
    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    @Schema(description = "商品审核失败原因")
    private String auditFailReason;

    public void validate() {
        if (VendorProductAuditStateEnum.REJECT_AUDIT.getCode() == this.auditState) {
            Assert.isTrue(StrUtil.isNotBlank(this.auditFailReason), "商品审核失败原因不能为空");
        }
    }

    public VendorProductAuditLog createVendorProductAuditLog(Integer vendorId) {
        return VendorProductAuditLog.builder()
                .vendorProductId(this.vendorProductId)
                .auditState(this.auditState)
                .auditFailReason(this.auditFailReason)
                .vendorId(vendorId)
                .build();
    }
}
