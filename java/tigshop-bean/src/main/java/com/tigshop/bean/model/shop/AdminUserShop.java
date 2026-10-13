package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_user_shop")
@Schema(description = "店铺员工列表")
public class AdminUserShop {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "管理员ID")
    private Integer adminId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "员工名称")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "员工权限[json]")
    private String authList;

    @Schema(description = "是否停用：1 是 0 否")
    private Integer isUsing;

    @Schema(description = "是否为管理员：1 是 0 否")
    private Integer isAdmin;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "权限组ID")
    private Integer roleId;

    @Schema(description = "收银上班/交班起始时间")
    private Long workOnTime;

    @Schema(description = "登录后是否默认进收银台：1 是 0 否")
    private Integer defaultPosLogin;

    @Schema(description = "上次登录端：admin/shop/store/pos")
    private String lastLoginSystem;

}
