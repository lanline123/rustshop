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

import java.math.BigDecimal;

/**
 * 平台端手动调整应缴参数。
 *
 * <p>adjustType=1（按类目最高）将清空 requiredManual，应缴恢复为按规则实时算；
 * adjustType=2（手动固定）将 requiredManual 设为 requiredManual 字段值，永久生效直到再次改回自动。</p>
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "手动调整应缴参数")
public class DepositAdjustDTO {

    @Schema(description = "持有者类型 DepositOwnerType：null=店铺(兼容) 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId；为空时回退使用 shopId）")
    private Integer ownerId;

    @Schema(description = "店铺id（兼容字段；historical 仅传 shopId 时视为店铺维度）")
    private Integer shopId;

    @Schema(description = "调整方式 DepositAdjustSource：1按类目最高(清空手动值) 2手动固定")
    private Integer adjustType;

    @Schema(description = "手动固定应缴金额（adjustType=2 时必填，且不可为负）")
    private BigDecimal requiredManual;

    @Schema(description = "调整原因")
    private String reason;
}
