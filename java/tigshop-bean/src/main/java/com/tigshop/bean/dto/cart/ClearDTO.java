package com.tigshop.bean.dto.cart;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;



/**
 * @author wzh
 */
@Data
@Schema(description = "清楚购物车接口参数")
public class ClearDTO {

    @Schema(description = "店铺ID")
    private Integer shopId;

}
