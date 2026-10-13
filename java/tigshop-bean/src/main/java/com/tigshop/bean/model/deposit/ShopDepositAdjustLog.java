// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.deposit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺保证金应缴调整记录表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_adjust_log")
@Schema(description = "店铺保证金应缴调整记录")
public class ShopDepositAdjustLog {
    @TableId(value = "shop_deposit_adjust_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金应缴调整记录ID")
    private Integer shopDepositAdjustId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "调整前应缴")
    private BigDecimal beforeRequired;

    @Schema(description = "调整后应缴")
    private BigDecimal afterRequired;

    @Schema(description = "来源 1按类目最高 2手动固定(DepositAdjustSource)")
    private Integer source;

    @Schema(description = "操作管理员ID")
    private Integer adminId;

    @Schema(description = "调整原因")
    private String reason;

    @Schema(description = "创建时间")
    private Long addTime;
}
