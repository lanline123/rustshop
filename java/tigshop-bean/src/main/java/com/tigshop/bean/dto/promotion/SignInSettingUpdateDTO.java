// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.promotion.SignInSettingConstants.*;
import static com.tigshop.common.constant.promotion.SignInSettingConstants.POINTS_NOT_NULL;

/**
  * 积分签到更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "积分签到参数")
public class SignInSettingUpdateDTO {
    @Schema(description = "积分签到ID")
    @NotNull(message = SIGN_IN_SETTING_ID_NOT_NULL)
    private Integer id;

    @Schema(description ="积分签到名称")
    @NotBlank(message = NAME_NOT_NULL)
    @Size(max = 30, message = NAME_OVER_LENGTH)
    private String name;

    @Schema(description ="赠送积分")
    // 数值大于0的注解
    @NotNull(message = POINTS_NOT_NULL)
    @Min(value = 1, message = POINTS_NOT_NULL)
    private Integer points;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序不能小于0")
    private Integer sortOrder;
}
