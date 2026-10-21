// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 会员提现账号model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_withdraw_account")
@Schema(description = "会员提现账号")
public class WithdrawApplyAccount implements Serializable {

    @TableId(value = "account_id", type = IdType.AUTO)
    @Schema(description = "会员提现账号ID")
    private Integer accountId;

    @Schema(description ="用户id")
    private Integer userId;

    @Schema(description ="提现账号类型")
    private Integer accountType;

    @Schema(description ="姓名")
    private String accountName;

    @Schema(description ="银行卡号或账号")
    private String accountNo;

    @Schema(description ="身份证号")
    private String identity;

    @Schema(description ="银行名称")
    private String bankName;
}