// **---------------------------------------------------------------------+
// ** 文件 -- UserBalanceLog.java
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户余额日志模型
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_balance_log")
@Schema(description = "用户余额日志")
public class UserBalanceLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "余额ID")
    private Integer logId;

    @Schema(description = "用户ID")
    private Integer userId;

    @TableField(exist = false)
    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "更新前余额")
    private BigDecimal beforeBalance;

    @Schema(description = "增加或减少的余额")
    private BigDecimal balance;

    @Schema(description = "更新后余额")
    private BigDecimal afterBalance;

    @Schema(description = "被冻结的余额")
    private BigDecimal frozenBalance;

    @Schema(description = "增加或减少之前的被冻结的余额")
    private BigDecimal beforeFrozenBalance;

    @Schema(description = "增加或减少之后的被冻结的余额")
    private BigDecimal afterFrozenBalance;

    @Schema(description = "该笔操作发生的时间（Unix时间戳）")
    private Long changeTime;

    @Schema(description = "该笔操作的备注")
    private String changeDesc;

    @Schema(description = "类型，1：增加，2：减少")
    private Integer changeType;

    @Schema(description = "钱包类型：0-余额账户，1-佣金账户（D1107）")
    private Integer walletType;
}
