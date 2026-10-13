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
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.shop.ShopConstants.*;

/**
 * 店铺表创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "店铺表参数")
public class ShopCreateDTO extends StoreExtendedDto{
    @Schema(description ="店铺表名称")
    @NotNull(message = SHOP_NOT_NULL)
    @Size(max = 50, message = SHOP_OVER_LENGTH)
    private String shopTitle;

    @Schema(description ="店铺logo/门店logo")
    private String shopLogo;

    @Schema(description ="商户表id")
    private Integer merchantId;

}
