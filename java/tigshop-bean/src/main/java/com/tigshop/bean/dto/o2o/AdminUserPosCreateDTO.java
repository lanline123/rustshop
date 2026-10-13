// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.o2o;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 收银台员工创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月04日 17:13
 */
@Data
@Schema(description = "收银台员工创建")
public class AdminUserPosCreateDTO {
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

    @Schema(description = "权限id", hidden = true)
    private Integer roleId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "权限列表")
    private List<String> authList;

}
