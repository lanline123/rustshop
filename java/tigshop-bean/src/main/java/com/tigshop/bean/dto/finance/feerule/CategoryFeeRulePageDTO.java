package com.tigshop.bean.dto.finance.feerule;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "费率规则分页")
public class CategoryFeeRulePageDTO extends BasePage {

    @NotNull(message = "适用对象类型不能为空")
    @Schema(description = "适用对象类型 1店铺 2供应商")
    private Integer targetType;

    @Schema(description = "状态 1启用 0禁用")
    private Integer status;
}
