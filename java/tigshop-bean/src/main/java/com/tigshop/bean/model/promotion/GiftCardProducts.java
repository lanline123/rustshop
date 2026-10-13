package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡兑换商品
 *
 * @author Tigshop项目组
 * @TableName gift_card_products
 */
@TableName(value = "gift_card_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftCardProducts implements Serializable {

    @Schema(description = "卡组商品id")
    @TableId(type = IdType.AUTO)
    private Integer giftProductId;

    @Schema(description = "卡组ID")
    private Integer cardGroupsId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "SKU ID")
    private Integer skuId;

}
