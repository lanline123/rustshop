// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.salesman;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存日志model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("salesman_order")
@Schema(description = "分销订单")
public class SalesmanOrder {

    @TableId(value = "salesman_order_id", type = IdType.AUTO)
    @Schema(description = "分销业绩订单结算ID")
    private Integer salesmanOrderId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "收益总额")
    private BigDecimal amount;

    @Schema(description = "结算状态0待结算1已结算2已失效订单")
    private Integer status;

    @Schema(description = "生成时间")
    private Long addTime;

    @Schema(description = "订单明细ID")
    private Integer itemId;

    @Schema(description = "商品运营数据")
    private String salesmanProductData;

    @Schema(description = "订单商品金额")
    private BigDecimal orderAmount;

    @Schema(description = "结算配置数据")
    private String salesmanSettlementData;

    @Schema(description = "结算时间")
    private Long settlementTime;

    @Schema(description = "商品id")
    private Integer productId;
}