package com.tigshop.bean.param.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员备注参数
 *
 * @author kidd
 * @since 2025/6/13 11:11
 */
@Data
public class OrderAdminNoteParam {

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "ids")
    private String ids;

    @Schema(description = "备注")
    private String adminNote;

    @Schema(description = "标记")
    private Integer mark;

}
