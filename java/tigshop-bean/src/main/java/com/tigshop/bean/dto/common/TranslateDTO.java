// **---------------------------------------------------------------------+
// ** 文件 --
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

import static com.tigshop.common.constant.ResultTextConstants.FIELD_VALUE_CANNOT_BE_NULL;

/**
 * @author Tigshop团队
 * @create 2025/6/6 15:37
 */
@Data
@Schema(description = "翻译参数")
public class TranslateDTO {
    @Schema(description = "字段名")
    @NotNull(message = FIELD_VALUE_CANNOT_BE_NULL)
    private String translationName;
}
