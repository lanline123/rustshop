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

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺/供应商端「款项审核」合并列表查询参数。owner 取自登录态，不在入参。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺/供应商端款项审核合并列表查询参数")
public class DepositPaymentAuditListDTO extends BasePage {

    @Schema(description = "审核类型 1充值 2退还；不传=全部混合")
    private Integer auditType;

    @Schema(description = "审核状态 0待审核 1已通过 2未通过；不传=全部")
    private Integer status;

    @Schema(description = "提交开始时间(秒级时间戳)")
    private Long startTime;

    @Schema(description = "提交结束时间(秒级时间戳)")
    private Long endTime;
}
