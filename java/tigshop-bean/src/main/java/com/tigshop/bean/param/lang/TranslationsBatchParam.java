package com.tigshop.bean.param.lang;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作入参
 *
 * @author kidd
 * @since 2025/4/19 13:59
 */
@Schema(description = "批量操作入参")
@Data
public class TranslationsBatchParam {

    @Schema(description = "批量操作类型")
    private String type;

    @NotEmpty(message = "未选择项目")
    @Schema(description = "批量操作id")
    private List<Integer> ids;


}
