package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设置管理员备注参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(name = "设置管理员备注参数")
public class SetAdminNoteParam {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户ID列表(逗号分隔，如：1,2,3)")
    private String userIds;

    @Schema(description = "管理员备注")
    private String adminNote;

    @Schema(description = "标记")
    private Integer mark;
}

