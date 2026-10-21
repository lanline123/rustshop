package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 拼团信息
 *
 * @TableName groupon_promotion_detail
 */
@TableName(value = "groupon_promotion_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrouponPromotionDetail implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long grouponDetailId;

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "拼团商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品规格 id")
    private Long skuId;

    @Schema(description = "拼团价（json）")
    private String groupPrice;

    @Schema(description = "可售数量")
    private Integer limitNum;

    @Schema(description = "是否参加；0-否，1-是")
    private Integer isJoin;

    @Schema(description = "创建时间")
    private Long addTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}