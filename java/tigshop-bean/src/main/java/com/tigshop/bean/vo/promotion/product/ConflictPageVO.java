package com.tigshop.bean.vo.promotion.product;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.ProductPromotionTypeEnum;
import com.tigshop.bean.model.promotion.ProductPromotion;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 活动冲突列表
 *
 * @author kidd
 * @since 2025/4/30 09:22
 */
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动冲突列表")
@Data
public class ConflictPageVO {

    @Schema(description = "活动ID")
    private Integer promotionId;

    @Schema(description = "活动名称")
    private String promotionName;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "开始时间")
    private String startTime;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "[JSON]仅限会员等级")
    private String limitUserRank;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    @TableField(value = "`range`")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private String rangeData;

    @Schema(description = "订单金额下限")
    private BigDecimal minOrderAmount;

    @Schema(description = "订单金额上限，可用于阶梯设置")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠类型，1：满减，2：折扣，3：满赠")
    private Integer promotionType;

    @Schema(description = "优惠类型，1：满减，2：折扣，3：满赠")
    private String promotionTypeName;

    @Schema(description = "[data]类型相关数据，如满减金额、折扣金额、赠品等")
    @TableField(value = "promotion_type_data")
    private String promotionTypeData;

    @Schema(description = "是否启用")
    private Integer isAvailable;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "优惠规则：1 阶梯优惠 0 循环优惠")
    private Integer rulesType;

    @Schema(description = "优惠单位：1 元 2 件")
    private Integer unit;

    public ConflictPageVO(ProductPromotion productPromotion) {
        BeanUtil.copyProperties(productPromotion, this);

        this.promotionTypeName = ProductPromotionTypeEnum.getTypeName(productPromotion.getPromotionType());
    }
}
