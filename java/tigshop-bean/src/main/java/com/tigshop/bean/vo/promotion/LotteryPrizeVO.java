package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖奖品VO(按活动管理)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖奖品VO")
public class LotteryPrizeVO {

    /**
     * 奖品ID
     */
    @Schema(description = "奖品ID")
    private Integer prizeId;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 奖品名称
     */
    @Schema(description = "奖品名称")
    @JsonTranslate(dataType = 12)
    private String prizeName;

    /**
     * 奖品类型:1-积分,2-商品,3-优惠券
     */
    @Schema(description = "奖品类型:1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 奖品类型文本
     */
    @Schema(description = "奖品类型文本")
    private String prizeTypeText;

    /**
     * 奖品值
     */
    @Schema(description = "奖品值")
    private String prizeValue;

    /**
     * 奖品图片URL
     */
    @Schema(description = "奖品图片URL")
    @JsonTranslate(dataType = 13)
    private String prizeImage;

    /**
     * 奖品库存
     */
    @Schema(description = "奖品库存")
    private Integer prizeStock;

    /**
     * 已使用库存
     */
    @Schema(description = "已使用库存")
    private Integer prizeStockUsed;

    /**
     * 剩余库存
     */
    @Schema(description = "剩余库存")
    private Integer remainStock;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;
}
