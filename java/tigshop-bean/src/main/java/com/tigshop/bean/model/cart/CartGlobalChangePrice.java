package com.tigshop.bean.model.cart;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "cart_global_change_price")
public class CartGlobalChangePrice {
    @Schema(description = "用户id")
    @TableId
    private Integer userId;

    @Schema(description = "改价的值")
    private BigDecimal changePriceValue;

    @Schema(description = "改价类型")
    private Integer changePriceType;
}
