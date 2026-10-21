package com.tigshop.bean.dto.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台保证金收款账户创建/更新参数。
 *
 * <p>平台收款账户为平台级数据，独立表 shop_deposit_payee_account 存储。</p>
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "平台保证金收款账户参数")
public class DepositPayeeAccountDTO {

    @Schema(description = "账户ID（更新时必填）")
    private Integer id;

    @Schema(description = "账户类型 1银行卡 2支付宝 3微信")
    private Integer accountType;

    @Schema(description = "账户名称（户名）")
    private String accountName;

    @Schema(description = "账户号")
    private String accountNo;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "银行支行")
    private String bankBranch;

    @Schema(description = "状态 1启用 0停用（不传默认启用）")
    private Integer status;

    @Schema(description = "排序（默认0）")
    private Integer sortOrder;
}
