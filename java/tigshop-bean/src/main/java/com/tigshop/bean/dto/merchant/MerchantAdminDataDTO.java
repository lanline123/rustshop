package com.tigshop.bean.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 入驻申请审核数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "商户管理用户数据")
public class MerchantAdminDataDTO {
    @Schema(description = "admin_id")
    private Integer adminId;

    @Schema(description = "admin_user_id")
    private Integer adminUserId;

    @Schema(description = "is_admin")
    private Integer isAdmin;

    @Schema(description = "merchant_id")
    private Integer merchantId;

    @Schema(description = "merchant_user_id")
    private Integer merchantUserId;

    @Schema(description = "user_id")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;
}
