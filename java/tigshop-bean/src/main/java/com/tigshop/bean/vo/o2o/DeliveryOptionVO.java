package com.tigshop.bean.vo.o2o;

import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 门店配送方式返回参数
 *
 * @author Tigshop项目组
 * @create 2025年08月22日 14:51
 */
@Data
public class DeliveryOptionVO {
    @Schema(description = "配送方式 1 物流配送，2 自提，3 同城配送")
    private Integer deliveryType;
    @Schema(description = "配送门店商品")
    private List<Product> deliveryProducts;
    @Schema(description = "自提门店商品")
    private List<PickupShopInfo> pickupProducts;
    @Schema(description = "同城配送数据商品")
    private List<ShopDeliveryData> shopDeliveryData;
    @Schema(description = "门店配送商品")
    private List<Product> shopDeliveryProducts;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PickupShopInfo {
        @Schema(description = "门店ID")
        private Integer shopId;
        @Schema(description = "门店自提点ID")
        private Set<Shop> pickups;
        @Schema(description = "属于这个店铺的商品ID")
        private List<Product> products;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShopDeliveryData {
        @Schema(description = "门店ID")
        private Integer shopId;

        @Schema(description = "距离")
        private BigDecimal distance;
        //是否超出配送范围
        @Schema(description = "是否超出配送范围")
        private Boolean isOutOfRange;
    }
}