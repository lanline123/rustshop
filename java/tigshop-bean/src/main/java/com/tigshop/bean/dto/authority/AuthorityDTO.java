// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import static com.tigshop.common.constant.authority.AuthorityConstants.*;

/**
 * 权限入参DTO
 *
 * @author Jayce
 * @create 2024年10月22日 10:56
 */
@Data
@Schema(description = "权限入参DTO")
public class AuthorityDTO implements Serializable {
    /**
     * 自增id号
     */
    @Schema(description = "自增id号")
    private Integer authorityId;

    /**
     * 权限编号
     */
    @Schema(description = "权限编号")
    @NotNull(message = AUTHORITY_SN_IS_NULL)
    private String authoritySn;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    @NotNull(message = AUTHORITY_NAME_IS_NULL)
    @Size(max = 30, min = 1, message = AUTHORITY_LENGTH_ERROR)
    private String authorityName;

    /**
     * 该权限的父类ID
     */
    @Schema(description = "该权限的父类ID")
    private Integer parentId;

    /**
     * 该权限在目录显示的的顺序,数字越大顺序越靠后,同数字,id在前的先显示
     */
    @Schema(description = "该权限在目录显示的的顺序")
    private Integer sortOrder;

    @Schema(description = "是否有子菜单")
    private Long hasChildren;

    /**
     * 是否在目录显示 1显示; 0不显示
     */
    @Schema(description = "是否在目录显示")
    private Integer isShow;

    /**
     * JSON:子权限[{auth_name|auth_sn}]
     */
    @Schema(description = "JSON:子权限")
    private List<AuthorityChildDTO> childAuth;

    /**
     * 路由链接
     */
    @Schema(description = "路由链接")
    private String routeLink;

    /**
     *
     ICO图标
     */
    @Schema(description = "ICO图标")
    private String authorityIco;

    /**
     * 是否系统目录，是的话只能有限编辑或隐藏，不能删除
     */
    @Schema(description = "是否系统目录")
    private Integer isSystem;

    /**
     * admin管理后台使用shop店铺使用
     */
    @Schema(description = "admin管理后台使用shop店铺使用")
    private String adminType;
}