// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.bean.vo.vendor.VendorVO;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.tigshop.common.constant.authority.AdminUserConstants.USERNAME_IS_NULL;
import static com.tigshop.common.constant.authority.AdminUserConstants.USERNAME_IS_OVERLENGTH;

/**
 * 管理员用户
 *
 * @author Jayce
 * @create 2024年10月29日 14:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员用户")
public class AdminUserDTO {

    @Schema(description = "自增id号")
    private Integer adminId;

    @Schema(description = "管理员登录名")
    @NotNull(message = USERNAME_IS_NULL)
    @Size(max = 30, min = 1, message = USERNAME_IS_OVERLENGTH)
    private String username;

    @Schema(description = "管理员类型，admin:管理员，shop:店铺")
    private String adminType;

    @Sensitive(type = SensitiveType.PHONE)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "管理员登录密码")
    private String password;

    @Schema(description = "管理员邮箱")
    private String email;

    @Schema(description = "管理员添加时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String addTime;

    @Schema(description = "JSON管理员管理权限列表")
    private List<String> authList;

    @Schema(description = "绑定的会员id")
    private Integer userId;

    @Schema(description = "供应商id")
    private Integer suppliersId;

    @Schema(description = "角色id")
    private Integer roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "父管理员id，表明当前为子管理员")
    private Integer parentId;

    @Schema(description = "JSON店铺存的标签")
    private String menuTag;

    @Schema(description = "JSON订单导出存的标签")
    private String orderExport;

    @Schema(description = "JSON扩展信息")
    private String extra;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否停用：1 是 0 否")
    private Integer isUsing;

    @Schema(description = "旧密码")
    private String oldPassword;

    @Schema(description = "确认密码")
    private String pwdConfirm;

    @Schema(description = "初始密码")
    private String initialPassword;

    @Schema(description = "userShop")
    private List<UserShopDTO> userShop;

    @Schema(description = "userVendor")
    private List<UserVendorDTO> userVendor;

    @Data
    public static class UserShopDTO {
        ShopVO shop;
    }

    @Data
    public static class UserVendorDTO {
        VendorVO vendor;
    }
}
