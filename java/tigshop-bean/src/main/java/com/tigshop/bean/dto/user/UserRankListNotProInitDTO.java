package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 初始化非pro版会员等级配置DTO
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "初始化非pro版会员等级配置DTO")
public class UserRankListNotProInitDTO {
    @Schema(description = "等级级别")
    private Integer rankLevel;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "等级图标")
    private String rankLogo;
}
