package com.tigshop.bean.vo.im;

import cn.hutool.json.JSONArray;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客服人员管理VO
 *
 * @author Tigshop团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客服人员管理VO")
public class AdminUserKefuVO {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "管理员ID")
    private Integer adminId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "角色ID")
    private Integer roleId;

    @Schema(description = "权限列表")
    private JSONArray authList;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "管理员用户信息")
    private AdminUserVO adminUser;

    @Schema(description = "角色信息")
    private RoleVO role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户VO")
    public static class UserVO {

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "手机号码")
        @Sensitive(type = SensitiveType.PHONE)
        private String mobile;

        @Schema(description = "昵称")
        private String nickname;

        public UserVO(User user) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.avatar = user.getAvatar();
            this.mobile = user.getMobile();
            this.nickname = user.getNickname();
        }
    }

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

        @Schema(description = "店铺ID")
        private Integer shopId;

        public RoleVO(AdminRole adminRole) {
            this.roleId = adminRole.getRoleId();
            this.roleName = adminRole.getRoleName();
            this.shopId = adminRole.getShopId();
        }
    }

    public AdminUserKefuVO(AdminUserShop adminUserShop) {
        this.id = adminUserShop.getId();
        this.adminId = adminUserShop.getAdminId();
        this.shopId = adminUserShop.getShopId();
        this.username = adminUserShop.getUsername();
        this.email = adminUserShop.getEmail();
        this.avatar = adminUserShop.getAvatar();
        this.isUsing = adminUserShop.getIsUsing();
        this.roleId = adminUserShop.getRoleId();

        this.authList = adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList()) ?
                JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class) : new JSONArray();

        this.addTime = TigUtils.handelTime(adminUserShop.getAddTime());
    }

    public void assembleData(User user, AdminUser adminUser, AdminRole adminRole) {
        this.user = user != null ? new UserVO(user) : null;
        this.adminUser = adminUser != null ? new AdminUserVO(adminUser) : null;
        this.role = adminRole != null ? new RoleVO(adminRole) : null;
    }
}
