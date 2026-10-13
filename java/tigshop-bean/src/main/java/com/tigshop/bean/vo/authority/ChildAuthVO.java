package com.tigshop.bean.vo.authority;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 菜单子权限
 *
 * @author kidd
 * @since 2025/6/11 13:23
 */
@Data
public class ChildAuthVO {

    @JSONField(name = "authName", alternateNames = "auth_name")
    @Schema(description = "权限名称")
    private String authName;

    @JSONField(name = "authSn", alternateNames = "auth_sn")
    @Schema(description = "权限标识")
    private String authSn;
}
