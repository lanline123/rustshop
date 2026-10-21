package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 账期结算提交DTO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "账期结算提交参数")
public class CreditSettlementSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择会员")
    @Schema(description = "会员ID")
    private Integer userId;

    @NotBlank(message = "请输入回款流水单号")
    @Schema(description = "回款流水单号")
    private String settlementSn;

    @NotBlank(message = "请选择结算时间")
    @Schema(description = "结算时间")
    private String settlementTime;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "请选择要结算的订单")
    @Schema(description = "要结算的订单ID列表")
    private List<Integer> orderIds;
}
