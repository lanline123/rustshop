package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 供应商商品审核记录
 *
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "vendor_product_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductAuditLog extends BasePO implements Serializable {

    @Schema(description = "供应商商品 ID")
    private Long vendorProductId;

    @Schema(description = "审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    @Schema(description = "审核失败原因")
    private String auditFailReason;

    @Schema(description = "供应商 ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}