package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.promotion.lottery.DecorateConfig;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 抽奖活动信息VO(用户端)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖活动信息VO")
public class LotteryActivityInfoVO {

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
     * 每次消耗积分
     */
    @Schema(description = "每次消耗积分")
    private Integer costPoints;

    /**
     * 装修配置(包含玩法规则)
     */
    @Schema(description = "装修配置")
    private DecorateConfig decorateConfig;

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
     * 九宫格配置(8个位置,包含奖品详情)
     */
    @Schema(description = "九宫格配置(8个位置,包含奖品详情)")
    private List<GridPositionVO> gridConfig;

    /**
     * 用户当前积分
     */
    @Schema(description = "用户当前积分")
    private Integer userPoints;

    /**
     * 今日剩余次数（-1表示不限制）
     */
    @Schema(description = "今日剩余次数（-1表示不限制）")
    private Integer todayRemaining;

    /**
     * 总剩余次数（-1表示不限制）
     */
    @Schema(description = "总剩余次数（-1表示不限制）")
    private Integer totalRemaining;

    /**
     * 可中奖剩余次数（-1表示不限制）
     */
    @Schema(description = "可中奖剩余次数（-1表示不限制）")
    private Integer winRemaining;

    /**
     * 用户积分可抽奖次数（根据用户积分和每次消耗积分计算）
     */
    @Schema(description = "用户积分可抽奖次数")
    private Integer availableDrawCount;
}
