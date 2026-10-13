// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.promotion.SignInSettingConstants.NAME_NOT_NULL;
import static com.tigshop.common.constant.promotion.SignInSettingConstants.NAME_OVER_LENGTH;
import static com.tigshop.common.constant.promotion.SignInSettingConstants.POINTS_NOT_NULL;

/**
 * 积分签到新增参数。
 */
@Data
@Schema(description = "积分签到新增参数")
public class SignInSettingCreateDTO {

    @Schema(description = "积分签到名称")
    @NotBlank(message = NAME_NOT_NULL)
    @Size(max = 30, message = NAME_OVER_LENGTH)
    private String name;

    @Schema(description = "赠送积分")
    @NotNull(message = POINTS_NOT_NULL)
    @Min(value = 1, message = POINTS_NOT_NULL)
    private Integer points;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序不能小于0")
    private Integer sortOrder;
}
