package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客服人员创建数据对象
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "客服人员创建")
public class AdminUserKefuCreateDTO {
    @Schema(description = "店铺ID", hidden = true)
    private Integer shopId;

    @Schema(description = "管理员ID", hidden = true)
    private Integer adminId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "角色ID", hidden = true)
    private Integer roleId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "权限列表")
    private List<String> authList;
}
