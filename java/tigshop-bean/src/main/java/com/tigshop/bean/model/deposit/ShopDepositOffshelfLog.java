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

/**
 * 店铺保证金下架日志表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_offshelf_log")
@Schema(description = "店铺保证金下架日志")
public class ShopDepositOffshelfLog {
    @TableId(value = "shop_deposit_offshelf_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金下架日志ID")
    private Integer shopDepositOffshelfId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "下架时间")
    private Long offTime;

    @Schema(description = "恢复时间")
    private Long restoreTime;

    @Schema(description = "创建时间")
    private Long addTime;
}
