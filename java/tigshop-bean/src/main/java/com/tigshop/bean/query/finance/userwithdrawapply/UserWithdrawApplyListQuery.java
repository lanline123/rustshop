package com.tigshop.bean.query.finance.userwithdrawapply;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 体现列表dto
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "提现列表参数")
public class UserWithdrawApplyListQuery extends BasePage {

    @Schema(description = "提现类型")
    private Integer accountType;

    @Schema(description = "提现状态")
    private Integer accountId;
}
