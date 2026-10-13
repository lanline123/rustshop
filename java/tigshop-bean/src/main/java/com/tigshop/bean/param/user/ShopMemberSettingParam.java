package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存店铺会员总开关入参。
 */
@Data
@Schema(description = "店铺会员设置保存入参")
public class ShopMemberSettingParam {

    @Schema(description = "店铺会员功能总开关；0-关闭 1-启用")
    @NotNull(message = "shopMemberEnabled 不能为空")
    @Min(value = 0, message = "shopMemberEnabled 取值 0/1")
    @Max(value = 1, message = "shopMemberEnabled 取值 0/1")
    private Integer shopMemberEnabled;

    @Schema(description = "店铺客户信息脱敏；0-关闭 1-启用")
    @Min(value = 0, message = "shopMemberDesensitize 取值 0/1")
    @Max(value = 1, message = "shopMemberDesensitize 取值 0/1")
    private Integer shopMemberDesensitize;

    @Schema(description = "店铺添加客户；0-关闭 1-启用")
    @Min(value = 0, message = "shopMemberAllowAdd 取值 0/1")
    @Max(value = 1, message = "shopMemberAllowAdd 取值 0/1")
    private Integer shopMemberAllowAdd;

    @Schema(description = "店铺导入客户；0-关闭 1-启用")
    @Min(value = 0, message = "shopMemberAllowImport 取值 0/1")
    @Max(value = 1, message = "shopMemberAllowImport 取值 0/1")
    private Integer shopMemberAllowImport;
}
