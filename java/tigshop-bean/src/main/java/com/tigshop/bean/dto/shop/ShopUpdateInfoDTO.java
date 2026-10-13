package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.shop.ShopConstants.SHOP_NOT_NULL;

/**
 * 店铺信息更新参数
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺信息更新参数")
public class ShopUpdateInfoDTO extends StoreExtendedDto{

    @Schema(description = "店铺表ID")
    @NotNull(message = SHOP_NOT_NULL)
    private Integer shopId;

    @Schema(description ="店铺表名称")
    private String shopTitle;

    @Schema(description ="店铺表图片")
    private String shopLogo;
}
