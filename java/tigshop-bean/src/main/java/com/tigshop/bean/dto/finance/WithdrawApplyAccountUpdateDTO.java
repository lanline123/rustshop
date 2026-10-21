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
import lombok.Data;

import static com.tigshop.common.constant.finance.WithdrawApplyAccountConstants.WITHDRAW_APPLY_ACCOUNT_ID_NOT_NULL;

/**
  * 会员提现账号更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "会员提现账号参数")
public class WithdrawApplyAccountUpdateDTO {
    @Schema(description = "提现账号ID")
    private Integer accountId;

    @Schema(description = "提现类型")
    private Integer accountType;

    @Schema(description = "提现名称")
    private String accountName;

    @Schema(description = "提现账号")
    private String accountNo;

    @Schema(description = "身份证")
    private String identity;

    @Schema(description = "开户行")
    private String bankName;
}
