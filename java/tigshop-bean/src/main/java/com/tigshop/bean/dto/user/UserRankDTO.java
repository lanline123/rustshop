package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

import static com.tigshop.common.constant.user.UserRankConstants.*;

/**
 * 会员等级表
 */
@Schema(description = "会员等级")
@Data
public class UserRankDTO {

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "会员等级名称")
    @NotNull(message = RANK_NAME_IS_NULL)
    private String rankName;

    @Schema(description = "该等级的最低成长值")
    @NotNull(message = MIN_GROWTH_POINTS_IS_NULL)
    private BigDecimal minGrowthPoints;

    @Schema(description = "该等级的最高成长值")
    @NotNull(message = MAX_GROWTH_POINTS_IS_NULL)
    private Integer maxGrowthPoints;

    @Schema(description = "该会员等级的商品折扣")
    @NotNull(message = DISCOUNT_IS_NULL)
    private BigDecimal discount;

    @Schema(description = "是否显示商品价格")
    private Integer showPrice;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "等级背景图（移动端）")
    private String rankBg;
}
