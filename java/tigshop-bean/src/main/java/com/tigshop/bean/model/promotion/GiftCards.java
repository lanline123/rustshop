package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡信息
 *
 * @author Tigshop项目组
 * @TableName gift_cards
 */
@TableName(value = "gift_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftCards implements Serializable {

    @Schema(description = "卡id")
    @TableId(type = IdType.AUTO)
    private Integer giftCardsId;

    @Schema(description = "所属卡组ID")
    private Integer cardGroupsId;

    @Schema(description = "卡号")
    private String cardNumber;

    @Schema(description = "卡密码")
    private String cardPassword;

    @Schema(description = "卡序列号")
    private String giftCardSn;

    @Schema(description = "1-可用 0-不可用")
    private Integer isAvailable;

    @Schema(description = "0-未使用 1-已使用 2-已过期")
    private Integer status;

    @Schema(description = "使用时间")
    private Long usedTime;

    @Schema(description = "使用者ID")
    private Integer userId;

    @Schema(description = "领取卡用户")
    private Integer claimedUserId;

    @Schema(description = "领取状态;0=不可领取，1=可领取，2=已领取")
    private Integer claimedStatus;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "过期时间")
    private Long expiredTime;

    @Schema(description = "下单时间")
    private Long orderTime;

}
