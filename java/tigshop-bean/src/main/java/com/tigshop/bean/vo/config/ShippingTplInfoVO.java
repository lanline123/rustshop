package com.tigshop.bean.vo.config;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 运费模板
 *
 * @author Jayce
 * @create 2024年10月22日 11:17
 */
@Getter
@Setter
@Schema(description = "运费模板")
public class ShippingTplInfoVO<T> {
    @Schema(description = "运费模板")
    private T shippingTplInfo;

    public ShippingTplInfoVO(T shippingTplInfo) {
        this.shippingTplInfo = shippingTplInfo;
    }
}
