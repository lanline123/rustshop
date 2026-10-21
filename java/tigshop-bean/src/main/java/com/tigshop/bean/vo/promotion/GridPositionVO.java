package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 九宫格位置配置VO
 *
 * @author Tigshop团队
 * @create 2024年12月17日
 */
@Data
@Schema(description = "九宫格位置配置VO")
public class GridPositionVO {

    /**
     * 位置(1-8)
     */
    @Schema(description = "位置(1-8)")
    private Integer position;

    /**
     * 奖品ID(为空表示未中奖)
     */
    @Schema(description = "奖品ID(为空表示未中奖)")
    private Integer prizeId;

    /**
     * 中奖概率(0-100)，支持最多4位小数
     */
    @Schema(description = "中奖概率(0-100)，支持最多4位小数")
    private BigDecimal probability;

    /**
     * 奖品名称(prizeId为空时显示未中奖名称)
     */
    @Schema(description = "奖品名称")
    @JsonTranslate(dataType = 12)
    private String prizeName;

    /**
     * 奖品图片(prizeId为空时显示未中奖图片)
     */
    @Schema(description = "奖品图片")
    @JsonTranslate(dataType = 13)
    private String prizeImage;

    /**
     * 奖品类型:1-积分,2-商品,3-优惠券,0-未中奖
     */
    @Schema(description = "奖品类型:1-积分,2-商品,3-优惠券,0-未中奖")
    private Integer prizeType;

    /**
     * 奖品值
     */
    @Schema(description = "奖品值")
    private String prizeValue;
}
