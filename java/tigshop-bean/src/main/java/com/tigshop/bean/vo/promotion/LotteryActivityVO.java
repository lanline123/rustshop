package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.promotion.lottery.DecorateConfig;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 抽奖活动VO
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖活动VO")
public class LotteryActivityVO {

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    @JsonTranslate(dataType = 12)
    private String activityName;

    /**
     * 活动开始时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "活动开始时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    /**
     * 活动结束时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "活动结束时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    /**
     * 抽奖参与条件:0-无限制(免费),1-消耗积分
     */
    @Schema(description = "抽奖参与条件:0-无限制(免费),1-消耗积分")
    private Integer costType;

    /**
     * 每次抽奖消耗积分
     */
    @Schema(description = "每次抽奖消耗积分")
    private Integer costPoints;

    /**
     * 每日最大抽奖次数
     */
    @Schema(description = "每日最大抽奖次数")
    private Integer dailyLimit;

    /**
     * 最大抽奖次数
     */
    @Schema(description = "最大抽奖次数")
    private Integer totalLimit;

    /**
     * 每人最多中奖次数
     */
    @Schema(description = "每人最多中奖次数")
    private Integer winLimit;

    /**
     * 活动状态:0-未开始,1-进行中,2-已结束(根据时间自动计算)
     */
    @Schema(description = "活动状态:0-未开始,1-进行中,2-已结束")
    private Integer status;

    /**
     * 活动状态文本
     */
    @Schema(description = "活动状态文本")
    private String statusText;

    /**
     * 装修配置
     */
    @Schema(description = "装修配置")
    private DecorateConfig decorateConfig;

    /**
     * 奖品列表
     */
    @Schema(description = "奖品列表")
    private List<LotteryPrizeVO> prizeList;

    /**
     * 九宫格配置(包含奖品详情)
     */
    @Schema(description = "九宫格配置")
    private List<GridPositionVO> gridConfig;

    /**
     * 未中奖名称
     */
    @Schema(description = "未中奖名称")
    @JsonTranslate(dataType = 12)
    private String noWinName;

    /**
     * 未中奖图片URL
     */
    @Schema(description = "未中奖图片URL")
    @JsonTranslate(dataType = 13)
    private String noWinImage;

    /**
     * 未中奖提示语
     */
    @Schema(description = "未中奖提示语")
    @JsonTranslate(dataType = 12)
    private String noWinTips;

    /**
     * 奖品入口图URL
     */
    @Schema(description = "奖品入口图URL")
    @JsonTranslate(dataType = 13)
    private String prizeEntryImage;

    /**
     * 参与人数
     */
    @Schema(description = "参与人数")
    private Integer participantCount;

    /**
     * 中奖人次
     */
    @Schema(description = "中奖人次")
    private Integer winCount;

    /**
     * 创建时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @Schema(description = "创建时间(格式:yyyy-MM-dd HH:mm:ss)")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;
}
