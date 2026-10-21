package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.tigshop.bean.model.promotion.lottery.DecorateConfig;
import com.tigshop.bean.model.promotion.lottery.GridPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 抽奖活动表
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@TableName(value = "lottery_activity", autoResultMap = true)
@Schema(description = "抽奖活动")
public class LotteryActivity implements Serializable {

    /**
     * 活动ID
     */
    @TableId(value = "activity_id", type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 活动开始时间(秒级时间戳)
     */
    @Schema(description = "活动开始时间(秒级时间戳)")
    private Long startTime;

    /**
     * 活动结束时间(秒级时间戳)
     */
    @Schema(description = "活动结束时间(秒级时间戳)")
    private Long endTime;

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
     * 每日最大抽奖次数(0表示不限制)
     */
    @Schema(description = "每日最大抽奖次数(0表示不限制)")
    private Integer dailyLimit;

    /**
     * 最大抽奖次数(0表示不限制)
     */
    @Schema(description = "最大抽奖次数(0表示不限制)")
    private Integer totalLimit;

    /**
     * 每人最多中奖次数(0表示不限制)
     */
    @Schema(description = "每人最多中奖次数(0表示不限制)")
    private Integer winLimit;

    /**
     * 装修配置(JSON)
     */
    @TableField(value = "decorate_config", typeHandler = JacksonTypeHandler.class)
    @Schema(description = "装修配置(JSON)")
    private DecorateConfig decorateConfig;

    /**
     * 九宫格配置(JSON)
     * 包含8个位置的奖品ID和概率配置
     */
    @TableField(value = "grid_config", typeHandler = JacksonTypeHandler.class)
    @Schema(description = "九宫格配置(JSON)")
    private List<GridPosition> gridConfig;

    /**
     * 未中奖名称(九宫格未选择奖品的位置显示)
     */
    @Schema(description = "未中奖名称(九宫格未选择奖品的位置显示)")
    private String noWinName;

    /**
     * 未中奖图片URL(九宫格第8格显示)
     */
    @Schema(description = "未中奖图片URL(九宫格第8格显示)")
    private String noWinImage;

    /**
     * 未中奖提示语(弹窗显示)
     */
    @Schema(description = "未中奖提示语(弹窗显示)")
    private String noWinTips;

    /**
     * 店铺ID(平台为0)
     */
    @Schema(description = "店铺ID(平台为0)")
    private Integer shopId;

    /**
     * 是否删除:0-否,1-是(软删除)
     */
    @Schema(description = "是否删除:0-否,1-是(软删除)")
    private Integer isDelete;

    /**
     * 创建时间(秒级时间戳)
     */
    @Schema(description = "创建时间(秒级时间戳)")
    private Long createTime;

    /**
     * 更新时间(秒级时间戳)
     */
    @Schema(description = "更新时间(秒级时间戳)")
    private Long updateTime;
}


