package com.tigshop.bean.dto.user;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户资金管理
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户资金管理")
public class UserFundManagementDTO {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "变动说明")
    private String changeDesc;

    @Schema(description = "金额类型")
    private Integer typeBalance;

    @Schema(description = "用户余额")
    private BigDecimal balance;

    @Schema(description = "冻结金额类型")
    private Integer typeFrozenBalance;

    @Schema(description = "冻结金额")
    private BigDecimal frozenBalance;

    @Schema(description = "积分类型")
    private Integer typePoints;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "成长积分类型")
    private Integer typeGrowthPoints;

    @Schema(description = "成长积分")
    private Integer growthPoints;

    public void validParam() {
        Assert.isTrue(StrUtil.isNotBlank(changeDesc), () -> new GlobalException("请填写资金变动说明"));

        boolean changeFlag = balance != null || frozenBalance != null || points != null || growthPoints != null;
        Assert.isTrue(changeFlag, () -> new GlobalException("没有账户变动"));
        validateAmount(balance, typeBalance, "余额");
        validateAmount(frozenBalance, typeFrozenBalance, "冻结金额");
    }

    private void validateAmount(BigDecimal amount, Integer type, String label) {
        if (amount == null) {
            return;
        }
        Assert.isTrue(amount.signum() >= 0, () -> new GlobalException(label + "不能为负数"));
        Assert.isTrue(Integer.valueOf(1).equals(type) || Integer.valueOf(2).equals(type),
                () -> new GlobalException(label + "变动类型错误"));
    }
}
