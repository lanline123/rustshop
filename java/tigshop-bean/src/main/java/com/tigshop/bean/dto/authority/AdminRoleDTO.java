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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限列表
 */
@Data
@Schema(name = "权限列表")
public class AdminRoleDTO {
    @Schema(description = "角色ID")
    private Integer roleId;

    @Schema(description = "名称")
    private String roleName;

    @Schema(description = "描述")
    private String roleDesc;

    @Schema(description = "权限")
    private List<String> authorityList;

    @Schema(description = "admin_type")
    private String adminType;

    @Schema(description = "merchant_id")
    private String merchantId;

    @Schema(description = "shop_id")
    private String shopId;

    @Schema(description = "是否系统内置角色 0-否 1-是")
    private Integer isSystem;

}
