// **---------------------------------------------------------------------+
// ** 文件 -- 更新字段信息
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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
@Schema(description = "更新字段信息")
public class UpdateFieldDTO {

    @Schema(description = "id")
    @NotNull(message = ID_CANNOT_IS_NULL)
    private Integer id;

    @Schema(description = "字段名")
    @NotNull(message = FIELD_NAME_CANNOT_BE_NULL)
    private String field;

    @Schema(description = "值")
    @NotNull(message = FIELD_VALUE_CANNOT_BE_NULL)
    private String val;
}
