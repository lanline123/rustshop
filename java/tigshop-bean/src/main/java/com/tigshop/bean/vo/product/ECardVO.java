package com.tigshop.bean.vo.product;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.product.ECard;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电子卡券VO
 *
 * @author kidd
 * @create 2025/7/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "电子卡券参数")
public class ECardVO {

    // *** ECard ***

    @Schema(description = "卡券分组id")
    private Integer cardId;

    @Schema(description = "卡券分组id")
    private Integer groupId;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡号密码")
    private String cardPwd;

    @Schema(description = "0:未使用,1:已使用")
    private Integer isUse;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单item_id")
    private Integer orderItemId;

    // *** ECard ***

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "添加时间")
    private String addTime;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "更新时间")
    private String upTime;

    public ECardVO(ECard eCard) {
        this.cardId = eCard.getCardId();
        this.groupId = eCard.getGroupId();
        this.cardNumber = eCard.getCardNumber();
        this.cardPwd = eCard.getCardPwd();
        this.isUse = eCard.getIsUse();
        this.orderId = eCard.getOrderId();
        this.orderItemId = eCard.getOrderItemId();

        this.addTime = eCard.getAddTime().toString();
        this.upTime = eCard.getUpTime().toString();
    }
}