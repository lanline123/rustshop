package com.tigshop.bean.param.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽奖奖品项参数（用于活动保存时的奖品列表）
 * 
 * 支持两种场景：
 * 1. 新增奖品：使用 tempIndex 作为临时标识
 * 2. 编辑已有奖品：使用 prizeId 标识
 *
 * @author Tigshop团队
 * @create 2024年12月22日
 */
@Data
@Schema(description = "抽奖奖品项参数")
public class LotteryPrizeItemParam {

    /**
     * 临时索引（前端生成，用于新增奖品时与九宫格关联）
     */
    @Schema(description = "临时索引（前端生成，新增奖品使用，用于与九宫格关联）")
    private Integer tempIndex;

    /**
     * 奖品ID（编辑已有奖品时使用）
     */
    @Schema(description = "奖品ID（编辑已有奖品时使用）")
    private Integer prizeId;

    /**
     * 奖品名称
     */
    @NotBlank(message = "奖品名称不能为空")
    @Schema(description = "奖品名称")
    private String prizeName;

    /**
     * 奖品类型:1-积分,2-商品,3-优惠券
     */
    @NotNull(message = "奖品类型不能为空")
    @Schema(description = "奖品类型:1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 奖品值(积分数量/商品ID/优惠券ID)
     */
    @NotBlank(message = "奖品值不能为空")
    @Schema(description = "奖品值(积分数量/商品ID/优惠券ID)")
    private String prizeValue;

    /**
     * 奖品图片URL
     */
    @NotBlank(message = "奖品图片不能为空")
    @Schema(description = "奖品图片URL")
    private String prizeImage;

    /**
     * 奖品库存
     */
    @NotNull(message = "奖品库存不能为空")
    @Min(value = 1, message = "奖品库存必须大于0")
    @Schema(description = "奖品库存")
    private Integer prizeStock;
}

