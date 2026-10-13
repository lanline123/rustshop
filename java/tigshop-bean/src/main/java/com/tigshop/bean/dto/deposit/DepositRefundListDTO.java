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
 * 店铺保证金退还审核列表查询参数（平台端）。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金退还审核列表查询参数")
public class DepositRefundListDTO extends BasePage {

    @Schema(description = "持有者类型 1店铺 2供应商；不传=全部")
    private Integer ownerType;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）；兼容原 shopId 入参")
    private Integer ownerId;

    @Schema(description = "店铺id（兼容入参，等价于 ownerId）")
    private Integer shopId;

    @Schema(description = "组织名称（模糊匹配，店铺匹店铺名/供应商匹供应商名）；不传=全部")
    private String ownerName;

    @Schema(description = "主体类型/商户性质 1个人 2企业（店铺取商户性质，供应商取供应商性质）；不传=全部")
    private Integer subjectType;

    @Schema(description = "退还类型 1部分退还 2全部退还")
    private Integer type;

    @Schema(description = "状态 0待审核 1已通过 2未通过")
    private Integer status;

    @Schema(description = "申请开始时间(秒级时间戳)")
    private Long startTime;

    @Schema(description = "申请结束时间(秒级时间戳)")
    private Long endTime;
}
