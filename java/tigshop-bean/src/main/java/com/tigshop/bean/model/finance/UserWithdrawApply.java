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

import java.math.BigDecimal;

/**
 * 用户提现申请表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("user_withdraw_apply")
@Schema(description = "用户提现申请表")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithdrawApply {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "提现申请id")
    private Integer id;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "提现钱包：0-余额，1-佣金（D1107）")
    private Integer walletType;

    @Schema(description = "申请提现的金额")
    private BigDecimal amount;

    @Schema(description = "记录插入时间")
    private Long addTime;

    @Schema(description = "完成时间")
    private Long finishedTime;

    @Schema(description = "管理员的备注")
    private String postscript;

    @Schema(description = "处理状态，0：待处理，1：已完成，2：拒绝申请")
    private Integer status;

    @Schema(description = "[JSON]提现人卡号等信息")
    private String accountData;
}