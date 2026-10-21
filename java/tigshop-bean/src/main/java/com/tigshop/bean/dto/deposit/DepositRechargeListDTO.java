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
 * 店铺保证金充值审核列表查询参数（平台端）。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金充值审核列表查询参数")
public class DepositRechargeListDTO extends BasePage {

    @Schema(description = "持有者类型 1店铺 2供应商；不传=全部")
    private Integer ownerType;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）；兼容原 shopId 入参")
    private Integer ownerId;

    @Schema(description = "店铺id（兼容入参，等价于 ownerId）")
    private Integer shopId;

    @Schema(description = "组织名称（店铺名/供应商名模糊匹配）；不传=全部")
    private String ownerName;

    @Schema(description = "主体类型（商户性质）1个人 2企业；不传=全部（店铺按 merchant.type，供应商按 vendor.type）")
    private Integer subjectType;

    @Schema(description = "支付方式（收款账户类型）1银行卡 2支付宝 3微信；不传=全部")
    private Integer payeeAccountType;

    @Schema(description = "状态 0待审核 1已通过 2未通过")
    private Integer status;

    @Schema(description = "申请开始时间(秒级时间戳)")
    private Long startTime;

    @Schema(description = "申请结束时间(秒级时间戳)")
    private Long endTime;
}
