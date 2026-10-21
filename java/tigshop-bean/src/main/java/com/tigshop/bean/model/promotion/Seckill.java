package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 限时秒杀model
 *
 * @author kidd
 * @create 2025/7/2
 */
@Data
@TableName("seckill")
@Schema(description = "限时秒杀")
public class Seckill implements Serializable {

    @TableId(value = "seckill_id", type = IdType.AUTO)
    @Schema(description = "秒杀活动ID")
    private Integer seckillId;

    @Schema(description = "活动名称")
    private String seckillName;

    @Schema(description = "起始时间")
    private Long seckillStartTime;

    @Schema(description = "结束时间")
    private Long seckillEndTime;

    @Schema(description = "限购数量")
    private Integer seckillLimitNum;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "店铺ID，平台为0")
    private Integer shopId;
}