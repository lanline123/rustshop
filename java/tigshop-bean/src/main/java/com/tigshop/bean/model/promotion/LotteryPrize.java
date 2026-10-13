package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 抽奖奖品表(关联活动)
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@TableName("lottery_prize")
@Schema(description = "抽奖奖品")
public class LotteryPrize implements Serializable {

    /**
     * 奖品ID
     */
    @TableId(value = "prize_id", type = IdType.AUTO)
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
    private String prizeName;

    /**
     * 奖品类型:1-积分,2-商品,3-优惠券
     */
    @Schema(description = "奖品类型:1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 奖品值(积分数量/商品ID/优惠券ID)
     */
    @Schema(description = "奖品值(积分数量/商品ID/优惠券ID)")
    private String prizeValue;

    /**
     * 奖品图片URL
     */
    @Schema(description = "奖品图片URL")
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


