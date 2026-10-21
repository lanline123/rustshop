package com.tigshop.bean.feign.shipping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物流信息请求参数
 *
 * @author Tigshop团队
 * @create 2024年12月27日 14:19
 */
@Data
@Schema(description = "物流信息请求参数")
public class ShippingParam {
    @Schema(description = "api_key")
    private String apiKey;

    @Schema(description = "code")
    private String code;

    @Schema(description = "number")
    private String number;
}
