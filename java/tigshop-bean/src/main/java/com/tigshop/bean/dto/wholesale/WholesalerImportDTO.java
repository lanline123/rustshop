package com.tigshop.bean.dto.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 批量导入批发商入参
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批量导入批发商入参")
public class WholesalerImportDTO {

    @Schema(description = "true=导入全部 user；false=按 userIds，默认 false", example = "false")
    private Boolean importAll;

    @Schema(description = "待导入的 user_id 列表（自选会员导入时传；importAll=false 且未传 rankIds 时必填，size 1-5000）")
    private List<Integer> userIds;

    @Schema(description = "会员等级 ID 列表（按会员等级导入时传；importAll=false 且未传 userIds 时生效）")
    private List<Integer> rankIds;

    @Positive(message = "gradeId 必须为正数")
    @Schema(description = "统一批发等级 ID（启用中）；不传时使用配置的默认批发等级",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer gradeId;

    @Schema(description = "已存在策略：OVERWRITE_GRADE=覆盖等级（默认） / SKIP=跳过", example = "OVERWRITE_GRADE")
    private String existsStrategy;
}
