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
 * 店铺保证金账户表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit")
@Schema(description = "店铺保证金账户")
public class ShopDeposit {
    @TableId(value = "shop_deposit_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金账户ID")
    private Integer shopDepositId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "已缴纳保证金金额")
    private BigDecimal paid;

    @Schema(description = "手动应缴覆盖值,NULL=按规则实时算")
    private BigDecimal requiredManual;

    @Schema(description = "上次应缴快照(类目变更diff用,记录调整前后值)")
    private BigDecimal requiredSnapshot;

    @Schema(description = "收取节点缓冲到期时间,0未触发")
    private Long collectDueTime;

    @Schema(description = "补缴缓冲到期时间,0未触发")
    private Long supplementDueTime;

    @Schema(description = "收取是否已激活:0未激活(首单前) 1已激活")
    private Integer collectActivated;

    @Schema(description = "扣罚状态")
    private Integer penaltyStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
