package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单配置信息数据
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单配置信息数据")
public class OrderConfigDataDTO {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "天数设置")
    private Integer useDay;
}
