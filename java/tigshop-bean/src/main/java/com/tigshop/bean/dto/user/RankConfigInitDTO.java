package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 初始化会员等级DTO
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "初始化会员等级DTO")
public class RankConfigInitDTO {
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "rank_after_month")
    private Integer rankAfterMonth;

    @Schema(description = "use_month")
    private Integer useMonth;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "数据")
    private DataDTO data;

    @Data
    @Schema(description = "数据")
    public static class DataDTO {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "rank_after_month")
        private Integer rankAfterMonth;

        @Schema(description = "use_month")
        private Integer useMonth;
    }
}
