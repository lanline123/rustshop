package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台保证金收款账户VO。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "平台保证金收款账户")
public class DepositPayeeAccountVO {

    @Schema(description = "账户ID")
    private Integer id;

    @Schema(description = "账户类型 1银行卡 2支付宝 3微信")
    private Integer accountType;

    @Schema(description = "账户类型文本")
    private String accountTypeText;

    @Schema(description = "账户名称（户名）")
    private String accountName;

    @Schema(description = "账户号")
    private String accountNo;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "银行支行")
    private String bankBranch;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "添加时间（秒级时间戳）")
    private Long addTime;

    @Schema(description = "更新时间（秒级时间戳）")
    private Long updateTime;
}
