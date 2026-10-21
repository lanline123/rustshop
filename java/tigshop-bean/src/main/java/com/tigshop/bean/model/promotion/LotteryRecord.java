package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 抽奖记录表
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@TableName("lottery_record")
@Schema(description = "抽奖记录")
public class LotteryRecord implements Serializable {

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Integer recordId;

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
     * 中奖奖品ID(0表示未中奖)
     */
    @Schema(description = "中奖奖品ID(0表示未中奖)")
    private Integer prizeId;

    /**
     * 奖品名称(未中奖时为"未中奖")
     */
    @Schema(description = "奖品名称(未中奖时为'未中奖')")
    private String prizeName;

    /**
     * 奖品类型:0-未中奖,1-积分,2-商品,3-优惠券
     */
    @Schema(description = "奖品类型:0-未中奖,1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 奖品值
     */
    @Schema(description = "奖品值")
    private String prizeValue;

    /**
     * 奖品图片
     */
    @Schema(description = "奖品图片")
    private String prizeImage;

    /**
     * 消耗积分
     */
    @Schema(description = "消耗积分")
    private Integer costPoints;

    /**
     * 是否中奖:0-未中奖,1-中奖
     */
    @Schema(description = "是否中奖:0-未中奖,1-中奖")
    private Integer isWin;

    /**
     * 奖品状态:0-未领取,1-已领取,2-已过期
     */
    @Schema(description = "奖品状态:0-未领取,1-已领取,2-已过期")
    private Integer status;

    /**
     * 领取时间(秒级时间戳)
     */
    @Schema(description = "领取时间(秒级时间戳)")
    private Long receiveTime;

    /**
     * 抽奖时间(秒级时间戳)
     */
    @Schema(description = "抽奖时间(秒级时间戳)")
    private Long createTime;
}


