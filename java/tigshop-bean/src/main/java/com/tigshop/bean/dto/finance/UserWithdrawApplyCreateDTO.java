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
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现申请创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "提现申请参数")
public class UserWithdrawApplyCreateDTO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "申请提现的金额")
    private BigDecimal amount;

    @Schema(description = "记录插入时间")
    private Integer addTime;

    @Schema(description = "完成时间")
    private Integer finishedTime;

    @Schema(description = "管理员的备注")
    @Size(max = 80, message = "管理员的备注不能超过80个字符")
    private String postscript;

    @Schema(description = "处理状态，0：待处理，1：已完成，2：拒绝申请")
    private Integer status;

    @Schema(description = "[JSON]提现人卡号等信息")
    private UserWithdrawApplyCreateAccountDataDTO accountData;
}
