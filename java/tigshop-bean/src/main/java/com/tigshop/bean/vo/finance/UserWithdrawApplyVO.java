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

import java.math.BigDecimal;

/**
 * 提现申请VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "提现申请参数")
public class UserWithdrawApplyVO {

    @Schema(description = "提现申请ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "提现钱包：0-余额，1-佣金（D1107）")
    private Integer walletType;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "申请提现的金额")
    private BigDecimal amount;

    @Schema(description = "记录插入时间")
    private String addTime;

    @Schema(description = "完成时间")
    private Integer finishedTime;

    @Schema(description = "管理员的备注")
    private String postscript;

    @Schema(description = "处理状态，0：待处理，1：已完成，2：拒绝申请")
    private Integer status;
    @Schema(description = "处理状态")
    private String statusType;

    @Schema(description = "[JSON]提现人卡号等信息")
    private Object accountData;

    // 其他字段可以根据需求添加

    @Schema(description = "手续费")
    private BigDecimal handlingFee;

    @Schema(description = "手续费率")
    private BigDecimal handlingRate;
}