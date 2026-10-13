package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收银台登录偏好设置参数（登录后默认进收银台 / 记录上次登录端）
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "收银台登录偏好设置参数")
public class PosLoginSettingDTO {

    @Schema(description = "门店ID")
    private Integer shopId;

    @Schema(description = "登录后是否默认进收银台：1 是 0 否")
    private Integer defaultPosLogin;

    @Schema(description = "上次登录端：admin/shop/store/pos")
    private String lastLoginSystem;
}
