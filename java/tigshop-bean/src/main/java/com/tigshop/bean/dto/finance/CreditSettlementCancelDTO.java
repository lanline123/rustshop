package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 账期结算撤销DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "账期结算撤销参数")
public class CreditSettlementCancelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "结算ID不能为空")
    @Schema(description = "结算ID")
    private Integer settlementId;

    @Schema(description = "撤销原因")
    private String cancelReason;
}
