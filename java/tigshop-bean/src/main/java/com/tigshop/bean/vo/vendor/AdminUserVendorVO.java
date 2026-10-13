package com.tigshop.bean.vo.vendor;

import cn.hutool.json.JSONArray;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商员工管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "供应商员工管理VO")
public class AdminUserVendorVO {

    // *** AdminUserVendor ***

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "管理员ID")
    private Integer adminId;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "是否管理员")
    private Integer isAdmin;

    @Schema(description = "角色ID")
    private Integer roleId;

    // *** Other ***

    @Schema(description = "权限列表")
    private JSONArray authList;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "管理员用户信息")
    private AdminUserVO adminUser;

    @Schema(description = "角色信息")
    private RoleVO role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "管理员用户VO")
    public static class AdminUserVO {

        @Schema(description = "管理员ID")
        private Integer adminId;

        @Schema(description = "手机号码")
        @Sensitive(type = SensitiveType.PHONE)
        private String mobile;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "商户ID")
        private Integer merchantId;

        @Schema(description = "用户名")
        private String username;

        public AdminUserVO(AdminUser adminUser) {
            this.adminId = adminUser.getAdminId();
            this.mobile = adminUser.getMobile();
            this.avatar = adminUser.getAvatar();
            this.merchantId = adminUser.getMerchantId();
            this.username = adminUser.getUsername();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色VO")
    public static class RoleVO {

        @Schema(description = "角色ID")
        private Integer roleId;

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "供应商id")
        private Integer vendorId;

        public RoleVO(AdminRole adminRole) {
            this.roleId = adminRole.getRoleId();
            this.roleName = adminRole.getRoleName();
            this.vendorId = adminRole.getVendorId();
        }
    }

    public AdminUserVendorVO(AdminUserVendor adminUserVendor) {
        this.id = adminUserVendor.getId();
        this.adminId = adminUserVendor.getAdminId();
        this.vendorId = adminUserVendor.getVendorId();
        this.username = adminUserVendor.getUsername();
        this.email = adminUserVendor.getEmail();
        this.avatar = adminUserVendor.getAvatar();
        this.isUsing = adminUserVendor.getIsUsing();
        this.isAdmin = adminUserVendor.getIsAdmin();
        this.roleId = adminUserVendor.getRoleId();

        this.authList = adminUserVendor.getAuthList() != null && !"[]".equals(adminUserVendor.getAuthList()) ?
                JsonUtil.fromJson(adminUserVendor.getAuthList(), JSONArray.class) : new JSONArray();

        this.addTime = TigUtils.handelTime(adminUserVendor.getAddTime());


    }

    public void assembleData(AdminUser adminUser, AdminRole adminRole) {
        this.adminUser = adminUser != null ? new AdminUserVO(adminUser) : null;
        this.role = adminRole != null ? new RoleVO(adminRole) : null;
    }
}
