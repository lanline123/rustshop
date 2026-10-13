// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺资金变化model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("shop_account_log")
@Schema(description = "店铺资金变化")
public class Account {
    @TableId(value = "shop_account_log_id", type = IdType.AUTO)
    @Schema(description = "店铺资金变化ID")
    private Integer shopAccountLogId;

    @Schema(description ="添加时间")
    private Long addTime;

    @Schema(description ="店铺资金")
    private BigDecimal shopMoney;

    @Schema(description ="店铺冻结资金")
    private BigDecimal frozenMoney;


    @Schema(description ="1.店铺资金增加 2提现")
    private Integer type;

    @Schema(description ="备注")
    private String remarks;

    @Schema(description ="现店铺资金")
    private BigDecimal newShopMoney;

    @Schema(description ="现店铺冻结资金")
    private BigDecimal newFrozenMoney;

    @Schema(description ="店铺id")
    private Integer shopId;

    @Schema(description ="新老数据兼容，0:旧版本，1:新版本")
    private Integer isNew;

    @Schema(description = "资金账户类型：1 线下余额，2 线上渠道资金")
    private Integer balanceType;

    @Schema(description = "线上资金渠道：WECHAT/HUIFU/ALIPAY")
    private String payChannel;

    @Schema(description = "资金变动金额，历史空值按旧日志规则计算")
    private BigDecimal changeAmount;

    @Schema(description = "资金事件唯一键，用于回调及重试幂等")
    private String eventKey;
}
