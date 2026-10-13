package com.tigshop.bean.param.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.merchant.MerchantApplyConstants.*;

/**
 * 入驻申请审核数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "入驻申请审核数据对象")
public class MerchantApplyAuditParam {

    @Schema(description = "主键")
    @NotNull(message = MERCHANT_APPLY_ID_NOT_NULL)
    private Integer merchantApplyId;

    @Schema(description = "审核状态")
    @NotNull(message = MERCHANT_APPLY_STATUS_NOT_NULL)
    private Integer status;

    @Schema(description = "审核备注")
    private String auditRemark;
}
