// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
  * 提现申请更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "提现申请参数")
public class UserWithdrawApplyUpdateDTO {

    @NotNull(message = "提现申请id不能为空")
    @Schema(description = "提现申请ID")
    private Integer id;

    @Size(max = 80, message = "管理员备注最多80个字符")
    @Schema(description = "管理员的备注")
    private String postscript;

    @Schema(description = "处理状态，0：待处理，1：已完成，2：拒绝申请")
    private Integer status;

}
