package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户等级配置表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户等级配置表")
public class UserRankConfigDTO {
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "值")
    private Integer rankAfterMonth;

    @Schema(description = "值")
    private Integer useMonth;
}
