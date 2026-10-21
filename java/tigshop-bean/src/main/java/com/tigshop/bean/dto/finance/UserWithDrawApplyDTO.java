package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员提现账号申请参数
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "会员提现账号申请参数")
public class UserWithDrawApplyDTO {
    @Schema(description = "提现金额", required = true)
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "提现金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "账户数据")
    private AccountDataDTO accountData;

    @Data
    @Schema(description = "账户数据")
    public static class AccountDataDTO {
        @Schema(description = "提现类型")
        private Integer accountType;

        @Schema(description = "提现名称")
        private String accountName;

        @Schema(description = "提现账号")
        private String accountNo;

        @Schema(description = "开户行")
        private String bankName;
    }
}
