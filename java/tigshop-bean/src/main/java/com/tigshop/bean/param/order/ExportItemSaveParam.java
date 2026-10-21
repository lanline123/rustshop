package com.tigshop.bean.param.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导出标签信息保存参数
 *
 * @author kidd
 * @since 2025/5/13 09:28
 */
@Data
public class ExportItemSaveParam {

    @NotBlank(message = "导出标签信息不能为空")
    @Schema(description = "导出标签信息")
    private String exportItem;
}
