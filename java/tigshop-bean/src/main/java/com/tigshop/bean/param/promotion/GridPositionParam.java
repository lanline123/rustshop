package com.tigshop.bean.param.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 九宫格位置配置参数
 * 
 * 奖品关联方式：
 * 1. 有奖品位置：使用 tempIndex 关联奖品列表中的奖品
 * 2. 未中奖位置：tempIndex 为空
 *
 * @author Tigshop团队
 * @create 2024年12月17日
 */
@Data
@Schema(description = "九宫格位置配置参数")
public class GridPositionParam {

    /**
     * 位置(1-8)
     */
    @NotNull(message = "位置不能为空")
    @Min(value = 1, message = "位置最小为1")
    @Max(value = 8, message = "位置最大为8")
    @Schema(description = "位置(1-8)")
    private Integer position;

    /**
     * 临时索引(关联奖品时使用，与奖品列表中的tempIndex对应，为空表示未中奖位置)
     */
    @Schema(description = "临时索引(关联奖品列表中的tempIndex，为空表示未中奖位置)")
    private Integer tempIndex;

    /**
     * 中奖概率(0-100)，支持最多4位小数
     */
    @NotNull(message = "概率不能为空")
    @DecimalMin(value = "0", message = "概率最小为0")
    @DecimalMax(value = "100", message = "概率最大为100")
    @Digits(integer = 3, fraction = 4, message = "概率最多支持4位小数")
    @Schema(description = "中奖概率(0-100)，支持最多4位小数")
    private BigDecimal probability;
}

