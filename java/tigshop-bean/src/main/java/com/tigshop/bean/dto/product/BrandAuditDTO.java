package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class BrandAuditDTO {
    @NotNull(message = "品牌id不能为空")
    @Schema(description = "品牌id")
    private Integer brandId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态:0:待审核,1:审核通过,2:已拒绝")
    private Integer status;

    @Schema(description = "拒绝原因")
    private String rejectRemark;
}
