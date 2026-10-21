package com.tigshop.bean.dto.order.deliver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author zx
 * @create 2024年12月16日 13:47
 */
@Data
public class DeliverDataList {

    @Schema(description = "数据")
    private List<DeliverInfo> deliverInfo;

    @Schema(description = "订单id")
    private Integer id;

    @Schema(description = "快递单号")
    private String trackingNo;

}
