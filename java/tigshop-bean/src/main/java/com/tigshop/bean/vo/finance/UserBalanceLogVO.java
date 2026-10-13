// **---------------------------------------------------------------------+
// ** 文件 -- UserBalanceLogVO.java
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.core.BigDecimalSerializer;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用户余额日志视图对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户余额日志参数")
public class UserBalanceLogVO {

    @Schema(description = "余额ID")
    private Integer logId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "增加或减少的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal balance;

    @Schema(description = "增加或减少之前的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal beforeBalance;

    @Schema(description = "增加或减少之后的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal afterBalance;

    @Schema(description = "被冻结的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal frozenBalance;

    @Schema(description = "增加或减少之前的被冻结的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal beforeFrozenBalance;

    @Schema(description = "增加或减少之扣的被冻结的余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal afterFrozenBalance;

    @Schema(description = "该笔操作发生的时间（Unix时间戳）")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String changeTime;

    @Schema(description = "该笔操作的备注")
    @JsonTranslate(dataType = 0)
    private String changeDesc;

    @Schema(description = "类型，1：增加，2：减少")
    private Integer changeType;

    @Schema(description = "类型，1：增加，2：减少")
    private String changeTypeName;

    public UserBalanceLogVO(UserBalanceLog log, User user) {
        this.logId = log.getLogId();
        this.userId = log.getUserId();
        this.beforeBalance = log.getBeforeBalance();
        this.balance = log.getBalance();
        this.frozenBalance = log.getFrozenBalance();
        this.changeTime = TigUtils.handelTime(log.getChangeTime());
        this.changeDesc = log.getChangeDesc();
        this.changeType = log.getChangeType();

        switch (log.getChangeType()) {
            case 1:
                this.changeTypeName = "增加";
                break;
            case 2:
                this.changeTypeName = "减少";
                break;
            case 99:
                this.changeTypeName = "其他";
                break;
            default:
                this.changeTypeName = "未知";
                break;
        }

        this.beforeFrozenBalance = getBeforeFrozenBalance(user);
    }


    // 获取变动前冻结余额的逻辑
    public BigDecimal getBeforeFrozenBalance(User user) {
        if (user == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentFrozenBalance = user.getFrozenBalance();

        if (changeType == 1) {
            return currentFrozenBalance.subtract(frozenBalance).setScale(2, RoundingMode.HALF_UP);
        } else {
            return currentFrozenBalance.add(frozenBalance).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
