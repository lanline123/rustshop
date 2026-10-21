package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖结果VO
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖结果VO")
public class LotteryResultVO {

    /**
     * 是否中奖:true-中奖,false-未中奖
     */
    @Schema(description = "是否中奖:true-中奖,false-未中奖")
    private Boolean isWin;

    /**
     * 奖品ID(未中奖为0)
     */
    @Schema(description = "奖品ID(未中奖为0)")
    private Integer prizeId;

    /**
     * 奖品名称
     */
    @Schema(description = "奖品名称")
    @JsonTranslate(dataType = 12)
    private String prizeName;

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

    /**
     * 奖品图片URL
     */
    @Schema(description = "奖品图片URL")
    @JsonTranslate(dataType = 13)
    private String prizeImage;

    /**
     * 中奖位置(1-8,用于前端动画定位)
     */
    @Schema(description = "中奖位置(1-8,用于前端动画定位)")
    private Integer position;

    /**
     * 今日剩余次数
     */
    @Schema(description = "今日剩余次数")
    private Integer todayRemaining;

    /**
     * 总剩余次数
     */
    @Schema(description = "总剩余次数")
    private Integer totalRemaining;

    /**
     * 抽奖记录记录ID
     */
    @Schema(description = "抽奖记录记录ID")
    private Integer recordId;
}
