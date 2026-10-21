// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保证金应缴调整记录行
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "保证金应缴调整记录行")
public class DepositAdjustLogVO {

    @Schema(description = "调整记录id")
    private Integer shopDepositAdjustId;

    @Schema(description = "持有者类型 DepositOwnerType：1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者类型文案（店铺/供应商）")
    private String ownerTypeText;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）")
    private Integer ownerId;

    @Schema(description = "持有者名称（店铺名或供应商名，统一展示）")
    private String ownerName;

    @Schema(description = "店铺id（兼容字段，仅店铺行填充）")
    private Integer shopId;

    @Schema(description = "调整前应缴")
    private BigDecimal beforeRequired;

    @Schema(description = "调整后应缴")
    private BigDecimal afterRequired;

    @Schema(description = "来源 1按类目最高 2手动固定")
    private Integer source;

    @Schema(description = "来源文案")
    private String sourceText;

    @Schema(description = "操作管理员id")
    private Integer adminId;

    @Schema(description = "操作管理员名称")
    private String adminName;

    @Schema(description = "操作管理员头像")
    private String adminAvatar;

    @Schema(description = "调整原因")
    private String reason;

    @Schema(description = "时间")
    private String addTime;
}
