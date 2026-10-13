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
 * 平台端发送保证金预警参数。通过站内信（AdminMsg）发往目标持有者（店铺/供应商）。
 *
 * <p>owner 维度：ownerType 1店铺 2供应商，ownerId 店铺为 shopId、供应商为 vendorId。
 * 为向后兼容历史仅传 shopId 的调用，ownerType 缺省视为店铺(1)、ownerId 为空回退 shopId。</p>
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "发送保证金预警参数")
public class DepositSendWarningDTO {

    @Schema(description = "持有者类型 1店铺 2供应商（不传默认店铺）")
    private Integer ownerType;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId；不传则回退 shopId）")
    private Integer ownerId;

    @Schema(description = "店铺id（兼容字段，等价于 ownerType=店铺 时的 ownerId）")
    private Integer shopId;

    @Schema(description = "预警标题（不传则按待补缴金额自动生成）")
    private String title;

    @Schema(description = "预警内容（不传则按待补缴金额自动生成）")
    private String content;
}
