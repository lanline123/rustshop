// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员提现账号VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员提现账号参数")
public class WithdrawApplyAccountVO {
    @Schema(description = "账户id")
    private Integer accountId;

    @Schema(description = "账户名称")
    private String accountName;

    @Schema(description = "提现申请编号")
    private String accountNo;

    @Schema(description = "提现申请类型")
    private Integer accountType;

    @Schema(description = "提现申请类型名称")
    private String accountTypeName;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "identity")
    private String identity;

    @Schema(description = "用户id")
    private Integer userId;
}