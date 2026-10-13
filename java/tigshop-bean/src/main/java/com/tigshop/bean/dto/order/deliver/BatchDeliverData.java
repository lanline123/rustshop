package com.tigshop.bean.dto.order.deliver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author zx
 * @create 2024年12月16日 13:47
 */
@Data
public class BatchDeliverData {

    @Schema(description = "数据")
    private List<DeliverDataList> deliverData;

    @Schema(description = "物流ID")
    private Integer logisticsId;

    @Schema(description = "订单id")
    private Integer id;

    @Schema(description = "发货方法")
    private Integer shippingMethod;

}
