package com.tigshop.bean.vo.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限列表
 *
 * @author kidd
 * @since 2025/8/19 14:02
 */
@Data
public class AuthorityVO {

    @Schema(description = "权限编号")
    private String authoritySn;

    @Schema(description = "路由链接")
    private String routeLink;

    @Schema(description = "权限名称")
    private String authorityName;

    @Schema(description = "该权限的父类ID")
    private Integer parentId;

    // *** Other ***

    @Schema(description = "权限名称列表")
    private List<String> authorityNames;
}
