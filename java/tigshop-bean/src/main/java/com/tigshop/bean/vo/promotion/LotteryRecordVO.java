package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖记录VO
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖记录VO")
public class LotteryRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Integer recordId;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    @JsonTranslate(dataType = 12)
    private String activityName;

    /**
     * 奖品名称
     */
    @Schema(description = "奖品名称")
    @JsonTranslate(dataType = 12)
    private String prizeName;

    /**
     * 奖品类型:0-未中奖,1-积分,2-商品,3-优惠券
     */
    @Schema(description = "奖品类型:0-未中奖,1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 奖品图片
     */
    @Schema(description = "奖品图片")
    @JsonTranslate(dataType = 13)
    private String prizeImage;

    /**
     * 奖品值
     */
    @Schema(description = "奖品值")
    private String prizeValue;

    /**
     * 状态:0-未领取,1-已领取,2-已过期
     */
    @Schema(description = "状态:0-未领取,1-已领取,2-已过期")
    private Integer status;

    /**
     * 状态文本
     */
    @Schema(description = "状态文本")
    private String statusText;

    /**
     * 中奖时间
     */
    @Schema(description = "中奖时间")
    private Long createTime;

    /**
     * 领取时间
     */
    @Schema(description = "领取时间")
    private Long receiveTime;
}
