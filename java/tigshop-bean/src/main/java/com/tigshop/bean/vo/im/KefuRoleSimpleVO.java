package com.tigshop.bean.vo.im;

import com.tigshop.bean.model.authority.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客服角色简单VO
 *
 * @author Tigshop团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客服角色简单VO")
public class KefuRoleSimpleVO {

    @Schema(description = "角色ID")
    private Integer roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识")
    private String roleCode;

    @Schema(description = "店铺ID")
    private Integer shopId;

    public KefuRoleSimpleVO(AdminRole adminRole) {
        this.roleId = adminRole.getRoleId();
        this.roleName = adminRole.getRoleName();
        this.roleCode = adminRole.getRoleCode();
        this.shopId = adminRole.getShopId();
    }
}
