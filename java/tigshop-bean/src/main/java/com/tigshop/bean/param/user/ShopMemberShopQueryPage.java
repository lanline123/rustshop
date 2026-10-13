package com.tigshop.bean.param.user;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 平台端店铺列表查询参数（店铺会员）。
 */
@Setter
@Getter
@Schema(description = "店铺会员-店铺列表查询参数")
public class ShopMemberShopQueryPage extends BasePage {

    @Schema(description = "店铺名称（模糊）")
    private String shopTitle;

    @Schema(description = "店铺类型")
    private Integer shopType;
}
