// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单预售信息表
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单预售信息表")
@TableName("order_presale")
public class OrderPresale implements Serializable {

    @TableId(value = "presale_order_id", type = IdType.AUTO)
    @Schema(description = "预售订单ID")
    private Integer presaleOrderId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "销售模式：1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "定金金额（定金预售）")
    private BigDecimal depositAmount;

    @Schema(description = "尾款金额（定金预售）")
    private BigDecimal finalPaymentAmount;

    @Schema(description = "定金支付时间")
    private Long depositPaidTime;

    @Schema(description = "尾款开始支付时间（实际计算值）")
    private Long finalPaymentStartTime;

    @Schema(description = "尾款支付时间")
    private Long finalPaymentPaidTime;

    @Schema(description = "预售状态：0-待支付定金，1-待支付尾款，2-已完成支付")
    private Integer presaleStatus;

    @Schema(description = "发货天数（相对时间）")
    private Integer deliveryDays;

    @Schema(description = "发货日期（指定日期）")
    private Long deliveryDate;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}

