package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户抽奖统计表
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@TableName("lottery_user_stat")
@Schema(description = "用户抽奖统计")
public class LotteryUserStat implements Serializable {

    /**
     * 统计ID
     */
    @TableId(value = "stat_id", type = IdType.AUTO)
    @Schema(description = "统计ID")
    private Integer statId;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Integer activityId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer userId;

    /**
     * 累计抽奖次数
     */
    @Schema(description = "累计抽奖次数")
    private Integer totalCount;

    /**
     * 累计中奖次数
     */
    @Schema(description = "累计中奖次数")
    private Integer winCount;

    /**
     * 今日抽奖次数
     */
    @Schema(description = "今日抽奖次数")
    private Integer todayCount;

    /**
     * 今日日期(YYYY-MM-DD)
     */
    @Schema(description = "今日日期(YYYY-MM-DD)")
    private String todayDate;

    /**
     * 最后抽奖时间(秒级时间戳)
     */
    @Schema(description = "最后抽奖时间(秒级时间戳)")
    private Long lastLotteryTime;

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


