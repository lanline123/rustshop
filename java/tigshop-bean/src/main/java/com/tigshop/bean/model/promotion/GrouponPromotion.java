package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 拼团活动
 *
 * @TableName groupon_promotion
 */
@TableName(value = "groupon_promotion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrouponPromotion implements Serializable {

    @Schema(description = "拼团 ID")
    @TableId(type = IdType.AUTO)
    private Long grouponPromotionId;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "成团有效时间")
    private Long expirationTime;

    @Schema(description = "h5活动图片")
    private String grouponImage;

    @Schema(description = "pc活动图片")
    private String pcGrouponImage;

    @Schema(description = "分享图")
    private String shareImage;

    @Schema(description = "活动说明")
    private String grouponExplain;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}