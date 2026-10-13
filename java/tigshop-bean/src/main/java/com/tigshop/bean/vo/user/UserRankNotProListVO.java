package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台非pro会员等级列表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "后台非pro会员等级列表VO")
public class UserRankNotProListVO {
    @Schema(description = "等级Id")
    private Integer rankId;

    @Schema(description = "用户统计")
    private Integer userCount;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "等级logo")
    private String rankLogo;

    @Schema(description = "等级级别")
    private Integer rankLevel;
}
