package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新授信会员DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "更新授信会员参数")
public class CreditUserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "授信会员ID不能为空")
    @Schema(description = "授信会员ID")
    private Integer creditUserId;

    @DecimalMin(value = "0.01", message = "信用额度必须大于0")
    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    @Schema(description = "结算账期类型：1-月结，2-季结")
    private Integer settlementType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
