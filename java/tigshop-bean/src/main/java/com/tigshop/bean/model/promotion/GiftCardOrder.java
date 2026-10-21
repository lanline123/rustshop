package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单卡关联表
 *
 * @author Tigshop项目组
 * @TableName order_card
 */
@TableName(value = "gift_card_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftCardOrder implements Serializable {

    @Schema(description = "订单卡id")
    @TableId(type = IdType.AUTO)
    private Integer orderCardId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "卡id")
    private Integer cardId;
}
