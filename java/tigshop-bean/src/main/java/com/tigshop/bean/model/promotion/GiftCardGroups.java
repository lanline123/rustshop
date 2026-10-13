package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 礼品卡组
 *
 * @author Tigshop项目组
 * @TableName gift_card_groups
 */
@TableName(value = "gift_card_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftCardGroups implements Serializable {

    @Schema(description = "卡组id")
    @TableId(type = IdType.AUTO)
    private Integer cardGroupsId;

    @Schema(description = "1-兑换卡 2-储值卡")
    private Integer groupType;

    @Schema(description = "1-电子卡 2-实体卡")
    private Integer cardType;

    @Schema(description = "卡组名称")
    private String cardGroupName;

    @Schema(description = "卡图片URL")
    private String imageUrl;

    @Schema(description = "有效期类型")
    private Integer validityType;

    @Schema(description = "有效期天数")
    private Integer validityDays;

    @Schema(description = "到期时间")
    private Long expireTime;

    @Schema(description = "面额(储值卡专用)")
    private BigDecimal faceAmount;

    @Schema(description = "1-开启 0-关闭")
    private Integer status;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "可用库存数")
    private Integer availabilityStock;
}
