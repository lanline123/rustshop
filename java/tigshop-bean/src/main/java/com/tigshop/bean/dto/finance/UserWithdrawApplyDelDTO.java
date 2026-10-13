package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会员提现账号删除
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "会员提现账号删除")
public class UserWithdrawApplyDelDTO {
    @Schema(description = "提现账号ID")
    private Integer accountId;
}
