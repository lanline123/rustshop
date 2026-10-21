package com.tigshop.bean.param.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "审核买家秀参数")
public class BuyerShowAuditParam {
    @NotNull(message = "买家秀id不能为空")
    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态，1：通过，2：不通过")
    private Integer status;
}
