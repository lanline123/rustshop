package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺会员总开关设置 VO。
 */
@Data
@Schema(description = "店铺会员设置")
public class ShopMemberSettingVO {

    @Schema(description = "店铺会员功能总开关；0-关闭 1-启用")
    private Integer shopMemberEnabled;

    @Schema(description = "店铺客户信息脱敏；0-关闭 1-启用（开启后店铺端客户电话/姓名脱敏）")
    private Integer shopMemberDesensitize;

    @Schema(description = "店铺添加客户；0-关闭 1-启用")
    private Integer shopMemberAllowAdd;

    @Schema(description = "店铺导入客户；0-关闭 1-启用")
    private Integer shopMemberAllowImport;
}
