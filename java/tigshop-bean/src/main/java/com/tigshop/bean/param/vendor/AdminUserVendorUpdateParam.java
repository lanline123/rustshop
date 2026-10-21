// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/7/7 16:15
 */
@Data
public class AdminUserVendorUpdateParam {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "是否使用")
    private Integer isUsing;

    @Schema(description = "权限id")
    private Integer roleId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "确认密码")
    private String pwdConfirm;

    @Schema(description = "权限列表")
    private List<String> authList;

    @Schema(description = "管理员ID")
    @Hidden
    private Integer adminId;

    @Schema(description = "是不是管理员")
    @Hidden
    private Integer isAdmin;
}
