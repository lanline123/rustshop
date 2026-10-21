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
import lombok.Data;

/**
 * 提现申请创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "提现申请参数")
public class UserWithdrawApplyCreateAccountDataDTO {
    @Schema(description = "用户ID")
    private String accountName;

    @Schema(description = "账户号码")
    private String accountNo;

    @Schema(description = "账户类型")
    private Integer accountType;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "身份证号码或唯一标识")
    private String identity;
}
