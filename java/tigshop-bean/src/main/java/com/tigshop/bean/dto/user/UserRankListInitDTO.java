package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 初始化会员等级配置DTO
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "初始化")
public class UserRankListInitDTO {
    @Schema(description = "等级")
    private Integer rankLevel;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "等级图标")
    private String rankLogo;

    @Schema(description = "等级卡片类型")
    private Integer rankCardType;

    @Schema(description = "等级ico")
    private String rankIco;

    @Schema(description = "等级背景")
    private String rankBg;

    @Schema(description = "最小成长积分")
    private BigDecimal minGrowthPoints;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "等级分数")
    private Integer rankPoint;

    @Schema(description = "是否免运费")
    private Integer freeShipping;

    @Schema(description = "rights")
    private List<String> rights;
}
