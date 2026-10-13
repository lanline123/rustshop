package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 地区更新参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "地区更新参数")
public class RegionUpdateDTO extends RegionCreateDTO {
    @NotNull(message = "地区ID不能为空")
    @Schema(description = "地区ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;
}
