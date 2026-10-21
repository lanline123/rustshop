package com.tigshop.bean.dto.finance.feerule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "费率规则状态更新")
public class CategoryFeeRuleUpdateStatusDTO {

    @NotNull(message = "规则ID不能为空")
    private Integer id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
