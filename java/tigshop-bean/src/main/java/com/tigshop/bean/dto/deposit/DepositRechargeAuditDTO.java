// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺保证金充值审核参数（平台端）。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金充值审核参数")
public class DepositRechargeAuditDTO {

    @Schema(description = "充值单id")
    private Integer id;

    @Schema(description = "是否通过 true通过入账 false驳回")
    private Boolean pass;

    @Schema(description = "审核备注/驳回原因")
    private String auditRemark;
}
