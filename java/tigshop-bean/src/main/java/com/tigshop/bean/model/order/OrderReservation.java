package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单预约信息表
 * 
 * @author tigshop
 */
@Data
@TableName("order_reservations")
@Schema(description = "订单预约信息")
public class OrderReservation {

    @TableId(value = "order_reservations_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer orderReservationsId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "预约时间段ID")
    private Integer productReservationTimeId;

    @Schema(description = "SKU ID")
    private Integer skuId;

    @Schema(description = "预约日期")
    private String reservationDate;

    @Schema(description = "预约开始时间")
    private String startTime;

    @Schema(description = "预约结束时间")
    private String endTime;

    @Schema(description = "时间段描述")
    private String timeSlotDescription;

    @Schema(description = "创建时间")
    private Long addTime;
}