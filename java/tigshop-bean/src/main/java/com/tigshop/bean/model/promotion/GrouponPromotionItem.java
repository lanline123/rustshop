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
import java.math.BigDecimal;

/**
 * 拼团活动商品
 *
 * @TableName groupon_promotion_item
 */
@TableName(value = "groupon_promotion_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrouponPromotionItem implements Serializable {

    @Schema(description = "拼团活动商品 ID")
    @TableId(type = IdType.AUTO)
    private Long grouponPromotionItemId;

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "成团人数（json）")
    private String groupNum;

    @Schema(description = "每单限购数量；0-不限购")
    private Integer orderLimitNum;

    @Schema(description = "是否自动成团；0-否，1-是")
    private Integer isAutoGroup;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "成团最低价")
    private BigDecimal minGroupPrice;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}