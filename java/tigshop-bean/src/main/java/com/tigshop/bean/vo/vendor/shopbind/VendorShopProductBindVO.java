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


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/7/9 10:24
 */
@Data
@Schema(description = "关联店铺产品列表VO")
public class VendorShopProductBindVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品缩略图片")
    private String picThumb;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "产品SN")
    private String productSn;

    @Schema(description = "产品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "商品类目id")
    private Integer productCategoryId;

    @Schema(description = "商品类目名称")
    private String productCategoryName;

    @Schema(description = "商品品牌id")
    private Integer productBrandId;

    @Schema(description = "商品品牌名称")
    private String productBrandName;

    @Schema(description = "供货价")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productSupplyPrice;

    @Schema(description = "库存")
    private Integer productStock;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "销量")
    private Integer salesVolume;

    @Schema(description = "供应商商品id")
    private Long vendorProductId;

}
