package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台PRO版会员等级列表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
public class UserRankListVO {

    @Schema(description = "用户权限")
    private String userRights;

    @Schema(description = "用户数量")
    private int userCount;

    @Schema(description = "等级ID")
    private int rankId;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "最小成长值")
    private BigDecimal minGrowthPoints;

    @Schema(description = "最大成长值")
    private int maxGrowthPoints;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "是否显示价格")
    private int showPrice;

    @Schema(description = "等级类型")
    private int rankType;

    @Schema(description = "等级图标URL")
    private String rankLogo;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "等级背景")
    private String rankBg;

    @Schema(description = "等级积分")
    private String rankPoint;

    @Schema(description = "是否免运费")
    private int freeShipping;

    @Schema(description = "等级卡类型")
    private int rankCardType;

    @Schema(description = "权限列表")
    private List<Right> rights;

    @Schema(description = "等级级别")
    private Integer rankLevel;

    @Data
    @Schema(description = "权限对象")
    public static class Right {
        @Schema(description = "权限名称")
        private String name;

        @Schema(description = "显示名称")
        private String showName;

        @Schema(description = "图标URL")
        private String icon;

        @Schema(description = "权限值")
        private String value;

        @Schema(description = "描述")
        private String describe;

        @Schema(description = "是否选中")
        private int isChecked;
    }
}
