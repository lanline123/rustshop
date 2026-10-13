package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工信息修改
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "员工信息修改参数")
public class AdminUserShopModifyDTO {
    @Schema(description = "员工id")
    private Integer id;

    @Schema(description = "员工头像")
    private String avatar;

    @Schema(description = "email")
    private String email;

    @Schema(description = "admin_id")
    private Integer adminId;

    @Schema(description = "店铺id")
    private Integer shopId;
}
