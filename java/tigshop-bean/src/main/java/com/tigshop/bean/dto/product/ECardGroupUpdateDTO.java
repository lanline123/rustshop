// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.product.ECardGroupConstants.*;

/**
  * 电子卡券组更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "电子卡券组参数")
public class ECardGroupUpdateDTO {
    @Schema(description = "电子卡券组ID")
    @NotNull(message = E_CARD_GROUP_ID_NOT_NULL)
    private Integer groupId;

    @Schema(description = "分组名称")
    @NotNull(message = GROUP_NAME_NOT_NULL)
    @Size(max = 255, message = GROUP_NAME_OVER_LENGTH)
    private String groupName;

    @Schema(description = "备注")
    private String remark;
}
