package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户等级
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户等级")
@TableName(value = "user_rank")
public class UserRank implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "会员等级名称")
    private String rankName;

    @Schema(description = "该等级的最低成长值")
    private BigDecimal minGrowthPoints;

    @Schema(description = "该等级的最高成长值")
    private Integer maxGrowthPoints;

    @Schema(description = "该会员等级的商品折扣")
    private BigDecimal discount;

    @Schema(description = "是否显示商品价格")
    private Integer showPrice;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "等级背景图（移动端）")
    private String rankBg;

    @Schema(description = "等级图标")
    private Integer freeShipping;

    @Schema(description = "会员图标")
    private String rankLogo;

    @Schema(description = "权益积分")
    private String rankPoint;

    @Schema(description = "会员卡类型：1 背景色 2自定义图片")
    private Integer rankCardType;

    @Schema(description = "自定义权益")
    private String rights;

    @Schema(description = "会员等级")
    private Integer rankLevel;

    @TableField(exist = false)
    @Schema(description = "该等级会员数量（正式会员 user_type=0，含正常与禁用；非持久字段）")
    private Long userCount;
}
