// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.finance.withdrawapply;

import com.tigshop.bean.model.finance.WithdrawApplyAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 会员提现账号创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "会员提现账号参数")
public class WithdrawApplyAccountCreateParam {

    @Schema(description = "提现类型")
    private Integer accountType;

    @NotBlank(message = "提现名称不能为空")
    @Schema(description = "提现名称")
    private String accountName;

    @NotBlank(message = "提现账号不能为空")
    @Schema(description = "提现账号")
    private String accountNo;

    @NotBlank(message = "身份证不能为空")
    @Schema(description = "身份证")
    private String identity;

    @Schema(description = "开户行")
    private String bankName;

    public WithdrawApplyAccount createWithdrawApplyAccount(Integer userId) {
        return WithdrawApplyAccount.builder()
                .userId(userId)
                .accountType(this.accountType)
                .accountName(this.accountName)
                .accountNo(this.accountNo)
                .identity(this.identity)
                .bankName(this.bankName)
                .build();
    }
}
