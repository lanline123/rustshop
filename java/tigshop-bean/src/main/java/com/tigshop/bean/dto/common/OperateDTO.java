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

import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * 操作参数（用于删除）
 *
 * @author Jayce
 * @create 2024年10月25日 14:15
 */
@Data
@Schema(description = "操作参数")
public class OperateDTO {

    @Schema(description = "id")
    @NotNull(message = ID_CANNOT_IS_NULL)
    private Integer id;

    @Schema(description = "备注")
    private String adminNote;

    @Schema(description = "页面类型")
    private Integer pageType;
}
