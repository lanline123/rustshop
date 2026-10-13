package com.tigshop.bean.vo.order;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.order.OrderReservation;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单预约信息表
 * 
 * @author tigshop
 */
@Data
@Schema(description = "订单预约信息")
public class OrderReservationVO {

    @Schema(description = "ID")
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
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    // OrderReservation to OrderReservationVO
    public static OrderReservationVO toOrderReservationVO(OrderReservation orderReservation) {
        OrderReservationVO orderReservationVO = new OrderReservationVO();
        orderReservationVO.orderReservationsId = orderReservation.getOrderReservationsId();
        orderReservationVO.orderId = orderReservation.getOrderId();
        orderReservationVO.productReservationTimeId = orderReservation.getProductReservationTimeId();
        orderReservationVO.skuId = orderReservation.getSkuId();
        orderReservationVO.reservationDate = orderReservation.getReservationDate();
        orderReservationVO.startTime = orderReservation.getStartTime();
        orderReservationVO.endTime = orderReservation.getEndTime();
        orderReservationVO.timeSlotDescription = orderReservation.getTimeSlotDescription();
        orderReservationVO.addTime = orderReservation.getAddTime().toString();
        return orderReservationVO;
    }
}