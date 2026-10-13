package com.tigshop.bean.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.ResultTextConstants.*;

/**
 * @author Jayce
 * @create 2024/10/8 16:07
 */
@Data
@Schema(description = "更新字段信息(产品)")
public class UpdateFieldProductDTO {
    @Schema(description = "id")
    @NotNull(message = ID_CANNOT_IS_NULL)
    private Integer id;

    @Schema(description = "字段名")
    @NotNull(message = FIELD_NAME_CANNOT_BE_NULL)
    private String field;

    @Schema(description = "值")
    @NotNull(message = FIELD_VALUE_CANNOT_BE_NULL)
    private Object val;
}
