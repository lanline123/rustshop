// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.vendor.shopbind;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/7/9 10:24
 */
@Data
@Schema(description = "关联店铺列表VO")
public class VendorShopBindVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "商户数据")
    private String merchantName;

    @Schema(description = "上架商品数量")
    private Long upProductCount;

    @Schema(description = "下架商品数量")
    private Long downProductCount;

    @Schema(description = "订单量")
    private Long orderCount;

    @Schema(description = "结算金额")
    private BigDecimal orderAmount;
}
