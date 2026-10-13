package com.tigshop.bean.vo.config;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 状态list
 *
 * @author Jayce
 * @create 2024年10月22日 11:17
 */
@Getter
@Setter
@Schema(description = "运费模板列表返回")
public class ShippingTplListVO<T> {
    @Schema(description = "模板list")
    private T shippingTplList;

    public ShippingTplListVO(T shippingTplList) {
        this.shippingTplList = shippingTplList;
    }
}
