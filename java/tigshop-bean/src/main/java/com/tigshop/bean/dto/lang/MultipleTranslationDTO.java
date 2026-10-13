package com.tigshop.bean.dto.lang;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 一键翻译 参数
 *
 * @author kidd
 * @since 2025/3/25 15:00
 */
@Data
public class MultipleTranslationDTO {

    @Schema(description = "数据类型")
    private Integer dataType;

    @Schema(description = "")
    private Integer localesId;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "数量")
    private Integer size;

    @Schema(description = "")
    private List<Integer> ids;

    @Schema(description = "更新类型：1=仅翻译未维护的内容（默认）, 2=覆盖全部内容", defaultValue = "1")
    private Integer updateType;
}
