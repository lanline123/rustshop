package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单商品model
 *
 * @author kidd
 * @create 2025/7/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_log")
@Schema(description = "订单商品")
public class OrderLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "流水号")
    private Integer logId;

    @Schema(description = "被操作的订单id")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "操作管理员（后台）")
    private Integer adminId;

    @Schema(description = "操作会员（前台）")
    private Integer userId;

    @Schema(description = "操作备注")
    private String description;

    @Schema(description = "操作时间")
    private Long logTime;

    @Schema(description = "店铺id")
    private Integer shopId;

}