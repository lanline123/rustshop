// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.shop.ShopConstants.SHOP_NOT_NULL;

/**
  * 店铺表更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "店铺表参数")
public class ShopUpdateDTO extends StoreExtendedDto{
    @Schema(description = "店铺表ID")
    @NotNull(message = SHOP_NOT_NULL)
    private Integer shopId;

    @Schema(description ="店铺表名称")
    private String shopTitle;

    @Schema(description ="店铺表图片")
    private String shopLogo;

    @Schema(description ="联系电话")
    private String contactMobile;
}
