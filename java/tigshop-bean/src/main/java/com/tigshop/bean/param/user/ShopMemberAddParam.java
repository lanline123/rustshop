package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 店铺端新增客户入参（CR-20260708-1530）。
 *
 * <p>手机号为查重/建号主键：已存在对应 user 则复用，仅补 shop_member 绑定；不存在则新建 user 再绑定。
 */
@Data
@Schema(description = "店铺新增客户入参")
public class ShopMemberAddParam {

    @Schema(description = "手机号（必填，查重主键）")
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "昵称（选填）")
    private String nickname;

    @Schema(description = "用户名（选填，为空自动生成）")
    private String username;

    @Schema(description = "邮箱（选填）")
    private String email;
}
