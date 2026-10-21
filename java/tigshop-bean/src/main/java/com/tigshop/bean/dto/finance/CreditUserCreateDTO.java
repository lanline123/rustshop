package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建授信会员DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "创建授信会员参数")
public class CreditUserCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择会员")
    @Schema(description = "会员ID（必须是已通过认证的会员）")
    private Integer userId;

    @NotNull(message = "请输入信用额度")
    @DecimalMin(value = "0.01", message = "信用额度必须大于0")
    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    @NotNull(message = "请选择结算账期类型")
    @Schema(description = "结算账期类型：1-月结，2-季结")
    private Integer settlementType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
