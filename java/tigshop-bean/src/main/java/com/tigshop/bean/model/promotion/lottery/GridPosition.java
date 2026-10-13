package com.tigshop.bean.model.promotion.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 九宫格位置配置
 *
 * @author Tigshop团队
 * @create 2024年12月17日
 */
@Data
@Schema(description = "九宫格位置配置")
public class GridPosition implements Serializable {

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
}

