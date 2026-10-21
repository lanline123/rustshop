package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台端店铺列表行 VO（店铺会员）。
 */
@Data
@Schema(description = "店铺会员-店铺列表行")
public class ShopMemberShopListVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺Logo")
    private String shopLogo;

    @Schema(description = "店铺状态")
    private Integer status;

    @Schema(description = "店铺类型")
    private Integer shopType;

    @Schema(description = "该店会员数")
    private Long memberCount;
}
