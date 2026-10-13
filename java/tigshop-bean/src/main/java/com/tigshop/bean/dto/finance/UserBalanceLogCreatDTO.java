// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 余额日志列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "余额日志参数")
public class UserBalanceLogCreatDTO extends BasePage {

    @Schema(description = "用户ID")
    private Integer userId;

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

    @Schema(description = "该笔操作的备注")
    private String changeDesc;

    @Schema(description = "类型，1：增加，2：减少")
    private Integer changeType;

    @Schema(description = "钱包类型：0-余额账户，1-佣金账户（D1107）")
    private Integer walletType;
}
