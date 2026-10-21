package com.tigshop.bean.vo.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockAdjustRecordDetailVO {

    @Schema(description = "调整类型")
    private Integer adjustType;

    @Schema(description = "调整数量")
    private Integer quantity;

    @Schema(description = "创建人")
    private String addBy;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "调整前数量")
    private Integer changeBeforeQuantity;

    @Schema(description = "调整后数量")
    private Integer changeAfterQuantity;
}
