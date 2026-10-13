package com.tigshop.bean.dto.o2o.reservationtpl;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "预约模版详情")
public class ReservationTplDetailDTO {

    @NotBlank(message = "字段名称不能为空")
    @Schema(description = "字段名称")
    private String fieldName;

    @NotBlank(message = "字段自定义名称不能为空")
    @Schema(description = "字段自定义名称")
    private String customName;

    @NotNull(message = "是否必填不能为空")
    @Schema(description = "是否必填,1表示必填，0非必填")
    private Integer isRequired;

    @NotNull(message = "是否显示不能为空")
    @Schema(description = "是否显示,1表示展示,0不展示")
    private Integer isShow;
}
