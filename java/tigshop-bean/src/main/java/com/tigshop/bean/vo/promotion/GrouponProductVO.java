package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 拼团商品
 *
 * @author kidd
 * @since 2025/8/30 09:50
 */
@Data
public class GrouponProductVO {

    @Schema(description = "商品")
    private Product product;

    @Schema(description = "商品SKU")
    private List<ProductSkuDTO> productSkus;

    @Schema(description = "是否已加入拼团；0-否，1-是")
    private Integer isAdded;

    @Schema(description = "已存在其他拼团")
    private Boolean isOtherGrouponProduct;

    @Schema(description = "是否预约到店商品")
    private Boolean isReservationProduct;

    @Schema(description = "门店平台商品不分配给平台")
    private Boolean isO2oPlatformNotAllowPlatform;

    @Schema(description = "门店平台商品不分配给平台")
    private Boolean isPresaleProduct;

    @Schema(description = "是否秒杀商品")
    private Boolean isSeckillProduct;

    @Schema(description = "是否限时折扣商品")
    private Boolean isTimeDiscountProduct;
}
